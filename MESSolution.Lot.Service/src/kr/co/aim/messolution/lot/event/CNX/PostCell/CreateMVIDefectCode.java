package kr.co.aim.messolution.lot.event.CNX.PostCell;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class CreateMVIDefectCode extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String superDefectCode = SMessageUtil.getBodyItemValue(doc, "SUPERDEFECTCODE", true);
		String defectCode = SMessageUtil.getBodyItemValue(doc, "DEFECTCODE", true);
		String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", true);
		String levelNo = SMessageUtil.getBodyItemValue(doc, "LEVELNO", true);
		String conditionFlag = SMessageUtil.getBodyItemValue(doc, "CONDITIONFLAG", false);
		String panelGrade = SMessageUtil.getBodyItemValue(doc, "PANELGRADE", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateMVIDefectCode", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

	/*	if (StringUtils.equals(levelNo, "2"))
		{
			if (StringUtils.isEmpty(conditionFlag))
				throw new CustomException("DURABLE-0014");

			if (StringUtils.isEmpty(panelGrade))
				throw new CustomException("DURABLE-0014");
		}*/

		ExtendedObjectProxy.getMVIDefectCodeService().createMVIDefectCode(eventInfo, productSpecName, defectCode, description, superDefectCode, Long.parseLong(levelNo), conditionFlag, panelGrade);

		return doc;
	}
}
