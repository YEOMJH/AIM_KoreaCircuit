package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.IPQCLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class IPQCTrackOut extends SyncHandler {

	private static Log log = LogFactory.getLog(IPQCTrackOut.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String fqcLotName = SMessageUtil.getBodyItemValue(doc, "FQCLOTNAME", true);
		String lotDetailGrade = SMessageUtil.getBodyItemValue(doc, "GRADE", true);
		String seq = SMessageUtil.getBodyItemValue(doc, "SEQ", true);
		String fqcResult = SMessageUtil.getBodyItemValue(doc, "FQCRESULT", true);
		String manualResult = SMessageUtil.getBodyItemValue(doc, "MANUALRESULT", true);
		
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		// EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOut", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		EventInfo futureActionEventInfo = (EventInfo)ObjectUtil.copyTo(eventInfo);
		futureActionEventInfo.setEventName("Delete");
		
		boolean reserveFlag = false;

		// SQL
		String queryStringLot = "UPDATE LOT SET LOTPROCESSSTATE = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
				+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LASTEVENTFLAG = ?, LASTLOGGEDOUTTIME = ?, LASTLOGGEDOUTUSER = ?, "
				+ "PROCESSOPERATIONNAME = ?, PROCESSOPERATIONVERSION = ?,  BEFOREOPERATIONNAME = ?, BEFOREOPERATIONVER = ?, "
				+ "NODESTACK = ? WHERE LOTNAME = ?";
		
		List<Map<String, Object>> lotList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayGroup(fqcLotName);
		Lot lotInfo = MESLotServiceProxy.getLotServiceUtil().getLotData(lotList.get(0).get("LOTNAME").toString());
		Lot oldLotInfo = (Lot) ObjectUtil.copyTo(lotInfo);
		
		Node nextNode = new Node();
		
		if(manualResult.equals("NG"))
		{
			String processOperationName = lotInfo.getUdfs().get("BEFOREOPERATIONNAME");
			String processOperationVersion = "00001";
			
			nextNode = ProcessFlowServiceProxy.getNodeService().getNode(factoryName, lotInfo.getProcessFlowName(), lotInfo.getProcessFlowVersion(), constantMap.Node_ProcessOperation , processOperationName, processOperationVersion);
		}
		else //Next Node
		{
			
				ProcessFlowKey processFlowKey = new ProcessFlowKey();

				processFlowKey.setFactoryName(lotInfo.getFactoryName());
				processFlowKey.setProcessFlowName(lotInfo.getProcessFlowName());
				processFlowKey.setProcessFlowVersion(lotInfo.getProcessFlowVersion());

				ProcessFlow processFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
				
				kr.co.aim.greentrack.processflow.management.iter.NodeStack nodeStack = NodeStackUtil.stringToNodeStack(lotInfo.getNodeStack());
				ProcessFlowIterator pfi = new ProcessFlowIterator(processFlow, nodeStack, "");
								
				PFIValueSetter valueSetter = new LotPFIValueSetter(pfi, oldLotInfo, lotInfo );
				pfi.moveNext("N", valueSetter);

				nextNode = pfi.getCurrentNodeData();
			
		}
		
		for (Map<String, Object> lotData : lotList) {
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.get("LOTNAME").toString());
			Lot oldLot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.get("LOTNAME").toString());
			
			boolean reserveCheck = MESLotServiceProxy.getLotServiceUtil().PostCellDeleteLotFutureAction(futureActionEventInfo, lot, nextNode.getNodeAttribute1(), nextNode.getNodeAttribute2());
			
			if(reserveCheck)
			{
				reserveFlag = true;
			}
			
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
		MESLotServiceProxy.getLotServiceUtil().updateBatch(queryStringLot, updateLotArgList);
		try 
		{
			CommonUtil.executeBatch("insert", updateLotHistoryList);
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}
		
		IPQCLot fqcLot = ExtendedObjectProxy.getIPQCLotService().selectByKey(false, new Object[] { fqcLotName, Integer.parseInt(seq) });
		
		fqcLot.setLotState(constantMap.Lot_Completed);
		fqcLot.setLastEventComment(eventInfo.getEventComment());
		fqcLot.setLastEventName(eventInfo.getEventName());
		fqcLot.setLastEventTime(eventInfo.getEventTime());
		fqcLot.setLastEventTimeKey(eventInfo.getEventTimeKey());
		fqcLot.setLastEventUser(eventInfo.getEventUser());
		
		ExtendedObjectProxy.getIPQCLotService().modify(eventInfo, fqcLot);
		
		if(reserveFlag)
		{
			EventInfo eventInfoHold = EventInfoUtil.makeEventInfo("ReserveHold", getEventUser(), getEventComment(), null, null);
			eventInfoHold.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoHold.setEventTime(TimeStampUtil.getCurrentTimestamp());
			
			MESLotServiceProxy.getLotServiceUtil().PanelHoldByTrayGroup(eventInfoHold, fqcLotName);
		}
		
		return doc;
	}
	
	private List<Map<String, Object>> getCellTestOperation(String factoryname, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion)
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
				"       AND PS.DETAILPROCESSOPERATIONTYPE IN ('CT', 'CellTest') " ;

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryname);
		args.put("PRODUCTSPECNAME", productSpecName);
		args.put("PRODUCTSPECVERSION", productSpecVersion);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
		
		return result;
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
