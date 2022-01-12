package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.BankQueueTime;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class BankQueueTimeService extends CTORMService<BankQueueTime>{

	public List<BankQueueTime> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<BankQueueTime> result = super.select(condition, bindSet, BankQueueTime.class);

		return result;
	}
	
	public BankQueueTime selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(BankQueueTime.class, isLock, keySet);
	}
	
	public BankQueueTime create(EventInfo eventInfo, BankQueueTime dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, BankQueueTime dataInfo) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
	}
	
	public BankQueueTime modify(EventInfo eventInfo, BankQueueTime dataInfo)
	{
		super.update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
}
