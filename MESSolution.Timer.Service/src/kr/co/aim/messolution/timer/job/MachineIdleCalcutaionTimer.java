package kr.co.aim.messolution.timer.job;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

public class MachineIdleCalcutaionTimer implements Job, InitializingBean  {
	private static Log log = LogFactory.getLog(MachineIdleCalcutaionTimer.class);
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub

		try
		{
			// Monitor
			// 1.monitorMachineIdleTime
			log.debug("monitorMachineIdleTime-Start");
			monitorMachineIdleTime();
			
			// 2.monitorMachineIdleTimeByChamber
			log.debug("monitorMachineIdleTimeByChamber-Start");
			monitorMachineIdleTimeByChamber();
			
			// 3.PPIDIdleTimeControlMachine
			//log.debug("PPIDIdleTimeControlMachine-Start");
			//PPIDIdleTimeControlMachine();
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
		log.debug("monitorMachineIdleTime-End");
	
		
	}
	public void monitorMachineIdleTime() throws CustomException, InterruptedException, MalformedURLException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("  SELECT M.MACHINENAME   ");
		sql.append("                ,SYSDATE   ");
		sql.append("                ,M.LASTIDLETIME   ");
		sql.append("                ,ROUND ( (SYSDATE - M.LASTIDLETIME) * 24, 1) AS TIMEGAP   ");
		sql.append("                ,MS.MAXIDLETIME   ");
		sql.append("                ,M.DSPFLAG   ");
		sql.append("                ,MS.EQPDEPART   ");
		sql.append("            FROM MACHINE M, MACHINESPEC MS   ");
		sql.append("           WHERE M.MACHINENAME = MS.MACHINENAME   ");
		sql.append("             AND MS.DETAILMACHINETYPE IN ('MAIN')   ");
		sql.append("             AND MS.MACHINETYPE IN ('ProductionMachine', 'InspectionMachine')   ");
		sql.append("             AND NVL (MS.CHECKIDLETIME, 'N') = 'Y'   ");
		sql.append("             AND MS.MAXIDLETIME IS NOT NULL   ");
		sql.append("             AND M.LASTIDLETIME IS NOT NULL   ");
		sql.append("             AND NVL (M.MACHINELOCKFLAG, 'N') = 'N'   ");
		sql.append("             AND TO_NUMBER (MS.MAXIDLETIME) > 0   ");
		sql.append("             AND ROUND ( (SYSDATE - M.LASTIDLETIME) * 24, 1) > MS.MAXIDLETIME   ");
		sql.append("             AND M.MACHINENAME NOT IN (SELECT E.ENUMVALUE FROM ENUMDEFVALUE E WHERE E.ENUMNAME = 'PPIDIdleTimeControlMachine') ");
		sql.append("          ORDER BY M.LASTIDLETIME DESC ");
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), new HashMap<String, Object>());

		if (result.size() > 0)
		{
			String eventUser = "MES";
			String eventComment = "Change Machine Lock Flag by Over IdleTime. ";
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeMachineLockFlag", eventUser, eventComment, null, null);

			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("MACHINELOCKFLAG", "Y");

			for (Map<String, Object> row : result)
			{
				String machineName = ConvertUtil.getMapValueByName(row, "MACHINENAME");
				String lastIdleTime = ConvertUtil.getMapValueByName(row, "LASTIDLETIME");
				String maxIdleTime = ConvertUtil.getMapValueByName(row, "MAXIDLETIME");
				String timeGap = ConvertUtil.getMapValueByName(row, "TIMEGAP");
				String eqpDepart = ConvertUtil.getMapValueByName(row, "EQPDEPART");

				eventInfo.setEventComment(eventComment + "LastIdleTime: " + lastIdleTime + ". LimitTime: " + maxIdleTime + ". TimeGap: " + timeGap);

				this.ChangeMachineLockFlag(eventInfo, machineName, udfs);

				
				if (StringUtils.isNotEmpty(eqpDepart))
				{
					String emailMessage = eqpIdleTimeMailFormat(machineName, lastIdleTime, maxIdleTime, timeGap);
					
					// Send Alarm EMail
					try
					{						
						String[] userGroupList = StringUtils.split(eqpDepart, ",");

						for (int i = 0; userGroupList.length > i; i++)
						{
							String userGroupName = userGroupList[i];
							CommonUtil.sendAlarmEmail(userGroupName, "EQPIDLETIME", emailMessage);
						}
					}
					catch (Exception e)
					{
						e.getStackTrace();
					}
					
					//houxk 20210617
					try
					{				
						sendToEm(eqpDepart, emailMessage );
					}
					catch (Exception e)
					{
						log.info("eMobile or WeChat Send Error : " + e.getCause());	
					}
				}
			}
		}
	}

	private String eqpIdleTimeMailFormat(String machineName, String lastIdleTime, String maxIdleTime, String timeGap)
	{
		StringBuilder message = new StringBuilder();
		message.append("<pre>=======Change Machine Lock Flag by Over IdleTime=======</pre>");
		message.append("<pre>=======================================================</pre>");
		message.append("<pre>- EQP ID : ").append(machineName).append("</pre>");
		message.append("<pre>- LastIdleTime : ").append(lastIdleTime).append("</pre>");
		message.append("<pre>- MaxIdleTime : ").append(maxIdleTime).append("</pre>");
		message.append("<pre>- TimeGap : ").append(timeGap).append("</pre>");
		message.append("<pre>=======================================================</pre>");

		return message.toString();
	}

	// Change Machine Lock Flag by Over IdleTime
	private void ChangeMachineLockFlag(EventInfo eventInfo, String machineName, Map<String, String> udfs) throws CustomException
	{
		// Change Machine Lock Flag
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		SetEventInfo setEventInfo = MESMachineServiceProxy.getMachineInfoUtil().setEventInfo(udfs);
		MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);
	}

	public void sendEmail()
	{
		// Required to add sending an email
	}
	
	public void monitorMachineIdleTimeByChamber() throws CustomException, InterruptedException, MalformedURLException
	{
		StringBuilder sql = new StringBuilder();
		sql.append(" SELECT M.MACHINENAME, ");
		sql.append("          M.LASTIDLETIME, ");
		sql.append("          SYSDATE, ");
		sql.append("          ROUND ( (SYSDATE - M.LASTIDLETIME) * 24, 1) AS DIFFTIME, ");
		sql.append("          MS.CHECKIDLETIME, ");
		sql.append("          MS.MAXIDLETIME, ");
		sql.append("          M.MACHINELOCKFLAG, ");
		sql.append("          CASE ");
		sql.append("             WHEN ROUND ( (SYSDATE - M.LASTIDLETIME) * 24, 1) > MAXIDLETIME AND MS.CHECKIDLETIME = 'Y' ");
		sql.append("             THEN ");
		sql.append("                'Over' ");
		sql.append("             ELSE ");
		sql.append("                'More' ");
		sql.append("          END ");
		sql.append("             RESULT ");
		sql.append("     FROM MACHINE M, MACHINESPEC MS,CT_MACHINEIDLEBYCHAMBER C ");//CAIXU
		sql.append("    WHERE     1 = 1 ");
		sql.append("          AND M.MACHINENAME=C.CHAMBERNAME ");
		sql.append("          AND (C.CONTROLSWITCH!='Y' OR C.CONTROLSWITCH IS NULL)  ");
		sql.append("          AND M.MACHINENAME = MS.MACHINENAME ");
		sql.append(" ORDER BY M.MACHINENAME ASC ");
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), new HashMap<String, Object>());
		
		if(sqlResult != null && sqlResult.size() > 0)
		{
			for (Map<String, Object> row : sqlResult)
			{
				if(ConvertUtil.getMapValueByName(row, "RESULT").toString().equals("Over"))
				{
					try
					{
						
						String CurrentTime = TimeStampUtil.getCurrentTime(TimeStampUtil.FORMAT_DEFAULT);
						String timeKey = TimeStampUtil.getCurrentEventTimeKey();
						
						StringBuilder updateSql = new StringBuilder();
						updateSql.append(" UPDATE CT_MACHINEIDLEBYCHAMBER A SET A.CONTROLSWITCH = 'Y',A.LASTEVENTTIME = :LASTEVENTTIME,A.LASTEVENTTIMEKEY = :LASTEVENTTIMEKEY,A.LASTEVENTNAME = 'ChangeSwitchState',A.LASTEVENTUSER = 'MESTimer' WHERE A.CHAMBERNAME = :CHAMBERNAME AND A.IDLEGROUPNAME = 'ChamberIdle' ");
						
						Map<String, Object> updateArgs = new HashMap<>();
						updateArgs.put("CHAMBERNAME", ConvertUtil.getMapValueByName(row, "MACHINENAME"));
						updateArgs.put("LASTEVENTTIME", CurrentTime);
						updateArgs.put("LASTEVENTTIMEKEY", timeKey);
						
						greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(updateSql.toString(), updateArgs);
					}
					catch (Exception e)
					{
						e.getStackTrace();
					}
				}
			}
		}
	}
	
	public void PPIDIdleTimeControlMachine() throws CustomException, InterruptedException, MalformedURLException
	{
		StringBuilder sql = new StringBuilder();
		sql.append("     WITH RECIPEINFO     ");
		sql.append("                          AS (SELECT C.MACHINENAME,     ");
		sql.append("                            C.RECIPENAME,     ");
		sql.append("                            C.LASTEVENTTIMEKEY,     ");
		sql.append("                            C.LASTEVENTUSER,     ");
		sql.append("                            C.LASTEVENTCOMMENT,     ");
		sql.append("                            C.LASTTRACKOUTTIMEKEY, ");
		sql.append("                            MA.MACHINELOCKFLAG,     ");
		sql.append("                            TO_DATE(SUBSTR(C.LASTTRACKOUTTIMEKEY,0,14),'yyyymmddhh24miss') AS LASTTACKOUTTIME,     ");
		sql.append("                            ROUND( ( SYSDATE - TO_DATE(SUBSTR(C.LASTTRACKOUTTIMEKEY,0,14),'yyyymmddhh24miss') ) * 24 , 1 ) AS TIMEDIFF,     ");
		sql.append("                            MS.CHECKIDLETIME,     ");
		sql.append("                            MS.MAXIDLETIME,     ");
		sql.append("                            CASE WHEN MS.CHECKIDLETIME = 'Y' AND ROUND( ( SYSDATE - TO_DATE(SUBSTR(C.LASTTRACKOUTTIMEKEY,0,14),'yyyymmddhh24miss') ) * 24 , 1 ) >= MS.MAXIDLETIME THEN 'TimeOut' ELSE 'NoTimeOut' END RESULT     ");
		sql.append("                       FROM CT_RECIPE C , MACHINESPEC MS , MACHINE MA , ");
		sql.append("                       (SELECT A.ENUMVALUE FROM ENUMDEFVALUE A WHERE A.ENUMNAME = 'PPIDIdleTimeControlMachine' AND A.DEFAULTFLAG = 'Y') M     ");
		sql.append("                      WHERE C.MACHINENAME IN M.ENUMVALUE ");
		sql.append("                      AND MA.MACHINENAME = MS.MACHINENAME ");
		sql.append("                      AND C.LASTTRACKOUTTIMEKEY = (SELECT MAX(A.LASTTRACKOUTTIMEKEY) AS LASTTRACKOUTTIMEKEY FROM CT_RECIPE A WHERE A.MACHINENAME IN M.ENUMVALUE)     ");
		sql.append("                      AND C.MACHINENAME = MS.MACHINENAME),   ");
		sql.append("                             LOCKTIME   ");
		sql.append("                           AS (SELECT M.MACHINENAME,MAX (M.EVENTTIME) AS CHANGELOACKTIME   ");
		sql.append("                               FROM MACHINEHISTORY M   ");
		sql.append("                               WHERE     M.MACHINENAME IN (SELECT MACHINENAME FROM RECIPEINFO)   ");
		sql.append("                               AND M.EVENTNAME = 'ChangeMachineLockFlag'   ");
		sql.append("                               AND M.MACHINELOCKFLAG = 'N'   ");
		sql.append("                               GROUP BY M.MACHINENAME),   ");
		sql.append("                          LOTINFO     ");
		sql.append("                          AS (SELECT P.MACHINENAME,COUNT(P.PORTNAME) AS RUNCOUNT     ");
		sql.append("                       FROM PORT P,     ");
		sql.append("                            DURABLE D,     ");
		sql.append("                            LOT L,     ");
		sql.append("                            TPFOPOLICY TP,     ");
		sql.append("                            POSMACHINE PM,     ");
		sql.append("                            RECIPEINFO R     ");
		sql.append("                      WHERE     P.MACHINENAME IN R.MACHINENAME     ");
		sql.append("                            AND P.MACHINENAME = D.MACHINENAME     ");
		sql.append("                            AND P.PORTNAME = D.PORTNAME     ");
		sql.append("                            AND D.DURABLENAME = L.CARRIERNAME     ");
		sql.append("                            AND TP.CONDITIONID = PM.CONDITIONID     ");
		sql.append("                            AND TP.PRODUCTSPECNAME = L.PRODUCTSPECNAME     ");
		sql.append("                            AND TP.PRODUCTSPECVERSION = L.PRODUCTSPECVERSION     ");
		sql.append("                            AND TP.PROCESSFLOWNAME = L.PROCESSFLOWNAME     ");
		sql.append("                            AND TP.PROCESSFLOWVERSION = L.PROCESSFLOWVERSION     ");
		sql.append("                            AND TP.PROCESSOPERATIONNAME = L.PROCESSOPERATIONNAME     ");
		sql.append("                            AND TP.PROCESSOPERATIONVERSION = L.PROCESSOPERATIONVERSION     ");
		sql.append("                            AND L.LOTPROCESSSTATE = 'RUN'     ");
		sql.append("                            AND L.PRODUCTIONTYPE IN ('P','E')     ");
		sql.append("                            AND P.TRANSFERSTATE = 'Processing'     ");
		sql.append("                            AND PM.MACHINERECIPENAME IN (SELECT R.RECIPENAME FROM CT_RECIPE R WHERE R.MACHINENAME IN (SELECT A.ENUMVALUE FROM ENUMDEFVALUE A WHERE A.ENUMNAME = 'PPIDIdleTimeControlMachine' AND A.DEFAULTFLAG = 'Y'))     ");
		sql.append("                            GROUP BY P.MACHINENAME)     ");
		sql.append("                     SELECT R.* , L.RUNCOUNT  , K.CHANGELOACKTIME , CASE WHEN R.CHECKIDLETIME = 'Y' AND ROUND( ( SYSDATE - K.CHANGELOACKTIME ) * 24 , 1 ) >= R.MAXIDLETIME THEN 'TimeOut' ELSE 'NoTimeOut' END LOCKRESULT    ");
		sql.append("                       FROM RECIPEINFO R , LOTINFO L , LOCKTIME K    ");
		sql.append("                       WHERE 1 = 1     ");
		sql.append("                       AND R.MACHINENAME = L.MACHINENAME(+)   ");
		sql.append("                       AND R.MACHINENAME = K.MACHINENAME(+) ");
		
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), new HashMap<String, Object>());
		
		if(sqlResult != null && sqlResult.size() > 0)
		{
			for (Map<String, Object> row : sqlResult)
			{
				if(ConvertUtil.getMapValueByName(row, "RESULT").toString().equals("TimeOut") && ConvertUtil.getMapValueByName(row, "CHECKIDLETIME").toString().equals("Y") && ConvertUtil.getMapValueByName(row, "MACHINELOCKFLAG").toString().equals("N") && !StringUtils.isNotEmpty(ConvertUtil.getMapValueByName(row, "RUNCOUNT").toString()) && ConvertUtil.getMapValueByName(row, "LOCKRESULT").toString().equals("TimeOut"))
				{
					try
					{
						String eventUser = "MES";
						String eventComment = "Change Machine Lock Flag by PPIDIdleTimeOut. ";
						EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeMachineLockFlag", eventUser, eventComment, null, null);

						Map<String, String> udfs = new HashMap<String, String>();
						udfs.put("MACHINELOCKFLAG", "Y");
						
						String machineName = ConvertUtil.getMapValueByName(row, "MACHINENAME");
						String lastTrackOutTime = ConvertUtil.getMapValueByName(row, "LASTTACKOUTTIME");
						String maxIdleTime = ConvertUtil.getMapValueByName(row, "MAXIDLETIME");
						String timeDiff = ConvertUtil.getMapValueByName(row, "TIMEDIFF");

						eventInfo.setEventComment(eventComment + "lastTrackOutTime: " + lastTrackOutTime + ". LimitTime: " + maxIdleTime + ". timeDiff: " + timeDiff);

						this.ChangeMachineLockFlag(eventInfo, machineName, udfs);
					}
					catch (Exception e)
					{
						e.getStackTrace();
					}
				}
			}
		}
	}
	
	public void sendToEm(String eqpDepart, String messageInfo)
	{
		String[] userList = getUserList(eqpDepart,"EQPIDLETIME");	
		if(userList == null || userList.length ==0) return;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MachineIdleCalcutaionTimer", "MES", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{	
			log.info("OverMachineIdleTime Start Send To Emobile & Wechat");	
						
			String title = "OverMachineIdleTime";
			String detailtitle = "${}CIM系统消息通知";
			String url = "";
			
			StringBuffer info = new StringBuffer();
			info.append("<pre>=======================AlarmInformation=======================</pre>");
			info.append(messageInfo);
			info.append("<pre>=============================End=============================</pre>");			
			
			String message = info.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userList, title, detailtitle, message, url);
									
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userList, title, detailtitle, message, "");
		}
		catch (Exception e)
		{
			log.info("eMobile or WeChat Send Error : " + e.getCause());	
		} 
	}
	
	//AlarmGroup = EQPIDLETIME && EQPDepart
	private String[] getUserList(String eqpDepart, String alarmGroupName)
	{		
		String[] userList = null;
		List<String> sb = new ArrayList<String>();
		
		try 
		{
			if (StringUtils.isNotEmpty(eqpDepart)) 
			{
				List<String> department =  CommonUtil.splitStringDistinct(",",eqpDepart);

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
			log.info("Not Found the Department of "+ eqpDepart);
			log.info(" Failed to send to EMobile, MachineName: " + eqpDepart);
		}
		return userList;			
	}

}
