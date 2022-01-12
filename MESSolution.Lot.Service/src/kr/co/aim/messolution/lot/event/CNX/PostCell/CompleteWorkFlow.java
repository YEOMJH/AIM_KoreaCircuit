package kr.co.aim.messolution.lot.event.CNX.PostCell;

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
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;

public class CompleteWorkFlow extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		List<Element> WorkFlowList = SMessageUtil.getBodySequenceItemList(doc, "WORKFLOWLIST", false);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CompleteWorkFlow", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		eventInfo.setEventTime(TimeUtils.getCurrentTimestamp());
		List<Object[]> updateFutureActionArgList = new ArrayList<Object[]>();
		for (Element WorkFlow  : WorkFlowList)
		{
			String workFlowID = SMessageUtil.getChildText(WorkFlow, "WORKFLOWID", true);
			String workFlowType = SMessageUtil.getChildText(WorkFlow, "WORKFLOWTYPE", true);
			String testItem= SMessageUtil.getChildText(WorkFlow, "TESTITEM", true);	
			String testMachineType =SMessageUtil.getChildText(WorkFlow, "TESTMACHINETYPE", false);
			String testMachineName =SMessageUtil.getChildText(WorkFlow, "TESTMACHINENAME", true);
			checkWorkFlowdate(workFlowID,workFlowType,testItem);
			updateWorkFlowMachineList(workFlowID,workFlowType,testItem,testMachineType,testMachineName);
			String sql= "SELECT *FROM CT_WORKFLOWMACHINE "
						+ "WHERE WORKFLOWID = :WORKFLOWID "
						+ "AND WORKFLOWTYPE = :WORKFLOWTYPE "
						+ "AND TESTITEM = :TESTITEM "
						+ "AND TESTITEMSTATE ='Started' ";

				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("WORKFLOWID",workFlowID);
				bindMap.put("WORKFLOWTYPE",workFlowType);
				bindMap.put("TESTITEM",testItem);
				List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
				if(result.size()==0)
				{
					List<Object> WorkFlowItemList = new ArrayList<Object>();
					WorkFlowItemList.add(eventInfo.getEventName());
					WorkFlowItemList.add(eventInfo.getEventUser());
					WorkFlowItemList.add(eventInfo.getEventComment());
					WorkFlowItemList.add(eventInfo.getEventTime());
					WorkFlowItemList.add(eventInfo.getEventTimeKey());
					WorkFlowItemList.add("Completed");
					WorkFlowItemList.add(eventInfo.getEventTime());
					WorkFlowItemList.add(workFlowID);
					WorkFlowItemList.add(workFlowType);
					WorkFlowItemList.add(testItem);
					updateFutureActionArgList.add(WorkFlowItemList.toArray());
					updateLotFutureActionData(updateFutureActionArgList);
				}
		}
		return doc;
	}
	private void changeMachineState(String machineName) throws CustomException  {
		try
		{
			Machine machineData  = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			String oldMachineState = machineData.getMachineStateName();
			Map<String, String> machineUdfs = new HashMap<String, String>();
			if(oldMachineState.equalsIgnoreCase("RUN"))
			{
				String machineStateName="IDLE";//IDLE
				machineUdfs.put("LASTIDLETIME", "");
				machineUdfs.put("MACHINESUBSTATE", "IDLE");
				String sReasonCodeType ="WorkFlowItemComplete" ;
			    String sReasonCode="IDLE";
			    EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeState", getEventUser(), getEventComment(), sReasonCodeType, sReasonCode);
			    
			    eventInfo.setReasonCode(sReasonCode);

				MakeMachineStateByStateInfo transitionInfo = MESMachineServiceProxy.getMachineInfoUtil().makeMachineStateByStateInfo(machineData, machineStateName);
				transitionInfo.setUdfs(machineUdfs);
				MESMachineServiceProxy.getMachineServiceImpl().makeMachineStateByState(machineData, transitionInfo, eventInfo);
			}
		
		}
		catch (CustomException fe)
		{
			throw new CustomException("", fe.getMessage());
		}
		
	}
	private void checkWorkFlowdate(String workFlowID, String workFlowType, String testItem) throws CustomException{
		String sql= "SELECT * FROM CT_WORKFLOWITEM "
				+ "WHERE WORKFLOWID = :WORKFLOWID "
				+ "AND WORKFLOWTYPE = :WORKFLOWTYPE "
				+ "AND TESTITEM = :TESTITEM ";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("WORKFLOWID",workFlowID);
		bindMap.put("WORKFLOWTYPE",workFlowType);
		bindMap.put("TESTITEM",testItem);
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		String state = result.get(0).get("TESTITEMSTATE").toString();
		if(!state.equals("Started"))
		{
			throw new CustomException("TEST-0011", workFlowID);
		}
		
	}
	private void updateLotFutureActionData(List<Object[]> updateFutureActionArgList) throws CustomException
	{
		StringBuffer sqlFA = new StringBuffer();
		sqlFA.append(" UPDATE CT_WORKFLOWITEM  ");
		sqlFA.append(" SET LASTEVENTNAME=?,LASTEVENTUSER=?,LASTEVENTCOMMENT=?,LASTEVENTTIME=?,");
		sqlFA.append(" LASTEVENTTIMEKEY=? ,TESTITEMSTATE=?,TESTENDTIME=? ");
		sqlFA.append(" WHERE WORKFLOWID=? AND WORKFLOWTYPE=? AND TESTITEM=? ");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlFA.toString(), updateFutureActionArgList);
		
	}
	private void updateWorkFlowMachineList(String workFlowID, String workFlowType, String testItem,String testMachineType,String testMachineName  ) throws CustomException
	{
		try
		{
		List<Object> WorkFlowItemList = new ArrayList<Object>();
		List<Object[]> updateWorkFlowItemListList = new ArrayList<Object[]>();
		WorkFlowItemList.add("Completed");
		WorkFlowItemList.add(workFlowID);
		WorkFlowItemList.add(workFlowType);
		WorkFlowItemList.add(testItem);
		WorkFlowItemList.add(testMachineType);
		WorkFlowItemList.add(testMachineName);
		updateWorkFlowItemListList.add(WorkFlowItemList.toArray());
		StringBuffer sqlFMachine = new StringBuffer();
		sqlFMachine.append(" UPDATE CT_WORKFLOWMACHINE  ");
		sqlFMachine.append(" SET TESTITEMSTATE=?");
		sqlFMachine.append(" WHERE  WORKFLOWID=? AND WORKFLOWTYPE=? AND TESTITEM=? AND TESTMACHINETYPE=? AND TESTMACHINENAME=? ");
		
		StringBuffer sqlFMaterial = new StringBuffer();
		sqlFMaterial.append(" UPDATE CT_WORKFLOWMATERIAL  ");
		sqlFMaterial.append(" SET TESTITEMSTATE=?");
		sqlFMaterial.append(" WHERE  WORKFLOWID=? AND WORKFLOWTYPE=? AND TESTITEM=? AND TESTMACHINETYPE=? AND TESTMACHINENAME=? ");
       
        MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlFMachine.toString(), updateWorkFlowItemListList);
        MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlFMaterial.toString(), updateWorkFlowItemListList);
	    String sqlM= "SELECT * FROM CT_WORKFLOWMACHINE "
					+ "WHERE TESTMACHINENAME = :TESTMACHINENAME "
					+ "AND TESTITEMSTATE ='Started' ";
	    Map<String, Object> bindM = new HashMap<String, Object>();
		bindM.put("TESTMACHINENAME",testMachineName);
	    List<Map<String, Object>> resultM = GenericServiceProxy.getSqlMesTemplate().queryForList(sqlM, bindM);
		if(resultM.size()==0)
		{	 
		  changeMachineState(testMachineName);
		  
		}
	  }
	  catch(CustomException fe)
	  {
		throw new CustomException("", fe.getMessage());
	  }
		
	}
	
}
