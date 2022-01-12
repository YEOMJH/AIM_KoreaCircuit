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
import kr.co.aim.messolution.extended.object.management.data.ScrapProduct;
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
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.DeassignCarrierInfo;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

public class ScrapPanel extends SyncHandler {

	public static Log logger = LogFactory.getLog(ScrapPanel.class);

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
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		String queryStringLot = "UPDATE LOT SET LOTSTATE = ?, LOTPROCESSSTATE = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
				+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LOTGRADE = ?, REASONCODE = ?, REASONCODETYPE = ?, PROCESSOPERATIONNAME = ?, "
				+ "PROCESSOPERATIONVERSION = ?, BEFOREOPERATIONNAME = ?, BEFOREOPERATIONVER = ?, NODESTACK = ?, LOTDETAILGRADE = ?  WHERE LOTNAME = ?";
		//String queryScrapProduct = "INSERT INTO CT_SCRAPPRODUCT(PRODUCTNAME,LOTNAME,PRODUCTTYPE,PRODUCTGRADE,PRODUCTSTATE,LASTEVENTTIMEKEY,LASTEVENTTIME,LASTEVENTUSER,REASONCODETYPE,REASONCODE,SCRAPDEPARTMENT,SCRAPOPERATIONNAME,SCRAPMACHINENAME,LASTEVENTCOMMENT)"
				                //  +"VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
		
		List<Object[]> updateScrapProduct= new ArrayList<Object[]>();

		Lot lotInfo = MESLotServiceProxy.getLotServiceUtil().getLotData(lotList.get(0).getChildText("LOTNAME"));


		for (Element element : lotList)
		{
			String lotName = element.getChildText("LOTNAME");
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			Lot oldLot = (Lot)ObjectUtil.copyTo(lotData);
			CommonValidation.checkLotState(lotData);
			CommonValidation.checkLotProcessState(lotData);

			List<Map<String, Object>> operationData = getOperationByDetailOperType(oldLot.getFactoryName(), oldLot.getProductSpecName(), oldLot.getProductSpecVersion(), oldLot.getProcessFlowName(),
					oldLot.getProcessFlowVersion(), "ScrapPack");
			Node nextNode = ProcessFlowServiceProxy.getNodeService().getNode(oldLot.getFactoryName(), oldLot.getProcessFlowName(), oldLot.getProcessFlowVersion(), constantMap.Node_ProcessOperation,
					operationData.get(0).get("PROCESSOPERATIONNAME").toString(), operationData.get(0).get("PROCESSOPERATIONVERSION").toString());

			LotKey lotKey = lotData.getKey();
			List<ProductU> productUSequence = new ArrayList<ProductU>();
			// productUSequence = MESLotServiceProxy.getLotInfoUtil().getAllUnScrapProductUSequence(lotData);

			// 1.SourceLot Deassign With SourceCST
			if (StringUtil.isNotEmpty(lotData.getCarrierName()))
			{
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(lotData.getCarrierName());

				DeassignCarrierInfo createInfo = MESLotServiceProxy.getLotInfoUtil().deassignCarrierInfo(lotData, durableData, productUSequence);

				eventInfo.setEventName("Deassign");

				if (!durableData.getUdfs().get("COVERNAME").isEmpty() && !durableData.getUdfs().get("COVERNAME").equals(durableData.getKey().getDurableName()))
				{
					Durable olddurableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableData.getUdfs().get("COVERNAME"));
					Durable durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableData.getUdfs().get("COVERNAME"));

					durableInfo.setLotQuantity(durableInfo.getLotQuantity() - 1);
					durableInfo.setLastEventName(eventInfo.getEventName());
					durableInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
					durableInfo.setLastEventTime(eventInfo.getEventTime());
					durableInfo.setLastEventUser(eventInfo.getEventUser());
					durableInfo.setLastEventComment(eventInfo.getEventComment());

					long lotQty = durableInfo.getLotQuantity() - 1;
					if (lotQty == 0)
						durableInfo.setDurableState("Available");
					
					DurableHistory durHistory = new DurableHistory();
					durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfo, durableInfo, durHistory);

					DurableServiceProxy.getDurableService().update(durableInfo);
					DurableServiceProxy.getDurableHistoryService().insert(durHistory);
				}

				MESLotServiceProxy.getLotServiceImpl().deassignCarrier(lotData, createInfo, eventInfo);
				lotData.setCarrierName("");
			}

			// 2.Scrap
			eventInfo.setEventName("Scrap");//caixu 20200601 ScrapLot Change Scrap
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

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
			//lotBindList.add()
			lotBindList.add(SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true) + " - " + sReasonCodeType);
			lotBindList.add(nextNode.getNodeAttribute1());
			lotBindList.add(nextNode.getNodeAttribute2());
			lotBindList.add(oldLot.getProcessOperationName());
			lotBindList.add(oldLot.getProcessOperationVersion());
			lotBindList.add(nextNode.getKey().getNodeId());
			lotBindList.add("");
			lotBindList.add(lotData.getKey().getLotName());

			updateLotArgList.add(lotBindList.toArray());

			// History
			lotData.setLotState(constantMap.Lot_Scrapped);
			lotData.setLotProcessState(constantMap.Lot_Wait);
			lotData.setLotGrade("S");
			lotData.setReasonCode(sReasonCode);
			lotData.setReasonCodeType(SMessageUtil.getBodyItemValue(doc, "DEPARTMENT", true) + " - " + sReasonCodeType);
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
			lotUdf.put("POSITION", "");
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
     		// Change WO
			ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfo, lotData.getProductRequestName(), 1, 0);

			/*if (newProductRequestData.getPlanQuantity() == newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity())
			{
				EventInfo newEventInfo = (EventInfo) ObjectUtil.copyTo(eventInfo);
				newEventInfo.setEventName("Complete");

				MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(newEventInfo, lotData.getProductRequestName());
			}*///2020/12/23 caixu 屏蔽自动Complete的功能	

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
		return doc;
	}

	private List<Map<String, Object>> getOperationByDetailOperType(String factoryname, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion,
			String detailOperType)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT TP.PROCESSOPERATIONNAME, TP.PROCESSOPERATIONVERSION, PS.DETAILPROCESSOPERATIONTYPE ");
		sql.append("  FROM TPFOPOLICY TP, PROCESSOPERATIONSPEC PS ");
		sql.append(" WHERE TP.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND TP.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		sql.append("   AND TP.PRODUCTSPECVERSION = :PRODUCTSPECVERSION ");
		sql.append("   AND TP.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		sql.append("   AND TP.PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");
		sql.append("   AND TP.PROCESSOPERATIONNAME = PS.PROCESSOPERATIONNAME ");
		sql.append("   AND TP.PROCESSOPERATIONVERSION = PS.PROCESSOPERATIONVERSION ");
		sql.append("   AND PS.DETAILPROCESSOPERATIONTYPE = :DETAILPROCESSOPERATIONTYPE ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryname);
		args.put("PRODUCTSPECNAME", productSpecName);
		args.put("PRODUCTSPECVERSION", productSpecVersion);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("DETAILPROCESSOPERATIONTYPE", detailOperType);

		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return result;
	}
}