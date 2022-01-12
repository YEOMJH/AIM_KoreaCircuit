package kr.co.aim.messolution.lot.event;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.port.management.data.PortSpec;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;

public class ValidateSorterJobRequest extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		// MACHINENAME
		// OPERATIONMODENAME
		// PORTLIST
		// PORT
		// PORTNAME
		// CARRIERNAME
		// PORTTYPE

		// This message is only use at TP Sorter machine currently
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String operationMode = SMessageUtil.getBodyItemValue(doc, "OPERATIONMODENAME", true);

		Element bodyElement = SMessageUtil.getBodyElement(doc);
		Element portListElement = bodyElement.getChild("PORTLIST");
		SMessageUtil.setItemValue(doc, "Body", "RESULT", "NG");
		SMessageUtil.setItemValue(doc, "Body", "RESULTDESCRIPTION", "");

		// Set MessageName
		SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "ValidateSorterJobReply");

		Lot compareLotData = null;
		int plCount = 0;
		int plProductCount = 0;
		int puCount = 0;

		if (portListElement != null)
		{
			for (Iterator<?> iterator = portListElement.getChildren().iterator(); iterator.hasNext();)
			{
				Element port = (Element) iterator.next();

				String portName = port.getChildText("PORTNAME");
				String carrierName = port.getChildText("CARRIERNAME");
				String portType = port.getChildText("PORTTYPE");

				// Get Data
				Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
				PortSpec portSpec = MESPortServiceProxy.getPortServiceUtil().getPortSpecInfo(machineName, portName);

				// OLED CST -> TP CST
				if (operationMode.equals(GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE_MERGE))
				{
					if (portSpec.getUdfs().get("UseDurableType").equals(GenericServiceProxy.getConstantMap().CST_TYPE_OLED))
					{
						plCount++;

						Lot lotData = CommonUtil.getLotInfoBydurableName(carrierName);

						// Check Durable Info
						if (!StringUtil.equals(durableData.getDurableType(), GenericServiceProxy.getConstantMap().CST_TYPE_OLED))
						{
							throw new CustomException("DURABLE-1002", carrierName);
						}

						// Check SlotMap
						if (compareLotData == null)
						{
							compareLotData = lotData;
						}
						else
						{
							// Check WorkOrder
							if (!compareLotData.getProductRequestName().equals(lotData.getProductRequestName()))
								throw new CustomException("LOT-1002", compareLotData.getKey().getLotName(), lotData.getKey().getLotName());

							// Check ProductSpec
							if (!compareLotData.getProductSpecName().equals(lotData.getProductSpecName()))
								throw new CustomException("LOT-1003", compareLotData.getKey().getLotName(), lotData.getKey().getLotName());

							if (!compareLotData.getProductSpecVersion().equals(lotData.getProductSpecVersion()))
								throw new CustomException("LOT-1004", compareLotData.getKey().getLotName(), lotData.getKey().getLotName());

							// Check ProcessFlow
							if (!compareLotData.getProcessFlowName().equals(lotData.getProcessFlowName()))
								throw new CustomException("LOT-1005", compareLotData.getKey().getLotName(), lotData.getKey().getLotName());

							if (!compareLotData.getProcessFlowVersion().equals(lotData.getProcessFlowVersion()))
								throw new CustomException("LOT-1006", compareLotData.getKey().getLotName(), lotData.getKey().getLotName());

							// Check ProcessOperation
							if (!compareLotData.getProcessOperationName().equals(lotData.getProcessOperationName()))
								throw new CustomException("LOT-1007", compareLotData.getKey().getLotName(), lotData.getKey().getLotName());

							if (!compareLotData.getProcessOperationVersion().equals(lotData.getProcessOperationVersion()))
								throw new CustomException("LOT-1008", compareLotData.getKey().getLotName(), lotData.getKey().getLotName());

							List<Product> compareAllUnScrappedProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(compareLotData.getKey().getLotName());
							List<Product> allUnScrappedProductList = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(lotData.getKey().getLotName());

							// Check PL Port ProductQty (OLED CST Port)
							plProductCount += compareAllUnScrappedProductList.size();
							plProductCount += allUnScrappedProductList.size();

						}
					}
					else if (portSpec.getUdfs().get("UseDurableType").equals(GenericServiceProxy.getConstantMap().CST_TYPE_TPNORMAL))
					{
						puCount++;

						if (!StringUtil.equals(durableData.getDurableType(), GenericServiceProxy.getConstantMap().CST_TYPE_TPNORMAL))
						{
							throw new CustomException("DURABLE-1003", carrierName);
						}

						try
						{
							Lot lotData = CommonUtil.getLotInfoBydurableName(carrierName);
							if (lotData != null)
								throw new CustomException("DURABLE-0004", carrierName);
						}
						catch (Exception ex)
						{
							// lotData null is OK
						}
					}
				}
				// TP CST -> OLED CST
				if (operationMode.equals(GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE_SPLIT))
				{
					if (portSpec.getUdfs().get("UseDurableType").equals(GenericServiceProxy.getConstantMap().CST_TYPE_TPNORMAL))
					{
						plCount++;

						Lot lotData = CommonUtil.getLotInfoBydurableName(carrierName);

						// Check Durable Info
						if (!StringUtil.equals(durableData.getDurableType(), GenericServiceProxy.getConstantMap().CST_TYPE_TPNORMAL))
						{
							throw new CustomException("DURABLE-0002", carrierName);
						}

						// Check SlotMap
						if (compareLotData == null)
						{
							compareLotData = lotData;
						}
						else
						{
							throw new CustomException("MACHINE-0020", machineName, operationMode);
						}

					}
					else if (portSpec.getUdfs().get("UseDurableType").equals(GenericServiceProxy.getConstantMap().CST_TYPE_OLED))
					{
						puCount++;

						// 2. Check Durable Info
						if (!StringUtil.equals(durableData.getDurableType(), GenericServiceProxy.getConstantMap().CST_TYPE_OLED))
						{
							throw new CustomException("DURABLE-0003", carrierName);
						}

						try
						{
							Lot lotData = CommonUtil.getLotInfoBydurableName(carrierName);
							if (lotData != null)
								throw new CustomException("DURABLE-0004", carrierName);
						}
						catch (Exception ex)
						{
							// lotData null is OK
						}

					}
				}
			}
		}
		else
		{
			throw new CustomException("MACHINE-0018", machineName);
		}

		if (operationMode.equals(GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE_MERGE))
		{
			if (plCount != 2 || puCount != 1)
				throw new CustomException("MACHINE-0019", machineName, operationMode);
			if ((plProductCount % 2) != 0)
				throw new CustomException("MACHINE-0025", machineName, operationMode);
		}
		else if (operationMode.equals(GenericServiceProxy.getConstantMap().SORT_OPERATIONMODE_SPLIT))
		{
			if (plCount != 1 || puCount != 2)
				throw new CustomException("MACHINE-0020", machineName, operationMode);
		}
		else
			throw new CustomException("MACHINE-0021", machineName, operationMode);

		SMessageUtil.setItemValue(doc, "Body", "RESULT", "OK");
		SMessageUtil.setItemValue(doc, "Body", "RESULTDESCRIPTION", "");

		return doc;
	}

}
