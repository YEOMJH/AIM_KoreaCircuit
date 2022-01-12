package kr.co.aim.messolution.durable.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ReserveMaskList;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.durable.management.data.DurableHistoryKey;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.AssignTransportGroupInfo;
import kr.co.aim.greentrack.durable.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.durable.management.info.CleanInfo;
import kr.co.aim.greentrack.durable.management.info.CreateInfo;
import kr.co.aim.greentrack.durable.management.info.DeassignTransportGroupInfo;
import kr.co.aim.greentrack.durable.management.info.DecrementDurationUsedInfo;
import kr.co.aim.greentrack.durable.management.info.DecrementTimeUsedInfo;
import kr.co.aim.greentrack.durable.management.info.DirtyInfo;
import kr.co.aim.greentrack.durable.management.info.IncrementDurationUsedInfo;
import kr.co.aim.greentrack.durable.management.info.IncrementTimeUsedInfo;
import kr.co.aim.greentrack.durable.management.info.MakeAvailableInfo;
import kr.co.aim.greentrack.durable.management.info.MakeInUseInfo;
import kr.co.aim.greentrack.durable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.durable.management.info.MakeNotInUseInfo;
import kr.co.aim.greentrack.durable.management.info.MakeScrappedInfo;
import kr.co.aim.greentrack.durable.management.info.RepairInfo;
import kr.co.aim.greentrack.durable.management.info.SetAreaInfo;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.management.info.UndoInfo;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.generic.info.UndoTimeKeys;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DurableServiceImpl implements ApplicationContextAware {
	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(DurableServiceImpl.class);

	@Override
	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		applicationContext = arg0;
	}

	public void assignTransportGroup(Durable durableData, AssignTransportGroupInfo assignTransportGroupInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal,
			NotFoundSignal, DuplicateNameSignal
	{
		DurableServiceProxy.getDurableService().assignTransportGroup(durableData.getKey(), eventInfo, assignTransportGroupInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void changeSpec(Durable durableData, ChangeSpecInfo changeSpecInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{

		DurableServiceProxy.getDurableService().changeSpec(durableData.getKey(), eventInfo, changeSpecInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void clean(Durable durableData, CleanInfo cleanInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{

		DurableServiceProxy.getDurableService().clean(durableData.getKey(), eventInfo, cleanInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public Durable create(String durableName, CreateInfo createInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{

		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(durableName);

		Map<String, String> udfs = createInfo.getUdfs();
		if (udfs.isEmpty())
		{
			udfs = new HashMap<String, String>();
		}
		udfs.put("DURABLETYPE1", createInfo.getDurableType());
		createInfo.setUdfs(udfs);
		Durable durableData = DurableServiceProxy.getDurableService().create(durableKey, eventInfo, createInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());

		return durableData;
	}

	public void cancelCreateDurable(Durable oldData, EventInfo eventInfo)
	{
		// Set new Durable Data
		Durable newData = new Durable();
		ObjectUtil.copyField(oldData, newData);

		newData.setLastEventTimeKey(eventInfo.getEventTimeKey());

		// Set DurableHistory Data
		DurableHistory historyData = new DurableHistory();
		historyData = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldData, newData, historyData);
		historyData.setEventName(eventInfo.getEventName());
		historyData.setEventComment(eventInfo.getEventComment());
		historyData.setEventTime(eventInfo.getEventTime());
		historyData.setEventUser(eventInfo.getEventUser());
		historyData.setCancelFlag("R");
		historyData.setCancelTimeKey(newData.getLastEventTimeKey());

		// CancelCreate
		DurableServiceProxy.getDurableHistoryService().insert(historyData);
		DurableServiceProxy.getDurableService().delete(newData.getKey());
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey = " + eventInfo.getEventTimeKey());
	}

	public void deassignTransportGroup(Durable durableData, DeassignTransportGroupInfo deassignTransportGroupInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal,
			NotFoundSignal, DuplicateNameSignal
	{

		DurableServiceProxy.getDurableService().deassignTransportGroup(durableData.getKey(), eventInfo, deassignTransportGroupInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void decrementDurationUsed(Durable durableData, DecrementDurationUsedInfo decrementDurationUsedInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal,
			NotFoundSignal, DuplicateNameSignal
	{

		DurableServiceProxy.getDurableService().decrementDurationUsed(durableData.getKey(), eventInfo, decrementDurationUsedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void decrementTimeUsed(Durable durableData, DecrementTimeUsedInfo decrementTimeUsedInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal
	{

		DurableServiceProxy.getDurableService().decrementTimeUsed(durableData.getKey(), eventInfo, decrementTimeUsedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void dirty(Durable durableData, DirtyInfo dirtyInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{

		DurableServiceProxy.getDurableService().dirty(durableData.getKey(), eventInfo, dirtyInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void incrementDurationUsed(Durable durableData, IncrementDurationUsedInfo incrementDurationUsedInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal,
			NotFoundSignal, DuplicateNameSignal
	{

		DurableServiceProxy.getDurableService().incrementDurationUsed(durableData.getKey(), eventInfo, incrementDurationUsedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void incrementTimeUsed(Durable durableData, IncrementTimeUsedInfo incrementTimeUsedInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal
	{

		DurableServiceProxy.getDurableService().incrementTimeUsed(durableData.getKey(), eventInfo, incrementTimeUsedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void makeAvailable(Durable durableData, MakeAvailableInfo makeAvailableInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal
	{

		DurableServiceProxy.getDurableService().makeAvailable(durableData.getKey(), eventInfo, makeAvailableInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void makeInUse(Durable durableData, MakeInUseInfo makeInUseInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{

		DurableServiceProxy.getDurableService().makeInUse(durableData.getKey(), eventInfo, makeInUseInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void makeNotAvailable(Durable durableData, MakeNotAvailableInfo makeNotAvailableInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal
	{

		DurableServiceProxy.getDurableService().makeNotAvailable(durableData.getKey(), eventInfo, makeNotAvailableInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void makeNotInUse(Durable durableData, MakeNotInUseInfo makeNotInUseInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal
	{

		DurableServiceProxy.getDurableService().makeNotInUse(durableData.getKey(), eventInfo, makeNotInUseInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void makeScrapped(Durable durableData, MakeScrappedInfo makeScrappedInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal,
			DuplicateNameSignal
	{

		DurableServiceProxy.getDurableService().makeScrapped(durableData.getKey(), eventInfo, makeScrappedInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void repair(Durable durableData, RepairInfo repairInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{

		DurableServiceProxy.getDurableService().repair(durableData.getKey(), eventInfo, repairInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void setArea(Durable durableData, SetAreaInfo setAreaInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{

		DurableServiceProxy.getDurableService().setArea(durableData.getKey(), eventInfo, setAreaInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void setEvent(Durable durableData, SetEventInfo setEventInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{

		DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void setMaterialLocation(Durable durableData, SetMaterialLocationInfo setMaterialLocationInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal,
			NotFoundSignal, DuplicateNameSignal
	{

		DurableServiceProxy.getDurableService().setMaterialLocation(durableData.getKey(), eventInfo, setMaterialLocationInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void undo(Durable durableData, UndoInfo undoInfo, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal
	{

		UndoTimeKeys undoTimeKeys = new UndoTimeKeys();
		undoTimeKeys = DurableServiceProxy.getDurableService().undo(durableData.getKey(), eventInfo, undoInfo);

		try
		{
			durableData = DurableServiceProxy.getDurableService().selectByKey(durableData.getKey());
			DurableHistoryKey keyInfo = new DurableHistoryKey();
			keyInfo.setDurableName(durableData.getKey().getDurableName());
			keyInfo.setTimeKey(undoTimeKeys.getRemarkedTimeKey());

			DurableHistory durableHistory = new DurableHistory();

			durableHistory = DurableServiceProxy.getDurableHistoryService().selectByKey(keyInfo);
			durableHistory.setEventComment(eventInfo.getEventComment());

			DurableServiceProxy.getDurableHistoryService().update(durableHistory);
		}
		catch (Exception e)
		{
			DurableHistoryKey keyInfo = new DurableHistoryKey();
			keyInfo.setDurableName(durableData.getKey().getDurableName());
			keyInfo.setTimeKey(undoTimeKeys.getRemarkedTimeKey());

			DurableHistory durableHistory = new DurableHistory();
			durableHistory = DurableServiceProxy.getDurableHistoryService().selectByKey(keyInfo);
			durableHistory.setEventComment(eventInfo.getEventComment());

			DurableServiceProxy.getDurableHistoryService().update(durableHistory);
		}
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void makeTransportStateMoving(EventInfo eventInfo, String carrierName) throws CustomException
	{
		Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

		SetAreaInfo setAreaInfo = new SetAreaInfo();

		setAreaInfo.setAreaName("");

		Map<String, String> carrierUdfs = new HashMap<String, String>();
		carrierUdfs.put("MACHINENAME", "");
		carrierUdfs.put("PORTNAME", "");
		carrierUdfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_MOVING);
		carrierUdfs.put("POSITIONTYPE", "");
		carrierUdfs.put("POSITIONNAME", "");
		carrierUdfs.put("ZONENAME", "");
		setAreaInfo.setUdfs(carrierUdfs);

		eventInfo = EventInfoUtil.makeEventInfo(eventInfo.getEventName(), eventInfo.getEventUser(), eventInfo.getEventComment(), null, null);

		// 5. Execute setArea
		DurableServiceProxy.getDurableService().setArea(carrierData.getKey(), eventInfo, setAreaInfo);
	}

	public void makeTransportStateOnEQP(EventInfo eventInfo, String carrierName, String machineName, String portName) throws CustomException
	{
		Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

		SetAreaInfo setAreaInfo = new SetAreaInfo();
		setAreaInfo.setAreaName("");

		Map<String, String> carrierUdfs = new HashMap<String, String>();
		carrierUdfs.put("MACHINENAME", machineName);
		carrierUdfs.put("PORTNAME", portName);
		carrierUdfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_ONEQP);
		carrierUdfs.put("POSITIONTYPE", "PORT");
		carrierUdfs.put("POSITIONNAME", "");
		carrierUdfs.put("ZONENAME", "");
		setAreaInfo.setUdfs(carrierUdfs);

		DurableServiceProxy.getDurableService().setArea(carrierData.getKey(), eventInfo, setAreaInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void makeTransportStateOnEQPAndDirty(EventInfo eventInfo, String carrierName, String machineName, String portName) throws CustomException
	{
		Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		
		DirtyInfo dirtyInfo = new DirtyInfo();
		
		Map<String, String> carrierUdfs = new HashMap<String, String>();
		carrierUdfs.put("MACHINENAME", machineName);
		carrierUdfs.put("PORTNAME", portName);
		carrierUdfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_ONEQP);
		carrierUdfs.put("POSITIONTYPE", "PORT");
		carrierUdfs.put("POSITIONNAME", "");
		carrierUdfs.put("ZONENAME", "");
		
		dirtyInfo.setUdfs(carrierUdfs);
		
		MESDurableServiceProxy.getDurableServiceImpl().dirty(carrierData, dirtyInfo, eventInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}
	
	public boolean checkExistDurable(String durableName) throws CustomException
	{

		boolean existDurableName = false;

		String condition = "DURABLENAME = ?";

		Object[] bindSet = new Object[] { durableName };

		try
		{
			List<Durable> sqlResult = DurableServiceProxy.getDurableService().select(condition, bindSet);
			if (sqlResult.size() > 0)
			{
				throw new CustomException("MASK-0001", durableName);

			}
			return existDurableName = false;
		}
		catch (Exception e)
		{
			return existDurableName = false;
		}
	}

	public SetAreaInfo makeEVAMaskCSTTransportStateOnEQP(EventInfo eventInfo, Durable durableData, String machineName, String portName) throws CustomException
	{
		SetAreaInfo setAreaInfo = new SetAreaInfo();

		setAreaInfo.setAreaName("");
		Map<String, String> carrierUdfs = new HashMap<String, String>();
		carrierUdfs.put("MACHINENAME", machineName);
		carrierUdfs.put("PORTNAME", portName);
		carrierUdfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_ONEQP);
		carrierUdfs.put("POSITIONTYPE", "PORT");
		carrierUdfs.put("POSITIONNAME", "");
		carrierUdfs.put("ZONENAME", "");
		setAreaInfo.setUdfs(carrierUdfs);

		return setAreaInfo;
	}

	public SetAreaInfo makeTransportStateOutSTK(EventInfo eventInfo, String carrierName, String machineName, String portName) throws CustomException
	{
		Durable carrierData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);

		SetAreaInfo setAreaInfo = new SetAreaInfo();

		setAreaInfo.setAreaName("");

		Map<String, String> carrierUdfs = new HashMap<String, String>();
		carrierUdfs.put("MACHINENAME", "");
		carrierUdfs.put("PORTNAME", "");
		carrierUdfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_OUTSTK);
		carrierUdfs.put("POSITIONTYPE", "");
		carrierUdfs.put("POSITIONNAME", "");
		carrierUdfs.put("ZONENAME", "");
		setAreaInfo.setUdfs(carrierUdfs);

		return setAreaInfo;
	}

	public static List<ReserveMaskList> checkReserveMask(String maskName, String carrierName, String subUnitName) throws CustomException
	{
		String condition = "WHERE maskName = : maskName and carrierName = :carrierName and subUnitName = :subUnitName ";

		Object[] bindSet = new Object[] { maskName, carrierName, subUnitName };
		List<ReserveMaskList> maskList = null;
		try
		{
			maskList = ExtendedObjectProxy.getReserveMaskService().select(condition, bindSet);
		}
		catch (Exception e)
		{
			throw new CustomException("MASK-0007", maskName);
		}
		return maskList;
	}

	public void makeUnScrap(Durable durableData, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{

		String sql = " UPDATE DURABLE SET DURABLESTATE = :durableState WHERE DURABLENAME = :durableName ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("durableState", "Available");
		bindMap.put("durableName", durableData.getKey().getDurableName());

		try
		{
			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", e.getMessage());
		}

		SetEventInfo setEventInfo = new SetEventInfo();
		DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void makePhotoMaskUnScrap(Durable durableData, EventInfo eventInfo) throws InvalidStateTransitionSignal, FrameworkErrorSignal, NotFoundSignal, DuplicateNameSignal, CustomException
	{

		String sql = " UPDATE DURABLE SET DURABLESTATE = :durableState, TRANSPORTSTATE = :transportState WHERE DURABLENAME = :durableName ";

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("durableState", "UnMounted");
		bindMap.put("transportState", GenericServiceProxy.getConstantMap().Dur_INSTK);
		bindMap.put("durableName", durableData.getKey().getDurableName());

		try
		{
			GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", e.getMessage());
		}

		SetEventInfo setEventInfo = new SetEventInfo();
		DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey" + eventInfo.getEventTimeKey());
	}

	public void incrementDurableTimeUseCount(Durable durableData, int timeUseCount, EventInfo eventInfo)
	{
		eventInfo.setEventName("Increment");

		IncrementTimeUsedInfo incrementTimeUsedInfo = new IncrementTimeUsedInfo();
		incrementTimeUsedInfo.setTimeUsed(timeUseCount);
		MESDurableServiceProxy.getDurableServiceImpl().incrementTimeUsed(durableData, incrementTimeUsedInfo, eventInfo);

		// Check Qty & Durable not available
		if ((durableData.getTimeUsed() + timeUseCount) >= durableData.getTimeUsedLimit())
		{
			eventInfo.setEventName("ChangeState");
			MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
			MESDurableServiceProxy.getDurableServiceImpl().makeNotAvailable(durableData, makeNotAvailableInfo, eventInfo);
		}
	}
	
	/**
	 * Mantis : 0000440
	 * 所有Mask CST，当TimeUseCount>TimeUseCountLimit时，MaskCST变Dirty
	 * 
	 * 2021-01-11	dhko	Add Function
	 */
	public void incrementTimeUseCount(Durable durableData, int timeUseCount, EventInfo eventInfo)
	{
		eventInfo.setEventName("Increment");

		IncrementTimeUsedInfo incrementTimeUsedInfo = new IncrementTimeUsedInfo();
		incrementTimeUsedInfo.setTimeUsed(timeUseCount);
		MESDurableServiceProxy.getDurableServiceImpl().incrementTimeUsed(durableData, incrementTimeUsedInfo, eventInfo);
	}
	
	public void incrementMaskTimeUseCount(String maskName, int timeUseCount, EventInfo eventInfo) throws CustomException
	{
		eventInfo.setEventName("Increment");

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(maskName);
		durableData.setTimeUsed(durableData.getTimeUsed() + timeUseCount);
		DurableServiceProxy.getDurableService().update(durableData);

		SetEventInfo setEvent = new SetEventInfo();
		MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEvent, eventInfo);

		// Check Qty & Durable Hold
		if (durableData.getTimeUsed() >= durableData.getTimeUsedLimit())
		{
			eventInfo.setEventName("Hold");

			SetEventInfo setEventInfo = new SetEventInfo();
			Map<String, String> durableUdfs = new HashMap<String, String>();
			durableUdfs.put("DURABLEHOLDSTATE", "Y");
			setEventInfo.setUdfs(durableUdfs);

			MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
		}
	}

	public void durableStateChangeAfterOLEDMaskLotProcess(String durableName, String durableState, String lotQuantity, EventInfo eventInfo) throws CustomException
	{
		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(durableName);
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		// Variable DurableState ['ASSING','DEASSIN'] aren't states,
		// but just processFlag
		// When DurableState change to InUse (example :Assign)
		if (durableState.equals("ASSIGN"))
		{
			// Change DurableInfo
			if (durableData.getLotQuantity() == 0)
			{
				durableData.setDurableState(constantMap.Dur_InUse);
			}

			// MaskLots are assigned maximum cappcity of carrier
			if (durableData.getLotQuantity() + Long.parseLong(lotQuantity) > durableData.getCapacity())
			{
				throw new CustomException("CST-0013", durableName);
			}
			else
			{
				durableData.setLotQuantity(durableData.getLotQuantity() + Long.parseLong(lotQuantity));
			}
		}

		// When DurableState change to InUse (example :DeAssign), if durable
		// QTY is 0 , durableState change to Available
		if (durableState.equals("DEASSIGN"))
		{
			if (durableData.getLotQuantity() - Long.parseLong(lotQuantity) == 0)
			{
				durableData.setLotQuantity(0);
				durableData.setDurableState(constantMap.Dur_Available);
			}
			else if (durableData.getLotQuantity() - Long.parseLong(lotQuantity) < 0)
			{
				throw new CustomException("CST-0014", durableName);
			}
			else
			{
				durableData.setLotQuantity(durableData.getLotQuantity() - Long.parseLong(lotQuantity));
			}
		}

		DurableServiceProxy.getDurableService().update(durableData);
		SetEventInfo setEventInfo = new SetEventInfo();

		DurableServiceProxy.getDurableService().setEvent(durableKey, eventInfo, setEventInfo);

		log.info("Event time = " + eventInfo.getEventTime() + " , EventTimeKey = " + eventInfo.getEventTimeKey());
	}

	public void durableStateChange(String durableName, String durableState, String lotQuantity, EventInfo eventInfo) throws CustomException
	{
		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(durableName);
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		// Variable DurableState ['ASSING','DEASSIN'] aren't states,
		// but just processFlag
		// When DurableState change to InUse (example :Assign)
		if (durableState.equals("ASSIGN"))
		{
			// Change DurableInfo
			if (durableData.getLotQuantity() == 0)
			{
				durableData.setDurableState(constantMap.Dur_InUse);
			}

			// MaskLots are assigned maximum cappcity of carrier
			if (durableData.getLotQuantity() + Long.parseLong(lotQuantity) > durableData.getCapacity())
			{
				throw new CustomException("CST-0013", durableName);
			}
			else
			{
				durableData.setLotQuantity(durableData.getLotQuantity() + Long.parseLong(lotQuantity));
			}
		}

		// When DurableState change to InUse (example :DeAssign), if durable
		// QTY is 0 , durableState change to Available
		if (durableState.equals("DEASSIGN"))
		{
			if (durableData.getLotQuantity() - Long.parseLong(lotQuantity) == 0)
			{
				durableData.setLotQuantity(0);
				durableData.setDurableState(constantMap.Dur_Available);
			}
			else if (durableData.getLotQuantity() - Long.parseLong(lotQuantity) < 0)
			{
				throw new CustomException("CST-0014", durableName);
			}
			else
			{
				durableData.setLotQuantity(durableData.getLotQuantity() - Long.parseLong(lotQuantity));
			}
		}

		DurableServiceProxy.getDurableService().update(durableData);
		SetEventInfo setEventInfo = new SetEventInfo();

		DurableServiceProxy.getDurableService().setEvent(durableKey, eventInfo, setEventInfo);

		log.info("Event time = " + eventInfo.getEventTime() + " , EventTimeKey = " + eventInfo.getEventTimeKey());
	}

	public void mountPhotoMask(Durable durableData, String durableState, Map<String, String> udfs, EventInfo eventInfo)
	{
		durableData.setDurableState(durableState);
		durableData.setUdfs(udfs);

		DurableServiceProxy.getDurableService().update(durableData);

		SetEventInfo setEventInfo = new SetEventInfo();
		DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey = " + eventInfo.getEventTimeKey());
	}

	public void KitMask(Durable durableData, String durableState, String transportState, String position, Map<String, String> udfs, EventInfo eventInfo)
	{
		durableData.setDurableState(durableState);
		DurableServiceProxy.getDurableService().update(durableData);

		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.setUdfs(udfs);
		setEventInfo.getUdfs().put("TRANSPORTSTATE", transportState);
		setEventInfo.getUdfs().put("RETICLESLOT", position);
		
		DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);
		log.info("Event Name = " + eventInfo.getEventName() + " , EventTimeKey = " + eventInfo.getEventTimeKey());
	}

	public void incrementDurableUseCount(Durable durableData, int timeUseCount, EventInfo eventInfo)
	{
		eventInfo.setEventName("MonitorCSTUsedCount");

		IncrementTimeUsedInfo incrementTimeUsedInfo = new IncrementTimeUsedInfo();
		incrementTimeUsedInfo.setTimeUsed(timeUseCount);
		MESDurableServiceProxy.getDurableServiceImpl().incrementTimeUsed(durableData, incrementTimeUsedInfo, eventInfo);
	}
}