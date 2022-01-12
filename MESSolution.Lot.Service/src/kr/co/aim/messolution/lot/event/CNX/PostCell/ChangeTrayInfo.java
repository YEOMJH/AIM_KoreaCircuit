package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangeTrayInfo extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String trayName = SMessageUtil.getBodyItemValue(doc, "TRAYNAME", true);
		String bcrFlag = SMessageUtil.getBodyItemValue(doc, "BCRFLAG", false);
		String coverYN = SMessageUtil.getBodyItemValue(doc, "COVERYN", false);
		String ngFlag = SMessageUtil.getBodyItemValue(doc, "NGFLAG", false);
		String operationMode = SMessageUtil.getBodyItemValue(doc, "OPERATIONMODE", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeTrayInfo", getEventUser(), getEventComment(), "", "");
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
		ConstantMap constMap = GenericServiceProxy.getConstantMap();

		boolean bcr = false;
		boolean ng = false;
		boolean op = false;
		boolean cover = false;

		if (!StringUtils.equals(durableData.getUdfs().get("BCRFLAG"), bcrFlag))
			bcr = true;

		if (!StringUtils.equals(durableData.getUdfs().get("COVERYN"), coverYN))
			cover = true;

		if (!StringUtils.equals(durableData.getUdfs().get("NGFLAG"), ngFlag))
			ng = true;

		if (!StringUtils.equals(durableData.getUdfs().get("OPERATIONMODE"), operationMode))
			op = true;

		SetEventInfo setEventInfo = new SetEventInfo();

		if (cover)
		{
			CommonValidation.CheckDurableState(durableData);

			if ("Y".equals(coverYN))
			{
				CommonValidation.CheckEmptyCoverName(durableData);

				setEventInfo.getUdfs().put("DURABLETYPE1", "CoverTray");
				setEventInfo.getUdfs().put("COVERNAME", trayName);
				durableData.setDurableType("CoverTray");

				if (!constMap.Dur_InUse.equals(durableData.getDurableState()))
					durableData.setDurableState(constMap.Dur_InUse);
			}
			else if ("N".equals(coverYN))
			{
				if (!durableData.getDurableType().equals("CoverTray"))
					throw new CustomException("DURABLE-0009", durableData.getKey().getDurableName());

				eventInfo = EventInfoUtil.makeEventInfo("DeassignTrayGroup", getEventUser(), getEventComment());
				List<Durable> trayList = MESDurableServiceProxy.getDurableServiceUtil().getSubTrayListByCoverTray(durableData.getKey().getDurableName(), false);

				if (trayList.size() == 0)
				{
					if (durableData.getLotQuantity() > 0)
					{
						eventInfo.setEventName("DeassignTray");
						List<Lot> lotDataList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayName(durableData.getKey().getDurableName(), false);

						List<Lot> updateLotList = new ArrayList<>();
						List<LotHistory> updateHistList = new ArrayList<>();

						for (Lot lotData : lotDataList)
						{
							CommonValidation.checkLotProcessState(lotData);
							CommonValidation.checkLotHoldState(lotData);
							Lot oldLot = (Lot) ObjectUtil.copyTo(lotData);

							lotData.setCarrierName("");
							lotData.setLastEventName(eventInfo.getEventName());
							lotData.setLastEventUser(eventInfo.getEventUser());
							lotData.setLastEventTime(eventInfo.getEventTime());
							lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
							lotData.setLastEventComment(eventInfo.getEventComment());

							LotHistory lotHistData = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, new LotHistory());

							updateLotList.add(lotData);
							updateHistList.add(lotHistData);
						}

						if (updateLotList.size() > 0)
						{
							try
							{
								CommonUtil.executeBatch("update", updateLotList, true);
								CommonUtil.executeBatch("insert", updateHistList, true);
							}
							catch (Exception e)
							{
								throw new CustomException(e.getCause());
							}
						}
					}
				}
				else
				{
					for (Durable trayData : trayList)
					{
						CommonValidation.CheckDurableState(trayData);

						if (trayData.getLotQuantity() == 0)
						{
							trayData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
						}
						else
						{
							trayData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
							List<Lot> lotDataList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayName(trayData.getKey().getDurableName(), false);
							for (Lot lotData : lotDataList)
							{
								CommonValidation.checkLotProcessState(lotData);
								CommonValidation.checkLotHoldState(lotData);
							}
						}
						DurableServiceProxy.getDurableService().update(trayData);

						SetEventInfo deassinInfo = new SetEventInfo();
						deassinInfo.getUdfs().put("COVERNAME", "");
						deassinInfo.getUdfs().put("POSITION", "");
						deassinInfo.getUdfs().put("DURABLETYPE1", "Tray");
						MESDurableServiceProxy.getDurableServiceImpl().setEvent(trayData, deassinInfo, eventInfo);
					}
				}

				durableData.setDurableType("Tray");
				durableData.setLotQuantity(0);
				durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);

				setEventInfo.getUdfs().put("COVERNAME", "");
				setEventInfo.getUdfs().put("POSITION", "");
				setEventInfo.getUdfs().put("DURABLETYPE1", "Tray");
			}
		}

		if (bcr)
		{
			if (StringUtils.equals("Y", bcrFlag))
			{
				if (StringUtils.equals(durableData.getDurableType(), "CoverTray"))
				{
					List<Durable> trayList = MESDurableServiceProxy.getDurableServiceUtil().getSubTrayListByCoverTray(durableData.getKey().getDurableName(), false);

					for (Durable trayData : trayList)
					{
						trayData.setCapacity(GenericServiceProxy.getConstantMap().BCRTrayCapacity);

						SetEventInfo trayEventInfo = new SetEventInfo();
						trayEventInfo.getUdfs().put("BCRFLAG", bcrFlag);

						DurableServiceProxy.getDurableService().update(trayData);
						DurableServiceProxy.getDurableService().setEvent(trayData.getKey(), eventInfo, trayEventInfo);
					}
				}
				else
				{
					durableData.setCapacity(GenericServiceProxy.getConstantMap().BCRTrayCapacity);
					setEventInfo.getUdfs().put("BCRFLAG", bcrFlag);
				}
			}
			else
			{
				if (durableData.getDurableType().equals("CoverTray"))
				{
					List<Durable> trayList = MESDurableServiceProxy.getDurableServiceUtil().getSubTrayListByCoverTray(durableData.getKey().getDurableName(), false);

					for (Durable trayData : trayList)
					{
						DurableSpec durableSpecData = GenericServiceProxy.getSpecUtil().getDurableSpec(durableData.getFactoryName(), durableData.getDurableSpecName(),
								durableData.getDurableSpecVersion());

						trayData.setCapacity(durableSpecData.getDefaultCapacity());

						SetEventInfo trayEventInfo = new SetEventInfo();
						trayEventInfo.getUdfs().put("BCRFLAG", bcrFlag);

						DurableServiceProxy.getDurableService().update(trayData);
						DurableServiceProxy.getDurableService().setEvent(trayData.getKey(), eventInfo, trayEventInfo);
					}
				}
				else
				{
					DurableSpec durableSpecData = GenericServiceProxy.getSpecUtil().getDurableSpec(durableData.getFactoryName(), durableData.getDurableSpecName(), durableData.getDurableSpecVersion());

					durableData.setCapacity(durableSpecData.getDefaultCapacity());
					setEventInfo.getUdfs().put("BCRFLAG", bcrFlag);
				}
			}
		}

		if (ng)
			setEventInfo.getUdfs().put("NGFLAG", ngFlag);

		if (op)
		{
			if (StringUtils.equals(durableData.getDurableType(), "CoverTray"))
			{
				List<Durable> trayList = MESDurableServiceProxy.getDurableServiceUtil().getSubTrayListByCoverTray(durableData.getKey().getDurableName(), false);

				for (Durable trayData : trayList)
				{
					SetEventInfo trayEventInfo = new SetEventInfo();
					trayEventInfo.getUdfs().put("OPERATIONMODE", operationMode);

					DurableServiceProxy.getDurableService().setEvent(trayData.getKey(), eventInfo, trayEventInfo);
				}
			}

			setEventInfo.getUdfs().put("OPERATIONMODE", operationMode);
		}

		DurableServiceProxy.getDurableService().update(durableData);
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);

		return doc;
	}
}
