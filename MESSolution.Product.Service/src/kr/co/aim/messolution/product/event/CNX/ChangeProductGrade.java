package kr.co.aim.messolution.product.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.GlassJudge;
import kr.co.aim.messolution.extended.object.management.data.PanelJudge;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.product.management.data.Product;
import kr.co.aim.greentrack.product.management.info.ChangeGradeInfo;
 
public class ChangeProductGrade extends SyncHandler {
	private static Log log = LogFactory.getLog(ChangeProductGrade.class);
	@Override
	public Object doWorks(Document doc)
		throws CustomException 
	{
		String lotName = SMessageUtil.getBodyItemValue(doc, "LOTNAME", true);
		String productName = SMessageUtil.getBodyItemValue(doc, "SHEETNAME", true);
		String productGrade = SMessageUtil.getBodyItemValue(doc, "SHEETJUDGE", true);
		String processOperationName = SMessageUtil.getBodyItemValue(doc, "PROCESSOPERATIONNAME", false);
		
		Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);
		
		Map<String, String> productUdfs = productData.getUdfs();
		productUdfs.put("RSFLAG", "Y");
		productData.setUdfs(productUdfs);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), null, null);
		 
		
		//judge
		ChangeGradeInfo changeGradeInfo = MESProductServiceProxy.getProductInfoUtil().changeGradeInfo(productData, productData.getPosition(), productGrade, productData.getProductProcessState(),
											productData.getSubProductGrades1(), productData.getSubProductGrades2(), productData.getSubProductQuantity1(), productData.getSubProductQuantity2());
		
		productData = MESProductServiceProxy.getProductServiceImpl().changeGrade(productData, changeGradeInfo, eventInfo);
		
		
		
		List<Map<String, Object>> RemoveOperation = MESProductServiceProxy.getProductServiceUtil().getEnumList("ArrayJudgeRemoveOperation", processOperationName);

		Lot lotData = MESLotServiceProxy.getLotInfoUtil().getLotData(lotName);
		if(!lotData.getLotProcessState().equalsIgnoreCase("RUN"))
		{
			throw new CustomException("LOT-9020", lotName, lotData.getLotProcessState());
		}


		Map<String,Object> coverFlag = new HashMap<String,Object>();

		List<Map<String, Object>> coverAll = MESProductServiceProxy.getProductServiceUtil().getEnumList("ArrayJudgeCoverAll", lotData.getMachineName());
		if(coverAll.size() <= 0)
			coverFlag.put("ArrayJudgeCoverAll", false);
		else
			coverFlag.put("ArrayJudgeCoverAll", true);

		List<Map<String, Object>> coverExceptPanelJudgeNMachine = MESProductServiceProxy.getProductServiceUtil().getEnumList("ArrayJudgeCoverExceptPanelJudgeN", lotData.getMachineName());
		if(coverExceptPanelJudgeNMachine.size() <= 0)
			coverFlag.put("ArrayJudgeCoverExceptPanelJudgeN", false);
		else
			coverFlag.put("ArrayJudgeCoverExceptPanelJudgeN", true);
		
		
		
		if(RemoveOperation.size() <= 0)
		{
			//upsert Glass
			for (Element eleGlass : SMessageUtil.getBodySequenceItemList(doc, "GLASSLIST", false))
			{
				try
				{
					String glassName = SMessageUtil.getChildText(eleGlass, "GLASSNAME", true);
					
				
					if (glassName.length() < 12)
					{
						continue;
					}
					
					String glassJudge = SMessageUtil.getChildText(eleGlass, "GLASSJUDGE", true);
					String glassXAxis = SMessageUtil.getChildText(eleGlass, "XAXIS", false);
					String glassYAxis = SMessageUtil.getChildText(eleGlass, "YAXIS", false);
					
					GlassJudge glassData;
					try
					{
						eventInfo.setEventName("ChangeGrade");
						
						glassData = ExtendedObjectProxy.getGlassJudgeService().selectByKey(false, new Object[] {glassName});
						
						if (!Boolean.parseBoolean(coverFlag.get("ArrayJudgeCoverAll").toString()))
						{
							if(StringUtil.equals(glassData.getReuseFlag(), "N"))
					        {
								//glassData.setGlassJudge(glassJudge);
					        	log.info("Warning: " + glassName + " ReuseFlag = 'N',so keep GlassJudge = 'N'! ");
					        }
							else
							{
								//Get JudgePriority
								List<Map<String, Object>> oldJudgePriority = MESProductServiceProxy.getProductServiceUtil().getEnumList("ArrayJudgePriority", glassData.getGlassJudge());
								List<Map<String, Object>> newJudgePriority = MESProductServiceProxy.getProductServiceUtil().getEnumList("ArrayJudgePriority", glassJudge);
								
								if (Boolean.parseBoolean(coverFlag.get("ArrayJudgeCoverExceptPanelJudgeN").toString()))
								{
									if (!StringUtil.equals(glassData.getGlassJudge(),"N"))
									{
										glassData.setGlassJudge(glassJudge);
									}
								}
								//Check result
								else if (oldJudgePriority.size() > 0 && newJudgePriority.size() > 0 
									&& (oldJudgePriority.get(0).get("DESCRIPTION").toString().compareTo(newJudgePriority.get(0).get("DESCRIPTION").toString()) > 0))
								{
									glassData.setGlassJudge(glassJudge);
								}
							}
							
							glassData.setSheetName(productName);
							glassData.setxAxis(glassXAxis);
							glassData.setyAxis(glassYAxis);
							
							glassData.setLastEventComment(eventInfo.getEventComment());
							glassData.setLastEventName(eventInfo.getEventName());
							glassData.setLastEventTime(eventInfo.getEventTime());
							glassData.setLastEventUser(eventInfo.getEventUser());
							glassData.setProcessOperationName(processOperationName);
							
							glassData.setLotName(lotName);							 
						
							
							ExtendedObjectProxy.getGlassJudgeService().modify(eventInfo, glassData);
							
						}
						else
						{
							if(StringUtil.equals(glassData.getReuseFlag(), "N"))
					        {
								log.info("Warning: " + glassName + " ReuseFlag = 'N',so keep GlassJudge = 'N'! ");
								//glassData.setGlassJudge(glassJudge);
					        }
							else
							{
								glassData.setGlassJudge(glassJudge);
							}							
							glassData.setSheetName(productName);
							glassData.setxAxis(glassXAxis);
							glassData.setyAxis(glassYAxis);
							
							glassData.setLastEventComment(eventInfo.getEventComment());
							glassData.setLastEventName(eventInfo.getEventName());
							glassData.setLastEventTime(eventInfo.getEventTime());
							glassData.setLastEventUser(eventInfo.getEventUser());
							glassData.setProcessOperationName(processOperationName);
							
							glassData.setLotName(lotName);						
							
							
							ExtendedObjectProxy.getGlassJudgeService().modify(eventInfo, glassData);
						}
					}
					catch (NotFoundSignal ne)
					{
						eventInfo.setEventName("Create");
						
						glassData = new GlassJudge(glassName);
						glassData.setGlassJudge(glassJudge);
						glassData.setSheetName(productName);
						glassData.setProcessFlowName(productData.getProcessFlowName());
						glassData.setProcessFlowVersion(productData.getProcessFlowVersion());
						glassData.setxAxis(glassXAxis);
						glassData.setyAxis(glassYAxis);
						
						glassData.setLastEventComment(eventInfo.getEventComment());
						glassData.setLastEventName(eventInfo.getEventName());
						glassData.setLastEventTime(eventInfo.getEventTime());
						glassData.setLastEventUser(eventInfo.getEventUser());
						glassData.setProcessOperationName(processOperationName);
						
						glassData.setLotName(lotName);						
						
						
						ExtendedObjectProxy.getGlassJudgeService().create(eventInfo, glassData);
					}
					
					//upsert Panel
					for (Element elePanel : SMessageUtil.getSubSequenceItemList(eleGlass, "PANELLIST", false))
					{
						try
						{
							String panelName = SMessageUtil.getChildText(elePanel, "PANELNAME", true);
							
							
							if (panelName.length() < 12)
							{
								continue;
							}
							
							String panelJudge = SMessageUtil.getChildText(elePanel, "PANELJUDGE", true);
							String panelXAxis = SMessageUtil.getChildText(elePanel, "XAXIS", false);
							String panelYAxis = SMessageUtil.getChildText(elePanel, "YAXIS", false);
							
							PanelJudge panelData;
							try
							{
								eventInfo.setEventName("ChangeGrade");
								
								panelData = ExtendedObjectProxy.getPanelJudgeService().selectByKey(false, new Object[] {panelName});
							
								//Get JudgePriority
								List<Map<String, Object>> oldJudgePriority = MESProductServiceProxy.getProductServiceUtil().getEnumList("ArrayJudgePriority", panelData.getPaneljudge());
								List<Map<String, Object>> newJudgePriority = MESProductServiceProxy.getProductServiceUtil().getEnumList("ArrayJudgePriority", panelJudge);
								
								
								if (!Boolean.parseBoolean(coverFlag.get("ArrayJudgeCoverAll").toString()))
								{
									if (Boolean.parseBoolean(coverFlag.get("ArrayJudgeCoverExceptPanelJudgeN").toString()))
									{
										if (!StringUtil.equals(panelData.getPaneljudge(),"N"))
										{
											panelData.setPaneljudge(panelJudge);
										}
									}
									else if (oldJudgePriority.size() > 0 && newJudgePriority.size() > 0 
											&& (oldJudgePriority.get(0).get("DESCRIPTION").toString().compareTo(newJudgePriority.get(0).get("DESCRIPTION").toString()) > 0))
										{
											panelData.setPaneljudge(panelJudge);
										}
									
									panelData.setSheetname(productName);
									panelData.setGlassname(glassName);
									panelData.setXaxis(panelXAxis);
									panelData.setYaxis(panelYAxis);
									
									panelData.setLasteventcomment(eventInfo.getEventComment());
									panelData.setLasteventname(eventInfo.getEventName());
									panelData.setLasteventtime(eventInfo.getEventTime());
									panelData.setLasteventuser(eventInfo.getEventUser());
									panelData.setProcessOperationName(processOperationName);
									
									ExtendedObjectProxy.getPanelJudgeService().modify(eventInfo, panelData);
								}
								
								else
								{
									panelData.setPaneljudge(panelJudge);
									panelData.setSheetname(productName);
									panelData.setGlassname(glassName);
									panelData.setXaxis(panelXAxis);
									panelData.setYaxis(panelYAxis);
									
									panelData.setLasteventcomment(eventInfo.getEventComment());
									panelData.setLasteventname(eventInfo.getEventName());
									panelData.setLasteventtime(eventInfo.getEventTime());
									panelData.setLasteventuser(eventInfo.getEventUser());
									panelData.setProcessOperationName(processOperationName);
									
									ExtendedObjectProxy.getPanelJudgeService().modify(eventInfo, panelData);
								}
							}
							catch (NotFoundSignal ne)
							{
								eventInfo.setEventName("Create");
								
								panelData = new PanelJudge(panelName);
								panelData.setPaneljudge(panelJudge);
								panelData.setSheetname(productName);
								panelData.setGlassname(glassName);
								panelData.setXaxis(panelXAxis);
								panelData.setYaxis(panelYAxis);
								
								panelData.setLasteventcomment(eventInfo.getEventComment());
								panelData.setLasteventname(eventInfo.getEventName());
								panelData.setLasteventtime(eventInfo.getEventTime());
								panelData.setLasteventuser(eventInfo.getEventUser());
								panelData.setProcessOperationName(processOperationName);
								
								ExtendedObjectProxy.getPanelJudgeService().create(eventInfo, panelData);
							}
						}
						catch (Exception ex)
						{
							eventLog.error(ex);
						}
					}
				}
				catch (Exception ex)
				{
					eventLog.error(ex);
				}
			}
		}
		
		return doc;
	}
}
