package kr.co.aim.messolution.timer.job;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Timestamp;
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
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;
public class OperationChangedReport implements Job, InitializingBean {
	private static Log log = LogFactory.getLog(OperationChangedReport.class);
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
			monitorOperation();
		}
		catch (CustomException e)
		{
			if (log.isDebugEnabled())
				log.error(e.errorDef.getLoc_errorMessage());
		}
	}
	private void monitorOperation()  throws CustomException 
	{
		// TODO Auto-generated method stub
		log.info("OperationSpecChangedReport start by Timer");
		StringBuffer sql = new StringBuffer(); 
		sql.append("SELECT PC.PRODUCTSPECNAME, PS.FACTORYNAME,P1.PROCESSFLOWNAME, P1.NODEATTRIBUTE1 AS PROCESSOPERATIONNAME,P1.NODEATTRIBUTE2 AS PROCESSOPERATIONVERSION, ");
		sql.append("   P1.EVENTNAME,P1.EVENTTIME,P1.EVENTUSER,PS.CREATETIME,PS.CREATEUSER,  ");
		sql.append("   PS.DESCRIPTION,PS.PROCESSOPERATIONTYPE,PS.PROCESSOPERATIONGROUP ");
		sql.append("  FROM NODEHISTORY P1,  PROCESSOPERATIONSPEC PS, PRODUCTSPEC PC,   ");
		sql.append("       (  SELECT PH.NODEATTRIBUTE1, MAX (PH.TIMEKEY) TIMEKEY ");
		sql.append("            FROM NODEHISTORY PH ");
		sql.append("           WHERE TIMEKEY BETWEEN TO_CHAR (SYSDATE - 1, 'YYYYMMDD')||'200000' ");
		sql.append("                              AND TO_CHAR (SYSDATE, 'YYYYMMDD') ||'200000' ");
		sql.append("         GROUP BY PH.NODEATTRIBUTE1) P2 ");
		sql.append("  WHERE P1.NODEATTRIBUTE1 = P2.NODEATTRIBUTE1  ");
		sql.append("    AND P1.TIMEKEY = P2.TIMEKEY ");
		sql.append("    AND PS.PROCESSOPERATIONGROUP <> 'MQC' ");
		sql.append("    AND P1.EVENTNAME IN ('Create','Remove')  ");
		sql.append("    AND P1.NODEATTRIBUTE1 = PS.PROCESSOPERATIONNAME(+) ");
		sql.append("    AND P1.PROCESSFLOWNAME = PC.PROCESSFLOWNAME(+) ");
		sql.append("    AND P1.NODEATTRIBUTE2 = PS.PROCESSOPERATIONVERSION(+) ");
		Object[] bindArray = new Object[0];
		
		List<ListOrderedMap> mesResult;
		
		try
		{
			GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
			mesResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindArray);
			log.info("Get OperationSpecChangedReport Result Rows Count: " + mesResult.size());
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		}
		catch (FrameworkErrorSignal fe)
		{
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			mesResult = null;
			throw new CustomException("SYS-9999", fe.getMessage());
		}
		if(mesResult != null && mesResult.size() > 0)
		{
			String message = "<pre> ===============OperationChangedReport===============</pre>";
	  	  	try
	  	  	{
				ByteArrayOutputStream baos = sendMailWithExcel(mesResult);
				CommonUtil.sendAlarmEmail("OperationChanged", message, baos);	
			} 
	  	  	catch (IOException e)
	  	  	{
				//CUSTOM-0018: Create Excel Fail:{0}
				throw new CustomException("CUSTOM-0018", e.getMessage());
			}
	  	  	
	  	  	//houxk 20210617
			try
			{				
				sendToEm("OperationChanged", message);
			}
			catch (Exception e)
			{
				log.info("eMobile or WeChat Send Error : " + e.getCause());	
			}
	  	  	
		}
		
	}
	private ByteArrayOutputStream sendMailWithExcel(List<ListOrderedMap> mesResult)  throws IOException {
		// TODO Auto-generated method stub
		
		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("Operation");
		String[] titel = {"FactoryName","ProductSpecName", "ProcessOperationName", "ProcessOperationVersionName", "Description", "EventName", "EventUser", "CreateTime",
				  "CreateUser", "ProcessOperationType", "ProcessOperationGroup", "EventTime","ProcessFlowName"};
		try
		{
				
			HSSFRow row = sheet.createRow(0);
			for (int i = 0; i < titel.length; i++)
			{
				row.createCell(i).setCellValue(titel[i]);
			}
			for (int i = 0; i < mesResult.size(); i++)
			{
				row = sheet.createRow(i+1);
				row.createCell(0).setCellValue(mesResult.get(i).get("FACTORYNAME").toString());
				if (mesResult.get(i).get("PRODUCTSPECNAME") != null)
				{
					row.createCell(1).setCellValue(mesResult.get(i).get("PRODUCTSPECNAME").toString());
				}
				row.createCell(2).setCellValue(mesResult.get(i).get("PROCESSOPERATIONNAME").toString());
				row.createCell(3).setCellValue(mesResult.get(i).get("PROCESSOPERATIONVERSION").toString());
				if (mesResult.get(i).get("DESCRIPTION") != null)
				{
					row.createCell(4).setCellValue(mesResult.get(i).get("DESCRIPTION").toString());
				}
				if (mesResult.get(i).get("EVENTNAME") != null)
				{
					row.createCell(5).setCellValue(mesResult.get(i).get("EVENTNAME").toString());
				}
				if (mesResult.get(i).get("EVENTUSER") != null)
				{
					row.createCell(6).setCellValue(mesResult.get(i).get("EVENTUSER").toString());
				}
				if (mesResult.get(i).get("CREATETIME") != null)
				{
					row.createCell(7).setCellValue(mesResult.get(i).get("CREATETIME").toString());
				}
				if (mesResult.get(i).get("CREATEUSER") != null)
				{
					row.createCell(8).setCellValue(mesResult.get(i).get("CREATEUSER").toString());
				}
				if (mesResult.get(i).get("PROCESSOPERATIONTYPE") != null)
				{
					row.createCell(9).setCellValue(mesResult.get(i).get("PROCESSOPERATIONTYPE").toString());
				}
				if (mesResult.get(i).get("PROCESSOPERATIONGROUP") != null)
				{
					row.createCell(10).setCellValue(mesResult.get(i).get("PROCESSOPERATIONGROUP").toString());
				}
				if (mesResult.get(i).get("EVENTTIME") != null)
				{
					row.createCell(11).setCellValue(mesResult.get(i).get("EVENTTIME").toString());
				}
				if (mesResult.get(i).get("PROCESSFLOWNAME") != null)
				{
					row.createCell(12).setCellValue(mesResult.get(i).get("PROCESSFLOWNAME").toString());
				}
			}//dan yuan ge chang du she zhi
			for (int i = 0; i <= titel.length; i++)
			{
				sheet.autoSizeColumn(i, true);
			}
			sheet.createFreezePane(0, 1);
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			workbook.write(os);
			os.flush();
			os.close();

			return os;
			
			
		}
		catch(Exception ex)
		{
			throw new IOException(ex.getMessage());
		}
		
	}
	
	public void sendToEm(String alarmGroupName, String messageInfo)
	{
		String userList[] = getUserList(alarmGroupName);	
		if(userList == null || userList.length ==0) return;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("OperationChangedReport", "MES", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{	
			log.info("OperationChangedReport Start Send To Emobile & Wechat");	
						
			String title = "OperationChangedReport";
			String detailtitle = "${}CIM系统消息通知";
			String url = "";
			
			StringBuffer info = new StringBuffer();
			info.append(messageInfo);
			info.append("<pre>====================详情请见邮件==================</pre>");					
			String message = info.toString();			
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userList, title, detailtitle, message, url);
									
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userList, title, detailtitle, message, "");
		}
		catch (Exception e)
		{
			log.info("eMobile or WeChat Send Error : " + e.getCause());	
		} 
	}
	//AlarmGroup = OperationChanged
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
