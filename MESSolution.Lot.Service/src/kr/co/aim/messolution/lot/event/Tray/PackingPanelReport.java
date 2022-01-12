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
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.messolution.productrequest.MESWorkOrderServiceProxy;
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
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.iter.NodeStackUtil;
import kr.co.aim.greentrack.processflow.management.iter.PFIValueSetter;
import kr.co.aim.greentrack.processflow.management.iter.ProcessFlowIterator;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupHistory;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

public class PackingPanelReport extends AsyncHandler 
{
	private static Log log = LogFactory.getLog(PackingPanelReport.class);

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String packingName = SMessageUtil.getBodyItemValue(doc, "PACKINGNAME", true);
		String trayName = SMessageUtil.getBodyItemValue(doc, "TRAYNAME", false);
		String machineRecipeName = SMessageUtil.getBodyItemValue(doc, "MACHINERECIPENAME", false);
		String quantity = SMessageUtil.getBodyItemValue(doc, "QUANTITY", true);
		
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();
		
		List<Element> panelList = SMessageUtil.getBodySequenceItemList(doc, "PANELLIST", true);
		List<Lot> panelDataList = getLotList(panelList);
		
		Lot firstLotData = panelDataList.get(0);
		Lot backUpLotData=(Lot) ObjectUtil.copyTo(firstLotData);
		
		Durable trayData = new Durable();
		
		if(StringUtil.isNotEmpty(trayName))
		{
			trayData = MESDurableServiceProxy.getDurableServiceUtil().getDurableDataForUpdate(trayName);
		}
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Packing", this.getEventUser(), this.getEventComment());
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		String lotDetailGrade = firstLotData.getUdfs().get("LOTDETAILGRADE").toString();
		
		if(StringUtil.equals(firstLotData.getLotGrade(), "S"))
		{
			lotDetailGrade = "S";
		}
		
		if(StringUtil.in(lotDetailGrade, "C1", "C2", "C3", "C4","C5","C6"))
		{
			lotDetailGrade = "C0";
		}

		//Get Node
		ProcessFlow processFlow = CommonUtil.getProcessFlowData(firstLotData.getFactoryName(), firstLotData.getProcessFlowName(), firstLotData.getProcessFlowVersion());

		kr.co.aim.greentrack.processflow.management.iter.NodeStack nodeStack = NodeStackUtil.stringToNodeStack(firstLotData.getNodeStack());
		ProcessFlowIterator pfi = new ProcessFlowIterator(processFlow, nodeStack, "");

		PFIValueSetter valueSetter = new LotPFIValueSetter(pfi, firstLotData, firstLotData );
		pfi.moveNext("N", valueSetter);

		Node nextNode = pfi.getCurrentNodeData();
		
		// Make Panel
		List<Lot> updateLotArgList = new ArrayList<Lot>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();
		
		ProductRequest firstProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(firstLotData.getProductRequestName());

		ProcessGroup processGroup = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(packingName);
		
		for (Lot lot : panelDataList)
		{
			Lot oldLot = (Lot) ObjectUtil.copyTo(lot);
			Map<String, String> lotUdf = new HashMap<>();
			
			if (!StringUtils.isEmpty(lot.getProcessGroupName()))
				throw new CustomException("PROCESSGROUP-0002", lot.getKey().getLotName());
			
			if(!StringUtil.equals(lotDetailGrade, "S"))
			{
				if(StringUtil.isEmpty(trayName))
				{
					throw new CustomException("TRAY-0005");
				}
				
				if(!StringUtil.equals(trayName, lot.getCarrierName()))
				{
					throw new CustomException("TRAY-0004", lot.getKey().getLotName());
				}
			}
			else
			{
				CommonValidation.checkLotProcessStateRun(lot);
				CommonValidation.checkLotStateScrapped(lot);
			}
			
			lot.setLotProcessState(constantMap.Lot_Wait);
			if(!StringUtil.equals(lotDetailGrade, "S"))
			{
				lot.setLastLoggedInTime(eventInfo.getEventTime());
				lot.setLastLoggedInUser(eventInfo.getEventUser());
			}
			lot.setLastLoggedOutTime(eventInfo.getEventTime());
			lot.setLastLoggedOutUser(eventInfo.getEventUser());
			lot.setProcessOperationName(nextNode.getNodeAttribute1());
			lot.setProcessOperationVersion(nextNode.getNodeAttribute2());
			lot.setNodeStack(nextNode.getKey().getNodeId());
			lot.setMachineName(machineName);
			lot.setProcessGroupName(packingName);
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());

			lotUdf.put("BEFOREOPERATIONNAME", oldLot.getProcessOperationName());
			lotUdf.put("BEFOREOPERATIONVER", oldLot.getProcessOperationVersion());
			lotUdf.put("BEFOREFLOWNAME", oldLot.getProcessFlowName());
			lot.setUdfs(lotUdf);

			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
			
