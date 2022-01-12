package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCPlan;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskMQCPlan;
import kr.co.aim.messolution.extended.object.management.data.MaskMQCPlanDetail;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;

import org.jdom.Document;

public class ReleaseMaskMQCPlan extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);
		MaskMQCPlan planData = ExtendedObjectProxy.getMaskMQCPlanService().selectByKey(false, new Object[] { jobName });

		// MQC validation
		if (planData.getMQCState().equalsIgnoreCase("Released"))
		{
			//MQC-0010: MQC plan already released
			throw new CustomException("MQC-0010");
		}

		if (!planData.getMQCState().equalsIgnoreCase("Created") && !planData.getMQCState().equalsIgnoreCase("Suspending"))
			throw new CustomException("LOT-9024", "Released", planData.getMQCState(), jobName);

		checkMQCStateReleasedCount(planData.getMaskLotName(), "Released");

		List<MaskMQCPlan> resultList;
		try
		{
			resultList = ExtendedObjectProxy.getMaskMQCPlanService().select("MasklotName = ? AND MQCState = ? AND jobName <> ?",
					new Object[] { planData.getMaskLotName(), "Released", planData.getJobName() });
		}
		catch (Exception ne)
		{
			// no data, therefore, free Lot
			resultList = new ArrayList<MaskMQCPlan>();
		}

		if (resultList.size() > 0)
		{
			//MQC-0009:Lot already has being in any MQC
			throw new CustomException("MQC-0009");
		}

		String maskLotName = planData.getMaskLotName();

		MaskLot maskLotData = null;

		try
		{
			maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(false, new Object[] { maskLotName });
		}
		catch (greenFrameDBErrorSignal nfdes)
		{
			throw new CustomException("DURABLE-5050", maskLotName);
		}

		// Lot validation
		if (!maskLotData.getMaskLotState().equals(GenericServiceProxy.getConstantMap().Lot_Released))
			throw new CustomException("LOT-0016", maskLotData.getMaskLotName(), maskLotData.getMaskLotState());
		if (!maskLotData.getMaskLotProcessState().equals(GenericServiceProxy.getConstantMap().Lot_LoggedOut))
			throw new CustomException("LOT-0016", maskLotData.getMaskLotName(), maskLotData.getMaskLotProcessState());
		if (!maskLotData.getMaskLotHoldState().equals(GenericServiceProxy.getConstantMap().Flag_N))
			throw new CustomException("LOT-0016", maskLotData.getMaskLotName(), maskLotData.getMaskLotHoldState());

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

		String targetFlowName = "";
		String targetFlowVersion = "";
		String targetOperationName = "";
		String targetOperationVer = "";
		String targetNodeStack = "";

		Node targetNode = getFirstNode(jobName, planData.getFactoryName(), planData.getMaskProcessFlowName(), planData.getMaskProcessFlowVersion());

		if (targetNode == null)
		{
			Node startNode = ProcessFlowServiceProxy.getProcessFlowService().getStartNode(
					new ProcessFlowKey(planData.getFactoryName(), planData.getMaskProcessFlowName(), planData.getMaskProcessFlowVersion()));
			targetNode = ProcessFlowServiceProxy.getProcessFlowService().getNextNode(startNode.getKey().getNodeId(), "Normal", "");
		}

		targetFlowName = targetNode.getProcessFlowName();
		targetFlowVersion = targetNode.getProcessFlowVersion();
		targetOperationName = targetNode.getNodeAttribute1();
		targetOperationVer = targetNode.getNodeAttribute2();
		targetNodeStack = new StringBuilder(targetNode.getKey().getNodeId()).toString();

		// release MQC plan
		eventInfo.setEventName("ReleaseMaskMQC");
		planData.setMQCState("Released");
		planData.setLastEventComment(eventInfo.getEventComment());
		planData.setLastEventName(eventInfo.getEventName());
		planData.setLastEventTime(eventInfo.getEventTime());
		planData.setLastEventUser(eventInfo.getEventUser());

		planData = ExtendedObjectProxy.getMaskMQCPlanService().modify(eventInfo, planData);

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

		// houxk add 20200430
		for (MaskMQCPlanDetail MaskPlanDetail : MaskMQCPlanDetail)
		{
			try
			{
				MaskPlanDetail.setLastEventComment(eventInfo.getEventComment());
				MaskPlanDetail.setLastEventName(eventInfo.getEventName());
				MaskPlanDetail.setLastEventTime(eventInfo.getEventTime());
				MaskPlanDetail.setLastEventUser(eventInfo.getEventUser());

				ExtendedObjectProxy.getMaskMQCPlanDetailService().modify(eventInfo, MaskPlanDetail);

			}
			catch (Exception ex)
			{
				eventLog.error(String.format("DetailPlan[%s, %s] MQC plan not released yet", MaskPlanDetail.getMaskProcessFlowName(), MaskPlanDetail.getMaskProcessOperationName()));
			}
		}
		
		// move to MQC flow
		try
		{
			// Set Event Info
			eventInfo.setEventName("ReleaseMaskMQC");
			// maskLotData.setMaskSpecName(maskLotData.getMaskSpecName());
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

		return doc;
	}

	private static void checkMQCStateReleasedCount(String lotName, String eventName) throws CustomException
	{
		List<MQCPlan> mqcPlanData = ExtendedObjectProxy.getMQCPlanService().getReleasedMQCPlanDataByLotName(lotName);

		if (mqcPlanData != null)
			throw new CustomException("LOT-0104", eventName);
	}

	private Node getFirstNode(String jobName, String factoryName, String processFlowName, String processFlowVersion)
	{
		Node targetNode = null;

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT QL.NODEID ");
		sql.append("  FROM PROCESSOPERATIONSPEC PO, ");
		sql.append("       (SELECT LEVEL LV, ");
		sql.append("               N.FACTORYNAME, ");
		sql.append("               N.NODEATTRIBUTE1 AS PROCESSOPERATIONNAME, ");
		sql.append("               N.NODEATTRIBUTE2 AS PROCESSOPERATIONVERSION, ");
		sql.append("               N.PROCESSFLOWNAME, ");
		sql.append("               N.PROCESSFLOWVERSION, ");
		sql.append("               N.NODEID ");
		sql.append("          FROM ARC A, NODE N, PROCESSFLOW PF ");
		sql.append("         WHERE 1 = 1 ");
		sql.append("           AND N.NODETYPE = 'ProcessOperation' ");
		sql.append("           AND PF.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("           AND PF.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("           AND N.FACTORYNAME = :FACTORYNAME ");
		sql.append("           AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME ");
		sql.append("           AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION ");
		sql.append("           AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME ");
		sql.append("           AND N.FACTORYNAME = PF.FACTORYNAME ");
		sql.append("           AND A.FROMNODEID = N.NODEID ");
		sql.append("        START WITH N.NODETYPE = 'Start' ");
		sql.append("        CONNECT BY NOCYCLE A.FROMNODEID = PRIOR A.TONODEID AND A.FACTORYNAME = :FACTORYNAME) QL, ");
		sql.append("       (SELECT DISTINCT P.FACTORYNAME, ");
		sql.append("               D.PROCESSFLOWNAME, ");
		sql.append("               D.PROCESSFLOWVERSION, ");
		sql.append("               D.PROCESSOPERATIONNAME, ");
		sql.append("               D.PROCESSOPERATIONVERSION ");
		sql.append("          FROM CT_MQCPLAN P, CT_MQCPLANDETAIL D ");
		sql.append("         WHERE P.JOBNAME = D.JOBNAME ");
		sql.append("           AND P.JOBNAME = :JOBNAME ");
		sql.append("           AND P.MQCSTATE = 'Created' ");
		sql.append("           AND D.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("           AND D.PROCESSFLOWVERSION = :PROCESSFLOWVERSION) P ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND PO.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND PO.PROCESSOPERATIONNAME = QL.PROCESSOPERATIONNAME ");
		sql.append("   AND PO.PROCESSOPERATIONVERSION = QL.PROCESSOPERATIONVERSION ");
		sql.append("   AND PO.FACTORYNAME = P.FACTORYNAME ");
		sql.append("   AND QL.FACTORYNAME = P.FACTORYNAME ");
		sql.append("   AND QL.PROCESSFLOWNAME = P.PROCESSFLOWNAME ");
		sql.append("   AND QL.PROCESSFLOWVERSION = P.PROCESSFLOWVERSION ");
		sql.append("   AND PO.PROCESSOPERATIONNAME = P.PROCESSOPERATIONNAME ");
		sql.append("   AND PO.PROCESSOPERATIONVERSION = P.PROCESSOPERATIONVERSION ");
		sql.append("ORDER BY QL.LV ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("FACTORYNAME", factoryName);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("JOBNAME", jobName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			String nodeId = ConvertUtil.getMapValueByName(result.get(0), "NODEID");
			targetNode = ProcessFlowServiceProxy.getProcessFlowService().getNode(nodeId);
		}

		return targetNode;
	}

}