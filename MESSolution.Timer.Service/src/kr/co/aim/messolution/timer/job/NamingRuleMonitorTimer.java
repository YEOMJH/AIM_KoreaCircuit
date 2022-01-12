package kr.co.aim.messolution.timer.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EMailInterface;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

public class NamingRuleMonitorTimer implements Job, InitializingBean
{	
	private static Log log = LogFactory.getLog(NamingRuleMonitorTimer.class);
	
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}
	
	
	public void execute(JobExecutionContext arg0) throws JobExecutionException 
	{	
		try
		{
			// Search Product In CT_ComponentMonitor
			List<Map<String, Object>> dataInfo = this.GetDataList();	
			
			if (dataInfo.size() > 0)
			{
				// execute message
				StringBuffer messageInfo = executeMessage(dataInfo);

				//List<String> emailList = getEmailList();

				//sendEmail(emailList,messageInfo);

				String userList = getUserList();	
						
				//SendToEM
				sendToEM(userList,messageInfo);
			}
		}
		catch (Exception e)
		{
			if (log.isDebugEnabled())
				log.info(e.getCause());
		}
	}

	
	// sendEmail
	private void sendEmail(List<String> emailList, StringBuffer messageInfo) 
	{
		if(emailList !=null && emailList.size()>0)
		{
			StringBuffer message = new StringBuffer();
			message.append("<pre>=============== Naming Rule Over Used ===============</pre>");
			message.append(messageInfo);
		 
			try
			{
				EMailInterface ei = new EMailInterface("mail.visionox.com", "25", "1000", "1000");
				ei.postMail(emailList,  " Naming Rule Over Used ", message.toString(), "V3MES", "V3MES@visionox.com", "V3MES", "vis@2019");
			}
			catch (Exception e)
			{
				log.info(" Failed to send mail. MachineName: ");
			}
		}
		
	}


	// find EmailList
	private List<String> getEmailList() 
	{
		List<Map<String,Object>> resultList = null;
		List<String> emailList = new ArrayList<String>();
		
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT A.* FROM USERPROFILE U,CT_ALARMUSER A WHERE U.DEPARTMENT = 'CIM' AND U.USERID = A.USERID ");
		
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString());
		}
		catch(Exception ex)
		{
		    log.info(ex.getCause());
		}
		
		try 
		{
			if (resultList.size() > 0) 
			{
				for (Map<String, Object> user : resultList) 
				{
					String eMail = ConvertUtil.getMapValueByName(user, "EMAIL");
					emailList.add(eMail);
				}
			}
		}
		catch (Exception e)
		{
			log.info(" Failed to send mail. Email List is null");
		}
		
		return emailList;
	}


	// execute message
	private StringBuffer executeMessage(List<Map<String, Object>> dataInfo) 
	{
		StringBuffer sqlBuffer = new StringBuffer();
		
		for (Map<String, Object> dataInfos : dataInfo) 
		{
			sqlBuffer.append(" <pre> RuleName: " + ConvertUtil.getMapValueByName(dataInfos, "RULENAME") + "  PREFIX: "
					+ ConvertUtil.getMapValueByName(dataInfos, "PREFIX") + "  LASTSERIALNO: "
					+ ConvertUtil.getMapValueByName(dataInfos, "LASTSERIALNO") + "</pre> ");
		}
		return sqlBuffer;
	}

	// Search Product In CT_ComponentMonitor
	public List<Map<String, Object>> GetDataList()
	{
		StringBuffer sqlBuffer = new StringBuffer();
		
		sqlBuffer.append(" SELECT * "
				+ "   FROM NAMEGENERATORSERIAL A "
				+ "  WHERE      SUBSTR (A.LASTSERIALNO, 2, 1) > 'W' "
				+ "        AND (SUBSTR (A.LASTSERIALNO, 1, 1) = 'Z' OR A.PREFIX = 'F3MA14E') "
				+ "        AND A.RULENAME IN ('ProductionLotNaming','LotNaming') ");
		
		List<Map<String, Object>> dataInfo;

		try 
		{
			dataInfo = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString());
		} 
		catch(Exception ex)
		{
			dataInfo = null;
			log.info(ex.getCause());
		}
		
		return dataInfo;
	}
	
	//Start houxk 20210324
	//get UserList
	private String getUserList()
	{
		List<Map<String,Object>> resultList = null;
		List<String> emailList = new ArrayList<String>();
		String userList = new String();
		StringBuilder sb = new StringBuilder();
		
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT A.* FROM USERPROFILE U,CT_ALARMUSER A WHERE U.DEPARTMENT = 'CIM' AND U.USERID = A.USERID ");
		
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString());
		}
		catch(Exception ex)
		{
		    log.info(ex.getCause());
		}
		
		try 
		{
			if (resultList.size() > 0) 
			{
						for (Map<String, Object> userRow : resultList)
						{
							String user = ConvertUtil.getMapValueByName(userRow, "USERID");
							sb.append(user + ","); 
						}
			}
			userList = sb.toString();
		}
		catch (Exception e)
		{
			userList = "";
			log.info(" Failed to send to EMobile");
		}
		return userList;
	}
	
	//sendToEM & WeChat
	private void sendToEM(String userList, StringBuffer messageInfo) 
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ComponentMonitorTimer", "MES", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{																
			String[] userGroup = userList.split(",");				
			String title = " Naming Rule Over Used ";
			String detailtitle = "${}CIM系统消息通知";
			String url = "";
			
			StringBuffer info = new StringBuffer();
			info.append("<pre>======================= Naming Rule Over Used =======================</pre>");
			info.append(messageInfo);
			info.append("<pre>=============================End=============================</pre>");			
			
			String message = info.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userGroup, title, detailtitle, message, url);
			//log.info("eMobile Send Success!");	
			
			StringBuffer weChatInfo = new StringBuffer();
			weChatInfo.append("<pre>==== Naming Rule Over Used ====</pre>");
			weChatInfo.append(messageInfo);
			weChatInfo.append("<pre>====AlarmInfoEnd====</pre>");
			
			String weChatMessage = weChatInfo.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userGroup, title, detailtitle, weChatMessage, "");
			//log.info("WeChat Send Success!");	
		}
		catch (Exception e)
		{
			log.info("eMobile or WeChat Send Error : " + e.getCause());	
		}
	}
}
