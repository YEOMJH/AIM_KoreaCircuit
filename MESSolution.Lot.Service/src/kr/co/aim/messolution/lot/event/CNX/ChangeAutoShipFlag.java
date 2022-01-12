package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;

import org.jdom.Document;

public class ChangeAutoShipFlag extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName      = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String autoShipFlag = SMessageUtil.getBodyItemValue(doc, "AUTOSHIPFLAG", true);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		if(lotData.getUdfs().get("AUTOSHIPPINGFLAG") != null && lotData.getUdfs().get("AUTOSHIPPINGFLAG").toString().equals(autoShipFlag))
			throw new CustomException("RECIPE-0001"); 

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeAutoShipFlag", getEventUser(), getEventComment(), "", "");
		eventInfo.setReasonCode(lotData.getReasonCode());
		eventInfo.setReasonCodeType(lotData.getReasonCodeType());
		
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("AUTOSHIPPINGFLAG", autoShipFlag);

		LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);

		return doc;
	}
}

