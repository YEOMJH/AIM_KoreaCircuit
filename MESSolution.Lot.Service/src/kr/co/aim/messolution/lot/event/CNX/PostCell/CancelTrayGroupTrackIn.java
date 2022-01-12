package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FQCLot;
import kr.co.aim.messolution.extended.object.management.data.FQCPanelJudge;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.policy.util.LotPFIValueSetter;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processflow.management.iter.NodeStackUtil;
import kr.co.aim.greentrack.processflow.management.iter.PFIValueSetter;
import kr.co.aim.greentrack.processflow.management.iter.ProcessFlowIterator;
import kr.co.aim.greentrack.processoperationspec.ProcessOperationSpecServiceProxy;
import kr.co.aim.greentrack.processoperationspec.management.ProcessOperationSpecService;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpecKey;

public class CancelTrayGroupTrackIn extends SyncHandler {

	private static Log log = LogFactory.getLog(CancelTrayGroupTrackIn.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String trayGroupName = SMessageUtil.getBodyItemValue(doc, "TRAYGROUPNAME", true);

		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		// EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelTrackIn", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		EventInfo futureActionEventInfo = (EventInfo)ObjectUtil.copyTo(eventInfo);
		futureActionEventInfo.setEventName("Delete");
		
		// SQL
		String queryStringLot = "UPDATE LOT SET LOTPROCESSSTATE = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
				+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ? WHERE LOTNAME = ?";	
		
		List<Map<String, Object>> lotList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayGroup(trayGroupName);

		List<Lot> lotDataList = getLotDataList(lotList);
		
		Lot firstPanel = lotDataList.get(0);
		ProcessOperationSpec operationSpec = ProcessOperationSpecServiceProxy.getProcessOperationSpecService().selectByKey(new ProcessOperationSpecKey(firstPanel.getFactoryName(), firstPanel.getProcessOperationName(), firstPanel.getProcessOperationVersion()));
		
		if(StringUtil.equals(operationSpec.getDetailProcessOperationType(), "FQC"))
		{
			try
			{
				String seq = ExtendedObjectProxy.getFQCLotService().getCurrentFQCLotSeq(trayGroupName);
				FQCLot fqcLot = ExtendedObjectProxy.getFQCLotService().selectByKey(false, new Object[]{trayGroupName, seq});
				
				String condition = " FQCLOTNAME = ? AND SEQ = ? ";
				Object[] bindSet = new Object[] { trayGroupName, seq };
				
				List<FQCPanelJudge> fqcPanelJudgeList = ExtendedObjectProxy.getFQCPanelJudgeService().select(condition, bindSet);
				ExtendedObjectProxy.getFQCPanelJudgeService().remove(eventInfo, fqcPanelJudgeList);
				ExtendedObjectProxy.getFQCLotService().remove(eventInfo, fqcLot);
			}
			catch (Exception e)
			{
				throw new CustomException("FQC-0001", trayGroupName);
			}
		}
		
		for (Lot lot : lotDataList)
		{
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);
			
			Machine eqpData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(lot.getMachineName());
			CommonValidation.ChekcMachinState(eqpData);
			CommonValidation.checkMachineHold(eqpData);
			CommonValidation.checkLotProcessStateRun(lot);
			
			List<Object> lotBindList = new ArrayList<Object>();
			
			lotBindList.add(constantMap.Lot_Wait);
			lotBindList.add(eventInfo.getEventName());
			lotBindList.add(eventInfo.getEventTimeKey());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventComment());
			lotBindList.add(lot.getKey().getLotName());
			
			updateLotArgList.add(lotBindList.toArray());
			
			//History	
			lot.setLotProcessState(constantMap.Lot_Wait);
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());
			
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

		return doc;
	}
	
	private List<Lot> getLotDataList(List<Map<String, Object>> lotList) throws CustomException
	{
		String condition = "WHERE LOTNAME IN(";
		for (Map<String, Object> lotMap : lotList) 
		{
			String lotName = lotMap.get("LOTNAME").toString();
			
			condition += "'" + lotName + "',";
		}
		condition = condition.substring(0, condition.length() - 1) + ")";
		
		List<Lot> lotDataList = LotServiceProxy.getLotService().select(condition, new Object[] { });
		
		Lot groupLotData = lotDataList.get(0);
		
		for (Lot lotData : lotDataList) 
		{
			if (!lotData.getFactoryName().equals(groupLotData.getFactoryName()) ||
				!lotData.getProductSpecName().equals(groupLotData.getProductSpecName()) ||
				!lotData.getProductSpecVersion().equals(groupLotData.getProductSpecVersion()) ||
				!lotData.getProcessFlowName().equals(groupLotData.getProcessFlowName()) ||
				!lotData.getProcessFlowVersion().equals(groupLotData.getProcessFlowVersion()) ||
				!lotData.getProcessOperationName().equals(groupLotData.getProcessOperationName()) ||
				!lotData.getProcessOperationVersion().equals(groupLotData.getProcessOperationVersion()) ||
				!lotData.getProductionType().equals(groupLotData.getProductionType()) ||
				!lotData.getProductRequestName().equals(groupLotData.getProductRequestName()) ||
				!lotData.getUdfs().get("LOTDETAILGRADE").equals(groupLotData.getUdfs().get("LOTDETAILGRADE")))
			{
				throw new CustomException("LOT-0020", lotData.getKey().getLotName(), groupLotData.getKey().getLotName());
			}
		}
		
		return lotDataList;
	}
}
