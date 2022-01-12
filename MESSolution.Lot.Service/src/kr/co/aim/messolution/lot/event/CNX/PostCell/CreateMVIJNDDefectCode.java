package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import org.jdom.Element;

import org.jdom.Document;

public class CreateMVIJNDDefectCode extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> defectCodeList = SMessageUtil.getBodySequenceItemList(doc, "CREATEMVIJNDCODELIST", false);
		if(defectCodeList.size()>0){
			for (Element defect : defectCodeList)
			{
			String	 productSpecName = SMessageUtil.getChildText(defect, "PRODUCTSPECNAME", true);
			String	 superDefectCode =SMessageUtil.getChildText(defect, "SUPERDEFECTCODE", true);
			String	 superDefectCodeDescription =SMessageUtil.getChildText(defect, "SUPERDEFECTCODEDESCRIPTION", true);
			String	 defectCode =SMessageUtil.getChildText(defect, "DEFECTCODE", true);
			String	 defectCodeDescription =SMessageUtil.getChildText(defect, "DEFECTCODEDESCRIPTION", true);
			String	 pattern =SMessageUtil.getChildText(defect, "PATTERN", true);
			String	 jndName =SMessageUtil.getChildText(defect, "JNDNAME", true);
			String	 panelGrade = SMessageUtil.getChildText(defect, "PANELGRADE", true);
			String   sign ="";
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateMVIJNDDefectCode", getEventUser(), getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			ExtendedObjectProxy.getMVIJNDDefectCodeService().CreateMVIJNDDefectCode(eventInfo, productSpecName, defectCode, defectCodeDescription, superDefectCode, superDefectCodeDescription, pattern,
					jndName, sign, panelGrade);

		     }
			
			
		}else{
			String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
			String superDefectCode = SMessageUtil.getBodyItemValue(doc, "SUPERDEFECTCODE", true);
			String superDefectCodeDescription = SMessageUtil.getBodyItemValue(doc, "SUPERDEFECTCODEDESCRIPTION", true);
			String defectCode = SMessageUtil.getBodyItemValue(doc, "DEFECTCODE", true);
			String defectCodeDescription = SMessageUtil.getBodyItemValue(doc, "DEFECTCODEDESCRIPTION", true);
			String pattern = SMessageUtil.getBodyItemValue(doc, "PATTERN", true);
			String jndName = SMessageUtil.getBodyItemValue(doc, "JNDNAME", true);
			String sign = SMessageUtil.getBodyItemValue(doc, "SIGN", false);
			String panelGrade = SMessageUtil.getBodyItemValue(doc, "PANELGRADE", true);
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateMVIJNDDefectCode", getEventUser(), getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			ExtendedObjectProxy.getMVIJNDDefectCodeService().CreateMVIJNDDefectCode(eventInfo, productSpecName, defectCode, defectCodeDescription, superDefectCode, superDefectCodeDescription, pattern,
					jndName, sign, panelGrade);

			
		}

		return doc;
	}

}
