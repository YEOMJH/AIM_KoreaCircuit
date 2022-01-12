package kr.co.aim.messolution.extended.object.management.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.data.AMSMessage;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class AMSMessageService extends CTORMService<AMSMessage> {
	
	public static Log logger = LogFactory.getLog(AMSMessageService.class);
	
	private String queryString = "INSERT INTO CT_ERRORMESSAGELOG "
			+ "		(alarmID, alarmTypeName, keyValue, emailSendLog, emsSendLog, weChatSendLog, fmbSendLog, smsSendLog,"
			+ "		messageLog, sendUserList, lastEventTimeKey, lastEventName, lastEventUser, lastEventComment, lastEventTime) "
			+ " 	VALUES (:alarmID, :alarmTypeName, :keyValue, :emailSendLog, :emsSendLog, :weChatSendLog, :fmbSendLog, :smsSendLog,"
			+ "		:messageLog, :sendUserList, :lastEventTimeKey, :lastEventName, :lastEventUser, :lastEventComment, :lastEventTime) ";
	
	public List<AMSMessage> select(String condition, Object[] bindSet)
		throws CustomException
	{
		List<AMSMessage> result = super.select(condition, bindSet, AMSMessage.class);
		
		return result;
	}
	
	public AMSMessage selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		return super.selectByKey(AMSMessage.class, isLock, keySet);
	}
	
	public void create(EventInfo eventInfo, AMSMessage dataInfo)
		throws CustomException
	{
		try 
		{
			Map<String, Object> bindMap = new HashMap<String, Object>();
			
			bindMap.put("alarmID", dataInfo.getAlarmID());
			bindMap.put("alarmTypeName", dataInfo.getAlarmTypeName());
			bindMap.put("keyValue", dataInfo.getKeyValue());
			bindMap.put("emailSendLog", dataInfo.getEmailSendLog());
			bindMap.put("emsSendLog", dataInfo.getEmsSendLog());
			bindMap.put("weChatSendLog", dataInfo.getWeChatSendLog());
			bindMap.put("fmbSendLog", dataInfo.getFmbSendLog());
			bindMap.put("smsSendLog", dataInfo.getSmsSendLog());
			bindMap.put("messageLog", dataInfo.getSmsSendLog());
			bindMap.put("sendUserList", dataInfo.getSendUserList());
			bindMap.put("lastEventTimeKey", eventInfo.getEventTimeKey());
			bindMap.put("lastEventName", eventInfo.getEventName());
			bindMap.put("lastEventUser", eventInfo.getEventUser());
			bindMap.put("lastEventComment", eventInfo.getEventComment());
			bindMap.put("lastEventTime", eventInfo.getEventTime());
			
			if (!bindMap.isEmpty()) 
			{
				GenericServiceProxy.getSqlMesTemplate().update(queryString, bindMap);
			}
		} 
		catch (Exception e) 
		{
			logger.info(e);
		}
	}
	
	/*public void remove(EventInfo eventInfo, AMSMessage dataInfo)
		throws CustomException
	{
		super.delete(dataInfo);
	}
	
	public AMSMessage modify(EventInfo eventInfo, AMSMessage dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} */
}
