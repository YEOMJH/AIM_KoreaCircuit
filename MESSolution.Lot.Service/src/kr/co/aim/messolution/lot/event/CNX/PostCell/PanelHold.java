package kr.co.aim.messolution.lot.event.CNX.PostCell;


import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import org.jdom.Document;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;


public class PanelHold  extends SyncHandler{
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String holdState = "Y";
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("PanelHold", getEventUser(), getEventComment(), "", "");
		
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
		
		lotData.setLotHoldState(holdState);
		eventInfo.setReasonCode(reasonCode);
		eventInfo.setReasonCodeType(reasonCodeType);
		lotData.setLastEventComment(eventInfo.getEventComment());
		lotData.setLastEventName(eventInfo.getEventName());
		lotData.setLastEventTime(TimeStampUtil.getTimestamp(TimeUtils.getCurrentTime(TimeUtils.FORMAT_DEFAULT)));
		lotData.setLastEventUser(eventInfo.getEventUser());
		SetEventInfo setEventInfo = new SetEventInfo();
		
		LotServiceProxy.getLotService().update(lotData);
		LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
	
	return doc;
	}

}
