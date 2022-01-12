package kr.co.aim.messolution.lot.event.Tray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotFutureAction;
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
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
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
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.policy.util.LotPFIValueSetter;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processflow.management.iter.NodeStackUtil;
import kr.co.aim.greentrack.processflow.management.iter.PFIValueSetter;
import kr.co.aim.greentrack.processflow.management.iter.ProcessFlowIterator;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class TrayGroupProcessEndForClave extends SyncHandler {
	private static Log log = LogFactory.getLog(TrayGroupProcessEndForClave.class);

	@Override
	public Document doWorks(Document doc) throws CustomException
	{
		try
		{
			SMessageUtil.setHeaderItemValue(doc, "MESSAGENAME", "TrayGroupProcessEndReplyForClave");

			String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
			String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
			String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", false);
			String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
			String trayGroupName = SMessageUtil.getBodyItemValue(doc, "TRAYGROUPNAME", true);
			String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
			List<Element> trayElementList = SMessageUtil.getBodySequenceItemList(doc, "TRAYLIST", true);
			
			// remove cover tray element from trayElementList and return the element.
			Element coverTrayElement = this.removeCoverTrayElement(trayElementList, trayGroupName);
			
			Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(machineName);
			Port portData = MESPortServiceProxy.getPortInfoUtil().getPortData(machineName, portName);
			Durable trayGroupData = MESDurableServiceProxy.getDurableServiceUtil().getDurableDataForUpdate(trayGroupName);
			
			CommonValidation.checkMachineHold(machineData);
			CommonValidation.CheckDurableState(trayGroupData);
			CommonValidation.CheckDurableHoldState(trayGroupData);

			if (trayElementList == null || trayElementList.size() <= 0)
			{
				//TRAY-0016: No TrayList information received from BC.
				throw new CustomException("TRAY-0016");
			}

			if (StringUtil.equals(CommonUtil.getValue(portData.getUdfs(), "PORTTYPE"), "PU"))
			{
				EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrayGroupProcessEnd", this.getEventUser(), this.getEventComment());

				Map<String, Durable> durableDataMap = this.generateDurableDataMap(trayElementList, true);
				Map<String, Map<String, Element>> trayPanelRelationMap = new HashMap<>();
				Map<String, Element> panelElementMap = new HashMap<>();

				int maxPosition = 1;
				List<Durable> updateDurableList = new ArrayList<>(trayElementList.size());
				List<DurableHistory> updateDurableHistList = new ArrayList<>(trayElementList.size());
				
				//sub tray
				for (Element trayElement : trayElementList)
				{
					String trayName = trayElement.getChildText("TRAYNAME");
					String position = trayElement.getChildText("POSITION");

					Map<String,Element> genPanelElementMap = this.getPanelElementMap(trayElement);
					trayPanelRelationMap.put(trayName, genPanelElementMap);
					panelElementMap.putAll(genPanelElementMap);

					Durable trayData = durableDataMap.get(trayName);

					if (StringUtils.equals(trayData.getDurableType(), "CoverTray"))
					{
						// TRAY-0030: The tray group [{0}] contains multiple cover trays.
						throw new CustomException("TRAY-0030", trayGroupName);
					}
					
					if (Integer.parseInt(trayElement.getChildText("POSITION")) > maxPosition)
						maxPosition = Integer.parseInt(trayElement.getChildText("POSITION"));

					Durable oldTrayData = (Durable) ObjectUtil.copyTo(trayData);

					trayData.setLotQuantity(trayPanelRelationMap.get(trayName).size());
					trayData.setLastEventName(eventInfo.getEventName());
					trayData.setLastEventTimeKey(eventInfo.getEventTimeKey());
					trayData.setLastEventTime(eventInfo.getEventTime());
					trayData.setLastEventUser(eventInfo.getEventUser());
					trayData.setLastEventComment(eventInfo.getEventComment());

					trayData.getUdfs().put("MACHINENAME", machineName);
					trayData.getUdfs().put("PORTNAME", portName);
					trayData.getUdfs().put("POSITION", position);

					DurableHistory durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldTrayData, trayData, new DurableHistory());

					updateDurableList.add(trayData);
					updateDurableHistList.add(durHistory);
				}
				
				// cover tray 
				Durable oldCoverTrayData = (Durable) ObjectUtil.copyTo(trayGroupData);

				trayGroupData.setLotQuantity(coverTrayElement == null ?panelElementMap.size():Integer.parseInt(coverTrayElement.getChildText("PRODUCTQUANTITY")));
				trayGroupData.setLastEventName(eventInfo.getEventName());
				trayGroupData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				trayGroupData.setLastEventTime(eventInfo.getEventTime());
				trayGroupData.setLastEventUser(eventInfo.getEventUser());
				trayGroupData.setLastEventComment(eventInfo.getEventComment());

				trayGroupData.getUdfs().put("MACHINENAME", machineName);
				trayGroupData.getUdfs().put("PORTNAME", portName);
				trayGroupData.getUdfs().put("POSITION", coverTrayElement == null ? String.valueOf(maxPosition+1) : coverTrayElement.getChildText("POSITION"));

				DurableHistory coverTrayHist = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldCoverTrayData, trayGroupData, new DurableHistory());

				updateDurableList.add(trayGroupData);
				updateDurableHistList.add(coverTrayHist);
				
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

				if (panelElementMap.size() == 0)
				{
					throw new CustomException("DURABLE-9004", trayGroupName);
				}

				List<String> panelNameList = new ArrayList<>(panelElementMap.keySet());
				List<Lot> lotDataList =  MESLotServiceProxy.getLotServiceUtil().getLotDataListByPanelNameList(panelNameList, true);
				Map<String, List<Lot>>  durableLotDataListMap = this.generateDurableLotDataListMap(trayPanelRelationMap, lotDataList);
				
				// trackout panel
				this.trackOutPanel(durableLotDataListMap, panelElementMap, machineData, portData, machineRecipeName);
				
				// referesh panel data
			    lotDataList =  MESLotServiceProxy.getLotServiceUtil().getLotDataListByPanelNameList(panelNameList, false);
			    this.holdTrayGroup(updateDurableList,trayGroupData);
				EventInfo futureActionEventInfo = EventInfoUtil.makeEventInfo("Delete", getEventUser(), getEventComment(), null, null);
				futureActionEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
				futureActionEventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
               
				// CheckReserveHold
				boolean reserveFlag = PostCellDeleteLotFutureAction(futureActionEventInfo, lotDataList);
				if (reserveFlag)
				{
					EventInfo eventInfoHold = EventInfoUtil.makeEventInfo("ReserveHold", getEventUser(), getEventComment(), null, null);
					eventInfoHold.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
					eventInfoHold.setEventTime(TimeStampUtil.getCurrentTimestamp());

					MESLotServiceProxy.getLotServiceUtil().PanelHoldByLotList(eventInfoHold, lotDataList);
				}
			}
			else
			{
				log.info("Skip logic : Port [" + portName + "], PortType [" + CommonUtil.getValue(portData.getUdfs(), "PORTTYPE") + "] ");
			}

			return setResultItemValue(doc, "OK", "");
		}
		catch (CustomException ce)
		{
			setResultItemValue(doc, "NG", ce.errorDef.getLoc_errorMessage());
			throw ce;
		}
		catch (Exception e)
		{
			this.setResultItemValue(doc, "NG", e.getMessage());
			throw new CustomException(e.getCause());
		}
	}

	private Document setResultItemValue(Document doc, String result, String resultDescription) throws CustomException
	{
		Element bodyElement = doc.getRootElement().getChild(SMessageUtil.Body_Tag);
		bodyElement.getChild("TRAYLIST").detach();

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
	
	public Map<String,Element> getPanelElementMap(Element trayElement) throws CustomException
	{
		List<Element> panelElementList = trayElement.getChild("PANELLIST").getChildren("PANEL");
		
		if (Integer.parseInt(trayElement.getChildText("PRODUCTQUANTITY").isEmpty() ? "0" : trayElement.getChildText("PRODUCTQUANTITY")) != panelElementList.size())
		{
			// TRAY-0026: ProductQuantity reported by BC does not match with actual elements quantity of panel[TrayName = {0}].
			throw new CustomException("TRAY-0026", trayElement.getChildText("TRAYNAME"));
		}

		Map<String, Element> panelElementMap = new HashMap<>();

		for (Element panelElement : panelElementList)
		{
			panelElementMap.put(panelElement.getChildText("PANELNAME"), panelElement);
		}

		return panelElementMap;
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

	public Map<String, List<Lot>> generateDurableLotDataListMap(Map<String,Map<String,Element>> trayPanelRelationMap,List<Lot> lotDataList) throws CustomException
	{
		Map<String,String> panelTrayMap = this.makePanelTrayMapByTrayPanelRelationMap(trayPanelRelationMap);
		Map<String, List<Lot>> durableLotListMap = new HashMap<>();

		for (final Lot lotData: lotDataList)
		{
			if (durableLotListMap.containsKey(panelTrayMap.get(lotData.getKey().getLotName())))
				durableLotListMap.get(panelTrayMap.get(lotData.getKey().getLotName())).add(lotData);
			else
				durableLotListMap.put(panelTrayMap.get(lotData.getKey().getLotName()), new ArrayList<Lot>() {{this.add(lotData);}});
		}

		return durableLotListMap;
	}
	
	public List<String> makePanelListByTrayPanelRelationMap(Map<String,Map<String,Element>> trayPanelRelationMap)
	{
		List<String> panelList = new ArrayList<>();

		for (Map<String, Element> value : trayPanelRelationMap.values())
		{
			panelList.addAll(value.keySet());
		}
		return panelList;
	}
	
	public Map<String,String> makePanelTrayMapByTrayPanelRelationMap(Map<String,Map<String,Element>> trayPanelRelationMap)
	{
		Map<String,String> panelTrayMap = new HashMap<>();

		for (String key : trayPanelRelationMap.keySet())
		{
			for(String subKey : trayPanelRelationMap.get(key).keySet())
			{
				panelTrayMap.put(subKey, key);
			}
		}
		
		return panelTrayMap;
	}

	public List<Element> getPanelElementListByTrayList(List<Element> trayElementList)
	{
		List<Element> panelElementList = new ArrayList<>();

		try
		{
			for (Element trayElement : trayElementList)
			{
				panelElementList.addAll(trayElement.getChild("PANELLIST").getChildren("PANEL"));
			}
		}
		catch (Exception ex)
		{
			log.info(ex.getMessage());
		}

		return panelElementList;
	}

	public Map<String, Element> generatePanelElementMap(List<Element> panelElementList)
	{
		Map<String, Element> panelElementMap = new HashMap<>();

		for (Element panelElement : panelElementList)
		{
			panelElementMap.put(panelElement.getChildText("PANELNAME"), panelElement);
		}

		return panelElementMap;
	}

	public List<Durable> getTrayDataListByPanelElementMap(Map<String,Map<String,Element>>  trayPanelRelationMap) throws CustomException
	{
		List<Map<String, Object>> resultList = null;
		Map<String, Object> bindMap = new HashMap<>();
		bindMap.put("DURABLELIST", trayPanelRelationMap.keySet());

		try
		{
			resultList = greenFrameServiceProxy.getSqlTemplate().queryForList(" SELECT * FROM DURABLE WHERE 1=1 AND DURABLENAME IN (:DURABLELIST) ", bindMap);
		}
		catch (Exception ex)
		{
			throw new CustomException(ex.getCause());
		}

		return DurableServiceProxy.getDurableService().transform(resultList);

	}

	public void trackOutPanel(Map<String, List<Lot>> durableLotListMap ,Map<String,Element> panelElementMap,Machine machineData,Port portData,String machineRecipeName) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOut", this.getEventUser(), this.getEventComment());
		List<Lot> updateLotList = new ArrayList<>();
		List<LotHistory> updateHistList = new ArrayList<>();
		List<Lot> scrapLotList = new ArrayList<>();
		
		String checkNodeStack = "";
		Node  nextNode = null;
		
		for (String carrierName : durableLotListMap.keySet())
		{
			for (Lot lotData : durableLotListMap.get(carrierName))
			{
				CommonValidation.checkLotProcessStateRun(lotData);
				CommonValidation.checkLotState(lotData);
				CommonValidation.checkLotHoldState(lotData);

				Lot oldLot = (Lot) ObjectUtil.copyTo(lotData);

				lotData.setMachineName(machineData.getKey().getMachineName());
				lotData.setMachineRecipeName(machineRecipeName);
				lotData.setCarrierName(carrierName);
				//lotData.setLotState(GenericServiceProxy.getConstantMap().Lot_Released);
				lotData.setLotProcessState(GenericServiceProxy.getConstantMap().Lot_Wait);
				lotData.setLotGrade(panelElementMap.get(lotData.getKey().getLotName()).getChildText("PRODUCTJUDGE"));
				lotData.setLastLoggedOutTime(eventInfo.getEventTime());
				lotData.setLastLoggedOutUser(eventInfo.getEventUser());
				lotData.setLastEventName(eventInfo.getEventName());
				lotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
				lotData.setLastEventTime(eventInfo.getEventTime());
				lotData.setLastEventUser(eventInfo.getEventUser());
				lotData.setLastEventComment(eventInfo.getEventComment());
				lotData.setLastEventFlag(GenericServiceProxy.getConstantMap().Flag_N);

				lotData.getUdfs().put("PORTNAME", portData.getKey().getPortName());
				lotData.getUdfs().put("PORTTYPE", portData.getUdfs().get("PORTTYPE"));
				lotData.getUdfs().put("PORTUSETYPE", portData.getUdfs().get("PORTUSETYPE"));

				lotData.getUdfs().put("BEFOREOPERATIONNAME", oldLot.getProcessOperationName());
				lotData.getUdfs().put("BEFOREOPERATIONVER", oldLot.getProcessOperationVersion());
				lotData.getUdfs().put("BEFOREFLOWNAME", oldLot.getProcessFlowName());
				lotData.getUdfs().put("LOTDETAILGRADE", panelElementMap.get(lotData.getKey().getLotName()).getChildText("PRODUCTGRADE"));
				lotData.getUdfs().put("POSITION", panelElementMap.get(lotData.getKey().getLotName()).getChildText("POSITION"));

				if (checkNodeStack.isEmpty())
				{
					checkNodeStack = lotData.getNodeStack();
				}
				else
				{
					if (!checkNodeStack.equals(lotData.getNodeStack()))
					{
						// TRAY-0027:There are panels of different operations in the traygroup.
						throw new CustomException("TRAY-0027");
					}
				}

				if (nextNode == null)
				{
					ProcessFlowKey processFlowKey = new ProcessFlowKey();
					processFlowKey.setFactoryName(lotData.getFactoryName());
					processFlowKey.setProcessFlowName(lotData.getProcessFlowName());
					processFlowKey.setProcessFlowVersion(lotData.getProcessFlowVersion());

					ProcessFlow processFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);

					kr.co.aim.greentrack.processflow.management.iter.NodeStack nodeStack = NodeStackUtil.stringToNodeStack(lotData.getNodeStack());
					ProcessFlowIterator pfi = new ProcessFlowIterator(processFlow, nodeStack, "");

					PFIValueSetter valueSetter = new LotPFIValueSetter(pfi, oldLot, lotData );
					pfi.moveNext("N", valueSetter);

					nextNode = pfi.getCurrentNodeData();
				}

				lotData.setNodeStack(nextNode.getKey().getNodeId());
				lotData.setProcessFlowName(nextNode.getProcessFlowName());
				lotData.setProcessFlowVersion(nextNode.getProcessFlowVersion());
				lotData.setProcessOperationName(nextNode.getNodeAttribute1());
				lotData.setProcessOperationVersion(nextNode.getNodeAttribute2());

				LotHistory lotHist = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, new LotHistory());

				updateLotList.add(lotData);
				updateHistList.add(lotHist);
				
				/*//2020/11/03 caixu Clave Track Out LotGrade is S not Scrap
				 * if (StringUtil.in(lotData.getLotGrade(),  "S"))
				{
					scrapLotList.add(lotData);
				}*/
			}
		}

		if (updateLotList.size() > 0 && updateHistList.size() > 0)
		{
			try
			{
				CommonUtil.executeBatch("update", updateLotList, true);
				CommonUtil.executeBatch("insert", updateHistList, true);

				log.info(String.format("▶Successfully update %s pieces of panels.", updateLotList.size()));
			}
			catch (Exception e)
			{
				log.error(e.getMessage());
				throw new CustomException(e.getCause());
			}
		}
		
		//auto scrap
		if(scrapLotList.size()>0)
		{
			EventInfo scrapEventInfo = EventInfoUtil.makeEventInfo("ScrapLot", getEventUser(), getEventComment());

			List<Lot> updateScrapLotList = new ArrayList<>();
			List<LotHistory> updateScrapHistList = new ArrayList<>();

			for (int i = 0; i < scrapLotList.size(); i++)
			{
				Lot lotData = scrapLotList.get(i);
				Lot oldLot = (Lot) ObjectUtil.copyTo(lotData);

				//lotData.setLotState(GenericServiceProxy.getConstantMap().Lot_Scrapped);
				lotData.setLotGrade("S");
				lotData.setReasonCode("Auto Scrap");
				lotData.getUdfs().put("LOTDETAILGRADE", "");
				lotData.setLastEventName(scrapEventInfo.getEventName());
				lotData.setLastEventTimeKey(scrapEventInfo.getEventTimeKey());
				lotData.setLastEventTime(scrapEventInfo.getEventTime());
				lotData.setLastEventUser(scrapEventInfo.getEventUser());
				lotData.setLastEventComment(scrapEventInfo.getEventComment());
				lotData.setLastEventFlag(GenericServiceProxy.getConstantMap().Flag_N);

				LotHistory lotHist = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, new LotHistory());

				updateScrapLotList.add(lotData);
				updateScrapHistList.add(lotHist);
			}
			
			try
			{
				CommonUtil.executeBatch("update", updateScrapLotList, true);
				CommonUtil.executeBatch("insert", updateScrapHistList, true);

				log.info(String.format("▶Successfully scrap %s pieces of panels.", updateScrapLotList.size()));
			}
			catch (Exception e)
			{
				log.error(e.getMessage());
				throw new CustomException(e.getCause());
			}
			
			
			String workOrderName = updateScrapLotList.get(0).getProductRequestName();

			// ChangeWorkOrder Scrap
			ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(scrapEventInfo, workOrderName, updateScrapLotList.size(), 0);

			if (newProductRequestData.getPlanQuantity() == newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity())
			{
				scrapEventInfo.setEventName("Complete");
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(scrapEventInfo, workOrderName);
			}
		}
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
	private void holdTrayGroup(List<Durable> trayDataList,Durable trayGroupData) throws CustomException
	{
		try
		{
			String reasonCode = "HD100";
			String reasonCodeType = "HOLD";
			boolean holdTrayGroup = false;
			
			ConstantMap constantMap = GenericServiceProxy.getConstantMap();
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Hold", this.getEventUser(), this.getEventComment());
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

						eventInfo.setEventComment(String.format("Multiple specifications of panels in the TrayGroup[%s].", trayGroupData.getKey().getDurableName()));
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
			log.info("Error Occurred - Hold TrayGroup by Mismatched VCR ID");
		}
	}

	private void trayGroupHoldForMismatchedVCRID(List<String> panelNameList, List<Durable> allTrayDataList,String coverTrayName) throws CustomException
	{
		try
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("Hold", this.getEventUser(), this.getEventComment());
			String reasonCode = "HD001";
			String reasonCodeType = "HOLD";
			boolean holdTrayGroup = false;
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT * ");
			sql.append(" FROM LOT ");
			sql.append(" WHERE LOTNAME IN (:LOTLIST) ");
			sql.append(" AND VCRPRODUCTNAME IS NOT NULL ");
			sql.append(" AND LOTNAME != VCRPRODUCTNAME ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("LOTLIST", panelNameList);

			List<Map<String, Object>> resultList = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(sql.toString(), bindMap);

			if (resultList.size() > 0)
			{
				//String reasonCode = "HD100";
				reasonCode = "HD-PanelID Mismatched";
			    reasonCodeType = "HOLD";
				ConstantMap constantMap = GenericServiceProxy.getConstantMap();

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
					lotData.setLastEventComment(String.format("Hold mismatched panel. [Mes Registred: %s, VCR Read: %s]", lotData.getKey().getLotName(),lotData.getUdfs().get("VCRPRODUCTNAME")));

					LotHistory lotHist = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lotData, new LotHistory());

					updateLotList.add(lotData);
					updateHistList.add(lotHist);
				}

				try
				{
					CommonUtil.executeBatch("update", updateLotList, true);
					CommonUtil.executeBatch("insert", updateHistList, true);

					log.info(String.format("▶Successfully hold %s pieces of panels.", updateLotList.size()));
				}
				catch (Exception e)
				{
					log.error(e.getMessage());
					throw new CustomException(e.getCause());
				}

				List<Durable> updateDurableList = new ArrayList<>();
				List<DurableHistory> updateDurableHistList = new ArrayList<>();

				for (Durable durableData : allTrayDataList)
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
			
		}
		catch (Exception e)
		{
			log.info("Error Occurred - Hold TrayGroup by Mismatched VCR ID");
		}
	}
}
