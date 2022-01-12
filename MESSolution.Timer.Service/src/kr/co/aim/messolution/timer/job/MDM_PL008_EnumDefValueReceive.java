package kr.co.aim.messolution.timer.job;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class MDM_PL008_EnumDefValueReceive implements Job, InitializingBean {
	private static Log log = LogFactory.getLog(MDM_PL008_EnumDefValueReceive.class);

	@Override
	public void afterPropertiesSet() throws Exception
	{
		log.info(String.format("Job[%s] scheduler job service set completed", getClass().getSimpleName()));
	}

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
		String sendRresult = "";
		String resultMessage = "";
		String serviceName = "MES_MDMIF_PL008";
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", "MDM", "MES_MDMIF_PL008", "", "");
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT MD_ID, MD_CODE, MD_DESCRIPTION, MD_TYPE_CODE, MD_CATE_CODE, STATUS_CODE, CREATION_DATE, LAST_UPDATE_DATE, DATA_STATUS, SUBPRODUCTIONTYPE, DESCRIPTION, WERKS, PRODUCTIONTYPE, RESULTMESSAGE ");
		sql.append("FROM MES_MDMIF_PL008@OADBLINK.V3FAB.COM ");
		sql.append("WHERE DATA_STATUS != 'Y' ");

		Object[] bindArray = new Object[0];

		List<ListOrderedMap> result;

		try
		{
			GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
			result = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindArray);
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		}
		catch (FrameworkErrorSignal fe)
		{
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			result = null;
			throw new CustomException("SYS-9999", fe.getMessage());
		}

		if (result != null && result.size() > 0)
		{
			String dbLink = CommonUtil.getEnumDefValueStringByEnumName("MODDBLink");
			
			StringBuffer sqlACTVEnumValueFAB = new StringBuffer();
			sqlACTVEnumValueFAB.append("MERGE INTO ENUMDEFVALUE ");
			sqlACTVEnumValueFAB.append("    USING DUAL ");
			sqlACTVEnumValueFAB.append("        ON(ENUMNAME = ? AND ENUMVALUE = ?) ");
			sqlACTVEnumValueFAB.append("    WHEN MATCHED THEN ");
			sqlACTVEnumValueFAB.append("        UPDATE SET DESCRIPTION = ?              ");
			sqlACTVEnumValueFAB.append("    WHEN NOT MATCHED THEN ");
			sqlACTVEnumValueFAB.append("        INSERT(ENUMNAME, ENUMVALUE, DESCRIPTION)  ");
			sqlACTVEnumValueFAB.append("        VALUES(?, ?, ?) ");
			
			StringBuffer sqlACTVEnumValueMOD = new StringBuffer();
			sqlACTVEnumValueMOD.append("MERGE INTO ENUMDEFVALUE@" + dbLink);
			sqlACTVEnumValueMOD.append("    USING DUAL ");
			sqlACTVEnumValueMOD.append("        ON(ENUMNAME = ? AND ENUMVALUE = ?) ");
			sqlACTVEnumValueMOD.append("    WHEN MATCHED THEN ");
			sqlACTVEnumValueMOD.append("        UPDATE SET DESCRIPTION = ?              ");
			sqlACTVEnumValueMOD.append("    WHEN NOT MATCHED THEN ");
			sqlACTVEnumValueMOD.append("        INSERT(ENUMNAME, ENUMVALUE, DESCRIPTION)  ");
			sqlACTVEnumValueMOD.append("        VALUES(?, ?, ?) ");
			
			StringBuffer sqlRemoveEnumValueFAB = new StringBuffer();
			sqlRemoveEnumValueFAB.append("DELETE FROM ENUMDEFVALUE WHERE ENUMNAME = ? AND ENUMVALUE = ? ");
			
			StringBuffer sqlRemoveEnumValueMOD = new StringBuffer();
			sqlRemoveEnumValueMOD.append("DELETE FROM ENUMDEFVALUE@" + dbLink + " WHERE ENUMNAME = ? AND ENUMVALUE = ? ");
			
			StringBuffer sqlResult = new StringBuffer();
			sqlResult.append("UPDATE MES_MDMIF_PL008@OADBLINK.V3FAB.COM SET DATA_STATUS = ?, RESULTMESSAGE = ? WHERE MD_ID = ? AND MD_TYPE_CODE = ? ");

			for (ListOrderedMap resultRow : result)
			{
				String MD_ID = CommonUtil.getValue(resultRow, "MD_ID");
				String MD_Type_Code = CommonUtil.getValue(resultRow, "MD_TYPE_CODE");
				String MD_Desc = CommonUtil.getValue(resultRow, "MD_DESCRIPTION");
				String MD_Code = CommonUtil.getValue(resultRow, "MD_CODE");
				
				List<Object[]> updateArgListSAP = new ArrayList<Object[]>();
				List<Object[]> updateArgListMES = new ArrayList<Object[]>();
				List<Object> bindListSAP = new ArrayList<Object>();
				List<Object> bindListMES = new ArrayList<Object>();
				List<Object[]> resultArgList = new ArrayList<Object[]>();
				List<Object> resultBindList = new ArrayList<Object>();
				
				if(StringUtil.isEmpty( CommonUtil.getValue(resultRow, "PRODUCTIONTYPE")))
				{
					GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
					
					resultBindList.add("N");
					resultBindList.add("PRODUCTIONTYPE IS NULL");
					resultBindList.add(MD_ID);
					resultBindList.add(MD_Type_Code);
					resultArgList.add(resultBindList.toArray());
					
					GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
					GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResult.toString(), resultArgList);
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					
					sendRresult = "N";
					resultMessage ="PRODUCTIONTYPE IS NULL";
				}
				else
				{					
					String enumNameSAP = CommonUtil.getValue(resultRow, "PRODUCTIONTYPE") + "SapSubProductionType";
					String enumValueSAP = CommonUtil.getValue(resultRow, "SUBPRODUCTIONTYPE");
					String descriptionSAP = CommonUtil.getValue(resultRow, "DESCRIPTION");
					String statusCode = CommonUtil.getValue(resultRow, "STATUS_CODE");
					
					String enumNameMES = CommonUtil.getValue(resultRow, "PRODUCTIONTYPE") + "MESSubProductionType";
					String enumValueMES = CommonUtil.getValue(resultRow, "SUBPRODUCTIONTYPE");
					String descriptionMES = CommonUtil.getValue(resultRow, "");
					

					
					String updateSQLFAB = "";
					String updateSQLMOD = "";
					
					if(statusCode.equals("ACTV"))
					{
						updateSQLFAB = sqlACTVEnumValueFAB.toString();
						updateSQLMOD = sqlACTVEnumValueMOD.toString();
						
						//Match
						bindListSAP.add(enumNameSAP);
						bindListSAP.add(enumValueSAP);
						bindListMES.add(enumNameMES);
						bindListMES.add(enumValueMES);
						
						//Update
						bindListSAP.add(descriptionSAP);
						bindListMES.add(descriptionMES);
						
						//Insert
						bindListSAP.add(enumNameSAP);
						bindListSAP.add(enumValueSAP);
						bindListSAP.add(descriptionSAP);
						bindListMES.add(enumNameMES);
						bindListMES.add(enumValueMES);
						bindListMES.add(descriptionMES);
					}
					else
					{
						updateSQLFAB = sqlRemoveEnumValueFAB.toString();
						updateSQLMOD = sqlRemoveEnumValueMOD.toString();
						
						bindListSAP.add(enumNameSAP);
						bindListSAP.add(enumValueSAP);
						bindListMES.add(enumNameMES);
						bindListMES.add(enumValueMES);
					}
					
					updateArgListSAP.add(bindListSAP.toArray());
					updateArgListMES.add(bindListMES.toArray());
					
					try
					{
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						GenericServiceProxy.getSqlMesTemplate().updateBatch(updateSQLFAB.toString(), updateArgListSAP);
						GenericServiceProxy.getSqlMesTemplate().updateBatch(updateSQLFAB.toString(), updateArgListMES);
						//GenericServiceProxy.getSqlMesTemplate().updateBatch(updateSQLMOD.toString(), updateArgList);
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
						
						resultBindList.add("Y");
						resultBindList.add("SUCCESS");
						resultBindList.add(MD_ID);
						resultBindList.add(MD_Type_Code);
						resultArgList.add(resultBindList.toArray());
						
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResult.toString(), resultArgList);
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
						
						sendRresult = "Y";
						resultMessage = "OK";
					}
					catch(Exception e)
					{
						GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
						
						resultBindList.add("N");
						resultBindList.add(e.getMessage());
						resultBindList.add(MD_ID);
						resultBindList.add(MD_Type_Code);
						resultArgList.add(resultBindList.toArray());
						
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResult.toString(), resultArgList);
						GenericServiceProxy.getTxDataSourceManager().commitTransaction();
						
						sendRresult = "N";
						resultMessage = e.getMessage();
					}
				}				
				CommonUtil.resultSendToMDM(MD_ID, MD_Code, MD_Desc, MD_Type_Code, eventInfo, serviceName, resultMessage, sendRresult);
			}
		}
	}
}
