package kr.co.aim.messolution.lot.event.CNX;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LOIPanelCodeList;
import kr.co.aim.messolution.extended.object.management.data.ReserveRepairPanelInfo;
import kr.co.aim.messolution.extended.object.management.data.ReviewComponentHistory;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ChangeGradeInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

import com.sun.xml.internal.bind.v2.model.core.BuiltinLeafInfo;

public class ReviewProductJudgeForAT extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String FactoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String ProductName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String ProcessOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", true);
		String MachineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String ProductJudge = SMessageUtil.getBodyItemValue(doc, "PRODUCTJUDGE", false);
		String GlassAJudge = SMessageUtil.getBodyItemValue(doc, "GLASSAJUDGE", false);
		String GlassBJudge = SMessageUtil.getBodyItemValue(doc, "GLASSBJUDGE", false);
		String SubProductGrade = SMessageUtil.getBodyItemValue(doc, "SUBPRODUCTGRADE", false);
		String SubProductGrades2 = SMessageUtil.getBodyItemValue(doc, "SUBPRODUCTGRADES2", false);
		String ActionType = SMessageUtil.getBodyItemValue(doc, "ACTIONTYPE", true);
		String detailGrade = SMessageUtil.getBodyItemValue(doc, "DETAILGRADE", false);
		String glassADetailGrade = SMessageUtil.getBodyItemValue(doc, "GLASSADETAILGRADE", false);
		String glassBDetailGrade = SMessageUtil.getBodyItemValue(doc, "GLASSBDETAILGRADE", false);
		String UnitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);

		List<Element> PanelList = SMessageUtil.getBodySequenceItemList(doc, "PANELJUDGELIST", false);
		List<Element> LOIDefectList = SMessageUtil.getBodySequenceItemList(doc, "LOIDEFECTLIST", false);
		List<Element> reserveRepairList = SMessageUtil.getBodySequenceItemList(doc, "RESERVEREPAIRLIST", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(ActionType, getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		Product productInfo = MESProductServiceProxy.getProductServiceUtil().getProductData(ProductName);
		String currentFlow=productInfo.getProcessFlowName();
		String productSpecName=productInfo.getProductSpecName();
		String aoiFlow="";
		String currectOper=productInfo.getProcessOperationName();
		ProcessOperationSpec operationSpecData = CommonUtil.getProcessOperationSpec(FactoryName,currectOper ,"00001");

		if (ActionType.equals("AssignUser"))
		{
			int qty = getReviewProductJudge(ProductName, ProcessOperationName, MachineName);

			String ActionKind = "";

			if (qty == 0)
				ActionKind = "Insert";
			else
				ActionKind = "Update";

			MESLotServiceProxy.getLotServiceImpl().insertCtReviewProductJudge(eventInfo, ProductName, ProcessOperationName, MachineName, ProductJudge, ActionKind,false);
			
			try
			{
				List<ReviewComponentHistory> reviewComponentHisList=ExtendedObjectProxy.getReviewComponentHistoryService().select(
						" PRODUCTNAME=? AND PROCESSOPERATIONNAME=? AND MATERIALLOCATIONNAME=? ", new Object[]{ProductName,ProcessOperationName,UnitName  });
				if(reviewComponentHisList!=null &&reviewComponentHisList.size()>0)
				{
					for(ReviewComponentHistory reviewComponentHisInfo:reviewComponentHisList)
					{
						reviewComponentHisInfo.setAssignTime(eventInfo.getEventTime());
						reviewComponentHisInfo.setAssignUser(eventInfo.getEventUser());	
					}
					ExtendedObjectProxy.getReviewComponentHistoryService().update(reviewComponentHisList);
				}
			}
			catch(greenFrameDBErrorSignal n)
			{
				;
			}	
		}
		else if(ActionType.equals("AssignDMUser"))
		{
			int qty = getReviewProductJudge(ProductName, ProcessOperationName, MachineName);

			String ActionKind = "";

			if (qty == 0)
				ActionKind = "Insert";
			else
				ActionKind = "Update";

			MESLotServiceProxy.getLotServiceImpl().insertCtReviewProductJudge(eventInfo, ProductName, ProcessOperationName, MachineName, ProductJudge, ActionKind,true);		
		}
		else
		{
			if (ActionType.equals("ClearAssignUser")||ActionType.equals("ClearDMAssignUser"))
			{
				eventInfo.setEventUser("");
			}

			MESLotServiceProxy.getLotServiceImpl().insertCtReviewProductJudge(eventInfo, ProductName, ProcessOperationName, MachineName, ProductJudge, "Update",false);
		}
		
		return doc;
	}

	public int getReviewProductJudge(String ProductName, String ProcessOperationName, String MachineName)
	{
		int qty = 0;

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT COUNT (PRODUCTNAME) AS QTY ");
		sql.append("  FROM CT_REVIEWPRODUCTJUDGE ");
		sql.append(" WHERE PRODUCTNAME = :PRODUCTNAME ");
		sql.append("   AND PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME ");
		sql.append("   AND MACHINENAME = :MACHINENAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("PRODUCTNAME", ProductName);
		args.put("PROCESSOPERATIONNAME", ProcessOperationName);
		args.put("MACHINENAME", MachineName);

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
