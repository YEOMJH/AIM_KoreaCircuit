package kr.co.aim.messolution.timer.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

public class BorrowPanelTimer implements Job, InitializingBean {

	private Log log = LogFactory.getLog(BorrowPanelTimer.class);

	@Override
	public void afterPropertiesSet() throws Exception
	{
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException
	{
		try
		{
			borrowPanelMonitor();
		}
		catch (CustomException e)
		{
			e.printStackTrace();
		}
	}

	private void borrowPanelMonitor() throws CustomException
	{
		List<Map<String, Object>> enumDefValueList = CommonUtil.getEnumDefValueByEnumName("BorrowTargetLimit");
		int renewCountLimit = Integer.parseInt(ConvertUtil.getMapValueByName(enumDefValueList.get(0), "ENUMVALUE"));

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT TASKID, LOTNAME, BORROWSTATE, BORROWDATE, RENEWCOUNT, RENEWDATE ");
		sql.append("  FROM CT_BORROWPANEL ");
		sql.append(" WHERE BORROWSTATE = 'Borrowed' ");
		sql.append("   AND ( (RENEWCOUNT = 0 AND (SYSDATE - BORROWDATE) * 24 > :LIMIT) ");
		sql.append("     OR (RENEWCOUNT != 0 AND (SYSDATE - RENEWDATE) * 24 > :LIMIT)) ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LIMIT", renewCountLimit);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (sqlResult.size() < 1)
		{
			log.info("BorrowPanel Data is not exist");
		}

		for (Map<String, Object> result : sqlResult)
		{
			String taskID = ConvertUtil.getMapValueByName(result, "TASKID");
			String lotName = ConvertUtil.getMapValueByName(result, "LOTNAME");
			String borrowState = ConvertUtil.getMapValueByName(result, "BORROWSTATE");
			String borrowDate = ConvertUtil.getMapValueByName(result, "BORROWDATE");
			String renewCount = ConvertUtil.getMapValueByName(result, "RENEWCOUNT");
			String renewDate = ConvertUtil.getMapValueByName(result, "RENEWDATE");

			String[] mailList = CommonUtil.getEmailListByAlarmGroup("BorrowTimeOver");

			if (mailList == null || mailList.length == 0)
				return;

			String message = "<pre>=======================AlarmInformation=======================</pre>";
			message += "<pre>==============================================================</pre>";
			message += "<pre>- TaskID			: " + taskID + "</pre>";
			message += "<pre>- LotName			: " + lotName + "</pre>";
			message += "<pre>- BorrowState		: " + borrowState + "</pre>";
			message += "<pre>- BorrowDate		: " + borrowDate + "</pre>";
			message += "<pre>- RenewCount		: " + renewCount + "</pre>";
			message += "<pre>- renewDate		: " + renewDate + "</pre>";
			message += "<pre>==============================================================</pre>";

			try
			{
				GenericServiceProxy.getMailSerivce().postMail(mailList, this.getClass().getSimpleName(), message);								
			}
			catch (Exception ex)
			{
				if (ex instanceof CustomException)
				{
					log.info(((CustomException) ex).errorDef.getEng_errorMessage());
				}
				else
				{
					throw new CustomException(ex.getCause());
				}
			}
			
			//houxk 20210617
			try
			{				
				sendToEm("BorrowTimeOver", message);
			}
			catch (Exception e)
			{
				log.info("eMobile or WeChat Send Error : " + e.getCause());	
			}
		}

	}
	
	public void sendToEm(String alarmGroupName, String message)
	{
		String userList[] = getUserList(alarmGroupName);	
		if(userList == null || userList.length ==0) return;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("BorrowPanelTimer", "MES", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{	
			log.info("BorrowTimeOver Start Send To Emobile & Wechat");	
						
			String title = "BorrowTimeOver";
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
	//AlarmGroup = BorrowTimeOver
	private String[] getUserList(String alarmGroupName)
	{
		String sql = " SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B "
				   + " WHERE A.USERID = B.USERID "
				   + " AND A.ALARMGROUPNAME = :ALARMGROUPNAME ";
		
		List<Map<String, Object>> resultList = null;
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new String[] { alarmGroupName});
		}
		catch (Exception ex)
		{
			log.info(ex.getCause());
		}

		if(resultList ==null || resultList.size()==0) return null;
		
		return CommonUtil.makeListBySqlResult(resultList, "USERID").toArray(new String[] {});
	}
	
}
