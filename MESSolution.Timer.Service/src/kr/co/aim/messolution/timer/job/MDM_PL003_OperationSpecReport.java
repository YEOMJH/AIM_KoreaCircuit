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

public class MDM_PL003_OperationSpecReport implements Job, InitializingBean
{	
	private static Log log = LogFactory.getLog(MDM_PL003_OperationSpecReport.class);
	
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
		log.info("MDM_PL003_OperationSpecReport start by Timer");
		StringBuffer sql = new StringBuffer(); 
		sql.append("SELECT P1.FACTORYNAME, P1.PROCESSOPERATIONNAME,PS.PROCESSOPERATIONNAME AS REMOVEFLAG, P1.PROCESSOPERATIONVERSION, PS.DESCRIPTION, PS.CHECKSTATE, PS.ACTIVESTATE, PS.CREATETIME, PS.CREATEUSER, ");
		sql.append("    PS.CHECKOUTTIME, PS.CHECKOUTUSER, P1.PROCESSOPERATIONTYPE, P1.PROCESSOPERATIONGROUP, P1.PROCESSOPERATIONUNIT, P1.ISLOGINREQUIRED, P1.DEFAULTAREANAME,  ");
		sql.append("    P1.LAYERNAME, P1.ISMAINOPERATION, P1.DEPARTMENT, P1.CHANGELOTNAME, P1.MAINLAYERSTEP ");
		sql.append("  FROM PROCESSOPERATIONSPECHISTORY P1,  PROCESSOPERATIONSPEC PS,  ");
		sql.append("       (  SELECT PH.PROCESSOPERATIONNAME, MAX (PH.TIMEKEY) TIMEKEY ");
		sql.append("            FROM PROCESSOPERATIONSPECHISTORY PH ");
		sql.append("           WHERE TIMEKEY BETWEEN TO_CHAR (SYSDATE - 1, 'YYYYMMDD')||'200000'  ");
		sql.append("                             AND TO_CHAR (SYSDATE, 'YYYYMMDD') ||'200000' ");
		sql.append("        GROUP BY PH.PROCESSOPERATIONNAME) P2 ");
		sql.append(" WHERE P1.PROCESSOPERATIONNAME = P2.PROCESSOPERATIONNAME  ");
		sql.append("    AND P1.TIMEKEY = P2.TIMEKEY ");
		sql.append("    AND P1.PROCESSOPERATIONNAME = PS.PROCESSOPERATIONNAME(+) ");
		sql.append("    AND P1.PROCESSOPERATIONVERSION = PS.PROCESSOPERATIONVERSION(+) ");
		Object[] bindArray = new Object[0];
		
		List<ListOrderedMap> mesResult; 
		
