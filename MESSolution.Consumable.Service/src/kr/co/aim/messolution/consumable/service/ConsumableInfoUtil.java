package kr.co.aim.messolution.consumable.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.info.AssignTransportGroupInfo;
import kr.co.aim.greentrack.consumable.management.info.ChangeSpecInfo;
import kr.co.aim.greentrack.consumable.management.info.CreateInfo;
import kr.co.aim.greentrack.consumable.management.info.DeassignTransportGroupInfo;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.IncrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeAvailableInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.consumable.management.info.MergeInfo;
import kr.co.aim.greentrack.consumable.management.info.SetAreaInfo;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.consumable.management.info.SplitInfo;
import kr.co.aim.greentrack.consumable.management.info.UndoInfo;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ConsumableInfoUtil implements ApplicationContextAware {
	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(ConsumableInfoUtil.class);

	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		applicationContext = arg0;
	}
	
	public List<Consumable> getFilmListByBoxDESC(String boxName) throws CustomException
	{
		if (boxName == null || boxName.isEmpty())
			log.info("The incoming argument value is Empty or Null!!.");

		List<Consumable> consumableDataList = null;

		try
		{
			consumableDataList = ConsumableServiceProxy.getConsumableService().select(" WHERE 1=1 AND CARRIERNAME = ? ORDER BY SEQ DESC ", new Object[] { boxName });
		}
		catch (NotFoundSignal notFoundEx)
		{
			throw new CustomException("COMM-1000", "Consumable", "CarrierName = " + boxName);
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		return consumableDataList;
	}

	public AssignTransportGroupInfo assignTransportGroupInfo(Consumable consumableData, String transportGroupName)
	{
		AssignTransportGroupInfo assignTransportGroupInfo = new AssignTransportGroupInfo();
		assignTransportGroupInfo.setTransportGroupName(transportGroupName);

		return assignTransportGroupInfo;
	}

	public ChangeSpecInfo changeSpecInfo(Consumable consumableData, String areaName, String consumableSpecName, String consumableSpecVersion, String factoryName)
	{
		ChangeSpecInfo changeSpecInfo = new ChangeSpecInfo();
		changeSpecInfo.setAreaName(areaName);
		changeSpecInfo.setConsumableSpecName(consumableSpecName);
		changeSpecInfo.setConsumableSpecVersion(consumableSpecVersion);
		changeSpecInfo.setFactoryName(factoryName);

		return changeSpecInfo;
	}

	public CreateInfo createInfo(String factoryName, String areaName, String consumableName, String consumableSpecName, String consumableSpecVersion, String consumableType, double quantity,
			Map<String, String> udfs)
	{
		CreateInfo createInfo = new CreateInfo();
		createInfo.setAreaName(areaName);
		createInfo.setConsumableName(consumableName);
		createInfo.setConsumableSpecName(consumableSpecName);
		createInfo.setConsumableSpecVersion(consumableSpecVersion);
		createInfo.setConsumableType(consumableType);
		createInfo.setFactoryName(factoryName);
		createInfo.setQuantity(quantity);

		createInfo.setUdfs(udfs);

		return createInfo;
	}

	public DeassignTransportGroupInfo deassignTransportGroupInfo(Consumable consumableData)
	{
		DeassignTransportGroupInfo deassignTransportGroupInfo = new DeassignTransportGroupInfo();
		
		return deassignTransportGroupInfo;
	}

	public DecrementQuantityInfo decrementQuantityInfo(String consumerLotName, String consumerPOName, String consumerPOVersion, String consumerProductName, String consumerTimeKey, double quantity,
			Map<String, String> udfs)
	{
		DecrementQuantityInfo decrementQuantityInfo = new DecrementQuantityInfo();
		decrementQuantityInfo.setConsumerLotName(consumerLotName);
		decrementQuantityInfo.setConsumerPOName(consumerPOName);
		decrementQuantityInfo.setConsumerPOVersion(consumerPOVersion);
		decrementQuantityInfo.setConsumerProductName(consumerProductName);
		decrementQuantityInfo.setConsumerTimeKey(consumerTimeKey);
		decrementQuantityInfo.setQuantity(quantity);

		Map<String, String> consumableUdfs = udfs;
		decrementQuantityInfo.setUdfs(consumableUdfs);

		return decrementQuantityInfo;
	}

	public IncrementQuantityInfo incrementQuantityInfo(double quantity, Map<String, String> udfs)
	{
		IncrementQuantityInfo incrementQuantityInfo = new IncrementQuantityInfo();
		incrementQuantityInfo.setQuantity(quantity);

		Map<String, String> consumableUdfs = udfs;
		incrementQuantityInfo.setUdfs(consumableUdfs);

		return incrementQuantityInfo;
	}

	public MakeAvailableInfo makeAvailableInfo(Consumable consumableData)
	{
		MakeAvailableInfo makeAvailableInfo = new MakeAvailableInfo();

		return makeAvailableInfo;
	}

	public MakeNotAvailableInfo makeNotAvailableInfo(Consumable consumableData)
	{
		MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();

		return makeNotAvailableInfo;
	}

	public MergeInfo mergeInfo(Consumable consumableData, String parentConsumableName, Map<String, String> parentConsumableUdfs)
	{
		MergeInfo mergeInfo = new MergeInfo();

		mergeInfo.setParentConsumableName(parentConsumableName);
		mergeInfo.setParentConsumableUdfs(parentConsumableUdfs);

		return mergeInfo;
	}

	public SetAreaInfo setAreaInfo(Consumable consumableData, String areaName)
	{
		SetAreaInfo setAreaInfo = new SetAreaInfo();
		setAreaInfo.setAreaName(areaName);

		return setAreaInfo;
	}

	public SetMaterialLocationInfo setMaterialLocationInfo(Consumable consumableData, String materialLocationName)
	{
		SetMaterialLocationInfo setMaterialLocationInfo = new SetMaterialLocationInfo();
		setMaterialLocationInfo.setMaterialLocationName(materialLocationName);

		return setMaterialLocationInfo;
	}

	public SetMaterialLocationInfo setMaterialLocationInfo(String materialLocationName, Map<String, String> udfs)
	{
		SetMaterialLocationInfo setMaterialLocationInfo = new SetMaterialLocationInfo();
		setMaterialLocationInfo.setMaterialLocationName(materialLocationName);

		setMaterialLocationInfo.setUdfs(udfs);

		return setMaterialLocationInfo;
	}

	public SplitInfo splitInfo(Consumable consumableData, String childConsumableName, Map<String, String> childConsumableUdfs, double quantity)
	{
		SplitInfo splitInfo = new SplitInfo();
		splitInfo.setChildConsumableName(childConsumableName);
		splitInfo.setChildConsumableUdfs(childConsumableUdfs);
		splitInfo.setQuantity(quantity);

		return splitInfo;
	}

	public UndoInfo undoInfo(Consumable consumableData, String eventName, Timestamp eventTime, String eventTimeKey, String eventUser, String lastEventTimeKey)
	{
		UndoInfo undoInfo = new UndoInfo();
		undoInfo.setEventName(eventName);
		undoInfo.setEventTime(eventTime);
		undoInfo.setEventTimeKey(eventTimeKey);
		undoInfo.setEventUser(eventUser);
		undoInfo.setLastEventTimeKey(lastEventTimeKey);

		return undoInfo;
	}

	public Consumable getConsumableData(String consumableName) throws CustomException
	{
		try
		{
			ConsumableKey consumableKey = new ConsumableKey();
			consumableKey.setConsumableName(consumableName);
			Consumable consumableData = ConsumableServiceProxy.getConsumableService().selectByKey(consumableKey);

			return consumableData;
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("CRATE-9001", consumableName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("CRATE-9999", fe.getMessage());
		}
	}

	public Consumable getMaterialData(String consumableName) throws CustomException
	{
		try
		{
			ConsumableKey consumableKey = new ConsumableKey();
			consumableKey.setConsumableName(consumableName);
			Consumable consumableData = ConsumableServiceProxy.getConsumableService().selectByKey(consumableKey);

			return consumableData;
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("MATERIAL-9001", consumableName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("CRATE-9999", fe.getMessage());
		}
	}

	public List<Consumable> getFilmListByMachine(String machineName) throws CustomException
	{
		List<Consumable> filmList = new ArrayList<Consumable>();

		String condition = " WHERE ConsumableType IN ('TFEFilm') AND machineName = ? ";
		Object[] bindSet = new Object[] { machineName };

		try
		{
			filmList = ConsumableServiceProxy.getConsumableService().select(condition, bindSet);
		}
		catch (Exception ex)
		{
			filmList = null;
		}

		log.info("FilmList.Size is  " + filmList.size());
		return filmList;
	}
	
	public List<Consumable> getFilmListByCarrierName(String carrierName) throws CustomException
	{
		List<Consumable> filmDataList = new ArrayList<Consumable>();

		String condition = " WHERE CARRIERNAME = ? ";
		Object[] bindSet = new Object[] { carrierName };

		try
		{
			filmDataList = ConsumableServiceProxy.getConsumableService().select(condition, bindSet);
			
			log.info("FilmList.Size is  " + filmDataList.size());
		}
		catch (Exception ex)
		{
			filmDataList = null;
		}

		return filmDataList;
	}
}
