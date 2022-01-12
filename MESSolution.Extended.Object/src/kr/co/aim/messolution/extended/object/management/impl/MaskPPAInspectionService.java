package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MaskPPAInspection;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class MaskPPAInspectionService extends CTORMService<MaskPPAInspection> {
	
	public static Log logger = LogFactory.getLog(MaskPPAInspection.class);

	public List<MaskPPAInspection> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<MaskPPAInspection> result = super.select(condition, bindSet, MaskPPAInspection.class);
		return result;
	}

	public MaskPPAInspection selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MaskPPAInspection.class, isLock, keySet);
	}

	public MaskPPAInspection create(EventInfo eventInfo, MaskPPAInspection dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<MaskPPAInspection> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);
	}

	public void remove(EventInfo eventInfo, MaskPPAInspection dataInfo) throws greenFrameDBErrorSignal
	{
		super.delete(dataInfo);
	}

	public MaskPPAInspection modify(EventInfo eventInfo, MaskPPAInspection dataInfo)
	{
		super.update(dataInfo);
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
