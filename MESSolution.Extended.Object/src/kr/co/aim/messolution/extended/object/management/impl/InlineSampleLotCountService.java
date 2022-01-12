package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.InlineSampleLotCount;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class InlineSampleLotCountService extends CTORMService<InlineSampleLotCount> {

	public static Log logger = LogFactory.getLog(InlineSampleLotCount.class);

	private final String historyEntity = "";

	public List<InlineSampleLotCount> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<InlineSampleLotCount> result = super.select(condition, bindSet, InlineSampleLotCount.class);

		return result;
	}

	public InlineSampleLotCount selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(InlineSampleLotCount.class, isLock, keySet);
	}

	public InlineSampleLotCount create(EventInfo eventInfo, InlineSampleLotCount dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<InlineSampleLotCount> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}

	public void remove(EventInfo eventInfo, InlineSampleLotCount dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public void remove(EventInfo eventInfo, List<InlineSampleLotCount> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);

		super.delete(dataInfoList);
	}

	public InlineSampleLotCount modify(EventInfo eventInfo, InlineSampleLotCount dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void modify(EventInfo eventInfo, List<InlineSampleLotCount> dataInfoList)
	{
		super.update(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}

}
