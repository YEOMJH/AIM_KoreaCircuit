package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.print.attribute.HashAttributeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaskStick;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.greenframe.exception.ErrorSignal;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import sun.net.www.content.text.Generic;

public class MaskStickService extends CTORMService<MaskStick>{
	
	public static Log logger = LogFactory.getLog(MaskStick.class);
	
	private final String historyEntity = "MaskStickHistory";
	
	public List<MaskStick> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<MaskStick> result = super.select(condition, bindSet, MaskStick.class);

		return result;
	}

	public MaskStick selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MaskStick.class, isLock, keySet);
	}

	public MaskStick create(EventInfo eventInfo, MaskStick dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void remove(EventInfo eventInfo, MaskStick dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public MaskStick modify(EventInfo eventInfo, MaskStick dataInfo)
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void modify(EventInfo eventInfo, List<MaskStick> dataInfoList)
	{
		dataInfoList = setDataFromEventInfo(eventInfo, dataInfoList);
		
		super.update(dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	private List<MaskStick> setDataFromEventInfo(EventInfo eventInfo, List<MaskStick> dataInfoList)
	{
		for(MaskStick dataInfo : dataInfoList)
		{
			dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		}
		
		return dataInfoList;
	}
	
	private MaskStick setDataFromEventInfo(EventInfo eventInfo, MaskStick dataInfo)
	{
		String eventTimekey = null;
		if(StringUtil.isNotEmpty(eventInfo.getLastEventTimekey()))
		{
			eventTimekey = eventInfo.getLastEventTimekey();
		}
		else if(StringUtil.isNotEmpty(eventInfo.getEventTimeKey()))
		{
			eventTimekey = eventInfo.getEventTimeKey();
		}
		else
		{
			eventTimekey = TimeStampUtil.getCurrentEventTimeKey();
		}
		
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventTimeKey(eventTimekey);
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		
		return dataInfo;
	}
	
	public List<MaskStick> getAssignedStickList(String maskLotName) throws CustomException
	{
		if (maskLotName == null || maskLotName.isEmpty())
		{
			//SYSTEM-0011:{0} The incoming variable value can not be empty or null!!
			throw new CustomException("SYSTEM-0011",Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		
		List<MaskStick> stickDataList = new ArrayList<>();
		
		try
		{
			stickDataList = this.select(" where 1=1 and masklotname = ?  and stickstate = 'Inuse' and position is not null  ", new Object[]{maskLotName});
		}
		catch (greenFrameDBErrorSignal dbEx)
		{
			if (!dbEx.getErrorCode().equals(ErrorSignal.NotFoundSignal))
				throw new CustomException(dbEx.getCause());
		}
		catch(Exception ex)
		{
			throw new CustomException(ex.getCause());
		}
		
		return stickDataList;
	}
	
	public void checkSpecRule(String maskName, String stickName) throws CustomException
	{
		String maskProductCode = maskName.substring(1,3);
		String stickProductCode = stickName.substring(1,3);
		
		String maskFilmLayer = maskName.substring(10,12);
		String stickFilmLayer = stickName.substring(4,6);
		String stickType =stickName.substring(3,4);
		
		//STICK-0004:ProductCode of [0] and [{1}] do not match.
		if(!maskProductCode.equals(stickProductCode))
			throw new CustomException("STICK-0004","MaskName = " + maskName ,"StickName" + stickName);
		
		String sql = " SELECT 1 FROM CT_MASKSTICKRULE "
				+ " WHERE MASKFILMLAYER = :MASKFILMLAYER"
				+ " AND STICKTYPE =:STICKTYPE "
				+ " AND STICKFILMLAYER =:STICKFILMLAYER ";
		
		Map<String,Object> bindMap = new HashMap<String, Object>();
		bindMap.put("MASKFILMLAYER", maskFilmLayer);
		bindMap.put("STICKTYPE", stickType);
		bindMap.put("STICKFILMLAYER", stickFilmLayer);
		
		List<Map<String, Object>> resultList = null;
		try
		{
			resultList = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
		}
		catch (Exception ex)
		{
			logger.info(ex.getMessage());
		}
		//STICK-0005:Could not find rule information registered as [{0}] and  [{1}] in [{2}] table.
		if(resultList ==null || resultList.size()==0)
			throw new CustomException("STICK-0005","MaskName = " + maskName ,"StickName" + stickName,"CT_MASKSTICKRULE");
	}
	
	public void stickCommonCheck(MaskStick maskStick,boolean IsInspectionOper) throws CustomException
	{
		if (maskStick == null)
		{
			// SYSTEM-0011:{0} The incoming variable value can not be empty or null!!
			throw new CustomException("SYSTEM-0011",Thread.currentThread().getStackTrace()[1].getMethodName());
		}

		checkStickJudge(maskStick);

		if (IsInspectionOper)
		{
			checkStickStateInISP(maskStick);
		}
		else
		{
			checkStickState(maskStick);
		}
	}
	
	public void checkStickJudge(MaskStick maskStick) throws CustomException
	{
		if (maskStick == null)
		{
			//SYSTEM-0011:{0}: The incoming variable value can not be empty or null!!
			throw new CustomException("SYSTEM-0011",Thread.currentThread().getStackTrace()[1].getMethodName());
		}

		ConstantMap constMap = GenericServiceProxy.getConstantMap();

		if (!maskStick.getStickJudge().equals(constMap.Stick_Judge_G))
			throw new CustomException("STICK-0003", maskStick.getStickName(),maskStick.getStickJudge(),constMap.Stick_Judge_G);
	}
	
	public void checkStickStateInISP(MaskStick maskStick) throws CustomException
	{
		if (maskStick == null)
		{
			//SYSTEM-0011:{0}: The incoming variable value can not be empty or null!!
			throw new CustomException("SYSTEM-0011",Thread.currentThread().getStackTrace()[1].getMethodName());
		}

		ConstantMap constMap = GenericServiceProxy.getConstantMap();

		if (maskStick.getStickType().equals(constMap.Stick_Type_F))
		{
			if (!maskStick.getStickState().equals(constMap.Stick_State_NotAvailable))
				throw new CustomException("STICK-0002", maskStick.getStickName(), String.format("Type = %s,State = %s", maskStick.getType(), maskStick.getStickState()));
		}
		else
		{
			if (!maskStick.getStickState().equals(constMap.Stick_State_Available))
				throw new CustomException("STICK-0002", maskStick.getStickName(), String.format("Type = %s,State = %s", maskStick.getType(), maskStick.getStickState()));
		}
	}
	
	public void checkStickState(MaskStick maskStick) throws CustomException
	{
		if (maskStick == null)
		{
			//SYSTEM-0011:{0}: The incoming variable value can not be empty or null!!
			throw new CustomException("SYSTEM-0011",Thread.currentThread().getStackTrace()[1].getMethodName());
		}

		ConstantMap constMap = GenericServiceProxy.getConstantMap();

		if (!maskStick.getStickState().equals(constMap.Stick_State_Available))
			throw new CustomException("STICK-0002", maskStick.getStickName(), maskStick.getStickState());
	}

	public MaskStick getMaskStickData(String stickName) throws CustomException
	{
		if (StringUtil.isEmpty(stickName))
		{
			//SYSTEM-0010:{0} The incoming variable value can not be empty!!
			throw new CustomException("SYSTEM-0010",Thread.currentThread().getStackTrace()[1].getMethodName());
		}

		MaskStick dataInfo = null;
		try
		{
			dataInfo = this.selectByKey(false, new Object[] { stickName });
		}
		catch (greenFrameDBErrorSignal dbEx)
		{
			if (dbEx.getErrorCode().equals(ErrorSignal.NotFoundSignal))
				throw new CustomException("COMM-1000", "CT_MASKSTICK", "StickName = " + stickName);
			else 
				throw new CustomException(dbEx.getCause());
		}
		catch(Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		return dataInfo;
	}
	
	public MaskStick traceStickData(String maskLotName,String stickType, String position) throws CustomException
	{
		if (StringUtil.in(StringUtil.EMPTY, maskLotName,stickType,position))
		{
			//SYSTEM-0010:{0} The incoming variable value can not be empty!!
			throw new CustomException("SYSTEM-0010",Thread.currentThread().getStackTrace()[1].getMethodName());
		}
		
		List<MaskStick> dataList = null;
		
		try
		{
			dataList = this.select(" where 1=1 and masklotname = ? and sticktype = ? and stickstate ='InUse' and position = ? ", new Object[]{maskLotName,stickType,position});
		}
		catch (greenFrameDBErrorSignal dbEx)
		{
			if (dbEx.getErrorCode().equals(ErrorSignal.NotFoundSignal))
				throw new CustomException("COMM-1000", "CT_MASKSTICK", String.format("MaskLotName = %s, StickType = %s,Position = %s", maskLotName,stickType,position));
			else 
				throw new CustomException(dbEx.getCause());
		}
		catch(Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		return dataList.get(0);
	}
}
