package kr.co.aim.messolution.timer.job;

import org.springframework.beans.factory.InitializingBean;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.greentrack.generic.util.TimeUtils;

import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EMailInterface;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AlarmDefinition;

public class BorroTimer implements Job, InitializingBean {
	private static Log log = LogFactory.getLog(BorroTimer.class);

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			// Monitor
			log.debug("monitorBorroTimer-Start");
			monitorBorroTimer();
		} catch (CustomException e) {
			if (log.isDebugEnabled())
				log.error(e.errorDef.getLoc_errorMessage());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		log.debug("monitorBorroTimer-End");
	}

	public void monitorBorroTimer() throws CustomException {
		StringBuilder sql = new StringBuilder();
		sql.append(" WITH AC AS ");
		sql.append(" (SELECT BC.BORROWCENTRALITY, BC.BORROWDEPARTMENT, BC.BORROWUSERNAME, BC.PHONENUMBER,BC.SENDEMAILFLG, ");
		sql.append("  TO_CHAR (TO_DATE(BC.BORROWTIMEA, 'yyyy-mm-dd HH24:MI:SS' ),'yyyy-mm-dd HH24:MI:SS') AS BORROWTIME  ");
		sql.append("  FROM (SELECT B.BORROWUSERNAME,B.BORROWDEPARTMENT,B.SENDEMAILFLG,B.PHONENUMBER, ");
		sql.append(" B.BORROWCENTRALITY,TO_CHAR (B.BORROWDATE, 'yyyy-mm-dd HH24:MI') AS BORROWTIMEA ");
		sql.append(" FROM CT_BORROWPANEL B ");
		sql.append(" WHERE B.BORROWDEPARTMENT IS NOT NULL AND B.BORROWCENTRALITY IS NOT NULL  ");
		sql.append(" ORDER BY B.BORROWDATE ASC)BC) ");
		sql.append("  SELECT AC.BORROWCENTRALITY, AC.BORROWDEPARTMENT, AC.BORROWUSERNAME, AC.PHONENUMBER, ");
		sql.append("  AC.SENDEMAILFLG,AC.BORROWTIME, ");
		sql.append("  COUNT (*) AS BORROWNUMBER ");
		sql.append("  FROM AC ");
		sql.append(" GROUP BY AC.BORROWCENTRALITY,AC.BORROWDEPARTMENT,AC.BORROWUSERNAME, ");
		sql.append(" AC.PHONENUMBER, AC.SENDEMAILFLG,AC.BORROWTIME ");
		sql.append(" ORDER BY AC.BORROWTIME ");
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate()
				.queryForList(sql.toString());
		if (sqlResult.size() > 0) {
			CheckBorroTimer(sqlResult);
		}
	}

	public void CheckBorroTimer(List<Map<String, Object>> sqlResult) throws CustomException {
		for (Map<String, Object> BorroTimeMap : sqlResult) {
			String borrowUsername;
			String borrowDepartment;
			String borrowCentrality;
			String borrowDate;
			String borrownumber;
			String phoneNumber;
			String sendEmailFlag;
			borrowUsername = ConvertUtil.getMapValueByName(BorroTimeMap, "BORROWUSERNAME");
			borrowDepartment = ConvertUtil.getMapValueByName(BorroTimeMap, "BORROWDEPARTMENT");
			borrowCentrality=ConvertUtil.getMapValueByName(BorroTimeMap, "BORROWCENTRALITY");
			borrownumber = ConvertUtil.getMapValueByName(BorroTimeMap, "BORROWNUMBER");
			sendEmailFlag = ConvertUtil.getMapValueByName(BorroTimeMap, "SENDEMAILFLG");
			phoneNumber = ConvertUtil.getMapValueByName(BorroTimeMap, "PHONENUMBER");
			borrowDate = ConvertUtil.getMapValueByName(BorroTimeMap, "BORROWTIME");
			Timestamp Time = Timestamp.valueOf(borrowDate);
			Double borrowTime = ConvertUtil.getDiffTime(TimeUtils.toTimeString(Time, TimeStampUtil.FORMAT_TIMEKEY),
					TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY));
			borrowTime = (borrowTime / 60 / 60);
			if (borrowTime > Double.valueOf(48.0) && borrowTime <= Double.valueOf(72.0)&& (sendEmailFlag == null || sendEmailFlag.equals("")))
			{
				String NSendEmailFlag = "A";
				String alarmCode="B001";
				DecimalFormat df = new DecimalFormat("#.00");
				String BorrowTime = df.format(borrowTime);
				sendBorrowTimeEmailToborrowUsername(alarmCode,borrowCentrality,borrowDepartment,borrowUsername,borrownumber,NSendEmailFlag,phoneNumber,borrowDate,BorrowTime);

			}
			if (borrowTime > Double.valueOf(72.0) && borrowTime <= Double.valueOf(96.0)&&!sendEmailFlag.equals("B"))
			{
				String NSendEmailFlag = "B";
				String alarmCode="B002";
				DecimalFormat df = new DecimalFormat("#.00");
				String BorrowTime = df.format(borrowTime);
				sendBorrowTimeEmailToborrowUsername(alarmCode,borrowCentrality,borrowDepartment,borrowUsername,borrownumber,NSendEmailFlag,phoneNumber,borrowDate,BorrowTime);

			}
			if (borrowTime > Double.valueOf(96.0) && !sendEmailFlag.equals("C"))
			{
				String NSendEmailFlag = "C";
				String alarmCode="B003";
				DecimalFormat df = new DecimalFormat("#.00");
				String BorrowTime = df.format(borrowTime);
				sendBorrowTimeEmailToborrowUsername(alarmCode,borrowCentrality,borrowDepartment,borrowUsername,borrownumber,NSendEmailFlag,phoneNumber,borrowDate,BorrowTime);

			}

		}

	}

	public void sendBorrowTimeEmailToborrowUsername(String alarmCode,String borrowCentrality,String borrowDepartment,String borrowUsername,String borrownumber,String sendEmailFlag,String phoneNumber,String borrowDate,String BorrowTime) throws CustomException 
	{
		StringBuilder sql = new StringBuilder();
		sql = new StringBuilder();
		sql.append("select*from CT_ALARMDEFINITION L  ");
		sql.append(" WHERE L.ALARMCODE='B001' ");
		sql.append("AND L.ALARMTYPE='Borrow'");
		sql.append(" ORDER BY ALARMCODE");

		List<Map<String, Object>> alarmDef = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString());
		if (alarmDef.size() > 0) {
			String message = "<pre>===============BorrowInformation===============</pre>";
			message += "<pre>-BorrowNumber       : " + borrownumber + "</pre>";
			message += "<pre>-BorrowData         : " + borrowDate + "</pre>";
			message += "<pre>-BorrowTime         : " + BorrowTime + "</pre>";
			message += "<pre>-PhoneNumber        : " + phoneNumber + "</pre>";
			message += "<pre>==============================================</pre>";
			
			String nSendEmailFlag= sendEmailFlag;
			
			log.info("send borrowEmailStrated");
			CommonUtil.sendAlarmEmailToborrowUsername(nSendEmailFlag,alarmCode,"Borrow",message,borrowCentrality,borrowDepartment,borrowUsername,borrowDate);//B001自定义AlarmCode
			 log.info("sendborrowEmailFinshed");
		}
	}
		
		
	

}
