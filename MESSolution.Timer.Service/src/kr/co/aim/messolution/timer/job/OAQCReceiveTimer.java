package kr.co.aim.messolution.timer.job;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

public class OAQCReceiveTimer implements Job, InitializingBean {
	private static Log log = LogFactory.getLog(OAQCReceiveTimer.class);

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		try
		{
			monitorQC();
		}
		catch (CustomException e)
		{
			if (log.isDebugEnabled())
				log.error(e.errorDef.getLoc_errorMessage());
		}
	}
	public void monitorQC() throws CustomException
	{
        Timestamp Time=TimeUtils.getCurrentTimestamp();
        String Timekey=TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY);
        StringBuffer sql = new StringBuffer();
		sql.append("SELECT*FROM CT_OAQCFLOWINFO@oadblink WHERE RETURNCODE IS NULL");
		Map<String, String> bindMap = new HashMap<String, String>();
		List<Object[]> updateWorkFlow = new ArrayList<Object[]>();
		List<Object[]> updateWorkFlowItem = new ArrayList<Object[]>();
		List<Object[]> updateOAWorkFlow = new ArrayList<Object[]>();
		List<Map<String, Object>> sqlResult;
		try
		{
			GenericServiceProxy.getTxDataSourceManager().beginTransaction();
			sqlResult = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), bindMap);
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		}
		catch (FrameworkErrorSignal fe)
		{
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			sqlResult = null;
			throw new CustomException("Select OAQC Failed", fe.getMessage());
			// return;
		}
		if (sqlResult != null && sqlResult.size() > 0)
		{ for (Map<String, Object> list : sqlResult)
		  {
			String workFlowID = ConvertUtil.getMapValueByName(list, "WORKFLOWID");
			String workFlowType="QC";
			String deparment=ConvertUtil.getMapValueByName(list, "DEPARMENT");
			String MachineType=ConvertUtil.getMapValueByName(list, "TESTMACHINETYPE");
			String requestTime=ConvertUtil.getMapValueByName(list, "REQUESTTIME");
			String leve=ConvertUtil.getMapValueByName(list, "LEVE");
			String tstType=ConvertUtil.getMapValueByName(list, "TESTTYPE");
			String testItem=ConvertUtil.getMapValueByName(list, "TESTITEM");
			String itemQuantity=ConvertUtil.getMapValueByName(list, "ITEMQUANTITY");
			String testRequireMent=ConvertUtil.getMapValueByName(list, "TESTREQUIREMENT");
			String testEnginner=ConvertUtil.getMapValueByName(list, "TESTENGINEER");
			
			List<Object> conBindList = new ArrayList<Object>();
			conBindList.add(workFlowID);
			conBindList.add(workFlowType);
			conBindList.add(deparment);
			conBindList.add(TimeUtils.getTimestamp(requestTime));
			conBindList.add(leve);
			conBindList.add(tstType);
			conBindList.add("");
			conBindList.add(testEnginner);
			conBindList.add("第三节点");
			conBindList.add("Created");
			conBindList.add("MES");
			conBindList.add(Timekey);
			conBindList.add(Time);
			conBindList.add("OAQCReceiveTimer");
			
			updateWorkFlow.add(conBindList.toArray());
			
			List<Object> conItemList = new ArrayList<Object>();
			conItemList.add(workFlowID);
			conItemList.add(workFlowType);
			conItemList.add(MachineType);
			conItemList.add("");
			conItemList.add("Created");
			conItemList.add(testItem);
			conItemList.add(itemQuantity);
			conItemList.add("");
			conItemList.add("MES");
			conItemList.add(Timekey);
			conItemList.add(Time);
			conItemList.add("OAQCReceiveTimer");
			conItemList.add("");
			conItemList.add("");
			conItemList.add("");
			conItemList.add("OAQCReceiveTimer");
			conItemList.add("");
			updateWorkFlowItem.add(conItemList.toArray());
			List<Object> conOABindList = new ArrayList<Object>();
			conOABindList.add("Y");
			conOABindList.add(workFlowID);
			conOABindList.add(testItem);
			conOABindList.add(itemQuantity);
			updateOAWorkFlow.add(conOABindList.toArray());
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
			
			StringBuffer updateOAConsql = new StringBuffer();
			updateOAConsql.append("UPDATE CT_OAQCFLOWINFO@oadblink  ");
			updateOAConsql.append("   SET RETURNCODE = ? ");
			updateOAConsql.append(" WHERE WORKFLOWID = ? AND TESTITEM=? AND ITEMQUANTITY=? ");
			try
			{
			GenericServiceProxy.getTxDataSourceManager().beginTransaction();
			
			MESLotServiceProxy.getLotServiceUtil().updateBatch(insertWorkFlowConsql.toString(), updateWorkFlow);
			MESLotServiceProxy.getLotServiceUtil().updateBatch(insertupdateWorkFlowItemConsql.toString(), updateWorkFlowItem);
			MESLotServiceProxy.getLotServiceUtil().updateBatch(updateOAConsql.toString(), updateOAWorkFlow);
		    GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		    }
	        catch (Exception e)
		    {
			
	    	  GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
	    	  return;
				
		    }
		 }
	   }
	}
}
