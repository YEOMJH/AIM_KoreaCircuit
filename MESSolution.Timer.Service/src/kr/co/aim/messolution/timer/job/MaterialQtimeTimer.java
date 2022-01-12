package kr.co.aim.messolution.timer.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EMailInterface;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class MaterialQtimeTimer implements Job, InitializingBean
{	
	private static Log log = LogFactory.getLog(MaterialQtimeTimer.class);
	
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
		ConstantMap constMap = GenericServiceProxy.getConstantMap();
		
		String sql = " SELECT DISTINCT C.CONSUMABLENAME , C.CONSUMABLETYPE , C.FACTORYNAME, C.EXPIRATIONDATE,U.USERID, U.EMAIL, U.DEPARTMENT, U.GROUPNAME, U.RANGE "
				   		  + " FROM ( SELECT B.ALARMGROUPNAME, B.USERID, B.RANGE, A.DEPARTMENT, A.EMAIL, A.GROUPNAME  "
				   		  + " FROM CT_ALARMUSER A, CT_ALARMUSERGROUP B"
				   		  + " WHERE B.ALARMGROUPNAME = 'MaterialQTime'"
				   		  + " AND A.USERID = B.USERID ) U, "
				   		  + "( SELECT  B.CONSUMABLENAME , B.CONSUMABLETYPE , B.FACTORYNAME , B.EXPIRATIONDATE, O.DEPARTMENT, O.GROUPNAME  "
				   		  + " FROM CONSUMABLESPEC A, CONSUMABLE B, CT_MATERIALINFO O "
				   		  + " WHERE A.FACTORYNAME = B.FACTORYNAME "
				   		  + " AND A.CONSUMABLESPECNAME = B.CONSUMABLESPECNAME "
				   		  + " AND A.CONSUMABLESPECVERSION = B.CONSUMABLESPECVERSION "
				   		  + " AND A.CONSUMABLETYPE = B.CONSUMABLETYPE "
				   		  + " AND B.CONSUMABLETYPE = O.MATERIALTYPE"
				   		  + " AND B.FACTORYNAME = O.FACTORYNAME"
				   		  + " AND B.CONSUMABLETYPE NOT IN('Organic','InOrganic')"
				   		  + " AND B.CONSUMABLESTATE != :CONSUMABLESTATE "
				   		  //+ " AND B.TRANSPORTSTATE = :TRANSPORTSTATE"
				   		  //+ " AND B.MACHINENAME  = M.MACHINENAME "
				   		  //+ " AND B.FACTORYNAME = M.FACTORYNAME "
				   		  //+ " AND M.DETAILMACHINETYPE = :DETIALMACHINETYPE "
				   		  //+ " AND M.MACHINETYPE =:MACHINETYPE 
				   		  + ") C , USERPROFILE P"
				 + " WHERE  1=1 AND U.DEPARTMENT = C.DEPARTMENT "
				 + " AND (U.GROUPNAME = C.GROUPNAME OR U.GROUPNAME IS NULL OR C.GROUPNAME IS NULL) "
				 + " AND ( SYSDATE <= C.EXPIRATIONDATE AND ( C.EXPIRATIONDATE - SYSDATE ) * 24 < U.RANGE OR SYSDATE > C.EXPIRATIONDATE) "
				 + " AND P.USERID = U.USERID "
				 + " AND (U.DEPARTMENT = :DEPARTMENT OR :DEPARTMENT IS NULL) "
				 + " AND (U.GROUPNAME = :GROUPNAME OR :GROUPNAME IS NULL) "
				 + " ORDER BY U.DEPARTMENT,U.GROUPNAME,U.USERID,C.CONSUMABLETYPE ";
				 
		Map<String,Object> bindMap = new HashMap<>();
		bindMap.put("CONSUMABLESTATE", constMap.Cons_NotAvailable);
		//bindMap.put("TRANSPORTSTATE", constMap.Cons_TransportState_OnEQP);
		//bindMap.put("DETIALMACHINETYPE", constMap.DetailMachineType_Main);
		//bindMap.put("MACHINETYPE", constMap.Mac_ProductionMachine);
		bindMap.put("DEPARTMENT", "");
		bindMap.put("GROUPNAME", "");
		
		List<Map<String,Object>> resultList = null;
		try 
		{		
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		} 
		catch (Exception ex) 
		{
           log.debug(ex.getCause());
		}
		
		if (resultList == null || resultList.size() == 0) 
		{
			log.info("No Over MaterialExpiration Info");
			return;
		}
		
		//get distinct department
		List<String> department = new ArrayList<String>();
		try
		{			
			StringBuilder departInfo = new StringBuilder();  
			String depart = "";
			for (int i = 0; i < resultList.size(); i++)
			{		                 
				if (i < resultList.size() - 1) {  
					departInfo.append(ConvertUtil.getMapValueByName(resultList.get(i), "DEPARTMENT") + "+" + ConvertUtil.getMapValueByName(resultList.get(i), "GROUPNAME") + ",");  
	            } else {  
	            	departInfo.append(ConvertUtil.getMapValueByName(resultList.get(i), "DEPARTMENT") + "+" + ConvertUtil.getMapValueByName(resultList.get(i), "GROUPNAME"));  
	            }   
			}
			depart = departInfo.toString();
			department =  CommonUtil.splitStringDistinct(",",depart);
			log.info("DepartmentInfo:" + department.toString());
		}
		catch(Exception e)
		{
			log.info("No DepartmentInfo about Over MaterialExpiration");
			return;
		}
		
		//Ready To Send Mail
		if(department != null && department.size() >0)
		{			
			StringBuffer sql1 = new StringBuffer();
			sql1.append(
					" SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B, USERPROFILE C WHERE A.USERID = B.USERID AND B.USERID = C.USERID AND A.ALARMGROUPNAME = :ALARMGROUPNAME AND B.DEPARTMENT=:DEPARTMENT AND A.USERLEVEL = :USERLEVEL AND (B.GROUPNAME = :GROUPNAME OR :GROUPNAME IS NULL) ");
			Map<String, Object> args1 = new HashMap<String, Object>();

			for(int j = 0; j < department.size(); j++)
			{
				//get UserList & EmailList
				String[] userList = null;				
				List<String> emailList = new ArrayList<String>();
				List<String> sb = new ArrayList<String>();
				try
				{									
					args1.put("ALARMGROUPNAME", "MaterialQTime");
					args1.put("DEPARTMENT", StringUtils.substringBefore(department.get(j), "+"));
					if(StringUtils.isNotEmpty(StringUtils.substringAfter(department.get(j), "+")))
					{
						args1.put("USERLEVEL", "1");
						args1.put("GROUPNAME", StringUtils.substringAfter(department.get(j), "+"));
					}
					else
					{
						args1.put("USERLEVEL", "2");
						args1.put("GROUPNAME", "");
					}					
					List<Map<String, Object>> sqlResult1 = GenericServiceProxy.getSqlMesTemplate()
							.queryForList(sql1.toString(), args1);
					
					if (sqlResult1!= null && sqlResult1.size() > 0) 
					{
						for (Map<String, Object> userInfo : sqlResult1)
						{
							String user = ConvertUtil.getMapValueByName(userInfo, "USERID");
							sb.add(user);
							
							emailList.add(ConvertUtil.getMapValueByName(userInfo, "EMAIL"));						
						}	
						userList = sb.toArray(new String[] {});
					}
				}
				catch(Exception e)
				{
					log.info("Could not find UserInfo about Over MaterialExpiration of"+department.get(j));
				}
				
				//get MaterialList
				List<String> materialList = null;				
				List<Map<String,Object>> sqlResult2 = null;
				bindMap.put("DEPARTMENT", StringUtils.substringBefore(department.get(j), "+"));
				bindMap.put("GROUPNAME", StringUtils.substringAfter(department.get(j), "+"));				
				
				try 
				{					
					sqlResult2 = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);

				} catch (Exception ex) 
				{
		           log.debug(ex.getCause());
				}
				
				try
				{
					if (sqlResult2 != null && sqlResult2.size() > 0) 
					{
						StringBuilder materialInfo = new StringBuilder();  
						String material = "";
						for (int i = 0; i < sqlResult2.size(); i++)
						{		                 
							if (i < sqlResult2.size() - 1) {  
								materialInfo.append(ConvertUtil.getMapValueByName(sqlResult2.get(i), "CONSUMABLENAME") + ",");  
				            } else {  
				            	materialInfo.append(ConvertUtil.getMapValueByName(sqlResult2.get(i), "CONSUMABLENAME"));  
				            }   
						}
						material = materialInfo.toString();
						materialList =  CommonUtil.splitStringDistinct(",",material);
						log.info("Depart:"+department.get(j)+", materialInfo:" + materialList.toString());										
					}
				}
				catch(Exception e)
				{
					log.info("Could not find materialInfo about Over MaterialExpiration of"+department.get(j));
				}
				
				//Make Message
				String message = "";
				try
				{
					if(materialList != null)
					{				
						String message1 = "<pre>==========================AlarmInformation==================================</pre>";
						String message2 = "<pre>==================以下物料将要过期或已过期，请及时处理======================</pre>";
						String message3  = "";
						String message4  ="<pre>===============================End==========================================</pre>";
						
						for(int i = 0; i < materialList.size(); i++)
						{
							Consumable materialData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialList.get(i));
							message3 += "<pre>物料类型:"+materialData.getConsumableType()+",物料ID:"+materialList.get(i)+",有效期:"+materialData.getUdfs().get("EXPIRATIONDATE")+",机台:"+materialData.getUdfs().get("MACHINENAME")+";</pre>";
						}
						
						message = message1+message2+message3+message4;
					}
				}
				catch(Exception e)
				{
					log.info("message is null");
				}
				//Send Mail
				if(emailList != null && StringUtils.isNotEmpty(message))
				{
					try
					{
						EMailInterface ei = new EMailInterface("mail.visionox.com", "25", "1000", "1000");
						ei.postMail(emailList,  " Over Remain Expiration ", message.toString(), "V3MES", "V3MES@visionox.com", "V3MES", "vis@2019");				
					}
					catch (Exception e)
					{
						log.error("Failed to send mail.");
					}
				}
				//Send to EM & Wechat
				if(userList != null && StringUtils.isNotEmpty(message))
				{
					try
					{				
						sendToEm(userList, message);
					}
					catch (Exception e)
					{
						log.info("eMobile or WeChat Send Error : " + e.getCause());	
					}			
				}
			}		
		}				
	}
	
	public void sendToEm(String[] userList, String message)
	{			
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MaterialQtimeTimer", "MES", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{	
			log.info("MaterialQtimeTimer Start Send To Emobile & Wechat");	
						
			String title = "物料有效期监控预警";
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
		
}
