package kr.co.aim.messolution.datacollection.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kr.co.aim.messolution.datacollection.service.DataCollectionServiceUtil;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotLastProduction;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.sys.SystemPropHelper;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.generic.exception.LogLevel;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.MakeOnHoldInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

import sun.util.logging.resources.logging;

public class SPCEngineExecute extends AsyncHandler {
	
	public boolean OOCFlag = false;
	public boolean OOSFlag = false;
	
	public static boolean PRINT_DEBUG = false;
	
	@Override
	public void doWorks(Document doc)
		throws CustomException  
	{
		this.printLog("SPCEngineExecute - doWorks() started ");
		
        List<Element> resultList = new ArrayList<Element>();
        
		try 
		{
			resultList = this.getSelectedNodes(doc.getRootElement(), "//Message/Results/Result");	
			
			if(resultList == null || resultList.size() <= 0)
				return;
		} 
		catch (Exception e) 
		{
			this.printLog("Colud Not Access to /Message/Results  Reason : " + e.getMessage(), LogLevel.Error);
			return;
		}
		
		for (Element eleResult : resultList)
		{
			try {
				List<Element> lstControlItemResultVOs =  this.getSelectedNodes(eleResult, "./ControlItemResultVOs");
				
				for (Element controlItemResultVO : lstControlItemResultVOs)
				{
					List<Element> lstControlChartResultVOs = this.getSelectedNodes(controlItemResultVO, "./ControlChartResultVOs");
					
					for(Element controlCharResultVO : lstControlChartResultVOs)
					{
						List<Element> lstRuleResultVOs = this.getSelectedNodes(controlCharResultVO, "./RuleResultVOs");
						
						for( Element ruleResultVO : lstRuleResultVOs)
						{
							String ruleOut = ruleResultVO.getChildText("RuleOut");
							
							if(!StringUtil.equalsIgnoreCase("true", ruleOut))
								continue;
							
							//Process When Rule Out -------------------------------------------------
							try
							{
								this.sendReserveHoldMessage(ruleResultVO);
							}catch(Exception e)
							{
								this.printLog(String.format("Skipping Error - Reason : %s",e.getMessage()), LogLevel.Error);
								continue;
							}
						}
					}
				}
				
			} catch (JDOMException e) {
				this.printLog(String.format("Skipping JDOM Error - Reason : %s",e.getMessage()), LogLevel.Error);
				continue;
			} catch(Exception e)
			{
				this.printLog(String.format("Skipping Error - Reason : %s",e.getMessage()), LogLevel.Error);
				continue;
			}
		}
		
		OOCFlag = false;
		OOSFlag = false;
		
		this.printLog("SPCEngineExecute - doWorks() ended. ");
	}
	
	private List<Element> getSelectedNodes(Element element, String path) throws JDOMException, Exception
	{
		List<Element> lstReturn = new ArrayList<Element>();
		List<?> lstNodes = XPath.selectNodes(element, path);
		
		for( Object obj : lstNodes)
		{
			if( obj instanceof Element)
			{
				lstReturn.add((Element)obj);
			}else
			{
				throw new Exception("Selected Node is not Element ");
			}
		}
		
		return lstReturn;
	}
	
