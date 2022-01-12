package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DummyProductReserve;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;

public class ConfirmDummyProduct extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "CONFIRMDUMMYPRODUCTLIST", true);

		for(Element lot:lotList){
			
			String lotName = SMessageUtil.getChildText(lot, "LOTNAME", true);
			String seqNo = SMessageUtil.getChildText(lot, "SEQ", true);
		
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ConfirmDummyProduct", getEventUser(), getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				
			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("MERGEABLEFLAG", "Y");
			
			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
	
			MESLotServiceProxy.getLotServiceImpl().updateLotData("MERGEABLEFLAG", "Y", lotName);
			MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			DummyProductReserve dataInfo =ExtendedObjectProxy.getDummyProductReserveService().selectByKey(false, new Object[]{lotName,seqNo});
		
			
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventComment(eventInfo.getEventTimeKey());
			dataInfo.setLastEventComment(eventInfo.getEventComment());

			
			ExtendedObjectProxy.getDummyProductReserveService().modify(eventInfo, dataInfo);
		}
		
		return doc;
	}
	
}
