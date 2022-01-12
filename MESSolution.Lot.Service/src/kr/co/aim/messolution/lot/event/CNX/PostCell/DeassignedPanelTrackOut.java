package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
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
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class DeassignedPanelTrackOut extends SyncHandler {

	private static Log log = LogFactory.getLog(DeassignedPanelTrackOut.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		// Get Doc
		String durableName = SMessageUtil.getBodyItemValue(doc, "TRAYNAME", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String trayGroupName = SMessageUtil.getBodyItemValue(doc, "TRAYGROUPNAME", true);
		String seq = SMessageUtil.getBodyItemValue(doc, "SEQ", true);

		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Deassign", getEventUser(), getEventComment(), null, null);
		// Update TrayGroup Data
		setTrayGroupData(eventInfo, trayGroupName, lotList.size(), Integer.parseInt(seq));

		// Deassign old tray
		deassignPanel(eventInfo, lotList);
        
		

		boolean reserveFlag = false;

		Lot lotInfo = MESLotServiceProxy.getLotServiceUtil().getLotData(lotList.get(0).getChildText("LOTNAME"));

		Node nextNode = getNextNode(lotInfo, factoryName);

		eventInfo.setEventName("TrackOut");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		EventInfo futureActionEventInfo = (EventInfo) ObjectUtil.copyTo(eventInfo);
		futureActionEventInfo.setEventName("Delete");

		// TrackOut With New Tray
		reserveFlag = trackOutPanelwithNewTray(eventInfo, lotList, nextNode, durableName, futureActionEventInfo, reserveFlag);

		// Scrap Panel
		if (StringUtils.equals(lotInfo.getLotGrade(), "S"))
		{
			EventInfo eventInfoScrap = EventInfoUtil.makeEventInfo("Scrap", getEventUser(), getEventComment(), null, null);//caixu 2020/12/07 ScrapLot Modify Scrap
			eventInfoScrap.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoScrap.setEventTime(TimeStampUtil.getCurrentTimestamp());

			scrapPanel(eventInfoScrap, lotList, lotInfo);
		}
		
		PanelHoldBYS1(lotInfo.getKey().getLotName(),reserveFlag);
		// Future Hold
		if (reserveFlag)
		{
			EventInfo eventInfoHold = EventInfoUtil.makeEventInfo("ReserveHold", getEventUser(), getEventComment(), null, null);
			eventInfoHold.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoHold.setEventTime(TimeStampUtil.getCurrentTimestamp());

			MESLotServiceProxy.getLotServiceUtil().PanelHoldByTray(eventInfoHold, durableName);
		}

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

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return result;
	}

	private void deassignPanel(EventInfo eventInfo, List<Element> lotList) throws CustomException
	{

		List<Object[]> deassignLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryListDeassign = new ArrayList<LotHistory>();

		for (Element lotData : lotList)
		{
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);

			ProcessOperationSpec OperationSpecData = CommonUtil.getProcessOperationSpec(lot.getFactoryName(), lot.getProcessOperationName(), lot.getProcessOperationVersion());

			if (!OperationSpecData.getDetailProcessOperationType().equals("FQC"))
				throw new CustomException("LOT-0089", OperationSpecData.getDetailProcessOperationType());

			if (!lot.getLotProcessState().equals(GenericServiceProxy.getConstantMap().Lot_Run))
				throw new CustomException("LOT-0088", lot.getKey().getLotName(), lot.getLotProcessState());

			List<Object> lotBindList = new ArrayList<Object>();

			lotBindList.add("");
			lotBindList.add(eventInfo.getEventName());
			lotBindList.add(eventInfo.getEventTimeKey());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventComment());
			lotBindList.add(lot.getKey().getLotName());

			deassignLotArgList.add(lotBindList.toArray());

			// History
			lot.setCarrierName("");
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());

			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
			
			updateLotHistoryListDeassign.add(lotHistory);

			// Update Old Tray Data
			setTrayData(eventInfo, oldLot);
		}

		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE LOT ");
		sql.append("   SET CARRIERNAME = ?, ");
		sql.append("       LASTEVENTNAME = ?, ");
		sql.append("       LASTEVENTTIMEKEY = ?, ");
		sql.append("       LASTEVENTTIME = ?, ");
		sql.append("       LASTEVENTUSER = ?, ");
		sql.append("       LASTEVENTCOMMENT = ? ");
		sql.append(" WHERE LOTNAME = ? ");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), deassignLotArgList);
		try 
		{
			CommonUtil.executeBatch("insert", updateLotHistoryListDeassign);
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}
	}

	private void setTrayData(EventInfo eventInfo, Lot oldLot) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		Durable oldDurableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(oldLot.getCarrierName());
		Durable durableInfo = (Durable) ObjectUtil.copyTo(oldDurableInfo);

		long durLotQty = oldDurableInfo.getLotQuantity() - 1;
		durableInfo.setLotQuantity(durLotQty);

		if (durLotQty == 0)
		{
			Map<String, String> udfs = durableInfo.getUdfs();
			udfs.put("COVERNAME", "");
			udfs.put("POSITION", "");
			udfs.put("DURABLETYPE1", "Tray");
			durableInfo.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
			durableInfo.setUdfs(udfs);
		}
		else
		{
			durableInfo.setUdfs(oldDurableInfo.getUdfs());
		}

		durableInfo.setLastEventName(eventInfo.getEventName());
		durableInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		durableInfo.setLastEventTime(eventInfo.getEventTime());
		durableInfo.setLastEventUser(eventInfo.getEventUser());
		durableInfo.setLastEventComment(eventInfo.getEventComment());

		DurableHistory durHistory = new DurableHistory();
		durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldDurableInfo, durableInfo, durHistory);

		DurableServiceProxy.getDurableService().update(durableInfo);
		DurableServiceProxy.getDurableHistoryService().insert(durHistory);
	}

	private void setTrayGroupData(EventInfo eventInfo, String trayGroupName, long lotQty, int seq) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		Durable oldTrayGroupInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayGroupName);
		Durable trayGroupInfo = (Durable) ObjectUtil.copyTo(oldTrayGroupInfo);
		List<Durable> trayList = MESDurableServiceProxy.getDurableServiceUtil().getSubTrayListByCoverTray(trayGroupName,false);
		if(trayList.size()>0)
		{
			long durLotQty = oldTrayGroupInfo.getLotQuantity() - lotQty;
			trayGroupInfo.setLotQuantity(durLotQty);

			if (durLotQty == 0)
			{
				trayGroupInfo.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
				trayGroupInfo.setDurableType("Tray");

				Map<String, String> udfs = trayGroupInfo.getUdfs();
				udfs.put("COVERNAME", "");
				udfs.put("POSITION", "");
				udfs.put("DURABLETYPE1", "Tray");
				trayGroupInfo.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);
				trayGroupInfo.setUdfs(udfs);

				ExtendedObjectProxy.getFQCLotService().completeFQCLotData(eventInfo, trayGroupName, seq);
			}
			else
			{
				trayGroupInfo.setUdfs(oldTrayGroupInfo.getUdfs());
			}

			trayGroupInfo.setLastEventName(eventInfo.getEventName());
			trayGroupInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
			trayGroupInfo.setLastEventTime(eventInfo.getEventTime());
			trayGroupInfo.setLastEventUser(eventInfo.getEventUser());
			trayGroupInfo.setLastEventComment(eventInfo.getEventComment());

			DurableHistory trayGroupHistory = new DurableHistory();
			trayGroupHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldTrayGroupInfo, trayGroupInfo, trayGroupHistory);

			DurableServiceProxy.getDurableService().update(trayGroupInfo);
			DurableServiceProxy.getDurableHistoryService().insert(trayGroupHistory);
			
		}
	}

	private void trackOutPanel(EventInfo eventInfo, List<Object[]> updateLotArgList, List<LotHistory> updateLotHistoryList) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE LOT ");
		sql.append("   SET LOTSTATE = ?, ");
		sql.append("       LOTPROCESSSTATE = ?, ");
		sql.append("       LASTEVENTNAME = ?, ");
		sql.append("       LASTEVENTTIMEKEY = ?, ");
		sql.append("       LASTEVENTTIME = ?, ");
		sql.append("       LASTEVENTUSER = ?, ");
		sql.append("       LASTEVENTCOMMENT = ?, ");
		sql.append("       LASTEVENTFLAG = ?, ");
		sql.append("       LASTLOGGEDOUTTIME = ?, ");
		sql.append("       LASTLOGGEDOUTUSER = ?, ");
		sql.append("       PROCESSOPERATIONNAME = ?, ");
		sql.append("       PROCESSOPERATIONVERSION = ?, ");
		sql.append("       BEFOREOPERATIONNAME = ?, ");
		sql.append("       BEFOREOPERATIONVER = ?, ");
		sql.append("       BEFOREFLOWNAME = ?, ");
		sql.append("       POSITION = ?, ");
		sql.append("       NODESTACK = ?, ");
		sql.append("       CARRIERNAME = ? ");
		sql.append(" WHERE LOTNAME = ? ");

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

	private void assignNewTray(EventInfo eventInfo, String durableName, long lotQty) throws CustomException
	{
		Durable olddurableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		Durable durableInfo = (Durable) ObjectUtil.copyTo(olddurableInfo);

		CommonValidation.checkAvailableCst(durableInfo);

		durableInfo.setLotQuantity(lotQty);
		durableInfo.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
		durableInfo.setLastEventName(eventInfo.getEventName());
		durableInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		durableInfo.setLastEventTime(eventInfo.getEventTime());
		durableInfo.setLastEventUser(eventInfo.getEventUser());
		durableInfo.setLastEventComment(eventInfo.getEventComment());

		DurableHistory durHistory = new DurableHistory();
		durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfo, durableInfo, durHistory);

		DurableServiceProxy.getDurableService().update(durableInfo);
		DurableServiceProxy.getDurableHistoryService().insert(durHistory);
	}

	private void scrapPanel(EventInfo eventInfoScrap, List<Element> lotList, Lot lotInfo) throws CustomException
	{
		List<Object[]> updateLotArgListScrap = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		for (Element lotData : lotList)
		{
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);

			List<Object> lotBindList = new ArrayList<Object>();

			lotBindList.add(GenericServiceProxy.getConstantMap().Lot_Scrapped);
			lotBindList.add(eventInfoScrap.getEventName());
			lotBindList.add(eventInfoScrap.getEventTimeKey());
			lotBindList.add(eventInfoScrap.getEventTime());
			lotBindList.add(eventInfoScrap.getEventUser());
			lotBindList.add(eventInfoScrap.getEventComment());
			lotBindList.add(GenericServiceProxy.getConstantMap().Flag_N);
			lotBindList.add("Auto Scrap");
			lotBindList.add(lotData.getChildText("LOTNAME"));

			updateLotArgListScrap.add(lotBindList.toArray());

			// History
			lot.setLotState(GenericServiceProxy.getConstantMap().Lot_Scrapped);
			lot.setReasonCode("Auto Scrap");
			lot.setLastEventName(eventInfoScrap.getEventName());
			lot.setLastEventTime(eventInfoScrap.getEventTime());
			lot.setLastEventTimeKey(eventInfoScrap.getEventTimeKey());
			lot.setLastEventComment(eventInfoScrap.getEventComment());
			lot.setLastEventUser(eventInfoScrap.getEventUser());

			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
			
			updateLotHistoryList.add(lotHistory);
		}

		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE LOT ");
		sql.append("   SET LOTSTATE = ?, ");
		sql.append("       LASTEVENTNAME = ?, ");
		sql.append("       LASTEVENTTIMEKEY = ?, ");
		sql.append("       LASTEVENTTIME = ?, ");
		sql.append("       LASTEVENTUSER = ?, ");
		sql.append("       LASTEVENTCOMMENT = ?, ");
		sql.append("       LASTEVENTFLAG = ?, ");
		sql.append("       REASONCODE = ? ");
		sql.append(" WHERE LOTNAME = ? ");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateLotArgListScrap);
		try 
		{
			CommonUtil.executeBatch("insert", updateLotHistoryList);
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}

		// ChangeWorkOrder Scrap
		ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfoScrap, lotInfo.getProductRequestName(), updateLotArgListScrap.size(), 0);

		/*if (newProductRequestData.getPlanQuantity() == newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity())
		{
			EventInfo newEventInfo = eventInfoScrap;
			newEventInfo.setEventName("Complete");
			MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(newEventInfo, lotInfo.getProductRequestName());
		}*///屏蔽自动Complete的功能	
	}

	private Node getNextNode(Lot lotInfo, String factoryName)
	{
		Node nextNode = new Node();
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		if (lotInfo.getLotGrade().equals("S"))
		{
			List<Map<String, Object>> operationData = getOperationByDetailOperType(lotInfo.getFactoryName(), lotInfo.getProductSpecName(), lotInfo.getProductSpecVersion(),
					lotInfo.getProcessFlowName(), lotInfo.getProcessFlowVersion(), "ScrapPack");

			nextNode = ProcessFlowServiceProxy.getNodeService().getNode(lotInfo.getFactoryName(), lotInfo.getProcessFlowName(), lotInfo.getProcessFlowVersion(), constantMap.Node_ProcessOperation,
					operationData.get(0).get("PROCESSOPERATIONNAME").toString(), operationData.get(0).get("PROCESSOPERATIONVERSION").toString());
		}
		else
		{
			String sql = "SELECT DESCRIPTION FROM ENUMDEFVALUE "
					+ "WHERE ENUMNAME = :ENUMNAME AND ENUMVALUE = :ENUMVALUE";
		
			Map<String, String> bindMap = new HashMap<String, String>();
			bindMap.put("ENUMNAME", "FQCNGTrackOutOpera");
			bindMap.put("ENUMVALUE", lotInfo.getProcessFlowName());
			
			List<Map<String, Object>> sqlResult = 
					GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
			
			if(sqlResult!=null && sqlResult.size()>0)
			{
				nextNode = ProcessFlowServiceProxy.getNodeService().getNode(factoryName, lotInfo.getProcessFlowName(), lotInfo.getProcessFlowVersion(), constantMap.Node_ProcessOperation,
						sqlResult.get(0).get("DESCRIPTION").toString(), "00001");
			}
			else 
			{
				nextNode = ProcessFlowServiceProxy.getNodeService().getNode(factoryName, lotInfo.getProcessFlowName(), lotInfo.getProcessFlowVersion(), constantMap.Node_ProcessOperation,
						"35052", "00001");//caixu 2020/12/7 Modify MVI Operation
			}
		}

		return nextNode;
	}

	private boolean trackOutPanelwithNewTray(EventInfo eventInfo, List<Element> lotList, Node nextNode, String durableName, EventInfo futureActionEventInfo, boolean reserveFlag)
			throws CustomException
	{
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		for (Element lotData : lotList)
		{
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);
			String position = lotData.getChildText("POSITION");

			boolean reserveCheck = MESLotServiceProxy.getLotServiceUtil().PostCellDeleteLotFutureAction(futureActionEventInfo, lot, nextNode.getNodeAttribute1(), nextNode.getNodeAttribute2());

			if (reserveCheck)
				reserveFlag = true;

			List<Object> lotBindList = new ArrayList<Object>();
			lotBindList.add(GenericServiceProxy.getConstantMap().Lot_Released);
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

			// History
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

		trackOutPanel(eventInfo, updateLotArgList, updateLotHistoryList);
		assignNewTray(eventInfo, durableName, lotList.size());

		return reserveFlag;
	}
	private void PanelHoldBYS1(String LotName,boolean reserveFlag ) throws CustomException
	{
		try
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Hold", this.getEventUser(), this.getEventComment());

			StringBuilder sql = new StringBuilder();
			sql.append("SELECT * ");
			sql.append(" FROM LOT ");
			sql.append(" WHERE LOTNAME IN (:LOTLIST) ");
			sql.append(" AND PROCESSOPERATIONNAME='37000'");
			sql.append(" AND LOTGRADE='S'");
			sql.append(" AND LOTDETAILGRADE='S1'");
			

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTLIST", LotName);
            
			List<Map<String, Object>> resultList = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), bindMap);
			if (resultList.size() > 0)
			{
				reserveFlag = true;
			}

	
		}
		catch (Exception e)
		{
			log.info("Error Occurred - Hold TrayGroup by Mismatched VCR ID");
		}
	}
}
