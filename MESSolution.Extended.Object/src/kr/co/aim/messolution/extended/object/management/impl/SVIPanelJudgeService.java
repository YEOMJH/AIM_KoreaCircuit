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
import kr.co.aim.messolution.extended.object.management.data.SVIPanelJudge;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class SVIPanelJudgeService extends CTORMService<SVIPanelJudge> {
	
	public static Log logger = LogFactory.getLog(SVIPanelJudge.class);

	public List<SVIPanelJudge> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<SVIPanelJudge> result = super.select(condition, bindSet, SVIPanelJudge.class);

		return result;
	}

	public SVIPanelJudge selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(SVIPanelJudge.class, isLock, keySet);
	}

	public SVIPanelJudge create(EventInfo eventInfo, SVIPanelJudge dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<SVIPanelJudge> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);
	}

	public void remove(EventInfo eventInfo, SVIPanelJudge dataInfo) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
	}

	public SVIPanelJudge modify(EventInfo eventInfo, SVIPanelJudge dataInfo)
	{
		super.update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void modify(EventInfo eventInfo, List<SVIPanelJudge> dataInfoList)
	{
		super.update(dataInfoList);
	}
	
	public List<SVIPanelJudge> getFirstSVIPanelData(String PanelName)
	{
		String condition = " AND panelNAME = ? ORDER BY SEQ DESC ";
		Object[] bindSet = new Object[] { PanelName };

		List<SVIPanelJudge> dataInfoList = new ArrayList<SVIPanelJudge>();

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

	public String getSVILotSeq(String lotName)
	{
		String sql = "  SELECT NVL(MAX(SEQ) , 0) + 1 AS SEQ FROM CT_SVIPANELJUDGE WHERE PANELNAME = :PANELNAME ";
		String seq = "";

		Map<String, String> args = new HashMap<String, String>();
		args.put("PANELNAME", lotName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

		if (result.size() > 0)
		{
			seq = ConvertUtil.getMapValueByName(result.get(0), "SEQ");
		}

		return seq;
	}

}
