package kr.co.aim.messolution.lot.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveRepairPolicy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ReserveRepair extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String repairFlowName = SMessageUtil.getBodyItemValue(doc, "REPAIRFLOWNAME", true);
		String repairOperationName = SMessageUtil.getBodyItemValue(doc, "REPAIROPERATIONNAME", true);
		String executeType = SMessageUtil.getBodyItemValue(doc, "EXECUTETYPE", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReserveRepair", getEventUser(), getEventComment(), null, null);
		
		if (StringUtils.equals(executeType, "INSERT"))
		{
			eventInfo.setEventName("ReserveRepair");
		}
		else if (StringUtils.equals(executeType, "DELETE"))
		{
			eventInfo.setEventName("CancelReserveRepair");
		}

		ReserveRepairPolicy dataInfo = new ReserveRepairPolicy();
		dataInfo.setFactoryName(factoryName);
		dataInfo.setProductSpecName(productSpecName);
		dataInfo.setProductSpecVersion(productSpecVersion);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion("00001");
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion("00001");
		dataInfo.setRepairFlowName(repairFlowName);
		dataInfo.setRepairFlowVersion("00001");
		dataInfo.setRepairOperationName(repairOperationName);
		dataInfo.setRepairOperationVersion("00001");
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		
		if (StringUtils.equals(executeType, "INSERT"))
		{
			ExtendedObjectProxy.getReserveRepairPolicyService().create(eventInfo, dataInfo);	
		}
		else if (StringUtils.equals(executeType, "DELETE"))
		{
			ExtendedObjectProxy.getReserveRepairPolicyService().remove(eventInfo, dataInfo);
		}
		
		return doc;
	}
}
