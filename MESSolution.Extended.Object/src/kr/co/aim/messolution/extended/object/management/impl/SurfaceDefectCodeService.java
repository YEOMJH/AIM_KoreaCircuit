package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.SurfaceDefectCode;
import kr.co.aim.messolution.extended.object.management.data.SurfaceDefectCode;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class SurfaceDefectCodeService extends CTORMService<SurfaceDefectCode> {

public static Log logger = LogFactory.getLog(SurfaceDefectCode.class);
	
	private final String historyEntity = "CT_SURFACEDEFECTCODEHISTORY";
	
	public List<SurfaceDefectCode> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<SurfaceDefectCode> result = super.select(condition, bindSet, SurfaceDefectCode.class);
		
		return result;
	}
	
	public SurfaceDefectCode selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(SurfaceDefectCode.class, isLock, keySet);
	}
	
	public SurfaceDefectCode create(EventInfo eventInfo, SurfaceDefectCode dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, SurfaceDefectCode dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public SurfaceDefectCode modify(EventInfo eventInfo, SurfaceDefectCode dataInfo)
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	
}
