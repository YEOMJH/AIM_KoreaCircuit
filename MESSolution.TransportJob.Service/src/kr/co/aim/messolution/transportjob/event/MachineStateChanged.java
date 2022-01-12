package kr.co.aim.messolution.transportjob.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.util.CommonUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;

public class MachineStateChanged extends AsyncHandler {
	Log log = LogFactory.getLog(this.getClass());
	/**
	 * MessageSpec [MCS -> TEX -> FMC]
	 * 
	 * <Body>
	 *    <MACHINENAME />
	 *    <MACHINESTATE />
	 *    <FULLSTATE />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		// Validation : Exist Carrier
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String machineState = SMessageUtil.getBodyItemValue(doc, "MACHINESTATE", true);
		String fullState = SMessageUtil.getBodyItemValue(doc, "FULLSTATE", false);

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

		String oldFullState = machineData.getUdfs().get("FULLSTATE");
		String oldMachineState = machineData.getMachineStateName();

		MakeMachineStateByStateInfo makeMachineStateByStateInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, machineState);

		if (StringUtil.equals(machineData.getUdfs().get("CHANGESTATELOCKBYOIC"), "Y"))
		{
			throw new CustomException("MACHINE-0036", machineState, oldMachineState);
		}

		try
		{
			// Same Value Check
			if (!StringUtil.equals(machineData.getMachineStateName(), machineState) || !StringUtil.equals(oldFullState, fullState))
			{
				Map<String, String> udfs = new HashMap<String, String>();
				udfs.put("FULLSTATE", fullState);
				udfs.put("MACHINESUBSTATE", "");
//				if (CommonUtil.equalsIn(StringUtils.upperCase(machineState), "MANUAL", "DOWN"))
//					udfs.put("CHANGESTATELOCKBYOIC", "Y");		
				
				if(machineState.equalsIgnoreCase("DOWN"))
				{
					//send message to em & wechat
					String[] userGroup = MESMachineServiceProxy.getMachineInfoUtil().getAlarmUserIdByUnitName(machineName);
					if(userGroup != null)
					{
						sendMessageToEM(eventInfo,userGroup,"",machineName,machineState,oldMachineState);
					}else
					{
						log.info("when machinestate is down,there is no user need to send emoblie&wechat");
					}
				}
				
				makeMachineStateByStateInfo.setUdfs(udfs);

				MachineServiceProxy.getMachineService().makeMachineStateByState(machineData.getKey(), eventInfo, makeMachineStateByStateInfo);
			}
			else
			{
				throw new CustomException("MACHINE-0001", machineData.getKey().getMachineName(), machineData.getMachineStateName() + "/" + oldFullState, machineState + "/" + fullState);
			}
		}
		catch (InvalidStateTransitionSignal ie)
		{
			throw new CustomException("MACHINE-9003", machineData.getKey().getMachineName());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-9999", machineData.getKey().getMachineName());
		}
		catch (DuplicateNameSignal de)
		{
			throw new CustomException("MACHINE-9002", machineData.getKey().getMachineName());
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("MACHINE-9001", machineData.getKey().getMachineName());
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
	
	private void sendMessageToEM(EventInfo eventInfo, String[] userGroup, String title, String machineName, 
			String machineStateName, String oldMachineState) {
		try
		{																
			log.info("usergroup:" + userGroup.toString());
			String detailtitle = "${}CIM系统消息通知";

			StringBuffer info = new StringBuffer();
			info.append("<pre>=======================MachineInformation=======================</pre>");
			info.append("<pre>	MachineName："+machineName+"</pre>");
			info.append("<pre>	MachineStateName："+machineStateName+"</pre>");
			info.append("<pre>	OldMachineState："+oldMachineState+"</pre>");
			info.append("<pre>===============================End==============================</pre>");			
			
			String message = info.toString();
			
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().eMobileSend(eventInfo, userGroup, title, detailtitle, message, "");
			
			StringBuffer weChatInfo = new StringBuffer();
			weChatInfo.append("<pre>====MachineInformation=====</pre>");
			weChatInfo.append("<pre>MachineName："+machineName+"</pre>");
			weChatInfo.append("<pre>	MachineStateName："+machineStateName+"</pre>");
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