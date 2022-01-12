package kr.co.aim.messolution.lot.event.CNX.PostCell;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.greentrack.lot.management.info.SetEventInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.management.impl.LotQueueTimeService;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;


public class PanelHoldExcel  extends SyncHandler{
	
	public static Log logger = LogFactory.getLog(LotQueueTimeService.class);
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		Element eleBody = SMessageUtil.getBodyElement(doc);
		
		if (eleBody != null){
			
			String actionName = SMessageUtil.getBodyItemValue(doc, "EVENTNAME", true);
			//Hold Action Part
			if(actionName.toString().equals("Hold")){
				
				for (Element eleLot : SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", false)){
					
					String holdState = "Y";
					String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
					String reasonCode = SMessageUtil.getChildText(eleLot, "REASONCODE", false);
					String reasonCodeType = SMessageUtil.getChildText(eleLot, "REASONCODETYPE", false);
					String trayName = SMessageUtil.getChildText(eleLot, "TRAYNAME", false);
					String trayGroupName = SMessageUtil.getChildText(eleLot, "TRAYGROUPNAME", false);
					
					
					if(StringUtils.isEmpty(trayName.toString())){
						
									
						EventInfo eventInfo = EventInfoUtil.makeEventInfo("HoldPanelExcel", getEventUser(), getEventComment(), "", "");
						try{
							
							// Hold Panel		
							Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
							
							CommonValidation.checkLotProcessStateWait(lotData);
							
							lotData.setLotHoldState(holdState);
							eventInfo.setReasonCode(reasonCode);
							eventInfo.setReasonCodeType(reasonCodeType);
							lotData.setLastEventComment(eventInfo.getEventComment());
							lotData.setLastEventName(eventInfo.getEventName());
							lotData.setLastEventTime(TimeStampUtil.getTimestamp(TimeUtils.getCurrentTime(TimeUtils.FORMAT_DEFAULT)));
							lotData.setLastEventUser(eventInfo.getEventUser());
							SetEventInfo setEventInfo = new SetEventInfo();
							
							LotServiceProxy.getLotService().update(lotData);
							LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
							
						}catch (Exception e)
						{
							throw new CustomException();
						}
						
					}
					else{
						try{
							
							if (StringUtil.isEmpty(trayGroupName))
							{
								
								ConstantMap constantMap = GenericServiceProxy.getConstantMap();
								
								Durable trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
								
								//CommonValidation.CheckDurableHoldState(trayData);
								
								EventInfo eventInfo = EventInfoUtil.makeEventInfo("HoldPnaelExcel", getEventUser(), getEventComment(), "", "");
								
								//Hold Panel
								Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
								
								CommonValidation.checkLotProcessStateWait(lotData);
								
								lotData.setLotHoldState(holdState);
								eventInfo.setReasonCode(reasonCode);
								eventInfo.setReasonCodeType(reasonCodeType);
								lotData.setLastEventComment(eventInfo.getEventComment());
								lotData.setLastEventName(eventInfo.getEventName());
								lotData.setLastEventTime(TimeStampUtil.getTimestamp(TimeUtils.getCurrentTime(TimeUtils.FORMAT_DEFAULT)));
								lotData.setLastEventUser(eventInfo.getEventUser());
								SetEventInfo setEventInfo = new SetEventInfo();
								
								LotServiceProxy.getLotService().update(lotData);
								LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
								
								//Hold Tray
								Durable olddurableInfo = (Durable)ObjectUtil.copyTo(trayData);

								Map<String, String> durUdf = new HashMap<>();
								
								trayData.setReasonCode(reasonCode);
								trayData.setReasonCodeType(reasonCodeType);
								durUdf.put("DURABLEHOLDSTATE", constantMap.DURABLE_HOLDSTATE_Y);

								trayData.setUdfs(durUdf);
								trayData.setLastEventName("HoldTrayExcel");
								trayData.setLastEventTimeKey(eventInfo.getEventTimeKey());
								trayData.setLastEventTime(eventInfo.getEventTime());
								trayData.setLastEventUser(eventInfo.getEventUser());
								trayData.setLastEventComment("HoldTrayExcel");

								DurableHistory durHistory = new DurableHistory();
								durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfo, trayData, durHistory);

								DurableServiceProxy.getDurableService().update(trayData);
								DurableServiceProxy.getDurableHistoryService().insert(durHistory);		
							}else{
								
								ConstantMap constantMap = GenericServiceProxy.getConstantMap();
								
								Durable trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
								Durable trayGroupData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayGroupName);
								
								//CommonValidation.CheckDurableHoldState(trayData);
								//CommonValidation.CheckDurableHoldState(trayGroupData);
								
								EventInfo eventInfo = EventInfoUtil.makeEventInfo("HoldPanelExcel", getEventUser(), getEventComment(), "", "");
								EventInfo eventInfoTray = EventInfoUtil.makeEventInfo("HoldTrayExcel", getEventUser(), getEventComment(), "", "");
								EventInfo eventInfoTrayGroup = EventInfoUtil.makeEventInfo("HoldTrayGroupExcel", getEventUser(), getEventComment(), "", "");
								
								// Hold Panel
								Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
								
								CommonValidation.checkLotProcessStateWait(lotData);
								
								lotData.setLotHoldState(holdState);
								eventInfo.setReasonCode(reasonCode);
								eventInfo.setReasonCodeType(reasonCodeType);
								lotData.setLastEventComment(eventInfo.getEventComment());
								lotData.setLastEventName(eventInfo.getEventName());
								lotData.setLastEventTime(TimeStampUtil.getTimestamp(TimeUtils.getCurrentTime(TimeUtils.FORMAT_DEFAULT)));
								lotData.setLastEventUser(eventInfo.getEventUser());
								SetEventInfo setEventInfo = new SetEventInfo();
								
								LotServiceProxy.getLotService().update(lotData);
								LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
								
								
								//Hold Tray
								Durable olddurableInfo = (Durable)ObjectUtil.copyTo(trayData);

								Map<String, String> durUdf = new HashMap<>();
								
								trayData.setReasonCode(reasonCode);
								trayData.setReasonCodeType(reasonCodeType);
								durUdf.put("DURABLEHOLDSTATE", constantMap.DURABLE_HOLDSTATE_Y);

								trayData.setUdfs(durUdf);
								trayData.setLastEventName("HoldTrayExcel");
								trayData.setLastEventTimeKey(eventInfo.getEventTimeKey());
								trayData.setLastEventTime(eventInfo.getEventTime());
								trayData.setLastEventUser(eventInfo.getEventUser());
								trayData.setLastEventComment("HoldTrayExcel");

								DurableHistory durHistory = new DurableHistory();
								durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfo, trayData, durHistory);

								DurableServiceProxy.getDurableService().update(trayData);
								DurableServiceProxy.getDurableHistoryService().insert(durHistory);
								
								
								//Hold TrayGroup
								Durable oldCoverdurableInfo = (Durable)ObjectUtil.copyTo(trayGroupData);

								Map<String, String> durcoverUdf = new HashMap<>();
								
								trayGroupData.setReasonCode(reasonCode);
								trayGroupData.setReasonCodeType(reasonCodeType);
								durcoverUdf.put("DURABLEHOLDSTATE", constantMap.DURABLE_HOLDSTATE_Y);

								trayGroupData.setUdfs(durcoverUdf);
								trayGroupData.setLastEventName("HoldTrayGroupExcel");
								trayGroupData.setLastEventTimeKey(eventInfo.getEventTimeKey());
								trayGroupData.setLastEventTime(eventInfo.getEventTime());
								trayGroupData.setLastEventUser(eventInfo.getEventUser());
								trayGroupData.setLastEventComment("HoldTrayGroupExcel");

								DurableHistory durCoverHistory = new DurableHistory();
								durCoverHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldCoverdurableInfo, trayGroupData, durCoverHistory);

								DurableServiceProxy.getDurableService().update(trayGroupData);
								DurableServiceProxy.getDurableHistoryService().insert(durCoverHistory);

							}
						
						}catch (Exception e){
							
							throw new CustomException();
						}
					}	
				}
			}else if(actionName.toString().equals("Release")){
				

				
				for (Element eleLot : SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", false)){
					
					String holdState = "N";
					String lotName = SMessageUtil.getChildText(eleLot, "LOTNAME", true);
					String trayName = SMessageUtil.getChildText(eleLot, "TRAYNAME", false);
					String trayGroupName = SMessageUtil.getChildText(eleLot, "TRAYGROUPNAME", false);
					
					
					if(StringUtils.isEmpty(trayName.toString())){
						
									
						EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReleaseHoldPanelExcel", getEventUser(), getEventComment(), "", "");
						try{
							
							// Release Panel		
							Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
							
							CommonValidation.checkLotProcessStateWait(lotData);
							
							lotData.setLotHoldState(holdState);
							lotData.setLastEventComment(eventInfo.getEventComment());
							lotData.setLastEventName(eventInfo.getEventName());
							lotData.setLastEventTime(TimeStampUtil.getTimestamp(TimeUtils.getCurrentTime(TimeUtils.FORMAT_DEFAULT)));
							lotData.setLastEventUser(eventInfo.getEventUser());
							SetEventInfo setEventInfo = new SetEventInfo();
							
							LotServiceProxy.getLotService().update(lotData);
							LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
							
						}catch (Exception e)
						{
							throw new CustomException();
						}
						
					}
					else{
						try{
							
							if (StringUtil.isEmpty(trayGroupName))
							{
								
								ConstantMap constantMap = GenericServiceProxy.getConstantMap();
								
								Durable trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
								
								//CommonValidation.CheckDurableHoldState(trayData);
								
								EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReleaseHoldPnaelExcel", getEventUser(), getEventComment(), "", "");
								
								//Release Panel
								Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
								
								CommonValidation.checkLotProcessStateWait(lotData);
								
								lotData.setLotHoldState(holdState);
								lotData.setLastEventComment(eventInfo.getEventComment());
								lotData.setLastEventName(eventInfo.getEventName());
								lotData.setLastEventTime(TimeStampUtil.getTimestamp(TimeUtils.getCurrentTime(TimeUtils.FORMAT_DEFAULT)));
								lotData.setLastEventUser(eventInfo.getEventUser());
								SetEventInfo setEventInfo = new SetEventInfo();
								
								LotServiceProxy.getLotService().update(lotData);
								LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
								
								//Release Tray
								Durable olddurableInfo = (Durable)ObjectUtil.copyTo(trayData);

								Map<String, String> durUdf = new HashMap<>();								

								durUdf.put("DURABLEHOLDSTATE", constantMap.DURABLE_HOLDSTATE_N);

								trayData.setUdfs(durUdf);
								trayData.setLastEventName("ReleaseHoldTrayExcel");
								trayData.setLastEventTimeKey(eventInfo.getEventTimeKey());
								trayData.setLastEventTime(eventInfo.getEventTime());
								trayData.setLastEventUser(eventInfo.getEventUser());
								trayData.setLastEventComment("ReleaseHoldTrayExcel");

								DurableHistory durHistory = new DurableHistory();
								durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfo, trayData, durHistory);

								DurableServiceProxy.getDurableService().update(trayData);
								DurableServiceProxy.getDurableHistoryService().insert(durHistory);		
							}else{
								
								ConstantMap constantMap = GenericServiceProxy.getConstantMap();
								
								Durable trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
								Durable trayGroupData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayGroupName);
								
								//CommonValidation.CheckDurableHoldState(trayData);
								//CommonValidation.CheckDurableHoldState(trayGroupData);
								
								EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReleaseHoldPnaelExcel", getEventUser(), getEventComment(), "", "");

								// Release Panel
								Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);
								
								CommonValidation.checkLotProcessStateWait(lotData);
								
								lotData.setLotHoldState(holdState);
								lotData.setLastEventComment(eventInfo.getEventComment());
								lotData.setLastEventName(eventInfo.getEventName());
								lotData.setLastEventTime(TimeStampUtil.getTimestamp(TimeUtils.getCurrentTime(TimeUtils.FORMAT_DEFAULT)));
								lotData.setLastEventUser(eventInfo.getEventUser());
								SetEventInfo setEventInfo = new SetEventInfo();
								
								LotServiceProxy.getLotService().update(lotData);
								LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);
								
								
								//Release Tray
								Durable olddurableInfo = (Durable)ObjectUtil.copyTo(trayData);

								Map<String, String> durUdf = new HashMap<>();
								String sql = "SELECT * FROM LOT WHERE CARRIERNAME = :CARRIERNAME AND LOTHOLDSTATE ='Y' ";

								Map<String, String> args = new HashMap<String, String>();
								args.put("CARRIERNAME", trayName);
							
								List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);

								if(result.size()==0){

									durUdf.put("DURABLEHOLDSTATE", constantMap.DURABLE_HOLDSTATE_N);

									trayData.setUdfs(durUdf);
									trayData.setLastEventName("ReleaseHoldTrayExcel");
									trayData.setLastEventTimeKey(eventInfo.getEventTimeKey());
									trayData.setLastEventTime(eventInfo.getEventTime());
									trayData.setLastEventUser(eventInfo.getEventUser());
									trayData.setLastEventComment("ReleaseHoldTrayExcel");

									DurableHistory durHistory = new DurableHistory();
									durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfo, trayData, durHistory);

									DurableServiceProxy.getDurableService().update(trayData);
									DurableServiceProxy.getDurableHistoryService().insert(durHistory);
								}
								
								
								//Release TrayGroup
								Durable oldCoverdurableInfo = (Durable)ObjectUtil.copyTo(trayGroupData);

								Map<String, String> durcoverUdf = new HashMap<>();
								String sql1 = "SELECT * FROM DURABLE A   WHERE COVERNAME =:COVERNAME  AND DURABLETYPE ='Tray' AND DURABLEHOLDSTATE ='Y' ";

								Map<String, String> args1 = new HashMap<String, String>();
								args1.put("COVERNAME", trayGroupName);
							
								List<Map<String, Object>> result1 = GenericServiceProxy.getSqlMesTemplate().queryForList(sql1, args1);

								if(result1.size()==0){
								
									durcoverUdf.put("DURABLEHOLDSTATE", constantMap.DURABLE_HOLDSTATE_N);

									trayGroupData.setUdfs(durcoverUdf);
									trayGroupData.setLastEventName("ReleaseHoldTrayGroupExcel");
									trayGroupData.setLastEventTimeKey(eventInfo.getEventTimeKey());
									trayGroupData.setLastEventTime(eventInfo.getEventTime());
									trayGroupData.setLastEventUser(eventInfo.getEventUser());
									trayGroupData.setLastEventComment("ReleaseHoldTrayGroupExcel");

									DurableHistory durCoverHistory = new DurableHistory();
									durCoverHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldCoverdurableInfo, trayGroupData, durCoverHistory);

									DurableServiceProxy.getDurableService().update(trayGroupData);
									DurableServiceProxy.getDurableHistoryService().insert(durCoverHistory);
								}
							}
						
						}catch (Exception e){
							
							throw new CustomException();
						}
					}	
				}
			}
		}
	return doc;
	}
}
