package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.extended.object.management.data.ReviewOperationInfo;
import kr.co.aim.messolution.extended.object.management.data.ShieldLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Element;

public class ReviewOperationInfoService extends CTORMService<ReviewOperationInfo> {
	public static Log logger = LogFactory.getLog(ReviewOperationInfoService.class);

	public List<ReviewOperationInfo> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<ReviewOperationInfo> result = super.select(condition, bindSet, ReviewOperationInfo.class);

		return result;
	}

	public ReviewOperationInfo selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ReviewOperationInfo.class, isLock, keySet);
	}

	public ReviewOperationInfo create(EventInfo eventInfo, ReviewOperationInfo dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<ReviewOperationInfo> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);
	}

	public void remove(EventInfo eventInfo, ReviewOperationInfo dataInfo) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
	}

	public void remove(EventInfo eventInfo, List<ReviewOperationInfo> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfoList);
	}

	public ReviewOperationInfo modify(EventInfo eventInfo, ReviewOperationInfo dataInfo)
	{
		super.update(dataInfo);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
