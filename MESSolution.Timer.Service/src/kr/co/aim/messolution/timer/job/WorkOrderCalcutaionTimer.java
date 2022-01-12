package kr.co.aim.messolution.timer.job;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EMailInterfaceExcel;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Font;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

public class WorkOrderCalcutaionTimer implements Job, InitializingBean  {
	private static Log log = LogFactory.getLog(WorkOrderCalcutaionTimer.class);
	@Override
	public void afterPropertiesSet() throws Exception 
	{
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException 
	{
		if(CommonUtil.getEnumDefValueByEnumName("ProductRequestEmailswitch").isEmpty())
		{
			log.info("ProductRequestEmailswitch Off");
			return;
		}
		
		try
		{
			log.debug("WorkOrderCalcutaionTimer-Start");
			// Get WorkOrderList
			List<Map<String, Object>> dataInfo = this.getWorkOrderList();
			
			ByteArrayOutputStream excel1 = new ByteArrayOutputStream();
			ByteArrayOutputStream excel2 = new ByteArrayOutputStream();
			ByteArrayOutputStream excel3 = new ByteArrayOutputStream();
			
			if(dataInfo != null && !dataInfo.isEmpty())
			{
				List<String> poUserList1 = getUserList(dataInfo, "预警", "1");
				List<String> emailList1 = new ArrayList<String>();
				for (String user : poUserList1)
				{
					emailList1 = getEmailList(user, "1",emailList1);
				}
				excel1 = generateExcel(dataInfo, "预警", poUserList1);
				sendEmailExcel("WoTimer", "<pre> ===============工单超时预警===============</pre>", excel1,"工单超时预警", emailList1);
				
				List<String> poUserList2 = getUserList(dataInfo, "到期", "2");
				List<String> emailList2 = new ArrayList<String>();
				for (String user : poUserList2)
				{
					emailList2 = getEmailList(user, "2", emailList2);
				}
				excel2 = generateExcel(dataInfo, "到期",poUserList2);
				sendEmailExcel("WoTimer", "<pre> ===============工单到期===============</pre>", excel2,"工单到期", emailList2);
				
				List<String> poUserList3 = getUserList(dataInfo, "超期3天", "3");
				List<String> emailList3 = new ArrayList<String>();
				for (String user : poUserList3)
				{
					emailList3 = getEmailList(user, "3", emailList3);
				}
				excel3 = generateExcel(dataInfo, "超期3天", poUserList3);
				sendEmailExcel("WoTimer", "<pre> ===============工单超期3天===============</pre>", excel3,"工单超期3天", emailList3);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		log.debug("WorkOrderCalcutaionTimer-End");
	}

	private List<String> getEmailList(String user, String level, List<String> emailList) 
	{
		List<Map<String,Object>> resultList = null;
		
		StringBuffer sql = new StringBuffer();
		if (level.equals("1"))
		{
			sql.append(" SELECT A.* FROM CT_ALARMUSERGROUP A WHERE A.USERID = :USERID AND A.ALARMGROUPNAME = 'WoTimer' ");
		}
		else if (level.equals("2"))
		{
			sql.append(" SELECT A.* FROM CT_ALARMUSERGROUP DP, CT_ALARMUSERGROUP A WHERE DP.USERID = :USERID AND A.DEPARTMENT = DP.DEPARTMENT AND (A.USERLEVEL = '2' OR A.USERID = :USERID) AND A.ALARMGROUPNAME = DP.ALARMGROUPNAME AND A.ALARMGROUPNAME = 'WoTimer' ");
		}
		else if (level.equals("3"))
		{
			sql.append(" SELECT A1.* FROM CT_ALARMUSERGROUP DP1, CT_ALARMUSERGROUP A1 WHERE DP1.USERID = :USERID AND A1.DEPARTMENT = SUBSTR(DP1.DEPARTMENT,1,INSTR(DP1.DEPARTMENT, '-', 1, 1) - 1) AND A1.USERLEVEL = '3' AND A1.ALARMGROUPNAME = DP1.ALARMGROUPNAME AND A1.ALARMGROUPNAME = 'WoTimer' ");
			sql.append(" UNION ");
			sql.append(" SELECT A2.* FROM CT_ALARMUSERGROUP DP2, CT_ALARMUSERGROUP A2 WHERE DP2.USERID = :USERID AND A2.DEPARTMENT = DP2.DEPARTMENT AND (A2.USERLEVEL = '2' OR A2.USERID = :USERID) AND A2.ALARMGROUPNAME = DP2.ALARMGROUPNAME AND A2.ALARMGROUPNAME = 'WoTimer' ");
		}
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("USERID", user);
		
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		}
		catch(Exception ex)
		{
			resultList = null;
		    log.info(ex.getCause());
		}
		
		if (!resultList.isEmpty() && resultList != null) 
		{
			for (Map<String,Object> row :resultList)
			{
				if (StringUtil.isNotEmpty(ConvertUtil.getMapValueByName(row, "EMAIL")))
					emailList.add(ConvertUtil.getMapValueByName(row, "EMAIL"));
			}
		}
		
		return emailList;
	}

	private void sendEmailExcel(String userGroup, String message, ByteArrayOutputStream sheet, String title,
			List<String> userList) 
	{
		String smtpHostName = "10.71.6.26";
		String smtpPort = "25";
		String connectionTimeout = "1000";
		String socketIOTimeout = "1000";
		StringBuffer sql = new StringBuffer();
		sql.append(
				"SELECT * FROM ENUMDEFVALUE L WHERE L.ENUMNAME = :ENUMNAME");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("ENUMNAME", "V3MESMail_IF");
		
		List<Map<String, Object>> sqlResult = null;
		try 
		{
			sqlResult = GenericServiceProxy.getSqlMesTemplate()
					.queryForList(sql.toString(), args);
		} 
		catch(Exception ex)
		{
			sqlResult = null;
			log.info(ex.getCause());
		}
		
		if (sqlResult != null && sqlResult.size() > 0)
		{
			smtpHostName = ConvertUtil.getMapValueByName(sqlResult.get(0), "ENUMVALUE");
			smtpPort = ConvertUtil.getMapValueByName(sqlResult.get(0), "DESCRIPTION");
			connectionTimeout = ConvertUtil.getMapValueByName(sqlResult.get(0), "DEFAULTFLAG");
			socketIOTimeout = ConvertUtil.getMapValueByName(sqlResult.get(0), "DISPLAYCOLOR");
		}
		
		String fromUserName = "V3MES";
		String fromUserMail = "V3MES@visionox.com";
		String authUserName = "V3MES";
		String authPwd = "vis@2019";
		
		StringBuffer sql1 = new StringBuffer();
		sql1.append(
				"SELECT * FROM ENUMDEFVALUE L WHERE L.ENUMNAME = :ENUMNAME");
		
		Map<String, Object> args1 = new HashMap<String, Object>();
		args1.put("ENUMNAME", "V3MESMail_PWD");
		
		List<Map<String, Object>> sqlResult1 = null;
		try 
		{
			sqlResult1 = GenericServiceProxy.getSqlMesTemplate()
					.queryForList(sql1.toString(), args1);
		} 
		catch(Exception ex)
		{
			sqlResult1 = null;
			log.info(ex.getCause());
		}
		
		if (sqlResult1 != null && sqlResult1.size() > 0)
		{
			fromUserName = ConvertUtil.getMapValueByName(sqlResult1.get(0), "ENUMVALUE");
			fromUserMail = ConvertUtil.getMapValueByName(sqlResult1.get(0), "DESCRIPTION");
			authUserName = ConvertUtil.getMapValueByName(sqlResult1.get(0), "DEFAULTFLAG");
			authPwd = ConvertUtil.getMapValueByName(sqlResult1.get(0), "DISPLAYCOLOR");
		}
		
		userList = getMFGEmailList(userList);
		if (userList.size() > 0)
		{
			try
			{
				EMailInterfaceExcel ei = new EMailInterfaceExcel(smtpHostName, smtpPort, connectionTimeout, socketIOTimeout);
				ei.postMail(userList, title, message, title, sheet, fromUserName, fromUserMail, authUserName, authPwd);					
			}
			catch (Exception e)
			{
				log.error("Failed to send mail.");
			}
		}
	}

	private List<String> getMFGEmailList(List<String> userList) 
	{
		List<Map<String,Object>> resultList = null;
		
		StringBuffer sql = new StringBuffer();
		sql.append(" SELECT A.* FROM CT_ALARMUSERGROUP A WHERE A.ALARMGROUPNAME = 'WoTimer' AND A.DEPARTMENT = :DEPARTMENT ");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("DEPARTMENT", "MFG");
		
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		}
		catch(Exception ex)
		{
			resultList = null;
		    log.info(ex.getCause());
		}
		
		if (!resultList.isEmpty() && resultList != null) 
		{
			for (Map<String,Object> row :resultList)
			{
				if (StringUtil.isNotEmpty(ConvertUtil.getMapValueByName(row, "EMAIL")))
					userList.add(ConvertUtil.getMapValueByName(row, "EMAIL"));
			}
		}
		
		return userList;
	}

	// get UserList
	private List<String> getUserList(List<Map<String, Object>> dataInfo, String type, String level) 
	{
		List<String>poUserList = new ArrayList<>();
		for ( Map<String, Object> row : dataInfo)
		{
			if (!poUserList.contains(ConvertUtil.getMapValueByName(row, "PROWNER")) && ConvertUtil.getMapValueByName(row, "ALARMLEVEL").equals(type))
			{
				poUserList.add(ConvertUtil.getMapValueByName(row, "PROWNER"));
			}
		}
		
		return poUserList;
	}

	private ByteArrayOutputStream generateExcel(List<Map<String, Object>> dataInfo, String type, List<String> poUserList) 
	{
		HSSFWorkbook workbook = new HSSFWorkbook();
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		HSSFSheet sheet = workbook.createSheet("WorkOrderList" + type);
		
		HSSFRow titel = sheet.createRow(0);

		titel.createCell(0).setCellValue("分类");
		titel.createCell(1).setCellValue("工厂");
		titel.createCell(2).setCellValue("工单号");
		titel.createCell(3).setCellValue("工单开立时间");
		titel.createCell(4).setCellValue("工单周期");
		titel.createCell(5).setCellValue("SAP计划结案时间");
		titel.createCell(6).setCellValue("Lot ID");
		titel.createCell(7).setCellValue("CST ID");
		titel.createCell(8).setCellValue("数量");
		titel.createCell(9).setCellValue("站点");
		titel.createCell(10).setCellValue("工单描述");
		
		try
		{
			for (int i = 0; i < dataInfo.size(); i++)
			{
				for (String user : poUserList) 
				{
					if (ConvertUtil.getMapValueByName(dataInfo.get(i), "ALARMLEVEL").equals(type) && ConvertUtil.getMapValueByName(dataInfo.get(i), "PROWNER").equals(user)) 
					{
						HSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
	
						row.createCell(0).setCellValue(ConvertUtil.getMapValueByName(dataInfo.get(i), "ALARMLEVEL"));
						row.createCell(1).setCellValue(ConvertUtil.getMapValueByName(dataInfo.get(i), "FACTORYNAME"));
						row.createCell(2).setCellValue(ConvertUtil.getMapValueByName(dataInfo.get(i), "PRODUCTREQUESTNAME"));
						row.createCell(3).setCellValue(ConvertUtil.getMapValueByName(dataInfo.get(i), "RELEASETIME"));
						row.createCell(4).setCellValue(ConvertUtil.getMapValueByName(dataInfo.get(i), "RULETIME"));
						row.createCell(5).setCellValue(ConvertUtil.getMapValueByName(dataInfo.get(i), "PLANFINISHEDTIME"));
						row.createCell(6).setCellValue(ConvertUtil.getMapValueByName(dataInfo.get(i), "LOTNAME"));
						row.createCell(7).setCellValue(ConvertUtil.getMapValueByName(dataInfo.get(i), "CARRIERNAME"));
						row.createCell(8).setCellValue(ConvertUtil.getMapValueByName(dataInfo.get(i), "PRODUCTQUANTITY"));
						row.createCell(9).setCellValue(ConvertUtil.getMapValueByName(dataInfo.get(i), "PROCESSOPERATIONNAME"));
						row.createCell(10).setCellValue(ConvertUtil.getMapValueByName(dataInfo.get(i), "DESCRIPTION"));
						
						continue;
					} 
				}
			}
			
			for (int i = 0; i < 11; i++)
			{
				sheet.autoSizeColumn(i);
			}
			
			sheet.createFreezePane(0, 1);
			workbook.write(os);
			os.flush();
			os.close();
		}
		catch(Exception e)
		{
			log.info(e);
			os = null;
		}
		return os;
	}

	private List<Map<String, Object>> getWorkOrderList() 
	{
		log.info("Start get WorkOrderList");
		
		StringBuffer sqlBuffer = new StringBuffer();
		
		sqlBuffer.append("WITH                                                                                                ");
		sqlBuffer.append("    WO                                                                                              ");
		sqlBuffer.append("    AS                                                                                              ");
		sqlBuffer.append("        (SELECT PR.PRODUCTREQUESTNAME,                                                              ");
		sqlBuffer.append("                PR.PRODUCTREQUESTTYPE,                                                              ");
		sqlBuffer.append("                PR.FACTORYNAME,                                                                     ");
		sqlBuffer.append("                PS.PRODUCTCODE,                                                                     ");
		sqlBuffer.append("                ROUND (SYSDATE - PR.RELEASETIME, 2)     DURATIONTIME,                               ");
		sqlBuffer.append("                PR.RELEASETIME,                                                                     ");
		sqlBuffer.append("                PR.PLANFINISHEDTIME,                                                                ");
		sqlBuffer.append("                PR.PLANQUANTITY,                                                                    ");
		sqlBuffer.append("                PR.RELEASEDQUANTITY,                                                                ");
		sqlBuffer.append("                PR.SCRAPPEDQUANTITY,                                                                ");
		sqlBuffer.append("                PR.FINISHEDQUANTITY,                                                                ");
		sqlBuffer.append("                E.DISPLAYCOLOR                          AS RULETIME,                                ");
		sqlBuffer.append("                PR.PROWNER                              PROWNER,                                    ");
		sqlBuffer.append("                PR.DESCRIPTION                                                                      ");
		sqlBuffer.append("           FROM PRODUCTREQUEST  PR, PRODUCTSPEC     PS, ENUMDEFVALUE    E                           ");
		sqlBuffer.append("          WHERE     PR.PRODUCTSPECNAME = PS.PRODUCTSPECNAME                                         ");
		sqlBuffer.append("                AND PR.FACTORYNAME = PS.FACTORYNAME                                                 ");
		sqlBuffer.append("                AND E.ENUMNAME = 'ProductRequestEmailTimer'                                         ");
		sqlBuffer.append("                AND PR.PROWNER IS NOT NULL                                                          ");
		sqlBuffer.append("                AND E.ENUMVALUE =                                                                   ");
		sqlBuffer.append("                    CASE                                                                            ");
		sqlBuffer.append("                        WHEN (SELECT COUNT (*)                                                      ");
		sqlBuffer.append("                                FROM ENUMDEFVALUE EM                                                ");
		sqlBuffer.append("                               WHERE     PS.FACTORYNAME || PS.PRODUCTCODE =                         ");
		sqlBuffer.append("                                         EM.ENUMVALUE                                               ");
		sqlBuffer.append("                                     AND EM.ENUMNAME =                                              ");
		sqlBuffer.append("                                         'ProductRequestEmailTimer') >                              ");
		sqlBuffer.append("                             0                                                                      ");
		sqlBuffer.append("                        THEN                                                                        ");
		sqlBuffer.append("                            PS.FACTORYNAME || PS.PRODUCTCODE                                        ");
		sqlBuffer.append("                        ELSE                                                                        ");
		sqlBuffer.append("                            PS.FACTORYNAME                                                          ");
		sqlBuffer.append("                    END                                                                             ");
		sqlBuffer.append("                AND PR.PRODUCTREQUESTSTATE = 'Released'                                             ");
		sqlBuffer.append("                AND PR.PRODUCTREQUESTTYPE IN ('P', 'E')                                             ");
		sqlBuffer.append("                AND ((PR.FACTORYNAME <> 'ARRAY' AND SYSDATE - PR.RELEASETIME >                      ");
		sqlBuffer.append("                    CASE                                                                            ");
		sqlBuffer.append("                        WHEN (SELECT COUNT (*)                                                      ");
		sqlBuffer.append("                                FROM ENUMDEFVALUE EM                                                ");
		sqlBuffer.append("                               WHERE     PS.FACTORYNAME || PS.PRODUCTCODE =                         ");
		sqlBuffer.append("                                         EM.ENUMVALUE                                               ");
		sqlBuffer.append("                                     AND EM.ENUMNAME =                                              ");
		sqlBuffer.append("                                         'ProductRequestEmailTimer') >                              ");
		sqlBuffer.append("                             0                                                                      ");
		sqlBuffer.append("                        THEN                                                                        ");
		sqlBuffer.append("                              (SELECT EM.DISPLAYCOLOR                                               ");
		sqlBuffer.append("                                 FROM ENUMDEFVALUE EM                                               ");
		sqlBuffer.append("                                WHERE     PS.FACTORYNAME || PS.PRODUCTCODE =                        ");
		sqlBuffer.append("                                          EM.ENUMVALUE                                              ");
		sqlBuffer.append("                                      AND EM.ENUMNAME =                                             ");
		sqlBuffer.append("                                          'ProductRequestEmailTimer')                               ");
		sqlBuffer.append("                            - 3                                                                     ");
		sqlBuffer.append("                        WHEN (SELECT COUNT (*)                                                      ");
		sqlBuffer.append("                                FROM ENUMDEFVALUE EM                                                ");
		sqlBuffer.append("                               WHERE     PS.FACTORYNAME = EM.ENUMVALUE                              ");
		sqlBuffer.append("                                     AND EM.ENUMNAME =                                              ");
		sqlBuffer.append("                                         'ProductRequestEmailTimer') >                              ");
		sqlBuffer.append("                             0                                                                      ");
		sqlBuffer.append("                        THEN                                                                        ");
		sqlBuffer.append("                              (SELECT EM.DISPLAYCOLOR                                               ");
		sqlBuffer.append("                                 FROM ENUMDEFVALUE EM                                               ");
		sqlBuffer.append("                                WHERE     PS.FACTORYNAME = EM.ENUMVALUE                             ");
		sqlBuffer.append("                                      AND EM.ENUMNAME =                                             ");
		sqlBuffer.append("                                          'ProductRequestEmailTimer')                               ");
		sqlBuffer.append("                            - 3                                                                     ");
		sqlBuffer.append("                    END) OR (PR.FACTORYNAME = 'ARRAY' AND PR.PLANFINISHEDTIME - SYSDATE < 3)))      ");
		sqlBuffer.append("  SELECT CASE                                                                                       ");
		sqlBuffer.append("             WHEN WO.FACTORYNAME <> 'ARRAY' AND WO.DURATIONTIME BETWEEN WO.RULETIME - 3 AND WO.RULETIME ");
		sqlBuffer.append("             THEN                                                                                   ");
		sqlBuffer.append("                 '预警'                                                                             ");
		sqlBuffer.append("             WHEN WO.FACTORYNAME = 'ARRAY' AND WO.PLANFINISHEDTIME - SYSDATE BETWEEN 0 AND 3        ");
		sqlBuffer.append("             THEN                                                                                   ");
		sqlBuffer.append("                 '预警'                                                                             ");
		sqlBuffer.append("             WHEN WO.FACTORYNAME <> 'ARRAY' AND WO.DURATIONTIME BETWEEN WO.RULETIME AND WO.RULETIME + 3 ");
		sqlBuffer.append("             THEN                                                                                   ");
		sqlBuffer.append("                 '到期'                                                                             ");
		sqlBuffer.append("             WHEN WO.FACTORYNAME = 'ARRAY' AND WO.PLANFINISHEDTIME - SYSDATE BETWEEN -3 AND 0       ");
		sqlBuffer.append("             THEN                                                                                   ");
		sqlBuffer.append("                 '到期'                                                                             ");
		sqlBuffer.append("             ELSE                                                                                   ");
		sqlBuffer.append("                 '超期3天'                                                                          ");
		sqlBuffer.append("         END                                                                                        ");
		sqlBuffer.append("             AS ALARMLEVEL,                                                                         ");
		sqlBuffer.append("         L.PRODUCTREQUESTNAME,                                                                      ");
		sqlBuffer.append("         WO.RELEASETIME,                                                                            ");
		sqlBuffer.append("         WO.RULETIME || '天'                                                                        ");
		sqlBuffer.append("             AS RULETIME,                                                                           ");
		sqlBuffer.append("         WO.PLANFINISHEDTIME,                                                                       ");
		sqlBuffer.append("         L.LOTNAME,                                                                                 ");
		sqlBuffer.append("         WO.FACTORYNAME,                                                                            ");
		sqlBuffer.append("         L.CARRIERNAME,                                                                             ");
		sqlBuffer.append("         L.PRODUCTQUANTITY,                                                                         ");
		sqlBuffer.append("         L.PROCESSOPERATIONNAME,                                                                    ");
		sqlBuffer.append("         WO.DESCRIPTION,                                                                            ");
		sqlBuffer.append("         WO.PROWNER                                                                                 ");
		sqlBuffer.append("    FROM WO, LOT L                                                                                  ");
		sqlBuffer.append("   WHERE     WO.PRODUCTREQUESTNAME = L.PRODUCTREQUESTNAME(+)                                        ");
		sqlBuffer.append("         AND L.LOTSTATE IN ('Created', 'Released')                                                  ");
		sqlBuffer.append("         AND L.FACTORYNAME <> 'POSTCELL'                                                            ");
		sqlBuffer.append("         AND L.PRODUCTQUANTITY <> '0'                                                               ");
		sqlBuffer.append("ORDER BY WO.FACTORYNAME, ALARMLEVEL                                                                 ");
		
		List<Map<String, Object>> dataInfo;

		try 
		{
			dataInfo = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), new Object[] {});
		} 
		catch(Exception ex)
		{
			dataInfo = null;
			log.info(ex.getCause());
		}
		
		log.info("End get WorkOrderList. Size：" + dataInfo.size());
		return dataInfo;
	}
}