	private void sendReserveHoldMessage(Element ruleResultVOs) throws Exception
	{	
		if( !StringUtil.equalsIgnoreCase("RuleResultVOs",ruleResultVOs.getName()))
		{
			throw new Exception("Parameter Element Must be [RuleResultVOs]");
		}
		
		Element spcControlRule = JdomUtils.getNode(ruleResultVOs, "./SPCControlRule");
		
		String controlLimitType = spcControlRule.getChildText("ControlLimitType");
		
		String comment = "";
		String reasonCode = "";
		String materialType = "";
		String materialName = "";
		String factoryName = "";
		String lotName = "";
		String processflowName = "";
		String itemName = "";
		String dcdataid="";
		String sql = "";
		String processoperationname="";
		List<Map<String, Object>> materialData = null;
		
		if(StringUtil.equals("SpecLimit", controlLimitType))
		{
			comment = "Spc Lot Hold By OOS";
			reasonCode = "OOS";
		}
		else if(StringUtil.equals("ControlLimit", controlLimitType))
		{
			comment = "Spc Alarm By OOC   ";
			reasonCode = "OOC";
		}
		
		Element spcControlDataResult = JdomUtils.getNode(ruleResultVOs, "../SPCControlDataResult");
		
		materialType = spcControlDataResult.getChildText("MaterialType");
		materialName = spcControlDataResult.getChildText("MaterialName");
		itemName = spcControlDataResult.getChildText("ItemName");
		dcdataid = spcControlDataResult.getChildText("DCDataId");
		
		if(StringUtil.equals("Product", materialType))
		{
			materialData = DataCollectionServiceUtil.getProductData(materialName);
		}
		else if(StringUtil.equals("Lot", materialType))
		{
			materialData = DataCollectionServiceUtil.getLotData(materialName);
		}
		
		if( materialData == null || materialData.size() <= 0)
		{
			throw new Exception(String.format("MaterialData Not Found - MaterialName[%s]",materialName));
		}
		
		factoryName = (String)materialData.get(0).get("FACTORYNAME");
		lotName = (String)materialData.get(0).get("LOTNAME");
		processflowName = (String)materialData.get(0).get("PROCESSFLOWNAME");
		processoperationname=(String)materialData.get(0).get("PROCESSOPERATIONNAME");
		
		sql="SELECT FACTORYNAME,PROCESSOPERATIONNAME,LOTNAME,MACHINENAME,PROCESSFLOWNAME,PRODUCTSPECNAME " +
				"FROM MES_DCDATA WHERE DCDATAID=:DCDATAID ";
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("DCDATAID", dcdataid);
		
		List<Map<String, Object>> result= GenericServiceProxy.getDcolQueryTemplate().getSimpleJdbcTemplate().queryForList(sql,bindMap);
		
		if(StringUtil.equals("OOS",reasonCode) && !(OOSFlag))
		{
			if(!StringUtil.equals(((String)result.get(0).get("PROCESSOPERATIONNAME")),processoperationname) && 
					!((StringUtil.equals("Processing",((String)materialData.get(0).get("PRODUCTPROCESSSTATE")))) || 
					 (StringUtil.equals("RUN",((String)materialData.get(0).get("LOTPROCESSSTATE"))))))
			{
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("SPC Hold", "SPC", comment+" ItemName:"+itemName, null, reasonCode);
				Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
				List<ProductU> productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllProductUSequence(lotData);
				MakeOnHoldInfo makeOnHoldInfo = MESLotServiceProxy.getLotInfoUtil().makeOnHoldInfo(productUSequence, new HashMap<String, String>());
				LotServiceProxy.getLotService().makeOnHold(lotData.getKey(), eventInfo, makeOnHoldInfo);
				
				DataCollectionServiceUtil.insertAlarm("TrackOut",lotName, 
													(String)result.get(0).get("MACHINENAME"), 
													reasonCode, 
													(String)result.get(0).get("PROCESSOPERATIONNAME"), 
													comment ,
													(String)result.get(0).get("FACTORYNAME"),
													(String)result.get(0).get("PROCESSFLOWNAME"),
													(String)result.get(0).get("PRODUCTSPECNAME")
													);
				eventLog.info("SPC OOS Hold and InsertAlarm Table Success");
				
				OOSFlag = true;
			}
			else
			{
				StringBuilder spcXmlMsg = new StringBuilder(50000);
				
				spcXmlMsg.append("<Message>").append(SystemPropHelper.CR);
				spcXmlMsg.append("<Header>").append(SystemPropHelper.CR);
				spcXmlMsg.append("<MESSAGENAME>ReserveHold</MESSAGENAME>").append(SystemPropHelper.CR);
				spcXmlMsg.append("<SHOPNAME>"+factoryName+"</SHOPNAME>").append(SystemPropHelper.CR);
				spcXmlMsg.append("<MACHINENAME />").append(SystemPropHelper.CR);
				spcXmlMsg.append("<TRANSACTIONID>"+TimeStampUtil.getCurrentEventTimeKey()+"</TRANSACTIONID>").append(SystemPropHelper.CR);
				spcXmlMsg.append("<ORIGINALSOURCESUBJECTNAME />").append(SystemPropHelper.CR);
				spcXmlMsg.append("<SOURCESUBJECTNAME />").append(SystemPropHelper.CR);
				spcXmlMsg.append("<TARGETSUBJECTNAME />").append(SystemPropHelper.CR);
				spcXmlMsg.append("<EVENTUSER>SPC</EVENTUSER>").append(SystemPropHelper.CR);
				spcXmlMsg.append("<EVENTCOMMENT>"+comment+" ItemName:"+itemName+"</EVENTCOMMENT>").append(SystemPropHelper.CR);
				spcXmlMsg.append("<INPUTSET />").append(SystemPropHelper.CR);
				spcXmlMsg.append("<LANGUAGE>Chinese</LANGUAGE>").append(SystemPropHelper.CR);
				spcXmlMsg.append("</Header>").append(SystemPropHelper.CR);
				spcXmlMsg.append("<Body>").append(SystemPropHelper.CR);
				spcXmlMsg.append("<FACTORYNAME>"+factoryName+"</FACTORYNAME>").append(SystemPropHelper.CR);
				spcXmlMsg.append("<LOTNAME>"+lotName+"</LOTNAME>").append(SystemPropHelper.CR);
				spcXmlMsg.append("<PROCESSFLOWNAME>"+processflowName+"</PROCESSFLOWNAME>").append(SystemPropHelper.CR);
				spcXmlMsg.append("<PROCESSOPERATIONNAME>"+(String)result.get(0).get("PROCESSOPERATIONNAME")+"</PROCESSOPERATIONNAME>").append(SystemPropHelper.CR);
				spcXmlMsg.append("<REASONCODE>"+reasonCode+"</REASONCODE>").append(SystemPropHelper.CR);
				spcXmlMsg.append("<REASONCODETYPE>SPC</REASONCODETYPE>").append(SystemPropHelper.CR);
				spcXmlMsg.append("<ATTRIBUTE1>"+materialName+"</ATTRIBUTE1>").append(SystemPropHelper.CR);
				spcXmlMsg.append("</Body>").append(SystemPropHelper.CR);
				spcXmlMsg.append("</Message>").append(SystemPropHelper.CR);
				
				try
				{
					this.printLog("SendMsg :" + spcXmlMsg.toString());
					GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("CNXsvr"), spcXmlMsg.toString(), "OICSender");
				
					OOSFlag = true;
				}
				catch (Exception ex)
				{
					this.printLog("Error occurs in - sendReserveHoldMessage() ", LogLevel.Error);
					throw ex;
				}
			}
			
        	Lot lotData = CommonUtil.getLotInfoByLotName(lotName);
            ProcessFlow flowData = MESLotServiceProxy.getLotInfoUtil().getProcessFlowData(lotData);

            String productiontype  = lotData.getProductionType();
            String processflowtype = flowData.getProcessFlowType();
            	
            if (!StringUtil.equals(productiontype,"M") && !StringUtil.equals(productiontype,"D") && !StringUtil.equals(processflowtype,"Pioneer"))
            {
			    this.sendMachineRecycleMessage((String)result.get(0).get("MACHINENAME"), lotName, (String)result.get(0).get("PROCESSOPERATIONNAME"));
		
			    //SPC Relation
			    String relationSQL = "SELECT RELATIONOPERATION FROM SPCRELATIONPOLICY WHERE ENABLE = 'Y' AND PROCESSOPERATION = :PROCESSOPERATION";
				Map<String, Object> relationBindMap = new HashMap<String, Object>();
				relationBindMap.put("PROCESSOPERATION", lotData.getProcessOperationName());
				List<Map<String, Object>> relationResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(relationSQL, relationBindMap);	
				if(relationResult.size() > 0)
				{
					for (Map<String, Object> relationData : relationResult)
					{
						try
						{					
							LotLastProduction sourceData = ExtendedObjectProxy.getLotLastProductionService().selectByKey(false, new Object[]{lotData.getKey().getLotName(), relationData.get("RELATIONOPERATION").toString()});
							sendMachineRecycleMessage(sourceData.getMachineName(), lotName, (String)result.get(0).get("PROCESSOPERATIONNAME"));
						}
						catch(Exception e)
						{
							eventLog.info("Relation Operation product history not exits!");
						}
					}
				}
             }
		}

