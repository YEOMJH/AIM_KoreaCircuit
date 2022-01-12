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
import kr.co.aim.greentrack.durable.management.data.Durable;
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

public class SurfaceInspectionTrackOut extends SyncHandler {

	private static Log log = LogFactory.getLog(SurfaceInspectionTrackOut.class);
	
	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		//Get Doc
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", true);
		
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		//EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOut", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		EventInfo futureActionEventInfo = (EventInfo)ObjectUtil.copyTo(eventInfo);
		futureActionEventInfo.setEventName("Delete");
		
		boolean reserveFlag = false;
		
		//Make Panel
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
		
		//Get Node
		Lot lotInfo = MESLotServiceProxy.getLotServiceUtil().getLotData(lotList.get(0).getChildText("LOTNAME"));
		Lot oldLotInfo = (Lot)ObjectUtil.copyTo(lotInfo);
		String newLotGrade=lotList.get(0).getChildText("LOTGRADE");
		
		String trayName = lotInfo.getCarrierName();
		Durable durData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
		String trayGroupName = durData.getUdfs().get("COVERNAME").toString();
		
		CommonValidation.checkLotProcessStateRun(lotInfo);
		
		ProcessFlowKey processFlowKey = new ProcessFlowKey();

		processFlowKey.setFactoryName(lotInfo.getFactoryName());
		processFlowKey.setProcessFlowName(lotInfo.getProcessFlowName());
		processFlowKey.setProcessFlowVersion(lotInfo.getProcessFlowVersion());

		ProcessFlow processFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
		
		//Get NextNode
		lotInfo.setLotGrade(newLotGrade);
		kr.co.aim.greentrack.processflow.management.iter.NodeStack nodeStackG = NodeStackUtil.stringToNodeStack(lotInfo.getNodeStack());
		ProcessFlowIterator pfi2 = new ProcessFlowIterator(processFlow, nodeStackG, "");
		
		PFIValueSetter valueSetter2 = new LotPFIValueSetter(pfi2, oldLotInfo, lotInfo );
		pfi2.moveNext("N", valueSetter2);
		
		Node nextNode = pfi2.getCurrentNodeData();
		//{2020/11/18 增加 S/R的TrackOut逻辑 caixu
		for (Element lotData : lotList)
		{   
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);

			CommonValidation.checkLotProcessStateRun(lot);
			
			String lotGrade =newLotGrade;
			String lotDetailGrade="";
			if(lotGrade.equals("G"))//caixu 2020/11/18 如果是G，获取其lotDetailGrade
			{
			 lotDetailGrade = lotData.getChildText("LOTDETAILGRADE");
			}
			boolean reserveCheck = MESLotServiceProxy.getLotServiceUtil().PostCellDeleteLotFutureAction(futureActionEventInfo, lot, nextNode.getNodeAttribute1(), nextNode.getNodeAttribute2());
			
			if(reserveCheck)
			{
				reserveFlag = true;
			}
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());
			lot.setLotGrade(lotData.getChildText("LOTGRADE"));
			lot.setReasonCode(lotData.getChildText("REASONCODE"));
			lot.setLotProcessState("WAIT");
			lot.setLastLoggedOutTime(eventInfo.getEventTime());
			lot.setLastLoggedOutUser(eventInfo.getEventUser());
			lot.setProcessOperationName(nextNode.getNodeAttribute1());
			lot.setProcessOperationVersion(nextNode.getNodeAttribute2());
			lot.setNodeStack(nextNode.getKey().getNodeId());
			
			Map<String, String> lotUdf = new HashMap<>();
			lotUdf = lot.getUdfs();
			lotUdf.put("BEFOREOPERATIONNAME", oldLot.getProcessOperationName());
			lotUdf.put("BEFOREOPERATIONVER", oldLot.getProcessOperationVersion());
			lotUdf.put("LOTDETAILGRADE", lotDetailGrade);
			lotUdf.put("POSITION", oldLot.getUdfs().get("POSITION"));
			lotUdf.put("FIRSTGLASSFLAG", "");
			lot.setUdfs(lotUdf);
			
			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
			
			updateLotHistoryList.add(lotHistory);
			LotServiceProxy.getLotService().update(lot);
			LotServiceProxy.getLotHistoryService().insert(lotHistory);
		}
		
		if(lotList.get(0).getChildText("LOTGRADE").equals("S") && !lotInfo.getProcessOperationName().equals("35021"))
		{
			EventInfo eventInfoScrap = EventInfoUtil.makeEventInfo("Scrap", getEventUser(), getEventComment(), null, null);//2020/12/08 ScrapLotModifyLot
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
		}//}
		if(reserveFlag)
		{
			EventInfo eventInfoHold = EventInfoUtil.makeEventInfo("ReserveHold", getEventUser(), getEventComment(), null, null);
			eventInfoHold.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoHold.setEventTime(TimeStampUtil.getCurrentTimestamp());
			
			MESLotServiceProxy.getLotServiceUtil().PanelHoldByTrayGroup(eventInfoHold, trayGroupName);
		}

		return doc;
	}
	
}
