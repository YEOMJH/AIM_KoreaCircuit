package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class DeleteMVIDefectCode extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> DEFECTLIST = SMessageUtil.getBodySequenceItemList(doc, "DELETELIST", true);
		
		for (Element DEFECT : DEFECTLIST)
		{
			String productSpecName = SMessageUtil.getChildText(DEFECT, "PRODUCTSPECNAME", true);
			String defectCode = SMessageUtil.getChildText(DEFECT, "DEFECTCODE", true);
			String superDefectCode = SMessageUtil.getChildText(DEFECT, "SUPERDEFECTCODE", true);
			String levelNo = SMessageUtil.getChildText(DEFECT, "LEVELNO", false);

			//EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteMVIDefectCode", getEventUser(), getEventComment(), "", "");
			//eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		//String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		//String superDefectCode = SMessageUtil.getBodyItemValue(doc, "SUPERDEFECTCODE", true);
		//String defectCode = SMessageUtil.getBodyItemValue(doc, "DEFECTCODE", true);
		//String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", true);
		//String levelNo = SMessageUtil.getBodyItemValue(doc, "LEVELNO", true);
		

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteMVIDefectCode", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		
			
		ExtendedObjectProxy.getMVIDefectCodeService().deleteMVIDefectCode(eventInfo, productSpecName, defectCode, superDefectCode);
		
		}
		return doc;
	}

}
