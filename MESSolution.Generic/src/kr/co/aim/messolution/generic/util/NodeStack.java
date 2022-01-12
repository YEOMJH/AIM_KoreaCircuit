
package kr.co.aim.messolution.generic.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;

 

@SuppressWarnings("serial")
public class NodeStack extends Stack<String>
{
	public static NodeStack stringToNodeStack(final String nodeText)
	{
		NodeStack nodeStack = new NodeStack();
		
		nodeStack.addAll(Arrays.asList(nodeText.split("\\.")));
		
		return nodeStack;
	}
	public static String nodeStackToString(final NodeStack nodeStack)
	{
		String nodeText = "";
		
		for ( int i = 0; i < nodeStack.size(); i++ )
		{
			nodeText += nodeStack.get(i);
			
			if ( i < nodeStack.size() - 1 )
				nodeText += ".";				
		}
		
		return nodeText;
	}

	public static String getNodeID(String factoryName, String processFlowName, String processFlowVersion, 
			String processOperationName, String processOperationVer) throws CustomException
	{
		ProcessFlowKey pfKey = new ProcessFlowKey();
		pfKey.setFactoryName(factoryName);
		pfKey.setProcessFlowName(processFlowName);
		pfKey.setProcessFlowVersion("00001");

		String nodeId = "";

		if (processOperationName == null)
		{
			nodeId = ProcessFlowServiceProxy.getProcessFlowService().getStartNode(pfKey).getKey().getNodeId();
		}
		else
		{
			String sql = "SELECT NODEID FROM NODE "
					+ "WHERE FACTORYNAME = :FACTORYNAME "
					+ "AND PROCESSFLOWNAME = :PROCESSFLOWNAME "
					+ "AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
					+ "AND NODEATTRIBUTE1 = :NODEATTRIBUTE1 "
					+ "AND NODEATTRIBUTE2 = :NODEATTRIBUTE2 "
					+ "AND NODETYPE = :NODETYPE ";
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("PROCESSFLOWNAME", processFlowName);
			bindMap.put("PROCESSFLOWVERSION", processFlowVersion);
			bindMap.put("NODEATTRIBUTE1", processOperationName);
			bindMap.put("NODEATTRIBUTE2", processOperationVer);
			bindMap.put("NODETYPE", "ProcessOperation");
			
			List<Map<String, Object>> result = 
					GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

			if (result.size() > 0)
			{
				nodeId = ConvertUtil.getMapValueByName(result.get(0), "NODEID");
				return nodeId;
			}
			else
			{
				throw new CustomException("LOT-0041", processOperationName);
			}
		}

		return nodeId;
	}

	public static String getNodeID(String factoryName,
							String processFlowName,
							String processOperationName,
							String processOperationVer) throws CustomException
	{		
		ProcessFlowKey pfKey = new ProcessFlowKey();
		pfKey.setFactoryName( factoryName );
		pfKey.setProcessFlowName( processFlowName );
		pfKey.setProcessFlowVersion( "00001" );
		
		String nodeId  = "";
		
		if ( processOperationName == null )
		{
			nodeId = ProcessFlowServiceProxy.getProcessFlowService().getStartNode(pfKey).getKey().getNodeId();
		}
		else
		{
			String sql = "SELECT NODEID FROM NODE "
					+ "WHERE FACTORYNAME = :FACTORYNAME "
					+ "AND PROCESSFLOWNAME = :PROCESSFLOWNAME "
					+ "AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
					+ "AND NODEATTRIBUTE1 = :NODEATTRIBUTE1 "
					+ "AND NODEATTRIBUTE2 = :NODEATTRIBUTE2 "
					+ "AND NODETYPE = :NODETYPE ";
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("PROCESSFLOWNAME", processFlowName);
			bindMap.put("PROCESSFLOWVERSION", "00001");
			bindMap.put("NODEATTRIBUTE1", processOperationName);
			bindMap.put("NODEATTRIBUTE2", processOperationVer);
			bindMap.put("NODETYPE", "ProcessOperation");
			
			List<Map<String, Object>> result = 
					GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			if (result.size() > 0)
			{
				nodeId = ConvertUtil.getMapValueByName(result.get(0), "NODEID");
				return nodeId;
			}
			else
			{
				throw new CustomException("LOT-0041", processOperationVer);
			}
		}
		
		return nodeId;
	}
	
	public static String getFlowNodeID(String factoryName, String currentFlowName, String nextFlowName) 
			throws CustomException
	{
		String sql = "SELECT NODEID FROM NODE "
				+ "WHERE FACTORYNAME = :FACTORYNAME "
				+ "AND PROCESSFLOWNAME = :PROCESSFLOWNAME "
				+ "AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION "
				+ "AND NODEATTRIBUTE1 = :NODEATTRIBUTE1 "
				+ "AND NODEATTRIBUTE2 = :NODEATTRIBUTE2 "
				+ "AND NODETYPE = :NODETYPE";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PROCESSFLOWNAME", currentFlowName);
		bindMap.put("PROCESSFLOWVERSION", "00001");
		bindMap.put("NODEATTRIBUTE1", nextFlowName);
		bindMap.put("NODEATTRIBUTE2", "00001");
		bindMap.put("NODETYPE", "ProcessFlow");
		
		List<Map<String, Object>> result = 
				GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		if (result.size() > 0)
		{
			String nodeId = ConvertUtil.getMapValueByName(result.get(0), "NODEID");
			return nodeId;
		}
		else
		{
			throw new CustomException("LOT-0042", currentFlowName, nextFlowName);
		}
	}
}
