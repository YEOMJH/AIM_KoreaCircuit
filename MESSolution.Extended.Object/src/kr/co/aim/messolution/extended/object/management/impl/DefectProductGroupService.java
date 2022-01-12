package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.DefectGroup;
import kr.co.aim.messolution.extended.object.management.data.DefectProductGroup;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

public class DefectProductGroupService extends CTORMService<DefectProductGroup> {
	public static Log logger = LogFactory.getLog(DefectProductGroupService.class);
	private final String historyEntity = "DefectProductGroupHist";

	public List<DefectProductGroup> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<DefectProductGroup> result = super.select(condition, bindSet, DefectProductGroup.class);

		return result;
	}
	
	public DefectProductGroup selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(DefectProductGroup.class, isLock, keySet);
	}

	public DefectProductGroup create(EventInfo eventInfo, DefectProductGroup dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, DefectProductGroup dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public DefectProductGroup modify(EventInfo eventInfo, DefectProductGroup dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void createdDefectProductGroup(EventInfo eventInfo, String defectFuction, String factoryName,
			String productSpecName, String productSpecVersion, String defectGroupName, String useFlag) {
		
		DefectProductGroup dataInfo =new DefectProductGroup();
		
		dataInfo.setDefectFunction(defectFuction);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setProductSpecName(productSpecName);
		dataInfo.setProductSpecVersion(productSpecVersion);
		if(StringUtil.isNotEmpty(defectGroupName))
		dataInfo.setDefectGroupName(defectGroupName);
		dataInfo.setUseFlag(useFlag);

		
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		
		dataInfo=this.create(eventInfo, dataInfo);
		
	}
}
