package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MaskOffsetSpec;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class MaskOffsetSpecService extends CTORMService<MaskOffsetSpec> {
	public static Log logger = LogFactory.getLog(MaskOffsetSpecService.class);

	private final String historyEntity = "MaskOffsetSpecHist";

	public List<MaskOffsetSpec> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<MaskOffsetSpec> result = super.select(condition, bindSet, MaskOffsetSpec.class);

		return result;
	}

	public MaskOffsetSpec selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MaskOffsetSpec.class, isLock, keySet);
	}

	public MaskOffsetSpec create(EventInfo eventInfo, MaskOffsetSpec dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void create(EventInfo eventInfo, List<MaskOffsetSpec> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}

	public void remove(EventInfo eventInfo, MaskOffsetSpec dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}
	
	public void remove(EventInfo eventInfo, List<MaskOffsetSpec> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);

		super.delete(dataInfoList);
	}

	public MaskOffsetSpec modify(EventInfo eventInfo, MaskOffsetSpec dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void modify(EventInfo eventInfo, List<MaskOffsetSpec> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.update(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}

}
