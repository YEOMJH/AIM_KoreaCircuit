package kr.co.aim.messolution.machine.event;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.EnumDefValue;
import kr.co.aim.messolution.extended.object.management.data.MachineIdleByChamber;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class InsertMachineIdleByChamber extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> chamberList = SMessageUtil.getBodySequenceItemList(doc, "CHAMBERLIST", false);
		String type = SMessageUtil.getBodyItemValue(doc, "EVENTNAME", false);

		if (!type.equals("ChangeReleaseFLag")) 
		{
			for (Element chamber : chamberList) 
			{
				String machineName = SMessageUtil.getChildText(chamber, "MACHINENAME", true);
				String chamberName = SMessageUtil.getChildText(chamber, "CHAMBERNAME", true);
				String processOperationName = SMessageUtil.getChildText(chamber, "PROCESSOPERATIONNAME", true);
				String processOperationVersion = SMessageUtil.getChildText(chamber, "PROCESSOPERATIONVERSION", true);
				String recipeName = SMessageUtil.getChildText(chamber, "RECIPENAME", false);

				EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
				eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());

				MachineIdleByChamber machineIdleByChamberData = ExtendedObjectProxy.getMachineIdleByChamberService()
						.getMachineIdleByChamber(machineName, chamberName, processOperationName,
								processOperationVersion);

				if (machineIdleByChamberData == null) 
				{
					eventInfo.setEventName("Create");
					ExtendedObjectProxy.getMachineIdleByChamberService().createMachineIdleByChamber(eventInfo,
							machineName, chamberName, processOperationName, processOperationVersion, "ChamberIdle",
							recipeName, "", "", "", "");
				}
			}
		} 
		else 
		{
			String flag = SMessageUtil.getBodyItemValue(doc, "FLAG", true);
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			
			if (flag.equals("Y") && CommonUtil.getEnumDefValueStringByEnumNameAndEnumValue("IDLETimeByChamberRunRelease", machineName).isEmpty())
			{
				EnumDefValue dataInfo = new EnumDefValue();
				
				dataInfo.setEnumName("IDLETimeByChamberRunRelease");
				dataInfo.setEnumValue(machineName);
				dataInfo.setDefaultFlag("Y");
				dataInfo.setDescription(getEventUser());
				
				ExtendedObjectProxy.getEnumDefValueService().create(dataInfo);
			}
			
			if(flag.equals("N") && !CommonUtil.getEnumDefValueStringByEnumNameAndEnumValue("IDLETimeByChamberRunRelease", machineName).isEmpty())
			{
				ExtendedObjectProxy.getEnumDefValueService().remove("IDLETimeByChamberRunRelease", machineName);;
			}
		}

		return doc;
	}

}