package kr.co.aim.messolution.timer.job;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.InitializingBean;

import kr.co.aim.messolution.extended.object.management.data.DefectCode;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class MDM_PL005_DefectCodeReceive implements Job, InitializingBean {
	private static Log log = LogFactory.getLog(MDM_PL005_DefectCodeReceive.class);

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
		Object[] bindArray = new Object[0];
		
		String result = "";
		String resultMessage = "";
		String serviceName = "MES_MDMIF_PL005";
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", "MDM", "MES_MDMIF_PL005", "", "");
		
		//RS
		StringBuffer sqlRS = new StringBuffer();
		sqlRS.append("SELECT MD_ID, MD_CODE, MD_DESCRIPTION, MD_TYPE_CODE, MD_CATE_CODE, STATUS_CODE, CREATION_DATE, LAST_UPDATE_DATE, FACTORYNAME_RS, PRODUCTSPECNAME_RS,  ");
		sqlRS.append("    PROCESSOPERATIONNAME_RS, DEFECTCODE_RS, SEQ_RS, REFERENCEQUANTITY_RS, EVENTNAME_RS, EVENTUSER_RS, EVENTTIME_RS, EVENTCOMMENT_RS, DESCRIPTION_RS,  ");
		sqlRS.append("    REVIEWPROCESSOPERATIONNAME_RS, PEP_RS, DEPARTMENT_RS, EXCEPTIONCODE_RS, NGQUANTITY_RS, MANAGETYPE_RS, DEFECTJUDGE_RS, POSSIBLEJUDGE_RS, DEFECTSIZE_RS,  ");
		sqlRS.append("    ABNORMALPOSSIBLEJUDGE_RS, ACTIVESTATE_RS, ORG_CODE_RS, ORG_NAME_RS, DATA_STATUS_RS, RESULTMESSAGE ");
		sqlRS.append("FROM MES_MDMIF_PL005@OADBLINK.V3FAB.COM  ");
		sqlRS.append("WHERE MD_CATE_CODE = 'REVIEWDEFECTCODE' ");
		sqlRS.append("    AND DATA_STATUS_RS != 'Y' ");

		List<ListOrderedMap> resultRS;

		try
		{
			GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
			resultRS = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlRS.toString(), bindArray);
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		}
		catch (FrameworkErrorSignal fe)
		{
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			resultRS = null;
		}
		
		if (resultRS != null && resultRS.size() > 0)
		{
			StringBuffer sqlMergeRS = new StringBuffer();
			sqlMergeRS.append("MERGE INTO CT_REVIEWDEFECTCODE ");
			sqlMergeRS.append("    USING DUAL ");
			sqlMergeRS.append("        ON(FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PROCESSOPERATIONNAME = ? AND DEFECTCODE = ? AND REVIEWPROCESSOPERATIONNAME = ?) ");
			sqlMergeRS.append("    WHEN MATCHED THEN ");
			sqlMergeRS.append("        UPDATE SET SEQ = ?, REFERENCEQUANTITY = ?, EVENTNAME = ?, EVENTUSER = ?, EVENTTIME = ?, EVENTCOMMENT = ?, DESCRIPTION = ?, PEP = ?,  ");
			sqlMergeRS.append("            DEPARTMENT = ?, EXCEPTIONCODE = ?, NGQUANTITY = ?, MANAGETYPE = ?, DEFECTJUDGE = ?, POSSIBLEJUDGE = ?, DEFECTSIZE = ?, ABNORMALPOSSIBLEJUDGE = ?                 ");
			sqlMergeRS.append("    WHEN NOT MATCHED THEN ");
			sqlMergeRS.append("        INSERT(FACTORYNAME, PRODUCTSPECNAME, PROCESSOPERATIONNAME, DEFECTCODE, REVIEWPROCESSOPERATIONNAME, SEQ, REFERENCEQUANTITY, EVENTNAME, EVENTUSER, EVENTTIME,  ");
			sqlMergeRS.append("               EVENTCOMMENT, DESCRIPTION, PEP, DEPARTMENT, EXCEPTIONCODE, NGQUANTITY, MANAGETYPE, DEFECTJUDGE, POSSIBLEJUDGE, DEFECTSIZE,  ");
			sqlMergeRS.append("               ABNORMALPOSSIBLEJUDGE)  ");
			sqlMergeRS.append("        VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  ");
			sqlMergeRS.append("               ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ");
			sqlMergeRS.append("               ?) ");
			
			StringBuffer sqlRemoveRS = new StringBuffer();
			sqlRemoveRS.append("DELETE FROM CT_REVIEWDEFECTCODE WHERE FACTORYNAME = ? AND PRODUCTSPECNAME = ? AND PROCESSOPERATIONNAME = ? AND DEFECTCODE = ? AND REVIEWPROCESSOPERATIONNAME = ? ");
			
			StringBuffer sqlResultRS = new StringBuffer();
			sqlResultRS.append("UPDATE MES_MDMIF_PL005@OADBLINK.V3FAB.COM SET DATA_STATUS_RS = ?, RESULTMESSAGE = ? WHERE MD_ID = ? AND MD_TYPE_CODE = ? ");

			for (ListOrderedMap resultRowRS : resultRS)
			{
				String MD_ID = CommonUtil.getValue(resultRowRS, "MD_ID");
				String MD_Code = CommonUtil.getValue(resultRowRS, "MD_CODE");
				String MD_Type_Code = CommonUtil.getValue(resultRowRS, "MD_TYPE_CODE");
				String MD_Desc = CommonUtil.getValue(resultRowRS, "MD_DESCRIPTION");
				String factoryName = CommonUtil.getValue(resultRowRS, "FACTORYNAME_RS");
				String productSpecName = CommonUtil.getValue(resultRowRS, "PRODUCTSPECNAME_RS");
				String processOperationName = CommonUtil.getValue(resultRowRS, "PROCESSOPERATIONNAME_RS");
				String defectCode = CommonUtil.getValue(resultRowRS, "DEFECTCODE_RS");
				String seq = "";
				if(StringUtils.isNotEmpty(CommonUtil.getValue(resultRowRS, "SEQ_RS")))
				{
					 seq = CommonUtil.getValue(resultRowRS, "SEQ_RS");
				}
				else
				{
					 seq = "0";
				}
				String referenceQty = CommonUtil.getValue(resultRowRS, "REFERENCEQUANTITY_RS");
				String eventName = CommonUtil.getValue(resultRowRS, "EVENTNAME_RS");
				String eventUser = CommonUtil.getValue(resultRowRS, "EVENTUSER_RS");
				Timestamp eventTime = TimeStampUtil.getCurrentTimestamp();
				String eventComment = CommonUtil.getValue(resultRowRS, "EVENTCOMMENT_RS");
				String description = CommonUtil.getValue(resultRowRS, "DESCRIPTION_RS");
				String RSOperationName = CommonUtil.getValue(resultRowRS, "REVIEWPROCESSOPERATIONNAME_RS");
				String pep = CommonUtil.getValue(resultRowRS, "PEP_RS");
				String department = CommonUtil.getValue(resultRowRS, "DEPARTMENT_RS");
				String exceptionCode = CommonUtil.getValue(resultRowRS, "EXCEPTIONCODE_RS");
				String NGQty = CommonUtil.getValue(resultRowRS, "NGQUANTITY_RS");
				String manageType = CommonUtil.getValue(resultRowRS, "MANAGETYPE_RS");
				String defectJudge = CommonUtil.getValue(resultRowRS, "DEFECTJUDGE_RS");
				String possibleJudge = CommonUtil.getValue(resultRowRS, "POSSIBLEJUDGE_RS");
				String defectSize = CommonUtil.getValue(resultRowRS, "DEFECTSIZE_RS");
				String abnormalPossibleJudge = CommonUtil.getValue(resultRowRS, "ABNORMALPOSSIBLEJUDGE_RS");
				String activeState = CommonUtil.getValue(resultRowRS, "ACTIVESTATE_RS");
				
				List<Object[]> updateArgList = new ArrayList<Object[]>();
				List<Object> bindList = new ArrayList<Object>();
				List<Object[]> resultArgList = new ArrayList<Object[]>();
				List<Object> resultBindList = new ArrayList<Object>();
				
				String updateSQL = "";
				
				if(activeState.equals("Active"))
				{
					updateSQL = sqlMergeRS.toString();

					//Match
					bindList.add(factoryName);
					bindList.add(productSpecName);
					bindList.add(processOperationName);
					bindList.add(defectCode);
					bindList.add(RSOperationName);
					
					//Update
					bindList.add(seq);		
					bindList.add(referenceQty);	
					bindList.add(eventName);	
					bindList.add(eventUser);	
					bindList.add(eventTime);	
					bindList.add(eventComment);	
					bindList.add(description);	
					bindList.add(pep);	
					bindList.add(department);	
					bindList.add(exceptionCode);	
					bindList.add(NGQty);	
					bindList.add(manageType);	
					bindList.add(defectJudge);	
					bindList.add(possibleJudge);	
					bindList.add(defectSize);	
					bindList.add(abnormalPossibleJudge);
					
					//Insert
					bindList.add(factoryName);
					bindList.add(productSpecName);
					bindList.add(processOperationName);
					bindList.add(defectCode);
					bindList.add(RSOperationName);
					bindList.add(seq);		
					bindList.add(referenceQty);	
					bindList.add(eventName);	
					bindList.add(eventUser);	
					bindList.add(eventTime);	
					bindList.add(eventComment);	
					bindList.add(description);	
					bindList.add(pep);	
					bindList.add(department);	
					bindList.add(exceptionCode);	
					bindList.add(NGQty);	
					bindList.add(manageType);	
					bindList.add(defectJudge);	
					bindList.add(possibleJudge);	
					bindList.add(defectSize);	
					bindList.add(abnormalPossibleJudge);
					updateArgList.add(bindList.toArray());
				}
				else
				{
					updateSQL = sqlRemoveRS.toString();
					
					bindList.add(factoryName);
					bindList.add(productSpecName);
					bindList.add(processOperationName);
					bindList.add(defectCode);
					bindList.add(RSOperationName);
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
					GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResultRS.toString(), resultArgList);
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					
					result = "Y";
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
					GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResultRS.toString(), resultArgList);
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					
					result = "N";
					resultMessage = e.getMessage();
				}
				
				CommonUtil.resultSendToMDM(MD_ID, MD_Code, MD_Desc, MD_Type_Code, eventInfo, serviceName, resultMessage, result);
			}
		}
		
		//LOI
		StringBuffer sqlLOI = new StringBuffer();
		sqlLOI.append("SELECT MD_ID, MD_CODE, MD_DESCRIPTION, MD_TYPE_CODE, MD_CATE_CODE, STATUS_CODE, CREATION_DATE, LAST_UPDATE_DATE, DEFECTTYPE_LOI, DEFECTCODE_LOI, DESCRIPTION_LOI, GRADE_LOI,  ");
		sqlLOI.append("    ACTIVESTATE_LOI, ORG_CODE_LOI, ORG_NAME_LOI, DATA_STATUS_LOI, RESULTMESSAGE ");
		sqlLOI.append("FROM MES_MDMIF_PL005@OADBLINK.V3FAB.COM  ");
		sqlLOI.append("WHERE MD_CATE_CODE = 'LOIDEFECTCODE' ");
		sqlLOI.append("    AND DATA_STATUS_LOI != 'Y' ");

		List<ListOrderedMap> resultLOI;

		try
		{
			GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
			resultLOI = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlLOI.toString(), bindArray);
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		}
		catch (FrameworkErrorSignal fe)
		{
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			resultLOI = null;
		}
		
		if (resultLOI != null && resultLOI.size() > 0)
		{
			StringBuffer sqlMergeLOI = new StringBuffer();
			sqlMergeLOI.append("MERGE INTO CT_LOIDEFECTCODE ");
			sqlMergeLOI.append("    USING DUAL ");
			sqlMergeLOI.append("        ON(DEFECTTYPE = ? AND DEFECTCODE = ?) ");
			sqlMergeLOI.append("    WHEN MATCHED THEN ");
			sqlMergeLOI.append("        UPDATE SET DESCRIPTION = ?, GRADE = ?           ");
			sqlMergeLOI.append("    WHEN NOT MATCHED THEN ");
			sqlMergeLOI.append("        INSERT(DEFECTTYPE, DEF"
					+ "ECTCODE, DESCRIPTION, GRADE)  ");
			sqlMergeLOI.append("        VALUES(?, ?, ?, ?) ");
			
			StringBuffer sqlRemoveLOI = new StringBuffer();
			sqlRemoveLOI.append("DELETE FROM CT_LOIDEFECTCODE WHERE DEFECTTYPE = ? AND DEFECTCODE = ? ");
			
			StringBuffer sqlResultLOI = new StringBuffer();
			sqlResultLOI.append("UPDATE MES_MDMIF_PL005@OADBLINK.V3FAB.COM SET DATA_STATUS_LOI = ?, RESULTMESSAGE = ? WHERE MD_ID = ? AND MD_TYPE_CODE = ? ");


			for (ListOrderedMap resultRowLOI : resultLOI)
			{
				String MD_ID = CommonUtil.getValue(resultRowLOI, "MD_ID");
				String MD_Code = CommonUtil.getValue(resultRowLOI, "MD_CODE");
				String MD_Type_Code = CommonUtil.getValue(resultRowLOI, "MD_TYPE_CODE");
				String MD_Desc = CommonUtil.getValue(resultRowLOI, "MD_DESCRIPTION");
				String defectType = CommonUtil.getValue(resultRowLOI, "DEFECTTYPE_LOI");
				String defectCode = CommonUtil.getValue(resultRowLOI, "DEFECTCODE_LOI");
				String description = CommonUtil.getValue(resultRowLOI, "DESCRIPTION_LOI");
				String grade = CommonUtil.getValue(resultRowLOI, "GRADE_LOI");
				String activeState = CommonUtil.getValue(resultRowLOI, "ACTIVESTATE_LOI");
				
				List<Object[]> updateArgList = new ArrayList<Object[]>();
				List<Object> bindList = new ArrayList<Object>();
				List<Object[]> resultArgList = new ArrayList<Object[]>();
				List<Object> resultBindList = new ArrayList<Object>();
				
				String updateSQL = "";
				
				if(activeState.equals("Active"))
				{
					updateSQL = sqlMergeLOI.toString();

					//Match
					bindList.add(defectType);
					bindList.add(defectCode);
					
					//Update
					bindList.add(description);		
					bindList.add(grade);	
					
					//Insert
					bindList.add(defectType);
					bindList.add(defectCode);
					bindList.add(description);		
					bindList.add(grade);
					updateArgList.add(bindList.toArray());
				}
				else
				{
					updateSQL = sqlRemoveLOI.toString();
					
					bindList.add(defectType);
					bindList.add(defectCode);
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
					GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResultLOI.toString(), resultArgList);
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					
					result = "Y";
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
					GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResultLOI.toString(), resultArgList);
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					
					result = "N";
					resultMessage = e.getMessage();
				}
				
				CommonUtil.resultSendToMDM(MD_ID, MD_Code, MD_Desc, MD_Type_Code, eventInfo, serviceName, resultMessage, result);
			}
		}
		
		//MVI
		StringBuffer sqlMVI = new StringBuffer();
		sqlMVI.append("SELECT MD_ID, MD_CODE, MD_DESCRIPTION, MD_TYPE_CODE, MD_CATE_CODE, STATUS_CODE, CREATION_DATE, LAST_UPDATE_DATE, PRODUCTSPECNAME_MVI, DEFECTCODE_MVI, DESCRIPTION_MVI,  ");
		sqlMVI.append("    SUPERDEFECTCODE_MVI, LEVELNO_MVI, CONDITIONFLAG_MVI, PANELGRADE_MVI, ACTIVESTATE_MVI, ORG_CODE_MVI, ORG_NAME_MVI, DATA_STATUS_MVI, RESULTMESSAGE ");
		sqlMVI.append("FROM MES_MDMIF_PL005@OADBLINK.V3FAB.COM  ");
		sqlMVI.append("WHERE MD_CATE_CODE = 'MVIDEFECTCODE' ");
		sqlMVI.append("    AND DATA_STATUS_MVI != 'Y' ");

		List<ListOrderedMap> resultMVI;

		try
		{
			GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
			resultMVI = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlMVI.toString(), bindArray);
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		}
		catch (FrameworkErrorSignal fe)
		{
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			resultMVI = null;
		}
		
		if (resultMVI != null && resultMVI.size() > 0)
		{
			StringBuffer sqlMergeMVI = new StringBuffer();
			sqlMergeMVI.append("MERGE INTO CT_MVIDEFECTCODE ");
			sqlMergeMVI.append("    USING DUAL ");
			sqlMergeMVI.append("        ON(PRODUCTSPECNAME = ? AND DEFECTCODE = ? AND SUPERDEFECTCODE = ?) ");
			sqlMergeMVI.append("    WHEN MATCHED THEN ");
			sqlMergeMVI.append("        UPDATE SET DESCRIPTION = ?, LEVELNO = ?, CONDITIONFLAG = ?, PANELGRADE = ?               ");
			sqlMergeMVI.append("    WHEN NOT MATCHED THEN ");
			sqlMergeMVI.append("        INSERT(PRODUCTSPECNAME, DEFECTCODE, SUPERDEFECTCODE, DESCRIPTION, LEVELNO, CONDITIONFLAG, PANELGRADE)  ");
			sqlMergeMVI.append("        VALUES(?, ?, ?, ?, ?, ?, ?) ");
			
			StringBuffer sqlRemoveMVI = new StringBuffer();
			sqlRemoveMVI.append("DELETE FROM CT_MVIDEFECTCODE WHERE PRODUCTSPECNAME = ? AND DEFECTCODE = ? AND SUPERDEFECTCODE = ? ");

			StringBuffer sqlResultMVI = new StringBuffer();
			sqlResultMVI.append("UPDATE MES_MDMIF_PL005@OADBLINK.V3FAB.COM SET DATA_STATUS_MVI = ?, RESULTMESSAGE = ? WHERE MD_ID = ? AND MD_TYPE_CODE = ? ");

			for (ListOrderedMap resultRowMVI : resultMVI)
			{
				String MD_ID = CommonUtil.getValue(resultRowMVI, "MD_ID");
				String MD_Code = CommonUtil.getValue(resultRowMVI, "MD_CODE");
				String MD_Type_Code = CommonUtil.getValue(resultRowMVI, "MD_TYPE_CODE");
				String MD_Desc = CommonUtil.getValue(resultRowMVI, "MD_DESCRIPTION");
				String productSpecName = CommonUtil.getValue(resultRowMVI, "PRODUCTSPECNAME_MVI");
				String defectCode = CommonUtil.getValue(resultRowMVI, "DEFECTCODE_MVI");
				String description = CommonUtil.getValue(resultRowMVI, "DESCRIPTION_MVI");
				String superDefectCode = CommonUtil.getValue(resultRowMVI, "SUPERDEFECTCODE_MVI");
				String levelNo = CommonUtil.getValue(resultRowMVI, "LEVELNO_MVI");
				String conditionFlag = CommonUtil.getValue(resultRowMVI, "CONDITIONFLAG_MVI");
				String panelGrade = CommonUtil.getValue(resultRowMVI, "PANELGRADE_MVI");
				String activeState = CommonUtil.getValue(resultRowMVI, "ACTIVESTATE_MVI");
				
				List<Object[]> updateArgList = new ArrayList<Object[]>();
				List<Object> bindList = new ArrayList<Object>();
				List<Object[]> resultArgList = new ArrayList<Object[]>();
				List<Object> resultBindList = new ArrayList<Object>();
				
				String updateSQL = "";
				
				if(activeState.equals("Active"))
				{
					updateSQL = sqlMergeMVI.toString();

					//Match
					bindList.add(productSpecName);
					bindList.add(defectCode);
					bindList.add(superDefectCode);
					
					//Update
					bindList.add(description);		
					bindList.add(levelNo);	
					bindList.add(conditionFlag);	
					bindList.add(panelGrade);	
					
					//Insert
					bindList.add(productSpecName);
					bindList.add(defectCode);
					bindList.add(superDefectCode);
					bindList.add(description);		
					bindList.add(levelNo);	
					bindList.add(conditionFlag);	
					bindList.add(panelGrade);
					updateArgList.add(bindList.toArray());
				}
				else
				{
					updateSQL = sqlRemoveMVI.toString();
					
					bindList.add(productSpecName);
					bindList.add(defectCode);
					bindList.add(superDefectCode);
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
					GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResultMVI.toString(), resultArgList);
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					
					result = "Y";
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
					GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResultMVI.toString(), resultArgList);
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					
					result = "N";
					resultMessage = e.getMessage();
				}
				
				CommonUtil.resultSendToMDM(MD_ID, MD_Code, MD_Desc, MD_Type_Code, eventInfo, serviceName, resultMessage, result);
			}
		}
		
		//JND
		StringBuffer sqlJND = new StringBuffer();
		sqlJND.append("SELECT MD_ID, MD_CODE, MD_DESCRIPTION, MD_TYPE_CODE, MD_CATE_CODE, STATUS_CODE, CREATION_DATE, LAST_UPDATE_DATE, PRODUCTSPECNAME_JND, DEFECTCODE_JND,  ");
		sqlJND.append("    DEFECTCODEDESCRIPTION_JND,     SUPERDEFECTCODE_JND, SUPERDEFECTCODEDESCRIPTION_JND, PATTERN_JND, JNDNAME_JND, SIGN_JND, PANELGRADE_JND, ACTIVESTATE_JND, ORG_CODE_JND,  ");
		sqlJND.append("    ORG_NAME_JND, DATA_STATUS_JND, RESULTMESSAGE ");
		sqlJND.append("FROM MES_MDMIF_PL005@OADBLINK.V3FAB.COM  ");
		sqlJND.append("WHERE MD_CATE_CODE = 'MVIJNDDEFECTCODE' ");
		sqlJND.append("    AND DATA_STATUS_JND != 'Y' ");

		List<ListOrderedMap> resultJND;

		try
		{
			GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
			resultJND = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlJND.toString(), bindArray);
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		}
		catch (FrameworkErrorSignal fe)
		{
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			resultJND = null;
		}
		
		if (resultJND != null && resultJND.size() > 0)
		{
			StringBuffer sqlMergeJND = new StringBuffer();
			sqlMergeJND.append("MERGE INTO CT_MVIJNDDEFECTCODE ");
			sqlMergeJND.append("    USING DUAL ");
			sqlMergeJND.append("        ON(PRODUCTSPECNAME = ? AND DEFECTCODE = ? AND SUPERDEFECTCODE = ? AND PATTERN = ? AND JNDNAME = ? AND PANELGRADE =?) ");
			sqlMergeJND.append("    WHEN MATCHED THEN ");
			sqlMergeJND.append("        UPDATE SET DEFECTCODEDESCRIPTION = ?, SUPERDEFECTCODEDESCRIPTION = ?, SIGN = ?             ");
			sqlMergeJND.append("    WHEN NOT MATCHED THEN ");
			sqlMergeJND.append("        INSERT(PRODUCTSPECNAME, DEFECTCODE, SUPERDEFECTCODE, PATTERN, JNDNAME, PANELGRADE, DEFECTCODEDESCRIPTION, SUPERDEFECTCODEDESCRIPTION, SIGN)  ");
			sqlMergeJND.append("        VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?) ");
			
			StringBuffer sqlRemoveJND = new StringBuffer();
			sqlRemoveJND.append("DELETE FROM CT_MVIJNDDEFECTCODE WHERE PRODUCTSPECNAME = ? AND DEFECTCODE = ? AND SUPERDEFECTCODE = ? AND PATTERN = ? AND JNDNAME = ? AND PANELGRADE =? ");

			StringBuffer sqlResultJND = new StringBuffer();
			sqlResultJND.append("UPDATE MES_MDMIF_PL005@OADBLINK.V3FAB.COM SET DATA_STATUS_JND = ?, RESULTMESSAGE = ? WHERE MD_ID = ? AND MD_TYPE_CODE = ? ");

			for (ListOrderedMap resultRowJND : resultJND)
			{
				String MD_ID = CommonUtil.getValue(resultRowJND, "MD_ID");
				String MD_Code = CommonUtil.getValue(resultRowJND, "MD_CODE");
				String MD_Desc = CommonUtil.getValue(resultRowJND, "MD_DESCRIPTION");
				String MD_Type_Code = CommonUtil.getValue(resultRowJND, "MD_TYPE_CODE");
				String productSpecName = CommonUtil.getValue(resultRowJND, "PRODUCTSPECNAME_JND");
				String defectCode = CommonUtil.getValue(resultRowJND, "DEFECTCODE_JND");
				String defectCodeDesc = CommonUtil.getValue(resultRowJND, "DEFECTCODEDESCRIPTION_JND");
				String superDefectCode = CommonUtil.getValue(resultRowJND, "SUPERDEFECTCODE_JND");
				String superDefectCodeDesc = CommonUtil.getValue(resultRowJND, "SUPERDEFECTCODEDESCRIPTION_JND");
				String pattern = CommonUtil.getValue(resultRowJND, "PATTERN_JND");
				String JNDName = CommonUtil.getValue(resultRowJND, "JNDNAME_JND");
				String sign = CommonUtil.getValue(resultRowJND, "SIGN_JND");
				String panelGrade = CommonUtil.getValue(resultRowJND, "PANELGRADE_JND");
				String activeState = CommonUtil.getValue(resultRowJND, "ACTIVESTATE_JND");
				
				List<Object[]> updateArgList = new ArrayList<Object[]>();
				List<Object> bindList = new ArrayList<Object>();
				List<Object[]> resultArgList = new ArrayList<Object[]>();
				List<Object> resultBindList = new ArrayList<Object>();
				
				String updateSQL = "";
				
				if(activeState.equals("Active"))
				{
					updateSQL = sqlMergeJND.toString();

					//Match
					bindList.add(productSpecName);
					bindList.add(defectCode);
					bindList.add(superDefectCode);
					bindList.add(pattern);
					bindList.add(JNDName);
					bindList.add(panelGrade);
					
					//Update
					bindList.add(defectCodeDesc);		
					bindList.add(superDefectCodeDesc);	
					bindList.add(sign);	
					
					//Insert
					bindList.add(productSpecName);
					bindList.add(defectCode);
					bindList.add(superDefectCode);
					bindList.add(pattern);
					bindList.add(JNDName);
					bindList.add(panelGrade);
					bindList.add(defectCodeDesc);		
					bindList.add(superDefectCodeDesc);	
					bindList.add(sign);	
					updateArgList.add(bindList.toArray());
				}
				else
				{
					updateSQL = sqlRemoveJND.toString();
					
					bindList.add(productSpecName);
					bindList.add(defectCode);
					bindList.add(superDefectCode);
					bindList.add(pattern);
					bindList.add(JNDName);
					bindList.add(panelGrade);
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
					GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResultJND.toString(), resultArgList);
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					
					result = "Y";
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
					GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResultJND.toString(), resultArgList);
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					
					result = "N";
					resultMessage = e.getMessage();
				}
				
				CommonUtil.resultSendToMDM(MD_ID, MD_Code, MD_Desc, MD_Type_Code, eventInfo, serviceName, resultMessage, result);
			}
		}
		
		//SUR
		StringBuffer sqlSUR = new StringBuffer();
		sqlSUR.append("SELECT MD_ID, MD_CODE, MD_DESCRIPTION, MD_TYPE_CODE, MD_CATE_CODE, STATUS_CODE, CREATION_DATE, LAST_UPDATE_DATE, PRODUCTSPECNAME_SUR, OPERATIONNAME_SUR,  ");
		sqlSUR.append("    JUDGE_SUR, DEFECTCODE_SUR, STANDARD_SUR, DESCRIPTION_SUR, ACTIVESTATE_SUR, ORG_CODE_SUR, ORG_NAME_SUR, DATA_STATUS_SUR, RESULTMESSAGE ");
		sqlSUR.append("FROM MES_MDMIF_PL005@OADBLINK.V3FAB.COM  ");
		sqlSUR.append("WHERE MD_CATE_CODE = 'SURFACEDEFECTCODE' ");
		sqlSUR.append("    AND DATA_STATUS_SUR != 'Y' ");

		List<ListOrderedMap> resultSUR;

		try
		{
			GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
			resultSUR = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlSUR.toString(), bindArray);
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		}
		catch (FrameworkErrorSignal fe)
		{
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			resultSUR = null;
		}
		
		if (resultSUR != null && resultSUR.size() > 0)
		{
			StringBuffer sqlMergeSUR = new StringBuffer();
			sqlMergeSUR.append("MERGE INTO CT_SURFACEDEFECTCODE ");
			sqlMergeSUR.append("    USING DUAL ");
			sqlMergeSUR.append("        ON(PRODUCTSPECNAME = ? AND OPERATIONNAME = ? AND JUDGE = ? AND DEFECTCODE = ?) ");
			sqlMergeSUR.append("    WHEN MATCHED THEN ");
			sqlMergeSUR.append("        UPDATE SET STANDARD = ?, DESCRIPTION = ?             ");
			sqlMergeSUR.append("    WHEN NOT MATCHED THEN ");
			sqlMergeSUR.append("        INSERT(PRODUCTSPECNAME, OPERATIONNAME, JUDGE, DEFECTCODE, STANDARD, DESCRIPTION)  ");
			sqlMergeSUR.append("        VALUES(?, ?, ?, ?, ?, ?) ");
			
			StringBuffer sqlRemoveSUR = new StringBuffer();
			sqlRemoveSUR.append("DELETE FROM CT_SURFACEDEFECTCODE WHERE PRODUCTSPECNAME = ? AND OPERATIONNAME = ? AND JUDGE = ? AND DEFECTCODE = ? ");

			StringBuffer sqlResultSUR = new StringBuffer();
			sqlResultSUR.append("UPDATE MES_MDMIF_PL005@OADBLINK.V3FAB.COM SET DATA_STATUS_SUR = ?, RESULTMESSAGE = ? WHERE MD_ID = ? AND MD_TYPE_CODE = ? ");

			for (ListOrderedMap resultRowSUR : resultSUR)
			{
				String MD_ID = CommonUtil.getValue(resultRowSUR, "MD_ID");
				String MD_Code = CommonUtil.getValue(resultRowSUR, "MD_CODE");
				String MD_Desc = CommonUtil.getValue(resultRowSUR, "MD_DESCRIPTION");
				String MD_Type_Code = CommonUtil.getValue(resultRowSUR, "MD_TYPE_CODE");
				String productSpecName = CommonUtil.getValue(resultRowSUR, "PRODUCTSPECNAME_SUR");
				String operationName = CommonUtil.getValue(resultRowSUR, "OPERATIONNAME_SUR");
				String judge = CommonUtil.getValue(resultRowSUR, "JUDGE_SUR");
				String defectCode = CommonUtil.getValue(resultRowSUR, "DEFECTCODE_SUR");
				String standard = CommonUtil.getValue(resultRowSUR, "STANDARD_SUR");
				String description = CommonUtil.getValue(resultRowSUR, "DESCRIPTION_SUR");
				String activeState = CommonUtil.getValue(resultRowSUR, "ACTIVESTATE_SUR");
				
				List<Object[]> updateArgList = new ArrayList<Object[]>();
				List<Object> bindList = new ArrayList<Object>();
				List<Object[]> resultArgList = new ArrayList<Object[]>();
				List<Object> resultBindList = new ArrayList<Object>();
				
				String updateSQL = "";
				
				if(activeState.equals("Active"))
				{
					updateSQL = sqlMergeSUR.toString();

					//Match
					bindList.add(productSpecName);
					bindList.add(operationName);
					bindList.add(judge);
					bindList.add(defectCode);
					
					//Update
					bindList.add(standard);		
					bindList.add(description);	
				
					//Insert
					bindList.add(productSpecName);
					bindList.add(operationName);
					bindList.add(judge);
					bindList.add(defectCode);
					bindList.add(standard);		
					bindList.add(description);
					updateArgList.add(bindList.toArray());
				}
				else
				{
					updateSQL = sqlRemoveSUR.toString();
					
					bindList.add(productSpecName);
					bindList.add(operationName);
					bindList.add(judge);
					bindList.add(defectCode);
					updateArgList.add(bindList.toArray());
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
					GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResultSUR.toString(), resultArgList);
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					
					result = "Y";
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
					GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResultSUR.toString(), resultArgList);
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					
					result = "N";
					resultMessage = e.getMessage();
				}
				
				CommonUtil.resultSendToMDM(MD_ID, MD_Code, MD_Desc, MD_Type_Code, eventInfo, serviceName, resultMessage, result);
			}
		}
		
		//ACT
		StringBuffer sqlACT = new StringBuffer();
		sqlACT.append("SELECT MD_ID, MD_CODE, MD_DESCRIPTION, MD_TYPE_CODE, MD_CATE_CODE, STATUS_CODE, CREATION_DATE, LAST_UPDATE_DATE, FACTORYNAME_ACT, ACTIONCODE_ACT,  ");
		sqlACT.append("    DEPARTMENT_ACT, DESCRIPTION_ACT, ACTIVESTATE_ACT, ORG_CODE_ACT, ORG_NAME_ACT, DATA_STATUS_ACT, RESULTMESSAGE ");
		sqlACT.append("FROM MES_MDMIF_PL005@OADBLINK.V3FAB.COM  ");
		sqlACT.append("WHERE MD_CATE_CODE = 'ABNORMALSHEETACTIONCODE' ");
		sqlACT.append("    AND DATA_STATUS_ACT != 'Y' ");

		List<ListOrderedMap> resultACT;

		try
		{
			GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
			resultACT = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlACT.toString(), bindArray);
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		}
		catch (FrameworkErrorSignal fe)
		{
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			resultACT = null;
		}
		
		if (resultACT != null && resultACT.size() > 0)
		{
			StringBuffer sqlMergeACT = new StringBuffer();
			sqlMergeACT.append("MERGE INTO CT_ABNORMALSHEETACTIONCODE ");
			sqlMergeACT.append("    USING DUAL ");
			sqlMergeACT.append("        ON(FACTORYNAME = ? AND ACTIONCODE = ?) ");
			sqlMergeACT.append("    WHEN MATCHED THEN ");
			sqlMergeACT.append("        UPDATE SET DEPARTMENT = ?, DESCRIPTION = ?             ");
			sqlMergeACT.append("    WHEN NOT MATCHED THEN ");
			sqlMergeACT.append("        INSERT(FACTORYNAME, ACTIONCODE, DEPARTMENT, DESCRIPTION)  ");
			sqlMergeACT.append("        VALUES(?, ?, ?, ?) ");
			
			StringBuffer sqlRemoveACT = new StringBuffer();
			sqlRemoveACT.append("DELETE FROM CT_ABNORMALSHEETACTIONCODE WHERE FACTORYNAME = ? AND ACTIONCODE = ? ");

			StringBuffer sqlResultACT = new StringBuffer();
			sqlResultACT.append("UPDATE MES_MDMIF_PL005@OADBLINK.V3FAB.COM SET DATA_STATUS_ACT = ?, RESULTMESSAGE = ? WHERE MD_ID = ? AND MD_TYPE_CODE = ? ");

			for (ListOrderedMap resultRowACT : resultACT)
			{
				String MD_ID = CommonUtil.getValue(resultRowACT, "MD_ID");
				String MD_Code = CommonUtil.getValue(resultRowACT, "MD_CODE");
				String MD_Desc = CommonUtil.getValue(resultRowACT, "MD_DESCRIPTION");
				String MD_Type_Code = CommonUtil.getValue(resultRowACT, "MD_TYPE_CODE");
				String factoryName = CommonUtil.getValue(resultRowACT, "FACTORYNAME_ACT");
				String actionCode = CommonUtil.getValue(resultRowACT, "ACTIONCODE_ACT");
				String department = CommonUtil.getValue(resultRowACT, "DEPARTMENT_ACT");
				String description = CommonUtil.getValue(resultRowACT, "DESCRIPTION_ACT");
				String activeState = CommonUtil.getValue(resultRowACT, "ACTIVESTATE_ACT");
				
				List<Object[]> updateArgList = new ArrayList<Object[]>();
				List<Object> bindList = new ArrayList<Object>();
				List<Object[]> resultArgList = new ArrayList<Object[]>();
				List<Object> resultBindList = new ArrayList<Object>();
				
				String updateSQL = "";
				
				if(activeState.equals("Active"))
				{
					updateSQL = sqlMergeACT.toString();

					//Match
					bindList.add(factoryName);
					bindList.add(actionCode);
					
					//Update
					bindList.add(department);		
					bindList.add(description);	
				
					//Insert
					bindList.add(factoryName);
					bindList.add(actionCode);
					bindList.add(department);		
					bindList.add(description);
					updateArgList.add(bindList.toArray());
				}
				else
				{
					updateSQL = sqlRemoveACT.toString();
					
					bindList.add(factoryName);
					bindList.add(actionCode);
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
					GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResultACT.toString(), resultArgList);
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					
					result = "Y";
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
					GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResultACT.toString(), resultArgList);
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					
					result = "N";
					resultMessage = e.getMessage();
				}
				
				CommonUtil.resultSendToMDM(MD_ID, MD_Code, MD_Desc, MD_Type_Code, eventInfo, serviceName, resultMessage, result);
			}
		}
		
		//EXCP
		StringBuffer sqlEXCP = new StringBuffer();
		sqlEXCP.append("SELECT MD_ID, MD_CODE, MD_DESCRIPTION, MD_TYPE_CODE, MD_CATE_CODE, STATUS_CODE, CREATION_DATE, LAST_UPDATE_DATE, FACTORYNAME_EXCP,  ");
		sqlEXCP.append("    EXCEPTIONCODE_EXCP, DEPARTMENT_EXCP, SUBCODE1_EXCP, DESCRIPTION1_EXCP, SUBCODE2_EXCP, DESCRIPTION2_EXCP, ACTIVESTATE_EXCP, ORG_CODE_EXCP,  ");
		sqlEXCP.append("    ORG_NAME_EXCP, DATA_STATUS_EXCP, RESULTMESSAGE ");
		sqlEXCP.append("FROM MES_MDMIF_PL005@OADBLINK.V3FAB.COM  ");
		sqlEXCP.append("WHERE MD_CATE_CODE = 'ABNORMALSHEETEXCEPTIONCODE' ");
		sqlEXCP.append("    AND DATA_STATUS_EXCP != 'Y' ");

		List<ListOrderedMap> resultEXCP;

		try
		{
			GenericServiceProxy.getTxDataSourceManager().beginTransaction(PropagationBehavior.PROPAGATION_REQUIRES_NEW);
			resultEXCP = kr.co.aim.greentrack.generic.GenericServiceProxy.getSqlMesTemplate().queryForList(sqlEXCP.toString(), bindArray);
			GenericServiceProxy.getTxDataSourceManager().commitTransaction();
		}
		catch (FrameworkErrorSignal fe)
		{
			GenericServiceProxy.getTxDataSourceManager().rollbackTransaction();
			resultEXCP = null;
		}
		
		if (resultEXCP != null && resultEXCP.size() > 0)
		{
			StringBuffer sqlMergeEXCP = new StringBuffer();
			sqlMergeEXCP.append("MERGE INTO CT_ABNORMALSHEETEXCEPTIONCODE ");
			sqlMergeEXCP.append("    USING DUAL ");
			sqlMergeEXCP.append("        ON(FACTORYNAME = ? AND EXCEPTIONCODE = ?) ");
			sqlMergeEXCP.append("    WHEN MATCHED THEN ");
			sqlMergeEXCP.append("        UPDATE SET DEPARTMENT = ?, SUBCODE1 = ?, DESCRIPTION1 = ?, SUBCODE2 = ?, DESCRIPTION2 = ?              ");
			sqlMergeEXCP.append("    WHEN NOT MATCHED THEN ");
			sqlMergeEXCP.append("        INSERT(FACTORYNAME, EXCEPTIONCODE, DEPARTMENT, SUBCODE1, DESCRIPTION1, SUBCODE2, DESCRIPTION2)  ");
			sqlMergeEXCP.append("        VALUES(?, ?, ?, ?, ?, ?, ?) ");
			
			StringBuffer sqlRemoveEXCP = new StringBuffer();
			sqlRemoveEXCP.append("DELETE FROM CT_ABNORMALSHEETEXCEPTIONCODE WHERE FACTORYNAME = ? AND EXCEPTIONCODE = ? ");

			StringBuffer sqlResultEXCP = new StringBuffer();
			sqlResultEXCP.append("UPDATE MES_MDMIF_PL005@OADBLINK.V3FAB.COM SET DATA_STATUS_EXCP = ?, RESULTMESSAGE = ? WHERE MD_ID = ? AND MD_TYPE_CODE = ? ");

			for (ListOrderedMap resultRowEXCP : resultEXCP)
			{
				String MD_ID = CommonUtil.getValue(resultRowEXCP, "MD_ID");
				String MD_Code = CommonUtil.getValue(resultRowEXCP, "MD_CODE");
				String MD_Desc = CommonUtil.getValue(resultRowEXCP, "MD_DESCRIPTION");
				String MD_Type_Code = CommonUtil.getValue(resultRowEXCP, "MD_TYPE_CODE");
				String factoryName = CommonUtil.getValue(resultRowEXCP, "FACTORYNAME_EXCP");
				String exceptionCode = CommonUtil.getValue(resultRowEXCP, "EXCEPTIONCODE_EXCP");
				String department = CommonUtil.getValue(resultRowEXCP, "DEPARTMENT_EXCP");
				String subCode1 = CommonUtil.getValue(resultRowEXCP, "SUBCODE1_EXCP");
				String description1 = CommonUtil.getValue(resultRowEXCP, "DESCRIPTION1_EXCP");
				String subCode2 = CommonUtil.getValue(resultRowEXCP, "SUBCODE2_EXCP");
				String description2 = CommonUtil.getValue(resultRowEXCP, "DESCRIPTION2_EXCP");
				String activeState = CommonUtil.getValue(resultRowEXCP, "ACTIVESTATE_EXCP");
				
				List<Object[]> updateArgList = new ArrayList<Object[]>();
				List<Object> bindList = new ArrayList<Object>();
				List<Object[]> resultArgList = new ArrayList<Object[]>();
				List<Object> resultBindList = new ArrayList<Object>();
				
				String updateSQL = "";
				
				if(activeState.equals("Active"))
				{
					updateSQL = sqlMergeEXCP.toString();

					//Match
					bindList.add(factoryName);
					bindList.add(exceptionCode);
					
					//Update
					bindList.add(department);		
					bindList.add(subCode1);
					bindList.add(description1);
					bindList.add(subCode2);
					bindList.add(description2);	
				
					//Insert
					bindList.add(factoryName);
					bindList.add(exceptionCode);
					bindList.add(department);		
					bindList.add(subCode1);
					bindList.add(description1);
					bindList.add(subCode2);
					bindList.add(description2);	
					updateArgList.add(bindList.toArray());
				}
				else
				{
					updateSQL = sqlRemoveEXCP.toString();
					
					bindList.add(factoryName);
					bindList.add(exceptionCode);
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
					GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResultEXCP.toString(), resultArgList);
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					
					result = "Y";
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
					GenericServiceProxy.getSqlMesTemplate().updateBatch(sqlResultEXCP.toString(), resultArgList);
					GenericServiceProxy.getTxDataSourceManager().commitTransaction();
					
					result = "N";
					resultMessage = e.getMessage();
				}
				
				CommonUtil.resultSendToMDM(MD_ID, MD_Code, MD_Desc, MD_Type_Code, eventInfo, serviceName, resultMessage, result);
			}
		}
	}
}
