package kr.co.aim.messolution.lot.event.CNX.PostCell;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class WorkFlowCancelReserveMachine extends SyncHandler {
	public static Log logger = LogFactory.getLog(WorkFlowCancelReserveMachine.class);
	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		List<Element> WorkFlowList = SMessageUtil.getBodySequenceItemList(doc, "WorkFlowList", false);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("WorkFlowCancelReserveMachine", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		eventInfo.setEventTime(TimeUtils.getCurrentTimestamp());
		List<Object[]> updateFutureActionArgList = new ArrayList<Object[]>();
		for (Element WorkFlow  : WorkFlowList)
		{
			String workFlowID = SMessageUtil.getChildText(WorkFlow, "WORKFLOWID", true);
			String workFlowType = SMessageUtil.getChildText(WorkFlow, "WORKFLOWTYPE", true);
			String testItem= SMessageUtil.getChildText(WorkFlow, "TESTITEM", true);
			String testMachineName = SMessageUtil.getChildText(WorkFlow, "TESTMACHINENAME", true);
			String testMachineType = SMessageUtil.getChildText(WorkFlow, "TESTMACHINETYPE", true);
			checkWorkFlowdate(workFlowID,workFlowType,testItem,testMachineName,testMachineType);
			List<Object> WorkFlowItemList = new ArrayList<Object>();
			WorkFlowItemList.add(workFlowID);
			WorkFlowItemList.add(workFlowType);
			WorkFlowItemList.add(testMachineType);
			WorkFlowItemList.add(testMachineName);
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
	private void checkWorkFlowdate(String workFlowID, String workFlowType, String testItem, String testMachineName,
			String testMachineType) throws CustomException{
		String sql= "SELECT * FROM CT_WORKFLOWMACHINE "
				+ "WHERE WORKFLOWID = :WORKFLOWID "
				+ "AND WORKFLOWTYPE = :WORKFLOWTYPE "
				+ "AND TESTMACHINETYPE = :TESTMACHINETYPE "
				+ "AND TESTITEM = :TESTITEM "
				+ "AND TESTMACHINENAME =:TESTMACHINENAME";
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("WORKFLOWID",workFlowID);
		bindMap.put("WORKFLOWTYPE",workFlowType);
		bindMap.put("TESTMACHINETYPE",testMachineType);
		bindMap.put("TESTMACHINENAME",testMachineName);
		bindMap.put("TESTITEM",testItem);
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		if(result==null)
		{
			throw new CustomException("DURABLE-0010", testMachineName);
		}
		
	}
	private void insertLotFutureActionData(List<Object[]> updateFutureActionArgList) throws CustomException
	{
		StringBuffer sqlFA = new StringBuffer();
		sqlFA.append(" DELETE CT_WORKFLOWMACHINE  ");
		sqlFA.append(" WHERE WORKFLOWID=? AND WORKFLOWTYPE=? AND TESTMACHINETYPE=? AND TESTMACHINENAME=? AND TESTITEM=?");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlFA.toString(), updateFutureActionArgList);
		
	}
	
}
