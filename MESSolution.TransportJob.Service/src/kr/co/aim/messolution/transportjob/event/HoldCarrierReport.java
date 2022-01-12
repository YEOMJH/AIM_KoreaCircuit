package kr.co.aim.messolution.transportjob.event;

import java.util.Map;

import org.jdom.Document;

import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;

public class HoldCarrierReport extends AsyncHandler {

	/**
	 * MessageSpec [MCS -> TEX]
	 * 
	 * <Body>
	 *    <CARRIERNAME />
	 *    <REASONCODE />
	 *    <REASONCOMMENT />
	 * </Body>
	 */
	
	@Override
	public void doWorks(Document doc) throws CustomException
	{
		// CarrierDataInstalled Transaction
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		String reasonComment = SMessageUtil.getBodyItemValue(doc, "REASONCOMMENT", false);
		
		// 2021-05-17	dhko	If the value of <REASONCODE> comes to reasoncomment
		if (StringUtil.isEmpty(reasonComment))
		{
			reasonComment = reasonCode;
			reasonCode = "MCS";
		}
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("HoldCarrierReport", getEventUser(), reasonComment, "HOLD", reasonCode);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		Durable durableData = CommonValidation.checkExistCarrier(carrierName);

		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("DURABLEHOLDSTATE", "Y");
		
		DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
	}
}
