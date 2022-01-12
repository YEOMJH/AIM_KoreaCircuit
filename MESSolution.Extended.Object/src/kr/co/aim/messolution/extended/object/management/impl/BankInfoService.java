package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.BankInfo;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class BankInfoService extends CTORMService<BankInfo>{

	public List<BankInfo> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<BankInfo> result = super.select(condition, bindSet, BankInfo.class);

		return result;
	}
	
	public BankInfo selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(BankInfo.class, isLock, keySet);
	}
	
	public BankInfo create(EventInfo eventInfo, BankInfo dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, BankInfo dataInfo) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
	}
	
	public BankInfo modify(EventInfo eventInfo, BankInfo dataInfo)
	{
		super.update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
}

