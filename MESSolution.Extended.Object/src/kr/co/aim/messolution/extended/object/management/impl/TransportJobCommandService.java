package kr.co.aim.messolution.extended.object.management.impl;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.TransportJobCommand;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class TransportJobCommandService extends CTORMService<TransportJobCommand> {
	
	public static Log logger = LogFactory.getLog(TransportJobCommandService.class);
	
	private final String historyEntity = "TransportJobCommandHist";
	
	public List<TransportJobCommand> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal
	{
		List<TransportJobCommand> result = new ArrayList<TransportJobCommand>();
		
		try
		{
			result = super.select(condition, bindSet, TransportJobCommand.class);
		}
		catch(greenFrameDBErrorSignal ne)
		{
			if (!ne.getErrorCode().equals("NotFoundSignal"))
				throw ne;
		}
		
		return result;
	}
	
	public TransportJobCommand selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(TransportJobCommand.class, isLock, keySet);
	}
	
	public TransportJobCommand create(EventInfo eventInfo, TransportJobCommand dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, TransportJobCommand dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		super.delete(dataInfo);
	}
	
	public TransportJobCommand modify(EventInfo eventInfo, TransportJobCommand dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);
		super.addHistory(eventInfo, historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
}
