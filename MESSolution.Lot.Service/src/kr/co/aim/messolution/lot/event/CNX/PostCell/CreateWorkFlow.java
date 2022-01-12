package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.List;

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

public class CreateWorkFlow extends SyncHandler {
	@Override
	public Object doWorks(Document doc) throws CustomException 
	{
		List<Element> WorkFlowMaterialList = SMessageUtil.getBodySequenceItemList(doc, "WORKFLOWLIST", false);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateWorkFlow", getEventUser(), getEventComment(), "", "");
		List<Object[]> updateWorkFlow = new ArrayList<Object[]>();
		List<Object[]> updateWorkFlowItem = new ArrayList<Object[]>();
		for (Element WorkFlow  : WorkFlowMaterialList)
		{
			String workFlowID = SMessageUtil.getChildText(WorkFlow, "WORKFLOWID", true);
			String workFlowType = SMessageUtil.getChildText(WorkFlow, "WORKFLOWTYPE", true);
			String deparment= SMessageUtil.getChildText(WorkFlow, "DEPARMENT", true);
			String RequestTime = SMessageUtil.getChildText(WorkFlow, "REQUESTTIME", true);
			String workFlowLeve = SMessageUtil.getChildText(WorkFlow, "REQUESTTIME", true);
			String samplingType= SMessageUtil.getChildText(WorkFlow, "SAMPLINGTYPE", false);
			String testType = SMessageUtil.getChildText(WorkFlow, "TESTTYPE", true);
			String MachineType = SMessageUtil.getChildText(WorkFlow, "TESTMACHINETYPE", true);
			String testItem = SMessageUtil.getChildText(WorkFlow, "TESTITEM", true);
			String testEnginner=SMessageUtil.getChildText(WorkFlow, "TESTENGINNER", true);
			String itemQuantity= SMessageUtil.getChildText(WorkFlow, "ITEMQUANTITY", false);
			String ItemCutQuantity= SMessageUtil.getChildText(WorkFlow, "ITEMCUTQUANTITY", false);
			//workFlowItem&workFlow
			
			List<Object> conBindList = new ArrayList<Object>();
			conBindList.add(workFlowID);
			conBindList.add(workFlowType);
			conBindList.add(deparment);
			conBindList.add(TimeUtils.getTimestamp(RequestTime));
			conBindList.add(workFlowLeve);
			conBindList.add(testType);
			conBindList.add(samplingType);
			conBindList.add(testEnginner);
			conBindList.add("OIC创建");
			conBindList.add("Created");
			conBindList.add(eventInfo.getEventUser().toString());
			conBindList.add(TimeUtils.getCurrentEventTimeKey());
			conBindList.add(TimeUtils.getCurrentTimestamp());
			conBindList.add("CreateWorkFlowByExcel");
			updateWorkFlow.add(conBindList.toArray());
			
			List<Object> conItemList = new ArrayList<Object>();
			conItemList.add(workFlowID);
			conItemList.add(workFlowType);
			conItemList.add(MachineType);
			conItemList.add("");
			conItemList.add("Created");
			conItemList.add(testItem);
			conItemList.add(itemQuantity);
			conItemList.add(ItemCutQuantity);
			conItemList.add(eventInfo.getEventUser().toString());
			conItemList.add(TimeUtils.getCurrentEventTimeKey());
			conItemList.add(TimeUtils.getCurrentTimestamp());
			conItemList.add("CreateWorkFlowByExcel");
			conItemList.add("");
			conItemList.add("");
			conItemList.add("");
			conItemList.add("CreateWorkFlowByExcel");
			conItemList.add("");
			updateWorkFlowItem.add(conItemList.toArray());
		}
		if (updateWorkFlow.size() > 0&&updateWorkFlowItem.size()>0)
		{
			StringBuffer insertWorkFlowConsql = new StringBuffer();
			insertWorkFlowConsql.append("INSERT INTO CT_WORKFLOW  ");
			insertWorkFlowConsql.append("(WORKFLOWID,WORKFLOWTYPE,DEPARMENT,REQUESTTIME,WORKFLOWLEVEL,TESTTYPE,SAMPLINGTYPE, ");
			insertWorkFlowConsql.append(" TESTENGINEER,WORKFLOWNODE,WORKFLOWSTATE,LASTEVENTUSER,LASTEVENTTIMEKEY,LASTEVENTTIME, ");
			insertWorkFlowConsql.append(" LASTEVENTNAME ) ");
			insertWorkFlowConsql.append(" VALUES  ");
			insertWorkFlowConsql.append("(?,?,?,?,?,?,?,?,?,?,?,?,?,?) ");
			
			StringBuffer insertupdateWorkFlowItemConsql = new StringBuffer();
			insertupdateWorkFlowItemConsql.append("INSERT INTO CT_WORKFLOWITEM  ");
			insertupdateWorkFlowItemConsql.append("(WORKFLOWID,WORKFLOWTYPE,TESTMACHINETYPE,TESTMACHINENAME,TESTITEMSTATE,TESTITEM,ITEMQUANTITY,ITEMCUTQUANTITY, ");
			insertupdateWorkFlowItemConsql.append(" LASTEVENTUSER,LASTEVENTTIMEKEY,LASTEVENTTIME,LASTEVENTNAME,TESTSTARTTIME,TESTENDTIME,TESTRESULT,LASTEVENTCOMMENT, ");
			insertupdateWorkFlowItemConsql.append("  TESTUSENAME) ");
			insertupdateWorkFlowItemConsql.append(" VALUES  ");
			insertupdateWorkFlowItemConsql.append("(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ");
			try
			{
			    GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);	
				MESLotServiceProxy.getLotServiceUtil().updateBatch(insertWorkFlowConsql.toString(), updateWorkFlow);
				MESLotServiceProxy.getLotServiceUtil().updateBatch(insertupdateWorkFlowItemConsql.toString(), updateWorkFlowItem);
				GenericServiceProxy.getTxDataSourceManager().commitTransaction();
			}
			catch (Exception e)
		    {
			
	    	  GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
		    }
		}
		else
		{
			throw new CustomException("Fail");
		}
	return doc;
	}
}
