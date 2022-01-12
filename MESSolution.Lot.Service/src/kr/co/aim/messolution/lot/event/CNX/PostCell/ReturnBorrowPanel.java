package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class ReturnBorrowPanel extends SyncHandler {
	private static Log log = LogFactory.getLog(ReturnBorrowPanel.class);
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String taskID = SMessageUtil.getBodyItemValue(doc, "TASKID", true);
		String taskState = SMessageUtil.getBodyItemValue(doc, "TASKSTATE", true);

		List<Element> panelList = SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", true);
		Element bodyElement = SMessageUtil.getBodyElement(doc);
		List<String> panelNameList = CommonUtil.makeList(bodyElement, "PANELLIST", "LOTNAME");

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReturnBorrowPanel", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		for (Element panel : panelList)
		{
			String lotName = SMessageUtil.getChildText(panel, "LOTNAME", true);

			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

			CommonValidation.checkLotProcessState(lotData);
			//CommonValidation.checkLotStateScrapped(lotData);
			//checkLotStateBorrowed(lotData);
			//CommonValidation.checkLotHoldState(lotData);

			ProcessOperationSpec operationData = CommonUtil.getProcessOperationSpec(lotData.getFactoryName(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion());

			if (!StringUtils.equals(operationData.getDetailProcessOperationType(), "ScrapPack"))
				throw new CustomException("BORROW-0003", lotData.getKey().getLotName());

			BorrowPanel dataInfo = ExtendedObjectProxy.getBorrowPanelService().getBorrowPanelData(taskID, lotName);

			if (dataInfo == null)
				throw new CustomException("BORROW-0001", taskID, lotName);

			if (!StringUtils.equals(dataInfo.getBorrowState(), taskState))
				throw new CustomException("BORROW-0002", taskState);

			if (!StringUtils.equals(dataInfo.getBorrowState(), constantMap.Borrow_Borrowed))
				throw new CustomException("BORROW-0006", dataInfo.getTaskId(), dataInfo.getLotName());

			ExtendedObjectProxy.getBorrowPanelService().changeState(eventInfo, dataInfo, constantMap.Borrow_Completed);
			ExtendedObjectProxy.getBorrowPanelService().deleteBorrowPanel(eventInfo, taskID, lotName, constantMap.Borrow_Completed);
			String lotState = lotData.getLotState();
			lotData.setLotState(lotState);//
			LotServiceProxy.getLotService().update(lotData);
			kr.co.aim.greentrack.lot.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
		}
		panelReleaseHoldForBorrowed(panelNameList);

		return doc;
	}

	private static void checkLotStateBorrowed(Lot lotData) throws CustomException
	{
		if ( !(lotData.getLotState().equals("Borrowed")) )
		{
			throw new CustomException("LOT-0043", lotData.getKey().getLotName(), lotData.getLotState()); 
		}
	}
	private void panelReleaseHoldForBorrowed(List<String> panelNameList) throws CustomException
	{
		try
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReleaseHold", this.getEventUser(), this.getEventComment());

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

				eventInfo.setEventComment("ReleaseHold Panel by Borrowed");
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
               
				lotData.setLotHoldState(constantMap.Flag_N);
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
			log.info("Error Occurred - ReleaseHold Panel by Borrowed");
		}
	}

}
