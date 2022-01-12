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

public class DSPWIPBalance extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> eleDSPList = SMessageUtil.getBodySequenceItemList(doc, "DSPWIPLIST", true);

		for (Element eleDSP : eleDSPList)
		{
			String EventName = SMessageUtil.getBodyItemValue(doc, "EVENTNAME", false);
			String FactoryName = SMessageUtil.getChildText(eleDSP, "FACTORYNAME", true);
			String TargetOperationName = SMessageUtil.getChildText(eleDSP, "TARGETOPERATIONNAME", true);
			String TargetOperationVersion = SMessageUtil.getChildText(eleDSP, "TARGETOPERATIONVERSION", false);
			String ReasonOperationName = SMessageUtil.getChildText(eleDSP, "REASONOPERATIONNAME", false);
			String ReasonOperationVersion = SMessageUtil.getChildText(eleDSP, "REASONOPERATIONVERSION", false);
			String ReworkOperationName = SMessageUtil.getChildText(eleDSP, "REWORKOPERATIONNAME", false);
			String Limit = SMessageUtil.getChildText(eleDSP, "LIMIT", false);
			String DispatchState = SMessageUtil.getChildText(eleDSP, "DISPATCHSTATE", false);

			if (StringUtils.isNotEmpty(EventName) && StringUtils.equals(EventName, "InsertData"))
			{
				// Insert CT_WIPBALANCE table.
				insertData(FactoryName, TargetOperationName, TargetOperationVersion, ReasonOperationName, ReasonOperationVersion, ReworkOperationName, Limit, DispatchState);
			}
			else if (StringUtils.isNotEmpty(EventName) && StringUtils.equals(EventName, "DeleteData"))
			{
				// Delete CT_WIPBALANCE table.
				deleteData(FactoryName, TargetOperationName, TargetOperationVersion, ReasonOperationName, ReasonOperationVersion, Limit, DispatchState);
			}
			else if (StringUtils.isNotEmpty(EventName) && StringUtils.equals(EventName, "ModifyLimit"))
			{
				// Update CT_WIPBALANCE table.
				updateData(FactoryName, TargetOperationName, TargetOperationVersion, ReasonOperationName, ReasonOperationVersion, Limit, DispatchState);
			}
		}

		return doc;
	}

	public void insertData(String FactoryName, String TargetOperationName, String TargetOperationVersion, String ReasonOperationName, String ReasonOperationVersion, String ReworkOperationName,
			String Limit, String DispatchState)
	{
		// insert CT_WIPBALANCE data.
		StringBuilder insertSql = new StringBuilder();
		insertSql.append("INSERT ");
		insertSql.append("  INTO CT_WIPBALANCE  ");
		insertSql.append("  (FACTORYNAME, TARGETOPERATIONNAME, TARGETOPERATIONVERSION, REASONOPERATIONNAME, REASONOPERATIONVERSION, ");
		insertSql.append("   REWORKOPERATIONNAME, REWORKOPERATIONVERSION, LIMIT, DISPATCHSTATE) ");
		insertSql.append("VALUES  ");
		insertSql.append("  (:FACTORYNAME, :TARGETOPERATIONNAME, :TARGETOPERATIONVERSION, :REASONOPERATIONNAME, :REASONOPERATIONVERSION, ");
		insertSql.append("   :REWORKOPERATIONNAME, :REWORKOPERATIONVERSION, :LIMIT, :DISPATCHSTATE) ");

		Map<String, Object> inargs = new HashMap<>();

		inargs.put("FACTORYNAME", FactoryName);
		inargs.put("TARGETOPERATIONNAME", TargetOperationName);
		inargs.put("TARGETOPERATIONVERSION", TargetOperationVersion);
		inargs.put("REASONOPERATIONNAME", ReasonOperationName);
		inargs.put("REASONOPERATIONVERSION", ReasonOperationVersion);
		inargs.put("REWORKOPERATIONNAME", ReworkOperationName);
		inargs.put("REWORKOPERATIONVERSION", "00001");
		inargs.put("LIMIT", Limit);
		inargs.put("DISPATCHSTATE", DispatchState);

		GenericServiceProxy.getSqlMesTemplate().update(insertSql.toString(), inargs);
	}

	public void updateData(String FactoryName, String TargetOperationName, String TargetOperationVersion, String ReasonOperationName, String ReasonOperationVersion, String Limit, String DispatchState)
	{
		// Update CT_WIPBALANCE data.
		StringBuilder updateSql = new StringBuilder();
		updateSql.append("UPDATE CT_WIPBALANCE W ");
		updateSql.append("   SET W.LIMIT = :LIMIT, W.DISPATCHSTATE = :DISPATCHSTATE ");
		updateSql.append(" WHERE W.FACTORYNAME = :FACTORYNAME ");
		updateSql.append("   AND W.TARGETOPERATIONNAME = :TARGETOPERATIONNAME ");
		updateSql.append("   AND W.TARGETOPERATIONVERSION = :TARGETOPERATIONVERSION ");
		updateSql.append("   AND W.REASONOPERATIONNAME = :REASONOPERATIONNAME ");
		updateSql.append("   AND W.REASONOPERATIONVERSION = :REASONOPERATIONVERSION ");

		Map<String, Object> updateArgs = new HashMap<>();

		updateArgs.put("FACTORYNAME", FactoryName);
		updateArgs.put("TARGETOPERATIONNAME", TargetOperationName);
		updateArgs.put("TARGETOPERATIONVERSION", TargetOperationVersion);
		updateArgs.put("REASONOPERATIONNAME", ReasonOperationName);
		updateArgs.put("REASONOPERATIONVERSION", ReasonOperationVersion);
		updateArgs.put("LIMIT", Limit);
		updateArgs.put("DISPATCHSTATE", DispatchState);

		GenericServiceProxy.getSqlMesTemplate().update(updateSql.toString(), updateArgs);
	}

	public void deleteData(String FactoryName, String TargetOperationName, String TargetOperationVersion, String ReasonOperationName, String ReasonOperationVersion, String Limit, String DispatchState)
	{
		// delete CT_WIPBALANCE data.
		StringBuilder deleteSql = new StringBuilder();
		deleteSql.append("DELETE FROM CT_WIPBALANCE ");
		deleteSql.append(" WHERE FACTORYNAME = :FACTORYNAME ");
		deleteSql.append("   AND TARGETOPERATIONNAME = :TARGETOPERATIONNAME ");
		deleteSql.append("   AND TARGETOPERATIONVERSION = :TARGETOPERATIONVERSION ");
		deleteSql.append("   AND REASONOPERATIONNAME = :REASONOPERATIONNAME ");
		deleteSql.append("   AND REASONOPERATIONVERSION = :REASONOPERATIONVERSION ");
		deleteSql.append("   AND LIMIT = :LIMIT ");
		deleteSql.append("   AND DISPATCHSTATE = :DISPATCHSTATE ");

		Map<String, Object> deleteArgs = new HashMap<>();

		deleteArgs.put("FACTORYNAME", FactoryName);
		deleteArgs.put("TARGETOPERATIONNAME", TargetOperationName);
		deleteArgs.put("TARGETOPERATIONVERSION", TargetOperationVersion);
		deleteArgs.put("REASONOPERATIONNAME", ReasonOperationName);
		deleteArgs.put("REASONOPERATIONVERSION", ReasonOperationVersion);
		deleteArgs.put("LIMIT", Limit);
		deleteArgs.put("DISPATCHSTATE", DispatchState);

		GenericServiceProxy.getSqlMesTemplate().update(deleteSql.toString(), deleteArgs);
	}

}