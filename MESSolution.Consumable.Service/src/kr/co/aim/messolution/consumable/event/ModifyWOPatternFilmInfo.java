package kr.co.aim.messolution.consumable.event;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.WOPatternFilmInfo;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class ModifyWOPatternFilmInfo extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", false);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", false);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", false);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", false);
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECEIPENAME", true);
		String materialSpecName = SMessageUtil.getBodyItemValue(doc, "MATERIALSPECNAME", false);
		String materialSpecVersion = SMessageUtil.getBodyItemValue(doc, "MATERIALSPECVERSION", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyWOPatternFilmInfo", getEventUser(), getEventComment(), "", "");

		WOPatternFilmInfo dataInfo = ExtendedObjectProxy.getWOPatternFilmInfoService().getWOPatternFilmInfoData(productSpecName, productSpecVersion,processFlowName,processFlowVersion,processOperationName,processOperationVersion,machineName, productRequestName);

		if (dataInfo == null)
		{
			throw new CustomException("POSTCELL-0001");
		}

		if (StringUtils.isNotEmpty(productSpecName))
		{
			dataInfo.setProcessFlowName(processFlowName);
			dataInfo.setProcessFlowVersion(processFlowVersion);
		}

		if (StringUtils.isNotEmpty(processOperationName))
		{
			dataInfo.setProcessOperationName(processOperationName);
			dataInfo.setProcessOperationVersion(processOperationVersion);
		}

		dataInfo.setFactoryName(factoryName);

		if (StringUtils.isNotEmpty(materialSpecName))
		{
			dataInfo.setMaterialSpecName(materialSpecName);
			dataInfo.setMaterialSpecVersion(materialSpecVersion);
		}
		
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		dataInfo.setLastEventTime(eventInfo.getEventTime());

		ExtendedObjectProxy.getWOPatternFilmInfoService().modify(eventInfo, dataInfo);

		return doc;
	}

}
