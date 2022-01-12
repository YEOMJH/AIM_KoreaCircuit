package kr.co.aim.messolution.durable.event.CNX;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class UnscrapOLEDMaskLot extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		Element eleBody = SMessageUtil.getBodyElement(doc);
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		if (eleBody != null)
		{
			List<Element> eleMaskList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", false);

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnScrapMask", this.getEventUser(), this.getEventComment(), "", "", "Y");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			for (Element eleMask : eleMaskList)
			{		
				String maskLotName = eleMask.getChildText("MASKLOTNAME");
				String maskLotState = eleMask.getChildText("MASKLOTSTATE");
				MaskLot maskLotData=ExtendedObjectProxy.getMaskLotService().selectByKey(false,new Object[] {maskLotName} );
				if(!maskLotData.getMaskLotState().equals(constantMap.MaskLotState_Scrapped))
                {
                	throw new CustomException("MASK-0085",maskLotName);
	            }
				ExtendedObjectProxy.getMaskLotService().maskStateChange(eventInfo, maskLotName, maskLotState);
			}
		}
		return doc;
	}
}
