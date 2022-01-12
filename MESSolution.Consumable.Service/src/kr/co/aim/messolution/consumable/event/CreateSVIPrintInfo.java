package kr.co.aim.messolution.consumable.event;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SVIPickInfo;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import org.jdom.Document;

public class CreateSVIPrintInfo extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String judge = SMessageUtil.getBodyItemValue(doc, "JUDGE", true);
		String code = SMessageUtil.getBodyItemValue(doc, "DEFECTCODE", true);
		String qty = SMessageUtil.getBodyItemValue(doc, "QTY", true);
		String productSpec = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String workOrder = SMessageUtil.getBodyItemValue(doc, "WORKORDER", false);
		String point = SMessageUtil.getBodyItemValue(doc, "POINT", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateSVIPrintInfo", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		try{
			SVIPickInfo pickInfo = new SVIPickInfo();

			pickInfo.setTimeKey(eventInfo.getEventTimeKey());
			pickInfo.setMachineName(machineName);
			pickInfo.setProcessOperationName(processOperationName);
			pickInfo.setProcessOperationVersion("00001");
			pickInfo.setJudge(judge);
			pickInfo.setCode(code);
			pickInfo.setQuantity(Integer.parseInt(qty));
			pickInfo.setJudge(judge);
			pickInfo.setDownLoadFlag("N");
			pickInfo.setEndQuantity(0);
			pickInfo.setProductSpecName(productSpec);
			pickInfo.setProductSpecVersion("00001");
			pickInfo.setWorkOrder(workOrder);
			pickInfo.setPoint(point);
			
			

			pickInfo.setLastEventName(eventInfo.getEventName());
			pickInfo.setLastEventUser(eventInfo.getEventUser());
			pickInfo.setLastEventTime(eventInfo.getEventTime());
			pickInfo.setLastEventComment(eventInfo.getEventComment());
			pickInfo.setLastEventTimeKey(eventInfo.getLastEventTimekey());

			ExtendedObjectProxy.getSVIPickInfoService().create(eventInfo, pickInfo);
		}
		catch(Exception ex)
		{
		    throw new CustomException(ex.getCause());	
		}
		

		return doc;
	}
}
