package kr.co.aim.messolution.consumable.event;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import org.jdom.Document;
import org.jdom.Element;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CrucibleLot;
import kr.co.aim.messolution.extended.webinterface.ExtendedWebInterfaceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.messolution.consumable.event.KitBatchCrucible;

public class UnKitBatchCrucible extends SyncHandler {
	private static Log log = LogFactory.getLog(UnkitCrucible.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnKitBatchCrucible", getEventUser(), getEventComment(), null, null);
		eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
		
		List<Element> crucibleList = SMessageUtil.getBodySequenceItemList(doc, "CRUCIBLELIST", true);
		List<Element> moveList = SMessageUtil.getBodySequenceItemList(doc, "MOVELIST", false);
		List<Element> excelList = SMessageUtil.getBodySequenceItemList(doc, "EXCELLIST", false);		
		String onlySAP = SMessageUtil.getBodyItemValue(doc, "ONLYSAP", true);
		
		if(StringUtils.equals(onlySAP, "FALSE"))
		{
			for (Element crucible : crucibleList)
			{
				String crucibleName = SMessageUtil.getChildText(crucible, "CRUCIBLENAME", true);
				
				List<Map<String, Object>> organicList = KitBatchCrucible.getOrganicListByCrucible(crucibleName);

				Durable crucibleData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(crucibleName);
				CrucibleLot crucibleLotData = ExtendedObjectProxy.getCrucibleLotService().getCrucibleLotList(crucibleName).get(0);

				for (Map<String, Object> organic : organicList)
				{								
					String organicName = organic.get("CONSUMABLENAME").toString();

					Consumable organicData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(organicName);
					
					String kitTime = "";
					String unKitTime = "";
					double oriQty = 0;
					DateFormat sdf = new SimpleDateFormat("yyyyMMdd");
					kitTime = sdf.format(Timestamp.valueOf(organicData.getUdfs().get("KITTIME")));
					unKitTime = sdf.format(eventInfo.getEventTime());
					oriQty = organicData.getQuantity();
					
					organicData.setMaterialLocationName("");
					organicData.setQuantity(0);
					organicData.setConsumableState("NotAvailable");

					SetEventInfo setEventInfo = new SetEventInfo();
					setEventInfo.getUdfs().put("CARRIERNAME", "");
					setEventInfo.getUdfs().put("MACHINENAME", "");
					setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_Bank);
					setEventInfo.getUdfs().put("ASSIGNEDQTY", "0");
					setEventInfo.getUdfs().put("CRUCIBLELOTNAME", "");
					setEventInfo.getUdfs().put("KITQUANTITY", "0");
					setEventInfo.getUdfs().put("UNKITTIME", TimeStampUtil.toTimeString(eventInfo.getEventTime()));
					setEventInfo.getUdfs().put("KITUSER", "");

					ConsumableServiceProxy.getConsumableService().update(organicData);
					MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(organicData.getKey().getConsumableName(), setEventInfo, eventInfo);
					
					if(CommonUtil.equalsIn(organicData.getConsumableType(),"Organic","InOrganic"))
					{
						//sendToSAP(organicName,organicData.getConsumableSpecName(),kitTime,unKitTime,"5001","OF01",organicData.getUdfs().get("BATCHNO"),oriQty);
					}	
				}

				// Update CrucibleData
				crucibleData.setLotQuantity(0);
				crucibleData.setMaterialLocationName("");
				crucibleData.setDurableState("Available");

				kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
				setEventInfo.getUdfs().put("MACHINENAME", "");
				setEventInfo.getUdfs().put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_OUTEQP);

				DurableServiceProxy.getDurableService().update(crucibleData);
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(crucibleData, setEventInfo, eventInfo);

				// Update CrucibleLotData
				crucibleLotData.setCrucibleLotState("Completed");
				crucibleLotData.setDurableName("");
				crucibleLotData.setWeight(0);		
				crucibleLotData.setCrucibleWeight(0);
				crucibleLotData.setLastEventComment(eventInfo.getEventComment());
				crucibleLotData.setLastEventName(eventInfo.getEventName());
				crucibleLotData.setLastEventTime(eventInfo.getEventTime());
				crucibleLotData.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
				crucibleLotData.setLastEventUser(eventInfo.getEventUser());
				crucibleLotData.setUnkitTime(eventInfo.getEventTime());
				ExtendedObjectProxy.getCrucibleLotService().modify(eventInfo, crucibleLotData);
				
			}	
			sendToSAP(excelList,moveList,eventInfo);
		}
		else 
		{
			for (Element crucible : crucibleList)
			{
                String crucibleName = SMessageUtil.getChildText(crucible, "CRUCIBLENAME", true);
                String consumeRatio = SMessageUtil.getChildText(crucible, "RATIO", true);	
				CrucibleLot crucibleLotData = ExtendedObjectProxy.getCrucibleLotService().getCrucibleLotList(crucibleName).get(0);
				crucibleLotData.setConsumeRatio(consumeRatio);
				crucibleLotData.setLastEventComment(eventInfo.getEventComment());
				crucibleLotData.setLastEventName("OnlyToSAP");
				crucibleLotData.setLastEventTime(eventInfo.getEventTime());
				crucibleLotData.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
				crucibleLotData.setLastEventUser(eventInfo.getEventUser());
				ExtendedObjectProxy.getCrucibleLotService().modify(eventInfo, crucibleLotData);
			}
			sendToSAP(excelList,moveList,eventInfo);
		}
			
		return doc;
	}
	
	private void sendToSAP(List<Element> excelList,List<Element> moveList,EventInfo eventInfo) throws CustomException
	{
		StringBuffer sql = new StringBuffer();
		sql.append("INSERT INTO CT_GASAUTOCONSUMEMATERIAL(SEQ, FACTORYCODE,FACTORYPOSITION, ");
		sql.append("  MATERIALSPECNAME,BATCHNO,STARTTIME,ENDTIME,QUANTITY,EVENTUSER,EVENTCOMMENT,MACHINENAME)  ");
		sql.append("  VALUES(CONCAT(TO_CHAR(SYSTIMESTAMP, 'YYYYMMDDHH24MISSFF3'), LPAD(TIMEKEYID.NEXTVAL, 3, '0')), ?, ?, ?, ?, ?, ");
		sql.append("   ?, ?, ?, ?,?) ");
		
		List<Object[]> updateLotArgList = new ArrayList<Object[]>();
		
		//InsertDB CT_GASAUTOCONSUMEMATERIAL
		for (Element excelColumn : excelList)
		{
			String factotyCode ="5001";
			String factoryPosition = "OF01";
			String materialSpecName = SMessageUtil.getChildText(excelColumn, "MATERIALSPECNAME", false);
			String batchNo = SMessageUtil.getChildText(excelColumn, "BATCHNO", false);
			String startTime = SMessageUtil.getChildText(excelColumn, "STARTTIME", false);
			String endTime = SMessageUtil.getChildText(excelColumn, "ENDTIME", false);
			String quantity = SMessageUtil.getChildText(excelColumn, "QUANTITY", false);
			String machineName = SMessageUtil.getChildText(excelColumn, "MACHINENAME", false);
			
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
			lotBindList.add(machineName);
			
			updateLotArgList.add(lotBindList.toArray());
		}
		if(updateLotArgList.size()>0)
		{
			MESLotServiceProxy.getLotServiceUtil().updateBatch(sql.toString(), updateLotArgList);
		}
		//Report To SAP
		List<List<Map<String, String>>> ERPReportBatchList = new ArrayList<List<Map<String, String>>>();
		for (Element moveColumn : moveList)
		{
			String productReuqestName = SMessageUtil.getChildText(moveColumn, "PRODUCTREQUESTNAME", true);
			String materialSpecName = SMessageUtil.getChildText(moveColumn, "MATERIALSPECNAME", true);
			String processOperationName = SMessageUtil.getChildText(moveColumn, "PROCESSOPERATIONNAME", true);
			String quantity = SMessageUtil.getChildText(moveColumn, "QUANTITY", true);
			String consumeUnit = SMessageUtil.getChildText(moveColumn, "CONSUMEUNIT", true);
			String factotyCode = SMessageUtil.getChildText(moveColumn, "FACTORYCODE", true);
			String factoryPosition = SMessageUtil.getChildText(moveColumn, "FACTORYPOSITION", true);
			String batchNo = SMessageUtil.getChildText(moveColumn, "BATCHNO", true);
			String productQuantity = SMessageUtil.getChildText(moveColumn, "PRODUCTQUANTITY", true);
			String mFlag = SMessageUtil.getChildText(moveColumn, "MFLAG", false);
			
			if(StringUtils.equals(quantity, "0"))
			{
				continue;
			}

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
            if(StringUtils.equals(mFlag, "Y"))
            {
            	ERPInfo.put("PRODUCTQUANTITY","0");
            }
            else
            {
    			ERPInfo.put("PRODUCTQUANTITY",productQuantity);
            }
			
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
			if(StringUtils.equals(mFlag, "Y")&&StringUtils.contains(quantity, "-"))
			{
				ERPInfo.put("CANCELFLAG", "X");
				ERPInfo.put("WSFLAG", "");
			}
			else
			{
				ERPInfo.put("CANCELFLAG", "");
				ERPInfo.put("WSFLAG", "");
			}		
			ERPReportList.add(ERPInfo);
			ERPReportBatchList.add(ERPReportList);
			
			eventInfo.setEventName("UnKitBatchCrucible");
			
		}
		//Send
		try
		{
			if(ERPReportBatchList.size()>0)
			{
				ExtendedWebInterfaceProxy.getExtendedWebInterfaceServiceImpl().sapVXG372SendBatch(eventInfo, ERPReportBatchList,1);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
