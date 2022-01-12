package kr.co.aim.messolution.durable.service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.data.DurableSpecKey;
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
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DurableInfoUtil implements ApplicationContextAware {
	private ApplicationContext applicationContext;

	private static Log log = LogFactory.getLog(DurableInfoUtil.class);

	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		applicationContext = arg0;
	}

	public AssignTransportGroupInfo assignTransportGroupInfo(Durable durableData, String transportGroupName)
	{

		AssignTransportGroupInfo assignTransportGroupInfo = new AssignTransportGroupInfo();

		assignTransportGroupInfo.setTransportGroupName(transportGroupName);

		return assignTransportGroupInfo;
	}

	public ChangeSpecInfo changeSpecInfo(Durable durableData, String areaName, long capacity, String durableSpecName, String durableSpecVersion, double durationUsedLimit, String factoryName,
			double timeUsedLimit)
	{

		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();

		changeSpecInfo.setAreaName(areaName);
		changeSpecInfo.setCapacity(capacity);
		changeSpecInfo.setDurableSpecName(durableSpecName);
		changeSpecInfo.setDurableSpecVersion(durableSpecVersion);
		changeSpecInfo.setDurationUsedLimit(durationUsedLimit);
		changeSpecInfo.setFactoryName(factoryName);
		changeSpecInfo.setTimeUsedLimit(timeUsedLimit);
		
		return changeSpecInfo;
	}

	public CleanInfo cleanInfo(Durable durableData, String machineName)
	{

		CleanInfo cleanInfo = new CleanInfo();

		Map<String, String> durableUdfs = new HashMap<String, String>();
		if (!StringUtils.isEmpty(machineName))
		{
			durableUdfs.put("MACHINENAME", machineName);
		}

		cleanInfo.setUdfs(durableUdfs);

		return cleanInfo;

	}

	public CreateInfo createInfo(Durable durableData, String durableName, String durableType, String durableSpecName, String durableSpecVersion, double timeUsedLimit, double durationUsedLimit,
			long capacity, String factoryName, String areaName)
	{

		CreateInfo createInfo = new CreateInfo();

		createInfo.setAreaName(areaName);
		createInfo.setCapacity(capacity);
		createInfo.setDurableName(durableName);
		createInfo.setDurableSpecName(durableSpecName);
		createInfo.setDurableSpecVersion(durableSpecVersion);
		createInfo.setDurableType(durableType);
		createInfo.setDurationUsedLimit(durationUsedLimit);
		createInfo.setFactoryName(factoryName);
		createInfo.setTimeUsedLimit(timeUsedLimit);

		return createInfo;
	}

	public DeassignTransportGroupInfo deassignTransportGroupInfo(Durable durableData, String transportGroupName)
	{

		DeassignTransportGroupInfo deassignTransportGroupInfo = new DeassignTransportGroupInfo();

		return deassignTransportGroupInfo;
	}

	public DecrementDurationUsedInfo decrementDurationUsedInfo(Durable durableData, double durationUsed)
	{

		DecrementDurationUsedInfo decrementDurationUsedInfo = new DecrementDurationUsedInfo();

		decrementDurationUsedInfo.setDurationUsed(durationUsed);
		
		return decrementDurationUsedInfo;
	}

	public DecrementTimeUsedInfo decrementTimeUsedInfo(Durable durableData, double timeUsed)
	{

		DecrementTimeUsedInfo decrementTimeUsedInfo = new DecrementTimeUsedInfo();

		decrementTimeUsedInfo.setTimeUsed(timeUsed);

		return decrementTimeUsedInfo;
	}

	public DirtyInfo dirtyInfo(Durable durableData, String machineName)
	{

		DirtyInfo dirtyInfo = new DirtyInfo();

		Map<String, String> durableUdfs = new HashMap<String, String>();
		if (!StringUtils.isEmpty(machineName))
		{
			durableUdfs.put("MACHINENAME", machineName);
		}
		dirtyInfo.setUdfs(durableUdfs);

		return dirtyInfo;
	}

	public IncrementDurationUsedInfo incrementDurationUsedInfo(Durable durableData, double durationUsed)
	{

		IncrementDurationUsedInfo incrementDurationUsedInfo = new IncrementDurationUsedInfo();

		incrementDurationUsedInfo.setDurationUsed(durationUsed);

		return incrementDurationUsedInfo;
	}

	public IncrementTimeUsedInfo incrementTimeUsedInfo(Durable durableData, int timeUsed, String consumerLotName, String consumerProductName, String consumerTimeKey, String consumerPOName,
			String consumerPOVersion)
	{
		IncrementTimeUsedInfo incrementTimeUsedInfo = new IncrementTimeUsedInfo();

		incrementTimeUsedInfo.setTimeUsed(timeUsed);
		incrementTimeUsedInfo.setConsumerLotName(consumerLotName);
		incrementTimeUsedInfo.setConsumerProductName(consumerProductName);
		incrementTimeUsedInfo.setConsumerTimeKey(consumerTimeKey);
		incrementTimeUsedInfo.setConsumerPOName(consumerPOName);
		incrementTimeUsedInfo.setConsumerPOVersion(consumerPOVersion);

		return incrementTimeUsedInfo;
	}

	public MakeAvailableInfo makeAvailableInfo(Durable durableData)
	{
		MakeAvailableInfo makeAvailableInfo = new MakeAvailableInfo();

		return makeAvailableInfo;
	}

	public MakeInUseInfo makeInUseInfo(Durable durableData, String consumerLotName, String consumerTimeKey, String consumerPOName, String consumerPOVersion)
	{
		MakeInUseInfo makeInUseInfo = new MakeInUseInfo();

		makeInUseInfo.setConsumerLotName(consumerLotName);
		makeInUseInfo.setConsumerPOName(consumerPOName);
		makeInUseInfo.setConsumerPOVersion(consumerPOVersion);
		makeInUseInfo.setConsumerTimeKey(consumerTimeKey);

		return makeInUseInfo;
	}

	public MakeNotAvailableInfo makeNotAvailableInfo(Durable durableData)
	{
		MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();

		return makeNotAvailableInfo;
	}

	public MakeNotInUseInfo makeNotInUseInfo(Durable durableData, String consumerLotName, String consumerTimeKey, String consumerPOName, String consumerPOVersion)
	{
		MakeNotInUseInfo makeNotInUseInfo = new MakeNotInUseInfo();

		makeNotInUseInfo.setConsumerLotName(consumerLotName);
		makeNotInUseInfo.setConsumerPOName(consumerPOName);
		makeNotInUseInfo.setConsumerPOVersion(consumerPOVersion);
		makeNotInUseInfo.setConsumerTimeKey(consumerTimeKey);

		return makeNotInUseInfo;
	}

	public MakeScrappedInfo makeScrappedInfo(Durable durableData)
	{
		MakeScrappedInfo makeScrappedInfo = new MakeScrappedInfo();

		return makeScrappedInfo;
	}

	public RepairInfo repairInfo(Durable durableData, String machineName)
	{
		RepairInfo repairInfo = new RepairInfo();

		Map<String, String> durableUdfs = new HashMap<String, String>();
		if (!StringUtils.isEmpty(machineName))
		{
			durableUdfs.put("MACHINENAME", machineName);
		}
		repairInfo.setUdfs(durableUdfs);

		return repairInfo;
	}

	public SetAreaInfo setAreaInfo(Durable durableData, String areaName)
	{
		SetAreaInfo setAreaInfo = new SetAreaInfo();

		setAreaInfo.setAreaName(areaName);

		return setAreaInfo;
	}

	public SetAreaInfo AreaInfo(String areaName, Map<String, String> udfs)
	{
		SetAreaInfo areaInfo = new SetAreaInfo();

		areaInfo.setAreaName(areaName);
		areaInfo.setUdfs(udfs);

		return areaInfo;
	}

	public SetEventInfo setEventInfo(Durable durableData, Map<String, String> udfs)
	{
		SetEventInfo setEventInfo = new SetEventInfo();

		setEventInfo.setUdfs(udfs);

		return setEventInfo;
	}

	public SetEventInfo setEventInfo(Map<String, String> udfs)
	{
		SetEventInfo setEventInfo = new SetEventInfo();

		Map<String, String> durableUdfs = udfs;
		setEventInfo.setUdfs(durableUdfs);

		return setEventInfo;
	}

	public SetMaterialLocationInfo setMaterialLocationInfo(Durable durableData, String materialLocationName)
	{
		SetMaterialLocationInfo setMaterialLocationInfo = new SetMaterialLocationInfo();

		setMaterialLocationInfo.setMaterialLocationName(materialLocationName);

		return setMaterialLocationInfo;
	}

	public UndoInfo undoInfo(Durable durableData, String eventName, Timestamp eventTime, String eventTimeKey, String eventUser, String lastEventTimeKey)
	{
		UndoInfo undoInfo = new UndoInfo();
		undoInfo.setEventName(eventName);
		undoInfo.setEventTime(eventTime);
		undoInfo.setEventTimeKey(eventTimeKey);
		undoInfo.setEventUser(eventUser);
		undoInfo.setLastEventTimeKey(lastEventTimeKey);

		return undoInfo;
	}

	public CreateInfo createInfo(String durableName, String durableSpecName, String durableSpecVersion, String capacity, String factoryName) throws CustomException
	{

		if (StringUtil.isEmpty(durableSpecVersion))
			durableSpecVersion = GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION;

		DurableSpecKey durableSpecKey = new DurableSpecKey();

		durableSpecKey.setDurableSpecName(durableSpecName);
		durableSpecKey.setDurableSpecVersion(durableSpecVersion);
		durableSpecKey.setFactoryName(factoryName);

		DurableSpec durableSpecData = DurableServiceProxy.getDurableSpecService().selectByKey(durableSpecKey);

		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(durableName);

		CreateInfo createInfo = new CreateInfo();
		createInfo.setAreaName("");
		createInfo.setCapacity(Long.valueOf(capacity).longValue());
		createInfo.setDurableName(durableName);
		createInfo.setDurableSpecName(durableSpecName);
		createInfo.setDurableSpecVersion(durableSpecVersion);
		createInfo.setDurableType(durableSpecData.getDurableType());
		createInfo.setDurationUsedLimit(durableSpecData.getDurationUsedLimit());
		createInfo.setTimeUsedLimit(durableSpecData.getTimeUsedLimit());
		createInfo.setFactoryName(factoryName);

		Map<String, String> durableUdfs = new HashMap<String, String>();
		durableUdfs.put("TRANSPORTSTATE", "");
		durableUdfs.put("DURABLEHOLDSTATE", GenericServiceProxy.getConstantMap().Mac_NotOnHold);

		createInfo.setUdfs(durableUdfs);

		return createInfo;
	}

	public CreateInfo CreateMaskInfo(String durableName, String factoryName, String durableType, String durableSpecName, String timeUseLimit, String cleanUsedLimit, String durationUsedLimit,
			String MaskProcessRecipe, String MaskReworkRecipe, String Offset_X, String Offset_Y, String Offset_T, String MaskThickness) throws CustomException
	{
		DurableSpecKey durableSpecKey = new DurableSpecKey();
		durableSpecKey.setDurableSpecName(durableSpecName);
		durableSpecKey.setDurableSpecVersion("00001");
		durableSpecKey.setFactoryName(factoryName);

		DurableSpec durableSpecData = DurableServiceProxy.getDurableSpecService().selectByKey(durableSpecKey);

		CreateInfo createInfo = new CreateInfo();
		createInfo.setDurableName(durableName);
		createInfo.setDurableSpecName(durableSpecName);
		createInfo.setDurableSpecVersion("00001");
		createInfo.setDurableType(durableType);
		createInfo.setFactoryName(factoryName);

		Map<String, String> durableUdfs = new HashMap<String, String>();

		createInfo.setTimeUsedLimit(durableSpecData.getTimeUsedLimit());
		durableUdfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_INSTK);

		createInfo.setUdfs(durableUdfs);

		return createInfo;
	}

	public SetEventInfo setAssignMaskInfo(String durableType, String machineName, String unitName, String sSubUnitName, String maskPosition) throws CustomException
	{
		SetEventInfo setEventInfo = new SetEventInfo();

		setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_OUTSTK);
		setEventInfo.getUdfs().put("MACHINENAME", machineName);
		setEventInfo.getUdfs().put("UNITNAME", unitName);

		return setEventInfo;
	}

	public SetEventInfo setDeassignMaskInfo(String durableType, String machineName, String unitName, String subUnitName)
	{
		SetEventInfo setEventInfo = new SetEventInfo();

		setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_INSTK);
		setEventInfo.getUdfs().put("MACHINENAME", machineName);
		setEventInfo.getUdfs().put("UNITNAME", unitName);
		setEventInfo.getUdfs().put("POSITIONNAME", "");

		return setEventInfo;
	}

	public CreateInfo createMaskCSTInfo(String durableName, String durableSpecName, String capacity, String factoryName)
	{

		DurableSpecKey durableSpecKey = new DurableSpecKey();

		durableSpecKey.setDurableSpecName(durableSpecName);
		durableSpecKey.setDurableSpecVersion("00001");
		durableSpecKey.setFactoryName(factoryName);

		DurableSpec durableSpecData = DurableServiceProxy.getDurableSpecService().selectByKey(durableSpecKey);

		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(durableName);

		CreateInfo createInfo = new CreateInfo();
		createInfo.setAreaName("");
		createInfo.setCapacity(Long.valueOf(capacity).longValue());
		createInfo.setDurableName(durableName);
		createInfo.setDurableSpecName(durableSpecName);
		createInfo.setDurableSpecVersion("00001");
		createInfo.setDurableType(durableSpecData.getDurableType());
		createInfo.setDurationUsedLimit(durableSpecData.getDurationUsedLimit());
		createInfo.setTimeUsedLimit(durableSpecData.getTimeUsedLimit());
		createInfo.setFactoryName(factoryName);

		Map<String, String> durableUdfs = new HashMap<String, String>();
		durableUdfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_OUTSTK);
		durableUdfs.put("DURABLEHOLDSTATE", GenericServiceProxy.getConstantMap().Mac_NotOnHold);

		createInfo.setUdfs(durableUdfs);

		return createInfo;
	}

	public CreateInfo createPPBoxInfo(String durableName, String durableSpecName, String capacity, String factoryName)
	{

		DurableSpecKey durableSpecKey = new DurableSpecKey();

		durableSpecKey.setDurableSpecName(durableSpecName);
		durableSpecKey.setDurableSpecVersion("00001");
		durableSpecKey.setFactoryName(factoryName);

		DurableSpec durableSpecData = DurableServiceProxy.getDurableSpecService().selectByKey(durableSpecKey);

		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(durableName);

		CreateInfo createInfo = new CreateInfo();
		createInfo.setCapacity(Long.valueOf(capacity).longValue());
		createInfo.setDurableName(durableName);
		createInfo.setDurableSpecName(durableSpecName);
		createInfo.setDurableSpecVersion("00001");
		createInfo.setDurableType(durableSpecData.getDurableType());
		createInfo.setDurationUsedLimit(durableSpecData.getDurationUsedLimit());
		createInfo.setTimeUsedLimit(durableSpecData.getTimeUsedLimit());
		createInfo.setFactoryName(factoryName);

		return createInfo;
	}

	public SetEventInfo setAssignEVAMaskPositionInfo(Durable maskData, String maskCarrierName, String maskPosition) throws CustomException
	{
		// Put data into UDF
		SetEventInfo setEventInfo = new SetEventInfo();
		setEventInfo.getUdfs().put("MASKCARRIERNAME", maskCarrierName);
		setEventInfo.getUdfs().put("MASKPOSITION", maskPosition);

		return setEventInfo;
	}

	public SetEventInfo setDeassignEVAMaskCSTInfo(String maskCarrierName, String maskPosition)
	{
		// Put data into UDF
		SetEventInfo setEventInfo = new SetEventInfo();

		setEventInfo.getUdfs().put("MASKCARRIERNAME", maskCarrierName);
		setEventInfo.getUdfs().put("MASKPOSITION", maskPosition);
		
		return setEventInfo;

	}

	public SetEventInfo setReserveMaskInfo(String durableType, String transportState, String machineName, String unitName, String sSubUnitName, String position,
			String maskPosition)
	{

		SetEventInfo setEventInfo = new SetEventInfo();
		
		setEventInfo.getUdfs().put("TRANSPORTSTATE", transportState);
		setEventInfo.getUdfs().put("MACHINENAME", machineName);
		setEventInfo.getUdfs().put("UNITNAME", unitName);
		setEventInfo.getUdfs().put("MASKPOSITION", maskPosition);
		setEventInfo.getUdfs().put("POSITIONNAME", position + "+" + sSubUnitName);

		return setEventInfo;
	}

	public SetEventInfo setMaskProcessEndInfo(String machinename, String unitname, String maskCarrierName, String offset_X, String offset_Y, String offset_T)
	{
		SetEventInfo setEventInfo = new SetEventInfo();
		
		setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_OUTSTK);
		setEventInfo.getUdfs().put("MACHINENAME", "");
		setEventInfo.getUdfs().put("UNITNAME", "");
		setEventInfo.getUdfs().put("POSITIONNAME", "");

		return setEventInfo;
	}

	public CreateInfo CreatePPBoxIDInfo(String ppboxID, String capacity, String factoryName, String durableSpecName) throws CustomException
	{
		DurableSpecKey durableSpecKey = new DurableSpecKey();
		durableSpecKey.setDurableSpecName(durableSpecName);
		durableSpecKey.setDurableSpecVersion("00001");
		durableSpecKey.setFactoryName(factoryName);

		DurableSpec durableSpecData = DurableServiceProxy.getDurableSpecService().selectByKey(durableSpecKey);

		CreateInfo createInfo = new CreateInfo();
		createInfo.setDurableName(ppboxID);
		createInfo.setDurableSpecName(durableSpecName);
		createInfo.setDurableSpecVersion("00001");
		createInfo.setCapacity(Long.valueOf(capacity).longValue());
		createInfo.setFactoryName(factoryName);
		createInfo.setDurableType("PPBox");
		createInfo.setTimeUsedLimit(durableSpecData.getTimeUsedLimit());

		Map<String, String> durableUdfs = new HashMap<String, String>();

		createInfo.setUdfs(durableUdfs);

		return createInfo;
	}

	public CreateInfo createProbeInfo(String durableName, String durableSpecName, String capacity, String factoryName)
	{

		DurableSpecKey durableSpecKey = new DurableSpecKey();

		durableSpecKey.setDurableSpecName(durableSpecName);
		durableSpecKey.setDurableSpecVersion("00001");
		durableSpecKey.setFactoryName(factoryName);

		DurableSpec durableSpecData = DurableServiceProxy.getDurableSpecService().selectByKey(durableSpecKey);

		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(durableName);

		CreateInfo createInfo = new CreateInfo();
		createInfo.setAreaName("");
		createInfo.setCapacity(Long.valueOf(capacity).longValue());
		createInfo.setDurableName(durableName);
		createInfo.setDurableSpecName(durableSpecName);
		createInfo.setDurableSpecVersion("00001");
		createInfo.setDurableType(durableSpecData.getDurableType());
		createInfo.setDurationUsedLimit(durableSpecData.getDurationUsedLimit());
		createInfo.setTimeUsedLimit(durableSpecData.getTimeUsedLimit());
		createInfo.setFactoryName(factoryName);
		Map<String, String> durableUdfs = new HashMap<String, String>();
		durableUdfs.put("DURABLEHOLDSTATE", GenericServiceProxy.getConstantMap().Mac_NotOnHold);

		createInfo.setUdfs(durableUdfs);

		return createInfo;
	}

}
