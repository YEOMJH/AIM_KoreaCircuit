package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.lot.management.policy.util.LotPFIValueSetter;
import kr.co.aim.greentrack.port.management.data.Port;
import kr.co.aim.greentrack.processflow.management.data.Node;
import kr.co.aim.greentrack.processflow.management.data.ProcessFlow;
import kr.co.aim.greentrack.processflow.management.iter.NodeStackUtil;
import kr.co.aim.greentrack.processflow.management.iter.PFIValueSetter;
import kr.co.aim.greentrack.processflow.management.iter.ProcessFlowIterator;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupHistory;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupKey;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class Palletizing extends SyncHandler {

	private static Log log = LogFactory.getLog(CancelCommonTrackIn.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		// Get Doc
		List<Element> boxList = SMessageUtil.getBodySequenceItemList(doc, "OUTERBOXLIST", true);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String lotDetailGrade = SMessageUtil.getBodyItemValue(doc, "LOTDETAILGRADE", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);

		String processGroupName = CreateBox("PALLET", lotDetailGrade, productSpecName, "", boxList.get(0).getChildText("OUTERBOXID"));

		int boxQty = boxList.size();

		Port portInfo = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		// EventInfo
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Packing", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		// Make Panel
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		String nextOper = "";
		String nextOperVer = "";
		String nextNodeStack = "";

		String workOrder = "";

		ProcessGroupKey firstGroupKey = new ProcessGroupKey();
		firstGroupKey.setProcessGroupName(boxList.get(0).getChildText("OUTERBOXID"));

		ProcessGroup firstProcessGroup = ProcessGroupServiceProxy.getProcessGroupService().selectByKey(firstGroupKey);
		workOrder = firstProcessGroup.getUdfs().get("PRODUCTREQUESTNAME");

		// OUTER
		for (Element boxData : boxList)
		{
			String outerBoxName = boxData.getChildText("OUTERBOXID");

			String condition = " WHERE SUPERPROCESSGROUPNAME = ? ";
			Object[] bindSet = new Object[] { outerBoxName };

			List<ProcessGroup> innerBoxList = ProcessGroupServiceProxy.getProcessGroupService().select(condition, bindSet);

			for (ProcessGroup innerBoxData : innerBoxList)
			{
				String innerBoxID = innerBoxData.getKey().getProcessGroupName();

				List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getLotListByProcessGroup(innerBoxID);

				for (Lot lot : lotList)
				{
					Lot oldLot = (Lot)ObjectUtil.copyTo(lot);

					if (!StringUtil.isNotEmpty(nextOper))
					{
						Node nextNode = new Node();

						ProcessFlow processFlow = CommonUtil.getProcessFlowData(lot.getFactoryName(), lot.getProcessFlowName(), lot.getProcessFlowVersion());

						kr.co.aim.greentrack.processflow.management.iter.NodeStack nodeStack = NodeStackUtil.stringToNodeStack(lot.getNodeStack());
						ProcessFlowIterator pfi = new ProcessFlowIterator(processFlow, nodeStack, "");
						
						PFIValueSetter valueSetter = new LotPFIValueSetter(pfi, oldLot, lot );
						pfi.moveNext("N", valueSetter);

						// 1.3. Set ProcessFlow Iterator Related Data
						nextNode = pfi.getCurrentNodeData();

						nextOper = nextNode.getNodeAttribute1();
						nextOperVer = nextNode.getNodeAttribute2();
						nextNodeStack = nextNode.getKey().getNodeId();
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
					lotBindList.add(eventInfo.getEventTime());
					lotBindList.add(eventInfo.getEventUser());
					lotBindList.add(nextOper);
					lotBindList.add(nextOperVer);
					lotBindList.add(lot.getProcessOperationName());
					lotBindList.add(lot.getProcessOperationVersion());
					lotBindList.add(lot.getProcessFlowName());
					lotBindList.add(nextNodeStack);
					lotBindList.add(portName);
					lotBindList.add(portInfo.getUdfs().get("PORTTYPE"));
					lotBindList.add(portInfo.getUdfs().get("PORTUSETYPE"));
					lotBindList.add(machineName);
					lotBindList.add(lot.getKey().getLotName());

					updateLotArgList.add(lotBindList.toArray());

					// History
					lot.setLotState(constantMap.Lot_Released);
					lot.setLotProcessState(constantMap.Lot_Wait);
					lot.setLastLoggedInTime(eventInfo.getEventTime());
					lot.setLastLoggedInUser(eventInfo.getEventUser());
					lot.setLastLoggedOutTime(eventInfo.getEventTime());
					lot.setLastLoggedOutUser(eventInfo.getEventUser());
					lot.setProcessOperationName(nextOper);
					lot.setProcessOperationVersion(nextOperVer);
					lot.setNodeStack(nextNodeStack);
					lot.setMachineName(machineName);
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
					lotUdf.put("PORTNAME", portName);
					lotUdf.put("PORTTYPE", portInfo.getUdfs().get("PORTTYPE"));
					lotUdf.put("PORTUSETYPE", portInfo.getUdfs().get("PORTUSETYPE"));
					lot.setUdfs(lotUdf);

					LotHistory lotHistory = new LotHistory();
					lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
					
					updateLotHistoryList.add(lotHistory);
				}
			}

			ProcessGroup processGroup = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(outerBoxName);
			ProcessGroup oldProcessGroup = (ProcessGroup) ObjectUtil.copyTo(processGroup);

			if(!StringUtils.isEmpty(processGroup.getSuperProcessGroupName()))
				throw new CustomException("PROCESSGROUP-0003", processGroup.getKey().getProcessGroupName());

			eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
			
			processGroup.setSuperProcessGroupName(processGroupName);
			processGroup.setLastEventName(eventInfo.getEventName());
			processGroup.setLastEventTimeKey(eventInfo.getEventTimeKey());
			processGroup.setLastEventTime(eventInfo.getEventTime());
			processGroup.setLastEventUser(eventInfo.getEventUser());
			processGroup.setLastEventComment(eventInfo.getEventComment());
			processGroup.setLastEventFlag("N");

			ProcessGroupHistory processGroupHistory = new ProcessGroupHistory();
			processGroupHistory = ProcessGroupServiceProxy.getProcessGroupHistoryDataAdaptor().setHV(oldProcessGroup, processGroup, processGroupHistory);

			ProcessGroupServiceProxy.getProcessGroupService().update(processGroup);
			ProcessGroupServiceProxy.getProcessGroupHistoryService().insert(processGroupHistory);
		}

		updateLotData(eventInfo, updateLotArgList, updateLotHistoryList);

		ProcessGroup processGroup = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(processGroupName);
		ProcessGroup oldProcessGroup = (ProcessGroup) ObjectUtil.copyTo(processGroup);

		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		processGroup.setMaterialType("OuterPacking");
		processGroup.setDetailMaterialType("InnerPacking");
		processGroup.setMaterialQuantity(boxQty);
		processGroup.setLastEventName(eventInfo.getEventName());
		processGroup.setLastEventTimeKey(eventInfo.getEventTimeKey());
		processGroup.setLastEventTime(eventInfo.getEventTime());
		processGroup.setLastEventUser(eventInfo.getEventUser());
		processGroup.setLastEventComment(eventInfo.getEventComment());
		processGroup.setLastEventFlag("N");

		Map<String, String> packUdfs = new HashMap<>();
		packUdfs = processGroup.getUdfs();
		packUdfs.put("PRODUCTREQUESTNAME", workOrder);
		packUdfs.put("LOTDETAILGRADE", lotDetailGrade);
		packUdfs.put("PACKINGCOMMENT", eventInfo.getEventComment());
		processGroup.setUdfs(packUdfs);

		ProcessGroupHistory processGroupHistory = new ProcessGroupHistory();
		processGroupHistory = ProcessGroupServiceProxy.getProcessGroupHistoryDataAdaptor().setHV(oldProcessGroup, processGroup, processGroupHistory);

		ProcessGroupServiceProxy.getProcessGroupService().update(processGroup);
		ProcessGroupServiceProxy.getProcessGroupHistoryService().insert(processGroupHistory);

		SMessageUtil.addItemToBody(doc, "NEWPACKINGNAME", processGroupName);

		return doc;
	}

	private String CreateBox(String packingType, String lotDetailGrade, String productSpecName, String productRequestName, String firstBoxName) throws CustomException
	{
		String productType = firstBoxName.substring(2, 6);
		String vendor = firstBoxName.substring(7, 9);
		
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
		else if (packingType.equals("PALLET"))
		{
			ProcessGroupType = "Pallet";
			MaterialType = "OuterPacking";
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

	private void InsertProcessGroup(String ProcessGroupName, String ProcessGroupType, String MaterialType, String lotGrade, String productSpecName, String productRequestName, EventInfo eventInfo)
			throws CustomException
	{
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT INTO PROCESSGROUP ");
			sql.append("(PROCESSGROUPNAME, PROCESSGROUPTYPE, MATERIALTYPE, MATERIALQUANTITY, LASTEVENTNAME,  ");
			sql.append(" LASTEVENTTIME, LASTEVENTUSER, LASTEVENTCOMMENT, LASTEVENTTIMEKEY, CREATETIME,  ");
			sql.append("  CREATEUSER, PRODUCTSPECNAME, PRODUCTREQUESTNAME, LOTDETAILGRADE)  ");
			sql.append("VALUES ");
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

	private void InsertProcessGroupHistory(String ProcessGroupName, String lotGrade, String productSpecName, String productRequestName, EventInfo eventInfo) throws CustomException
	{
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT INTO PROCESSGROUPHISTORY ");
			sql.append("(PROCESSGROUPNAME, TIMEKEY, EVENTTIME, EVENTNAME, EVENTUSER, ");
			sql.append(" EVENTCOMMENT, PRODUCTSPECNAME, PRODUCTREQUESTNAME, LOTDETAILGRADE)  ");
			sql.append(" VALUES ");
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
		sql.append("       PORTNAME = ?, ");
		sql.append("       PORTTYPE = ?, ");
		sql.append("       PORTUSETYPE = ?, ");
		sql.append("       MACHINENAME = ? ");
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
