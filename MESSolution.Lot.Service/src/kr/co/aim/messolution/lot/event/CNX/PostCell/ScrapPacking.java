package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
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
import kr.co.aim.messolution.port.MESPortServiceProxy;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableHistory;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class ScrapPacking extends SyncHandler {

	private static Log log = LogFactory.getLog(ScrapPacking.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		// Get Doc
		List<Element> lotList = SMessageUtil.getBodySequenceItemList(doc, "LOTLIST", false);
		String portName = SMessageUtil.getBodyItemValue(doc, "PORTNAME", true);
		String lotDetailGrade = SMessageUtil.getBodyItemValue(doc, "LOTDETAILGRADE", false);
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", false);
		String topProductRequestName = SMessageUtil.getBodyItemValue(doc, "SUPERPRODUCTREQUESTNAME", true);


		String firstTrayName = getFirstTrayName(lotList);
		String processGroupName="";
		if(lotDetailGrade.equals("S1"))
		{
			
	     processGroupName = CreateInnerBox("SPALLET", lotDetailGrade, productSpecName, productRequestName, firstTrayName);
			
		}
		else
		{
			
		 processGroupName = CreateInnerBox("SPALLET", "S", productSpecName, productRequestName, firstTrayName);	
		}
		

		Port portInfo = MESPortServiceProxy.getPortServiceUtil().getPortData(machineName, portName);

		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		// EventInfo
		EventInfo deassignTraygroupEventInfo = EventInfoUtil.makeEventInfo("DeassignTrayGroup", getEventUser(), getEventComment(), null, null);
		deassignTraygroupEventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		deassignTraygroupEventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Packing", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());

		EventInfo deassignEventInfo = EventInfoUtil.makeEventInfo("Deassign", getEventUser(), getEventComment(), null, null);
		deassignEventInfo.setEventTimeKey(eventInfo.getEventTimeKey());
		deassignEventInfo.setEventTime(eventInfo.getEventTime());

		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		List<Object[]> updateTrayArgList = new ArrayList<Object[]>();
		List<DurableHistory> updateTrayHistoryList = new ArrayList<DurableHistory>();

		List<Object[]> updateCoverTrayArgList = new ArrayList<Object[]>();
		List<DurableHistory> updateCoverTrayHistoryList = new ArrayList<DurableHistory>();

		Lot lotInfo = MESLotServiceProxy.getLotServiceUtil().getLotData(lotList.get(0).getChildText("LOTNAME"));
		Lot oldLotInfo = (Lot)ObjectUtil.copyTo(lotInfo);

		ProcessFlow processFlow = CommonUtil.getProcessFlowData(lotInfo.getFactoryName(), lotInfo.getProcessFlowName(), lotInfo.getProcessFlowVersion());

		kr.co.aim.greentrack.processflow.management.iter.NodeStack nodeStack = NodeStackUtil.stringToNodeStack(lotInfo.getNodeStack());
		ProcessFlowIterator pfi = new ProcessFlowIterator(processFlow, nodeStack, "");
		
		PFIValueSetter valueSetter = new LotPFIValueSetter(pfi, oldLotInfo, lotInfo );
		pfi.moveNext("N", valueSetter);

		Node nextNode = pfi.getCurrentNodeData();

		for (Element lotData : lotList)
		{
			Lot lot = MESLotServiceProxy.getLotServiceUtil().getLotData(lotData.getChildText("LOTNAME"));
			Lot oldLot = (Lot)ObjectUtil.copyTo(lot);

			CommonValidation.checkLotProcessStateRun(lot);
			CommonValidation.checkLotStateScrapped(lot);

			if (StringUtils.isNotEmpty(lot.getProcessGroupName()))
				throw new CustomException("PROCESSGROUP-0002", lot.getKey().getLotName());

			if (lot.getLotHoldState().equals("Y"))
				throw new CustomException("LOT-4444", lot.getKey().getLotName(), lot.getLotHoldState());

			String trayName = lot.getCarrierName();

			if (!trayName.isEmpty())
				CommonValidation.checkTrayHoldState(trayName);

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
			lotBindList.add("");
			lotBindList.add(portName);
			lotBindList.add(portInfo.getUdfs().get("PORTTYPE"));
			lotBindList.add(portInfo.getUdfs().get("PORTUSETYPE"));
			lotBindList.add(machineName);
			lotBindList.add("");
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
			lot.setCarrierName("");
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

			if (StringUtils.isNotEmpty(trayName))
			{
				boolean checkTray = true;
				for (int i = 0; i < updateTrayHistoryList.size(); i++)
				{
					DurableHistory trayInfo = updateTrayHistoryList.get(i);

					if (StringUtils.equals(trayInfo.getKey().getDurableName(), trayName))
					{
						checkTray = false;
						break;
					}
				}

				if (checkTray)
				{
					Durable durableInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(trayName);
					Durable olddurableInfo = (Durable) ObjectUtil.copyTo(durableInfo);

					if (!durableInfo.getUdfs().get("COVERNAME").isEmpty())
					{
						boolean checkCoverTray = true;

						for (int i = 0; i < updateCoverTrayHistoryList.size(); i++)
						{
							DurableHistory coverTrayInfo = updateCoverTrayHistoryList.get(i);

							if (coverTrayInfo.getKey().getDurableName().equals(durableInfo.getUdfs().get("COVERNAME").toString()))
							{
								checkCoverTray = false;
								break;
							}
						}

						if (checkCoverTray)
						{
							Durable coverInfo = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableInfo.getUdfs().get("COVERNAME").toString());
							Durable oldCoverInfo = (Durable) ObjectUtil.copyTo(coverInfo);

							String coverName = coverInfo.getUdfs().get("COVERNAME").toString();
							CommonValidation.checkTrayGroupHoldState(coverName);

							List<Object> coverTrayBindList = new ArrayList<Object>();
							coverTrayBindList.add(0);
							coverTrayBindList.add(deassignTraygroupEventInfo.getEventName());
							coverTrayBindList.add(deassignTraygroupEventInfo.getEventTimeKey());
							coverTrayBindList.add(deassignTraygroupEventInfo.getEventTime());
							coverTrayBindList.add(deassignTraygroupEventInfo.getEventUser());
							coverTrayBindList.add(deassignTraygroupEventInfo.getEventComment());
							coverTrayBindList.add("");
							coverTrayBindList.add(constantMap.Dur_Available);
							coverTrayBindList.add("Tray");
							coverTrayBindList.add("Tray");
							coverTrayBindList.add("");
							coverTrayBindList.add(coverInfo.getKey().getDurableName());

							coverInfo.setLotQuantity(0);
							coverInfo.setDurableState(constantMap.Dur_Available);
							coverInfo.setDurableType("Tray");
							coverInfo.setLastEventName(eventInfo.getEventName());
							coverInfo.setLastEventTime(eventInfo.getEventTime());
							coverInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
							coverInfo.setLastEventComment(eventInfo.getEventComment());
							coverInfo.setLastEventUser(eventInfo.getEventUser());

							Map<String, String> durUdfs = new HashMap<>();
							durUdfs = oldCoverInfo.getUdfs();
							durUdfs.put("POSITION", "");
							durUdfs.put("COVERNAME", "");
							durUdfs.put("DURABLETYPE1", "Tray");
							coverInfo.setUdfs(durUdfs);

							updateCoverTrayArgList.add(coverTrayBindList.toArray());
							DurableHistory durableHistory = new DurableHistory();
							durableHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(oldCoverInfo, coverInfo, durableHistory);
							
							updateCoverTrayHistoryList.add(durableHistory);
						}
					}

					List<Object> trayBindList = new ArrayList<Object>();

					trayBindList.add(0);
					trayBindList.add(deassignEventInfo.getEventName());
					trayBindList.add(deassignEventInfo.getEventTimeKey());
					trayBindList.add(deassignEventInfo.getEventTime());
					trayBindList.add(deassignEventInfo.getEventUser());
					trayBindList.add(deassignEventInfo.getEventComment());
					trayBindList.add("");
					trayBindList.add(constantMap.Dur_Available);
					trayBindList.add("Tray");
					trayBindList.add("Tray");
					trayBindList.add("");
					trayBindList.add(durableInfo.getKey().getDurableName());

					durableInfo.setLotQuantity(0);
					durableInfo.setDurableState(constantMap.Dur_Available);
					durableInfo.setDurableType("Tray");
					durableInfo.setLastEventName(eventInfo.getEventName());
					durableInfo.setLastEventTime(eventInfo.getEventTime());
					durableInfo.setLastEventTimeKey(eventInfo.getEventTimeKey());
					durableInfo.setLastEventComment(eventInfo.getEventComment());
					durableInfo.setLastEventUser(eventInfo.getEventUser());

					Map<String, String> durUdfs = new HashMap<>();
					durUdfs = olddurableInfo.getUdfs();
					durUdfs.put("POSITION", "");
					durUdfs.put("COVERNAME", "");
					durUdfs.put("DURABLETYPE1", "Tray");
					durableInfo.setUdfs(durUdfs);

					updateTrayArgList.add(trayBindList.toArray());
					
					DurableHistory durableHistory = new DurableHistory();
					durableHistory = DurableServiceProxy.getDurableHistoryDataAdaptor().setHV(olddurableInfo, durableInfo, durableHistory);
					
					updateTrayHistoryList.add(durableHistory);
				}
			}
		}

		updateCoverTrayData(deassignTraygroupEventInfo, updateCoverTrayArgList, updateCoverTrayHistoryList);

		updateTrayData(deassignEventInfo, updateTrayArgList, updateTrayHistoryList);

		updateLotData(eventInfo, updateLotArgList, updateLotHistoryList);

		List<Map<String, Object>> result = getProductRequestQuantity(processGroupName);
		String ProductRequestName = ConvertUtil.getMapValueByName(result.get(0), "PRODUCTREQUESTNAME");
		ProcessGroup processGroup = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(processGroupName);
		ProcessGroup oldProcessGroup = (ProcessGroup) ObjectUtil.copyTo(processGroup);

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
		packUdfs.put("PRODUCTREQUESTNAME", topProductRequestName);
		packUdfs.put("PACKINGCOMMENT", eventInfo.getEventComment());
		processGroup.setUdfs(packUdfs);

		ProcessGroupHistory processGroupHistory = new ProcessGroupHistory();
		processGroupHistory = ProcessGroupServiceProxy.getProcessGroupHistoryDataAdaptor().setHV(oldProcessGroup, processGroup, processGroupHistory);

		ProcessGroupServiceProxy.getProcessGroupService().update(processGroup);
		ProcessGroupServiceProxy.getProcessGroupHistoryService().insert(processGroupHistory);

		SMessageUtil.addItemToBody(doc, "NEWPACKINGNAME", processGroupName);

		return doc;
	}

	private String CreateInnerBox(String packingType, String lotDetailGrade, String productSpecName, String productRequestName, String trayName) throws CustomException
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
				vendor = durableData.getDurableSpecName().substring(7, 9);
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

		if (packingType.equals("SPALLET"))
		{
			ProcessGroupType = "ScrapPacking";
			MaterialType = "Panel";
			eventInfo.setEventComment("Create Scrap Packing.");
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
			sql.append("(PROCESSGROUPNAME, PROCESSGROUPTYPE, MATERIALTYPE, MATERIALQUANTITY, LASTEVENTNAME, ");
			sql.append(" LASTEVENTTIME, LASTEVENTUSER, LASTEVENTCOMMENT, LASTEVENTTIMEKEY, CREATETIME, ");
			sql.append(" CREATEUSER, PRODUCTSPECNAME, PRODUCTREQUESTNAME, LOTDETAILGRADE)  ");
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
			sql.append("VALUES ");
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

	private List<Map<String, Object>> getProductRequestQuantity(String palletName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT PRODUCTREQUESTNAME, COUNT (PRODUCTREQUESTNAME) AS QUANTITY ");
		sql.append("  FROM LOT ");
		sql.append(" WHERE PROCESSGROUPNAME = :PROCESSGROUPNAME ");
		sql.append("GROUP BY PRODUCTREQUESTNAME ");

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("PROCESSGROUPNAME", palletName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		return sqlResult;
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

	private void updateCoverTrayData(EventInfo deassignTraygroupEventInfo, List<Object[]> updateCoverTrayArgList, List<DurableHistory> updateCoverTrayHistoryList)
			throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE DURABLE ");
		sql.append("   SET LOTQUANTITY = ?, ");
		sql.append("       LASTEVENTNAME = ?, ");
		sql.append("       LASTEVENTTIMEKEY = ?, ");
		sql.append("       LASTEVENTTIME = ?, ");
		sql.append("       LASTEVENTUSER = ?, ");
		sql.append("       LASTEVENTCOMMENT = ?, ");
		sql.append("       POSITION = ?, ");
		sql.append("       DURABLESTATE = ?, ");
		sql.append("       DURABLETYPE = ?, ");
		sql.append("       DURABLETYPE1 = ?, ");
		sql.append("       COVERNAME = ? ");
		sql.append(" WHERE DURABLENAME = ? ");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateCoverTrayArgList);
		try 
		{
			CommonUtil.executeBatch("insert", updateCoverTrayHistoryList);
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}
	}

	private void updateTrayData(EventInfo deassignEventInfo, List<Object[]> updateTrayArgList, List<DurableHistory> updateTrayHistoryList) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE DURABLE ");
		sql.append("   SET LOTQUANTITY = ?, ");
		sql.append("       LASTEVENTNAME = ?, ");
		sql.append("       LASTEVENTTIMEKEY = ?, ");
		sql.append("       LASTEVENTTIME = ?, ");
		sql.append("       LASTEVENTUSER = ?, ");
		sql.append("       LASTEVENTCOMMENT = ?, ");
		sql.append("       POSITION = ?, ");
		sql.append("       DURABLESTATE = ?, ");
		sql.append("       DURABLETYPE = ?, ");
		sql.append("       DURABLETYPE1 = ?, ");
		sql.append("       COVERNAME = ? ");
		sql.append(" WHERE DURABLENAME = ? ");

		MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateTrayArgList);
		try 
		{
			CommonUtil.executeBatch("insert", updateTrayHistoryList);
		} 
		catch (Exception e) 
		{
			log.error(e.getMessage());
			throw new CustomException(e.getCause());
		}
	}

	private String getFirstTrayName(List<Element> lotList) throws FrameworkErrorSignal, NotFoundSignal, CustomException
	{
		String firstTrayName = "";
		
		for (Element lot : lotList)
		{
			String lotName = SMessageUtil.getChildText(lot, "LOTNAME", true);
			Lot lotdata = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

			if (!StringUtils.isEmpty(lotdata.getCarrierName()))
			{
				firstTrayName = lotdata.getCarrierName();
				break;
			}
		}
		
		return firstTrayName;
	}
}
