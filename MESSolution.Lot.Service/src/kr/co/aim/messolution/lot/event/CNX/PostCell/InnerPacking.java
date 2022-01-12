package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.SuperProductRequest;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.PolicyUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
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
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupHistory;
import kr.co.aim.greentrack.productrequest.management.data.ProductRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class InnerPacking extends SyncHandler {

	private static Log log = LogFactory.getLog(InnerPacking.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String lotDetailGrade = SMessageUtil.getBodyItemValue(doc, "LOTDETAILGRADE", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String superproductRequestName = SMessageUtil.getBodyItemValue(doc, "SUPERPRODUCTREQUESTNAME", true);

		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", false);
		
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		if(StringUtil.in(lotDetailGrade, "C1", "C2", "C3", "C4","C5","C6"))
		{
			lotDetailGrade = "C0";
		}
		
		// EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Packing", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		// Make Panel
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		Port portInfo = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);

		Lot firstLotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotList.get(0).getChildText("LOTNAME"));
		ProductRequest firstProductRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(firstLotData.getProductRequestName());

		String oldSubWoType = firstProductRequestData.getUdfs().get("SUBPRODUCTIONTYPE");
		String oldsuperproductRequestName = firstProductRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME");

		List<String> workOrderList = new ArrayList<String>();
		List<String> superworkOrderList = new ArrayList<String>();
		
		String firstTrayName = "";
		List<Map<String, Object>> panelList=new ArrayList<>();
		
		for (Element lot : lotList)
		{
			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lot.getChildText("LOTNAME"));
			CommonValidation.checkLotHoldState(lotData);

			ProductRequest productRequestData = MESWorkOrderServiceProxy.getProductRequestServiceUtil().getProductRequestData(lotData.getProductRequestName());

			String subWoType = productRequestData.getUdfs().get("SUBPRODUCTIONTYPE");
			superproductRequestName=productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME");

			if (!subWoType.equals(oldSubWoType))
				throw new CustomException("PRODUCTREQUEST-0002");
			if (!superproductRequestName.equals(oldsuperproductRequestName))
				throw new CustomException("PRODUCTREQUEST-0002");

			oldSubWoType = subWoType;
			oldsuperproductRequestName = superproductRequestName;

			if (!workOrderList.contains(lotData.getProductRequestName()))
				workOrderList.add(lotData.getProductRequestName());
			
			if (!superworkOrderList.contains(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
				workOrderList.add(productRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME"));
            if(subWoType.equals("DOESY"))
            {
            	if (workOrderList.size() > 2)
    				throw new CustomException("PRODUCTREQUEST-0003");

            }
            if(superworkOrderList.size()>2)
            {
               throw new CustomException("PRODUCTREQUEST-0003");
            }
			if (!StringUtils.isEmpty(lotData.getProcessGroupName()))
				throw new CustomException("PROCESSGROUP-0002", lotData.getKey().getLotName());

			if (StringUtils.isEmpty(firstTrayName) && !StringUtils.isEmpty(lotData.getCarrierName()))
				firstTrayName = lotData.getCarrierName();
		}
		
			

		String processGroupName = CreateInnerBox("INNER", lotDetailGrade, productSpecName, superproductRequestName, firstTrayName);
		

		for (Element lotData : lotList)
		{
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
			Lot oldLot = (Lot) ObjectUtil.copyTo(lot);

			String trayName = lot.getCarrierName();

			if (!trayName.isEmpty())
				CommonValidation.checkTrayHoldState(trayName);

			Node nextNode = PolicyUtil.getNextOperation(lot);

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
			lotBindList.add(eventInfo.getEventTime());
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(nextNode.getNodeAttribute1());
			lotBindList.add(nextNode.getNodeAttribute2());
			lotBindList.add(lot.getProcessOperationName());
			lotBindList.add(lot.getProcessOperationVersion());
			lotBindList.add(lot.getProcessFlowName());
			lotBindList.add(nextNode.getKey().getNodeId());
			lotBindList.add(trayName);
			lotBindList.add(portName);
			lotBindList.add(portInfo.getUdfs().get("PORTTYPE"));
			lotBindList.add(portInfo.getUdfs().get("PORTUSETYPE"));
			lotBindList.add(machineName);
			lotBindList.add(lot.getUdfs().get("POSITION"));
			lotBindList.add(processGroupName);
			lotBindList.add(lot.getKey().getLotName());

			updateLotArgList.add(lotBindList.toArray());

			// History
			lot.setLotState(constantMap.Lot_Released);
			lot.setLotProcessState(constantMap.Lot_Wait);
			lot.setLastLoggedInTime(eventInfo.getEventTime());
			lot.setLastLoggedInUser(eventInfo.getEventUser());
			lot.setLastLoggedOutTime(eventInfo.getEventTime());
			lot.setLastLoggedOutUser(eventInfo.getEventUser());
			lot.setProcessOperationName(nextNode.getNodeAttribute1());
			lot.setProcessOperationVersion(nextNode.getNodeAttribute2());
			lot.setNodeStack(nextNode.getKey().getNodeId());
			lot.setCarrierName(trayName);
			lot.setMachineName(machineName);
			lot.setProcessGroupName(processGroupName);
			lot.setLastEventName(eventInfo.getEventName());
			lot.setLastEventTime(eventInfo.getEventTime());
			lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
			lot.setLastEventComment(eventInfo.getEventComment());
			lot.setLastEventUser(eventInfo.getEventUser());

			Map<String, String> lotUdf = new HashMap<>();
			lotUdf = lot.getUdfs();
			lotUdf.put("BEFOREOPERATIONNAME", oldLot.getProcessOperationName());
			lotUdf.put("BEFOREOPERATIONVER", oldLot.getProcessOperationVersion());
			lotUdf.put("BEFOREFLOWNAME", oldLot.getProcessFlowName());
			lotUdf.put("POSITION", "");
			lotUdf.put("PORTNAME", portName);
			lotUdf.put("PORTTYPE", portInfo.getUdfs().get("PORTTYPE"));
			lotUdf.put("PORTUSETYPE", portInfo.getUdfs().get("PORTUSETYPE"));
			lot.setUdfs(lotUdf);

			LotHistory lotHistory = new LotHistory();
			lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
			
			updateLotHistoryList.add(lotHistory);

			EventInfo eventInfoD = EventInfoUtil.makeEventInfo("Deassign", getEventUser(), getEventComment(), null, null);
			eventInfoD.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			eventInfoD.setEventTime(TimeStampUtil.getCurrentTimestamp());

			/*if (!StringUtils.isEmpty(trayName))
			{
				Durable durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
				Durable olddurableInfo = (Durable) ObjectUtil.copyTo(durableInfo);

				durableInfo.setLotQuantity(durableInfo.getLotQuantity() - 1);

				if (durableInfo.getLotQuantity() == 0)
					durableInfo.setDurableState(constantMap.Dur_Available);
				else
					durableInfo.setDurableState(constantMap.Dur_InUse);

				durableInfo.setLastEventName(eventInfoD.getEventName());
				durableInfo.setLastEventTimeKey(eventInfoD.getEventTimeKey());
				durableInfo.setLastEventTime(eventInfoD.getEventTime());
				durableInfo.setLastEventUser(eventInfoD.getEventUser());
				durableInfo.setLastEventComment(eventInfoD.getEventComment());

				DurableHistory durHistory = new DurableHistory();
				durHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfo, durableInfo, durHistory);

				DurableServiceProxy.getDurableService().update(durableInfo);
				DurableServiceProxy.getDurableHistoryService().insert(durHistory);
			}*/
		}

		updateLotData(eventInfo, updateLotArgList, updateLotHistoryList);

		ProcessGroup processGroup = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(processGroupName);
		ProcessGroup oldProcessGroup = (ProcessGroup) ObjectUtil.copyTo(processGroup);

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		processGroup.setMaterialType("Panel");
		processGroup.setMaterialQuantity(lotList.size());
		processGroup.setLastEventName(eventInfo.getEventName());
		processGroup.setLastEventTimeKey(eventInfo.getEventTimeKey());
		processGroup.setLastEventTime(eventInfo.getEventTime());
		processGroup.setLastEventUser(eventInfo.getEventUser());
		processGroup.setLastEventComment(eventInfo.getEventComment());
		processGroup.setLastEventFlag("N");

		Map<String, String> packUdfs = new HashMap<>();
		packUdfs = processGroup.getUdfs();
		packUdfs.put("PRODUCTREQUESTNAME", superproductRequestName);
		packUdfs.put("LOTDETAILGRADE", lotDetailGrade);
		packUdfs.put("PACKINGCOMMENT", eventInfo.getEventComment());
		processGroup.setUdfs(packUdfs);

		ProcessGroupHistory processGroupHistory = new ProcessGroupHistory();
		processGroupHistory = ProcessGroupServiceProxy.getProcessGroupHistoryDataAdaptor().setHV(oldProcessGroup, processGroup, processGroupHistory);

		ProcessGroupServiceProxy.getProcessGroupService().update(processGroup);
		ProcessGroupServiceProxy.getProcessGroupHistoryService().insert(processGroupHistory);

		SMessageUtil.addItemToBody(doc, "NEWPACKINGNAME", processGroupName);
		
		try
		{
			String sapFlag=CommonUtil.getEnumDefValueStringByEnumName("SAPFLAG");
			if(StringUtil.isNotEmpty(sapFlag)&&StringUtil.equals(sapFlag, "Y")&&
					StringUtils.isNotEmpty(firstProductRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")))
			{
				SuperProductRequest superWO = ExtendedObjectProxy.getSuperProductRequestService().selectByKey(false, new Object[]{firstProductRequestData.getUdfs().get("SUPERPRODUCTREQUESTNAME")});
				
				MESConsumableServiceProxy.getConsumableServiceUtil().trackOutERPBOMReportForTrayGroup(eventInfo, firstLotData, superWO,machineName, lotList.size(), panelList);
				MESConsumableServiceProxy.getConsumableServiceUtil().trackOutERPBOMReportForSort(eventInfo, firstLotData, superWO,machineName, lotList.size(), "3S004");
			}
		}
		catch(Exception x)
		{
			eventLog.info("SAP Report Error");
		}

		
		return doc;
	}

	private String CreateInnerBox(String packingType, String lotDetailGrade, String productSpecName,String productRequestName, String trayName) throws CustomException
	{
		String productSpecSize = productSpecName.substring(2, 3);
		String productType = "";

		try
		{
			Integer.parseInt(productSpecSize);
			productType = "0" + productSpecName.substring(2, 5);
		}
		catch (Exception e)
		{
			productType = "10" + productSpecName.substring(3, 5);
		}
		
		String vendor = "00";
		
		if (!StringUtils.isEmpty(trayName))
		{
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
			try
			{
				vendor = trayName.substring(7, 9);
			}
			catch(Exception x)
			{
				throw new CustomException("PROCESSGROUP-0017", durableData.getKey().getDurableName(), durableData.getDurableSpecName());
			}
		}

		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("PACKINGTYPE", packingType);
		nameRuleAttrMap.put("PRODUCTTYPE", productType);
		nameRuleAttrMap.put("VENDOR", vendor);
		nameRuleAttrMap.put("LOTGRADE", lotDetailGrade + "0");

		List<String> nameList = CommonUtil.generateNameByNamingRule("PackingNaming", nameRuleAttrMap, 1);
		String newPackingName = nameList.get(0);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		eventInfo.setEventName("CreateProcessGroup");

		String ProcessGroupType = "";
		String MaterialType = "";

		if (packingType.equals("INNER"))
		{
			ProcessGroupType = "InnerPacking";
			MaterialType = "Panel";
		}
		else
		{
			ProcessGroupType = "OuterPacking";
			MaterialType = "InnerPacking";
		}

		InsertProcessGroup(newPackingName, ProcessGroupType, MaterialType, lotDetailGrade, productSpecName, productRequestName, eventInfo);
		InsertProcessGroupHistory(newPackingName, lotDetailGrade, productSpecName, productRequestName, eventInfo);

		return newPackingName;
	}

	private void InsertProcessGroup(String ProcessGroupName, String ProcessGroupType, String MaterialType, String lotGrade, String productSpecName,String productRequestName, EventInfo eventInfo)
			throws CustomException
	{
		
		 try
		 {
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT INTO PROCESSGROUP  ");
			sql.append("(PROCESSGROUPNAME, PROCESSGROUPTYPE, MATERIALTYPE, MATERIALQUANTITY, LASTEVENTNAME, ");
			sql.append(" LASTEVENTTIME, LASTEVENTUSER, LASTEVENTCOMMENT, LASTEVENTTIMEKEY, CREATETIME, ");
			sql.append(" CREATEUSER, PRODUCTSPECNAME, PRODUCTREQUESTNAME, LOTDETAILGRADE) ");
			sql.append("VALUES  ");
			sql.append("(:PROCESSGROUPNAME, :PROCESSGROUPTYPE, :MATERIALTYPE, :MATERIALQUANTITY, :LASTEVENTNAME, ");
			sql.append(" :LASTEVENTTIME, :LASTEVENTUSER, :LASTEVENTCOMMENT, :LASTEVENTTIMEKEY, :CREATETIME, ");
			sql.append(" :CREATEUSER, :PRODUCTSPECNAME, :PRODUCTREQUESTNAME, :LOTDETAILGRADE) ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PROCESSGROUPNAME", ProcessGroupName);
			bindMap.put("PROCESSGROUPTYPE", ProcessGroupType);
			bindMap.put("MATERIALTYPE", MaterialType);
			bindMap.put("MATERIALQUANTITY", 0);
			bindMap.put("LASTEVENTNAME", eventInfo.getEventName());
			bindMap.put("LASTEVENTTIME", eventInfo.getEventTime());
			bindMap.put("LASTEVENTUSER", eventInfo.getEventUser());
			bindMap.put("LASTEVENTCOMMENT", eventInfo.getEventComment());
			bindMap.put("LASTEVENTTIMEKEY", eventInfo.getEventTimeKey());
			bindMap.put("CREATETIME", eventInfo.getEventTime());
			bindMap.put("CREATEUSER", eventInfo.getEventUser());
			bindMap.put("PRODUCTSPECNAME", productSpecName);
			bindMap.put("PRODUCTREQUESTNAME", productRequestName);
			bindMap.put("LOTDETAILGRADE", lotGrade);

			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "for insert " + ProcessGroupName + " into PROCESSGROUP   Error : " + e.toString());
		}
	   
	  
	}

	private void InsertProcessGroupHistory(String ProcessGroupName, String lotGrade, String productSpecName,String productRequestName, EventInfo eventInfo) throws CustomException
	{
		
		try
		 {
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT INTO PROCESSGROUPHISTORY  ");
			sql.append("(PROCESSGROUPNAME, TIMEKEY, EVENTTIME, EVENTNAME, EVENTUSER, ");
			sql.append(" EVENTCOMMENT, PRODUCTSPECNAME, PRODUCTREQUESTNAME, LOTDETAILGRADE) ");
			sql.append("VALUES  ");
			sql.append("(:PROCESSGROUPNAME, :TIMEKEY, :EVENTTIME, :EVENTNAME, :EVENTUSER, ");
			sql.append(" :EVENTCOMMENT, :PRODUCTSPECNAME, :PRODUCTREQUESTNAME, :LOTDETAILGRADE) ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("PROCESSGROUPNAME", ProcessGroupName);
			bindMap.put("TIMEKEY", eventInfo.getEventTimeKey());
			bindMap.put("EVENTTIME", eventInfo.getEventTime());
			bindMap.put("EVENTNAME", eventInfo.getEventName());
			bindMap.put("EVENTUSER", eventInfo.getEventUser());
			bindMap.put("EVENTCOMMENT", eventInfo.getEventComment());
			bindMap.put("PRODUCTSPECNAME", productSpecName);
			bindMap.put("PRODUCTREQUESTNAME", productRequestName);
			bindMap.put("LOTDETAILGRADE", lotGrade);

			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "for insert " + ProcessGroupName + " into PROCESSGROUPHISTORY   Error : " + e.toString());
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
		sql.append("       LASTLOGGEDINTIME = ?, ");
		sql.append("       LASTLOGGEDINUSER = ?, ");
		sql.append("       LASTLOGGEDOUTTIME = ?, ");
		sql.append("       LASTLOGGEDOUTUSER = ?, ");
		sql.append("       PROCESSOPERATIONNAME = ?, ");
		sql.append("       PROCESSOPERATIONVERSION = ?, ");
		sql.append("       BEFOREOPERATIONNAME = ?, ");
		sql.append("       BEFOREOPERATIONVER = ?, ");
		sql.append("       BEFOREFLOWNAME = ?, ");
		sql.append("       NODESTACK = ?, ");
		sql.append("       CARRIERNAME = ?, ");
		sql.append("       PORTNAME = ?, ");
		sql.append("       PORTTYPE = ?, ");
		sql.append("       PORTUSETYPE = ?, ");
		sql.append("       MACHINENAME = ?, ");
		sql.append("       POSITION = ?, ");
		sql.append("       PROCESSGROUPNAME = ? ");
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
