package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.processgroup.MESProcessGroupServiceProxy;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.data.LotHistory;
import kr.co.aim.greentrack.processgroup.ProcessGroupServiceProxy;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroup;
import kr.co.aim.greentrack.processgroup.management.data.ProcessGroupHistory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class ScrapOuterPacking extends SyncHandler {

	private static Log log = LogFactory.getLog(ScrapOuterPacking.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		// Get Doc
		List<Element> processGroupList = SMessageUtil.getBodySequenceItemList(doc, "PROCESSGROUPLIST", false);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", true);
		String lotDetailGrade = SMessageUtil.getBodyItemValue(doc, "LOTDETAILGRADE", false);
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "SUPERPRODUCTREQUESTNAME", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ScrapOuterPacking", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventTime(TimeStampUtil.getCurrentTimestamp());
		String processGroupName="";
		if(lotDetailGrade.equals("S1"))
		{
			
	     processGroupName = CreateInnerBox("SCRAPOUTER", lotDetailGrade, productSpecName, productRequestName, processGroupList.size(), processGroupList.get(0).getChildText("PROCESSGROUPNAME"));
			
		}
		else
		{
			
		 processGroupName = CreateInnerBox("SCRAPOUTER", "S", productSpecName, productRequestName, processGroupList.size(), processGroupList.get(0).getChildText("PROCESSGROUPNAME"));	
		}
		updateProcessGroupInfo(processGroupName, processGroupList);

		ProcessGroup processGroup = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(processGroupName);
		ProcessGroup oldProcessGroup = (ProcessGroup) ObjectUtil.copyTo(processGroup);

		processGroup.setMaterialType("ScrapPacking");
		processGroup.setLastEventName(eventInfo.getEventName());
		processGroup.setLastEventTimeKey(eventInfo.getEventTimeKey());
		processGroup.setLastEventTime(eventInfo.getEventTime());
		processGroup.setLastEventUser(eventInfo.getEventUser());
		processGroup.setLastEventComment(eventInfo.getEventComment());
		processGroup.setLastEventFlag("N");

		Map<String, String> packUdfs = new HashMap<>();
		packUdfs = processGroup.getUdfs();
		packUdfs.put("PACKINGCOMMENT", eventInfo.getEventComment());
		processGroup.setUdfs(packUdfs);

		ProcessGroupHistory processGroupHistory = new ProcessGroupHistory();
		processGroupHistory = ProcessGroupServiceProxy.getProcessGroupHistoryDataAdaptor().setHV(oldProcessGroup, processGroup, processGroupHistory);

		ProcessGroupServiceProxy.getProcessGroupService().update(processGroup);
		ProcessGroupServiceProxy.getProcessGroupHistoryService().insert(processGroupHistory);

		SMessageUtil.addItemToBody(doc, "NEWPACKINGNAME", processGroupName);

		return doc;
	}

	private void updateProcessGroupInfo(String superProcessGroupName, List<Element> processGroupList) throws CustomException
	{
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		List<LotHistory> updateLotHistoryList = new ArrayList<LotHistory>();

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ScrapOuterPacking", getEventUser(), getEventComment(), "", "");

		for (Element processGroupData : processGroupList)
		{
			String processGroupName = processGroupData.getChildText("PROCESSGROUPNAME");
			eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));

			ProcessGroup processGroup = MESProcessGroupServiceProxy.getProcessGroupServiceUtil().getProcessGroupData(processGroupName);
			ProcessGroup oldProcessGroup = (ProcessGroup) ObjectUtil.copyTo(processGroup);

			processGroup.setSuperProcessGroupName(superProcessGroupName);
			processGroup.setLastEventName(eventInfo.getEventName());
			processGroup.setLastEventTimeKey(eventInfo.getEventTimeKey());
			processGroup.setLastEventTime(eventInfo.getEventTime());
			processGroup.setLastEventUser(eventInfo.getEventUser());
			processGroup.setLastEventComment(eventInfo.getEventComment());
			processGroup.setLastEventFlag("N");

			ProcessGroupHistory processGroupHistory = new ProcessGroupHistory();
			ProcessGroupServiceProxy.getProcessGroupService().update(processGroup);
			processGroupHistory = ProcessGroupServiceProxy.getProcessGroupHistoryDataAdaptor().setHV(oldProcessGroup, processGroup, processGroupHistory);
			ProcessGroupServiceProxy.getProcessGroupHistoryService().insert(processGroupHistory);

			List<Lot> lotList = MESLotServiceProxy.getLotServiceUtil().getScrapLotListByProcessGroup(processGroupName);

			for (Lot lot : lotList)
			{
				Lot oldLot = (Lot)ObjectUtil.copyTo(lot);

				List<Object> lotBindList = new ArrayList<Object>();

				lotBindList.add(eventInfo.getEventName());
				lotBindList.add(eventInfo.getEventTimeKey());
				lotBindList.add(eventInfo.getEventTime());
				lotBindList.add(eventInfo.getEventUser());
				lotBindList.add(eventInfo.getEventComment());
				lotBindList.add("N");
				lotBindList.add(lot.getKey().getLotName());
				updateLotArgList.add(lotBindList.toArray());

				//History
				lot.setLastEventName(eventInfo.getEventName());
				lot.setLastEventTime(eventInfo.getEventTime());
				lot.setLastEventTimeKey(eventInfo.getEventTimeKey());
				lot.setLastEventComment(eventInfo.getEventComment());
				lot.setLastEventUser(eventInfo.getEventUser());
				
				LotHistory lotHistory = new LotHistory();
				lotHistory = LotServiceProxy.getLotHistoryDataAdaptor().setHV(oldLot, lot, lotHistory);
				
				updateLotHistoryList.add(lotHistory);
			}
		}

		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE LOT ");
		sql.append("   SET LASTEVENTNAME = ?, ");
		sql.append("       LASTEVENTTIMEKEY = ?, ");
		sql.append("       LASTEVENTTIME = ?, ");
		sql.append("       LASTEVENTUSER = ?, ");
		sql.append("       LASTEVENTCOMMENT = ?, ");
		sql.append("       LASTEVENTFLAG = ? ");
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

	private String CreateInnerBox(String packingType, String lotDetailGrade, String productSpecName, String productRequestName, int materialQuantity, String firstBoxName) throws CustomException
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

		String ProcessGroupType = "ScrapOuterPacking";
		String MaterialType = "ScrapPacking";

		InsertProcessGroup(newPackingName, ProcessGroupType, MaterialType, lotDetailGrade, productSpecName, productRequestName, eventInfo, materialQuantity);
		InsertProcessGroupHistory(newPackingName, lotDetailGrade, productSpecName, productRequestName, eventInfo, materialQuantity);

		return newPackingName;
	}

	private void InsertProcessGroup(String ProcessGroupName, String ProcessGroupType, String MaterialType, String lotGrade, String productSpecName, String productRequestName, EventInfo eventInfo,
			int materialQuantity) throws CustomException
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
			bindMap.put("MATERIALQUANTITY", materialQuantity);
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

	private void InsertProcessGroupHistory(String ProcessGroupName, String lotGrade, String productSpecName, String productRequestName, EventInfo eventInfo, int materialQuantity)
			throws CustomException
	{
		try
		{
			StringBuffer sql = new StringBuffer();
			sql.append("INSERT INTO PROCESSGROUPHISTORY ");
			sql.append("(PROCESSGROUPNAME, TIMEKEY, EVENTTIME, EVENTNAME, EVENTUSER, ");
			sql.append(" EVENTCOMMENT, PRODUCTSPECNAME, PRODUCTREQUESTNAME, LOTDETAILGRADE,MATERIALQUANTITY)  ");
			sql.append("VALUES ");
			sql.append("(:PROCESSGROUPNAME, :TIMEKEY, :EVENTTIME, :EVENTNAME, :EVENTUSER, ");
			sql.append(" :EVENTCOMMENT, :PRODUCTSPECNAME, :PRODUCTREQUESTNAME, :LOTDETAILGRADE,:MATERIALQUANTITY) ");

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
			bindMap.put("MATERIALQUANTITY", materialQuantity);

			GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		}
		catch (Exception e)
		{
			throw new CustomException("SYS-8001", "for insert " + ProcessGroupName + " into PROCESSGROUPHISTORY   Error : " + e.toString());
		}
	}
}
