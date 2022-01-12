package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskMQCPlan;
import kr.co.aim.messolution.extended.object.management.data.MaskMQCPlanDetail;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;

import org.jdom.Document;
import org.jdom.Element;

public class CompleteMaskMQCPlan extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String returnMaskFlowName = SMessageUtil.getBodyItemValue(doc, "RETURNMASKFLOWNAME", true);
		String returnMaskFlowVersion = SMessageUtil.getBodyItemValue(doc, "RETURNMASKFLOWVERSION", true);
		String returnMaskOperation = SMessageUtil.getBodyItemValue(doc, "RETURNMASKOPERATION", true);
		String returnOperationVersion = SMessageUtil.getBodyItemValue(doc, "RETURNOPERATIONVERSION", true);
		// String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CompleteMaskMQC", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		List<Element> maskLotList = SMessageUtil.getBodySequenceItemList(doc, "MASKLOTLIST", true);

		for (Element maskLotE : maskLotList)
		{
			String maskLotName = maskLotE.getChildText("MASKLOTNAME");
			MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });

			String jobName = getMQCJobName(maskLotName);
			MaskMQCPlan planData = ExtendedObjectProxy.getMaskMQCPlanService().selectByKey(false, new Object[] { jobName });

			// MaskLotProcessState Check
			if (maskLotData.getMaskLotProcessState().equalsIgnoreCase(GenericServiceProxy.getConstantMap().MaskLotProcessState_Run))
				throw new CustomException("OLEDMASK-0002", new Object[] { GenericServiceProxy.getConstantMap().MaskLotProcessState_Run + "(" + maskLotName + ")" });

			// MaskLotHoldState Check
			if (StringUtil.equals(maskLotData.getMaskLotHoldState(), GenericServiceProxy.getConstantMap().MaskLotHoldState_OnHold))
				throw new CustomException("MASK-0013", maskLotData.getMaskLotName());

			// MaskLotState Check
			if (!StringUtil.equals(maskLotData.getMaskLotState(), GenericServiceProxy.getConstantMap().MaskLotState_Released))
				throw new CustomException("MASK-0026", maskLotData.getMaskLotName(), maskLotData.getMaskLotState());

