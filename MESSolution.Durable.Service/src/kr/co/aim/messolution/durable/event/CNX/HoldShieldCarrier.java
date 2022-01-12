package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ShieldLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class HoldShieldCarrier extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);

		List<Element> durableList = SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("HoldShieldCarrier", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());

		for (Element durable : durableList)
		{
			String durableName = SMessageUtil.getChildText(durable, "DURABLENAME", true);
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);

			CommonValidation.CheckDurableHoldState(durableData);
			checkShieldState(durableData);

			eventInfo.setReasonCodeType(reasonCodeType);
			eventInfo.setReasonCode(reasonCode);

			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("DURABLEHOLDSTATE", "Y");

			DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
		}

		return doc;
	}

	private void checkShieldState(Durable durableData) throws CustomException
	{
		if (durableData.getLotQuantity() > 0)
		{
			List<ShieldLot> shieldLotList = new ArrayList<ShieldLot>();

			String condition = " CARRIERNAME = ? AND LOTPROCESSSTATE = 'RUN' ";
			Object[] bindSet = new Object[] { durableData.getKey().getDurableName() };

			try
			{
				shieldLotList = ExtendedObjectProxy.getShieldLotService().select(condition, bindSet);
			}
			catch (Exception e)
			{
				shieldLotList = null;
			}

			if (shieldLotList != null && shieldLotList.size() > 0)
			{
				throw new CustomException("SHIELD-0015", durableData.getKey().getDurableName());
			}
		}
	}
}
