package kr.co.aim.messolution.machine.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.AbnormalEQP;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.util.EMailInterface;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import com.sun.xml.internal.ws.api.pipe.Tube;

public class CreateAbnormalEQP extends SyncHandler {
	private static Log log = LogFactory.getLog(CreateAbnormalEQP.class);
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> abnormalList = SMessageUtil.getBodySequenceItemList(doc, "ABNORMALLIST", true);
		List<Element> eleAbnormalList = new ArrayList<Element>();
		String url = "";
		int  num = 1;
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		
		for (Element abnormal : abnormalList)
		{
			String abnormalEQPName = SMessageUtil.getChildText(abnormal, "ABNORMALEQPNAME", true);
			String abnormalOperationName = SMessageUtil.getChildText(abnormal, "ABNORMALOPERATIONNAME", false);
			String lotName = SMessageUtil.getChildText(abnormal, "LOTNAME", false);
			String department = SMessageUtil.getChildText(abnormal, "DEPARTMENT", true);
			String startTime = SMessageUtil.getChildText(abnormal, "STARTTIME", true);
			String abnormalType = SMessageUtil.getChildText(abnormal, "ABNORMALTYPE", true);
			String productNameList = SMessageUtil.getChildText(abnormal, "PRODUCTNAMELIST", false);
			String productionType = SMessageUtil.getChildText(abnormal, "PRODUCTIONTYPE", false);
			String productSpecName = SMessageUtil.getChildText(abnormal, "PRODUCTSPECNAME", false);
			String description = SMessageUtil.getChildText(abnormal, "DESCRIPTION", true);
			String factory = SMessageUtil.getChildText(abnormal, "FACTORYNAME", true);
			// convert
			Timestamp tStartTime = TimeUtils.getTimestamp(startTime);
			String  timeName =abnormalType + "-" + TimeStampUtil.getCurrentEventTimeKey().substring(2, 14);
			String abnormalName = String.format(timeName + "%03d", num);
			num=num+1;
			MachineSpec abnormalEQPSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(abnormalEQPName);

			if(abnormalType.equals("EQP"))
				eventInfo.setEventName("CreateAbnormalEQP");
			else if(abnormalType.equals("LOT"))
				eventInfo.setEventName("CreateAbnormalLOT");
			else if(abnormalType.equals("MASK"))
				eventInfo.setEventName("CreateAbnormalMask");
			else 
				eventInfo.setEventName("CreateAbnormal");
			
			AbnormalEQP newAbnormal = createAbnormalEQP(abnormalEQPName,productionType,productSpecName,abnormalName,abnormalOperationName,lotName,productNameList,"",department,tStartTime,abnormalType,description,factory,eventInfo);
			String rabnormalName =newAbnormal.getAbnormalName();
		    eleAbnormalList.add(setReturnAbnormalList(newAbnormal,startTime));
		    			
			String userList = getUserList(rabnormalName);
			String createUser = newAbnormal.getCreateUser();
			List<String> emailList = getEmailList(rabnormalName);						
		    try
			   {																
					String[] userGroup = userList.split(",");				
					String title = "异常单创建通知";
					String detailtitle = "${}CIM系统消息通知";
					
					StringBuffer info = new StringBuffer();
					info.append("<pre>=======================NoticeInformation=======================</pre>");
					info.append("<pre>	abnormal："+abnormalName+"</pre>");
					info.append("<pre>	abnormalEQPName："+abnormalEQPName+"</pre>");
					info.append("<pre>	createUser："+createUser+"</pre>");
					info.append("<pre>	department："+department+"</pre>");
					info.append("<pre>	startTime："+startTime+"</pre>");
					info.append("<pre>	description："+description+"</pre>");
					info.append("<pre>=============================End=============================</pre>");				
					
					String message = info.toString();
					
					ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userGroup, title, detailtitle, message, url);
					//log.info("eMobile Send Success!");	
					
					StringBuffer weChatInfo = new StringBuffer();
					weChatInfo.append("<pre>======NoticeInformation======</pre>");			
					weChatInfo.append("<pre>	abnormal："+abnormalName+"</pre>");
					weChatInfo.append("<pre> abnormalEQPName："+abnormalEQPName+"</pre>");
					weChatInfo.append("<pre>	createUser："+createUser+"</pre>");
					weChatInfo.append("<pre>	department："+department+"</pre>");
					weChatInfo.append("<pre>	         startTime："+startTime+"</pre>");		
					weChatInfo.append("<pre>	description："+description+"</pre>");
					weChatInfo.append("<pre>	=======NoticeInfoEnd========</pre>");
					
					String weChatMessage = weChatInfo.toString();
					
					ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userGroup, title, detailtitle, weChatMessage, "");
					//log.info("WeChat Send Success!");	
					
					
					StringBuffer EmailInfo = new StringBuffer();
					EmailInfo.append("<pre>======NoticeInformation======</pre>");		
					EmailInfo.append("<pre>	abnormal："+abnormalName+"</pre>");
					EmailInfo.append("<pre>	abnormalEQPName："+abnormalEQPName+"</pre>");
					EmailInfo.append("<pre>	createUser："+createUser+"</pre>");
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
		}
		
		// call by value so that reply would be modified
		XmlUtil.setSubChildren(SMessageUtil.getBodyElement(doc), "NEWABNORMALLIST", eleAbnormalList);
		
		return doc;
	}

	private AbnormalEQP createAbnormalEQP(String abnormalEQPName,String productionType,String productSpecName,String  abnormalName, String abnormalOperationName, String lotName,String productNameList, String reasonCode, String department,
			 Timestamp tStartTime, String abnormalType, String description,String factory, EventInfo eventInfo ) throws CustomException 
	{
		AbnormalEQP newAbnormal = new AbnormalEQP();
		//Timestamp tPlanFinishTime =new Timestamp (tStartTime.getTime()+(12 * 3600 * 1000));
		newAbnormal.setAbnormalEQPName(abnormalEQPName);
		newAbnormal.setAbnormalOperationName(abnormalOperationName);
		newAbnormal.setAbnormalName(abnormalName);
		newAbnormal.setAbnormalState("Created");
		newAbnormal.setAbnormalType(abnormalType);
		newAbnormal.setProductionType(productionType);
		newAbnormal.setProductSpecName(productSpecName);
		newAbnormal.setCreateTime(eventInfo.getEventTime());
		newAbnormal.setCreateUser(eventInfo.getEventUser());
		newAbnormal.setDepartment(department);
		newAbnormal.setLastEventComment(eventInfo.getEventComment());
		newAbnormal.setLastEventName(eventInfo.getEventName());
		newAbnormal.setLastEventTime(eventInfo.getEventTime());
		newAbnormal.setLastEventTimeKey(eventInfo.getEventTimeKey());
		newAbnormal.setLastEventUser(eventInfo.getEventUser());
		newAbnormal.setLotName(lotName);
		newAbnormal.setProductNameList(productNameList);
		newAbnormal.setReasonCode(reasonCode);
		newAbnormal.setStartTime(tStartTime);
		//newAbnormal.setPlanFinishTime(tPlanFinishTime);
		newAbnormal.setDescription(description);
		newAbnormal.setFactoryName(factory);
		
		newAbnormal = ExtendedObjectProxy.getAbnormalEQPService().create(eventInfo, newAbnormal);
		return newAbnormal;
	}
	
	private Element setReturnAbnormalList(AbnormalEQP abnormal, String startTime)
	{
		Element eleAbnormal = new Element("ABNORMAL");

		try
		{

			XmlUtil.addElement(eleAbnormal, "ABNORMALNAME", abnormal.getAbnormalName());
			XmlUtil.addElement(eleAbnormal, "ABNORMALOPERATIONNAME", abnormal.getAbnormalOperationName());
			XmlUtil.addElement(eleAbnormal, "ABNORMALEQPNAME", abnormal.getAbnormalEQPName());
			XmlUtil.addElement(eleAbnormal, "LOTNAME", abnormal.getLotName());
			XmlUtil.addElement(eleAbnormal, "PRODUCTNAMELIST", abnormal.getProductNameList());
			XmlUtil.addElement(eleAbnormal, "PRODUCTIONTYPE", abnormal.getProductionType());
			XmlUtil.addElement(eleAbnormal, "DEPARTMENT", abnormal.getDepartment());
			//XmlUtil.addElement(eleAbnormal, "PLANFINISHTIME", abnormal.getPlanFinishTime().toString());
			XmlUtil.addElement(eleAbnormal, "STARTTIME", startTime);
			XmlUtil.addElement(eleAbnormal, "DESCRIPTION", abnormal.getDescription());
		}
		catch (Exception ex)
		{
			eventLog.warn(String.format("Scribing Abnormal[%s] is failed so that skip", abnormal.getAbnormalName()));
		}

		return eleAbnormal;
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
