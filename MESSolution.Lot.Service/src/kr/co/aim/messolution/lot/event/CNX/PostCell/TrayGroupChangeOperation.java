package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

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
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;


public class TrayGroupChangeOperation extends SyncHandler {

	public static Log logger = LogFactory.getLog(TrayGroupChangeOperation.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{	
		String trayGroupName = SMessageUtil.getBodyItemValue(doc, "TRAYGROUPNAME", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVer = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeOperation", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		EventInfo futureActionEventInfo = (EventInfo)ObjectUtil.copyTo(eventInfo);
		futureActionEventInfo.setEventName("Delete");
		
		boolean reserveFlag = false;
		
		List<Map<String, Object>> lotList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayGroup(trayGroupName);
		
		String queryStringLot = "UPDATE LOT SET PROCESSOPERATIONNAME = ?, PROCESSOPERATIONVERSION = ?, NODESTACK = ? ,BEFOREOPERATIONNAME = ?, BEFOREOPERATIONVER = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
				+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, FIRSTGLASSFLAG = null WHERE LOTNAME = ?";
		
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
		
		Node changeNode = ProcessFlowServiceProxy.getNodeService().getNode(lotList.get(0).get("FACTORYNAME").toString(), lotList.get(0).get("PROCESSFLOWNAME").toString(), lotList.get(0).get("PROCESSFLOWVERSION").toString(), constantMap.Node_ProcessOperation, processOperationName, processOperationVer);
		
		//Lot
		for(Map<String, Object> lotMap : lotList) 
		{		
			String lotName = lotMap.get("LOTNAME").toString();
			
			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			Lot oldLot = (Lot)ObjectUtil.copyTo(lotData);		
			List<Object> lotBindList = new ArrayList<Object>();
			
			CommonValidation.checkLotHoldState(lotData);
			//CommonValidation.checkLotState(lotData);
			
			boolean reserveCheck = MESLotServiceProxy.getLotServiceUtil().PostCellDeleteLotFutureAction(futureActionEventInfo, lotData, processOperationName, processOperationVer);
			
			if(reserveCheck)
			{
				reserveFlag = true;
			}
			
			lotBindList.add(processOperationName);
			lotBindList.add(processOperationVer);
			lotBindList.add(changeNode.getKey().getNodeId());
			lotBindList.add(lotData.getProcessOperationName());
			lotBindList.add(lotData.getProcessOperationVersion());
			lotBindList.add(eventInfo.getEventName());
			lotBindList.add(eventInfo.getEventTimeKey());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventComment());
			lotBindList.add(lotData.getKey().getLotName());
			
			updateLotArgList.add(lotBindList.toArray());
			
			Map<String, String> udfs = lotData.getUdfs();
			udfs.put("BEFOREOPERATIONNAME", lotData.getProcessOperationName());
			udfs.put("BEFOREOPERATIONVER", lotData.getProcessOperationVersion());
			udfs.put("FIRSTGLASSFLAG", "");
			lotData.setUdfs(udfs);
			
			lotData.setProcessOperationName(processOperationName);
			lotData.setProcessOperationVersion(processOperationVer);
			lotData.setNodeStack(changeNode.getKey().getNodeId());
			lotData.setLastEventName(eventInfo.getEventName());
			lotData.setLastEventTime(eventInfo.getEventTime());
			lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lotData.setLastEventComment(eventInfo.getEventComment());
			lotData.setLastEventUser(eventInfo.getEventUser());
			
			//History	
			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, lotHistory);
			
			updateLotHistoryList.add(lotHistory);
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
		
		if(reserveFlag)
		{
			EventInfo eventInfoHold = EventInfoUtil.makeEventInfo("ReserveHold", getEventUser(), getEventComment(), null, null);
			eventInfoHold.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoHold.setEventTime(TimeStampUtil.getCurrentTimestamp());
			
			MESLotServiceProxy.getLotServiceUtil().PanelHoldByTrayGroup(eventInfoHold, trayGroupName);
		}
		
		return doc;
	}
}