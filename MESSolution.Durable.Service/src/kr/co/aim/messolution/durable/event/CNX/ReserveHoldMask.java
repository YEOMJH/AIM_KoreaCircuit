package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskFutureAction;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class ReserveHoldMask extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);
		List<Element> maskList = SMessageUtil.getBodySequenceItemList(doc, "MASKLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("InsertFutureHold", getEventUser(), getEventComment(), "", "");

		List<MaskFutureAction> futureActionList = new ArrayList<MaskFutureAction>();
		for (Element mask : maskList)
		{
			String maskLotName = SMessageUtil.getChildText(mask, "MASKLOTNAME", true);
			MaskFutureAction futureAction = null;
			try
			{
				futureAction = ExtendedObjectProxy.getMaskFutureActionService().selectByKey(false,
						new Object[] { maskLotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, 0, reasonCodeType, reasonCode });
			}
			catch (greenFrameDBErrorSignal nfdes)
			{
			}

			if (futureAction != null)
				throw new CustomException("OLEDMASK-0005");

			futureAction = new MaskFutureAction(maskLotName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, 0, reasonCodeType, reasonCode, "hold",
					"System", eventInfo.getEventTime(), eventInfo.getEventName(), eventInfo.getEventUser(), eventInfo.getEventComment());
			futureActionList.add(futureAction);
		}

		ExtendedObjectProxy.getMaskFutureActionService().create(eventInfo, futureActionList);

		return doc;
	}
}
