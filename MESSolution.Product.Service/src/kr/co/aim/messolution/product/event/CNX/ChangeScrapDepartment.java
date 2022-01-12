package kr.co.aim.messolution.product.event.CNX;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.product.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangeScrapDepartment extends SyncHandler{
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String reasonCodeType = SMessageUtil.getBodyItemValue(doc, "REASONCODETYPE", true);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeScrapDepartment", this.getEventUser(), this.getEventComment(), "", "");

		Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);

		if (!StringUtils.equals(productData.getProductState(),"Scrapped"))
			throw new CustomException("PRODUCT-0006");
		
		eventInfo.setReasonCode(reasonCode);
		eventInfo.setReasonCodeType(reasonCodeType + " - " + "ScrapGlass");

		ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, new SetEventInfo());

		return doc;
	}

}