//			if (StringUtil.isEmpty(maskLotData.getCarrierName()))
//				throw new CustomException("MASK-0045", maskLotName);

			List<MaskMQCPlanDetail> MaskMQCPlanDetail;

			try
			{
				MaskMQCPlanDetail = ExtendedObjectProxy.getMaskMQCPlanDetailService().select("jobName = ?", new Object[] { planData.getJobName() });
			}
			catch (Exception ex)
			{
				eventLog.error("No details for this MQC plan");
				MaskMQCPlanDetail = new ArrayList<MaskMQCPlanDetail>();
			}

			// purge dependent plans
			for (MaskMQCPlanDetail MaskPlanDetail : MaskMQCPlanDetail)
			{
				try
				{
					MaskPlanDetail.setLastEventComment(eventInfo.getEventComment());
					MaskPlanDetail.setLastEventName(eventInfo.getEventName());
					MaskPlanDetail.setLastEventTime(eventInfo.getEventTime());
					MaskPlanDetail.setLastEventUser(eventInfo.getEventUser());

					ExtendedObjectProxy.getMaskMQCPlanDetailService().remove(eventInfo, MaskPlanDetail);

				}
				catch (Exception ex)
				{
					eventLog.error(String.format("DetailPlan[%s, %s] MQC plan not removed yet", MaskPlanDetail.getMaskProcessFlowName(), MaskPlanDetail.getMaskProcessOperationName()));
				}
			}

			planData.setLastEventComment(eventInfo.getEventComment());
			planData.setLastEventName(eventInfo.getEventName());
			planData.setLastEventTime(eventInfo.getEventTime());
			planData.setLastEventUser(eventInfo.getEventUser());
			planData.setMQCState("Suspending");

			ExtendedObjectProxy.getMaskMQCPlanService().remove(eventInfo, planData);

			String targetFlowName = "";
			String targetFlowVersion = "";
			String targetOperationName = "";
			String targetOperationVer = "";
			String targetNodeStack = "";
			
			Node returnNode = ProcessFlowServiceProxy.getProcessFlowService().getNode(factoryName, returnMaskFlowName, returnMaskFlowVersion, returnMaskOperation, returnOperationVersion);

			targetFlowName = returnNode.getProcessFlowName();
			targetFlowVersion = returnNode.getProcessFlowVersion();
			targetOperationName = returnNode.getNodeAttribute1();
			targetOperationVer = returnNode.getNodeAttribute2();
			targetNodeStack = new StringBuilder(returnNode.getKey().getNodeId()).toString();

			// move to Return flow
			try
			{
				// Set Event Info
				eventInfo.setEventName("CompleteMaskMQC");
				maskLotData.setMaskProcessFlowName(targetFlowName);
				maskLotData.setMaskProcessFlowVersion(targetFlowVersion);
				maskLotData.setMaskProcessOperationName(targetOperationName);
				maskLotData.setMaskProcessOperationVersion(targetOperationVer);
				maskLotData.setNodeStack(targetNodeStack);
				maskLotData.setLastEventName(eventInfo.getEventName());
				maskLotData.setLastEventTime(eventInfo.getEventTime());
				maskLotData.setLastEventTimeKey(eventInfo.getLastEventTimekey());
				maskLotData.setLastEventUser(eventInfo.getEventUser());
				maskLotData.setLastEventComment(eventInfo.getEventComment());

				ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);
			}
			catch (Exception e)
			{
			}

		}

		return doc;
	}

	private String getMQCJobName(String maskLotName) throws CustomException
	{
		String jobName = "";

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT M.JOBNAME, ");
		sql.append("       M.MASKLOTNAME, ");
		sql.append("       M.MASKPROCESSFLOWNAME, ");
		sql.append("       M.MASKPROCESSFLOWVERSION, ");
		sql.append("       M.MQCSTATE, ");
		sql.append("       M.FACTORYNAME, ");
		sql.append("       M.CREATEUSER ");
		sql.append("  FROM CT_MASKMQCPLAN M, CT_MASKLOT L, CT_MASKMQCPLANDETAIL MD ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND M.FACTORYNAME = L.FACTORYNAME ");
		sql.append("   AND M.JOBNAME = MD.JOBNAME ");
		sql.append("   AND MD.MASKPROCESSFLOWNAME = L.MASKPROCESSFLOWNAME ");
		sql.append("   AND MD.MASKPROCESSFLOWVERSION = L.MASKPROCESSFLOWVERSION ");
		sql.append("   AND MD.MASKPROCESSOPERATIONNAME = L.MASKPROCESSOPERATIONNAME ");
		sql.append("   AND MD.MASKPROCESSOPERATIONVERSION = L.MASKPROCESSOPERATIONVERSION ");
		sql.append("   AND M.MASKLOTNAME = L.MASKLOTNAME ");
		sql.append("   AND L.MASKLOTNAME = :MASKLOTNAME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("MASKLOTNAME", maskLotName);

		try
		{
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
			jobName = ConvertUtil.getMapValueByName(sqlResult.get(0), "JOBNAME");

			return jobName;
		}
		catch (Exception e)
		{

			StringBuffer subSql = new StringBuffer();
			subSql.append("SELECT DISTINCT ");
			subSql.append("       M.JOBNAME, ");
			subSql.append("       M.MASKLOTNAME, ");
			subSql.append("       M.MASKPROCESSFLOWNAME, ");
			subSql.append("       M.MASKPROCESSFLOWVERSION, ");
			subSql.append("       M.MQCSTATE, ");
			subSql.append("       M.FACTORYNAME, ");
			subSql.append("       M.CREATEUSER ");
			subSql.append("  FROM CT_MASKMQCPLAN M, CT_MASKLOT L, CT_MASKMQCPLANDETAIL MD ");
			subSql.append(" WHERE 1 = 1 ");
			subSql.append("   AND M.FACTORYNAME = L.FACTORYNAME ");
			subSql.append("   AND M.JOBNAME = MD.JOBNAME ");
			subSql.append("   AND M.MASKLOTNAME = L.MASKLOTNAME ");
			subSql.append("   AND L.MASKLOTNAME = :MASKLOTNAME ");

			Map<String, String> argsSub = new HashMap<String, String>();
			argsSub.put("MASKLOTNAME", maskLotName);

			try
			{
				@SuppressWarnings("unchecked")
				List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(subSql.toString(), argsSub);
				jobName = ConvertUtil.getMapValueByName(sqlResult.get(0), "JOBNAME");

				if (sqlResult == null || sqlResult.size() < 1)
					throw new CustomException("LOT-0105");

				return jobName;
			}
			catch (Exception ex)
			{
				throw new CustomException("LOT-0105");
			}

		}

	}

	int getNodeStackCount(String str, char c)
	{
		int count = 0;
		for (int i = 0; i < str.length(); i++)
		{
			if (str.charAt(i) == c)
				count++;
		}
		return count;
	}
}
