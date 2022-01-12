package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.data.LotKey;
import kr.co.aim.greentrack.lot.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.lot.management.policy.util.LotPFIValueSetter;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.iter.NodeStackUtil;
import kr.co.aim.greentrack.processflow.management.iter.PFIValueSetter;
import kr.co.aim.greentrack.processflow.management.iter.ProcessFlowIterator;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.IncrementReleasedQuantityByInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class PostCellRMA extends SyncHandler {

	private static Log log = LogFactory.getLog(CancelTrackIn.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("PostCellRMA", getEventUser(), getEventComment(), null, null);

		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String workOrderName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String trayGroupID = SMessageUtil.getBodyItemValue(doc, "TRAYGROUPID", true);
		String batchNo = SMessageUtil.getBodyItemValue(doc, "BATCHNO", false);
		String factoryCode = SMessageUtil.getBodyItemValue(doc, "FACTORYCODE", false);
		String factoryPosition = SMessageUtil.getBodyItemValue(doc, "FACTORYPOSITION", false);
		String porcessOper="";

		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		List<Element> trayIDList = SMessageUtil.getBodySequenceItemList(doc, "TRAYLIST", true);

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		List<Lot> lotDataList = new ArrayList<Lot>();

		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		// 1. Get ProductSpec Data
		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, productSpecName, productSpecVersion);

		for (Element trayData : trayIDList)
		{
			String trayID = trayData.getChildText("TRAYID");
			String position = trayData.getChildText("TRAYPOSITION");
			String lotQuantity = trayData.getChildText("LOTQUANTITY");

			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayID);

			if (!durableData.getDurableState().equals("Available"))
				throw new CustomException("PROCESSGROUP-0006", trayID);

			durableData.setDurableState("InUse");
			durableData.setDurableType("Tray");
			durableData.setLotQuantity(Long.parseLong(lotQuantity));

			Map<String, String> udfs = new HashMap<>();
			udfs.put("COVERNAME", trayGroupID);
			udfs.put("POSITION", position);
			udfs.put("DURABLETYPE1", "Tray");

			kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfoDur = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
			setEventInfoDur.setUdfs(udfs);

			DurableServiceProxy.getDurableService().update(durableData);
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfoDur, eventInfo);
		}

		kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfoTrayGroup = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
		Durable trayGroupData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayGroupID);

		if (!trayGroupData.getDurableState().equals("Available"))
			throw new CustomException("PROCESSGROUP-0007", trayGroupID);

		trayGroupData.setDurableState("InUse");
		trayGroupData.setDurableType("CoverTray");
		trayGroupData.setLotQuantity(lotList.size());

		Map<String, String> udfs = new HashMap<>();
		int position = trayIDList.size() + 1;
		udfs.put("COVERNAME", trayGroupID);
		udfs.put("POSITION", String.valueOf(position));
		udfs.put("DURABLETYPE1", "CoverTray");
		setEventInfoTrayGroup.setUdfs(udfs);

		DurableServiceProxy.getDurableService().update(trayGroupData);
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(trayGroupData, setEventInfoTrayGroup, eventInfo);

		for (Element lot : lotList)
		{
			String lotName = lot.getChildText("LOTNAME");

			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			Lot oldLotData = (Lot) ObjectUtil.copyTo(lotData);

			String trayID = lot.getChildText("TRAYID");
			String panelPosition = lot.getChildText("POSITION");

			if (!lotData.getLotState().equals("Shipped"))
				throw new CustomException("PROCESSGROUP-0008", lotName);

			ProcessFlow processFlow = CommonUtil.getProcessFlowData(factoryName, processFlowName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
			String startNodeStack = ProcessFlowServiceProxy.getProcessFlowService().getStartNode(processFlow.getKey()).getKey().getNodeId();

			kr.co.aim.greentrack.processflow.management.iter.NodeStack nodeStack = NodeStackUtil.stringToNodeStack(startNodeStack);
			ProcessFlowIterator pfi = new ProcessFlowIterator(processFlow, nodeStack, "");

			lotData.setNodeStack(startNodeStack);
			
			@SuppressWarnings("rawtypes")
			PFIValueSetter valueSetter = new LotPFIValueSetter(pfi, oldLotData, lotData);
			pfi.moveNext("N", valueSetter);

			Node nextNode = pfi.getCurrentNodeData();

			String targetOperationName = nextNode.getNodeAttribute1();
			String targerOperationVer = nextNode.getNodeAttribute2();
			String targetNodeId = nextNode.getKey().getNodeId();
			porcessOper=targetOperationName;

			Map<String, String> lotUdfs = lotData.getUdfs();
			lotUdfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
			lotUdfs.put("LASTFACTORYNAME", lotData.getFactoryName());
			lotUdfs.put("RECEIVELOTNAME", lotData.getKey().getLotName());

			SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Calendar c = Calendar.getInstance();
			c.add(Calendar.DAY_OF_MONTH, 7);
			String lastime = sf.format(c.getTime());
			Timestamp tDueDate = Timestamp.valueOf(lastime);

			this.DeleteLotData(lotName);
			//add by  jinlj 2021029 delete CT_scraplot table 's data
			this.DeleteScrapProduct(lotName);

			List<Object> lotBindList = new ArrayList<Object>();
			lotBindList.add(lotName);
			lotBindList.add(workOrderName);
			lotBindList.add(tDueDate);
			lotBindList.add(productSpecData.getProductionType());
			lotBindList.add(productSpecName);
			lotBindList.add(productSpecVersion);
			lotBindList.add(lotName);
			lotBindList.add(lotName);
			lotBindList.add(lotName);
			lotBindList.add(lotName);
			lotBindList.add(productSpecData.getProductType());
			lotBindList.add(0);
			lotBindList.add(1);
			lotBindList.add(0);
			lotBindList.add(1);
			lotBindList.add(0);
			lotBindList.add(factoryName);
			lotBindList.add(lotData.getAreaName());
			lotBindList.add(constantMap.Lot_Released);
			lotBindList.add(constantMap.Lot_Wait);
			lotBindList.add(constantMap.Flag_N);
			lotBindList.add(eventInfo.getEventName());
			lotBindList.add(eventInfo.getEventTimeKey());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventComment());
			lotBindList.add(constantMap.Flag_N);
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(processFlowName);
			lotBindList.add(processFlowVersion);
			lotBindList.add(targetOperationName);
			lotBindList.add(targerOperationVer);
			lotBindList.add("");
			lotBindList.add(constantMap.Lot_NotInRework);
			lotBindList.add(0);
			lotBindList.add(lotData.getUdfs().get("PORTNAME"));
			lotBindList.add(lotData.getUdfs().get("PORTTYPE"));
			lotBindList.add(lotData.getUdfs().get("PORTUSETYPE"));
			lotBindList.add(lotData.getUdfs().get("BEFOREOPERATIONNAME"));
			lotBindList.add(lotData.getUdfs().get("BEFOREOPERATIONVER"));
			lotBindList.add(lotData.getUdfs().get("BEFOREFLOWNAME"));
			lotBindList.add(lotData.getLotGrade());
			lotBindList.add(targetNodeId);
			lotBindList.add("1");
			lotBindList.add("");
			lotBindList.add(trayID);
			lotBindList.add(panelPosition);
			lotBindList.add(lotData.getUdfs().get("LOTDETAILGRADE"));

			updateLotArgList.add(lotBindList.toArray());

			Lot newPanel = new Lot();
			newPanel.setKey(new LotKey(lotName));
			newPanel.setProductionType(productSpecData.getProductionType());
			newPanel.setProductSpecName(productSpecName);
			newPanel.setProductSpecVersion(productSpecVersion);
			newPanel.setOriginalLotName(lotName);
			newPanel.setSourceLotName(lotName);
			newPanel.setParentLotName(lotName);
			newPanel.setRootLotName(lotName);
			newPanel.setProductType(constantMap.ProductType_Panel);
			newPanel.setSubProductUnitQuantity1(0);
			newPanel.setCreateProductQuantity(1);
			newPanel.setCreateSubProductQuantity(0);
			newPanel.setProductQuantity(1);
			newPanel.setSubProductQuantity(0);
			newPanel.setFactoryName(factoryName);
			newPanel.setAreaName(lotData.getAreaName());
			newPanel.setLotState(constantMap.Lot_Released);
			newPanel.setLotProcessState(constantMap.Lot_Wait);
			newPanel.setLotHoldState(constantMap.Flag_N);
			newPanel.setLastLoggedInTime(eventInfo.getEventTime());
			newPanel.setLastLoggedInUser(eventInfo.getEventUser());
			newPanel.setProcessFlowName(processFlowName);
			newPanel.setProcessFlowVersion(processFlowVersion);
			newPanel.setProcessOperationName(targetOperationName);
			newPanel.setProcessOperationVersion(targerOperationVer);
			newPanel.setMachineName("");
			newPanel.setReworkState(constantMap.Lot_NotInRework);
			newPanel.setReworkCount(0);
			newPanel.setLotGrade(lotData.getLotGrade());
			newPanel.setNodeStack(targetNodeId);
			newPanel.setProductRequestName(workOrderName);
			newPanel.setDueDate(tDueDate);
			newPanel.setPriority(1);
			newPanel.setCarrierName(trayID);
			newPanel.setLastEventName(eventInfo.getEventName());
			newPanel.setLastEventTime(eventInfo.getEventTime());
			newPanel.setLastEventTimeKey(eventInfo.getEventTimeKey());
			newPanel.setLastEventComment(eventInfo.getEventComment());
			newPanel.setLastEventUser(eventInfo.getEventUser());

			Map<String, String> lotUdfsForSetHistory = new HashMap<>();
			lotUdfsForSetHistory.put("PORTNAME", lotData.getUdfs().get("PORTNAME"));
			lotUdfsForSetHistory.put("PORTTYPE", lotData.getUdfs().get("PORTTYPE"));
			lotUdfsForSetHistory.put("PORTUSETYPE", lotData.getUdfs().get("PORTUSETYPE"));
			lotUdfsForSetHistory.put("BEFOREOPERATIONNAME", lotData.getUdfs().get("BEFOREOPERATIONNAME"));
			lotUdfsForSetHistory.put("BEFOREOPERATIONVER", lotData.getUdfs().get("BEFOREOPERATIONVER"));
			lotUdfsForSetHistory.put("BEFOREFLOWNAME", lotData.getUdfs().get("BEFOREFLOWNAME"));
			lotUdfsForSetHistory.put("ARRAYLOTNAME", "");
			lotUdfsForSetHistory.put("POSITION", panelPosition);
			newPanel.setUdfs(lotUdfs);

			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(newPanel, newPanel, lotHistory);
			
			updateLotHistoryList.add(lotHistory);

			if (StringUtils.isNotEmpty(productSpecData.getProductionType()) && !StringUtils.equals(lotData.getProductionType(), productSpecData.getProductionType()))
			{
				lotData.getUdfs().put("CHANGEPRODUCTIONTYPE", productSpecData.getProductionType());
				lotDataList.add(lotData);
			}
		}

		updateLotData(eventInfo, updateLotArgList, updateLotHistoryList);

		eventInfo = EventInfoUtil.makeEventInfo("PostCellRMA", getEventUser(), getEventComment(), null, null);
		this.incrementProductRequest(eventInfo, lotList.size(), workOrderName);

		if (lotDataList.size() > 0)
		{
			eventInfo.setEventName("ChangeProductionType");
			eventInfo.setEventTimeKey(ConvertUtil.getCurrTimeKey());

			for (Lot lotData : lotDataList)
			{
				ChangeSpecInfo changeSpecInfo = MESLotServiceProxy.getLotInfoUtil().changeSpecInfo(lotData, lotData.getAreaName(), lotData.getDueDate(), lotData.getFactoryName(),
						lotData.getLotHoldState(), lotData.getLotProcessState(), lotData.getLotState(), lotData.getNodeStack(), lotData.getPriority(), lotData.getProcessFlowName(),
						lotData.getProcessFlowVersion(), lotData.getProcessOperationName(), lotData.getProcessOperationVersion(), lotData.getUdfs().get("CHANGEPRODUCTIONTYPE"),
						lotData.getProductRequestName(), lotData.getProductSpec2Name(), lotData.getProductSpec2Version(), lotData.getProductSpecName(), lotData.getProductSpecVersion(),
						new ArrayList<ProductU>(), lotData.getSubProductUnitQuantity1(), lotData.getSubProductUnitQuantity2());

				MESLotServiceProxy.getLotServiceImpl().changeProcessOperation(eventInfo, lotData, changeSpecInfo);
			}
		}
		
		//SAP Send
		ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(workOrderName);
		SuperProductRequest superWO = new SuperProductRequest();
		
		String sapFlag=CommonUtil.getEnumDefValueStringByEnumName("SAPFLAG");
		if(StringUtil.isNotEmpty(sapFlag)&&StringUtil.equals(sapFlag, "Y")&&
				StringUtils.isNotEmpty(workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
		{
			superWO = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
			
			List<Map<String, String>> ERPReportList = new ArrayList<Map<String, String>>();
			Map<String, String> ERPInfo = new HashMap<>();
			
			ERPInfo.put("SEQ", TimeUtils.getCurrentEventTimeKey());
			ERPInfo.put("PRODUCTREQUESTNAME", superWO.getProductRequestName());
			ERPInfo.put("MATERIALSPECNAME", productSpecName.substring(0, productSpecName.length() - 1));
			ERPInfo.put("PROCESSOPERATIONNAME",porcessOper);
			ERPInfo.put("QUANTITY", String.valueOf(lotList.size()));
			ERPInfo.put("CONSUMEUNIT", superWO.getProductType());
			ERPInfo.put("FACTORYCODE",factoryCode);
			ERPInfo.put("FACTORYPOSITION", factoryPosition);			

		    ERPInfo.put("BATCHNO", batchNo);			
			ERPInfo.put("PRODUCTQUANTITY", String.valueOf(lotList.size()));
			
			Calendar cal = Calendar.getInstance();
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			if(hour >= 19)
			{
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				cal.add(Calendar.DAY_OF_MONTH, 1);
				Timestamp receiveTime = new Timestamp(cal.getTime().getTime());
				ERPInfo.put("EVENTTIME", receiveTime.toString().replace("-","").substring(0,8));
			}
			else
			{
				ERPInfo.put("EVENTTIME", eventInfo.getEventTimeKey().substring(0,8));
			}
			ERPInfo.put("CANCELFLAG", "");
			ERPInfo.put("WSFLAG", "X");
			
			ERPReportList.add(ERPInfo);
			
			eventInfo.setEventName("RMS");
			
			//Send
			try
			{
				ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().sapVXG372Send(eventInfo, ERPReportList, 1, superWO.getProductType());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return doc;
	}
// add by jinlj start
	private void DeleteScrapProduct(String lotName) throws CustomException
	{
		try
		{
			String SeleteSql = "SELECT * FROM CT_SCRAPPRODUCT WHERE PRODUCTNAME = :PRODUCTNAME ";
			
			String DeleteSql = "DELETE FROM CT_SCRAPPRODUCT WHERE PRODUCTNAME = :PRODUCTNAME ";
			
			Map<String, Object> SeleteMap = new HashMap<String, Object>();
			SeleteMap.put("PRODUCTNAME", lotName);
			
			List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(SeleteSql.toString(), SeleteMap);

			if (sqlResult.size() > 0)
			{
				Map<String, Object> DeleteMap = new HashMap<String, Object>();
				DeleteMap.put("PRODUCTNAME", lotName);
				GenericServiceProxy.getSqlMesTemplate().update(DeleteSql, DeleteMap);	
			}
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", e.getMessage());
		}
	}
    //end
	private void DeleteLotData(String lotName) throws CustomException
	{
		try
		{
			String DeleteSql = "DELETE FROM LOT WHERE LOTNAME = :LOTNAME ";
			
			Map<String, Object> DeleteMap = new HashMap<String, Object>();
			DeleteMap.put("LOTNAME", lotName);
			
			GenericServiceProxy.getSqlMesTemplate().update(DeleteSql, DeleteMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", e.getMessage());
		}
	}

	private void incrementProductRequest(EventInfo eventInfo, int incrementQty, String productRequestName) throws CustomException
	{
		ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);

		IncrementReleasedQuantityByInfo incrementReleasedQuantityByInfo = new IncrementReleasedQuantityByInfo();
		incrementReleasedQuantityByInfo.setQuantity(incrementQty);

		int createdQty = Integer.parseInt(workOrderData.getUdfs().get("CREATEDQUANTITY")) + incrementQty;
		
		Map<String, String> productRequestUdfs = workOrderData.getUdfs();
		productRequestUdfs.put("CREATEDQUANTITY", Integer.toString(createdQty));
		incrementReleasedQuantityByInfo.setUdfs(productRequestUdfs);
		
		// Increment Release Qty
		eventInfo.setEventName("IncreamentQuantity");
		workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementReleasedQuantityBy(workOrderData, incrementReleasedQuantityByInfo, eventInfo);
		
		if (workOrderData.getPlanQuantity() < workOrderData.getReleasedQuantity())
		{
			throw new CustomException("PRODUCTREQUEST-0026", String.valueOf(workOrderData.getPlanQuantity()), String.valueOf(workOrderData.getReleasedQuantity()));
		}
	}

	private void updateLotData(EventInfo eventInfo, List<Object[]> updateLotArgList, List<LotHistory> updateLotHistoryList) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO LOT  ");
		sql.append("(LOTNAME, PRODUCTREQUESTNAME, DUEDATE, PRODUCTIONTYPE, PRODUCTSPECNAME, ");
		sql.append(" PRODUCTSPECVERSION, ORIGINALLOTNAME, SOURCELOTNAME, ROOTLOTNAME, PARENTLOTNAME, ");
		sql.append(" PRODUCTTYPE, SUBPRODUCTUNITQUANTITY1, CREATEPRODUCTQUANTITY, CREATESUBPRODUCTQUANTITY, PRODUCTQUANTITY, ");
		sql.append(" SUBPRODUCTQUANTITY, FACTORYNAME, AREANAME, LOTSTATE, LOTPROCESSSTATE, ");
		sql.append(" LOTHOLDSTATE, LASTEVENTNAME, LASTEVENTTIMEKEY, LASTEVENTTIME, LASTEVENTUSER, ");
		sql.append(" LASTEVENTCOMMENT, LASTEVENTFLAG, CREATETIME, CREATEUSER, RELEASETIME, ");
		sql.append(" RELEASEUSER, LASTLOGGEDINTIME, LASTLOGGEDINUSER, PROCESSFLOWNAME, PROCESSFLOWVERSION, ");
		sql.append(" PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION, MACHINENAME, REWORKSTATE, REWORKCOUNT, ");
		sql.append(" PORTNAME, PORTTYPE, PORTUSETYPE, BEFOREOPERATIONNAME, BEFOREOPERATIONVER, ");
		sql.append(" BEFOREFLOWNAME, LOTGRADE, NODESTACK, PRIORITY, ARRAYLOTNAME, ");
		sql.append(" CARRIERNAME, POSITION,LOTDETAILGRADE) ");
		sql.append("VALUES  ");
		sql.append("(?, ?, ?, ?, ?, ");
		sql.append(" ?, ?, ?, ?, ?, ");
		sql.append(" ?, ?, ?, ?, ?, ");
		sql.append(" ?, ?, ?, ?, ?, ");
		sql.append(" ?, ?, ?, ?, ?, ");
		sql.append(" ?, ?, ?, ?, ?, ");
		sql.append(" ?, ?, ?, ?, ?, ");
		sql.append(" ?, ?, ?, ?, ?, ");
		sql.append(" ?, ?, ?, ?, ?, ");
		sql.append(" ?, ?, ?, ?, ?, ");
		sql.append(" ?, ?,? ) ");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateLotArgList);
		try 
		{
			CommonUtil.executeBatch("insert", updateLotHistoryList);
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}
	}

}
