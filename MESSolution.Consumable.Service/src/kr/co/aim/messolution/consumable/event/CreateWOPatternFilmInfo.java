package kr.co.aim.messolution.consumable.event;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.WOPatternFilmInfo;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class CreateWOPatternFilmInfo extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String recipeName = SMessageUtil.getBodyItemValue(doc, "RECEIPENAME", true);
		String materialSpecName = SMessageUtil.getBodyItemValue(doc, "MATERIALSPECNAME", false);
		String materialSpecVersion = SMessageUtil.getBodyItemValue(doc, "MATERIALSPECVERSION", false);
		List<Element> machineList = SMessageUtil.getBodySequenceItemList(doc, "MACHINELIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateWOPatternFilmInfo", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeUtils.getCurrentTimestamp());
		for(Element machine:machineList)
		{
	      String machineName=SMessageUtil.getChildText(machine, "MACHINENAME", true);
	 	  WOPatternFilmInfo dataInfo = new WOPatternFilmInfo(productSpecName, productSpecVersion,processFlowName,processFlowVersion,processOperationName,processOperationVersion,machineName, productRequestName);
		  dataInfo.setRecipeName(recipeName);
		  dataInfo.setFactoryName(factoryName);
		  dataInfo.setMaterialSpecName(materialSpecName);
		  dataInfo.setMaterialSpecVersion(materialSpecVersion);
		  dataInfo.setLastEventUser(eventInfo.getEventUser());
		  dataInfo.setLastEventName(eventInfo.getEventName());
		  dataInfo.setLastEventComment(eventInfo.getEventComment());
		  dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		  dataInfo.setLastEventTime(eventInfo.getEventTime());
		  dataInfo.setRmsFlag("N");

		  ExtendedObjectProxy.getWOPatternFilmInfoService().create(eventInfo, dataInfo);	
		}

		return doc;
	}

}
