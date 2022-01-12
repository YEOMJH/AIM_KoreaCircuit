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

public class MDM_PL006_ReasonCodeReceive implements Job, InitializingBean {
	private static Log log = LogFactory.getLog(MDM_PL006_ReasonCodeReceive.class);

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
		String serviceName = "MES_MDMIF_PL006";
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", "MDM", "MES_MDMIF_PL006", "", "");
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT MD_ID, MD_CODE, MD_DESCRIPTION, MD_TYPE_CODE, MD_CATE_CODE, STATUS_CODE, CREATION_DATE, LAST_UPDATE_DATE, DATA_STATUS, FACTORYNAME, REASONCODETYPE, REASONCODE, SUPERREASONCODE, LEVELNO, AVAILABILITY, SEQ, MACHINENAME, ORG_CODE, ORG_NAME, RESULTMESSAGE ");
		sql.append("FROM MES_MDMIF_PL006@OADBLINK.V3FAB.COM  ");
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
			
			StringBuffer sqlType = new StringBuffer();
			sqlType.append("SELECT REASONCODETYPENAME, DESCRIPTION FROM REASONCODETYPE ");
			
			List<ListOrderedMap> reasonCodeTypeList = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlType.toString(), bindArray);
			
			StringBuffer sqlACTVReasonCodeFAB = new StringBuffer();
			sqlACTVReasonCodeFAB.append("MERGE INTO REASONCODE ");
			sqlACTVReasonCodeFAB.append("    USING DUAL ");
			sqlACTVReasonCodeFAB.append("        ON(FACTORYNAME = ? AND REASONCODETYPE = ? AND REASONCODE = ?) ");
			sqlACTVReasonCodeFAB.append("    WHEN MATCHED THEN ");
			sqlACTVReasonCodeFAB.append("        UPDATE SET DESCRIPTION = ?, SUPERREASONCODE = ?, LEVELNO = ?, AVAILABILITY = ?, SEQ = ?, MACHINENAME = ?               ");
			sqlACTVReasonCodeFAB.append("    WHEN NOT MATCHED THEN ");
			sqlACTVReasonCodeFAB.append("        INSERT(FACTORYNAME, REASONCODETYPE, REASONCODE, DESCRIPTION, SUPERREASONCODE, LEVELNO, AVAILABILITY, SEQ, MACHINENAME)  ");
			sqlACTVReasonCodeFAB.append("        VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?) ");
			
			StringBuffer sqlACTVReasonCodeMOD = new StringBuffer();
			sqlACTVReasonCodeMOD.append("MERGE INTO REASONCODE@" + dbLink);
			sqlACTVReasonCodeMOD.append("    USING DUAL ");
			sqlACTVReasonCodeMOD.append("        ON(FACTORYNAME = ? AND REASONCODETYPE = ? AND REASONCODE = ?) ");
			sqlACTVReasonCodeMOD.append("    WHEN MATCHED THEN ");
			sqlACTVReasonCodeMOD.append("        UPDATE SET DESCRIPTION = ?, SUPERREASONCODE = ?, LEVELNO = ?, AVAILABILITY = ?, SEQ = ?, MACHINENAME = ?               ");
			sqlACTVReasonCodeMOD.append("    WHEN NOT MATCHED THEN ");
			sqlACTVReasonCodeMOD.append("        INSERT(FACTORYNAME, REASONCODETYPE, REASONCODE, DESCRIPTION, SUPERREASONCODE, LEVELNO, AVAILABILITY, SEQ, MACHINENAME)  ");
			sqlACTVReasonCodeMOD.append("        VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?) ");
			
			StringBuffer sqlRemoveReasonCodeFAB = new StringBuffer();
			sqlRemoveReasonCodeFAB.append("DELETE FROM REASONCODE WHERE FACTORYNAME = ? AND REASONCODETYPE = ? AND REASONCODE = ? ");
			
			StringBuffer sqlRemoveReasonCodeMOD = new StringBuffer();
			sqlRemoveReasonCodeMOD.append("DELETE FROM REASONCODE@" + dbLink + " WHERE FACTORYNAME = ? AND REASONCODETYPE = ? AND REASONCODE = ? ");
			
			StringBuffer sqlResult = new StringBuffer();
			sqlResult.append("UPDATE MES_MDMIF_PL006@OADBLINK.V3FAB.COM SET DATA_STATUS = ?, RESULTMESSAGE = ? WHERE MD_ID = ? AND MD_TYPE_CODE = ? ");

			for (ListOrderedMap resultRow : result)
			{
				String MD_ID = CommonUtil.getValue(resultRow, "MD_ID");
				String MD_Type_Code = CommonUtil.getValue(resultRow, "MD_TYPE_CODE");
				String MD_Code = CommonUtil.getValue(resultRow, "MD_CODE");
				String MD_Desc = CommonUtil.getValue(resultRow, "MD_DESCRIPTION");
				String statusCode = CommonUtil.getValue(resultRow, "STATUS_CODE");
				String org_Code = CommonUtil.getValue(resultRow, "ORG_CODE");
				String factoryName = CommonUtil.getValue(resultRow, "FACTORYNAME");
				String reasonCodeType = CommonUtil.getValue(resultRow, "REASONCODETYPE");
				String reasonCode = CommonUtil.getValue(resultRow, "REASONCODE");
				String description = CommonUtil.getValue(resultRow, "DESCRIPTION");
				String superReasonCode = CommonUtil.getValue(resultRow, "SUPERREASONCODE");
				String levelNo = CommonUtil.getValue(resultRow, "LEVELNO");
				String availability = CommonUtil.getValue(resultRow, "AVAILABILITY");
				String seq = CommonUtil.getValue(resultRow, "SEQ");
				String machineName = CommonUtil.getValue(resultRow, "MACHINENAME");
				
				List<Object[]> updateArgList = new ArrayList<Object[]>();
				List<Object> bindList = new ArrayList<Object>();
				List<Object[]> resultArgList = new ArrayList<Object[]>();
				List<Object> resultBindList = new ArrayList<Object>();
				
				boolean typeFlag = false;
				
				for (ListOrderedMap typeRow : reasonCodeTypeList)
				{
					String reasonCodeTypeName = CommonUtil.getValue(typeRow, "REASONCODETYPENAME");
					
					if(StringUtil.equals(reasonCodeTypeName, reasonCodeType))
					{
						typeFlag = true;
						break;
					}
				}
				
				String updateSQL;
				
				if(typeFlag)
				{
					if(statusCode.equals("ACTV"))
					{
						if(StringUtil.equals(org_Code, "500101"))
						{
							updateSQL = sqlACTVReasonCodeFAB.toString();
						}
						else
						{
							updateSQL = sqlACTVReasonCodeMOD.toString();
						}

						//Match
						bindList.add(factoryName);
						bindList.add(reasonCodeType);
						bindList.add(reasonCode);
						
						//Update
						bindList.add(description);		
						bindList.add(superReasonCode);	
						bindList.add(levelNo);	
						bindList.add(availability);	
						bindList.add(seq);	
						bindList.add(machineName);	
						
						//Insert
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
					else
					{
						if(StringUtil.equals(org_Code, "500101"))
						{
							updateSQL = sqlRemoveReasonCodeFAB.toString();
						}
						else
						{
							updateSQL = sqlRemoveReasonCodeMOD.toString();
						}
						
						bindList.add(factoryName);
						bindList.add(reasonCode);
						bindList.add(reasonCodeType);
						updateArgList.add(bindList.toArray());
					}
					
					try
					{
						GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
						GenericServiceProxy.getSqlMesTemplate().updateBatch(updateSQL, updateArgList);
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
				else
				{
					resultBindList.add("N");
					resultBindList.add("There is no ReasonCodeType Data in MES DB");
					resultBindList.add(MD_ID);
					resultBindList.add(MD_Type_Code);
					resultArgList.add(resultBindList.toArray());
					
					GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
					GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResult.toString(), resultArgList);
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					
					sendRresult = "N";
					resultMessage = "There is no ReasonCodeType Data in MES DB";
				}
				
				CommonUtil.resultSendToMDM(MD_ID, MD_Code, MD_Desc, MD_Type_Code, eventInfo, serviceName, resultMessage, sendRresult);
			}
		}
	}
}
