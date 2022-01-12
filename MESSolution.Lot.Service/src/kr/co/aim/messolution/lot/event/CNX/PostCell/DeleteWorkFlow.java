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
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class DeleteWorkFlow extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		List<Element> WorkFlowMaterialList = SMessageUtil.getBodySequenceItemList(doc, "WORKFLOWLIST", false);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("DeleteWorkFlow", getEventUser(), getEventComment(), "", "");
		List<Object[]> updateWorkFlow = new ArrayList<Object[]>();
		List<Object[]> updateWorkFlowItem = new ArrayList<Object[]>();
		for (Element WorkFlow  : WorkFlowMaterialList)
		{
			String workFlowID = SMessageUtil.getChildText(WorkFlow, "WORKFLOWID", true);
			String workFlowType = SMessageUtil.getChildText(WorkFlow, "WORKFLOWTYPE", true);
			String ItemQuantity= SMessageUtil.getChildText(WorkFlow, "ITEMQUANTITY", false);
			//workFlowItem&workFlow
			deleteWorkFlow(workFlowID,workFlowType);
			deleteWorkFlowItem(workFlowID,workFlowType,ItemQuantity);		
		}
	return doc;
	}

	private void deleteWorkFlowItem(String workFlowID, String workFlowType, String itemQuantity) throws CustomException {	
		String sql= "DELETE CT_WORKFLOWITEM WHERE WORKFLOWID = :WORKFLOWID AND WORKFLOWTYPE = :WORKFLOWTYPE AND ITEMQUANTITY = :ITEMQUANTITY";
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("WORKFLOWID",workFlowID);
		bindMap.put("WORKFLOWTYPE",workFlowType);
		bindMap.put("ITEMQUANTITY",itemQuantity);
		GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
	}
	private void deleteWorkFlow(String workFlowID, String workFlowType) throws CustomException{
		
		String sql= "DELETE CT_WORKFLOW WHERE WORKFLOWID = :WORKFLOWID AND WORKFLOWTYPE = :WORKFLOWTYPE ";
		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("WORKFLOWID",workFlowID);
		bindMap.put("WORKFLOWTYPE",workFlowType);
		GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
	}
}
