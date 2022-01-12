package kr.co.aim.messolution.extended.object.management.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ReserveRepairPolicy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;

public class ReserveRepairPolicyService extends CTORMService<ReserveRepairPolicy> {
	
	public static Log logger = LogFactory.getLog(ReserveRepairPolicyService.class);
	
	private final String historyEntity = "ReserveRepairPolicyHist";
	
	public List<ReserveRepairPolicy> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<ReserveRepairPolicy> result = super.select(condition, bindSet, ReserveRepairPolicy.class);
		
		return result;
	}
	
	public ReserveRepairPolicy selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(ReserveRepairPolicy.class, isLock, keySet);
	}
	
	public ReserveRepairPolicy create(EventInfo eventInfo, ReserveRepairPolicy dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ReserveRepairPolicy dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public ReserveRepairPolicy modify(EventInfo eventInfo, ReserveRepairPolicy dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	@SuppressWarnings("unchecked")
	public boolean isExistData(Lot lotData)
	{
		logger.info("&&& Execute Function : ReserveRepairPolicyService.isExistData Start &&&");
		
		try
		{
			String sql = "SELECT RP.FACTORYNAME, "
					   + "       RP.PRODUCTSPECNAME, "
					   + "       RP.PRODUCTSPECVERSION, "
					   + "       RP.PROCESSFLOWNAME, "
					   + "       RP.PROCESSOPERATIONNAME, "
					   + "       RP.REPAIRFLOWNAME, "
					   + "       RP.REPAIROPERATIONNAME "
					   + "FROM CT_RESERVEREPAIRPOLICY RP, NODE N "
					   + "WHERE 1 = 1 "
					   + "  AND N.NODETYPE = 'ProcessOperation' "
					   + "  AND N.FACTORYNAME = :FACTORYNAME "
					   + "  AND N.PROCESSFLOWNAME = :PROCESSFLOWNAME "
					   + "  AND N.PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
					   + "  AND N.NODEATTRIBUTE1 NOT IN( "
					   + "        SELECT N.NODEATTRIBUTE1 "
					   + "        FROM NODE N, ARC A "
					   + "        WHERE 1 = 1 "
					   + "          AND N.NODETYPE = 'ProcessOperation' "
					   + "          AND N.FACTORYNAME = :FACTORYNAME "
					   + "          AND N.PROCESSFLOWNAME = :PROCESSFLOWNAME "
					   + "          AND N.PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
					   + "          AND A.FROMNODEID = N.NODEID "
					   + "        START WITH N.NODETYPE = 'Start' "
					   + "        CONNECT BY NOCYCLE PRIOR A.TONODEID = A.FROMNODEID AND N.NODEID <> :NODEID) "
					   + "  AND N.FACTORYNAME = RP.FACTORYNAME "
					   + "  AND N.PROCESSFLOWNAME = RP.PROCESSFLOWNAME "
					   + "  AND N.PROCESSFLOWVERSION = RP.PROCESSFLOWVERSION "
					   + "  AND N.NODEATTRIBUTE1 = RP.PROCESSOPERATIONNAME "
					   + "  AND N.NODEATTRIBUTE2 = RP.PROCESSOPERATIONVERSION ";
			
			String nodeStack = lotData.getNodeStack();
			if (nodeStack.contains("."))
			{
				nodeStack = StringUtil.getFirstValue(nodeStack, ".");
			}
			
			Node nodeData = ProcessFlowServiceProxy.getNodeService().getNode(nodeStack);
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("FACTORYNAME", lotData.getFactoryName());
			bindMap.put("PRODUCTSPECNAME", lotData.getProductSpecName());
			bindMap.put("PRODUCTSPECVERSION", lotData.getProductSpecVersion());
			bindMap.put("PROCESSFLOWNAME", nodeData.getProcessFlowName());
			bindMap.put("PROCESSFLOWVERSION", nodeData.getProcessFlowVersion());
			bindMap.put("NODEID", nodeStack);
			
			List<OrderedMap> resultDataList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, bindMap);
			if(resultDataList != null && resultDataList.size() > 0)
			{
				return true;
			}
		}
		catch(Exception ex)
		{
			logger.info(ex.getCause().toString());
		}
		
		logger.info("&&& Execute Function : ReserveRepairPolicyService.isExistData End &&&");
		
		return false;
	}
	
//	public List<ReserveRepairPolicy> getReserveRepairPolicyData(Lot lotData)
//	{
//		try
//		{			
//			String factoryName = lotData.getFactoryName();
//			String productSpecName = lotData.getProductSpecName();
//			String productSpecVersion = lotData.getProductSpecVersion();
//			String inspectionFlowName = lotData.getProcessFlowName();
//			String inspectionFlowVersion = lotData.getProcessFlowVersion();
//			String inspectionOperationName = lotData.getProcessOperationName();
//			String inspectionOperationVersion = lotData.getProcessOperationVersion();
//			
//			String nodeId = lotData.getNodeStack();
//			if (nodeId.indexOf(".") != -1)
//			{
//				nodeId = nodeId.split("\\.")[0];
//			}
//			
//			Node nodeData = ProcessFlowServiceProxy.getNodeService().getNode(nodeId);
//			
//			String targetFlowName = nodeData.getProcessFlowName();
//			String targetFlowVersion = nodeData.getProcessFlowVersion();
//			
//			String condition = "WHERE 1 = 1 "
//							 + "  AND FACTORYNAME = ? "
//							 + "  AND PRODUCTSPECNAME = ? "
//							 + "  AND PRODUCTSPECVERSION = ? "
//							 + "  AND INSPECTIONFLOWNAME = ? "
//							 + "  AND INSPECTIONFLOWVERSION = ? "
//							 + "  AND INSPECTIONOPERATIONNAME = ? "
//							 + "  AND INSPECTIONOPERATIONVERSION = ? "
//							 + "  AND TARGETFLOWNAME = ? "
//							 + "  AND TARGETFLOWVERSION = ? ";
//					
//			return this.select(condition, new Object[] { factoryName, 
//														 productSpecName, productSpecVersion,
//														 inspectionFlowName, inspectionFlowVersion, 
//														 inspectionOperationName, inspectionOperationVersion,
//														 targetFlowName, targetFlowVersion });
//		}
//		catch (Exception ex)
//		{
//			logger.warn("ReserveRepairPolicy.getReserveRepairPolicyData() : " + ex.getCause());
//			return new ArrayList<ReserveRepairPolicy>();
//		}
//	}
}
