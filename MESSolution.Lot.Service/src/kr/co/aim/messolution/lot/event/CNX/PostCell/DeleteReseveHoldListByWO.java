package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveHoldByWOInfo;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class DeleteReseveHoldListByWO   extends SyncHandler{

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		List<Element> reserveHoldByWoList = SMessageUtil.getBodySequenceItemList(doc, "RESERVEHOLDLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Delete", getEventUser(), getEventComment(), "", "");
		for(Element reserveHoldByWo : reserveHoldByWoList)
		{
			String productRequestName = SMessageUtil.getChildText(reserveHoldByWo, "PRODUCTREQUESTNAME", true);
			String processOperationName = SMessageUtil.getChildText(reserveHoldByWo, "PROCESSOPERATIONNAME", true);
			String panelGrade = SMessageUtil.getChildText(reserveHoldByWo, "PANELGRADE", true);
			eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
			eventInfo.setEventTime(TimeUtils.getCurrentTimestamp());
			ReserveHoldByWOInfo reserveHoldByWOData =ExtendedObjectProxy.ReserveHoldByWOInfoService().getReserveHoldByWOInfoData(productRequestName, processOperationName, panelGrade);
			if (reserveHoldByWOData == null)
			{
				throw new CustomException("POSTCELL-0001");
			}

			ExtendedObjectProxy.ReserveHoldByWOInfoService().remove(eventInfo, reserveHoldByWOData);
		}

		// TODO Auto-generated method stu
		return doc;
	}

}
