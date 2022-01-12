package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MVIPanelJudge;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class MVIPanelJudgeService extends CTORMService<MVIPanelJudge> {

	public static Log logger = LogFactory.getLog(MVIPanelJudge.class);

	public List<MVIPanelJudge> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<MVIPanelJudge> result = super.select(condition, bindSet, MVIPanelJudge.class);

		return result;
	}

	public MVIPanelJudge selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MVIPanelJudge.class, isLock, keySet);
	}

	public MVIPanelJudge create(EventInfo eventInfo, MVIPanelJudge dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<MVIPanelJudge> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);
	}

	public void remove(EventInfo eventInfo, MVIPanelJudge dataInfo) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
	}

	public MVIPanelJudge modify(EventInfo eventInfo, MVIPanelJudge dataInfo)
	{
		super.update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void modify(EventInfo eventInfo, List<MVIPanelJudge> dataInfoList)
	{
		super.update(dataInfoList);
	}

	public String getMVIPanelSeq(String lotName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT NVL (MAX (SEQ), 0) + 1 AS SEQ ");
		sql.append("  FROM CT_MVIPANELJUDGE ");
		sql.append(" WHERE PANELNAME = :PANELNAME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("PANELNAME", lotName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		String seq = "";
		if (result.size() > 0)
		{
			seq = ConvertUtil.getMapValueByName(result.get(0), "SEQ");
		}

		return seq;
	}

	public String getMVIPanelSeqV2(String lotName)
	{//caixu 2020/11/26 MVI Judge
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT COUNT(*) + 1 AS SEQ ");
		sql.append("  FROM CT_MVIPANELJUDGE ");
		sql.append(" WHERE PANELNAME = :PANELNAME ");

		Map<String, String> args = new HashMap<String, String>();
		args.put("PANELNAME", lotName);

		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		try
		{
			result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), args);
		}
		catch (Exception e)
		{
			logger.info("Data Null");
		}

		String seq = "1";

		if (result.size() > 0)
		{
			seq = ConvertUtil.getMapValueByName(result.get(0), "SEQ");
		}

		return seq;
	}
    //GET SEQ
	public List<MVIPanelJudge> getMVIPanelJudgeDataList(String panelName)
	{
		String condition = " PANELNAME = :PANELNAME ORDER BY SEQ DESC ";
		Object[] bindSet = new Object[] { panelName };

		List<MVIPanelJudge> dataInfoList = new ArrayList<MVIPanelJudge>();

		try
		{
			dataInfoList = this.select(condition, bindSet);
		}
		catch (Exception e)
		{
			dataInfoList = null;
		}

		return dataInfoList;
	}

	public void setPanelJudgeData(long seq, String lotName, String opticalJudge, String electricalJudge, String judge, EventInfo eventInfo, EventInfo trackOutEventInfo, String machineName,
			String tpJudge) throws CustomException
	{
		try
		{
			MVIPanelJudge dataInfo = ExtendedObjectProxy.getMVIPanelJudgeService().selectByKey(false, new Object[] { seq, lotName });
			dataInfo.setOpticalJudge(opticalJudge);
			dataInfo.setElectricalJudge(electricalJudge);
			dataInfo.setAfterGrade(judge);
			dataInfo.setEventTime(eventInfo.getEventTime());
			dataInfo.setEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
			dataInfo.setLastLoggedOutTime(trackOutEventInfo.getEventTime());
			dataInfo.setMachineName(machineName);
			dataInfo.setTpJudge(tpJudge);

			ExtendedObjectProxy.getMVIPanelJudgeService().update(dataInfo);
		}
		catch (greenFrameDBErrorSignal ex)
		{
			throw new CustomException("LOT-0318");
		}
	}
	
	public void setPanelJudgeDataForSort(long seq, String lotName, String opticalJudge, String electricalJudge, String judge, EventInfo eventInfo, String machineName,
			String tpJudge) throws CustomException
	{
		try
		{
			MVIPanelJudge dataInfo = ExtendedObjectProxy.getMVIPanelJudgeService().selectByKey(false, new Object[] { seq, lotName });
			dataInfo.setOpticalJudge(opticalJudge);
			dataInfo.setElectricalJudge(electricalJudge);
			dataInfo.setAfterGrade(judge);
			dataInfo.setEventTime(eventInfo.getEventTime());
			dataInfo.setEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
			dataInfo.setMachineName(machineName);
			dataInfo.setTpJudge(tpJudge);

			ExtendedObjectProxy.getMVIPanelJudgeService().update(dataInfo);
		}
		catch (greenFrameDBErrorSignal ex)
		{
			throw new CustomException("LOT-0318");
		}
	}
}
