package kr.co.aim.messolution.timer.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ReviewStationOLEDOfflineQtimeTimer implements Job, InitializingBean
{	
	private static Log log = LogFactory.getLog(ReviewStationOLEDOfflineQtimeTimer.class);
	
	@Override
	public void afterPropertiesSet() throws Exception 
	{
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}
	
	public void execute(JobExecutionContext arg0) throws JobExecutionException
	{	
		try
		{
			startQtimeMonitoring();
		}
		catch (CustomException e)
		{
			if (log.isDebugEnabled())
				log.error(e.errorDef.getLoc_errorMessage());
		}
	}
	
	private void startQtimeMonitoring() throws CustomException
	{
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		String sql = "SELECT P.PRODUCTNAME, P.PROCESSOPERATIONNAME, P.LOTNAME, P.PROCESSFLOWNAME, P.PRODUCTSPECNAME " +
					 "FROM PRODUCT P, CT_COMPONENTINSPECTHISTORY CH, CT_REVIEWOPERATIONINFO RI, LOT L " +
					 "WHERE 1=1 " +
					 "	AND P.PRODUCTNAME NOT IN (SELECT RJ.PRODUCTNAME FROM CT_REVIEWPRODUCTJUDGE RJ WHERE RJ.PROCESSOPERATIONNAME = P.PROCESSOPERATIONNAME) " +
					 "	AND RI.QTIMEFLAG = 'Y' " +
					 "	AND P.PROCESSOPERATIONNAME = RI.PROCESSOPERATIONNAME " +
					 "	AND P.LOTNAME = L.LOTNAME " +
					 "	AND P.PRODUCTNAME = CH.PRODUCTNAME " +
					 "	AND L.BEFOREOPERATIONNAME = CH.PROCESSOPERATIONNAME " +
					 "	AND CH.EVENTTIME + (RI.QTIME / 24) < SYSDATE " ;
		
		Map<String,Object> bindMap = new HashMap<>();
		List<Map<String,Object>> resultList = null;
		try {
			
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		} catch (Exception ex) 
		{
           log.debug(ex.getCause());
		}
		
		if (resultList == null) {
			log.info("");
			return;
		}
		
		
		String sqlEmail = "SELECT UF.* "
				+ "FROM CT_ALARMGROUP AG, CT_ALARMUSERGROUP AUG, CT_ALARMUSER UF "
				+ "WHERE AG.ALARMGROUPNAME = AUG.ALARMGROUPNAME "
				+ "AND AG.ALARMGROUPNAME = 'ReviewQTime' "
				+ "AND AG.ALARMTYPE = 'QTimeOver' "
				+ "AND AUG.USERID = UF.USERID";
		
		bindMap.clear();
		List<Map<String,Object>> resultListEmail = null;
		try {
			
			resultListEmail = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlEmail, bindMap);

		} catch (Exception ex) 
		{
           log.debug(ex.getCause());
		}
		
		if (resultListEmail == null) {
			log.info("");
			return;
		}
		
		// mail message

		String message = "<pre>===============ReviewStation QTimerOver===============</pre>";
		for (Map<String, Object> map : resultList) 
		{
			message += "<pre>" + map.get("LOTNAME").toString() + "</pre>";
			message += "<pre>" + map.get("PRODUCTNAME").toString() + "</pre>";
			message += "<pre>" + map.get("PROCESSFLOWNAME").toString() + "</pre>";
			message += "<pre>" + map.get("PROCESSOPERATIONNAME").toString() + "</pre>";
			message += "<pre></pre>";
		} 
		
		 List<String> emailList = CommonUtil.makeListBySqlResult(resultListEmail, "EMAIL");
	  	 List<String> userList = CommonUtil.makeListBySqlResult(resultListEmail, "USERID");
	  	 
		try 
		{
			GenericServiceProxy.getMailSerivce().postMail(emailList.toArray(new String[] {}), "ReviewQTime", message);
		} 
		catch (CustomException customException) 
		{
			log.error("Failed to send mail.");
			throw customException;
		} 
		catch (Exception ex) 
		{
			throw new CustomException("MAIL-0002", ex.getCause());
		}
		
		//houxk 20210617
		if(userList.size() > 0)
		{
			try
			{				
				sendToEm(userList, message );
			}
			catch (Exception e)
			{
				log.info("eMobile or WeChat Send Error : " + e.getCause());	
			}
		}
	}
	
	//AlarmType = QTimeOver & AlarmGroup = ReviewQTime
	public void sendToEm(List<String> sb, String message)
	{		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReviewStationOLEDOfflineQtimeTimer", "MES", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		String[] userList = sb.toArray(new String[] {});
		
		try
		{	
			log.info("ReviewQTimeOver Start Send To Emobile & Wechat");	
						
			String title = "ReviewQTimeOver";
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
	
}
