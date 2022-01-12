package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassJob;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.event.EventInfoExtended;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.MakeNotInReworkInfo;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.jdom.Document;
import org.jdom.Element;

public class CompleteRework extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String returnFlowName = SMessageUtil.getBodyItemValue(doc, "RETURNFLOWNAME", true);
		String returnOperationName = SMessageUtil.getBodyItemValue(doc, "RETURNOPERATION", true);
		String returnOperationVer = SMessageUtil.getBodyItemValue(doc, "RETURNOPERATIONVER", false);
		String reworkUser = SMessageUtil.getBodyItemValue(doc, "REWORKUSER", false);
		if (!StringUtil.isNotEmpty(returnOperationVer))
			returnOperationVer = "00001";

		Element element = doc.getDocument().getRootElement();

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		Lot oldlotData = (Lot) ObjectUtil.copyTo(lotData);

		List<Product> producList = LotServiceProxy.getLotService().allProducts(lotName);

		// check ProcessInfo
		List<String> productNameList = new ArrayList<>();

		for (Product productA : producList)
		{
			String productName = productA.getKey().getProductName();
			productNameList.add(productName);
		}

		CommonValidation.checkProductProcessInfobyString(productNameList);
		CommonValidation.checkLotProcessStateWait(lotData);
		CommonValidation.checkJobDownFlag(lotData);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CompleteRework", getEventUser(), getEventComment(), "", "");

		// migration from FutureAction to SampleData, delete SampleData
		MESLotServiceProxy.getLotServiceUtil().deleteSamplingForCompleteRework(eventInfo, lotData, new ArrayList<Element>(), true);

		//Map<String, String> udfs = MESLotServiceProxy.getLotServiceUtil().setNamedValueSequence(lotName, element);
		Map<String, String> udfs = new HashMap<String, String>();
		List<ProductU> productU = MESLotServiceProxy.getLotServiceUtil().setProductUSequence(doc);

		//beforeOperationName = MESLotServiceProxy.getLotServiceUtil().getBeforeOperationName(returnFlowName, returnOperationName);
		udfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
		udfs.put("BEFOREFLOWNAME", lotData.getProcessFlowName());

		ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

		MakeNotInReworkInfo makeNotInReworkInfo = MESLotServiceProxy.getLotInfoUtil().makeNotInReworkInfo_CompleteRework(lotData, eventInfo, lotName, returnFlowName, returnOperationName, returnOperationVer, udfs, productU);

		MESLotServiceProxy.getLotServiceImpl().completeRework(eventInfo, lotData, makeNotInReworkInfo);

		// delete ReturnInformation
		Lot afterLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotData.getKey().getLotName());

		processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(afterLotData);

		if (processFlowData.getProcessFlowType().equals("Main"))
			MESLotServiceProxy.getLotServiceImpl().deleteLotReturnInformation(afterLotData);

		// recover Lot & Product judge
		try
		{
			eventInfo.setEventName("ChangeGrade");
			MESLotServiceProxy.getLotServiceUtil().recoverLotGrade(eventInfo, afterLotData, "R", "G");
		}
		catch (Exception ex)
		{
			eventLog.warn("Judge recovery is failed");
		}

		// recover Product Processing Info
		try
		{
			this.recoverProcessingInfo(eventInfo, afterLotData);
		}
		catch (Exception ex)
		{
			eventLog.warn("ProcessingFlag recovery is failed");
		}

		// additional MQC recycling
		try
		{
			ExtendedObjectProxy.getMQCPlanService().makeWaiting(eventInfo, afterLotData);
		}
		catch (Exception ex)
		{
			eventLog.warn("post MQC processing is failed");
		}

		// For FirstGlass Child Lot Hold
		try
		{
			Object[] bindSet = new Object[] { afterLotData.getUdfs().get("JOBNAME") };
			FirstGlassJob jobData = ExtendedObjectProxy.getFirstGlassJobService().selectByKey(false, bindSet);

			if (jobData != null)
			{
				afterLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(afterLotData.getKey().getLotName());
				MESLotServiceProxy.getLotServiceUtil().executePostAction(eventInfo, lotData, afterLotData, false);
			}
		}
		catch (Exception ex)
		{
			eventLog.info("This Lot is not FirstGlass");
		}

		if (lotData.getFactoryName().equals("ARRAY") || lotData.getFactoryName().equals("TP"))
		{
			EventInfoExtended CompleteReworkHoldEventInfo = new EventInfoExtended(eventInfo);
			CompleteReworkHoldEventInfo.setEventComment("ManualCompleteReworkAutoHold + " + getEventComment());

			MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(CompleteReworkHoldEventInfo, lotName, lotData.getFactoryName(), returnFlowName, "00001",
					returnOperationName, returnOperationVer, "0", "hold", "System", "ReserveHoldLot", "ManualCompleteReworkAutoHold", "", "", "", "True", "False",
					CompleteReworkHoldEventInfo.getEventComment(), "",StringUtil.isNotEmpty(reworkUser)?reworkUser:getEventUser() , "", "Insert", "", "");
		}

		afterLotData = MESLotServiceProxy.getLotServiceUtil().executePostAction(eventInfo, oldlotData, afterLotData, false);

		//Hold
		//makeOnHoldForCompleteRework(eventInfo, afterLotData);
		
		return doc;
	}

	private void recoverProcessingInfo(EventInfo eventInfo, Lot lotData) throws CustomException
	{
		List<Product> productList = ProductServiceProxy.getProductService().select("lotName = ? AND productState = ? AND processingInfo = ?",
				new Object[] { lotData.getKey().getLotName(), GenericServiceProxy.getConstantMap().Prod_InProduction, "B" });

		for (Iterator<Product> iteratorProduct = productList.iterator(); iteratorProduct.hasNext();)
		{
			Product product = iteratorProduct.next();

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PROCESSINGINFO", "S"); // StringUtil.EMPTY);
			bindMap.put("PRODUCTNAME", product.getKey().getProductName());

			StringBuffer updatesql = new StringBuffer();
			updatesql.append("UPDATE PRODUCT ");
			updatesql.append("   SET PROCESSINGINFO = :PROCESSINGINFO ");
			updatesql.append(" WHERE PRODUCTNAME = :PRODUCTNAME ");
			
			GenericServiceProxy.getSqlMesTemplate().update(updatesql.toString(), bindMap);

			StringBuffer updatesq2 = new StringBuffer();
			updatesq2.append("UPDATE PRODUCTHISTORY ");
			updatesq2.append("   SET PROCESSINGINFO = :PROCESSINGINFO ");
			updatesq2.append(" WHERE PRODUCTNAME = :PRODUCTNAME ");
			updatesq2.append("   AND TIMEKEY = (SELECT LASTEVENTTIMEKEY ");
			updatesq2.append("                    FROM PRODUCT ");
			updatesq2.append("                   WHERE PRODUCTNAME = :PRODUCTNAME) ");
			
			GenericServiceProxy.getSqlMesTemplate().update(updatesq2.toString(), bindMap);
		}
	}
	
	private void makeOnHoldForCompleteRework (EventInfo eventInfo, Lot lotData) throws CustomException
	{
		eventInfo.setEventName("Hold");
		eventInfo.setReasonCode("SYSTEM");
		eventInfo.setEventComment("Complete Rework [" + lotData.getKey().getLotName() + "], " + eventInfo.getEventComment());
		
		Map<String, String> udfs = new HashMap<String, String>();
		
		if (StringUtil.equals(lotData.getLotState(), GenericServiceProxy.getConstantMap().Lot_Released) && StringUtil.equals(lotData.getLotProcessState(), "WAIT"))
		{
			MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, lotData, udfs);
		}
		else
		{
			throw new CustomException("LOT-0113", lotData.getLotState(), lotData.getLotProcessState());
		}
	}
}
