package kr.co.aim.messolution.dispatch.event.CNX;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;

import org.jdom.Document;

public class ChangeLotDSPFlag extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String LotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String DSPFlag = SMessageUtil.getBodyItemValue(doc, "DSPFLAG", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeLotDSPFlag", getEventUser(), getEventComment(), "", "");

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(LotName);

		if (!StringUtil.equals(lotData.getUdfs().get("DSPFLAG"), DSPFlag))
		{
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("DSPFLAG", DSPFlag);

			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
		}
		return doc;
	}
}
