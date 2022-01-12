package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MainReserveSkip;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class DeleteMainReserveSkip extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		List<Element> operationList = SMessageUtil.getBodySequenceItemList(doc, "OPERATIONLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteMainReserveSkip", getEventUser(), "", "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());

		for (Element operation : operationList)
		{
			String processOperationName = operation.getChildText("PROCESSOPERATIONNAME");
			String processOperationVersion = operation.getChildText("PROCESSOPERATIONVERSION");

			MainReserveSkip mainReserveSkipData = ExtendedObjectProxy.getMainReserveSkipService().getMainResrveSkipData(lotName, processOperationName, processOperationVersion);

			if (mainReserveSkipData != null)
			{
				ExtendedObjectProxy.getMainReserveSkipService().remove(eventInfo, mainReserveSkipData);
			}
			else
			{
				throw new CustomException("LOT-0215", lotName, processOperationName, processOperationVersion);
			}
		}

		return doc;
	}

}
