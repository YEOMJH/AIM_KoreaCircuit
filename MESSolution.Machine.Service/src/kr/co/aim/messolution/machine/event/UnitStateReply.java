package kr.co.aim.messolution.machine.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;

public class UnitStateReply extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{

		List<Element> unitList = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "UNITLIST", true);

		for (Element unitElement : unitList)
		{
			String machineName = SMessageUtil.getChildText(unitElement, "UNITNAME", true);
			String machineStateName = SMessageUtil.getChildText(unitElement, "UNITSTATENAME", true);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");

			// Check exist machine
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);

			try
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
			}
			catch (CustomException ex)
			{
				eventLog.warn(ex.getLocalizedMessage());
			}
		}

		// success then report to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);

		String originalSourceSubjectName = getOriginalSourceSubjectName();

		// Send Reply to OIC
		GenericServiceProxy.getESBServive().sendBySenderWithoutChangeReturnElement(originalSourceSubjectName, doc, "OICSender");
	}

}
