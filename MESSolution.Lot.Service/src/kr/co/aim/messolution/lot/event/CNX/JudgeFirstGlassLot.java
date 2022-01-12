package kr.co.aim.messolution.lot.event.CNX;

import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.SetEventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class JudgeFirstGlassLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		List<Element> productList = SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("JudgeFirstGlassLot", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		eventInfo.setEventComment("JudgeFirstGlassLot: " + eventInfo.getEventComment());

		// Update Product Grade
		for (Element productE : productList)
		{
			String productName = SMessageUtil.getChildText(productE, "PRODUCTNAME", true);
			Product product = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(productName);

			String productGrade = SMessageUtil.getChildText(productE, "PRODUCTJUDGE", true);

			product.setProductGrade(productGrade);
			ProductServiceProxy.getProductService().update(product);

			// Insert History
			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.setUdfs(product.getUdfs());
			ProductServiceProxy.getProductService().setEvent(product.getKey(), eventInfo, setEventInfo);
		}

		// After Decide Lot Grade, Update Lot Grade
		// Decide Lot Grade
		String lotJudge = CommonUtil.judgeLotGradeByProductList(productList, "PRODUCTJUDGE");

		// Update Lot Grade
		Lot lot = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		lot.setLotGrade(lotJudge);
		LotServiceProxy.getLotService().update(lot);

		// Insert History
		kr.co.aim.greentrack.lot.management.info.SetEventInfo setLotEventInfo = new kr.co.aim.greentrack.lot.management.info.SetEventInfo();
		setLotEventInfo.setUdfs(lot.getUdfs());
		LotServiceProxy.getLotService().setEvent(lot.getKey(), eventInfo, setLotEventInfo);

		return doc;
	}
}
