package kr.co.aim.messolution.generic.util;

import java.util.List;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpecKey;
import kr.co.aim.greentrack.datacollection.DataCollectionServiceProxy;
import kr.co.aim.greentrack.datacollection.management.data.DCSpecItem;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.durable.management.data.DurableSpecKey;
import kr.co.aim.greentrack.factory.FactoryServiceProxy;
import kr.co.aim.greentrack.factory.management.data.MESFactory;
import kr.co.aim.greentrack.factory.management.data.MESFactoryKey;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.machine.MachineServiceProxy;
import kr.co.aim.greentrack.machine.management.data.MachineSpec;
import kr.co.aim.greentrack.machine.management.data.MachineSpecKey;
import kr.co.aim.greentrack.port.PortServiceProxy;
import kr.co.aim.greentrack.port.management.data.PortSpec;
import kr.co.aim.greentrack.port.management.data.PortSpecKey;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.product.management.data.ProductSpecKey;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpecUtil implements ApplicationContextAware {

	Log logger = LogFactory.getLog(SpecUtil.class);

	@Override
	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		if (logger.isInfoEnabled())
			logger.info("MES Specipication Utility is loaded");
	}

	public ProductSpec getProductSpec(String factoryName, String productSpecName, String productSpecVersion) throws CustomException
	{
		try
		{
			if (StringUtil.isEmpty(productSpecVersion))
				productSpecVersion = GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION;

			ProductSpecKey productSpecKey = new ProductSpecKey(factoryName, productSpecName, productSpecVersion);

			ProductSpec productSpec = ProductServiceProxy.getProductSpecService().selectByKey(productSpecKey);

			return productSpec;
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("PRODUCTSPEC-9001", productSpecName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("PRODUCT-9999", fe.getMessage());
		}
	}

	public ConsumableSpec getConsumableSpec(String factoryName, String consumableSpecName, String consumableSpecVersion) throws CustomException
	{
		try
		{
			if (StringUtil.isEmpty(consumableSpecVersion))
				consumableSpecVersion = GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION;

			ConsumableSpecKey consumableSpecKey = new ConsumableSpecKey();
			consumableSpecKey.setFactoryName(factoryName);
			consumableSpecKey.setConsumableSpecName(consumableSpecName);
			consumableSpecKey.setConsumableSpecVersion(consumableSpecVersion);

			ConsumableSpec consumableSpec = ConsumableServiceProxy.getConsumableSpecService().selectByKey(consumableSpecKey);

			return consumableSpec;
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("CRATE-9001", consumableSpecName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("CRATE-9999", fe.getMessage());
		}
	}

	public ConsumableSpec getMaterialSpec(String factoryName, String consumableSpecName, String consumableSpecVersion) throws CustomException
	{
		try
		{
			if (StringUtil.isEmpty(consumableSpecVersion))
				consumableSpecVersion = GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION;

			ConsumableSpecKey consumableSpecKey = new ConsumableSpecKey();
			consumableSpecKey.setFactoryName(factoryName);
			consumableSpecKey.setConsumableSpecName(consumableSpecName);
			consumableSpecKey.setConsumableSpecVersion(consumableSpecVersion);

			ConsumableSpec consumableSpec = ConsumableServiceProxy.getConsumableSpecService().selectByKey(consumableSpecKey);

			return consumableSpec;
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("MATERIAL-9001", consumableSpecName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MATERIAL-9999", fe.getMessage());
		}
	}

	public MachineSpec getMachineSpec(String machineName) throws CustomException
	{
		MachineSpecKey machineSpecKey = new MachineSpecKey();
		machineSpecKey.setMachineName(machineName);

		MachineSpec machineSpecData;
		try
		{
			machineSpecData = MachineServiceProxy.getMachineSpecService().selectByKey(machineSpecKey);
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("MACHINE-9001", machineName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("MACHINE-9999", fe.getMessage());
		}

		return machineSpecData;
	}

	public PortSpec getPortSpec(String machineName, String portName) throws CustomException
	{
		PortSpecKey portSpecKey = new PortSpecKey();
		portSpecKey.setMachineName(machineName);
		portSpecKey.setPortName(portName);

		PortSpec portSpecData = null;

		try
		{
			portSpecData = PortServiceProxy.getPortSpecService().selectByKey(portSpecKey);

		}
		catch (Exception e)
		{
			throw new CustomException("PORT-9000", machineName, portName);
		}

		return portSpecData;
	}

	public List<DCSpecItem> getDCSpecItem(String itemName) throws CustomException
	{
		String condition = "ITEMNAME = ? ORDER BY ITEMNAME";

		Object[] bindSet = new Object[] { itemName };

		List<DCSpecItem> sqlResult = DataCollectionServiceProxy.getDCSpecItemService().select(condition, bindSet);

		return sqlResult;
	}

	public MESFactory getFactory(String factoryName) throws CustomException
	{
		try
		{
			MESFactoryKey keyInfo = new MESFactoryKey();
			keyInfo.setFactoryName(factoryName);

			MESFactory factoryData = FactoryServiceProxy.getMESFactoryService().selectByKey(keyInfo);

			return factoryData;
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("SYS-9001", "MESFactory");
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", "MESFactory", fe.getMessage());
		}
	}

	public String getDefaultArea(String factoryName) throws CustomException
	{
		try
		{
			MESFactory factoryData = getFactory(factoryName);

			return CommonUtil.getValue(factoryData.getUdfs(), "DEFAULTAREANAME");
		}
		catch (CustomException ce)
		{
			return "";
		}
	}

	public DurableSpec getDurableSpec(String factoryName, String durableSpecName, String durableSpecVersion) throws CustomException
	{
		try
		{
			if (StringUtil.isEmpty(durableSpecVersion))
				durableSpecVersion = GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION;

			DurableSpecKey durableSpecKey = new DurableSpecKey();
			durableSpecKey.setFactoryName(factoryName);
			durableSpecKey.setDurableSpecName(durableSpecName);
			durableSpecKey.setDurableSpecVersion(durableSpecVersion);

			DurableSpec durableSpec = DurableServiceProxy.getDurableSpecService().selectByKey(durableSpecKey);

			return durableSpec;
		}
		catch (NotFoundSignal ne)
		{
			throw new CustomException("SPEC-0002", durableSpecName);
		}
		catch (FrameworkErrorSignal fe)
		{
			throw new CustomException("SYS-9999", fe.getMessage());
		}
	}

}
