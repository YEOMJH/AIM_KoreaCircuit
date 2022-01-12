package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;

import kr.co.aim.messolution.consumable.event.MaterialInfoRequest;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FQCRule;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class CreatePackingName extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		
		String packingType = SMessageUtil.getBodyItemValue(doc, "PACKINGTYPE", true);
		String lotGrade = SMessageUtil.getBodyItemValue(doc, "LOTGRADE", true);
		String productSpecName = SMessageUtil.getBodyItemValue(doc, "PRODUCTSPECNAME", false);
		String productRequestName = SMessageUtil.getBodyItemValue(doc, "PRODUCTREQUESTNAME", false);

		Map<String, Object> nameRuleAttrMap = new HashMap<String, Object>();
		nameRuleAttrMap.put("PACKINGTYPE", packingType);
		nameRuleAttrMap.put("PRODUCTTYPE", "0000");
		nameRuleAttrMap.put("VENDOR", "00");
		nameRuleAttrMap.put("LOTGRADE", lotGrade + "0");

		List<String> lstName = CommonUtil.generateNameByNamingRule("PackingNaming", nameRuleAttrMap, 1);
		String newPackingName = lstName.get(0);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		eventInfo.setEventName("CreateProcessGroup");

		String ProcessGroupType = "";
		String MaterialType = "";
		
		if(packingType.equals("INNER"))
		{
			ProcessGroupType = "InnerPacking";
			MaterialType = "Panel";
			eventInfo.setEventComment("Create Inner Packing.");
		}
		else
		{
			ProcessGroupType = "OuterPacking";
			MaterialType = "InnerPacking";
			eventInfo.setEventComment("Create Outer Packing.");
		}

		InsertProcessGroup(newPackingName, ProcessGroupType, MaterialType, lotGrade, productSpecName, productRequestName, eventInfo);
		InsertProcessGroupHistory(newPackingName, lotGrade, productSpecName, productRequestName, eventInfo);
		
		SMessageUtil.addItemToBody(doc, "NEWPACKINGNAME", newPackingName);

		return doc;

	}
	

	private void InsertProcessGroup(String ProcessGroupName, String ProcessGroupType, String MaterialType, String lotGrade, String productSpecName, String productRequestName, EventInfo eventInfo) throws CustomException
	{	
		try
		{
			String sql = "INSERT INTO PROCESSGROUP(PROCESSGROUPNAME, PROCESSGROUPTYPE, MATERIALTYPE, MATERIALQUANTITY, LASTEVENTNAME, LASTEVENTTIME, LASTEVENTUSER, LASTEVENTCOMMENT, LASTEVENTTIMEKEY, CREATETIME, CREATEUSER, PRODUCTSPECNAME, PRODUCTREQUESTNAME, LOTDETAILGRADE) VALUES" +
			 " (:PROCESSGROUPNAME, :PROCESSGROUPTYPE, :MATERIALTYPE, :MATERIALQUANTITY, :LASTEVENTNAME, :LASTEVENTTIME, :LASTEVENTUSER, :LASTEVENTCOMMENT, :LASTEVENTTIMEKEY, :CREATETIME, :CREATEUSER, :PRODUCTSPECNAME, :PRODUCTREQUESTNAME, :LOTDETAILGRADE)";
			
			Map<String,Object> bindMap = new HashMap<String,Object>();
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
			
			int result = GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		}
		catch(Exception e)
		{
			throw new CustomException("SYS-8001","for insert " + ProcessGroupName + " into PROCESSGROUP   Error : " + e.toString());
		}
	}
	
	private void InsertProcessGroupHistory(String ProcessGroupName, String lotGrade, String productSpecName, String productRequestName, EventInfo eventInfo) throws CustomException
	{	
		try
		{
			String sql = "INSERT INTO PROCESSGROUPHISTORY(PROCESSGROUPNAME, TIMEKEY, EVENTTIME, EVENTNAME, EVENTUSER, EVENTCOMMENT, PRODUCTSPECNAME, PRODUCTREQUESTNAME, LOTDETAILGRADE) VALUES" +
			 " (:PROCESSGROUPNAME, :TIMEKEY, :EVENTTIME, :EVENTNAME, :EVENTUSER, :EVENTCOMMENT, :PRODUCTSPECNAME, :PRODUCTREQUESTNAME, :LOTDETAILGRADE)";
			
			Map<String,Object> bindMap = new HashMap<String,Object>();
			bindMap.put("PROCESSGROUPNAME", ProcessGroupName);
			bindMap.put("TIMEKEY", eventInfo.getEventTimeKey());
			bindMap.put("EVENTTIME", eventInfo.getEventTime());
			bindMap.put("EVENTNAME", eventInfo.getEventName());
			bindMap.put("EVENTUSER", eventInfo.getEventUser());
			bindMap.put("EVENTCOMMENT", eventInfo.getEventComment());
			bindMap.put("PRODUCTSPECNAME", productSpecName);
			bindMap.put("PRODUCTREQUESTNAME", productRequestName);
			bindMap.put("LOTDETAILGRADE", lotGrade);
			
			int result = GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
		}
		catch(Exception e)
		{
			throw new CustomException("SYS-8001","for insert " + ProcessGroupName + " into PROCESSGROUPHISTORY   Error : " + e.toString());
		}
	}

}

