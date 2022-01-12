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
import kr.co.aim.messolution.generic.util.CommonValidation;
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
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.data.DurableSpecKey;
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

public class SurfaceMappingTrackOut extends SyncHandler {
	private static Log log = LogFactory.getLog(SurfaceMappingTrackOut.class);
	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		//Get Doc
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", true);
		String sourceTrayGroupName = SMessageUtil.getBodyItemValue(doc, "SOURCETRAYGROUP", true);
		String targetTrayGroupName = SMessageUtil.getBodyItemValue(doc, "TARGETTRAYGROUP", true);
		String targetTray = SMessageUtil.getBodyItemValue(doc, "TARGETTRAY", true);
		
		int lotQty = lotList.size();
		
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		//EventInfo
		EventInfo deassignEventInfo = EventInfoUtil.makeEventInfo("Deassign", getEventUser(), getEventComment(), null, null);
		deassignEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		deassignEventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		//DeassignPanel
		List<Lot> updateDeassignLotList = new ArrayList<Lot>();
		List<LotHistory> updateDeassignLotHistory = new ArrayList<LotHistory>();
		
		Map<String, Integer> trayQtyList = new HashMap<String, Integer>();
		ArrayList<String> trayList = new ArrayList<String>();
		
		for (Element lotData : lotList)
		{
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));		
			Lot oldLot = (Lot) ObjectUtil.copyTo(lot);
			
			CommonValidation.checkLotProcessStateRun(lot);
			
			lot.setLastEventName(deassignEventInfo.getEventName());
			lot.setLastEventTime(deassignEventInfo.getEventTime());
			lot.setLastEventTimeKey(deassignEventInfo.getEventTimeKey());
			lot.setLastEventComment(deassignEventInfo.getEventComment());
			lot.setLastEventUser(deassignEventInfo.getEventUser());
			lot.setCarrierName("");
			
			Map<String, String> lotUdf = new HashMap<>();	
			lotUdf.put("POSITION", "");
			lot.setUdfs(lotUdf);
			
			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
			
			updateDeassignLotList.add(lot);
			updateDeassignLotHistory.add(lotHistory);
			
			//MakeDeassignTrayList
			if(trayList.size() == 0)
			{
				trayList.add(oldLot.getCarrierName());
				trayQtyList.put(oldLot.getCarrierName(), 1);
			}
			else
			{
				boolean isFound = false;
				for(int i = 0 ; i < trayList.size() ; i++)
				{
					if(trayList.get(i).equals(oldLot.getCarrierName()))
					{
						isFound = true;
						break;
					}
				}
				
				if(isFound)
				{
					trayQtyList.put(oldLot.getCarrierName(), trayQtyList.get(oldLot.getCarrierName()) + 1);
				}
				else
				{
					trayList.add(oldLot.getCarrierName());
					trayQtyList.put(oldLot.getCarrierName(), 1);
				}
			}	
		}
		
		
		if(updateDeassignLotList.size() > 0)
		{
			log.debug("Insert Lot, LotHistory");
			try
			{
				CommonUtil.executeBatch("update", updateDeassignLotList);
				CommonUtil.executeBatch("insert", updateDeassignLotHistory);
			}
			catch (Exception e)
			{
				log.error(e.getMessage());
				throw new CustomException(e.getCause());
			}
		}
		
		
		// Change Durable Qty
		List<Durable> updateDeassignDurableList = new ArrayList<Durable>();
		List<DurableHistory> updateDeassignDurableHistory = new ArrayList<DurableHistory>();
		
		for (int i = 0; i < trayList.size(); i++)
		{
			Durable durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayList.get(i));
			Durable oldDurableInfo = (Durable) ObjectUtil.copyTo(durableInfo);

			Map<String, String> durUdf = new HashMap<>();	
			
			durableInfo.setLotQuantity(oldDurableInfo.getLotQuantity() - trayQtyList.get(trayList.get(i)));
			if (durableInfo.getLotQuantity() == 0)
			{
				durableInfo.setDurableState("Available");
				durUdf.put("POSITION", "");
				durUdf.put("COVERNAME", "");
				durableInfo.setUdfs(durUdf);
			}

			durableInfo.setLastEventName(deassignEventInfo.getEventName());
			durableInfo.setLastEventTime(deassignEventInfo.getEventTime());
			durableInfo.setLastEventTimeKey(deassignEventInfo.getEventTimeKey());
			durableInfo.setLastEventComment(deassignEventInfo.getEventComment());
			durableInfo.setLastEventUser(deassignEventInfo.getEventUser());
			
			DurableHistory durHistory = new DurableHistory();
			durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldDurableInfo, durableInfo, durHistory);
			
			//DurableServiceProxy.getDurableService().update(durableInfo);
			//DurableServiceProxy.getDurableHistoryService().insert(durHistory);
			
			updateDeassignDurableList.add(durableInfo);
			updateDeassignDurableHistory.add(durHistory);
		}
		
		
		if(updateDeassignDurableList.size() > 0)
		{
			log.debug("Insert Durable, DurableHistory");
			try
			{
				CommonUtil.executeBatch("update", updateDeassignDurableList);
				CommonUtil.executeBatch("insert", updateDeassignDurableHistory);
			}
			catch (Exception e)
			{
				log.error(e.getMessage());
				throw new CustomException(e.getCause());
			}
		}
		
		
		// ChangeTrayGroup LotQty
		Durable durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sourceTrayGroupName);
		Durable olddurableInfo = (Durable) ObjectUtil.copyTo(durableInfo);;

		Map<String, String> durUdf = new HashMap<>();
		
		durableInfo.setLotQuantity(olddurableInfo.getLotQuantity()-lotQty);
		if (durableInfo.getLotQuantity() == 0)
		{
			durableInfo.setDurableState("Available");
			durableInfo.setDurableType("Tray");
			
			durUdf.put("POSITION", "");
			durUdf.put("COVERNAME", "");
			durUdf.put("DURABLETYPE1", "Tray");
			durableInfo.setUdfs(durUdf);
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
		
		//TrackOut
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOut", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		EventInfo futureActionEventInfo = (EventInfo)ObjectUtil.copyTo(eventInfo);
		futureActionEventInfo.setEventName("Delete");
		
		boolean reserveFlag = false;
		
		// GetNode
		Lot lotInfo = MESLotServiceProxy.getLotServiceUtil().getLotData(lotList.get(0).getChildText("LOTNAME"));
		Lot oldLotInfo = (Lot) ObjectUtil.copyTo(lotInfo);
		
		lotInfo.setLotGrade(lotList.get(0).getChildText("LOTGRADE"));

		ProcessFlowKey processFlowKey = new ProcessFlowKey();

		processFlowKey.setFactoryName(lotInfo.getFactoryName());
		processFlowKey.setProcessFlowName(lotInfo.getProcessFlowName());
		processFlowKey.setProcessFlowVersion(lotInfo.getProcessFlowVersion());

		ProcessFlow processFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

		kr.co.aim.greentrack.processflow.management.iter.NodeStack nodeStack = NodeStackUtil.stringToNodeStack(lotInfo.getNodeStack());
		ProcessFlowIterator pfi = new ProcessFlowIterator(processFlow, nodeStack, "");
		
		PFIValueSetter valueSetter = new LotPFIValueSetter(pfi, oldLotInfo, lotInfo );
		pfi.moveNext("N", valueSetter);

		Node Node = pfi.getCurrentNodeData();
		
		//boolean sampleflag = lotInfo.getUdfs().get("FIRSTGLASSFLAG").equals("C") ? true:false;
		//Assign to Tray and TrackOut
		for (Element lotData : lotList)
		{
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
			Lot oldLot = (Lot) ObjectUtil.copyTo(lot);
			String lotGrade = lotInfo.getLotGrade();
			String lotDetailGrade="";
			if(lotGrade.equals("G"))//caixu 2020/11/18 如果是G，获取其lotDetailGrade
			{
			 lotDetailGrade = lotData.getChildText("LOTDETAILGRADE");
			}
			
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());
			lot.setLotGrade(lotData.getChildText("LOTGRADE"));
			lot.setReasonCode(lotData.getChildText("REASONCODE"));
			lot.setCarrierName(targetTray);
			lot.setLotProcessState("WAIT");
			lot.setLastLoggedOutTime(eventInfo.getEventTime());
			lot.setLastLoggedOutUser(eventInfo.getEventUser());
			
			Map<String, String> lotUdf = new HashMap<>();
			
		    lot.setProcessOperationName(Node.getNodeAttribute1());
			lot.setProcessOperationVersion(Node.getNodeAttribute2());
			lot.setNodeStack(Node.getKey().getNodeId());
				
			boolean reserveCheck = MESLotServiceProxy.getLotServiceUtil().PostCellDeleteLotFutureAction(futureActionEventInfo, lot, Node.getNodeAttribute1(), Node.getNodeAttribute2());
			
			if(reserveCheck)
			{
			 reserveFlag = true;
			}
			
			lotUdf.put("BEFOREOPERATIONNAME", oldLot.getProcessOperationName());
			lotUdf.put("BEFOREOPERATIONVER", oldLot.getProcessOperationVersion());
			lotUdf.put("LOTDETAILGRADE", lotDetailGrade);
			lotUdf.put("FIRSTGLASSFLAG", "");
			
			//Get PanelPosition
			Durable trayInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(targetTray);
			DurableSpecKey specKey = new DurableSpecKey();
			specKey.setFactoryName(lot.getFactoryName());
			specKey.setDurableSpecName(trayInfo.getDurableSpecName());
			specKey.setDurableSpecVersion(trayInfo.getDurableSpecVersion());
			DurableSpec traySpec = DurableServiceProxy.getDurableSpecService().selectByKey(specKey);

			int xCount = Integer.parseInt(traySpec.getUdfs().get("XCOUNT").toString());
			int yCount = Integer.parseInt(traySpec.getUdfs().get("YCOUNT").toString());

			List<Map<String, Object>> assignedLotList = MESLotServiceProxy.getLotServiceUtil().getLotListByTray(targetTray);

			String position = "";
			boolean positionFind = false;
			for (int i = 0; i < yCount; i++)
			{
				for (int j = 0; j < xCount; j++)
				{
					String newPosition = (char) (65 + i) + "0" + Integer.valueOf(j + 1).toString();

					boolean panelCheck = true;
					for (int k = 0; k < assignedLotList.size(); k++)
					{
						Map<String, Object> assingedLotInfo = assignedLotList.get(k);
						String panelPosition = assingedLotInfo.get("POSITION").toString();

						if (panelPosition.equals(newPosition))
						{
							panelCheck = false;
							break;
						}
					}
					if (panelCheck)
					{
						position = newPosition;
						positionFind = true;
						break;
					}
				}
				if (positionFind)
				{
					break;
				}
			}
		
			lotUdf.put("POSITION", position);
			lot.setUdfs(lotUdf);
			
			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
			
			LotServiceProxy.getLotService().update(lot);
			LotServiceProxy.getLotHistoryService().insert(lotHistory);
		}
		
		//ChangeTrayInfo
		Durable assignTrayInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(targetTray);
		Durable oldAssignTrayInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(targetTray);

		assignTrayInfo.setLotQuantity(assignTrayInfo.getLotQuantity() + lotList.size());
		if(assignTrayInfo.getCapacity() < assignTrayInfo.getLotQuantity())
		{
			throw new CustomException("DURABLE-1001", targetTray);
		}
		assignTrayInfo.setDurableState(constantMap.Dur_InUse);
		assignTrayInfo.setLastEventName(eventInfo.getEventName());
		assignTrayInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		assignTrayInfo.setLastEventTime(eventInfo.getEventTime());
		assignTrayInfo.setLastEventUser(eventInfo.getEventUser());
		assignTrayInfo.setLastEventComment(eventInfo.getEventComment());

		String position = "1";
		List<Durable> assignedTrayList = MESDurableServiceProxy.getDurableServiceUtil().getTrayListByCoverName(targetTrayGroupName);
		
		if(assignTrayInfo.getUdfs().get("COVERNAME").equals(targetTrayGroupName))
		{
			position = assignTrayInfo.getUdfs().get("POSITION");
		}
		else if(assignedTrayList != null)
		{
			for (int i = 1 ; i <= 50 ; i++)
			{
				boolean checkPosition = true;
				for (Durable durData : assignedTrayList)
				{
					if(durData.getUdfs().get("POSITION").equals(String.valueOf(i)))
					{
						checkPosition = false;
						break;
					}
				}
				if(checkPosition)
				{
					position = String.valueOf(i);
					break;
				}
			}
		}
		
		Map<String, String> assignedTrayUdf = new HashMap<>();
		assignedTrayUdf.put("POSITION", position);
		assignedTrayUdf.put("COVERNAME", targetTrayGroupName);
		assignedTrayUdf.put("DURABLETYPE1", "Tray");
		assignTrayInfo.setUdfs(assignedTrayUdf);
		
		DurableHistory assignDurHistory = new DurableHistory();
		assignDurHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldAssignTrayInfo, assignTrayInfo, assignDurHistory);

		DurableServiceProxy.getDurableService().update(assignTrayInfo);
		DurableServiceProxy.getDurableHistoryService().insert(assignDurHistory);
		
		//ChangeTrayGroupInfo
		EventInfo assignEventInfo = EventInfoUtil.makeEventInfo("AssignTrayGroup", getEventUser(), getEventComment(), null, null);
		assignEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		assignEventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		Durable assignTrayGroupInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(targetTrayGroupName);
		Durable oldAssignTrayGroupInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(targetTrayGroupName);

		assignTrayGroupInfo.setLotQuantity(assignTrayGroupInfo.getLotQuantity() + lotList.size());
		assignTrayGroupInfo.setDurableState(constantMap.Dur_InUse);
		assignTrayGroupInfo.setLastEventName(assignEventInfo.getEventName());
		assignTrayGroupInfo.setLastEventTimeKey(assignEventInfo.getEventTimeKey());
		assignTrayGroupInfo.setLastEventTime(assignEventInfo.getEventTime());
		assignTrayGroupInfo.setLastEventUser(assignEventInfo.getEventUser());
		assignTrayGroupInfo.setLastEventComment(assignEventInfo.getEventComment());
		assignTrayGroupInfo.setDurableType("CoverTray");

		assignedTrayList = MESDurableServiceProxy.getDurableServiceUtil().getTrayListByCoverName(targetTrayGroupName);
		
		int trayGroupPosition = 1;
		
		for (Durable durData : assignedTrayList)
		{
			if(Integer.parseInt(durData.getUdfs().get("POSITION")) > trayGroupPosition)
			{
				trayGroupPosition = Integer.parseInt(durData.getUdfs().get("POSITION"));
			}
		}
		
		Map<String, String> assignedTrayGroupUdf = new HashMap<>();
		assignedTrayGroupUdf.put("POSITION", String.valueOf(trayGroupPosition + 1));
		assignedTrayGroupUdf.put("COVERNAME", targetTrayGroupName);
		assignedTrayGroupUdf.put("DURABLETYPE1", "CoverTray");
		assignTrayGroupInfo.setUdfs(assignedTrayGroupUdf);
		
		DurableHistory assignTrayGroupHistory = new DurableHistory();
		assignTrayGroupHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldAssignTrayGroupInfo, assignTrayGroupInfo, assignTrayGroupHistory);

		DurableServiceProxy.getDurableService().update(assignTrayGroupInfo);
		DurableServiceProxy.getDurableHistoryService().insert(assignTrayGroupHistory);
		
		
		if(lotList.get(0).getChildText("LOTGRADE").equals("S") && !lotInfo.getProcessOperationName().equals("35021"))
		{
			EventInfo eventInfoScrap = EventInfoUtil.makeEventInfo("Scrap", getEventUser(), getEventComment(), null, null);//2020/12/8 caixu ScrapLot Modify Scrap
			eventInfoScrap.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoScrap.setEventTime(TimeStampUtil.getCurrentTimestamp());

			// SQL
			String queryStringLotScrap = "UPDATE LOT SET LOTSTATE = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
					+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LASTEVENTFLAG = ?, REASONCODE = ? WHERE LOTNAME = ?";

			List<Object[]> updateLotArgListScrap = new ArrayList<Object[]>();
			List<LotHistory> updateLotHistoryListScrap = new ArrayList<LotHistory>();

			String WO = "";

			for (Element lotData : lotList)
			{
				Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
				Lot oldLot = (Lot)ObjectUtil.copyTo(lot);
				String reasonCode = lotData.getChildText("REASONCODE");
				
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
				lotBindList.add(lotData.getChildText("LOTNAME"));

				updateLotArgListScrap.add(lotBindList.toArray());

				// History
				lot.setLotState(constantMap.Lot_Scrapped);
				lot.setReasonCode(reasonCode);
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

				/*if (newProductRequestData.getPlanQuantity() == newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity())
				{
					EventInfo newEventInfo = eventInfoScrap;
					newEventInfo.setEventName("Complete");
					MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(newEventInfo, WO);
				}*///2020/12/23 caixu 屏蔽自动Complete的功能	
			}
		}
		
		if(reserveFlag)
		{
			EventInfo eventInfoHold = EventInfoUtil.makeEventInfo("ReserveHold", getEventUser(), getEventComment(), null, null);
			eventInfoHold.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoHold.setEventTime(TimeStampUtil.getCurrentTimestamp());
			
			MESLotServiceProxy.getLotServiceUtil().PanelHoldByTrayGroup(eventInfoHold, targetTrayGroupName);
		}
		
		return doc;
	}
}
