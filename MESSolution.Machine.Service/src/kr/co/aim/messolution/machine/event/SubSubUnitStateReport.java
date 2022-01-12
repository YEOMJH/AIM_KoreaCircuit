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

public class SubSubUnitStateReport extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), "", "");

		List<Element> unitList = SMessageUtil.getSubSequenceItemList(SMessageUtil.getBodyElement(doc), "UNITLIST", false);

		for (Element unitElement : unitList)
		{
			List<Element> subUnitList = SMessageUtil.getSubSequenceItemList(unitElement, "SUBUNITLIST", false);

			for (Element subUnitElement : subUnitList)
			{
				List<Element> subSubUnitList = SMessageUtil.getSubSequenceItemList(subUnitElement, "SUBSUBUNITLIST", false);

				for (Element subSubUnitElement : subSubUnitList)
				{
					String machineName = SMessageUtil.getChildText(subSubUnitElement, "SUBSUBUNITNAME", true);
					String machineStateName = SMessageUtil.getChildText(subSubUnitElement, "SUBSUBUNITSTATENAME", true);

					try
					{
						Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
						String oldMachineState = machineData.getMachineStateName();// old

						if (StringUtil.equals(machineStateName, "MANUAL"))
						{
							throw new CustomException("MACHINE-0035", machineStateName);
						}

						if (StringUtil.equals(machineData.getUdfs().get("CHANGESTATELOCKBYOIC"), "Y"))
						{
							throw new CustomException("MACHINE-0036", machineStateName, oldMachineState);
						}

						// if MACHINESTATAE is same, Pass
						if (StringUtil.equals(machineStateName, machineData.getMachineStateName()))
						{
							throw new CustomException("MACHINE-0001", machineName, machineData.getMachineStateName(), machineStateName);
						}

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
					catch (CustomException ce)
					{
						eventLog.warn(ce.getLocalizedMessage());
					}
				}
			}
		}

		// success then report to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);

		// send to PMS
		GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("PMS"), doc, "PMSSender");
	}

}
