package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.PackingBan;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PackingBanService extends CTORMService<PackingBan> {
	public static Log logger = LogFactory.getLog(PackingBanService.class);

	public List<PackingBan> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<PackingBan> result = super.select(condition, bindSet, PackingBan.class);

		return result;
	}

	public PackingBan selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(PackingBan.class, isLock, keySet);
	}

	public PackingBan create(EventInfo eventInfo, PackingBan dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, PackingBan dataInfo) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
	}

	public PackingBan modify(EventInfo eventInfo, PackingBan dataInfo)
	{
		super.update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public PackingBan createPackingBan(EventInfo eventInfo, String productName, String productType, String banType, String banReason, String description, String factoryName, String defaultFlag)
	{
		PackingBan dataInfo = new PackingBan();
		dataInfo.setProductName(productName);
		dataInfo.setProductType(productType);
		dataInfo.setBanType(banType);
		dataInfo.setBanReason(banReason);
		dataInfo.setFactoryName(factoryName);
		dataInfo.setDefaultFlag(defaultFlag);
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventComment(eventInfo.getEventComment());

		this.create(eventInfo, dataInfo);

		return dataInfo;
	}

	public void deletePackingBan(String productName)
	{
		PackingBan dataInfo = this.selectByKey(false, new Object[] { productName });

		this.delete(dataInfo);
	}
}
