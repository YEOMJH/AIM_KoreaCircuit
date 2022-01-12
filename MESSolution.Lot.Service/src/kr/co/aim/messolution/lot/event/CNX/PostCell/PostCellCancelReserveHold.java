package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
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

public class PostCellCancelReserveHold extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> operationList = SMessageUtil.getBodySequenceItemList(doc, "OPERTAIONLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelReserveHold", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		List<LotFutureAction> futureActionList = new ArrayList<LotFutureAction>();

		for (Element operaData : operationList)
		{
			String lotName = SMessageUtil.getChildText(operaData, "LOTNAME", true);
			String factoryName = SMessageUtil.getChildText(operaData, "FACTORYNAME", true);
			String processFlowName = SMessageUtil.getChildText(operaData, "PROCESSFLOWNAME", true);
			String processFlowVersion = SMessageUtil.getChildText(operaData, "PROCESSFLOWVERSION", true);
			String processOperationName = SMessageUtil.getChildText(operaData, "PROCESSOPERATIONNAME", true);
			String processOperationVersion = SMessageUtil.getChildText(operaData, "PROCESSOPERATIONVERSION", true);
			String position = SMessageUtil.getChildText(operaData, "POSITION", true);
			String reasonCode = SMessageUtil.getChildText(operaData, "REASONCODE", true);

			LotFutureAction futureActionData = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionData(lotName, factoryName, processFlowName, processFlowVersion, processOperationName,
					processOperationVersion, Integer.parseInt(position), reasonCode);

			if (futureActionData == null)
				throw new CustomException("JOB-9001", operaData.getChildText("LOTNAME").toString());

			futureActionList.add(futureActionData);
		}

		if (futureActionList.size() > 0)
			ExtendedObjectProxy.getLotFutureActionService().remove(eventInfo, futureActionList);

		return doc;
	}
}
