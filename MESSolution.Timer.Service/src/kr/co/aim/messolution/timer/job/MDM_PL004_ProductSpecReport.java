package kr.co.aim.messolution.timer.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.ObjectUtils.Null;
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

public class MDM_PL004_ProductSpecReport implements Job, InitializingBean
{	
	private static Log log = LogFactory.getLog(MDM_PL004_ProductSpecReport.class);
	
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
		log.info("MDM_PL004_ProductSpecReport start by Timer");
		StringBuffer sql = new StringBuffer(); 
		sql.append("SELECT P1.FACTORYNAME, P1.PRODUCTSPECNAME,PS.PRODUCTSPECNAME AS REMOVEFLAG, PS.DESCRIPTION, PS.CHECKSTATE, PS.ACTIVESTATE, PS.CREATETIME, PS.CREATEUSER, ");
		sql.append("    PS.CHECKOUTTIME, PS.CHECKOUTUSER, P1.PRODUCTIONTYPE, P1.PRODUCTTYPE, P1.PRODUCTQUANTITY, P1.SUBPRODUCTTYPE, P1.SUBPRODUCTUNITQUANTITY1, P1.SUBPRODUCTUNITQUANTITY2,  ");
		sql.append("    P1.PROCESSFLOWNAME, P1.PROCESSFLOWVERSION, P1.ESTIMATEDCYCLETIME, P1.MULTIPRODUCTSPECTYPE, P1.PRODUCTSPEC2NAME, P1.PRODUCTCOUNTTOXAXIS, P1.PRODUCTCOUNTTOYAXIS,  ");
		sql.append("    P1.GLASSTYPE, P1.CATEGORYTYPE, P1.ROOTPRODUCTSPEC, P1.PRODUCTSPECGROUP, P1.INNERPACKINGQUANTITY, P1.OUTERPACKINGQUANTITY, P1.PALLETQUANTITY, P1.PRODUCTCODE,  ");
		sql.append("    P1.PROCESSFLOWTYPE, P1.PRODUCTSPECVERSION, P1.PRODUCTSPEC2VERSION, P1.PRODUCTSPECTYPE ");
		sql.append("  FROM PRODUCTSPECHISTORY P1,  PRODUCTSPEC PS,  ");
		sql.append("       (  SELECT PH.PRODUCTSPECNAME, MAX (PH.TIMEKEY) TIMEKEY ");
		sql.append("            FROM PRODUCTSPECHISTORY PH ");
		sql.append("           WHERE TIMEKEY BETWEEN TO_CHAR (SYSDATE - 1, 'YYYYMMDD')||'200000'  ");
		sql.append("                             AND TO_CHAR (SYSDATE, 'YYYYMMDD') ||'200000'  ");
		sql.append("        GROUP BY PH.PRODUCTSPECNAME) P2 ");
		sql.append(" WHERE P1.PRODUCTSPECNAME = P2.PRODUCTSPECNAME  ");
		sql.append("    AND P1.TIMEKEY = P2.TIMEKEY ");
		sql.append("    AND P1.PRODUCTSPECNAME = PS.PRODUCTSPECNAME(+) ");
		sql.append("    AND P1.PRODUCTSPECVERSION = PS.PRODUCTSPECVERSION(+) ");
		sql.append("    AND P1.PRODUCTIONTYPE IN ('E','P','T')    ");
		Object[] bindArray = new Object[0];
		
		List<ListOrderedMap> mesResult; 
		
