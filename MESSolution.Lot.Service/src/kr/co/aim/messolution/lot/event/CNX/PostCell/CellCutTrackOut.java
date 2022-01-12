package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.policy.util.LotPFIValueSetter;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.iter.NodeStackUtil;
import kr.co.aim.greentrack.processflow.management.iter.PFIValueSetter;
import kr.co.aim.greentrack.processflow.management.iter.ProcessFlowIterator;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class CellCutTrackOut extends SyncHandler {

	private static Log log = LogFactory.getLog(CellCutTrackOut.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		// Get Doc
		String durableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);

		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		// EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOut", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		EventInfo futureActionEventInfo = (EventInfo) ObjectUtil.copyTo(eventInfo);
		futureActionEventInfo.setEventName("Delete");

		// Make Panel
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		Lot lotInfo = MESLotServiceProxy.getLotServiceUtil().getLotData(lotList.get(0).getChildText("LOTNAME"));
		Lot oldLotInfo = (Lot) ObjectUtil.copyTo(lotInfo);

		String lotGradeFind = lotList.get(0).getChildText("LOTGRADE");
		lotInfo.setLotGrade(lotGradeFind);

		ProcessFlow processFlowData = CommonUtil.getProcessFlowData(lotInfo.getFactoryName(), lotInfo.getProcessFlowName(), lotInfo.getProcessFlowVersion());

		kr.co.aim.greentrack.processflow.management.iter.NodeStack nodeStack = NodeStackUtil.stringToNodeStack(lotInfo.getNodeStack());
		ProcessFlowIterator pfi = new ProcessFlowIterator(processFlowData, nodeStack, "");

		// MovePOFlag aMovePOFlag = MovePOFlag.MP_MoveNext;
		PFIValueSetter valueSetter = new LotPFIValueSetter(pfi, oldLotInfo, lotInfo );
		pfi.moveNext("N", valueSetter);

		// 1.3. Set ProcessFlow Iterator Related Data
		Node nextNode = pfi.getCurrentNodeData();

		boolean reserveFlag = false;

		for (Element lotData : lotList)
		{
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
			Lot oldLot = (Lot) ObjectUtil.copyTo(lot);

			String position = lotData.getChildText("POSITION");
			String lotGrade = lotData.getChildText("LOTGRADE");
			String reasonCode = lotData.getChildText("REASONCODE");
			String reasonCodeType = lotData.getChildText("REASONCODETYPE");

			CommonValidation.checkLotProcessStateRun(lot);

			boolean reserveCheck = MESLotServiceProxy.getLotServiceUtil().PostCellDeleteLotFutureAction(futureActionEventInfo, lot, nextNode.getNodeAttribute1(), nextNode.getNodeAttribute2());

			if (reserveCheck)
				reserveFlag = true;

			List<Object> lotBindList = new ArrayList<Object>();
			lotBindList.add(constantMap.Lot_Released);
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
			lotBindList.add(lotGrade);
			lotBindList.add(reasonCode);
			lotBindList.add(reasonCodeType);
			lotBindList.add(portName);
			lotBindList.add(portType);
			lotBindList.add(portUseType);
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
			lot.setLotGrade(lotGrade);
			lot.setReasonCode(reasonCode);
			lot.setReasonCodeType(reasonCodeType);
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
			lotUdf.put("PORTNAME", portName);
			lotUdf.put("PORTTYPE", portType);
			lotUdf.put("PORTUSETYPE", portUseType);
			lot.setUdfs(lotUdf);

			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
			updateLotHistoryList.add(lotHistory);
		}

		updateLotData(eventInfo, updateLotArgList, updateLotHistoryList);

		// Durable
		Durable durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		Durable olddurableInfo = (Durable) ObjectUtil.copyTo(durableInfo);

		CommonValidation.checkAvailableCst(durableInfo);

		durableInfo.setLotQuantity(lotList.size());
		durableInfo.setDurableState(constantMap.Dur_InUse);
		durableInfo.setLastEventName(eventInfo.getEventName());
		durableInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
		durableInfo.setLastEventTime(eventInfo.getEventTime());
		durableInfo.setLastEventUser(eventInfo.getEventUser());
		durableInfo.setLastEventComment(eventInfo.getEventComment());

		DurableHistory durHistory = new DurableHistory();
		durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfo, durableInfo, durHistory);

		DurableServiceProxy.getDurableService().update(durableInfo);
		DurableServiceProxy.getDurableHistoryService().insert(durHistory);

		if (reserveFlag)
		{
			EventInfo eventInfoHold = EventInfoUtil.makeEventInfo("ReserveHold", getEventUser(), getEventComment(), null, null);
			eventInfoHold.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoHold.setEventTime(TimeStampUtil.getCurrentTimestamp());

			MESLotServiceProxy.getLotServiceUtil().PanelHoldByTray(eventInfoHold, durableName);
		}

		return doc;
	}

	private void updateLotData(EventInfo eventInfo, List<Object[]> updateLotArgList, List<LotHistory> updateLotHistoryList) throws CustomException
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
		sql.append("       CARRIERNAME = ?, ");
		sql.append("       LOTGRADE = ?, ");
		sql.append("       REASONCODE = ?, ");
		sql.append("       REASONCODETYPE = ?, ");
		sql.append("       PORTNAME = ?, ");
		sql.append("       PORTTYPE = ?, ");
		sql.append("       PORTUSETYPE = ? ");
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
}
