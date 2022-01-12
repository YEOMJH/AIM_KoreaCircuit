package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.management.data.ProductSpec;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.jdom.Document;
import org.jdom.Element;

public class ModifyCreateLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productSpecVersion = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECVERSION", true);
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", true);

		List<Element> LotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);

		// ProductSpec info
		ProductSpec productSpecData = GenericServiceProxy.getSpecUtil().getProductSpec(factoryName, productSpecName, productSpecVersion);
		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(productRequestName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeCreate", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		long beforeQty = 0;
		long afterQty = 0;

		for (Element Lot : LotList)
		{
			String lotName = SMessageUtil.getChildText(Lot, "LOTNAME", true);
			String productQty = SMessageUtil.getChildText(Lot, "PRODUCTQUANTITY", true);
			String cycleNo = SMessageUtil.getChildText(Lot, "CYCLENO", true);

			Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
			
			int beforeProdQty = Integer.parseInt(lotData.getUdfs().get("PLANPRODUCTQUANTITY"));

			beforeQty += Long.parseLong(lotData.getUdfs().get("PLANPRODUCTQUANTITY"));
			afterQty += Long.parseLong(productQty);

			double sProcessSubQuantity1 = productSpecData.getSubProductUnitQuantity1();
			double sPlanSubProductQuantity = Double.parseDouble(productQty) * sProcessSubQuantity1;

			lotData.setCreateProductQuantity(Double.parseDouble(productQty));
			
			// Mantis - 0000041
			lotData.setCreateSubProductQuantity(sPlanSubProductQuantity);
			lotData.setCreateSubProductQuantity1(sPlanSubProductQuantity);

			SetEventInfo lotEventInfo = new SetEventInfo();
			lotEventInfo.getUdfs().put("CYCLENO", cycleNo);
			lotEventInfo.getUdfs().put("PLANPRODUCTQUANTITY", productQty);
			lotEventInfo.getUdfs().put("PLANSUBPRODUCTQUANTITY", String.valueOf((int) sPlanSubProductQuantity));

			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, lotEventInfo);

			// Change CreateQuantity
			if (beforeProdQty != Integer.parseInt(productQty))
			{
				if (beforeProdQty > Integer.parseInt(productQty))
				{
					MESWorkOrderServiceProxy.getProductRequestServiceImpl().incrementCreatedQuantityBy(productRequestData, Integer.parseInt(productQty) - beforeProdQty, eventInfo);
				}
				else
				{
					MESWorkOrderServiceProxy.getProductRequestServiceImpl().DecrementCreatedQuantityBy(productRequestData, beforeProdQty - Integer.parseInt(productQty), eventInfo);
				}
			}
		}

		long planQty = productRequestData.getPlanQuantity();
		long createdQty = Long.parseLong(productRequestData.getUdfs().get("CREATEDQUANTITY"));

		if (beforeQty < afterQty)
		{
			if (planQty - createdQty < afterQty - beforeQty)
			{
				throw new CustomException("PRODUCTREQUEST-0030");
			}
		}

		return doc;
	}
}
