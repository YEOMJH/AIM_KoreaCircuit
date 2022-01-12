package kr.co.aim.messolution.alarm.event;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.DocFlavor.STRING;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.alarm.MESAlarmServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AbnormalEQP;
import kr.co.aim.messolution.extended.object.management.data.AlarmActionDef;
import kr.co.aim.messolution.extended.object.management.data.AlarmDefinition;
import kr.co.aim.messolution.extended.object.management.data.CustomAlarm;
import kr.co.aim.messolution.extended.object.management.data.MachineAlarmProductList;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;

public class SPC_HOLD_ALARM extends AsyncHandler 
{
	private static Log log = LogFactory.getLog(SPC_HOLD_ALARM.class);
	
	@Override
	public void doWorks(Document doc) throws CustomException {

		String alarmTimeKey = TimeUtils.getCurrentEventTimeKey();
		String factoryName = SMessageUtil.getHeaderItemValue(doc, "SHOPNAME", false);
		String spcAlarmType = SMessageUtil.getBodyItemValue(doc, "SPCALARMTYPE", false);
		String alarmCode = SMessageUtil.getBodyItemValue(doc, "ACTIVESTATE", true);
		String alarmIndex = SMessageUtil.getBodyItemValue(doc, "SPCALARMINDEX", false);
		String itemName = SMessageUtil.getBodyItemValue(doc, "ITEMNAME", false);
		String RULE = SMessageUtil.getBodyItemValue(doc, "RULE", false);
		String ucl = SMessageUtil.getBodyItemValue(doc, "UCL", false);
		String lcl = SMessageUtil.getBodyItemValue(doc, "LCL", false);
		String value = SMessageUtil.getBodyItemValue(doc, "VALUE", false);
		String moduleName = SMessageUtil.getBodyItemValue(doc, "MODULENAME", false);
		String moduleID = SMessageUtil.getBodyItemValue(doc, "MODULEID", false);
		String alarmType = SMessageUtil.getBodyItemValue(doc, "ALARMTYPE", false);
		String alarmState = SMessageUtil.getBodyItemValue(doc, "ALARMSTATE", false);
		String alarmSeverity = SMessageUtil.getBodyItemValue(doc, "ALARMLEVEL", false);
		String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", false);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", false);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", false);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", false);
		String nowProcessOperationName = SMessageUtil.getBodyItemValue(doc, "NOWPROCESSOPERATIONNAME", false);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", false);
		
		List<Element> productElementList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SPCAlarm", "SPC", "", "SPCAlarm", alarmCode);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		CustomAlarm alarmData = new CustomAlarm(alarmCode,alarmTimeKey);
		alarmData.setAlarmType(alarmType);
		alarmData.setAlarmState(alarmState);
		alarmData.setAlarmIndex(alarmIndex);
		alarmData.setAlarmSeverity(alarmSeverity);
		alarmData.setDescription(description);
		alarmData.setProductSpecName(productSpecName);
		alarmData.setProductSpecVersion(productSpecVersion);
		alarmData.setProcessFlowName(processFlowName);
		alarmData.setProcessFlowVersion(processFlowVersion);
		alarmData.setProcessOperationName(processOperationName);
		alarmData.setProcessOperationVersion(processOperationVersion);
		alarmData.setLotName(lotName);
		alarmData.setFactoryName(factoryName);
		alarmData.setCreateTimeKey(eventInfo.getEventTimeKey());
		alarmData.setCreateUser(eventInfo.getEventUser());
		alarmData.setLastEventName(eventInfo.getEventName());
		alarmData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		alarmData.setLastEventUser(eventInfo.getEventUser());
		alarmData.setLastEventComment(eventInfo.getEventComment());
		alarmData.setReleaseHoldFlag("");
		alarmData.setMachineName(machineName);
		alarmData.setUnitName(unitName);
		alarmData.setSubUnitName(subUnitName);
		alarmData.setRULE(RULE);
		alarmData.setItemName(itemName);
		alarmData.setUCL(ucl);
		alarmData.setLCL(lcl);
		alarmData.setValue(value);
		alarmData.setModualId(moduleID);
		
		alarmData = ExtendedObjectProxy.getCustomAlarmService().create(eventInfo, alarmData);
		
		List<MachineAlarmProductList> alarmProductList = new ArrayList<>();
		String productComment="[";
		for(Element productElement : productElementList)
		{
			String productName = productElement.getChildText("PRODUCTNAME");
			if(StringUtil.isEmpty(productName)) continue;
			
		     MachineAlarmProductList mProductList = new MachineAlarmProductList(alarmTimeKey,productName);
		     alarmProductList.add(mProductList);
		     productComment+=productName+" ";
		}
		
		String productList = productComment;
		productList = productList.replace('[', ' ');
		
		productComment+="Unit:"+unitName+" BeforeOper:"+processOperationName+" Item:"+itemName+" VALUE:"+value+"]";
		
		if (alarmProductList.size() > 0)
			ExtendedObjectProxy.getMachineAlarmProductListService().insert(alarmProductList);

		try
		{
			doAlarmAction(doc, alarmData,alarmCode, machineName, eventInfo, productElementList,productComment,unitName,itemName);
		}catch (Exception ex)
		{
			if (ex instanceof CustomException)
				throw (CustomException) ex;
			else
				throw new CustomException(ex.getCause());
		}
			
		if(spcAlarmType.equalsIgnoreCase("OOS") && StringUtil.isNotEmpty(lotName) && !StringUtils.equals(lotName, "NA"))
		{
			try 
			{
				String department = getDepartmentForAbnormal(nowProcessOperationName,itemName,moduleName);
				log.info("OOS autoCreateSPCAbnormal start");
				autoCreateSPCAbnormal(machineName,nowProcessOperationName,lotName,department,productList,productComment,factoryName,eventInfo);
			}catch (Exception e) 
			{
				// TODO: handle exception
				log.info("autoCreateSPCAbnormal fail");
			}
		}else
		{
			//log.info(String.format("spcAlarmType:s% skip autoCreateSPCAbnormal", spcAlarmType));
			log.info(spcAlarmType+" skip autoCreateSPCAbnormal");
		}
	}
	
	private String getDepartmentForAbnormal(String nowProcessOperationName, String itemName, String moduleName) 
	{
		List<Map<String,Object>> resultList = null;
		StringBuilder sb = new StringBuilder();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT SEQ FROM  ENUMDEFVALUE A WHERE A.ENUMNAME = 'AbnormalSPCCreateRule' ");
		sql.append("AND A.DESCRIPTION = :DESCRIPTION AND A.DEFAULTFLAG = :DEFAULTFLAG AND A.DISPLAYCOLOR = :DISPLAYCOLOR");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("DESCRIPTION", nowProcessOperationName);
		args.put("DEFAULTFLAG", itemName);
		args.put("DISPLAYCOLOR", moduleName);
	
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		}
		catch(Exception ex)
		{
		    log.info(ex.getCause());
		}
		
		if (resultList.size() > 0)
		{
			return resultList.get(0).get("SEQ").toString();
		}
		else 
		{
			log.info("Can't Find CreateRule,Create To QA");
			return "品保";
		}
	}

	private void doAlarmAction(Document doc,CustomAlarm alarmData,String alarmCode, String machineName, EventInfo eventInfo, List<Element> productElementList,String productComment,String unitName,String item) throws CustomException
	{
		eventInfo.setReasonCodeType("SPCHOLD");
		eventInfo.setReasonCode("SPC");		
		
		String  Alarmtype="SPC";
		
		//Start houxk 20210621
		try
		{
			MachineSpec eqpData    = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(unitName);
            
	        if (StringUtils.equals(eqpData.getProcessUnit(), "Mask"))
			{
				MESAlarmServiceProxy.getAlarmServiceUtil().maskLotHoldAction(machineName, eventInfo, productElementList,Alarmtype, productComment,unitName,item);
			}
		}
		catch(Exception e)
		{
			log.info("No Found UnitInfo!");
		}	
        //End
        
		if (alarmCode.equals("EH")&&StringUtil.isNotEmpty(machineName))
		{				
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			MESAlarmServiceProxy.getAlarmServiceUtil().eqpHoldAction(machineData, eventInfo);
		}					
		else if  (alarmCode.equals("LH"))
		{
			MESAlarmServiceProxy.getAlarmServiceUtil().lotHoldAction(machineName, eventInfo, productElementList,Alarmtype, productComment,unitName,item);
		}		
		else if  (alarmCode.equals("OL")&&StringUtil.isNotEmpty(machineName))
		{
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			String mSubjectName = machineData.getUdfs().get("MCSUBJECTNAME");

			if (mSubjectName.isEmpty())
			{
				log.info(String.format("Machine [%s] MCSUBJECTNAME is Empty!!", machineData.getKey().getMachineName()));
				return;
			}

			GenericServiceProxy.getESBServive().sendBySender(mSubjectName, this.createOPCallMessage(doc, alarmData, mSubjectName), "EISSender");
		}
		else if  (alarmCode.equals("CP")&&StringUtil.isNotEmpty(machineName))
		{
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			String mSubjectName = machineData.getUdfs().get("MCSUBJECTNAME");

			if (mSubjectName.isEmpty())
			{
				log.info(String.format("Machine [%s] MCSUBJECTNAME is Empty!!", machineData.getKey().getMachineName()));
				return;
			}

			GenericServiceProxy.getESBServive().sendBySender(mSubjectName, this.createChamberPauseMessage(doc, alarmData, mSubjectName), "EISSender");
		
		}
	}
	
	public String makeListByKey(List<Element> productElementList,String keyName)
	{
		String productList = "";

		for (Element productE : productElementList)
		{
			productList = productList + productE.getChildText(keyName) + ",";
		}

		return StringUtil.isEmpty(productList)? productList:productList.substring(0,productList.length()-1);
	}
	
	private String findMatchedItemValue(List<Element> spcDataElementList,String findItem)
	{
		if (spcDataElementList == null || spcDataElementList.size() == 0 || findItem.isEmpty())
			return "";

		String valueStr = "";

		for (Element spcDataElement : spcDataElementList)
		{
			if (spcDataElement.getChildText("ITEMNAME").equals(findItem))
				valueStr = spcDataElement.getChildText("VALUE");
		}

		return valueStr;
	}
	
	
	private Document createOPCallMessage(Document doc, CustomAlarm alarmData,String replySubjectName)
	{
		Element originalHeaderElement = doc.getRootElement().getChild(SMessageUtil.Header_Tag);
		
		Element rootElement = new Element(SMessageUtil.Message_Tag);
		Element headerElement = new Element(SMessageUtil.Header_Tag);
		Element bodyElement = new Element(SMessageUtil.Body_Tag);
		
		headerElement.addContent(new Element("MESSAGENAME").setText("OpCallSend"));
	    headerElement.addContent(new Element("SHOPNAME").setText(originalHeaderElement.getChildText("SHOPNAME")));
		headerElement.addContent(new Element("TRANSACTIONID").setText(TimeUtils.getCurrentEventTimeKey()));
		headerElement.addContent(new Element("ORIGINALSOURCESUBJECTNAME").setText(originalHeaderElement.getChildText("SOURCESUBJECTNAME")));
		headerElement.addContent(new Element("SOURCESUBJECTNAME").setText(originalHeaderElement.getChildText("TARGETSUBJECTNAME")));
		headerElement.addContent(new Element("TARGETSUBJECTNAME").setText(replySubjectName));
		headerElement.addContent(new Element("EVENTUSER").setText(this.getEventUser()));
		headerElement.addContent(new Element("EVENTCOMMENT").setText(this.getEventComment()));
		
		rootElement.addContent(headerElement);
		
		bodyElement.addContent(new Element("MACHINENAME").setText(alarmData.getMachineName()));
		bodyElement.addContent(new Element("OPCALLDESCRIPTION").setText("SPCAlarm : "+ alarmData.getDescription()));
		
		rootElement.addContent(bodyElement);
		
		return new Document(rootElement);
	}
	
	private Document createChamberPauseMessage(Document doc, CustomAlarm alarmData,String replySubjectName) throws CustomException
	{
		List<Element> spcDataElementList = SMessageUtil.getBodySequenceItemList(doc, "SPCDATALIST", true);
		Element originalHeaderElement = doc.getRootElement().getChild(SMessageUtil.Header_Tag);
		
		Element rootElement = new Element(SMessageUtil.Message_Tag);
		Element headerElement = new Element(SMessageUtil.Header_Tag);
		Element bodyElement = new Element(SMessageUtil.Body_Tag);
		
		headerElement.addContent(new Element("MESSAGENAME").setText("OpCallSend"));
	    headerElement.addContent(new Element("SHOPNAME").setText(originalHeaderElement.getChildText("SHOPNAME")));
		headerElement.addContent(new Element("TRANSACTIONID").setText(TimeUtils.getCurrentEventTimeKey()));
		headerElement.addContent(new Element("ORIGINALSOURCESUBJECTNAME").setText(originalHeaderElement.getChildText("SOURCESUBJECTNAME")));
		headerElement.addContent(new Element("SOURCESUBJECTNAME").setText(originalHeaderElement.getChildText("TARGETSUBJECTNAME")));
		headerElement.addContent(new Element("TARGETSUBJECTNAME").setText(replySubjectName));
		headerElement.addContent(new Element("EVENTUSER").setText(this.getEventUser()));
		headerElement.addContent(new Element("EVENTCOMMENT").setText(this.getEventComment()));
		
		rootElement.addContent(headerElement);
		
		bodyElement.addContent(new Element("UNITID").setText(alarmData.getUnitName()));
		bodyElement.addContent(new Element("SUBUNITID").setText(alarmData.getSubUnitName()));
		bodyElement.addContent(new Element("MASKID").setText(this.findMatchedItemValue(spcDataElementList, "MASKNAME")));
		
		rootElement.addContent(bodyElement);
		
		return new Document(rootElement);
	}
	
	public String createMailContentMessage(CustomAlarm alarmData,List<Element> productElementList)
	{
		  String message = "<pre>===============AlarmInformation===============</pre>";
	      message += "<pre>==============================================</pre>";
	      message += "<pre>- AlarmCode		: " + alarmData.getAlarmCode() + "</pre>";
	      message += "<pre>- AlarmIndex		: " + alarmData.getAlarmIndex() + "</pre>";
	      message += "<pre>- AlarmType		: " + alarmData.getAlarmType() + "</pre>";
	      message += "<pre>- AlarmState		: " + alarmData.getAlarmState() + "</pre>";
	      message += "<pre>- UserName		: " + alarmData.getLastEventUser() + "</pre>";
	      message += "<pre>- AlarmComment		: " + alarmData.getLastEventComment() + "</pre>";
	      message += "<pre>- MachineName		: " + alarmData.getMachineName() + "</pre>";
	      message += "<pre>- LotName		: " + alarmData.getLotName() + "</pre>";
	      message += "<pre>- ProductSpecName	: " + alarmData.getProductSpecName() + "</pre>";
	      message += "<pre>- ProductSpecVersion	: " + alarmData.getProductSpecVersion() + "</pre>";
	      message += "<pre>- FlowName		: " + alarmData.getProcessFlowName() + "</pre>";
	      message += "<pre>- FlowVersion		: " + alarmData.getProcessFlowVersion() + "</pre>";
	      message += "<pre>- OperationName		: " + alarmData.getProcessOperationName() + "</pre>";
	      message += "<pre>- OperationVersion	: " + alarmData.getProcessOperationVersion() + "</pre>";
	      if (StringUtil.isNotEmpty(makeListByKey(productElementList,"PRODUCTNAME")))
	      {
	    	  message += "<pre>- ProductList</pre>";
	    	  for (Element productE : productElementList)
	  		  {
	    		  message += "<pre>	ProductName	: " + productE.getChildText("PRODUCTNAME") + "</pre>";
	  		  }
	      }
	      message += "<pre>==============================================</pre>";
		  return message;
	}
	
	private void autoCreateSPCAbnormal(String abnormalEQPName, String abnormalOperationName, String lotName, String department,
			String productNameList, String description, String factory, EventInfo eventInfo) throws CustomException 
	{
		String abnormalType = "SPC";
		String productionType = "";

		// convert
		Timestamp tStartTime = TimeUtils.getCurrentTimestamp();
		String startTime = TimeUtils.toTimeString(tStartTime);
		String  timeName =abnormalType + "-" + TimeStampUtil.getCurrentEventTimeKey();
		String abnormalName = timeName;
		
		EventInfo eventInfoForSPC = (EventInfo) ObjectUtil.copyTo(eventInfo);
		eventInfoForSPC.setEventName("CreateAbnormalSPC");
		

		AbnormalEQP newAbnormal = new AbnormalEQP();
		
		newAbnormal.setAbnormalEQPName(abnormalEQPName);
		newAbnormal.setAbnormalOperationName(abnormalOperationName);
		newAbnormal.setAbnormalName(abnormalName);
		newAbnormal.setAbnormalState("Created");
		newAbnormal.setAbnormalType(abnormalType);
		newAbnormal.setCreateTime(eventInfoForSPC.getEventTime());
		newAbnormal.setCreateUser(eventInfoForSPC.getEventUser());
		newAbnormal.setDepartment(department); 
		newAbnormal.setLastEventComment(eventInfoForSPC.getEventComment());
		newAbnormal.setLastEventName(eventInfoForSPC.getEventName());
		newAbnormal.setLastEventTime(eventInfoForSPC.getEventTime());
		newAbnormal.setLastEventTimeKey(eventInfoForSPC.getEventTimeKey());
		newAbnormal.setLastEventUser(eventInfoForSPC.getEventUser());
		newAbnormal.setLotName(lotName);
		newAbnormal.setProductNameList(productNameList);
		newAbnormal.setStartTime(tStartTime);
		newAbnormal.setDescription(description);
		newAbnormal.setFactoryName(factory.toUpperCase());
		newAbnormal.setCanReleaseHoldLotFlag("N");
		
		newAbnormal = ExtendedObjectProxy.getAbnormalEQPService().create(eventInfo, newAbnormal);
	
		log.info("autoCreateSPCAbnormal end");
		
		String rabnormalName =newAbnormal.getAbnormalName();
	  
		String userList = getUserList(rabnormalName);
		String createUser = newAbnormal.getCreateUser();
			
	    try
		   {																
				String[] userGroup = userList.split(",");				
				String title = "SPC异常单创建通知";
				String detailtitle = "${}CIM系统消息通知";
				
				StringBuffer info = new StringBuffer();
				info.append("<pre>=======================NoticeInformation=======================</pre>");
				info.append("<pre>	abnormal："+abnormalName+"</pre>");
				info.append("<pre>	abnormalEQPName："+abnormalEQPName+"</pre>");
				info.append("<pre>	createUser："+createUser+"</pre>");
				info.append("<pre>	department："+department+"</pre>");
				info.append("<pre>	startTime："+startTime+"</pre>");
				info.append("<pre>	description："+description+"</pre>");
				info.append("<pre>=============================End=============================</pre>");				
				
				String message = info.toString();
				
				ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userGroup, title, detailtitle, message, "");
				//log.info("eMobile Send Success!");	
				
				StringBuffer weChatInfo = new StringBuffer();
				weChatInfo.append("<pre>======NoticeInformation======</pre>");			
				weChatInfo.append("<pre>	abnormal："+abnormalName+"</pre>");
				weChatInfo.append("<pre> abnormalEQPName："+abnormalEQPName+"</pre>");
				weChatInfo.append("<pre>	createUser："+createUser+"</pre>");
				weChatInfo.append("<pre>	department："+department+"</pre>");
				weChatInfo.append("<pre>	         startTime："+startTime+"</pre>");		
				weChatInfo.append("<pre>	description："+description+"</pre>");
				weChatInfo.append("<pre>	=======NoticeInfoEnd========</pre>");
				
				String weChatMessage = weChatInfo.toString();
				
				ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userGroup, title, detailtitle, weChatMessage, "");
				//log.info("WeChat Send Success!");	
				
				
				StringBuffer EmailInfo = new StringBuffer();
				EmailInfo.append("<pre>======NoticeInformation======</pre>");		
				EmailInfo.append("<pre>	abnormal："+abnormalName+"</pre>");
				EmailInfo.append("<pre>	abnormalEQPName："+abnormalEQPName+"</pre>");
				EmailInfo.append("<pre>	createUser："+createUser+"</pre>");
				EmailInfo.append("<pre>	department："+department+"</pre>");
				EmailInfo.append("<pre>	startTime："+startTime+"</pre>");		
				EmailInfo.append("<pre>	description："+description+"</pre>");
				EmailInfo.append("<pre>	=======NoticeInfoEnd========</pre>");
				//EMailInterface ei = new EMailInterface("mail.visionox.com", "25", "1000", "1000");
				//ei.postMail(emailList,  "异常单创建通知", message.toString(), "V3MES", "V3MES@visionox.com", "V3MES", "vis@2019");
		    }
			catch (Exception e)
			{
				log.info("eMobile or WeChat or email Send Error : " + e.getCause());	
			}
	}
	
	private String getUserList(String rabnormalName)
	{
		List<Map<String,Object>> resultList = null;
		String userList = new String();
		StringBuilder sb = new StringBuilder();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT B.DEPARTMENT FROM CT_ALARMUSERGROUP B, (SELECT A.CREATEUSER FROM CT_ABNORMALEQP A   "
				+ "WHERE A.ABNORMALNAME = :ABNORMALNAME ) C WHERE B.USERID=C.CREATEUSER AND B.ALARMGROUPNAME='AbnormalEQP' "
				+ "UNION SELECT D.DEPARTMENT FROM  CT_ABNORMALEQP D WHERE D.ABNORMALNAME = :ABNORMALNAME");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("ABNORMALNAME", rabnormalName);
	
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		}
		catch(Exception ex)
		{
		    log.info(ex.getCause());
		}
		
		try 
		{
			if (resultList.size() > 0) 
			{
				String departmentAll= "";

				for(Map<String,Object> Dep :  resultList)
				{
					if(departmentAll.isEmpty())
					{
						departmentAll = Dep.get("DEPARTMENT").toString()+departmentAll;
					}
					else
					{
						departmentAll = Dep.get("DEPARTMENT").toString()+","+departmentAll;
					}
					
				}
				List<String> department =  CommonUtil.splitStringDistinct(",",departmentAll);
				department.add("品保");
				
				StringBuffer sql1 = new StringBuffer();
				sql1.append("SELECT * FROM CT_ALARMUSERGROUP  WHERE ALARMGROUPNAME = 'AbnormalEQP' AND DEPARTMENT =:DEPARTMENT AND USERLEVEL='1'");
				Map<String, Object> args1 = new HashMap<String, Object>();

				//for (String department1 : department) 
				for(int j = 0; j < department.size(); j++)
				{
					args1.put("DEPARTMENT", department.get(j));
					List<Map<String, Object>> sqlResult1 = GenericServiceProxy.getSqlMesTemplate()
							.queryForList(sql1.toString(), args1);
					
					if (sqlResult1.size() > 0) 
					{
						if(j < department.size() - 1)
						{
							for (int i = 0; i < sqlResult1.size(); i++) 
							{  
								String user = ConvertUtil.getMapValueByName(sqlResult1.get(i), "USERID");
								sb.append(user + ",");  
				             } 
						}
						else
						{
							for (int i = 0; i < sqlResult1.size(); i++) 
							{  
								String user = ConvertUtil.getMapValueByName(sqlResult1.get(i), "USERID");
				                 if (i < sqlResult1.size() - 1) {  
				                     sb.append(user + ",");  
				                 } else {  
				                     sb.append(user);  
				                 }  
				             } 
						}						 						
					}
				}
				userList = sb.toString();
			}
		}
		catch (Exception e)
		{
			log.info("Not Found the Department of "+ rabnormalName);
			log.info(" Failed to send to EMobile, MachineName: " + rabnormalName);
		}
		return userList;
	}
}
