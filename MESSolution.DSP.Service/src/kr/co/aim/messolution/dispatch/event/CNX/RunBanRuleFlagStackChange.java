package kr.co.aim.messolution.dispatch.event.CNX;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.management.data.Product;

public class RunBanRuleFlagStackChange extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		CommonValidation.checkLotShippedState(lotData);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeProductFlagStack", getEventUser(), getEventComment(), "", "");

		for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false))
		{
			String sProductName = SMessageUtil.getChildText(eledur, "PRODUCTNAME", false);
			String sFlagStack = SMessageUtil.getChildText(eledur, "FLAGSTACK", false);
			String Name = "FLAGSTACK";

			kr.co.aim.greentrack.product.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
			Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(sProductName);

			MESProductServiceProxy.getProductServiceUtil().updateProductData(Name, sFlagStack, sProductName);

			MESProductServiceProxy.getProductServiceImpl().setEvent(productData, setEventInfo, eventInfo);
		}

		return doc;
	}
}
