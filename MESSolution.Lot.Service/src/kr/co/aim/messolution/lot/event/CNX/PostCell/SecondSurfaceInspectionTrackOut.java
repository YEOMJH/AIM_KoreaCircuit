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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class SecondSurfaceInspectionTrackOut extends SyncHandler {

	private static Log log = LogFactory.getLog(SecondSurfaceInspectionTrackOut.class);
	
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
		List<Lot> updateLotArgList = new ArrayList<Lot>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		//Make Info
		Lot lotInfo = MESLotServiceProxy.getLotServiceUtil().getLotData(lotList.get(0).getChildText("LOTNAME"));
		
		String trayName = lotInfo.getCarrierName();
		Durable durData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
		String trayGroupName = durData.getUdfs().get("COVERNAME").toString();
		
		List<Lot> lotDataList = getLotDataList(lotList);
		
		//Get Node
		ProcessFlowKey processFlowKey = new ProcessFlowKey();

		processFlowKey.setFactoryName(lotInfo.getFactoryName());
		processFlowKey.setProcessFlowName(lotInfo.getProcessFlowName());
		processFlowKey.setProcessFlowVersion(lotInfo.getProcessFlowVersion());

		ProcessFlow processFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
		
		//Get NextNode
		kr.co.aim.greentrack.processflow.management.iter.NodeStack nodeStackG = NodeStackUtil.stringToNodeStack(lotInfo.getNodeStack());
		ProcessFlowIterator pfi2 = new ProcessFlowIterator(processFlow, nodeStackG, "");

		PFIValueSetter valueSetter2 = new LotPFIValueSetter(pfi2, lotInfo, lotInfo);
		pfi2.moveNext("N", valueSetter2);
		
		Node nextNode = pfi2.getCurrentNodeData();
		
		for (Lot lot : lotDataList)
		{
			String lotGrade = "";
			String lotDetailGrade = "";
			
			for(Element lotE : lotList)
			{
				String lotName = lotE.getChildText("LOTNAME");
				
				if(StringUtils.equals(lotName, lot.getKey().getLotName()))
				{
					lotGrade = lotE.getChildText("LOTGRADE");
					lotDetailGrade = lotE.getChildText("LOTDETAILGRADE");
					break;
				}
			}
			
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);

			CommonValidation.checkLotProcessStateRun(lot);

			boolean reserveCheck = MESLotServiceProxy.getLotServiceUtil().PostCellDeleteLotFutureAction(futureActionEventInfo, lot, nextNode.getNodeAttribute1(), nextNode.getNodeAttribute2());
			
			if(reserveCheck)
			{
				reserveFlag = true;
			}
			
			//Lot
			lot.setProcessOperationName(nextNode.getNodeAttribute1());
			lot.setProcessOperationVersion(nextNode.getNodeAttribute2());
			lot.setNodeStack(nextNode.getKey().getNodeId());
			lot.setLotGrade(lotGrade);
			lot.setLotProcessState("WAIT");
			lot.setLastLoggedOutTime(eventInfo.getEventTime());
			lot.setLastLoggedOutUser(eventInfo.getEventUser());
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());
			
			
			Map<String, String> lotUdf = new HashMap<>();
			lotUdf.put("BEFOREOPERATIONNAME", oldLot.getProcessOperationName());
			lotUdf.put("BEFOREOPERATIONVER", oldLot.getProcessOperationVersion());
			lotUdf.put("LOTDETAILGRADE", lotDetailGrade);
			lot.setUdfs(lotUdf);
			
			updateLotArgList.add(lot);
			
			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
			
			updateLotHistoryList.add(lotHistory);
		}

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
		
		if(reserveFlag)
		{
			EventInfo eventInfoHold = EventInfoUtil.makeEventInfo("ReserveHold", getEventUser(), getEventComment(), null, null);
			eventInfoHold.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoHold.setEventTime(TimeStampUtil.getCurrentTimestamp());
			
			MESLotServiceProxy.getLotServiceUtil().PanelHoldByTrayGroup(eventInfoHold, trayGroupName);
		}

		return doc;
	}
	
	private List<Lot> getLotDataList(List<Element> lotElementList) throws CustomException
	{
		String condition = "WHERE LOTNAME IN(";
		for (Element lotElement : lotElementList) 
		{
			String lotName = lotElement.getChildText("LOTNAME");
			
			condition += "'" + lotName + "',";
		}
		condition = condition.substring(0, condition.length() - 1) + ")";
		
		List<Lot> lotDataList = LotServiceProxy.getLotService().select(condition, new Object[] { });
		
		return lotDataList;
	}
}
