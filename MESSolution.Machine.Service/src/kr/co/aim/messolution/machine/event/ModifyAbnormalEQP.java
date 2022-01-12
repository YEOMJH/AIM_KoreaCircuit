package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AbnormalEQP;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class ModifyAbnormalEQP extends SyncHandler { 
	private static Log log = LogFactory.getLog(ModifyAbnormalEQP.class);
	@Override	
	public Object doWorks(Document doc) throws CustomException
	{
		String abnormalName = SMessageUtil.getBodyItemValue(doc, "ABNORMALNAME", true);
		String department = SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true);
		String abnormalEQPName = SMessageUtil.getBodyItemValue(doc, "ABNORMALEQPNAME", true);
		String startTime = SMessageUtil.getBodyItemValue(doc, "STARTTIME", true);
		String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", true);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String abnormalType = SMessageUtil.getBodyItemValue(doc, "ABNORMALTYPE", true);
		String productNameList = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAMELIST", false);
		
		Timestamp tStartTime = TimeUtils.getTimestamp(startTime);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyAbnormalEQP", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		
		AbnormalEQP abnormalData = ExtendedObjectProxy.getAbnormalEQPService().selectByKey(false, new String[]{abnormalName});
		
		if(!abnormalData.getAbnormalState().equalsIgnoreCase("Created"))
		{
			log.info("abnormalState is not Created");
			
			//CUSTOM-0027:abnormalState is not Created
			throw new CustomException("CUSTOM-0027");
		}
		
		/*if (!StringUtil.equals(abnormalType, abnormalData.getAbnormalType()))
		{
			String sql = "UPDATE CT_ABNORMALEQP SET ABNORMALNAME = :ABNORMALNAME  WHERE ABNORMALNAME = :OLDABNORMALNAME";
			
			String newAbnormalName = abnormalType + abnormalData.getAbnormalName().substring(3, abnormalName.length());
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("ABNORMALNAME", newAbnormalName);
			bindMap.put("OLDABNORMALNAME", abnormalData.getAbnormalName());
			
			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
			abnormalData = ExtendedObjectProxy.getAbnormalEQPService().selectByKey(false, new String[]{newAbnormalName});
		}*/
		
		abnormalData.setDepartment(department);
		abnormalData.setAbnormalEQPName(abnormalEQPName);
		abnormalData.setStartTime(tStartTime);
		abnormalData.setDescription(description);
		abnormalData.setLotName(lotName);
		abnormalData.setProductNameList(productNameList);
		abnormalData.setAbnormalType(abnormalType);	
		abnormalData.setLastEventComment(eventInfo.getEventComment());
		abnormalData.setLastEventName(eventInfo.getEventName());
		abnormalData.setLastEventTime(eventInfo.getEventTime());
		abnormalData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		abnormalData.setLastEventUser(eventInfo.getEventUser());
		
		abnormalData = ExtendedObjectProxy.getAbnormalEQPService().modify(eventInfo, abnormalData);	
		
		//start send message
		String rabnormalName =abnormalData.getAbnormalName();
		String userList = getUserList(rabnormalName);
		String createUser = eventInfo.getEventUser();
		List<String> emailList = getEmailList(rabnormalName);
		
		try
		{																
			String[] userGroup = userList.split(",");				
			String title = "异常单修改通知";
			String detailtitle = "${}CIM系统消息通知";
			
			StringBuffer info = new StringBuffer();
			info.append("<pre>=======================NoticeInformation=======================</pre>");
			info.append("<pre>	abnormal："+rabnormalName+"</pre>");
			info.append("<pre>	abnormalEQPName："+abnormalEQPName+"</pre>");
			info.append("<pre>	modifyUser："+createUser+"</pre>");
			info.append("<pre>	department："+department+"</pre>");
			info.append("<pre>	startTime："+startTime+"</pre>");
			info.append("<pre>	description："+description+"</pre>");
			info.append("<pre>=============================End=============================</pre>");				
			
			String message = info.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userGroup, title, detailtitle, message, "");
			//log.info("eMobile Send Success!");	
			
			StringBuffer weChatInfo = new StringBuffer();
			weChatInfo.append("<pre>======NoticeInformation======</pre>");			
			weChatInfo.append("<pre>	abnormal："+rabnormalName+"</pre>");
			weChatInfo.append("<pre> abnormalEQPName："+abnormalEQPName+"</pre>");
			weChatInfo.append("<pre>	modifyUser："+createUser+"</pre>");
			weChatInfo.append("<pre>	department："+department+"</pre>");
			weChatInfo.append("<pre>	         startTime："+startTime+"</pre>");	
			weChatInfo.append("<pre>	description："+description+"</pre>");
			weChatInfo.append("<pre>	=======NoticeInfoEnd========</pre>");
			
			String weChatMessage = weChatInfo.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userGroup, title, detailtitle, weChatMessage, "");
			//log.info("WeChat Send Success!");	
			
			
			StringBuffer EmailInfo = new StringBuffer();
			EmailInfo.append("<pre>======NoticeInformation======</pre>");		
			EmailInfo.append("<pre>	abnormal："+rabnormalName+"</pre>");
			EmailInfo.append("<pre>	abnormalEQPName："+abnormalEQPName+"</pre>");
			EmailInfo.append("<pre>	modifyUser："+createUser+"</pre>");
			EmailInfo.append("<pre>	department："+department+"</pre>");
			EmailInfo.append("<pre>	startTime："+startTime+"</pre>");		
			EmailInfo.append("<pre>	description："+description+"</pre>");
			EmailInfo.append("<pre>	=======NoticeInfoEnd========</pre>");
			//EMailInterface ei = new EMailInterface("mail.visionox.com", "25", "1000", "1000");
			//ei.postMail(emailList,  "异常单创建通知", message.toString(), "V3MES", "V3MES@visionox.com", "V3MES", "vis@2019");
		}
		catch (Exception e)
		{
			log.info("eMobile or WeChat or email Send Error : " + e.getCause());	
		}	
		return doc;
	}
	
	private String getUserList(String rabnormalName)
	{
		List<Map<String,Object>> resultList = null;
		String userList = new String();
		StringBuilder sb = new StringBuilder();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT B.DEPARTMENT FROM CT_ALARMUSERGROUP B, (SELECT A.CREATEUSER FROM CT_ABNORMALEQP A   "
				+ "WHERE A.ABNORMALNAME = :ABNORMALNAME ) C WHERE B.USERID=C.CREATEUSER AND B.ALARMGROUPNAME='AbnormalEQP' "
				+ "UNION SELECT D.DEPARTMENT FROM  CT_ABNORMALEQP D WHERE D.ABNORMALNAME = :ABNORMALNAME");
		
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
				sql1.append("SELECT * FROM CT_ALARMUSERGROUP  WHERE ALARMGROUPNAME = 'AbnormalEQP' AND DEPARTMENT =:DEPARTMENT");
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
			log.info("Not Found the Department of "+ rabnormalName);
			log.info(" Failed to send to EMobile, MachineName: " + rabnormalName);
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
				sql1.append("SELECT * FROM CT_ALARMUSERGROUP  WHERE ALARMGROUPNAME = 'AbnormalEQP' AND DEPARTMENT =:DEPARTMENT");
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