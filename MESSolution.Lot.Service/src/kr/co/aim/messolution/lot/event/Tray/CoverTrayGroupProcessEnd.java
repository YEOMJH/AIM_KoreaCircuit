package kr.co.aim.messolution.lot.event.Tray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotFutureAction;
import kr.co.aim.messolution.extended.object.management.data.RiskLot;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EnumInfoUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.generic.util.XmlUtil;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

public class CoverTrayGroupProcessEnd extends SyncHandler {
	private static Log log = LogFactory.getLog(CoverTrayGroupProcessEnd.class);

	@Override
	public Document doWorks(Document doc) throws CustomException
	{
		try
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "CoverTrayGroupProcessEndReply");
			
			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
			String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
			String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
			String coverTrayName = SMessageUtil.getBodyItemValue(doc, "COVERTRAYNAME", true);
			String operationMode = SMessageUtil.getBodyItemValue(doc, "OPERATIONMODE", true);
			String BCRFlag = SMessageUtil.getBodyItemValue(doc, "BCRFLAG", true);
			List<Element> trayElementList = SMessageUtil.getBodySequenceItemList(doc, "TRAYLIST", false);
			
			Element coverTrayElement = this.removeCoverTrayElement(trayElementList, coverTrayName);
			
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
			Durable coverTrayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableDataForUpdate(coverTrayName);

			CommonValidation.checkMachineHold(machineData);
			CommonValidation.CheckDurableState(coverTrayData);
			CommonValidation.CheckDurableHoldState(coverTrayData);
			
			List<Durable> updateDurableList = new ArrayList<>();
			List<DurableHistory> updateDurableHistList = new ArrayList<>();

