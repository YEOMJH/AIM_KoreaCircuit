package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.NodeStack;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.SampleMask;
import kr.co.aim.messolution.extended.object.management.data.TPFOPolicy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;

public class ChangeMaskBoxName extends SyncHandler {

	private static Log log = LogFactory.getLog(ChangeMaskBoxName.class);

	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		List<Element> MaskList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", true);
		// List<String> maskList = new ArrayList<String>();

		for (Element Mask : MaskList)
		{
			String maskLotName = SMessageUtil.getChildText(Mask, "MASKLOTNAME", true);
			MaskLot maskLotData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskLotName });

			String maskBoxName = SMessageUtil.getChildText(Mask, "MASKBOXNAME", true);
			

			// Set Mask Info
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeMaskBoxName", this.getEventUser(), this.getEventComment(), "", "");
			
			maskLotData.setMaskBoxName(maskBoxName);
			String timeKey = TimeUtils.getCurrentEventTimeKey();
			eventInfo.setEventTimeKey(timeKey);
			eventInfo.setLastEventTimekey(timeKey);

			eventInfo.setEventUser(getEventUser());
			eventInfo.setEventName(eventInfo.getEventName());
			eventInfo.setEventComment(eventInfo.getEventComment());

			// update mask info and history
			ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskLotData);
		}

		return doc;
	}
}
