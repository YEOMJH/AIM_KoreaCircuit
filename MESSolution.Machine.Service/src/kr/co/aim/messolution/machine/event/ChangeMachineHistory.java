package kr.co.aim.messolution.machine.event;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MachineHistory;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.machine.management.data.Machine;
import org.jdom.Element;
import org.jdom.Document;

public class ChangeMachineHistory extends SyncHandler {

	public Document doWorks(Document doc) throws CustomException
	{

		//String Flag = SMessageUtil.getBodyItemValue(doc, "Flag", true);
		//String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String TIMEKEY = SMessageUtil.getBodyItemValue(doc, "TIMEKEY", true);
		String machineStateName = SMessageUtil.getBodyItemValue(doc, "MACHINESTATENAME", false);
		String machineSubState = SMessageUtil.getBodyItemValue(doc, "MACHINESUBSTATE", false);
		String sReasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", false);
		String ReasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		String eventTime = SMessageUtil.getBodyItemValue(doc, "EVENTTIME", false);
		//String OccurTime = SMessageUtil.getBodyItemValue(doc, "lblOccurTime", false);
		//String Radio = SMessageUtil.getBodyItemValue(doc, "Radio", false);
		
		List<Element> machineList = SMessageUtil.getBodySequenceItemList(doc, "MACHINELIST", true);
		
		String EventComment = getEventComment();
		String Eventuser = getEventUser();
		
		Timestamp tEventTime = Timestamp.valueOf(eventTime);	
		
		for(Element machineInfo : machineList)
		{		
			String machineName = SMessageUtil.getChildText(machineInfo, "MACHINENAME", true);
			String Flag = SMessageUtil.getChildText(machineInfo, "Flag", true);
			
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);			
			
			if (Flag.equals("Modify"))
			{	
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeHistory", getEventUser(), getEventComment(), sReasonCodeType, ReasonCode);
				
				MachineHistory machine = ExtendedObjectProxy.getMachineHistoryService().selectByKey(false, new Object[]{machineName, TIMEKEY }); 
				
				machine.setMachineName(machineName);
				machine.setMachineStateName(machineStateName);
				machine.setMACHINESUBSTATE(machineSubState);
				machine.setReasonCode(ReasonCode);
				machine.setReasonCodeType(sReasonCodeType);
				machine.setTimeKey(TIMEKEY);
				machine.setEventComment(EventComment);
				machine.setEventUser(Eventuser);							
				machine.setSYSTEMTIME(TimeStampUtil.getCurrentTimestamp());		
				machine.setEventName(eventInfo.getEventName());			
				machine.setEventTime(tEventTime);
				
				ExtendedObjectProxy.getMachineHistoryService().modify(eventInfo, machine);				
			}
			else
			{			
				MachineHistory MachineStateData = new MachineHistory();
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeHistory", getEventUser(), getEventComment(), sReasonCodeType, ReasonCode);

				MachineStateData.setMachineStateName(machineStateName);
				MachineStateData.setTimeKey(TIMEKEY);
				MachineStateData.setEventComment(EventComment);
				MachineStateData.setEventUser(Eventuser);
				MachineStateData.setReasonCode(ReasonCode);
				MachineStateData.setReasonCodeType(sReasonCodeType);
				MachineStateData.setE10State(machineData.getE10State());
				MachineStateData.setMACHINESUBSTATE(machineSubState);
				//MachineStateData.setRADIO(Radio);
				MachineStateData.setSYSTEMTIME(TimeStampUtil.getCurrentTimestamp());
				MachineStateData.setOLDE10STATE(machineData.getE10State());
				MachineStateData.setRESOURCESTATE(machineData.getResourceState());
				MachineStateData.setOLDRESOURCESTATE(machineData.getResourceState());
				MachineStateData.setMachineName(machineName);
				//MachineStateData.setOldMachineStateName(oldMachineState);
				//MachineStateData.setMachineEventName(machineData.getMachineEventName());
				MachineStateData.setCommunicationState(machineData.getCommunicationState());
				MachineStateData.setEventName(eventInfo.getEventName());
			    MachineStateData.setCancelFlag("A");
			    MachineStateData.setEventFlag(machineData.getLastEventFlag());
			    if(machineData.getUdfs().get("LASTIDLETIME") != null)
			    {
			    	MachineStateData.setLastIdleTime(Timestamp.valueOf(machineData.getUdfs().get("LASTIDLETIME")));
			    }		    
			    MachineStateData.setProcessCount(machineData.getProcessCount());
			    MachineStateData.setMCSUBJECTNAME(machineData.getUdfs().get("MCSUBJECTNAME"));
			    MachineStateData.setEventTime(tEventTime);
			    
				ExtendedObjectProxy.getMachineHistoryService().create(eventInfo, MachineStateData);
			}
		}	
		return doc;
	}
}
