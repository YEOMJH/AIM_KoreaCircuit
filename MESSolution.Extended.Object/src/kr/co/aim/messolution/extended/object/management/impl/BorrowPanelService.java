package kr.co.aim.messolution.extended.object.management.impl;

import java.util.List;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.CTORMService;
import kr.co.aim.messolution.extended.object.management.CTORMUtil;
import kr.co.aim.messolution.extended.object.management.data.BorrowPanel;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BorrowPanelService extends CTORMService<BorrowPanel> {

	public static Log logger = LogFactory.getLog(BorrowPanelService.class);

	private final String historyEntity = "BorrowPanelHistory";

	public List<BorrowPanel> select(String condition, Object[] bindSet) throws CustomException
	{
		List<BorrowPanel> result = super.select(condition, bindSet, BorrowPanel.class);

		return result;
	}

	public BorrowPanel selectByKey(boolean isLock, Object[] keySet) throws CustomException
	{
		return super.selectByKey(BorrowPanel.class, isLock, keySet);
	}

	public boolean create(EventInfo eventInfo, BorrowPanel dataInfo) throws CustomException
	{

		super.insert(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return true;
	}

	public void remove(EventInfo eventInfo, BorrowPanel dataInfo) throws CustomException
	{
		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		super.delete(dataInfo);
	}

	public BorrowPanel modify(EventInfo eventInfo, BorrowPanel dataInfo) throws CustomException
	{
		super.update(dataInfo);

		super.addHistory(eventInfo, this.historyEntity, dataInfo, logger);

		return selectByKey(false, CTORMUtil.makeKeyParam(dataInfo).toArray());
	}

	public BorrowPanel getBorrowPanelData(String taskID, String lotName)
	{
		BorrowPanel dataInfo = new BorrowPanel();

		try
		{
			dataInfo = this.selectByKey(false, new Object[] { taskID, lotName });
		}
		catch (Exception e)
		{
			logger.info("BorrowPanel Data is not exist : " + taskID + ", " + lotName);
			dataInfo = null;
		}

		return dataInfo;
	}

	public void deleteBorrowPanel(EventInfo eventInfo, String taskID, String lotName, String state) throws CustomException
	{
		BorrowPanel dataInfo = this.getBorrowPanelData(taskID, lotName);

		if (dataInfo == null)
		{
			throw new CustomException("BORROW-0001", taskID, lotName);
		}

		if (!StringUtils.equals(dataInfo.getBorrowState(), state))
		{
			throw new CustomException("BORROW-0002", state);
		}

		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

		this.remove(eventInfo, dataInfo);
	}

	public BorrowPanel increaseRenuwCount(EventInfo eventInfo, String taskID, String lotName) throws CustomException
	{
		BorrowPanel dataInfo = ExtendedObjectProxy.getBorrowPanelService().getBorrowPanelData(taskID, lotName);

		if (dataInfo == null)
			throw new CustomException("BORROW-0001", taskID, lotName);

		if (!StringUtils.equals(dataInfo.getBorrowState(), "Borrowed"))
			throw new CustomException("BORROW-0002", "Borrowed");

		String renewCount = dataInfo.getRenewCount().toString();

		if (StringUtils.isEmpty(renewCount))
			renewCount = "0";

		int iRenewCount = Integer.parseInt(renewCount) + 1;

		if (iRenewCount > 1)
			throw new CustomException("BORROW-0004", 1);

		dataInfo.setRenewCount(iRenewCount);
		dataInfo.setRenewDate(eventInfo.getEventTime());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

		dataInfo = this.modify(eventInfo, dataInfo);

		return dataInfo;
	}

	public BorrowPanel changeState(EventInfo eventInfo, BorrowPanel dataInfo, String state) throws CustomException
	{
		dataInfo.setBorrowState(state);

		if (StringUtils.equals(state, GenericServiceProxy.getConstantMap().Borrow_Borrowed))
		{
			dataInfo.setBorrowDate(eventInfo.getEventTime());
		}

		if (StringUtils.equals(state, GenericServiceProxy.getConstantMap().Borrow_Completed))
		{
			dataInfo.setReturnDate(eventInfo.getEventTime());
		}

		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

		dataInfo = this.modify(eventInfo, dataInfo);

		return dataInfo;
	}

	public BorrowPanel changePanelOutFlag(EventInfo eventInfo, BorrowPanel dataInfo) throws CustomException
	{
		dataInfo.setPanelOutFlag("Y");
		//jinlj 12/22
		dataInfo.setBorrowDate(eventInfo.getEventTime());
		dataInfo.setBorrowState("Borrowed");

		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
		dataInfo = this.modify(eventInfo, dataInfo);

		return dataInfo;
	}
}
