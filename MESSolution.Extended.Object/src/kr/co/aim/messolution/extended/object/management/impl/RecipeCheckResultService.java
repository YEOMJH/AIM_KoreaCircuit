package kr.co.aim.messolution.extended.object.management.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.RecipeCheckResult;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;


public class RecipeCheckResultService extends CTORMService<RecipeCheckResult> {
	
	public static Log logger = LogFactory.getLog(RecipeCheckResult.class);
	
	private final String historyEntity = "RecipeCheckResultHistory";
	
	public List<RecipeCheckResult> select(String condition, Object[] bindSet)
			throws greenFrameDBErrorSignal
	{
		List<RecipeCheckResult> result = super.select(condition, bindSet, RecipeCheckResult.class);
		
		return result;
	}
	
	public RecipeCheckResult selectByKey(boolean isLock, Object[] keySet)
		throws greenFrameDBErrorSignal
	{
		return super.selectByKey(RecipeCheckResult.class, isLock, keySet);
	}
	/*
	public RecipeCheckResult create(EventInfo eventInfo, RecipeCheckResult dataInfo)
		throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);
	
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	*/
	public RecipeCheckResult create(EventInfo eventInfo, RecipeCheckResult dataInfo, String unitName)
			throws greenFrameDBErrorSignal 
	{
		super.insert(dataInfo);

		insertRecipeCheckResultHistory(eventInfo, dataInfo, unitName);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
/*	
	public void create(EventInfo eventInfo, List<RecipeCheckResult> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}

	public void remove(EventInfo eventInfo, RecipeCheckResult dataInfo)
		throws greenFrameDBErrorSignal
	{	
		super.delete(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
	}
*/	
	public void remove(EventInfo eventInfo, RecipeCheckResult dataInfo, String unitName)
			throws greenFrameDBErrorSignal
	{	
		super.delete(dataInfo);
			
		insertRecipeCheckResultHistory(eventInfo, dataInfo, unitName);
	}
/*
	public RecipeCheckResult modify(EventInfo eventInfo, RecipeCheckResult dataInfo)
	{
		super.update(dataInfo);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	
	public void modify(EventInfo eventInfo, List<RecipeCheckResult> dataInfoList)
	{
		super.update(dataInfoList);
		
		//super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}
	*/
	
	public RecipeCheckResult modify(EventInfo eventInfo, RecipeCheckResult dataInfo, String unitName)
	{
		super.update(dataInfo);
		
		insertRecipeCheckResultHistory(eventInfo, dataInfo, unitName);
		
		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}
	/*
	public void insertHistory(EventInfo eventInfo, RecipeCheckResult dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);
	}
	*/
	public void insertRecipeCheckResultHistory(EventInfo eventInfo, RecipeCheckResult dataInfo, String unitName)
	{
		String insertHistorySQL = "Insert into CT_RECIPECHECKRESULTHISTORY (TIMEKEY, MACHINENAME, PORTNAME, CARRIERNAME, CHECKLEVEL, RECIPENAME, ORIGINALSUBJECTNAME, UNITQTY, CHECKUNITQTY, SUBUNITQTY, "
									+"CHECKSUBUNITQTY, RESULT, RESULTCOMMENT, CREATETIMEKEY, UPDATETIMEKEY, EVENTTIME, EVENTCOMMENT, EVENTUSER, EVENTNAME, CURRENTCHECKCOMMENT, UNITNAME) "
									+"Values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		List<Object[]> insertArgListHistory = new ArrayList<Object[]>();

		// insert History
		List<Object> bindList = new ArrayList<Object>();
		bindList.add(eventInfo.getEventTimeKey());
		bindList.add(dataInfo.getMachineName());
		bindList.add(dataInfo.getPortName());
		bindList.add(dataInfo.getCarrierName());
		bindList.add(dataInfo.getCheckLevel());
		bindList.add(dataInfo.getRecipeName());
		bindList.add(dataInfo.getOriginalSubjectName());
		bindList.add(dataInfo.getUnitQty());
		bindList.add(dataInfo.getCheckUnitQty());
		bindList.add(0);
		bindList.add(0);
		bindList.add(dataInfo.getResult());
		bindList.add(dataInfo.getResultComment());
		bindList.add(dataInfo.getCreateTimeKey());
		bindList.add(dataInfo.getUpdateTimeKey());
		bindList.add(eventInfo.getEventTime());
		bindList.add(eventInfo.getEventComment());
		bindList.add(eventInfo.getEventUser());
		bindList.add(eventInfo.getEventName());
		bindList.add(dataInfo.getCurrentCheckComment());
		bindList.add(unitName);

		insertArgListHistory.add(bindList.toArray());

		GenericServiceProxy.getSqlMesTemplate().update(insertHistorySQL, insertArgListHistory.get(0));
	}
}
