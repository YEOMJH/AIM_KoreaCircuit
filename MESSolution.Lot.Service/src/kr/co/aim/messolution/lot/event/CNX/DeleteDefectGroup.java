package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DefectGroup;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class DeleteDefectGroup extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		List<Element> codeList = SMessageUtil.getBodySequenceItemList(doc, "DELETECODELIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteDefectGroup", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		for (Element code : codeList)
		{
			String defectGroupName = SMessageUtil.getChildText(code, "DEFECTGROUPNAME", true);
			String defectCode = SMessageUtil.getChildText(code, "DEFECTCODE", true);
			String judge = SMessageUtil.getChildText(code, "JUDGE", true);

			DefectGroup dataInfo = ExtendedObjectProxy.getDefectGroupServices().selectByKey(false, new Object[] { defectGroupName, defectCode, judge  });
			
			ExtendedObjectProxy.getDefectGroupServices().remove(eventInfo, dataInfo);
		}
		
		return doc;
	}

}