		if((StringUtil.equals("OOC",reasonCode)) && !(OOCFlag) )
		{
			String sql1 = "SELECT LOTNAME,PRODUCTSPECNAME,PROCESSFLOWNAME,PROCESSOPERATIONNAME,MACHINENAME,LASTEVENTNAME " +
							" FROM ALARM WHERE LOTNAME=:LOTNAME AND PRODUCTSPECNAME=:PRODUCTSPECNAME " +
							" AND PROCESSFLOWNAME=:PROCESSFLOWNAME AND PROCESSOPERATIONNAME=:PROCESSOPERATIONNAME " +
							" AND MACHINENAME=:MACHINENAME AND LASTEVENTNAME !=:LASTEVENTNAME ";
		
			HashMap<String, Object> bindMap1 = new HashMap<String, Object>();
			bindMap1.put("LOTNAME", lotName);
			bindMap1.put("PRODUCTSPECNAME", (String)result.get(0).get("PRODUCTSPECNAME"));
			bindMap1.put("PROCESSFLOWNAME", (String)result.get(0).get("PROCESSFLOWNAME"));
			bindMap1.put("PROCESSOPERATIONNAME", (String)result.get(0).get("PROCESSOPERATIONNAME"));
			bindMap1.put("MACHINENAME", (String)result.get(0).get("MACHINENAME"));
			bindMap1.put("LASTEVENTNAME", "TrackOut");
			
			List<Map<String, Object>> alarmData = GenericServiceProxy.getSqlMesTemplate().queryForList(sql1, bindMap1);
			
			if(alarmData.size()==0)
			{
				if(!StringUtil.equals(((String)result.get(0).get("PROCESSOPERATIONNAME")),processoperationname))
				{
					DataCollectionServiceUtil.insertAlarm("TrackOut",lotName, 
														(String)result.get(0).get("MACHINENAME"), 
														reasonCode, 
														(String)result.get(0).get("PROCESSOPERATIONNAME"), 
														comment ,
														(String)result.get(0).get("FACTORYNAME"),
														(String)result.get(0).get("PROCESSFLOWNAME"),
														(String)result.get(0).get("PRODUCTSPECNAME")
														);
				}
				else
				{
					DataCollectionServiceUtil.insertAlarm("Insert",lotName, 
														(String)result.get(0).get("MACHINENAME"), 
														reasonCode, 
														(String)result.get(0).get("PROCESSOPERATIONNAME"), 
														comment ,
														(String)result.get(0).get("FACTORYNAME"),
														(String)result.get(0).get("PROCESSFLOWNAME"),
														(String)result.get(0).get("PRODUCTSPECNAME")
														);
				}
				eventLog.info("SPC OOC InsertAlarm Table Success");
				
				OOCFlag = true;
			}
			else
			{
				eventLog.info("OOC record has been existed");
			}
		}
	}
	
	//sendMachineRecycleMessage
	private void sendMachineRecycleMessage(String machineName,String lotName,String processOperationName) throws Exception
	{	
		//get line machine
		Machine machineData	= MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		
		String recycleStatus;
		if(StringUtil.equals(machineData.getMachineStateName(),"DOWN") && StringUtil.equals(machineData.getReasonCode(),"RECYCLE"))
		{
			recycleStatus = "ON";
		}
		else
		{
			recycleStatus = "OFF";
		}
		
	     String sql1_flag = "SELECT ENUMNAME, ENUMVALUE,DEFAULTFLAG FROM  ENUMDEFVALUE  " +
	     					" WHERE ENUMNAME = 'ReserveHold' "+
	     					"   AND ENUMVALUE = :MACHINENAME ";
	     Map<String,Object> bindMap1_flag = new HashMap<String,Object>();
	     bindMap1_flag.put("MACHINENAME", machineName);
	     List<Map<String, Object>> flaglist = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql1_flag, bindMap1_flag);     
	     if (flaglist.size() > 0)
	     {
	    	 if (StringUtil.equals(((String)flaglist.get(0).get("DEFAULTFLAG")),"Y"))
		     {
		    	 //get lot data
		    	 Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		    	 //MES-EAP protocol
		    	 String targetSubjectName = CommonUtil.getValue(machineData.getUdfs(), "MCSUBJECTNAME");
		    	 
		    	 StringBuilder spcXmlMsg = new StringBuilder(50000);
		    	 spcXmlMsg.append("<Message>").append(SystemPropHelper.CR);
		    	 spcXmlMsg.append("<Header>").append(SystemPropHelper.CR);
		    	 spcXmlMsg.append("<MESSAGENAME>MachineRecycleCommandSend</MESSAGENAME>").append(SystemPropHelper.CR);
		    	 spcXmlMsg.append("<SHOPNAME>"+lotData.getFactoryName()+"</SHOPNAME>").append(SystemPropHelper.CR);
		    	 spcXmlMsg.append("<MACHINENAME>"+machineName+"</MACHINENAME>").append(SystemPropHelper.CR);
		    	 spcXmlMsg.append("<TRANSACTIONID>"+TimeStampUtil.getCurrentEventTimeKey()+"</TRANSACTIONID>").append(SystemPropHelper.CR);
		    	 spcXmlMsg.append("<ORIGINALSOURCESUBJECTNAME>"+GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("PEXsvr")+"</ORIGINALSOURCESUBJECTNAME>").append(SystemPropHelper.CR);
		    	 spcXmlMsg.append("<SOURCESUBJECTNAME>"+GenericServiceProxy.getESBServive().makeCustomServerLocalSubject("PEXsvr")+"</SOURCESUBJECTNAME>").append(SystemPropHelper.CR);
		    	 spcXmlMsg.append("<TARGETSUBJECTNAME>"+targetSubjectName+"</TARGETSUBJECTNAME>").append(SystemPropHelper.CR);
		    	 spcXmlMsg.append("<EVENTUSER>"+machineName+"</EVENTUSER>").append(SystemPropHelper.CR);
		    	 spcXmlMsg.append("<EVENTCOMMENT>MachineRecycleCommandSend</EVENTCOMMENT>").append(SystemPropHelper.CR);
		    	 spcXmlMsg.append("</Header>").append(SystemPropHelper.CR);
		    	 spcXmlMsg.append("<Body>").append(SystemPropHelper.CR);
		    	 spcXmlMsg.append("<MACHINENAME>"+machineName+"</MACHINENAME>").append(SystemPropHelper.CR);
		    	 spcXmlMsg.append("<LOTNAME>"+lotName+"</LOTNAME>").append(SystemPropHelper.CR);
		    	 spcXmlMsg.append("<CARRIERNAME>"+lotData.getCarrierName()+"</CARRIERNAME>").append(SystemPropHelper.CR);
		    	 spcXmlMsg.append("<PRODUCTSPECNAME>"+lotData.getProductSpecName()+"</PRODUCTSPECNAME>").append(SystemPropHelper.CR);
		    	 spcXmlMsg.append("<PROCESSOPERATIONNAME>"+processOperationName+"</PROCESSOPERATIONNAME>").append(SystemPropHelper.CR);
		    	 spcXmlMsg.append("<RECYCLECHANGESTATUS>"+recycleStatus+"</RECYCLECHANGESTATUS>").append(SystemPropHelper.CR);
		    	 spcXmlMsg.append("</Body>").append(SystemPropHelper.CR);
		    	 spcXmlMsg.append("</Message>").append(SystemPropHelper.CR);
		    	 try
		    	 {
		    		 this.printLog("SendMsg :" + spcXmlMsg.toString());
		    		 
		    		 GenericServiceProxy.getESBServive().sendBySender(targetSubjectName, spcXmlMsg.toString(), "EISSender");
		    	 }
		    	 catch (Exception ex)
		    	 {
		    		 this.printLog("Error occurs in - sendMachineRecycleMessage() ", LogLevel.Error);
		    		 throw ex;
		    	 }
		     }
	     }
	}
	
	private void printLog(String message, LogLevel level)
	{
		StringBuffer sbMessage = new StringBuffer(">>>>>>>>>>>>>>>>>>>> Log : ");
		sbMessage.append(message);
		sbMessage.append(" <<<<<<<<<<<<<<<<<<<< ");
		
		if( level == LogLevel.Debug)
		{
			if( PRINT_DEBUG )
			{
				eventLog.debug(sbMessage.toString());
			}
		}else if( level == LogLevel.Info)
		{
			eventLog.info(sbMessage.toString());
		}else if( level == LogLevel.Warn)
		{
			eventLog.warn(sbMessage.toString());
		}else
		{
			eventLog.error(sbMessage.toString());
		}
		
	}
	
	private void printLog(String message)
	{
		printLog(message, LogLevel.Debug);
		
	}
}
