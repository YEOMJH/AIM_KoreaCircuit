package kr.co.aim.messolution.durable.event;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CrucibleLot;
import kr.co.aim.messolution.extended.object.management.data.MaterialProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;

public class ExposureFeedBackReport extends AsyncHandler {

	private static Log log = LogFactory.getLog(ExposureFeedBackReport.class);

	public void doWorks(Document doc) throws CustomException
	{
		// MACHINENAME
		// UNITNAME
		// LOTNAME
		// PRODUCTNAME
		// MASKNAME
		// EXPOSURERECIPENAME

		String messageName = SMessageUtil.getMessageName(doc);

		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", false);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", false);
		String durableName = SMessageUtil.getBodyItemValue(doc, "MASKNAME ", false);
		String exposureRecipeName = SMessageUtil.getBodyItemValue(doc, "EXPOSURERECIPENAME ", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, this.getEventUser(), this.getEventComment(), null, null);
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		
		SetEventInfo setEventInfo = new SetEventInfo();

		setEventInfo.getUdfs().put("MACHINENAME", machineName);
		setEventInfo.getUdfs().put("UNITNAME", unitName);
		setEventInfo.getUdfs().put("MACHINERECIPENAME", exposureRecipeName);

		DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfo);

		if (StringUtils.isNotEmpty(productName) && StringUtils.isNotEmpty(exposureRecipeName))
		{
			// Set exposureRecipeName to Product
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

			Map<String, String> productUdfs = new HashMap<String, String>();
			productUdfs.put("EXPOSURERECIPENAME", exposureRecipeName);

			kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfoProduct = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
			setEventInfoProduct.setUdfs(productUdfs);

			ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setEventInfoProduct);

			// Create MaterialProduct
			MaterialProduct dataInfo = new MaterialProduct();
			dataInfo.setTimeKey(eventInfo.getEventTimeKey());
			dataInfo.setProductName(productName);
			dataInfo.setLotName(productData.getLotName());
			dataInfo.setMaterialKind(GenericServiceProxy.getConstantMap().MaterialKind_Durable);
			dataInfo.setMaterialType(durableData.getDurableType());
			dataInfo.setMaterialName(durableName);
			dataInfo.setQuantity(1);
			dataInfo.setEventName(eventInfo.getEventName());
			dataInfo.setEventTime(eventInfo.getEventTime());
			dataInfo.setFactoryName(productData.getFactoryName());
			dataInfo.setProductSpecName(productData.getProductSpecName());
			dataInfo.setProductSpecVersion(productData.getProductSpecVersion());
			dataInfo.setProcessFlowName(productData.getProcessFlowName());
			dataInfo.setProcessFlowVersion(productData.getProcessFlowVersion());
			dataInfo.setProcessOperationName(productData.getProcessOperationName());
			dataInfo.setProcessOperationVersion(productData.getProcessOperationVersion());
			dataInfo.setMachineName(machineName);
			dataInfo.setMaterialLocationName(unitName);

			ExtendedObjectProxy.getMaterialProductService().create(eventInfo, dataInfo);

		}
		else
			log.info("ProductName [" + productName + "], MachineName [" + machineName + "], UnitName [" + unitName + "] ExposureRecipeName is empty!!");
	}

}
