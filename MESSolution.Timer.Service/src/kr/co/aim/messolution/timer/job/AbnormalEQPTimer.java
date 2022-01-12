package kr.co.aim.messolution.timer.job;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.messolution.extended.object.management.data.AbnormalEQP;

import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

public class AbnormalEQPTimer implements Job, InitializingBean {

    private Log log = LogFactory.getLog(AbnormalEQPTimer.class);

  	@Override
  	public void afterPropertiesSet() throws Exception
  	{
  		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
  	}
  	
  	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException
	{
  		String url = "";
  		String sql = "SELECT * "
				   + " FROM CT_ABNORMALEQP "
				   + " WHERE (SYSDATE-LASTEVENTTIME)*24 > 12 "
  		           + " AND ABNORMALSTATE !='Closed' AND ABNORMALNAME LIKE '%EQP-%' AND SENDFLAG IS NULL ";
				  
		
  		List<Map<String, Object>> resultDataList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, new Object[] {});
		if (resultDataList == null || resultDataList.size() == 0)
		{
			log.info("CT_ABNORMALEQP is no Delaydata.");
			return ;
		}
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("AbnormalEQPDelayInfo Send","SYSTEM","AbnormalEQPDelayInfo Send");
  		
		for (Map<String, Object> resultData : resultDataList) 
		{
			String abnormalname = resultData.get("ABNORMALNAME").toString();
			String abnormalEQPName = resultData.get("ABNORMALEQPNAME").toString();
			String createUser = resultData.get("CREATEUSER").toString();
			String department = resultData.get("DEPARTMENT").toString();
			String startTime = resultData.get("STARTTIME").toString();
			String description = resultData.get("DESCRIPTION").toString();
			String lasteventtime = resultData.get("LASTEVENTTIME").toString();
			String abnormalstate = resultData.get("ABNORMALSTATE").toString();
			String sendflag = "Y";
			String rabnormalstate =abnormalstate + "-Delay" ;
			
			String userList = getUserList(abnormalname);
			List<String> emailList = getEmailList(abnormalname);
			
			try
			{																
				String[] userGroup = userList.split(",");				
				String title = "异常单未处理通知";
				String detailtitle = "${}CIM系统消息通知";
				
				StringBuffer info = new StringBuffer();
				info.append("<pre>=======================NoticeInformation=======================</pre>");
				info.append("<pre>	abnormal："+abnormalname+"</pre>");
				info.append("<pre>	abnormalEQPName："+abnormalEQPName+"</pre>");
				info.append("<pre>	createUser："+createUser+"</pre>");
				info.append("<pre>	department："+department+"</pre>");
				info.append("<pre>	startTime："+startTime+"</pre>");
				info.append("<pre>	description："+description+"</pre>");
				info.append("<pre>	lasteventtime："+lasteventtime+"</pre>");
				info.append("<pre>=============================End=============================</pre>");				
				
				String message = info.toString();
				
				ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userGroup, title, detailtitle, message, url);
				//log.info("eMobile Send Success!");	
				
				StringBuffer weChatInfo = new StringBuffer();
				weChatInfo.append("<pre>======NoticeInformation======</pre>");			
				weChatInfo.append("<pre>	abnormal："+abnormalname+"</pre>");
				weChatInfo.append("<pre> abnormalEQPName："+abnormalEQPName+"</pre>");
				weChatInfo.append("<pre>	createUser："+createUser+"</pre>");
				weChatInfo.append("<pre>	department："+department+"</pre>");
				weChatInfo.append("<pre>	         startTime："+startTime+"</pre>");		
				weChatInfo.append("<pre>	description："+description+"</pre>");
				weChatInfo.append("<pre>	lasteventtime："+lasteventtime+"</pre>");
				weChatInfo.append("<pre>	=======NoticeInfoEnd========</pre>");
				
				String weChatMessage = weChatInfo.toString();
				
				ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userGroup, title, detailtitle, weChatMessage, "");
				//log.info("WeChat Send Success!");	
				
				
				StringBuffer EmailInfo = new StringBuffer();
				EmailInfo.append("<pre>======NoticeInformation======</pre>");		
				EmailInfo.append("<pre>	abnormal："+abnormalname+"</pre>");
				EmailInfo.append("<pre>	abnormalEQPName："+abnormalEQPName+"</pre>");
				EmailInfo.append("<pre>	createUser："+createUser+"</pre>");
				EmailInfo.append("<pre>	department："+department+"</pre>");
				EmailInfo.append("<pre>	startTime："+startTime+"</pre>");		
				EmailInfo.append("<pre>	description："+description+"</pre>");
				EmailInfo.append("<pre>	lasteventtime："+lasteventtime+"</pre>");
				EmailInfo.append("<pre>	=======NoticeInfoEnd========</pre>");
				//EMailInterface ei = new EMailInterface("mail.visionox.com", "25", "1000", "1000");
				//ei.postMail(emailList,  "异常单创建通知", message.toString(), "V3MES", "V3MES@visionox.com", "V3MES", "vis@2019");
				