		try
		{
			GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
			mesResult = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindArray);
			log.info("Get PL004 Result Rows Count: " + mesResult.size());
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
				updateSql.append("MERGE INTO MES_MDMIF_PL004@OADBLINK.V3FAB.COM ");
				updateSql.append("    USING DUAL ");
				updateSql.append("        ON(PRODUCTSPECNAME = ? AND ORG_CODE = '500101') ");
				updateSql.append("    WHEN MATCHED THEN ");
				updateSql.append("        UPDATE SET STATUS_CODE = ?, FACTORYNAME = ?, DESCRIPTION = ?, CHECKSTATE = ?, ACTIVESTATE = ?, CREATETIME = ?, CREATEUSER = ?, CHECKOUTTIME = ?, CHECKOUTUSER = ?, PRODUCTIONTYPE = ?, PRODUCTTYPE = ?,  ");
				updateSql.append("            PRODUCTQUANTITY = ?, SUBPRODUCTTYPE =?, SUBPRODUCTUNITQUANTITY1 = ?, SUBPRODUCTUNITQUANTITY2 = ?, PROCESSFLOWNAME = ?, PROCESSFLOWVERSION = ?, ESTIMATEDCYCLETIME = ?, MULTIPRODUCTSPECTYPE = ?, PRODUCTSPEC2NAME = ?,  ");
				updateSql.append("            PRODUCTCOUNTTOXAXIS = ?, PRODUCTCOUNTTOYAXIS = ?, GLASSTYPE = ?, CATEGORYTYPE = ?, ROOTPRODUCTSPEC = ?, PRODUCTSPECGROUP = ?, INNERPACKINGQUANTITY = ?, OUTERPACKINGQUANTITY = ?, ");
				updateSql.append("            PALLETQUANTITY = ?, PRODUCTCODE = ?, PROCESSFLOWTYPE = ?, PRODUCTSPECVERSION = ?, PRODUCTSPEC2VERSION = ?, PRODUCTSPECTYPE = ?, ESBFLAG = 'N'              ");
				updateSql.append("    WHEN NOT MATCHED THEN ");
				updateSql.append("        INSERT(MD_TYPE_CODE, MD_CATE_CODE, STATUS_CODE, FACTORYNAME, DESCRIPTION, CHECKSTATE, ACTIVESTATE, CREATETIME, CREATEUSER, CHECKOUTTIME,  ");
				updateSql.append("               CHECKOUTUSER, PRODUCTIONTYPE, PRODUCTTYPE, PRODUCTQUANTITY, SUBPRODUCTTYPE, SUBPRODUCTUNITQUANTITY1, SUBPRODUCTUNITQUANTITY2, PROCESSFLOWNAME, PROCESSFLOWVERSION, ESTIMATEDCYCLETIME,  ");
				updateSql.append("               MULTIPRODUCTSPECTYPE, PRODUCTSPEC2NAME, PRODUCTCOUNTTOXAXIS, PRODUCTCOUNTTOYAXIS, GLASSTYPE, CATEGORYTYPE, ROOTPRODUCTSPEC, PRODUCTSPECGROUP, INNERPACKINGQUANTITY, OUTERPACKINGQUANTITY,  ");
				updateSql.append("               PALLETQUANTITY, PRODUCTCODE, PROCESSFLOWTYPE, PRODUCTSPECVERSION, PRODUCTSPEC2VERSION, PRODUCTSPECTYPE, ESBFLAG, PRODUCTSPECNAME, ORG_CODE) ");
				updateSql.append("        VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  ");
				updateSql.append("               ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ");
				updateSql.append("               ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,   ");
				updateSql.append("               ?, ?, ?, ?, ?, ?, 'N', ?, '500101') ");

