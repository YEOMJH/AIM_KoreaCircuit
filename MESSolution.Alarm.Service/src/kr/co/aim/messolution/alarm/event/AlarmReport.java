package kr.co.aim.messolution.alarm.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.alarm.MESAlarmServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlarmActionDef;
import kr.co.aim.messolution.extended.object.management.data.AlarmDefinition;
import kr.co.aim.messolution.extended.object.management.data.MachineAlarmList;
import kr.co.aim.messolution.extended.object.management.data.MachineAlarmProductList;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EMailInterface;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class AlarmReport extends AsyncHandler {

	private static Log log = LogFactory.getLog(AlarmReport.class);
	
	@SuppressWarnings("unchecked")
	@Override
	public void doWorks(Document doc) throws CustomException
	{
        ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		String alarmCode = SMessageUtil.getBodyItemValue(doc, "ALARMCODE", true);
		String alarmState = SMessageUtil.getBodyItemValue(doc, "ALARMSTATE", true);
		String alarmSeverity = SMessageUtil.getBodyItemValue(doc, "ALARMSEVERITY", true);
		String alarmText = SMessageUtil.getBodyItemValue(doc, "ALARMTEXT", false);

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String sunitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String ssubUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		
		String alarmTimeKey = TimeUtils.getCurrentEventTimeKey();
		List<Element> productElementList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);

		if (StringUtils.isEmpty(subUnitName))
			subUnitName = "-";


		if (StringUtils.isEmpty(unitName))
			unitName = "-";
		else
			MachineServiceProxy.getMachineService().select(" superMachineName = ? and machineName = ? ", new Object[] { machineName, unitName });
		
		Machine machineData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Issue", getEventUser(), getEventComment(), "AlarmHold",alarmCode);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		//=======================AlarmReportForOven Start===========================//
		String sql = "SELECT * FROM ENUMDEFVALUE E WHERE E.ENUMNAME = 'AlarmCodeForOven' AND E.DEFAULTFLAG = 'Y' AND E.ENUMVALUE = :ENUMVALUE ";
		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("ENUMVALUE", alarmCode);
		
		List<Map<String, Object>> sqlResult = null;
		try 
		{
			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql,bindMap);
		} 
		catch (Exception e) 
		{
			log.info("No Data From EnumDefValue ENUMNAME = 'AlarmCodeForOven' ");
		}

		if(machineName.contains("3TOV") && alarmState.equals(constMap.AlarmState_ISSUE) 
				&& sqlResult != null && sqlResult.size()>0)
		{
			/**
			 * TP OVEN獵삣춴汝믣몜鈺��싩윥�돶若싦볶�몮
			 */
			try
			{				
				String alarmTxt = sqlResult.get(0).get("DESCRIPTION").toString();
				StringBuffer info = new StringBuffer();
				info.append("<pre>=======================AlarmInformation=======================</pre>");
				info.append("<pre>	MachineName竊�"+machineName+"</pre>");
				info.append("<pre>	UnitName竊�"+unitName+"</pre>");
				info.append("<pre>	AlarmCode竊�"+alarmCode+"</pre>");
				info.append("<pre>	AlarmText竊�"+alarmText+"</pre>");
				info.append("<pre>		"+alarmTxt+"</pre>");
				info.append("<pre>=============================End=============================</pre>");			
				
				String message = info.toString();
				
				sendToEMForOven(message);
			}
			catch (Exception e)
			{
				log.info("eMobile or WeChat Send Error : " + e.getCause());	
			}
		}
		//=======================AlarmReportForOven End===========================//
		
		List<AlarmDefinition> alarmDefList = ExtendedObjectProxy.getAlarmDefinitionService().getAlarmData(alarmCode, machineName, unitName);

		List<AlarmActionDef> alarmActionList = null;

		if (alarmDefList != null && alarmDefList.size() > 0) 
		{
			alarmActionList = ExtendedObjectProxy.getAlarmActionDefService().getAlarmActionDefData(alarmCode, machineName, unitName);
		}
		
		MachineAlarmList machineAlarmList = ExtendedObjectProxy.getMachineAlarmListService().getMachineAlarmListData(machineName, unitName, subUnitName, alarmCode);
		
		if (machineAlarmList != null)
		{
			machineAlarmList.setAlarmState(alarmState);
			machineAlarmList.setAlarmSeverity(alarmSeverity);
			machineAlarmList.setAlarmText(alarmText);
			machineAlarmList.setAlarmTimeKey(alarmTimeKey);

			machineAlarmList.setEventName(eventInfo.getEventName());
			machineAlarmList.setEventUser(eventInfo.getEventUser());
			machineAlarmList.setEventTime(eventInfo.getEventTime());
			machineAlarmList.setEventTimeKey(eventInfo.getEventTimeKey());
			machineAlarmList.setEventComment(eventInfo.getEventComment());

			if (alarmState.equals(constMap.AlarmState_ISSUE))
			{
				log.info(" Update original alarm for ISSUE");
				eventInfo.setEventName("Issue");
				eventInfo.setEventUser(machineName);
				ExtendedObjectProxy.getMachineAlarmListService().modify(eventInfo, machineAlarmList);
				
				if(alarmActionList!=null && alarmActionList.size()>0)
				{
					doAlarmAction(alarmActionList,machineAlarmList, machineData, eventInfo, productElementList);
				}
				SendEmailByMachineGroup(machineData, machineName, sunitName, ssubUnitName, productElementList, alarmText, eventInfo, alarmCode, alarmSeverity);				
			}
			else if (alarmState.equals(constMap.AlarmState_CLEAR))
			{
				log.info(" Delete original alarm");
				eventInfo.setEventName("Clear");
				ExtendedObjectProxy.getMachineAlarmListService().remove(eventInfo, machineAlarmList);
			}
		}
		else
		{
			log.info(" No exist Data,Insert new Data");
			machineAlarmList = new MachineAlarmList(machineName, unitName, subUnitName, alarmCode);

			machineAlarmList.setAlarmState(alarmState);
			machineAlarmList.setAlarmSeverity(alarmSeverity);
			machineAlarmList.setAlarmText(alarmText);

			machineAlarmList.setEventName(eventInfo.getEventName());
			machineAlarmList.setEventUser(eventInfo.getEventUser());
			machineAlarmList.setEventTime(eventInfo.getEventTime());
			machineAlarmList.setEventTimeKey(eventInfo.getEventTimeKey());
			machineAlarmList.setEventComment(eventInfo.getEventComment());

			if (alarmState.equals(constMap.AlarmState_ISSUE))
			{
				log.info(" Create new Issue Alarm");
				eventInfo.setEventName("Issue");
				eventInfo.setEventUser(machineName);
				ExtendedObjectProxy.getMachineAlarmListService().create(eventInfo, machineAlarmList);
				
				if(alarmActionList!=null && alarmActionList.size()>0)
				{
					doAlarmAction(alarmActionList,machineAlarmList, machineData, eventInfo, productElementList);
				}
				SendEmailByMachineGroup(machineData, machineName, sunitName, ssubUnitName, productElementList, alarmText, eventInfo, alarmCode, alarmSeverity);			
			}
			else if (alarmState.equals(constMap.AlarmState_CLEAR))
			{
				eventInfo.setEventName("Clear");
				ExtendedObjectProxy.getMachineAlarmListService().addHistory(eventInfo, "MachineAlarmHistory", machineAlarmList, LogFactory.getLog(MachineAlarmList.class));
			}

		}
		
		List<MachineAlarmProductList> alarmProductList = new ArrayList<>();
		for(Element productElement : productElementList)
		{
			String productName = productElement.getChildText("PRODUCTNAME");
			String maskName = productElement.getChildText("MASKNAME");
			//if(StringUtil.isEmpty(productName )) continue;
			//modify by cjl 20201026 :MastTest : ProductList , report MASKNAME is not null, report productname is null ,then set as '-',and get different timekey insert data. other not changed.
			if(StringUtil.isEmpty(productName ) && StringUtil.isEmpty(maskName ))
			{
				continue ;				
			}
			else
			{
				if(StringUtil.isEmpty(productName ))
				{
					productName = "-";
					alarmTimeKey = TimeUtils.getCurrentEventTimeKey();
				}				    
			}
			
			
			 Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			
		     MachineAlarmProductList mProductList = new MachineAlarmProductList(alarmTimeKey,productName);
		     mProductList.setMaskName(maskName);
		     mProductList.setProcessOperationName(productData.getProcessOperationName());
		     alarmProductList.add(mProductList);
		}

		if (alarmProductList.size() > 0)
			ExtendedObjectProxy.getMachineAlarmProductListService().insert(alarmProductList);

		// send to PMS
		GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("PMS"), doc, "PMSSender");
	}

	private void SendEmailByMachineGroup(Machine machineData, String machineName, String unitName, String subUnitName, List<Element> productElementList, String alarmText, EventInfo eventInfo, String alarmCode, String alarmSeverity) 
	{
		boolean notSkipManualFlag = false;
		if (!CommonUtil.getEnumDefValueStringByEnumNameAndEnumValue("AlarmReportNotSkipManual", machineName).isEmpty())
			notSkipManualFlag = true;
			
		if(machineData.getMachineStateName().equals("MANUAL") && !notSkipManualFlag)
			return;
		
		if(!unitName.isEmpty())
		{
			try 
			{
				Machine unitData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
				if(unitData.getMachineStateName().equals("MANUAL") && !notSkipManualFlag)
					return;
			} 
			catch (CustomException e) 
			{
				return;
			}
		}
		
		if(!subUnitName.isEmpty())
		{
			try 
			{
				Machine subUnitData =  MESMachineServiceProxy.getMachineInfoUtil().getMachineData(subUnitName);
				if(subUnitData.getMachineStateName().equals("MANUAL") && !notSkipManualFlag)
					return;
			} 
			catch (CustomException e) 
			{
				return;
			}
		}
		
		List<Map<String, Object>> sqlResult = null;
		try
		{
			/**
			 * DEFAULTFLAG: Y竊덅럼瓦뉑뙁若쉆larmCode竊�
			 * SEQ: Y竊덂룕�곫뙁若쉆larmCode竊�
			 * DISPLAYCOLOR竊� Y竊덅퓝譯짲ightAlarm竊�
			 */
			MachineSpec machineSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
			String sql = "SELECT * FROM ENUMDEFVALUE E WHERE E.ENUMNAME = 'AlarmReportEmail' AND E.ENUMVALUE = :ENUMVALUE AND E.DESCRIPTION LIKE '%'|| :DESCRIPTION ||'%' ";
			
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("ENUMVALUE", machineSpec.getMachineGroupName());
			bindMap.put("DESCRIPTION", machineData.getFactoryName());
			
			sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql,bindMap);
		}
		catch(Exception ex)
		{
			eventLog.info(ex.getCause());
		}
		
		//mailFlag-瓮녘퓝�쉪Code
		Boolean mailFlag = true;
		try
		{
			MachineSpec machineSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
			String sql = "SELECT * FROM ENUMDEFVALUE E WHERE E.ENUMNAME = 'AlarmReportEmailSkip' AND E.ENUMVALUE = :ENUMVALUE";
			
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("ENUMVALUE", unitName + "-" + alarmCode);
			
			List<Map<String, Object>> sqlRs = GenericServiceProxy.getSqlMesTemplate().queryForList(sql,bindMap);
			if(sqlRs.size() == 0 || sqlRs == null)
			{
				mailFlag = false;
			}
		}
		catch(Exception ex)
		{
			eventLog.info(ex.getCause());
		}
		
		//specificFlag-�돶若싩쉪Code
		Boolean specificFlag = false;
		String fmbColor = "";
		String alarmDesc = "";
		try
		{
			MachineSpec machineSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
			String sql = "SELECT * FROM ENUMDEFVALUE E WHERE E.ENUMNAME = 'AlarmReportEmailSpecific' AND E.ENUMVALUE = :ENUMVALUE";
			
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("ENUMVALUE", unitName + "-" + alarmCode);
			
			List<Map<String, Object>> sqlRs = GenericServiceProxy.getSqlMesTemplate().queryForList(sql,bindMap);
			if(sqlRs != null && sqlRs.size() > 0)
			{
				specificFlag = true;
				fmbColor = sqlRs.get(0).get("DISPLAYCOLOR").toString();
				alarmDesc = sqlRs.get(0).get("DESCRIPTION").toString();
			}
		}
		catch(Exception ex)
		{
			eventLog.info(ex.getCause());
		}
		
		if(sqlResult.size() > 0 && sqlResult != null)
		{
			if (sqlResult.get(0).get("DEFAULTFLAG").equals("Y") && mailFlag)
				return;
			
			if (sqlResult.get(0).get("DISPLAYCOLOR").equals("Y") && alarmSeverity.equals("LIGHT"))
				return;
			
			if (sqlResult.get(0).get("SEQ").equals("Y") && !specificFlag)
				return;
			
			eventLog.info(" AlarmReportEmail Flag: "+ machineName +" : ON ");
			
			//Execute Message
			StringBuffer messageInfo = executeMessage(machineName, unitName, subUnitName, productElementList, alarmText, eventInfo, alarmCode, alarmSeverity);
			
			List<String> emailList = null;
			emailList = MESLotServiceProxy.getLotServiceImpl().getEmailList(machineName,"AlarmReportEmail");
			
			if(emailList !=null && emailList.size()>0)
			{
				try 
				{					
					EMailInterface ei = new EMailInterface("mail.visionox.com", "25", "1000", "1000");
					ei.postMail(emailList,  " EQPAlarmReport ", messageInfo.toString(), "V3MES", "V3MES@visionox.com", "V3MES", "vis@2019");
					//MESLotServiceProxy.getLotServiceImpl().sendEmail(emailList, messageInfo.toString(), "EQP Alarm Report");
				} 
				catch (Exception e) 
				{
					eventLog.info(" Failed to send mail. ");
				}
			}
								
			//houxk 20210601			
			try
			{				
				sendToEm(machineName,messageInfo.toString());
				
				if(!fmbColor.isEmpty())
					sendToFMB(machineName, unitName, alarmCode, alarmText, alarmDesc, fmbColor, eventInfo, machineData.getFactoryName());
			}
			catch (Exception e)
			{
				log.info("eMobile or WeChat Send Error : " + e.getCause());	
			}
		}
	}
	private void sendToFMB(String machineName, String unitName, String alarmCode, String alarmText, String alarmDesc, String fmbColor, EventInfo eventInfo, String factoryName) 
	{
		Element rootElement = new Element(SMessageUtil.Message_Tag);
		
		Element headerElement = new Element(SMessageUtil.Header_Tag);
			
		headerElement.addContent(new Element("MESSAGENAME").setText("SpecialAlarmReport"));
		//headerElement.addContent(new Element("TRANSACTIONID").setText(TimeUtils.getCurrentEventTimeKey()));
		//headerElement.addContent(new Element("TRANSACTIONID").setText(TimeStampUtil.getEventTimeKeyFromTimestamp(tLastEventTime)));
		headerElement.addContent(new Element("ORIGINALSOURCESUBJECTNAME").setText(""));
		headerElement.addContent(new Element("EVENTUSER").setText(eventInfo.getEventUser()));
		headerElement.addContent(new Element("EVENTCOMMENT").setText(eventInfo.getEventComment()));
		headerElement.addContent(new Element("LANGUAGE").setText(""));
		
		rootElement.addContent(headerElement);
		
		Element bodyElement = new Element(SMessageUtil.Body_Tag);
		XmlUtil.addElement(bodyElement, "FACTORYNAME", factoryName);
		XmlUtil.addElement(bodyElement, "MACHINENAME", machineName);
		XmlUtil.addElement(bodyElement, "UNITNAME", unitName);
		XmlUtil.addElement(bodyElement, "ALARMTEXT", alarmCode + ": " + alarmText + ". (" + alarmDesc + ")");
		XmlUtil.addElement(bodyElement, "COLOR", fmbColor);
		rootElement.addContent(bodyElement);
		
		//Send to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(new Document(rootElement));
	}

	private StringBuffer executeMessage(String machineName, String unitName, String subUnitName, List<Element> productElementList, String alarmText, EventInfo eventInfo, String alarmCode, String alarmSeverity) 
	{
		StringBuffer messageBF = new StringBuffer();
		messageBF.append("<pre>-Machine Name: </pre>"
				+ "<pre> " + machineName + "</pre>"
				+ "<pre>-Alarm Text: </pre>"
				+ "<pre> " + alarmText + "</pre>"
				+"<pre>-Alarm Code: </pre>"
				+ "<pre> " + alarmCode + "</pre>"
				+ "<pre>-Alarm Severity: </pre>"
				+ "<pre> " + alarmSeverity + "</pre>");
		
		if (StringUtil.isNotEmpty(subUnitName))
		{
			messageBF.append("<pre>-SubUnitName: </pre>"
					+ "<pre> " + subUnitName + "</pre>");
		}
		else if (StringUtil.isEmpty(subUnitName) && StringUtil.isNotEmpty(unitName)) 
		{
			messageBF.append("<pre>-UnitName: </pre>"
					+ "<pre> " + unitName + "</pre>");
		}

		if (StringUtil.isNotEmpty(makeListByKey(productElementList,"PRODUCTNAME")))
		{
			List<String> lotList = new ArrayList<String>();
			List<String> carrierList = new ArrayList<String>();
			List<String> productList = new ArrayList<String>();
			List<String> productSpec = new ArrayList<String>();
			for (Element productE : productElementList)
			{
				String productName = productE.getChildText("PRODUCTNAME");
				String sql1 = "SELECT PRODUCTNAME ,LOTNAME FROM PRODUCT WHERE PRODUCTNAME = :PRODUCTNAME AND PRODUCTSTATE = 'InProduction'";
				Map<String, Object> bindMap1 = new HashMap<String, Object>();
				bindMap1.put("PRODUCTNAME", productName);
				List<Map<String, Object>> Result1 = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql1, bindMap1);
				
				if (Result1.size() > 0 && Result1 != null)
				{
					String lotName = Result1.get(0).get("LOTNAME").toString();
					Lot lotData = null;
					try 
					{
						lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
					} 
					catch (Exception e) 
					{
						eventLog.info(e.getCause());
					}
					if (lotData != null)
					{
						carrierList.add(lotData.getCarrierName());
						lotList.add(lotName);
						productList.add(productName);
						productSpec.add(lotData.getProductSpecName());
					}
				}
			}
			messageBF.append("<pre>-ProductSpec: </pre>"
					+ "<pre> " + productSpec + "</pre>"
					+ "<pre>-CARRIERNAME: </pre>"
					+ "<pre> " + carrierList + "</pre>"
					+ "<pre>-LOTNAME: </pre>"
					+ "<pre> " + lotList + "</pre>"
					+ "<pre>-PRODUCTNAME: </pre>"
					+ "<pre> " + productList + "</pre>");
		}
		messageBF.append("<pre>-Timekey: </pre>"
				+ "<pre> " + eventInfo.getEventTimeKey() + "</pre>");
				
		return messageBF;
		
	}
	public void sendRunOutMail(AlarmActionDef alarmActionDef)
	{
		List<Map<String,Object>> resultList = null;
	    String sql =" SELECT P.EMAIL FROM ( SELECT  C.USERID "
	    							    + " FROM CT_ALARMACTIONDEF A, CT_ALARMGROUP B , CT_ALARMUSERGROUP C "
	    							    + " WHERE A.ALARMCODE = :ALARMCODE AND A.ALARMTYPE = :ALARMTYPE"
	    							    + " AND A.MACHINENAME = :MACHINENAME AND A.UNITNAME = :UNITNAME"
	    							    + " AND A.ACTIONNAME = 'Mail' AND A.ALARMTYPE = B.ALARMTYPE "
	    							    + " AND B.ALARMGROUPNAME = C.ALARMGROUPNAME "
	    							    + " AND C.USERLEVEL = ( SELECT NVL(MIN(USERLEVEL),999999) "
	    							    					+ " FROM CT_ALARMGROUP G , CT_ALARMUSERGROUP U "
	    							    					+ " WHERE G.ALARMTYPE = :ALARMTYPE"
	    							    					+ " AND G.ALARMGROUPNAME = U.ALARMGROUPNAME  )) U , USERPROFILE P "
	    		 + " WHERE U.USERID = P.USERID ";
	    
		Map<String,Object> bindMap = new HashMap<>();
		bindMap.put("ALARMCODE", alarmActionDef.getAlarmCode());
		bindMap.put("ALARMTYPE", alarmActionDef.getAlarmType());
		bindMap.put("MACHINENAME", alarmActionDef.getMachineName());
		bindMap.put("UNITNAME",alarmActionDef.getUnitName());
	
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		
		}catch(Exception ex)
		{
		    log.info(ex.getCause());
		}
		
		if(resultList !=null && resultList.size()>0)
		{
			String message = "<pre>===============MateriaQTimeOverAlarm===============</pre>";
	  	  	   message += "<pre>===================================================</pre>";
		
			List<String> userList = CommonUtil.makeListBySqlResult(resultList, "EMAIL");
			try {
				GenericServiceProxy.getMailSerivce().postMail(userList.toArray(new String[] {}), this.getClass().getSimpleName(),message);
			} 
			catch (Exception e) 
			{
				log.error("Failed to send mail.");
				e.printStackTrace();
			}
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
	
	public String createMailContentMessage(MachineAlarmList alarmData,List<Element> productElementList)
	{
		String message = "<pre>===============AlarmInformation===============</pre>";
	  	  message += "<pre>==============================================</pre>";
	      message += "<pre>- AlarmCode	: " + alarmData.getAlarmCode() + "</pre>";
	  	  message += "<pre>- MachineName	: " + alarmData.getMachineName() + "</pre>";
	  	  message += "<pre>- UnitName	: " + alarmData.getUnitName() + "</pre>";
	  	  message += "<pre>- SubUnitName	: " + alarmData.getSubUnitName() + "</pre>";
	  	  message += "<pre>- AlarmTime	: " + alarmData.getEventTimeKey() + "</pre>";
	  	  message += "<pre>- AlarmState	: " + alarmData.getAlarmState() + "</pre>";
	  	  message += "<pre>- AlarmSeverity	: " + alarmData.getAlarmSeverity() + "</pre>";
	  	  message += "<pre>- AlarmText	: " + alarmData.getAlarmText() + "</pre>";
	  	
	  	  if (StringUtil.isNotEmpty(makeListByKey(productElementList,"PRODUCTNAME")))
	  	  {
	  		  message += "<pre>- ProductList</pre>";
	  		  for (Element productE : productElementList)
	  		  {
	  			  message += "<pre>	ProductName	: " + productE.getChildText("PRODUCTNAME") + "</pre>";
	  			  message += "<pre>	MaskName	: " + productE.getChildText("MASKNAME") + "</pre>";
	  		  }
	      }
	  	  
	  	  message += "<pre>==============================================</pre>";
		  return message;
	}

	private String alarmSmsText(String alarmCode, String machineName, String unitName, String subUnitName, String alarmState, String alarmSeverity, String alarmText,String alarmTime )
	{
		StringBuilder smsMessage = new StringBuilder();
		smsMessage.append("\n");
		smsMessage.append("AlarmCode: ");
		smsMessage.append(alarmCode);
		smsMessage.append("\n");
		smsMessage.append("AlarmType: ");
		smsMessage.append("EQP");
		smsMessage.append("\n");
		smsMessage.append("Machine: ");
		smsMessage.append(machineName);
		smsMessage.append("\n");
		smsMessage.append("UnitName: ");
		smsMessage.append(unitName);
		smsMessage.append("\n");
		smsMessage.append("SubUnitName: ");
		smsMessage.append(subUnitName);
		smsMessage.append("\n");
		smsMessage.append("AlarmTime: ");
		smsMessage.append(alarmTime);
		smsMessage.append("\n");
		smsMessage.append("AlarmState: ");
		smsMessage.append(alarmState);
		smsMessage.append("\n");
		smsMessage.append("AlarmSeverity: ");
		smsMessage.append(alarmSeverity);
		smsMessage.append("\n");
		smsMessage.append("AlarmText: ");
		smsMessage.append(alarmText);

		return smsMessage.toString();
	}
	private void doAlarmAction(List<AlarmActionDef> actionDataList,MachineAlarmList machineAlarmData,Machine machineData, EventInfo eventInfo, List<Element> productElementList ) throws CustomException
	{
		ConstantMap constMap  = GenericServiceProxy.getConstantMap();

		for (AlarmActionDef actionData : actionDataList)
		{
			String reasonCodeType = actionData.getReasonCodeType();
			String reasonCode = actionData.getReasonCode();

			eventLog.info(String.format("Alarm[%s] AlarmAction[%s] would be executed soon", actionData.getAlarmCode(), actionData.getActionName()));
			log.info("ReasonCodeType: " + reasonCodeType + ", ReasonCode: " + reasonCode);

			if (StringUtils.isNotEmpty(reasonCodeType))
			{
				eventInfo.setReasonCodeType(reasonCodeType);
			}
			if (StringUtils.isNotEmpty(reasonCode))
			{
				eventInfo.setReasonCode(reasonCode);
			}

			if (StringUtils.equals(actionData.getActionName(), constMap.AlarmAction_EQPHold)&&!"N".equals(actionData.getMachineLockFlag()))
			{
				MESAlarmServiceProxy.getAlarmServiceUtil().eqpHoldAction(machineData, eventInfo);
			}
			else if (StringUtils.equals(actionData.getActionName(), constMap.AlarmAction_LotHold) && !"N".equals(actionData.getHoldFlag()))
			{
				MESAlarmServiceProxy.getAlarmServiceUtil().lotHoldAction(machineAlarmData,actionData,machineData, eventInfo, productElementList);
			}
			else if(StringUtils.equals(actionData.getActionName(), constMap.AlarmAction_Mail)&&!"N".equals(actionData.getMailFlag()))
			{
				String [] mailList = CommonUtil.getEmailList(actionData.getAlarmType());
				if(mailList == null || mailList.length ==0) continue ;
				
				String message = createMailContentMessage(machineAlarmData,productElementList);
				
				try {
					
					GenericServiceProxy.getMailSerivce().postMail(mailList, this.getClass().getSimpleName(), message);
				} 
				catch (Exception  ex) 
				{
					if(ex instanceof CustomException)
					{
						log.info(((CustomException)ex).errorDef.getEng_errorMessage());
						CommonUtil.sendSMSWhenPostMailFail(message);
					}
					else 
					{
						throw new CustomException(ex.getCause());
					}
				}
			}else if(actionData.getAlarmType().equals(constMap.AlarmType_RunOut)&&!"N".equals(actionData.getMailFlag()))
			{
				//this.sendRunOutMail(actionData);
		    }
			else if(StringUtils.equals(actionData.getActionName(), constMap.AlarmAction_CHPause)&&!"N".equals(actionData.getMachineLockFlag()))
			{
				
			}
			else if(StringUtils.equals(actionData.getActionName(), constMap.AlarmAction_EQPHalt)&&!"N".equals(actionData.getMachineLockFlag()))
			{
				
			}
			else if(StringUtils.equals(actionData.getActionName(), constMap.AlarmAction_OPCall))
			{
				
			}
			
			// Send SMS
			String smsMessage = alarmSmsText(machineAlarmData.getAlarmCode(), machineAlarmData.getMachineName(), machineAlarmData.getUnitName(),machineAlarmData.getSubUnitName(), machineAlarmData.getAlarmState(), machineAlarmData.getAlarmSeverity(), machineAlarmData.getAlarmText(),machineAlarmData.getEventTimeKey());
			//GenericServiceProxy.getSMSInterface().AlarmSmsSend(machineAlarmData.getAlarmCode(), actionData.getAlarmType(), machineData.getFactoryName(), machineAlarmData.getMachineName(), machineAlarmData.getUnitName(), smsMessage);
		}
	}
	
	public void sendToEm(String machineName, String messageInfo)
	{
		String[] userList = getUserList(machineName,"AlarmReportEmail");	
		if(userList == null || userList.length ==0) return;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("AlarmReport", "MES", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{	
			log.info("EQPAlarmReport Start Send To Emobile & Wechat");	
						
			String title = "EQP Alarm Report";
			String detailtitle = "${}CIM楹사퍨易덃겘�싩윥";
			String url = "";
			
			StringBuffer info = new StringBuffer();
			info.append("<pre>=======================AlarmInformation=======================</pre>");
			info.append(messageInfo);
			info.append("<pre>=============================End=============================</pre>");			
			
			String message = info.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userList, title, detailtitle, message, url);
									
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userList, title, detailtitle, message, "");
		}
		catch (Exception e)
		{
			log.info("eMobile or WeChat Send Error : " + e.getCause());	
		} 
	}
	
	//AlarmGroup = AlarmReportEmail && RMSDepart
	private String[] getUserList(String machineName, String alarmGroupName)
	{
		List<Map<String,Object>> resultList = null;
		String[] userList = null;
		List<String> sb = new ArrayList<String>();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT MS.RMSDEPARTMENT FROM MACHINESPEC MS WHERE MS.MACHINENAME = :MACHINENAME");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("MACHINENAME", machineName);
	
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
				String departmentAll = resultList.get(0).get("RMSDEPARTMENT").toString();

				List<String> department =  CommonUtil.splitStringDistinct(",",departmentAll);

				StringBuffer sql1 = new StringBuffer();
				sql1.append(
						"SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B, USERPROFILE C WHERE A.USERID = B.USERID AND B.USERID = C.USERID AND A.ALARMGROUPNAME = :ALARMGROUPNAME AND B.DEPARTMENT=:DEPARTMENT");
				Map<String, Object> args1 = new HashMap<String, Object>();

				for(int j = 0; j < department.size(); j++)
				{
					args1.put("ALARMGROUPNAME", alarmGroupName);
					args1.put("DEPARTMENT", department.get(j));
					List<Map<String, Object>> sqlResult1 = GenericServiceProxy.getSqlMesTemplate()
							.queryForList(sql1.toString(), args1);
					
					if (sqlResult1.size() > 0) 
					{
						for (Map<String, Object> userInfo : sqlResult1)
						{
							String user = ConvertUtil.getMapValueByName(userInfo, "USERID");
							sb.add(user);
						}										 						
					}
				}
				userList = sb.toArray(new String[] {});
			}
		}
		catch (Exception e)
		{
			log.info("Not Found the Department of "+ machineName);
			log.info(" Failed to send to EMobile, MachineName: " + machineName);
		}
		return userList;			
	}
	
	public String[] getAlarmUserIdForOven() throws CustomException
	{
		String[] userGroup = null;
		StringBuffer userList = new StringBuffer();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT B.USERID FROM CT_ALARMUSERGROUP B  ");
		sql.append(" WHERE 1=1 ");
		sql.append(" AND B.ALARMGROUPNAME = 'AlarmReportForOven' ");
		sql.append(" AND B.USERID LIKE 'V00%' ");
		Map<String, Object> args = new HashMap<String, Object>();
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(),args);

		if(sqlResult != null && sqlResult.size()>0)
		{
			for (int i = 0; i < sqlResult.size(); i++) {
				userList = userList.append(sqlResult.get(i).get("USERID").toString()+";");
			}
			userGroup = userList.toString().split(";") ;
		}
		else
		{
			userGroup = null ;
		}
		
		return userGroup;
	}
	
	private void sendToEMForOven(String message) throws CustomException 
	{
		String[] userList = getAlarmUserIdForOven();	
		if(userList == null || userList.length ==0) return;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("AlarmReport", "MES", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{	
			log.info("EQPAlarmReport Start Send To Emobile & Wechat");	
			
			String title = "EQP Alarm Report";
			String detailtitle = "${}CIM楹사퍨易덃겘�싩윥";
			String url = "";
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userList, title, detailtitle, message, url);
									
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userList, title, detailtitle, message, url);
		}
		catch (Exception e)
		{
			log.info("eMobile or WeChat Send Error : " + e.getCause());	
		} 
	}
}
