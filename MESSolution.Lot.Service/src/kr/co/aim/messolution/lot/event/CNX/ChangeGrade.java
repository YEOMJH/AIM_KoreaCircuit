package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.PanelJudge;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ChangeGrade extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);

		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);

		// validation.
		String lotGrade = SMessageUtil.getBodyItemValue(doc, "LOTGRADE", true);
		CommonValidation.checkReworkLotGrade(lotData, lotGrade);
		CommonValidation.checkLotShippedState(lotData);
		CommonValidation.checkJobDownFlag(lotData);
		CommonValidation.checkLotProcessState(lotData);

		boolean isfound = false;
		String sCellName = "";

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeGrade", getEventUser(), getEventComment(), "", "");

		PanelJudge panelJudgeData = new PanelJudge();
		for (Element eledur : SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false))
		{
			String sProductName = SMessageUtil.getChildText(eledur, "PRODUCTNAME", false);
			sCellName = sProductName;

			// judge product table data
			if (lotData.getProductSpecName().substring(2, 5).equals("145") || lotData.getProductSpecName().substring(2, 5).equals("120"))
			{
				isfound = productExist(sProductName);
			}

			if (isfound == true)
				sProductName = sProductName.substring(0, 14);

			Product productData = MESProductServiceProxy.getProductInfoUtil().getProductByProductName(sProductName);

			// insert into ct_paneljudge table
			if (factoryName.equals("POSTCELL"))
			{
				panelJudgeData.setPanelName(sCellName);
				panelJudgeData.setSheetname(sProductName.substring(0, 11));
				panelJudgeData.setGlassname(sProductName.substring(0, 12));
				panelJudgeData.setLasteventname(eventInfo.getEventName());
				panelJudgeData.setLasteventuser(this.getEventUser());
				panelJudgeData.setLasteventtime(eventInfo.getEventTime());
				panelJudgeData.setLasteventcomment(this.getEventComment());

				panelJudgeData.setProcessOperationName(productData.getProcessOperationName());
				boolean existProductName = ExtendedObjectProxy.getPanelJudgeService().checkExistProductName(sCellName);

				if (existProductName == true)
					ExtendedObjectProxy.getPanelJudgeService().modify(eventInfo, panelJudgeData);
				else
					throw new CustomException("LOT-0102");
			}

			// ProductTable PanelGrade Update
			if (isfound == false)
				ProductServiceProxy.getProductService().update(productData);
		}

		lotGrade = CommonUtil.judgeLotGradeByProductList(SMessageUtil.getBodySequenceItemList(doc, "PRODUCTLIST", false), "PRODUCTGRADE");

		if (isfound == false)
		{
			//Mantis - 0000061
			if (StringUtils.equals(lotData.getLotGrade(), "P") && StringUtils.equals(lotGrade, "R"))
			{
				throw new CustomException("LOT-0223"); 
			}
			
			List<ProductPGS> productPGS = MESLotServiceProxy.getLotServiceUtil().setProductPGSSequence(doc);

			ChangeGradeInfo changeGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(lotData, lotGrade, productPGS);

			MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, lotData, changeGradeInfo);
		}

		return doc;
	}

	private boolean productExist(String productName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT PRODUCTNAME ");
		sql.append("  FROM PRODUCT ");
		sql.append(" WHERE PRODUCTNAME = :PRODUCTNAME ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PRODUCTNAME", productName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> Result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		boolean check = true;

		if (Result.size() > 0)
			check = false;

		return check;
	}
}
