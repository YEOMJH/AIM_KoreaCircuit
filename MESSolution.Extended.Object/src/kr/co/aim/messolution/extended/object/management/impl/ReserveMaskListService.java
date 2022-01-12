package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskList;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReserveMaskListService extends CTORMService<ReserveMaskList> {
	
	public static Log logger = LogFactory.getLog(ReserveLotService.class);
	
	private final String historyEntity = "ReserveMaskListHist";
	
	public List<ReserveMaskList> select(String condition, Object[] bindSet)
		throws greenFrameDBErrorSignal, CustomException
	{
		List<ReserveMaskList> result;
		try
		{
			result = super.select(condition, bindSet, ReserveMaskList.class);
		}
		catch (greenFrameDBErrorSignal de )
		{
			result = null;
			if(de.getErrorCode().equals("NotFoundSignal"))
			{
				throw new greenFrameDBErrorSignal("MASK-0009",de.getSql()); 
			}else 
			{
				throw new CustomException("SYS-8001",de.getSql()); 
			}
		}
		
		return result;
		
	}
	
	public ReserveMaskList selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(ReserveMaskList.class, isLock, keySet);
	}
	
	public ReserveMaskList create(EventInfo eventInfo, ReserveMaskList dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void remove(EventInfo eventInfo, ReserveMaskList dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}
	
	public ReserveMaskList modify(EventInfo eventInfo, ReserveMaskList dataInfo)
	{
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	} 

	public static List<ReserveMaskList> getMaskInfoByCarreierName(String carrierName) throws CustomException
	{ 
		String condition = "WHERE carrierName =? order by position";			
		Object[] bindSet = new Object[] {carrierName,};
		List<ReserveMaskList> maskList = new ArrayList<ReserveMaskList>();
		try
		{
			maskList = ExtendedObjectProxy.getReserveMaskService().select(condition, bindSet);
		}
		catch(greenFrameDBErrorSignal de)
		{
			maskList = null;
			
			if(de.getErrorCode().equals("NotFoundSignal"))
			{
				throw new NotFoundSignal(de.getDataKey(),de.getSql()); 
			}else 
			{
				throw new CustomException("SYS-8001",de.getSql()); 
			}
		}
		return maskList;
	}
	
	public static List<ReserveMaskList> getReservedCarreierName(String machineName,String portName,String maskName) throws CustomException
	{ 
		String condition = "WHERE machineName =? and portName =? and  maskName =?";
		Object[] bindSet = new Object[] {machineName,portName,maskName,};
		List<ReserveMaskList> result = new ArrayList<ReserveMaskList>();
		try
		{
			result = ExtendedObjectProxy.getReserveMaskService().select(condition, bindSet);
		}
		catch(greenFrameDBErrorSignal de)
		{
			result = null;
			
			if(de.getErrorCode().equals("NotFoundSignal"))
			{
				throw new NotFoundSignal(de.getDataKey(),de.getSql()); 
			}else 
			{
				throw new CustomException("SYS-8001",de.getSql()); 
			}
		}
		return result;
	}
	
	public static List<ReserveMaskList> getMaskInfoByCarreierName(String machineName,String portName,String carrierName) throws CustomException
	{ 
		String condition = "WHERE machineName =? and portName =? and  carrierName =? order by position";
		Object[] bindSet = new Object[] {machineName,portName,carrierName,};
		List<ReserveMaskList> maskList = new ArrayList<ReserveMaskList>();
		try
		{
			maskList = ExtendedObjectProxy.getReserveMaskService().select(condition, bindSet);
		}
		catch(greenFrameDBErrorSignal de)
		{
			maskList = null;
			
			if(de.getErrorCode().equals("NotFoundSignal"))
			{
				throw new NotFoundSignal(de.getDataKey(),de.getSql()); 
			}else 
			{
				throw new CustomException("SYS-8001",de.getSql()); 
			}
		}
		return maskList;
	}
}
