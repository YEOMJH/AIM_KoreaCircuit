package kr.co.aim.messolution.lot.event.CNX;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import com.sun.org.apache.xpath.internal.operations.Number;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.POSAlterProcessOperation;
import kr.co.aim.messolution.extended.object.management.data.POSMachine;
import kr.co.aim.messolution.extended.object.management.data.POSQueueTime;
import kr.co.aim.messolution.extended.object.management.data.POSSample;
import kr.co.aim.messolution.extended.object.management.data.TFOMPolicy;
import kr.co.aim.messolution.extended.object.management.data.TFOPolicy;
import kr.co.aim.messolution.extended.object.management.data.TPFOPolicy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.GenericServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Arc;
import kr.co.aim.greentrack.processflow.management.data.ArcKey;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.NodeKey;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.messolution.generic.master.ConstantMap;

public class ProcessFlowCopyLot extends SyncHandler 
{
	private static Log log = LogFactory.getLog(ProcessFlowCopyLot.class);
	
	@Override
	public Object doWorks(Document doc)throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String flowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String flowName = SMessageUtil.getBodyItemValue(doc, "PROCSSFLOWNAME", true);
		String newFlowName = SMessageUtil.getBodyItemValue(doc, "NEWPROCSSFLOWNAME", true);
		
		//Policy Flag
		String processFlowPolicyFlag = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWPOLICY", true);
		String reworkPolicyFlag = SMessageUtil.getBodyItemValue(doc, "REWORKPOLICY", true);
		String qtimePolicyFlag = SMessageUtil.getBodyItemValue(doc, "QTIMEPOLICY", true);		
		String samplingPolicyFlag = SMessageUtil.getBodyItemValue(doc, "SAMPLINGPOLICY", true);
		String tpfoPolicyFlag = SMessageUtil.getBodyItemValue(doc, "TPFOPOLICY", true);
		String E2PFlag = SMessageUtil.getBodyItemValue(doc, "E2P", true);
		//String tpomPolicyFlag = SMessageUtil.getBodyItemValue(doc, "TPOMPOLICY", true);

		//新建TPFOPolicy时使用 
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
		String newProdcutSpecName = SMessageUtil.getBodyItemValue(doc, "NEWPRODUCTSPECNAME", false);
		
		/*======================================================================================*/
		//Flow Copy功能
		// [start]
		if (StringUtil.equals(processFlowPolicyFlag, "true"))
		{
			log.info("Start Flow Copy");
			
			String nodeID = "";
			String originalNodeID = "";
			String originalToNodeId = "";
			String originalFromNodeId = "";
			String toNodeId = "";
			String fromNodeId = "";
			
			ProcessFlowKey newKeyInfo = new ProcessFlowKey(factoryName, newFlowName, flowVersion);
			ProcessFlowKey keyInfo = new ProcessFlowKey(factoryName, flowName, flowVersion);
	
			Calendar cal = Calendar.getInstance(); 
			SimpleDateFormat timeFormat = new SimpleDateFormat("yyMMddHHmmss"); 
			String simpleTime = timeFormat.format(cal.getTime());
			
			//event Data
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);
	
			//flow Data
			ProcessFlow processFlowData = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(keyInfo);
			String flowDesc = processFlowData.getDescription();					
			
			//copy时chkState固定为"CheckedIn"状态,防止出现异常（copy对象Flow点Modify后变为CheckOut状态，Copy完成后NewFlow也为CheckOut，只能改DB为CheckIn）              
			String chkState = "CheckedIn";
			
			String actState = processFlowData.getActiveState();
			String flowType = processFlowData.getProcessFlowType();
			String processFlowUsedType = processFlowData.getUdfs().get("PROCESSFLOWUSEDTYPE");
			String detailProcessFlowType = processFlowData.getUdfs().get("DETAILPROCESSFLOWTYPE");
			Map<String, String> processFlowUdfs = new HashMap<String, String>();
			processFlowUdfs.put("PROCESSFLOWUSEDTYPE", processFlowUsedType);
			processFlowUdfs.put("DETAILPROCESSFLOWTYPE", detailProcessFlowType);
			
			//Insert new Flow Name
			ProcessFlow dataInfo = new ProcessFlow();
		
			dataInfo.setKey(newKeyInfo);
			dataInfo.setDescription(flowDesc);
			dataInfo.setCheckState(chkState);
			dataInfo.setActiveState(actState);
			dataInfo.setCreateTime(eventInfo.getEventTime());
			dataInfo.setCreateUser(eventInfo.getEventUser());
			dataInfo.setProcessFlowType(flowType);
			dataInfo.setUdfs(processFlowUdfs);
		
			ProcessFlowServiceProxy.getProcessFlowService().insert(dataInfo);
			log.info("Insert By New Flow : " + newFlowName);
			
			//Operation Data
			//Node Data
			//Arc Data
			//Select NODE
			HashMap<String, Object> bindMapNode = new HashMap<String, Object>();
			String sqlNode = "SELECT N.NODEID, " +
							 " N.NODETYPE, " +
							 " N.NODEATTRIBUTE1, " +
							 " N.NODEATTRIBUTE2, " +
							 " N.XCOORDINATE, " +
							 " N.YCOORDINATE " +
							 "  FROM NODE N " +
							 " WHERE     1 = 1 " +
							 "   AND N.FACTORYNAME = :FACTORYNAME " +
							 "   AND N.PROCESSFLOWNAME = :PROCESSFLOWNAME " +
							 "   AND N.PROCESSFLOWVERSION = :PROCESSFLOWVERSION " ;
	
			bindMapNode.put("FACTORYNAME", factoryName);
			bindMapNode.put("PROCESSFLOWNAME", flowName);
			bindMapNode.put("PROCESSFLOWVERSION", flowVersion);
	        
