package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.GlassJudge;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class GlassJudgeService extends CTORMService<GlassJudge> {
	
public static Log logger = LogFactory.getLog(GlassJudgeService.class);
	
	private final String historyEntity = "GlassJudgeHistory";
	
	public List<GlassJudge> select(String condition, Object[] bindSet)
		throws CustomException, NotFoundSignal
	{
		try
		{
			List<GlassJudge> result = super.select(condition, bindSet, GlassJudge.class);
			
			return result;
		}
		catch (greenFrameDBErrorSignal de)
		{
			if (de.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(de.getDataKey(), de.getSql());
			else
				throw new CustomException("SYS-8001", de.getSql());
		}
	}
	
	public GlassJudge selectByKey(boolean isLock, Object[] keySet)
		throws CustomException, NotFoundSignal
	{
		try
		{
			return super.selectByKey(GlassJudge.class, isLock, keySet);
		}
		catch (greenFrameDBErrorSignal de)
		{
			if (de.getErrorCode().equals("NotFoundSignal"))
				throw new NotFoundSignal(de.getDataKey(), de.getSql());
			else
				throw new CustomException("SYS-8001", de.getSql());
		}
	}
	
	public GlassJudge create(EventInfo eventInfo, GlassJudge dataInfo)
		throws CustomException
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, GlassJudge dataInfo)
		throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public GlassJudge modify(EventInfo eventInfo, GlassJudge dataInfo)
		throws CustomException
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 

}
