package kr.co.aim.messolution.lot.event.CNX.PostCell;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class WorkFlowReserveMachine extends SyncHandler {
	public static Log logger = LogFactory.getLog(WorkFlowReserveMachine.class);
	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		List<Element> WorkFlowList = SMessageUtil.getBodySequenceItemList(doc, "WorkFlowList", false);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("WorkFlowReserveMachine", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		eventInfo.setEventTime(TimeUtils.getCurrentTimestamp());
		List<Object[]> updateFutureActionArgList = new ArrayList<Object[]>();
		for (Element WorkFlow  : WorkFlowList)
		{
			String workFlowID = SMessageUtil.getChildText(WorkFlow, "WORKFLOWID", true);
			String workFlowType = SMessageUtil.getChildText(WorkFlow, "WORKFLOWTYPE", true);
			String testItem= SMessageUtil.getChildText(WorkFlow, "TESTITEM", true);
			String testMachineType = SMessageUtil.getChildText(WorkFlow, "TESTMACHINETYPE", false);
			String testMachineName = SMessageUtil.getChildText(WorkFlow, "TESTMACHINENAME", true);
			String testUseID= SMessageUtil.getChildText(WorkFlow, "USENAME", false);
			
			List<Object> WorkFlowItemList = new ArrayList<Object>();
			WorkFlowItemList.add(workFlowID);
			WorkFlowItemList.add(testMachineName);
			WorkFlowItemList.add(testUseID);
			WorkFlowItemList.add(workFlowType);
			WorkFlowItemList.add(testMachineType);
			WorkFlowItemList.add(testItem);
			updateFutureActionArgList.add(WorkFlowItemList.toArray());
		}
		try
		{
		insertLotFutureActionData(updateFutureActionArgList);
		}
		catch(Exception e) 
		{
			
			logger.error(e.getMessage());
			throw new CustomException(e.getCause());
		}
		return doc;
	}
	private void insertLotFutureActionData(List<Object[]> updateFutureActionArgList) throws CustomException
	{
		
		StringBuffer sqlFM = new StringBuffer();
		sqlFM.append(" INSERT INTO CT_WORKFLOWMACHINE  ");
		sqlFM.append(" (WORKFLOWID,TESTMACHINENAME,TESTUSERNAME,WORKFLOWTYPE,TESTMACHINETYPE,TESTITEM)");
		sqlFM.append(" VALUES");
		sqlFM.append(" (?,?,?,?,?,?) ");

		
		MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlFM.toString(), updateFutureActionArgList);
		
	}
	
}
