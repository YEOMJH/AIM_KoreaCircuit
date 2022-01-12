package kr.co.aim.messolution.durable.event.OledMask;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaterialProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.product.management.data.Product;

public class OLEDMaskUseReport extends AsyncHandler {

	private static Log log = LogFactory.getLog(OLEDMaskUseReport.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		/* MACHINENAME 
		 * UNITNAME
		 * SUBUNITNAME
		 * PRODUCTNAME
		 * MASKNAME
		 * MASKPOSITION
		 * MASKUSEDCOUNT
		 * MASKCYCLECOUNT
		 * OFFSET_X
		 * OFFSET_Y
		 * OFFSET_THETA
		 */
		
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String maskName = SMessageUtil.getBodyItemValue(doc, "MASKNAME", true);
		String maskPosition = SMessageUtil.getBodyItemValue(doc, "MASKPOSITION", false);
		String maskUsedCount = SMessageUtil.getBodyItemValue(doc, "MASKUSEDCOUNT", false);
		String maskCycleCount = SMessageUtil.getBodyItemValue(doc, "MASKCYCLECOUNT", false);
		String offSetX = SMessageUtil.getBodyItemValue(doc, "OFFSET_X", false);
		String offSetY = SMessageUtil.getBodyItemValue(doc, "OFFSET_Y", false);
		String offsetTheta = SMessageUtil.getBodyItemValue(doc, "OFFSET_THETA", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("MaskStateChanged", this.getEventUser(), this.getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		// Get Mask Lot Data
		MaskLot maskData = ExtendedObjectProxy.getMaskLotService().selectByKey(true, new Object[] { maskName });

		// Get Product Data
		Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

		String materialLocationName = "";
		if (StringUtils.isNotEmpty(subUnitName))
		{
			materialLocationName = subUnitName;
		}
		else if (StringUtils.isNotEmpty(unitName))
		{
			materialLocationName = unitName;
		}
		else
		{
			materialLocationName = machineName;
		}

		// Set data
		maskData.setMachineName(machineName);
		maskData.setMaterialLocationName(materialLocationName);
		maskData.setChamberName(subUnitName);
		//maskData.setPosition(maskPosition);
		maskData.setTimeUsed(Float.valueOf(maskUsedCount));
		maskData.setLastEventUser(eventInfo.getEventUser());
		maskData.setLastEventTimeKey(eventInfo.getEventTimeKey());
		maskData.setLastEventName(eventInfo.getEventName());
		maskData.setLastEventComment(eventInfo.getEventComment());
		maskData.setLastEventTime(eventInfo.getEventTime());

		ExtendedObjectProxy.getMaskLotService().modify(eventInfo, maskData);

		// Create MaterialProduct
		// MaterialKind - Mask
		// MaterialType - MaskType (CMM, FMM)
		// other information from Product Data
		MaterialProduct dataInfo = new MaterialProduct();
		dataInfo.setTimeKey(eventInfo.getEventTimeKey());
		dataInfo.setProductName(productName);
		dataInfo.setLotName(productData.getLotName());
		dataInfo.setMaterialKind(GenericServiceProxy.getConstantMap().MaterialType_Mask);
		dataInfo.setMaterialType(maskData.getMaskType());
		dataInfo.setMaterialName(maskName);
		dataInfo.setQuantity(1);
		dataInfo.setEventName(eventInfo.getEventName());
		dataInfo.setEventTime(eventInfo.getEventTime());
		dataInfo.setFactoryName(productData.getFactoryName());
		dataInfo.setProductSpecName(productData.getProductSpecName());
		dataInfo.setProductSpecVersion(productData.getProductSpecVersion());
		dataInfo.setProcessFlowName(productData.getProcessFlowName());
		dataInfo.setProcessFlowVersion(productData.getProcessFlowVersion());
		dataInfo.setProcessOperationName(productData.getProcessOperationName());
		dataInfo.setMaskCycleCount(maskCycleCount);
		dataInfo.setOffsetX(offSetX);
		dataInfo.setOffsetY(offSetY);
		dataInfo.setOffsetT(offsetTheta);
		
		dataInfo.setProcessOperationVersion(productData.getProcessOperationVersion());
		dataInfo.setMachineName(machineName);
		dataInfo.setMaterialLocationName(materialLocationName);

		ExtendedObjectProxy.getMaterialProductService().create(eventInfo, dataInfo);
	}
}
