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
import kr.co.aim.greentrack.generic.info.EventInfo;

public class DeleteWOPatternFilmInfo extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> assignList = SMessageUtil.getBodySequenceItemList(doc, "ASSIGNLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteWOPatternFilmInfo", getEventUser(), getEventComment(), "", "");

		for (Element assign : assignList)
		{
			String productSpecName = SMessageUtil.getChildText(assign, "PRODUCTSPECNAME", true);
			String productSpecVersion = SMessageUtil.getChildText(assign, "PRODUCTSPECVERSION", true);
			String processFlowName = SMessageUtil.getChildText(assign, "PROCESSFLOWNAME", true);
			String processFlowVersion = SMessageUtil.getChildText(assign, "PROCESSFLOWVERSION", true);
			String processOperationName = SMessageUtil.getChildText(assign, "PROCESSOPERATIONNAME", true);
			String processOperationVersion = SMessageUtil.getChildText(assign, "PROCESSOPERATIONVERSION", true);
			String machineName = SMessageUtil.getChildText(assign, "MACHINENAME", true);
			String productRequestName = SMessageUtil.getChildText(assign, "PRODUCTREQUESTNAME", true);
			String recipeName = SMessageUtil.getChildText(assign, "RECIPENAME", true);
			WOPatternFilmInfo dataInfo = ExtendedObjectProxy.getWOPatternFilmInfoService().getWOPatternFilmInfoData(productSpecName, productSpecVersion,processFlowName,processFlowVersion,processOperationName,processOperationVersion,machineName, productRequestName);

			if (dataInfo == null)
			{
				throw new CustomException("POSTCELL-0001");
			}

			ExtendedObjectProxy.getWOPatternFilmInfoService().remove(eventInfo, dataInfo);
		}

		return doc;
	}

}
