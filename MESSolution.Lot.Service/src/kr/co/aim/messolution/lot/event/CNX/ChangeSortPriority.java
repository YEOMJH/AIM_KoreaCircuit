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

public class ChangeSortPriority extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeSortPriority", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		List<Element> LotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);

		for (Element LotE : LotList)
		{
			String jobName = LotE.getChild("JOBNAME").getText();
			String priority = LotE.getChild("PRIORITY").getText();

			ExtendedObjectProxy.getSortJobService().updateSortJob(eventInfo, jobName, "", "", priority, "", "");
		}

		return doc;
	}

}
