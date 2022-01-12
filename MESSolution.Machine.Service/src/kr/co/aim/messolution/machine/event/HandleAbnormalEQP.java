package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AbnormalEQP;
import kr.co.aim.messolution.extended.object.management.data.AbnormalEQPCommand;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class HandleAbnormalEQP extends SyncHandler{
	private static Log log = LogFactory.getLog(HandleAbnormalEQP.class);
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String rabnormalName = SMessageUtil.getBodyItemValue(doc, "ABNORMALNAME", true);
		String handleType = SMessageUtil.getBodyItemValue(doc, "HANDLETYPE", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
        
		AbnormalEQP dtAbnormal = ExtendedObjectProxy.getAbnormalEQPService().selectByKey(false, new String[]{rabnormalName});
		
		if(!(dtAbnormal.getAbnormalState().contains("Created") || dtAbnormal.getAbnormalState().contains("Handling")))
		{
			//CUSTOM-0028: AbnormalState Is Not Created Or Handling!
			throw new CustomException("CUSTOM-0028");
		}
		
		if (handleType.equals("Transfer"))
		{
			String command = SMessageUtil.getBodyItemValue(doc, "TRANSFERREASON", true);
			String department = SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true);
			
			eventInfo.setEventName("TransferAbnormal");
			
			Timestamp tCommandTime = TimeUtils.getCurrentTimestamp();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date;
			
			try {
				date = sdf.parse(TimeStampUtil.getCurrentTimestamp().toString());
				tCommandTime = new Timestamp(date.getTime());
			} catch (ParseException e) {
				//CUSTOM-0029: error commandtime!
				throw new CustomException("CUSTOM-0029");
			}
			
			AbnormalEQPCommand newCommand = new AbnormalEQPCommand();				
			
			newCommand.setAbnormalEQPName(dtAbnormal.getAbnormalEQPName());
			newCommand.setAbnormalName(rabnormalName);
			newCommand.setCommand(command);
			newCommand.setCommandReply("");
			newCommand.setCommandReplyTime(null);
			newCommand.setCommandTime(tCommandTime);
			newCommand.setDepartment(department);
			newCommand.setLastEventComment(eventInfo.getEventComment());
			newCommand.setLastEventName(eventInfo.getEventName());
			newCommand.setLastEventTime(eventInfo.getEventTime());
			newCommand.setLastEventTimeKey(eventInfo.getEventTimeKey());
			newCommand.setLastEventUser(eventInfo.getEventUser());
			newCommand = ExtendedObjectProxy.getAbnormalEQPCommandService().create(eventInfo, newCommand);
			
			dtAbnormal.setDepartment(department);
			dtAbnormal.setAbnormalState("Handling");
			dtAbnormal.setReasonCode("");
			dtAbnormal.setCauseEQPName("");
			dtAbnormal.setHandler("");
			dtAbnormal.setProcess("");
			dtAbnormal.setReason("");
			dtAbnormal.setSendFlag("");
			dtAbnormal.setPlanFinishTime(null);
			dtAbnormal.setLastEventComment(eventInfo.getEventComment());
			dtAbnormal.setLastEventName(eventInfo.getEventName());
			dtAbnormal.setLastEventTime(eventInfo.getEventTime());
			dtAbnormal.setLastEventTimeKey(eventInfo.getEventTimeKey());
			dtAbnormal.setLastEventUser(eventInfo.getEventUser());
			dtAbnormal = ExtendedObjectProxy.getAbnormalEQPService().modify(eventInfo, dtAbnormal);
			
			String userList = getUserList(department);
			String createUser = dtAbnormal.getCreateUser();
				
		    try
		    {															
				String[] userGroup = userList.split(",");				
				String title = "异常单转单通知";
				String detailtitle = "${}CIM系统消息通知";
				
				StringBuffer info = new StringBuffer();
				info.append("<pre>=======================NoticeInformation=======================</pre>");
				info.append("<pre>	abnormal："+rabnormalName+"</pre>");
				info.append("<pre>	abnormalType："+dtAbnormal.getAbnormalType()+"</pre>");
				info.append("<pre>	createUser："+createUser+"</pre>");
				info.append("<pre>	department："+department+"</pre>");
				info.append("<pre>	startTime："+TimeUtils.toTimeString(dtAbnormal.getStartTime())+"</pre>");
				info.append("<pre>	description："+dtAbnormal.getDescription()+"</pre>");
				info.append("<pre>=============================End=============================</pre>");				
				
				String message = info.toString();
				
				ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userGroup, title, detailtitle, message, "");
				//log.info("eMobile Send Success!");	
				
				StringBuffer weChatInfo = new StringBuffer();
				weChatInfo.append("<pre>======NoticeInformation======</pre>");			
				weChatInfo.append("<pre>	abnormal："+rabnormalName+"</pre>");
				weChatInfo.append("<pre> abnormalType："+dtAbnormal.getAbnormalType()+"</pre>");
				weChatInfo.append("<pre>	createUser："+createUser+"</pre>");
				weChatInfo.append("<pre>	department："+department+"</pre>");
				weChatInfo.append("<pre>	         startTime："+TimeUtils.toTimeString(dtAbnormal.getStartTime())+"</pre>");		
				weChatInfo.append("<pre>	description："+dtAbnormal.getDescription()+"</pre>");
				weChatInfo.append("<pre>	=======NoticeInfoEnd========</pre>");
				
				String weChatMessage = weChatInfo.toString();
				
				ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userGroup, title, detailtitle, weChatMessage, "");
				//log.info("WeChat Send Success!");	
		    }
			catch (Exception e)
			{
				log.info("eMobile or WeChat or email Send Error : " + e.getCause());	
			}
		}
		else if (handleType.equals("Handle"))
		{
			String causeEQP = SMessageUtil.getBodyItemValue(doc, "CAUSEEQP", false);
			String handler = SMessageUtil.getBodyItemValue(doc, "HANDLER", true);
			String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);
			String planFinishTime = SMessageUtil.getBodyItemValue(doc, "PLANFINISHTIME", true);
			String reason = SMessageUtil.getBodyItemValue(doc, "REASON", true);
			String process = SMessageUtil.getBodyItemValue(doc, "PROCESS", true);
			String abnormalLevel = SMessageUtil.getBodyItemValue(doc, "ABNORMALLEVEL", true);
			
			eventInfo.setEventName("HandleAbnormal");
			
			dtAbnormal.setAbnormalState("Handling");
			dtAbnormal.setReasonCode(reasonCode);
			dtAbnormal.setAbnormalLevel(abnormalLevel);
			dtAbnormal.setCauseEQPName(causeEQP);
			dtAbnormal.setHandler(handler);
			dtAbnormal.setProcess(process);
			dtAbnormal.setReason(reason);
			dtAbnormal.setSendFlag("");
			dtAbnormal.setPlanFinishTime(TimeUtils.getTimestamp(planFinishTime));
			dtAbnormal.setLastEventName(eventInfo.getEventName());
			dtAbnormal.setLastEventComment(eventInfo.getEventComment());
			dtAbnormal.setLastEventTimeKey(eventInfo.getEventTimeKey());
			dtAbnormal.setLastEventTime(eventInfo.getEventTime());
			dtAbnormal.setLastEventUser(eventInfo.getEventUser());
			dtAbnormal = ExtendedObjectProxy.getAbnormalEQPService().modify(eventInfo, dtAbnormal);
			
			if(!abnormalLevel.equals("A"))
			{
				String userList = getUserList("品保");
				String createUser = dtAbnormal.getCreateUser();
				
				try
			    {															
					String[] userGroup = userList.split(",");				
					String title = "判定B,L等级异常单通知";
					String detailtitle = "${}CIM系统消息通知";
					
					StringBuffer info = new StringBuffer();
					info.append("<pre>=======================NoticeInformation=======================</pre>");
					info.append("<pre>	abnormal："+rabnormalName+"</pre>");
					info.append("<pre>	abnormalType："+dtAbnormal.getAbnormalType()+"</pre>");
					info.append("<pre>	createUser："+createUser+"</pre>");
					info.append("<pre>	department："+dtAbnormal.getDepartment()+"</pre>");
					info.append("<pre>	startTime："+TimeUtils.toTimeString(dtAbnormal.getStartTime())+"</pre>");
					info.append("<pre>	description："+dtAbnormal.getDescription()+"</pre>");
					info.append("<pre>=============================End=============================</pre>");				
					
					String message = info.toString();
					
					ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userGroup, title, detailtitle, message, "");
					//log.info("eMobile Send Success!");	
					
					StringBuffer weChatInfo = new StringBuffer();
					weChatInfo.append("<pre>======NoticeInformation======</pre>");			
					weChatInfo.append("<pre>	abnormal："+rabnormalName+"</pre>");
					weChatInfo.append("<pre> abnormalType："+dtAbnormal.getAbnormalType()+"</pre>");
					weChatInfo.append("<pre>	createUser："+createUser+"</pre>");
					weChatInfo.append("<pre>	department："+dtAbnormal.getDepartment()+"</pre>");
					weChatInfo.append("<pre>	         startTime："+TimeUtils.toTimeString(dtAbnormal.getStartTime())+"</pre>");		
					weChatInfo.append("<pre>	description："+dtAbnormal.getDescription()+"</pre>");
					weChatInfo.append("<pre>	=======NoticeInfoEnd========</pre>");
					
					String weChatMessage = weChatInfo.toString();
					
					ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userGroup, title, detailtitle, weChatMessage, "");
					//log.info("WeChat Send Success!");	
			    }
				catch (Exception e)
				{
					log.info("eMobile or WeChat or email Send Error : " + e.getCause());	
				}
			}
			
			if(dtAbnormal.getAbnormalType().equals("SPC"))
			{
				String userList = getUserList("品保");
				String createUser = dtAbnormal.getCreateUser();
					
			    try
			    {															
					String[] userGroup = userList.split(",");				
					String title = "SPC异常单可审核通知";
					String detailtitle = "${}CIM系统消息通知";
					
					StringBuffer info = new StringBuffer();
					info.append("<pre>=======================NoticeInformation=======================</pre>");
					info.append("<pre>	abnormal："+rabnormalName+"</pre>");
					info.append("<pre>	abnormalType："+dtAbnormal.getAbnormalType()+"</pre>");
					info.append("<pre>	createUser："+createUser+"</pre>");
					info.append("<pre>	department："+dtAbnormal.getDepartment()+"</pre>");
					info.append("<pre>	startTime："+TimeUtils.toTimeString(dtAbnormal.getStartTime())+"</pre>");
					info.append("<pre>	description："+dtAbnormal.getDescription()+"</pre>");
					info.append("<pre>=============================End=============================</pre>");				
					
					String message = info.toString();
					
					ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userGroup, title, detailtitle, message, "");
					//log.info("eMobile Send Success!");	
					
					StringBuffer weChatInfo = new StringBuffer();
					weChatInfo.append("<pre>======NoticeInformation======</pre>");			
					weChatInfo.append("<pre>	abnormal："+rabnormalName+"</pre>");
					weChatInfo.append("<pre> abnormalType："+dtAbnormal.getAbnormalType()+"</pre>");
					weChatInfo.append("<pre>	createUser："+createUser+"</pre>");
					weChatInfo.append("<pre>	department："+dtAbnormal.getDepartment()+"</pre>");
					weChatInfo.append("<pre>	         startTime："+TimeUtils.toTimeString(dtAbnormal.getStartTime())+"</pre>");		
					weChatInfo.append("<pre>	description："+dtAbnormal.getDescription()+"</pre>");
					weChatInfo.append("<pre>	=======NoticeInfoEnd========</pre>");
					
					String weChatMessage = weChatInfo.toString();
					
					ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userGroup, title, detailtitle, weChatMessage, "");
					//log.info("WeChat Send Success!");	
			    }
				catch (Exception e)
				{
					log.info("eMobile or WeChat or email Send Error : " + e.getCause());	
				}
			}
		}
		
		return doc;
	}
	
	private String getUserList(String department)
	{
		String userList = new String();
		try 
		{
			StringBuilder sb = new StringBuilder();
			List<String> departmentList =  new ArrayList<String>();
			departmentList.add(department);
			
			StringBuffer sql1 = new StringBuffer();
			sql1.append("SELECT * FROM CT_ALARMUSERGROUP  WHERE ALARMGROUPNAME = 'AbnormalEQP' AND DEPARTMENT =:DEPARTMENT AND USERLEVEL='1'");
			Map<String, Object> args1 = new HashMap<String, Object>();

			//for (String department1 : department) 
			for(int j = 0; j < departmentList.size(); j++)
			{
				args1.put("DEPARTMENT", departmentList.get(j));
				List<Map<String, Object>> sqlResult1 = GenericServiceProxy.getSqlMesTemplate()
						.queryForList(sql1.toString(), args1);
				
				if (sqlResult1.size() > 0) 
				{
					if(j < departmentList.size() - 1)
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
		catch (Exception e)
		{
			log.info(" Failed to send to EMobile");
		}
		
		return userList;
	}
}