			if (portType.equals(portData.getUdfs().get("PORTTYPE")) && StringUtil.in(portType, "PU", "BU"))
			{
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignTrayGroup", this.getEventUser(), this.getEventComment());
				
				if(EnumInfoUtil.SorterOperationCondition.CTRAY.getOperationMode().equals(operationMode) && StringUtil.equals(portName, "P10"))
				{
					log.info("C-TRAY Fail");
					
					// init cover tray
					coverTrayData.setDurableType("CoverTray");
					coverTrayData.setLotQuantity(0);
					coverTrayData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
					DurableServiceProxy.getDurableService().update(coverTrayData);

					SetEventInfo setEventInfo = new SetEventInfo();
					setEventInfo.getUdfs().put("COVERNAME", coverTrayData.getKey().getDurableName());
					setEventInfo.getUdfs().put("POSITION", "1");
					setEventInfo.getUdfs().put("MACHINENAME", "");
					setEventInfo.getUdfs().put("PORTNAME", "");
					setEventInfo.getUdfs().put("BCRFLAG", BCRFlag);
					setEventInfo.getUdfs().put("DURABLETYPE1", "CoverTray");
					
					if(EnumInfoUtil.SorterOperationCondition.CTRAY.getOperationMode().equals(operationMode))
					{
						setEventInfo.getUdfs().put("NGFLAG", "Y");
					}

					coverTrayData = DurableServiceProxy.getDurableService().setEvent(coverTrayData.getKey(), eventInfo, setEventInfo);
					
					setResultItemValue(doc, "OK", "");
					return doc;
				}
				else
				{
					int totalLotQty = 0;
					int maxPosition = 1;
					
					// ENB1 Case
					if (trayElementList == null || trayElementList.size() == 0)
					{
						if (coverTrayElement != null)
						{
							String position = coverTrayElement.getChildText("POSITION");
							String productQty = coverTrayElement.getChildText("PRODUCTQUANTITY");

							totalLotQty = Integer.parseInt(productQty);
							maxPosition = Integer.parseInt(position);
						}
						else
						{
							//TRAY-0016: No TrayList information received from BC.
							throw new CustomException("TRAY-0016");
						}
					}
					else
					{
						// ENB1xNB1,BN1 Case
						Map<String, Durable> durableDataMap = this.generateDurableDataMap(trayElementList, true);

						for (Element trayElement : trayElementList)
						{
							String trayName = trayElement.getChildText("TRAYNAME");
							String position = trayElement.getChildText("POSITION");
							String productQty = trayElement.getChildText("PRODUCTQUANTITY");

							Durable trayData = durableDataMap.get(trayName);

							if (StringUtils.equals(trayData.getDurableType(), "CoverTray"))
							{
								//TRAY-0017: The tray group [{0}] contains multiple cover trays.[IssueTray:{1}]
								throw new CustomException("TRAY-0017", coverTrayName, trayData.getKey().getDurableName());
							}

							if (portType.equals("PU") && !EnumInfoUtil.SorterOperationCondition.CTRAY.getOperationMode().equals(operationMode))
							{
								// check is not empty tray
								if (!("Available".equals(trayData.getDurableState()) && CommonValidation.checkTrayIsEmpty(trayName)))
									totalLotQty += Integer.parseInt(productQty);

								if (Integer.parseInt(position) > maxPosition)
									maxPosition = Integer.parseInt(position) + 1;
							}
							else
							{
								// BU or PU(C-TRAY Mode) Port tray must be empty
								CommonValidation.checkTrayIsEmpty(trayData);
							}

							Durable oldTrayData = (Durable) ObjectUtil.copyTo(trayData);

							// assign tray group
							trayData.getUdfs().put("COVERNAME", coverTrayName);
							trayData.getUdfs().put("POSITION", position);
							trayData.getUdfs().put("MACHINENAME", "");
							trayData.getUdfs().put("PORTNAME", "");
							trayData.getUdfs().put("DURABLETYPE1", "Tray");
							trayData.setLastEventName(eventInfo.getEventName());
							trayData.setLastEventTimeKey(eventInfo.getEventTimeKey());
							trayData.setLastEventTime(eventInfo.getEventTime());
							trayData.setLastEventUser(eventInfo.getEventUser());
							trayData.setLastEventComment(eventInfo.getEventComment());

							DurableHistory durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldTrayData, trayData, new DurableHistory());

							updateDurableList.add(trayData);
							updateDurableHistList.add(durHistory);
						}
						
						if(EnumInfoUtil.SorterOperationCondition.CTRAY.getOperationMode().equals(operationMode) && StringUtil.equals(portType, "PU"))
						{
							maxPosition = trayElementList.size() + 1;
						}

						if (updateDurableList.size() > 0)
						{
							try
							{
								CommonUtil.executeBatch("update", updateDurableList, true);
								CommonUtil.executeBatch("insert", updateDurableHistList, true);

								log.info(String.format("▶Successfully update %s pieces of trays.", updateDurableList.size()));
							}
							catch (Exception e)
							{
								log.error(e.getMessage());
								throw new CustomException(e.getCause());
							}
						}
					}

					// init cover tray
					coverTrayData.setDurableType("CoverTray");
					coverTrayData.setLotQuantity(totalLotQty);
					coverTrayData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
					DurableServiceProxy.getDurableService().update(coverTrayData);

					SetEventInfo setEventInfo = new SetEventInfo();
					setEventInfo.getUdfs().put("COVERNAME", coverTrayData.getKey().getDurableName());
					setEventInfo.getUdfs().put("POSITION", String.valueOf(maxPosition));
					setEventInfo.getUdfs().put("MACHINENAME", "");
					setEventInfo.getUdfs().put("PORTNAME", "");
					setEventInfo.getUdfs().put("BCRFLAG", BCRFlag);
					setEventInfo.getUdfs().put("DURABLETYPE1", "CoverTray");
					
					if(EnumInfoUtil.SorterOperationCondition.CTRAY.getOperationMode().equals(operationMode) &&  "NG".equals(portUseType))
					{
						setEventInfo.getUdfs().put("NGFLAG", "Y");
					}

					coverTrayData = DurableServiceProxy.getDurableService().setEvent(coverTrayData.getKey(), eventInfo, setEventInfo);
					
					// sub tray List + cover tray
					updateDurableList.add(coverTrayData);
					
					//Send To SAP
					/*
					if(EnumInfoUtil.SorterOperationCondition.TSHIP.getOperationMode().equals(operationMode))
					{
						String sql = " SELECT LOTNAME FROM LOT WHERE CARRIERNAME=:CARRIERNAME ";
						
						Map<String, String> bindMap = new HashMap<String, String>();
						bindMap.put("CARRIERNAME", coverTrayName);
						
						List<Map<String, Object>> sqlResult =GenericServiceProxy.getSqlMesTemplate().queryForList(sql, bindMap);
						if(sqlResult!=null&&sqlResult.size()>0)
						{
							Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(sqlResult.get(0).get("LOTNAME").toString());
							ProductRequest workOrderData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lot.getProductRequestName());
							try
							{
	 							String sapFlag=CommonUtil.getEnumDefValueStringByEnumName("SAPFLAG");
								if(StringUtils.isNotEmpty(sapFlag)&&StringUtils.equals(sapFlag, "Y")&&
										StringUtils.isNotEmpty(workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
								{							
									List<Map<String, Object>> panelList=new ArrayList<>();
									SuperProductRequest superWO = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{workOrderData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});								
									MESConsumableServiceProxy.getConsumableServiceUtil().trackOutERPBOMReportForTrayGroup(eventInfo, lot, superWO,machineName,totalLotQty, panelList);
								}
							}
							catch(Exception x)
							{
								eventLog.info("SAP Report Error");
							}
						}
					}*/
				}
			}
			else
			{
				log.info("Skip logic : Port [" + portName + "], PortType [" + CommonUtil.getValue(portData.getUdfs(), "PORTTYPE") + "] ");
				
				setResultItemValue(doc, "OK", "");
				return doc;
			}

			if (portType.equals("PU") && !EnumInfoUtil.SorterOperationCondition.CTRAY.getOperationMode().equals(operationMode))
			{
				// updateDurableList is coverTray + subTray  List
				List<Lot> panelDataList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayList(updateDurableList, true);
				List<String> panelNameList = CommonUtil.makeToStringList(panelDataList);
				
				// hold tray group by case . 1.Read panelName missmatch 2. hold tray in traygroup 3. multi spec 4. contains emptyTray
				this.holdTrayGroup(panelNameList, updateDurableList,coverTrayName,portName,portType,operationMode,BCRFlag);

				if (panelNameList.size() > 0)
				{
					EventInfo dEventInfo = EventInfoUtil.makeEventInfo("ClearPickFlag", this.getEventUser(), this.getEventComment());
					
					// remove operationflag
					if ( EnumInfoUtil.SorterOperationCondition.TSHIP.getOperationMode().equals(operationMode))
					{
						List<RiskLot> sorterPickLotList = ExtendedObjectProxy.getRiskLotService().getDataInfoListByLotNameList(panelNameList);
						
						if(sorterPickLotList != null && sorterPickLotList.size()>0)
						{
							for (RiskLot riskLot : sorterPickLotList) 
							{
								riskLot.setPickFlag("N");
								riskLot.setLastEventUser(dEventInfo.getEventUser());
								riskLot.setLastEventComment(dEventInfo.getEventComment());
								riskLot.setLastEventName(dEventInfo.getEventName());
								riskLot.setLastEventTime(dEventInfo.getEventTime());
								riskLot.setLastEventTimeKey(dEventInfo.getEventTimeKey());
								ExtendedObjectProxy.getRiskLotService().modify(dEventInfo, riskLot);
							}
						}

						//ExtendedObjectProxy.getSorterPickPrintInfoService().remove(dEventInfo,panelNameList);
					}

					// referesh panel data
					panelDataList = MESLotServiceProxy.getLotServiceUtil().getLotDataListByPanelNameList(panelNameList, false);

					// CheckReserveHold
					EventInfo futureActionEventInfo = EventInfoUtil.makeEventInfo("Delete", getEventUser(), getEventComment());
					boolean reserveFlag = PostCellDeleteLotFutureAction(futureActionEventInfo, panelDataList);

					if (reserveFlag)
					{
						EventInfo eventInfoHold = EventInfoUtil.makeEventInfo("ReserveHold", getEventUser(), getEventComment());
						MESLotServiceProxy.getLotServiceUtil().PanelHoldByLotList(eventInfoHold, panelDataList);
					}
				}
			}
			
			setResultItemValue(doc, "OK", "");
		}
		catch (CustomException ce)
		{
			setResultItemValue(doc, "NG", ce.errorDef.getLoc_errorMessage());
			throw ce;
		}
		catch (Exception e)
		{
			setResultItemValue(doc, "NG", e.getMessage());
			throw new CustomException(e.getCause());
		}
		return doc;
	}

	private Document setResultItemValue(Document doc, String result, String resultDescription) throws CustomException
	{
		Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
		Element trayListElement = XmlUtil.getChild(bodyElement,"TRAYLIST", false);
		
		trayListElement.detach();
		
		bodyElement.addContent(new Element("RESULT").setText(result));
		bodyElement.addContent(new Element("RESULTDESCRIPTION").setText(resultDescription));

		return doc;
	}

	public Element removeCoverTrayElement(List<Element> trayElementList, String coverTrayName) throws CustomException
	{
		Element coverTrayElement = null;

		for (Element trayElement : new ArrayList<>(trayElementList))
		{
			if (trayElement.getChildText("TRAYNAME").equals(coverTrayName))
			{
				coverTrayElement = (Element) trayElement.clone();
				trayElementList.remove(trayElement);
			}
		}

		if (coverTrayElement == null)
			log.info("Important content: There is no CoverTray information in the TrayList reported by BC .");

		return coverTrayElement;
	}
	
	public Map<String, Element> makeTrayElementMap(List<Element> trayElementList)
	{
		Map<String, Element> trayElementMap = new HashMap<>();

		for (Element trayEleemnt : trayElementList)
			trayElementMap.put(trayEleemnt.getChildText("TRAYNAME"), trayEleemnt);

		return trayElementMap;
	}

	public Map<String, Durable> generateDurableDataMap(List<Element> trayElementList, boolean forUpdate) throws CustomException
	{
		List<Durable> durableList = MESDurableServiceProxy.getDurableServiceUtil().getTrayListByTrayNameList(CommonUtil.makeList(trayElementList, "TRAYNAME"), forUpdate);

		Map<String, Durable> durableDataMap = new HashMap<>(trayElementList.size());

		for (Durable durableData : durableList)
		{
			durableDataMap.put(durableData.getKey().getDurableName(), durableData);
		}

		return durableDataMap;
	}

	public boolean PostCellDeleteLotFutureAction(EventInfo eventInfo, List<Lot> lotList) throws CustomException
	{
		List<LotFutureAction> futureActionList = ExtendedObjectProxy.getLotFutureActionService().getLotFutureActionDataWithLotList(lotList);

		if (futureActionList != null)
		{
			ExtendedObjectProxy.getLotFutureActionService().deleteLotFutureActionWithLotList(eventInfo, futureActionList);

			return true;
		}
		else
		{
			return false;
		}
	}

	private void holdTrayGroup(List<String> panelNameList, List<Durable> trayDataList,String coverTrayName,String portName,String portType,String operationMode,String BCRFlag) throws CustomException
	{
		try
		{
			String reasonCode = "HD100";
			String reasonCodeType = "HOLD";
			boolean holdTrayGroup = false;
			
			ConstantMap constantMap = GenericServiceProxy.getConstantMap();
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Hold", this.getEventUser(), this.getEventComment());
			
			if (panelNameList.size() > 0)
			{
				StringBuilder query = new StringBuilder();
				query.append("SELECT * ");
				query.append(" FROM LOT ");
				query.append(" WHERE LOTNAME IN (:LOTLIST) ");
				query.append(" AND VCRPRODUCTNAME IS NOT NULL ");
				query.append(" AND LOTNAME != VCRPRODUCTNAME ");
				
				
				StringBuilder querySort = new StringBuilder();
				querySort.append("SELECT * ");
				querySort.append(" FROM LOT ");
				querySort.append(" WHERE LOTNAME IN (:LOTLIST) ");
				querySort.append(" AND PROCESSOPERATIONNAME='34000'");
				querySort.append(" AND BEFOREOPERATIONNAME='3S001'");
				
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("LOTLIST", panelNameList);

				List<Map<String, Object>> resultList = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(query.toString(), bindMap);
				List<Map<String, Object>> resultSortList = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(querySort.toString(), bindMap);
                if( resultSortList.size()>0)
                {//2021/1/31 caixu 2021/1/31 Add Sort DoePanel
                	if(portName.equals("P06")||portName.equals("P07")||portName.equals("P08")||portName.equals("P09")||portName.equals("P10") )
                	{
                		// BCR Read missmatch
    					eventInfo.setEventComment("Doe Panel");
    					eventInfo.setReasonCode(reasonCode);
    					eventInfo.setReasonCodeType(reasonCodeType);
    					eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
    					eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

    					List<Lot> updateLotList = new ArrayList<>(panelNameList.size());
    					List<LotHistory> updateHistList = new ArrayList<>(panelNameList.size());

    					List<Lot> lotDataList = LotServiceProxy.getLotService().transform(resultSortList);

    					for (Lot lotData : lotDataList)
    					{
    						Lot oldLot = (Lot) ObjectUtil.copyTo(lotData);

    						lotData.setLotHoldState(constantMap.Flag_Y);
    						lotData.setReasonCode(reasonCode);
    						lotData.setReasonCodeType(reasonCodeType);
    						lotData.setLastEventName(eventInfo.getEventName());
    						lotData.setLastEventTime(eventInfo.getEventTime());
    						lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
    						lotData.setLastEventUser(eventInfo.getEventUser());
    						lotData.setLastEventComment(eventInfo.getEventComment());

    						LotHistory lotHist = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, new LotHistory());

    						updateLotList.add(lotData);
    						updateHistList.add(lotHist);
    					}

    					try
    					{
    						holdTrayGroup = true;
    						CommonUtil.executeBatch("update", updateLotList, true);
    						CommonUtil.executeBatch("insert", updateHistList, true);

    						log.info(String.format("?Successfully hold %s pieces of panels.", updateLotList.size()));
    					}
    					catch (Exception e)
    					{
    						log.error(e.getMessage());
    						throw new CustomException(e.getCause());
    					}
    				}
                	
                }
				if (resultList.size() > 0&&!holdTrayGroup)
				{
					// BCR Read missmatch
					eventInfo.setEventComment(String.format("Hold TrayGroup by Mismatched Panel ID.[CoverTray=%s,Issue Panel Qunatity=%s]", coverTrayName, resultList.size()));
					eventInfo.setReasonCode(reasonCode);
					eventInfo.setReasonCodeType(reasonCodeType);
					eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

					List<Lot> updateLotList = new ArrayList<>(panelNameList.size());
					List<LotHistory> updateHistList = new ArrayList<>(panelNameList.size());

					List<Lot> lotDataList = LotServiceProxy.getLotService().transform(resultList);

					for (Lot lotData : lotDataList)
					{
						Lot oldLot = (Lot) ObjectUtil.copyTo(lotData);

						lotData.setLotHoldState(constantMap.Flag_Y);
						lotData.setReasonCode(reasonCode);
						lotData.setReasonCodeType(reasonCodeType);
						lotData.setLastEventName(eventInfo.getEventName());
						lotData.setLastEventTime(eventInfo.getEventTime());
						lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
						lotData.setLastEventUser(eventInfo.getEventUser());
						lotData.setLastEventComment(eventInfo.getEventComment());

						LotHistory lotHist = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, new LotHistory());

						updateLotList.add(lotData);
						updateHistList.add(lotHist);
					}

					try
					{
						holdTrayGroup = true;
						CommonUtil.executeBatch("update", updateLotList, true);
						CommonUtil.executeBatch("insert", updateHistList, true);

						log.info(String.format("▶Successfully hold %s pieces of panels.", updateLotList.size()));
					}
					catch (Exception e)
					{
						log.error(e.getMessage());
						throw new CustomException(e.getCause());
					}
				}
			}
			//T-SHIP MODE & P05 Is Sorter Pick Mode ,Hold Panel
			if(!holdTrayGroup)
			{
				if(EnumInfoUtil.SorterOperationCondition.TSHIP.getOperationMode().equals(operationMode))
				{
					try
					{
						StringBuilder sql = new StringBuilder();
						sql.append("SELECT A.* ");
						sql.append(" FROM LOT A,CT_RISKLOT B");
						sql.append(" WHERE B.LOTNAME IN (:LOTLIST) ");
						sql.append(" AND A.LOTNAME = B.LOTNAME");
						sql.append(" AND A.BEFOREOPERATIONNAME='3S004'");
						sql.append(" AND B.PICKFLAG='Y'");
						
						Map<String, Object> bindMap = new HashMap<String, Object>();
						bindMap.put("LOTLIST", panelNameList);
						
						List<Map<String, Object>> resultList = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), bindMap);
						
						if (resultList != null&&resultList.size() > 0)
						{
							if(resultList.size()!=panelNameList.size())
							{
								eventInfo.setEventComment(String.format("Mode=TSHIP Port=P05 Operation=3S004,Hold Picked Panel .[CoverTray=%s,Issue Panel Qunatity=%s]",coverTrayName, resultList.size()));
								eventInfo.setReasonCode(reasonCode);
								eventInfo.setReasonCodeType(reasonCodeType);
								eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
								eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
								
								List<Lot> updateLotList = new ArrayList<>(panelNameList.size());
								List<LotHistory> updateHistList = new ArrayList<>(panelNameList.size());

								List<Lot> lotDataList = LotServiceProxy.getLotService().transform(resultList);
								
								for (Lot lotData : lotDataList)
								{
									Lot oldLot = (Lot) ObjectUtil.copyTo(lotData);

									lotData.setLotHoldState(constantMap.Flag_Y);
									lotData.setReasonCode(reasonCode);
									lotData.setReasonCodeType(reasonCodeType);
									lotData.setLastEventName(eventInfo.getEventName());
									lotData.setLastEventTime(eventInfo.getEventTime());
									lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
									lotData.setLastEventUser(eventInfo.getEventUser());
									lotData.setLastEventComment(eventInfo.getEventComment());

									LotHistory lotHist = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, new LotHistory());

									updateLotList.add(lotData);
									updateHistList.add(lotHist);	
								}
								try
								{
									holdTrayGroup = true;
									CommonUtil.executeBatch("update", updateLotList, true);
									CommonUtil.executeBatch("insert", updateHistList, true);

									log.info(String.format("?Successfully hold %s pieces of panels.", updateLotList.size()));
								}
								catch (Exception e)
								{
									log.error(e.getMessage());
									throw new CustomException(e.getCause());
								}
							}						
						}

					}
					catch (Exception e)
					{
						log.info("Error Occurred - Hold TrayGroup by Picked Panel");
					}
				}
			}
			//Hold issus panel qty != Commen panel qty  20210907 jinlj 
			if(!holdTrayGroup)
			{
				try
				{
					StringBuilder sql = new StringBuilder();
					sql.append("SELECT * ");
					sql.append(" FROM LOT ");
					sql.append(" WHERE LOTNAME IN (:LOTLIST) ");
					sql.append(" AND LOTISSUESTATE = 'Y' ");
					
					Map<String, Object> bindMap = new HashMap<String, Object>();
					bindMap.put("LOTLIST", panelNameList);
					
					List<Map<String, Object>> resultList = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), bindMap);
					
					if (resultList != null&&resultList.size() > 0)
					{
						if(resultList.size()!=panelNameList.size())
						{
							eventInfo.setEventComment(String.format("Exist Issue Panel .[CoverTray=%s,Issue Panel Qunatity=%s]",coverTrayName, resultList.size()));
							eventInfo.setReasonCode(reasonCode);
							eventInfo.setReasonCodeType(reasonCodeType);
							eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
							eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
							
							List<Lot> updateLotList = new ArrayList<>(panelNameList.size());
							List<LotHistory> updateHistList = new ArrayList<>(panelNameList.size());

							List<Lot> lotDataList = LotServiceProxy.getLotService().transform(resultList);
							
							for (Lot lotData : lotDataList)
							{
								Lot oldLot = (Lot) ObjectUtil.copyTo(lotData);

								lotData.setLotHoldState(constantMap.Flag_Y);
								lotData.setReasonCode(reasonCode);
								lotData.setReasonCodeType(reasonCodeType);
								lotData.setLastEventName(eventInfo.getEventName());
								lotData.setLastEventTime(eventInfo.getEventTime());
								lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
								lotData.setLastEventUser(eventInfo.getEventUser());
								lotData.setLastEventComment(eventInfo.getEventComment());

								LotHistory lotHist = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, new LotHistory());

								updateLotList.add(lotData);
								updateHistList.add(lotHist);	
							}
							try
							{
								holdTrayGroup = true;
								CommonUtil.executeBatch("update", updateLotList, true);
								CommonUtil.executeBatch("insert", updateHistList, true);

								log.info(String.format("?Successfully hold %s pieces of panels.", updateLotList.size()));
							}
							catch (Exception e)
							{
								log.error(e.getMessage());
								throw new CustomException(e.getCause());
							}
						}						
					}

				}
				catch (Exception e)
				{
					log.info("Error Occurred - Hold TrayGroup by Mismatched VCR ID");
				}
				
			}
			if (!holdTrayGroup)
			{
				// tray group contain hold tray
				for (Durable trayData : trayDataList)
				{
					if ("Y".equals(trayData.getUdfs().get("DURABLEHOLDSTATE")))
					{
						holdTrayGroup = true;
						
						eventInfo.setEventComment(String.format("Hold tray [%s] in the TrayGroup[%s].",trayData.getKey().getDurableName(), coverTrayName));
						eventInfo.setReasonCode(reasonCode);
						eventInfo.setReasonCodeType(reasonCodeType);
						eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
						eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
						
						break;
					}
				}
			}
			
			if(!holdTrayGroup)
			{
				String sql =" SELECT DISTINCT PRODUCTREQUESTNAME , PRODUCTSPECNAME , PRODUCTSPECVERSION ,"
						  + " PROCESSFLOWNAME, PROCESSFLOWVERSION, PROCESSOPERATIONNAME,PROCESSOPERATIONVERSION "
						  + " FROM LOT WHERE CARRIERNAME IN (:TRAYLIST)  ";
				
				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("TRAYLIST", CommonUtil.makeToStringList(trayDataList));
				
				List<Map<String,Object>> resultList =null;
				
				try
				{
				   resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(sql, bindMap);
				}
				catch (Exception ex)
				{
					throw new CustomException(ex.getCause());
				}
				
				if(resultList!=null&& resultList.size()>1)
				{
					holdTrayGroup = true;

					eventInfo.setEventComment(String.format("Multiple specifications of panels in the TrayGroup[%s].", coverTrayName));
					eventInfo.setReasonCode(reasonCode);
					eventInfo.setReasonCodeType(reasonCodeType);
					eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
				}
			}
			
			if (holdTrayGroup)
			{
				List<Durable> updateDurableList = new ArrayList<>();
				List<DurableHistory> updateDurableHistList = new ArrayList<>();

				for (Durable durableData : trayDataList)
				{
					Durable oldDurbale = (Durable) ObjectUtil.copyTo(durableData);

					durableData.setReasonCode(reasonCode);
					durableData.setReasonCodeType(reasonCodeType);
					durableData.getUdfs().put("DURABLEHOLDSTATE", constantMap.DURABLE_HOLDSTATE_Y);
					durableData.setLastEventName(eventInfo.getEventName());
					durableData.setLastEventTimeKey(eventInfo.getEventTimeKey());
					durableData.setLastEventTime(eventInfo.getEventTime());
					durableData.setLastEventUser(eventInfo.getEventUser());
					durableData.setLastEventComment(eventInfo.getEventComment());

					DurableHistory durableHist = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldDurbale, durableData, new DurableHistory());

					updateDurableList.add(durableData);
					updateDurableHistList.add(durableHist);
				}

				try
				{
					CommonUtil.executeBatch("update", updateDurableList, true);
					CommonUtil.executeBatch("insert", updateDurableHistList, true);

					log.info(String.format("▶Successfully hold %s trays.", updateDurableList.size()));
				}
				catch (Exception e)
				{
					log.error(e.getMessage());
					throw new CustomException(e.getCause());
				}
			}
			
			// Hold only coverTray when contains an empty tray
			if(!holdTrayGroup)
			{
				for (Durable trayData : trayDataList)
				{
					if(!coverTrayName.equals(trayData.getKey().getDurableName())&&"Available".equals(trayData.getDurableState()) && CommonValidation.checkTrayIsEmpty(trayData.getKey().getDurableName()))
					{
						eventInfo.setEventComment(String.format("Hold CoverTray[%s]!!The TrayGroup contains an empty Tray[%s].",coverTrayName,trayData.getKey().getDurableName()));
						eventInfo.setReasonCode(reasonCode);
						eventInfo.setReasonCodeType(reasonCodeType);
						eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
						eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
						
						Durable trayGroupData =  MESDurableServiceProxy.getDurableServiceUtil().getDurableData(coverTrayName);
						trayGroupData.setReasonCode(reasonCode);
						trayGroupData.setReasonCodeType(reasonCodeType);
						trayGroupData.getUdfs().put("DURABLEHOLDSTATE", constantMap.DURABLE_HOLDSTATE_Y);
						trayGroupData.setLastEventName(eventInfo.getEventName());
						trayGroupData.setLastEventTimeKey(eventInfo.getEventTimeKey());
						trayGroupData.setLastEventTime(eventInfo.getEventTime());
						trayGroupData.setLastEventUser(eventInfo.getEventUser());
						trayGroupData.setLastEventComment(eventInfo.getEventComment());

						DurableServiceProxy.getDurableService().update(trayGroupData);
						
						SetEventInfo setEventInfo = new SetEventInfo();
						setEventInfo.getUdfs().put("DURABLEHOLDSTATE", constantMap.DURABLE_HOLDSTATE_Y);
						
						DurableServiceProxy.getDurableService().setEvent(trayGroupData.getKey(), eventInfo, setEventInfo);
						
						break;
					}
				}
			}
			if(!holdTrayGroup)
			{
				for (Durable trayData : trayDataList)
				{
					//2021/7/12 T-SHIP Tray is Empty
					if(coverTrayName.equals(trayData.getKey().getDurableName()) && CommonValidation.checkTrayIsEmpty(trayData.getKey().getDurableName())&& StringUtil.in(portType, "PU")&&operationMode.equals("T-SHIP")&&BCRFlag.equals("Y"))
					{
						eventInfo.setEventComment(String.format("Hold CoverTray[%s]!!The T-SHIP TrayGroup is an empty Tray[%s].",coverTrayName,trayData.getKey().getDurableName()));
						eventInfo.setReasonCode(reasonCode);
						eventInfo.setReasonCodeType(reasonCodeType);
						eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
						eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
						Durable trayGroupData =  MESDurableServiceProxy.getDurableServiceUtil().getDurableData(coverTrayName);
						trayGroupData.setReasonCode(reasonCode);
						trayGroupData.setReasonCodeType(reasonCodeType);
						trayGroupData.getUdfs().put("DURABLEHOLDSTATE", constantMap.DURABLE_HOLDSTATE_Y);
						trayGroupData.setLastEventName(eventInfo.getEventName());
						trayGroupData.setLastEventTimeKey(eventInfo.getEventTimeKey());
						trayGroupData.setLastEventTime(eventInfo.getEventTime());
						trayGroupData.setLastEventUser(eventInfo.getEventUser());
						trayGroupData.setLastEventComment(eventInfo.getEventComment());

						DurableServiceProxy.getDurableService().update(trayGroupData);
						
						SetEventInfo setEventInfo = new SetEventInfo();
						setEventInfo.getUdfs().put("DURABLEHOLDSTATE", constantMap.DURABLE_HOLDSTATE_Y);
						
						DurableServiceProxy.getDurableService().setEvent(trayGroupData.getKey(), eventInfo, setEventInfo);
						
						break;
					}
				}
			}
		}
		catch (Exception e)
		{
			log.info("Error Occurred - Hold TrayGroup by Mismatched VCR ID");
		}
	}
}
