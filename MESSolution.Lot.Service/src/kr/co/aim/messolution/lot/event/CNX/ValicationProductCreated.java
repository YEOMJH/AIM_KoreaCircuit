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
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ValicationProductCreated extends SyncHandler{
	public static Log logger = LogFactory.getLog(ValicationProductCreated.class);
	@Override
	public Object doWorks(Document doc) throws CustomException {
		//String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", false);
		String selectAll = SMessageUtil.getBodyItemValue(doc, "SELECTALL", true);
		List<Element> valicationProductList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", false);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateValicationProduct", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		eventInfo.setEventTime(TimeUtils.getCurrentTimestamp());
	
		if(CommonUtil.equalsIn(selectAll, "Y"))
		{
			for (Element Product  : valicationProductList)
			{
				String productName = SMessageUtil.getChildText(Product, "PRODUCTNAME", true);
				String lotName = SMessageUtil.getChildText(Product, "LOTNAME", true);
				String productSpecName= SMessageUtil.getChildText(Product, "PRODUCTSPECNAME", true);
				String ProcessFlowName = SMessageUtil.getChildText(Product, "PROCESSFLOWNAME", true);
				ValicationProduct valicationProduct = new ValicationProduct();
				valicationProduct.setProductName(productName);
				valicationProduct.setProductSpecName(productSpecName);
				valicationProduct.setProcessFlowName(ProcessFlowName);
				valicationProduct.setEngOperationName("NULL");
				valicationProduct.setEngMachineName("NULL");
				valicationProduct.setToFlowName("ALL");
				valicationProduct.setSampleOperationName("ALL");
				valicationProduct.setUserID(eventInfo.getEventUser());;
				valicationProduct.setLotName(lotName);
				valicationProduct.setProductSpecVersion("00001");
				valicationProduct.setProcessFlowVersion("00001");
				valicationProduct.setLastEventName(eventInfo.getEventName());
				valicationProduct.setLastEventTime(eventInfo.getEventTime());
				valicationProduct.setLastEventTimeKey(eventInfo.getEventTimeKey());
				valicationProduct.setLastEventUser(eventInfo.getEventUser());
				valicationProduct.setLastEventComment(eventInfo.getEventComment());	
				valicationProduct.setBaseLineFlag("Y");
				valicationProduct.setALLFlag("Y");
				ExtendedObjectProxy.ValicationProductService().create(eventInfo, valicationProduct);
			}
			
			return doc;
		}
		else
		{
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
				valicationProduct.setUserID(eventInfo.getEventUser());;
				valicationProduct.setLotName(lotName);
				valicationProduct.setProductSpecVersion("00001");
				valicationProduct.setProcessFlowVersion("00001");
				valicationProduct.setLastEventName(eventInfo.getEventName());
				valicationProduct.setLastEventTime(eventInfo.getEventTime());
				valicationProduct.setLastEventTimeKey(eventInfo.getEventTimeKey());
				valicationProduct.setLastEventUser(eventInfo.getEventUser());
				valicationProduct.setLastEventComment(eventInfo.getEventComment());	
				//valicationProduct.setBaseLineFlag("N");
				valicationProduct.setBaseLineFlag("Y");
				ExtendedObjectProxy.ValicationProductService().create(eventInfo, valicationProduct);
			}
			return doc;
		}
		
		
	}
}
