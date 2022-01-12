package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.durable.service.DurableServiceUtil;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
//import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;

public class CreateFirstRun extends SyncHandler {

	private static Log log = LogFactory.getLog(CreateFirstRun.class);

	public Object doWorks(Document doc) throws CustomException
	{

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateFirstRun", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		Element eleBody = SMessageUtil.getBodyElement(doc);

		String sDurableName = SMessageUtil.getChildText(eleBody, "DURABLENAME", true); // CoverTrayName
		String sQty = SMessageUtil.getChildText(eleBody, "QUANTITY", true);

		// Check exist Durable
		Durable durable = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);

		if (!StringUtil.equals(durable.getDurableState(), GenericServiceProxy.getConstantMap().Dur_InUse))
			throw new CustomException("DURABLE-0012");

		CommonValidation.CheckDurableHoldState(durable);
		CommonValidation.CheckDurableCleanState(durable);

		try
		{
			DurableServiceProxy.getDurableService().update(durable);

			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("FIRSTRUNQTY", sQty);

			MESDurableServiceProxy.getDurableServiceImpl().setEvent(durable, setEventInfo, eventInfo);

		}
		catch (Exception e)
		{
			throw new CustomException(e.getMessage());
		}

		return doc;
	}

}
