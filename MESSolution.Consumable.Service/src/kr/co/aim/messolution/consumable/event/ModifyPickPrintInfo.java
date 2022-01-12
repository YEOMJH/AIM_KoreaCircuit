package kr.co.aim.messolution.consumable.event;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SorterPickPrintInfo;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ModifyPickPrintInfo extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> pickList = SMessageUtil.getBodySequenceItemList(doc, "PICKLIST", true);

		List<SorterPickPrintInfo> updateDataList = new ArrayList<SorterPickPrintInfo>();
		List<SorterPickPrintInfo> insertDataList = new ArrayList<SorterPickPrintInfo>();

		EventInfo createInfo = EventInfoUtil.makeEventInfo("Create", this.getEventUser(), this.getEventComment());
		EventInfo updateInfo = this.copyEventInfo("Update", createInfo);
		
		for (Element pick : pickList)
		{
			String lotName = SMessageUtil.getChildText(pick, "LOTNAME", true);
			String pickMode = SMessageUtil.getChildText(pick, "PICKPRINTMODE", false);

			SorterPickPrintInfo dataInfo = ExtendedObjectProxy.getSorterPickPrintInfoService().getDataInfoByKey(lotName, false);

			if (dataInfo == null)
			{
				// insert
				dataInfo = new SorterPickPrintInfo();
				dataInfo.setLotName(lotName);
				dataInfo.setPickPrintMode(pickMode);
				insertDataList.add(dataInfo);
			}
			else
			{
				if(dataInfo.getPickPrintMode().equals(pickMode)) continue;

				// modify
				dataInfo.setPickPrintMode(pickMode);
				updateDataList.add(dataInfo);
			}
		}

		if (insertDataList.size() > 0)
			ExtendedObjectProxy.getSorterPickPrintInfoService().create(createInfo, insertDataList);

		if (updateDataList.size() > 0)
			ExtendedObjectProxy.getSorterPickPrintInfoService().modify(updateInfo, updateDataList);

		return doc;
	}
	
	public EventInfo copyEventInfo(String newEventName,EventInfo sourceInfo)
	{
		EventInfo eventInfo = new EventInfo();

		eventInfo.setBehaviorName("greenTrack");
		eventInfo.setEventName(newEventName);
		eventInfo.setEventUser(sourceInfo.getEventUser());
		eventInfo.setEventComment(sourceInfo.getEventComment());
		eventInfo.setEventTime(sourceInfo.getEventTime());
		eventInfo.setCheckTimekeyValidation(false);
		eventInfo.setEventTimeKey(sourceInfo.getEventTimeKey());
		eventInfo.setReasonCodeType("");
		eventInfo.setReasonCode("");

		return eventInfo;
	}
}
