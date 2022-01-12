package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DefectProductGroup;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ModifyDefectProductGroup extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {

		List<Element> codeList = SMessageUtil.getBodySequenceItemList(doc, "DEFECTPRODUCTGROUPLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyDefectProductGroup", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		for (Element code : codeList)
		{
			String defectFuction = SMessageUtil.getChildText(code, "DEFECTFUNCTION", true);
			String factoryName = SMessageUtil.getChildText(code, "FACTORYNAME", true);
			String productSpecName = SMessageUtil.getChildText(code, "PRODUCTSPECNAME", true);
			String productSpecVersion = SMessageUtil.getChildText(code, "PRODUCTSPECVERSION", true);
			String useFlag = SMessageUtil.getChildText(code, "USEFLAG", false);

			DefectProductGroup dataInfo = ExtendedObjectProxy.getDefectProductGroupServices().selectByKey(false, new Object[] { defectFuction, factoryName, productSpecName,productSpecVersion });


			dataInfo.setUseFlag(useFlag);
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

			dataInfo = ExtendedObjectProxy.getDefectProductGroupServices().modify(eventInfo, dataInfo);
		}
		
		return doc;
	}

}
