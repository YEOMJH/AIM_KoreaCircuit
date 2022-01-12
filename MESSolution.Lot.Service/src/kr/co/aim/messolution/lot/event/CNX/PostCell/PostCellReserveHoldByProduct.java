package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.List;

import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.data.ProductSpec;

import org.jdom.Document;
import org.jdom.Element;

public class PostCellReserveHoldByProduct extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String processFlowName = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWNAME", true);
		String processFlowVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSFLOWVERSION", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String processOperationVersion = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONVERSION", true);

		List<Element> ReasonCodeList = SMessageUtil.getBodySequenceItemList(doc, "REASONCODELIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("FutureHold", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
		ProductSpec productSpec = MESProductServiceProxy.getProductServiceUtil().getProductSpecByProductName(productData);

		//CommonValidation.checkConsumedProduct(productData, productName);

		// Panel x, y
		int xProductCount = Integer.parseInt(productSpec.getUdfs().get("PRODUCTCOUNTTOXAXIS"));
		int yProductCount = Integer.parseInt(productSpec.getUdfs().get("PRODUCTCOUNTTOYAXIS"))/2;

		// set for count
		int xCnt, yCnt;

		if (xProductCount > 14)
			xCnt = 65 + xProductCount + 2;
		else if (xProductCount > 8)
			xCnt = 65 + xProductCount + 1;
		else
			xCnt = 65 + xProductCount;

		if (yProductCount > 14)
			yCnt = 65 + yProductCount + 2;
		else if (yProductCount > 8)
			yCnt = 65 + yProductCount + 1;
		else
			yCnt = 65 + yProductCount;

		List<Object[]> updateFutureActionArgList = new ArrayList<Object[]>();

		for (Element code : ReasonCodeList)
		{
			String reasonCodeType = code.getChildText("REASONCODETYPE");
			String reasonCode = code.getChildText("REASONCODE");

			for (int i = 65; i < yCnt; i++)
			{
				if (i == 73 || i == 79)
					continue;

				for (int j = 65; j < xCnt; j++)
				{
					if (j == 73 || j == 79)
						continue;

					// New Panel
					String newPanelName = productName + (char) i + (char) j + "0";

					List<Object> faBindList = new ArrayList<Object>();

					faBindList.add(newPanelName);
					faBindList.add(factoryName);
					faBindList.add(processFlowName);
					faBindList.add(processFlowVersion);
					faBindList.add(processOperationName);
					faBindList.add(processOperationVersion);
					faBindList.add("0");
					faBindList.add("hold");
					faBindList.add("System");
					faBindList.add(eventInfo.getEventName());
					faBindList.add(eventInfo.getEventUser());
					faBindList.add(eventInfo.getEventComment());
					faBindList.add(reasonCodeType);
					faBindList.add(reasonCode);
					faBindList.add(productName);
					faBindList.add("");
					faBindList.add("");
					faBindList.add(eventInfo.getEventTimeKey());

					updateFutureActionArgList.add(faBindList.toArray());
				}
			}
		}

		insertLotFutureActionData(updateFutureActionArgList);

		return doc;
	}

	private void insertLotFutureActionData(List<Object[]> updateFutureActionArgList) throws CustomException
	{
		StringBuffer sqlFA = new StringBuffer();
		sqlFA.append("INSERT INTO CT_LOTFUTUREACTION  ");
		sqlFA.append("(LOTNAME, FACTORYNAME, PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME, ");
		sqlFA.append(" PROCESSOPERATIONVERSION, POSITION, ACTIONNAME, ACTIONTYPE, LASTEVENTNAME, ");
		sqlFA.append(" LASTEVENTUSER, LASTEVENTCOMMENT, REASONCODETYPE, REASONCODE, ATTRIBUTE1, ");
		sqlFA.append(" ATTRIBUTE2, ATTRIBUTE3, LASTEVENTTIMEKEY) ");
		sqlFA.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");

		StringBuffer sqlFAHistory = new StringBuffer();
		sqlFAHistory.append("INSERT INTO CT_LOTFUTUREACTIONHIST  ");
		sqlFAHistory.append("(LOTNAME, FACTORYNAME, PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME, ");
		sqlFAHistory.append(" PROCESSOPERATIONVERSION, POSITION, ACTIONNAME, ACTIONTYPE, EVENTNAME, ");
		sqlFAHistory.append(" EVENTUSER, EVENTCOMMENT, REASONCODETYPE, REASONCODE, ATTRIBUTE1, ");
		sqlFAHistory.append(" ATTRIBUTE2, ATTRIBUTE3, TIMEKEY) ");
		sqlFAHistory.append("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlFA.toString(), updateFutureActionArgList);
		MESLotServiceProxy.getLotServiceUtil().updateBatch(sqlFAHistory.toString(), updateFutureActionArgList);

	}
}
