package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
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

public class CuttingRework extends SyncHandler {

	private static Log log = LogFactory.getLog(CuttingRework.class);
	
	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		//Get Doc
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", true);
		String trayGroupName = SMessageUtil.getBodyItemValue(doc, "COVERNAME", true);
		String trayName = SMessageUtil.getBodyItemValue(doc, "TRAYNAME", true);
		
		int scrapLotQty = 0;
		
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		//EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Judge", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		EventInfo deassignEventInfo = EventInfoUtil.makeEventInfo("Deassign", getEventUser(), getEventComment(), null, null);
		deassignEventInfo.setEventTimeKey(eventInfo.getEventTimeKey());
		deassignEventInfo.setEventTime(eventInfo.getEventTime());
		
		String queryStringLot = "UPDATE LOT SET LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LASTEVENTFLAG = ?, "
				+ " BEFOREOPERATIONNAME = ?, BEFOREOPERATIONVER = ?, PROCESSOPERATIONNAME = ?, PROCESSOPERATIONVERSION = ?, POSITION = ?, NODESTACK = ?, CARRIERNAME = ?, REASONCODE = ?, LOTGRADE = ? "
				+ "WHERE LOTNAME = ?";
		
		//Make Panel
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
		
		//For NextNode
		Lot lotInfo = MESLotServiceProxy.getLotServiceUtil().getLotData(lotList.get(0).getChildText("LOTNAME"));
		Lot oldLotInfo = (Lot)ObjectUtil.copyTo(lotInfo);
		
		ProcessFlowKey processFlowKey = new ProcessFlowKey();

		processFlowKey.setFactoryName(lotInfo.getFactoryName());
		processFlowKey.setProcessFlowName(lotInfo.getProcessFlowName());
		processFlowKey.setProcessFlowVersion(lotInfo.getProcessFlowVersion());

		ProcessFlow processFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
		
		//S
		lotInfo.setLotGrade("S");
		kr.co.aim.greentrack.processflow.management.iter.NodeStack nodeStack = NodeStackUtil.stringToNodeStack(lotInfo.getNodeStack());
		ProcessFlowIterator pfi = new ProcessFlowIterator(processFlow, nodeStack, "");
		
		PFIValueSetter valueSetter = new LotPFIValueSetter(pfi, oldLotInfo, lotInfo );
		pfi.moveNext("N", valueSetter);
		Node scrapNode = pfi.getCurrentNodeData();
		
		//G
		lotInfo.setLotGrade("G");
		kr.co.aim.greentrack.processflow.management.iter.NodeStack nodeStackG = NodeStackUtil.stringToNodeStack(lotInfo.getNodeStack());
		ProcessFlowIterator pfi2 = new ProcessFlowIterator(processFlow, nodeStackG, "");
		PFIValueSetter valueSetter2 = new LotPFIValueSetter(pfi2, oldLotInfo, lotInfo );
		pfi2.moveNext("N", valueSetter2);
		Node nextNode = pfi2.getCurrentNodeData();
		
		boolean reserveFlag = false;
		EventInfo futureActionEventInfo = (EventInfo)ObjectUtil.copyTo(eventInfo);
		futureActionEventInfo.setEventName("Delete");
		
		List<Lot> reservedScrapLot = new ArrayList<Lot>();
		
		for (Element lotData : lotList)
		{
			boolean reserveFlagS = false;
			
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);

			String lotGrade = lotData.getChildText("LOTGRADE");
			
			List<Object> lotBindList = new ArrayList<Object>();
			
