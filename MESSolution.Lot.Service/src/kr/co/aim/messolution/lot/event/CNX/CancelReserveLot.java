package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class CancelReserveLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);

		List<Element> reserveLotList = SMessageUtil.getBodySequenceItemList(doc, "RESERVELOTLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelReserveLot", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		for (Element reserveLot : reserveLotList)
		{
			String lotName = SMessageUtil.getChildText(reserveLot, "LOTNAME", true);
			ExtendedObjectProxy.getReserveLotService().cancelReserveLot(eventInfo, lotName, machineName);
		}

		return doc;
	}

}
