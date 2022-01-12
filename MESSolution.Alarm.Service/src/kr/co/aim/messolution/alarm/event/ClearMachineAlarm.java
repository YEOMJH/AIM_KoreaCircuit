package kr.co.aim.messolution.alarm.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MachineAlarmList;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ClearMachineAlarm extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> alarmList = SMessageUtil.getBodySequenceItemList(doc, "MACHINEALARMLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ClearMachineAlarm", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		for (Element alrm : alarmList)
		{
			String machineName = SMessageUtil.getChildText(alrm, "MACHINENAME", true);
			String unitName = SMessageUtil.getChildText(alrm, "UNITNAME", true);
			String alarmCode = SMessageUtil.getChildText(alrm, "ALARMCODE", true);
			String subUnitName = SMessageUtil.getChildText(alrm, "SUBUNITNAME", false);

			if (StringUtils.isEmpty(subUnitName))
			{
				subUnitName = "-";
			}

			MachineAlarmList machineAlarmData = ExtendedObjectProxy.getMachineAlarmListService().selectByKey(false, new Object[] { machineName, unitName, subUnitName, alarmCode });

			machineAlarmData.setAlarmState("CLEAR");
			machineAlarmData.setEventTimeKey(eventInfo.getEventTimeKey());
			machineAlarmData.setEventTime(eventInfo.getEventTime());
			machineAlarmData.setEventName(eventInfo.getEventName());
			machineAlarmData.setEventUser(eventInfo.getEventUser());
			machineAlarmData.setEventComment(eventInfo.getEventComment());

			ExtendedObjectProxy.getMachineAlarmListService().remove(eventInfo, machineAlarmData);
		}

		return doc;
	}

}
