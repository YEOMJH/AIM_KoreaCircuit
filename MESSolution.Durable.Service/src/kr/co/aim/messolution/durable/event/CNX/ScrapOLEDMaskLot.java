package kr.co.aim.messolution.durable.event.CNX;

import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ScrapOLEDMaskLot extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		Element eleBody = SMessageUtil.getBodyElement(doc);

		if (eleBody != null)
		{
			List<Element> eleMaskList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", false);

			for (Element eleMask : eleMaskList)
			{
				String maskLotName = eleMask.getChildText("MASKLOTNAME");
				String maskLotState = eleMask.getChildText("MASKLOTSTATE");
				String reasonCodeType = eleMask.getChildText("REASONCODETYPE");
				String reasonCode = eleMask.getChildText("REASONCODE");

				MaskLot maskLotData=ExtendedObjectProxy.getMaskLotService().selectByKey(false,new Object[] {maskLotName} );
                CommonValidation.checkMaskLotHoldState(maskLotData);
				CommonValidation.checkMaskLotState(maskLotData);
				if(!maskLotData.getMaskLotProcessState().equals("WAIT"))
				{
					throw new CustomException("MASK-0079");
				}
				EventInfo eventInfoForScrap = EventInfoUtil.makeEventInfo("ScrapMask", this.getEventUser(), this.getEventComment(), reasonCodeType, reasonCode, "Y");
				eventInfoForScrap.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

				ExtendedObjectProxy.getMaskLotService().maskStateChange(eventInfoForScrap, maskLotName, maskLotState);
			}
		}
		return doc;
	}
}
