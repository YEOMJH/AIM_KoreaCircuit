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
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
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
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;

public class MDM_PL002_MachineSpecReport implements Job, InitializingBean
{	
	private static Log log = LogFactory.getLog(MDM_PL002_MachineSpecReport.class);
	
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
		log.info("MDM_PL002_MachineSpecReport start by Timer");
		StringBuffer sql = new StringBuffer(); 
		sql.append("SELECT S1.MACHINENAME,M.MACHINENAME AS REMOVEFLAG, S1.MACHINETYPE, S1.DETAILMACHINETYPE, S1.DESCRIPTION, S1.FACTORYNAME, S1.AREANAME, S1.SUPERMACHINENAME, S1.MACHINEGROUPNAME, S1.VENDOR, S1.PROCESSUNIT, ");
		sql.append("    S1.PROCESSCAPACITY, S1.MACHINESTATEMODELNAME, S1.EQPDEPART, S1.RMSFLAG, S1.CHAMBERYN, S1.CHAMBERUSETYPE, S1.MQCLASTEVENTTIME, S1.MATERIALTYPE, S1.TPSLOTCHECK, S1.CHECKIDLETIME, ");
		sql.append("    S1.MAXIDLETIME, S1.LINETYPE, S1.OFFSETID, S1.IDLETIMELIMIT, S1.TURNUNITFLAG, S1.CUTUNITFLAG, S1.SCRAPUNITFLAG, S1.BASELINEFLAG, S1.GLASSPROCESSTIME, S1.TRANSFERTIME,  ");
		sql.append("    S1.TURNDEGREE, S1.QTIMEFLAG, M.RESOURCESTATE ");
		sql.append("  FROM MACHINESPECHISTORY S1, ");
		sql.append("       (  SELECT MH.MACHINENAME, MAX (MH.TIMEKEY) TIMEKEY ");
		sql.append("            FROM MACHINESPECHISTORY MH ");
		sql.append("           WHERE TIMEKEY BETWEEN TO_CHAR(SYSDATE - 1, 'YYYYMMDD')||'200000'   ");
		sql.append("                             AND TO_CHAR(SYSDATE, 'YYYYMMDD') ||'200000'  ");
		sql.append("        GROUP BY MH.MACHINENAME) S2, ");
		sql.append("        MACHINE M ");
		sql.append(" WHERE S1.MACHINENAME = S2.MACHINENAME  ");
		sql.append("    AND S1.TIMEKEY = S2.TIMEKEY ");
		sql.append("    AND S1.MACHINENAME = M.MACHINENAME(+) ");
		sql.append("    AND S1.FACTORYNAME NOT IN('INT','QC') ");

		Object[] bindArray = new Object[0];
		
		List<ListOrderedMap> mesResult; 
		
