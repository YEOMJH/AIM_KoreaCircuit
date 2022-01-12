package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MachineGroup;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class MachineGroupService extends CTORMService<MachineGroup> {
	
	public static Log logger = LogFactory.getLog(MachineGroupService.class);
	
	private final String historyEntity = "";
	
	public List<MachineGroup> select(String condition, Object[] bindSet)
		throws CustomException
	{
		try
		{
			List<MachineGroup> result = super.select(condition, bindSet, MachineGroup.class);
			
			return result;
		}
		catch (greenFrameDBErrorSignal de)
		{
			throw new CustomException("MACHINE-9198", de.getErrorCode(), de.getMessage());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-9199", fe.getMessage());
		}
	}
	
	public MachineGroup selectByKey(boolean isLock, Object[] keySet)
		throws CustomException
	{
		try
		{
			return super.selectByKey(MachineGroup.class, isLock, keySet);
		}
		catch (greenFrameDBErrorSignal de)
		{
			throw new CustomException("MACHINE-9198", de.getErrorCode(), de.getMessage());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-9199", fe.getMessage());
		}
	}
	
	public MachineGroup create(EventInfo eventInfo, MachineGroup dataInfo)
		throws CustomException
	{
		try
		{
			super.insert(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		catch (greenFrameDBErrorSignal de)
		{
			throw new CustomException("MACHINE-9198", de.getErrorCode(), de.getMessage());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-9199", fe.getMessage());
		}
	}
	
	public void remove(EventInfo eventInfo, MachineGroup dataInfo)
		throws CustomException
	{
		try
		{
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			super.delete(dataInfo);
		}
		catch (greenFrameDBErrorSignal de)
		{
			throw new CustomException("MACHINE-9198", de.getErrorCode(), de.getMessage());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-9199", fe.getMessage());
		}
	}
	
	public MachineGroup modify(EventInfo eventInfo, MachineGroup dataInfo)
		throws CustomException
	{
		try
		{
			super.update(dataInfo);
			
			super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
			
			return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
		}
		catch (greenFrameDBErrorSignal de)
		{
			throw new CustomException("MACHINE-9198", de.getErrorCode(), de.getMessage());
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-9199", fe.getMessage());
		}
	}
}
