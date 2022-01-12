package kr.co.aim.messolution.timer.job;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EMailInterface;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import net.sf.json.JSONObject;

public class ProcessingMonitorTimer implements Job, InitializingBean
{
	
	private static Log log = LogFactory.getLog(ProcessingMonitorTimer.class);
	
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}
	
	
	public void execute(JobExecutionContext arg0) throws JobExecutionException 
	{	
		try
		{
			// Search Lot
			List<Map<String, Object>> dataInfo = this.processingMonitor();
			
			if (dataInfo.size() > 0)
			{
				// Get all MachineName In List
				List<String> machineList = getMachineList(dataInfo);
				if (machineList.size() > 0)
				{
					for (String machineName : machineList)
					{
						// execute message
						StringBuffer messageInfo = executeMessage(dataInfo,machineName);
						
						// find EmailList
						List<String> emailList = getEmailList(machineName);
						
						// sendEmail
						sendEmail(emailList,messageInfo,machineName);
						
						// find EQPPhoneList
						//List<String> phoneList = getEQPPhoneList(machineName);
						
						// execute smsMessage
						//StringBuffer smsMessageInfo = executeSmsMessage(dataInfo,machineName);
						
						// sendMessage
						//GenericServiceProxy.getSMSInterface().sendMessage(smsMessageInfo,phoneList,machineName,"LotOverProcessingTime");
						
						//get UserList
						String userList = getUserList(machineName);	
						
						//SendToEM
						sendToEM(userList,machineName,messageInfo);
						
						//SendToFMB
						EventInfo eventInfo = EventInfoUtil.makeEventInfo("ProcessingMonitor", "MES", "", "", "");				
						MachineSpec machineSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
						sendToFMB(machineSpec.getFactoryName(), machineSpec.getKey().getMachineName(), eventInfo, messageInfo);
						//End
					}
					// Update SendFlag
					updateAlarmMail(dataInfo);
				}
			}
		}
		catch (Exception e)
		{
			if (log.isDebugEnabled())
				log.info(e.getCause());
		}
	}

	
	private void sendToFMB(String factoryName, String machineName, EventInfo eventInfo, StringBuffer messageInfo) 
	{
		Element rootElement = new Element(SMessageUtil.Message_Tag);
		
		Element headerElement = new Element(SMessageUtil.Header_Tag);
			
		headerElement.addContent(new Element("MESSAGENAME").setText("ProductInSubUnitOverTime"));
		headerElement.addContent(new Element("TRANSACTIONID").setText(TimeUtils.getCurrentEventTimeKey()));		
		headerElement.addContent(new Element("ORIGINALSOURCESUBJECTNAME").setText(""));
		headerElement.addContent(new Element("EVENTUSER").setText(eventInfo.getEventUser()));
		headerElement.addContent(new Element("EVENTCOMMENT").setText(eventInfo.getEventComment()));
		headerElement.addContent(new Element("LANGUAGE").setText(""));
		
		rootElement.addContent(headerElement);
		
		Element bodyElement = new Element(SMessageUtil.Body_Tag);
		XmlUtil.addElement(bodyElement, "FACTORYNAME", factoryName);
		XmlUtil.addElement(bodyElement, "MACHINENAME", machineName);
		XmlUtil.addElement(bodyElement, "ALARMINFORMATION", messageInfo.toString());

		rootElement.addContent(bodyElement);
		
		//Send to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(new Document(rootElement));
	}


	private void sendToEM(String userList, String machineName, StringBuffer messageInfo) 
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ProcessingMonitor", "MES", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{																
			String[] userGroup = userList.split(",");				
			String title = "工艺超时报警";
			String detailtitle = "${}CIM系统消息通知";
			String url = "";
			
			StringBuffer info = new StringBuffer();
			info.append("<pre>=======================AlarmInformation=======================</pre>");
			info.append("<pre> MachineName : " + machineName + "</pre>");
			info.append(messageInfo);
			info.append("<pre>=============================End=============================</pre>");			
			
			String message = info.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userGroup, title, detailtitle, message, url);
			//log.info("eMobile Send Success!");	
			
			StringBuffer weChatInfo = new StringBuffer();
			weChatInfo.append("<pre>====AlarmInformation====</pre>");
			weChatInfo.append("<pre> MachineName : " + machineName + "</pre>");
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


	private String getUserList(String machineName) 
	{
		List<Map<String,Object>> resultList = null;
		String userList = new String();
		StringBuilder sb = new StringBuilder();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT MS.RMSDEPARTMENT FROM MACHINESPEC MS WHERE MS.MACHINENAME = :MACHINENAME");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("MACHINENAME", machineName);
	
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
				String departmentAll = resultList.get(0).get("RMSDEPARTMENT").toString();

				List<String> department =  CommonUtil.splitStringDistinct(",",departmentAll);

				StringBuffer sql1 = new StringBuffer();
				sql1.append(
						" SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B, USERPROFILE C WHERE     A.USERID = B.USERID AND B.USERID = C.USERID AND A.ALARMGROUPNAME = 'ProcessingMonitor' AND C.DEPARTMENT = :DEPARTMENT ");
				Map<String, Object> args1 = new HashMap<String, Object>();

				//for (String department1 : department) 
				for(int j = 0; j < department.size(); j++)
				{
					args1.put("DEPARTMENT", department.get(j));
					List<Map<String, Object>> sqlResult1 = GenericServiceProxy.getSqlMesTemplate()
							.queryForList(sql1.toString(), args1);
					
					if (sqlResult1.size() > 0) 
					{
						if(j < department.size() - 1)
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
		}
		catch (Exception e)
		{
			log.info("Not Found the Department of "+ machineName);
			log.info(" Failed to send to EMobile, MachineName: " + machineName);
		}
		return userList;
	}


	private StringBuffer executeSmsMessage(List<Map<String, Object>> dataInfo, String machineName)
	{
		StringBuffer sqlBuffer = new StringBuffer();
		
		for (Map<String, Object> dataInfos : dataInfo)
		{
			if (ConvertUtil.getMapValueByName(dataInfos, "MACHINENAME").equals(machineName))
			{
				
				sqlBuffer.append("LotName: \n" + ConvertUtil.getMapValueByName(dataInfos, "LOTNAME")
						+ "\n OperationName: \n" + ConvertUtil.getMapValueByName(dataInfos, "PROCESSOPERATIONNAME")
						+ "\n  RUNTime: \n" + ConvertUtil.getMapValueByName(dataInfos, "RUNTIME") + "Hour"
						+ "\n  RuleTime: \n" + ConvertUtil.getMapValueByName(dataInfos, "DISPLAYCOLOR") + "Hour");
			}
		}
		return sqlBuffer;
	}


	private List<String> getEQPPhoneList(String machineName) {
		List<Map<String,Object>> resultList = null;
		List<String> phoneList = new ArrayList<String>();
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT MS.RMSDEPARTMENT FROM MACHINESPEC MS WHERE MS.MACHINENAME = :MACHINENAME");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("MACHINENAME", machineName);
	
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
				String departmentAll = resultList.get(0).get("RMSDEPARTMENT").toString();

				List<String> department =  CommonUtil.splitStringDistinct(",",departmentAll);

				StringBuffer sql1 = new StringBuffer();
				sql1.append(
						"SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B WHERE A.USERID = B.USERID  AND A.ALARMGROUPNAME = 'ProcessingMonitor' AND B.DEPARTMENT=:DEPARTMENT");
				Map<String, Object> args1 = new HashMap<String, Object>();

				for (String department1 : department) 
				{
					args1.put("DEPARTMENT", department1);
					List<Map<String, Object>> sqlResult1 = GenericServiceProxy.getSqlMesTemplate()
							.queryForList(sql1.toString(), args1);
					
					if (sqlResult1.size() > 0) 
					{
						for (Map<String, Object> user : sqlResult1)
						{
							String phonenumber = ConvertUtil.getMapValueByName(user, "PHONENUMBER");
							phoneList.add(phonenumber);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			log.info("Not Found the Department of "+ machineName);
			log.info(" Failed to send mail. MachineName: " + machineName);
		}
		
		return phoneList;
	}


	private void updateAlarmMail(List<Map<String, Object>> dataInfo) throws greenFrameDBErrorSignal, CustomException, ParseException 
	{
		StringBuffer sql1 = new StringBuffer();
		sql1.append(
				"SELECT * FROM CT_ALARMMAIL L WHERE L.LOTNAME = :LOTNAME AND L.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME AND L.MACHINENAME = :MACHINENAME AND L.ALARMTYPE = :ALARMTYPE");
		Map<String, Object> args1 = new HashMap<String, Object>();
		
		for ( Map<String, Object> lotList : dataInfo)
		{
			args1.put("LOTNAME", ConvertUtil.getMapValueByName(lotList, "LOTNAME"));
			args1.put("PROCESSOPERATIONNAME", ConvertUtil.getMapValueByName(lotList, "PROCESSOPERATIONNAME"));
			args1.put("MACHINENAME", ConvertUtil.getMapValueByName(lotList, "MACHINENAME"));
			args1.put("ALARMTYPE", "ProcessingMonitor");
			
			List<Map<String, Object>> sqlResult1 = GenericServiceProxy.getSqlMesTemplate()
					.queryForList(sql1.toString(), args1);
			if (sqlResult1.isEmpty())
			{
				String sql = " INSERT INTO CT_ALARMMAIL (TIMEKEY, MACHINENAME, LOTNAME, PROCESSOPERATIONNAME, ALARMTYPE, EMAILFLAG) "
						+ " VALUES ( :TIMEKEY, :MACHINENAME, :LOTNAME, :PROCESSOPERATIONNAME, :ALARMTYPE, :EMAILFLAG) ";
				
				Map<String, String> args = new HashMap<String, String>();
				
				args.put("TIMEKEY", TimeStampUtil.getCurrentEventTimeKey());
				args.put("MACHINENAME", ConvertUtil.getMapValueByName(lotList, "MACHINENAME"));
				args.put("LOTNAME", ConvertUtil.getMapValueByName(lotList, "LOTNAME"));
				args.put("PROCESSOPERATIONNAME", ConvertUtil.getMapValueByName(lotList, "PROCESSOPERATIONNAME"));
				args.put("ALARMTYPE", "ProcessingMonitor");
				args.put("EMAILFLAG", "Y");
				
				GenericServiceProxy.getSqlMesTemplate().update(sql, args);
			}
		}
	}


	// sendEmail
	private void sendEmail(List<String> emailList, StringBuffer messageInfo, String machineName) 
	{
		if(emailList !=null && emailList.size()>0)
		{
			StringBuffer message = new StringBuffer();
			message.append("<pre>===============LotOverProcessingTime===============</pre>");
			message.append("<pre> MachineName : " + machineName + "</pre>");
			message.append(messageInfo);
		 
			try
			{
				EMailInterface ei = new EMailInterface("mail.visionox.com", "25", "1000", "1000");
				ei.postMail(emailList,  " LotOverProcessingTime ", message.toString(), "V3MES", "V3MES@visionox.com", "V3MES", "vis@2019");
			}
			catch (Exception e)
			{
				log.info(" Failed to send mail. MachineName: " + machineName);
			}
		}
		
	}


	// find EmailList
	private List<String> getEmailList(String machineName) 
	{
		List<Map<String,Object>> resultList = null;
		List<String> emailList = new ArrayList<String>();
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT MS.RMSDEPARTMENT FROM MACHINESPEC MS WHERE MS.MACHINENAME = :MACHINENAME");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("MACHINENAME", machineName);
	
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
				String departmentAll = resultList.get(0).get("RMSDEPARTMENT").toString();

				List<String> department =  CommonUtil.splitStringDistinct(",",departmentAll);

				StringBuffer sql1 = new StringBuffer();
				sql1.append(
						" SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B, USERPROFILE C WHERE     A.USERID = B.USERID AND B.USERID = C.USERID AND A.ALARMGROUPNAME = 'ProcessingMonitor' AND C.DEPARTMENT = :DEPARTMENT ");
				Map<String, Object> args1 = new HashMap<String, Object>();

				for (String department1 : department) 
				{
					args1.put("DEPARTMENT", department1);
					List<Map<String, Object>> sqlResult1 = GenericServiceProxy.getSqlMesTemplate()
							.queryForList(sql1.toString(), args1);
					
					if (sqlResult1.size() > 0) 
					{
						for (Map<String, Object> user : sqlResult1)
						{
							String eMail = ConvertUtil.getMapValueByName(user, "EMAIL");
							emailList.add(eMail);
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			log.info("Not Found the Department of "+ machineName);
			log.info(" Failed to send mail. MachineName: " + machineName);
		}
		
		return emailList;
	}


	// execute message
	private StringBuffer executeMessage(List<Map<String, Object>> dataInfo, String machineName) 
	{
		StringBuffer sqlBuffer = new StringBuffer();
		
		for (Map<String, Object> dataInfos : dataInfo)
		{
			if (ConvertUtil.getMapValueByName(dataInfos, "MACHINENAME").equals(machineName))
			{
				sqlBuffer.append("<pre> LotName: " + ConvertUtil.getMapValueByName(dataInfos, "LOTNAME")
						+ "  OperationName: " + ConvertUtil.getMapValueByName(dataInfos, "PROCESSOPERATIONNAME")
						+ "  RUNTime: " + ConvertUtil.getMapValueByName(dataInfos, "RUNTIME") + " Hour "
						+ "  RuleTime: " + ConvertUtil.getMapValueByName(dataInfos, "DISPLAYCOLOR") + " Hour " + "</pre>");
			}
		}
		return sqlBuffer;
	}


	// Get all MachineName In List
	private List<String> getMachineList(List<Map<String, Object>> dataInfo) 
	{
		List<String> machineList = new ArrayList<>();
		
		for ( Map<String, Object> productList : dataInfo)
		{
			
			if (!machineList.contains(ConvertUtil.getMapValueByName(productList, "MACHINENAME")))
			{
				machineList.add(ConvertUtil.getMapValueByName(productList, "MACHINENAME"));
			}
		}
		
		return machineList;
	}


	// Search Product In CT_ComponentMonitor
	public List<Map<String, Object>> processingMonitor()
	{
		/*
		 * Table - EnumDefValue
		 * EnumName: ProcessingMonitor
		 * EnumValue: Operation
		 * Description: Factory
		 * DefaultFlag: Y ( Send Email only once ) , N ( Send Email until componentOutSubUnit )
		 * DisplayColor: timeMonitor
		 * SEQ: ProductSpec(It Means all while SEQ is null)
		 */
		StringBuffer sqlBuffer = new StringBuffer();
		
		sqlBuffer.append(" SELECT ROUND ((SYSDATE - L.LASTLOGGEDINTIME) * 24, 2)     RUNTIME, "
				+ "       L.PROCESSOPERATIONNAME,"
				+ "       L.MACHINENAME,"
				+ "       L.LASTLOGGEDINTIME,"
				+ "       L.FACTORYNAME,"
				+ "       L.PRODUCTSPECNAME,"
				+ "       L.LOTNAME,"
				+ "       L.CARRIERNAME,"
				+ "       L.PRODUCTIONTYPE,"
				+ "       ED.DISPLAYCOLOR"
				+ "  FROM ENUMDEFVALUE ED, LOT L"
				+ " WHERE     L.PROCESSOPERATIONNAME = ED.ENUMVALUE"
				+ "       AND ED.ENUMNAME = 'ProcessingMonitor'"
				+ "       AND L.LOTPROCESSSTATE = 'RUN'"
				+ "       AND L.LOTSTATE = 'Released'"
				+ "       AND L.FACTORYNAME = ED.DESCRIPTION"
				+ "       AND ROUND ((SYSDATE - L.LASTLOGGEDINTIME) * 24, 2) > ED.DISPLAYCOLOR"
				+ "       AND L.PRODUCTSPECNAME ="
				+ "           CASE WHEN ED.SEQ IS NULL THEN L.PRODUCTSPECNAME ELSE ED.SEQ END"
				+ "       AND (   ED.DEFAULTFLAG = 'N'"
				+ "            OR 1 ="
				+ "               (CASE"
				+ "                    WHEN (SELECT COUNT (*)"
				+ "                            FROM CT_ALARMMAIL AM"
				+ "                           WHERE     AM.ALARMTYPE = 'ProcessingMonitor'"
				+ "                                 AND AM.EMAILFLAG = ?"
				+ "                                 AND L.LOTNAME = AM.LOTNAME"
				+ "                                 AND L.PROCESSOPERATIONNAME ="
				+ "                                     AM.PROCESSOPERATIONNAME"
				+ "                                 AND L.MACHINENAME = AM.MACHINENAME) >"
				+ "                         0"
				+ "                    THEN"
				+ "                        2"
				+ "                    ELSE"
				+ "                        1"
				+ "                END))"
				+ " UNION ALL "
				+ " SELECT ROUND ((SYSDATE - L.LASTLOGGEDINTIME) * 24, 2)     RUNTIME, "
				+ "       L.MASKPROCESSOPERATIONNAME PROCESSOPERATIONNAME, "
				+ "       L.MACHINENAME, "
				+ "       L.LASTLOGGEDINTIME, "
				+ "       L.FACTORYNAME, "
				+ "       L.MASKSPECNAME PRODUCTSPECNAME, "
				+ "       L.MASKLOTNAME LOTNAME, "
				+ "       L.CARRIERNAME, "
				+ "       L.PRODUCTIONTYPE, "
				+ "       ED.DISPLAYCOLOR "
				+ "  FROM ENUMDEFVALUE ED, CT_MASKLOT L "
				+ " WHERE     L.MASKPROCESSOPERATIONNAME = ED.ENUMVALUE "
				+ "       AND ED.ENUMNAME = 'ProcessingMonitor' "
				+ "       AND L.MASKLOTPROCESSSTATE = 'RUN' "
				+ "       AND L.MASKLOTSTATE = 'Released' "
				+ "       AND L.FACTORYNAME = ED.DESCRIPTION "
				+ "       AND ROUND ((SYSDATE - L.LASTLOGGEDINTIME) * 24, 2) > ED.DISPLAYCOLOR "
				+ "       AND L.MASKSPECNAME = "
				+ "           CASE WHEN ED.SEQ IS NULL THEN L.MASKSPECNAME ELSE ED.SEQ END "
				+ "       AND (   ED.DEFAULTFLAG = 'N' "
				+ "            OR 1 = "
				+ "               (CASE "
				+ "                    WHEN (SELECT COUNT (*) "
				+ "                            FROM CT_ALARMMAIL AM "
				+ "                           WHERE     AM.ALARMTYPE = 'ProcessingMonitor' "
				+ "                                 AND AM.EMAILFLAG = 'Y' "
				+ "                                 AND L.MASKLOTNAME = AM.LOTNAME "
				+ "                                 AND L.MASKPROCESSOPERATIONNAME = "
				+ "                                     AM.PROCESSOPERATIONNAME "
				+ "                                 AND L.MACHINENAME = AM.MACHINENAME) > "
				+ "                         0 "
				+ "                    THEN "
				+ "                        2 "
				+ "                    ELSE "
				+ "                        1 "
				+ "                END)) ");
		
		List<Map<String, Object>> dataInfo;

		try 
		{
			dataInfo = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), new Object[] {"Y"});
		} 
		catch(Exception ex)
		{
			dataInfo = null;
			log.info(ex.getCause());
		}
		
		return dataInfo;
	}

}
