package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MVIDefectCode;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MVIDefectCodeService extends CTORMService<MVIDefectCode> {

	public static Log logger = LogFactory.getLog(MVIDefectCode.class);	

	private final String historyEntity = "MVIDefectCodeHIST";

	public List<MVIDefectCode> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<MVIDefectCode> result = super.select(condition, bindSet, MVIDefectCode.class);

		return result;
	}

	public MVIDefectCode selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MVIDefectCode.class, isLock, keySet);
	}

	public MVIDefectCode create(EventInfo eventInfo, MVIDefectCode dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, MVIDefectCode dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public MVIDefectCode modify(EventInfo eventInfo, MVIDefectCode dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public MVIDefectCode getMVIDefectCodeData(String productSpecName, String defectCode, String superDefectCode)
	{
		MVIDefectCode dataInfo = new MVIDefectCode();

		try
		{
			dataInfo = this.selectByKey(false, new String[] { productSpecName, defectCode, superDefectCode });
		}
		catch (Exception e)
		{
			dataInfo = null;
		}

		return dataInfo;
	}

	public MVIDefectCode createMVIDefectCode(EventInfo eventInfo, String productSpecName, String defectCode, String description, String superDefectCode, long levelNo, String conditionFlag,
			String panelGrade)
	{
		MVIDefectCode dataInfo = new MVIDefectCode();
		dataInfo.setProductSpecName(productSpecName);
		dataInfo.setDefectCode(defectCode);
		dataInfo.setDescription(description);
		dataInfo.setSuperDefectCode(superDefectCode);
		dataInfo.setLevelNo(levelNo);
		dataInfo.setConditionFlag(conditionFlag);
		dataInfo.setPanelGrade(panelGrade);
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		dataInfo.setLastEventComment(eventInfo.getEventComment());

		this.create(eventInfo, dataInfo);

		return dataInfo;
	}

	public void deleteMVIDefectCode(EventInfo eventInfo, String productSpecName, String defectCode, String superDefectCode)
	{
		MVIDefectCode dataInfo = this.getMVIDefectCodeData(productSpecName, defectCode, superDefectCode);

		if (dataInfo != null)
			this.remove(eventInfo, dataInfo);
	}

}
