package kr.co.aim.messolution.consumable.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.IncrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeAvailableInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.GenericServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.TransitionInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangeCrateQuantity extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventName("Adjust");

		String sConsumableName = SMessageUtil.getBodyItemValue(doc, "CONSUMABLENAME", true);
		String sQuantity = SMessageUtil.getBodyItemValue(doc, "QUANTITY", true);
		String sConsumableState = SMessageUtil.getBodyItemValue(doc, "CONSUMABLESTATE", true);
		String sLoadFlag = SMessageUtil.getBodyItemValue(doc, "LOADFLAG", true);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String sPortName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", false);

		Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Consumable.class.getSimpleName());

		Consumable crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(sConsumableName);

		double oldQuantity = crateData.getQuantity();
		double newQuantity = Double.parseDouble(sQuantity);

		boolean isSameQty = false;
		boolean isSameState = false;
		boolean isSameLoadFlag = false;

		if (oldQuantity == newQuantity)
			isSameQty = true;

		if (StringUtils.equals(crateData.getConsumableState(), sConsumableState))
			isSameState = true;

		if (StringUtils.equals(crateData.getUdfs().get("LOADFLAG").toString(), sLoadFlag))
			isSameLoadFlag = true;

		if (isSameQty && isSameState && isSameLoadFlag)
			throw new CustomException("CRATE-0001", sConsumableName);

		if (isSameState && (!isSameQty || !isSameLoadFlag) && StringUtils.equals(sConsumableState, GenericServiceProxy.getConstantMap().Cons_NotAvailable))
			throw new CustomException("CRATE-0004", sConsumableState);

		TransitionInfo transitionInfo;

		if (!StringUtil.equals(crateData.getConsumableState(), sConsumableState))
		{
			if (StringUtil.equals(sConsumableState, "NotAvailable") && !StringUtil.equals(crateData.getConsumableState(), "NotAvailable"))
			{
				eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), null, null);

				MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
				MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(crateData, makeNotAvailableInfo, eventInfo);
			}
			else if (StringUtil.equals(sConsumableState, "Available") && !StringUtil.equals(crateData.getConsumableState(), "Available"))
			{
				eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), null, null);

				MakeAvailableInfo makeAvailableInfo = new MakeAvailableInfo();
				MESConsumableServiceProxy.getConsumableServiceImpl().makeAvailable(crateData, makeAvailableInfo, eventInfo);
			}
		}

		if (oldQuantity > newQuantity)
		{
			eventInfo = EventInfoUtil.makeEventInfo("AdjustQty", getEventUser(), getEventComment(), null, null);

			transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo("", "", "", "", eventInfo.getEventTimeKey(), CommonUtil.doubleSubtract(oldQuantity, newQuantity),
					udfs);

			MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(crateData, (DecrementQuantityInfo) transitionInfo, eventInfo);

			crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(crateData.getKey().getConsumableName());
		}
		else if (oldQuantity < newQuantity)
		{
			crateData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(crateData.getKey().getConsumableName());

			eventInfo = EventInfoUtil.makeEventInfo("AdjustQty", getEventUser(), getEventComment(), null, null);

			transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().incrementQuantityInfo(CommonUtil.doubleSubtract(newQuantity, oldQuantity), udfs);

			MESConsumableServiceProxy.getConsumableServiceImpl().incrementQuantity(crateData, (IncrementQuantityInfo) transitionInfo, eventInfo);
		}
		else
		{
			if (StringUtil.equals(crateData.getConsumableState(), sConsumableState) && StringUtil.equals(crateData.getUdfs().get("LOADFLAG"), sLoadFlag))
				throw new CustomException("CRATE-0001", sConsumableName);
		}

		if (StringUtil.equals(sLoadFlag, "Y") && !StringUtil.equals(crateData.getUdfs().get("LOADFLAG"), "Y"))
		{
			List<Consumable> loadedCrateList = new ArrayList<Consumable>();

			try
			{
				String condition = " WHERE LOADFLAG = ? ";
				Object[] bindSet = new Object[] { "Y" };
				loadedCrateList = ConsumableServiceProxy.getConsumableService().select(condition, bindSet);
			}
			catch (Exception e)
			{
			}

			for (Consumable loadedCrate : loadedCrateList)
			{
				if (!StringUtil.equals(loadedCrate.getKey().getConsumableName(), crateData.getKey().getConsumableName()))
				{
					eventInfo = EventInfoUtil.makeEventInfo("Deassign", getEventUser(), getEventComment(), null, null);
					SetEventInfo setEventInfo = new SetEventInfo();
					setEventInfo.getUdfs().put("LOADFLAG", "");
					setEventInfo.getUdfs().put("MACHINENAME", "");
					setEventInfo.getUdfs().put("PORTNAME", "");
					setEventInfo.getUdfs().put("UNITNAME", "");
					MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(loadedCrate.getKey().getConsumableName(), setEventInfo, eventInfo);
				}
			}

			eventInfo = EventInfoUtil.makeEventInfo("Assign", getEventUser(), getEventComment(), null, null);
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("MACHINENAME", sMachineName);
			setEventInfo.getUdfs().put("PORTNAME", sPortName);
			setEventInfo.getUdfs().put("LOADFLAG", "Y");
			MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(crateData.getKey().getConsumableName(), setEventInfo, eventInfo);
		}
		else if (!StringUtil.equals(sLoadFlag, "Y") && StringUtil.equals(crateData.getUdfs().get("LOADFLAG"), "Y"))
		{
			eventInfo = EventInfoUtil.makeEventInfo("Assign", getEventUser(), getEventComment(), null, null);
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("MACHINENAME", sMachineName);
			setEventInfo.getUdfs().put("PORTNAME", sPortName);
			setEventInfo.getUdfs().put("LOADFLAG", sLoadFlag);
			MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(crateData.getKey().getConsumableName(), setEventInfo, eventInfo);
		}

		return doc;
	}

}
