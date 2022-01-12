package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.machine.management.info.MakeMachineStateByStateInfo;

public class ChangeWorkFlowInfo extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		// TODO Auto-generated method stub
		    String WorkFlow = SMessageUtil.getBodyItemValue(doc, "WORKFLOWID", true);
		    String testItem = SMessageUtil.getBodyItemValue(doc, "TESTITEM", true);
		    String materialName = SMessageUtil.getBodyItemValue(doc, "MATERIALNAME", true);
		    String machineName = SMessageUtil.getBodyItemValue(doc, "TESTMACHINENAME", true);
		    String MaterialQuantity = SMessageUtil.getBodyItemValue(doc, "MATERIALQUANTITY", true);
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeWorkFlowState", getEventUser(), getEventComment(), "", "");
			updateWorkFlowInfo(WorkFlow,testItem,materialName,MaterialQuantity,machineName);
		return doc;
	}

	private void insetHistory(String workFlow, String testItem, String materialName, String materialQuantity) throws CustomException{
		// TODO Auto-generated method stub
		
	}
	private void updateWorkFlowInfo(String workFlow, String testItem, String materialName, String materialQuantity,String machineName) throws CustomException{
		// TODO Auto-generated method stub
		try
		{
			String sql= "UPDATE CT_WORKFLOWMATERIAL  SET MATERIALQUANTITY = :MATERIALQUANTITY "
					+ "WHERE WORKFLOWID = :WORKFLOWID "
					+ "AND WORKFLOWTYPE = 'INT' "
					+ " AND TESTITEM = :TESTITEM "
					+ " AND MATERIALNAME = :MATERIALNAME "
					+ " AND TESTMACHINENAME = :TESTMACHINENAME ";
					
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("WORKFLOWID",workFlow);
			bindMap.put("TESTITEM",testItem);
			bindMap.put("MATERIALNAME",materialName);
			bindMap.put("TESTMACHINENAME",machineName);
			bindMap.put("MATERIALQUANTITY",materialQuantity);
			greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().update(sql, bindMap);
		}
		catch (Exception e)
		{
			eventLog.info("Can't Change MaterialQTY");
		}	
	}
}
