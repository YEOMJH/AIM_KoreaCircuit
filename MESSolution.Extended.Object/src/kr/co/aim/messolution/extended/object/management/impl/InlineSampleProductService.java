package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.InlineSampleProduct;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class InlineSampleProductService extends CTORMService<InlineSampleProduct> {

	public static Log logger = LogFactory.getLog(InlineSampleProduct.class);

	private final String historyEntity = "InlineSampleProductHist";

	public List<InlineSampleProduct> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<InlineSampleProduct> result = super.select(condition, bindSet, InlineSampleProduct.class);

		return result;
	}

	public InlineSampleProduct selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(InlineSampleProduct.class, isLock, keySet);
	}

	public InlineSampleProduct create(EventInfo eventInfo, InlineSampleProduct dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<InlineSampleProduct> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}

	public void remove(EventInfo eventInfo, InlineSampleProduct dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public void remove(EventInfo eventInfo, List<InlineSampleProduct> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);

		super.delete(dataInfoList);
	}

	public InlineSampleProduct modify(EventInfo eventInfo, InlineSampleProduct dataInfo)
	{
		super.update(dataInfo);

		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void modify(EventInfo eventInfo, List<InlineSampleProduct> dataInfoList)
	{
		super.update(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}

}
