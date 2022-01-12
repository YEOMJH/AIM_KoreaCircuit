package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ChangeReserveProductSpecPosition extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> reserveProductSpecList = SMessageUtil.getBodySequenceItemList(doc, "RESERVEPRODUCTSPECLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangePosition", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		for (Element reserveProductSpecE : reserveProductSpecList)
		{
			String machineName = reserveProductSpecE.getChildText("MACHINENAME");
			String productSpecName = reserveProductSpecE.getChildText("PRODUCTSPECNAME");
			String processOperationGroupName = reserveProductSpecE.getChildText("PROCESSOPERATIONGROUPNAME");
			String processOperationName = reserveProductSpecE.getChildText("PROCESSOPERATIONNAME");
			String position = reserveProductSpecE.getChildText("POSITION");

			MESDSPServiceProxy.getDSPServiceImpl().updateReserveProductSpec(eventInfo, machineName, processOperationGroupName, processOperationName, productSpecName, position, "SAME", "SAME", "SAME");
		}

		return doc;
	}

}
