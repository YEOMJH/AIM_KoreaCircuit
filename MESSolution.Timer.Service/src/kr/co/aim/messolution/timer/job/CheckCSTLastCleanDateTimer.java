package kr.co.aim.messolution.timer.job;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EMailInterface;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.DirtyInfo;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class CheckCSTLastCleanDateTimer implements Job, InitializingBean
{
	private static Log log = LogFactory.getLog(CheckCSTLastCleanDateTimer.class);

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}
	public void execute(JobExecutionContext arg0) throws JobExecutionException 
	{	
		try
		{
			CheckCSTLastCleanDate();
		}
		catch (CustomException e)
		{
			if (log.isDebugEnabled())
				log.error(e.errorDef.getLoc_errorMessage());
		}
	}
	private void CheckCSTLastCleanDate()throws CustomException {
		// TODO Auto-generated method stub
		StringBuffer sqlBuffer = new StringBuffer()
				.append("WITH DUEDATEDURABLELIST AS(                                                                        	    		                    \n")								
				.append("SELECT D.DURABLENAME,(D.LASTCLEANTIME+25)CheckTime,(D.LASTCLEANTIME+28)WARINGTIME,D.LASTCLEANTIME,D.DEPARTMENT,SYSDATE CURRENTTIME 	\n")
				.append("FROM DURABLE D WHERE 1=1         					   				                                                                    \n")
				.append("     AND D.DURABLETYPE IN( 'OLEDGlassCST','TPGlassCST','SheetCST')                                                                     \n")
				.append("     AND D.DEPARTMENT IS NOT NULL                                                          	                    		            \n")
				.append("     AND D.DURABLECLEANSTATE NOT IN ('Dirty','Repairing')                                                                            	\n")
				.append("     AND D.DURABLESTATE <> 'Scrapped')                                                            				                        \n")
				.append("SELECT * FROM (                		  			   				                                                                    \n")
				.append("        SELECT A.DURABLENAME,A.LASTCLEANTIME ,A.DEPARTMENT,               		  								   				        \n")
				.append("        CASE WHEN A.CURRENTTIME > A.CheckTime AND A.CURRENTTIME<A.WARINGTIME   THEN 'WARNING'		                                    \n")
				.append("             WHEN A.CURRENTTIME> A.WARINGTIME THEN 'WARNED'                                                                            \n")
				.append("             ELSE ''                                                                                           					    \n")
				.append("END AS DURABLENULL 		                                                                                                            \n")
				.append("FROM DUEDATEDURABLELIST A WHERE 1=1 																				    		        \n")
				.append(")R  WHERE DURABLENULL IN ( 'WARNED','WARNING') ORDER BY DURABLENULL DESC, DEPARTMENT DESC                                              \n");
        Object[] bindArray = new Object[0];
		
		List<ListOrderedMap> result; 
		try
		{
			result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlBuffer.toString(), bindArray);
		}
		catch (FrameworkErrorSignal fe)
		{
			result = null;
			throw new CustomException("SYS-9999", fe.getMessage());
			//return;
		}
		EventInfo eventInfo = null;
		String department1 = null;
		String department2 = null;
		List<Object> CSTList = new ArrayList<Object>();
		List<ListOrderedMap> result1 = new ArrayList<ListOrderedMap>();	
		List<ListOrderedMap> result2 = new ArrayList<ListOrderedMap>();
		String warning  = "WARNING";//��ȡ25�쵽28���
		String warned  = "WARNED";//28�����ϵ�
		for(ListOrderedMap row : result)
		{
			if(warning.equals(CommonUtil.getValue(row, "DURABLENULL")))
			{
				result1.add(row);
			}
			if(warned.equals(CommonUtil.getValue(row, "DURABLENULL")))
			{
				result2.add(row);
			}
		}
		for (ListOrderedMap row1 : result1)
		{
			 eventInfo = EventInfoUtil.makeEventInfo("CheckLastCleanDate", "MES", "Check CST Last Clean Date Timer", null, null);
			 String durableName = CommonUtil.getValue(row1, "DURABLENAME");
			 if(department1==null)
			 {
				 department1 = CommonUtil.getValue(row1, "DEPARTMENT");
				 CSTList.add(durableName);
			 }
			 else
			 {
			 //ͬһ���ŵĿ�ϻ����addCSTname
				 if (department1.equals(CommonUtil.getValue(row1, "DEPARTMENT")))
				 {
					 CSTList.add(durableName);
				 }
				 else
				 {
		             SendEmailForCSTdeparment(department1,CSTList,"1");
					 //���Ų���ȵ���ղ��ź� CSTList�ٸ�ֵ
					 department1 = CommonUtil.getValue(row1, "DEPARTMENT");
					 CSTList.clear();
					 CSTList.add(durableName);
				 }
			 }
		}
		try
		{
		    SendEmailForCSTdeparment(department1,CSTList,"1");//��N��}
		}
		catch (Exception e)
		{
			log.error("Failed to send mail.");
		}
		 for (ListOrderedMap row2 : result2)
		{
			 eventInfo = EventInfoUtil.makeEventInfo("CheckLastCleanDate", "MES", "Check CST Last Clean Date Timer", null, null);
			 String durableName = CommonUtil.getValue(row2, "DURABLENAME");
			 //ͬһ���ŵĿ�ϻ����addCSTname
			 
			 if(department2==null)
			 {
				 department2 = CommonUtil.getValue(row2, "DEPARTMENT");
				 CSTList.clear();
				 CSTList.add(durableName);
			 }
			 else
			 {
			 //ͬһ���ŵĿ�ϻ����addCSTname
				 if (department2.equals(CommonUtil.getValue(row2, "DEPARTMENT")))
				 {
					 CSTList.add(durableName);
				 }
				 else
				 {
		             SendEmailForCSTdeparment(department2,CSTList,"1");
					 //���Ų���ȵ���ղ��ź� CSTList�ٸ�ֵ
					 department2 = CommonUtil.getValue(row2, "DEPARTMENT");
					 CSTList.clear();
					 CSTList.add(durableName);
				}
			}
		}
		 try
		 {
			 SendEmailForCSTdeparment(department2,CSTList,"2");//��N��
		 }
		 catch (Exception e)
		 {
			log.error("Failed to send mail.");
		 }
		 
	}
	private void SendEmailForCSTdeparment(String department, List<Object> CSTList,String level)throws CustomException
	{
		List<String> emailList = getEmailList(department,level);
		String message1 = "<pre>Dear All</pre>";
		String message2 = "<pre>"+"  本部门（"+department+"）卡匣编号:";
		String message3  = "";
		String message4  ="</pre>";
		if(CSTList.size()>0)
		{
			for(int i = 0; i < CSTList.size(); i++)
			{
				message2 = message2+" "+CSTList.get(i).toString();
			}
		}
		if(level.equals("1"))
		{
			message3  = "</pre><pre>请注意卡匣的上一次清洗时间，这些卡匣已经超过25天未进行清洗 </pre>";
		}
		else
		{
			message3  = "</pre><pre>请注意卡匣的上一次清洗时间，这些卡匣已经超过28天未进行清洗</pre> ";
		}
		String message  = ""; 
        message = message1+message2+message4+message3;
		if (emailList.size() > 0&&!department.isEmpty())
		{
			try
			{
				EMailInterface ei = new EMailInterface("mail.visionox.com", "25", "1000", "1000");
				ei.postMail(emailList,  " CST LastCleanTime warning ", message, "V3MES", "V3MES@visionox.com", "V3MES", "vis@2019");
				//ei.postMail(emailList,  " CST LastCleanTime warning ", message, "V0042758", "jinlj@visionox.com", "V0042758", "vis!123456");
		    }
			catch (Exception e)
			{
				log.error("Failed to send mail.");
				//EMailInterface ei = new EMailInterface("mail.visionox.com", "25", "1000", "1000");
				//List<String> MESemailList = new ArrayList<String>();
				//ei.postMail(emailList,  " Failed to send mail. ", message, "V3MES", "V3MES@visionox.com", "V3MES", "vis@2020");
			}
			
			//houxk 20210611			
			try
			{				
				sendToEm(department, level, message);
			}
			catch (Exception e)
			{
				log.info("eMobile or WeChat Send Error : " + e.getCause());	
			}
		}	
	}
	private List<String> getEmailList(String department,String level) {
		List<String> emailList = new ArrayList<String>();
		StringBuffer sql = new StringBuffer();
		Map<String, Object> args = new HashMap<String, Object>();
		
		if(level.equals("1"))
		{
			sql.append("SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B WHERE A.USERID = B.USERID AND A.USERLEVEL =:USERLEVEL AND A.ALARMGROUPNAME = 'CSTWARNING' AND B.DEPARTMENT=:DEPARTMENT");
			args.put("DEPARTMENT", department);
			args.put("USERLEVEL", level);
		}
		else
		{
			sql.append("SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B WHERE A.USERID = B.USERID  AND A.ALARMGROUPNAME = 'CSTWARNING' AND B.DEPARTMENT=:DEPARTMENT");
			args.put("DEPARTMENT", department);
		}
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);
		if(sqlResult.size()>0)
		{
			for (Map<String, Object> user : sqlResult)
			{
				String eMail = ConvertUtil.getMapValueByName(user, "EMAIL");
				emailList.add(eMail);
			}
		}
		return emailList;
	}
	
	public void sendToEm(String department, String level, String message)
	{
		String[] userList = getUserList(department,level);	
		if(userList == null || userList.length ==0) return;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CheckCSTLastCleanDateTimer", "MES", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{	
			log.info("CheckCSTLastCleanDateTimer Start Send To Emobile & Wechat");	
						
			String title = "CST LastCleanTime warning";
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
	//department, alarmGroup = CSTWARNING
	private String[] getUserList(String department, String level)
	{
		StringBuffer sql = new StringBuffer();
		Map<String, Object> args = new HashMap<String, Object>();
		
		if(level.equals("1"))
		{
			sql.append("SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B WHERE A.USERID = B.USERID AND A.USERLEVEL =:USERLEVEL AND A.ALARMGROUPNAME = 'CSTWARNING' AND B.DEPARTMENT=:DEPARTMENT");
			args.put("DEPARTMENT", department);
			args.put("USERLEVEL", level);
		}
		else
		{
			sql.append("SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B WHERE A.USERID = B.USERID  AND A.ALARMGROUPNAME = 'CSTWARNING' AND B.DEPARTMENT=:DEPARTMENT");
			args.put("DEPARTMENT", department);
		}
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if(sqlResult ==null || sqlResult.size()==0) return null;
		
		return CommonUtil.makeListBySqlResult(sqlResult, "USERID").toArray(new String[] {});
	}
}
