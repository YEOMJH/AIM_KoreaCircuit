package kr.co.aim.messolution.durable.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.AssignCarrierInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;

import org.jdom.Document;

public class AssignCarrier extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String messageName = SMessageUtil.getMessageName(doc);
		String sDurableName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String sLotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);

		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(sLotName);

		List<ProductP> productPSequence = new ArrayList<ProductP>();
		List<Product> productDatas = new ArrayList<Product>();

		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(sDurableName);

		CommonValidation.CheckDurableCleanState(durableData);
		CommonValidation.CheckDurableHoldState(durableData);

		if (StringUtil.equals(durableData.getDurableState(), "InUse"))
			throw new CustomException("CST-0006", sDurableName);

		productDatas = ProductServiceProxy.getProductService().allUnScrappedProductsByLot(sLotName);

		for (Iterator<Product> iteratorProduct = productDatas.iterator(); iteratorProduct.hasNext();)
		{
			Product product = iteratorProduct.next();

			ProductP productP = new ProductP();

			productP.setProductName(product.getKey().getProductName());
			productP.setPosition(product.getPosition());

			productPSequence.add(productP);
		}

		AssignCarrierInfo createInfo = MESLotServiceProxy.getLotInfoUtil().AssignCarrierInfo(lotData, sDurableName, productPSequence);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, getEventUser(), getEventComment(), "", "");

		MESLotServiceProxy.getLotServiceImpl().assignCarrier(lotData, createInfo, eventInfo);

		return doc;
	}

}
