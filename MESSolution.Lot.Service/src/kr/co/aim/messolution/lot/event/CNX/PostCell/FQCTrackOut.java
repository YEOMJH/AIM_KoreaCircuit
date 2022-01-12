package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
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
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class FQCTrackOut extends SyncHandler {

	private static Log log = LogFactory.getLog(FQCTrackOut.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String fqcLotName = SMessageUtil.getBodyItemValue(doc, "FQCLOTNAME", true);
		String lotDetailGrade = SMessageUtil.getBodyItemValue(doc, "GRADE", true);
		String seq = SMessageUtil.getBodyItemValue(doc, "SEQ", true);
		String manualResult = SMessageUtil.getBodyItemValue(doc, "MANUALRESULT", true);

		// EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOut", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		EventInfo futureActionEventInfo = (EventInfo) ObjectUtil.copyTo(eventInfo);
		futureActionEventInfo.setEventName("Delete");

		boolean reserveFlag = false;

		// SQL
		List<Map<String, Object>> lotList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayGroup(fqcLotName);
		if (lotList == null || lotList.size() == 0)
		{
			throw new CustomException("SYS-0010", "Panel data by TrayGroup is not exist");
		}
		Lot lotInfo = MESLotServiceProxy.getLotServiceUtil().getLotData(lotList.get(0).get("LOTNAME").toString());
		Lot oldLotInfo = (Lot) ObjectUtil.copyTo(lotInfo);

		Node nextNode = getNextNode(manualResult, lotInfo, oldLotInfo, factoryName, lotDetailGrade);

		// TrackOut Panel
		reserveFlag = trackOutPanelList(eventInfo, lotList, nextNode, futureActionEventInfo, reserveFlag);

		// Complete FQCLot
		ExtendedObjectProxy.getFQCLotService().completeFQCLotData(eventInfo, fqcLotName, Long.parseLong(seq));
		//{2020/12/7 Add Scrap
		if (StringUtils.equals(lotInfo.getLotGrade(), "S"))
		{
			EventInfo eventInfoScrap = EventInfoUtil.makeEventInfo("Scrap", getEventUser(), getEventComment(), null, null);//caixu 2020/12/07 ScrapLot Modify Scrap
			eventInfoScrap.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoScrap.setEventTime(TimeStampUtil.getCurrentTimestamp());

			scrapPanel(eventInfoScrap, lotList, lotInfo);
		}//}
		
		if(lotDetailGrade.equals("S1"))//CAICU 2020/12/10 Add SI Hold
		{
			EventInfo eventInfoHold = EventInfoUtil.makeEventInfo("Hold By S1", getEventUser(), getEventComment(), null, null);
			eventInfoHold.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoHold.setEventTime(TimeStampUtil.getCurrentTimestamp());

			MESLotServiceProxy.getLotServiceUtil().PanelHoldByTrayGroup(eventInfoHold, fqcLotName);
		}
		


		// Future Hold
		if (reserveFlag)
		{
			EventInfo eventInfoHold = EventInfoUtil.makeEventInfo("ReserveHold", getEventUser(), getEventComment(), null, null);
			eventInfoHold.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoHold.setEventTime(TimeStampUtil.getCurrentTimestamp());

			MESLotServiceProxy.getLotServiceUtil().PanelHoldByTrayGroup(eventInfoHold, fqcLotName);
		}

		return doc;
	}

	private void updateLotData(EventInfo eventInfo, List<Object[]> updateLotArgList, List<LotHistory> updateLotHistoryList) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE LOT ");
		sql.append("   SET LOTPROCESSSTATE = ?, ");
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
		sql.append("       NODESTACK = ? ");
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

	private Node getNextNode(String manualResult, Lot lotInfo, Lot oldLotInfo, String factoryName, String lotDetailGrade) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		Node nextNode = new Node();
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		if (manualResult.equals("NG"))
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
		else
		// Next Node
		{
			if (lotDetailGrade.equals("S")||lotDetailGrade.equals("S1"))
			{
				List<Map<String, Object>> operationData = getOperationByDetailOperType(lotInfo.getFactoryName(), lotInfo.getProductSpecName(), lotInfo.getProductSpecVersion(),
						lotInfo.getProcessFlowName(), lotInfo.getProcessFlowVersion(), "ScrapPack");
				
				nextNode = ProcessFlowServiceProxy.getNodeService().getNode(lotInfo.getFactoryName(), lotInfo.getProcessFlowName(), lotInfo.getProcessFlowVersion(), constantMap.Node_ProcessOperation,
						operationData.get(0).get("PROCESSOPERATIONNAME").toString(), operationData.get(0).get("PROCESSOPERATIONVERSION").toString());
			}
			else
			{
				ProcessFlowKey processFlowKey = new ProcessFlowKey();

				processFlowKey.setFactoryName(lotInfo.getFactoryName());
				processFlowKey.setProcessFlowName(lotInfo.getProcessFlowName());
				processFlowKey.setProcessFlowVersion(lotInfo.getProcessFlowVersion());

				ProcessFlow processFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

				kr.co.aim.greentrack.processflow.management.iter.NodeStack nodeStack = NodeStackUtil.stringToNodeStack(lotInfo.getNodeStack());
				ProcessFlowIterator pfi = new ProcessFlowIterator(processFlow, nodeStack, "");
				


				PFIValueSetter<Lot, ProductSpec, ProcessFlow> valueSetter = new LotPFIValueSetter(pfi, oldLotInfo, lotInfo );
				pfi.moveNext("N", valueSetter);

				nextNode = pfi.getCurrentNodeData();
			}
		}

		return nextNode;
	}
	//{
	private void scrapPanel(EventInfo eventInfoScrap, List<Map<String, Object>> lotList, Lot lotInfo) throws CustomException
	{
		List<Object[]> updateLotArgListScrap = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		for (Map<String, Object> lotData : lotList)
		{
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.get("LOTNAME").toString());
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
			lotBindList.add(lotData.get("LOTNAME").toString());

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
		}*///2020/12/23 caixu 屏蔽自动Complete的功能	
	}//} caixu 2020 12/17 Add Scrap


	private boolean trackOutPanelList(EventInfo eventInfo, List<Map<String, Object>> lotList, Node nextNode, EventInfo futureActionEventInfo, boolean reserveFlag) throws FrameworkErrorSignal,
			NotFoundSignal, CustomException
	{
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		for (Map<String, Object> lotData : lotList)
		{
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.get("LOTNAME").toString());
			Lot oldLot = (Lot) ObjectUtil.copyTo(lot);

			boolean reserveCheck = MESLotServiceProxy.getLotServiceUtil().PostCellDeleteLotFutureAction(futureActionEventInfo, lot, nextNode.getNodeAttribute1(), nextNode.getNodeAttribute2());

			ProcessOperationSpec OperationSpecData = CommonUtil.getProcessOperationSpec(lot.getFactoryName(), lot.getProcessOperationName(), lot.getProcessOperationVersion());

			if (!OperationSpecData.getDetailProcessOperationType().equals("FQC"))
				throw new CustomException("LOT-0089", OperationSpecData.getDetailProcessOperationType());

			if (!lot.getLotProcessState().equals(GenericServiceProxy.getConstantMap().Lot_Run))
				throw new CustomException("LOT-0088", lot.getKey().getLotName(), lot.getLotProcessState());

			// add end
			if (reserveCheck)
				reserveFlag = true;

			List<Object> lotBindList = new ArrayList<Object>();

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
			lotBindList.add(oldLot.getProcessOperationName());
			lotBindList.add(oldLot.getProcessOperationVersion());
			lotBindList.add(nextNode.getKey().getNodeId());
			lotBindList.add(lot.getKey().getLotName());

			updateLotArgList.add(lotBindList.toArray());

			// History
			lot.setLotProcessState(constantMap.Lot_Wait);
			lot.setLastLoggedOutTime(eventInfo.getEventTime());
			lot.setLastLoggedOutUser(eventInfo.getEventUser());
			lot.setProcessOperationName(nextNode.getNodeAttribute1());
			lot.setProcessOperationVersion(nextNode.getNodeAttribute2());
			lot.setNodeStack(nextNode.getKey().getNodeId());
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());

			Map<String, String> lotUdf = new HashMap<>();
			lotUdf = lot.getUdfs();
			lotUdf.put("BEFOREOPERATIONNAME", lot.getProcessOperationName());
			lotUdf.put("BEFOREOPERATIONVER", lot.getProcessOperationVersion());
			lot.setUdfs(lotUdf);

			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
			
			updateLotHistoryList.add(lotHistory);
		}

		this.updateLotData(eventInfo, updateLotArgList, updateLotHistoryList);

		return reserveFlag;
	}
}
