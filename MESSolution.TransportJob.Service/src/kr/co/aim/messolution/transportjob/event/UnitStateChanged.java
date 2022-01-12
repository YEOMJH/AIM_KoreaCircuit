package kr.co.aim.messolution.transportjob.event;

import java.util.HashMap;
import java.util.Map;

import org.jdom.Document;
import kr.co.aim.messolution.generic.util.CommonUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class UnitStateChanged extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX -> FMC]
	 * 
	 * <Body>
	 *    <MACHINENAME />
	 *    <UNITNAME />
	 *    <UNITSTATE />
	 * </Body>
	 */
    private static Log log = LogFactory.getLog(UnitStateChanged.class);
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
		String machineStateName = SMessageUtil.getBodyItemValue(doc, "UNITSTATE", false);
		String superMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);;
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");

		// Check exist machine
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		String oldMachineState = machineData.getMachineStateName();// old
		
		if(machineData.getMachineGroupName().equals("STK"))
		{
			// if MACHINESTATAE is same, Pass
			if (StringUtil.equals(machineStateName, machineData.getMachineStateName()))
				throw new CustomException("MACHINE-0001", machineName, machineData.getMachineStateName(), machineStateName);
			
			Map<String, String> machineUdfs = new HashMap<String, String>();
			
			if (machineStateName.equalsIgnoreCase("IDLE"))
			{
				machineUdfs.put("LASTIDLETIME", TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
			}
			else
			{
				machineUdfs.put("LASTIDLETIME", "");
			}
			
			if (machineStateName.equalsIgnoreCase("DOWN"))
			{
				//send message to em & wechat
				String[] userGroup = MESMachineServiceProxy.getMachineInfoUtil().getAlarmUserIdByUnitName(machineName);
				if(userGroup != null)
				{
					sendMessageToEM(eventInfo,userGroup,"",superMachineName,machineName,machineStateName,"",oldMachineState);
				}
				else
				{
					log.info("when unitstate is down,there is no user need to send emoblie&wechat");
				}
			}

			MakeMachineStateByStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, machineStateName);
			transitionInfo.setUdfs(machineUdfs);
			MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(machineData, transitionInfo, eventInfo);
		}
		else
		{
			// if MACHINESTATAE is same, Pass
			if (StringUtil.equals(machineStateName, machineData.getMachineStateName()))
				throw new CustomException("MACHINE-0001", machineName, machineData.getMachineStateName(), machineStateName);

			// if Current MACHINESTATENAME = 'ENG' then cannot change state (Only can change at OIC)
			if (StringUtil.equals(machineData.getMachineStateName(), "ENG") && (!StringUtil.equals(machineStateName, "ENG") && !StringUtil.equals(machineStateName, "DOWN")))
				throw new CustomException("MACHINE-9006");

			// if Current MACHINESTATENAME = 'TEST' then cannot change state (Only can change at OIC)
			if (StringUtil.equals(machineData.getMachineStateName(), "TEST") && (!StringUtil.equals(machineStateName, "TEST") && !StringUtil.equals(machineStateName, "DOWN")))
				throw new CustomException("MACHINE-9007");

			Map<String, String> machineUdfs = new HashMap<String, String>();
			
//			if (CommonUtil.equalsIn(StringUtils.upperCase(machineStateName), "MANUAL", "DOWN"))
//				machineUdfs.put("CHANGESTATELOCKBYOIC", "Y");
			
			if (machineStateName.equalsIgnoreCase("IDLE"))
			{
				machineUdfs.put("LASTIDLETIME", TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
			}
			else
			{
				machineUdfs.put("LASTIDLETIME", "");
			}

			MakeMachineStateByStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, machineStateName);
			transitionInfo.setUdfs(machineUdfs);
			MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(machineData, transitionInfo, eventInfo);

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
	}
	
    private void sendMessageToEM(EventInfo eventInfo, String[] userGroup, String title, String machineName, String unitName, 
			String machineStateName, String reasonCode, String oldMachineState) 
    {
		try
		{																
			//String[] userGroup = userList.split(";");
			String detailtitle = "${}CIM系统消息通知";

			StringBuffer info = new StringBuffer();
			info.append("<pre>=======================MachineInformation=======================</pre>");
			info.append("<pre>	MachineName："+machineName+"</pre>");
			info.append("<pre>	UnitName："+unitName+"</pre>");
			info.append("<pre>	MachineStateName："+machineStateName+"</pre>");
			info.append("<pre>	ReasonCode："+reasonCode+"</pre>");
			info.append("<pre>	OldMachineState："+oldMachineState+"</pre>");
			info.append("<pre>===============================End==============================</pre>");			
			
			String message = info.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userGroup, title, detailtitle, message, "");
			
			StringBuffer weChatInfo = new StringBuffer();
			weChatInfo.append("<pre>====MachineInformation=====</pre>");
			weChatInfo.append("<pre>MachineName："+machineName+"</pre>");
			weChatInfo.append("<pre>    UnitName："+unitName+"</pre>");
			weChatInfo.append("<pre>	MachineStateName："+machineStateName+"</pre>");
			weChatInfo.append("<pre>	ReasonCode："+reasonCode+"</pre>");
			weChatInfo.append("<pre>	OldMachineState："+oldMachineState+"</pre>");
			weChatInfo.append("<pre>	======MachineInfoEnd======</pre>");
			
			String weChatMessage = weChatInfo.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().weChatSend(eventInfo, userGroup, title, detailtitle, weChatMessage, "");	
		}
		catch (Exception e)
		{
			log.info("eMobile or WeChat Send Error : " + e.getCause());	
		}
	}
	    
}