package kr.co.aim.messolution.timer.job;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EMailInterface;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlarmDefinition;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;

public class TransportJobTimer implements Job, InitializingBean {
private static Log log = LogFactory.getLog(TransportJobTimer.class);
	
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}
	public void execute(JobExecutionContext arg0) throws JobExecutionException
	{
		try
		{
			// Monitor
			log.debug("monitorTransportJobTimer-Start");
			monitorTransportJobTimer();
		}
		catch (CustomException e)
		{
			if (log.isDebugEnabled())
				log.error(e.errorDef.getLoc_errorMessage());
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.debug("monitorTransportJobTimer-End");
	}
	
	public void monitorTransportJobTimer()throws CustomException
	{
		StringBuilder sql = new StringBuilder();
		sql.append(" WITH ST AS (SELECT H.EVENTTIME,H.TRANSPORTJOBNAME FROM CT_TRANSPORTJOBCOMMANDHIST H ");
		sql.append("  WHERE H.JOBSTATE='Requested' AND H.EVENTTIME>(SYSDATE - 3 / 24 ) ) ");
		sql.append(" select T.TRANSPORTJOBNAME,T.TRANSPORTJOBTYPE,T.JOBSTATE,T.CARRIERNAME,T.CURRENTMACHINENAME, ");
		sql.append(" T.SOURCEMACHINENAME,T.DESTINATIONMACHINENAME,T.LASTEVENTTIME,T.SENDEMAILFLAG,T.TRANSFERSTATE, ");
	    sql.append(" H.EVENTUSER,H.EVENTRESULTTEXT,ST.EVENTTIME,D.FACTORYNAME,D.DURABLETYPE,D.DURABLESPECNAME ");
	    sql.append(" from CT_TRANSPORTJOBCOMMAND T,CT_TRANSPORTJOBCOMMANDHIST H,ST,DURABLE D  ");
	    sql.append(" WHERE T.JOBSTATE='Terminated' ");
	    sql.append(" AND   T.JOBSTATE=H.JOBSTATE");
	    sql.append(" AND   T.CARRIERNAME=D.DURABLENAME");
	    sql.append(" AND   T.TRANSPORTJOBNAME=H.TRANSPORTJOBNAME ");
	    sql.append(" AND   T.TRANSPORTJOBNAME=ST.TRANSPORTJOBNAME ");
	    sql.append(" AND   T.CARRIERNAME=H.CARRIERNAME ");
	    sql.append(" AND   T.LASTEVENTTIMEKEY =H.TIMEKEY ");
	    sql.append(" AND   T.TRANSPORTJOBNAME NOT like'%C20%' " );
	    sql.append(" AND   T.DESTINATIONMACHINENAME<>T.CURRENTMACHINENAME ");
	    sql.append(" AND   T.SOURCEMACHINENAME<>T.CURRENTMACHINENAME ");
	    sql.append(" AND   T.LASTEVENTTIME>(SYSDATE - 2 / 24 ) ");
	    sql.append(" ORDER BY TRANSPORTJOBNAME  DESC ");
        List<Map<String, Object>> sqlResult1= GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString());
		
        sql = new StringBuilder();
    	sql.append(" WITH ST AS (SELECT H.EVENTTIME,H.TRANSPORTJOBNAME FROM CT_TRANSPORTJOBCOMMANDHIST H ");
		sql.append("  WHERE H.JOBSTATE='Requested' AND H.EVENTTIME>(SYSDATE - 3 / 24 ) ) ");
		sql.append(" select T.TRANSPORTJOBNAME,T.TRANSPORTJOBTYPE,T.JOBSTATE,T.CARRIERNAME,T.CURRENTMACHINENAME, ");
		sql.append(" T.SOURCEMACHINENAME,T.DESTINATIONMACHINENAME,T.LASTEVENTTIME,T.SENDEMAILFLAG,T.TRANSFERSTATE, ");
	    sql.append(" T.LASTEVENTUSER,H.EVENTRESULTTEXT,ST.EVENTTIME,D.FACTORYNAME,D.DURABLETYPE,D.DURABLESPECNAME ");
	    sql.append(" from CT_TRANSPORTJOBCOMMAND T,CT_TRANSPORTJOBCOMMANDHIST H,ST,DURABLE D ");
	    sql.append(" WHERE T.JOBSTATE IN ('Started','Accepted','Requested') ");
	    sql.append(" AND   T.JOBSTATE=H.JOBSTATE");
	    sql.append(" AND   T.TRANSPORTJOBNAME=H.TRANSPORTJOBNAME ");
	    sql.append(" AND   T.TRANSPORTJOBNAME=ST.TRANSPORTJOBNAME ");
	    sql.append(" AND   T.CARRIERNAME=H.CARRIERNAME ");
	    sql.append(" AND   T.LASTEVENTTIMEKEY =H.TIMEKEY ");
	    sql.append(" AND   T.TRANSPORTJOBNAME NOT like'%C20%' " );
	    sql.append(" AND   T.CARRIERNAME=D.DURABLENAME");
	    sql.append(" AND   T.LASTEVENTTIME>(SYSDATE - 2 / 24 ) ");
	    sql.append(" ORDER BY TRANSPORTJOBNAME  DESC ");
        List<Map<String, Object>> sqlResult2= GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString());
        
        if(sqlResult1.size()>0)
		{
			CheckCSTID1(sqlResult1);
		}
		
        
		if(sqlResult2.size()>0)
		{
			CheckCSTID2(sqlResult2);
		}
	}
	 public void CheckCSTID1( List<Map<String, Object>> sqlResult1) throws CustomException
	 {
		 for (Map<String, Object> TransportTerminatedMap:sqlResult1)
		 {
			String transportjobName;
			String transportjobType;
			String jobState;
			String carrierName;
			String transferState;
			String currentmachineName;
			String sourcemachineName;
			String sendemailFlag;
			String creatTime;
			String destinationmachineName;
			String lasteventTime;
			String eventUser;
			String eventresultText;
			String factoryName;
			String durableType;
			String durableSpecName;
			
			transportjobName=ConvertUtil.getMapValueByName(TransportTerminatedMap, "TRANSPORTJOBNAME");
			transportjobType=ConvertUtil.getMapValueByName(TransportTerminatedMap, "TRANSPORTJOBTYPE");
			jobState=ConvertUtil.getMapValueByName(TransportTerminatedMap, "JOBSTATE");
			carrierName=ConvertUtil.getMapValueByName(TransportTerminatedMap, "CARRIERNAME");
		    transferState=ConvertUtil.getMapValueByName(TransportTerminatedMap, "TRANSFERSTATE");
		    currentmachineName=ConvertUtil.getMapValueByName(TransportTerminatedMap, "CURRENTMACHINENAME");
		    sourcemachineName=ConvertUtil.getMapValueByName(TransportTerminatedMap, "SOURCEMACHINENAME");
		    destinationmachineName=ConvertUtil.getMapValueByName(TransportTerminatedMap, "DESTINATIONMACHINENAME");
		    lasteventTime=ConvertUtil.getMapValueByName(TransportTerminatedMap, "LASTEVENTTIME");
		    creatTime=ConvertUtil.getMapValueByName(TransportTerminatedMap, "EVENTTIME");
		    Timestamp Time=Timestamp.valueOf(creatTime);
		    eventUser=ConvertUtil.getMapValueByName(TransportTerminatedMap, "EVENTUSER");
		    eventresultText=ConvertUtil.getMapValueByName(TransportTerminatedMap, "JOBSTATE");
		    sendemailFlag=ConvertUtil.getMapValueByName(TransportTerminatedMap, "SENDEMAILFLAG");
		    factoryName=ConvertUtil.getMapValueByName(TransportTerminatedMap, "FACTORYNAME");
		    durableType=ConvertUtil.getMapValueByName(TransportTerminatedMap, "DURABLETYPE");
		    durableSpecName=ConvertUtil.getMapValueByName(TransportTerminatedMap, "DURABLESPECNAME");
		    Double transportTime = ConvertUtil.getDiffTime(TimeUtils.toTimeString(Time, TimeStampUtil.FORMAT_TIMEKEY), TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY));
		    transportTime = (transportTime / 60 / 60);
		    if(jobState.equals("Terminated")&&(!sendemailFlag.equals("Y")))
		    {
		    	 sendemailFlag="Y";
		    	 java.text.DecimalFormat df =new java.text.DecimalFormat("#.##");  
				 String TransportTime  = df.format(transportTime);
		    	 sendTransportJobEmail(transportjobName,transportjobType,jobState,carrierName,currentmachineName,eventUser,eventresultText,sendemailFlag,factoryName,TransportTime,durableSpecName,transferState );
		    	 
		    	 //houxk 20210527		    	 
		    	 try
				 {				
		    		 sendToEm(transportjobName,transportjobType,jobState,carrierName,currentmachineName,eventUser,eventresultText,sendemailFlag,factoryName,TransportTime,durableSpecName,transferState);
				 }
				 catch (Exception e)
				 {
					log.info("eMobile or WeChat Send Error : " + e.getCause());	
				 }	    	 
		    }
		 }
	 }
	 public void CheckCSTID2( List<Map<String, Object>> sqlResult1) throws CustomException
	 {
		 for (Map<String, Object> TransportTerminatedMap:sqlResult1)
		 {
			    String transportjobName;
				String transportjobType;
				String jobState;
				String carrierName;
				String transferState;
				String currentmachineName;
				String sourcemachineName;
				String sendemailFlag;
				String creatTime;
				String destinationmachineName;
				String lasteventTime;
				String eventUser;
				String eventresultText;
				String factoryName;
				String durableType;
				String durableSpecName;
				
				transportjobName=ConvertUtil.getMapValueByName(TransportTerminatedMap, "TRANSPORTJOBNAME");
				transportjobType=ConvertUtil.getMapValueByName(TransportTerminatedMap, "TRANSPORTJOBTYPE");
				jobState=ConvertUtil.getMapValueByName(TransportTerminatedMap, "JOBSTATE");
				carrierName=ConvertUtil.getMapValueByName(TransportTerminatedMap, "CARRIERNAME");
			    transferState=ConvertUtil.getMapValueByName(TransportTerminatedMap, "TRANSFERSTATE");
			    currentmachineName=ConvertUtil.getMapValueByName(TransportTerminatedMap, "CURRENTMACHINENAME");
			    sourcemachineName=ConvertUtil.getMapValueByName(TransportTerminatedMap, "SOURCEMACHINENAME");
			    destinationmachineName=ConvertUtil.getMapValueByName(TransportTerminatedMap, "DESTINATIONMACHINENAME");
			    lasteventTime=ConvertUtil.getMapValueByName(TransportTerminatedMap, "LASTEVENTTIME");
			    creatTime=ConvertUtil.getMapValueByName(TransportTerminatedMap, "EVENTTIME");
			    Timestamp Time=Timestamp.valueOf(creatTime);
			    eventUser=ConvertUtil.getMapValueByName(TransportTerminatedMap, "LASTEVENTUSER");
			    eventresultText=ConvertUtil.getMapValueByName(TransportTerminatedMap, "JOBSTATE");
			    sendemailFlag=ConvertUtil.getMapValueByName(TransportTerminatedMap, "SENDEMAILFLAG");
			    factoryName=ConvertUtil.getMapValueByName(TransportTerminatedMap, "FACTORYNAME");
			    durableType=ConvertUtil.getMapValueByName(TransportTerminatedMap, "DURABLETYPE");
			    durableSpecName=ConvertUtil.getMapValueByName(TransportTerminatedMap, "DURABLESPECNAME");
			    Double transportTime = ConvertUtil.getDiffTime(TimeUtils.toTimeString(Time, TimeStampUtil.FORMAT_TIMEKEY), TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY));
			    transportTime = (transportTime / 60 / 60);
			    //根据不同的厂设定不同的时间
			    StringBuffer sql = new StringBuffer();
		        sql.append(" SELECT ENUMVALUE FROM  ENUMDEFVALUE where enumname='TransportjobName' AND DESCRIPTION=:DESCRIPTION  ");
		        Map<String, Object> args = new HashMap<String, Object>();
				args.put("DESCRIPTION", factoryName);
				List<Map<String, Object>> sqlResult3 = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		        Map<String, Object> map = sqlResult3.get(0);
		        Double TransportjobLimintTime=Double.parseDouble(map.get("ENUMVALUE").toString());
			    if( transportTime>TransportjobLimintTime&&(!sendemailFlag.equals("Y")))
			    {
			    	sendemailFlag="Y";
			    	java.text.DecimalFormat df =new java.text.DecimalFormat("#.##");
					String TransportTime  = df.format(transportTime);
			    	sendTransportJobEmail(transportjobName,transportjobType,jobState,carrierName,currentmachineName,eventUser,eventresultText,sendemailFlag,factoryName,TransportTime,durableSpecName,transferState );
			    	
			    	//houxk 20210527			    	
			    	try
					{				
			    		sendToEm(transportjobName,transportjobType,jobState,carrierName,currentmachineName,eventUser,eventresultText,sendemailFlag,factoryName,TransportTime,durableSpecName,transferState);
					}
					catch (Exception e)
					{
						log.info("eMobile or WeChat Send Error : " + e.getCause());	
					}
			    }
			 
		 }
	 }
	 public void sendTransportJobEmail(String transportjobName,String transportjobType,String jobState,String carrierName,String currentmachineName,String eventUser,String eventresultText,String sendemailFlag,String factoryName,String TransportTime,String durableSpecName,String transferState)throws CustomException
	 {
		 String message = "<pre>===============AlarmInformation===============</pre>";
			message += "<pre>-TransportJobName	: " + transportjobName + "</pre>";
			message += "<pre>-TransportJobType	: " + transportjobType + "</pre>";
			message += "<pre>-JobState	        : " + jobState + "</pre>";
			message += "<pre>-FactoryName	    : " + factoryName + "</pre>";
			message += "<pre>-CarrierName	    : " + carrierName + "</pre>";
			message += "<pre>-TransportTime	    : " + TransportTime + "</pre>";
			message += "<pre>-CurrentMachineName: " + currentmachineName + "</pre>";
			message += "<pre>-TransferState     : " + transferState + "</pre>";
			message += "<pre>-EventUser	        : " + eventUser + "</pre>";
			message += "<pre>-EventComment	    : " + eventresultText  + "</pre>";
			
			
			message += "<pre>==============================================</pre>";
			
			log.info("sendEmailStrated");
			if(factoryName.equals("ARRAY"))
			{
			 CommonUtil.sendTransportEmail("TA001", "MCS",  message,transportjobName,transportjobType,jobState,sendemailFlag);//H002自定义AlarmCode
			 String smsMessage = alarmSmsText(transportjobName,transportjobType,jobState,carrierName,currentmachineName,eventUser,eventresultText,sendemailFlag,factoryName,TransportTime,transferState);
			 //GenericServiceProxy.getSMSInterface().TransportSmsSend("TA001", "MCS",  smsMessage,transportjobName,transportjobType,jobState,sendemailFlag);
			}
			if(factoryName.equals("OLED"))
			{
				if(durableSpecName.equals("1C"))
				{
					CommonUtil.sendTransportEmail("MO001", "MES",  message,transportjobName,transportjobType,jobState,sendemailFlag);//H002自定义AlarmCode
					 String smsMessage = alarmSmsText(transportjobName,transportjobType,jobState,carrierName,currentmachineName,eventUser,eventresultText,sendemailFlag,factoryName,TransportTime,transferState);
					 //GenericServiceProxy.getSMSInterface().TransportSmsSend("MO001", "MCS",  smsMessage,transportjobName,transportjobType,jobState,sendemailFlag);
				}
				else
				{
					CommonUtil.sendTransportEmail("TO001", "MES",  message,transportjobName,transportjobType,jobState,sendemailFlag);//H002自定义AlarmCode
					 String smsMessage = alarmSmsText(transportjobName,transportjobType,jobState,carrierName,currentmachineName,eventUser,eventresultText,sendemailFlag,factoryName,TransportTime,transferState);
					 //GenericServiceProxy.getSMSInterface().TransportSmsSend("TO001", "MCS",  smsMessage,transportjobName,transportjobType,jobState,sendemailFlag);
				}
			}
			if(factoryName.equals("TP"))
			{
			 CommonUtil.sendTransportEmail("TT001", "MCS",  message,transportjobName,transportjobType,jobState,sendemailFlag);//H002自定义AlarmCode
			 String smsMessage = alarmSmsText(transportjobName,transportjobType,jobState,carrierName,currentmachineName,eventUser,eventresultText,sendemailFlag,factoryName,TransportTime,transferState);
			 //GenericServiceProxy.getSMSInterface().TransportSmsSend("TT001", "MCS",  smsMessage,transportjobName,transportjobType,jobState,sendemailFlag);
			}
			if(factoryName.equals("POSTCELL"))
			{
			 CommonUtil.sendTransportEmail("TP001", "MCS",  message,transportjobName,transportjobType,jobState,sendemailFlag);//H002自定义AlarmCode
			 String smsMessage = alarmSmsText(transportjobName,transportjobType,jobState,carrierName,currentmachineName,eventUser,eventresultText,sendemailFlag,factoryName,TransportTime,transferState);
			 //GenericServiceProxy.getSMSInterface().TransportSmsSend("TP001", "MCS",  smsMessage,transportjobName,transportjobType,jobState,sendemailFlag);
			}
			 log.info("sendEmailFinshed");
	 }
		private String alarmSmsText(String transportjobName,String transportjobType,String jobState,String carrierName,String currentmachineName,String eventUser,String eventresultText,String sendemailFlag,String factoryName,String TransportTime,String transferState)
		{
			StringBuilder smsMessage = new StringBuilder();
			smsMessage.append("\n");
			smsMessage.append("\n");
			smsMessage.append("TransportJobName: ");
			smsMessage.append(transportjobName);
			smsMessage.append("\n");
			smsMessage.append("TransportJobType: ");
			smsMessage.append(transportjobType);
			smsMessage.append("\n");
			smsMessage.append("JobState: ");
			smsMessage.append(jobState);
			smsMessage.append("\n");
			smsMessage.append("FactoryName: ");
			smsMessage.append(factoryName);
			smsMessage.append("\n");
			smsMessage.append("CarrierName: ");
			smsMessage.append(carrierName);
			smsMessage.append("\n");
			smsMessage.append("TransportTime: ");
			smsMessage.append(TransportTime);
			smsMessage.append("\n");
			smsMessage.append("CurrentMachineName: ");
			smsMessage.append(currentmachineName);
			smsMessage.append("\n");
			smsMessage.append("TransferState: ");
			smsMessage.append(transferState);
			smsMessage.append("\n");
			smsMessage.append("EventUser: ");
			smsMessage.append(eventUser);
			smsMessage.append("\n");
			smsMessage.append("EventComment: ");
			smsMessage.append(eventresultText);

			return smsMessage.toString();
		}
		
	public void sendToEm(String transportjobName,String transportjobType,String jobState,String carrierName,String currentmachineName,String eventUser,String eventresultText,String sendemailFlag,String factoryName,String TransportTime,String durableSpecName,String transferState)throws CustomException
	{
		String userList[] = getUserList(carrierName, "TransportJobAlarm");	
		if(userList == null || userList.length ==0) return;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TransportJobTimer", "MES", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{	
			log.info("TransportJobAlarm Start Send To Emobile & Wechat");	
						
			String title = "搬送异常提醒";
			String detailtitle = "${}CIM系统消息通知";
			String url = "";
			
			StringBuffer info = new StringBuffer();
			info.append("<pre>=======================AlarmInformation=======================</pre>");
			info.append("<pre>-TransportJobName	: " + transportjobName + "</pre>");
			info.append("<pre>-TransportJobType	: " + transportjobType + "</pre>");
			info.append("<pre>-JobState	        : " + jobState + "</pre>");
			info.append("<pre>-FactoryName	    : " + factoryName + "</pre>");
			info.append("<pre>-CarrierName	    : " + carrierName + "</pre>");
			info.append("<pre>-TransportTime	    : " + TransportTime + "</pre>");
			info.append("<pre>-CurrentMachineName: " + currentmachineName + "</pre>");
			info.append("<pre>-TransferState     : " + transferState + "</pre>");
			info.append("<pre>-EventUser	        : " + eventUser + "</pre>");
			info.append("<pre>-EventComment	    : " + eventresultText  + "</pre>");
			info.append("<pre>=============================End=============================</pre>");			
			
			String message = info.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userList, title, detailtitle, message, url);
			
			StringBuffer weChatInfo = new StringBuffer();
			weChatInfo.append("<pre>====AlarmInformation====</pre>");
			weChatInfo.append("<pre>-TransportJobName	: " + transportjobName + "</pre>");
			weChatInfo.append("<pre>-TransportJobType	: " + transportjobType + "</pre>");
			weChatInfo.append("<pre>-JobState	        : " + jobState + "</pre>");
			weChatInfo.append("<pre>-FactoryName	    : " + factoryName + "</pre>");
			weChatInfo.append("<pre>-CarrierName	    : " + carrierName + "</pre>");
			weChatInfo.append("<pre>-TransportTime	    : " + TransportTime + "</pre>");
			weChatInfo.append("<pre>-CurrentMachineName: " + currentmachineName + "</pre>");
			weChatInfo.append("<pre>-TransferState     : " + transferState + "</pre>");
			weChatInfo.append("<pre>-EventUser	        : " + eventUser + "</pre>");
			weChatInfo.append("<pre>-EventComment	    : " + eventresultText  + "</pre>");		
			weChatInfo.append("<pre>====AlarmInfoEnd====</pre>");
			
			String weChatMessage = weChatInfo.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userList, title, detailtitle, weChatMessage, "");
		}
		catch (Exception e)
		{
			log.info("eMobile or WeChat Send Error : " + e.getCause());	
		} 
	}
	
	//AlarmGroup = TransportJobAlarm && CSTDepart
	private String[] getUserList(String carrierName, String alarmGroupName)
	{		
		String[] userList = null;
		List<Map<String,Object>> resultList = null;
		List<String> sb = new ArrayList<String>();
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT M.DEPARTMENT AS DEPARTMENT1,D.DEPARTMENT AS DEPARTMENT2 FROM CT_MATERIALINFO M,DURABLE D WHERE M.MATERIALTYPE = D.DURABLETYPE AND D.DURABLENAME = :DURABLENAME ");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("DURABLENAME", carrierName);
	
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
				String departmentAll = ConvertUtil.getMapValueByName(resultList.get(0), "DEPARTMENT1") + "," + ConvertUtil.getMapValueByName(resultList.get(0), "DEPARTMENT2");

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
			log.info("Not Found the Department of "+ carrierName);
			log.info(" Failed to send to EMobile, MachineName: " + carrierName);
		}
		
		return userList;
	}

}
