package kr.co.aim.messolution.lot.event.CNX;


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.ValicationProduct;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class CancelValicationProduct extends SyncHandler{
	public static Log logger = LogFactory.getLog(CancelValicationProduct.class);
	@Override
	public Object doWorks(Document doc) throws CustomException {
		//String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		List<Element> valicationProductList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", false);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CancelValicationProduct", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		eventInfo.setEventTime(TimeUtils.getCurrentTimestamp());
		for (Element Product  : valicationProductList)
		{
			String productName = SMessageUtil.getChildText(Product, "PRODUCTNAME", true);
			String lotName = SMessageUtil.getChildText(Product, "LOTNAME", true);
			String productSpecName= SMessageUtil.getChildText(Product, "PRODUCTSPECNAME", true);
			String ProcessFlowName = SMessageUtil.getChildText(Product, "PROCESSFLOWNAME", true);
			String engOperationName = SMessageUtil.getChildText(Product, "ENGOPERATIONNAME", true);
			String engMachine = SMessageUtil.getChildText(Product, "ENGMACHINE", true);
			String toFlowName = SMessageUtil.getChildText(Product, "TOFLOWNAME", true);
			String SampleOperation = SMessageUtil.getChildText(Product, "SAMPLEOPERATIONNAME", true);
			ValicationProduct valicationProduct = new ValicationProduct();
			valicationProduct.setProductName(productName);
			valicationProduct.setProductSpecName(productSpecName);
			valicationProduct.setProcessFlowName(ProcessFlowName);
			valicationProduct.setEngOperationName(engOperationName);
			valicationProduct.setEngMachineName(engMachine);
			valicationProduct.setToFlowName(toFlowName);
			valicationProduct.setSampleOperationName(SampleOperation);
			ExtendedObjectProxy.ValicationProductService().remove(eventInfo,valicationProduct);
		}
		
		
		return doc;
	}
}