			List<ListOrderedMap> sqlNodeResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlNode, bindMapNode);
			HashSet<Integer> set  = new HashSet<Integer>();
			randomSet(10000,99999,sqlNodeResult.size(),set);
			
			Iterator<Integer> iterator = set.iterator();
			Map<String, String> bindMap = new HashMap<String, String>(); 
			
			if(sqlNodeResult.size() > 0)
			{
				for(int j = 0; j < sqlNodeResult.size(); j++)
				{
					while(iterator.hasNext())
					{						
						nodeID = simpleTime.substring(2, simpleTime.length()) + iterator.next().toString();
						bindMap.put(sqlNodeResult.get(j).get("NODEID").toString(),nodeID.toString());
						//bindMap.put("A"+sqlNodeResult.get(j).get("NODEID").toString(),nodeID.toString());
						
						String oldNodeID = "";
		
						HashMap<String, Object> bindMapCheckNode = new HashMap<String, Object>();
						String sqlCheckNode = "SELECT N.NODEID " +
									 	 	  "  FROM NODE N " +
									 	 	  " WHERE     1 = 1 " +
									 	 	  "   AND N.NODEID = :NODEID ";
		
						bindMapCheckNode.put("NODEID", nodeID);
						
						List<ListOrderedMap> sqlCheckNodeResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlCheckNode, bindMapCheckNode);
						
						if(sqlCheckNodeResult.size() > 0)
						{
							log.info("Node ID["+ nodeID +"] Already Exist");
							throw new CustomException("Node ID["+ nodeID +"] Already Exist", null);
						}
						else
						{
							if(j != 0)
							{
								oldNodeID = simpleTime + sqlNodeResult.get(j - 1).get("NODEID").toString();
							}
							if(!nodeID.equalsIgnoreCase(oldNodeID))
							{
								NodeKey nodeKeyInfo = new NodeKey();
								nodeKeyInfo.setNodeId(nodeID);
								
								Node nodeData = new Node();
								nodeData.setKey(nodeKeyInfo);
								nodeData.setFactoryName(factoryName);
								nodeData.setXCoordinate(Integer.parseInt(sqlNodeResult.get(j).get("XCOORDINATE").toString()));
								nodeData.setYCoordinate(Integer.parseInt(sqlNodeResult.get(j).get("YCOORDINATE").toString()));
								if(!sqlNodeResult.get(j).get("NODETYPE").toString().equalsIgnoreCase("Start")&& !sqlNodeResult.get(j).get("NODETYPE").toString().equalsIgnoreCase("End")
										&&!sqlNodeResult.get(j).get("NODETYPE").toString().equalsIgnoreCase("ConditionalConvergence"))
									
								{
									nodeData.setNodeAttribute1(sqlNodeResult.get(j).get("NODEATTRIBUTE1").toString());
									nodeData.setNodeAttribute2(sqlNodeResult.get(j).get("NODEATTRIBUTE2").toString());
								}								
								else 
								{
									nodeData.setNodeAttribute1("");
									nodeData.setNodeAttribute2("");
								}
								nodeData.setNodeType(sqlNodeResult.get(j).get("NODETYPE").toString());
								
								nodeData.setProcessFlowName(newFlowName);
								nodeData.setProcessFlowVersion(flowVersion);
								
								ProcessFlowServiceProxy.getNodeService().insert(nodeData);
								log.info("Insert By New Node : " + newFlowName + " by " + simpleTime + nodeID + " : " + j  + "rows");
							}
						}
					break;
					}
				}
			}
			else
			{
				throw new CustomException("Node Data Not Exist", null);
			}
	
			//Arc Data
			HashMap<String, Object> bindMapArc = new HashMap<String, Object>();
			String sqlArc = "SELECT A.FACTORYNAME,                           " +
							"       A.ARCTYPE,                               " +
							"       A.FROMNODEID,                            " +
							"       A.TONODEID,                              " +
							"       A.PROCESSFLOWNAME,                       " +
							"       A.PROCESSFLOWVERSION,                    " +
							"       A.ARCATTRIBUTE                           " +
							"  FROM ARC A                                    " +
							" WHERE     1 = 1                                " +
							"       AND A.PROCESSFLOWNAME = :PROCESSFLOWNAME " ;
	
			bindMapArc.put("PROCESSFLOWNAME", flowName);
			
			List<ListOrderedMap> sqlArcResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlArc, bindMapArc);
			
			if(sqlArcResult.size() > 0)
			{
				for(int u = 0; u < sqlArcResult.size(); u++)
				{
					//Insert ARC
					originalFromNodeId 	= sqlArcResult.get(u).get("FROMNODEID").toString();
					originalToNodeId 	= sqlArcResult.get(u).get("TONODEID").toString();
					
					fromNodeId 	= bindMap.get(originalFromNodeId);
					toNodeId 	= bindMap.get(originalToNodeId);
										
					HashMap<String, Object> bindMapCheckArc = new HashMap<String, Object>();
					String sqlCheckNode = "SELECT A.TONODEID " +
								 	 	  "  FROM ARC A  " +
								 	 	  " WHERE     1 = 1 " +
								 	 	  "   AND A.TONODEID = :TONODEID ";
	
					bindMapCheckArc.put("TONODEID", toNodeId);
					
					List<ListOrderedMap> sqlCheckArcResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlCheckNode, bindMapCheckArc);
					
					if(sqlCheckArcResult.size() > 0)
					{
						log.info("Node ID["+ toNodeId +"] Already Exist");
					}
					else
					{
						ArcKey arcKeyInfo = new ArcKey(fromNodeId, toNodeId);
						Arc arcData = new Arc();
						arcData.setKey(arcKeyInfo);
						
						arcData.setFactoryName(factoryName);
						arcData.setArcType(sqlArcResult.get(u).get("ARCTYPE").toString());
						arcData.setProcessFlowName(newFlowName);
						arcData.setProcessFlowVersion(flowVersion);
						if(sqlArcResult.get(u).get("ARCTYPE").toString().equalsIgnoreCase("Conditional"))
						{
							arcData.setArcAttribute(sqlArcResult.get(u).get("ARCATTRIBUTE").toString());
						}
						else
						{
							arcData.setArcAttribute("");
						}
		
						ProcessFlowServiceProxy.getArcService().insert(arcData);
						log.info("Insert By New Arc : " + newFlowName + " by " + sqlArcResult.get(u).get("FROMNODEID").toString());
					}
				}
			}
			else
			{
				throw new CustomException("Arc Data Not Exist", null);
			}
		}
		// [end]
		
		/*==============================================================================================*/
		
		//A. Rework Policy & Qtime Policy Copy
		//因Rework Policy与Qtime Policy共用TFOPolicy表，先后Copy的时间段内若人为修改某一Policy会产生问题，故要求二者一起Copy
		//注：当Rework Policy & Qtime Policy Copy不存在时将只Copy TFOPolicy表
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("FlowCopy", getEventUser(), getEventComment());
		if (StringUtil.equals(reworkPolicyFlag, "true") && StringUtil.equals(qtimePolicyFlag, "true"))
		{
			//1.TFOPolicy 
			//[start]
			log.info("Start TFOPolicy Copy.");
			
			String condition = " where factoryname = ? and processflowname = ? and processflowversion = ? ";
			Object[] bindSet = new Object[] { factoryName, flowName, flowVersion };
			List<TFOPolicy> TFOPolicyList = new ArrayList<TFOPolicy>();
			
			try
			{
				TFOPolicyList = ExtendedObjectProxy.getTFOPolicyService().select(condition, bindSet);
			}
			catch (greenFrameDBErrorSignal e)
			{
				//CUSTOM-0002:Quary for Source {0}Policy Error
				throw new CustomException ("CUSTOM-0002", "TFO");
			}
			
			
			for (TFOPolicy TFOPolicyData : TFOPolicyList)
			{
				TFOPolicyData.setPROCESSFLOWNAME(newFlowName);
				String conditionID = getTFOPolicyConditionID(TFOPolicyData);
				if (StringUtil.isNotEmpty(conditionID))
				{
					String Newcondition = " where factoryname = ? and processflowname = ? and processflowversion = ? and conditionID = ?";
					Object[] NewbindSet = new Object[] { factoryName, newFlowName, flowVersion,conditionID };
					List<TFOPolicy> NewTFOPolicyList = new ArrayList<TFOPolicy> ();
					try
					{
						NewTFOPolicyList = ExtendedObjectProxy.getTFOPolicyService().select(Newcondition, NewbindSet);
					}
					catch (greenFrameDBErrorSignal e)
					{
						TFOPolicyData.setCONDITIONID(conditionID);
						try
						{
							ExtendedObjectProxy.getTFOPolicyService().create(TFOPolicyData);
						}
						catch (greenFrameDBErrorSignal error)
						{
							//CUSTOM-0003: Copy {0}Policy  Failed
							throw new CustomException ("CUSTOM-0003","TFO");
						}
					}
				}
				else
				{
					//CUSTOM-0004: Get the {0}Policy Condition ID Failed
					throw new CustomException ("CUSTOM-0004" ,"TFO");
				}
			}
			
			log.info("TFOPolicy Copy Success.");
			//[end]
			
			//2.posalterprocessoperation
			//[start]
			log.info("Start Rework Policy Copy.");
			
			String reworkQry = " SELECT B.FACTORYNAME,B.PROCESSFLOWNAME,B.PROCESSFLOWVERSION,B.PROCESSOPERATIONNAME,"
					+ "B.PROCESSOPERATIONVERSION,A.CONDITIONID,A.CONDITIONNAME,A.CONDITIONVALUE,A.PRODUCTIONTYPE,A.RETURNOPERATIONNAME,"
					+ "A.RETURNOPERATIONVERSION,A.RETURNPROCESSFLOWNAME,A.RETURNPROCESSFLOWVERSION,A.REWORKCOUNTLIMIT,A.REWORKCOUNTLIMITBYTYPE,"
					+ "A.REWORKFLAG,A.REWORKTYPE,A.TOPROCESSFLOWNAME,A.TOPROCESSFLOWVERSION,A.TOPROCESSOPERATIONNAME,A.TOPROCESSOPERATIONVERSION "
					+ "FROM POSALTERPROCESSOPERATION A ,TFOPOLICY B "
					+ "WHERE A.CONDITIONID = B.CONDITIONID AND B.FACTORYNAME = ? AND B.PROCESSFLOWNAME = ? AND B.PROCESSFLOWVERSION = ?";
				
			String[] reworkBindSet = new String[]{ factoryName,flowName,  flowVersion };
			List<Map<String, String>> reworkList = greenFrameServiceProxy.getSqlTemplate().queryForList(reworkQry, reworkBindSet);

			
			if (reworkList.size() == 0)
			{
				log.info("原始Rework Policy不存在(Quary for Source Rework Policy Error)");
			}
			else
			{
				for (Map<String, String> reworkData : reworkList)
				{
					//check ProcessOperationName Exist
					this.checkOperationExist(newFlowName,reworkData.get("PROCESSOPERATIONNAME"),reworkData.get("FACTORYNAME"),flowVersion);
					//Check ReturnProcessOperationName Exist
					this.checkOperationExist(newFlowName,reworkData.get("RETURNOPERATIONNAME"),reworkData.get("FACTORYNAME"),flowVersion);
					TFOPolicy temp = new TFOPolicy();
					
					temp.setFACTORYNAME(reworkData.get("FACTORYNAME"));
					temp.setPROCESSFLOWNAME(newFlowName);
					temp.setPROCESSFLOWVERSION(reworkData.get("PROCESSFLOWVERSION"));
					temp.setPROCESSOPERATIONNAME(reworkData.get("PROCESSOPERATIONNAME"));
					temp.setPROCESSOPERATIONVERSION(reworkData.get("PROCESSOPERATIONVERSION"));
		
					String conditionID = getTFOPolicyConditionID(temp);
					//因TFOPolicy Copy中已对conditionID作空判断，故在此无需再作判断
					
					POSAlterProcessOperation reworkPolicy = new POSAlterProcessOperation();
					
					reworkPolicy.setCONDITIONID(conditionID);
					reworkPolicy.setPRODUCTIONTYPE(reworkData.get("PRODUCTIONTYPE"));
					reworkPolicy.setCONDITIONNAME(reworkData.get("CONDITIONNAME"));
					reworkPolicy.setCONDITIONVALUE(reworkData.get("CONDITIONVALUE"));
					reworkPolicy.setTOPROCESSFLOWVERSION(reworkData.get("TOPROCESSFLOWVERSION"));
					reworkPolicy.setRETURNOPERATIONNAME(reworkData.get("RETURNOPERATIONNAME"));
					reworkPolicy.setRETURNOPERATIONVERSION(reworkData.get("RETURNOPERATIONVERSION"));
					reworkPolicy.setRETURNPROCESSFLOWNAME(newFlowName);
					reworkPolicy.setREWORKFLAG(reworkData.get("REWORKFLAG"));
					reworkPolicy.setREWORKCOUNTLIMIT(reworkData.get("REWORKCOUNTLIMIT"));
					reworkPolicy.setREWORKCOUNTLIMITBYTYPE(reworkData.get("REWORKCOUNTLIMITBYTYPE"));
					reworkPolicy.setRETURNPROCESSFLOWVERSION(reworkData.get("RETURNPROCESSFLOWVERSION"));
					reworkPolicy.setREWORKTYPE(reworkData.get("REWORKTYPE"));					
					
				    reworkPolicy.setTOPROCESSFLOWNAME(reworkData.get("TOPROCESSFLOWNAME"));				
					reworkPolicy.setTOPROCESSOPERATIONNAME(reworkData.get("TOPROCESSOPERATIONNAME"));
					reworkPolicy.setTOPROCESSOPERATIONVERSION(reworkData.get("TOPROCESSOPERATIONVERSION"));;
		
					try 
					{
						ExtendedObjectProxy.getPOSAlterProcessOperationService().create(eventInfo, reworkPolicy);
					}
					catch (greenFrameDBErrorSignal e)
					{
						//CUSTOM-0003: Copy {0}Policy  Failed
						throw new CustomException ("CUSTOM-0003","Rework");
					}
				}
				
				log.info("Rework Policy Copy Success.");
			}
			//[end]
			
			//3.posqueuetime
			//[start]
			log.info("Start Qtime Policy Copy.");
			
			String qtimeQry = "SELECT A.FACTORYNAME,A.PROCESSFLOWNAME,A.PROCESSFLOWVERSION,A.PROCESSOPERATIONNAME,A.PROCESSOPERATIONVERSION,"
					+ "B.CONDITIONID,B.TOFACTORYNAME,B.TOPROCESSFLOWNAME,B.TOPROCESSOPERATIONNAME,B.WARNINGDURATIONLIMIT,B.INTERLOCKDURATIONLIMIT,"
					+ "B.REVIEWSTATIONQTIMELIMIT "
					+ "FROM TFOPOLICY A,POSQUEUETIME B "
					+ "WHERE A.CONDITIONID = B.CONDITIONID AND A.PROCESSFLOWNAME = ? AND A.FACTORYNAME = ? AND A.PROCESSFLOWVERSION = ?";
			
			String[] qtimeBindSet = new String[]{ flowName, factoryName, flowVersion };
			List<Map<String, Object>> qtimeList = greenFrameServiceProxy.getSqlTemplate().queryForList(qtimeQry, qtimeBindSet);
			
			//case not found
			if (qtimeList.size() == 0)
			{
				log.info("原始 Qtime Policy不存在(Quary for Source Qtime Policy Error)");
			}
			else
			{
				for (Map<String, Object> qtimeData : qtimeList)
				{
					//check ProcessOperationName
					this.checkOperationExist(newFlowName,qtimeData.get("PROCESSOPERATIONNAME").toString(),qtimeData.get("FACTORYNAME").toString(),flowVersion);
					//check TOProcessOperationName
					this.checkOperationExist(newFlowName,qtimeData.get("TOPROCESSOPERATIONNAME").toString(),qtimeData.get("FACTORYNAME").toString(),flowVersion);
					TFOPolicy temp = new TFOPolicy();
					
					temp.setFACTORYNAME(qtimeData.get("FACTORYNAME").toString());
					temp.setPROCESSFLOWNAME(newFlowName);
					temp.setPROCESSFLOWVERSION(qtimeData.get("PROCESSFLOWVERSION").toString());
					temp.setPROCESSOPERATIONNAME(qtimeData.get("PROCESSOPERATIONNAME").toString());
					temp.setPROCESSOPERATIONVERSION(qtimeData.get("PROCESSOPERATIONVERSION").toString());
		
					String conditionID = getTFOPolicyConditionID(temp);
					//因TFOPolicy Copy中已对conditionID作空判断，故在此无需再作判断
					
					POSQueueTime qtimePolicy = new POSQueueTime();
					
					qtimePolicy.setCONDITIONID(conditionID);
					qtimePolicy.setTOFACTORYNAME(qtimeData.get("TOFACTORYNAME").toString());
					qtimePolicy.setTOPROCESSFLOWNAME(newFlowName);
					qtimePolicy.setTOPROCESSOPERATIONNAME(qtimeData.get("TOPROCESSOPERATIONNAME").toString());
					if(qtimeData.get("WARNINGDURATIONLIMIT")!=null)
					qtimePolicy.setWARNINGDURATIONLIMIT(qtimeData.get("WARNINGDURATIONLIMIT").toString());
					if(qtimeData.get("INTERLOCKDURATIONLIMIT")!=null)
					qtimePolicy.setINTERLOCKDURATIONLIMIT(qtimeData.get("INTERLOCKDURATIONLIMIT").toString());
					if(qtimeData.get("REVIEWSTATIONQTIMELIMIT")!=null)
					{
						//BigDecimal reviewStationQtimeLimit = new BigDecimal(qtimeData.get("REVIEWSTATIONQTIMELIMIT").toString());
						Double reviewStationQtimeLimit = Double.valueOf(qtimeData.get("REVIEWSTATIONQTIMELIMIT").toString());
						qtimePolicy.setREVIEWSTATIONQTIMELIMIT(reviewStationQtimeLimit);
					}
		
					try
					{
						ExtendedObjectProxy.getPOSQueueTimeService().create(eventInfo, qtimePolicy);
					}
					catch (greenFrameDBErrorSignal e)
					{
						//CUSTOM-0003: Copy {0}Policy  Failed
						throw new CustomException ("CUSTOM-0003","Qtime");
					}
				}
				
				log.info("Qtime Policy Copy Success.");
			}
		}
	
		//B. Sampling Policy Copy
		if (StringUtil.equals(samplingPolicyFlag, "true"))
		{
			//4.TFOMPolicy
			//[start]
			log.info("Start TFOM Policy Copy.");
			
			String TFOMCondition = " where factoryname = ? and processflowname = ? and processflowversion = ? ";
			Object[] TFOMBindSet = new Object[] { factoryName, flowName, flowVersion };
	
			List<TFOMPolicy> TFOMPolicyList = new ArrayList<TFOMPolicy>();
			try
			{
				TFOMPolicyList = ExtendedObjectProxy.getTFOMPolicyService().select(TFOMCondition, TFOMBindSet);
			}
			catch (greenFrameDBErrorSignal e)
			{
				//CUSTOM-0002: Quary for Source {0}Policy Error
				throw new CustomException ("CUSTOM-0002","TFOM");
			}
			
			for (TFOMPolicy TFOMPolicyData : TFOMPolicyList)
			{
				TFOMPolicyData.setPROCESSFLOWNAME(newFlowName);
				String conditionID = getTFOMPolicyConditionID(TFOMPolicyData);
			
				if (StringUtil.isNotEmpty(conditionID))
				{
					TFOMPolicyData.setCONDITIONID(conditionID);
					
					try
					{
						ExtendedObjectProxy.getTFOMPolicyService().create(TFOMPolicyData);
					}
					catch (greenFrameDBErrorSignal e)
					{
						//CUSTOM-0003: Copy {0}Policy  Failed
						throw new CustomException ("CUSTOM-0003", "TFOM");
					}
				}
				else
				{
					//CUSTOM-0004: Get the {0}Policy Condition ID Failed    
					throw new CustomException ("CUSTOM-0004","TFOM");
				}
			}
			
			log.info("TFOM Policy Copy Success.");
			//[end]
			
			//5.POSSample
			//[start]
			log.info("Start Sample Policy Copy.");
			
			String sampleQry = "SELECT A.FACTORYNAME,A.PROCESSFLOWNAME,A.PROCESSFLOWVERSION,A.PROCESSOPERATIONNAME,A.PROCESSOPERATIONVERSION,"
					+ "A.MACHINENAME,A.CONDITIONID,A.DCSPECNAME,A.DCSPECVERSION,B.TOPROCESSFLOWNAME,B.TOPROCESSFLOWVERSION,"
					+ "B.TOPROCESSOPERATIONNAME,B.TOPROCESSOPERATIONVERSION,B.FLOWPRIORITY,B.LOTSAMPLINGCOUNT,B.PRODUCTSAMPLINGCOUNT,"
					+ "B.PRODUCTSAMPLINGPOSITION,B.RETURNOPERATIONNAME,B.RETURNOPERATIONVER "
					+ "FROM TFOMPOLICY A,POSSAMPLE B "
					+ "WHERE A.CONDITIONID = B.CONDITIONID AND A.PROCESSFLOWNAME = ? AND A.FACTORYNAME = ? AND A.PROCESSFLOWVERSION = ?";
		
			String[] sampleBindSet = new String[]{ flowName, factoryName, flowVersion };
			List<Map<String, Object>> sampleList = greenFrameServiceProxy.getSqlTemplate().queryForList(sampleQry, sampleBindSet);
			
			if (sampleList.size() == 0)
			{
				//CUSTOM-0003: Copy {0}Policy  Failed
				throw new CustomException ("CUSTOM-0003","Sampling");
			}
			
			
			for (Map<String, Object> SampleData : sampleList)
			{
				//check ProcessOperationName
				this.checkOperationExist(newFlowName,SampleData.get("PROCESSOPERATIONNAME").toString(),SampleData.get("FACTORYNAME").toString(),flowVersion);
				//check ReturnProcessOperationName
				this.checkOperationExist(newFlowName,SampleData.get("RETURNOPERATIONNAME").toString(),SampleData.get("FACTORYNAME").toString(),flowVersion);
				
				TFOMPolicy temp = new TFOMPolicy();
				
				temp.setFACTORYNAME(SampleData.get("FACTORYNAME").toString());
				temp.setPROCESSFLOWNAME(newFlowName);
				temp.setPROCESSFLOWVERSION(SampleData.get("PROCESSFLOWVERSION").toString());
				temp.setPROCESSOPERATIONNAME(SampleData.get("PROCESSOPERATIONNAME").toString());
				temp.setPROCESSOPERATIONVERSION(SampleData.get("PROCESSOPERATIONVERSION").toString());
				temp.setMACHINENAME(SampleData.get("MACHINENAME").toString());
				
				String conditionID = getTFOMPolicyConditionID(temp);
				//因TFOMPolicy Copy中已对conditionID作空判断，故在此无需再作判断
				
				POSSample samplePolicy = new POSSample();
				
				samplePolicy.setCONDITIONID(conditionID);
				samplePolicy.setTOPROCESSFLOWNAME(SampleData.get("TOPROCESSFLOWNAME").toString());
				samplePolicy.setTOPROCESSFLOWVERSION(SampleData.get("TOPROCESSFLOWVERSION").toString());
				samplePolicy.setTOPROCESSOPERATIONNAME(SampleData.get("TOPROCESSOPERATIONNAME").toString());
				samplePolicy.setTOPROCESSOPERATIONVERSION(SampleData.get("TOPROCESSOPERATIONVERSION").toString());
				if(SampleData.get("FLOWPRIORITY")!=null)				
				{
					Double flowPriority = Double.valueOf(SampleData.get("FLOWPRIORITY").toString());
					samplePolicy.setFLOWPRIORITY(flowPriority);
				}
				else
				{
					//CUSTOM-0020: {0} is null
					throw new CustomException ("CUSTOM-0020", "FlowPriority");
				}

				if(SampleData.get("LOTSAMPLINGCOUNT")!=null)
				samplePolicy.setLOTSAMPLINGCOUNT(SampleData.get("LOTSAMPLINGCOUNT").toString());
				if(SampleData.get("PRODUCTSAMPLINGCOUNT")!=null)
				samplePolicy.setPRODUCTSAMPLINGCOUNT(SampleData.get("PRODUCTSAMPLINGCOUNT").toString());
				if(SampleData.get("PRODUCTSAMPLINGPOSITION")!=null)
				samplePolicy.setPRODUCTSAMPLINGPOSITION(SampleData.get("PRODUCTSAMPLINGPOSITION").toString());
				samplePolicy.setRETURNOPERATIONNAME(SampleData.get("RETURNOPERATIONNAME").toString());
				samplePolicy.setRETURNOPERATIONVER(SampleData.get("RETURNOPERATIONVER").toString());
		
				try
				{
					ExtendedObjectProxy.getPOSSampleService().create(eventInfo, samplePolicy);
				}
				catch (greenFrameDBErrorSignal e)
				{
					//CUSTOM-0003: Copy {0}Policy  Failed
					throw new CustomException ("CUSTOM-0003","Sampling");
				}
			}
			
			log.info("Sample Policy Copy Success.");
			//[end]
		}
		
		//C. TPFO Policy Copy
		if (StringUtil.equals(tpfoPolicyFlag, "true"))
		{
			//6.TPFOPolicy
			//[start]
			log.info("Start TPFO Policy Copy.");
			
			String TPFOCondition = " where factoryname = ? and processflowname = ? and processflowversion = ? and productspecname = ?";
			Object[] TPFOBindSet = new Object[] { factoryName, flowName, flowVersion, productSpecName };
	
			List<TPFOPolicy> TPFOPolicyList = new ArrayList<TPFOPolicy>();
			try
			{
				TPFOPolicyList = ExtendedObjectProxy.getTPFOPolicyService().select(TPFOCondition, TPFOBindSet);
			}
			catch (greenFrameDBErrorSignal e)
			{
				//CUSTOM-0002:Quary for Source {0}Policy Error
				throw new CustomException ("CUSTOM-0002","TPFO");
			}
						
			for (TPFOPolicy TPFOPolicyData : TPFOPolicyList)
			{
				TPFOPolicyData.setPROCESSFLOWNAME(newFlowName);
				TPFOPolicyData.setPRODUCTSPECNAME(newProdcutSpecName);
				String conditionID = getTPFOPolicyConditionID(TPFOPolicyData);
			
				if (StringUtil.isNotEmpty(conditionID))
				{
					TPFOPolicyData.setCONDITIONID(conditionID);
					
					try
					{
						ExtendedObjectProxy.getTPFOPolicyService().create(TPFOPolicyData);
					}
					catch (greenFrameDBErrorSignal e)
					{
						//CUSTOM-0003: Copy {0}Policy  Failed
						throw new CustomException ("CUSTOM-0003","TPFO");
					}
					
				}
				else
				{
					// CUSTOM-0004: Get the {0}Policy Condition ID Failed
					throw new CustomException("CUSTOM-0004", "TPFO");
				}
			}
			
			log.info("TPFO Policy Copy Success.");
			//[end]
			
			//5.POSMachine
			//[start]
			log.info("Start POSMachine Policy Copy.");
			
			String POSMachineQry = "SELECT A.FACTORYNAME,A.CONDITIONID,A.PRODUCTSPECNAME,A.PROCESSFLOWNAME,A.PROCESSFLOWVERSION,A.PROCESSOPERATIONNAME,"
					+ "A.PROCESSOPERATIONVERSION,A.PRODUCTSPECVERSION,B.MACHINENAME, B.MACHINERECIPENAME,B.ROLLTYPE,B.CHECKLEVEL,B.RMSFLAG,"
					+ "B.DISPATCHPRIORITY,B.DISPATCHSTATE,B.ECRECIPEFLAG,B.ECRECIPENAME,B.MASKCYCLETARGET,B.INT,B.MFG,B.AUTOCHANGEFLAG,B.AUTOCHANGETIME,B.AUTOCHANGETIME,B.AUTOCHANGELOTQUANTITY"
					+ " FROM TPFOPOLICY A,POSMACHINE B WHERE 1=1 "
					+ "AND A.CONDITIONID = B.CONDITIONID AND A.FACTORYNAME = ? AND A.PRODUCTSPECNAME = ? AND A.PROCESSFLOWNAME = ? AND A.PROCESSFLOWVERSION = ?";
		
			Object[] POSMachineBindSet = new Object[] { factoryName, productSpecName,flowName, flowVersion };
			List<Map<String, Object>> POSmachineList = greenFrameServiceProxy.getSqlTemplate().queryForList(POSMachineQry, POSMachineBindSet);
			if (POSmachineList.size() == 0)
			{
				//CUSTOM-0002: Quary for Source {0}Policy Error
				throw new CustomException ("CUSTOM-0002","POSMachine");
			}
			
			
			for (Map<String, Object> POSMachineData : POSmachineList)
			{					
				TPFOPolicy temp = new TPFOPolicy();
				
				temp.setFACTORYNAME(POSMachineData.get("FACTORYNAME").toString());
				temp.setPRODUCTSPECNAME(productSpecName);
				temp.setPRODUCTSPECVERSION(POSMachineData.get("PRODUCTSPECVERSION").toString());;
				temp.setPROCESSFLOWNAME(newFlowName);
				temp.setPROCESSFLOWVERSION(flowVersion);
				temp.setPROCESSOPERATIONNAME(POSMachineData.get("PROCESSOPERATIONNAME").toString());
				temp.setPROCESSOPERATIONVERSION(POSMachineData.get("PROCESSOPERATIONVERSION").toString());			
				
				String conditionID = getTPFOPolicyConditionID(temp);
				//因TFOMPolicy Copy中已对conditionID作空判断，故在此无需再作判断
				
				POSMachine posMachine = new POSMachine();
				
				posMachine.setConditionID(conditionID);
				posMachine.setMachineName(POSMachineData.get("MACHINENAME").toString());
				if(POSMachineData.get("MACHINERECIPENAME")!=null)
				posMachine.setMachineRecipeName(POSMachineData.get("MACHINERECIPENAME").toString());
				if(POSMachineData.get("ROLLTYPE")!=null)
				posMachine.setRollType(POSMachineData.get("ROLLTYPE").toString());
				if(POSMachineData.get("CHECKLEVEL")!=null)
				posMachine.setCheckLevel(POSMachineData.get("CHECKLEVEL").toString());
				if(POSMachineData.get("RMSFLAG")!=null)
				posMachine.setRmsFlag(POSMachineData.get("RMSFLAG").toString());
				if(POSMachineData.get("DISPATCHPRIORITY")!=null)
				posMachine.setDisPatchPriority(POSMachineData.get("DISPATCHPRIORITY").toString());
				if(POSMachineData.get("DISPATCHSTATE")!=null)
				posMachine.setDisPatchState(POSMachineData.get("DISPATCHSTATE").toString());
				if(POSMachineData.get("ECRECIPEFLAG")!=null)
				posMachine.setEcRecipeFlag(POSMachineData.get("ECRECIPEFLAG").toString());
				if(POSMachineData.get("ECRECIPENAME")!=null)
				posMachine.setEcRecipeName(POSMachineData.get("ECRECIPENAME").toString());
				if(POSMachineData.get("MASKCYCLETARGET")!=null)
				posMachine.setMaskCycleTarget(POSMachineData.get("MASKCYCLETARGET").toString());
				if(POSMachineData.get("INT")!=null)
				posMachine.setINT(POSMachineData.get("INT").toString());
				if(POSMachineData.get("MFG")!=null)
				posMachine.setMFG(POSMachineData.get("MFG").toString());
				if(POSMachineData.get("AUTOCHANGEFLAG")!=null)
				posMachine.setAutoChangeFlag(POSMachineData.get("AUTOCHANGEFLAG").toString());
				if(POSMachineData.get("AUTOCHANGETIME")!=null)
				{
					Long autoChangeTime = Long.valueOf(POSMachineData.get("AUTOCHANGETIME").toString());
					posMachine.setAutoChangeTime(autoChangeTime);
				}
				if(POSMachineData.get("AUTOCHANGELOTQUANTITY")!=null)
				{
					Long autoChangeLotQuantity = Long.valueOf(POSMachineData.get("AUTOCHANGELOTQUANTITY").toString());
					posMachine.setAutoChangeLotQuantity(autoChangeLotQuantity);	
				}					
		
				try
				{
					ExtendedObjectProxy.getPOSMachineService().create(posMachine, eventInfo);
				}
				catch (greenFrameDBErrorSignal e)
				{
					//CUSTOM-0003: Copy {0}Policy  Failed
					throw new CustomException ("CUSTOM-0003","POSMachine");
				}
			}
			
			log.info("POSMachine Copy Success.");
			//[end]
			
		}
		
		
		//功能暂不启用
		//d. E2P Policy Copy
