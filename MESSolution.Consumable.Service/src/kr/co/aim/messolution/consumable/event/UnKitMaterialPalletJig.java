package kr.co.aim.messolution.consumable.event;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class UnKitMaterialPalletJig extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String palletName = SMessageUtil.getBodyItemValue(doc, "PALLETNAME", true);
		String palletUnkitTime = SMessageUtil.getBodyItemValue(doc, "UNKITTIME", true);
		String palletUseTime = SMessageUtil.getBodyItemValue(doc, "USETIME", true);
		String palletUseDuration = SMessageUtil.getBodyItemValue(doc, "USEDURATION", true);
		
		List<Element> eFPCList = SMessageUtil.getBodySequenceItemList(doc, "FPCLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnKit", getEventUser(), getEventComment(), null, null);

		Durable pallet = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(palletName);
		kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfoPallet = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
		
		if (!pallet.getUdfs().get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().Dur_ONEQP))
			throw new CustomException("MATERIAL-9017", palletName);

		if (pallet.getUdfs().get("DURABLEHOLDSTATE").equals(GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_Y))
			throw new CustomException("MATERIAL-9016", palletName);
		
		pallet.setDurationUsed(Double.parseDouble(palletUseDuration));
		pallet.setTimeUsed(Double.parseDouble(palletUseTime));
		pallet.setMaterialLocationName("");
		setEventInfoPallet.getUdfs().put("MACHINENAME", "");
		setEventInfoPallet.getUdfs().put("UNITNAME", "");

		if(pallet.getTimeUsed() > pallet.getTimeUsedLimit() || pallet.getDurationUsed() > pallet.getDurationUsedLimit())
		{
			pallet.setDurableState(GenericServiceProxy.getConstantMap().Dur_NotAvailable);
		}
		else
		{
			pallet.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
		}
		
		setEventInfoPallet.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_OUTEQP);
		setEventInfoPallet.getUdfs().put("UNKITTIME", palletUnkitTime);

		// Set durableState, materialLocation
		DurableServiceProxy.getDurableService().update(pallet);
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(pallet, setEventInfoPallet, eventInfo);
		
		for (Element materialE : eFPCList)
		{
			String materialID = materialE.getChildText("MATERIALID");
			String unkitTime = materialE.getChildText("UNKITTIME");
			String useTime = materialE.getChildText("USETIME");
			String useDuration = materialE.getChildText("USEDURATION");

			eventInfo.setEventName("UnKit");
			
			Durable fpc = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(materialID);
			kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
		
			fpc.setDurationUsed(Double.parseDouble(useDuration));
			fpc.setTimeUsed(Double.parseDouble(useTime));
			fpc.setMaterialLocationName("");
			setEventInfo.getUdfs().put("MACHINENAME", "");
			setEventInfo.getUdfs().put("UNITNAME", "");
			
			if(fpc.getTimeUsed() > fpc.getTimeUsedLimit() || fpc.getDurationUsed() > fpc.getDurationUsedLimit())
			{
				fpc.setDurableState(GenericServiceProxy.getConstantMap().Dur_NotAvailable);
			}
			else
			{
				fpc.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
			}
			
			setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_OUTEQP);
			setEventInfo.getUdfs().put("UNKITTIME", unkitTime);
			
			// Set durableState, materialLocation
			DurableServiceProxy.getDurableService().update(fpc);
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(fpc, setEventInfo, eventInfo);

		}

		return doc;
	}
}
