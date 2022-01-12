package kr.co.aim.messolution.dispatch.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;

import org.jdom.Document;
import org.jdom.Element;

public class DSPOperationPrioritySetUp extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> eleDSPList = SMessageUtil.getBodySequenceItemList(doc, "DSPLIST", true);

		for (Element eleDSP : eleDSPList)
		{
			String DispatchPriority = SMessageUtil.getChildText(eleDSP, "DISPATCHPRIORITY", true);
			String ProductSpecName = SMessageUtil.getChildText(eleDSP, "PRODUCTSPECNAME", true);
			String ProcessFlowName = SMessageUtil.getChildText(eleDSP, "PROCESSFLOWNAME", true);
			String ProcessOperationName = SMessageUtil.getChildText(eleDSP, "PROCESSOPERATIONNAME", true);
			String FactoryName = SMessageUtil.getChildText(eleDSP, "FACTORYNAME", true);
			String MachineName = SMessageUtil.getChildText(eleDSP, "MACHINENAME", true);
			String ConditionId = SMessageUtil.getChildText(eleDSP, "CONDITIONID", true);
			String DispatchState = SMessageUtil.getChildText(eleDSP, "DISPATCHSTATE", false);
			String MachineRecipeName = SMessageUtil.getChildText(eleDSP, "MACHINERECIPENAME", true);
			String RollType = SMessageUtil.getChildText(eleDSP, "ROLLTYPE", false);
			String CheckLevel = SMessageUtil.getChildText(eleDSP, "CHECKLEVEL", false);
			String EventUser = SMessageUtil.getBodyItemValue(doc, "EVENTUSER", true);
			String TimeKey = TimeStampUtil.getCurrentEventTimeKey();
			String EventName = "OICModifyDispatchPriority";
			String EventComment = SMessageUtil.getBodyItemValue(doc, "EVENTCOMMENT", false);

			updateDispatchPriority(ProductSpecName, ProcessFlowName, ProcessOperationName, DispatchPriority, FactoryName);
			insertHistory(ConditionId, MachineName, MachineRecipeName, RollType, CheckLevel, TimeKey, EventUser, EventName, EventComment, DispatchState, DispatchPriority);
		}

		return doc;
	}

	public void updateDispatchPriority(String ProductSpecName, String ProcessFlowName, String ProcessOperationName, String DispatchPriority, String FactoryName)
	{
		// update dispatch priority.
		StringBuilder updateSql = new StringBuilder();
		updateSql.append("UPDATE POSMACHINE P ");
		updateSql.append("   SET DISPATCHPRIORITY = :DISPATCHPRIORITY ");
		updateSql.append(" WHERE EXISTS ");
		updateSql.append("          (SELECT P.CONDITIONID ");
		updateSql.append("             FROM TPFOPOLICY T ");
		updateSql.append("            WHERE T.CONDITIONID = P.CONDITIONID ");
		updateSql.append("              AND T.PRODUCTSPECNAME = :PRODUCTSPECNAME ");
		updateSql.append("              AND T.PROCESSFLOWNAME = :PROCESSFLOWNAME ");
		updateSql.append("              AND T.PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		updateSql.append("              AND T.FACTORYNAME = :FACTORYNAME) ");

		Map<String, Object> args = new HashMap<>();

		args.put("DISPATCHPRIORITY", DispatchPriority);
		args.put("PRODUCTSPECNAME", ProductSpecName);
		args.put("PROCESSFLOWNAME", ProcessFlowName);
		args.put("PROCESSOPERATIONNAME", ProcessOperationName);
		args.put("FACTORYNAME", FactoryName);

		GenericServiceProxy.getSqlMesTemplate().update(updateSql.toString(), args);
	}

	public void insertHistory(String ConditionId, String MachineName, String MachineRecipeName, String RollType, String CheckLevel, String TimeKey, String EventUser, String EventName,
			String EventComment, String DispatchState, String DispatchPriority)
	{
		// insert POSMACHINEHISTORY data.
		StringBuilder insertSql = new StringBuilder();
		insertSql.append("INSERT ");
		insertSql.append("  INTO POSMACHINEHISTORY  ");
		insertSql.append("  (CONDITIONID, MACHINENAME, MACHINERECIPENAME, ROLLTYPE, CHECKLEVEL, ");
		insertSql.append("   TIMEKEY, EVENTUSER, EVENTNAME, EVENTCOMMENT, DISPATCHSTATE, ");
		insertSql.append("   DISPATCHPRIORITY) ");
		insertSql.append("VALUES  ");
		insertSql.append("  (:CONDITIONID, :MACHINENAME, :MACHINERECIPENAME, :ROLLTYPE, :CHECKLEVEL, ");
		insertSql.append("   :TIMEKEY, :EVENTUSER, :EVENTNAME, :EVENTCOMMENT, :DISPATCHSTATE, ");
		insertSql.append("   :DISPATCHPRIORITY) ");

		Map<String, Object> inargs = new HashMap<>();

		inargs.put("CONDITIONID", ConditionId);
		inargs.put("MACHINENAME", MachineName);
		inargs.put("MACHINERECIPENAME", MachineRecipeName);
		inargs.put("ROLLTYPE", RollType);
		inargs.put("CHECKLEVEL", CheckLevel);
		inargs.put("TIMEKEY", TimeKey);
		inargs.put("EVENTUSER", EventUser);
		inargs.put("EVENTNAME", EventName);
		inargs.put("EVENTCOMMENT", EventComment);
		inargs.put("DISPATCHSTATE", DispatchState);
		inargs.put("DISPATCHPRIORITY", DispatchPriority);

		GenericServiceProxy.getSqlMesTemplate().update(insertSql.toString(), inargs);
	}
}