//		if (StringUtil.equals(E2PFlag, "true"))
//		{
//			//7.E2P
//			//[start]
//			log.info("Start E2P Copy.");
//			
//			String E2PCondition = " where factoryname = ? and processflowname != ? and processflowversion = ? and productspecname = ?";
//			Object[] E2PBindSet = new Object[] { factoryName, flowName, flowVersion, productSpecName };
//	
//			List<TPFOPolicy> TPFOPolicyList = new ArrayList<TPFOPolicy>();
//			try
//			{
//				TPFOPolicyList = ExtendedObjectProxy.getTPFOPolicyService().select(E2PCondition, E2PBindSet);
//			}
//			catch (greenFrameDBErrorSignal e)
//			{
//				throw new CustomException ("CustomError", "原始E2P对象不存在", "Quary for Source E2P Error", "", "Quary for Source E2P Error");
//			}
//						
//			for (TPFOPolicy TPFOPolicyData : TPFOPolicyList)
//			{
//				//a.setPROCESSFLOWNAME(newFlowName);
//				TPFOPolicyData.setPRODUCTSPECNAME(newProdcutSpecName);
//				String conditionID = getTPFOPolicyConditionID(TPFOPolicyData);
//			
//				if (StringUtil.isNotEmpty(conditionID))
//				{
//					TPFOPolicyData.setCONDITIONID(conditionID);
//					
//					try
//					{
//						ExtendedObjectProxy.getTPFOPolicyService().create(TPFOPolicyData);
//					}
//					catch (greenFrameDBErrorSignal e)
//					{
//						throw new CustomException ("CustomError", "复制TPFOPolicy失败", "Copy TPFOPolicy  Failed", "", "Copy TPFOPolicy  Failed");
//					}
//					
//				}
//				else
//				{
//					throw new CustomException ("CustomError", "获取 TPFOPolicy Condition ID 失败", "Get the TPFOPolicy Condition ID Failed", "", "Get the TPFOPolicy Condition ID Failed");
//				}
//			}
//			
//			log.info("TPFO Policy Copy Success.");
			//[end]
