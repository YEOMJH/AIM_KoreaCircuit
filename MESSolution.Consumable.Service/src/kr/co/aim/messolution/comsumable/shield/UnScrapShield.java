package kr.co.aim.messolution.comsumable.shield;

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
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class UnScrapShield extends SyncHandler {
	
	public Object doWorks(Document doc) throws CustomException
	{
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		List<Element> shieldList = SMessageUtil.getBodySequenceItemList(doc, "SHIELDLIST", true);
		//boolean carMix = Boolean.parseBoolean(SMessageUtil.getBodyItemValue(doc, "CARMIX", true));

		List<Object[]> updateArgsList = new ArrayList<Object[]>();
		List<Object[]> insertHistArgsList = new ArrayList<Object[]>();

		boolean durFlag = false;

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnScrapShield", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());

		if (StringUtil.isNotEmpty(carrierName))
		{
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

			CommonValidation.checkShieldCarrierState(durableData);
			CommonValidation.CheckDurableHoldState(durableData);
			checkCarrierCapacity(durableData, shieldList.size());
			ShieldLot durShield = ExtendedObjectProxy.getShieldLotService().checkShieldSpecNotCreate(carrierName, shieldList, durableData);

			durFlag = true;
		}

		for (Element shield : shieldList)
		{
			String shieldLotName = SMessageUtil.getChildText(shield, "SHIELDLOTNAME", true);
			ShieldLot shieldLotData = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { shieldLotName });

			CommonValidation.checkShieldLotHoldStateN(shieldLotData);

			List<Object> updateBindList = new ArrayList<Object>();
			updateBindList.add("Released");
			updateBindList.add("");
			updateBindList.add("");
			updateBindList.add(carrierName);
			updateBindList.add(eventInfo.getEventComment());
			updateBindList.add(eventInfo.getEventUser());
			updateBindList.add(eventInfo.getEventName());
			updateBindList.add(eventInfo.getEventTime());
			updateBindList.add(eventInfo.getEventTimeKey());
			updateBindList.add(shieldLotName);

			updateArgsList.add(updateBindList.toArray());

			List<Object> histBindList = new ArrayList<Object>();
			histBindList.add(shieldLotData.getShieldLotName());
			histBindList.add(eventInfo.getEventTimeKey());
			histBindList.add(shieldLotData.getLine());
			histBindList.add(shieldLotData.getChamberType());
			histBindList.add(shieldLotData.getChamberNo());
			histBindList.add(shieldLotData.getSetValue());
			histBindList.add(shieldLotData.getJudge());
			histBindList.add(shieldLotData.getFactoryName());
			histBindList.add("Released");
			histBindList.add(shieldLotData.getLotProcessState());
			histBindList.add(shieldLotData.getLotHoldState());
			histBindList.add(shieldLotData.getNodeStack());
			histBindList.add(shieldLotData.getCleanState());
			histBindList.add(carrierName);
			histBindList.add(shieldLotData.getMachineName());
			histBindList.add(shieldLotData.getChamberName());
			histBindList.add(shieldLotData.getShieldSpecName());
			histBindList.add(shieldLotData.getProcessFlowName());
			histBindList.add(shieldLotData.getProcessFlowVersion());
			histBindList.add(shieldLotData.getProcessOperationName());
			histBindList.add(shieldLotData.getProcessOperationVersion());
			histBindList.add("");
			histBindList.add("");
			histBindList.add(shieldLotData.getReworkState());
			histBindList.add(shieldLotData.getReworkCount());
			histBindList.add(shieldLotData.getLastLoggedInTime());
			histBindList.add(shieldLotData.getLastLoggedInUser());
			histBindList.add(shieldLotData.getLastLoggedOutTime());
			histBindList.add(shieldLotData.getLastLoggedOutUser());
			histBindList.add(shieldLotData.getSampleFlag());
			histBindList.add(shieldLotData.getCreateTime());
			histBindList.add(eventInfo.getEventComment());
			histBindList.add(eventInfo.getEventName());
			histBindList.add(eventInfo.getEventUser());
			histBindList.add(eventInfo.getEventTime());
			histBindList.add(shieldLotData.getCarGroupName());
			histBindList.add(shieldLotData.getBasketGroupName());

			insertHistArgsList.add(histBindList.toArray());
			
			//20210218 houxk
			if(durFlag && StringUtil.isNotEmpty(shieldLotData.getCarrierName()))
			{
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(shieldLotData.getCarrierName());
				long lotQuantity = durableData.getLotQuantity() - 1;
				durableData.setLotQuantity(lotQuantity);
				if (lotQuantity < 1)
				{
					durableData.setDurableState("Available");
				}
				DurableServiceProxy.getDurableService().update(durableData);
				SetEventInfo setEventInfo = new SetEventInfo();
				DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
			}
		}

		if (durFlag)
		{
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
			durableData.setLotQuantity(durableData.getLotQuantity() + shieldList.size());

			if (StringUtils.equals(durableData.getDurableState(), "Available"))
				durableData.setDurableState("InUse");

			DurableServiceProxy.getDurableService().update(durableData);
			SetEventInfo setEventInfo = new SetEventInfo();
			DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
		}

		updateShieldLot(updateArgsList);
		ExtendedObjectProxy.getShieldLotService().insertHistory(insertHistArgsList);

		return doc;
	}

	private void checkCarrierCapacity(Durable durableData, int lotQuantity) throws CustomException
	{

		if (durableData.getCapacity() < durableData.getLotQuantity() + lotQuantity)
		{
			throw new CustomException("SHIELD-0018", durableData.getKey().getDurableName());
		}
	}

	private void updateShieldLot(List<Object[]> updateArgsList) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE CT_SHIELDLOT  ");
		sql.append(" SET LOTSTATE = ? , REASONCODETYPE = ? , REASONCODE = ? , CARRIERNAME = ? , LASTEVENTCOMMENT = ? , LASTEVENTNAME = ? , LASTEVENTUSER = ? , LASTEVENTTIME = ? , LASTEVENTTIMEKEY = ? ");
		sql.append(" WHERE SHIELDLOTNAME = ? ");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateArgsList);
	}
}
