package kr.co.aim.messolution.timer.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.DefectCode;
import kr.co.aim.messolution.extended.object.management.data.MaskSpec;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.esb.ESBService;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;
import kr.co.aim.greentrack.productrequest.management.info.CreateInfo;

public class MES_SAPIF_WOBOM_ReceiveBOM implements Job, InitializingBean {
	private static Log log = LogFactory.getLog(MES_SAPIF_WOBOM_ReceiveBOM.class);

	@Override
	public void afterPropertiesSet() throws Exception
	{
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}

	public void execute(JobExecutionContext arg0) throws JobExecutionException
	{
		try
		{
			monitorSAP();
		}
		catch (CustomException e)
		{
			if (log.isDebugEnabled())
				log.error(e.errorDef.getLoc_errorMessage());
		}
	}

	public void monitorSAP() throws CustomException
	{
		Object[] bindArray = new Object[0];
		int insertCount=0;
		
		StringBuffer sqlSelect = new StringBuffer();
		sqlSelect.append("SELECT PRODUCTREQUESTNAME,FACTORYNAME, CONSUMABLESPECNAME, QUANTITY, CONSUMEUNIT, PROCESSOPERATIONNAME, FACTORYCODE,  ");
		sqlSelect.append("    FACTORYPOSITION, VIRTUALFLAG, DELETEFLAG, SUBSTITUTEGROUP, ESBFLAG, RESULTMESSAGE ");
		sqlSelect.append("FROM MES_SAPIF_WOBOM@OADBLINK.V3FAB.COM ");
		sqlSelect.append("WHERE ESBFLAG = 'N' ");
		sqlSelect.append("AND FACTORYNAME <>'MODULE'  ");
		
		StringBuffer backUpMaterialSelect = new StringBuffer();
		backUpMaterialSelect.append(" SELECT DISTINCT MATERIALSPECNAME FROM CT_ERPBOM WHERE KITFLAG='Y' ");
		
		StringBuffer distinctSelect = new StringBuffer();
		distinctSelect.append(" SELECT CASE WHEN (FACTORYNAME like 'OLED%' ) THEN 'OLED' ELSE FACTORYNAME END FACTORYNAME,PRODUCTREQUESTNAME  FROM ( ");
		distinctSelect.append(" SELECT DISTINCT FACTORYNAME, PRODUCTREQUESTNAME ");
		distinctSelect.append("    FROM MES_SAPIF_WOBOM@OADBLINK.V3FAB.COM ");
		distinctSelect.append("    WHERE ESBFLAG = 'N' AND (FACTORYNAME IS NOT NULL AND FACTORYNAME <> 'MODULE')) ");

		List<ListOrderedMap> selResult;
		List<ListOrderedMap> backUpMaterialList;
		List<ListOrderedMap> distinctList;

		try
		{
			GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
			selResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlSelect.toString(), bindArray);
			backUpMaterialList = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(backUpMaterialSelect.toString(), bindArray);
			distinctList = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(distinctSelect.toString(), bindArray);
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		}
		catch (FrameworkErrorSignal fe)
		{
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			selResult = null;
			backUpMaterialList=null;
			distinctList=null;
		}
		
		if (selResult != null && selResult.size() > 0)
		{
			StringBuffer sqlUpdateResult = new StringBuffer();
			sqlUpdateResult.append("UPDATE MES_SAPIF_WOBOM@OADBLINK.V3FAB.COM SET ESBFLAG = ?, RESULT = ?, RESULTMESSAGE = ? WHERE PRODUCTREQUESTNAME = ? AND CONSUMABLESPECNAME = ? AND PROCESSOPERATIONNAME = ? ");
			
			StringBuffer sqlDelete = new StringBuffer();
			sqlDelete.append("  DELETE CT_ERPBOM WHERE FACTORYNAME=? AND PRODUCTREQUESTNAME=?  ");

			StringBuffer sql = new StringBuffer();
			
			sql.append("INSERT INTO CT_ERPBOM(FACTORYNAME, PRODUCTREQUESTNAME, PROCESSOPERATIONNAME, PROCESSOPERATIONVERSION, MATERIALSPECNAME,  ");
			sql.append("    MATERIALSPECVERSION, QUANTITY, LASTEVENTNAME, LASTEVENTTIMEKEY, LASTEVENTTIME,  ");
			sql.append("    LASTEVENTUSER, LASTEVENTCOMMENT, CONSUMEUNIT, FACTORYCODE, FACTORYPOSITION,SUBSTITUTEGROUP,VIRTUALFLAG,KITFLAG ) ");
			sql.append("    VALUES ");
			sql.append("    (?, ?, ?, ?, ?, ");
			sql.append("    ?, ?, ?, ?, ?, ");
			sql.append("    ?, ?, ?, ?, ?, ?, ?, ? ) ");
			
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("InsertERPBOM", "ERP", "Insert BOM By ERP", null, null);
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
			
			if(distinctList !=null && distinctList.size()>0)
			{
				for(ListOrderedMap resultRow : distinctList)
				{
					List<Object[]> deleteArgList = new ArrayList<Object[]>();
					List<Object> deleteBindList = new ArrayList<Object>();
					
					deleteBindList.add(CommonUtil.getValue(resultRow, "FACTORYNAME"));
					deleteBindList.add(CommonUtil.getValue(resultRow, "PRODUCTREQUESTNAME"));
					deleteArgList.add(deleteBindList.toArray());
						
					try
					{
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlDelete.toString(), deleteArgList);
						log.info("CT_ERPBOM Delete FactoryName: "+CommonUtil.getValue(resultRow, "FACTORYNAME")+" ProductRequest: "+CommonUtil.getValue(resultRow, "PRODUCTREQUESTNAME") );
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					}
					catch (Exception e)
					{
						log.error("Delete ERPBOM Error ,Factory=: "+CommonUtil.getValue(resultRow, "FACTORYNAME")
						+" ProductRequest=:"+CommonUtil.getValue(resultRow, "PRODUCTREQUESTNAME"));	
						continue;
					}
				}
			}
			
