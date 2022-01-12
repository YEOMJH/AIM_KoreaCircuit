package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DefectGroup;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class DefectGroupService extends CTORMService<DefectGroup> {
	public static Log logger = LogFactory.getLog(DefectGroupService.class);
	private final String historyEntity = "DefectGroupHist";

	public List<DefectGroup> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<DefectGroup> result = super.select(condition, bindSet, DefectGroup.class);

		return result;
	}

	public DefectGroup selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(DefectGroup.class, isLock, keySet);
	}

	public DefectGroup create(EventInfo eventInfo, DefectGroup dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, DefectGroup dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public DefectGroup modify(EventInfo eventInfo, DefectGroup dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public DefectGroup createdDefectGroup(EventInfo eventInfo,String defectGroupName,String defectCode,String  judge,String useFlag,String lowerQty,String upperQty){
		DefectGroup dataInfo =new DefectGroup();
		
		dataInfo.setDefectGroupName(defectGroupName);
		dataInfo.setDefectCode(defectCode);
		dataInfo.setJudge(judge);
		dataInfo.setUseFlag(useFlag);
		
		if(StringUtil.isNotEmpty(lowerQty))
		dataInfo.setLowerQty(lowerQty);
		
		if(StringUtil.isNotEmpty(upperQty))
		dataInfo.setUpperQty(upperQty);
		
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		
		dataInfo=this.create(eventInfo, dataInfo);
		
		return dataInfo;
	}
}
