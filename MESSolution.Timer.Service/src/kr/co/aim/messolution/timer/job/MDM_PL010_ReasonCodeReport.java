package kr.co.aim.messolution.timer.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class MDM_PL010_ReasonCodeReport implements Job, InitializingBean
{	
	private static Log log = LogFactory.getLog(MDM_PL010_ReasonCodeReport.class);
	
	@Override
	public void afterPropertiesSet() throws Exception 
	{
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException 
	{	
		try
		{
			monitorMDM();
		}
		catch (CustomException e)
		{
			if (log.isDebugEnabled())
				log.error(e.errorDef.getLoc_errorMessage());
		}
	}


	public void monitorMDM() throws CustomException
	{
		log.info("MDM_PL010_ReasonCode start by Timer");
		StringBuffer sql = new StringBuffer(); 
		sql.append("SELECT R1.FACTORYNAME, R1.REASONCODETYPE, R1.REASONCODE, R1.DESCRIPTION, R1.SUPERREASONCODE, R1.LEVELNO, R1.AVAILABILITY, R1.SEQ, R1.MACHINENAME,R.REASONCODE AS REMOVEFLAG ");
		sql.append("  FROM REASONCODEHISTORY R1,REASONCODE R, ");
		sql.append("       (  SELECT RH.REASONCODE, RH.FACTORYNAME, RH.REASONCODETYPE, MAX (RH.TIMEKEY) TIMEKEY ");
		sql.append("            FROM REASONCODEHISTORY RH ");
		sql.append("           WHERE TIMEKEY BETWEEN TO_CHAR (SYSDATE - 1/24, 'YYYYMMDDHH24') ");
		sql.append("                             AND TO_CHAR (SYSDATE, 'YYYYMMDDHH24') ");
		sql.append("            AND RH.REASONCODETYPE = 'ChangeMachineState' ");
		sql.append("        GROUP BY RH.FACTORYNAME, RH.REASONCODE, RH.REASONCODETYPE) R2 ");
		sql.append(" WHERE R1.REASONCODE = R2.REASONCODE ");
		sql.append("    AND R1.TIMEKEY = R2.TIMEKEY ");
		sql.append("    AND R1.FACTORYNAME = R2.FACTORYNAME ");
		sql.append("    AND R1.REASONCODETYPE = R2.REASONCODETYPE ");
		sql.append("    AND R1.FACTORYNAME=R.FACTORYNAME(+) ");
		sql.append("    AND R1.REASONCODETYPE = R.REASONCODETYPE(+) ");
		sql.append("    AND R1.REASONCODETYPE = R2.REASONCODETYPE ");
		sql.append("    AND R1.REASONCODE = R.REASONCODE(+) ");
		Object[] bindArray = new Object[0];
		
		List<ListOrderedMap> mesResult; 
		
		try
		{
			GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
			mesResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindArray);
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		}
		catch (FrameworkErrorSignal fe)
		{
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			mesResult = null;
		}
		
		if(mesResult != null && mesResult.size() > 0)
		{
			try
			{
				List<Object[]> updateArgList = new ArrayList<Object[]>();

				StringBuffer updateSql = new StringBuffer();
				updateSql.append("MERGE INTO MES_MDMIF_PL010@OADBLINK.V3FAB.COM ");
				updateSql.append("    USING DUAL ");
				updateSql.append("        ON(FACTORYNAME = ? AND REASONCODETYPE = ? AND REASONCODE = ? AND ORG_CODE = '500101') ");
				updateSql.append("    WHEN MATCHED THEN ");
				updateSql.append("        UPDATE SET STATUS_CODE = ?, DESCRIPTION = ?, SUPERREASONCODE = ?, LEVELNO = ?, AVAILABILITY = ?, SEQ = ?, MACHINENAME = ?, ESBFLAG = 'N'              ");
				updateSql.append("    WHEN NOT MATCHED THEN ");
				updateSql.append("        INSERT(MD_TYPE_CODE, MD_CATE_CODE, STATUS_CODE, FACTORYNAME, REASONCODETYPE, REASONCODE, DESCRIPTION, SUPERREASONCODE, LEVELNO, AVAILABILITY,  ");
				updateSql.append("               SEQ, MACHINENAME, ORG_CODE, ESBFLAG ");
				updateSql.append("               ) ");
				updateSql.append("        VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?,   ");
				updateSql.append("               ?, ?, '500101', 'N') ");

				for (ListOrderedMap mesRow : mesResult)
				{
					String MDTypeCode = "PL010";
					String MDCateCode = "CHANGEMACHINESTATE";
					String availability = CommonUtil.getValue(mesRow, "AVAILABILITY");
					
					String statusCode = "";
					if(availability.equals("Available"))
						statusCode = "ACTV";
					else
						statusCode = "EXPR";					
					String factoryName = CommonUtil.getValue(mesRow, "FACTORYNAME");
					String reasonCodeType = CommonUtil.getValue(mesRow, "REASONCODETYPE");
					String reasonCode =  CommonUtil.getValue(mesRow, "REASONCODE");
					String description = CommonUtil.getValue(mesRow, "DESCRIPTION");
					if(StringUtil.isEmpty(description))
					{
						description="����";
					}
					String superReasonCode = CommonUtil.getValue(mesRow, "SUPERREASONCODE");
					String levelNo = CommonUtil.getValue(mesRow, "LEVELNO");
					String seq = CommonUtil.getValue(mesRow, "SEQ");
					String machineName = CommonUtil.getValue(mesRow, "MACHINENAME");
					
					if( StringUtil.isEmpty(CommonUtil.getValue(mesRow, "REMOVEFLAG")))
					{
						availability="NotAvailable";
						description="Delete";
					}
					
					List<Object> bindList = new ArrayList<Object>();
					
					//Match
					bindList.add(factoryName);
					bindList.add(reasonCodeType);
					bindList.add(reasonCode);
					
					//Update
					bindList.add(statusCode);
					bindList.add(description);
					bindList.add(superReasonCode);
					bindList.add(levelNo);
					bindList.add(availability);
					bindList.add(seq);
					bindList.add(machineName);				
					
					//Insert
					bindList.add(MDTypeCode);
					bindList.add(MDCateCode);
					bindList.add(statusCode);
					bindList.add(factoryName);
					bindList.add(reasonCodeType);
					bindList.add(reasonCode);
					bindList.add(description);
					bindList.add(superReasonCode);
					bindList.add(levelNo);
					bindList.add(availability);
					bindList.add(seq);
					bindList.add(machineName);	
					
					updateArgList.add(bindList.toArray());
				}	

				//Update MDM
				if(updateArgList != null && updateArgList.size() > 0)
				{
					try{
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						GenericServiceProxy.getSqlMesTemplate().updateBatch(updateSql.toString(), updateArgList);
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					}catch(Exception e){
						GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
					}
				}
				else
					log.info("Update data not exist to MDM (MDM_PL010_ReasonCodeReport)");
			}
			catch(Exception e)
			{
				log.error(e.getMessage());
			}
			finally
			{
				log.info("MDM_PL010_ReasonCodeReport end");
			}
		}
	}
}
