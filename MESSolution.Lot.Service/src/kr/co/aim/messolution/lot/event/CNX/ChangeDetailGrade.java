package kr.co.aim.messolution.lot.event.CNX;

import java.util.Map;

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
import kr.co.aim.greentrack.product.management.info.ChangeGradeInfo;

import org.jdom.Document;
import org.jdom.Element;

public class ChangeDetailGrade extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		CommonValidation.checkLotShippedState(lotData);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeDetailGrade", getEventUser(), getEventComment(), "", "");

		for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false))
		{
			String sProductName = SMessageUtil.getChildText(eledur, "PRODUCTNAME", false);
			String sDetailGrade = SMessageUtil.getChildText(eledur, "DETAILGRADE", false);

			if (StringUtil.isEmpty(sDetailGrade))
				continue;

			Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(sProductName);

			ChangeGradeInfo changeGradeInfo = MESProductServiceProxy.getProductInfoUtil().changeGradeInfo(productData, productData.getPosition(), productData.getProductGrade(),
					productData.getProductProcessState(), productData.getSubProductGrades1(), productData.getSubProductGrades2(), productData.getSubProductQuantity1(),
					productData.getSubProductQuantity2());

			changeGradeInfo.getUdfs().put("DETAILGRADE", sDetailGrade);
			productData = MESProductServiceProxy.getProductServiceImpl().changeGrade(productData, changeGradeInfo, eventInfo);
		}

		return doc;
	}
}
