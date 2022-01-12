package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

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
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;

public class ChangeOperation extends SyncHandler {

	private static Log log = LogFactory.getLog(ChangeOperation.class);
	
	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		//Get Doc
		Element body = SMessageUtil.getBodyElement(doc);
		List<String> lotNameList = CommonUtil.makeList(body, "LOTLIST", "LOTNAME");
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", false);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);
		
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		// Validation
		CommonValidation.checkSameLotState(lotNameList, constantMap.Lot_Released);
		CommonValidation.checkSameLotProcessState(lotNameList, constantMap.Lot_LoggedOut);
		CommonValidation.checkLotHoldState(lotNameList);
		CommonValidation.checkDistinctProcessOperation(lotNameList);
		
		//EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeOper", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey( TimeUtils.getCurrentEventTimeKey());
			eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		EventInfo futureActionEventInfo = (EventInfo)ObjectUtil.copyTo(eventInfo);
		futureActionEventInfo.setEventName("Delete");
		
		//SQL
		String queryStringLot = "UPDATE LOT SET PROCESSOPERATIONNAME = ?, PROCESSOPERATIONVERSION = ?, NODESTACK = ? ,LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
				+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LASTEVENTFLAG = ?, FIRSTGLASSFLAG = null WHERE LOTNAME = ?";	
		
		//Make Panel
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
		List<Lot> reserveHoldLotList = new ArrayList<Lot>();
		
		for (Element lotData : lotList)
		{	
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);
			
			if (!lot.getProcessGroupName().equals(""))
			{
				throw new CustomException("PROCESSGROUP-0002", lot.getKey().getLotName());
			}
			
			CommonValidation.checkJobDownFlag(lot);
			
			Node changeNode = ProcessFlowServiceProxy.getNodeService().getNode(lot.getFactoryName(), lot.getProcessFlowName(), lot.getProcessFlowVersion(), constantMap.Node_ProcessOperation, processOperationName, processOperationVersion);
			
			boolean reserveFlag = MESLotServiceProxy.getLotServiceUtil().PostCellDeleteLotFutureAction(futureActionEventInfo, lot, processOperationName, processOperationVersion);
			
			if(reserveFlag)
			{
				reserveHoldLotList.add(lot);
			}
			
			List<Object> lotBindList = new ArrayList<Object>();
			
			lotBindList.add(processOperationName);
			lotBindList.add(processOperationVersion);
			lotBindList.add(changeNode.getKey().getNodeId());
			lotBindList.add(eventInfo.getEventName());
			lotBindList.add(eventInfo.getEventTimeKey());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventComment());
			lotBindList.add(constantMap.Flag_N);
			lotBindList.add(lot.getKey().getLotName());
			
			updateLotArgList.add(lotBindList.toArray());
			
			//History	
			lot.setProcessOperationName(processOperationName);
			lot.setProcessOperationVersion(processOperationVersion);
			lot.setNodeStack(changeNode.getKey().getNodeId());
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());
			
			Map<String, String> lotUdfs = lot.getUdfs();
			lotUdfs.put("BEFOREOPERATIONNAME", lot.getProcessOperationName());
			lotUdfs.put("BEFOREOPERATIONVER", lot.getProcessOperationVersion());
			lotUdfs.put("FIRSTGLASSFLAG", "");
			lot.setUdfs(lotUdfs);
			
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
		
		if(reserveHoldLotList.size() > 0)
		{
			EventInfo eventInfoHold = EventInfoUtil.makeEventInfo("ReserveHold", getEventUser(), getEventComment(), null, null);
			eventInfoHold.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoHold.setEventTime(TimeStampUtil.getCurrentTimestamp());
			
			MESLotServiceProxy.getLotServiceUtil().PanelHoldByPanelList(eventInfoHold, reserveHoldLotList);
		}
		
		return doc;
	}
}
