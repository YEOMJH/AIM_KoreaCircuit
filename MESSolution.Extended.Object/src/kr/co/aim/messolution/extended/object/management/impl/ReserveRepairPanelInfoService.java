package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.RecipeParameter;
import kr.co.aim.messolution.extended.object.management.data.ReserveRepairPanelInfo;
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

public class ReserveRepairPanelInfoService extends CTORMService<ReserveRepairPanelInfo> {
	public static Log logger = LogFactory.getLog(ReserveRepairPanelInfoService.class);

	public List<ReserveRepairPanelInfo> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<ReserveRepairPanelInfo> result = super.select(condition, bindSet, ReserveRepairPanelInfo.class);

		return result;
	}

	public ReserveRepairPanelInfo selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ReserveRepairPanelInfo.class, isLock, keySet);
	}

	public ReserveRepairPanelInfo create(EventInfo eventInfo, ReserveRepairPanelInfo dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<ReserveRepairPanelInfo> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);
	}

	public void remove(EventInfo eventInfo, ReserveRepairPanelInfo dataInfo) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
	}

	public void remove(EventInfo eventInfo, List<ReserveRepairPanelInfo> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfoList);
	}

	public ReserveRepairPanelInfo modify(EventInfo eventInfo, ReserveRepairPanelInfo dataInfo)
	{
		super.update(dataInfo);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void modify(EventInfo eventInfo, List<ReserveRepairPanelInfo> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.update(dataInfoList);
	}
}
