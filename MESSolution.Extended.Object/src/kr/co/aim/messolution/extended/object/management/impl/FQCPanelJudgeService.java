package kr.co.aim.messolution.extended.object.management.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.FQCPanelJudge;
import kr.co.aim.messolution.extended.object.management.data.ShieldLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FQCPanelJudgeService extends CTORMService<FQCPanelJudge> {

	public static Log logger = LogFactory.getLog(FQCPanelJudge.class);

	public List<FQCPanelJudge> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<FQCPanelJudge> result = super.select(condition, bindSet, FQCPanelJudge.class);

		return result;
	}

	public FQCPanelJudge selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(FQCPanelJudge.class, isLock, keySet);
	}

	public FQCPanelJudge create(EventInfo eventInfo, FQCPanelJudge dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<FQCPanelJudge> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);
	}

	public void remove(EventInfo eventInfo, FQCPanelJudge dataInfo) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
	}
	
	public void remove(EventInfo eventInfo, List<FQCPanelJudge> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfoList);
	}

	public FQCPanelJudge modify(EventInfo eventInfo, FQCPanelJudge dataInfo)
	{
		super.update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void modify(EventInfo eventInfo, List<FQCPanelJudge> dataInfoList)
	{
		super.update(dataInfoList);
	}

	public FQCPanelJudge getFQCPanelJudgeData(String fqcLotName, long seq, String lotName) throws greenFrameDBErrorSignal, CustomException
	{
		FQCPanelJudge dataInfo = new FQCPanelJudge();

		try
		{
			dataInfo = ExtendedObjectProxy.getFQCPanelJudgeService().selectByKey(false, new Object[] { fqcLotName, seq, lotName });
		}
		catch (Exception e)
		{
			dataInfo = null;
		}

		return dataInfo;
	}

	public FQCPanelJudge updateFQCPanelJudgeData(EventInfo eventInfo, String fqcLotName, long seq, String lotName, String opticalJudge, String electricalJudge, String afterGrade, String machineName)
			throws CustomException
	{
		FQCPanelJudge dataInfo = ExtendedObjectProxy.getFQCPanelJudgeService().getFQCPanelJudgeData(fqcLotName, seq, lotName);

		if (dataInfo == null)
			throw new CustomException("LOT-0319", lotName);

		dataInfo.setOpticalJudge(opticalJudge);
		dataInfo.setElectricalJudge(electricalJudge);
		dataInfo.setAfterGrade(afterGrade);
		dataInfo.setMachineName(machineName);
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

		dataInfo = ExtendedObjectProxy.getFQCPanelJudgeService().modify(eventInfo, dataInfo);

		return dataInfo;
	}

	public List<Map<String, Object>> getFQCPanelJudgeCount(String fqcLotName, String seq)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT COUNT (*) AS ACOUNT, ");
		sql.append("       SUM (CASE WHEN AFTERGRADE IS NOT NULL THEN 1 ELSE 0 END) NCOUNT, ");
		sql.append("       SUM (CASE ");
		sql.append("               WHEN AFTERGRADE IS NOT NULL ");
		sql.append("                AND BEFOREGRADE != AFTERGRADE ");
		sql.append("               THEN ");
		sql.append("                  1 ");
		sql.append("               ELSE ");
		sql.append("                  0 ");
		sql.append("            END) ");
		sql.append("          SCOUNT ");
		sql.append("  FROM CT_FQCPANELJUDGE P ");
		sql.append(" WHERE P.FQCLOTNAME = :FQCLOTNAME ");
		sql.append("   AND SEQ = :SEQ ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("FQCLOTNAME", fqcLotName);
		args.put("SEQ", seq);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		return sqlResult;
	}
}
