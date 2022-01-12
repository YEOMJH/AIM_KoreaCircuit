package kr.co.aim.messolution.product.event;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskLot;
import kr.co.aim.messolution.extended.object.management.data.MaterialProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameErrorSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductKey;

public class ProductOLEDMaskInfoDownloadRequest extends SyncHandler {

	private static Log log = LogFactory.getLog(ProductOLEDMaskInfoDownloadRequest.class);

	@Override
	public Object doWorks(Document doc) throws CustomException 
	{

		try
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "ProductOLEDMaskInfoDownloadReply");

			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", true);
			String subUnitName = SMessageUtil.getBodyItemValue(doc, "SUBUNITNAME", false);
			String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);

			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(unitName);
			
			Element bodyElement = SMessageUtil.getBodyElement(doc);
			XmlUtil.addElement(bodyElement, "MASKLIST", "");

			if (StringUtils.equals(unitName, "3CEE01-PPA") || StringUtils.equals(unitName, "3CEE02-PPA"))
			{
				try
				{
					Product productData = ProductServiceProxy.getProductService().selectByKey(new ProductKey(productName));
				}
				catch (NotFoundSignal ne)
				{
					log.info("ProductName Not Exist ");
					throw new CustomException("PRODUCT-9001",productName);
					
				}
				
				List<MaterialProduct> maskList = new ArrayList<MaterialProduct>();					
				
				maskList = ExtendedObjectProxy.getMaterialProductService().select("WHERE productName = ?  AND MATERIALKIND= 'Mask' ORDER BY MATERIALLOCATIONNAME ",new Object[] { productName });
				
				if (maskList.size() >0)
				{
					for (MaterialProduct mask : maskList) 
					{
						MaskLot masklotInfo = ExtendedObjectProxy.getMaskLotService().selectByKey(true,new Object[] { mask.getMaterialName() });
						
						Element maskElement = null;
						maskElement = createMaskElement(doc, masklotInfo, mask);
	
						Element maskListElement = XmlUtil.getChild(SMessageUtil.getBodyElement(doc), "MASKLIST", true);
						maskListElement.addContent(maskElement);
					}
				}
			}
		}
		catch (FrameworkErrorSignal ne)
		{

			throw new CustomException("SYS-0010", ne.getErrorCode());
			
		}
		catch (greenFrameErrorSignal ne)
		{

			log.info("Product does not have Mask information!!! ");
			throw new CustomException("PRODUCT-0002");
			
		}
		catch (CustomException ce)
		{

			throw new CustomException("SYS-0010", ce.errorDef.getLoc_errorMessage());
			
		}
		catch (Exception e)
		{
	
			throw new CustomException(e);
		}

		return doc;

	}
	
	private Element generateBodyTemplate(Element bodyElement, String machineName, String unitName, String productName,
			List<MaterialProduct> maskLotList) throws CustomException {
		if (maskLotList != null) {
			XmlUtil.addElement(bodyElement, "MACHINENAME", machineName);
			XmlUtil.addElement(bodyElement, "UNITNAME", unitName);
			XmlUtil.addElement(bodyElement, "SUBUNITNAME", StringUtil.EMPTY);
			XmlUtil.addElement(bodyElement, "PRODUCTNAME", StringUtil.EMPTY);
			XmlUtil.addElement(bodyElement, "MASKLIST", StringUtil.EMPTY);
		} else {
			XmlUtil.addElement(bodyElement, "MACHINENAME", machineName);
			XmlUtil.addElement(bodyElement, "UNITNAME", unitName);
			XmlUtil.addElement(bodyElement, "SUBUNITNAME", StringUtil.EMPTY);
			XmlUtil.addElement(bodyElement, "PRODUCTNAME", StringUtil.EMPTY);
			XmlUtil.addElement(bodyElement, "MASKLIST", StringUtil.EMPTY);
		}
		return bodyElement;
	}

	private Element createMaskElement(Document doc, MaskLot maskData, MaterialProduct product) throws CustomException {

		Element maskElement = new Element("MASK");

		Element eleMaskName = new Element("MASKNAME");
		eleMaskName.setText(product.getMaterialName());
		maskElement.addContent(eleMaskName);

		Element eleMaterialLocationName = new Element("MATERIALLOCATIONNAME");
		eleMaterialLocationName.setText(product.getMaterialLocationName());
		maskElement.addContent(eleMaterialLocationName);
		
		maskElement.addContent(new Element("MASKCYCLECOUNT").setText(String.valueOf(maskData.getMaskCycleCount())));

		Element eleMask_OffSet_X = new Element("INSPECTION_OFFSET_X");
		//Double dInitialOffSetX = StringUtil.isNotEmpty(maskData.getInitialOffSetX()) ? Double.valueOf(maskData.getInitialOffSetX()) * 10 : 0;
		//eleMask_OffSet_X.setText(String.valueOf(dInitialOffSetX.intValue()));
		eleMask_OffSet_X.setText(maskData.getInitialOffSetX());
		maskElement.addContent(eleMask_OffSet_X);

		Element eleMask_OffSet_Y = new Element("INSPECTION_OFFSET_Y");
		//Double dInitialOffSetY = StringUtil.isNotEmpty(maskData.getInitialOffSetY()) ? Double.valueOf(maskData.getInitialOffSetY()) * 10 : 0;
		//eleMask_OffSet_Y.setText(String.valueOf(dInitialOffSetY.intValue()));
		eleMask_OffSet_Y.setText(maskData.getInitialOffSetY());
		maskElement.addContent(eleMask_OffSet_Y);

		Element eleMask_OffSet_T = new Element("INSPECTION_OFFSET_THETA");
		eleMask_OffSet_T.setText(maskData.getInitialOffSetTheta());
		maskElement.addContent(eleMask_OffSet_T);

		return maskElement;
	}

}
