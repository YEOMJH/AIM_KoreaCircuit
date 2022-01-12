package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.GenericServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;

public class ChangeWorkFlowState extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
			List<Element> WorkFlowMaterialList = SMessageUtil.getBodySequenceItemList(doc, "WorkFlowMaterialList", false);
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeWorkFlowState", getEventUser(), getEventComment(), "", "");
			List<Object[]> updateWorkFlowArgList = new ArrayList<Object[]>();
			String testMachineName=null;
			for (Element WorkFlow  : WorkFlowMaterialList)
			{
				eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
				eventInfo.setEventTime(TimeUtils.getCurrentTimestamp());
				String workFlowID = SMessageUtil.getChildText(WorkFlow, "WORKFLOWID", true);
				String workFlowType = SMessageUtil.getChildText(WorkFlow, "WORKFLOWTYPE", true);
				String testItem= SMessageUtil.getChildText(WorkFlow, "TESTITEM", true);
				String testMachineType = SMessageUtil.getChildText(WorkFlow, "TESTMACHINETYPE", true);
			    testMachineName = SMessageUtil.getChildText(WorkFlow, "TESTMACHINENAME", true);
				String materialName= SMessageUtil.getChildText(WorkFlow, "MATERIALNAME", false);
				String materialQuantity= SMessageUtil.getChildText(WorkFlow, "MATERIALQUANTITY", false);
				String newTestItemState="Started";
				String oldTestItemState="Created";
				
				List<Object> WorkFlowItemList = new ArrayList<Object>();
				WorkFlowItemList.add(testMachineType);
				WorkFlowItemList.add(testMachineName);
				WorkFlowItemList.add(eventInfo.getEventName());
				WorkFlowItemList.add(eventInfo.getEventUser());
				WorkFlowItemList.add(eventInfo.getEventComment());
				WorkFlowItemList.add(eventInfo.getEventTime());
				WorkFlowItemList.add(eventInfo.getEventTimeKey());
				WorkFlowItemList.add(newTestItemState);
				WorkFlowItemList.add(eventInfo.getEventTime());//Report xvyao kaishi shijian
				WorkFlowItemList.add(workFlowID);
				WorkFlowItemList.add(workFlowType);
				WorkFlowItemList.add(testItem);
				WorkFlowItemList.add(oldTestItemState);
				updateWorkFlowArgList.add(WorkFlowItemList.toArray());
				if(materialName!=null&&!materialName.isEmpty())
				{
					insertWorkFlowMaterial(workFlowID,workFlowType,testMachineType,testMachineName,testItem,newTestItemState,materialName,materialQuantity);
				}
				changeMachineState(testMachineName);
			}
			updateWorkFlowActionData(updateWorkFlowArgList);
			
		return doc;
	}
	private void insertWorkFlowMaterial(String workFlowID, String workFlowType, String testMachineType,
			String testMachineName, String testItem,String newTestItemState, String materialName, String materialQuantity)throws CustomException {
		try
		{
			String sql = "INSERT INTO CT_WORKFLOWMATERIAL "
					+ "(WORKFLOWID,WORKFLOWTYPE,TESTMACHINETYPE,TESTMACHINENAME,TESTITEM,TESTITEMSTATE,MATERIALNAME,MATERIALQUANTITY) "
					+ "VALUES (:WORKFLOWID,:WORKFLOWTYPE,:TESTMACHINETYPE,:TESTMACHINENAME,:TESTITEM,:TESTITEMSTATE,:MATERIALNAME,:MATERIALQUANTITY)  ";	
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("WORKFLOWID",workFlowID);
			bindMap.put("WORKFLOWTYPE",workFlowType);
			bindMap.put("TESTMACHINETYPE",testMachineType);
			bindMap.put("TESTMACHINENAME",testMachineName);
			bindMap.put("TESTITEM",testItem);
			bindMap.put("TESTITEMSTATE",newTestItemState);
			bindMap.put("MATERIALNAME",materialName);
			bindMap.put("MATERIALQUANTITY",materialQuantity);
			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
			
			List<Object> WorkFlowMachineList = new ArrayList<Object>();
			List<Object[]> insertWorkFlowMachineList = new ArrayList<Object[]>();
			WorkFlowMachineList.add(newTestItemState);
			WorkFlowMachineList.add(workFlowID);
			WorkFlowMachineList.add(testMachineName);
			WorkFlowMachineList.add(workFlowType);
			WorkFlowMachineList.add(testMachineType);
			WorkFlowMachineList.add(testItem);
			insertWorkFlowMachineList.add(WorkFlowMachineList.toArray());
			StringBuffer sqlFM = new StringBuffer();
			sqlFM.append(" UPDATE CT_WORKFLOWMACHINE  ");
			sqlFM.append(" SET TESTITEMSTATE=?  ");
			sqlFM.append(" WHERE WORKFLOWID=? AND TESTMACHINENAME=? AND WORKFLOWTYPE=? AND TESTMACHINETYPE=? AND TESTITEM=? ");
			MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlFM.toString(), insertWorkFlowMachineList);
			
			
			
		}
		catch (Exception e)
		{
			throw new CustomException(e.getCause());
		}
		
	}



	/*private void insertWorkFlowActionData(
			List<Object[]> insertWorkFlowMaterialArgList) throws CustomException {
		
		// TODO Auto-generated method stub
		try
		{
		StringBuffer sqlFA = new StringBuffer();
		sqlFA.append("INSERT INTO CT_WORKFLOWMATERIAL  ");
		sqlFA.append("(WORKFLOWID,WORKFLOWTYPE,TESTMACHINETYPE,TESTMACHINENAME,TESTITEM,MATERIALNAME,MATERIALQUANTITY) ");
		sqlFA.append(" VALUES(?,?,?,?,?,?,?)  ");
		MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlFA.toString(), insertWorkFlowMaterialArgList);
		}
		catch(CustomException fe)
		{
			throw new CustomException("", fe.getMessage());
		}
		
		
	}*/

	private void updateWorkFlowActionData(
			List<Object[]> updateWorkFlowArgList) throws CustomException {
		// TODO Auto-generated method stub
		try
		{
		StringBuffer sqlFA = new StringBuffer();
		sqlFA.append(" UPDATE CT_WORKFLOWITEM  ");
		sqlFA.append(" SET TESTMACHINETYPE=?,TESTMACHINENAME=?,");
		sqlFA.append(" LASTEVENTNAME=?,LASTEVENTUSER=?,LASTEVENTCOMMENT=?,LASTEVENTTIME=?,LASTEVENTTIMEKEY=?,TESTITEMSTATE=?,TESTSTARTTIME=?  ");
		sqlFA.append(" WHERE WORKFLOWID=? AND WORKFLOWTYPE=? AND TESTITEM=? AND TESTITEMSTATE=?");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlFA.toString(), updateWorkFlowArgList);
		}
		catch (CustomException fe)
		{
			throw new CustomException("", fe.getMessage());
		}
	}
	
	private void changeMachineState(String testMachineName) throws CustomException {
		// TODO Auto-generated method stub
		try
		{
			Machine machineData    = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(testMachineName);
			String oldMachineState = machineData.getMachineStateName();
			Map<String, String> machineUdfs = new HashMap<String, String>();
			if(oldMachineState.equalsIgnoreCase("IDLE"))
			{
				String machineStateName="RUN";
				machineUdfs.put("LASTIDLETIME", "");
				machineUdfs.put("MACHINESUBSTATE", "RUN");
				String sReasonCodeType ="WorkFlowStarted" ;
			    String sReasonCode="Run";
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

}
