package kr.co.aim.messolution.alarm.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.alarm.MESAlarmServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
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
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class FDCAlarmReport extends AsyncHandler 
{
	private static Log log = LogFactory.getLog(FDCAlarmReport.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
        ConstantMap constantMap  = GenericServiceProxy.getConstantMap();

		String alarmTimeKey = TimeUtils.getCurrentEventTimeKey();
		String factoryName = SMessageUtil.getHeaderItemValue(doc, "SHOPNAME", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String alarmCode = SMessageUtil.getBodyItemValue(doc, "ALARMCODE", true);
		String alarmState = SMessageUtil.getBodyItemValue(doc, "ALARMSTATE", true);
		String alarmSeverity = SMessageUtil.getBodyItemValue(doc, "ALARMSEVERITY", true);
		String alarmText = SMessageUtil.getBodyItemValue(doc, "ALARMTEXT", true);
		String alarmComment = SMessageUtil.getBodyItemValue(doc, "ALARMCOMMENT", false);
		
		List<Element> productElementList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false);

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("FDCAlarm", machineName, alarmComment, "FDCAlarm", alarmCode);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		AlarmDefinition alarmDef = ExtendedObjectProxy.getAlarmDefinitionService().selectByKey(false, new Object[] { alarmCode, constantMap.AlarmType_FDC,machineName,unitName });
		
		CustomAlarm alarmData = new CustomAlarm(alarmCode, alarmTimeKey);
		alarmData.setAlarmCode(alarmCode);
		alarmData.setAlarmSeverity(alarmSeverity);
		alarmData.setAlarmState(alarmState);
		alarmData.setAlarmType(alarmDef.getAlarmType());
		alarmData.setDescription(alarmText);
		alarmData.setFactoryName(factoryName);
		alarmData.setMachineName(machineName);
		alarmData.setUnitName(unitName);
		alarmData.setSubUnitName(subUnitName);
		alarmData.setCreateTimeKey(eventInfo.getEventTimeKey());
		alarmData.setCreateUser(eventInfo.getEventUser());
		alarmData.setLastEventName(eventInfo.getEventName());
		alarmData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		alarmData.setLastEventUser(eventInfo.getEventUser());
		alarmData.setLastEventComment(eventInfo.getEventComment());

		alarmData = ExtendedObjectProxy.getCustomAlarmService().create(eventInfo, alarmData);
		
		List<MachineAlarmProductList> alarmProductList = new ArrayList<>();
		for(Element productElement : productElementList)
		{
			String productName = productElement.getChildText("PRODUCTNAME");
			if (StringUtil.isEmpty(productName)) continue;

			MachineAlarmProductList mProductList = new MachineAlarmProductList(alarmTimeKey, productName);
			alarmProductList.add(mProductList);
		}
		
		if (alarmProductList.size() > 0)
			ExtendedObjectProxy.getMachineAlarmProductListService().insert(alarmProductList);
		
		try
		{
			doAlarmAction(doc,alarmData, machineData, eventInfo, productElementList);
		}
		catch (Exception ex)
		{	
			if (ex instanceof CustomException)
				throw (CustomException) ex;
			else
				throw new CustomException(ex.getCause());
		}
	}
	
	private void doAlarmAction(Document doc,CustomAlarm alarmData, Machine machineData, EventInfo eventInfo, List<Element> productElementList) throws CustomException
	{
		ConstantMap constMap  = GenericServiceProxy.getConstantMap();
		List<AlarmActionDef> actionList = ExtendedObjectProxy.getAlarmActionDefService().select("alarmCode = ? and alarmType = ? and machineName = ? and unitName =?  ORDER BY seq ", 
																				  new Object[] { alarmData.getAlarmCode(),alarmData.getAlarmType(),alarmData.getMachineName(),alarmData.getUnitName() });
		
		for (AlarmActionDef actionData : actionList)
		{
			String reasonCodeType = actionData.getReasonCodeType();
			String reasonCode = actionData.getReasonCode();

			log.info(String.format("Alarm[%s] AlarmAction[%s] would be executed soon", actionData.getAlarmCode(), actionData.getActionName()));
			log.info("ReasonCodeType: " + reasonCodeType + ", ReasonCode: " + reasonCode);

			if (StringUtils.isNotEmpty(reasonCodeType))
			{
				eventInfo.setReasonCodeType(reasonCodeType);
			}
			if (StringUtils.isNotEmpty(reasonCode))
			{
				eventInfo.setReasonCode(reasonCode);
			}

			if (StringUtils.equals(actionData.getActionName(), constMap.AlarmAction_EQPHold)&&"Y".equals(actionData.getMachineLockFlag()))
			{
				MESAlarmServiceProxy.getAlarmServiceUtil().eqpHoldAction(machineData, eventInfo);
			}
			else if (StringUtils.equals(actionData.getActionName(), constMap.AlarmAction_LotHold) && "Y".equals(actionData.getHoldFlag()))
			{
				MESAlarmServiceProxy.getAlarmServiceUtil().lotHoldAction(alarmData,actionData,machineData,eventInfo, productElementList);
			}
			else if(StringUtils.equals(actionData.getActionName(), constMap.AlarmAction_Mail)&&"Y".equals(actionData.getMailFlag()))
			{
				String [] mailList = CommonUtil.getEmailList(actionData.getAlarmType());
				
				if(mailList == null || mailList.length ==0) continue ;
				String message =createMailContentMessage(alarmData);
				
				try 
				{					
					GenericServiceProxy.getMailSerivce().postMail(mailList, this.getClass().getSimpleName(),message );										
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
				
				//houxk 20210527
				try
				{				
					sendToEm(actionData.getAlarmType(), message);
				}
				catch (Exception e)
				{
					log.info("eMobile or WeChat Send Error : " + e.getCause());	
				}
			}
			else if(StringUtils.equals(actionData.getActionName(), constMap.AlarmAction_OPCall))
			{
				String mSubjectName = machineData.getUdfs().get("MCSUBJECTNAME");
				
				if(mSubjectName.isEmpty())
				{
					log.info(String.format("Machine [%s] MCSUBJECTNAME is Empty!!",machineData.getKey().getMachineName()));
					return ;
				}
				
				GenericServiceProxy.getESBServive().sendBySender(mSubjectName, this.createOPCallMessage(doc, alarmData, mSubjectName), "EISSender");
			}
		}
	}
	
	public Document createOPCallMessage(Document doc, CustomAlarm alarmData,String replySubjectName)
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
		bodyElement.addContent(new Element("OPCALLDESCRIPTION").setText("FDCAlarm : "+ alarmData.getDescription()));
		
		rootElement.addContent(bodyElement);
		
		return new Document(rootElement);
	}
	
	public String createMailContentMessage(CustomAlarm alarmData)
	{
		 String message = "<pre>===============AlarmInformation===============</pre>";
	  	  message += "<pre>==============================================</pre>";
		  message += "<pre>- AlarmCode	: " + alarmData.getAlarmCode() + "</pre>";
		  message += "<pre>- MachineName	: " + alarmData.getMachineName() + "</pre>";
		  message += "<pre>- UnitName	: " + alarmData.getUnitName() + "</pre>";
		  message += "<pre>- SubUnitName	: " + alarmData.getSubUnitName() + "</pre>";
		  message += "<pre>- AlarmState	: " + alarmData.getAlarmState() + "</pre>";
		  message += "<pre>- AlarmComment	: " + alarmData.getLastEventComment() + "</pre>";
		  message += "<pre>==============================================</pre>";
		  
		  return message;
	}
	
	public void sendToEm(String alarmType, String message)
	{
		String[] userList = getUserList(alarmType);	
		if(userList == null || userList.length ==0) return;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("FDCAlarmReport", "MES", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{	
			log.info("FDCAlarm Start Send To Emobile & Wechat");	
						
			String title = "FDCAlarm";
			String detailtitle = "${}CIM系统消息通知";
			String url = "";
									
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userList, title, detailtitle, message, url);
									
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userList, title, detailtitle, message, "");
		}
		catch (Exception e)
		{
			log.info("eMobile or WeChat Send Error : " + e.getCause());	
		} 
	}
	//AlarmType = FDC
	private String[] getUserList(String alarmType)
	{
		String sql = " SELECT DISTINCT  UP.USERID "
				   + " FROM CT_ALARMGROUP AG , CT_ALARMUSERGROUP AU  , USERPROFILE UP"
				   + " WHERE AG.ALARMTYPE = :ALARMTYPE"
				   + " AND AG.ALARMGROUPNAME = AU.ALARMGROUPNAME "
				   + " AND AU.USERID = UP.USERID "
				   + " AND UP.USERID IS NOT NULL ";
		
		List<Map<String, Object>> resultList = null;
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new String[] { alarmType });
		}
		catch (Exception ex)
		{
			log.info(ex.getCause());
		}

		if(resultList ==null || resultList.size()==0) return null;
		
		return CommonUtil.makeListBySqlResult(resultList, "USERID").toArray(new String[] {});
	}
}
