package kr.co.aim.messolution.dispatch.event.CNX;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.TryRunControl;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class CreateTryRunControl extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String porcessOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", false);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", false);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", false);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", false);
		String actionType = SMessageUtil.getBodyItemValue(doc, "ACTIONTYPE", false);
		String useFlag = SMessageUtil.getBodyItemValue(doc, "USEFLAG", false);
		String strUseCount = SMessageUtil.getBodyItemValue(doc, "USECOUNT", false);
		String strUseCountLimit = SMessageUtil.getBodyItemValue(doc, "USECOUNTLIMIT", false);

		int useCount = (!StringUtil.equals(strUseCount, "")) ? Integer.parseInt(strUseCount) : 0;
		int useCountLimit = (!StringUtil.equals(strUseCountLimit, "")) ? Integer.parseInt(strUseCountLimit) : 0;

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateTryRunControl", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		TryRunControl dataInfo = new TryRunControl();

		dataInfo.setMachineName(machineName);
		dataInfo.setProcessOperationName(processOperationName);
		dataInfo.setProcessOperationVersion(porcessOperationVersion);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setProductSpecName(productSpecName);
		dataInfo.setProductSpecVersion(productSpecVersion);
		dataInfo.setProcessFlowName(processFlowName);
		dataInfo.setProcessFlowVersion(processFlowVersion);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setActionType(actionType);
		dataInfo.setUseFlag(useFlag);
		dataInfo.setUseCount(useCount);
		dataInfo.setUseCountLimit(useCountLimit);
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		ExtendedObjectProxy.getTryRunControlService().create(eventInfo, dataInfo);

		return doc;
	}

}
