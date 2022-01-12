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
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
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
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.info.ext.ProductU;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.IncrementReleasedQuantityByInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class PostCellRMAForWorkOrder extends SyncHandler {

private static Log log = LogFactory.getLog(CancelTrackIn.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("PostCellRMA", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String workOrderName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);
		String boxID = SMessageUtil.getBodyItemValue(doc, "BOXID", true);
		String batchNo = SMessageUtil.getBodyItemValue(doc, "BATCHNO", false);
		String porcessOper=SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String porcessOperVer=SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		String factoryCode = SMessageUtil.getBodyItemValue(doc, "FACTORYCODE", true);
		String factoryPosition = SMessageUtil.getBodyItemValue(doc, "FACTORYPOSITION", true);
		String trayDeassignFlag = SMessageUtil.getBodyItemValue(doc, "TRAYDEASSIGNFLAG", true);
		
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		List<Lot> lotDataList = new ArrayList<Lot>();

		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		// 1. Get ProductSpec Data
		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, productSpecName, productSpecVersion);
		//反工的重置Tray信息
		List<Durable> TrayList = getCoverListByProcessGroup(boxID);
		if (!TrayList.isEmpty()&&TrayList!=null && !trayDeassignFlag.equals("N"))
		{
			for(Durable tray : TrayList)
			{
				DurableSpec durableSpecData = GenericServiceProxy.getSpecUtil().getDurableSpec(tray.getFactoryName(), tray.getDurableSpecName(),
						tray.getDurableSpecVersion());
				
				tray.setDurableType(constantMap.DURABLETYPE_Tray);
				tray.setCapacity(durableSpecData.getDefaultCapacity());
				tray.setLotQuantity(0);
				tray.setDurableState(constantMap.Dur_Available);
				DurableServiceProxy.getDurableService().update(tray);
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("COVERNAME", "");
				setEventInfo.getUdfs().put("POSITION", "");
				setEventInfo.getUdfs().put("DURABLETYPE1", "Tray");
				setEventInfo.getUdfs().put("BCRFLAG", "");
				setEventInfo.getUdfs().put("OPERATIONMODE", "");
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(tray, setEventInfo, eventInfo);						
			}
		}
		else if(!TrayList.isEmpty()&&TrayList!=null && !trayDeassignFlag.equals("Y"))
		{
			for(Durable tray : TrayList)
			{
				DurableSpec durableSpecData = GenericServiceProxy.getSpecUtil().getDurableSpec(tray.getFactoryName(), tray.getDurableSpecName(),
						tray.getDurableSpecVersion());
				tray.setDurableState(constantMap.Dur_InUse);
				DurableServiceProxy.getDurableService().update(tray);
				SetEventInfo setEventInfo = new SetEventInfo();
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(tray, setEventInfo, eventInfo);	
			}
			
		}
		for (Element lot : lotList)
		{
			String lotName = lot.getChildText("LOTNAME");

			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			Lot oldLotData = (Lot) ObjectUtil.copyTo(lotData);

			if (!lotData.getLotState().equals("Shipped"))
				throw new CustomException("PROCESSGROUP-0008", lotName);

			ProcessFlow processFlow = CommonUtil.getProcessFlowData(factoryName, processFlowName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
			//String startNodeStack = ProcessFlowServiceProxy.getProcessFlowService().getStartNode(processFlow.getKey()).getKey().getNodeId();

			//kr.co.aim.greentrack.processflow.management.iter.NodeStack nodeStack = NodeStackUtil.stringToNodeStack(startNodeStack);
			//ProcessFlowIterator pfi = new ProcessFlowIterator(processFlow, nodeStack, "");

			//lotData.setNodeStack(startNodeStack);

			@SuppressWarnings("rawtypes")
			//PFIValueSetter valueSetter = new LotPFIValueSetter(pfi, oldLotData, lotData);
			//pfi.moveNext("N", valueSetter);
			
			//Node nextNode = pfi.getCurrentNodeData();

			//String targetOperationName = nextNode.getNodeAttribute1();
			//String targerOperationVer = nextNode.getNodeAttribute2();
			//String targetNodeId = nextNode.getKey().getNodeId();
			//porcessOper=targetOperationName;
			Node changeNode = ProcessFlowServiceProxy.getNodeService().getNode("POSTCELL", processFlowName, processFlowVersion, constantMap.Node_ProcessOperation, porcessOper, porcessOperVer);
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
			this.DeleteWMSIFDate(lotName,boxID);

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
			//lotBindList.add(targetOperationName);
			//lotBindList.add(targerOperationVer);
			lotBindList.add(porcessOper);
			lotBindList.add(porcessOperVer);
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
			//lotBindList.add(targetNodeId);
			lotBindList.add(changeNode.getKey().getNodeId());
			lotBindList.add("1");
			lotBindList.add("");
			
			if(trayDeassignFlag.equals("N"))
			{
				lotBindList.add(lotData.getCarrierName());
				lotBindList.add(lotData.getUdfs().get("POSITION"));
			}
			else 
			{
				lotBindList.add("");
				lotBindList.add("");
			}

			lotBindList.add(lotData.getUdfs().get("LOTDETAILGRADE"));
			lotBindList.add(eventInfo.getEventTime());//jinlj 20210907
			lotBindList.add(eventInfo.getEventUser());//jinlj 20210907
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
			//newPanel.setProcessOperationName(targetOperationName);
			//newPanel.setProcessOperationVersion(targerOperationVer);//porcessOper
			newPanel.setProcessOperationName(porcessOper);
			newPanel.setProcessOperationVersion(porcessOperVer);//porcessOper
			newPanel.setMachineName("");
			newPanel.setReworkState(constantMap.Lot_NotInRework);
			newPanel.setReworkCount(0);
			newPanel.setLotGrade(lotData.getLotGrade());
			newPanel.setNodeStack(changeNode.getKey().getNodeId());
			newPanel.setProductRequestName(workOrderName);
			newPanel.setDueDate(tDueDate);
			newPanel.setPriority(1);
			if(trayDeassignFlag.equals("N"))
			{
				newPanel.setCarrierName(lotData.getCarrierName());
			}
			else
			{
				newPanel.setCarrierName("");
			}
			
			newPanel.setLastEventName(eventInfo.getEventName());
			newPanel.setLastEventTime(eventInfo.getEventTime());
			newPanel.setLastEventTimeKey(eventInfo.getEventTimeKey());
			newPanel.setLastEventComment(eventInfo.getEventComment());
			newPanel.setLastEventUser(eventInfo.getEventUser());
			newPanel.setLastLoggedOutTime(eventInfo.getEventTime());//20210908 jinlj
			newPanel.setLastLoggedOutUser(eventInfo.getEventUser());//20210908 jinlj
			Map<String, String> lotUdfsForSetHistory = new HashMap<>();
			lotUdfsForSetHistory.put("PORTNAME", lotData.getUdfs().get("PORTNAME"));
			lotUdfsForSetHistory.put("PORTTYPE", lotData.getUdfs().get("PORTTYPE"));
			lotUdfsForSetHistory.put("PORTUSETYPE", lotData.getUdfs().get("PORTUSETYPE"));
			lotUdfsForSetHistory.put("BEFOREOPERATIONNAME", lotData.getUdfs().get("BEFOREOPERATIONNAME"));
			lotUdfsForSetHistory.put("BEFOREOPERATIONVER", lotData.getUdfs().get("BEFOREOPERATIONVER"));
			lotUdfsForSetHistory.put("BEFOREFLOWNAME", lotData.getUdfs().get("BEFOREFLOWNAME"));
			lotUdfsForSetHistory.put("ARRAYLOTNAME", "");
			if(trayDeassignFlag.equals("N"))
			{
				lotUdfsForSetHistory.put("POSITION", lotData.getUdfs().get("POSITION"));
			}
			else
			{
				lotUdfsForSetHistory.put("POSITION", "");
			}
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
	private List<Durable> getCoverListByProcessGroup(String boxID) {
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT  distinct  L.CARRIERNAME  FROM LOT L,MES_WMSIF_RECEIVESHT@OADBLINK.V3FAB.COM SH ");//@OADBLINK.V3FAB.COM
		//sql.append("SELECT  distinct  L.CARRIERNAME  FROM LOT L,MES_WMSIF_RECEIVESHT SH ");//TEST
		sql.append(" WHERE  L.LOTNAME =SH.PRD_SEQ_ID ");
		sql.append("     AND SH.INNER_ID=:BOX_ID ");
		sql.append("     AND L.LOTSTATE = 'Shipped' ");
		sql.append("     AND L.CARRIERNAME IS NOT NULL ");
		
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("BOX_ID", boxID);
		
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);
		List<Durable> TrayDataList = new ArrayList<Durable>();
		if(sqlResult != null && !sqlResult.isEmpty())
		{
			String condition = "WHERE DURABLENAME IN(";
			Object[] bindSet = new Object[] {};
			
			for (Map<String, Object> coverNameMap : sqlResult) 
			{
				String durableName = coverNameMap.get("CARRIERNAME").toString();
				
				condition += "'" + durableName + "',";
			}
			condition = condition.substring(0, condition.length() - 1) + ")";
			try
			{
			TrayDataList = DurableServiceProxy.getDurableService().select(condition, bindSet);
			}
			catch (Exception de)
			{
				TrayDataList = null;
			}

		}
		return TrayDataList;
	}
	private void DeleteWMSIFDate(String lotName, String boxID)  throws CustomException{
	try
	{
		//String SeleteSql = "SELECT * FROM MES_WMSIF_RECEIVESHT@OADBLINK.V3FAB.COM WHERE PRD_SEQ_ID = :PRODUCTNAME AND BOX_ID = :BOX_ID ";//@OADBLINK.V3FAB.COM
		//String SeleteSql = "SELECT * FROM MES_WMSIF_RECEIVESHT WHERE PRD_SEQ_ID = :PRODUCTNAME AND INNER_ID = :INNER_ID ";//TEST
		String SeleteSql = "SELECT * FROM MES_WMSIF_RECEIVESHT@OADBLINK.V3FAB.COM WHERE PRD_SEQ_ID = :PRODUCTNAME AND INNER_ID = :INNER_ID ";//PRD
		//String DeleteSql = "DELETE FROM MES_WMSIF_RECEIVESHT@OADBLINK.V3FAB.COM  WHERE PRD_SEQ_ID = :PRODUCTNAME AND BOX_ID = :BOX_ID ";
		//String DeleteSql = "DELETE FROM MES_WMSIF_RECEIVESHT  WHERE PRD_SEQ_ID = :PRODUCTNAME AND INNER_ID = :INNER_ID ";//TEST
		String DeleteSql = "DELETE FROM MES_WMSIF_RECEIVESHT@OADBLINK.V3FAB.COM  WHERE PRD_SEQ_ID = :PRODUCTNAME AND INNER_ID = :INNER_ID ";//PRD
		Map<String, Object> SeleteMap = new HashMap<String, Object>();
		SeleteMap.put("PRODUCTNAME", lotName);
		SeleteMap.put("INNER_ID", boxID);//INNER_ID
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(SeleteSql.toString(), SeleteMap);

		if (sqlResult.size() > 0)
		{
			Map<String, Object> DeleteMap = new HashMap<String, Object>();
			DeleteMap.put("PRODUCTNAME", lotName);
			DeleteMap.put("INNER_ID", boxID);//INNER_ID
			GenericServiceProxy.getSqlMesTemplate().update(DeleteSql, DeleteMap);	
		}
	}
	catch (Exception e)
	{
		throw new CustomException("SYS-8001", e.getMessage());
	}
		
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
		
		if (Integer.parseInt(workOrderData.getUdfs().get("CREATEDQUANTITY")) < workOrderData.getReleasedQuantity())
		{
			throw new CustomException("PRODUCTREQUEST-0031");
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
		sql.append(" CARRIERNAME, POSITION,LOTDETAILGRADE,LASTLOGGEDOUTTIME,LASTLOGGEDOUTUSER) ");
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
		//sql.append(" ?, ?,? ) ");
		sql.append(" ?, ?, ?, ?, ? ) ");

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
