package kr.co.aim.messolution.extended.object.management.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.EvaLineSchedule;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;



public class EvaLineScheduleService extends CTORMService<EvaLineSchedule> {
	
	private static Log log = LogFactory.getLog(EvaLineScheduleService.class);
	
	public static Log logger = LogFactory.getLog(EvaLineSchedule.class);
	
	private final String historyEntity = "EvaLineScheduleHistory";

	ConstantMap constantMap = GenericServiceProxy.getConstantMap();
	
	public List<EvaLineSchedule> select(String condition, Object[] bindSet)
			throws greenFrameDBErrorSignal
	{
		List<EvaLineSchedule> result = super.select(condition, bindSet, EvaLineSchedule.class);
		
		return result;
	}
	
	public EvaLineSchedule selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(EvaLineSchedule.class, isLock, keySet);
	}
	
	public EvaLineSchedule create(EventInfo eventInfo, EvaLineSchedule dataInfo)
		throws greenFrameDBErrorSignal
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		super.insert(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void create(EventInfo eventInfo, List<EvaLineSchedule> dataInfoList)
			throws greenFrameDBErrorSignal
	{		
		dataInfoList = setDataFromEventInfo(eventInfo, dataInfoList);
		
		super.insert(dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	public void remove(EventInfo eventInfo, EvaLineSchedule dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		super.delete(dataInfo);
	}

	public EvaLineSchedule modify(EventInfo eventInfo, EvaLineSchedule dataInfo)
	{
		dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		
		super.update(dataInfo);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void modify(EventInfo eventInfo, List<EvaLineSchedule> dataInfoList)
	{
		dataInfoList = setDataFromEventInfo(eventInfo, dataInfoList);
		
		super.update(dataInfoList);
		
		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	
	private EvaLineSchedule setDataFromEventInfo(EventInfo eventInfo, EvaLineSchedule dataInfo)
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
	
	private List<EvaLineSchedule> setDataFromEventInfo(EventInfo eventInfo, List<EvaLineSchedule> dataInfoList)
	{
		for(EvaLineSchedule dataInfo : dataInfoList)
		{
			dataInfo = setDataFromEventInfo(eventInfo, dataInfo);
		}
		
		return dataInfoList;
	}
	
	public boolean selectByKeyReturnTrueOrFalse(String machineName,String lineType,String productRequestName,String planStartDate)
			throws greenFrameDBErrorSignal
	{		
		String planNewTime = planStartDate.substring(0,planStartDate.length()-2);
		String sql = "SELECT MACHINENAME, LINETYPE, PRODUCTSPECNAME, SEQ, PRODUCTREQUESTNAME, STATE, PLANQUANTITY, PLANSTARTDATE, PLANENDDATE, USEFLAG, LASTEVENTNAME, LASTEVENTTIMEKEY, LASTEVENTTIME, LASTEVENTUSER, LASTEVENTCOMMENT,SCHEDULETIMEKEY "
				+ " FROM CT_EVALINESCHEDULE WHERE 1 = 1"
				+ " AND MACHINENAME = :MACHINENAME "
				+ " AND LINETYPE = :LINETYPE "
				+ " AND PRODUCTREQUESTNAME = :PRODUCTREQUESTNAME "
				+ " AND PLANSTARTDATE =  TO_DATE(:PLANSTARTDATE,'YYYY-MM-DD HH24:MI:SS')"; 
		Map<String, String> args = new HashMap<String, String>();
		args.put("MACHINENAME", machineName);
		args.put("LINETYPE", lineType);
		args.put("PRODUCTREQUESTNAME", productRequestName);
		args.put("PLANSTARTDATE", planNewTime);
		List<Map<String, Object>> result = greenFrameServiceProxy.getSqlTemplate().getSimpleJdbcTemplate().queryForList(sql, args);
		
		if (result.size() > 0)
		{
			return true;
		}
		
		return false;

	}

}
