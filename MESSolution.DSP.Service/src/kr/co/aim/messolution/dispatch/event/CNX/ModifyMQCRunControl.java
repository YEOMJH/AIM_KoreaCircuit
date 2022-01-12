package kr.co.aim.messolution.dispatch.event.CNX;

import org.jdom.Document;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MQCRunControl;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ModifyMQCRunControl extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		String maxProductQtyByChamber = SMessageUtil.getBodyItemValue(doc, "MAXPRODUCTQTYBYCHAMBER", true);
		String maxMQCQtyByChamber = SMessageUtil.getBodyItemValue(doc, "MAXMQCQTYBYCHAMBER", true);
		String chamberQty = SMessageUtil.getBodyItemValue(doc, "CHAMBERQTY", true);
		String mqcProcessFlowName = SMessageUtil.getBodyItemValue(doc, "MQCPROCESSFLOWNAME", true);
		String mqcProcessFlowVersion = SMessageUtil.getBodyItemValue(doc, "MQCPROCESSFLOWVERSION", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyMQCRunControl", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		MQCRunControl dataInfo = ExtendedObjectProxy.getMQCRunControlService().selectByKey(false, new Object[] { machineName, processOperationName, processOperationVersion });
		dataInfo.setMaxProductQtyByChamber(StringUtil.isEmpty(maxProductQtyByChamber) ? 0 : Integer.valueOf(maxProductQtyByChamber));
		dataInfo.setMaxMQCQtyByChamber(StringUtil.isEmpty(maxMQCQtyByChamber) ? 0 : Integer.valueOf(maxMQCQtyByChamber));
		dataInfo.setChamberQty(StringUtil.isEmpty(chamberQty) ? 0 : Integer.valueOf(chamberQty));
		dataInfo.setMqcProcessFlowName(mqcProcessFlowName);
		dataInfo.setMqcProcessFlowVersion(mqcProcessFlowVersion);
		
		ExtendedObjectProxy.getMQCRunControlService().modify(eventInfo, dataInfo);

		return doc;
	}
}