				for (ListOrderedMap mesRow : mesResult)
				{
					
					String MDTypeCode = "PL004";
					String MDCateCode = "PRODUCTSPEC";
					String activeState = CommonUtil.getValue(mesRow, "ACTIVESTATE");
					String statusCode = "";
					if(activeState.equals("Active"))
						statusCode = "ACTV";
					else
						statusCode = "EXPR";
					String factoryName = CommonUtil.getValue(mesRow, "FACTORYNAME");
					String description = CommonUtil.getValue(mesRow, "DESCRIPTION");
					if(StringUtil.isEmpty(description))
					{
						description="����";
					}
					String checkState = CommonUtil.getValue(mesRow, "CHECKSTATE");
					String createTime = (mesRow.get("CREATETIME")==null?"":mesRow.get("CREATETIME")).toString();
					String createUser = CommonUtil.getValue(mesRow, "CREATEUSER");
					String checkOutTime =(mesRow.get("CHECKOUTTIME")==null?"":mesRow.get("CHECKOUTTIME")).toString();
					String checkOutUser = CommonUtil.getValue(mesRow, "CHECKOUTUSER");
					String productionType = CommonUtil.getValue(mesRow, "PRODUCTIONTYPE");
					String productType = CommonUtil.getValue(mesRow, "PRODUCTTYPE");
					String productQuantity =  CommonUtil.getValue(mesRow, "PRODUCTQUANTITY");
					String subProductType = CommonUtil.getValue(mesRow, "SUBPRODUCTTYPE");
					String subProductUnitQuantity1 = CommonUtil.getValue(mesRow, "SUBPRODUCTUNITQUANTITY1");
					String subProductUnitQuantity2 = CommonUtil.getValue(mesRow, "SUBPRODUCTUNITQUANTITY2");
					String processFlowName = CommonUtil.getValue(mesRow, "PROCESSFLOWNAME");
					String processFlowVersion = CommonUtil.getValue(mesRow, "PROCESSFLOWVERSION");
					String estimatedCycleTime = CommonUtil.getValue(mesRow, "ESTIMATEDCYCLETIME");
					String multiProductSpecType = CommonUtil.getValue(mesRow, "MULTIPRODUCTSPECTYPE");
					String productSpec2Name = CommonUtil.getValue(mesRow, "PRODUCTSPEC2NAME");
					String productCountToX = CommonUtil.getValue(mesRow, "PRODUCTCOUNTTOXAXIS");
					String productCountToY = CommonUtil.getValue(mesRow, "PRODUCTCOUNTTOYAXIS");
					String glassType = CommonUtil.getValue(mesRow, "GLASSTYPE");
					String categoryType = CommonUtil.getValue(mesRow, "CATEGORYTYPE");
					String rootProductSpec = CommonUtil.getValue(mesRow, "ROOTPRODUCTSPEC");
					String productSpecGroup = CommonUtil.getValue(mesRow, "PRODUCTSPECGROUP");
					String innerPackQty = CommonUtil.getValue(mesRow, "INNERPACKINGQUANTITY");
					String outerPackQty = CommonUtil.getValue(mesRow, "OUTERPACKINGQUANTITY");
					String palletQty = CommonUtil.getValue(mesRow, "PALLETQUANTITY");
					String productCode = CommonUtil.getValue(mesRow, "PRODUCTCODE");
					String processFlowType = CommonUtil.getValue(mesRow, "PROCESSFLOWTYPE");
					String productSpecVersion = CommonUtil.getValue(mesRow, "PRODUCTSPECVERSION");
					String productSpec2Version = CommonUtil.getValue(mesRow, "PRODUCTSPEC2VERSION");
					String productSpecType = CommonUtil.getValue(mesRow, "PRODUCTSPECTYPE");
					String productSpecName = CommonUtil.getValue(mesRow, "PRODUCTSPECNAME");
					//REMOVEFLAG
					if(StringUtils.isEmpty(CommonUtil.getValue(mesRow, "REMOVEFLAG")))
					{
						description="Delete";
						checkState="CheckedIn";
						statusCode = "EXPR";
						activeState="NotActive";
					}
					
					List<Object> bindList = new ArrayList<Object>();
					//Match
					bindList.add(productSpecName);
					
					//Update
					bindList.add(statusCode);
					bindList.add(factoryName);
					bindList.add(description);
					bindList.add(checkState);
					bindList.add(activeState);
					bindList.add(createTime);
					bindList.add(createUser);
					bindList.add(checkOutTime);
					bindList.add(checkOutUser);
					bindList.add(productionType);
					bindList.add(productType);
					bindList.add(productQuantity);
					bindList.add(subProductType);
					bindList.add(subProductUnitQuantity1);
					bindList.add(subProductUnitQuantity2);
					bindList.add(processFlowName);
					bindList.add(processFlowVersion);
					bindList.add(estimatedCycleTime);
					bindList.add(multiProductSpecType);
					bindList.add(productSpec2Name);
					bindList.add(productCountToX);
					bindList.add(productCountToY);
					bindList.add(glassType);
					bindList.add(categoryType);
					bindList.add(rootProductSpec);
					bindList.add(productSpecGroup);
					bindList.add(innerPackQty);
					bindList.add(outerPackQty);
					bindList.add(palletQty);
					bindList.add(productCode);
					bindList.add(processFlowType);
					bindList.add(productSpecVersion);
					bindList.add(productSpec2Version);
					bindList.add(productSpecType);					
					
					//Insert
					bindList.add(MDTypeCode);
					bindList.add(MDCateCode);
					bindList.add(statusCode);
					bindList.add(factoryName);
					bindList.add(description);
					bindList.add(checkState);
					bindList.add(activeState);
					bindList.add(createTime);
					bindList.add(createUser);
					bindList.add(checkOutTime);
					bindList.add(checkOutUser);
					bindList.add(productionType);
					bindList.add(productType);
					bindList.add(productQuantity);
					bindList.add(subProductType);
					bindList.add(subProductUnitQuantity1);
					bindList.add(subProductUnitQuantity2);
					bindList.add(processFlowName);
					bindList.add(processFlowVersion);
					bindList.add(estimatedCycleTime);
					bindList.add(multiProductSpecType);
					bindList.add(productSpec2Name);
					bindList.add(productCountToX);
					bindList.add(productCountToY);
					bindList.add(glassType);
					bindList.add(categoryType);
					bindList.add(rootProductSpec);
					bindList.add(productSpecGroup);
					bindList.add(innerPackQty);
					bindList.add(outerPackQty);
					bindList.add(palletQty);
					bindList.add(productCode);
					bindList.add(processFlowType);
					bindList.add(productSpecVersion);
					bindList.add(productSpec2Version);
					bindList.add(productSpecType);
					bindList.add(productSpecName);
					
					updateArgList.add(bindList.toArray());
				}	

				//Update MDM
				if(updateArgList != null && updateArgList.size() > 0)
				{
					try
					{
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						GenericServiceProxy.getSqlMesTemplate().updateBatch(updateSql.toString(), updateArgList);
						log.info("PL004 Insert or Update Rows Count: "+updateArgList.size());
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					}
					catch(Exception e)
					{
						GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
					}
				}
				else
					log.info("Update data not exist to MDM (MDM_PL004_ProductSpecReport)");
			}
			catch(Exception e)
			{
				log.error(e.getMessage());
			}
			finally
			{
				log.info("MDM_PL004_ProductSpecReport end");
			}
		}
	}
}
