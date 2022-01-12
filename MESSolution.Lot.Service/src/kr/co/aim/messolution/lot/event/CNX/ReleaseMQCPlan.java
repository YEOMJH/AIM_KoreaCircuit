package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCPlan;
import kr.co.aim.messolution.extended.object.management.data.MQCPlanDetail;
import kr.co.aim.messolution.extended.object.management.data.MQCPlanDetail_Extended;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ReleaseMQCPlan extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String jobName = SMessageUtil.getBodyItemValue(doc, "JOBNAME", true);
		MQCPlan planData = ExtendedObjectProxy.getMQCPlanService().selectByKey(false, new Object[] { jobName });

		if (planData.getMQCState().equalsIgnoreCase("Released"))
		{
			//MQC-0010:MQC plan already released
			throw new CustomException("MQC-0010");
		}

		if (!planData.getMQCState().equalsIgnoreCase("Created") && !planData.getMQCState().equalsIgnoreCase("Suspending"))
			throw new CustomException("LOT-9024", "Released", planData.getMQCState(), jobName);

		CommonValidation.checkMQCStateReleasedCount(planData.getLotName(), "Released");

		List<MQCPlan> resultList;
		try
		{
			resultList = ExtendedObjectProxy.getMQCPlanService().select("lotName = ? AND MQCState IN (?, ?) AND jobName <> ?",
					new Object[] { planData.getLotName(), "Released", "Recycling", planData.getJobName() });
		}
		catch (Exception ne)
		{
			// no data, therefore, free Lot
			resultList = new ArrayList<MQCPlan>();
		}

		if (resultList.size() > 0)
		{
			//MQC-0009:Lot already has being in any MQC
			throw new CustomException("MQC-0009");
		}

		String lotName = planData.getLotName();
		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		CommonValidation.checkLotState(lotData);
		CommonValidation.checkLotProcessState(lotData);
		CommonValidation.checkLotHoldState(lotData);
		CommonValidation.checkLotReworkState(lotData);

		ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

		if (!StringUtils.equals(flowData.getProcessFlowType(), "MQCPrepare"))
			throw new CustomException("LOT-0147", flowData.getProcessFlowType());

		List<MQCPlanDetail> planDetailList = ExtendedObjectProxy.getMQCPlanDetailService().getMQCPlanDetailDataByJobName(planData.getJobName());

		// Validation - UsedLimitCount
		checkUsedLimitCount(jobName, planData, planDetailList);
		checkProductUsedCount(jobName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);

		String targetFlowName = "";
		String targetFlowVersion = "";
		String targetOperationName = "";
		String targetOperationVer = "";
		String targetNodeStack = "";

		List<ProductU> productUdfs = new ArrayList<ProductU>();

		// resume case
		if (!StringUtil.isEmpty(planData.getReturnFlowName()) && !StringUtil.isEmpty(planData.getReturnOperationName()))
		{
			targetFlowName = planData.getReturnFlowName();
			targetFlowVersion = planData.getReturnFlowVersion();
			targetOperationName = planData.getReturnOperationName();
			targetOperationVer = planData.getReturnOperationVersion();
			targetNodeStack = new StringBuilder(lotData.getNodeStack()).append(".")
					.append(NodeStack.getNodeID(planData.getFactoryName(), targetFlowName, targetFlowVersion, targetOperationName, targetOperationVer)).toString();
		}
		else
		{
			Node targetNode = getFirstNode(jobName, planData.getFactoryName(), planData.getProcessFlowName(), planData.getProcessFlowVersion());

			if (targetNode == null)
			{
				Node startNode = ProcessFlowServiceProxy.getProcessFlowService().getStartNode(
						new ProcessFlowKey(planData.getFactoryName(), planData.getProcessFlowName(), planData.getProcessFlowVersion()));
				targetNode = ProcessFlowServiceProxy.getProcessFlowService().getNextNode(startNode.getKey().getNodeId(), "Normal", "");
			}

			targetFlowName = targetNode.getProcessFlowName();
			targetFlowVersion = targetNode.getProcessFlowVersion();
			targetOperationName = targetNode.getNodeAttribute1();
			targetOperationVer = targetNode.getNodeAttribute2();
			targetNodeStack = new StringBuilder(lotData.getNodeStack()).append(".").append(targetNode.getKey().getNodeId()).toString();
		}

		eventInfo.setEventName("ReleaseMQC");
		ExtendedObjectProxy.getMQCPlanService().updateMQCPlanReturnInfo(eventInfo, planData, "Released", targetFlowName, targetFlowVersion, targetOperationName, targetOperationVer);

		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo(lotData.getProductionType(), planData.getProductSpecName(), planData.getProductSpecVersion(), lotData.getProductSpec2Name(),
				lotData.getProductSpec2Version(), lotData.getProductRequestName(), lotData.getSubProductUnitQuantity1(), lotData.getSubProductUnitQuantity2(), lotData.getDueDate(),
				lotData.getPriority(), lotData.getFactoryName(), lotData.getAreaName(), lotData.getLotState(), lotData.getLotProcessState(), lotData.getLotHoldState(), targetFlowName,
				targetFlowVersion, targetOperationName, targetOperationVer, targetNodeStack, productUdfs);

		changeSpecInfo.getUdfs().put("RETURNFLOWNAME", lotData.getProcessFlowName());
		changeSpecInfo.getUdfs().put("RETURNOPERATIONNAME", lotData.getProcessOperationName());
		changeSpecInfo.getUdfs().put("RETURNOPERATIONVER", lotData.getProcessOperationVersion());

		// set trace info
		changeSpecInfo.getUdfs().put("BEFOREFLOWNAME", lotData.getProcessFlowName());
		changeSpecInfo.getUdfs().put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());

		eventInfo.setEventName("ReleaseMQC");
		Lot afterLot = MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);

		MESLotServiceProxy.getLotServiceImpl().deleteCtLotFutureActionByFlow(eventInfo, afterLot.getKey().getLotName(), afterLot.getFactoryName(), afterLot.getProcessFlowName(),
				afterLot.getProcessFlowVersion(), "1");

		return doc;
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

	private void checkUsedLimitCount(String jobName, MQCPlan planData, List<MQCPlanDetail> planDetailList) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		Map<String, Object> args = new HashMap<String, Object>();

		if (StringUtil.isEmpty(planData.getReturnFlowName()) && StringUtil.isEmpty(planData.getReturnOperationName()))
		{
			// Release - first MQC Operation
			for (MQCPlanDetail planDetail : planDetailList)
			{
				List<MQCPlanDetail_Extended> result = ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().getMQCPlanDetail_ExtendedOverCount(jobName, planDetail.getProcessFlowName(),
						planDetail.getProcessFlowVersion(), planDetail.getProcessOperationName(), planDetail.getProcessOperationVersion(), planDetail.getDummyUsedLimit().toString());

				if (result != null)
				{
					String sProductName = result.get(0).getProductName();
					String sDummyUsedCount = result.get(0).getDummyUsedCount().toString();

					throw new CustomException("LOT-0040", sProductName, planData.getDummyUsedLimit(), sDummyUsedCount, planDetail.getProcessFlowName(), planDetail.getProcessOperationName());
				}
			}
		}
		else
		{
			sql.setLength(0);
			sql.append("SELECT C.PROCESSFLOWNAME, ");
			sql.append("       C.PROCESSFLOWVERSION, ");
			sql.append("       C.PROCESSOPERATIONNAME, ");
			sql.append("       C.PROCESSOPERATIONVERSION, ");
			sql.append("       C.DUMMYUSEDLIMIT ");
			sql.append("  FROM CT_MQCPLANDETAIL C, ");
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
			sql.append("           AND PF.FACTORYNAME = :FACTORYNAME ");
			sql.append("           AND PF.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
			sql.append("           AND PF.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
			sql.append("           AND N.FACTORYNAME = PF.FACTORYNAME ");
			sql.append("           AND N.PROCESSFLOWNAME = PF.PROCESSFLOWNAME ");
			sql.append("           AND N.PROCESSFLOWVERSION = PF.PROCESSFLOWVERSION ");
			sql.append("           AND N.FACTORYNAME = A.FACTORYNAME ");
			sql.append("           AND N.PROCESSFLOWNAME = A.PROCESSFLOWNAME ");
			sql.append("           AND N.PROCESSFLOWVERSION = A.PROCESSFLOWVERSION ");
			sql.append("           AND N.FACTORYNAME = PF.FACTORYNAME ");
			sql.append("           AND A.FROMNODEID = N.NODEID ");
			sql.append("        START WITH N.NODETYPE = 'Start' ");
			sql.append("        CONNECT BY NOCYCLE A.FROMNODEID = PRIOR A.TONODEID AND A.FACTORYNAME = :FACTORYNAME) QL ");
			sql.append(" WHERE C.JOBNAME = :JOBNAME ");
			sql.append("   AND C.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
			sql.append("   AND C.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
			sql.append("   AND C.PROCESSFLOWNAME = QL.PROCESSFLOWNAME ");
			sql.append("   AND C.PROCESSFLOWVERSION = QL.PROCESSFLOWVERSION ");
			sql.append("   AND C.PROCESSOPERATIONNAME = QL.PROCESSOPERATIONNAME ");
			sql.append("   AND C.PROCESSOPERATIONVERSION = QL.PROCESSOPERATIONVERSION ");
			sql.append("ORDER BY QL.LV ");

			args.put("JOBNAME", jobName);
			args.put("PROCESSFLOWNAME", planData.getProcessFlowName());
			args.put("PROCESSFLOWVERSION", planData.getProcessFlowVersion());
			args.put("FACTORYNAME", planData.getFactoryName());

			@SuppressWarnings("unchecked")
			List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			boolean checkStart = false;

			for (Map<String, Object> map : result)
			{
				String operationName = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONNAME");
				String operationVersion = ConvertUtil.getMapValueByName(map, "PROCESSOPERATIONVERSION");
				String dummyUsedLimit = ConvertUtil.getMapValueByName(map, "DUMMYUSEDLIMIT");

				if (StringUtils.equals(operationName, planData.getReturnOperationName()) && StringUtils.equals(operationVersion, planData.getReturnOperationVersion()))
				{
					checkStart = true;
				}

				if (checkStart)
				{
					List<MQCPlanDetail_Extended> result2 = ExtendedObjectProxy.getMQCPlanDetail_ExtendedService().getMQCPlanDetail_ExtendedOverCount(jobName, planData.getProcessFlowName(),
							planData.getProcessFlowVersion(), operationName, operationVersion, dummyUsedLimit);

					if (result2 != null)
					{
						String sProductName = result2.get(0).getProductName();
						String sDummyUsedCount = result2.get(0).getDummyUsedCount().toString();

						throw new CustomException("LOT-0040", sProductName, dummyUsedLimit, sDummyUsedCount, planData.getProcessFlowName(), operationName);
					}
				}
			}
		}
	}

	private void checkProductUsedCount(String jobName) throws CustomException
	{
		// Check DummyUsedCount in Product
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT ME.PRODUCTNAME, COUNT (*) AS COUNT ");
		sql.append("  FROM CT_MQCPLANDETAIL_EXTENDED ME, CT_MQCPLAN M ");
		sql.append(" WHERE ME.JOBNAME = M.JOBNAME ");
		sql.append("   AND ME.JOBNAME = :JOBNAME ");
		sql.append("GROUP BY ME.PRODUCTNAME ");
		sql.append("ORDER BY ME.PRODUCTNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("JOBNAME", jobName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (sqlResult.size() > 0)
		{
			for (Map<String, Object> result : sqlResult)
			{
				String productName = ConvertUtil.getMapValueByName(result, "PRODUCTNAME");
				String sOperationCount = ConvertUtil.getMapValueByName(result, "COUNT");
				
				int operationCount = Integer.parseInt(sOperationCount);
				
				Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

				String mqcProductUsedLimit = productData.getUdfs().get("MQCPRODUCTUSEDLIMIT");
				String dummyUsedCount = productData.getUdfs().get("DUMMYUSEDCOUNT");

				if(StringUtils.isEmpty(dummyUsedCount))
					dummyUsedCount = "0";

				if (StringUtils.isEmpty(mqcProductUsedLimit) || Integer.parseInt(mqcProductUsedLimit) < 1)
				{
					throw new CustomException("LOT-0151", productName);
				}

				if (Integer.parseInt(mqcProductUsedLimit) < Integer.parseInt(dummyUsedCount) + operationCount)
				{
					throw new CustomException("LOT-0152", mqcProductUsedLimit, productName, dummyUsedCount, operationCount);
				}
			}
		}

	}
}
