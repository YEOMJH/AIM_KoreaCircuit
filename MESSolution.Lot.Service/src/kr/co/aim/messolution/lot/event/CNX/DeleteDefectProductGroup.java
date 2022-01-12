package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DefectGroup;
import kr.co.aim.messolution.extended.object.management.data.DefectProductGroup;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class DeleteDefectProductGroup extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		List<Element> codeList = SMessageUtil.getBodySequenceItemList(doc, "DELETEGROUPLIST", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteDefectProductGroup", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		for (Element code : codeList)
		{
			String defectFunction = SMessageUtil.getChildText(code, "DEFECTFUNCTION", true);
			String factoryName = SMessageUtil.getChildText(code, "FACTORYNAME", true);
			String productSpecName = SMessageUtil.getChildText(code, "PRODUCTSPECNAME", true);
			String productSpecVersion = SMessageUtil.getChildText(code, "PRODUCTSPECVERSION", true);

			DefectProductGroup dataInfo = ExtendedObjectProxy.getDefectProductGroupServices().selectByKey(false, new Object[] { defectFunction, factoryName, productSpecName,productSpecVersion  });
			
			ExtendedObjectProxy.getDefectProductGroupServices().remove(eventInfo, dataInfo);
		}
		
		return doc;
	}

}
