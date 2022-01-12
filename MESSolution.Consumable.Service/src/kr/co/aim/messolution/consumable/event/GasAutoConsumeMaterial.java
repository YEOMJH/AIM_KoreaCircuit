package kr.co.aim.messolution.consumable.event;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.machine.MESMachineServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;


import org.jdom.Document;
import org.jdom.Element;

import com.sun.org.apache.bcel.internal.generic.RETURN;

public class GasAutoConsumeMaterial extends SyncHandler{

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> excelList = SMessageUtil.getBodySequenceItemList(doc, "EXCELLIST", true);
		List<Element> movelList = SMessageUtil.getBodySequenceItemList(doc, "MOVELIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("GasAutoConsumeMaterial", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		//2021-03-31 ghhan Change Batch
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO CT_GASAUTOCONSUMEMATERIAL(SEQ, FACTORYCODE,FACTORYPOSITION, ");
		sql.append("  MATERIALSPECNAME,BATCHNO,STARTTIME,ENDTIME,QUANTITY,EVENTUSER,EVENTCOMMENT)  ");
		sql.append("  VALUES(CONCAT(TO_CHAR(SYSTIMESTAMP, 'YYYYMMDDHH24MISSFF3'), LPAD(TIMEKEYID.NEXTVAL, 3, '0')), ?, ?, ?, ?, ?, ");
		sql.append("   ?, ?, ?, ?) ");
		
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		
		//InsertDB CT_GASAUTOCONSUMEMATERIAL
		for (Element excelColumn : excelList)
		{
			String factotyCode = SMessageUtil.getChildText(excelColumn, "FACTORYCODE", true);
			String factoryPosition = SMessageUtil.getChildText(excelColumn, "FACTORYPOSITION", true);
			String materialSpecName = SMessageUtil.getChildText(excelColumn, "MATERIALSPECNAME", true);
			String batchNo = SMessageUtil.getChildText(excelColumn, "BATCHNO", false);
			String startTime = SMessageUtil.getChildText(excelColumn, "STARTTIME", true);
			String endTime = SMessageUtil.getChildText(excelColumn, "ENDTIME", true);
			String quantity = SMessageUtil.getChildText(excelColumn, "QUANTITY", true);
			
			List<Object> lotBindList = new ArrayList<Object>();
			
			lotBindList.add(factotyCode);
			lotBindList.add(factoryPosition);
			lotBindList.add(materialSpecName);
			lotBindList.add(batchNo);
			lotBindList.add(startTime);
			lotBindList.add(endTime);
			lotBindList.add(quantity);
			lotBindList.add(eventInfo.getEventUser());
			lotBindList.add(eventInfo.getEventComment());
			
			updateLotArgList.add(lotBindList.toArray());
		}
		
		MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateLotArgList);
		
		//Report To SAP
		List<List<Map<String, String>>> ERPReportBatchList = new ArrayList<List<Map<String, String>>>();
		for (Element moveColumn : movelList)
		{
			String productReuqestName = SMessageUtil.getChildText(moveColumn, "PRODUCTREQUESTNAME", true);
			String materialSpecName = SMessageUtil.getChildText(moveColumn, "MATERIALSPECNAME", true);
			String processOperationName = SMessageUtil.getChildText(moveColumn, "PROCESSOPERATIONNAME", true);
			String quantity = SMessageUtil.getChildText(moveColumn, "QUANTITY", true);
			String consumeUnit = SMessageUtil.getChildText(moveColumn, "CONSUMEUNIT", true);
			String factotyCode = SMessageUtil.getChildText(moveColumn, "FACTORYCODE", true);
			String factoryPosition = SMessageUtil.getChildText(moveColumn, "FACTORYPOSITION", true);
			String batchNo = SMessageUtil.getChildText(moveColumn, "BATCHNO", false);
			String productQuantity = SMessageUtil.getChildText(moveColumn, "PRODUCTQUANTITY", true);

			List<Map<String, String>> ERPReportList = new ArrayList<Map<String, String>>();
			Map<String, String> ERPInfo = new HashMap<>();
			
			ERPInfo.put("SEQ", TimeUtils.getCurrentEventTimeKey());
			ERPInfo.put("PRODUCTREQUESTNAME", productReuqestName);
			ERPInfo.put("MATERIALSPECNAME", materialSpecName);
			ERPInfo.put("PROCESSOPERATIONNAME", processOperationName);
			ERPInfo.put("QUANTITY", quantity);
			ERPInfo.put("CONSUMEUNIT", consumeUnit);

			ERPInfo.put("FACTORYCODE", factotyCode);
			ERPInfo.put("FACTORYPOSITION",factoryPosition);

		    ERPInfo.put("BATCHNO", batchNo);		
			ERPInfo.put("PRODUCTQUANTITY",productQuantity);
			
			Calendar cal = Calendar.getInstance();
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			if(hour >= 19)
			{
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				cal.add(Calendar.DAY_OF_MONTH, 1);
				Timestamp receiveTime = new Timestamp(cal.getTime().getTime());
				ERPInfo.put("EVENTTIME", receiveTime.toString().replace("-","").substring(0,8));
			}
			else
			{
				ERPInfo.put("EVENTTIME", eventInfo.getEventTimeKey().substring(0,8));
			}
			ERPInfo.put("CANCELFLAG", "");
			ERPInfo.put("WSFLAG", "");
			
			ERPReportList.add(ERPInfo);
			ERPReportBatchList.add(ERPReportList);
			
			eventInfo.setEventName("GasAutoConsumeMaterial");
			
		}
		//Send
		try
		{
			ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().sapVXG372SendBatch(eventInfo, ERPReportBatchList,1);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return doc;
	}
	
}
