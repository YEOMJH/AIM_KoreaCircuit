package kr.co.aim.messolution.alarm.event;

import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class SPCToMESSendAlarm extends AsyncHandler {
	private static Log log = LogFactory.getLog(SPCToMESSendAlarm.class);
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String theme = SMessageUtil.getBodyItemValue(doc, "THEME", false);
		String alarmIndex = SMessageUtil.getBodyItemValue(doc, "ALARMINDEX", false);
		String url = SMessageUtil.getBodyItemValue(doc, "URL", true);
		String dataDate = SMessageUtil.getBodyItemValue(doc, "DATADATE", false);
		String loaderDate = SMessageUtil.getBodyItemValue(doc, "LOADERDATE", false);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", false);
		String mean = SMessageUtil.getBodyItemValue(doc, "MEAN", false);
		String std = SMessageUtil.getBodyItemValue(doc, "STD", false);
		String range = SMessageUtil.getBodyItemValue(doc, "RANGE", false);
		String max = SMessageUtil.getBodyItemValue(doc, "MAX", false);
		String min = SMessageUtil.getBodyItemValue(doc, "MIN", true);
		String unif = SMessageUtil.getBodyItemValue(doc, "UNIF", false);
		String count = SMessageUtil.getBodyItemValue(doc, "COUNT", false);
		String usl = SMessageUtil.getBodyItemValue(doc, "USL", false);
		String target = SMessageUtil.getBodyItemValue(doc, "TARGET", false);
		String lsl = SMessageUtil.getBodyItemValue(doc, "LSL", false);
		String spcModule = SMessageUtil.getBodyItemValue(doc, "SPCMODULE", false);
		String charID = SMessageUtil.getBodyItemValue(doc, "CHARTID", false);
//		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
//		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
//		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
//		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
//		String itemName = SMessageUtil.getBodyItemValue(doc, "ITEMNAME", true);
		String spcModuleInfo = SMessageUtil.getBodyItemValue(doc, "SPCMODULEINFO", true);
		String otherInformation = SMessageUtil.getBodyItemValue(doc, "OTHERINFORMATION", false);
		String userList = SMessageUtil.getBodyItemValue(doc, "USERLIST", true);		
		
		
		String productSpecName = "";
		String lotName = "";
		String processOperationName = "";
		String machineName = "";
		String itemName = "";
		
		String[] spcModuleInfoList = spcModuleInfo.split(";");	
		if(spcModuleInfoList != null && spcModuleInfoList.length >0)
		{
			for (int i = 0; i < spcModuleInfoList.length; i++) {
				if(spcModuleInfoList[i].trim().startsWith("PRODUCTSPECNAME:"))
				{
					productSpecName = spcModuleInfoList[i].split("PRODUCTSPECNAME:")[1].trim();
					continue;
				}
				if(spcModuleInfoList[i].trim().startsWith("LOTNAME:"))
				{
					lotName = spcModuleInfoList[i].split("LOTNAME:")[1];
					continue;
				}
				if(spcModuleInfoList[i].trim().startsWith("PROCESSOPERATIONNAME:"))
				{
					processOperationName = spcModuleInfoList[i].split("PROCESSOPERATIONNAME:")[1];
					continue;
				}
				if(spcModuleInfoList[i].trim().startsWith("MACHINENAME:"))
				{
					machineName = spcModuleInfoList[i].split("MACHINENAME:")[1];
					continue;
				}
				if(spcModuleInfoList[i].trim().startsWith("ITEMNAME:"))
				{
					itemName = spcModuleInfoList[i].split("ITEMNAME:")[1];
					continue;
				}
				
			}
		}
		//Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);	

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SPCToMESSendAlarm", "SPCSystem", "", "SPCAlarm", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{																
			String[] userGroup = userList.split(";");				
			String title = theme;
			String detailtitle = "${}CIM系统消息通知";
			
			StringBuffer info = new StringBuffer();
			info.append("<pre>=======================AlarmInformation=======================</pre>");
//			info.append("<pre>	alarmIndex："+alarmIndex+"</pre>");
			info.append("<pre>	url："+url+"</pre>");
//			info.append("<pre>	dataDate："+dataDate+"</pre>");
//			info.append("<pre>	loaderDate："+loaderDate+"</pre>");
//			info.append("<pre>	productName："+productName+"</pre>");
//			info.append("<pre>	mean："+mean+"</pre>");
//			info.append("<pre>	std："+std+"</pre>");
//			info.append("<pre>	range："+range+"</pre>");
//			info.append("<pre>	max："+max+"</pre>");
//			info.append("<pre>	min："+min+"</pre>");
//			info.append("<pre>	unif："+unif+"</pre>");
//			info.append("<pre>	count："+count+"</pre>");
//			info.append("<pre>	usl："+usl+"</pre>");
//			info.append("<pre>	target："+target+"</pre>");
//			info.append("<pre>	lsl："+lsl+"</pre>");
			info.append("<pre>	spcModule："+spcModule+"</pre>");
//			info.append("<pre>	charID："+charID+"</pre>");
			info.append("<pre>	productSpecName："+productSpecName+"</pre>");
			info.append("<pre>	lotName："+lotName+" productName: "+productName+"</pre>");
			info.append("<pre>	processOperationName："+processOperationName+"</pre>");
			info.append("<pre>	machineName："+machineName+"</pre>");
			info.append("<pre>	itemName："+itemName+"</pre>");
//			info.append("<pre>	otherInformation："+otherInformation+"</pre>");		
			info.append("<pre>=============================End=============================</pre>");			
			
			String message = info.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userGroup, title, detailtitle, message, url);
			//log.info("eMobile Send Success!");	
			
			StringBuffer weChatInfo = new StringBuffer();
			weChatInfo.append("<pre>======AlarmInformation======</pre>");
			weChatInfo.append("<pre>url："+url+"</pre>");
			weChatInfo.append("<pre>   spcModule："+spcModule+"</pre>");
			weChatInfo.append("<pre>	productSpecName："+productSpecName+"</pre>");
			weChatInfo.append("<pre>		lotName："+lotName+"</pre>");
			weChatInfo.append("<pre>	productName："+productName+"</pre>");
			weChatInfo.append("<pre>	processOperationName："+processOperationName+"</pre>");
			weChatInfo.append("<pre> machineName："+machineName+"</pre>");
			weChatInfo.append("<pre>	itemName："+itemName+"</pre>");
			weChatInfo.append("<pre>	=======AlarmInfoEnd========</pre>");
			
			String weChatMessage = weChatInfo.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userGroup, title, detailtitle, weChatMessage, "");
			//log.info("WeChat Send Success!");	
		}
		catch (Exception e)
		{
			log.info("eMobile or WeChat Send Error : " + e.getCause());	
		}	
		
		try
		{
			// find EQPPhoneList
			List<String> phoneList = getEQPPhoneList(userList);
			
			// execute smsMessage
			StringBuffer smsMessageInfo = new StringBuffer();
			smsMessageInfo.append("url：\n"+url);
			smsMessageInfo.append("\n spcModule：\n"+spcModule);
			smsMessageInfo.append("\n productSpecName：\n"+productSpecName);
			smsMessageInfo.append("\n lotName：\n"+lotName);
			smsMessageInfo.append("\n productName：\n"+productName);
			smsMessageInfo.append("\n processOperationName：\n"+processOperationName);
			smsMessageInfo.append("\n machineName：\n"+machineName);
			smsMessageInfo.append("\n itemName：\n"+itemName);
			
			// sendMessage
			GenericServiceProxy.getSMSInterface().sendMessage(smsMessageInfo,phoneList,machineName,theme);
		}
		catch(Exception e)
		{
			log.info("PhoneMail Send Error : " + e.getCause());	
		}
				
	}
	
	private List<String> getEQPPhoneList(String userInfo) 
	{
		List<String> phoneList = new ArrayList<String>();
		try
		{		
			List<String> userList =  CommonUtil.splitStringDistinct(";",userInfo);
			StringBuffer sql1 = new StringBuffer();
			sql1.append(
					" SELECT B.PHONENUMBER FROM  CT_ALARMUSER B WHERE B.USERID = :USERID ");
			Map<String, Object> args1 = new HashMap<String, Object>();

			for (String user : userList) 
			{
				args1.put("USERID", user);
				List<Map<String, Object>> sqlResult1 = GenericServiceProxy.getSqlMesTemplate()
						.queryForList(sql1.toString(), args1);
				
				if (sqlResult1 != null && sqlResult1.size() > 0) 
				{
					String phonenumber = ConvertUtil.getMapValueByName(sqlResult1.get(0), "PHONENUMBER");
					phoneList.add(phonenumber);
				}
			}
		}
		catch (Exception e)
		{
			log.info("Not Found the PhoneNumber of "+ userInfo);
		}											
		return phoneList;
	}		
	
}
