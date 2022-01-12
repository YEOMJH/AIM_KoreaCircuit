package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.lot.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.jdom.Document;
import org.jdom.Element;

public class SplitFirstGlassLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);

		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		List<Element> flowList = SMessageUtil.getBodySequenceItemList(doc, "OPERATIONLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		eventInfo.setEventComment("SplitFirstGlassLot: " + eventInfo.getEventComment());

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		CommonValidation.checkJobDownFlag(lotData);

		List<Product> producList = LotServiceProxy.getLotService().allProducts(lotName);
		// check ProcessInfo
		List<String> productNameList = new ArrayList<>();
		for (Product productA : producList)
		{
			String productName = productA.getKey().getProductName();
			productNameList.add(productName);
		}
		CommonValidation.checkProductProcessInfobyString(productNameList);

		// QTY validation
		if (productList.size() < 1)
			throw new CustomException("FIRSTGLASS-0001");

		FirstGlassJob jobData = ExtendedObjectProxy.getFirstGlassJobService().selectByKey(false, new Object[] { jobName });

		ExtendedObjectProxy.getFirstGlassJobService().validateJobState(jobData, "Reserved", "Released");

		if (StringUtil.equals(jobData.getJudge(), "Pass"))
			throw new CustomException("FIRSTGLASS-0009", jobData.getJobName());

		// Lot's Operation[{0}] is unable to Split. Job's Operation is [{0}]
		if (!lotData.getProcessOperationName().equals(jobData.getProcessOperationName()))
			throw new CustomException("FIRSTGLASS-0004", lotData.getProcessOperationName(), jobData.getProcessOperationName());

		// Check exist splitLot(ChildLot) that is not Complete ( if FirstGlassFlag is 'Y' , the splitLot was processed in FirstGlass Flow. )
		try
		{
			List<Lot> lotList = LotServiceProxy.getLotService().select(" PARENTLOTNAME =  ? AND FIRSTGLASSFLAG = 'N' AND LOTSTATE = 'Released' ", new Object[] { lotName });
			Lot previousChildLotData = lotList.get(0);

			throw new CustomException("FIRSTGLASS-0002", lotName, previousChildLotData.getKey().getLotName());
		}
		catch (NotFoundSignal ne)
		{
			// Not Exist. do split
		}

		// For Split lot, Release Hold Mother Lot
		if (lotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Lot_OnHold))
		{
			eventInfo.setEventName("ReleaseHold");

			List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
			MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(lotData, productUSequence, new HashMap<String, String>());
			lotData = LotServiceProxy.getLotService().makeNotOnHold(lotData.getKey(), eventInfo, makeNotOnHoldInfo);

			// delete in LOTMULTIHOLD table
			MESLotServiceProxy.getLotServiceUtil().releaseMultiHold(lotName, "FirstGlassHold", jobData.getProcessOperationName());
			// delete in PRODUCTMULTIHOLD table
			MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(lotName, "FirstGlassHold", jobData.getProcessOperationName());

			// Add 20191106 for SripCase
			// delete in LOTMULTIHOLD table
			MESLotServiceProxy.getLotServiceUtil().releaseMultiHold(lotName, "StripHOLD", jobData.getProcessOperationName());
			// delete in PRODUCTMULTIHOLD table
			MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(lotName, "StripHOLD", jobData.getProcessOperationName());

			// setHoldState
			MESLotServiceProxy.getLotServiceUtil().setHoldState(eventInfo, lotData);
		}

		Lot childLotData = MESLotServiceProxy.getLotServiceUtil().splitFirstGlassLot(eventInfo, productList, lotData, jobName);

		MESLotServiceProxy.getLotServiceUtil().reserveFirstGlass(eventInfo, jobData, childLotData, flowList);

		// Mother Lot Hold
		if (lotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Lot_NotOnHold))
		{
			lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());

			if (lotData.getProductQuantity() > 0)
			{
				// Mother Lot Hold
				eventInfo.setReasonCode("FirstGlassHold");
				eventInfo.setReasonCodeType("FirstGlass");

				MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, lotData, new HashMap<String, String>());
			}
			else
			{
				// Make Emptied when All Child Lot Split
				if (StringUtil.isNotEmpty(lotData.getCarrierName()))
				{
					Durable sLotDurableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());
					DeassignCarrierInfo deassignCarrierInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, sLotDurableData, new ArrayList<ProductU>());

					// De-assign Carrier
					eventInfo.setEventName("DeassignCarrier");
					MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, deassignCarrierInfo, eventInfo);
				}

				eventInfo.setEventName("MakeEmptied");
				MESLotServiceProxy.getLotServiceImpl().MakeEmptied(eventInfo, lotData, new ArrayList<ProductU>(), new HashMap<String, String>());
			}
		}

		eventInfo.setReasonCode("");
		eventInfo.setReasonCodeType("");

		// Release Job
		if (StringUtil.equals(jobData.getJobState(), GenericServiceProxy.getConstantMap().FIRSTGLASS_JOBSTATE_RESERVED))
		{
			eventInfo.setEventName("Release");
			jobData.setJobState(GenericServiceProxy.getConstantMap().Lot_Released);
			ExtendedObjectProxy.getFirstGlassJobService().modify(eventInfo, jobData);
		}

		// Reserve ChildLot Future Hold
		eventInfo.setEventName("InsertFutureHold");
		eventInfo.setEventComment("ReserveOper:" + childLotData.getProcessOperationName() + "." + eventInfo.getEventComment());

		String beforeActionComment = eventInfo.getEventComment();
		String beforeActionUser = eventInfo.getEventUser();

		MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, childLotData.getKey().getLotName(), childLotData.getFactoryName(),
				jobData.getReturnProcessFlowName(), jobData.getReturnProcessFlowVersion(), jobData.getReturnProcessOperationName(), jobData.getReturnProcessOperationVersion(), "0", "hold", "System",
				"FirstGlass", "FirstGlassHold", "", "", "", "True", "False", beforeActionComment, "", beforeActionUser, "", "Insert", "", "");

		XmlUtil.setChildText(SMessageUtil.getBodyElement(doc), "CHILDLOTNAME", childLotData.getKey().getLotName());

		return doc;
	}

}