package kr.co.aim.messolution.generic.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.SampleLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.sys.SystemPropHelper;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import sun.java2d.loops.MaskFill;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PolicyUtil {
	private static Log logger = LogFactory.getLog(PolicyUtil.class);
	public static String NEWLINE = SystemPropHelper.CR;
	
	public static String getPhotoRecipeRMSFlag(String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion,
			String processOperationName, String processOperationVersion, String machineName, String recipeName) throws CustomException {

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT P.RMSFLAG ");
		sql.append("  FROM TPFOPOLICY T, POSPHOTOMACHINE P ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND T.CONDITIONID = P.CONDITIONID ");
		sql.append("   AND T.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("   AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("   AND P.MACHINENAME = :MACHINENAME ");
		sql.append("   AND P.MACHINERECIPENAME = :MACHINERECIPENAME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryName);
		args.put("PRODUCTSPECNAME", productSpecName);
		args.put("PRODUCTSPECVERSION", productSpecVersion);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("PROCESSOPERATIONNAME", processOperationName);
		args.put("PROCESSOPERATIONVERSION", processOperationVersion);
		args.put("MACHINENAME", machineName);
		args.put("MACHINERECIPENAME", recipeName);

		try 
		{
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (sqlResult != null && sqlResult.size() > 0) 
			{
				String RMSFlag = ConvertUtil.getMapValueByName(sqlResult.get(0), "RMSFLAG");

				if (StringUtils.isEmpty(RMSFlag)) 
				{
					RMSFlag = "N";
				}

				return RMSFlag;
			} 
			else 
			{
				//CUSTOM-0022:Machine[{0}] is not enable with [POSPhotoMachine = {1},{2},{3},{4}]
				throw new CustomException("CUSTOM-0022", machineName, factoryName, productSpecName,processFlowName, processOperationName);
			}
		} 
		catch (FrameworkErrorSignal de) 
		{
			throw new CustomException("SYS-9999", "POSPhotoMachine", de.getMessage());
		}
	}

	public static ListOrderedMap getMachineRecipeName(String factoryName, 
											  String productSpecName, 
											  String productSpecVersion,
											  String processFlowName, 
											  String processFlowVersion,
											  String processOperationName, 
											  String processOperationVersion,
											  String machineName)
		throws CustomException
	{
		StringBuffer sqlBuffer = new StringBuffer("")
									.append(" SELECT P.MACHINERECIPENAME").append(NEWLINE)
									.append("   FROM TPFOPOLICY T, POSMACHINE P ").append(NEWLINE)
									.append("  WHERE 1=1 ").append(NEWLINE)
									.append("	 AND T.FACTORYNAME= ? ").append(NEWLINE)
									.append("    AND T.PRODUCTSPECNAME = ? ").append(NEWLINE)
									.append("    AND T.PRODUCTSPECVERSION = ? ").append(NEWLINE)
									.append("    AND T.PROCESSFLOWNAME = ? ").append(NEWLINE)
									.append("    AND T.PROCESSFLOWVERSION = ? ").append(NEWLINE)
									.append("    AND T.PROCESSOPERATIONNAME= ? ").append(NEWLINE)
									.append("    AND T.PROCESSOPERATIONVERSION = ? ").append(NEWLINE)
									.append("    AND P.MACHINENAME = ?").append(NEWLINE)
									.append("    AND T.CONDITIONID = P.CONDITIONID ").append(NEWLINE)
									.append("").append(NEWLINE);
	
		String sqlStmt = sqlBuffer.toString();
		
		Object[] bindSet = new String[]{factoryName, productSpecName, productSpecVersion, processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName};
		
		try
		{
			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt, bindSet);
		
			if(sqlResult.size() > 0)
				return sqlResult.get(0);
			else 
				throw new CustomException("SYS-9999", "POSMachine",
							String.format("Machine[%s] is not enable with [%s, %s, %s, %s]", machineName, factoryName, productSpecName, processFlowName, processOperationName));
		}
		catch (FrameworkErrorSignal de)
		{
			throw new CustomException("SYS-9999", "POSMachine", de.getMessage());
		}
	}
	
	public static String getPOSMachineRMSFlag(String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion, 
			String processOperationName, String processOperationVersion, String machineName) throws CustomException {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT P.RMSFLAG ");
		sql.append("  FROM TPFOPOLICY T, POSMACHINE P ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND T.CONDITIONID = P.CONDITIONID ");
		sql.append("   AND T.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("   AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("   AND P.MACHINENAME = :MACHINENAME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryName);
		args.put("PRODUCTSPECNAME", productSpecName);
		args.put("PRODUCTSPECVERSION", productSpecVersion);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("PROCESSOPERATIONNAME", processOperationName);
		args.put("PROCESSOPERATIONVERSION", processOperationVersion);
		args.put("MACHINENAME", machineName);

		try {
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (sqlResult != null && sqlResult.size() > 0) 
			{
				String RMSFlag = ConvertUtil.getMapValueByName(sqlResult.get(0), "RMSFLAG");

				if (StringUtils.isEmpty(RMSFlag)) 
				{
					RMSFlag = "N";
				}

				return RMSFlag;
			} 
			else 
			{
				//CUSTOM-0023: Machine[{0}] is not enable with [POSMachine = {1},{2},{3},{4}]
				throw new CustomException("CUSTOM-0023", machineName, factoryName, productSpecName, processFlowName, processOperationName);
			}
		} 
		catch (FrameworkErrorSignal de) 
		{
			throw new CustomException("SYS-9999", "POSMachine", de.getMessage());
		}
	}
	
	public static String getRecipeCheckLevel(String factoryName, 
											  String productSpecName, 
											  String productSpecVersion,
											  String processFlowName, 
											  String processFlowVersion,
											  String processOperationName, 
											  String processOperationVersion,
											  String machineName
											) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT P.CHECKLEVEL ");
		sql.append("  FROM TPFOPOLICY T, POSMACHINE P ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND T.CONDITIONID = P.CONDITIONID ");
		sql.append("   AND T.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("   AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("   AND P.MACHINENAME = :MACHINENAME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryName);
		args.put("PRODUCTSPECNAME", productSpecName);
		args.put("PRODUCTSPECVERSION", productSpecVersion);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("PROCESSOPERATIONNAME", processOperationName);
		args.put("PROCESSOPERATIONVERSION", processOperationVersion);
		args.put("MACHINENAME", machineName);

		try
		{
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (sqlResult != null && sqlResult.size() > 0)
			{
				String checkLevel = ConvertUtil.getMapValueByName(sqlResult.get(0), "CHECKLEVEL");

				if (StringUtils.isEmpty(checkLevel))
				{
					checkLevel = "N";
				}

				return checkLevel;
			}
			else
			{
				throw new CustomException("SYS-9999", "POSMachine",
						String.format("Machine[%s] is not enable with [%s, %s, %s, %s]", machineName, factoryName, productSpecName, processFlowName, processOperationName));
			}
		}
		catch (FrameworkErrorSignal de)
		{
			throw new CustomException("SYS-9999", "POSMachine", de.getMessage());
		}
	}
	
	

	public static String getPhotoMachineRecipeCheckLevel(String factoryName, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion, String processOperationName,
			String processOperationVersion, String machineName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		
		sql.append("SELECT P.CHECKLEVEL ");
		sql.append("  FROM TPFOPOLICY T, POSPHOTOMACHINE P ");
		sql.append(" WHERE 1 = 1 ");
		sql.append("   AND T.CONDITIONID = P.CONDITIONID ");
		sql.append("   AND T.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND T.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("   AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND T.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND T.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("   AND P.MACHINENAME = :MACHINENAME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryName);
		args.put("PRODUCTSPECNAME", productSpecName);
		args.put("PRODUCTSPECVERSION", productSpecVersion);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("PROCESSOPERATIONNAME", processOperationName);
		args.put("PROCESSOPERATIONVERSION", processOperationVersion);
		args.put("MACHINENAME", machineName);

		try
		{
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (sqlResult != null && sqlResult.size() > 0)
			{
				String checkLevel = ConvertUtil.getMapValueByName(sqlResult.get(0), "CHECKLEVEL");

				if (StringUtils.isEmpty(checkLevel))
				{
					checkLevel = "N";
				}

				return checkLevel;
			}
			else
			{
				throw new CustomException("SYS-9999", "POSMachine",
						String.format("Machine[%s] is not enable with [%s, %s, %s, %s]", machineName, factoryName, productSpecName, processFlowName, processOperationName));
			}
		}
		catch (FrameworkErrorSignal de)
		{
			throw new CustomException("SYS-9999", "POSMachine", de.getMessage());
		}
	}
	
	public static List<ListOrderedMap> getAvailableMachine(String factoryName, String machineGroupName)
		throws CustomException
	{
		StringBuffer sqlBuffer = new StringBuffer("")
									.append(" SELECT P.machineName, P.rollType ").append(NEWLINE)
									.append("   FROM TGPolicy C, POSMachine P ").append(NEWLINE)
									.append("  WHERE C.conditionId = P.conditionId ").append(NEWLINE)
									.append("   AND C.factoryName= ? ").append(NEWLINE)
									.append("  	AND C.machineGroupName = ? ").append(NEWLINE);
		
		Object[] bindSet = new String[]{factoryName, machineGroupName};
		
		try
		{
			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindSet); 
			
			if(sqlResult.size() < 1)
				throw new CustomException("SYS-9001", "POSMachine");
			else 
				return sqlResult;
		}
		catch (FrameworkErrorSignal de)
		{
			throw new CustomException("SYS-9999", "POSMachine", de.getMessage());
		}
	}

	public static String getAvailableMachine(Lot lotData)
	{
		logger.info("getAvailableMachine started....");
		
		String sqlStmt =  " SELECT P.MACHINENAME " + NEWLINE 
							+ "   FROM POSMACHINE P, TPFOPOLICY T " + NEWLINE
							+ "  WHERE P.CONDITIONID = T.CONDITIONID " + NEWLINE
							+ "	   AND T.FACTORYNAME= ? " + NEWLINE
							+ "    AND T.PRODUCTSPECNAME = ? " + NEWLINE
							+ "    AND T.PRODUCTSPECVERSION = '00001' " + NEWLINE
							+ "    AND T.PROCESSFLOWNAME = ? " + NEWLINE
							+ "    AND T.PROCESSFLOWVERSION = '00001' " + NEWLINE
							+ "    AND T.PROCESSOPERATIONNAME= ? " + NEWLINE
							+ "    AND T.PROCESSOPERATIONVERSION = '00001'";
	
		Object[] bindSet = new String[]{lotData.getFactoryName(),
										lotData.getProductSpecName(),
										lotData.getProcessFlowName(),
										lotData.getProcessOperationName()};
		List<ListOrderedMap> resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt.toString(), bindSet);
		
		for (ListOrderedMap row : resultList)
		{
			return row.get("MACHINENAME").toString();
		}
		
		return "";
	}
	
	public static String getWhereNext(Lot lotData, String condition, String value, String reworkFlag)
		throws CustomException
	{
		//hard coding must be changed
		if (reworkFlag.equals(GenericServiceProxy.getConstantMap().Flag_Y))
			condition = "Rework";
		
		String destinationNodeStack = "";
		String toFactoryName = "";
		String toFlowName = "";
		String toOperationName = "";
		String toOperationVer = "";
		String returnFlowName = "";
		String returnOperationName = "";
		String returnOperationVer = "";
		
		try
		{
			List<ListOrderedMap> alterPathList = getAlterProcessOperation(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());
			
			for (ListOrderedMap alterPath : alterPathList)
			{
				String conditionName = CommonUtil.getValue(alterPath, "CONDITIONNAME");
				String conditionValue = CommonUtil.getValue(alterPath, "CONDITIONVALUE");
				
				if (condition.equalsIgnoreCase(conditionName) && value.equalsIgnoreCase(conditionValue))
				{
					toFactoryName = CommonUtil.getValue(alterPath, "FACTORYNAME");
					toFlowName = CommonUtil.getValue(alterPath, "TOPROCESSFLOWNAME");
					toOperationName = CommonUtil.getValue(alterPath, "TOPROCESSOPERATIONNAME");
					toOperationVer = CommonUtil.getValue(alterPath, "TOPROCESSOPERATIONVERSION");
					returnFlowName = CommonUtil.getValue(alterPath, "RETURNPROCESSFLOWNAME");
					returnOperationName = CommonUtil.getValue(alterPath, "RETURNOPERATIONNAME");
					returnOperationVer = CommonUtil.getValue(alterPath, "RETURNOPERATIONVERSION");
					
					//return info setup into track-out Lot
					lotData.getUdfs().put("RETURNFLOWNAME", returnFlowName);
					lotData.getUdfs().put("RETURNOPERATIONNAME", returnOperationName);
					
					//destination is only one
					break;
				}
			}
			
			if (!StringUtil.isEmpty(toFactoryName) && !StringUtil.isEmpty(toFlowName) && !StringUtil.isEmpty(toOperationName))
				destinationNodeStack = NodeStack.getNodeID(toFactoryName, toFlowName, toOperationName, toOperationVer);
			
			if (!StringUtil.isEmpty(returnFlowName) && !StringUtil.isEmpty(returnOperationName))
			{
				String returnNodeId = NodeStack.getNodeID(lotData.getFactoryName(), returnFlowName, returnOperationName, returnOperationVer);
				destinationNodeStack = new StringBuffer(returnNodeId).append(".").append(destinationNodeStack).toString();
			}
		}
		catch (Exception ex)
		{
			logger.error("Cannot find next ProcessOperation");
			destinationNodeStack = "";
		}
		
		return destinationNodeStack;
	}
	
	public static List<ListOrderedMap> getAlterProcessOperation(String factoryName, String processFlowName, String processFlowVer, String processOperationName, String processOperationVer)
		throws CustomException
	{
		StringBuffer queryBuffer = new StringBuffer()
			.append("SELECT C.factoryName, C.processFlowName, C.processOperationName,\n")
			.append("       P.toProcessFlowName, P.toProcessOperationName, P.toProcessOperationVersion,			 \n")
			.append("       P.returnProcessFlowName, P.returnOperationName,	P.returnOperationVersion,	     \n")
			.append("       P.conditionName, P.conditionValue, P.reworkFlag		     \n")
			.append("FROM POSAlterProcessOperation P, TFOPolicy C                    \n")
			.append("WHERE C.conditionId = P.conditionId                             \n")
			.append("    AND C.factoryName = :FACTORYNAME                            \n")
			.append("    AND C.processFlowName = :PROCESSFLOWNAME                    \n")
			.append("    AND C.processFlowVersion = :PROCESSFLOWVERSION              \n")
			.append("    AND C.processOperationName = :PROCESSOPERATIONNAME          \n")
			.append("    AND C.processOperationVersion = :PROCESSOPERATIONVERSION    \n");
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("PROCESSFLOWVERSION", processFlowVer);
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		bindMap.put("PROCESSOPERATIONVERSION", processOperationVer);
		
		try
		{
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
			
			return result;
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "POSPolicy", fe.getMessage());
		}
	}

	public static List<ListOrderedMap> getQTimeSpec(String factoryName, String productSpecName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT C.FACTORYNAME, ");
		sql.append("       C.PROCESSFLOWNAME, ");
		sql.append("       C.PROCESSOPERATIONNAME, ");
		sql.append("       P.TOFACTORYNAME, ");
		sql.append("       P.TOPROCESSFLOWNAME, ");
		sql.append("       P.TOPROCESSOPERATIONNAME, ");
		sql.append("       P.WARNINGDURATIONLIMIT, ");
		sql.append("       P.INTERLOCKDURATIONLIMIT ");
		sql.append("  FROM POSQUEUETIME P, TFOPOLICY C ");
		sql.append(" WHERE C.CONDITIONID = P.CONDITIONID ");
		sql.append("   AND C.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND C.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND C.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND C.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND C.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");

		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("PROCESSFLOWVERSION", processFlowVersion);
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		bindMap.put("PROCESSOPERATIONVERSION", processOperationVersion);

		try
		{
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			return result;
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "POSPolicy", fe.getMessage());
		}
	}

	public static List<ListOrderedMap> getQTimeSpec(String factoryName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion)
			throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT C.FACTORYNAME, ");
		sql.append("       C.PROCESSFLOWNAME, ");
		sql.append("       C.PROCESSOPERATIONNAME, ");
		sql.append("       P.TOFACTORYNAME, ");
		sql.append("       P.TOPROCESSFLOWNAME, ");
		sql.append("       P.TOPROCESSOPERATIONNAME, ");
		sql.append("       P.WARNINGDURATIONLIMIT, ");
		sql.append("       P.INTERLOCKDURATIONLIMIT ");
		sql.append("  FROM POSQUEUETIME P, TFOPOLICY C ");
		sql.append(" WHERE C.CONDITIONID = P.CONDITIONID ");
		sql.append("   AND C.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND C.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND C.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND C.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND C.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");

		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("PROCESSFLOWVERSION", processFlowVersion);
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		bindMap.put("PROCESSOPERATIONVERSION", processOperationVersion);

		try
		{
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			return result;
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "POSPolicy", fe.getMessage());
		}
	}

	public static Node getNextOperation(Lot lotData)
		throws CustomException
	{
		String[] nodeStackArray = StringUtil.split(lotData.getNodeStack(), ".");
		
		Node nextNode = null;
		boolean isCurrent = true;

		ProcessFlow processFlowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

		if(StringUtil.equals("Rework", processFlowData.getProcessFlowType()) || StringUtil.equals("Strip", processFlowData.getProcessFlowType()))
		{
			for (int idx=nodeStackArray.length; idx > 0; idx--)
			{
				if (isCurrent)
				{
					try
					{
						//though which is successful, first loop must be descreminated
						isCurrent = false;
						List<ListOrderedMap> nodeList = getNodeList(lotData);
						String currentReworkNode = nodeStackArray[idx - 1];

						for(int i=0; i < nodeList.size(); i++)
						{
							nextNode = getNextNode(currentReworkNode);
							
							if (StringUtil.isEmpty(nextNode.getNodeAttribute1()) || StringUtil.isEmpty(nextNode.getProcessFlowName()))
								throw new Exception();

							List<SampleLot> sampleLot = ExtendedObjectProxy.getSampleLotService().getSampleLotDataListForNextNode(lotData.getKey().getLotName(), lotData.getFactoryName(),
									lotData.getProductSpecName(), lotData.getProductSpecVersion(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), nextNode.getNodeAttribute1(),
									nextNode.getNodeAttribute2());

							if(sampleLot == null)
							{
								logger.info("Oper["+nextNode.getNodeAttribute1()+"] is no sample data. restart get nextNode! ");
								currentReworkNode = nextNode.getKey().getNodeId();
								continue;
							}
							
							break;
						}
						break;
					}
					catch (Exception ex)
					{
						logger.debug("It is last node");
					}
				}
				else
				{
					nextNode = ProcessFlowServiceProxy.getNodeService().getNode(nodeStackArray[idx - 1]);
					break;
				}
			}
		}
		else
		{
			for (int idx=nodeStackArray.length; idx > 0; idx--)
			{
				if (isCurrent)
				{
					try
					{
						//though which is successful, first loop must be descreminated
						isCurrent = false;
	
						nextNode = getNextNode(nodeStackArray[idx - 1]);
						
						if (StringUtil.isEmpty(nextNode.getNodeAttribute1()) || StringUtil.isEmpty(nextNode.getProcessFlowName()))
							throw new Exception();
						
						
						break;
					}
					catch (Exception ex)
					{
						logger.debug("It is last node");
					}
				}
				else
				{
					nextNode = ProcessFlowServiceProxy.getNodeService().getNode(nodeStackArray[idx - 1]);
					break;
				}
			}
		}
		
		if (nextNode != null)
			return nextNode;
		else
			throw new CustomException("", "");
	}
	
	public static Node getNextOLEDMaskOperation(MaskLot MasklotData)
			throws CustomException
		{
			String[] nodeStackArray = StringUtil.split(MasklotData.getNodeStack(), ".");
			
			Node nextNode = null;
			boolean isCurrent = true;
			
			for (int idx=nodeStackArray.length; idx > 0; idx--)
			{
				if (isCurrent)
				{
					try
					{
						//though which is successful, first loop must be descreminated
						isCurrent = false;
						
						nextNode = getNextNode(nodeStackArray[idx - 1]);
						
						if (StringUtil.isEmpty(nextNode.getNodeAttribute1()) || StringUtil.isEmpty(nextNode.getProcessFlowName()))
							throw new Exception();
						
						
						break;
					}
					catch (Exception ex)
					{
						logger.debug("It is last node");
					}
				}
				else
				{
					nextNode = ProcessFlowServiceProxy.getNodeService().getNode(nodeStackArray[idx - 1]);
					break;
				}
			}
			
			if (nextNode != null)
				return nextNode;
			else
				throw new CustomException("", "");
		}
	
	public static Node getNextNode(String currentNodeStack)
		throws Exception
	{
		Node nextNode = ProcessFlowServiceProxy.getProcessFlowService().getNextNode(currentNodeStack, "Normal", "");
		
		return nextNode;
	}
	
	public static List<ListOrderedMap> getAlterProcessOperationByCondition(String factoryName, String processFlowName, String processFlowVersion, String processOperationName,String processOperationVersion,String TFO_CondtionName)
		throws CustomException
	{
		StringBuffer queryBuffer = new StringBuffer()
			.append("SELECT C.factoryName, C.processFlowName, C.processOperationName,\n")
			.append("       P.toProcessFlowName, P.toProcessOperationName, 			 \n")
			.append("       P.returnProcessFlowName, P.returnOperationName,		     \n")
			.append("       P.conditionName, P.conditionValue, P.reworkFlag		     \n")
			.append("FROM POSAlterProcessOperation P, TFOPolicy C                    \n")
			.append("WHERE C.conditionId = P.conditionId                             \n")
			.append("    AND C.factoryName = :FACTORYNAME                            \n")
			.append("    AND C.processFlowName = :PROCESSFLOWNAME                    \n")
			.append("    AND C.processFlowVersion = :PROCESSFLOWVERSION              \n")
			.append("    AND C.processOperationName = :PROCESSOPERATIONNAME          \n")
			.append("    AND C.processOperationVersion = :PROCESSOPERATIONVERSION    \n")
			.append("    AND P.conditionName = :CONDITIONNAME    \n");
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("PROCESSFLOWVERSION", processFlowVersion);
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		bindMap.put("PROCESSOPERATIONVERSION", processOperationVersion);
		bindMap.put("CONDITIONNAME", TFO_CondtionName);
		
		try
		{
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
			
			return result;
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "POSPolicy", fe.getMessage());
		}
	}
	
	public static List<ListOrderedMap> getAlterProcessOperation(String factoryName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String TFO_CondtionValue)
		throws CustomException
	{
		StringBuffer queryBuffer = new StringBuffer()
			.append("SELECT C.factoryName, C.processFlowName, C.processOperationName, C.processOperationVersion, \n")
			.append("       P.toProcessFlowName, P.toProcessOperationName, P.toProcessOperationVersion,			 \n")
			.append("       P.returnProcessFlowName, P.returnOperationName,	P.returnOperationVersion,  	         \n")
			.append("       P.conditionName, P.conditionValue, P.reworkFlag		     \n")
			.append("FROM POSAlterProcessOperation P, TFOPolicy C                    \n")
			.append("WHERE C.conditionId = P.conditionId                             \n")
			.append("    AND C.factoryName = :FACTORYNAME                            \n")
			.append("    AND C.processFlowName = :PROCESSFLOWNAME                    \n")
			.append("    AND C.processFlowVersion = :PROCESSFLOWVERSION              \n")
			.append("    AND C.processOperationName = :PROCESSOPERATIONNAME          \n")
			.append("    AND C.processOperationVersion = :PROCESSOPERATIONVERSION    \n")
			.append("    AND P.conditionValue = :CONDITIONVALUE    \n");
		
		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("PROCESSFLOWNAME", processFlowName);
		bindMap.put("PROCESSFLOWVERSION", processFlowVersion);
		bindMap.put("PROCESSOPERATIONNAME", processOperationName);
		bindMap.put("PROCESSOPERATIONVERSION", processOperationVersion);
		bindMap.put("CONDITIONVALUE", TFO_CondtionValue);
		
		try
		{
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
			
			return result;
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "POSPolicy", fe.getMessage());
		}
	}

	public static void validateCrossFactory(String lotName, String factoryName, String productSpecName, String targetFactoryName, String targetProductSpecName)
			throws CustomException
	{
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT C.factoryName, C.productSpecName, P.toFactoryName,").append("\n")
					.append("       P.toProductSpecName, P.shipUnit, P.jobType").append("\n")
					.append(" FROM TPPolicy C, POSFactoryRelation P").append("\n")
					.append(" WHERE C.conditionId = P.conditionId").append("\n")
					.append("  AND jobType = ?").append("\n")
					.append("  AND C.factoryName = ?").append("\n")
					.append("  AND C.productSpecName = ?").append("\n")
					.append("  AND P.toFactoryName = ?").append("\n")
					.append("  AND P.toProductSpecName = ?").append("\n");
		
		Object[] bindArray = new Object[] {"Cross", factoryName, productSpecName, targetFactoryName, targetProductSpecName};
		
		try
		{
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuilder.toString(), bindArray);

			if (result.size() < 1)
				throw new CustomException("LOT-3001", lotName, factoryName, productSpecName, targetFactoryName, targetProductSpecName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "POSFactoryRelation", fe.getMessage());
		}
	}
	
	public static List<ListOrderedMap> getPhotoMaskPolicy(Lot lotData, String machineName) throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT P.MASKNAME ");
		sql.append("  FROM POSPHOTOMASK P, TPFOMPOLICY C ");
		sql.append(" WHERE C.CONDITIONID = P.CONDITIONID ");
		sql.append("   AND C.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND C.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND C.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("   AND C.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND C.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND C.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND C.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("   AND C.MACHINENAME = :MACHINENAME ");

		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", lotData.getFactoryName());
		bindMap.put("PRODUCTSPECNAME", lotData.getProductSpecName());
		bindMap.put("PRODUCTSPECVERSION", lotData.getProductSpecVersion());
		bindMap.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
		bindMap.put("PROCESSFLOWVERSION", lotData.getProcessFlowVersion());
		bindMap.put("PROCESSOPERATIONNAME", lotData.getProcessOperationName());
		bindMap.put("PROCESSOPERATIONVERSION", lotData.getProcessOperationVersion());
		bindMap.put("MACHINENAME", machineName);

		
		try
		{
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			return result;
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "POSPolicy", fe.getMessage());
		}
	}
	
	public static List<ListOrderedMap> getAlterProcessOperationForReworkType(String factoryName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String toProcessFlowName, String toProcessFlowVersion, String toProcessOperationName, String toProcessOperationVersion, String conditionName)
			throws CustomException
		{
			StringBuffer queryBuffer = new StringBuffer()
				.append("SELECT C.factoryName, C.processFlowName, C.processOperationName, C.processOperationVersion, \n")
				.append("       P.toProcessFlowName, P.toProcessOperationName, P.toProcessOperationVersion,			 \n")
				.append("       P.returnProcessFlowName, P.returnOperationName,	P.returnOperationVersion,  	         \n")
				.append("       P.conditionName, P.conditionValue, P.reworkFlag,P.reworkCountLimit, P.reworkType		     \n")
				.append("FROM POSAlterProcessOperation P, TFOPolicy C                    \n")
				.append("WHERE C.conditionId = P.conditionId                             \n")
				.append("    AND C.factoryName = :FACTORYNAME                            \n")
				.append("    AND C.processFlowName = :PROCESSFLOWNAME                    \n")
				.append("    AND C.processFlowVersion = :PROCESSFLOWVERSION              \n")
				.append("    AND C.processOperationName = :PROCESSOPERATIONNAME          \n")
				.append("    AND C.processOperationVersion = :PROCESSOPERATIONVERSION    \n")
				.append("    AND P.conditionName = :CONDITIONNAME    \n")
				.append("    AND P.toProcessFlowName = :TOPROCESSFLOWNAME    \n")
//				.append("    AND P.toProcessFlowVersion = :TOPROCESSFLOWVERSION    \n")
				.append("    AND P.toProcessOperationName = :TOPROCESSOPERAIONNAME    \n")
//				.append("    AND P.toProcessOperationVersion = :TOPROCESSOPERAIONVERSION    \n")
				;
			
			HashMap<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("FACTORYNAME", factoryName);
			bindMap.put("PROCESSFLOWNAME", processFlowName);
			bindMap.put("PROCESSFLOWVERSION", processFlowVersion);
			bindMap.put("PROCESSOPERATIONNAME", processOperationName);
			bindMap.put("PROCESSOPERATIONVERSION", processOperationVersion);
			bindMap.put("CONDITIONNAME", conditionName);
			bindMap.put("TOPROCESSFLOWNAME", toProcessFlowName);
			bindMap.put("TOPROCESSFLOWVERSION", toProcessFlowVersion);
			bindMap.put("TOPROCESSOPERAIONNAME", toProcessOperationName);
			bindMap.put("TOPROCESSOPERAIONVERSION", toProcessOperationVersion);
			
			
			try
			{
				List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(queryBuffer.toString(), bindMap);
				
				return result;
			}
			catch (FrameworkErrorSignal fe)
			{
				throw new CustomException("SYS-9999", "POSPolicy", fe.getMessage());
			}
		}
	public static ListOrderedMap getMachineRecipeNameForOLEDMask(String factoryName, 
			  String maskSpecName, 
			  String processFlowName, 
			  String processFlowVersion,
			  String processOperationName, 
			  String processOperationVersion,
			  String machineName)
	throws CustomException
	{
		StringBuffer sqlBuffer = new StringBuffer("")
			.append(" SELECT P.MACHINERECIPENAME").append(NEWLINE)
			.append("   FROM TRFOPOLICY T, POSMACHINE P ").append(NEWLINE)
			.append("  WHERE 1=1 ").append(NEWLINE)
			.append("	 AND T.FACTORYNAME= ? ").append(NEWLINE)
			.append("    AND T.MASKSPECNAME = ? ").append(NEWLINE)
			.append("    AND T.PROCESSFLOWNAME = ? ").append(NEWLINE)
			.append("    AND T.PROCESSFLOWVERSION = ? ").append(NEWLINE)
			.append("    AND T.PROCESSOPERATIONNAME= ? ").append(NEWLINE)
			.append("    AND T.PROCESSOPERATIONVERSION = ? ").append(NEWLINE)
			.append("    AND P.MACHINENAME = ?").append(NEWLINE)
			.append("    AND T.CONDITIONID = P.CONDITIONID ").append(NEWLINE)
			.append("").append(NEWLINE);
	
		String sqlStmt = sqlBuffer.toString();
	
		Object[] bindSet = new String[]{factoryName, maskSpecName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName};
	
		try
		{
			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt, bindSet);
		
			if(sqlResult.size() > 0)
				return sqlResult.get(0);
			else 
				throw new CustomException("SYS-9999", "POSMachine",
						String.format("Machine[%s] is not enable with [%s, %s, %s, %s]", machineName, factoryName, maskSpecName, processFlowName, processOperationName));
		}
		catch (FrameworkErrorSignal de)
		{
			throw new CustomException("SYS-9999", "POSMachine", de.getMessage());
		}
	}
	
	public static String getRecipeCheckLevelForOLEDMask(String factoryName, 
			  String maskSpecName, 
			  String processFlowName, 
			  String processFlowVersion,
			  String processOperationName, 
			  String processOperationVersion,
			  String machineName
	)
	throws CustomException
	{
		StringBuffer sqlBuffer = new StringBuffer("")
		.append(" SELECT P.CHECKLEVEL").append(NEWLINE)
		.append("   FROM TRFOPOLICY T, POSMACHINE P ").append(NEWLINE)
		.append("  WHERE 1=1 ").append(NEWLINE)
		.append("	 AND T.FACTORYNAME= ? ").append(NEWLINE)
		.append("    AND T.MASKSPECNAME = ? ").append(NEWLINE)
		.append("    AND T.PROCESSFLOWNAME = ? ").append(NEWLINE)
		.append("    AND T.PROCESSFLOWVERSION = ? ").append(NEWLINE)
		.append("    AND T.PROCESSOPERATIONNAME= ? ").append(NEWLINE)
		.append("    AND T.PROCESSOPERATIONVERSION = ? ").append(NEWLINE)
		.append("    AND P.MACHINENAME = ?").append(NEWLINE)
		.append("    AND T.CONDITIONID = P.CONDITIONID ").append(NEWLINE)
		.append("").append(NEWLINE);

		String sqlStmt = sqlBuffer.toString();

		Object[] bindSet = new String[]{factoryName, maskSpecName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, machineName};

		try
		{
			List<ListOrderedMap> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlStmt, bindSet);
	
			if(sqlResult != null && sqlResult.size() > 0)
				return sqlResult.get(0).get("CHECKLEVEL").toString();
			else 
				throw new CustomException("SYS-9999", "POSMachine",
						String.format("Machine[%s] is not enable with [%s, %s, %s, %s]", machineName, factoryName, maskSpecName, processFlowName, processOperationName));
		}
		catch (FrameworkErrorSignal de)
		{
			throw new CustomException("SYS-9999", "POSMachine", de.getMessage());
		}
	}
	
	public static List<ListOrderedMap> getNodeList(Lot lotData)throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT NODEATTRIBUTE1  ");
		sql.append(" FROM NODE WHERE ");
		sql.append(" FACTORYNAME = :FACTORYNAME ");
		sql.append(" AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append(" AND NODEATTRIBUTE1 IS NOT NULL ");

		HashMap<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("FACTORYNAME", lotData.getFactoryName());
		bindMap.put("PROCESSFLOWNAME", lotData.getProcessFlowName());
		bindMap.put("PROCESSFLOWVERSION", lotData.getProcessFlowVersion());

		try
		{
			List<ListOrderedMap> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
			return result;
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "Node", fe.getMessage());
		}
	}
	
	public static List<Map<String, Object>> getTSMachineInfo(String factoryName, String maskStickSpecName, String machineName) throws CustomException 
	{
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT PM.*  ");
		sql.append("FROM POSMACHINE PM, TSPOLICY TP ");
		sql.append("WHERE PM.CONDITIONID = TP.CONDITIONID ");
		sql.append("    AND TP.FACTORYNAME = :FACTORYNAME ");
		sql.append("    AND TP.MASKSTICKSPECNAME = :MASKSTICKSPECNAME ");
		sql.append("    AND PM.MACHINENAME = :MACHINENAME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryName);
		args.put("MASKSTICKSPECNAME", maskStickSpecName);
		args.put("MACHINENAME", machineName);

		try 
		{
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (sqlResult != null && sqlResult.size() > 0) 
			{
				String RMSFlag = ConvertUtil.getMapValueByName(sqlResult.get(0), "RMSFLAG");

				if (StringUtils.isEmpty(RMSFlag)) 
				{
					RMSFlag = "N";
				}

				return sqlResult;
			} 
			else 
			{
				//CUSTOM-0024: Machine[{0}] is not enable with [PosTSMachine={1}, {2}]
				throw new CustomException("CUSTOM-0024", machineName, factoryName, maskStickSpecName);
			}
		} 
		catch (FrameworkErrorSignal de) 
		{
			throw new CustomException("SYS-9999", "PosTSMachine", de.getMessage());
		}
	}
	
	public static List<Map<String, Object>> getTRFOAbnormalRecipeInfo(String factoryName, String maskSpecName, String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion, String machineName, String recipeName) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT PM.MACHINERECIPENAME, PM.RMSFLAG, PM.ECRECIPEFLAG, PM.ECRECIPENAME, PM.MASKCYCLETARGET ");
		sql.append("  FROM TRFOPOLICY TR, POSABNORMALMACHINE PM ");
		sql.append(" WHERE TR.CONDITIONID = PM.CONDITIONID ");
		sql.append("   AND TR.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND TR.MASKSPECNAME = :MASKSPECNAME ");
		sql.append("   AND TR.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND TR.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND TR.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND TR.PROCESSOPERATIONVERSION = :PROCESSOPERATIONVERSION ");
		sql.append("   AND PM.MACHINENAME = :MACHINENAME ");
		sql.append("   AND PM.MACHINERECIPENAME = :MACHINERECIPENAME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryName);
		args.put("MASKSPECNAME", maskSpecName);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("PROCESSOPERATIONNAME", processOperationName);
		args.put("PROCESSOPERATIONVERSION", processOperationVersion);
		args.put("MACHINENAME", machineName);
		args.put("MACHINERECIPENAME", recipeName);
		
		try 
		{
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

			if (sqlResult != null && sqlResult.size() > 0) 
			{
				String RMSFlag = ConvertUtil.getMapValueByName(sqlResult.get(0), "RMSFLAG");

				if (StringUtils.isEmpty(RMSFlag)) 
				{
					RMSFlag = "N";
				}

				return sqlResult;
			} 
			else 
			{
				//CUSTOM-0025: Machine[{0}] Recipe[{1}] is not enable with [PosTSMachine={2}, {3}, {4},{5}]
				throw new CustomException("CUSTOM-0025",  machineName, recipeName, factoryName, maskSpecName, processFlowName, processOperationName);
			}
		} 
		catch (FrameworkErrorSignal de) 
		{
			throw new CustomException("SYS-9999", "PosAbnormalMachine", de.getMessage());
		}
	}
}
