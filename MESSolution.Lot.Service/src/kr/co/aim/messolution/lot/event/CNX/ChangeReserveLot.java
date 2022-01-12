package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class ChangeReserveLot extends SyncHandler {

	private static Log log = LogFactory.getLog(ChangeReserveLot.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);

		List<Element> reserveLotList = SMessageUtil.getBodySequenceItemList(doc, "RESERVELOTLIST", true);

		ConstantMap contantMap = GenericServiceProxy.getConstantMap();
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		for (Element reserveLot : reserveLotList)
		{
			String position = SMessageUtil.getChildText(reserveLot, "POSITION", true);
			String lotName = SMessageUtil.getChildText(reserveLot, "LOTNAME", true);

			Long lPosition = Long.parseLong(position);

			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
			
			ReserveLot reserveLotData = ExtendedObjectProxy.getReserveLotService().selectByKey(false, new Object[] { lotData.getKey().getLotName(), "-", "-" });

			if (StringUtils.equals(reserveLotData.getReserveState(), contantMap.RESV_STATE_CREATE))
			{
				log.info("Change State/Position : Reserved/" + lPosition);

				eventInfo.setBehaviorName("ReserveLot");

				reserveLotData.setPosition(lPosition);
				reserveLotData.setReserveState(contantMap.RESV_STATE_RESV);
				reserveLotData.setMachineName(machineName);

				ExtendedObjectProxy.getReserveLotService().modify(eventInfo, reserveLotData);
			}
			else if (reserveLotData.getPosition() != lPosition)
			{
				log.info("Change position : " + lPosition);
				
				eventInfo.setBehaviorName("ChangePosition");
				
				reserveLotData.setPosition(lPosition);

				ExtendedObjectProxy.getReserveLotService().modify(eventInfo, reserveLotData);
			}
		}

		return doc;
	}
}
