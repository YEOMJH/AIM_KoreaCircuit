package kr.co.aim.messolution.timer.job;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ComponentMonitor;
import kr.co.aim.messolution.extended.object.management.data.ProductFutureAction;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EMailInterface;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;
import net.sf.json.JSONObject;

public class ComponentMonitorTimer implements Job, InitializingBean
{	
	private static Log log = LogFactory.getLog(ComponentMonitorTimer.class);
	
	@Override
	public void afterPropertiesSet() throws Exception {
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}
	
	
	public void execute(JobExecutionContext arg0) throws JobExecutionException 
	{	
		try
		{
			// Search Product In CT_ComponentMonitor
			List<Map<String, Object>> dataInfo = this.componentMonitor();	
			
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
						//GenericServiceProxy.getSMSInterface().sendMessage(smsMessageInfo,phoneList,machineName,"ProductInSubUnitOverTime");
						
						//Start houxk 20210324
						//get UserList
						String userList = getUserList(machineName);	
						
						//SendToEM
						sendToEM(userList,machineName,messageInfo);
						
						//SendToFMB
						EventInfo eventInfo = EventInfoUtil.makeEventInfo("ComponentMonitorTimer", "MES", "", "", "");				
						MachineSpec machineSpec = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
						sendToFMB(machineSpec.getFactoryName(), machineSpec.getKey().getMachineName(), eventInfo, messageInfo);
						//End
					}
					// Update SendFlag
					updateSendFlag(dataInfo);
				}
			}
		}
		catch (Exception e)
		{
			if (log.isDebugEnabled())
				log.info(e.getCause());
		}
	}

	
	private StringBuffer executeSmsMessage(List<Map<String, Object>> dataInfo, String machineName)
	{
		StringBuffer sqlBuffer = new StringBuffer();
		
		for (Map<String, Object> dataInfos : dataInfo)
		{
			if (ConvertUtil.getMapValueByName(dataInfos, "MACHINENAME").equals(machineName))
			{
				
				sqlBuffer.append("productName: \n" + ConvertUtil.getMapValueByName(dataInfos, "PRODUCTNAME")
						+ "\n MachineName: \n" + ConvertUtil.getMapValueByName(dataInfos, "MATERIALLOCATIONNAME")
						+ "\n  KeepInTime: \n" + ConvertUtil.getMapValueByName(dataInfos, "DurationTime") + "min"
						+ "\n  RuleTime: \n" + ConvertUtil.getMapValueByName(dataInfos, "RULETIME") + "min");
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
						"SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B WHERE A.USERID = B.USERID  AND A.ALARMGROUPNAME = 'ComponentMonitor' AND B.DEPARTMENT=:DEPARTMENT");
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


	private void updateSendFlag(List<Map<String, Object>> dataInfo) throws greenFrameDBErrorSignal, CustomException 
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(null, null, null, null, null);
		List<ComponentMonitor> updateList = new ArrayList<ComponentMonitor>();
		
		for (Map<String, Object> dataInfos : dataInfo)
		{
			List<ComponentMonitor> updateInfo = ExtendedObjectProxy.getComponentMonitorService().select("PRODUCTNAME = ?", new Object[] { ConvertUtil.getMapValueByName(dataInfos, "PRODUCTNAME") });
			
			for(ComponentMonitor row : updateInfo)
			{
				row.setSendFlag("Y");
				
				updateList.add(row);
			}
		}
		try
		{
			ExtendedObjectProxy.getComponentMonitorService().modify(eventInfo, updateList);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}


	// sendEmail
	private void sendEmail(List<String> emailList, StringBuffer messageInfo, String machineName) 
	{
		if(emailList !=null && emailList.size()>0)
		{
			StringBuffer message = new StringBuffer();
			message.append("<pre>===============ProductInMachOverTime===============</pre>");
			message.append("<pre> MachineName : " + machineName + "</pre>");
			message.append(messageInfo);
		 
			try
			{
				EMailInterface ei = new EMailInterface("mail.visionox.com", "25", "1000", "1000");
				ei.postMail(emailList,  " ProductInMachineOverTime ", message.toString(), "V3MES", "V3MES@visionox.com", "V3MES", "vis@2019");
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
						"SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B, USERPROFILE C WHERE     A.USERID = B.USERID AND B.USERID = C.USERID AND A.ALARMGROUPNAME = 'ComponentMonitor' AND C.DEPARTMENT = :DEPARTMENT");
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
		List<String> list = new ArrayList<String>();
		
		for (Map<String, Object> dataInfos : dataInfo)
		{
			if (ConvertUtil.getMapValueByName(dataInfos, "MACHINENAME").equals(machineName.substring(0, 6)))
			{
				sqlBuffer.append("<pre> productName: " + ConvertUtil.getMapValueByName(dataInfos, "PRODUCTNAME")
					+ "  MachineName: " + ConvertUtil.getMapValueByName(dataInfos, "MATERIALLOCATIONNAME")
					+ "  ProcessingTime: " + Integer.parseInt(ConvertUtil.getMapValueByName(dataInfos, "DurationTime"))/60 + " Hour, " + Integer.parseInt(ConvertUtil.getMapValueByName(dataInfos, "DurationTime"))%60 + " min "
					+ "  RuleTime: " + ConvertUtil.getMapValueByName(dataInfos, "RULETIME") + " min " + "</pre>");
				
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("ProcessingOverTime", machineName.substring(0, 6), " productName: " + ConvertUtil.getMapValueByName(dataInfos, "PRODUCTNAME")
					+ "  MachineName: " + ConvertUtil.getMapValueByName(dataInfos, "MATERIALLOCATIONNAME")
					+ "  ProcessingTime: " + Integer.parseInt(ConvertUtil.getMapValueByName(dataInfos, "DurationTime"))/60 + " Hour, " + Integer.parseInt(ConvertUtil.getMapValueByName(dataInfos, "DurationTime"))%60 + " min "
					+ "  RuleTime: " + ConvertUtil.getMapValueByName(dataInfos, "RULETIME") + " min ", "RH-OverTime", "");
				
				eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
			
				if(!CommonUtil.getEnumDefValueStringByEnumNameAndEnumValue("ComponentMonitorOverTimeHold", machineName.substring(0, 6)).isEmpty())
				{
					Product productData=new Product();
					Lot LotData=new Lot();
					
					try
					{
						productData = ProductServiceProxy.getProductService().selectByKey(new ProductKey(ConvertUtil.getMapValueByName(dataInfos, "PRODUCTNAME")));
						LotData =  LotServiceProxy.getLotService().selectByKey(new LotKey(productData.getLotName()));
					}
					catch(NotFoundSignal n)
					{
						log.error("Not Found Product: " + ConvertUtil.getMapValueByName(dataInfos, "PRODUCTNAME"));
						continue;
					}
					catch (FrameworkErrorSignal e) 
					{
						log.error("Not Found Product: " + ConvertUtil.getMapValueByName(dataInfos, "PRODUCTNAME"));
						continue;
					}
					
					try
					{
						if(LotData.getLotState().equals("Released")&&productData.getProductState().equals("InProduction"))
						{
							if(StringUtils.equals(LotData.getLotProcessState(), "WAIT")&&!StringUtils.equals(LotData.getUdfs().get("JOBDOWNFLAG"), "Y"))
							{
								if(!list.contains(productData.getLotName()))
								{
									list.add(productData.getLotName());
								}
							}
							else
							{
								ProductFutureAction actionData 
								= ExtendedObjectProxy.getProductFutureActionService().selectByKey(false, 
											new Object[] {ConvertUtil.getMapValueByName(dataInfos, "PRODUCTNAME"),productData.getFactoryName(),productData.getProcessFlowName(),productData.getProcessFlowVersion(),
													productData.getProcessOperationName(),productData.getProcessOperationVersion(), ConvertUtil.getMapValueByName(dataInfos, "MATERIALLOCATIONNAME").substring(0, 10), "Over-Time"});
								ExtendedObjectProxy.getProductFutureActionService().updateProductFutureAction(eventInfo, ConvertUtil.getMapValueByName(dataInfos, "PRODUCTNAME"), productData.getFactoryName(), productData.getProcessFlowName(),
										productData.getProcessFlowVersion(), productData.getProcessOperationName(), productData.getProcessOperationVersion(), 
										ConvertUtil.getMapValueByName(dataInfos, "MATERIALLOCATIONNAME").substring(0, 10), "RH-OverTime", "HOLD", "hold", "System", productData.getLotName());
							}
						}
						else
						{
							log.info("LotState is not Released or productState is not InProduction");
						}
					}
					catch (Exception no) 
					{
						log.info("Not Found productFutureAction: "+ConvertUtil.getMapValueByName(dataInfos, "PRODUCTNAME"));
						try 
						{
							ExtendedObjectProxy.getProductFutureActionService().insertProductFutureAction(eventInfo, ConvertUtil.getMapValueByName(dataInfos, "PRODUCTNAME"), productData.getFactoryName(),
									productData.getProcessFlowName(), productData.getProcessFlowVersion(), productData.getProcessOperationName(),
									productData.getProcessOperationVersion(), ConvertUtil.getMapValueByName(dataInfos, "MATERIALLOCATIONNAME").substring(0, 10), "RH-OverTime", "HOLD", "hold", "System", productData.getLotName());
						} 
						catch (CustomException e) 
						{
							log.info("Insert fail");
						}
					}
				}
				
				if (list.size() > 0)
				{
					List newList = new ArrayList(new TreeSet(list));
					try 
					{
						for (int i = 0; i < newList.size(); i++) 
						{
							String lotName = newList.get(i).toString();
							Lot postLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
							String sql3 = "SELECT LOTNAME,REASONCODE FROM LOTMULTIHOLD WHERE LOTNAME = :LOTNAME AND REASONCODE=:REASONCODE";
							Map<String, Object> bindMap2 = new HashMap<String, Object>();

							bindMap2.put("LOTNAME", lotName);
							bindMap2.put("REASONCODE", eventInfo.getReasonCode());
							List<Map<String, Object>> listResult = kr.co.aim.greentrack.generic.GenericServiceProxy
									.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql3, bindMap2);

							// LotMultiHold
							if (listResult != null && listResult.size() <= 0) 
							{
								MESLotServiceProxy.getLotServiceImpl().lotMultiHold(eventInfo, postLotData,
										postLotData.getUdfs());
							}
						}
					} 
					catch (Exception e) 
					{
						log.info(e.getCause());
					}
				}
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
			if (!machineList.contains(ConvertUtil.getMapValueByName(productList, "MATERIALLOCATIONNAME").substring(0, 10)))
			{
				machineList.add(ConvertUtil.getMapValueByName(productList, "MATERIALLOCATIONNAME").substring(0, 10));
			}
		}
		
		return machineList;
	}


	// Search Product In CT_ComponentMonitor
	public List<Map<String, Object>> componentMonitor()
	{
		/*
		 * Table - EnumDefValue
		 * EnumName: ComponentMonitor
		 * EnumValue: MachineGroupName
		 * Description: Factory
		 * DefaultFlag: Y ( Send Email only once ) , N ( Send Email until componentOutMachine )
		 * DisplayColor: timeMonitor
		 */
		StringBuffer sqlBuffer = new StringBuffer();
		
		sqlBuffer.append("SELECT CM.PRODUCTNAME,"
				+ "       ROUND ("
				+ "             TO_NUMBER ("
				+ "                   TO_DATE (TO_CHAR (SYSDATE, 'yyyy-MM-dd HH24:mi:ss'),"
				+ "                            'yyyy-mm-dd hh24-mi-ss')"
				+ "                 - TO_DATE (SUBSTR (CM.TIMEKEY, 0, 14),"
				+ "                            'yyyy-MM-dd HH24:mi:ss'))"
				+ "           * 24"
				+ "           * 60)"
				+ "           DurationTime,"
				+ "       CM.RULETIME,"
				+ "       CM.EMAILTYPE,"
				+ "       CM.SENDFLAG,"
				+ "       CM.MACHINENAME,"
				+ "       CM.MATERIALLOCATIONNAME"
				+ "  FROM CT_COMPONENTMONITOR CM, LOT L"
				+ " WHERE     ROUND ("
				+ "                 TO_NUMBER ("
				+ "                       TO_DATE (TO_CHAR (SYSDATE, 'yyyy-MM-dd HH24:mi:ss'),"
				+ "                                'yyyy-mm-dd hh24-mi-ss')"
				+ "                     - TO_DATE (SUBSTR (CM.TIMEKEY, 0, 14),"
				+ "                                'yyyy-MM-dd HH24:mi:ss'))"
				+ "               * 24"
				+ "               * 60) >"
				+ "           CM.RULETIME"
				+ "       AND L.FACTORYNAME = CM.FACTORYNAME"
				+ "       AND L.LOTNAME = CM.LOTNAME"
				+ "       AND L.MACHINENAME = CM.MACHINENAME"
				+ "       AND L.PROCESSOPERATIONNAME = CM.PROCESSOPERATIONNAME"
				+ "       AND L.PROCESSOPERATIONVERSION = CM.PROCESSOPERATIONVERSION"
				+ "       AND L.PRODUCTSPECNAME = CM.PRODUCTSPECNAME"
				+ "       AND L.PRODUCTSPECVERSION = CM.PRODUCTSPECVERSION"
				+ "       AND L.LOTPROCESSSTATE = 'RUN'"
				+ "       AND CM.EVENTNAME IN ('ComponentInSubUnit', 'ComponentInUnit')"
				+ "       AND (CM.SENDFLAG = 'N' OR (CM.EMAILTYPE = 'N' AND CM.SENDFLAG = ?))");
		
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
	
	//Start houxk 20210324
	//get UserList
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
						"SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B, USERPROFILE C WHERE     A.USERID = B.USERID AND B.USERID = C.USERID AND A.ALARMGROUPNAME = 'ComponentMonitor' AND C.DEPARTMENT = :DEPARTMENT");
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
	
	//sendToEM & WeChat
	private void sendToEM(String userList, String machineName, StringBuffer messageInfo) 
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ComponentMonitorTimer", "MES", "", "", "");
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
	
	//sendToFMB
	private void sendToFMB(String factoryName, String machineName, EventInfo eventInfo, StringBuffer messageInfo) throws CustomException
	{
	
		Element rootElement = new Element(SMessageUtil.Message_Tag);
		
		Element headerElement = new Element(SMessageUtil.Header_Tag);
			
		headerElement.addContent(new Element("MESSAGENAME").setText("ProductInMachineOverTime"));
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
	//End
}
