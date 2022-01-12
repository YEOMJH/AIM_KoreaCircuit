package kr.co.aim.messolution.durable.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.RelocateProductsInfo;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductP;

import org.jdom.Document;
import org.jdom.Element;

public class ChangeCarrierMapForTP extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeCSTMapForTP", this.getEventUser(), this.getEventComment(), "", "");

		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);

		List<ProductP> productPSequence = new ArrayList<ProductP>();

		for (Element product : productList)
		{
			String productName = SMessageUtil.getChildText(product, "PRODUCTNAME", true);
			String position = SMessageUtil.getChildText(product, "POSITION", true);
			String slotPosition = SMessageUtil.getChildText(product, "SLOTPOSITION", true);

			ProductP productP = new ProductP();
			productP.setProductName(productName);
			productP.setPosition(Long.valueOf(position));

			productP.getUdfs().put("SLOTPOSITION", slotPosition);
			productPSequence.add(productP);
		}

		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

		RelocateProductsInfo relocateInfo = MESLotServiceProxy.getLotInfoUtil().relocateProductsInfo(lotData, productPSequence, lotData.getProductQuantity());

		MESLotServiceProxy.getLotServiceImpl().relocateProducts(lotData, relocateInfo, eventInfo);

		return doc;
	}

}
