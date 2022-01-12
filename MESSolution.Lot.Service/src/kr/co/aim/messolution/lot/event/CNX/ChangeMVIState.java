package kr.co.aim.messolution.lot.event.CNX;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.data.MachineHistory;

public class ChangeMVIState extends SyncHandler {

	@Override
	public Document doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String machineStateName = SMessageUtil.getBodyItemValue(doc, "MACHINESTATENAME", true);
		String machineSubState = SMessageUtil.getBodyItemValue(doc, "MACHINESUBSTATE", false);
		String sReasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", false);
		String sReasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		List<Element> MachineList = SMessageUtil.getBodySequenceItemList(doc, "MACHINELIST", true);

		List<Machine> machineList = new ArrayList<Machine>();
		List<MachineHistory> machineHistoryList = new ArrayList<MachineHistory>();
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeMVIMachineState", this.getEventUser(), this.getEventComment(), sReasonCodeType, sReasonCode);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		for (Element Machine : MachineList)
		{
			try
			{
				String machineName = SMessageUtil.getChildText(Machine, "MACHINENAME", true);
				Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
				Map<String, String> machineUdfs = machineData.getUdfs();
				machineUdfs.put("MACHINESUBSTATE", machineSubState);

				Machine newMachineData = (Machine) ObjectUtil.copyTo(machineData);
				newMachineData.setMachineStateName(machineStateName);
				newMachineData.setLastEventComment(eventInfo.getEventComment());
				newMachineData.setLastEventFlag("N");
				newMachineData.setLastEventName(eventInfo.getEventName());
				newMachineData.setLastEventTime(eventInfo.getEventTime());
				newMachineData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				newMachineData.setLastEventUser(eventInfo.getEventUser());
				newMachineData.setReasonCode(eventInfo.getReasonCode());
				newMachineData.setReasonCodeType(eventInfo.getReasonCodeType());
				newMachineData.setUdfs(machineUdfs);
				MachineHistory HistoryData = new MachineHistory();
				MachineServiceProxy.getMachineHistoryDataAdaptor().setHV(machineData, newMachineData, HistoryData);
				HistoryData.setCancelFlag("A");
				machineList.add(newMachineData);
				machineHistoryList.add(HistoryData);
			}
			catch (Exception e)
			{
				eventLog.info("Failed to Change Machine State to " + machineStateName);
			}
		}

		// success then report to FMB
		GenericServiceProxy.getESBServive().sendBySenderToFMB(doc);

		// send to PMS
		GenericServiceProxy.getESBServive().sendBySender(GenericServiceProxy.getESBServive().getSendSubject("PMS"), doc, "PMSSender");
		if (machineList.size() > 0)
		{
			try
			{
				// update machine info and history
				CommonUtil.executeBatch("update", machineList);
				CommonUtil.executeBatch("insert", machineHistoryList);
				eventLog.info(machineList.size() + " Machine State updated");
			}
			catch (Exception e)
			{
				throw new CustomException(e.getCause());
			}
		}

		eventLog.info("changeMachineStateChange-End");
		return doc;
	}

}
