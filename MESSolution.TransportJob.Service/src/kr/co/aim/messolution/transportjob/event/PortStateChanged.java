package kr.co.aim.messolution.transportjob.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.info.MakePortStateByStateInfo;

public class PortStateChanged extends AsyncHandler {
	private static Log log = LogFactory.getLog(PortStateChanged.class);
	/**
	 * MessageSpec [MCS -> TEX -> FMC]
	 * 
	 * <Body>
	 *     <MACHINENAME />
	 *     <PORTNAME />
	 *     <PORTSTATE />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portStateName = SMessageUtil.getBodyItemValue(doc, "PORTSTATE", true);

		Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
		Machine machineData =MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		MakePortStateByStateInfo transitionInfo = MESPortServiceProxy.getPortInfoUtil().makePortStateByStateInfo(portData, portStateName);
		MESPortServiceProxy.getPortServiceImpl().makePortStateByState(portData, transitionInfo, eventInfo);
	
		if("DOWN".equals(portStateName) && !"MANUAL".equals(machineData.getMachineStateName()))
		{
			String alarmCode ="PortDownSendEmailByMachineGroup";			
			String alarmType ="EQP";
			
			String message = "<pre>===============================PortStateChanged======================================</pre>";
			message += "<pre>- MachineName   :     " + machineName + "</pre>";			
			message += "<pre>- PortName      :     " + portName + "</pre>";			
			message += "<pre>- PortState     :     " + portStateName + "</pre>";			
			message += "<pre>- Message       : " + "The current port has been down,please confirm!"+"</pre>";			
			message += "<pre>=====================================================================================</pre>";
			
			CommonUtil.PortDownSendEmailByMachineGroup(alarmCode,alarmType,machineName,portName,portStateName, message);
			
			//houxk 20210615						
			try
			{				
				sendToEm(machineName,message);
			}
			catch (Exception e)
			{
				log.info("eMobile or WeChat Send Error : " + e.getCause());	
			}
			
			// SMS not used.
			// CommonUtil.sendPortDownSms(alarmCode,alarmType,eventName,machineName,portName,portStateName);
		}
		
		try
		{
			// success then report to FMB
			GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);
		}
		catch (Exception ex)
		{
			eventLog.warn("FMC Report Failed!");
		}
	}
	
	public void sendToEm(String machineName, String messageInfo)
	{
		String[] userList = getUserList(machineName,"PortStateDown");	
		if(userList == null || userList.length ==0) return;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("PortStateChanged", "MES", "", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		try
		{	
			log.info("PortStateChangedDown Start Send To Emobile & Wechat");	
			
			//String[] userGroup = userList.split(",");				
			String title = "PortStateChangedDown";
			String detailtitle = "${}CIM系统消息通知";
			String url = "";
			
			StringBuffer info = new StringBuffer();
			info.append("<pre>=======================AlarmInformation=======================</pre>");
			info.append(messageInfo);
			info.append("<pre>=============================End=============================</pre>");			
			
			String message = info.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userList, title, detailtitle, message, url);
									
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userList, title, detailtitle, message, "");
		}
		catch (Exception e)
		{
			log.info("eMobile or WeChat Send Error : " + e.getCause());	
		} 
	}
	
	//AlarmGroup = PortStateDown && RMSDepart
	private String[] getUserList(String machineName, String alarmGroupName)
	{
		List<Map<String,Object>> resultList = null;
		String[] userList = null;
		List<String> sb = new ArrayList<String>();
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
			//if (resultList.size() > 0) 
			{
				String departmentAll = ConvertUtil.getMapValueByName(resultList.get(0), "RMSDEPARTMENT") + ",FA";

				List<String> department =  CommonUtil.splitStringDistinct(",",departmentAll);

				StringBuffer sql1 = new StringBuffer();
				sql1.append(
						"SELECT B.* FROM CT_ALARMUSERGROUP A, CT_ALARMUSER B, USERPROFILE C WHERE A.USERID = B.USERID AND B.USERID = C.USERID AND A.ALARMGROUPNAME = :ALARMGROUPNAME AND B.DEPARTMENT=:DEPARTMENT");
				Map<String, Object> args1 = new HashMap<String, Object>();

				for(int j = 0; j < department.size(); j++)
				{
					args1.put("ALARMGROUPNAME", alarmGroupName);
					args1.put("DEPARTMENT", department.get(j));
					List<Map<String, Object>> sqlResult1 = GenericServiceProxy.getSqlMesTemplate()
							.queryForList(sql1.toString(), args1);
					
					if (sqlResult1.size() > 0) 
					{
						for (Map<String, Object> userInfo : sqlResult1)
						{
							String user = ConvertUtil.getMapValueByName(userInfo, "USERID");
							sb.add(user);
						}										 						
					}
				}
				userList = sb.toArray(new String[] {});
			}
		}
		catch (Exception e)
		{
			log.info("Not Found the Department of "+ machineName);
			log.info(" Failed to send to EMobile, MachineName: " + machineName);
		}
		return userList;			
	}
}
	
	
