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

public class ReviewDefectCode extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String FactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String ProcessOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String RSProcessOperationName = SMessageUtil.getBodyItemValue(doc, "REVIEWPROCESSOPERATIONNAME", true);

		List<Element> PanelList = SMessageUtil.getBodySequenceItemList(doc, "DEFECTCODELIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		for (Element code : PanelList)
		{
			String defectCode = code.getChild("DEFECTCODE").getText();
			String seq = code.getChild("SEQ").getText();
			String desc = code.getChild("DESCRIPTION").getText();
			String rqty = code.getChild("REFERENCEQUANTITY").getText();
			String pep = code.getChild("PEP").getText();
			String department = code.getChild("DEPARTMENT").getText();
			String exceptioncode = code.getChild("EXCEPTIONCODE").getText();
			String ngquantity = code.getChild("NGQUANTITY").getText();
			String managertype = code.getChild("MANAGETYPE").getText();
			String productSpecName = code.getChild("PRODUCTSPECNAME").getText();
			String possibleJudge = code.getChild("POSSIBLEJUDGE").getText();
			String abnormalPossibleJudge = code.getChild("ABNORMALPOSSIBLEJUDGE").getText();
			String defectSize = code.getChild("DEFECTSIZE").getText();
			String aType = code.getChild("ACTIONTYPE").getText();

			if (aType.equals("Insert"))
			{
				eventInfo.setEventName("Insert Defect Code.");
				eventInfo.setEventComment("Insert Defect Code.");

				InsertDefectCode(FactoryName, ProcessOperationName, RSProcessOperationName, defectCode, seq, rqty, desc, eventInfo, pep, department, exceptioncode, ngquantity, managertype,
						productSpecName,possibleJudge,abnormalPossibleJudge,defectSize);
			}
			else if (aType.equals("Update"))
			{
				eventInfo.setEventName("Update Defect Code.");
				eventInfo.setEventComment("Update Defect Code.");

				UpdateDefectCode(FactoryName, ProcessOperationName, RSProcessOperationName, defectCode, seq, rqty, desc, eventInfo, pep, department, exceptioncode, ngquantity, managertype,
						productSpecName,possibleJudge,abnormalPossibleJudge,defectSize);
			}
			else if (aType.equals("Delete"))
			{
				eventInfo.setEventName("Delete Defect Code.");
				eventInfo.setEventComment("Delete Defect Code.");

				DeleteDefectCode(FactoryName, ProcessOperationName, RSProcessOperationName, defectCode, productSpecName);
			}
		}

		return doc;
	}

	private void InsertDefectCode(String factoryname, String processOperationName, String reviewProcessOperationName, String defectCode, String seq, String rQty, String desc, EventInfo eventInfo,
			String pep, String department, String exceptioncode, String ngquantity, String managertype, String productSpecName,String possibleJudge,String abnormalPossibleJudge,String defectSize) throws CustomException
	{
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT ");
			sql.append("  INTO CT_REVIEWDEFECTCODE  ");
			sql.append("  (FACTORYNAME, PROCESSOPERATIONNAME, DEFECTCODE, SEQ, REFERENCEQUANTITY, ");
			sql.append("   EVENTNAME, EVENTUSER, EVENTTIME, EVENTCOMMENT, DESCRIPTION, ");
			sql.append("   REVIEWPROCESSOPERATIONNAME, PEP, DEPARTMENT, EXCEPTIONCODE, NGQUANTITY, ");
			sql.append("   MANAGETYPE, PRODUCTSPECNAME,POSSIBLEJUDGE,ABNORMALPOSSIBLEJUDGE,DEFECTSIZE) ");
			sql.append("VALUES  ");
			sql.append("  (:FACTORYNAME, :PROCESSOPERATIONNAME, :DEFECTCODE, :SEQ, :REFERENCEQUANTITY, ");
			sql.append("   :EVENTNAME, :EVENTUSER, :EVENTTIME, :EVENTCOMMENT, :DESCRIPTION, ");
			sql.append("   :REVIEWPROCESSOPERATIONNAME, :PEP, :DEPARTMENT, :EXCEPTIONCODE, :NGQUANTITY, ");
			sql.append("   :MANAGETYPE, :PRODUCTSPECNAME, :POSSIBLEJUDGE, :ABNORMALPOSSIBLEJUDGE, :DEFECTSIZE ) ");
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("FACTORYNAME", factoryname);
			bindMap.put("PROCESSOPERATIONNAME", processOperationName);
			bindMap.put("DEFECTCODE", defectCode);
			bindMap.put("SEQ", seq);
			bindMap.put("REFERENCEQUANTITY", rQty);
			bindMap.put("EVENTNAME", eventInfo.getEventName());
			bindMap.put("EVENTUSER", eventInfo.getEventUser());
			bindMap.put("EVENTTIME", eventInfo.getEventTime());
			bindMap.put("EVENTCOMMENT", eventInfo.getEventComment());
			bindMap.put("DESCRIPTION", desc);
			bindMap.put("REVIEWPROCESSOPERATIONNAME", reviewProcessOperationName);
			bindMap.put("PEP", pep);
			bindMap.put("DEPARTMENT", department);
			bindMap.put("EXCEPTIONCODE", exceptioncode);
			bindMap.put("NGQUANTITY", ngquantity);
			bindMap.put("MANAGETYPE", managertype);
			bindMap.put("PRODUCTSPECNAME", productSpecName);
			bindMap.put("POSSIBLEJUDGE", possibleJudge);
			bindMap.put("ABNORMALPOSSIBLEJUDGE", abnormalPossibleJudge);
			bindMap.put("DEFECTSIZE", defectSize);

			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "for insert " + defectCode + " into CT_REVIEWDEFECTCODE   Error : " + e.toString());
		}
	}

	private void UpdateDefectCode(String factoryname, String processOperationName, String reviewProcessOperationName, String defectCode, String seq, String rQty, String desc, EventInfo eventInfo,
			String pep, String department, String exceptioncode, String ngquantity, String managertype, String productSpecName,String possibleJudge,String abnormalPossibleJudge,String defectSize) throws CustomException
	{
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE CT_REVIEWDEFECTCODE ");
			sql.append("   SET SEQ = :SEQ, ");
			sql.append("       REFERENCEQUANTITY = :REFERENCEQUANTITY, ");
			sql.append("       EVENTNAME = :EVENTNAME, ");
			sql.append("       EVENTUSER = :EVENTUSER, ");
			sql.append("       EVENTTIME = :EVENTTIME, ");
			sql.append("       EVENTCOMMENT = :EVENTCOMMENT, ");
			sql.append("       DESCRIPTION = :DESCRIPTION, ");
			sql.append("       PEP = :PEP, ");
			sql.append("       DEPARTMENT = :DEPARTMENT, ");
			sql.append("       EXCEPTIONCODE = :EXCEPTIONCODE, ");
			sql.append("       NGQUANTITY = :NGQUANTITY, ");
			sql.append("       MANAGETYPE = :MANAGETYPE, ");
			sql.append("       POSSIBLEJUDGE = :POSSIBLEJUDGE, ");
			sql.append("       ABNORMALPOSSIBLEJUDGE = :ABNORMALPOSSIBLEJUDGE, ");
			sql.append("       DEFECTSIZE = :DEFECTSIZE ");
			sql.append(" WHERE FACTORYNAME = :FACTORYNAME ");
			sql.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
			sql.append("   AND DEFECTCODE = :DEFECTCODE ");
			sql.append("   AND REVIEWPROCESSOPERATIONNAME = :REVIEWPROCESSOPERATIONNAME ");
			sql.append("   AND PRODUCTSPECNAME = :PRODUCTSPECNAME ");
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("FACTORYNAME", factoryname);
			bindMap.put("PROCESSOPERATIONNAME", processOperationName);
			bindMap.put("DEFECTCODE", defectCode);
			bindMap.put("SEQ", seq);
			bindMap.put("REFERENCEQUANTITY", rQty);
			bindMap.put("EVENTNAME", eventInfo.getEventName());
			bindMap.put("EVENTUSER", eventInfo.getEventUser());
			bindMap.put("EVENTTIME", eventInfo.getEventTime());
			bindMap.put("EVENTCOMMENT", eventInfo.getEventComment());
			bindMap.put("DESCRIPTION", desc);
			bindMap.put("REVIEWPROCESSOPERATIONNAME", reviewProcessOperationName);
			bindMap.put("PEP", pep);
			bindMap.put("DEPARTMENT", department);
			bindMap.put("EXCEPTIONCODE", exceptioncode);
			bindMap.put("NGQUANTITY", ngquantity);
			bindMap.put("MANAGETYPE", managertype);
			bindMap.put("PRODUCTSPECNAME", productSpecName);
			bindMap.put("POSSIBLEJUDGE", possibleJudge);
			bindMap.put("ABNORMALPOSSIBLEJUDGE", abnormalPossibleJudge);
			bindMap.put("DEFECTSIZE", defectSize);

			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "for update " + defectCode + " to the CT_REVIEWDEFECTCODE   Error : " + e.toString());
		}
	}

	private void DeleteDefectCode(String factoryname, String processOperationName, String reviewProcessOperationName, String defectCode, String productSpecName) throws CustomException
	{
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("DELETE FROM CT_REVIEWDEFECTCODE ");
			sql.append(" WHERE FACTORYNAME = :FACTORYNAME ");
			sql.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
			sql.append("   AND DEFECTCODE = :DEFECTCODE ");
			sql.append("   AND REVIEWPROCESSOPERATIONNAME = :REVIEWPROCESSOPERATIONNAME ");
			sql.append("   AND PRODUCTSPECNAME = :PRODUCTSPECNAME ");
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("FACTORYNAME", factoryname);
			bindMap.put("PROCESSOPERATIONNAME", processOperationName);
			bindMap.put("DEFECTCODE", defectCode);
			bindMap.put("REVIEWPROCESSOPERATIONNAME", reviewProcessOperationName);
			bindMap.put("PRODUCTSPECNAME", productSpecName);

			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "for delete " + defectCode + " from the CT_REVIEWDEFECTCODE   Error : " + e.toString());
		}
	}

}
