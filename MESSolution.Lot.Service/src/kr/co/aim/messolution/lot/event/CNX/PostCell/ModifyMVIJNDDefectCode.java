package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class ModifyMVIJNDDefectCode extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);

		List<Element> defectCodeList = SMessageUtil.getBodySequenceItemList(doc, "DEFECTCODELIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyMVIJNDDefectCode", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		for (Element defect : defectCodeList)
		{
			String defectCode = SMessageUtil.getChildText(defect, "DEFECTCODE", true);
			String superDefectCode = SMessageUtil.getChildText(defect, "SUPERDEFECTCODE", true);
			String pattern = SMessageUtil.getChildText(defect, "PATTERN", true);
			String jndName = SMessageUtil.getChildText(defect, "JNDNAME", true);
			String panelGrade = SMessageUtil.getChildText(defect, "PANELGRADE", true);
			String sign = SMessageUtil.getChildText(defect, "SIGN", true);

			ExtendedObjectProxy.getMVIJNDDefectCodeService().ModifySign(eventInfo, productSpecName, defectCode, superDefectCode, pattern, jndName, panelGrade, sign);
		}

		return doc;
	}

}
