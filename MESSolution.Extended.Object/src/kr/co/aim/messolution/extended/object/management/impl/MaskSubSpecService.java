package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MaskSubSpec;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class MaskSubSpecService extends CTORMService<MaskSubSpec> {

	public static Log logger = LogFactory.getLog(MaskSubSpec.class);

	private final String historyEntity = "MaskSubSpecHistory";

	public List<MaskSubSpec> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<MaskSubSpec> result = super.select(condition, bindSet, MaskSubSpec.class);

		return result;
	}

	public MaskSubSpec selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MaskSubSpec.class, isLock, keySet);
	}

	public MaskSubSpec create(EventInfo eventInfo, MaskSubSpec dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, MaskSubSpec dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public MaskSubSpec modify(EventInfo eventInfo, MaskSubSpec dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public List<String> getMaskSubSpecList(String maskSpec) throws CustomException
	{
		if (maskSpec == null || maskSpec.isEmpty())
		{
			//SYSTEM-0011:{0}: The incoming variable value can not be empty or null!!
			throw new CustomException("SYSTEM-0011",Thread.currentThread().getStackTrace()[1].getMethodName());
		}

		List<MaskSubSpec> resultList = null;

		try
		{
			resultList = this.select("where 1=1 and maskspecname = ? ", new Object[] { maskSpec });
		}
		catch (greenFrameDBErrorSignal dbEx)
		{
			if (dbEx.getErrorCode().equals(ErrorSignal.NotFoundSignal))
				throw new CustomException("COMM-1000", "MaskSubSpec", "MaskSpec = " + maskSpec);
			else
				throw new CustomException(dbEx.getCause());
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		List<String> subSpecList = new ArrayList<>();

		for (MaskSubSpec subSpecData : resultList)
		{
			subSpecList.add(subSpecData.getMaskSubSpecName());
		}

		return subSpecList;
	}
}
