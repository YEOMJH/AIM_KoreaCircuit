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

public class ChangeReserveProductSpecQuantity extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String processOperationGroupName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONGROUPNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String reservedQuantity = SMessageUtil.getBodyItemValue(doc, "RESERVEDQUANTITY", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeQty", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		MESDSPServiceProxy.getDSPServiceImpl().updateReserveProductSpec(eventInfo, machineName, processOperationGroupName,
				processOperationName, productSpecName, "SAME", "SAME", reservedQuantity, "SAME");

		List<Map<String, Object>> reserveProductSpecInfo = MESDSPServiceProxy.getDSPServiceUtil().getReserveProductSpecData(
				machineName, processOperationGroupName, processOperationName, productSpecName);

		return doc;
	}

}
