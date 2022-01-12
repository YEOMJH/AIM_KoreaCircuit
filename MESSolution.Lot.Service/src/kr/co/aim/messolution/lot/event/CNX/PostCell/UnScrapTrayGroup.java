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
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

public class UnScrapTrayGroup extends SyncHandler {

	public static Log logger = LogFactory.getLog(LotQueueTimeService.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnScrap", this.getEventUser(), this.getEventComment(), "", "");//caixu 20200601 UnScrapTrayGroup change Unscrap
		
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		List<String> lotNameList = CommonUtil.makeList(SMessageUtil.getBodyElement(doc), "LOTLIST", "LOTNAME");
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		// Check different wo
		String sqlDiffWO = "SELECT DISTINCT PRODUCTREQUESTNAME FROM LOT WHERE LOTNAME IN (:PANELLIST)";
		Map<String, Object> inquirybindMap = new HashMap<String, Object>();
		inquirybindMap.put("PANELLIST", lotNameList);
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlDiffWO, inquirybindMap);
				
		if(result.size() != 1)
		{
			throw new CustomException("PANEL-0009");
		}
		
		String queryStringLot = "UPDATE LOT SET LOTSTATE = ?, LOTPROCESSSTATE = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
				+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LOTGRADE = ?, LOTDETAILGRADE = ?,REASONCODE = ?, REASONCODETYPE = ?,BEFOREOPERATIONNAME = ?, BEFOREOPERATIONVER = ? WHERE LOTNAME = ?";	
		
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<Lot> lotHistoryArgList = new ArrayList<Lot>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
		
		String WO = "";
		for(Element element : lotList) 
		{
			String lotName = element.getChildText("LOTNAME");
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			Lot oldLot = (Lot)ObjectUtil.copyTo(lotData);
//			Node nextNode = ProcessFlowServiceProxy.getNodeService().getNode(lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), constantMap.Node_ProcessOperation , lotData.getUdfs().get("BEFOREOPERATIONNAME").toString(), lotData.getUdfs().get("BEFOREOPERATIONVER").toString()); 
			
			CommonValidation.checkLotStateScrapped(lotData);
			
			if (WO.isEmpty())
			{
				WO = lotData.getProductRequestName();
			}
			
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());		
			
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
//			lotBindList.add(oldLot.getProcessOperationName());
//			lotBindList.add(oldLot.getProcessOperationVersion());
			lotBindList.add(oldLot.getProcessOperationName());
			lotBindList.add(oldLot.getProcessOperationVersion());
//			lotBindList.add(oldLot.getNodeStack());
			lotBindList.add(lotData.getKey().getLotName());
			
			updateLotArgList.add(lotBindList.toArray());
			
			//History	
			lotData.setLotState(constantMap.Lot_Released);
			lotData.setLotProcessState(constantMap.Lot_Wait);
			lotData.setLotGrade("G");
			lotData.setReasonCode("");
			lotData.setReasonCodeType("");
			lotData.setLastEventName(eventInfo.getEventName());
			lotData.setLastEventTime(eventInfo.getEventTime());
			lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lotData.setLastEventComment(eventInfo.getEventComment());
			lotData.setLastEventUser(eventInfo.getEventUser());
			/*
			lotData.setProcessOperationName(oldLot.getProcessOperationName());
			lotData.setProcessOperationVersion(oldLot.getProcessOperationVersion());
			lotData.setNodeStack(oldLot.getNodeStack());
			*/
			Map<String, String> lotUdf = new HashMap<>();
			lotUdf = oldLot.getUdfs();
			lotUdf.put("LOTDETAILGRADE", "");
			lotData.setUdfs(lotUdf);
		
			lotHistoryArgList.add(lotData);
			
			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, lotHistory);
			
			updateLotHistoryList.add(lotHistory);
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
		MESLotServiceProxy.getLotServiceUtil().deleteScrapProduct(lotHistoryArgList);
		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(WO);
		
		if(productRequestData.getProductRequestState().equals("Completed") && (productRequestData.getPlanQuantity() == productRequestData.getFinishedQuantity() + productRequestData.getScrappedQuantity()))
		{
			EventInfo newEventInfo = (EventInfo)ObjectUtil.copyTo(eventInfo);
			newEventInfo.setEventName("Release");
			MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeReleased(newEventInfo, WO);
		}
		
		// change scrap qty
		MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, WO, 0, updateLotArgList.size());
		
		return doc;
	}
	
}
