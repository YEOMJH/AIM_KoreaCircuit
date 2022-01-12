package kr.co.aim.messolution.durable.event.CNX;

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

public class CancelReserveHoldMask extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String maskLotName = SMessageUtil.getBodyItemValue(doc, "MASKLOTNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		List<Element> reasonCodeList = SMessageUtil.getBodySequenceItemList(doc, "REASONCODELIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteFutureHold", getEventUser(), getEventComment(), "", "");

		boolean atLeastOneFutureActionExist = false;
		for (Element reasonCode : reasonCodeList)
		{
			String code = SMessageUtil.getChildText(reasonCode, "REASONCODE", true);
			List<MaskFutureAction> futureActionList = null;
			try
			{
				String condition = " WHERE MASKLOTNAME = ?  AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND REASONCODE = ? ";
				Object[] bindSet = new Object[] { maskLotName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, code};
				futureActionList = ExtendedObjectProxy.getMaskFutureActionService().select(condition, bindSet);
			}
			catch (greenFrameDBErrorSignal nfdes)
			{
			}

			if (futureActionList == null)
				continue;
			else
				atLeastOneFutureActionExist = true;

			for (MaskFutureAction futureAction : futureActionList)
			{
				ExtendedObjectProxy.getMaskFutureActionService().remove(eventInfo, futureAction);
			}
		}

		if (!atLeastOneFutureActionExist)
			throw new CustomException("OLEDMASK-0006");

		return doc;
	}
}