			for (ListOrderedMap resultRow : selResult)
			{			
				String productRequestName = CommonUtil.getValue(resultRow, "PRODUCTREQUESTNAME");
				String consumableSpecName = CommonUtil.getValue(resultRow, "CONSUMABLESPECNAME");
				String quantity = CommonUtil.getValue(resultRow, "QUANTITY");
				String consumUnit = CommonUtil.getValue(resultRow, "CONSUMEUNIT");
				String processOperationName = CommonUtil.getValue(resultRow, "PROCESSOPERATIONNAME");
				String factoryCode = CommonUtil.getValue(resultRow, "FACTORYCODE");
				String factoryPosition = CommonUtil.getValue(resultRow, "FACTORYPOSITION");
				String factoryName = CommonUtil.getValue(resultRow, "FACTORYNAME");
				String subStituteGroup = CommonUtil.getValue(resultRow, "SUBSTITUTEGROUP");
				String virtualFlag = CommonUtil.getValue(resultRow, "VIRTUALFLAG");
				String kitFlag="";				
				if(factoryName.contains("OLED"))
				{
					factoryName="OLED";
				}
				
				List<Object[]> insertArgList = new ArrayList<Object[]>();
				List<Object> insertBindList = new ArrayList<Object>();
				
				List<Object[]> resultArgList = new ArrayList<Object[]>();
				List<Object> resultBindList = new ArrayList<Object>();
				
				if(StringUtil.isEmpty(factoryName))
				{
					resultBindList.add("Y");
					resultBindList.add("E");
					resultBindList.add(eventInfo.getEventTimeKey()+" FactoryName is empty");
					resultBindList.add(productRequestName);
					resultBindList.add(consumableSpecName);
					resultBindList.add(processOperationName);
					resultArgList.add(resultBindList.toArray());
					
					GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
					GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlUpdateResult.toString(), resultArgList);
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					
					continue;
				}
				
                if(backUpMaterialList!=null && backUpMaterialList.size()>0)
                {
                	for(ListOrderedMap backUpMaterial : backUpMaterialList)
                	{
                 		if(CommonUtil.getValue(backUpMaterial, "MATERIALSPECNAME").equals(consumableSpecName)
                				&&StringUtils.isNotEmpty(subStituteGroup))
                		{
                			kitFlag="Y";
                			break;
                		}
                	}
                }
				
				insertBindList.add(factoryName);
				insertBindList.add(productRequestName);
				insertBindList.add(processOperationName);
				insertBindList.add("00001");
				insertBindList.add(consumableSpecName);	
				insertBindList.add("00001");
				insertBindList.add(quantity);
				insertBindList.add(eventInfo.getEventName());
				insertBindList.add(eventInfo.getEventTimeKey());
				insertBindList.add(eventInfo.getEventTime());	
				insertBindList.add(eventInfo.getEventUser());
				insertBindList.add(eventInfo.getEventComment());
				insertBindList.add(consumUnit);
				insertBindList.add(factoryCode);
				insertBindList.add(factoryPosition);
				insertBindList.add(subStituteGroup);
				insertBindList.add(virtualFlag);
				insertBindList.add(kitFlag);
				insertArgList.add(insertBindList.toArray());
					
				try
				{
					GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
					GenericServiceProxy.getSqlMesTemplate().updateBatch(sql.toString(), insertArgList);
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					insertCount++;
				}
				catch (Exception e)
				{
					resultBindList.add("Y");
					resultBindList.add("E");
					resultBindList.add(eventInfo.getEventTimeKey()+" "+e.getMessage());
					resultBindList.add(productRequestName);
					resultBindList.add(consumableSpecName);
					resultBindList.add(processOperationName);
					resultArgList.add(resultBindList.toArray());
						
					GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
					GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlUpdateResult.toString(), resultArgList);
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
						
					continue;
				}
				
				resultBindList.add("Y");
				resultBindList.add("S");
				resultBindList.add(eventInfo.getEventTimeKey()+" SCUUESS");
				resultBindList.add(productRequestName);
				resultBindList.add(consumableSpecName);
				resultBindList.add(processOperationName);
				resultArgList.add(resultBindList.toArray());
				
				GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
				GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlUpdateResult.toString(), resultArgList);
				GenericServiceProxy.getTxDataSourceManager().commitTransaction();
			}
			log.info("CT_ERPBOM Insert Rows Count: " + insertCount);
		}
	}
}
