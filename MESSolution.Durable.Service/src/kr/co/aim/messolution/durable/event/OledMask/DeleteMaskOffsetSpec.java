package kr.co.aim.messolution.durable.event.OledMask;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskOffsetSpec;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class DeleteMaskOffsetSpec extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {

		List<Element> MaskOffsetList = SMessageUtil.getBodySequenceItemList(doc, "MASKLOTLIST", true);

		try{
			for(Element MaskOffset:MaskOffsetList){
				
				String maskLotName = SMessageUtil.getChildText(MaskOffset, "MASKLOTNAME", true);

				EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteMaskOffsetSpec", getEventUser(), getEventComment(), "", "");
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				
				MaskOffsetSpec dataInfo = ExtendedObjectProxy.getMaskOffsetSpecService().selectByKey(false,new Object[]{maskLotName});
				
				ExtendedObjectProxy.getMaskOffsetSpecService().remove(eventInfo, dataInfo);
			}
		}catch(Exception ex)
		{
		    throw new CustomException(ex.getCause());	
		}
	
		return doc;
	}

}
