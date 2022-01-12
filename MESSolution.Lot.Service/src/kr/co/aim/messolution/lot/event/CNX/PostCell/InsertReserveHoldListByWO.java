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

public class InsertReserveHoldListByWO  extends SyncHandler{

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		
		
		List<Element> reserveHoldList = SMessageUtil.getBodySequenceItemList(doc, "RESERVEHOLDBYWOLIST", true);

		for (Element reserveHold : reserveHoldList)
		{
			String productRequestName = SMessageUtil.getChildText(reserveHold, "PRODUCTREQUESTNAME", true);
			String processOperationName = SMessageUtil.getChildText(reserveHold, "PROCESSOPERATIONNAME", true);
			String panelGrade = SMessageUtil.getChildText(reserveHold, "PANELGRADE", true);
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
			eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
			eventInfo.setEventTime(TimeUtils.getCurrentTimestamp());
			eventInfo.setEventName("Create");
			ReserveHoldByWOInfo reserveHoldByWOData =ExtendedObjectProxy.ReserveHoldByWOInfoService().getReserveHoldByWOInfoData(productRequestName, processOperationName, panelGrade);

			if (reserveHoldByWOData == null)
			{
				ReserveHoldByWOInfo dataInfo= new ReserveHoldByWOInfo();
				dataInfo.setProductRequestName(productRequestName);
				dataInfo.setProcessOperation(processOperationName);
				dataInfo.setPanelGrade(panelGrade);
				dataInfo.setLastEventName(eventInfo.getEventName());
				dataInfo.setLastEventTime(eventInfo.getEventTime());
				dataInfo.setLastEventUser(eventInfo.getEventUser());
				dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
				dataInfo.setLastEventComment(eventInfo.getEventComment());
				ExtendedObjectProxy.ReserveHoldByWOInfoService().create(eventInfo, dataInfo);
				
			}
		}

		return doc;
	}

}
