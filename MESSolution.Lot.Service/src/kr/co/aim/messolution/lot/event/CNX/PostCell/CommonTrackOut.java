package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.PanelProcessCount;
import kr.co.aim.messolution.extended.object.management.data.ReworkProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
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
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class CommonTrackOut extends SyncHandler {
	
	Log log = LogFactory.getLog(this.getClass());

	@Override
	public Object doWorks(Document doc)
		throws CustomException
	{
		//Get Doc
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", true);
		String trayName = SMessageUtil.getBodyItemValue(doc, "DURABLENAME", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String portType = SMessageUtil.getBodyItemValue(doc, "PORTTYPE", true);
		String portUseType = SMessageUtil.getBodyItemValue(doc, "PORTUSETYPE", false);
		String detailOperationType = SMessageUtil.getBodyItemValue(doc, "DETAILOPERATIONTYPE", true);
		String coverFlag = SMessageUtil.getBodyItemValue(doc, "COVERFLAG", false);//caixu 2021/2/20 ADD coverFlag

		int panelQty = lotList.size();
		
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		//EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("TrackOut", getEventUser(), getEventComment(), null, null);
		String timeKey = TimeUtils.getCurrentEventTimeKey();
		eventInfo.setEventTimeKey(timeKey);
			eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		
		EventInfo futureActionEventInfo = (EventInfo)ObjectUtil.copyTo(eventInfo);
		futureActionEventInfo.setEventName("Delete");
		
		String queryStringLot = "UPDATE LOT SET LOTSTATE = ?, LOTPROCESSSTATE = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
				+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LASTEVENTFLAG = ?, LASTLOGGEDOUTTIME = ?, LASTLOGGEDOUTUSER = ?, "
				+ "PROCESSOPERATIONNAME = ?, PROCESSOPERATIONVERSION = ?, BEFOREOPERATIONNAME = ?, BEFOREOPERATIONVER = ?, BEFOREFLOWNAME = ?, POSITION = ?, NODESTACK = ?, CARRIERNAME = ?, LOTGRADE = ?, LOTDETAILGRADE= ?, PORTNAME = ?, PORTTYPE = ?, PORTUSETYPE = ?, JOBDOWNFLAG=? WHERE LOTNAME = ?";	
		
		//Make Panel
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
		
		List<ReworkProduct> createList = new ArrayList<ReworkProduct>();
		List<ReworkProduct> updateList = new ArrayList<ReworkProduct>();
		
		boolean reserveFlag = false;
		
		//For SAP Data
		Lot oldLotforSAP = MESLotServiceProxy.getLotServiceUtil().getLotData(lotList.get(0).getChildText("LOTNAME"));
		
		for (Element lotData : lotList)
		{
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);
			
			Machine eqpData = MESMachineServiceProxy.getMachineInfoUtil().getMachineData(lot.getMachineName());
			CommonValidation.ChekcMachinState(eqpData);
			CommonValidation.checkMachineHold(eqpData);
			
			CommonValidation.checkLotProcessStateRun(lot);
			
			String position = lotData.getChildText("POSITION");
			String lotGrade = lotData.getChildText("LOTGRADE");
			String lotDetailGrade = lotData.getChildText("LOTDETAILGRADE");
			
			if(!lotGrade.equals("G"))
			{
				lotDetailGrade = "";
			}
			
			//Node nextNode = new Node();
			lot.setLotGrade(lotGrade);
			
			ProcessFlowKey processFlowKey = new ProcessFlowKey();

			processFlowKey.setFactoryName(lot.getFactoryName());
			processFlowKey.setProcessFlowName(lot.getProcessFlowName());
			processFlowKey.setProcessFlowVersion(lot.getProcessFlowVersion());

			ProcessFlow processFlow = ProcessFlowServiceProxy.getProcessFlowService().selectByKey(processFlowKey);
			
			kr.co.aim.greentrack.processflow.management.iter.NodeStack nodeStack = NodeStackUtil.stringToNodeStack(lot.getNodeStack());
			ProcessFlowIterator pfi = new ProcessFlowIterator(processFlow, nodeStack, "");
			
			PFIValueSetter valueSetter = new LotPFIValueSetter(pfi, oldLot, lot);
			pfi.moveNext("N", valueSetter);

			// 1.3. Set ProcessFlow Iterator Related Data
			Node nextNode = pfi.getCurrentNodeData();
		
			boolean reserveCheck = MESLotServiceProxy.getLotServiceUtil().PostCellDeleteLotFutureAction(futureActionEventInfo, lot, nextNode.getNodeAttribute1(), nextNode.getNodeAttribute2());
			
			if(reserveCheck)
			{
				reserveFlag = true;
			}
			
			List<Object> lotBindList = new ArrayList<Object>();
			
			lotBindList.add(constantMap.Lot_Released);
			lotBindList.add(constantMap.Lot_Wait);
			lotBindList.add(eventInfo.getEventName());
			lotBindList.add(eventInfo.getEventTimeKey());
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventComment());
			lotBindList.add(constantMap.Flag_N);
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(nextNode.getNodeAttribute1());
			lotBindList.add(nextNode.getNodeAttribute2());
			lotBindList.add(lot.getProcessOperationName());
			lotBindList.add(lot.getProcessOperationVersion());
			lotBindList.add(lot.getProcessFlowName());
			lotBindList.add(position);
			lotBindList.add(nextNode.getKey().getNodeId());
			lotBindList.add(trayName);
			lotBindList.add(lotGrade);
			lotBindList.add(lotDetailGrade);
			lotBindList.add(portName);
			lotBindList.add(portType);
			lotBindList.add(portUseType);
			if(!StringUtils.equals(lot.getUdfs().get("JOBDOWNFLAG"), "PFL"))
			{
				lotBindList.add("");
			}
			else
			{
				lotBindList.add(lot.getUdfs().get("JOBDOWNFLAG"));
			}
			lotBindList.add(lot.getKey().getLotName());
			
			updateLotArgList.add(lotBindList.toArray());
			
			//History	
			lot.setLotState(constantMap.Lot_Released);
			lot.setLotProcessState(constantMap.Lot_Wait);
			lot.setLastLoggedOutTime(eventInfo.getEventTime());
			lot.setLastLoggedOutUser(eventInfo.getEventUser());
			lot.setNodeStack(nextNode.getKey().getNodeId());
			lot.setLotGrade(lotGrade);
			lot.setCarrierName(trayName);
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());
			
			Map<String, String> lotUdf = new HashMap<>();
			lotUdf = lot.getUdfs();
			lotUdf.put("BEFOREOPERATIONNAME", lot.getProcessOperationName());
			lotUdf.put("BEFOREOPERATIONVER", lot.getProcessOperationVersion());
			lotUdf.put("BEFOREFLOWNAME", lot.getProcessFlowName());
			lotUdf.put("LOTDETAILGRADE", lotDetailGrade);
			lotUdf.put("PORTNAME", portName);
			lotUdf.put("PORTTYPE", portType);
			lotUdf.put("PORTUSETYPE", portUseType);
			lotUdf.put("POSITION", position);
			if(!StringUtils.equals(lot.getUdfs().get("JOBDOWNFLAG"), "PFL"))
			{
				lotUdf.put("JOBDOWNFLAG", "");
			}
			else
			{
				lotUdf.put("JOBDOWNFLAG", lot.getUdfs().get("JOBDOWNFLAG"));
			}
			lot.setUdfs(lotUdf);
			lot.setProcessOperationName(nextNode.getNodeAttribute1());
			lot.setProcessOperationVersion(nextNode.getNodeAttribute2());
			
			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
			
			updateLotHistoryList.add(lotHistory);
			
			//Modify 2018-11-21 RepairCountCheck //2020/11/3 CAIXU 前期取消该功能
			/*if(lot.getLotGrade().equals("P"))
			{
				List<Map<String, Object>> reworkCountData = getReworkCountData(oldLot.getKey().getLotName(), "RP");

				if(reworkCountData.size() > 0)
				{
					if(Long.parseLong(ConvertUtil.getMapValueByName(reworkCountData.get(0), "REWORKCOUNT")) >= 2)
					{
						throw new CustomException("LOT-0100", lot.getKey().getLotName(), Long.parseLong(ConvertUtil.getMapValueByName(reworkCountData.get(0), "REWORKCOUNT")));
					}
				}
			}*/
			
			// Req ID. PC-Tech-0026-01: Panel Process count management
			String detailOperType = MESLotServiceProxy.getLotServiceUtil().getProcessOperationSpecData(oldLot).getDetailProcessOperationType();
			Map<String,String> processLimitEnum = ExtendedObjectProxy.getPanelProcessCountService().getProcessLimitConfiguration("ProcessLimit");
			
			if (processLimitEnum != null && StringUtil.in(detailOperType, processLimitEnum.keySet().toArray(new String[]{})))
				ExtendedObjectProxy.getPanelProcessCountService().setPanelProcessCount(eventInfo, oldLot, detailOperType,processLimitEnum.get(detailOperType));
			
			if("AVI".equals(detailOperType)&& lot.getLotGrade().equals("P"))
			{
				List<PanelProcessCount> ppcDataList = ExtendedObjectProxy.getPanelProcessCountService().getDataListByPanelName(lot.getKey().getLotName(), false);
				
				if(ppcDataList!=null && ppcDataList.size()>0)
				{
					for(PanelProcessCount ppcData : ppcDataList)
					{
						if(ppcData.getProcessLimit().intValue() <= ppcData.getProcessCount().intValue())
						{
							lot.setLotGrade("S");
							log.info(String.format("▶ Panel [%s|%s] ProcessCount(%s) exceeded the ProcessLimit(%s).", ppcData.getLotName(),ppcData.getDetailProcessOperationType(),
																									                 ppcData.getProcessCount(),ppcData.getProcessLimit()));
							break;
						}
					} 
				}
			}
			
			//Modify 2019-01-02 AgingRework
			if((lot.getLotGrade().equals("D") && detailOperationType.equals("CT")))
			{
				String searchDetailOperationType = detailOperationType + "_AGING";
				
				List<Map<String, Object>> reworkCountData = getReworkCountData(oldLot.getKey().getLotName(), searchDetailOperationType);
				
				ReworkProduct reworkData = new ReworkProduct();

				if(reworkCountData.size() > 0)
				{
					throw new CustomException("LOT-0100", lot.getKey().getLotName(), Long.parseLong(ConvertUtil.getMapValueByName(reworkCountData.get(0), "REWORKCOUNT")));
				}
				else
				{
					reworkData = setReworkCountData(oldLot.getKey().getLotName(), oldLot.getFactoryName(), oldLot.getProcessFlowName(), oldLot.getProcessFlowVersion(), oldLot.getProcessOperationName(), oldLot.getProcessOperationVersion(), searchDetailOperationType, 1);
					createList.add(reworkData);
				}
			}
			
			//Modify 2018-11-21  2020/11/3 CAIXU 前期取消该功能
			/*if((lot.getLotGrade().equals("R") && !detailOperationType.equals("CT")) || detailOperationType.equals("RP"))
			{
				String searchDetailOperationType = detailOperationType;
				
				List<Map<String, Object>> reworkCountData = getReworkCountData(oldLot.getKey().getLotName(), searchDetailOperationType);
				
				ReworkProduct reworkData = new ReworkProduct();

				if(reworkCountData.size() > 0)
				{
					if(Long.parseLong(ConvertUtil.getMapValueByName(reworkCountData.get(0), "REWORKCOUNT")) >= 2)
					{
						throw new CustomException("LOT-0100", lot.getKey().getLotName(), Long.parseLong(ConvertUtil.getMapValueByName(reworkCountData.get(0), "REWORKCOUNT")));
					}
					else
					{
						reworkData = setReworkCountData(oldLot.getKey().getLotName(), oldLot.getFactoryName(), oldLot.getProcessFlowName(), oldLot.getProcessFlowVersion(), oldLot.getProcessOperationName(), oldLot.getProcessOperationVersion(), searchDetailOperationType, Long.parseLong(reworkCountData.get(0).get("REWORKCOUNT").toString()) + 1);
						updateList.add(reworkData);
					}
				}
				else
				{
					reworkData = setReworkCountData(oldLot.getKey().getLotName(), oldLot.getFactoryName(), oldLot.getProcessFlowName(), oldLot.getProcessFlowVersion(), oldLot.getProcessOperationName(), oldLot.getProcessOperationVersion(), searchDetailOperationType, 1);
					createList.add(reworkData);
				}
			}*/
		}
		MESLotServiceProxy.getLotServiceUtil().updateBatch(queryStringLot, updateLotArgList);
		try 
		{
			CommonUtil.executeBatch("insert", updateLotHistoryList);
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}
		
		//Rework, Repair Count
		if(createList.size() > 0)
		{
			ExtendedObjectProxy.getReworkProductService().insert(createList);
		}
		if(updateList.size() > 0)
		{
			ExtendedObjectProxy.getReworkProductService().update(updateList);
		}
		
		// Durable
		if(coverFlag.equals("Y"))
		{
			Durable olddurableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
			Durable durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
			CommonValidation.checkAvailableCst(durableInfo);

			durableInfo.setLotQuantity(panelQty);
			durableInfo.setDurableState(constantMap.Dur_InUse);
			durableInfo.setLastEventName(eventInfo.getEventName());
			durableInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
			durableInfo.setLastEventTime(eventInfo.getEventTime());
			durableInfo.setLastEventUser(eventInfo.getEventUser());
			durableInfo.setLastEventComment(eventInfo.getEventComment());
			durableInfo.getUdfs().put("DURABLETYPE1", "CoverTray");
			durableInfo.getUdfs().put("COVERNAME", trayName);
			durableInfo.setDurableType("CoverTray");


			DurableHistory durHistory = new DurableHistory();
			durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfo, durableInfo, durHistory);

			DurableServiceProxy.getDurableService().update(durableInfo);
			DurableServiceProxy.getDurableHistoryService().insert(durHistory);
			
			
			
		}
		else
		{
			Durable olddurableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
			Durable durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
			CommonValidation.checkAvailableCst(durableInfo);

			durableInfo.setLotQuantity(panelQty);
			durableInfo.setDurableState(constantMap.Dur_InUse);
			durableInfo.setLastEventName(eventInfo.getEventName());
			durableInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
			durableInfo.setLastEventTime(eventInfo.getEventTime());
			durableInfo.setLastEventUser(eventInfo.getEventUser());
			durableInfo.setLastEventComment(eventInfo.getEventComment());
			DurableHistory durHistory = new DurableHistory();
			durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfo, durableInfo, durHistory);

			DurableServiceProxy.getDurableService().update(durableInfo);
			DurableServiceProxy.getDurableHistoryService().insert(durHistory);
			
			
			
			
			
		}
		//Scrap //LotGrade is N not Scrap and LotGrade is s detailOperationType is not Scrapped
		if ((lotList.get(0).getChildText("LOTGRADE").toString().equals("S")) && (!detailOperationType.equals("CT")))
		{
			EventInfo eventInfoScrap = EventInfoUtil.makeEventInfo("Scrap", getEventUser(), getEventComment(), null, null);//2020/12/8 caixu ScrapLotModifyScrap		
			eventInfoScrap.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoScrap.setEventTime(TimeStampUtil.getCurrentTimestamp());


			// SQL
			String queryStringLotScrap = "UPDATE LOT SET LOTSTATE = ?, LASTEVENTNAME = ?, LASTEVENTTIMEKEY = ?, LASTEVENTTIME = ?, "
					+ "LASTEVENTUSER = ?, LASTEVENTCOMMENT = ?, LASTEVENTFLAG = ?, REASONCODE = ?, LOTGRADE = ? WHERE LOTNAME = ?";

			List<Object[]> updateLotArgListScrap = new ArrayList<Object[]>();
			List<LotHistory> updateLotHistoryListScrap = new ArrayList<LotHistory>();
			
			String WO = "";

			for (Element lotData : lotList)
			{
				Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
				Lot oldLot = (Lot)ObjectUtil.copyTo(lot);
				
				if(WO.isEmpty())
				{
					WO = lot.getProductRequestName();
				}

				List<Object> lotBindList = new ArrayList<Object>();

				lotBindList.add(constantMap.Lot_Scrapped);
				lotBindList.add(eventInfoScrap.getEventName());
				lotBindList.add(eventInfoScrap.getEventTimeKey());
				lotBindList.add(eventInfoScrap.getEventTime());
				lotBindList.add(eventInfoScrap.getEventUser());
				lotBindList.add(eventInfoScrap.getEventComment());
				lotBindList.add(constantMap.Flag_N);
				lotBindList.add("Auto Scrap");
				lotBindList.add("S");
				lotBindList.add(lotData.getChildText("LOTNAME"));

				updateLotArgListScrap.add(lotBindList.toArray());

				// History
				lot.setLotState(constantMap.Lot_Scrapped);
				lot.setReasonCode("Auto Scrap");
				lot.setLotGrade("S");
				lot.setLastEventName(eventInfoScrap.getEventName());
				lot.setLastEventTime(eventInfoScrap.getEventTime());
				lot.setLastEventTimeKey(eventInfoScrap.getEventTimeKey());
				lot.setLastEventComment(eventInfoScrap.getEventComment());
				lot.setLastEventUser(eventInfoScrap.getEventUser());

				LotHistory lotHistory = new LotHistory();
				lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
				
				updateLotHistoryListScrap.add(lotHistory);
			}

			MESLotServiceProxy.getLotServiceUtil().updateBatch(queryStringLotScrap, updateLotArgListScrap);
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

			ProductRequest newProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceImpl().ChangeScrapQty(eventInfoScrap, WO, updateLotArgListScrap.size(), 0);

			/*if (newProductRequestData.getPlanQuantity() == newProductRequestData.getFinishedQuantity() + newProductRequestData.getScrappedQuantity())
			{
				EventInfo newEventInfo = eventInfoScrap;
				newEventInfo.setEventName("Complete");
				MESWorkOrderServiceProxy.getProductRequestServiceImpl().makeCompleted(newEventInfo, WO);
			}*///
		}
		
		if(reserveFlag)
		{
			EventInfo eventInfoHold = EventInfoUtil.makeEventInfo("ReserveHold", getEventUser(), getEventComment(), null, null);
			eventInfoHold.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoHold.setEventTime(TimeStampUtil.getCurrentTimestamp());
			
			MESLotServiceProxy.getLotServiceUtil().PanelHoldByTray(eventInfoHold, trayName);
		}
		
		//TrackOut Report for SAP////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/*trackOutERPBOMReport功能未发布，暂时屏蔽
		 * ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(oldLotforSAP.getProductRequestName());
		
		if(StringUtils.isNotEmpty(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
		{
			SuperProductRequest superWO = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
			
			MESConsumableServiceProxy.getConsumableServiceUtil().trackOutERPBOMReport(eventInfo, oldLotforSAP, superWO, oldLotforSAP.getMachineName(), panelQty);
		}*/
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		

		return doc;
	}
	/*
	private List<Map<String, Object>> getFQCOperation(String factoryname, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion)
	{
		String sql = "SELECT TP.PROCESSOPERATIONNAME, " +
				"       TP.PROCESSOPERATIONVERSION, " +
				"       PS.DETAILPROCESSOPERATIONTYPE " +
				"  FROM TPFOPOLICY TP, PROCESSOPERATIONSPEC PS " +
				" WHERE     TP.FACTORYNAME = :FACTORYNAME " +
				"       AND TP.PRODUCTSPECNAME = :PRODUCTSPECNAME " +
				"       AND TP.PRODUCTSPECVERSION = :PRODUCTSPECVERSION " +
				"       AND TP.PROCESSFLOWNAME = :PROCESSFLOWNAME " +
				"       AND TP.PROCESSFLOWVERSION = :PROCESSFLOWVERSION " +
				"       AND TP.PROCESSOPERATIONNAME = PS.PROCESSOPERATIONNAME " +
				"       AND TP.PROCESSOPERATIONVERSION = PS.PROCESSOPERATIONVERSION " +
				"       AND PS.DETAILPROCESSOPERATIONTYPE = 'FQC' " ;

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryname);
		args.put("PRODUCTSPECNAME", productSpecName);
		args.put("PRODUCTSPECVERSION", productSpecVersion);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
		
		return result;
	}
	*/
	/*
	private List<Map<String, Object>> getOperationByDetailOperType(String factoryname, String productSpecName, String productSpecVersion, String processFlowName, String processFlowVersion, String detailOperType)
	{
		String sql = "SELECT TP.PROCESSOPERATIONNAME, " +
				"       TP.PROCESSOPERATIONVERSION, " +
				"       PS.DETAILPROCESSOPERATIONTYPE " +
				"  FROM TPFOPOLICY TP, PROCESSOPERATIONSPEC PS " +
				" WHERE     TP.FACTORYNAME = :FACTORYNAME " +
				"       AND TP.PRODUCTSPECNAME = :PRODUCTSPECNAME " +
				"       AND TP.PRODUCTSPECVERSION = :PRODUCTSPECVERSION " +
				"       AND TP.PROCESSFLOWNAME = :PROCESSFLOWNAME " +
				"       AND TP.PROCESSFLOWVERSION = :PROCESSFLOWVERSION " +
				"       AND TP.PROCESSOPERATIONNAME = PS.PROCESSOPERATIONNAME " +
				"       AND TP.PROCESSOPERATIONVERSION = PS.PROCESSOPERATIONVERSION " +
				"       AND PS.DETAILPROCESSOPERATIONTYPE = :DETAILPROCESSOPERATIONTYPE " ;

		Map<String, String> args = new HashMap<String, String>();
		args.put("FACTORYNAME", factoryname);
		args.put("PRODUCTSPECNAME", productSpecName);
		args.put("PRODUCTSPECVERSION", productSpecVersion);
		args.put("PROCESSFLOWNAME", processFlowName);
		args.put("PROCESSFLOWVERSION", processFlowVersion);
		args.put("DETAILPROCESSOPERATIONTYPE", detailOperType);
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
		
		return result;
	}
	*/
	/*
	private List<Map<String, Object>> getBeforeRepairOperation(Lot lotInfo)
	{
		String sql = "SELECT BEFOREOPERATIONNAME, BEFOREOPERATIONVER  " +
				"FROM LOTHISTORY " +
				"WHERE LOTNAME = :LOTNAME " +
				"    AND BEFOREOPERATIONNAME != :BEFOREOPERATIONNAME " +
				"    ORDER BY TIMEKEY DESC " ;

		Map<String, String> args = new HashMap<String, String>();
		args.put("LOTNAME", lotInfo.getKey().getLotName());
		args.put("BEFOREOPERATIONNAME", lotInfo.getProcessOperationName());
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
		
		return result;
	}
	*/
	private List<Map<String, Object>> getReworkCountData(String lotName, String detailOperType)
	{
		String sql = "SELECT * FROM CT_REWORKPRODUCT WHERE PRODUCTNAME = :PRODUCTNAME AND REWORKTYPE = :REWORKTYPE ";

		Map<String, String> args = new HashMap<String, String>();
		args.put("PRODUCTNAME", lotName);
		args.put("REWORKTYPE", detailOperType);
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql, args);
		
		return result;
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
}
