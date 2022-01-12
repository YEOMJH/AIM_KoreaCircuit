package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.CrucibleLot;
import kr.co.aim.messolution.extended.object.management.data.MaskSpec;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class MaskSpecService extends CTORMService<MaskSpec> {

	public static Log logger = LogFactory.getLog(MaskSpec.class);

	private final String historyEntity = "MaskSpecHistory";

	public List<MaskSpec> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<MaskSpec> result = super.select(condition, bindSet, MaskSpec.class);

		return result;
	}

	public MaskSpec selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MaskSpec.class, isLock, keySet);
	}

	public MaskSpec create(EventInfo eventInfo, MaskSpec dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, MaskSpec dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public MaskSpec modify(EventInfo eventInfo, MaskSpec dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public MaskSpec getMaskSpecData(String factoryName, String maskSpecName) throws CustomException
	{
		MaskSpec maskSpecData = new MaskSpec();
		Object[] keySet = new Object[] { factoryName, maskSpecName };

		try
		{
			maskSpecData = ExtendedObjectProxy.getMaskSpecService().selectByKey(false, keySet);
		}
		catch (Exception e)
		{
			throw new CustomException("MASKSPEC-0002", maskSpecName);
		}

		return maskSpecData;
	}
}
