package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.management.impl.LotQueueTimeService;
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
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;


public class UnScrapPanel extends SyncHandler {

	public static Log logger = LogFactory.getLog(LotQueueTimeService.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnScrap", this.getEventUser(), this.getEventComment(), "", "");//caixu 20200601 UnScrapLot Change UnScrap
		
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		String queryStringLot = "UPDATE LOT SET LOTSTATE = ?, LOTPROCESSSTATE = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?,"
				+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LOTGRADE = ?, LOTDETAILGRADE = ?,REASONCODE = ?, REASONCODETYPE = ?,BEFOREOPERATIONNAME = ?, BEFOREOPERATIONVER = ? WHERE LOTNAME = ?";	
		
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<Lot> updateLotList = new ArrayList<Lot>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
		
		
		for(Element element : lotList) 
		{
			String lotName = element.getChildText("LOTNAME");
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			Lot oldLot = (Lot)ObjectUtil.copyTo(lotData);
			Node nextNode = ProcessFlowServiceProxy.getNodeService().getNode(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), constantMap.Node_ProcessOperation , lotData.getUdfs().get("BEFOREOPERATIONNAME").toString(), lotData.getUdfs().get("BEFOREOPERATIONVER").toString()); 
			
			CommonValidation.checkLotStateScrapped(lotData);
			CommonValidation.checkLotCarriernameNull(lotData);
			
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
			
//			Lot oldLot = lotData;		
			
			List<Object> lotBindList = new ArrayList<Object>();
			
			lotBindList.add(constantMap.Lot_Released);
			lotBindList.add(constantMap.Lot_Wait);
			lotBindList.add(eventInfo.getEventName());
			lotBindList.add(eventInfo.getEventTimeKey());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventComment());
			lotBindList.add("G");
			lotBindList.add("");
			lotBindList.add("");
			lotBindList.add("");
			//lotBindList.add(nextNode.getNodeAttribute1());
			//lotBindList.add(nextNode.getNodeAttribute2());
			lotBindList.add(oldLot.getProcessOperationName());
			lotBindList.add(oldLot.getProcessOperationVersion());
			//lotBindList.add(nextNode.getKey().getNodeId());
			lotBindList.add(lotData.getKey().getLotName());
			
			updateLotArgList.add(lotBindList.toArray());
			
			//History	
			lotData.setLotState(constantMap.Lot_Released);
			lotData.setLotProcessState(constantMap.Lot_Wait);
			lotData.setLotGrade("G");
			lotData.setReasonCode("");
			lotData.setReasonCodeType("");
			//lotData.setProcessOperationName(nextNode.getNodeAttribute1());
			//lotData.setProcessOperationVersion(nextNode.getNodeAttribute2());
			//lotData.setNodeStack(nextNode.getKey().getNodeId());
			lotData.setLastEventName(eventInfo.getEventName());
			lotData.setLastEventTime(eventInfo.getEventTime());
			lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lotData.setLastEventComment(eventInfo.getEventComment());
			lotData.setLastEventUser(eventInfo.getEventUser());
			
			Map<String, String> lotUdf = new HashMap<>();
			lotUdf = oldLot.getUdfs();
			lotUdf.put("LOTDETAILGRADE", "");
			//lotUdf.put("BEFOREOPERATIONNAME", oldLot.getProcessOperationName());
			//lotUdf.put("BEFOREOPERATIONVER", oldLot.getProcessOperationVersion());
			lotData.setUdfs(lotUdf);
			
			updateLotList.add(lotData);
			
			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, lotHistory);
			
			updateLotHistoryList.add(lotHistory);
			
			// change scrap qty
			ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, lotData.getProductRequestName(), 0, 1);
			
			if(newProductRequestData.getProductRequestState().equals("Completed") && (newProductRequestData.getPlanQuantity() > newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity()))
			{
				EventInfo newEventInfo = (EventInfo)ObjectUtil.copyTo(eventInfo);
				newEventInfo.setEventName("Release");
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeReleased(newEventInfo, lotData.getProductRequestName());
			}
			
		}
		
		MESLotServiceProxy.getLotServiceUtil().updateBatch(queryStringLot, updateLotArgList);
		try 
		{
			CommonUtil.executeBatch("insert", updateLotHistoryList);
		} 
		catch (Exception e) 
		{
			logger.error(e.getMessage());
			throw new CustomException(e.getCause());
		}
		MESLotServiceProxy.getLotServiceUtil().deleteScrapProduct(updateLotList);
		
		return doc;
	}
}