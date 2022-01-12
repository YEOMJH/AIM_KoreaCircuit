package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.MaskFutureAction;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;

public class MaskFutureActionService extends CTORMService<MaskFutureAction> {
	public static Log logger = LogFactory.getLog(MaskFutureActionService.class);

	private final String historyEntity = "MaskFutureActionHistory";

	public List<MaskFutureAction> select(String condition, Object[] bindSet) throws greenFrameDBErrorSignal
	{
		List<MaskFutureAction> result = super.select(condition, bindSet, MaskFutureAction.class);

		return result;
	}

	public MaskFutureAction selectByKey(boolean isLock, Object[] keySet) throws greenFrameDBErrorSignal
	{
		return super.selectByKey(MaskFutureAction.class, isLock, keySet);
	}

	public MaskFutureAction create(EventInfo eventInfo, MaskFutureAction dataInfo) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void create(EventInfo eventInfo, List<MaskFutureAction> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.insert(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}

	public MaskFutureAction modify(EventInfo eventInfo, MaskFutureAction dataInfo) throws greenFrameDBErrorSignal
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public void modify(EventInfo eventInfo, List<MaskFutureAction> dataInfoList) throws greenFrameDBErrorSignal
	{
		super.update(dataInfoList);

		super.addHistory(eventInfo, this.historyEntity, dataInfoList, logger);
	}

	public void remove(EventInfo eventInfo, MaskFutureAction dataInfo) throws greenFrameDBErrorSignal
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}
	
	public List<MaskFutureAction> getMaskFutureActionList(String maskName, String factoryName, 
			String processFlowName, String processFlowVersion, String processOperationName, String processOperationVersion,
			int position) throws greenFrameDBErrorSignal
	{
		String condition = " WHERE MASKLOTNAME = ? AND FACTORYNAME = ? AND PROCESSFLOWNAME = ? AND PROCESSFLOWVERSION = ? AND PROCESSOPERATIONNAME = ? AND PROCESSOPERATIONVERSION = ? AND POSITION = ? ";
		Object[] bindSet = new Object[] { maskName, factoryName, processFlowName, processFlowVersion, processOperationName, processOperationVersion, 0 };
		List<MaskFutureAction> futureActionList = select(condition, bindSet);
		
		return futureActionList;
	}
}
