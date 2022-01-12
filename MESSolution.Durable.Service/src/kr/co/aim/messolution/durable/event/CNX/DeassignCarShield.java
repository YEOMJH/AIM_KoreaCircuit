package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ShieldLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
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

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class DeassignCarShield extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> shieldList = SMessageUtil.getBodySequenceItemList(doc, "SHIELDLIST", true);

		List<Object[]> updateArgsList = new ArrayList<Object[]>();
		List<Object[]> insertHistArgsList = new ArrayList<Object[]>();

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeassignCarShield", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());
		
		for (Element shield : shieldList)
		{
			String shieldName = SMessageUtil.getChildText(shield, "SHIELDLOTNAME", true);

			ShieldLot shieldLotData = ExtendedObjectProxy.getShieldLotService().selectByKey(false, new Object[] { shieldName });

			CommonValidation.checkShieldLotProcessStateWait(shieldLotData);

			List<Object> updateBindList = new ArrayList<Object>();
			updateBindList.add("");
			updateBindList.add(eventInfo.getEventComment());
			updateBindList.add(eventInfo.getEventUser());
			updateBindList.add(eventInfo.getEventName());
			updateBindList.add(eventInfo.getEventTime());
			updateBindList.add(eventInfo.getEventTimeKey());
			updateBindList.add(shieldName);

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
			histBindList.add(shieldLotData.getLotState());
			histBindList.add(shieldLotData.getLotProcessState());
			histBindList.add(shieldLotData.getLotHoldState());
			histBindList.add(shieldLotData.getNodeStack());
			histBindList.add(shieldLotData.getCleanState());
			histBindList.add("");
			histBindList.add(shieldLotData.getMachineName());
			histBindList.add(shieldLotData.getChamberName());
			histBindList.add(shieldLotData.getShieldSpecName());
			histBindList.add(shieldLotData.getProcessFlowName());
			histBindList.add(shieldLotData.getProcessFlowVersion());
			histBindList.add(shieldLotData.getProcessOperationName());
			histBindList.add(shieldLotData.getProcessOperationVersion());
			histBindList.add(shieldLotData.getReasonCodeType());
			histBindList.add(shieldLotData.getReasonCode());
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
		}

		updateShieldLot(updateArgsList);
		ExtendedObjectProxy.getShieldLotService().insertHistory(insertHistArgsList);
		makeAvailable (eventInfo, shieldList);
		
		return doc;
	}
	
	private void updateShieldLot(List<Object[]> updateArgsList) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE CT_SHIELDLOT  ");
		sql.append(" SET CARRIERNAME = ? , LASTEVENTCOMMENT = ? , LASTEVENTNAME = ? , LASTEVENTUSER = ? , LASTEVENTTIME = ? , LASTEVENTTIMEKEY = ? ");
		sql.append(" WHERE SHIELDLOTNAME = ? ");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateArgsList);
	}

	private void makeAvailable (EventInfo eventInfo, List<Element> shieldList) throws CustomException
	{
		Map<String, String> sumMap = new HashMap<String, String>();
		List<String> carrierList = new ArrayList<String>();

		for (Element shield : shieldList)
		{
			int iCount = 0;

			String carrierName = SMessageUtil.getChildText(shield, "CARRIERNAME", false);

			if (StringUtils.isNotEmpty(carrierName))
			{
				String count = sumMap.get(carrierName);

				if (StringUtils.isNotEmpty(count))
				{
					iCount = Integer.parseInt(count) + 1;
				}
				else
				{
					iCount = 1;
				}

				sumMap.put(carrierName, Integer.toString(iCount));

				if (!carrierList.contains(carrierName))
					carrierList.add(carrierName);
			}
		}
		
		if (carrierList.size() > 0)
		{
			for (String carrier : carrierList)
			{
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrier);

				String count = sumMap.get(carrier);
				long lotQuantity = durableData.getLotQuantity() - Long.parseLong(count);

				durableData.setLotQuantity(lotQuantity);

				if (lotQuantity < 0)
				{
					throw new CustomException("SHIELD-0009");
				}

				if (lotQuantity == 0)
				{
					durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
				}

				DurableServiceProxy.getDurableService().update(durableData);
				SetEventInfo setEventInfo = new SetEventInfo();
				DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
			}
		}
	}
}
