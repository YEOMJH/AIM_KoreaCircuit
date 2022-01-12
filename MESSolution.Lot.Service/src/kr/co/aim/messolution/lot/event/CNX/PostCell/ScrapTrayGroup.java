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
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;


public class ScrapTrayGroup extends SyncHandler {

	public static Log logger = LogFactory.getLog(ScrapTrayGroup.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		String sReasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", true);
		String sReasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);
		String sOperationName = SMessageUtil.getBodyItemValue(doc, "STEPNO", false);
		String sMachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", false);
		String sDeparment=SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true);
		String sUnitName="";
		String sSubUnitName="";
		
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
				+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LOTGRADE = ?, LOTDETAILGRADE = '',REASONCODE = ?, REASONCODETYPE = ?, PROCESSOPERATIONNAME = ?, "
				+ "PROCESSOPERATIONVERSION = ?, BEFOREOPERATIONNAME = ?, BEFOREOPERATIONVER = ?, NODESTACK = ?  WHERE LOTNAME = ?";
		
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
		List<Object[]> updateScrapProduct= new ArrayList<Object[]>();
		
		Lot lotInfo = MESLotServiceProxy.getLotServiceUtil().getLotData(lotList.get(0).getChildText("LOTNAME"));
		
		List<Map<String, Object>> operationData = getOperationByDetailOperType(lotInfo.getFactoryName(), lotInfo.getProductSpecName(), lotInfo.getProductSpecVersion(), lotInfo.getProcessFlowName(), lotInfo.getProcessFlowVersion(), "ScrapPack");
		Node nextNode = ProcessFlowServiceProxy.getNodeService().getNode(lotInfo.getFactoryName(), lotInfo.getProcessFlowName(), lotInfo.getProcessFlowVersion(), constantMap.Node_ProcessOperation , operationData.get(0).get("PROCESSOPERATIONNAME").toString(), operationData.get(0).get("PROCESSOPERATIONVERSION").toString()); 
	
		String WO = "";
		for(Element element : lotList) 
		{
			String lotName = element.getChildText("LOTNAME");
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			Lot oldLot = (Lot)ObjectUtil.copyTo(lotData);
			
			CommonValidation.checkLotProcessState(lotData);
			CommonValidation.checkLotStateUnScrapped(lotData);
			LotKey lotKey = lotData.getKey();
			List<ProductU> productUSequence = new ArrayList<ProductU>();
			//productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllUnScrapProductUSequence(lotData);
			
			if (WO.isEmpty())
			{
				WO = lotData.getProductRequestName();
			}
			
			// Scrap
			eventInfo.setEventName("Scrap");//caixu 20200601 ScrapTrayGroup Change Scrap
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
			
//			Lot oldLot = lotData;		
			
			List<Object> lotBindList = new ArrayList<Object>();
			
			lotBindList.add(constantMap.Lot_Scrapped);
			lotBindList.add(constantMap.Lot_Wait);
			lotBindList.add(eventInfo.getEventName());
			lotBindList.add(eventInfo.getEventTimeKey());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventComment());
			lotBindList.add("S");
			lotBindList.add(sReasonCode);
			lotBindList.add(sReasonCodeType);
			lotBindList.add(nextNode.getNodeAttribute1());
			lotBindList.add(nextNode.getNodeAttribute2());
			lotBindList.add(oldLot.getProcessOperationName());
			lotBindList.add(oldLot.getProcessOperationVersion());
			lotBindList.add(nextNode.getKey().getNodeId());
			lotBindList.add(lotData.getKey().getLotName());
			
			updateLotArgList.add(lotBindList.toArray());
			
			//History	
			lotData.setLotState(constantMap.Lot_Scrapped);
			lotData.setLotProcessState(constantMap.Lot_Wait);
			lotData.setLotGrade("S");
			lotData.setReasonCode(sReasonCode);
			lotData.setReasonCodeType(sReasonCodeType);
			lotData.setProcessOperationName(nextNode.getNodeAttribute1());
			lotData.setProcessOperationVersion(nextNode.getNodeAttribute2());
			lotData.setNodeStack(nextNode.getKey().getNodeId());
			lotData.setLastEventName(eventInfo.getEventName());
			lotData.setLastEventTime(eventInfo.getEventTime());
			lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lotData.setLastEventComment(eventInfo.getEventComment());
			lotData.setLastEventUser(eventInfo.getEventUser());
			
			Map<String, String> lotUdf = new HashMap<>();
			lotUdf = oldLot.getUdfs();
			lotUdf.put("BEFOREOPERATIONNAME", oldLot.getProcessOperationName());
			lotUdf.put("BEFOREOPERATIONVER", oldLot.getProcessOperationVersion());
			lotUdf.put("LOTDETAILGRADE", "");
			lotData.setUdfs(lotUdf);
			
			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, lotHistory);
			
			updateLotHistoryList.add(lotHistory);
			
			//ScrapProduct
		    List<Object> scrapProduct = new ArrayList<Object>();
			scrapProduct.add(lotName);
			scrapProduct.add(lotName);
			scrapProduct.add(lotData.getProductionType());
			scrapProduct.add("S");
			scrapProduct.add(constantMap.Lot_Scrapped);
			scrapProduct.add(eventInfo.getEventTimeKey());
			scrapProduct.add(eventInfo.getEventTime());
			scrapProduct.add(eventInfo.getEventUser());
			scrapProduct.add(sReasonCodeType);
			scrapProduct.add(sReasonCode);
			scrapProduct.add(sDeparment);
			scrapProduct.add(sOperationName);
			scrapProduct.add(sMachineName);
			scrapProduct.add(sUnitName);
			scrapProduct.add(sSubUnitName);
			scrapProduct.add(eventInfo.getEventComment());
			updateScrapProduct.add(scrapProduct.toArray());
			
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
		MESLotServiceProxy.getLotServiceUtil().InsertScrapProduct(updateScrapProduct);
		// ChangeWorkOrder Scrap

		ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, WO, updateLotArgList.size(), 0);

		/*if (newProductRequestData.getPlanQuantity() == newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity())
		{
			EventInfo newEventInfo = (EventInfo)ObjectUtil.copyTo(eventInfo);
			newEventInfo.setEventName("Complete");
			
			MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(newEventInfo, WO);
		}*///2020/12/23 caixu 屏蔽自动Complete的功能		
		return doc;
	}
	
	private List<Map<String, Object>> getOperationByDetailOperType(String factoryname, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion, String detailOperType)
	{
		String sql = "SELECT TP.PROCESSOPERATIONNAME, " +
				"       TP.PROCESSOPERATIONVERSION, " +
				"       PS.DETAILPROCESSOPERATIONTYPE " +
				"  FROM TPFOPOLICY TP, PROCESSOPERATIONSPEC PS " +
				" WHERE     TP.FACTORYNAME = :FACTORYNAME " +
				"       AND TP.PRODUCTSPECNAME = :PRODUCTSPECNAME " +
				"       AND TP.PRODUCTSPECVERSION = :PRODUCTSPECVERSION " +
				"       AND TP.PROCESSFLOWNAME = :PROCESSFLOWNAME " +
				"       AND TP.PROCESSFLOWVERSION = :PROCESSFLOWVERSION " +
				"       AND TP.PROCESSOPERATIONNAME = PS.PROCESSOPERATIONNAME " +
				"       AND TP.PROCESSOPERATIONVERSION = PS.PROCESSOPERATIONVERSION " +
				"       AND PS.DETAILPROCESSOPERATIONTYPE = :DETAILPROCESSOPERATIONTYPE " ;

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryname);
		args.put("PRODUCTSPECNAME", productSpecName);
		args.put("PRODUCTSPECVERSION", productSpecVersion);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("DETAILPROCESSOPERATIONTYPE", detailOperType);
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
		
		return result;
	}
}