			updateLotArgList.add(lot);
			updateLotHistoryList.add(lotHistory);
		}
		
		//Update PanelInfo
		if(updateLotArgList.size() > 0)
		{
			log.debug("Insert Lot, LotHistory");
			try
			{
				CommonUtil.executeBatch("update", updateLotArgList);
				CommonUtil.executeBatch("insert", updateLotHistoryList);
			}
			catch (Exception e)
			{
				log.error(e.getMessage());
				throw new CustomException(e.getCause());
			}
		}
		
		StringBuilder query = new StringBuilder();
		query.append("SELECT * ");
		query.append(" FROM LOT ");
		query.append(" WHERE PROCESSGROUPNAME=:PROCESSGROUPNAME ");

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("PROCESSGROUPNAME", packingName);

		List<Map<String, Object>> resultList = GenericServiceProxy.getSqlMesTemplate().getSimpleJdbcTemplate().queryForList(query.toString(), bindMap);
		int lotQuantity=Integer.parseInt(quantity);
		if(resultList.size()!=lotQuantity)
		{
			throw new CustomException("TRAY-0031");
		}
		//Update ProcessGroup
		ProcessGroup oldProcessGroup = (ProcessGroup) ObjectUtil.copyTo(processGroup);

		processGroup.setMaterialType("Panel");
		processGroup.setMaterialQuantity(panelList.size());
		processGroup.setLastEventName(eventInfo.getEventName());
		processGroup.setLastEventTimeKey(eventInfo.getEventTimeKey());
		processGroup.setLastEventTime(eventInfo.getEventTime());
		processGroup.setLastEventUser(eventInfo.getEventUser());
		processGroup.setLastEventComment(eventInfo.getEventComment());
		processGroup.setLastEventFlag("N");

		Map<String, String> packUdfs = new HashMap<>();
		packUdfs = processGroup.getUdfs();
		packUdfs.put("PRODUCTREQUESTNAME", firstProductRequestData.getKey().getProductRequestName());
		packUdfs.put("LOTDETAILGRADE", lotDetailGrade);
		packUdfs.put("PACKINGCOMMENT", eventInfo.getEventComment());
		processGroup.setUdfs(packUdfs);

		ProcessGroupHistory processGroupHistory = new ProcessGroupHistory();
		processGroupHistory = ProcessGroupServiceProxy.getProcessGroupHistoryDataAdaptor().setHV(oldProcessGroup, processGroup, processGroupHistory);

		ProcessGroupServiceProxy.getProcessGroupService().update(processGroup);
		ProcessGroupServiceProxy.getProcessGroupHistoryService().insert(processGroupHistory);
		
		//Update TrayInfo
		/*if(!StringUtil.equals(lotDetailGrade, "S"))
		{
			Durable oldTrayInfo = (Durable)ObjectUtil.copyTo(trayData);
			trayData.setDurableState(GenericServiceProxy.getConstantMap().Dur_NotAvailable);
			trayData.setLastEventName(eventInfo.getEventName());
			trayData.setLastEventTimeKey(eventInfo.getEventTimeKey());
			trayData.setLastEventTime(eventInfo.getEventTime());
			trayData.setLastEventUser(eventInfo.getEventUser());
			trayData.setLastEventComment(eventInfo.getEventComment());
			
			DurableHistory durHistory = new DurableHistory();
			durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldTrayInfo, trayData, durHistory);

			DurableServiceProxy.getDurableService().update(trayData);
			DurableServiceProxy.getDurableHistoryService().insert(durHistory);
		}*/
		
		try
		{
			String sapFlag=CommonUtil.getEnumDefValueStringByEnumName("SAPFLAG");
			if(StringUtil.isNotEmpty(sapFlag)&&StringUtil.equals(sapFlag, "Y")&&
					StringUtils.isNotEmpty(firstProductRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME"))
					&&StringUtils.equals(backUpLotData.getProcessOperationName(), "36010"))
			{
				List<Map<String, Object>> reportPanelList=new ArrayList<>();
				SuperProductRequest superWO = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{firstProductRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
				
				MESConsumableServiceProxy.getConsumableServiceUtil().trackOutERPBOMReportForTrayGroup(eventInfo, backUpLotData, superWO,machineName, panelList.size(), reportPanelList);
				MESConsumableServiceProxy.getConsumableServiceUtil().trackOutERPBOMReportForSort(eventInfo, backUpLotData, superWO,machineName, panelList.size(), "3S004");
			}
		}
		catch(Exception x)
		{
			eventLog.info("SAP Report Error");
		}
	}
	public List<Lot> getLotList(List<Element> panelList) throws CustomException
	{
		String condition = "WHERE LOTNAME IN(";
		for (Element panelElement : panelList) 
		{
			String lotName = panelElement.getChildText("PANELNAME");
			
			condition += "'" + lotName + "',";
		}
		condition = condition.substring(0, condition.length() - 1) + ")";
		
		List<Lot> lotDataList = new ArrayList<Lot>();
		
		try
		{
			lotDataList = LotServiceProxy.getLotService().select(condition, new Object[] { });
		}
		catch (Exception e)
		{
			//PANEL-0011 Panel list search fail
			throw new CustomException("PANEL-0011");
		}
		
		return lotDataList;
	}
}
