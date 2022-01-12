package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ext.ConsumedMaterial;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGSRC;

public class PostCellCancelTrackIn extends SyncHandler {
	public Object doWorks(Document doc) throws CustomException{
		String carrierName = "";
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);
		//Common
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);		
		Map<String, String> assignCarrierUdfs = new HashMap<String, String>();
		Map<String, String> deassignCarrierUdfs = new HashMap<String, String>();
		
		Lot cancelTrackInLot = new Lot();
		
		// get LotList by ProductList
		List<Map<String, Object>> lotListByProductList = MESLotServiceProxy.getLotServiceUtil().getLotListByProductList(productList);
		
		//nothing to track out case
		if (lotListByProductList.size() < 1)
			new CustomException("LOT-9001", "PRODUCTLIST : " + CommonUtil.makeListForQuery(productList, "PRODUCTNAME"));
		
		// first Lot of LotList is Base Lot : much productQty Lot of LotList
		Lot baseLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotListByProductList.get(0).get("LOTNAME").toString());
		
		if(lotListByProductList.size() == 1 && (productList.size() == baseLotData.getProductQuantity()))
		{//after update on subProductUnitQuantity then keep original
			cancelTrackInLot = MESLotServiceProxy.getLotInfoUtil().getLotData(baseLotData.getKey().getLotName());
		}
		
		List<ProductPGSRC> productPGSRCSequence = MESLotServiceProxy.getLotInfoUtil().setProductPGSRCSequence(
				SMessageUtil.getBodyElement(doc));

		deassignCarrierUdfs.clear();
		assignCarrierUdfs.clear();
		List<ConsumedMaterial> lotConsumedMaterail = new ArrayList<ConsumedMaterial>();
		
		eventInfo = EventInfoUtil.makeEventInfo("CancelTrackIn", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		cancelTrackInLot = MESLotServiceProxy.getLotServiceImpl().cancelTrackIn(
		eventInfo, cancelTrackInLot, productPGSRCSequence, assignCarrierUdfs, deassignCarrierUdfs, lotConsumedMaterail,carrierName);
		
		return doc;
	}
}
