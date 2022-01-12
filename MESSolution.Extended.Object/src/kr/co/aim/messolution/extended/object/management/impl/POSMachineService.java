package kr.co.aim.messolution.extended.object.management.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMServiceNoCT;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.POSMachine;
import kr.co.aim.messolution.extended.object.management.data.POSMachine;
import kr.co.aim.messolution.extended.object.management.data.STKConfig;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class POSMachineService extends CTORMServiceNoCT<POSMachine>
{

	public static Log logger = LogFactory.getLog(POSMachineService.class);
	
	private final String historyEntity = "POSMachineHistory";
	
	public POSMachine selectByKey(boolean isLock, Object[] keySet)
	throws greenFrameDBErrorSignal
	{
		return super.selectByKey(POSMachine.class, isLock, keySet);
	}	
	
	public List<POSMachine> select(String condition, Object[] bindSet)
	throws greenFrameDBErrorSignal
	{
		List<POSMachine> result = super.select(condition, bindSet, POSMachine.class);
		
		return result;
	}
	
	public void create(POSMachine dataInfo,EventInfo eventInfo)
	throws greenFrameDBErrorSignal, CustomException
	{
		super.insert(dataInfo);
		
		InsertPosMachineRecipeNameHistory(dataInfo, eventInfo);
		
	}
	
	public void modify(POSMachine dataInfo,EventInfo eventInfo)
	throws greenFrameDBErrorSignal, CustomException
	{
		super.update(dataInfo);
		
		InsertPosMachineRecipeNameHistory(dataInfo, eventInfo);
		
	}
	
	public void remove(POSMachine dataInfo,EventInfo eventInfo)
	throws greenFrameDBErrorSignal, CustomException
	{
		InsertPosMachineRecipeNameHistory(dataInfo, eventInfo);
		
		super.delete(dataInfo);
		
	}
	
	private void InsertPosMachineRecipeNameHistory(POSMachine dataInfo, EventInfo eventInfo) throws CustomException
	{	
		try
		{
			String sql = "INSERT INTO POSMACHINEHISTORY (CONDITIONID,MACHINENAME,ROLLTYPE,MACHINERECIPENAME,INT,MFG,AUTOCHANGEFLAG,CHECKLEVEL,TIMEKEY,EVENTUSER,EVENTNAME,EVENTCOMMENT, RMSFLAG, DISPATCHSTATE, AUTOCHANGELOTQUANTITY, AUTOCHANGETIME)"
					+ "VALUES(:CONDITIONID,:MACHINENAME,:ROLLTYPE,:MACHINERECIPENAME,'','','',:CHECKLEVEL,:TIMEKEY,:EVENTUSER,:EVENTNAME,:EVENTCOMMENT, :RMSFLAG, :DISPATCHSTATE, :AUTOCHANGELOTQUANTITY, :AUTOCHANGETIME)";
			
			Map<String,Object> bindMap = new HashMap<String,Object>();
			bindMap.put("CHECKLEVEL", dataInfo.getCheckLevel());
			bindMap.put("CONDITIONID", dataInfo.getConditionID());
			bindMap.put("MACHINENAME", dataInfo.getMachineName());
			bindMap.put("ROLLTYPE",dataInfo.getRollType());
			bindMap.put("MACHINERECIPENAME",dataInfo.getMachineRecipeName());
			bindMap.put("TIMEKEY", eventInfo.getEventTimeKey());
			bindMap.put("EVENTNAME", eventInfo.getEventName());
			bindMap.put("EVENTUSER", eventInfo.getEventUser());
			bindMap.put("EVENTCOMMENT", eventInfo.getEventComment());
			bindMap.put("RMSFLAG",dataInfo.getRmsFlag());
			bindMap.put("DISPATCHSTATE",dataInfo.getDisPatchState());
			bindMap.put("AUTOCHANGELOTQUANTITY",dataInfo.getAutoChangeLotQuantity());
			bindMap.put("AUTOCHANGETIME",dataInfo.getAutoChangeTime());
			
			int result = GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
			
			
		}
		catch(Exception e)
		{
			throw new CustomException("SYS-8001","for insert into POSMACHINEHISTORY   Error : " + e.toString());
		}
	}
	

		
		
}
