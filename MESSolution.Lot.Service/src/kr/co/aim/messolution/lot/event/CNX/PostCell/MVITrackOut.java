package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.policy.util.LotPFIValueSetter;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processflow.management.iter.NodeStackUtil;
import kr.co.aim.greentrack.processflow.management.iter.PFIValueSetter;
import kr.co.aim.greentrack.processflow.management.iter.ProcessFlowIterator;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class MVITrackOut extends SyncHandler {
	
	Log log = LogFactory.getLog(this.getClass());

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		//Get Doc
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		String trayName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);

		int panelQty = lotList.size();
		
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		//EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOut", getEventUser(), getEventComment(), null, null);
		String timeKey = TimeUtils.getCurrentEventTimeKey();
		eventInfo.setEventTimeKey(timeKey);
			eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		EventInfo futureActionEventInfo = (EventInfo)ObjectUtil.copyTo(eventInfo);
		futureActionEventInfo.setEventName("Delete");
		
		//Make Panel
		List<Lot> updateLotArgList = new ArrayList<Lot>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
		List<Object[]> updateLotArgListScrap = new ArrayList<Object[]>();
		List<LotHistory> updateScrapLotHistoryList = new ArrayList<LotHistory>();
		
		boolean reserveFlag = false;
		
		//For SAP Data
		Lot checkLot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotList.get(0).getChildText("LOTNAME"));
		
		String checkOperation = checkLot.getProcessOperationName();
		String checkWO = checkLot.getProductRequestName();
		String checkSpec = checkLot.getProductSpecName();
		String checkProductionType = checkLot.getProductionType();
		String lotGrade=checkLot.getLotGrade();
		
		ProcessFlowKey processFlowKey = new ProcessFlowKey();

		processFlowKey.setFactoryName(checkLot.getFactoryName());
		processFlowKey.setProcessFlowName(checkLot.getProcessFlowName());
		processFlowKey.setProcessFlowVersion(checkLot.getProcessFlowVersion());

		ProcessFlow processFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
		
		kr.co.aim.greentrack.processflow.management.iter.NodeStack nodeStack = NodeStackUtil.stringToNodeStack(checkLot.getNodeStack());
		ProcessFlowIterator pfi = new ProcessFlowIterator(processFlow, nodeStack, "");

		PFIValueSetter valueSetter = new LotPFIValueSetter(pfi, checkLot, checkLot);
		pfi.moveNext("N", valueSetter);

		// 1.3. Set ProcessFlow Iterator Related Data
		Node nextNode = pfi.getCurrentNodeData();
		
		for (Element lotData : lotList)
		{
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);
			String judge=lot.getUdfs().get("LOTDETAILGRADE").toString();
			String position = lotData.getChildText("POSITION");
			
			CommonValidation.checkLotProcessStateRun(lot);
			CommonValidation.checkLotSameSpecForMVI(lot, checkOperation, checkSpec, checkProductionType, checkLot.getKey().getLotName(), checkWO);
			
			boolean reserveCheck = MESLotServiceProxy.getLotServiceUtil().PostCellDeleteLotFutureAction(futureActionEventInfo, lot, nextNode.getNodeAttribute1(), nextNode.getNodeAttribute2());
			
			if(reserveCheck)
			{
				reserveFlag = true;
			}
			
		
				Map<String, String> lotUdf = new HashMap<>();
				
				lot.setLotProcessState(constantMap.Lot_Wait);
				lot.setLastEventName(eventInfo.getEventName());
				lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
				lot.setLastEventTime(eventInfo.getEventTime());
				lot.setLastEventUser(eventInfo.getEventUser());
				lot.setLastEventComment(eventInfo.getEventComment());
				lot.setLastLoggedOutTime(eventInfo.getEventTime());
				lot.setLastLoggedOutUser(eventInfo.getEventUser());
				lot.setProcessOperationName(nextNode.getNodeAttribute1());
				lot.setProcessOperationVersion(nextNode.getNodeAttribute2());
				lot.setNodeStack(nextNode.getKey().getNodeId());
				lot.setCarrierName(trayName);
				lotUdf.put("BEFOREOPERATIONNAME", oldLot.getProcessOperationName());
				lotUdf.put("BEFOREOPERATIONVER", oldLot.getProcessOperationVersion());
				lotUdf.put("BEFOREFLOWNAME", oldLot.getProcessFlowName());
				lotUdf.put("POSITION", position);
				lot.setUdfs(lotUdf);

				LotHistory lotHistory = new LotHistory();
				lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
				
				updateLotArgList.add(lot);
				updateLotHistoryList.add(lotHistory);
			
			// Req ID. PC-Tech-0026-01: Panel Process count management
			String detailOperType = MESLotServiceProxy.getLotServiceUtil().getProcessOperationSpecData(oldLot).getDetailProcessOperationType();
			Map<String,String> processLimitEnum = ExtendedObjectProxy.getPanelProcessCountService().getProcessLimitConfiguration("ProcessLimit");
			
			if (processLimitEnum != null && StringUtil.in(detailOperType, processLimitEnum.keySet().toArray(new String[]{})))
				ExtendedObjectProxy.getPanelProcessCountService().setPanelProcessCount(eventInfo, oldLot, detailOperType,processLimitEnum.get(detailOperType));
			if (lotGrade.equals("S"))
			{
				
				EventInfo eventInfoScrap = EventInfoUtil.makeEventInfo("Scrap", getEventUser(), getEventComment(), null, null);
				eventInfoScrap.setEventTimeKey(ConvertUtil.getCurrTimeKey());
				eventInfoScrap.setEventTime(TimeStampUtil.getCurrentTimestamp());


				List<Object> lotBindList = new ArrayList<Object>();

				lotBindList.add(GenericServiceProxy.getConstantMap().Lot_Scrapped);
				lotBindList.add(eventInfoScrap.getEventName());
				lotBindList.add(eventInfoScrap.getEventTimeKey());
				lotBindList.add(eventInfoScrap.getEventTime());
				lotBindList.add(eventInfoScrap.getEventUser());
				lotBindList.add(eventInfoScrap.getEventComment());
				lotBindList.add(GenericServiceProxy.getConstantMap().Flag_N);
				lotBindList.add("Auto Scrap");
				lotBindList.add(lotData.getChildText("LOTNAME"));

				updateLotArgListScrap.add(lotBindList.toArray());

				// History
				lot.setLotState(GenericServiceProxy.getConstantMap().Lot_Scrapped);
				lot.setReasonCode("Auto Scrap");
				lot.setLastEventName(eventInfoScrap.getEventName());
				lot.setLastEventTime(eventInfoScrap.getEventTime());
				lot.setLastEventTimeKey(eventInfoScrap.getEventTimeKey());
				lot.setLastEventComment(eventInfoScrap.getEventComment());
				lot.setLastEventUser(eventInfoScrap.getEventUser());

				LotHistory lotScrapHistory = new LotHistory();
				lotScrapHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotScrapHistory);
				updateScrapLotHistoryList.add(lotScrapHistory);
				String WO = lot.getProductRequestName();

				ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfoScrap, WO, 1, 0);

		     }
			if(judge.equals("S1"))//CAICU 2020/12/10 Add SI Hold
			{
				reserveFlag = true;;
			}
		}
		
		if(updateLotArgList.size() > 0)
		{
			log.debug("Insert Lot, LotHistory");
			try
			{
				CommonUtil.executeBatch("update", updateLotArgList);
				CommonUtil.executeBatch("insert", updateLotHistoryList);
			}
			catch (Exception e)
			{
				log.error(e.getMessage());
				throw new CustomException(e.getCause());
			}
		}
		if(updateLotArgListScrap.size()>0)
		{
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE LOT ");
			sql.append("   SET LOTSTATE = ?, ");
			sql.append("       LASTEVENTNAME = ?, ");
			sql.append("       LASTEVENTTIMEKEY = ?, ");
			sql.append("       LASTEVENTTIME = ?, ");
			sql.append("       LASTEVENTUSER = ?, ");
			sql.append("       LASTEVENTCOMMENT = ?, ");
			sql.append("       LASTEVENTFLAG = ?, ");
			sql.append("       REASONCODE = ? ");
			sql.append(" WHERE LOTNAME = ? ");

			MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateLotArgListScrap);
			try 
			{
				CommonUtil.executeBatch("insert", updateScrapLotHistoryList);
			} 
			catch (Exception e) 
			{
				log.error(e.getMessage());
				throw new CustomException(e.getCause());
			}
		}
		// Durable
		Durable olddurableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
		Durable durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
		CommonValidation.checkAvailableCst(durableInfo);

		durableInfo.setLotQuantity(panelQty);
		durableInfo.setDurableState(constantMap.Dur_InUse);
		durableInfo.setLastEventName(eventInfo.getEventName());
		durableInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		durableInfo.setLastEventTime(eventInfo.getEventTime());
		durableInfo.setLastEventUser(eventInfo.getEventUser());
		durableInfo.setLastEventComment(eventInfo.getEventComment());

		DurableHistory durHistory = new DurableHistory();
		durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfo, durableInfo, durHistory);

		DurableServiceProxy.getDurableService().update(durableInfo);
		DurableServiceProxy.getDurableHistoryService().insert(durHistory);
		
		if(reserveFlag)
		{
			EventInfo eventInfoHold = EventInfoUtil.makeEventInfo("ReserveHold", getEventUser(), getEventComment(), null, null);
			eventInfoHold.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoHold.setEventTime(TimeStampUtil.getCurrentTimestamp());
			
			MESLotServiceProxy.getLotServiceUtil().PanelHoldByTray(eventInfoHold, trayName);
		}

		return doc;
	}
}
