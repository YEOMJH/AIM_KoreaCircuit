package kr.co.aim.messolution.dispatch.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.dispatch.MESDSPServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DSPServiceImpl implements ApplicationContextAware {

	private ApplicationContext applicationContext;
	private static Log log = LogFactory.getLog(DSPServiceImpl.class);

	public void setApplicationContext(ApplicationContext arg0) throws BeansException
	{
		applicationContext = arg0;
	}

	public void insertReserveProductSpec(EventInfo eventInfo, String machineName, String processOperationGroupName, String processOperationName, String productSpecName, String position,
			String reserveState, String reservedQuantity, String completeQuantity)
	{
		String sql = "INSERT INTO CT_RESERVEPRODUCT (MACHINENAME, PROCESSOPERATIONGROUPNAME, PROCESSOPERATIONNAME, PRODUCTSPECNAME, POSITION, "
				+ " RESERVESTATE, RESERVEDQUANTITY, COMPLETEQUANTITY, LASTEVENTNAME, LASTEVENTTIMEKEY, LASTEVENTTIME, LASTEVENTUSER, LASTEVENTCOMMENT) VALUES (:machineName, :processOperationGroupName, :processOperationName, :productSpecName, :position, "
				+ "  :reserveState, :reservedQuantity, :completeQuantity, :lastEventName, :lastEventTimeKey, :lastEventTime, :lastEventUser, :lastEventComment) ";   //Modify By hankun  Mantis-0000202

		Map<String, Object> bindMap = new HashMap<String, Object>();

		bindMap.put("machineName", machineName);
		bindMap.put("processOperationGroupName", processOperationGroupName);
		bindMap.put("processOperationName", processOperationName);
		bindMap.put("productSpecName", productSpecName);
		bindMap.put("position", position);
		bindMap.put("reserveState", reserveState);
		bindMap.put("reservedQuantity", reservedQuantity);
		bindMap.put("completeQuantity", completeQuantity);
		bindMap.put("lastEventName", eventInfo.getEventName());
		bindMap.put("lastEventTimeKey", StringUtil.isEmpty(eventInfo.getEventTimeKey()) ? TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()) : eventInfo.getEventTimeKey());
		bindMap.put("lastEventTime", eventInfo.getEventTime());
		bindMap.put("lastEventUser", eventInfo.getEventUser());
		bindMap.put("lastEventComment", eventInfo.getEventComment());

		long rv = GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);

		insertReserveProductSpecHist(eventInfo, machineName, processOperationGroupName, processOperationName, productSpecName, position, reserveState, reservedQuantity, completeQuantity);
	}

	public void insertReserveProductSpecHist(EventInfo eventInfo, String machineName, String processOperationGroupName, String processOperationName, String productSpecName, String position,
			String reserveState, String reservedQuantity, String completeQuantity)
	{
		//Modify By hankun Mantis - 0000202
		String sql = "INSERT INTO CT_RESERVEPRODUCTHIST (MACHINENAME, PROCESSOPERATIONGROUPNAME, PROCESSOPERATIONNAME, PRODUCTSPECNAME, POSITION, "
				+ " RESERVESTATE, RESERVEDQUANTITY, COMPLETEQUANTITY, TIMEKEY, EVENTNAME, EVENTUSER, EVENTCOMMENT) VALUES "
				+ " (:machineName, :processOperationGroupName, :processOperationName, :productSpecName, :position, "
				+ "  :reserveState, :reservedQuantity, :completeQuantity, :timeKey, :eventName, :eventUser, :eventComment) ";

		Map<String, Object> bindMap = new HashMap<String, Object>();

		bindMap.put("machineName", machineName);
		bindMap.put("processOperationGroupName", processOperationGroupName);
		bindMap.put("processOperationName", processOperationName);
		bindMap.put("productSpecName", productSpecName);
		bindMap.put("position", position);
		bindMap.put("reserveState", reserveState);
		bindMap.put("reservedQuantity", reservedQuantity);
		bindMap.put("completeQuantity", completeQuantity);
		bindMap.put("timeKey", StringUtil.isEmpty(eventInfo.getEventTimeKey()) ? TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()) : eventInfo.getEventTimeKey());
		bindMap.put("eventName", eventInfo.getEventName());
		bindMap.put("eventUser", eventInfo.getEventUser());
		bindMap.put("eventComment", eventInfo.getEventComment());

		long rv = GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);
	}

	public void updateReserveProductSpec(EventInfo eventInfo, String machineName, String processOperationGroupName, String processOperationName, String productSpecName, String position,
			String reserveState, String reservedQuantity, String completeQuantity) throws CustomException
	{
		List<Map<String, Object>> reserveProductSpecInfo = MESDSPServiceProxy.getDSPServiceUtil().getReserveProductSpecData(machineName, processOperationGroupName, processOperationName,
				productSpecName);

		if (StringUtil.equals(position, "SAME"))
			position = (String) reserveProductSpecInfo.get(0).get("POSITION");

		if (StringUtil.equals(reserveState, "SAME"))
			reserveState = (String) reserveProductSpecInfo.get(0).get("RESERVESTATE");

		if (StringUtil.equals(reservedQuantity, "SAME"))
			reservedQuantity = (String) reserveProductSpecInfo.get(0).get("RESERVEDQUANTITY");

		if (StringUtil.equals(completeQuantity, "SAME"))
			completeQuantity = (String) reserveProductSpecInfo.get(0).get("COMPLETEQUANTITY");

		//Modify By hankun Mantis - 0000202
		String sql = "UPDATE CT_RESERVEPRODUCT SET POSITION = :position, RESERVESTATE = :reserveState, RESERVEDQUANTITY = :reservedQuantity, COMPLETEQUANTITY = :completeQuantity , LASTEVENTNAME = :lastEventName, LASTEVENTTIMEKEY = :lastEventTimeKey, LASTEVENTTIME = :lastEventTime, LASTEVENTUSER = :lastEventUser, LASTEVENTCOMMENT = :lastEventComment"
				+ " WHERE MACHINENAME = :machineName AND PROCESSOPERATIONGROUPNAME = :processOperationGroupName AND PROCESSOPERATIONNAME = :processOperationName "
				+ " AND PRODUCTSPECNAME = :productSpecName ";

		Map<String, Object> bindMap = new HashMap<String, Object>();

		bindMap.put("machineName", machineName);
		bindMap.put("processOperationGroupName", processOperationGroupName);
		bindMap.put("processOperationName", processOperationName);
		bindMap.put("productSpecName", productSpecName);
		bindMap.put("position", position);
		bindMap.put("reserveState", reserveState);
		bindMap.put("reservedQuantity", reservedQuantity);
		bindMap.put("completeQuantity", completeQuantity);
		bindMap.put("lastEventName", eventInfo.getEventName());
		bindMap.put("lastEventTimeKey", StringUtil.isEmpty(eventInfo.getEventTimeKey()) ? TimeStampUtil.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()) : eventInfo.getEventTimeKey());
		bindMap.put("lastEventTime", eventInfo.getEventTime());
		bindMap.put("lastEventUser", eventInfo.getEventUser());
		bindMap.put("lastEventComment", eventInfo.getEventComment());

		long rv = GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);

		// insert SortJobHist
		insertReserveProductSpecHist(eventInfo, machineName, processOperationGroupName, processOperationName, productSpecName, position, reserveState, reservedQuantity, completeQuantity);
	}

	public void deleteReserveProductSpec(EventInfo eventInfo, String machineName, String processOperationGroupName, String processOperationName, String productSpecName) throws CustomException
	{
		List<Map<String, Object>> reserveProductSpecInfo = MESDSPServiceProxy.getDSPServiceUtil().getReserveProductSpecData(machineName, processOperationGroupName, processOperationName,
				productSpecName);

		String sql = "DELETE CT_RESERVEPRODUCT WHERE MACHINENAME = :machineName AND PROCESSOPERATIONGROUPNAME = :processOperationGroupName "
				+ " AND PROCESSOPERATIONNAME = :processOperationName AND PRODUCTSPECNAME = :productSpecName ";

		Map<String, Object> bindMap = new HashMap<String, Object>();
		bindMap.put("machineName", machineName);
		bindMap.put("processOperationGroupName", processOperationGroupName);
		bindMap.put("processOperationName", processOperationName);
		bindMap.put("productSpecName", productSpecName);

		long rv = GenericServiceProxy.getSqlMesTemplate().update(sql, bindMap);

		// insert SortJobHist
		insertReserveProductSpecHist(eventInfo, machineName, processOperationGroupName, processOperationName, productSpecName, (String) reserveProductSpecInfo.get(0).get("POSITION"),
				(String) reserveProductSpecInfo.get(0).get("RESERVESTATE"), (String) reserveProductSpecInfo.get(0).get("RESERVEDQUANTITY"),
				(String) reserveProductSpecInfo.get(0).get("COMPLETEQUANTITY"));
	}
}
