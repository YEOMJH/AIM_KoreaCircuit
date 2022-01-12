package kr.co.aim.messolution.product.event.CNX;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jdom.Element;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.validation.CommonValidate;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class SaveSubProductGrade extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String ProductName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", true);
		String ProductJudge = "";
		String GlassAJudge = "";
		String GlassBJudge = "";
		String SubProductGrade = SMessageUtil.getBodyItemValue(doc, "SUBPRODUCTGRADE", false);
		String reasonCode = SMessageUtil.getBodyItemValue(doc, "REASONCODE", false);
		List<Element> scrapPanelList = SMessageUtil.getBodySequenceItemList(doc, "SCRAPPANELLIST", false);

		List<Map<String, Object>> result = new LinkedList<Map<String, Object>>();

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

		Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(ProductName);
		Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(productData.getLotName());

		CommonValidation.checkLotProcessStateWait(lotData);
		CommonValidation.checkJobDownFlag(lotData);
		
		if (GenericServiceProxy.getConstantMap().ProductType_Sheet.equals(productData.getProductType()))
		{
			String subProductJudge = "";

			subProductJudge = SubProductGrade.substring(0, SubProductGrade.length() / 2);
			GlassAJudge = CommonUtil.judgeProductGradeByHalfCutSubProductGrade(subProductJudge);

			result = checkGlass(ProductName + "1");

			if (result.size() > 0)
			{
				UpdateGlassJudge(ProductName + "1", GlassAJudge, ProductName, "ChangeSubProductJudge", eventInfo.getEventUser(), eventInfo.getEventComment(), productData.getProcessOperationName(),
						eventInfo.getEventTime(), subProductJudge);
			}
			else
			{
				InsertGlassJudge(ProductName + "1", GlassAJudge, ProductName, "", "", "ChangeSubProductJudge", eventInfo.getEventUser(), eventInfo.getEventComment(),
						productData.getProcessOperationName(), eventInfo.getEventTime(), subProductJudge);
			}

			subProductJudge = SubProductGrade.substring(SubProductGrade.length() / 2, SubProductGrade.length());
			GlassBJudge = CommonUtil.judgeProductGradeByHalfCutSubProductGrade(subProductJudge);

			result = checkGlass(ProductName + "2");

			if (result.size() > 0)
			{
				UpdateGlassJudge(ProductName + "2", GlassBJudge, ProductName, "ChangeSubProductJudge", eventInfo.getEventUser(), eventInfo.getEventComment(), productData.getProcessOperationName(),
						eventInfo.getEventTime(), subProductJudge);
			}
			else
			{
				InsertGlassJudge(ProductName + "2", GlassBJudge, ProductName, "", "", "ChangeSubProductJudge", eventInfo.getEventUser(), eventInfo.getEventComment(),
						productData.getProcessOperationName(), eventInfo.getEventTime(), subProductJudge);
			}

			if (GlassAJudge.equalsIgnoreCase("N") && GlassBJudge.equalsIgnoreCase("N"))
				ProductJudge = "N";
			else if (GlassAJudge.equalsIgnoreCase("P") || GlassBJudge.equalsIgnoreCase("P"))
				ProductJudge = "P";
			else
				ProductJudge = "G";
		}
		else
		{
			GlassAJudge = CommonUtil.judgeProductGradeByHalfCutSubProductGrade(SubProductGrade);

			result = checkGlass(ProductName);

			if (result.size() > 0)
			{
				UpdateGlassJudge(ProductName, GlassAJudge, ProductName, "ChangeSubProductJudge", eventInfo.getEventUser(), eventInfo.getEventComment(), productData.getProcessOperationName(),
						eventInfo.getEventTime(), SubProductGrade);
			}
			else
			{
				InsertGlassJudge(ProductName, GlassAJudge, ProductName, "", "", "ChangeSubProductJudge", eventInfo.getEventUser(), eventInfo.getEventComment(), productData.getProcessOperationName(),
						eventInfo.getEventTime(), SubProductGrade);
			}

			ProductJudge = GlassAJudge;
		}

		ChangeGradeInfo changeGradeInfo = MESProductServiceProxy.getProductInfoUtil().changeGradeInfo(productData, productData.getPosition(), ProductJudge, productData.getProductProcessState(),
				SubProductGrade, productData.getSubProductGrades2(), productData.getSubProductQuantity1(), productData.getSubProductQuantity2());
		productData = MESProductServiceProxy.getProductServiceImpl().changeGrade(productData, changeGradeInfo, eventInfo);

		// Change LotGrade
		String lotGrade = CommonUtil.judgeLotGradeByProducGrade(ProductJudge);

		List<ProductPGS> productPGS = MESLotServiceProxy.getLotServiceUtil().setProductPGSSequence(doc);
		kr.co.aim.greentrack.lot.management.info.ChangeGradeInfo changeLotGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(lotData, lotGrade, productPGS);

		MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, lotData, changeLotGradeInfo);
		
		//if Panel change to S,insert reasoncode and time to ct_paneljudge
		if(scrapPanelList != null && scrapPanelList.size() > 0)
		{
			for (Element scrapPanel : scrapPanelList) 
			{
				String panelName=scrapPanel.getChild("PANELNAME").getText();
				insertCt_PanelJudge(eventInfo,panelName,reasonCode);
			}
		}

		return doc;
	}

	private void InsertGlassJudge(String glassName, String glassJudge, String sheetName, String processFlowName, String processFlowVersion, String eventName, String eventUser, String eventComment,
			String processOperationName, Timestamp eventTime, String panelGrades) throws CustomException
	{
		try
		{
			StringBuffer sqlForInsertGlassJudge = new StringBuffer();
			sqlForInsertGlassJudge.append("INSERT ");
			sqlForInsertGlassJudge.append("  INTO CT_GLASSJUDGE  ");
			sqlForInsertGlassJudge.append("  (GLASSNAME, GLASSJUDGE, SHEETNAME, LASTEVENTNAME, LASTEVENTUSER, ");
			sqlForInsertGlassJudge.append("   LASTEVENTTIME, LASTEVENTCOMMENT, PROCESSOPERATIONNAME, PANELGRADES) ");
			sqlForInsertGlassJudge.append("VALUES  ");
			sqlForInsertGlassJudge.append("  (:GLASSNAME, :GLASSJUDGE, :SHEETNAME, :LASTEVENTNAME, :LASTEVENTUSER, ");
			sqlForInsertGlassJudge.append("   :LASTEVENTTIME, :LASTEVENTCOMMENT, :PROCESSOPERATIONNAME, :PANELGRADES) ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("GLASSNAME", glassName);
			bindMap.put("GLASSJUDGE", glassJudge);
			bindMap.put("SHEETNAME", sheetName);
			bindMap.put("LASTEVENTNAME", eventName);
			bindMap.put("LASTEVENTUSER", eventUser);
			bindMap.put("LASTEVENTTIME", eventTime);
			bindMap.put("LASTEVENTCOMMENT", eventComment);
			bindMap.put("PROCESSOPERATIONNAME", processOperationName);
			bindMap.put("PANELGRADES", panelGrades);

			GenericServiceProxy.getSqlMesTemplate().update(sqlForInsertGlassJudge.toString(), bindMap);

			StringBuffer insertHistory = new StringBuffer();
			insertHistory.append("INSERT ");
			insertHistory.append("  INTO CT_GLASSJUDGEHISTORY  ");
			insertHistory.append("  (GLASSNAME, TIMEKEY, GLASSJUDGE, SHEETNAME, EVENTNAME, ");
			insertHistory.append("   EVENTUSER, EVENTCOMMENT, EVENTTIME, PROCESSOPERATIONNAME, PANELGRADES) ");
			insertHistory.append("VALUES  ");
			insertHistory.append("  (:GLASSNAME, :TIMEKEY, :GLASSJUDGE, :SHEETNAME, :EVENTNAME, ");
			insertHistory.append("   :EVENTUSER, :EVENTCOMMENT, :EVENTTIME, :PROCESSOPERATIONNAME, :PANELGRADES) ");

			bindMap.put("EVENTCOMMENT", eventComment);
			bindMap.put("EVENTNAME", eventName);
			bindMap.put("EVENTTIME", eventTime);
			bindMap.put("EVENTUSER", eventUser);
			bindMap.put("TIMEKEY", TimeStampUtil.getCurrentEventTimeKey());

			GenericServiceProxy.getSqlMesTemplate().update(insertHistory.toString(), bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "for insert " + glassName + " into CT_GLASSJUDGE");
		}
	}

	private void UpdateGlassJudge(String glassName, String glassJudge, String sheetName, String eventName, String eventUser, String eventComment, String processOperationName, Timestamp eventTime,
			String panelGrades) throws CustomException
	{
		try
		{
			StringBuffer sqlForInsertGlassJudge = new StringBuffer();
			sqlForInsertGlassJudge.append("UPDATE CT_GLASSJUDGE ");
			sqlForInsertGlassJudge.append("   SET GLASSJUDGE = :GLASSJUDGE, ");
			sqlForInsertGlassJudge.append("       SHEETNAME = :SHEETNAME, ");
			sqlForInsertGlassJudge.append("       PROCESSOPERATIONNAME = :PROCESSOPERATIONNAME, ");
			sqlForInsertGlassJudge.append("       LASTEVENTNAME = :LASTEVENTNAME, ");
			sqlForInsertGlassJudge.append("       LASTEVENTUSER = :LASTEVENTUSER, ");
			sqlForInsertGlassJudge.append("       LASTEVENTTIME = :LASTEVENTTIME, ");
			sqlForInsertGlassJudge.append("       LASTEVENTCOMMENT = :LASTEVENTCOMMENT, ");
			sqlForInsertGlassJudge.append("       PANELGRADES = :PANELGRADES ");
			sqlForInsertGlassJudge.append(" WHERE GLASSNAME = :GLASSNAME ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("GLASSNAME", glassName);
			bindMap.put("GLASSJUDGE", glassJudge);
			bindMap.put("SHEETNAME", sheetName);
			bindMap.put("LASTEVENTNAME", eventName);
			bindMap.put("LASTEVENTUSER", eventUser);
			bindMap.put("LASTEVENTTIME", eventTime);
			bindMap.put("LASTEVENTCOMMENT", eventComment);
			bindMap.put("PROCESSOPERATIONNAME", processOperationName);
			bindMap.put("PANELGRADES", panelGrades);

			GenericServiceProxy.getSqlMesTemplate().update(sqlForInsertGlassJudge.toString(), bindMap);

			StringBuffer insertHistory = new StringBuffer();
			insertHistory.append("INSERT INTO CT_GLASSJUDGEHISTORY  ");
			insertHistory.append("  (GLASSNAME, TIMEKEY, GLASSJUDGE, XAXIS, YAXIS, ");
			insertHistory.append("   SHEETNAME, PROCESSFLOWNAME, PROCESSFLOWVERSION, EVENTNAME, EVENTUSER, ");
			insertHistory.append("   EVENTCOMMENT, EVENTTIME, PROCESSOPERATIONNAME, SCRAPFLAG, REUSEFLAG, ");
			insertHistory.append("   PANELGRADES, LOTNAME ) ");
			insertHistory.append("   SELECT GLASSNAME, ");
			insertHistory.append("          :TIMEKEY AS TIMEKEY, ");
			insertHistory.append("          GLASSJUDGE, ");
			insertHistory.append("          XAXIS, ");
			insertHistory.append("          YAXIS, ");
			insertHistory.append("          SHEETNAME, ");
			insertHistory.append("          PROCESSFLOWNAME, ");
			insertHistory.append("          PROCESSFLOWVERSION, ");
			insertHistory.append("          :EVENTNAME AS EVENTNAME, ");
			insertHistory.append("          :EVENTUSER AS EVENTUSER, ");
			insertHistory.append("          :EVENTCOMMENT AS EVENTCOMMENT, ");
			insertHistory.append("          :EVENTTIME AS EVENTTIME, ");
			insertHistory.append("          PROCESSOPERATIONNAME, ");
			insertHistory.append("          SCRAPFLAG, ");
			insertHistory.append("          REUSEFLAG, ");
			insertHistory.append("          PANELGRADES, ");
			insertHistory.append("          LOTNAME ");
			insertHistory.append("     FROM CT_GLASSJUDGE ");
			insertHistory.append("    WHERE GLASSNAME = :GLASSNAME ");

			bindMap.put("EVENTCOMMENT", eventComment);
			bindMap.put("EVENTNAME", eventName);
			bindMap.put("EVENTTIME", eventTime);
			bindMap.put("EVENTUSER", eventUser);
			bindMap.put("TIMEKEY", TimeStampUtil.getCurrentEventTimeKey());

			GenericServiceProxy.getSqlMesTemplate().update(insertHistory.toString(), bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "for update " + glassName + " to the CT_GLASSJUDGE");
		}
	}

	private List<Map<String, Object>> checkGlass(String productName)
	{
		String sql = "SELECT GLASSNAME FROM CT_GLASSJUDGE WHERE GLASSNAME = :GLASSNAME";

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("GLASSNAME", productName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

		return result;
	}
	
	private void insertCt_PanelJudge(EventInfo eventInfo, String panelName,String reasonCode)
	{
		StringBuffer querySql = new StringBuffer();
		querySql.append("SELECT PANELNAME, FIRSTSCRAPRSCODE ");
		querySql.append("  FROM CT_PANELJUDGE ");
		querySql.append(" WHERE PANELNAME = :PANELNAME ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("PANELNAME", panelName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(querySql.toString(), args);

		if (result.size() > 0) // update
		{
			if(result.get(0).get("FIRSTSCRAPRSCODE")!=null)
			{
				return;
			}
			StringBuilder updateSql = new StringBuilder();
			updateSql.append("UPDATE CT_PANELJUDGE ");
			updateSql.append("   SET FIRSTSCRAPRSCODE = :FIRSTSCRAPRSCODE, ");
			updateSql.append("       FIRSTSCRAPTIME = :FIRSTSCRAPTIME, ");
			updateSql.append("       LASTEVENTNAME = :LASTEVENTNAME, ");
			updateSql.append("       LASTEVENTUSER = :LASTEVENTUSER, ");
			updateSql.append("       LASTEVENTTIME = :LASTEVENTTIME, ");
			updateSql.append("       LASTEVENTCOMMENT = :LASTEVENTCOMMENT ");
			updateSql.append(" WHERE  PANELNAME = :PANELNAME ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("FIRSTSCRAPRSCODE", reasonCode);
			bindMap.put("FIRSTSCRAPTIME", eventInfo.getEventTime());
			bindMap.put("LASTEVENTNAME", eventInfo.getEventName());
			bindMap.put("LASTEVENTUSER", eventInfo.getEventUser());
			bindMap.put("LASTEVENTTIME", eventInfo.getEventTime());
			bindMap.put("LASTEVENTCOMMENT", eventInfo.getEventComment());
			bindMap.put("PANELNAME", panelName);
			GenericServiceProxy.getSqlMesTemplate().update(updateSql.toString(), bindMap);
		}
		else
		{// insert
			StringBuffer insertSql = new StringBuffer();
			insertSql.append("INSERT ");
			insertSql.append("  INTO CT_PANELJUDGE  ");
			insertSql.append("  (PANELNAME, PANELJUDGE, OLDPANELJUDGE, GLASSNAME, SHEETNAME, ");
			insertSql.append("   LASTEVENTNAME, LASTEVENTUSER, LASTEVENTTIME, LASTEVENTCOMMENT, PROCESSOPERATIONNAME, ");
			insertSql.append("   LOTNAME, FIRSTSCRAPOPERATIONNAME, FIRSTSCRAPFACTORYNAME,FIRSTSCRAPRSCODE,FIRSTSCRAPTIME) ");
			insertSql.append("VALUES  ");
			insertSql.append("  (:PANELNAME, :PANELJUDGE, :OLDPANELJUDGE, :GLASSNAME, :SHEETNAME, ");
			insertSql.append("   :LASTEVENTNAME, :LASTEVENTUSER, :LASTEVENTTIME, :LASTEVENTCOMMENT, :PROCESSOPERATIONNAME, ");
			insertSql.append("   :LOTNAME, :FIRSTSCRAPOPERATIONNAME, :FIRSTSCRAPFACTORYNAME, :FIRSTSCRAPRSCODE, :FIRSTSCRAPTIME) ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PANELNAME", panelName);
			bindMap.put("PANELJUDGE", "S");
			bindMap.put("OLDPANELJUDGE", "");
			bindMap.put("GLASSNAME", panelName.substring(0, panelName.length() - 3));
			bindMap.put("SHEETNAME", panelName.substring(0, panelName.length() - 4));
			bindMap.put("LASTEVENTNAME", eventInfo.getEventName());
			bindMap.put("LASTEVENTUSER", eventInfo.getEventUser());
			bindMap.put("LASTEVENTTIME", eventInfo.getEventTime());
			bindMap.put("LASTEVENTCOMMENT", eventInfo.getEventComment());
			bindMap.put("PROCESSOPERATIONNAME", "");
			bindMap.put("LOTNAME", "");
			bindMap.put("FIRSTSCRAPOPERATIONNAME", "");
			bindMap.put("FIRSTSCRAPFACTORYNAME", "");
			bindMap.put("FIRSTSCRAPRSCODE", reasonCode);
			bindMap.put("FIRSTSCRAPTIME", eventInfo.getEventTime());



			GenericServiceProxy.getSqlMesTemplate().update(insertSql.toString(), bindMap);
		}
	}

}