package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.IPQCLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
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
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

public class IPQCPanelTrackOut extends SyncHandler {

	private static Log log = LogFactory.getLog(IPQCPanelTrackOut.class);
	
	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		//Get Doc
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		String durableName = SMessageUtil.getBodyItemValue(doc, "TRAYNAME", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String trayGroupName = SMessageUtil.getBodyItemValue(doc, "TRAYGROUPNAME", true);
		String seq = SMessageUtil.getBodyItemValue(doc, "SEQ", true);
	
		int lotQty = lotList.size();
		
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		//EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Deassign", getEventUser(), getEventComment(), null, null);
		
		//Deassign
		
		//SQL
		String deassignLotSQL = "UPDATE LOT SET CARRIERNAME = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
				+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ? "
				+ "WHERE LOTNAME = ?";
		
		List<Object[]> deassignLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryListDeassign = new ArrayList<LotHistory>();
		
		for (Element lotData : lotList)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
			
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);
			
			List<Object> lotBindList = new ArrayList<Object>();
			
			lotBindList.add("");
			lotBindList.add(eventInfo.getEventName());
			lotBindList.add(eventInfo.getEventTimeKey());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventComment());
			lotBindList.add(lot.getKey().getLotName());
			
			deassignLotArgList.add(lotBindList.toArray());
			
			//History	
			lot.setCarrierName("");
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());

			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
			
			updateLotHistoryListDeassign.add(lotHistory);
			
			//TrayUpdate
			Durable oldDurableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(oldLot.getCarrierName());
			Durable durableInfo = (Durable) ObjectUtil.copyTo(oldDurableInfo);

			long durLotQty = oldDurableInfo.getLotQuantity() - 1;
			durableInfo.setLotQuantity(durLotQty);
			if(durLotQty == 0){
				Map<String, String> udfs = durableInfo.getUdfs();
				udfs.put("COVERNAME", "");
				udfs.put("POSITION", "");
				udfs.put("DURABLETYPE1", "Tray");
				durableInfo.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
				durableInfo.setUdfs(udfs);
			}
			else{
				durableInfo.setUdfs(oldDurableInfo.getUdfs());
			}
			durableInfo.setLastEventName(eventInfo.getEventName());
			durableInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
			durableInfo.setLastEventTime(eventInfo.getEventTime());
			durableInfo.setLastEventUser(eventInfo.getEventUser());
			durableInfo.setLastEventComment(eventInfo.getEventComment());
			//durableInfo.setUdfs(oldDurableInfo.getUdfs());

			DurableHistory durHistory = new DurableHistory();
			durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldDurableInfo, durableInfo, durHistory);

			DurableServiceProxy.getDurableService().update(durableInfo);
			DurableServiceProxy.getDurableHistoryService().insert(durHistory);
		}
		MESLotServiceProxy.getLotServiceUtil().updateBatch(deassignLotSQL, deassignLotArgList);
		try 
		{
			CommonUtil.executeBatch("insert", updateLotHistoryListDeassign);
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}
		
		//TrayGroupUpdate
		Durable oldTrayGroupInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayGroupName);
		Durable trayGroupInfo = (Durable) ObjectUtil.copyTo(oldTrayGroupInfo);

		long durLotQty = oldTrayGroupInfo.getLotQuantity() - lotQty;
		trayGroupInfo.setLotQuantity(durLotQty);
		if(durLotQty == 0){
			trayGroupInfo.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
			trayGroupInfo.setDurableType("Tray");
			Map<String, String> udfs = trayGroupInfo.getUdfs();
			udfs.put("COVERNAME", "");
			udfs.put("POSITION", "");	
			udfs.put("DURABLETYPE1", "Tray");
			trayGroupInfo.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
			trayGroupInfo.setUdfs(udfs);
			//2018.12.14
			IPQCLot fqcLot = ExtendedObjectProxy.getIPQCLotService().selectByKey(false, new Object[] { trayGroupName, Integer.parseInt(seq) });
			
			fqcLot.setLotState(constantMap.Lot_Completed);
			fqcLot.setLastEventComment(eventInfo.getEventComment());
			fqcLot.setLastEventName(eventInfo.getEventName());
			fqcLot.setLastEventTime(eventInfo.getEventTime());
			fqcLot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			fqcLot.setLastEventUser(eventInfo.getEventUser());
			
			ExtendedObjectProxy.getIPQCLotService().modify(eventInfo, fqcLot);
		}
		else{
			trayGroupInfo.setUdfs(oldTrayGroupInfo.getUdfs());
		}
		trayGroupInfo.setLastEventName(eventInfo.getEventName());
		trayGroupInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		trayGroupInfo.setLastEventTime(eventInfo.getEventTime());
		trayGroupInfo.setLastEventUser(eventInfo.getEventUser());
		trayGroupInfo.setLastEventComment(eventInfo.getEventComment());
		//trayGroupInfo.setUdfs(oldTrayGroupInfo.getUdfs());

		DurableHistory trayGroupHistory = new DurableHistory();
		trayGroupHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldTrayGroupInfo, trayGroupInfo, trayGroupHistory);

		DurableServiceProxy.getDurableService().update(trayGroupInfo);
		DurableServiceProxy.getDurableHistoryService().insert(trayGroupHistory);
		
		eventInfo.setEventName("TrackOut");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		EventInfo futureActionEventInfo = (EventInfo)ObjectUtil.copyTo(eventInfo);
		futureActionEventInfo.setEventName("Delete");
		
		boolean reserveFlag = false;
		
		//SQL
		String queryStringLot = "UPDATE LOT SET LOTSTATE = ?, LOTPROCESSSTATE = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
				+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LASTEVENTFLAG = ?, LASTLOGGEDOUTTIME = ?, LASTLOGGEDOUTUSER = ?, "
				+ "PROCESSOPERATIONNAME = ?, PROCESSOPERATIONVERSION = ?, BEFOREOPERATIONNAME = ?, BEFOREOPERATIONVER = ?, BEFOREFLOWNAME = ?, POSITION = ?, NODESTACK = ?, CARRIERNAME = ? "
				+ "WHERE LOTNAME = ?";	
		
		//Make Panel
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
		
		Lot lotInfo = MESLotServiceProxy.getLotServiceUtil().getLotData(lotList.get(0).getChildText("LOTNAME"));
		
		Node nextNode = new Node();
		String processOperationName = lotInfo.getUdfs().get("BEFOREOPERATIONNAME");
		String processOperationVersion = "00001";
		nextNode = ProcessFlowServiceProxy.getNodeService().getNode(factoryName, lotInfo.getProcessFlowName(), lotInfo.getProcessFlowVersion(), constantMap.Node_ProcessOperation , processOperationName, processOperationVersion);
		
		/*
		if(lotInfo.getLotGrade().equals("S"))
		{
			List<Map<String, Object>> operationData = getOperationByDetailOperType(lotInfo.getFactoryName(), lotInfo.getProductSpecName(), lotInfo.getProductSpecVersion(), lotInfo.getProcessFlowName(), lotInfo.getProcessFlowVersion(), "ScrapPack");
			nextNode = ProcessFlowServiceProxy.getNodeService().getNode(lotInfo.getFactoryName(), lotInfo.getProcessFlowName(), lotInfo.getProcessFlowVersion(), constantMap.Node_ProcessOperation , operationData.get(0).get("PROCESSOPERATIONNAME").toString(), operationData.get(0).get("PROCESSOPERATIONVERSION").toString()); 
		}
		else
		{
			nextNode = ProcessFlowServiceProxy.getNodeService().getNode(factoryName, lotInfo.getProcessFlowName(), lotInfo.getProcessFlowVersion(), constantMap.Node_ProcessOperation , lotInfo.getUdfs().get("BEFOREOPERATIONNAME"), lotInfo.getUdfs().get("BEFOREOPERATIONVER"));
		}
		*/
		for (Element lotData : lotList)
		{
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);
			String position = lotData.getChildText("POSITION");
			
			boolean reserveCheck = MESLotServiceProxy.getLotServiceUtil().PostCellDeleteLotFutureAction(futureActionEventInfo, lot, nextNode.getNodeAttribute1(), nextNode.getNodeAttribute2());
			
			if(reserveCheck)
			{
				reserveFlag = true;
			}
			
			List<Object> lotBindList = new ArrayList<Object>();
			
			lotBindList.add(constantMap.Lot_Released);
			lotBindList.add(constantMap.Lot_Wait);
			lotBindList.add(eventInfo.getEventName());
			lotBindList.add(eventInfo.getEventTimeKey());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventComment());
			lotBindList.add(constantMap.Flag_N);
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(nextNode.getNodeAttribute1());
			lotBindList.add(nextNode.getNodeAttribute2());
			lotBindList.add(lot.getProcessOperationName());
			lotBindList.add(lot.getProcessOperationVersion());
			lotBindList.add(lot.getProcessFlowName());
			lotBindList.add(position);
			lotBindList.add(nextNode.getKey().getNodeId());
			lotBindList.add(durableName);
			lotBindList.add(lot.getKey().getLotName());
			
			updateLotArgList.add(lotBindList.toArray());
			
			//History	
			lot.setLotState(constantMap.Lot_Released);
			lot.setLotProcessState(constantMap.Lot_Wait);
			lot.setLastLoggedOutTime(eventInfo.getEventTime());
			lot.setLastLoggedOutUser(eventInfo.getEventUser());
			lot.setProcessOperationName(nextNode.getNodeAttribute1());
			lot.setProcessOperationVersion(nextNode.getNodeAttribute2());
			lot.setNodeStack(nextNode.getKey().getNodeId());
			lot.setCarrierName(durableName);
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());
			
			Map<String, String> lotUdf = new HashMap<>();
			lotUdf = lot.getUdfs();
			lotUdf.put("BEFOREOPERATIONNAME", oldLot.getProcessOperationName());
			lotUdf.put("BEFOREOPERATIONVER", oldLot.getProcessOperationVersion());
			lotUdf.put("BEFOREFLOWNAME", oldLot.getProcessFlowName());
			lotUdf.put("POSITION", position);
			
			lot.setUdfs(lotUdf);
			
			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
			
			updateLotHistoryList.add(lotHistory);
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
		
		//Durable
		Durable olddurableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		Durable durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		
		CommonValidation.checkAvailableCst(durableInfo);
		
		durableInfo.setLotQuantity(lotQty);
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
		
		// Scrap
		if (lotInfo.getLotGrade().equals("S"))
		{
			EventInfo eventInfoScrap = EventInfoUtil.makeEventInfo("ScrapLot", getEventUser(), getEventComment(), null, null);
			eventInfoScrap.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoScrap.setEventTime(TimeStampUtil.getCurrentTimestamp());

			// SQL
			String queryStringLotScrap = "UPDATE LOT SET LOTSTATE = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
					+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LASTEVENTFLAG = ?, REASONCODE = ? WHERE LOTNAME = ?";

			List<Object[]> updateLotArgListScrap = new ArrayList<Object[]>();
			List<LotHistory> updateLotHistoryListScrap = new ArrayList<LotHistory>();

			for (Element lotData : lotList)
			{
				Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
				Lot oldLot = (Lot)ObjectUtil.copyTo(lot);

				List<Object> lotBindList = new ArrayList<Object>();

				lotBindList.add(constantMap.Lot_Scrapped);
				lotBindList.add(eventInfoScrap.getEventName());
				lotBindList.add(eventInfoScrap.getEventTimeKey());
				lotBindList.add(eventInfoScrap.getEventTime());
				lotBindList.add(eventInfoScrap.getEventUser());
				lotBindList.add(eventInfoScrap.getEventComment());
				lotBindList.add(constantMap.Flag_N);
				lotBindList.add("Auto Scrap");
				lotBindList.add(lotData.getChildText("LOTNAME"));

				updateLotArgListScrap.add(lotBindList.toArray());

				// History
				lot.setLotState(constantMap.Lot_Scrapped);
				lot.setReasonCode("Auto Scrap");
				lot.setLastEventName(eventInfoScrap.getEventName());
				lot.setLastEventTime(eventInfoScrap.getEventTime());
				lot.setLastEventTimeKey(eventInfoScrap.getEventTimeKey());
				lot.setLastEventComment(eventInfoScrap.getEventComment());
				lot.setLastEventUser(eventInfoScrap.getEventUser());

				LotHistory lotHistory = new LotHistory();
				lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
				
				updateLotHistoryListScrap.add(lotHistory);
			}

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

			ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfoScrap, lotInfo.getProductRequestName(), updateLotArgListScrap.size(), 0);

			if (newProductRequestData.getPlanQuantity() == newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity())
			{
				EventInfo newEventInfo = eventInfoScrap;
				newEventInfo.setEventName("Complete");
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(newEventInfo, lotInfo.getProductRequestName());
			}
		}
		
		if(reserveFlag)
		{
			EventInfo eventInfoHold = EventInfoUtil.makeEventInfo("ReserveHold", getEventUser(), getEventComment(), null, null);
			eventInfoHold.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoHold.setEventTime(TimeStampUtil.getCurrentTimestamp());
			
			MESLotServiceProxy.getLotServiceUtil().PanelHoldByTray(eventInfoHold, durableName);
		}
		
		return doc;
	}
}
