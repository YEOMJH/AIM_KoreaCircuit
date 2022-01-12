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

import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EMailInterface;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class OrganicExpirationTimer implements Job, InitializingBean{
	private static Log log = LogFactory.getLog(OrganicExpirationTimer.class);
	
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
	   if(preValidation()==false) return;
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		String sql = " SELECT  B.CONSUMABLENAME , B.CONSUMABLETYPE ,B.CONSUMABLESPECNAME,B.BATCHNO, B.EXPIRATIONDATE, B.QUANTITY, B.CONSUMABLESTATE "
				  +" FROM CONSUMABLESPEC A, CONSUMABLE B "
				  +" WHERE A.FACTORYNAME = B.FACTORYNAME "
				  +" AND A.CONSUMABLESPECNAME = B.CONSUMABLESPECNAME "
				  +" AND A.CONSUMABLESPECVERSION = B.CONSUMABLESPECVERSION"
				  +" AND A.CONSUMABLETYPE = B.CONSUMABLETYPE"
				  +" AND A.CONSUMABLETYPE IN ('Organic','InOrganic') "
				  +" AND B.CONSUMABLESTATE != :CONSUMABLESTATE "
				  +" AND  SYSDATE <  B.EXPIRATIONDATE "
				  +" AND  (B.EXPIRATIONDATE - SYSDATE)*24 < 600";				  
		
		Map<String,Object> bindMap = new HashMap<>();
		bindMap.put("CONSUMABLESTATE", constMap.Cons_NotAvailable);
		
		List<Map<String,Object>> resultList = null;
		try {
			
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		} catch (Exception ex) 
		{
           log.debug(ex.getCause());
		}	
		if (resultList == null || resultList.size()==0) {
			log.info("");
			return;
		}
		
		try
		{
			SendEmailForEVAdeparment(resultList);
		}
		catch (Exception e)
		{
			log.error("Failed to send mail.");
		}
				
	}
	
	private void SendEmailForEVAdeparment(List<Map<String,Object>> resultList)
	{
		// TODO Auto-generated method stub
		String message = "";
		String message1 = "<pre>==========================AlarmInformation==================================</pre>";
		String message2 = "<pre>==================以下物料剩余有效期少于25天，请及时处理======================</pre>";
		String message3  = "";
		String message4  ="<pre>===============================End==========================================</pre>";
		
		for(int i = 0; i < resultList.size(); i++)
		{
			message3 += "<pre>物料类型:"+ConvertUtil.getMapValueByName(resultList.get(i), "CONSUMABLETYPE")+",物料号:"+ConvertUtil.getMapValueByName(resultList.get(i), "CONSUMABLESPECNAME")+",批次号:"+ConvertUtil.getMapValueByName(resultList.get(i), "BATCHNO")+",物料ID:"+ConvertUtil.getMapValueByName(resultList.get(i), "CONSUMABLENAME")+",数量:"+ConvertUtil.getMapValueByName(resultList.get(i), "QUANTITY")+"G,有效期:"+ConvertUtil.getMapValueByName(resultList.get(i), "EXPIRATIONDATE")+", 物料状态:"+ConvertUtil.getMapValueByName(resultList.get(i), "CONSUMABLESTATE")+";</pre>";
		}
							
		message = message1+message2+message3+message4;
		
		List<String> emailList = new ArrayList<String>();
		
		String department = "EVA";
		String alarmGroup = "OverExpiration";

		StringBuffer sql1 = new StringBuffer();
		sql1.append(
				"SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B WHERE A.USERID = B.USERID  AND A.ALARMGROUPNAME = :ALARMGROUPNAME AND B.DEPARTMENT=:DEPARTMENT");
		Map<String, Object> args1 = new HashMap<String, Object>();

		args1.put("DEPARTMENT", department);
		args1.put("ALARMGROUPNAME", alarmGroup);
		
		List<Map<String, Object>> sqlResult1 = GenericServiceProxy.getSqlMesTemplate()
				.queryForList(sql1.toString(), args1);
		try 
		{
			if (sqlResult1.size() > 0) 
			{
				for (Map<String, Object> user : sqlResult1)
				{
					String eMail = ConvertUtil.getMapValueByName(user, "EMAIL");
					emailList.add(eMail);
				}
			}
		}
		catch (Exception e)
		{
			log.error("Not Found the Department of "+ "EVA");
			log.error("Failed to send mail.");
		}						
		
		if (emailList != null && emailList.size() > 0)
		{
			try
			{
				EMailInterface ei = new EMailInterface("mail.visionox.com", "25", "1000", "1000");
				ei.postMail(emailList,  " Over Remain Expiration ", message.toString(), "V3MES", "V3MES@visionox.com", "V3MES", "vis@2019");
				//ei.postMail(emailList,  " Lot Fail TrackIn ", message.toString(), "V0042748", "houxk@visionox.com", "V0042748", "a970225!!!");
			}
			catch (Exception e)
			{
				log.error("Failed to send mail.");
			}
			//houxk 20210610		
			try
			{				
				sendToEm(department, alarmGroup, message);
			}
			catch (Exception e)
			{
				log.info("eMobile or WeChat Send Error : " + e.getCause());	
			}
		}
	}	
	
	private boolean preValidation ()
	{
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		boolean returnFlag = false;
		
		//check MailInfo exist
		String sql = " SELECT DECODE(NVL(COUNT(1),0),0,'TRUE','FALSE') EXITFLAG "
				   + " FROM CT_ALARMUSER A, CT_ALARMUSERGROUP B "
				   + " WHERE B.ALARMGROUPNAME = :ALARMGROUPNAME "
				   + " AND A.USERID = B.USERID "
				   + " AND A.DEPARTMENT=:DEPARTMENT";
		
		Map<String,Object> bindMap = new HashMap<>();
		bindMap.put("ALARMGROUPNAME", "OverExpiration");
		bindMap.put("DEPARTMENT", "EVA");
		
		List<Map<String,Object>> resultList = null;
		try {
			
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

		} catch (Exception ex) 
		{
           log.debug(ex.getCause());
		}
		
		if(resultList==null || ConvertUtil.getMapValueByName(resultList.get(0), "EXITFLAG") == "TRUE") 
		{
			log.info("");
			return returnFlag;
		}				
		
		return true;
	}
	
	public void sendToEm(String department, String alarmGroup, String message)
	{
		String[] userList = getUserList(department,alarmGroup);	
		if(userList == null || userList.length ==0) return;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("OrganicExpirationTimer", "MES", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{	
			log.info("OrganicExpirationTimer Start Send To Emobile & Wechat");	
						
			String title = "蒸镀物料有效期监控预警";
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
	//department = EVA, alarmGroup = OverExpiration
	private String[] getUserList(String department, String alarmGroup)
	{
		String sql = " SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B  "
				   + " WHERE A.USERID = B.USERID  "
				   + " AND A.ALARMGROUPNAME = :ALARMGROUPNAME AND B.DEPARTMENT=:DEPARTMENT";
		
		List<Map<String, Object>> resultList = null;
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new String[] { alarmGroup, department});
		}
		catch (Exception ex)
		{
			log.info(ex.getCause());
		}

		if(resultList ==null || resultList.size()==0) return null;
		
		return CommonUtil.makeListBySqlResult(resultList, "USERID").toArray(new String[] {});
	}

}
