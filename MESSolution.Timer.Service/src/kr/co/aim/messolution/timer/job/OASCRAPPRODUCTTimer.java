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
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

public class OASCRAPPRODUCTTimer implements Job, InitializingBean {
	private static Log log = LogFactory.getLog(OASCRAPPRODUCTTimer.class);

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
			monitorOASCRAPPRODUCT();
		}
		catch (CustomException e)
		{
			if (log.isDebugEnabled())
				log.error(e.errorDef.getLoc_errorMessage());
		}
	}
	public void monitorOASCRAPPRODUCT() throws CustomException
	{
		//CT_OASCRAPPRODUCT@oadblink
        Timestamp Time=TimeUtils.getCurrentTimestamp();
        String Timekey=TimeUtils.getCurrentTime(TimeStampUtil.FORMAT_TIMEKEY);
        StringBuffer sql = new StringBuffer();
		sql.append("SELECT*FROM CT_OASCRAPPRODUCT@oadblink  WHERE DATA_STATUS IS NULL AND RESULTMESSAGE IS NULL AND PROCESSNO IS NOT NULL AND PRODUCTNAME IS  NOT NULL");
		Map<String, String> bindMap = new HashMap<String, String>();
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
			throw new CustomException("Select OASCRAPPRODUCT Failed", fe.getMessage());
			// return;
		}
		if (sqlResult != null && sqlResult.size() > 0)
		{ for (Map<String, Object> list : sqlResult)
		  {
			List<Object[]> updateOAScrapProductName = new ArrayList<Object[]>();
			List<Object[]> updateOAScrapProduct = new ArrayList<Object[]>();
			String productList=ConvertUtil.getMapValueByName(list, "PRODUCTNAME");
			String[] strs = productList.split(",");
			for(int i=0 ;i<strs.length;i++)
			{
		    String productName=strs[i].toString();
		    String processNo=ConvertUtil.getMapValueByName(list, "PROCESSNO");
		    String lotName=ConvertUtil.getMapValueByName(list, "LOTNAME");
		    String productSpecName=ConvertUtil.getMapValueByName(list, "PRODUCTSPEC");
		    String productRequest=ConvertUtil.getMapValueByName(list, "PRODUCTREQUESTNAME");
		    String processOperation=ConvertUtil.getMapValueByName(list, "PROCESSOPERATIONNAME");
		    String carrierName=ConvertUtil.getMapValueByName(list, "CARRIERNAME");
		    String scrapFactory=ConvertUtil.getMapValueByName(list, "PLANSCRAPFACTORY");
		    String scrapCode=ConvertUtil.getMapValueByName(list, "REASONCODE");
		    String productType=ConvertUtil.getMapValueByName(list, "PRODUCTTYPE");
		    String glassType=ConvertUtil.getMapValueByName(list, "GLASSTYPE");
		    String scrapMachineName=ConvertUtil.getMapValueByName(list, "UNITNAME");
		    String scrapUnitName=ConvertUtil.getMapValueByName(list, "SUBUNITNAME");
		    String scrapDeparment=ConvertUtil.getMapValueByName(list, "IMPLEMENTDEP");
		    String scrapUser=ConvertUtil.getMapValueByName(list, "EVENTUSER");
		    String responsiblityCode=ConvertUtil.getMapValueByName(list, "RESPONSIBILITYCODE");
		    String responsiblityDeparment=ConvertUtil.getMapValueByName(list, "RESPONSIBILITYDEP");
		    String operationType=ConvertUtil.getMapValueByName(list, "OPERATIONTYPE");
		    
		    List<Object> conBindList = new ArrayList<Object>();
			conBindList.add(processNo);
			conBindList.add(productName);
			conBindList.add(lotName);
			conBindList.add(productSpecName);
			conBindList.add(productRequest);
			conBindList.add(processOperation);
			conBindList.add(carrierName);
			conBindList.add(scrapFactory);
			conBindList.add(scrapCode);
			conBindList.add(productType);
			conBindList.add(glassType);
			conBindList.add(scrapMachineName);
			conBindList.add(scrapUnitName);
			conBindList.add(scrapDeparment);
			conBindList.add(scrapUser);
			conBindList.add(responsiblityCode);
			conBindList.add(responsiblityDeparment);
			conBindList.add(Timekey);
			conBindList.add(Time);
			conBindList.add(operationType);
			updateOAScrapProductName.add(conBindList.toArray());
			}
			String processNumber=ConvertUtil.getMapValueByName(list, "PROCESSNO");
			List<Object> conOABindList = new ArrayList<Object>();
			conOABindList.add("Y");
			conOABindList.add(processNumber);
			updateOAScrapProduct.add(conOABindList.toArray());
		  
		  if (updateOAScrapProductName.size()>0)
		  {
			StringBuffer insertWorkFlowConsql = new StringBuffer();
			insertWorkFlowConsql.append("INSERT INTO CT_OASCRAPPRODUCTNAME  ");
			insertWorkFlowConsql.append("(PROCESSNO,PRODUCTNAME,LOTNAME,PRODUCTSPECNAME,PRODUCTREQUESTNAME,PROCESSOPERATIONNAME,CARRIERNAME, ");
			insertWorkFlowConsql.append(" SCRAPFACTORY,SCRAPCODE,PRODUCTTYPE,GLASSTYPE,SCRAPMACHINENAME,SCRAPUNITNAME,SCRAPDEPARTMENT,SCRAPUSER, ");
			insertWorkFlowConsql.append(" RESPONSIBILITYCODE,RESPONSIBILITYDEPARMENT,LASTEVENTTIMEKEY,LASTEVENTTIME,OPERATIONTYPE ) ");
			insertWorkFlowConsql.append(" VALUES  ");
			insertWorkFlowConsql.append("(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ");
	
			StringBuffer updateOAConsql = new StringBuffer();
			updateOAConsql.append("UPDATE CT_OASCRAPPRODUCT@oadblink  ");
			updateOAConsql.append("   SET DATA_STATUS = ? ");
			updateOAConsql.append(" WHERE PROCESSNO=? ");
			try
			{
			GenericServiceProxy.getTxDataSourceManager().beginTransaction();
			
			MESLotServiceProxy.getLotServiceUtil().updateBatch(insertWorkFlowConsql.toString(), updateOAScrapProductName);
			MESLotServiceProxy.getLotServiceUtil().updateBatch(updateOAConsql.toString(), updateOAScrapProduct);
		    GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		    }
	        catch (Exception e)
		    {
	        	GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
	        	StringBuffer updateOAErrorConsql = new StringBuffer();
	        	updateOAErrorConsql.append("UPDATE CT_OASCRAPPRODUCT@oadblink  ");
	        	updateOAErrorConsql.append("   SET RESULTMESSAGE = ? ");
	        	updateOAErrorConsql.append(" WHERE PROCESSNO=? ");
	        	
	        	GenericServiceProxy.getTxDataSourceManager().beginTransaction();
	        	MESLotServiceProxy.getLotServiceUtil().updateBatch(updateOAErrorConsql.toString(), updateOAScrapProduct);
	        	GenericServiceProxy.getTxDataSourceManager().commitTransaction();
	    	  
	    	    continue;
		    }
		  }
	    }
	  }
	}
}
