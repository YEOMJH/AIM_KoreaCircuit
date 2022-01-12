package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FirstGlassJob;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.MakeNotOnHoldInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class ReserveFirstGlassInfo extends SyncHandler {

	private static Log log = LogFactory.getLog(SplitFirstGlassLot.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);

		List<Element> reserveList = SMessageUtil.getBodySequenceItemList(doc, "RESERVELIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReserveFirstGlassInfo", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		eventInfo.setEventComment("ReserveFirstGlassInfo: " + eventInfo.getEventComment());

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		FirstGlassJob jobData = ExtendedObjectProxy.getFirstGlassJobService().selectByKey(false, new Object[] { jobName });

		CommonValidation.checkJobDownFlag(lotData);

		String toProcessFlowType = jobData.getToProcessFlowType(); // Sampling or FirstGlass

		String fromProcessFlowName = jobData.getProcessFlowName();
		String fromProcessFlowVersion = jobData.getProcessFlowVersion();
		String fromProcessOperationName = jobData.getProcessOperationName();
		String fromProcessOperationVersion = jobData.getProcessOperationVersion();

		String returnProcessFlowName = jobData.getReturnProcessFlowName();
		String returnProcessFlowVersion = jobData.getReturnProcessFlowVersion();
		String returnProcessOperationName = jobData.getReturnProcessOperationName();
		String returnProcessOperationVersion = jobData.getReturnProcessOperationVersion();

		List<String> allProdPositionList = new ArrayList<String>();

		if (reserveList.size() < 1)
		{
			//FIRSTGLASS-0019: Reserve FirstGlass Info doesn't exist
			throw new CustomException("FIRSTGLASS-0019");
		}

		log.info("Start ReserveFirstGlassInfo jobName : " + jobName + ", LotName : " + lotName + ", CarrierName : " + carrierName);

		for (Element reserve : reserveList)
		{
			String toProcessFlowName = SMessageUtil.getChildText(reserve, "TOPROCESSFLOWNAME", true);
			String toProcessFlowVersion = SMessageUtil.getChildText(reserve, "TOPROCESSFLOWVERSION", true);
			String toProcessOperationName = SMessageUtil.getChildText(reserve, "TOPROCESSOPERATIONNAME", true);
			String toProcessOperationVersion = SMessageUtil.getChildText(reserve, "TOPROCESSOPERATIONVERSION", true);
			String machineName = SMessageUtil.getChildText(reserve, "MACHINENAME", true);

			// FirstGlassFlow exist only one about Operation. but SampleFlow has over one.
			int flowPriority = Integer.parseInt((StringUtil.equals(toProcessFlowType, "FirstGlass")) ? "1" : SMessageUtil.getChildText(reserve, "FLOWPRIORITY", true));

			log.info("toProcessFlowName : " + toProcessFlowName + ", toProcessOperationName : " + toProcessOperationName + ", machineName" + machineName + ", flowPriority" + flowPriority);

			List<Element> reserveProdList = SMessageUtil.getSubSequenceItemList(reserve, "RESERVEPRODLIST", true);

			List<String> actualSamplePositionList = new ArrayList<String>();

			for (Element reserveProd : reserveProdList)
			{
				String position = SMessageUtil.getChildText(reserveProd, "POSITION", true);

				actualSamplePositionList.add(position);

				if (!allProdPositionList.contains(position))
					allProdPositionList.add(position);
			}

			actualSamplePositionList = CommonUtil.sortActualSamplePosition(actualSamplePositionList);

			for (Element reserveProd : reserveProdList)
			{
				String productName = SMessageUtil.getChildText(reserveProd, "PRODUCTNAME", true);
				String position = SMessageUtil.getChildText(reserveProd, "POSITION", true);

				boolean isFirstGlassFlow = MESLotServiceProxy.getLotServiceUtil().isSampleFlow(lotData.getFactoryName(), toProcessFlowName, toProcessFlowVersion);

				if (!isFirstGlassFlow)
					throw new CustomException("FIRSTGLASS-0003", toProcessFlowName);

				// 1. set Sampling Lot Data
				// get SampleLotData(CT_SAMPLELOT)
				List<SampleLot> sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataList(lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
						lotData.getProductSpecVersion(), fromProcessFlowName, fromProcessFlowVersion, fromProcessOperationName, fromProcessOperationVersion, machineName, toProcessFlowName,
						toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

				// if alreay exist, remove sampling Lot in CT_SAMPLELOT, CT_SAMPLEPRODUCT Table
				if (sampleLot != null)
				{
					// if exist, delete previous SamplingProduct Data(CT_SAMPLEPRODUCT)
					ExtendedObjectProxy.getSampleProductService().deleteSampleProductByLotName(eventInfo, lotName, lotData.getFactoryName(), lotData.getProductSpecName(),
							lotData.getProductSpecVersion(), fromProcessFlowName, fromProcessFlowVersion, fromProcessOperationName, fromProcessOperationVersion, machineName, toProcessFlowName,
							toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);

					ExtendedObjectProxy.getSampleLotService().deleteSampleLotWithOutMachineName(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), lotData.getProductSpecName(),
							lotData.getProductSpecVersion(), fromProcessFlowName, fromProcessFlowVersion, fromProcessOperationName, fromProcessOperationVersion, toProcessFlowName,
							toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion);
				}

				log.info("Insert SampleProduct : " + productName + ", Position : " + position);

				// set SamplingProduct Data(CT_SAMPLEPRODUCT)
				ExtendedObjectProxy.getSampleProductService().insertSampleProduct(eventInfo, productName, lotName, lotData.getFactoryName(), lotData.getProductSpecName(),
						lotData.getProductSpecVersion(), fromProcessFlowName, fromProcessFlowVersion, fromProcessOperationName, fromProcessOperationVersion, machineName, toProcessFlowName,
						toProcessFlowVersion, toProcessOperationName, toProcessOperationVersion, "Y", String.valueOf(actualSamplePositionList.size()),
						CommonUtil.toStringWithoutBrackets(actualSamplePositionList), position, "", "Y");
			}

			log.info("Insert SampleLot : " + lotName);
			// set SampleLotData(CT_SAMPLELOT)
			ExtendedObjectProxy.getSampleLotService().insertSampleLot(eventInfo, lotName, lotData.getFactoryName(), lotData.getProductSpecName(), lotData.getProductSpecVersion(), fromProcessFlowName,
					fromProcessFlowVersion, fromProcessOperationName, fromProcessOperationVersion, machineName, toProcessFlowName, toProcessFlowVersion, toProcessOperationName,
					toProcessOperationVersion, "Y", "1", "1", "1", "1", CommonUtil.toStringWithoutBrackets(actualSamplePositionList), String.valueOf(actualSamplePositionList.size()),
					CommonUtil.toStringWithoutBrackets(actualSamplePositionList), "Y", "", flowPriority, returnProcessFlowName, returnProcessFlowVersion, returnProcessOperationName,
					returnProcessOperationVersion, "");
		}

		if (lotData.getLotHoldState().equals(GenericServiceProxy.getConstantMap().Lot_OnHold))
		{
			List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
			MakeNotOnHoldInfo makeNotOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeNotOnHoldInfo(lotData, productUSequence, new HashMap<String, String>());
			lotData = LotServiceProxy.getLotService().makeNotOnHold(lotData.getKey(), eventInfo, makeNotOnHoldInfo);

			// delete in LOTMULTIHOLD table
			MESLotServiceProxy.getLotServiceUtil().releaseMultiHold(lotName, "FirstGlassHold", jobData.getProcessOperationName());

			// delete in PRODUCTMULTIHOLD table
			MESProductServiceProxy.getProductServiceImpl().releaseProductMultiHold(lotName, "FirstGlassHold", jobData.getProcessOperationName());

			// setHoldState
			MESLotServiceProxy.getLotServiceUtil().setHoldState(eventInfo, lotData);
		}

		// Release Job
		if (StringUtil.equals(jobData.getJobState(), GenericServiceProxy.getConstantMap().FIRSTGLASS_JOBSTATE_RESERVED))
		{
			allProdPositionList = CommonUtil.sortActualSamplePosition(allProdPositionList);

			eventInfo.setEventName("Release");
			jobData.setJobState(GenericServiceProxy.getConstantMap().Lot_Released);

			if (StringUtils.equals(lotData.getUdfs().get("FIRSTGLASSALL"), "Y"))
				jobData.setFirstGlassAllPosition(CommonUtil.toStringWithoutBrackets(allProdPositionList));

			ExtendedObjectProxy.getFirstGlassJobService().modify(eventInfo, jobData);
		}

		// Reserve Future Hold
		eventInfo.setEventName("InsertFutureHold");
		eventInfo.setEventComment("ReserveOper:" + lotData.getProcessOperationName() + "." + eventInfo.getEventComment());

		String beforeActionComment = eventInfo.getEventComment();
		String beforeActionUser = eventInfo.getEventUser();

		MESLotServiceProxy.getLotServiceImpl().insertCtLotFutureMultiHoldActionForAfter(eventInfo, lotData.getKey().getLotName(), lotData.getFactoryName(), jobData.getReturnProcessFlowName(),
				jobData.getReturnProcessFlowVersion(), jobData.getReturnProcessOperationName(), jobData.getReturnProcessOperationVersion(), "0", "hold", "System", "FirstGlass", "FirstGlassHold", "",
				"", "", "True", "False", beforeActionComment, "", beforeActionUser, "", "Insert", "", "");

		return doc;
	}
}
