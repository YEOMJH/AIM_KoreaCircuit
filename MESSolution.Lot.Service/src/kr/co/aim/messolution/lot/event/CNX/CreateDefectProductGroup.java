package kr.co.aim.messolution.lot.event.CNX;

import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class CreateDefectProductGroup extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		String defectFuction = SMessageUtil.getBodyItemValue(doc, "DEFECTFUNCTION", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String defectGroupName = SMessageUtil.getBodyItemValue(doc, "DEFECTGROUPNAME", false);
		String useFlag = SMessageUtil.getBodyItemValue(doc, "USEFLAG", false);


		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateDefectGroup", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		ExtendedObjectProxy.getDefectProductGroupServices().createdDefectProductGroup(eventInfo, defectFuction, factoryName, productSpecName, productSpecVersion,defectGroupName,useFlag);

		return doc;
	}

}