		try
		{
			GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
			mesResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindArray);
			log.info("Get PL002 Result Rows Count: " + mesResult.size());
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		}
		catch (FrameworkErrorSignal fe)
		{
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			mesResult = null;
			throw new CustomException("SYS-9999", fe.getMessage());
		}
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", "MDM", "MDM_PL001", "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		if(mesResult != null && mesResult.size() > 0)
		{
			try
			{
				List<Object[]> updateArgList = new ArrayList<Object[]>();

				StringBuffer updateSql = new StringBuffer();
				updateSql.append("MERGE INTO MES_MDMIF_PL002@OADBLINK.V3FAB.COM IFM ");
				updateSql.append("    USING DUAL ");
				updateSql.append("        ON(IFM.MACHINENAME = ? AND IFM.ORG_CODE = '500101') ");
				updateSql.append("    WHEN MATCHED THEN ");
				updateSql.append("        UPDATE SET MDTYPECODE = ?, MDCATECODE = ?, STATUSCODE = ?, QTIMEFLAG = ?, RESOURCESTATE = ?, MACHINETYPE = ?, DETAILMACHINETYPE = ?, DESCRIPTION = ?, FACTORYNAME = ?, AREANAME = ?,  ");
				updateSql.append("            SUPERMACHINENAME = ?, MACHINEGROUPNAME = ?, VENDOR = ?, PROCESSUNIT = ?, PROCESSCAPACITY = ?, MACHINESTATEMODELNAME = ?, EQPDEPART = ?, RMSFLAG = ?, CHAMBERYN = ?, CHAMBERUSETYPE = ?,   ");
				updateSql.append("            MQCLASTEVENTTIME = ?, MATERIALTYPE = ?, TPSLOTCHECK = ?, CHECKIDLETIME = ?, MAXIDLETIME = ?, LINETYPE = ?, OFFSETID = ?, IDLETIMELIMIT = ?, TURNUNITFLAG = ?, CUTUNITFLAG = ?,  ");
				updateSql.append("            SCRAPUNITFLAG = ?, BASELINEFLAG = ?, GLASSPROCESSTIME = ?, TRANSFERTIME = ?, TURNDEGREE = ?, ESBFLAG = 'N' ");
				updateSql.append("    WHEN NOT MATCHED THEN ");
				updateSql.append("        INSERT(MDTYPECODE, MDCATECODE, STATUSCODE, QTIMEFLAG, RESOURCESTATE, MACHINETYPE, DETAILMACHINETYPE, DESCRIPTION, FACTORYNAME, AREANAME,  ");
				updateSql.append("               SUPERMACHINENAME, MACHINEGROUPNAME, VENDOR, PROCESSUNIT, PROCESSCAPACITY, MACHINESTATEMODELNAME, EQPDEPART, RMSFLAG, CHAMBERYN, CHAMBERUSETYPE,  ");
				updateSql.append("               MQCLASTEVENTTIME, MATERIALTYPE, TPSLOTCHECK, CHECKIDLETIME, MAXIDLETIME, LINETYPE, OFFSETID, IDLETIMELIMIT, TURNUNITFLAG, CUTUNITFLAG,  ");
				updateSql.append("               SCRAPUNITFLAG, BASELINEFLAG, GLASSPROCESSTIME, TRANSFERTIME, TURNDEGREE, MACHINENAME, ORG_CODE, ESBFLAG) ");
				updateSql.append("        VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  ");
				updateSql.append("               ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  ");
				updateSql.append("               ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  ");
				updateSql.append("               ?, ?, ?, ?, ?, ?, '500101', 'N') ");

				for (ListOrderedMap mesRow : mesResult)
				{
					String resourceState = CommonUtil.getValue(mesRow, "RESOURCESTATE");
					String machineName =  CommonUtil.getValue(mesRow, "MACHINENAME");
					String MDTypeCode = "PL002";
					String MDCateCode = "MACHINESPEC";
					String statusCode = "";
					if(resourceState.equals("InService"))
						statusCode = "ACTV";
					else
						statusCode = "EXPR";
					String QTimeFlag = CommonUtil.getValue(mesRow, "QTIMEFLAG");
					String machineType = CommonUtil.getValue(mesRow, "MACHINETYPE");
					String detailMachineType = CommonUtil.getValue(mesRow, "DETAILMACHINETYPE");
					String description = CommonUtil.getValue(mesRow, "DESCRIPTION");
					if(StringUtil.isEmpty(description))
					{
						description = "����";
					}
					String factoryName = CommonUtil.getValue(mesRow, "FACTORYNAME");
					String areaName = CommonUtil.getValue(mesRow, "AREANAME");
					String supermachineName = CommonUtil.getValue(mesRow, "SUPERMACHINENAME");
					String machineGroupName = CommonUtil.getValue(mesRow, "MACHINEGROUPNAME");
					String vendor = CommonUtil.getValue(mesRow, "VENDOR");
					String processUnit =  CommonUtil.getValue(mesRow, "PROCESSUNIT");
					String processCapacity = CommonUtil.getValue(mesRow, "PROCESSCAPACITY");
					String machineStateModelName = CommonUtil.getValue(mesRow, "MACHINESTATEMODELNAME");
					String EQPDepart = CommonUtil.getValue(mesRow, "EQPDEPART");
					String RMSFlag = CommonUtil.getValue(mesRow, "RMSFLAG");
					String chamberYN = CommonUtil.getValue(mesRow, "CHAMBERYN");
					String chamberUseType = CommonUtil.getValue(mesRow, "CHAMBERUSETYPE");
					String MQCLastEventTime = CommonUtil.getValue(mesRow, "MQCLASTEVENTTIME");
					String materialType = CommonUtil.getValue(mesRow, "MATERIALTYPE");
					String TPSlotCheck = CommonUtil.getValue(mesRow, "TPSLOTCHECK");
					String checkIdleTime = CommonUtil.getValue(mesRow, "CHECKIDLETIME");
					String maxIdleTime = CommonUtil.getValue(mesRow, "MAXIDLETIME");
					String LineType = CommonUtil.getValue(mesRow, "LINETYPE");
					String offsetID =  CommonUtil.getValue(mesRow, "OFFSETID");
					String idleTimeLimit = CommonUtil.getValue(mesRow, "IDLETIMELIMIT");
					String turnUnitFlag = CommonUtil.getValue(mesRow, "TURNUNITFLAG");
					String cutUnitFlag = CommonUtil.getValue(mesRow, "CUTUNITFLAG");
					String scrapUnitFlag = CommonUtil.getValue(mesRow, "SCRAPUNITFLAG");
					String baseLineFlag = CommonUtil.getValue(mesRow, "BASELINEFLAG");
					String glassProcessTime = CommonUtil.getValue(mesRow, "GLASSPROCESSTIME");
					String transperTime = CommonUtil.getValue(mesRow, "TRANSFERTIME");
					String turnDegree = CommonUtil.getValue(mesRow, "TURNDEGREE");
					if(StringUtils.isEmpty(CommonUtil.getValue(mesRow, "REMOVEFLAG")))
					{
						description="Delete";
						resourceState="OutOfService";
						statusCode = "EXPR";
					}
					
					MachineSpec oldMachineSpecData = null;
					
					try 
					{
						oldMachineSpecData = MESMachineServiceProxy.getMachineInfoUtil().getMachineSpec(machineName);
					} 
					catch (Exception e) 
					{
						oldMachineSpecData = null;
					}
					
					List<Object> bindList = new ArrayList<Object>();
					//Match
					bindList.add(machineName);
					
					//Update
					bindList.add(MDTypeCode);
					bindList.add(MDCateCode);
					bindList.add(statusCode);
					bindList.add(QTimeFlag);
					bindList.add(resourceState);
					bindList.add(machineType);
					bindList.add(detailMachineType);
					bindList.add(description);
					bindList.add(factoryName);
					bindList.add(areaName);
					bindList.add(supermachineName);
					bindList.add(machineGroupName);
					bindList.add(vendor);
					bindList.add(processUnit);
					bindList.add(processCapacity);
					bindList.add(machineStateModelName);
					bindList.add(EQPDepart);
					bindList.add(RMSFlag);
					bindList.add(chamberYN);
					bindList.add(chamberUseType);
					bindList.add(MQCLastEventTime);
					bindList.add(materialType);
					bindList.add(TPSlotCheck);
					bindList.add(checkIdleTime);
					bindList.add(maxIdleTime);
					bindList.add(LineType);
					bindList.add(offsetID);
					bindList.add(idleTimeLimit);
					bindList.add(turnUnitFlag);
					bindList.add(cutUnitFlag);
					bindList.add(scrapUnitFlag);
					bindList.add(baseLineFlag);
					bindList.add(glassProcessTime);
					bindList.add(transperTime);
					bindList.add(turnDegree);
					
					//Insert
					bindList.add(MDTypeCode);
					bindList.add(MDCateCode);
					bindList.add(statusCode);
					bindList.add(QTimeFlag);
					bindList.add(resourceState);
					bindList.add(machineType);
					bindList.add(detailMachineType);
					bindList.add(description);
					bindList.add(factoryName);
					bindList.add(areaName);
					bindList.add(supermachineName);
					bindList.add(machineGroupName);
					bindList.add(vendor);
					bindList.add(processUnit);
					bindList.add(processCapacity);
					bindList.add(machineStateModelName);
					bindList.add(EQPDepart);
					bindList.add(RMSFlag);
					bindList.add(chamberYN);
					bindList.add(chamberUseType);
					bindList.add(MQCLastEventTime);
					bindList.add(materialType);
					bindList.add(TPSlotCheck);
					bindList.add(checkIdleTime);
					bindList.add(maxIdleTime);
					bindList.add(LineType);
					bindList.add(offsetID);
					bindList.add(idleTimeLimit);
					bindList.add(turnUnitFlag);
					bindList.add(cutUnitFlag);
					bindList.add(scrapUnitFlag);
					bindList.add(baseLineFlag);
					bindList.add(glassProcessTime);
					bindList.add(transperTime);
					bindList.add(turnDegree);
					bindList.add(machineName);
					
					updateArgList.add(bindList.toArray());
				}	

				//Update MDM
				if(updateArgList != null && updateArgList.size() > 0)
				{
					try
					{
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						GenericServiceProxy.getSqlMesTemplate().updateBatch(updateSql.toString(), updateArgList);
						log.info("PL002 Insert or Update Rows Count: "+updateArgList.size());
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					}
					catch(Exception e)
					{
						GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
					}
				}
				else
					log.info("Update data not exist to MDM (MDM_PL002_MachineSpecReport)");
			}
			catch(Exception e)
			{
				log.error(e.getMessage());
			}
			finally
			{
				log.info("MDM_PL002_MachineSpecReport end");
			}
		}
	}
}
