package kr.co.aim.messolution.lot.event.CNX.PostCell;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.info.SetEventInfo;

import org.jdom.Document;

public class ChangePackingComment extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		String processGroupName = SMessageUtil.getBodyItemValue(doc, "PROCESSGROUPNAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangePackingComment", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		ProcessGroup processGroup = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(processGroupName);

		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("PACKINGCOMMENT", eventInfo.getEventComment());

		MESProcessGroupServiceProxy.getProcessGroupServiceImpl().setEvent(processGroup, setEventInfo, eventInfo);

		return doc;

	}
}
