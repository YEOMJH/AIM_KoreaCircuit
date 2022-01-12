package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.List;
import java.util.Map;

import org.jdom.Document;

import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ChangeReserveProductSpecState extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String processOperationGroupName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONGROUPNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String reserveState = SMessageUtil.getBodyItemValue(doc, "RESERVESTATE", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		// Validation .
		// 1. one EQP can has one Executing Spec
		if (StringUtil.equals(reserveState, "Executing"))
		{
			List<Map<String, Object>> reserveProductSpecList = MESDSPServiceProxy.getDSPServiceUtil().getReserveProductSpecList(machineName);

			for (Map<String, Object> reserveProductSpecM : reserveProductSpecList)
			{
				if (StringUtil.equals((String) reserveProductSpecM.get("RESERVESTATE"), "Executing"))
					throw new CustomException("SPEC-8002", machineName);
			}
		}

		MESDSPServiceProxy.getDSPServiceImpl().updateReserveProductSpec(eventInfo, machineName, processOperationGroupName, processOperationName, productSpecName, "SAME", reserveState, "SAME", "SAME");

		return doc;
	}

}
