package kr.co.aim.messolution.durable.event.CNX;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;

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

public class ScrapShieldCarrier extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);
		
		List<Element> durableList = SMessageUtil.getBodySequenceItemList(doc, "DURABLELIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ScrapShieldCarrier", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeStampUtil.getCurrentEventTimeKey());

		List<Object[]> updateArgsList = new ArrayList<Object[]>();
		List<Object[]> insertHistArgsList = new ArrayList<Object[]>();

		for (Element durable : durableList)
		{
			String durableName = SMessageUtil.getChildText(durable, "DURABLENAME", true);

			List<ShieldLot> shieldLotList = new ArrayList<ShieldLot>();

			String condition = " CARRIERNAME = ? ";
			Object[] bindSet = new Object[] { durableName };

			try
			{
				shieldLotList = ExtendedObjectProxy.getShieldLotService().select(condition, bindSet);
			}
			catch (Exception e)
			{
				shieldLotList = null;
			}

			if (shieldLotList != null)
			{
				for (ShieldLot shieldLotData : shieldLotList)
				{
					CommonValidation.checkShieldLotProcessStateWait(shieldLotData);

					List<Object> updateBindList = new ArrayList<Object>();
					updateBindList.add("");
					updateBindList.add(eventInfo.getEventComment());
					updateBindList.add(eventInfo.getEventUser());
					updateBindList.add(eventInfo.getEventName());
					updateBindList.add(eventInfo.getEventTime());
					updateBindList.add(eventInfo.getEventTimeKey());
					updateBindList.add(shieldLotData.getShieldLotName());

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
			}

			// Change DurableState
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
			
			CommonValidation.checkShieldCarrierState(durableData);
			
			durableData.setDurableState("NotAvailable");
			durableData.setLotQuantity(0);

			eventInfo.setReasonCodeType(reasonCodeType);
			eventInfo.setReasonCode(reasonCode);
			
			DurableServiceProxy.getDurableService().update(durableData);
			SetEventInfo setEventInfo = new SetEventInfo();
			DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
		}

		// DeassignCarrier
		if (updateArgsList.size() > 0 && insertHistArgsList.size() > 0)
		{
			updateShieldLot(updateArgsList);
			ExtendedObjectProxy.getShieldLotService().insertHistory(insertHistArgsList);
		}

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
}
