package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.SetEventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class SetMQCProductUsedLimit extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String dummyUsedLimit = SMessageUtil.getBodyItemValue(doc, "DUMMYUSEDLIMIT", false);

		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("SetProductDummyUsedLimit", getEventUser(), getEventComment(), "", "");

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		CommonValidation.checkLotState(lotData);
		CommonValidation.checkLotProcessStateWait(lotData);

		for (Element ProductDummyUsedLimitList : productList)
		{
			String productName = ProductDummyUsedLimitList.getChild("PRODUCTNAME").getText();
			Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
			
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("MQCPRODUCTUSEDLIMIT", dummyUsedLimit);
			
			ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setEventInfo);
		}

		return doc;
	}
}
