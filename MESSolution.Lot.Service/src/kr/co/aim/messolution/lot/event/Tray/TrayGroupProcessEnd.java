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
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greenframe.util.xml.JdomUtils;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

public class TrayGroupProcessEnd extends SyncHandler {
	private static Log log = LogFactory.getLog(TrayGroupProcessEnd.class);

	@Override
	public Document doWorks(Document doc) throws CustomException
	{
		try
		{
			// Set MessageName
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrayGroupProcessEndReply");

			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
			String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
			String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
			String trayGroupName = SMessageUtil.getBodyItemValue(doc, "TRAYGROUPNAME", true);
			List<Element> trayElementList = SMessageUtil.getBodySequenceItemList(doc, "TRAYLIST", false);

			// remove cover tray element from trayElementList and return the element.
			Element coverTrayElement = this.removeCoverTrayElement(trayElementList, trayGroupName);

			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
			Durable trayGroupData = MESDurableServiceProxy.getDurableServiceUtil().getDurableDataForUpdate(trayGroupName);

			CommonValidation.checkMachineHold(machineData);
			CommonValidation.CheckDurableState(trayGroupData);
			CommonValidation.CheckDurableHoldState(trayGroupData);
			CommonValidation.checkAvailableCst(trayGroupData);//caixu 2020/11/3 Add  CoverTray AvaialableCST Check

			if (portType.equals("PU") || portType.equals("BU"))
			{
				if (trayElementList == null || trayElementList.size() <= 0)
				{
					//TRAY-0016: No TrayList information received from BC.
					throw new CustomException("TRAY-0016");
				}
			}

			EventInfo eventInfo = EventInfoUtil.makeEventInfo("AssignTrayGroup", this.getEventUser(), this.getEventComment());
			Map<String, Durable> durableDataMap = this.generateDurableDataMap(trayElementList, true);

			int totalLotQty = 0;
			int maxPosition = 1;
			List<Durable> updateDurableList = new ArrayList<>(trayElementList.size());
			List<DurableHistory> updateDurableHistList = new ArrayList<>(trayElementList.size());

			for (Element trayElement : trayElementList)
			{
				String trayName = trayElement.getChildText("TRAYNAME");
				String position = trayElement.getChildText("POSITION");

				Durable trayData = durableDataMap.get(trayName);

				if (StringUtils.equals(trayData.getDurableType(), "CoverTray"))
				{
					//TRAY-0017:The tray group [{0}] contains multiple cover trays.[IssueTray:{1}]
					throw new CustomException("TRAY-0017", trayGroupName, trayData.getKey().getDurableName());
				}
				
				if (!StringUtil.isEmpty(trayData.getUdfs().get("COVERNAME")))
					throw new CustomException("CARRIER-9008", trayName, trayData.getUdfs().get("COVERNAME"));


				if (portType.equals("PU"))
				{
					// check is not empty tray
					if (!("Available".equals(trayData.getDurableState()) && CommonValidation.checkTrayIsEmpty(trayName)))
						totalLotQty += trayData.getLotQuantity();

					if (Integer.parseInt(trayElement.getChildText("POSITION")) > maxPosition)
						maxPosition = Integer.parseInt(trayElement.getChildText("POSITION"));
				}
				else
				{
					// PL or BU Port tray must be empty
					CommonValidation.checkTrayIsEmpty(trayData);
				}

				Durable oldTrayData = (Durable) ObjectUtil.copyTo(trayData);

				// assign tray group
				trayData.getUdfs().put("COVERNAME", trayGroupName);
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

			// init cover tray
			trayGroupData.setDurableType("CoverTray");
			trayGroupData.setLotQuantity(totalLotQty);
			trayGroupData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
			DurableServiceProxy.getDurableService().update(trayGroupData);

			SetEventInfo setEventInfo = new SetEventInfo();
			setEventInfo.getUdfs().put("COVERNAME",trayGroupData.getKey().getDurableName() );
			setEventInfo.getUdfs().put("POSITION", coverTrayElement == null ? String.valueOf(maxPosition + 1) : coverTrayElement.getChildText("POSITION"));
			setEventInfo.getUdfs().put("MACHINENAME", "");
			setEventInfo.getUdfs().put("PORTNAME", "");
			setEventInfo.getUdfs().put("DURABLETYPE1", "CoverTray");

			trayGroupData = DurableServiceProxy.getDurableService().setEvent(trayGroupData.getKey(), eventInfo, setEventInfo);

			if (portType.equals("PU"))
			{
				List<String> panelNameList = null;
				List<Lot> panelDataList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayList(updateDurableList, true);
				// hold tray group by case . 1.Read panelName missmatch 2. hold tray in traygroup 3. Add CRP2 LotGrade is G TrackOutHold 4. contains emptyTray//2020/12/19 caixu
				this.holdTrayGroup(panelNameList = CommonUtil.makeToStringList(panelDataList), updateDurableList,trayGroupData,machineData,portName, portType);

				//trayGroupHoldForMismatchedVCRID(panelNameList = CommonUtil.makeToStringList(panelDataList), trayGroupData);
				//trayGroupHoldForCRP2(panelNameList = CommonUtil.makeToStringList(panelDataList), trayGroupData);//caixu Add CRP2 LotGrade is G TrackOutHold
                
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
			else
			{
				log.info("Skip logic : Port [" + portName + "], PortType [" + CommonUtil.getValue(portData.getUdfs(), "PORTTYPE") + "] ");
			}
			
			//TrackOut Report for SAP////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//Mark by yueke 20210324-> change to panelProcessEnd
			/*
			List<Map<String, Object>> panelList = MESLotServiceProxy.getLotServiceUtil().getLotListByTrayGroup(trayGroupName);
			String groupLotName = panelList.get(0).get("LOTNAME").toString();
			Lot groupLotData = MESLotServiceProxy.getLotInfoUtil().getLotData(groupLotName);
			ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(groupLotData.getProductRequestName());
			//trackOutERPBOMReportForTrayGroup
			
			String sapFlag=CommonUtil.getEnumDefValueStringByEnumName("SAPFLAG");			
			if(StringUtil.isNotEmpty(sapFlag)&&StringUtil.equals(sapFlag, "Y")&&
					StringUtils.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
			{
				SuperProductRequest superWO = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
				
				MESConsumableServiceProxy.getConsumableServiceUtil().trackOutERPBOMReportForTrayGroup(eventInfo, groupLotData, superWO, groupLotData.getMachineName(), totalLotQty, panelList);
			}*/
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			
			log.info("Result : OK");
		}
		catch (CustomException ce)
		{
			log.info("Result : " + ce.errorDef.getErrorCode() + ", DESCRIPTION : " + ce.errorDef.getLoc_errorMessage());
		}
		catch (Exception e)
		{
			log.info("Result : UndefinedCode, DESCRIPTION : " + e.getMessage());
		}
		finally
		{
			Element newBody = generateReturnBodyTemplate(SMessageUtil.getBodyElement(doc));
			newBody.getChild("RESULT").setText("OK");
			newBody.getChild("RESULTDESCRIPTION").setText("");

			doc.getRootElement().getChild(SMessageUtil.Body_Tag).detach();
			doc.getRootElement().addContent(2, newBody);
		}

		return doc;
	}

	private Element generateReturnBodyTemplate(Element bodyElement) throws CustomException
	{
		Element body = new Element(SMessageUtil.Body_Tag);

		JdomUtils.addElement(body, "MACHINENAME", bodyElement.getChildText("MACHINENAME"));
		JdomUtils.addElement(body, "PORTNAME", bodyElement.getChildText("PORTNAME"));
		JdomUtils.addElement(body, "PORTTYPE", bodyElement.getChildText("PORTTYPE"));
		JdomUtils.addElement(body, "PORTUSETYPE", bodyElement.getChildText("PORTUSETYPE"));
		JdomUtils.addElement(body, "TRAYGROUPNAME", bodyElement.getChildText("TRAYGROUPNAME"));
		JdomUtils.addElement(body, "RESULT", "");
		JdomUtils.addElement(body, "RESULTDESCRIPTION", "");

		return body;
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
	private void holdTrayGroup(List<String> panelNameList, List<Durable> trayDataList,Durable trayGroupData,Machine MachineData,String PortName, String portType ) throws CustomException
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

				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("LOTLIST", panelNameList);

				List<Map<String, Object>> resultList = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(query.toString(), bindMap);

				if (resultList.size() > 0)
				{
					// BCR Read missmatch
					eventInfo.setEventComment(String.format("Hold TrayGroup by VCR Different Panel ID.[CoverTray=%s,Issue Panel Qunatity=%s]", trayGroupData.getKey().getDurableName(), resultList.size()));
					//eventInfo.setReasonCode(reasonCode);
					eventInfo.setReasonCode("HD-PanelID Mismatched"); // add by jinlj
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
						//lotData.setReasonCode(reasonCode);
						lotData.setReasonCode(eventInfo.getReasonCode());
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
					List<Durable> updateDurableList = new ArrayList<>();
					List<DurableHistory> updateDurableHistList = new ArrayList<>();
					// sub tray list + cover tray
					trayDataList.add(trayGroupData);

					for (Durable durableData : trayDataList)
					{
						Durable oldDurbale = (Durable) ObjectUtil.copyTo(durableData);

						//durableData.setReasonCode(reasonCode);
						durableData.setReasonCode(eventInfo.getReasonCode());
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
			}
			if(!holdTrayGroup)
			{
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
						log.info("Not Exit");
					}
					
					if(resultList!=null&& resultList.size()>1)
					{
						holdTrayGroup = true;

						eventInfo.setEventComment(String.format("Different ProductSpec or FlowName or WO or Operation  panels in the TrayGroup[%s].", trayGroupData.getKey().getDurableName()));
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
					trayDataList.add(trayGroupData);
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

						log.info(String.format("?Successfully hold %s trays.", updateDurableList.size()));
					}
					catch (Exception e)
					{
						log.error(e.getMessage());
						throw new CustomException(e.getCause());
					}
				}
		
		    }
			if(!holdTrayGroup)
			{
				try
				{

					StringBuilder sql = new StringBuilder();
					sql.append("SELECT * ");
					sql.append(" FROM LOT L,CT_RESERVEHOLDBYWOINFO RE ");
					sql.append(" WHERE L.LOTNAME IN (:LOTLIST)  ");
					sql.append(" AND L.BEFOREOPERATIONNAME=RE.PROCESSOPERATION ");
					sql.append(" AND L.PRODUCTREQUESTNAME=RE.PRODUCTREQUESTNAME ");
					sql.append(" AND L.LOTGRADE=RE.PANELGRADE ");
					

					Map<String, Object> bindMap = new HashMap<String, Object>();
					bindMap.put("LOTLIST", panelNameList);

					List<Map<String, Object>> resultList = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), bindMap);

					if (resultList.size() > 0)
					{
						String reserveHoldUser = resultList.get(0).get("LASTEVENTUSER").toString();
						eventInfo.setEventComment(String.format("Hold By ReserveHoldByWorkOrder.[CoverTray=%s,Issue Panel Qunatity=%s],ReserveUser=%s,ReserveComment=%s", trayGroupData.getKey().getDurableName(), resultList.size(), reserveHoldUser, resultList.get(0).get("LASTEVENTCOMMENT").toString()));
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
							lotData.setLastEventUser(reserveHoldUser);
							lotData.setLastEventComment(eventInfo.getEventComment());

							LotHistory lotHist = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, new LotHistory());

							updateLotList.add(lotData);
							updateHistList.add(lotHist);
						}

						try
						{
							CommonUtil.executeBatch("update", updateLotList, true);
							CommonUtil.executeBatch("insert", updateHistList, true);

							log.info(String.format("?Successfully hold %s pieces of panels.", updateLotList.size()));
						}
						catch (Exception e)
						{
							log.error(e.getMessage());
							throw new CustomException(e.getCause());
						}

						List<Durable> updateDurableList = new ArrayList<>();
						List<DurableHistory> updateDurableHistList = new ArrayList<>();

						// sub tray list + cover tray
						trayDataList.add(trayGroupData);

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

							log.info(String.format("?Successfully hold %s trays.", updateDurableList.size()));
						}
						catch (Exception e)
						{
							log.error(e.getMessage());
							throw new CustomException(e.getCause());
						}
					}
				}
				catch (Exception e)
				{
					log.info("Error Occurred - Hold By ReserveHoldByWorkOrder");
				}
			}
			if(!holdTrayGroup&&MachineData.getMachineGroupName().equals("SVI"))
			{
				try
				{

					StringBuilder sql = new StringBuilder();
					sql.append("SELECT * ");
					sql.append(" FROM LOT ");
					sql.append(" WHERE LOTNAME IN (:LOTLIST) ");
					sql.append(" AND PROCESSOPERATIONNAME='37000' ");
					sql.append(" AND BEFOREOPERATIONNAME='35052' ");
					sql.append(" AND LOTGRADE='S' ");
					sql.append(" AND LOTDETAILGRADE='S1'");
					

					Map<String, Object> bindMap = new HashMap<String, Object>();
					bindMap.put("LOTLIST", panelNameList);

					List<Map<String, Object>> resultList = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), bindMap);

					if (resultList.size() > 0)
					{

						eventInfo.setEventComment(String.format("Hold TrayGroup by S1 .[CoverTray=%s,Issue Panel Qunatity=%s]", trayGroupData.getKey().getDurableName(), resultList.size()));
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
							CommonUtil.executeBatch("update", updateLotList, true);
							CommonUtil.executeBatch("insert", updateHistList, true);

							log.info(String.format("?Successfully hold %s pieces of panels.", updateLotList.size()));
						}
						catch (Exception e)
						{
							log.error(e.getMessage());
							throw new CustomException(e.getCause());
						}

						List<Durable> updateDurableList = new ArrayList<>();
						List<DurableHistory> updateDurableHistList = new ArrayList<>();

						// sub tray list + cover tray
						trayDataList.add(trayGroupData);

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

							log.info(String.format("?Successfully hold %s trays.", updateDurableList.size()));
						}
						catch (Exception e)
						{
							log.error(e.getMessage());
							throw new CustomException(e.getCause());
						}
					}
				}
				catch (Exception e)
				{
					log.info("Error Occurred - Hold TrayGroup by S1");
				}
			}
			/*
			if(!holdTrayGroup&&MachineData.getMachineGroupName().equals("AVI"))
			{
				try
				{

					StringBuilder sql = new StringBuilder();
					sql.append("SELECT * ");
					sql.append(" FROM LOT ");
					sql.append(" WHERE LOTNAME IN (:LOTLIST) ");
					sql.append(" AND PROCESSOPERATIONNAME='32000'");
					sql.append(" AND BEFOREOPERATIONNAME='35000'");
					sql.append(" AND LOTGRADE='G'");
					
					Map<String, Object> bindMap = new HashMap<String, Object>();
					bindMap.put("LOTLIST", panelNameList);

					List<Map<String, Object>> resultList = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), bindMap);

					if (resultList.size() > 0)
					{

						eventInfo.setEventComment(String.format("Hold TrayGroup by AVI G .[CoverTray=%s,Issue Panel Qunatity=%s]", trayGroupData.getKey().getDurableName(), resultList.size()));
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
							CommonUtil.executeBatch("update", updateLotList, true);
							CommonUtil.executeBatch("insert", updateHistList, true);

							log.info(String.format("?Successfully hold %s pieces of panels.", updateLotList.size()));
						}
						catch (Exception e)
						{
							log.error(e.getMessage());
							throw new CustomException(e.getCause());
						}

						List<Durable> updateDurableList = new ArrayList<>();
						List<DurableHistory> updateDurableHistList = new ArrayList<>();

						// sub tray list + cover tray
						trayDataList.add(trayGroupData);

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

							log.info(String.format("?Successfully hold %s trays.", updateDurableList.size()));
						}
						catch (Exception e)
						{
							log.error(e.getMessage());
							throw new CustomException(e.getCause());
						}
					}
				}
				catch (Exception e)
				{
					log.info("Error Occurred - Hold TrayGroup by Mismatched VCR ID");
				}
			}
			*/
			if(!holdTrayGroup&&MachineData.getMachineGroupName().equals("AVI"))
			{
				try
				{

					StringBuilder sqlSort = new StringBuilder();
					sqlSort.append("SELECT * ");
					sqlSort.append(" FROM LOT ");
					sqlSort.append(" WHERE LOTNAME IN (:LOTLIST) ");
					sqlSort.append(" AND BEFOREOPERATIONNAME='3S003'");
					
					Map<String, Object> bindMap = new HashMap<String, Object>();
					bindMap.put("LOTLIST", panelNameList);

					List<Map<String, Object>> resultListSort = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sqlSort.toString(), bindMap);

					if (resultListSort.size()>0&&(PortName.equals("P04")||PortName.equals("P05")))
					{

						eventInfo.setEventComment(String.format("Hold TrayGroup by  AVI Sort 3S003.[CoverTray=%s,Issue Panel Qunatity=%s]", trayGroupData.getKey().getDurableName(), resultListSort.size()));
						eventInfo.setReasonCode(reasonCode);
						eventInfo.setReasonCodeType(reasonCodeType);
						eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
						eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

						List<Lot> updateLotList = new ArrayList<>(panelNameList.size());
						List<LotHistory> updateHistList = new ArrayList<>(panelNameList.size());						

						List<Lot> lotDataList = LotServiceProxy.getLotService().transform(resultListSort);
						//2021/8/25 jinlj start
						String issuse = "N";
						StringBuilder sqlIssuse = new StringBuilder();
						sqlIssuse.append("SELECT * FROM ENUMDEFVALUE ED ");
						sqlIssuse.append(" WHERE  ED.ENUMNAME  = 'IssusPanelByWorkOrder' ");
						sqlIssuse.append(" AND ED.ENUMVALUE IN ( SELECT DISTINCT PRODUCTREQUESTNAME  ");
						sqlIssuse.append(" FROM LOT L WHERE L.LOTNAME IN (:LOTLIST) )");
						Map<String, Object> bindMap1 = new HashMap<String, Object>();
						bindMap1.put("LOTLIST", panelNameList);
						List<Map<String, Object>> resultListIssuse = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sqlIssuse.toString(), bindMap1);
						if(resultListIssuse!=null&&resultListIssuse.size()>0)
						{
							 issuse = "Y";
						}
						//2021/8/25 jinlj end
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
							//2021/8/25 jinlj start
							if(issuse.equals("Y"))
							{
								Map<String, String> lotUdf = new HashMap<>();
								lotUdf.put("LOTISSUESTATE", "Y");
								lotData.setUdfs(lotUdf);
							}
							//2021/8/25 jinlj end

							LotHistory lotHist = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, new LotHistory());

							updateLotList.add(lotData);
							updateHistList.add(lotHist);
						}

						try
						{
							CommonUtil.executeBatch("update", updateLotList, true);
							CommonUtil.executeBatch("insert", updateHistList, true);

							log.info(String.format("?Successfully hold %s pieces of panels.", updateLotList.size()));
						}
						catch (Exception e)
						{
							log.error(e.getMessage());
							throw new CustomException(e.getCause());
						}

						List<Durable> updateDurableList = new ArrayList<>();
						List<DurableHistory> updateDurableHistList = new ArrayList<>();

						// sub tray list + cover tray
						trayDataList.add(trayGroupData);

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

							log.info(String.format("?Successfully hold %s trays.", updateDurableList.size()));
						}
						catch (Exception e)
						{
							log.error(e.getMessage());
							throw new CustomException(e.getCause());
						}
					}
				}
				catch (Exception e)
				{
					log.info("Error Occurred - Hold TrayGroup by  AVI Sort 3S003.");
				}
			}
			if(!holdTrayGroup)
			{
				try
				{
					String sqlAVI="SELECT  ENUMVALUE FROM ENUMDEFVALUE WHERE ENUMNAME='PostCellAVISortHoldSpec'";
					Map<String, Object> bindMapAVI = new HashMap<String, Object>();
					List<Map<String, Object>>resultAVI=null;
					resultAVI = greenFrameServiceProxy.getSqlTemplate().queryForList(sqlAVI, bindMapAVI);
					if(resultAVI!=null&& resultAVI.size()>0)
					{   
						List<Map<String, Object>> resultList = null;
						for(Map<String, Object> PRODUCTSPECNAME:resultAVI)
						{
							StringBuilder sql = new StringBuilder();
							sql.append("SELECT * ");
							sql.append(" FROM LOT ");
							sql.append(" WHERE LOTNAME IN (:LOTLIST) ");
							sql.append(" AND PROCESSOPERATIONNAME='34000' ");
							sql.append(" AND BEFOREOPERATIONNAME='3S001' ");
							sql.append(" AND PRODUCTSPECNAME=:PRODUCTSPECNAME");
		                   
							Map<String, Object> bindMap = new HashMap<String, Object>();
							bindMap.put("LOTLIST", panelNameList);
							bindMap.put("PRODUCTSPECNAME",PRODUCTSPECNAME.get("ENUMVALUE"));
							resultList = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), bindMap);
							if(resultList!=null&& resultList.size()>0)
							{
								break;
							}
							
						}

						if (resultList.size() > 0&&(PortName.equals("P04")||PortName.equals("P05")))
						{

							eventInfo.setEventComment(String.format("Hold TrayGroup by AVI Sort 3S001 P04 Or P05", trayGroupData.getKey().getDurableName(), resultList.size()));
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
								CommonUtil.executeBatch("update", updateLotList, true);
								CommonUtil.executeBatch("insert", updateHistList, true);

								log.info(String.format("?Successfully hold %s pieces of panels.", updateLotList.size()));
							}
							catch (Exception e)
							{
								log.error(e.getMessage());
								throw new CustomException(e.getCause());
							}

							List<Durable> updateDurableList = new ArrayList<>();
							List<DurableHistory> updateDurableHistList = new ArrayList<>();

							// sub tray list + cover tray
							trayDataList.add(trayGroupData);

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

								log.info(String.format("?Successfully hold %s trays.", updateDurableList.size()));
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
					log.info("Error Occurred - Hold TrayGroup by AVI Sort P04");
				}
			}
			if(!holdTrayGroup)
			{
				try
				{

					StringBuilder sql = new StringBuilder();
					sql.append("SELECT * ");
					sql.append(" FROM LOT ");
					sql.append(" WHERE LOTNAME IN (:LOTLIST) ");
					sql.append(" AND PROCESSOPERATIONNAME='32000'");
					sql.append(" AND BEFOREOPERATIONNAME='35081'");
					sql.append(" AND LOTGRADE='G'");
					

					Map<String, Object> bindMap = new HashMap<String, Object>();
					bindMap.put("LOTLIST", panelNameList);

					List<Map<String, Object>> resultList = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), bindMap);

					if (resultList.size() > 0)
					{

						eventInfo.setEventComment(String.format("Hold TrayGroup by CRP2 .[CoverTray=%s,Issue Panel Qunatity=%s]", trayGroupData.getKey().getDurableName(), resultList.size()));
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
							CommonUtil.executeBatch("update", updateLotList, true);
							CommonUtil.executeBatch("insert", updateHistList, true);

							log.info(String.format("?Successfully hold %s pieces of panels.", updateLotList.size()));
						}
						catch (Exception e)
						{
							log.error(e.getMessage());
							throw new CustomException(e.getCause());
						}

						List<Durable> updateDurableList = new ArrayList<>();
						List<DurableHistory> updateDurableHistList = new ArrayList<>();

						// sub tray list + cover tray
						trayDataList.add(trayGroupData);

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

							log.info(String.format("?Successfully hold %s trays.", updateDurableList.size()));
						}
						catch (Exception e)
						{
							log.error(e.getMessage());
							throw new CustomException(e.getCause());
						}
					}
				}
				catch (Exception e)
				{
					log.info("Error Occurred - Hold TrayGroup by CRP2 ");
				}
			}
			// Hold only coverTray when contains an empty tray
			if(!holdTrayGroup)
			{
				for (Durable trayData : trayDataList)
				{
					if(!trayGroupData.getKey().getDurableName().equals(trayData.getKey().getDurableName())&&"Available".equals(trayData.getDurableState()) && CommonValidation.checkTrayIsEmpty(trayData.getKey().getDurableName()))
					{
						eventInfo.setEventComment(String.format("Hold CoverTray[%s]!!The TrayGroup contains an empty Tray[%s].",trayGroupData.getKey().getDurableName(),trayData.getKey().getDurableName()));
						eventInfo.setReasonCode(reasonCode);
						eventInfo.setReasonCodeType(reasonCodeType);
						eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
						eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
						
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
			log.info("Error Occurred - Hold TrayGroup by contains an empty Tray");
		}
	}
}
