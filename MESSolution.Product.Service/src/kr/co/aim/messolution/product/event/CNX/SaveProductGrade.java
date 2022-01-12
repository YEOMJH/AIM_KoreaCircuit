package kr.co.aim.messolution.product.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.GlassJudge;
import kr.co.aim.messolution.extended.object.management.data.PanelJudge;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ChangeGradeInfo;
import kr.co.aim.greentrack.product.management.info.ext.ProductPGS;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class SaveProductGrade extends SyncHandler {

	private static Log log = LogFactory.getLog(SaveProductGrade.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String messageName = SMessageUtil.getMessageName(doc);

		if (messageName.equals("GetProductGrade"))
		{
		}
		else if (messageName.equals("SetProductGrade"))
		{
			List<Element> glassList = SMessageUtil.getBodySequenceItemList(doc, "GLASSLIST", true);
			List<Element> UnCheckglassList = SMessageUtil.getBodySequenceItemList(doc, "UNCHECKGLASSLIST", false);
			String changeLotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);

			String lotGrade = "G";

			String eventComment = "";

			if (StringUtil.isEmpty(getEventComment()))
				eventComment = messageName;
			else
				eventComment = getEventComment();

			EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, this.getEventUser(), this.getEventComment(), "", "");

			if (glassList.size() > 0)
			{
				for (int i = 0; i < glassList.size(); i++)
				{
					String glassName = glassList.get(i).getChildText("GLASSNAME");
					String glassJudge = glassList.get(i).getChildText("GLASSJUDGE");
					String reuseFlag = glassList.get(i).getChildText("REUSEFLAG");
					String sheetName = glassList.get(i).getChildText("SHEETNAME");
					String processFlowName = glassList.get(i).getChildText("PROCESSFLOWNAME");
					String processFlowVersion = glassList.get(i).getChildText("PROCESSFLOWVERSION");
					String processOperationName = glassList.get(i).getChildText("PROCESSOPERATIONNAME");
					String lotName = glassList.get(i).getChildText("LOTNAME");

					Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(sheetName);
					ChangeGradeInfo changeGradeInfo = MESProductServiceProxy.getProductInfoUtil().changeGradeInfo(productData, productData.getPosition(), glassJudge,
							productData.getProductProcessState(), productData.getSubProductGrades1(), productData.getSubProductGrades2(), productData.getSubProductQuantity1(),
							productData.getSubProductQuantity2());

					productData = MESProductServiceProxy.getProductServiceImpl().changeGrade(productData, changeGradeInfo, eventInfo);

					if (StringUtil.equals(glassJudge, "N"))
						lotGrade = "N";

					GlassJudge glassJudgeData;
					try
					{
						if (reuseFlag.equals("N"))
						{
							log.info("Warning: " + glassName + " ReuserFlag = 'N',so keep GlassJudge = 'N'! ");

							if (!glassJudge.equals("N"))
								throw new CustomException("PRODUCT-9011", glassName);
						}

						glassJudgeData = ExtendedObjectProxy.getGlassJudgeService().selectByKey(true, new Object[] { glassName });

						log.info("update Glass Info to lastest");

						glassJudgeData.setGlassJudge(glassJudge);
						glassJudgeData.setReuseFlag(reuseFlag);
						glassJudgeData.setSheetName(sheetName);
						glassJudgeData.setProcessFlowName(processFlowName);
						glassJudgeData.setProcessFlowVersion(processFlowVersion);
						glassJudgeData.setProcessOperationName(processOperationName);
						glassJudgeData.setLastEventName(eventInfo.getEventName());
						glassJudgeData.setLastEventUser(eventInfo.getEventUser());
						glassJudgeData.setLastEventComment(eventComment);
						glassJudgeData.setLotName(lotName);

						ExtendedObjectProxy.getGlassJudgeService().modify(eventInfo, glassJudgeData);
					}
					catch (NotFoundSignal ne)
					{
						log.info(" No Glass Judge Info, insert the lastest info ");

						glassJudgeData = new GlassJudge(glassName);
						glassJudgeData.setGlassJudge(glassJudge);
						glassJudgeData.setReuseFlag(reuseFlag);
						glassJudgeData.setSheetName(sheetName);
						glassJudgeData.setProcessFlowName(processFlowName);
						glassJudgeData.setProcessFlowVersion(processFlowVersion);
						glassJudgeData.setProcessOperationName(processOperationName);
						glassJudgeData.setLastEventName(eventInfo.getEventName());
						glassJudgeData.setLastEventUser(eventInfo.getEventUser());
						glassJudgeData.setLastEventTime(eventInfo.getEventTime());
						glassJudgeData.setLastEventComment(eventComment);
						glassJudgeData.setLotName(lotName);

						ExtendedObjectProxy.getGlassJudgeService().create(eventInfo, glassJudgeData);

					}

					// update ct_paneljudge
					updatePanelJudge(eventInfo, sheetName, glassName, processOperationName, glassJudge);
				}

			}

			if (UnCheckglassList.size() > 0)
			{
				for (int i = 0; i < UnCheckglassList.size(); i++)
				{
					String glassName = UnCheckglassList.get(i).getChildText("GLASSNAME");
					String glassJudge = UnCheckglassList.get(i).getChildText("GLASSJUDGE");
					String reuseFlag = UnCheckglassList.get(i).getChildText("REUSEFLAG");
					String sheetName = UnCheckglassList.get(i).getChildText("SHEETNAME");
					String processFlowName = UnCheckglassList.get(i).getChildText("PROCESSFLOWNAME");
					String processFlowVersion = UnCheckglassList.get(i).getChildText("PROCESSFLOWVERSION");
					String processOperationName = UnCheckglassList.get(i).getChildText("PROCESSOPERATIONNAME");
					String lotName = UnCheckglassList.get(i).getChildText("LOTNAME");

					if (StringUtil.equals(glassJudge, "N"))
						lotGrade = "N";

					GlassJudge glassJudgeData;

					try
					{
						List<Map<String, Object>> result = checkGlassExist(glassName);

						if (result.size() == 0)
						{
							log.info(" No Glass Judge Info, insert the lastest info ");

							glassJudgeData = new GlassJudge(glassName);
							glassJudgeData.setGlassJudge(glassJudge);
							glassJudgeData.setReuseFlag(reuseFlag);
							glassJudgeData.setSheetName(sheetName);
							glassJudgeData.setProcessFlowName(processFlowName);
							glassJudgeData.setProcessFlowVersion(processFlowVersion);
							glassJudgeData.setProcessOperationName(processOperationName);
							glassJudgeData.setLastEventName(eventInfo.getEventName());
							glassJudgeData.setLastEventUser(eventInfo.getEventUser());
							glassJudgeData.setLastEventTime(eventInfo.getEventTime());
							glassJudgeData.setLastEventComment("");
							glassJudgeData.setLotName(lotName);

							ExtendedObjectProxy.getGlassJudgeService().create(eventInfo, glassJudgeData);
						}

					}
					catch (NotFoundSignal ne)
					{

					}

					// update ct_paneljudge
					updatePanelJudge(eventInfo, sheetName, glassName, processOperationName, glassJudge);
				}

				checkSheetCount(glassList);
			}

			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(changeLotName);

			if (!StringUtils.equals(lotGrade, lotData.getLotGrade()))
			{
				List<ProductPGS> productPGS = MESLotServiceProxy.getLotServiceUtil().setProductPGSSequence(doc);

				kr.co.aim.greentrack.lot.management.info.ChangeGradeInfo changeGradeInfo = MESLotServiceProxy.getLotInfoUtil().changeGradeInfo(lotData, lotGrade, productPGS);

				MESLotServiceProxy.getLotServiceImpl().ChangeGrade(eventInfo, lotData, changeGradeInfo);
			}
		}
		else if (messageName.equals("SetPanelJudge"))
		{
			List<Element> panelList = SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", true);

			String eventComment = "";

			if (StringUtil.isEmpty(getEventComment()))
				eventComment = messageName;
			else
				eventComment = getEventComment();

			EventInfo eventInfo = EventInfoUtil.makeEventInfo(messageName, this.getEventUser(), this.getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

			if (panelList.size() > 0)
			{
				for (int i = 0; i < panelList.size(); i++)
				{
					String panelName = panelList.get(i).getChildText("PANELNAME");
					String panelJudge = panelList.get(i).getChildText("PANELJUDGE");
					String glassName = panelList.get(i).getChildText("GLASSNAME");
					String sheetName = panelList.get(i).getChildText("SHEETNAME");
					String panelXAxis = panelList.get(i).getChildText("XAXIS");
					String panelYAxis = panelList.get(i).getChildText("YAXIS");
					String processOperationName = panelList.get(i).getChildText("PROCESSOPERATIONNAME");

					PanelJudge panelData;
					try
					{
						panelData = ExtendedObjectProxy.getPanelJudgeService().selectByKey(false, new Object[] { panelName });

						if (!StringUtil.equals(panelData.getPaneljudge(), panelJudge))
						{
							log.info("update paneljudge Info to lastest");
							panelData.setPaneljudge(panelJudge);

							panelData.setSheetname(sheetName);
							panelData.setGlassname(glassName);
							panelData.setXaxis(panelXAxis);
							panelData.setYaxis(panelYAxis);

							panelData.setLasteventcomment(eventComment);
							panelData.setLasteventname(eventInfo.getEventName());
							panelData.setLasteventtime(eventInfo.getEventTime());
							panelData.setLasteventuser(eventInfo.getEventUser());
							panelData.setProcessOperationName(processOperationName);

							ExtendedObjectProxy.getPanelJudgeService().modify(eventInfo, panelData);
						}
					}
					catch (NotFoundSignal ne)
					{
						log.info(" No Panel Judge Info");
					}
				}
			}
		}
		else
		{
			throw new CustomException("SYS-1001", messageName);
		}

		return doc;
	}

	private void updatePanelJudge(EventInfo eventInfo, String sheetName, String glassName, String processOperationName, String panelJudge) throws CustomException
	{
		List<PanelJudge> panelList = ExtendedObjectProxy.getPanelJudgeService().getPanelList(sheetName, glassName);

		if (panelList != null && panelList.size() > 0)
		{
			for (int i = 0; i < panelList.size(); i++)
			{
				String panelName = panelList.get(i).getPanelName();
				PanelJudge panelData = ExtendedObjectProxy.getPanelJudgeService().selectByKey(false, new Object[] { panelName });
				panelData.setPaneljudge(panelJudge);
				ExtendedObjectProxy.getPanelJudgeService().modify(eventInfo, panelData);
			}
		}
	}

	private List<Map<String, Object>> checkGlassExist(String glassName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT * ");
		sql.append("  FROM CT_GLASSJUDGE G ");
		sql.append(" WHERE G.GLASSNAME = :GLASSNAME ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("GLASSNAME", glassName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		return result;
	}

	private void checkSheetCount(List<Element> glassList) throws CustomException
	{
		String bindProductList = CommonUtil.makeListForQuery(glassList, "SHEETNAME");

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT * ");
		sql.append("  FROM (SELECT G.SHEETNAME SHEETNAME, COUNT (G.GLASSNAME) COUNT ");
		sql.append("          FROM CT_GLASSJUDGE G ");
		sql.append("         WHERE G.SHEETNAME IN ( ");
		sql.append(bindProductList);
		sql.append(")");
		sql.append("        GROUP BY G.SHEETNAME) ");
		sql.append(" WHERE COUNT < 2 ");

		Map<String, Object> bindMap = new HashMap<String, Object>();

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		if (result.size() > 0)
			throw new CustomException("PRODUCT-9010");
	}
}