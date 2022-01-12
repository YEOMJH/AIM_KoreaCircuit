package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.LotFutureAction;
import kr.co.aim.messolution.extended.object.management.data.PanelProcessCount;
import kr.co.aim.messolution.extended.object.management.data.ReworkProduct;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.policy.util.LotPFIValueSetter;
import kr.co.aim.greentrack.machine.management.data.Machine;
import kr.co.aim.greentrack.processflow.ProcessFlowServiceProxy;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlowKey;
import kr.co.aim.greentrack.processflow.management.iter.NodeStackUtil;
import kr.co.aim.greentrack.processflow.management.iter.PFIValueSetter;
import kr.co.aim.greentrack.processflow.management.iter.ProcessFlowIterator;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processoperationspec.management.data.ProcessOperationSpec;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class CommonTrackOutExcel extends SyncHandler {

	Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> lotElementList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		
		String trayGroupName = SMessageUtil.getBodyItemValue(doc, "TRAYGROUPNAME", true);
		String groupLotGrade = SMessageUtil.getBodyItemValue(doc, "GROUPLOTGRADE", true);
		
		// Search GroupLot Data
		String groupLotName = SMessageUtil.getBodyItemValue(doc, "GROUPLOTNAME", true);
		Lot groupLotData =  MESLotServiceProxy.getLotServiceUtil().getLotData(groupLotName);
		groupLotData.setLotGrade(groupLotGrade);
		// Search Node Data by GroupLot
		Node nextNode = this.getNextNodeData(groupLotData);
		
		// Process Operation Data by GroupLot
		ProcessOperationSpec operationData = MESLotServiceProxy.getLotServiceUtil().getProcessOperationSpecData(groupLotData);
		
		Map<String, Map<String, Lot>> lotDataMap = this.getLotDataMap(groupLotData, lotElementList);
		Map<String, Lot> oldLotDataMap = lotDataMap.get("OLD");
		Map<String, Lot> newLotDataMap = lotDataMap.get("NEW");
		
		Machine machineData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(groupLotData.getMachineName());
		CommonValidation.ChekcMachinState(machineData);
		CommonValidation.checkMachineHold(machineData);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOut", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		// Panel Batch Job
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
		
		// ReworkProduct
		List<ReworkProduct> createList = new ArrayList<ReworkProduct>();
		List<ReworkProduct> updateList = new ArrayList<ReworkProduct>();
		
		for (Element lotElement : lotElementList) 
		{
			String lotName = lotElement.getChildText("LOTNAME");
			String carrierName = lotElement.getChildText("CARRIERNAME");
			String position = lotElement.getChildText("POSITION");
			String portName = lotElement.getChildText("PORTNAME");
			String portType = lotElement.getChildText("PORTTYPE");
			String portUseType  = lotElement.getChildText("PORTUSETYPE");
			String lotGrade = lotElement.getChildText("LOTGRADE");
			String lotDetailGrade = lotElement.getChildText("LOTDETAILGRADE");
			String trackOutTime = lotElement.getChildText("TRACKOUTTIME");
			String reasonCode = lotElement.getChildText("REASONCODE");
			String reasonCodeType = lotElement.getChildText("REASONCODETYPE");
			
			if (!"G".equals(lotGrade))
				lotDetailGrade = "";
			
			if (!StringUtils.isEmpty(trackOutTime))
			{
				eventInfo.setEventTimeKey(trackOutTime);
				eventInfo.setEventTime(TimeStampUtil.getTimestamp(trackOutTime));
			}
			
			Lot oldLotData = oldLotDataMap.get(lotName);
			Lot newLotData = newLotDataMap.get(lotName);
			
			newLotData.setLotGrade(lotGrade);
			
			// for Update Lot
			List<Object> lotBindList = new ArrayList<Object>();
			lotBindList.add(GenericServiceProxy.getConstantMap().Lot_Released);
			lotBindList.add(GenericServiceProxy.getConstantMap().Lot_Wait);
			lotBindList.add(eventInfo.getEventName());
			lotBindList.add(eventInfo.getEventTimeKey());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventComment());
			lotBindList.add(GenericServiceProxy.getConstantMap().Flag_N);
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(nextNode.getNodeAttribute1());
			lotBindList.add(nextNode.getNodeAttribute2());
			lotBindList.add(newLotData.getProcessOperationName());
			lotBindList.add(newLotData.getProcessOperationVersion());
			lotBindList.add(newLotData.getProcessFlowName());
			lotBindList.add(position);
			lotBindList.add(nextNode.getKey().getNodeId());
			lotBindList.add(carrierName);
			lotBindList.add(lotGrade);
			lotBindList.add(lotDetailGrade);
			lotBindList.add(portName);
			lotBindList.add(portType);
			lotBindList.add(portUseType);
			lotBindList.add(reasonCode);
			lotBindList.add(reasonCodeType);
			if(!StringUtils.equals(newLotData.getUdfs().get("JOBDOWNFLAG"), "PFL"))
			{
				lotBindList.add("");
			}
			else
			{
				lotBindList.add(newLotData.getUdfs().get("JOBDOWNFLAG"));
			}
			lotBindList.add(newLotData.getKey().getLotName());
			
			updateLotArgList.add(lotBindList.toArray());
			
			// for Insert LotHistory
			newLotData.setLotState(GenericServiceProxy.getConstantMap().Lot_Released);
			newLotData.setLotProcessState(GenericServiceProxy.getConstantMap().Lot_Wait);
			newLotData.setLastLoggedOutTime(eventInfo.getEventTime());
			newLotData.setLastLoggedOutUser(eventInfo.getEventUser());
			newLotData.setNodeStack(nextNode.getKey().getNodeId());
			newLotData.setLotGrade(lotGrade);
			newLotData.setCarrierName(carrierName);
			newLotData.setReasonCode(reasonCode);
			newLotData.setReasonCodeType(reasonCodeType);
			newLotData.setProcessOperationName(nextNode.getNodeAttribute1());
			newLotData.setProcessOperationVersion(nextNode.getNodeAttribute2());
			newLotData.setLastEventName(eventInfo.getEventName());
			newLotData.setLastEventTime(eventInfo.getEventTime());
			newLotData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			newLotData.setLastEventComment(eventInfo.getEventComment());
			newLotData.setLastEventUser(eventInfo.getEventUser());
			
			Map<String, String> udfs = newLotData.getUdfs();
			udfs.put("BEFOREOPERATIONNAME", oldLotData.getProcessOperationName());
			udfs.put("BEFOREOPERATIONVER", oldLotData.getProcessOperationVersion());
			udfs.put("BEFOREFLOWNAME", newLotData.getProcessFlowName());
			udfs.put("LOTDETAILGRADE", lotDetailGrade);
			udfs.put("PORTNAME", portName);
			udfs.put("PORTTYPE", portType);
			udfs.put("PORTUSETYPE", portUseType);
			udfs.put("POSITION", position);
			if(!StringUtils.equals(newLotData.getUdfs().get("JOBDOWNFLAG"), "PFL"))
			{
				udfs.put("JOBDOWNFLAG", "");
			}
			else
			{
				udfs.put("JOBDOWNFLAG", newLotData.getUdfs().get("JOBDOWNFLAG"));
			}
			newLotData.setUdfs(udfs);
			
			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLotData, newLotData, lotHistory);
			
			updateLotHistoryList.add(lotHistory);
			
			//Modify 2018-11-21 RepairCountCheck
			if ("P".equals(newLotData.getLotGrade()))
			{
				ReworkProduct reworkProductData = ExtendedObjectProxy.getReworkProductService().getReworkProductData(oldLotData.getKey().getLotName(), "RP");

				if (reworkProductData != null)
				{
					if (reworkProductData.getReworkCount() >= 2)
					{
						throw new CustomException("LOT-0100", newLotData.getKey().getLotName(), reworkProductData.getReworkCount());
					}
				}
			}
			
			// Req ID. PC-Tech-0026-01: Panel Process count management
			String detailOperType = operationData.getDetailProcessOperationType();
			Map<String, String> processLimitEnum = ExtendedObjectProxy.getPanelProcessCountService().getProcessLimitConfiguration("ProcessLimit");
			
			if (processLimitEnum != null && StringUtil.in(detailOperType, processLimitEnum.keySet().toArray(new String[]{})))
				ExtendedObjectProxy.getPanelProcessCountService().setPanelProcessCount(eventInfo, oldLotData, detailOperType, processLimitEnum.get(detailOperType));
			
			if ("AVI".equals(detailOperType) && "P".equals(newLotData.getLotGrade()))
			{
				List<PanelProcessCount> ppcDataList = ExtendedObjectProxy.getPanelProcessCountService().getDataListByPanelName(newLotData.getKey().getLotName(), false);
				
				if (ppcDataList != null && ppcDataList.size() > 0)
				{
					for (PanelProcessCount ppcData : ppcDataList)
					{
						if (ppcData.getProcessLimit().intValue() <= ppcData.getProcessCount().intValue())
						{
							newLotData.setLotGrade("S");
							log.info(String.format("Panel [%s|%s] ProcessCount(%s) exceeded the ProcessLimit(%s).", 
									ppcData.getLotName(), ppcData.getDetailProcessOperationType(), ppcData.getProcessCount(), ppcData.getProcessLimit()));
							break;
						}
					}
				}
			}
			
			//Modify 2019-01-02 AgingRework
			if ("D".equals(newLotData.getLotGrade()) && "CT".equals(operationData.getDetailProcessOperationType()))
			{
				String searchDetailOperationType = operationData.getDetailProcessOperationType() + "_AGING";
				ReworkProduct reworkProductData = ExtendedObjectProxy.getReworkProductService().getReworkProductData(oldLotData.getKey().getLotName(), searchDetailOperationType);

				ReworkProduct reworkData = new ReworkProduct();

				if (reworkProductData != null)
				{
					throw new CustomException("LOT-0100", newLotData.getKey().getLotName(), reworkProductData.getReworkCount());
				}
				else
				{
					reworkData = setReworkCountData(oldLotData.getKey().getLotName(), oldLotData.getFactoryName(), oldLotData.getProcessFlowName(), oldLotData.getProcessFlowVersion(),
							oldLotData.getProcessOperationName(), oldLotData.getProcessOperationVersion(), searchDetailOperationType, 1);
					createList.add(reworkData);
				}
			}
			
			if (("R".equals(newLotData.getLotGrade()) && !"CT".equals(operationData.getDetailProcessOperationType())) || "RP".equals(operationData.getDetailProcessOperationType()))
			{
				String searchDetailOperationType = operationData.getDetailProcessOperationType();
				ReworkProduct reworkProductData = ExtendedObjectProxy.getReworkProductService().getReworkProductData(oldLotData.getKey().getLotName(), searchDetailOperationType);
				
				ReworkProduct reworkData = new ReworkProduct();

				if (reworkProductData!=null)
				{
					if (reworkProductData.getReworkCount() >= 2)
					{
						throw new CustomException("LOT-0100", newLotData.getKey().getLotName(), reworkProductData.getReworkCount());
					}
					else
					{
						reworkData = setReworkCountData(oldLotData.getKey().getLotName(), oldLotData.getFactoryName(), oldLotData.getProcessFlowName(), oldLotData.getProcessFlowVersion(),
								oldLotData.getProcessOperationName(), oldLotData.getProcessOperationVersion(), searchDetailOperationType, reworkProductData.getReworkCount() + 1);
						updateList.add(reworkData);
					}
				}
				else
				{
					reworkData = setReworkCountData(oldLotData.getKey().getLotName(), oldLotData.getFactoryName(), oldLotData.getProcessFlowName(), oldLotData.getProcessFlowVersion(), oldLotData.getProcessOperationName(), oldLotData.getProcessOperationVersion(), searchDetailOperationType, 1);
					createList.add(reworkData);
				}
			}
		}
		
		updateLotData(eventInfo, updateLotArgList, updateLotHistoryList);
		
		if (createList.size() > 0)
			ExtendedObjectProxy.getReworkProductService().insert(createList);
		
		if (updateList.size() > 0)
			ExtendedObjectProxy.getReworkProductService().update(updateList);

		// Tray&TrayGroup Assign Batch Job
		assignTrayAndTrayGroup(eventInfo, trayGroupName, lotElementList);

		// Scrap
		/*if (("N".equals(groupLotGrade) || "S".equals(groupLotGrade)) && !"MVI".equals(operationData.getDetailProcessOperationType()) && !"CUT".equals(operationData.getDetailProcessOperationType()))
		{
			panelScrap(groupLotData, newLotDataMap);
		}*/

		EventInfo futureActionEventInfo = (EventInfo)ObjectUtil.copyTo(eventInfo);
		futureActionEventInfo.setEventName("Delete");
		
		boolean reserveFlag = deleteLotFutureAction(futureActionEventInfo, groupLotData, lotElementList, nextNode.getNodeAttribute1(), nextNode.getNodeAttribute2());	
		
		if(reserveFlag)
		{
			EventInfo eventInfoHold = EventInfoUtil.makeEventInfo("ReserveHold", getEventUser(), getEventComment(), null, null);
			eventInfoHold.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoHold.setEventTime(TimeStampUtil.getCurrentTimestamp());
			
			MESLotServiceProxy.getLotServiceUtil().PanelHoldByTrayGroup(eventInfoHold, trayGroupName);
		}
		
		//TrackOut Report for SAP////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(groupLotData.getProductRequestName());
		
		if(StringUtils.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
		{
			SuperProductRequest superWO = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
			
			MESConsumableServiceProxy.getConsumableServiceUtil().trackOutERPBOMReport(eventInfo, groupLotData, superWO, groupLotData.getMachineName(), lotElementList.size());
		}
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		
		return doc;
	}
	
	private ReworkProduct setReworkCountData(String productName, String factoryName ,String processFlowName, String processFlowVer, String processOperName, String processOperVer ,String reworkType, long reworkCount)
	{
		ReworkProduct reworkData = new ReworkProduct();
		
		reworkData.setProductName(productName);
		reworkData.setFactoryName(factoryName);
		reworkData.setProcessFlowName(processFlowName);
		reworkData.setProcessFlowVersion(processFlowVer);
		reworkData.setProcessOperationName(processOperName);
		reworkData.setProcessOperationVersion(processOperVer);
		reworkData.setReworkType(reworkType);
		reworkData.setReworkCount(reworkCount);
		
		return reworkData;
	}
	
	private Map<String, Map<String, Lot>> getLotDataMap(Lot groupLotData, List<Element> lotElementList) throws CustomException
	{
		String condition = "WHERE LOTNAME IN(";
		for (Element lotElement : lotElementList) 
		{
			String lotName = lotElement.getChildText("LOTNAME");
			
			condition += "'" + lotName + "',";
		}
		condition = condition.substring(0, condition.length() - 1) + ")";
		
		List<Lot> lotDataList = LotServiceProxy.getLotService().select(condition, new Object[] { });
		
		Map<String, Lot> oldLotDataMap = new HashMap<String, Lot>();
		Map<String, Lot> newLotDataMap = new HashMap<String, Lot>();
		
		for (Lot lotData : lotDataList) 
		{
			CommonValidation.checkLotProcessStateRun(lotData);
			
			if (!lotData.getFactoryName().equals(groupLotData.getFactoryName()) ||
				!lotData.getProductSpecName().equals(groupLotData.getProductSpecName()) ||
				!lotData.getProductSpecVersion().equals(groupLotData.getProductSpecVersion()) ||
				!lotData.getProcessFlowName().equals(groupLotData.getProcessFlowName()) ||
				!lotData.getProcessFlowVersion().equals(groupLotData.getProcessFlowVersion()) ||
				!lotData.getProcessOperationName().equals(groupLotData.getProcessOperationName()) ||
				!lotData.getProcessOperationVersion().equals(groupLotData.getProcessOperationVersion()) ||
				!lotData.getProductionType().equals(groupLotData.getProductionType()) ||
				!lotData.getProductRequestName().equals(groupLotData.getProductRequestName()))
			{
				throw new CustomException("LOT-0020", lotData.getKey().getLotName(), groupLotData.getKey().getLotName());
			}
						
			oldLotDataMap.put(lotData.getKey().getLotName(), (Lot)ObjectUtil.copyTo(lotData));
			newLotDataMap.put(lotData.getKey().getLotName(), lotData);
		}
		
		Map<String, Map<String, Lot>> lotDataMap = new HashMap<String, Map<String, Lot>>();
		lotDataMap.put("OLD", oldLotDataMap);
		lotDataMap.put("NEW", newLotDataMap);
		
		return lotDataMap;
	}
	
	private Map<String, Map<String, Durable>> getDurableDataMap(String trayGroupName, List<Element> lotElementList) throws CustomException
	{
		String condition = "WHERE DURABLENAME IN(";
		for (Element lotElement : lotElementList) 
		{
			String carrierName = lotElement.getChildText("CARRIERNAME");
			
			condition += "'" + carrierName + "',";
		}
		condition = condition.substring(0, condition.length() - 1) + ")";
		
		List<Durable> durableDataList = DurableServiceProxy.getDurableService().select(condition, new Object[] { });
		
		Map<String, Durable> oldTrayDataMap = new HashMap<String, Durable>();
		Map<String, Durable> newTrayDataMap = new HashMap<String, Durable>();
		
		for (Durable durableData : durableDataList) 
		{
			CommonValidation.CheckDurableHoldState(durableData);
			//CommonValidation.CheckDurableCleanState(durableData);
			CommonValidation.checkAvailableCst(durableData);
			
			oldTrayDataMap.put(durableData.getKey().getDurableName(), (Durable)ObjectUtil.copyTo(durableData));
			
			durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
			newTrayDataMap.put(durableData.getKey().getDurableName(), durableData);
		}
		
		Map<String, Durable> trayDataMapForTrayGoup = new HashMap<String, Durable>();
		
		for (Element lotElement : lotElementList)
		{
			String carrierName = lotElement.getChildText("CARRIERNAME");
			String trayPosition = lotElement.getChildText("TRAYPOSITION");
			
			Durable trayData = (Durable)ObjectUtil.copyTo(oldTrayDataMap.get(carrierName));

			trayData.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
			
			Map<String, String> udfs = trayData.getUdfs();
			udfs.put("POSITION", trayPosition);
			udfs.put("COVERNAME", trayGroupName);
			udfs.put("DURABLETYPE1", trayData.getKey().getDurableName().equals(trayGroupName) ? "CoverTray" : "Tray");
			
			trayData.setUdfs(udfs);
			
			trayDataMapForTrayGoup.put(trayData.getKey().getDurableName(), trayData);
		}
		
		Map<String, Map<String, Durable>> durableDataMap = new HashMap<String, Map<String, Durable>>();
		durableDataMap.put("OLD", oldTrayDataMap);
		durableDataMap.put("NEW", newTrayDataMap);
		durableDataMap.put("FORGROUP", trayDataMapForTrayGoup);
		
		return durableDataMap;
	}
	
	@SuppressWarnings("rawtypes")
	private Node getNextNodeData(Lot LotData)
	{
		ProcessFlowKey processFlowKey = new ProcessFlowKey();
		processFlowKey.setFactoryName(LotData.getFactoryName());
		processFlowKey.setProcessFlowName(LotData.getProcessFlowName());
		processFlowKey.setProcessFlowVersion(LotData.getProcessFlowVersion());
		ProcessFlow processFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
		
		kr.co.aim.greentrack.processflow.management.iter.NodeStack nodeStack = NodeStackUtil.stringToNodeStack(LotData.getNodeStack());
		ProcessFlowIterator pfi = new ProcessFlowIterator(processFlow, nodeStack, "");
		
		ProcessGroup oldProcGroup = null;
		try {
			oldProcGroup = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(LotData.getProcessGroupName());
		} catch (FrameworkErrorSignal | NotFoundSignal | CustomException e) {
			// TODO Auto-generated catch block
			
		}
		
		PFIValueSetter valueSetter = new LotPFIValueSetter(pfi, LotData, LotData);
		pfi.moveNext("N", valueSetter);

		return pfi.getCurrentNodeData();
	}
	
	private void assignTrayAndTrayGroup(EventInfo eventInfo, String trayGroupName, List<Element> lotElementList) throws CustomException
	{
		Map<String, Map<String, Durable>> durableDataMap = this.getDurableDataMap(trayGroupName, lotElementList);
		Map<String, Durable> oldTrayDataMap = durableDataMap.get("OLD");
		Map<String, Durable> newTrayDataMap = durableDataMap.get("NEW");
		Map<String, Durable> trayDataMapForTrayGroup = durableDataMap.get("FORGROUP");
		
		// Tray Assign
		eventInfo.setEventName("Assign");
		
		// Tray Batch Job
		List<Object[]> updateDurArgList = new ArrayList<Object[]>();
		
		for (String trayName : newTrayDataMap.keySet()) 
		{
			Durable newTrayData = newTrayDataMap.get(trayName);
			Durable trayDataForTrayGroup = trayDataMapForTrayGroup.get(trayName);
			
			List<Lot> lotDataList = MESLotServiceProxy.getLotInfoUtil().getLotListBydurableName(newTrayData.getKey().getDurableName());
			int lotQuantity = lotDataList.size();
			
			newTrayData.setLotQuantity(lotQuantity);
			newTrayData.setLastEventName(eventInfo.getEventName());
			newTrayData.setLastEventTime(eventInfo.getEventTime());
			newTrayData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			newTrayData.setLastEventUser(eventInfo.getEventUser());
			newTrayData.setLastEventComment(eventInfo.getEventComment());
			
			trayDataForTrayGroup.setLotQuantity(lotQuantity);
			trayDataMapForTrayGroup.put(trayName, trayDataForTrayGroup);
			
			List<Object> durBindList = new ArrayList<Object>();
			durBindList.add(GenericServiceProxy.getConstantMap().Dur_InUse);
			durBindList.add(lotQuantity);
			durBindList.add(eventInfo.getEventName());
			durBindList.add(eventInfo.getEventTimeKey());
			durBindList.add(eventInfo.getEventTime());
			durBindList.add(eventInfo.getEventUser());
			durBindList.add(eventInfo.getEventComment());
			durBindList.add("Tray");
			durBindList.add(newTrayData.getKey().getDurableName());
			
			updateDurArgList.add(durBindList.toArray());
		}
		
		String trayBatchSql = "UPDATE DURABLE "
							+ "SET DURABLESTATE = ?, LOTQUANTITY = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
							+ "    LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, DURABLETYPE1 = ? "
							+ "WHERE DURABLENAME = ? ";
		
		MESLotServiceProxy.getLotServiceUtil().updateBatch(trayBatchSql, updateDurArgList);
		MESDurableServiceProxy.getDurableServiceUtil().insertDurableHistory(eventInfo, newTrayDataMap, oldTrayDataMap); 
		
		// TrayGroup Assign
		eventInfo.setEventName("AssignTrayGroup");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		// TrayGroup Batch Job
		List<Object[]> updateDurArgList2 = new ArrayList<Object[]>();
		
		int maxPosition = 0;
		for (Durable trayData : trayDataMapForTrayGroup.values()) 
		{
			trayData.setLastEventName(eventInfo.getEventName());
			trayData.setLastEventTime(eventInfo.getEventTime());
			trayData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			trayData.setLastEventUser(eventInfo.getEventUser());
			trayData.setLastEventComment(eventInfo.getEventComment());
			
			if(maxPosition < Integer.parseInt(trayData.getUdfs().get("POSITION")))
			{
				maxPosition = Integer.parseInt(trayData.getUdfs().get("POSITION"));
			}
			
			List<Object> durBindList = new ArrayList<Object>();
			durBindList.add(trayData.getUdfs().get("POSITION"));
			durBindList.add(trayData.getUdfs().get("COVERNAME"));
			durBindList.add(eventInfo.getEventName());
			durBindList.add(eventInfo.getEventTimeKey());
			durBindList.add(eventInfo.getEventTime());
			durBindList.add(eventInfo.getEventUser());
			durBindList.add(eventInfo.getEventComment());
			durBindList.add("Tray");
			durBindList.add(trayData.getKey().getDurableName());
			
			updateDurArgList2.add(durBindList.toArray());
		}
		
		String trayGroupBatchSql = "UPDATE DURABLE "
								 + "SET POSITION = ?, COVERNAME = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, "
								 + "    LASTEVENTTIME = ?, LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, DURABLETYPE1 = ? "
								 + "WHERE DURABLENAME = ? ";
		
		MESLotServiceProxy.getLotServiceUtil().updateBatch(trayGroupBatchSql, updateDurArgList2);
		MESDurableServiceProxy.getDurableServiceUtil().insertDurableHistory(eventInfo, trayDataMapForTrayGroup, newTrayDataMap);
		
		// Update CoverTray
		Durable trayGroup = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayGroupName);
		Durable oldTrayGroup = trayGroup;
		
		trayGroup.setLotQuantity(lotElementList.size());
		trayGroup.setDurableType("CoverTray");
		trayGroup.setDurableState(GenericServiceProxy.getConstantMap().Dur_InUse);
		trayGroup.setLastEventName(eventInfo.getEventName());
		trayGroup.setLastEventTimeKey(eventInfo.getEventTimeKey());
		trayGroup.setLastEventTime(eventInfo.getEventTime());
		trayGroup.setLastEventUser(eventInfo.getEventUser());
		trayGroup.setLastEventComment(eventInfo.getEventComment());
		
		Map<String, String> trayGroupUdfs = trayGroup.getUdfs();
		trayGroupUdfs.put("COVERNAME", trayGroupName);
		trayGroupUdfs.put("POSITION", Integer.toString(maxPosition + 1));
		trayGroupUdfs.put("DURABLETYPE1", "CoverTray");
		trayGroup.setUdfs(trayGroupUdfs);
		
		DurableHistory durHistory = new DurableHistory();
		durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldTrayGroup, trayGroup, durHistory);
		
		DurableServiceProxy.getDurableService().update(trayGroup);
		DurableServiceProxy.getDurableHistoryService().insert(durHistory);
	}
	
	private void panelScrap(Lot groupLotData, Map<String, Lot> lotDataMap) throws CustomException
	{
		EventInfo eventInfoScrap = EventInfoUtil.makeEventInfo("Scrap", getEventUser(), getEventComment(), null, null);//2020/12/8 caixu ScrapLot Modify Scrap
		eventInfoScrap.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfoScrap.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		// Panel Batch Job
		List<Object[]> updateLotArgListScrap = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryListScrap = new ArrayList<LotHistory>();
		
		for (Lot lotData : lotDataMap.values()) 
		{
			Lot oldLotData = (Lot)ObjectUtil.copyTo(lotData);
					
			// for Update Lot
			List<Object> lotBindList = new ArrayList<Object>();
			lotBindList.add(GenericServiceProxy.getConstantMap().Lot_Scrapped);
			lotBindList.add(eventInfoScrap.getEventName());
			lotBindList.add(eventInfoScrap.getEventTimeKey());
			lotBindList.add(eventInfoScrap.getEventTime());
			lotBindList.add(eventInfoScrap.getEventUser());
			lotBindList.add(eventInfoScrap.getEventComment());
			lotBindList.add(GenericServiceProxy.getConstantMap().Flag_N);
			lotBindList.add("Auto Scrap");
			lotBindList.add("S");
			lotBindList.add(lotData.getKey().getLotName());
			
			updateLotArgListScrap.add(lotBindList.toArray());
			
			// for Insert LotHistory
			lotData.setLotState(GenericServiceProxy.getConstantMap().Lot_Scrapped);
			lotData.setReasonCode("Auto Scrap");
			lotData.setLotGrade("S");
			lotData.setLastEventName(eventInfoScrap.getEventName());
			lotData.setLastEventTime(eventInfoScrap.getEventTime());
			lotData.setLastEventTimeKey(eventInfoScrap.getEventTimeKey());
			lotData.setLastEventComment(eventInfoScrap.getEventComment());
			lotData.setLastEventUser(eventInfoScrap.getEventUser());

			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLotData, lotData, lotHistory);
			
			updateLotHistoryListScrap.add(lotHistory);
		}
		
		//SQL
		String scrapLotBatchSql = "UPDATE LOT "
								+ "SET LOTSTATE = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
								+ "    LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LASTEVENTFLAG = ?, REASONCODE = ?, LOTGRADE = ? "
								+ "WHERE LOTNAME = ? ";
		
		MESLotServiceProxy.getLotServiceUtil().updateBatch(scrapLotBatchSql, updateLotArgListScrap);
		try 
		{
			CommonUtil.executeBatch("insert", updateLotHistoryListScrap);
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}
		
		// ChangeWorkOrder Scrap
		ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfoScrap, groupLotData.getProductRequestName(), updateLotArgListScrap.size(), 0);

		/*if (newProductRequestData.getPlanQuantity() == newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity())
		{
			EventInfo newEventInfo = eventInfoScrap;
			newEventInfo.setEventName("Complete");
			MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(newEventInfo, groupLotData.getProductRequestName());
		}*///屏蔽自动Complete的功能	
	}
	
	private boolean deleteLotFutureAction(EventInfo eventInfo, Lot lotData, List<Element> lotElementList, String processOperationName, String processOperationVersion) throws CustomException
	{
		String condition = "WHERE 1 = 1 "
						 + "  AND FACTORYNAME = ? "
						 + "  AND PROCESSFLOWNAME = ? "
						 + "  AND PROCESSFLOWVERSION = ? "
						 + "  AND PROCESSOPERATIONNAME = ? "
						 + "  AND PROCESSOPERATIONVERSION = ? "
						 + "  AND LOTNAME IN(";
		for (Element lotElement : lotElementList) 
		{
			String lotName = lotElement.getChildText("LOTNAME");
			
			condition += "'" + lotName + "',";
		}
		condition = condition.substring(0, condition.length() - 1) + ")";
		
		Object[] bindSet = new Object[] { lotData.getFactoryName(), lotData.getProcessFlowName(), lotData.getProcessFlowVersion(), processOperationName, processOperationVersion };

		try
		{
			List<LotFutureAction> dataInfoList = ExtendedObjectProxy.getLotFutureActionService().select(condition, bindSet);
			if (dataInfoList != null)
			{
				eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

				for (LotFutureAction dataInfo : dataInfoList)
				{
					ExtendedObjectProxy.getLotFutureActionService().remove(eventInfo, dataInfo);
				}
			}
			
			return true;
		}
		catch (Exception ex)
		{
			return false;
		}
	}
	
	private void updateLotData(EventInfo eventInfo, List<Object[]> updateLotArgList, List<LotHistory> updateLotHistoryList) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE LOT ");
		sql.append("   SET LOTSTATE = ?, ");
		sql.append("       LOTPROCESSSTATE = ?, ");
		sql.append("       LASTEVENTNAME = ?, ");
		sql.append("       LASTEVENTTIMEKEY = ?, ");
		sql.append("       LASTEVENTTIME = ?, ");
		sql.append("       LASTEVENTUSER = ?, ");
		sql.append("       LASTEVENTCOMMENT = ?, ");
		sql.append("       LASTEVENTFLAG = ?, ");
		sql.append("       LASTLOGGEDOUTTIME = ?, ");
		sql.append("       LASTLOGGEDOUTUSER = ?, ");
		sql.append("       PROCESSOPERATIONNAME = ?, ");
		sql.append("       PROCESSOPERATIONVERSION = ?, ");
		sql.append("       BEFOREOPERATIONNAME = ?, ");
		sql.append("       BEFOREOPERATIONVER = ?, ");
		sql.append("       BEFOREFLOWNAME = ?, ");
		sql.append("       POSITION = ?, ");
		sql.append("       NODESTACK = ?, ");
		sql.append("       CARRIERNAME = ?, ");
		sql.append("       LOTGRADE = ?, ");
		sql.append("       LOTDETAILGRADE = ?, ");
		sql.append("       PORTNAME = ?, ");
		sql.append("       PORTTYPE = ?, ");
		sql.append("       PORTUSETYPE = ?, ");
		sql.append("       REASONCODE = ?, ");
		sql.append("       REASONCODETYPE = ?, ");
		sql.append("       JOBDOWNFLAG = ? ");
		sql.append(" WHERE LOTNAME = ? ");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateLotArgList);
		try 
		{
			CommonUtil.executeBatch("insert", updateLotHistoryList);
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}

	}
}
