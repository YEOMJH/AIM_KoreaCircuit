package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class DSPMaskCleanSetUp extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> eleDSPList = SMessageUtil.getBodySequenceItemList(doc, "CHAMBERLIST", true);

		for (Element eleDSP : eleDSPList)
		{
			String EventName = SMessageUtil.getBodyItemValue(doc, "EVENTNAME", false);
			String MachineName = SMessageUtil.getChildText(eleDSP, "MACHINENAME", true);
			String MaskType = SMessageUtil.getChildText(eleDSP, "MASKTYPE", true);
			String ProcessFlowName = SMessageUtil.getChildText(eleDSP, "PROCESSFLOWNAME", true);
			String ProcessFlowVersion = SMessageUtil.getChildText(eleDSP, "PROCESSFLOWVERSION", true);
			String LastEventTime = SMessageUtil.getChildText(eleDSP, "LASTEVENTTIME", true);
			String LastEventName = SMessageUtil.getChildText(eleDSP, "LASTEVENTNAME", true);
			String LastEventUser = SMessageUtil.getChildText(eleDSP, "LASTEVENTUSER", true);
			String LastEventTimeKey = SMessageUtil.getChildText(eleDSP, "LASTEVENTTIMEKEY", true);
			String LastEventComment = SMessageUtil.getChildText(eleDSP, "LASTEVENTCOMMENT", false);

			if (StringUtils.isNotEmpty(EventName) && StringUtils.equals(EventName, "InsertData"))
			{
				// Insert CT_DSPFMRELATION table data.
				insertData(MachineName, MaskType, ProcessFlowName, ProcessFlowVersion, LastEventTime, LastEventName, LastEventUser, LastEventTimeKey, LastEventComment);
			}
			else if (StringUtils.isNotEmpty(EventName) && StringUtils.equals(EventName, "DeleteData"))
			{
				// Delete CT_DSPFMRELATION table data.
				deleteData(MachineName, MaskType, ProcessFlowName, ProcessFlowVersion, LastEventTime, LastEventName, LastEventUser, LastEventTimeKey, LastEventComment);
			}
		}

		return doc;
	}

	public void insertData(String MachineName, String MaskType, String ProcessFlowName, String ProcessFlowVersion, String LastEventTime, String LastEventName, String LastEventUser,
			String LastEventTimeKey, String LastEventComment)
	{
		// insert CT_DSPFMRELATION data.
		StringBuilder insertSql = new StringBuilder();
		insertSql.append("INSERT ");
		insertSql.append("  INTO CT_DSPFMRELATION  ");
		insertSql.append("  (MACHINENAME, MASKTYPE, PROCESSFLOWNAME, PROCESSFLOWVERSION, LASTEVENTTIME, ");
		insertSql.append("   LASTEVENTNAME, LASTEVENTUSER, LASTEVENTTIMEKEY, LASTEVENTCOMMENT) ");
		insertSql.append("VALUES  ");
		insertSql.append("  (:MACHINENAME, :MASKTYPE, :PROCESSFLOWNAME, :PROCESSFLOWVERSION, :LASTEVENTTIME, ");
		insertSql.append("   :LASTEVENTNAME, :LASTEVENTUSER, :LASTEVENTTIMEKEY, :LASTEVENTCOMMENT) ");

		Map<String, Object> inargs = new HashMap<>();

		inargs.put("MACHINENAME", MachineName);
		inargs.put("MASKTYPE", MaskType);
		inargs.put("PROCESSFLOWNAME", ProcessFlowName);
		inargs.put("PROCESSFLOWVERSION", ProcessFlowVersion);
		inargs.put("LASTEVENTTIME", LastEventTime);
		inargs.put("LASTEVENTNAME", LastEventName);
		inargs.put("LASTEVENTUSER", LastEventUser);
		inargs.put("LASTEVENTTIMEKEY", LastEventTimeKey);
		inargs.put("LASTEVENTCOMMENT", LastEventComment);

		GenericServiceProxy.getSqlMesTemplate().update(insertSql.toString(), inargs);
	}

	public void deleteData(String MachineName, String MaskType, String ProcessFlowName, String ProcessFlowVersion, String LastEventTime, String LastEventName, String LastEventUser,
			String LastEventTimeKey, String LastEventComment)
	{
		// delete CT_DSPFMRELATION data
		StringBuilder deleteSql = new StringBuilder();
		deleteSql.append("DELETE FROM CT_DSPFMRELATION ");
		deleteSql.append(" WHERE MACHINENAME = :MACHINENAME ");
		deleteSql.append("   AND MASKTYPE = :MASKTYPE ");
		deleteSql.append("   AND PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		deleteSql.append("   AND PROCESSFLOWVERSION = :PROCESSFLOWVERSION ");

		Map<String, Object> deleteArgs = new HashMap<>();

		deleteArgs.put("MACHINENAME", MachineName);
		deleteArgs.put("MASKTYPE", MaskType);
		deleteArgs.put("PROCESSFLOWNAME", ProcessFlowName);
		deleteArgs.put("PROCESSFLOWVERSION", ProcessFlowVersion);

		GenericServiceProxy.getSqlMesTemplate().update(deleteSql.toString(), deleteArgs);
	}

}