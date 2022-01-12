package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;
import org.jdom.Element;

public class ReviewDefectCodeExcel extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> insertCodeList = SMessageUtil.getBodySequenceItemList(doc, "INSERTCODELIST", false);
		List<Element> updateCodeList = SMessageUtil.getBodySequenceItemList(doc, "UPDATECODELIST", false);
		List<Element> deleteCodeList = SMessageUtil.getBodySequenceItemList(doc, "DELETECODELIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		for (Element insertCode : insertCodeList)
		{
			String factoryName = insertCode.getChild("FACTORYNAME").getText();
			String productSpecName = insertCode.getChild("PRODUCTSPECNAME").getText();
			String processOperationName = insertCode.getChild("PROCESSOPERATIONNAME").getText();
			String reviewOperationName = insertCode.getChild("REVIEWOPERATIONNAME").getText();
			String defectCode = insertCode.getChild("DEFECTCODE").getText();
			String seq = insertCode.getChild("SEQ").getText();
			String desc = insertCode.getChild("DESCRIPTION").getText();
			String pep = insertCode.getChild("PEP").getText();
			String department = insertCode.getChild("DEPARTMENT").getText();
			String exceptionCode = insertCode.getChild("EXCEPTIONCODE").getText();
			String rqQty = insertCode.getChild("REFERENCEQUANTITY").getText();
			String ngQty = insertCode.getChild("NGQUANTITY").getText();
			String manageType = insertCode.getChild("MANAGETYPE").getText();
			String possibleJudge = insertCode.getChild("POSSIBLEJUDGE").getText();
			String defectSize = insertCode.getChild("DEFECTSIZE").getText();
			String abnormalPossibleJudge = insertCode.getChild("ABNORMALPOSSIBLEJUDGE").getText();


		    eventInfo.setEventName("Insert Defect Code.");

			InsertDefectCode(factoryName, productSpecName, processOperationName, reviewOperationName, defectCode, seq, desc, pep,department,
					exceptionCode,rqQty,ngQty,manageType,possibleJudge,defectSize,abnormalPossibleJudge,eventInfo);
		}
		for (Element updateCode : updateCodeList)
		{
			String factoryName = updateCode.getChild("FACTORYNAME").getText();
			String productSpecName = updateCode.getChild("PRODUCTSPECNAME").getText();
			String processOperationName = updateCode.getChild("PROCESSOPERATIONNAME").getText();
			String reviewOperationName = updateCode.getChild("REVIEWOPERATIONNAME").getText();
			String defectCode = updateCode.getChild("DEFECTCODE").getText();
			String seq = updateCode.getChild("SEQ").getText();
			String desc = updateCode.getChild("DESCRIPTION").getText();
			String pep = updateCode.getChild("PEP").getText();
			String department = updateCode.getChild("DEPARTMENT").getText();
			String exceptionCode = updateCode.getChild("EXCEPTIONCODE").getText();
			String rqQty = updateCode.getChild("REFERENCEQUANTITY").getText();
			String ngQty = updateCode.getChild("NGQUANTITY").getText();
			String manageType = updateCode.getChild("MANAGETYPE").getText();
			String possibleJudge = updateCode.getChild("POSSIBLEJUDGE").getText();
			String defectSize = updateCode.getChild("DEFECTSIZE").getText();
			String abnormalPossibleJudge = updateCode.getChild("ABNORMALPOSSIBLEJUDGE").getText();


		    eventInfo.setEventName("Update Defect Code.");

			UpdateDefectCode(factoryName, productSpecName, processOperationName, reviewOperationName, defectCode, seq, desc, pep,department,
					exceptionCode,rqQty,ngQty,manageType,possibleJudge,defectSize,abnormalPossibleJudge,eventInfo);
		}
		for (Element deleteCode : deleteCodeList)
		{
			String factoryName = deleteCode.getChild("FACTORYNAME").getText();
			String productSpecName = deleteCode.getChild("PRODUCTSPECNAME").getText();
			String processOperationName = deleteCode.getChild("PROCESSOPERATIONNAME").getText();
			String reviewOperationName = deleteCode.getChild("REVIEWOPERATIONNAME").getText();
			String defectCode = deleteCode.getChild("DEFECTCODE").getText();

		    eventInfo.setEventName("Delete Defect Code.");

		    DeleteDefectCode(factoryName,productSpecName, processOperationName,reviewOperationName, defectCode);
		}

		return doc;
	}

	private void InsertDefectCode(String factoryname, String productSpecName,String processOperationName, String reviewOperationName, String defectCode, 
			String seq, String desc, String pep, String department,String exceptionCode,String rqQty,String ngQty,String  manageType,
			String possibleJudge,String defectSize,String abnormalPossibleJudge,EventInfo eventInfo)
			throws CustomException
	{
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT ");
			sql.append("  INTO CT_REVIEWDEFECTCODE  ");
			sql.append("  (FACTORYNAME, PRODUCTSPECNAME,PROCESSOPERATIONNAME, DEFECTCODE, SEQ, REFERENCEQUANTITY, ");
			sql.append("   EVENTNAME, EVENTUSER, EVENTTIME, EVENTCOMMENT, DESCRIPTION, ");
			sql.append("   REVIEWPROCESSOPERATIONNAME,PEP,DEPARTMENT,EXCEPTIONCODE,NGQUANTITY,MANAGETYPE, ");
			sql.append("   DEFECTJUDGE,POSSIBLEJUDGE,DEFECTSIZE,ABNORMALPOSSIBLEJUDGE) ");
			sql.append("VALUES  ");
			sql.append("  (:FACTORYNAME,:PRODUCTSPECNAME,:PROCESSOPERATIONNAME, :DEFECTCODE, :SEQ, :REFERENCEQUANTITY, ");
			sql.append("   :EVENTNAME, :EVENTUSER, :EVENTTIME, :EVENTCOMMENT, :DESCRIPTION, ");
			sql.append("   :REVIEWPROCESSOPERATIONNAME,:PEP,:DEPARTMENT,:EXCEPTIONCODE,:NGQUANTITY,:MANAGETYPE, ");
			sql.append("   :DEFECTJUDGE,:POSSIBLEJUDGE,:DEFECTSIZE,:ABNORMALPOSSIBLEJUDGE) ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("FACTORYNAME", factoryname);
			bindMap.put("PRODUCTSPECNAME", productSpecName);
			bindMap.put("PROCESSOPERATIONNAME", processOperationName);
			bindMap.put("DEFECTCODE", defectCode);
			bindMap.put("SEQ", seq);
			bindMap.put("REFERENCEQUANTITY", rqQty);
			bindMap.put("EVENTNAME", eventInfo.getEventName());
			bindMap.put("EVENTUSER", eventInfo.getEventUser());
			bindMap.put("EVENTTIME", eventInfo.getEventTime());
			bindMap.put("EVENTCOMMENT", eventInfo.getEventComment());
			bindMap.put("DESCRIPTION", desc);
			bindMap.put("REVIEWPROCESSOPERATIONNAME", reviewOperationName);
			bindMap.put("PEP", pep);
			bindMap.put("DEPARTMENT", department);
			bindMap.put("EXCEPTIONCODE", exceptionCode);
			bindMap.put("NGQUANTITY", ngQty);
			bindMap.put("MANAGETYPE", manageType);
			bindMap.put("DEFECTJUDGE", "");
			bindMap.put("POSSIBLEJUDGE", possibleJudge);
			bindMap.put("DEFECTSIZE", defectSize);
			bindMap.put("ABNORMALPOSSIBLEJUDGE", abnormalPossibleJudge);
			

			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "for insert " + defectCode + " into CT_REVIEWDEFECTCODE   Error : " + e.toString());
		}
	}

	private void UpdateDefectCode(String factoryname, String productSpecName,String processOperationName, String reviewOperationName, String defectCode, 
			String seq, String desc, String pep, String department,String exceptionCode,String rqQty,String ngQty,String  manageType,
			String possibleJudge,String defectSize,String abnormalPossibleJudge,EventInfo eventInfo)
			throws CustomException
	{
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE CT_REVIEWDEFECTCODE ");
			sql.append("   SET SEQ = :SEQ, ");
			sql.append("       DESCRIPTION = :DESCRIPTION ");
			sql.append("       PEP = :PEP ");
			sql.append("       DEPARTMENT = :DEPARTMENT ");
			sql.append("       EXCEPTIONCODE = :EXCEPTIONCODE ");
			sql.append("       REFERENCEQUANTITY = :REFERENCEQUANTITY ");
			sql.append("       NGQUANTITY = :NGQUANTITY ");
			sql.append("       MANAGETYPE = :MANAGETYPE ");
			sql.append("       POSSIBLEJUDGE = :POSSIBLEJUDGE ");
			sql.append("       DEFECTSIZE = :DEFECTSIZE ");
			sql.append("       ABNORMALPOSSIBLEJUDGE = :ABNORMALPOSSIBLEJUDGE ");
			sql.append("       EVENTNAME = :EVENTNAME, ");
			sql.append("       EVENTUSER = :EVENTUSER, ");
			sql.append("       EVENTTIME = :EVENTTIME, ");
			sql.append("       EVENTCOMMENT = :EVENTCOMMENT, ");
			sql.append(" WHERE FACTORYNAME = :FACTORYNAME ");
			sql.append("   AND PRODUCTSPECNAME = :PRODUCTSPECNAME ");
			sql.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
			sql.append("   AND DEFECTCODE = :DEFECTCODE ");
			sql.append("   AND REVIEWPROCESSOPERATIONNAME = :REVIEWPROCESSOPERATIONNAME ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("FACTORYNAME", factoryname);
			bindMap.put("PRODUCTSPECNAME", productSpecName);
			bindMap.put("PROCESSOPERATIONNAME", processOperationName);
			bindMap.put("DEFECTCODE", defectCode);
			bindMap.put("REVIEWPROCESSOPERATIONNAME", reviewOperationName);
			bindMap.put("SEQ", seq);
			bindMap.put("DESCRIPTION", desc);
			bindMap.put("PEP", pep);
			bindMap.put("DEPARTMENT", department);
			bindMap.put("EXCEPTIONCODE", exceptionCode);
			bindMap.put("REFERENCEQUANTITY", rqQty);
			bindMap.put("NGQUANTITY", ngQty);
			bindMap.put("MANAGETYPE", manageType);
			bindMap.put("POSSIBLEJUDGE", possibleJudge);
			bindMap.put("DEFECTSIZE", defectSize);
			bindMap.put("ABNORMALPOSSIBLEJUDGE", abnormalPossibleJudge);
			
			bindMap.put("EVENTNAME", eventInfo.getEventName());
			bindMap.put("EVENTUSER", eventInfo.getEventUser());
			bindMap.put("EVENTTIME", eventInfo.getEventTime());
			bindMap.put("EVENTCOMMENT", eventInfo.getEventComment());
			

			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "for update " + defectCode + " to the CT_REVIEWDEFECTCODE   Error : " + e.toString());
		}
	}

	private void DeleteDefectCode(String factoryname, String productSpecName,String processOperationName, String reviewProcessOperationName, String defectCode) throws CustomException
	{
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("DELETE FROM CT_REVIEWDEFECTCODE ");
			sql.append(" WHERE FACTORYNAME = :FACTORYNAME ");
			sql.append("   AND PRODUCTSPECNAME = :PRODUCTSPECNAME ");
			sql.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
			sql.append("   AND DEFECTCODE = :DEFECTCODE ");
			sql.append("   AND REVIEWPROCESSOPERATIONNAME = :REVIEWPROCESSOPERATIONNAME ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("FACTORYNAME", factoryname);
			bindMap.put("PRODUCTSPECNAME", productSpecName);
			bindMap.put("PROCESSOPERATIONNAME", processOperationName);
			bindMap.put("DEFECTCODE", defectCode);
			bindMap.put("REVIEWPROCESSOPERATIONNAME", reviewProcessOperationName);

			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "for delete " + defectCode + " from the CT_REVIEWDEFECTCODE   Error : " + e.toString());
		}
	}

}
