package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupHistory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class OuterUnpacking extends SyncHandler {

	private static Log log = LogFactory.getLog(OuterUnpacking.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String outerBoxName = SMessageUtil.getBodyItemValue(doc, "PROCESSGROUPNAME", true);

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Unpacking", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		String condition = " WHERE SUPERPROCESSGROUPNAME = ? ";
		Object[] bindSet = new Object[] { outerBoxName };

		List<ProcessGroup> innerBoxList = ProcessGroupServiceProxy.getProcessGroupService().select(condition, bindSet);

		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		for (ProcessGroup boxData : innerBoxList)
		{
			List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotListByProcessGroup(boxData.getKey().getProcessGroupName());

			for (Lot lot : lotList)
			{
				Lot oldLot = (Lot) ObjectUtil.copyTo(lot);

				List<Map<String, Object>> operationData = MESLotServiceProxy.getLotServiceUtil().getOperationByDetailOperType(lot.getFactoryName(), lot.getProductSpecName(),
						lot.getProductSpecVersion(), lot.getProcessFlowName(), lot.getProcessFlowVersion(), "OuterPack");

				Node nextNode = ProcessFlowServiceProxy.getNodeService().getNode(oldLot.getFactoryName(), oldLot.getProcessFlowName(), oldLot.getProcessFlowVersion(),
						constantMap.Node_ProcessOperation, operationData.get(0).get("PROCESSOPERATIONNAME").toString(), operationData.get(0).get("PROCESSOPERATIONVERSION").toString());

				List<Object> lotBindList = new ArrayList<Object>();

				lotBindList.add(constantMap.Lot_Released);
				lotBindList.add(constantMap.Lot_Wait);
				lotBindList.add(eventInfo.getEventName());
				lotBindList.add(eventInfo.getEventTimeKey());
				lotBindList.add(eventInfo.getEventTime());
				lotBindList.add(eventInfo.getEventUser());
				lotBindList.add(eventInfo.getEventComment());
				lotBindList.add(constantMap.Flag_N);
				lotBindList.add(nextNode.getNodeAttribute1());
				lotBindList.add(nextNode.getNodeAttribute2());
				lotBindList.add(lot.getProcessOperationName());
				lotBindList.add(lot.getProcessOperationVersion());
				lotBindList.add(lot.getProcessFlowName());
				lotBindList.add(nextNode.getKey().getNodeId());
				lotBindList.add(lot.getKey().getLotName());

				updateLotArgList.add(lotBindList.toArray());

				// History
				lot.setLotState(constantMap.Lot_Released);
				lot.setLotProcessState(constantMap.Lot_Wait);
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
				lotUdf.put("BEFOREOPERATIONNAME", oldLot.getProcessOperationName());
				lotUdf.put("BEFOREOPERATIONVER", oldLot.getProcessOperationVersion());
				lotUdf.put("BEFOREFLOWNAME", oldLot.getProcessFlowName());
				lot.setUdfs(lotUdf);

				LotHistory lotHistory = new LotHistory();
				lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
				
				updateLotHistoryList.add(lotHistory);
			}

			ProcessGroup processGroup = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(boxData.getKey().getProcessGroupName());
			ProcessGroup oldProcessGroup = (ProcessGroup) ObjectUtil.copyTo(processGroup);

			processGroup.setSuperProcessGroupName("");
			processGroup.setLastEventName(eventInfo.getEventName());
			processGroup.setLastEventTimeKey(eventInfo.getEventTimeKey());
			processGroup.setLastEventTime(eventInfo.getEventTime());
			processGroup.setLastEventUser(eventInfo.getEventUser());
			processGroup.setLastEventComment(eventInfo.getEventComment());
			processGroup.setLastEventFlag("N");

			ProcessGroupHistory processGroupHistory = new ProcessGroupHistory();
			processGroupHistory = ProcessGroupServiceProxy.getProcessGroupHistoryDataAdaptor().setHV(oldProcessGroup, processGroup, processGroupHistory);

			ProcessGroupServiceProxy.getProcessGroupService().update(processGroup);
			ProcessGroupServiceProxy.getProcessGroupHistoryService().insert(processGroupHistory);
		}

		updateLotData(eventInfo, updateLotArgList, updateLotHistoryList);

		ProcessGroup processGroup = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(outerBoxName);
		ProcessGroup oldProcessGroup = (ProcessGroup) ObjectUtil.copyTo(processGroup);

		processGroup.setMaterialQuantity(0);
		processGroup.setLastEventName(eventInfo.getEventName());
		processGroup.setLastEventTimeKey(eventInfo.getEventTimeKey());
		processGroup.setLastEventTime(eventInfo.getEventTime());
		processGroup.setLastEventUser(eventInfo.getEventUser());
		processGroup.setLastEventComment(eventInfo.getEventComment());
		processGroup.setLastEventFlag("N");

		ProcessGroupHistory processGroupHistory = new ProcessGroupHistory();
		processGroupHistory = ProcessGroupServiceProxy.getProcessGroupHistoryDataAdaptor().setHV(oldProcessGroup, processGroup, processGroupHistory);

		ProcessGroupServiceProxy.getProcessGroupService().delete(processGroup.getKey());
		ProcessGroupServiceProxy.getProcessGroupHistoryService().insert(processGroupHistory);

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
		sql.append("       PROCESSOPERATIONNAME = ?, ");
		sql.append("       PROCESSOPERATIONVERSION = ?, ");
		sql.append("       BEFOREOPERATIONNAME = ?, ");
		sql.append("       BEFOREOPERATIONVER = ?, ");
		sql.append("       BEFOREFLOWNAME = ?, ");
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
}
