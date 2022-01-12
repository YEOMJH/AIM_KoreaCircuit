package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.BorrowPanel;
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
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class OutBorrowPanel extends SyncHandler {
	private static Log log = LogFactory.getLog(OutBorrowPanel.class);
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String taskID = SMessageUtil.getBodyItemValue(doc, "TASKID", true);
		String taskState = SMessageUtil.getBodyItemValue(doc, "TASKSTATE", true);
		//String coverName = SMessageUtil.getBodyItemValue(doc, "COVERNAME", true);

		List<Element> panelList = SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", true);

		Element bodyElement = SMessageUtil.getBodyElement(doc);
		List<String> panelNameList = CommonUtil.makeList(bodyElement, "PANELLIST", "LOTNAME");

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("OutBorrowPanel", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		List<String> trayList = new ArrayList<String>();

		for (Element panel : panelList)
		{
			String lotName = SMessageUtil.getChildText(panel, "LOTNAME", true);

			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

			CommonValidation.checkLotProcessState(lotData);
			//CommonValidation.checkLotStateScrapped(lotData);
			CommonValidation.checkLotHoldState(lotData);

			ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());

			if (!StringUtils.equals(operationData.getDetailProcessOperationType(), "ScrapPack"))
				throw new CustomException("BORROW-0003", lotData.getKey().getLotName());

			BorrowPanel dataInfo = ExtendedObjectProxy.getBorrowPanelService().getBorrowPanelData(taskID, lotName);

			if (dataInfo == null)
				throw new CustomException("BORROW-0001", taskID, lotName);

			if (!StringUtils.equals(dataInfo.getBorrowState(), taskState))
				throw new CustomException("BORROW-0002", taskState);

			if (!StringUtils.equals(dataInfo.getBorrowState(), constantMap.Borrow_Confirmed))
				throw new CustomException("BORROW-0006", dataInfo.getTaskId(), dataInfo.getLotName());

			/*Durable trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());

			if (!trayList.contains(trayData.getKey().getDurableName()))
				trayList.add(trayData.getKey().getDurableName());*/
			
			//PanelOutFlag-Y BorrowDate BorrowState-Borrowed
			/*String lotState = lotData.getLotState();
			String lotGrade = lotData.getLotGrade();
			Map<String, String> udfs = dataInfo.getUdfs();
			udfs.put("BEFORELOTSTATE", lotState);
			udfs.put("BEFORELOTGRADE", lotGrade);
			dataInfo.setUdfs(udfs);
			ExtendedObjectProxy.getBorrowPanelService().changePanelOutFlag(eventInfo, dataInfo);
			lotData.setLotState("Borrowed");
			LotServiceProxy.getLotService().update(lotData);
			kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);*/
			ExtendedObjectProxy.getBorrowPanelService().changePanelOutFlag(eventInfo, dataInfo);
			String state = "";
			state = constantMap.Borrow_Borrowed;
			ExtendedObjectProxy.getBorrowPanelService().changeState(eventInfo, dataInfo, state);
			
			String lotState = lotData.getLotState();
			String lotGrade = lotData.getLotGrade();
			Map<String, String> udfs = dataInfo.getUdfs();
			udfs.put("BEFORELOTSTATE", lotState);
			udfs.put("BEFORELOTGRADE", lotGrade);
			dataInfo.setUdfs(udfs);
			ExtendedObjectProxy.getBorrowPanelService().changePanelOutFlag(eventInfo, dataInfo);
			lotData.setLotState(lotState);
			LotServiceProxy.getLotService().update(lotData);
			kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
			
		}
		panelHoldForBorrowed(panelNameList);
		/*JINLJ202012/22
		Durable coverTrayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(coverName);

		if (!StringUtils.equals(coverTrayData.getUdfs().get("TRANSPORTSTATE"), "OUTSTK"))
			throw new CustomException("BORROW-0007", coverTrayData.getKey().getDurableName());

		long totalDecrementQty = 0;

		for (String tray : trayList)
		{
			Durable trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(tray);
			long decrementQty = 0;

			for (String panel : panelNameList)
			{
				Lot panelData = MESLotServiceProxy.getLotServiceUtil().getLotData(panel);

				if (StringUtils.equals(tray, panelData.getCarrierName()))
					decrementQty += 1;
				
				panelData.setCarrierName("");
				LotServiceProxy.getLotService().update(panelData);
				kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
				LotServiceProxy.getLotService().setEvent(panelData.getKey(), eventInfo, setEventInfo);
			}

			trayData.setLotQuantity(trayData.getLotQuantity() - decrementQty);

			SetEventInfo setEventInfo = new SetEventInfo();
			
			if (trayData.getLotQuantity() == 0)
			{
				trayData.setDurableState(constantMap.Dur_Available);
				setEventInfo.getUdfs().put("COVERNAME", "");
				setEventInfo.getUdfs().put("POSITION", "");
				setEventInfo.getUdfs().put("DURABLETYPE1", "Tray");
			}

			DurableServiceProxy.getDurableService().update(trayData);
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(trayData, setEventInfo, eventInfo);

			totalDecrementQty += decrementQty;
		}

		SetEventInfo setCoverEventInfo = new SetEventInfo();

		coverTrayData.setLotQuantity(coverTrayData.getLotQuantity() - totalDecrementQty);

		if (coverTrayData.getLotQuantity() == 0)
		{
			coverTrayData.setDurableType("Tray");
			coverTrayData.setDurableState(constantMap.Dur_Available);

			setCoverEventInfo.getUdfs().put("COVERNAME", "");
			setCoverEventInfo.getUdfs().put("POSITION", "");
			setCoverEventInfo.getUdfs().put("DURABLETYPE1", "Tray");
		}

		DurableServiceProxy.getDurableService().update(coverTrayData);
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(coverTrayData, setCoverEventInfo, eventInfo);*/
		return doc;
	}
	private void panelHoldForBorrowed(List<String> panelNameList) throws CustomException
	{
		try
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Hold", this.getEventUser(), this.getEventComment());

			StringBuilder sql = new StringBuilder();
			sql.append("SELECT * ");
			sql.append(" FROM LOT ");
			sql.append(" WHERE LOTNAME IN (:LOTLIST) ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTLIST", panelNameList);

			List<Map<String, Object>> resultList = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), bindMap);

			if (resultList.size() > 0)
			{
				String reasonCode = "HD100";
				String reasonCodeType = "HOLD";
				ConstantMap constantMap = GenericServiceProxy.getConstantMap();

				eventInfo.setEventComment("Hold Panel by Borrowed");
				eventInfo.setReasonCode(reasonCode);
				eventInfo.setReasonCodeType(reasonCodeType);
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
				List<Lot> updateLotList = new ArrayList<>();
				List<LotHistory> updateHistList = new ArrayList<>();
				List<Lot> lotDataList = LotServiceProxy.getLotService().transform(resultList);
				for (Lot lotData : lotDataList)
				{
				Lot oldLot = (Lot) ObjectUtil.copyTo(lotData);
               
				lotData.setLotHoldState(constantMap.Flag_Y);
				lotData.setReasonCode(reasonCode);
				lotData.setReasonCodeType(reasonCodeType);
				lotData.setLastEventName(eventInfo.getEventName());
				lotData.setLastEventTime(eventInfo.getEventTime());
				lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				lotData.setLastEventUser(eventInfo.getEventUser());
				lotData.setLastEventComment(eventInfo.getEventComment());

				LotHistory lotHist = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, new LotHistory());

				updateLotList.add(lotData);
				updateHistList.add(lotHist);
				}

				try
				{
					CommonUtil.executeBatch("update", updateLotList, true);
					CommonUtil.executeBatch("insert", updateHistList, true);

					log.info(String.format("?Successfully hold %s pieces of panels.", updateLotList.size()));
				}
				catch (Exception e)
				{
					log.error(e.getMessage());
					throw new CustomException(e.getCause());
				}
		   }
		}
		catch (Exception e)
		{
			log.info("Error Occurred - Hold Panel by Borrowed");
		}
	}
}
