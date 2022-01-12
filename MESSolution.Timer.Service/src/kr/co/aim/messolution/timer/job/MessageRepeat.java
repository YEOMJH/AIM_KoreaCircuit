package kr.co.aim.messolution.timer.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.greentrack.generic.GenericServiceProxy;

public class MessageRepeat implements Job, InitializingBean {

	private Log log=LogFactory.getLog(MessageRepeat.class);
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		try {
			queryExecute();
		} catch (CustomException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public  void  queryExecute() throws CustomException{
		StringBuilder sql =new StringBuilder();
		sql.append("SELECT EVENTNAME, TRANSACTIONID");
		sql.append("    FROM CT_MESSAGELOG");
		sql.append("    WHERE (SERVERNAME LIKE 'CNMsvrCNMsvr%' OR SERVERNAME LIKE 'CNXsvrCNXsvr%')");
		sql.append("     AND TIMEKEY BETWEEN");
		sql.append("     TO_CHAR(SYSDATE - 30 / 1440, 'YYYYMMDDHH24MI') || '00000000' AND");
		sql.append("     TO_CHAR(SYSDATE, 'YYYYMMDDHH24MI') || '00000000'");
		sql.append("     GROUP BY EVENTNAME, TRANSACTIONID");
		sql.append("     HAVING COUNT(*) > 3");
		
		List<Map<String, Object>> result=new ArrayList<Map<String, Object>>();
		Map<String, Object> args=new HashMap<>();
		
		 result=GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		 
		 if(result.size()>0){
			String eventName= ConvertUtil.getMapValueByName(result.get(0), "EVENTNAME");
			String transactionID= ConvertUtil.getMapValueByName(result.get(0), "TRANSACTIONID");
			

			String message = "<pre>===============AlarmInformation===============</pre>";
		  	  	  message += "<pre>==============================================</pre>";
			      message += "<pre>- EVENTNAME	: " + eventName + "</pre>";
			      message += "<pre>- TRANSACTIONID	: " + transactionID + "</pre>";
			
			 CommonUtil.sendAlarmEmail("ServerDoubleDispose", "MES", message);
			 StringBuilder smsMessage = new StringBuilder();
			 
			 
			    smsMessage.append("\n");
				smsMessage.append("AlarmCode: ");
				smsMessage.append("ServerDoubleDispose");
				smsMessage.append("\n");
				smsMessage.append("AlarmType: ");
				smsMessage.append("MES");
				smsMessage.append("\n");
				smsMessage.append("message: ");
				smsMessage.append(message);
				smsMessage.append("\n");

				//kr.co.aim.messolution.generic.GenericServiceProxy.getSMSInterface().AlarmSmsSend("ServerDoubleDispose", "MES", smsMessage.toString());
				
		 }
		
	}

}
