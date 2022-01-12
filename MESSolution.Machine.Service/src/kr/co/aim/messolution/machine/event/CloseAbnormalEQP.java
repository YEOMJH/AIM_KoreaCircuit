package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AbnormalEQP;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.messolution.generic.util.EMailInterface;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class CloseAbnormalEQP extends SyncHandler { 
	private static Log log = LogFactory.getLog(CloseAbnormalEQP.class);
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String abnormalName = SMessageUtil.getBodyItemValue(doc, "ABNORMALNAME", true);
		String realFinishTime = SMessageUtil.getBodyItemValue(doc, "REALFINISHTIME", true);
		String abnormalState = "Closed";
		
		Timestamp trealFinishTime = TimeUtils.getTimestamp(realFinishTime);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CloseAbnormal", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		
		AbnormalEQP abnormalData = ExtendedObjectProxy.getAbnormalEQPService().selectByKey(false, new String[]{abnormalName});
		
		if(!abnormalData.getAbnormalState().contains("Handling"))
		{
			log.info("abnormalState is not Handling");
			
			//CUSTOM-0026:abnormalState is not Handling
			throw new CustomException("CUSTOM-0026");
		}
		
		abnormalData.setAbnormalState(abnormalState);
		abnormalData.setRealFinishTime(trealFinishTime);
		abnormalData.setLastEventComment(eventInfo.getEventComment());
		abnormalData.setLastEventName(eventInfo.getEventName());
		abnormalData.setLastEventTime(eventInfo.getEventTime());
		abnormalData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		abnormalData.setLastEventUser(eventInfo.getEventUser());
		
		abnormalData = ExtendedObjectProxy.getAbnormalEQPService().modify(eventInfo, abnormalData);
		
		Timestamp tstartTime = abnormalData.getStartTime();
		String abnormaltype=abnormalData.getAbnormalType();
		if(abnormaltype.equals("EQP"))
		{
		   int hours = (int)((trealFinishTime.getTime() - tstartTime.getTime())/(1000*60*60));
		   if (hours >= 2)
		   {
			//start send message
			String rabnormalName =abnormalData.getAbnormalName();
			String userList = getUser3List(abnormalData.getDepartment());
			String handler = abnormalData.getHandler();
			//List<String> emailList = getEmailList(rabnormalName);
			
			if(StringUtil.isNotEmpty(userList))
			{
				try
				{																
					String[] userGroup = userList.split(",");				
					String title = "异常单关单通知";
					String detailtitle = "${}CIM系统消息通知";
					
					StringBuffer info = new StringBuffer();
					info.append("<pre>=======================NoticeInformation=======================</pre>");
					info.append("<pre>	abnormal："+rabnormalName+"</pre>");
					info.append("<pre>	abnormalEQPName："+abnormalData.getAbnormalEQPName()+"</pre>");
					info.append("<pre>	handler："+handler+"</pre>");
					info.append("<pre>	department："+abnormalData.getDepartment()+"</pre>");
					info.append("<pre>	realFinishTime - startTime > 2Hour： send to Manager and Leader"+"</pre>");
					info.append("<pre>	reason："+abnormalData.getReason()+"</pre>");
					info.append("<pre>=============================End=============================</pre>");				
					
					String message = info.toString();
					
					ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userGroup, title, detailtitle, message, "");
					//log.info("eMobile Send Success!");	
					
					StringBuffer weChatInfo = new StringBuffer();
					weChatInfo.append("<pre>======NoticeInformation======</pre>");			
					weChatInfo.append("<pre>	abnormal："+rabnormalName+"</pre>");
					weChatInfo.append("<pre> abnormalEQPName："+abnormalData.getAbnormalEQPName()+"</pre>");
					weChatInfo.append("<pre>	handler："+handler+"</pre>");
					weChatInfo.append("<pre>	department："+abnormalData.getDepartment()+"</pre>");
					weChatInfo.append("<pre>	realFinishTime - startTime > 2Hour： send to Manager and Leader" + "</pre>");
					weChatInfo.append("<pre>	reason："+abnormalData.getReason()+"</pre>");			
					weChatInfo.append("<pre>	=======NoticeInfoEnd========</pre>");
					
					String weChatMessage = weChatInfo.toString();
					
					ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userGroup, title, detailtitle, weChatMessage, "");
					//log.info("WeChat Send Success!");	
					
					
					StringBuffer EmailInfo = new StringBuffer();
					EmailInfo.append("<pre>======NoticeInformation======</pre>");		
					EmailInfo.append("<pre>	abnormal："+rabnormalName+"</pre>");
					EmailInfo.append("<pre>	abnormalEQPName："+abnormalData.getAbnormalEQPName()+"</pre>");
					EmailInfo.append("<pre>	handler："+handler+"</pre>");
					EmailInfo.append("<pre>	department："+abnormalData.getDepartment()+"</pre>");
					EmailInfo.append("<pre>	realFinishTime - startTime > 2Hour： send to Manager and Leader" + "</pre>");
					EmailInfo.append("<pre>	reason："+abnormalData.getReason()+"</pre>");			
					EmailInfo.append("<pre>	=======NoticeInfoEnd========</pre>");
					//EMailInterface ei = new EMailInterface("mail.visionox.com", "25", "1000", "1000");
					//ei.postMail(emailList,  "异常单创建通知", message.toString(), "V3MES", "V3MES@visionox.com", "V3MES", "vis@2019");
				}
				catch (Exception e)
				{
					log.info("eMobile or WeChat or email Send Error : " + e.getCause());	
				}	
			}
			else
			{
				log.info("userList is null,so don't send message");
			}
		}
		else if(hours >= 1)
		{
			//start send message
			String rabnormalName =abnormalData.getAbnormalName();
			String userList = getUser2List(abnormalData.getDepartment());
			String handler = abnormalData.getHandler();
			//List<String> emailList = getEmailList(rabnormalName);
			
			if(StringUtil.isNotEmpty(userList))
			{
				try
				{																
					String[] userGroup = userList.split(",");				
					String title = "异常单关单通知";
					String detailtitle = "${}CIM系统消息通知";
					
					StringBuffer info = new StringBuffer();
					info.append("<pre>=======================NoticeInformation=======================</pre>");
					info.append("<pre>	abnormal："+rabnormalName+"</pre>");
					info.append("<pre>	abnormalEQPName："+abnormalData.getAbnormalEQPName()+"</pre>");
					info.append("<pre>	handler："+handler+"</pre>");
					info.append("<pre>	department："+abnormalData.getDepartment()+"</pre>");
					info.append("<pre>	realFinishTime - startTime > 1Hour： send to Leader"+"</pre>");
					info.append("<pre>	reason："+abnormalData.getReason()+"</pre>");
					info.append("<pre>=============================End=============================</pre>");				
					
					String message = info.toString();
					
					ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userGroup, title, detailtitle, message, "");
					//log.info("eMobile Send Success!");	
					
					StringBuffer weChatInfo = new StringBuffer();
					weChatInfo.append("<pre>======NoticeInformation======</pre>");			
					weChatInfo.append("<pre>	abnormal："+rabnormalName+"</pre>");
					weChatInfo.append("<pre> abnormalEQPName："+abnormalData.getAbnormalEQPName()+"</pre>");
					weChatInfo.append("<pre>	handler："+handler+"</pre>");
					weChatInfo.append("<pre>	department："+abnormalData.getDepartment()+"</pre>");
					weChatInfo.append("<pre>	realFinishTime - startTime > 1Hour： send to Leader" + "</pre>");
					weChatInfo.append("<pre>	reason："+abnormalData.getReason()+"</pre>");			
					weChatInfo.append("<pre>	=======NoticeInfoEnd========</pre>");
					
					String weChatMessage = weChatInfo.toString();
					
					ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userGroup, title, detailtitle, weChatMessage, "");
					//log.info("WeChat Send Success!");	
					
					
					StringBuffer EmailInfo = new StringBuffer();
					EmailInfo.append("<pre>======NoticeInformation======</pre>");		
					EmailInfo.append("<pre>	abnormal："+rabnormalName+"</pre>");
					EmailInfo.append("<pre>	abnormalEQPName："+abnormalData.getAbnormalEQPName()+"</pre>");
					EmailInfo.append("<pre>	handler："+handler+"</pre>");
					EmailInfo.append("<pre>	department："+abnormalData.getDepartment()+"</pre>");
					EmailInfo.append("<pre>	realFinishTime - startTime > 1Hour： send to Leader" + "</pre>");
					EmailInfo.append("<pre>	reason："+abnormalData.getReason()+"</pre>");			
					EmailInfo.append("<pre>	=======NoticeInfoEnd========</pre>");
					//EMailInterface ei = new EMailInterface("mail.visionox.com", "25", "1000", "1000");
					//ei.postMail(emailList,  "异常单创建通知", message.toString(), "V3MES", "V3MES@visionox.com", "V3MES", "vis@2019");
				}
				catch (Exception e)
				{
					log.info("eMobile or WeChat or email Send Error : " + e.getCause());	
				}	
			}
			else
			{
				log.info("userList is null,so don't send message");
			}
		}
	}
		return doc;
	}
	
	private String getUser2List(String department)
	{
		List<Map<String,Object>> resultList = null;
		String userList = new String();
		StringBuilder sb = new StringBuilder();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT A.USERID FROM  (SELECT DISTINCT B.RANGE FROM CT_ALARMUSERGROUP B WHERE B.ALARMGROUPNAME = 'AbnormalEQP' AND B.DEPARTMENT = :DEPARTMENT)C, CT_ALARMUSERGROUP A WHERE C.RANGE = A.RANGE AND A.USERLEVEL = '2' ");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("DEPARTMENT", department);
	
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
			
			if (resultList.size() > 0) 
			{
				for (int i = 0; i < resultList.size(); i++) 
				{  
					String user = ConvertUtil.getMapValueByName(resultList.get(i), "USERID");
					sb.append(user + ",");  
	            }
				sb.substring(0,sb.length()-1);
			}
			userList = sb.toString();
		}
		catch(Exception ex)
		{
		    log.info(ex.getCause());
		}
		
		return userList;
	}
	
	private String getUser3List(String department)
	{
		List<Map<String,Object>> resultList = null;
		String userList = new String();
		StringBuilder sb = new StringBuilder();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT DISTINCT A.USERID FROM  (SELECT DISTINCT B.RANGE FROM CT_ALARMUSERGROUP B WHERE B.ALARMGROUPNAME = 'AbnormalEQP' AND B.DEPARTMENT = :DEPARTMENT)C, CT_ALARMUSERGROUP A WHERE C.RANGE = A.RANGE AND A.USERLEVEL IN ('2','3')");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("DEPARTMENT", department);
	
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
			
			if (resultList.size() > 0) 
			{
				for (int i = 0; i < resultList.size(); i++) 
				{  
					String user = ConvertUtil.getMapValueByName(resultList.get(i), "USERID");
					sb.append(user + ",");  
	            }
			}
			
			
			if (department.startsWith("T-图形")) 
			{
				sb.append("V0032401" + ",");
			}
			else if (department.startsWith("T-真空")) 
			{
				sb.append("V0032401" + ",");
			}

			userList = sb.toString();
		}
		catch(Exception ex)
		{
		    log.info(ex.getCause());
		}
		
		return userList;
	}
}