//		}
				
		return doc;

	}
	
	
	
	//Get ConditionID
		public String getTFOPolicyConditionID(TFOPolicy a)
		{
			String conditionID = a.getFACTORYNAME().substring(0, 1) + a.getPROCESSFLOWNAME() + 
			a.getPROCESSFLOWVERSION() + a.getPROCESSOPERATIONNAME() + a.getPROCESSOPERATIONVERSION();
			
			return conditionID;
		}
		public String getTFOMPolicyConditionID(TFOMPolicy a)
		{
			String conditionID = a.getFACTORYNAME().substring(0, 1) + a.getPROCESSFLOWNAME() + 
			a.getPROCESSFLOWVERSION() + a.getPROCESSOPERATIONNAME() + a.getPROCESSOPERATIONVERSION()
			+ a.getMACHINENAME();
			
			return conditionID;
		}
		public String getTPFOPolicyConditionID(TPFOPolicy a)
		{
			String conditionID = a.getFACTORYNAME().substring(0, 1) + a.getPRODUCTSPECNAME() 
			+ a.getPROCESSFLOWNAME() + a.getPROCESSOPERATIONNAME();
			
			return conditionID;
		}
//		public String getTPOMPolicyConditionID(TPOMPolicy a)
//		{
//			String conditionID = a.getFACTORYNAME().substring(0, 1) + a.CONDITIONID
//			return conditionID;
//		}
		/** 
		 * 随机指定范围内N个不重复的数 
		 * 利用HashSet的特征，只能存放不同的值 
		 * @param min 指定范围最小值 
		 * @param max 指定范围最大值 
		 * @param n 随机数个数 
		 * @param HashSet<Integer> set 随机数结果集 
		 */  
		   public static void randomSet(int min, int max, int n, HashSet<Integer> set) 
		   {  
		       if (n > (max - min + 1) || max < min) {  
		           return ;  
		       }  
		       for (int i = 0; i < n; i++) {  
		           // 调用Math.random()方法  
		           int num = (int) ((Math.random()+ 1)* (max - min)) + min;  
		           set.add(num);// 将不同的数存入HashSet中  
		       }  
		       int setSize = set.size();  
		       // 如果存入的数小于指定生成的个数，则调用递归再生成剩余个数的随机数，如此循环，直到达到指定大小  
		       if (setSize < n) {  
		        randomSet(min, max, n - setSize, set);// 递归  
		       }   
		   }  
		
		   /** 
			 * 判断Flow内是否存在指定站点 
			 * @param processFlowName 指定FlowName 
			 * @param processOperationName 指定站点 
			 * @author xiaoxh
			 * @since 2021/01/25 
			 */  
		   public void checkOperationExist(String processFlowName,String processOperationName,String factoryName,String processFlowVersion)throws CustomException
		   {
			   HashMap<String, Object> bindMapNode = new HashMap<String, Object>();
				String sqlNode = "SELECT N.NODEID, " +
								 " N.NODETYPE, " +
								 " N.NODEATTRIBUTE1, " +
								 " N.NODEATTRIBUTE2, " +
								 " N.XCOORDINATE, " +
								 " N.YCOORDINATE " +
								 "  FROM NODE N " +
								 " WHERE     1 = 1 " +
								 "   AND N.FACTORYNAME = :FACTORYNAME " +
								 "   AND N.PROCESSFLOWNAME = :PROCESSFLOWNAME " +
								 "   AND N.PROCESSFLOWVERSION = :PROCESSFLOWVERSION " +
								 "   AND N.NODEATTRIBUTE1 = :NODEATTRIBUTE1 " ;
		
				bindMapNode.put("FACTORYNAME", factoryName);
				bindMapNode.put("PROCESSFLOWNAME", processFlowName);
				bindMapNode.put("PROCESSFLOWVERSION", processFlowVersion);
				bindMapNode.put("NODEATTRIBUTE1", processOperationName);
		        
				List<ListOrderedMap> sqlNodeResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlNode, bindMapNode);
				if(sqlNodeResult.size()==0)
				{
					// CUSTOM-0006: ProcessOperation:{0} not exist in ProcessFlow:{1}Please Attention!
			        throw new CustomException("CUSTOM-0006", processFlowName, processOperationName);
				}
				
		   }
}