			lotBindList.add(eventInfo.getEventName());
			lotBindList.add(eventInfo.getEventTimeKey());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventComment());
			lotBindList.add(constantMap.Flag_N);
			lotBindList.add(lot.getProcessOperationName());
			lotBindList.add(lot.getProcessOperationVersion());
			if(lotGrade.equals("G"))
			{
				lotBindList.add(nextNode.getNodeAttribute1());
				lotBindList.add(nextNode.getNodeAttribute2());
				lotBindList.add(lot.getUdfs().get("POSITION"));
				lotBindList.add(nextNode.getKey().getNodeId());
				lotBindList.add(lot.getCarrierName());
				lotBindList.add(lotData.getChildText("REASONCODE"));
				
				boolean reserveCheck = MESLotServiceProxy.getLotServiceUtil().PostCellDeleteLotFutureAction(futureActionEventInfo, lot, nextNode.getNodeAttribute1(), nextNode.getNodeAttribute2());
				
				if(reserveCheck)
				{
					reserveFlag = true;
				}
			}
			else
			{
				lotBindList.add(scrapNode.getNodeAttribute1());
				lotBindList.add(scrapNode.getNodeAttribute2());
				lotBindList.add("");
				lotBindList.add(scrapNode.getKey().getNodeId());
				lotBindList.add("");
				lotBindList.add(""); 
				
				reserveFlagS = MESLotServiceProxy.getLotServiceUtil().PostCellDeleteLotFutureAction(futureActionEventInfo, lot, scrapNode.getNodeAttribute1(), scrapNode.getNodeAttribute2());
			}
			lotBindList.add(lotGrade);
			lotBindList.add(lot.getKey().getLotName());
			
			if(reserveFlagS)
			{
				reserveFlag = true;
				reservedScrapLot.add(lot);
			}
			
			updateLotArgList.add(lotBindList.toArray());
			
			//History
			if(lotGrade.equals("G"))
			{
				lot.setProcessOperationName(nextNode.getNodeAttribute1());
				lot.setProcessOperationVersion(nextNode.getNodeAttribute2());
				lot.setNodeStack(nextNode.getKey().getNodeId());
				lot.setCarrierName(lot.getCarrierName());
				lot.setReasonCode(lotData.getChildText("REASONCODE"));
			}
			else
			{
				lot.setProcessOperationName(scrapNode.getNodeAttribute1());
				lot.setProcessOperationVersion(scrapNode.getNodeAttribute2());
				lot.setNodeStack(scrapNode.getKey().getNodeId());
				lot.setCarrierName("");	
			}
			lot.setLotGrade(lotGrade);
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());
			
			Map<String, String> lotUdf = new HashMap<>();
			lotUdf = lot.getUdfs();
			lotUdf.put("BEFOREOPERATIONNAME", oldLot.getProcessOperationName());
			lotUdf.put("BEFOREOPERATIONVER", oldLot.getProcessOperationVersion());
			if(lotGrade.equals("G"))
			{
				lotUdf.put("POSITION", oldLot.getUdfs().get("POSITION"));
			}
			else
			{
				lotUdf.put("POSITION", "");
			}
			lot.setUdfs(lotUdf);
			
			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
			
			updateLotHistoryList.add(lotHistory);
			
			if(lotGrade.equals("S"))
			{
				scrapLotQty += 1;
			}
		}
		MESLotServiceProxy.getLotServiceUtil().updateBatch(queryStringLot, updateLotArgList);
		try 
		{
			CommonUtil.executeBatch("insert", updateLotHistoryList);
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}
		
		//Change Durable Qty
		Durable durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
		Durable olddurableInfo = (Durable)ObjectUtil.copyTo(durableInfo);
		
		durableInfo.setLotQuantity(durableInfo.getLotQuantity() - scrapLotQty);
		
		Map<String, String> durableUdf = durableInfo.getUdfs();
		
		if(durableInfo.getLotQuantity() == 0)
		{
			durableInfo.setDurableState("Available");
			durableUdf.put("COVERNAME", "");
			durableUdf.put("POSITION", "");
		}
		
		durableInfo.setLastEventName(deassignEventInfo.getEventName());
		durableInfo.setLastEventTimeKey(deassignEventInfo.getEventTimeKey());
		durableInfo.setLastEventTime(deassignEventInfo.getEventTime());
		durableInfo.setLastEventUser(deassignEventInfo.getEventUser());
		durableInfo.setLastEventComment(deassignEventInfo.getEventComment());
		
		DurableHistory durHistory = new DurableHistory();
		durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfo, durableInfo, durHistory);
		
		DurableServiceProxy.getDurableService().update(durableInfo);
		DurableServiceProxy.getDurableHistoryService().insert(durHistory);
		
		//ChangeTrayGroup LotQty
		Durable durableInfoTrayGroup = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayGroupName);
		Durable olddurableInfoTrayGroup = (Durable)ObjectUtil.copyTo(durableInfoTrayGroup);
		
		durableInfoTrayGroup.setLotQuantity(durableInfoTrayGroup.getLotQuantity() - scrapLotQty);
		
		if(durableInfoTrayGroup.getLotQuantity() == 0)
		{
			durableInfoTrayGroup.setDurableState("Available");
			durableInfoTrayGroup.setDurableType("Tray");
			Map<String, String> trayGroupUdf = new HashMap<>();
			trayGroupUdf.put("COVERNAME", "");
			trayGroupUdf.put("POSITION", "");
			trayGroupUdf.put("DURABLETYPE1", "Tray");
			durableInfoTrayGroup.setUdfs(trayGroupUdf);
		}
		durableInfoTrayGroup.setLastEventName(eventInfo.getEventName());
		durableInfoTrayGroup.setLastEventTimeKey(eventInfo.getEventTimeKey());
		durableInfoTrayGroup.setLastEventTime(eventInfo.getEventTime());
		durableInfoTrayGroup.setLastEventUser(eventInfo.getEventUser());
		durableInfoTrayGroup.setLastEventComment(eventInfo.getEventComment());
		
		DurableHistory trayGroupHistory = new DurableHistory();
		trayGroupHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfoTrayGroup, durableInfoTrayGroup, trayGroupHistory);
		
		DurableServiceProxy.getDurableService().update(durableInfoTrayGroup);
		DurableServiceProxy.getDurableHistoryService().insert(trayGroupHistory);
		
		if(reserveFlag)
		{
			EventInfo eventInfoHold = EventInfoUtil.makeEventInfo("ReserveHold", getEventUser(), getEventComment(), null, null);
			
			eventInfoHold.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoHold.setEventTime(TimeStampUtil.getCurrentTimestamp());
			
			
			MESLotServiceProxy.getLotServiceUtil().PanelHoldByTrayGroup(eventInfoHold, trayGroupName);
		}
		
		if(reservedScrapLot.size() > 0)
		{
			EventInfo eventInfoHold = EventInfoUtil.makeEventInfo("ReserveHold", getEventUser(), getEventComment(), null, null);
			eventInfoHold.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoHold.setEventTime(TimeStampUtil.getCurrentTimestamp());
			
			
			MESLotServiceProxy.getLotServiceUtil().PanelHoldByPanelList(eventInfoHold, reservedScrapLot);
		}
		
		// Scrap
		EventInfo eventInfoScrap = EventInfoUtil.makeEventInfo("ScrapLot", getEventUser(), getEventComment(), null, null);
		eventInfoScrap.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfoScrap.setEventTime(TimeStampUtil.getCurrentTimestamp());


		// SQL
		String queryStringLotScrap = "UPDATE LOT SET LOTSTATE = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
				+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LASTEVENTFLAG = ?, REASONCODE = ?, LOTGRADE = ? WHERE LOTNAME = ?";

		List<Object[]> updateLotArgListScrap = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryListScrap = new ArrayList<LotHistory>();

		String WO = "";

		for (Element lotData : lotList)
		{
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);

			String lotGrade = lotData.getChildText("LOTGRADE");
			String reasonCode = lotData.getChildText("REASONCODE");
			if(lotGrade.equals("G"))
			{
				continue;
			}
			
			if (WO.isEmpty())
			{
				WO = lot.getProductRequestName();
			}

			List<Object> lotBindList = new ArrayList<Object>();

			lotBindList.add(constantMap.Lot_Scrapped);
			lotBindList.add(eventInfoScrap.getEventName());
			lotBindList.add(eventInfoScrap.getEventTimeKey());
			lotBindList.add(eventInfoScrap.getEventTime());
			lotBindList.add(eventInfoScrap.getEventUser());
			lotBindList.add(eventInfoScrap.getEventComment());
			lotBindList.add(constantMap.Flag_N);
			lotBindList.add(reasonCode);
			lotBindList.add("S");
			lotBindList.add(lotData.getChildText("LOTNAME"));

			updateLotArgListScrap.add(lotBindList.toArray());

			// History
			lot.setLotState(constantMap.Lot_Scrapped);
			lot.setReasonCode(reasonCode);
			lot.setLotGrade("S");
			lot.setLastEventName(eventInfoScrap.getEventName());
			lot.setLastEventTime(eventInfoScrap.getEventTime());
			lot.setLastEventTimeKey(eventInfoScrap.getEventTimeKey());
			lot.setLastEventComment(eventInfoScrap.getEventComment());
			lot.setLastEventUser(eventInfoScrap.getEventUser());

			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
			
			updateLotHistoryListScrap.add(lotHistory);
		}

		if(updateLotArgListScrap.size() > 0)
		{
			MESLotServiceProxy.getLotServiceUtil().updateBatch(queryStringLotScrap, updateLotArgListScrap);
			try 
			{
				CommonUtil.executeBatch("insert", updateLotHistoryListScrap);
			} 
			catch (Exception e) 
			{
				log.error(e.getMessage());
				throw new CustomException(e.getCause());
			}

			// ChangeWorkOrder Scrap

			ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfoScrap, WO, updateLotArgListScrap.size(), 0);

			if (newProductRequestData.getPlanQuantity() == newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity())
			{
				EventInfo newEventInfo = eventInfoScrap;
				newEventInfo.setEventName("Complete");
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(newEventInfo, WO);
			}
		}
		
		return doc;
	}
}