		try
		{
			GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
			mesResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindArray);
			log.info("Get PL003 Result Rows Count: " + mesResult.size());
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		}
		catch (FrameworkErrorSignal fe)
		{
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			mesResult = null;
			throw new CustomException("SYS-9999", fe.getMessage());
		}
		
		if(mesResult != null && mesResult.size() > 0)
		{
			try
			{
				List<Object[]> updateArgList = new ArrayList<Object[]>();

				StringBuffer updateSql = new StringBuffer();
				updateSql.append("MERGE INTO MES_MDMIF_PL003@OADBLINK.V3FAB.COM IFM ");
				updateSql.append("    USING DUAL ");
				updateSql.append("        ON(IFM.FACTORYNAME = ? AND IFM.PROCESSOPERATIONNAME = ? AND IFM.PROCESSOPERATIONVERSION = ? AND IFM.ORG_CODE = '500101') ");
				updateSql.append("    WHEN MATCHED THEN ");
				updateSql.append("        UPDATE SET STATUSCODE = ?, DESCRIPTION = ?, CHECKSTATE = ?, ACTIVESTATE = ?, CREATETIME = ?, CREATEUSER = ?, CHECKOUTTIME = ?, CHECKOUTUSER = ?, PROCESSOPERATIONTYPE = ?, DETAILPROCESSOPERATIONTYPE = ?,  ");
				updateSql.append("            PROCESSOPERATIONGROUP = ?, PROCESSOPERATIONUNIT = ?, ISLOGINREQUIRED = ?, DEFAULTAREANAME = ?, LAYERNAME = ?, ISMAINOPERATION = ?, DEPARTMENT = ?, CHANGELOTNAME = ?, MAINLAYERSTEP = ?, ESBFLAG = 'N'   ");
				updateSql.append("    WHEN NOT MATCHED THEN ");
				updateSql.append("        INSERT(MDTYPECODE, MDCATECODE, STATUSCODE, DESCRIPTION, CHECKSTATE, ACTIVESTATE, CREATETIME, CREATEUSER, CHECKOUTTIME, CHECKOUTUSER,  ");
				updateSql.append("               PROCESSOPERATIONTYPE, DETAILPROCESSOPERATIONTYPE, PROCESSOPERATIONGROUP, PROCESSOPERATIONUNIT, ISLOGINREQUIRED, DEFAULTAREANAME, LAYERNAME, ISMAINOPERATION, DEPARTMENT, CHANGELOTNAME,  ");
				updateSql.append("               MAINLAYERSTEP, ESBFLAG, FACTORYNAME, ORG_CODE, PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION) ");
				updateSql.append("        VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  ");
				updateSql.append("               ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,   ");
				updateSql.append("               ?, 'N', ?, '500101', ?, ?) ");

				for (ListOrderedMap mesRow : mesResult)
				{
					String MDTypeCode = "PL003";
					String MDCateCode = "OPERATIONSPEC";
					String factoryName = CommonUtil.getValue(mesRow, "FACTORYNAME");
					String processOperationName =  CommonUtil.getValue(mesRow, "PROCESSOPERATIONNAME");
					String processOperationVersion = CommonUtil.getValue(mesRow, "PROCESSOPERATIONVERSION");
					String activeState = CommonUtil.getValue(mesRow, "ACTIVESTATE");
					String statusCode = "";
					if(activeState.equals("Active"))
						statusCode = "ACTV";
					else
						statusCode = "EXPR";
					String description = CommonUtil.getValue(mesRow, "DESCRIPTION");
					if(StringUtil.isEmpty(description))
					{
						description="����";
					}
					String checkState = CommonUtil.getValue(mesRow, "CHECKSTATE");
					String createTime =(mesRow.get("CREATETIME")==null?"":mesRow.get("CREATETIME")).toString();
					String createUser = CommonUtil.getValue(mesRow, "CREATEUSER");
					String checkOutTime =(mesRow.get("CHECKOUTTIME")==null?"":mesRow.get("CHECKOUTTIME")).toString();
					String checkOutUser = CommonUtil.getValue(mesRow, "CHECKOUTUSER");
					String processOperationType = CommonUtil.getValue(mesRow, "PROCESSOPERATIONTYPE");
					String detailProcessOperationType = CommonUtil.getValue(mesRow, "DETAILPROCESSOPERATIONTYPE");
					String processOperationGroup =  CommonUtil.getValue(mesRow, "PROCESSOPERATIONGROUP");
					String processOperationUnit = CommonUtil.getValue(mesRow, "PROCESSOPERATIONUNIT");
					String isLoginRequired = CommonUtil.getValue(mesRow, "ISLOGINREQUIRED");
					String defaultAreaName = CommonUtil.getValue(mesRow, "DEFAULTAREANAME");
					String layerName = CommonUtil.getValue(mesRow, "LAYERNAME");
					String isMainOperation = CommonUtil.getValue(mesRow, "ISMAINOPERATION");
					String department = CommonUtil.getValue(mesRow, "DEPARTMENT");
					String changeLotName = CommonUtil.getValue(mesRow, "CHANGELOTNAME");
					String mainLayerStep = CommonUtil.getValue(mesRow, "MAINLAYERSTEP");
					
					if(StringUtils.isEmpty(CommonUtil.getValue(mesRow, "REMOVEFLAG")))
					{
						description="Delete";
						checkState="CheckedIn";
						statusCode = "EXPR";
						activeState="NotActive";
					}
					
					List<Object> bindList = new ArrayList<Object>();
					//Match
					bindList.add(factoryName);
					bindList.add(processOperationName);
					bindList.add(processOperationVersion);
					
					//Update
					bindList.add(statusCode);
					bindList.add(description);
					bindList.add(checkState);
					bindList.add(activeState);
					bindList.add(createTime);
					bindList.add(createUser);
					bindList.add(checkOutTime);
					bindList.add(checkOutUser);
					bindList.add(processOperationType);
					bindList.add(detailProcessOperationType);
					bindList.add(processOperationGroup);
					bindList.add(processOperationUnit);
					bindList.add(isLoginRequired);
					bindList.add(defaultAreaName);
					bindList.add(layerName);
					bindList.add(isMainOperation);
					bindList.add(department);
					bindList.add(changeLotName);
					bindList.add(mainLayerStep);
					
					//Insert
					bindList.add(MDTypeCode);
					bindList.add(MDCateCode);
					bindList.add(statusCode);
					bindList.add(description);
					bindList.add(checkState);
					bindList.add(activeState);
					bindList.add(createTime);
					bindList.add(createUser);
					bindList.add(checkOutTime);
					bindList.add(checkOutUser);
					bindList.add(processOperationType);
					bindList.add(detailProcessOperationType);
					bindList.add(processOperationGroup);
					bindList.add(processOperationUnit);
					bindList.add(isLoginRequired);
					bindList.add(defaultAreaName);
					bindList.add(layerName);
					bindList.add(isMainOperation);
					bindList.add(department);
					bindList.add(changeLotName);
					bindList.add(mainLayerStep);
					bindList.add(factoryName);
					bindList.add(processOperationName);
					bindList.add(processOperationVersion);
					
					updateArgList.add(bindList.toArray());
				}	

				//Update MDM
				if(updateArgList != null && updateArgList.size() > 0)
				{
					try
					{
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						GenericServiceProxy.getSqlMesTemplate().updateBatch(updateSql.toString(), updateArgList);
						log.info("PL003 Insert or Update Rows Count: "+updateArgList.size());
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					}
					catch(Exception e)
					{
						GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
					}
				}
				else
					log.info("Update data not exist to MDM (MDM_PL003_ProcessOperationSpecReport)");
			}
			catch(Exception e)
			{
				log.error(e.getMessage());
			}
			finally
			{
				log.info("MDM_PL003_ProcessOperationSpecReport end");
			}
		}
	}
}
