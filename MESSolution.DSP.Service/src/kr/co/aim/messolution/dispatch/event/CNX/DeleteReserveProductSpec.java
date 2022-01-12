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

public class DeleteReserveProductSpec extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String processOperationGroupName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONGROUPNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Delete", getEventUser(), getEventComment(), null, null);

		List<Map<String, Object>> reserveProductSpecInfo = MESDSPServiceProxy.getDSPServiceUtil().getReserveProductSpecData(
				machineName, processOperationGroupName, processOperationName, productSpecName);

		if (!StringUtil.equals((String) reserveProductSpecInfo.get(0).get("RESERVESTATE"), "Reserved"))
			throw new CustomException("SPEC-8001");

		MESDSPServiceProxy.getDSPServiceImpl().deleteReserveProductSpec(eventInfo, machineName, processOperationGroupName, processOperationName, productSpecName);

		List<Map<String, Object>> reserveProductSpecList = MESDSPServiceProxy.getDSPServiceUtil().getReserveProductSpecList(machineName);

		for (Map<String, Object> reserveProductSpecM : reserveProductSpecList)
		{
			if (!StringUtil.equals(String.valueOf(reserveProductSpecM.get("SEQ")), (String) reserveProductSpecM.get("POSITION")))
			{
				eventInfo = EventInfoUtil.makeEventInfo("ChangePosition", getEventUser(), getEventComment(), null, null);

				MESDSPServiceProxy.getDSPServiceImpl().updateReserveProductSpec(eventInfo, (String) reserveProductSpecM.get("MACHINENAME"),
						(String) reserveProductSpecM.get("PROCESSOPERATIONGROUPNAME"), (String) reserveProductSpecM.get("PROCESSOPERATIONNAME"),
						(String) reserveProductSpecM.get("PRODUCTSPECNAME"), String.valueOf(Integer.valueOf(reserveProductSpecM.get("SEQ").toString())+1), "SAME", "SAME", "SAME");
			}
		}

		return doc;
	}

}
