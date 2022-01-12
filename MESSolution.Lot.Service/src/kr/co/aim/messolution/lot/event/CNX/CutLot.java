package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SeparateInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductNSubProductPGQS;

import org.jdom.Document;

public class CutLot extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String productQuantity = SMessageUtil.getBodyItemValue(doc, "PRODUCTQTY", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String productType = "";
		String subProductType = "";

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		List<String> productNameList = CommonUtil.makeList(SMessageUtil.getBodyElement(doc), "PRODUCTLIST", "PRODUCTNAME");

		if (StringUtil.equals(factoryName, "OLED"))
		{
			productType = "Glass";
			subProductType = "Panel";
		}
		else
		{
			productType = "Panel";
			subProductType = "";
		}

		double dProductQty = Double.valueOf(productQuantity).doubleValue();

		List<ProductNSubProductPGQS> productNSubProductPGQSSequence = MESLotServiceProxy.getLotInfoUtil().setProductNSubProductPGQSSequenceCUT(doc);

		SeparateInfo separateInfo = MESLotServiceProxy.getLotInfoUtil().separateInfo(lotData, productNSubProductPGQSSequence, dProductQty, productType, subProductType);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Cut", getEventUser(), getEventComment(), "", "");

		MESLotServiceProxy.getLotServiceImpl().CutLot(eventInfo, lotData, separateInfo);

		// Set LotName on ProductHistory for Consumed Product
		setLotNameOnProductHistory(productNameList, eventInfo.getEventTimeKey(), lotName);

		return doc;
	}

	private void setLotNameOnProductHistory(List<String> productNameList, String timeKey, String lotName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE PRODUCTHISTORY ");
		sql.append("   SET LOTNAME = :LOTNAME ");
		sql.append(" WHERE PRODUCTNAME IN (:PRODUCTNAMELIST) ");
		sql.append("   AND TIMEKEY = :TIMEKEY ");
		sql.append("   AND PRODUCTSTATE = 'Consumed' ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("LOTNAME", lotName);
		args.put("PRODUCTNAMELIST", productNameList);
		args.put("TIMEKEY", timeKey);

		try
		{
			int count = GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), args);
			eventLog.debug(count + " Rows updated to set LotName: " + lotName);
		}
		catch (Exception e)
		{
			eventLog.debug("Failed to update set LotName: " + lotName);
		}
	}
}
