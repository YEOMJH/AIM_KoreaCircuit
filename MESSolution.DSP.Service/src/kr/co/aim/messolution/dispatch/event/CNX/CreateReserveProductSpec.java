package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;
import java.util.Map;

import org.jdom.Document;

import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class CreateReserveProductSpec extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String processOperationGroupName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONGROUPNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		List<Map<String, Object>> reserveProductSpecInfo = MESDSPServiceProxy.getDSPServiceUtil().getReserveProductSpecData(machineName, processOperationGroupName, processOperationName,
				productSpecName);

		if (reserveProductSpecInfo.size() > 0)
			throw new CustomException("SPEC-0001", productSpecName);

		String nextPosition = MESDSPServiceProxy.getDSPServiceUtil().getNextPositionForReserveProductSpec(machineName);

		MESDSPServiceProxy.getDSPServiceImpl().insertReserveProductSpec(eventInfo, machineName, processOperationGroupName, processOperationName, productSpecName, nextPosition, "Reserved", "0", "0");

		return doc;
	}

}
