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
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.messolution.generic.util.EMailInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class ReciveAbnormalEQP extends SyncHandler{
	private static Log log = LogFactory.getLog(ReciveAbnormalEQP.class);
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String abnormalName = SMessageUtil.getBodyItemValue(doc, "ABNORMALNAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String planFinishTime = SMessageUtil.getBodyItemValue(doc, "PLANFINISHTIME", true);
		String reason = SMessageUtil.getBodyItemValue(doc, "REASON", true);
		String abnormalProduct = SMessageUtil.getBodyItemValue(doc, "ABNORMALPRODUCTLIST", false);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);
		String handler = SMessageUtil.getBodyItemValue(doc, "HANDLER", true);
		String process = SMessageUtil.getBodyItemValue(doc, "PROCESS", true);
		String causeEQP = SMessageUtil.getBodyItemValue(doc, "CAUSEEQP", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		eventInfo.setEventName("HandleAbnormalEQP");
		
		AbnormalEQP abnormalData = ExtendedObjectProxy.getAbnormalEQPService().selectByKey(false, new String[]{abnormalName});
		
		if(!abnormalData.getAbnormalState().contains("Created") && !abnormalData.getAbnormalState().contains("Handling"))
		{
			log.info("abnormalState is not Created");
			
			//CUSTOM-0028: AbnormalState Is Not Created Or Handling!
			throw new CustomException("CUSTOM-0028");
		}
		abnormalData.setAbnormalState("Handling");
		abnormalData.setUnitName(unitName);
		abnormalData.setSubUnitName(subUnitName);
		abnormalData.setReasonCode(reasonCode);
		abnormalData.setCauseEQPName(causeEQP);
		abnormalData.setHandler(handler);
		abnormalData.setProcess(process);
		abnormalData.setReason(reason);
		abnormalData.setProductNameList(abnormalProduct);
		abnormalData.setSendFlag("");
		abnormalData.setPlanFinishTime(TimeUtils.getTimestamp(planFinishTime));
		abnormalData.setLastEventName(eventInfo.getEventName());
		abnormalData.setLastEventComment(eventInfo.getEventComment());
		abnormalData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		abnormalData.setLastEventTime(eventInfo.getEventTime());
		abnormalData.setLastEventUser(eventInfo.getEventUser());
		
		String phoneNumber = "";
		try 
		{
			String sql = "SELECT PHONENUMBER FROM USERPROFILE WHERE USERID = :USERID";
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("USERID", this.getEventUser());		
			List<Map<String, Object>> phoneNumData = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			phoneNumber = phoneNumData.get(0).get("PHONENUMBER").toString();
		} catch (Exception e) 
		{
			log.info("can't find phonenumber");
		}
	
		//abnormalData.setPhone(phoneNumber);
		
		abnormalData = ExtendedObjectProxy.getAbnormalEQPService().modify(eventInfo, abnormalData);
		
		//start send message
		String rabnormalName =abnormalData.getAbnormalName();
		String userList = getUserList(rabnormalName);
		String receiveUser = eventInfo.getEventUser();
		List<String> emailList = getEmailList(rabnormalName);
		
		try
		{																
			String[] userGroup = userList.split(",");				
			String title = "异常单处置通知";
			String detailtitle = "${}CIM系统消息通知";
			
			StringBuffer info = new StringBuffer();
			info.append("<pre>=======================NoticeInformation=======================</pre>");
			info.append("<pre>	abnormal："+rabnormalName+"</pre>");
			info.append("<pre>	abnormalEQPName："+abnormalData.getAbnormalEQPName()+"</pre>");
			info.append("<pre>	handler："+receiveUser+"</pre>");
			info.append("<pre>	department："+abnormalData.getDepartment()+"</pre>");
			info.append("<pre>	planFinishTime："+planFinishTime+"</pre>");
			info.append("<pre>	reason："+reason+"</pre>");
			info.append("<pre>=============================End=============================</pre>");				
			
			String message = info.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userGroup, title, detailtitle, message, "");
			//log.info("eMobile Send Success!");	
			
			StringBuffer weChatInfo = new StringBuffer();
			weChatInfo.append("<pre>======NoticeInformation======</pre>");			
			weChatInfo.append("<pre>	abnormal："+rabnormalName+"</pre>");
			weChatInfo.append("<pre> abnormalEQPName："+abnormalData.getAbnormalEQPName()+"</pre>");
			weChatInfo.append("<pre>	handler："+receiveUser+"</pre>");
			weChatInfo.append("<pre>	department："+abnormalData.getDepartment()+"</pre>");
			weChatInfo.append("<pre>	       planFinishTime："+planFinishTime+"</pre>");
			weChatInfo.append("<pre>	reason："+reason+"</pre>");			
			weChatInfo.append("<pre>	=======NoticeInfoEnd========</pre>");
			
			String weChatMessage = weChatInfo.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userGroup, title, detailtitle, weChatMessage, "");
			//log.info("WeChat Send Success!");	
			
			
			StringBuffer EmailInfo = new StringBuffer();
			EmailInfo.append("<pre>======NoticeInformation======</pre>");		
			EmailInfo.append("<pre>	abnormal："+rabnormalName+"</pre>");
			EmailInfo.append("<pre>	abnormalEQPName："+abnormalData.getAbnormalEQPName()+"</pre>");
			EmailInfo.append("<pre>	handler："+receiveUser+"</pre>");
			EmailInfo.append("<pre>	department："+abnormalData.getDepartment()+"</pre>");
			EmailInfo.append("<pre>	planFinishTime："+planFinishTime+"</pre>");
			EmailInfo.append("<pre>	reason："+reason+"</pre>");			
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
				sql1.append("SELECT * FROM CT_ALARMUSERGROUP  WHERE ALARMGROUPNAME = 'AbnormalEQP' AND DEPARTMENT =:DEPARTMENT AND USERLEVEL='1'");
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
