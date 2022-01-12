package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DSPProductRequestPlan;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.data.PortSpec;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processoperationspec.ProcessOperationSpecServiceProxy;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpecKey;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class TrackOutForTP extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		Element bodyElement = SMessageUtil.getBodyElement(doc);

		List<String> srcLotList = new ArrayList<String>();
		List<String> destLotList = new ArrayList<String>();

		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String lotJudge = SMessageUtil.getBodyItemValue(doc, "LOTJUDGE", false);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);

		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		List<String> productNameList = CommonUtil.makeList(bodyElement, "PRODUCTLIST", "PRODUCTNAME");

		// Get Port Data
		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		PortSpec portSpecData = MESPortServiceProxy.getPortServiceUtil().getPortSpecInfo(machineName, portName);

		// Get Lot, Machine Data
		Machine eqpData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Lot baseLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		// for common
		EventInfo eventInfo = EventInfoUtil.makeEventInfo_TrackInOutWithoutHistory("", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventComment("TrackOutLot");

		// TK-OUT Lot ID
		String newLotName = "";

		// Validation
		CommonValidation.checkMachineHold(eqpData);
		CommonValidation.checkOriginalProduct(baseLotData);

		MESLotServiceProxy.getLotServiceUtil().checkLotValidation(baseLotData);

		Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
		Map<String, String> assignCarrierUdfs = new HashMap<String, String>();

		Lot trackOutLot = new Lot();
		srcLotList.add(lotName);

		Map<String, String> udfs = portData.getUdfs();
		udfs.put("PORTTYPE", portSpecData.getPortType());
		portData.setUdfs(udfs);

		trackOutLot = MESLotServiceProxy.getLotInfoUtil().getLotData(baseLotData.getKey().getLotName());
		
		// refined Lot logged in
		Lot beforeTrackOutLot = (Lot) ObjectUtil.copyTo(trackOutLot);

		List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(bodyElement);

		if (baseLotData.getUdfs().get("PORTTYPE").equals("PB"))
		{
			if (trackOutLot.getProductQuantity() != productPGSRCSequence.size())
				throw new CustomException("LOT-1011");
		}

		// Judge Lot Grade
		lotJudge = CommonUtil.judgeLotGradeByProductList(productList, "PRODUCTJUDGE");

		ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(trackOutLot);

		List<SampleLot> sampleLotList = new ArrayList<SampleLot>();

		if (!StringUtils.equals(processFlowData.getProcessFlowType(), "MQC"))
		{
			// if lot is FirstGlass child Lot, NG
			if (!StringUtils.isEmpty(trackOutLot.getUdfs().get("FIRSTGLASSFLAG")) && !StringUtils.isEmpty(trackOutLot.getUdfs().get("JOBNAME")))
			{
				if (trackOutLot.getUdfs().get("FIRSTGLASSFLAG").equals("N") && lotJudge.equals("N")) // delete sample lot/product
					MESLotServiceProxy.getLotServiceUtil().deleteSampleFirstGlass(eventInfo, trackOutLot.getUdfs().get("JOBNAME"), trackOutLot);
			}

			sampleLotList = MESLotServiceProxy.getLotServiceUtil().deleteSamplingDataReturn(eventInfo, trackOutLot, productList, true);
		}

		// Check complete TPTJ Product Data.
		MESLotServiceProxy.getLotServiceUtil().deleteTPTJProductData(eventInfo, trackOutLot); // TPTJCase

		// Set Data(MainReserveSkip) New Lot - Lot AR-AMF-0030-01
		ExtendedObjectProxy.getMainReserveSkipService().syncMainReserveSkip(eventInfo, srcLotList, destLotList);

		// Set SamplingData
		if (StringUtils.isEmpty(trackOutLot.getUdfs().get("FIRSTGLASSFLAG")))
		{
			if (StringUtils.isEmpty(trackOutLot.getUdfs().get("FIRSTGLASSALL")))
			{
				MESLotServiceProxy.getLotServiceUtil().setSamplingListData(eventInfo, trackOutLot, eqpData, productList); // Set Sampling Data
			}
		}

		// InlineSampling - Delete InlineSampling Data
		MESLotServiceProxy.getLotServiceUtil().deleteInlineSamplingData(eventInfo, trackOutLot, productList, machineName, true);

		// TK OUT
		Lot afterTrackOutLot;

		// Make Logged Out
		afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().trackOutLotWithSampling(eventInfo, trackOutLot, portData, carrierName, lotJudge, machineName, "", productPGSRCSequence,
					assignCarrierUdfs, deassignCarrierUdfs, new HashMap<String, String>());
		

		// Delete before operation sampling data all
		eventLog.info("Start delete before operation sampling data all");
		ProcessOperationSpecKey specKey = new ProcessOperationSpecKey(afterTrackOutLot.getFactoryName(), afterTrackOutLot.getProcessOperationName(), afterTrackOutLot.getProcessOperationVersion());
		ProcessOperationSpec afterOperSpec = ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(specKey);

		eventLog.info("ProcessOperationSpecData");
		eventLog.info("ProcessOperation - " + afterOperSpec.getKey().getProcessOperationName());
		eventLog.info("ProcessOperationType - " + afterOperSpec.getProcessOperationType());
		eventLog.info("IsMainOperation - " + afterOperSpec.getUdfs().get("ISMAINOPERATION").toString());
		eventLog.info("ProcessOperationGroup - " + afterOperSpec.getProcessOperationGroup());

		if (afterOperSpec.getProcessOperationType().equals("Production") && afterOperSpec.getUdfs().get("ISMAINOPERATION").toString().equals("Y")
				&& afterOperSpec.getProcessOperationGroup().equals("Normal"))
		{
			MESLotServiceProxy.getLotServiceUtil().deleteBeforeOperationSamplingData(eventInfo, afterTrackOutLot);
		}

		eventLog.info("End delete before operation sampling data all");

		// Set Data(Sample, FutureAction) Transfer Product
		MESLotServiceProxy.getLotServiceUtil().transferProductSyncData(eventInfo, srcLotList, destLotList);

		// Skip
		afterTrackOutLot = MESLotServiceProxy.getLotServiceUtil().executePostAction(eventInfo, beforeTrackOutLot, afterTrackOutLot, false);

		// return NewLot
		Document rtnDoc = new Document();
		rtnDoc = (Document) doc.clone();
		rtnDoc = SMessageUtil.addItemToBody(rtnDoc, "NEWLOTNAME", newLotName);

		updateReservedLotState(lotName, machineName, beforeTrackOutLot, eventInfo);

		return rtnDoc;
	}

	
	public void SetFlagStack(List<ProductPGSRC> productPGSRCSequence, Lot lotData)
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT DISTINCT FLAG ");
		sql.append("  FROM CT_OPERATIONFLAGCONDITION ");
		sql.append(" WHERE FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND MACHINENAME = :MACHINENAME ");
		sql.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");

		Map<String, Object> args = new HashMap<>();

		args.put("FACTORYNAME", lotData.getFactoryName());
		args.put("MACHINENAME", lotData.getMachineName());
		args.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		args.put("PROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			String flag = ConvertUtil.getMapValueByName(result.get(0), "FLAG");
			String sql2 = "SELECT DISTINCT DELETEFLAG FROM CT_OPERATIONFLAGCONDITION WHERE MACHINENAME = :MACHINENAME AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ";

			Map<String, Object> args2 = new HashMap<>();
			args2.put("MACHINENAME", lotData.getMachineName());
			args2.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());

			List<Map<String, Object>> result2 = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql2, args2);

			for (ProductPGSRC productPGSRC : productPGSRCSequence)
			{
				String[] flagArray = StringUtils.split(productPGSRC.getUdfs().get("FLAGSTACK"), ",");

				List<String> flagList = new ArrayList<String>();
				flagList.add(flag);

				for (int i = 0; i < flagArray.length; i++)
				{
					String tempFlag = flagArray[i];

					if (StringUtils.isNotEmpty(tempFlag) && !flagList.contains(tempFlag))
						flagList.add(tempFlag);
				}

				for (Map<String, Object> map : result2)
				{
					String deleteFlag = ConvertUtil.getMapValueByName(map, "DELETEFLAG");

					if (flagList.contains(deleteFlag))
						flagList.remove(deleteFlag);
				}

				String flagStack = StringUtils.join(flagList, ",");

				productPGSRC.getUdfs().put("FLAGSTACK", flagStack.toString());
			}
		}
	}

	private void updateReservedLotState(String lotName, String machineName, Lot lotData, EventInfo eventInfo) throws CustomException
	{
		try
		{
			String condition = "machineName = ? and lotName =? and productSpecName =? and processOperationName =? and productRequestName =? and reserveState = ? ";
			Object bindSet[] = new Object[] { machineName, lotName, lotData.getProductSpecName(), lotData.getProcessOperationName(), lotData.getProductRequestName(), "Executing" };
			List<ReserveLot> reserveLot = ExtendedObjectProxy.getReserveLotService().select(condition, bindSet);

			reserveLot.get(0).setReserveState(GenericServiceProxy.getConstantMap().RESV_STATE_END);
			reserveLot.get(0).setCompleteTimeKey(eventInfo.getEventTimeKey());

			ExtendedObjectProxy.getReserveLotService().modify(eventInfo, reserveLot.get(0));

			condition = "productRequestName = ? and productSpecName = ? and processFlowName = ? and processOperationName = ? and  machineName = ? and planDate = ? ";
			bindSet = new Object[] { lotData.getProductRequestName(), lotData.getProductSpecName(), lotData.getProcessFlowName(), lotData.getProcessOperationName(), machineName,
					reserveLot.get(0).getPlanDate() };
			List<DSPProductRequestPlan> productRequestPlan = ExtendedObjectProxy.getDSPProductRequestPlanService().select(condition, bindSet);

			long remainLotQty = productRequestPlan.get(0).getPlanLotQuantity() - 1;

			if (remainLotQty == 0)
				productRequestPlan.get(0).setPlanState("Completed");

			productRequestPlan.get(0).setPlanLotQuantity(remainLotQty);
			ExtendedObjectProxy.getDSPProductRequestPlanService().modify(eventInfo, productRequestPlan.get(0));
		}
		catch (Exception e)
		{
			eventLog.info("Fail ReservedLot Updating");
		}
	}

	public void AOILotJudge(Lot baseLotData, String lotName)
	{

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT M.MACHINEGROUPNAME ");
		sql.append("  FROM MACHINESPEC M ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND M.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND M.DETAILMACHINETYPE = 'MAIN' ");
		sql.append("   AND M.MACHINENAME = :MACHINENAME ");
		sql.append("ORDER BY M.MACHINENAME ASC ");

		Map<String, Object> args = new HashMap<>();

		args.put("FACTORYNAME", baseLotData.getFactoryName());
		args.put("MACHINENAME", baseLotData.getMachineName());

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), args);

		if (result.get(0).toString().equals("{MACHINEGROUPNAME=AutoMAC}") || result.get(0).toString().equals("{MACHINEGROUPNAME=ParticleCounter}"))
		{
			StringBuilder insql = new StringBuilder();
			insql.append("INSERT INTO CT_AOILOT (LOTNAME, FACTORYNAME, LOTJUDGE, EVENTUSER, EVENTTIME, TIMEKEY, MACHINENAME) ");
			insql.append("VALUES (:LOTNAME, :FACTORYNAME, 'N', :EVENTUSER, :EVENTTIME, :TIMEKEY, :MACHINENAME) ");

			String CurrentTime = TimeStampUtil.getCurrentTime(TimeStampUtil.FORMAT_DEFAULT);
			String timeKey = TimeStampUtil.getCurrentEventTimeKey();

			Map<String, Object> arg = new HashMap<String, Object>();
			arg.put("LOTNAME", lotName);
			arg.put("FACTORYNAME", baseLotData.getFactoryName());
			arg.put("MACHINENAME", baseLotData.getMachineName());
			arg.put("EVENTUSER", baseLotData.getLastEventUser());
			arg.put("EVENTTIME", CurrentTime);
			arg.put("TIMEKEY", timeKey);

			GenericServiceProxy.getSqlMesTemplate().update(insql.toString(), arg);

			SetEventInfo setEventInfo = new SetEventInfo();
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("AutoLotJudge-N", getEventUser(), getEventComment(), "", "");
			LotServiceProxy.getLotService().setEvent(baseLotData.getKey(), eventInfo, setEventInfo);
		}
	}
}
