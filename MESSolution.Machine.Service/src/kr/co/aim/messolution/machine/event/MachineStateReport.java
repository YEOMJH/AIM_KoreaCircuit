package kr.co.aim.messolution.machine.event;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeCommunicationStateInfo;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;

public class MachineStateReport extends AsyncHandler {

	Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String machineStateName = SMessageUtil.getBodyItemValue(doc, "MACHINESTATENAME", true);
		String communicationState = SMessageUtil.getBodyItemValue(doc, "COMMUNICATIONSTATE", true);
		String operationMode = SMessageUtil.getBodyItemValue(doc, "OPERATIONMODE", false);
		String softwareVersion = SMessageUtil.getBodyItemValue(doc, "SOFTWAREVERSION", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");

		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		String oldMachineState = machineData.getMachineStateName();// old

	/*	if (StringUtils.equals(machineData.getMachineGroupName(), GenericServiceProxy.getConstantMap().MachineGroup_EVA))
		{
			throw new CustomException("MACHINE-0034", machineName, oldMachineState);
		}*/

		if (StringUtil.equals(machineData.getUdfs().get("CHANGESTATELOCKBYOIC"), "Y"))
		{
			throw new CustomException("MACHINE-0036", machineStateName, oldMachineState);
		}

		// if MACHINESTATAE and COMMUNICATIONSTATE is same, Pass
		if (StringUtil.equals(machineStateName, machineData.getMachineStateName()) && StringUtil.equals(communicationState, machineData.getCommunicationState()))
		{
			throw new CustomException("MACHINE-0001", machineName, machineData.getMachineStateName() + ", " + machineData.getCommunicationState(), machineStateName + ", " + communicationState);
		}

		Map<String, String> setEventUdfs = new HashMap<String, String>();
		boolean setEventFlag = false ;
		
		//change machine Operation mode
		if (machineData.getUdfs().get("OPERATIONMODE").equals(operationMode))
		{
			log.info("OperationMode is same so do not changed");
		}
		else
		{
			setEventUdfs.put("OPERATIONMODE", operationMode);
			setEventFlag = true;
		}
		
		//change machine Software version
		if (machineData.getUdfs().get("SOFTWAREVERSION").equals(softwareVersion))
		{
			log.info("Software version is same so do not changed");
		}
		else
		{
			setEventUdfs.put("SOFTWAREVERSION", softwareVersion);
			setEventFlag = true;
		}
		
		if (setEventFlag) 
		{
			kr.co.aim.greentrack.machine.management.info.SetEventInfo setEventInfo = MESMachineServiceProxy.getMachineInfoUtil().setEventInfo(setEventUdfs);
			MESMachineServiceProxy.getMachineServiceImpl().setEvent(machineData, setEventInfo, eventInfo);
		}
		
		machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
		Map<String, String> apiEventUdfs = new HashMap<String, String>();
		
		if (machineStateName.equalsIgnoreCase("IDLE"))
		{
			apiEventUdfs.put("LASTIDLETIME", TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		}
		else
		{
			apiEventUdfs.put("LASTIDLETIME", "");
		}
		
		//Start 20210121 houxk
		apiEventUdfs.put("MACHINESUBSTATE", "");
		//End

		// Change machineState
		if (!StringUtil.equals(machineStateName, machineData.getMachineStateName()))
		{
			MakeMachineStateByStateInfo makeMachineStateByStateInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, machineStateName);
			makeMachineStateByStateInfo.setUdfs(apiEventUdfs);
			MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(machineData, makeMachineStateByStateInfo, eventInfo);
		}

		// Change communicationState
		if (!StringUtil.equals(communicationState, machineData.getCommunicationState()))
		{
			MakeCommunicationStateInfo makeCommunicationStateInfo = MESMachineServiceProxy.getMachineInfoUtil().makeCommunicationStateInfo(machineData, communicationState);
			makeCommunicationStateInfo.setUdfs(apiEventUdfs);
			
			try
			{
				MESMachineServiceProxy.getMachineServiceImpl().makeCommunicationState(machineData, makeCommunicationStateInfo, eventInfo);
			}
			catch (Exception ex)
			{
				if (ex instanceof CustomException)
					eventLog.info(((CustomException) ex).errorDef.getLoc_errorMessage());
				else
					eventLog.info(ex.getMessage());
			}
		}

		//To-do send to AMS: need cim confirm
		try{}
		
		
		catch (Exception e) {
			
			// success then report to FMB
			GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);

			// send to PMS
			GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("PMS"), doc, "PMSSender");
		}
			
			
		}
		
}
