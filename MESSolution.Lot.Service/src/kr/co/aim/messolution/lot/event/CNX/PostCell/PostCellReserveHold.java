package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotFutureAction;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class PostCellReserveHold extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		List<Element> ReasonCodeList = SMessageUtil.getBodySequenceItemList(doc, "REASONCODELIST", true);

		boolean actionFlag = false;

		EventInfo eventInfo = new EventInfo();
		eventInfo = EventInfoUtil.makeEventInfo("FutureHold", getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		// SPC ReserveHold
		for (Element code : ReasonCodeList)
		{
			String reasonCodeType = SMessageUtil.getChildText(code, "REASONCODETYPE", true);
			String reasonCode = SMessageUtil.getChildText(code, "REASONCODE", true);

			LotFutureAction futureActionData = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionData(lotName, factoryName, processFlowName, processFlowVersion, processOperationName,
					processOperationVersion, 0, reasonCode);

			if (futureActionData == null)
			{
				ExtendedObjectProxy.getLotFutureActionService().insertLotFutureAction(eventInfo, lotName, factoryName, processFlowName, processFlowVersion, processOperationName,
						processOperationVersion, 0, reasonCode, reasonCodeType, "hold", "System", "", "", "", "False", "True", "", getEventComment(), "", getEventUser());

				actionFlag = true;
			}
		}

		if (!actionFlag)
		{
			throw new CustomException("LOT-0083");
		}

		return doc;
	}
}