				AbnormalEQP abnormalData = ExtendedObjectProxy.getAbnormalEQPService().selectByKey(false, new String[]{abnormalname});
				abnormalData.setSendFlag(sendflag);
				abnormalData.setAbnormalState(rabnormalstate);
				abnormalData.setLastEventComment(eventInfo.getEventComment());
				abnormalData.setLastEventName(eventInfo.getEventName());
				abnormalData.setLastEventTime(eventInfo.getEventTime());
				abnormalData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				abnormalData.setLastEventUser(eventInfo.getEventUser());
				abnormalData = ExtendedObjectProxy.getAbnormalEQPService().modify(eventInfo, abnormalData);
				
			}
			catch (Exception e)
			{
				log.info("eMobile or WeChat or email Send Error : " + e.getCause());	
			}				
		}			
	}
  	
	private String getUserList(String abnormalname)
	{
		List<Map<String,Object>> resultList = null;
		String userList = new String();
		StringBuilder sb = new StringBuilder();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT B.DEPARTMENT FROM CT_ALARMUSERGROUP B, (SELECT A.CREATEUSER FROM CT_ABNORMALEQP A   "
				+ "WHERE A.ABNORMALNAME = :ABNORMALNAME ) C WHERE B.USERID=C.CREATEUSER AND B.ALARMGROUPNAME='AbnormalEQP' "
				+ "UNION SELECT D.DEPARTMENT FROM  CT_ABNORMALEQP D WHERE D.ABNORMALNAME = :ABNORMALNAME");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("ABNORMALNAME", abnormalname);
	
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
				String departmentAll= "";

				for(Map<String,Object> Dep :  resultList)
				{
					if(departmentAll.isEmpty())
					{
						departmentAll = Dep.get("DEPARTMENT").toString()+departmentAll;
					}
					else
					{
						departmentAll = Dep.get("DEPARTMENT").toString()+","+departmentAll;
					}
					
				}
				List<String> department =  CommonUtil.splitStringDistinct(",",departmentAll);

				StringBuffer sql1 = new StringBuffer();
				sql1.append("SELECT * FROM CT_ALARMUSERGROUP  WHERE ALARMGROUPNAME = 'AbnormalEQP' AND DEPARTMENT =:DEPARTMENT AND USERLEVEL !='1'");
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
			log.info("Not Found the Department of "+ abnormalname);
			log.info(" Failed to send to EMobile, MachineName: " + abnormalname);
		}
		return userList;
	}
	
	private List<String> getEmailList(String rabnormalName) 
	{
		List<Map<String,Object>> resultList = null;
		StringBuffer sql = new StringBuffer();
		List<String> emailList = new ArrayList<String>();
		sql.append("SELECT B.DEPARTMENT FROM CT_ALARMUSERGROUP B, (SELECT A.CREATEUSER FROM CT_ABNORMALEQP A   WHERE A.ABNORMALNAME = :ABNORMALNAME ) C WHERE B.USERID=C.CREATEUSER AND B.ALARMGROUPNAME = 'AbnormalEQP' UNION SELECT D.DEPARTMENT FROM  CT_ABNORMALEQP D WHERE D.ABNORMALNAME = :ABNORMALNAME");
		
		Map<String, Object> args = new HashMap<String, Object>();
		args.put("ABNORMALNAME", rabnormalName);
	
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
				String departmentAll= "";

				for(Map<String,Object> Dep :  resultList)
				{
					if(departmentAll.isEmpty())
					{
						departmentAll = Dep.get("DEPARTMENT").toString()+departmentAll;
					}
					else
					{
						departmentAll = Dep.get("DEPARTMENT").toString()+","+departmentAll;
					}
					
				}
				List<String> department =  CommonUtil.splitStringDistinct(",",departmentAll);
		
				StringBuffer sql1 = new StringBuffer();
				sql1.append("SELECT * FROM CT_ALARMUSERGROUP  WHERE ALARMGROUPNAME = 'AbnormalEQP' AND DEPARTMENT =:DEPARTMENT AND USERLEVEL !='1'");
				Map<String, Object> args1 = new HashMap<String, Object>();
				
				for(int j = 0; j < department.size(); j++)
				{
					args1.put("DEPARTMENT", department.get(j));
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
			log.info("Not Found the Department of "+ rabnormalName);
			log.info(" Failed to send mail. MachineName: " + rabnormalName);
		}		
		return emailList;
	}
  	
}