package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.jdom.Document;

public class OfflineReviewProductImageJudge extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String ProductName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String ProcessOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String MachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String ImageName = SMessageUtil.getBodyItemValue(doc, "IMAGENAME", true);
		String DefectCode = SMessageUtil.getBodyItemValue(doc, "DEFECTCODE", true);
		String DefectX = SMessageUtil.getBodyItemValue(doc, "DEFECTX", true);
		String DefectY = SMessageUtil.getBodyItemValue(doc, "DEFECTY", true);
		String PanelName = SMessageUtil.getBodyItemValue(doc, "PANELNAME", false);

		// Insert
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("OfflineReviewProductImageJudge", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		int qty = getOfflineReviewProductImageJudge(ProductName, ProcessOperationName, MachineName, ImageName);

		String ActionKind = "";

		if (qty == 0)
			ActionKind = "Insert";
		else
			ActionKind = "Update";

		MESLotServiceProxy.getLotServiceImpl().insertCtOfflineReviewProductImageJudge(eventInfo, ProductName, ProcessOperationName, MachineName, ImageName, DefectCode, PanelName, ActionKind, DefectX,
				DefectY);

		return doc;
	}

	private int getOfflineReviewProductImageJudge(String productName, String processOperationName, String machineName, String imageName)
	{
		int qty = 0;

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT COUNT (PRODUCTNAME) AS QTY ");
		sql.append("  FROM CT_OFFLINEREVIEWIMAGECODE ");
		sql.append(" WHERE PRODUCTNAME = :PRODUCTNAME ");
		sql.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND MACHINENAME = :MACHINENAME ");
		sql.append("   AND IMAGESEQ = :IMAGENAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("PRODUCTNAME", productName);
		args.put("PROCESSOPERATIONNAME", processOperationName);
		args.put("MACHINENAME", machineName);
		args.put("IMAGENAME", imageName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		if (result.size() > 0)
		{
			String quantity = ConvertUtil.getMapValueByName(result.get(0), "QTY");
			qty = Integer.parseInt(quantity);
		}

		return qty;
	}

}
