package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.BankQueueTime;
import kr.co.aim.messolution.extended.object.management.data.CrucibleOrganic;
import kr.co.aim.messolution.extended.object.management.data.OrganicExtractCard;
import kr.co.aim.messolution.extended.object.management.data.OrganicMapping;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class CrucibleOrganicService extends CTORMService<CrucibleOrganic>{

	public List<CrucibleOrganic> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<CrucibleOrganic> result = super.select(condition, bindSet, CrucibleOrganic.class);

		return result;
	}
	
	public CrucibleOrganic selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(CrucibleOrganic.class, isLock, keySet);
	}
	
	public CrucibleOrganic create(EventInfo eventInfo, CrucibleOrganic dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, CrucibleOrganic dataInfo) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
	}
	
	public CrucibleOrganic modify(EventInfo eventInfo, CrucibleOrganic dataInfo)
	{
		super.update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
}
