package kr.co.aim.messolution.consumable.event;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;
import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CrucibleLot;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class UnkitCrucible extends SyncHandler {
	private static Log log = LogFactory.getLog(UnkitCrucible.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String crucibleName = SMessageUtil.getBodyItemValue(doc, "CRUCIBLENAME", true);

		List<Element> organicList = SMessageUtil.getBodySequenceItemList(doc, "ORGANICLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnkitCrucible", getEventUser(), getEventComment(), null, null);

		Durable crucibleData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(crucibleName);
		CrucibleLot crucibleLotData = ExtendedObjectProxy.getCrucibleLotService().getCrucibleLotList(crucibleName).get(0);

		for (Element organic : organicList)
		{									
			String organicName = organic.getChildText("CONSUMABLENAME");

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
			//setEventInfo.getUdfs().put("KITQUANTITY", "0");
			setEventInfo.getUdfs().put("UNKITTIME", TimeStampUtil.toTimeString(eventInfo.getEventTime()));
			//setEventInfo.getUdfs().put("KITUSER", "");
			
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
		  //houxk Start
		crucibleLotData.setCrucibleWeight(0);
		crucibleLotData.setLastEventComment(eventInfo.getEventComment());
		crucibleLotData.setLastEventName(eventInfo.getEventName());
		crucibleLotData.setLastEventTime(eventInfo.getEventTime());
		crucibleLotData.setLastEventTimekey(TimeStampUtil.getCurrentEventTimeKey());
		crucibleLotData.setLastEventUser(eventInfo.getEventUser());
		crucibleLotData.setUnkitTime(eventInfo.getEventTime());
		  //houxk End	
		ExtendedObjectProxy.getCrucibleLotService().modify(eventInfo, crucibleLotData);		

		return doc;
	}
	
	private void sendToSAP(String consumableName,String consumableSpecName,String kitTime,
			String UnKitTime,String factoryCode,String factoryPosition,String batchNo,Double oriQty) throws CustomException
	{
		//check ERP Skip Material
		StringBuffer sql0 = new StringBuffer();
		sql0.append("SELECT DESCRIPTION,DEFAULTFLAG ");
		sql0.append("  FROM ENUMDEFVALUE  ");
		sql0.append("  WHERE ENUMNAME = 'ERPSkipMaterial' ");
		sql0.append("  AND DESCRIPTION = :DESCRIPTION ");
		sql0.append("  AND (DEFAULTFLAG = :DEFAULTFLAG OR :DEFAULTFLAG IS NULL) ");
		
		Object[] bindArray0 = new Object[]{consumableSpecName,batchNo,batchNo};	
		
		List<Map<String, Object>> result0 = GenericServiceProxy.getSqlMesTemplate().queryForList(sql0.toString(), bindArray0);
		
		if(result0 ==null || result0.size()==0)
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnKitCrucible", getEventUser(), getEventComment(), "","");
			
			//Confirm OriQty
			Consumable organicData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(consumableName);
			Consumable oriOrganicData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(consumableName.substring(0, consumableName.lastIndexOf("-")));
			double dOriQty = 0;
			if(StringUtils.equals(oriOrganicData.getConsumableState(), "Available"))
			{
				dOriQty = oriQty;
			}
			else if(StringUtils.equals(oriOrganicData.getConsumableState(), "NotAvailable"))
			{
				String organicName = consumableName.substring(0, consumableName.lastIndexOf("-"))+"-%";
				StringBuffer sql3 = new StringBuffer();
				sql3.append("SELECT * FROM CONSUMABLE ");
				sql3.append("  WHERE CONSUMABLENAME LIKE :CONSUMABLENAME ");
				sql3.append("  ORDER BY CONSUMABLENAME DESC ");
				
				Object[] bindArray3 = new Object[]{organicName};	
				
				List<Map<String, Object>> result3 = GenericServiceProxy.getSqlMesTemplate().queryForList(sql3.toString(), bindArray3);
				
				if(result3 !=null && result3.size()>0)
				{
					if(StringUtils.equals(consumableName, result3.get(0).get("CONSUMABLENAME").toString()))
					{
						dOriQty = new BigDecimal(oriQty).add(new BigDecimal(oriOrganicData.getQuantity())).doubleValue();
					}
					else
					{
						dOriQty = oriQty;
					}
				}
			}
			log.info("OriQty="+dOriQty);	
			
			String consumeUnit="";			
			double consumeQty = 0;
			double qty = 0;		
			BigDecimal totalConsumeQty = new BigDecimal(consumeQty);
			BigDecimal bQty = new BigDecimal(qty);
			BigDecimal bOriQty = new BigDecimal(dOriQty);
			
			//Confirm ConsumeUnit
			StringBuffer sql1 = new StringBuffer();
			sql1.append("SELECT DISTINCT CONSUMEUNIT ");
			sql1.append("  FROM CT_ERPBOM ");
			sql1.append(" WHERE MATERIALSPECNAME = ? ");
			
			Object[] bindArray1 = new Object[]{consumableSpecName};	
			
			List<Map<String, Object>> result1 = GenericServiceProxy.getSqlMesTemplate().queryForList(sql1.toString(), bindArray1);
			
			if(result1 !=null && result1.size()>0)
			{
				consumeUnit=result1.get(0).get("CONSUMEUNIT").toString();
			}
						
			//Confirm Qty
			StringBuffer sql2 = new StringBuffer();
			sql2.append(" SELECT W.SUPERPRODUCTREQUESTNAME,E.MATERIALSPECNAME,M.MATERIALNAME,E.QUANTITY,COUNT(M.PRODUCTNAME) AS COUNT ");
			sql2.append(" FROM CT_MATERIALPRODUCT M, PRODUCTHISTORY P, PRODUCTREQUEST W, CT_SUPERPRODUCTREQUEST S, CONSUMABLE C, CT_ERPBOM E ");
			sql2.append(" WHERE SUBSTR(M.PRODUCTNAME,1,11) = P.PRODUCTNAME ");
			sql2.append(" AND M.MATERIALNAME = C.CONSUMABLENAME ");
			sql2.append(" AND C.CONSUMABLESPECNAME = E.MATERIALSPECNAME ");
			sql2.append(" AND S.PRODUCTREQUESTNAME = E.PRODUCTREQUESTNAME ");
			sql2.append(" AND P.PRODUCTREQUESTNAME = W.PRODUCTREQUESTNAME ");
			sql2.append(" AND W.SUPERPRODUCTREQUESTNAME = S.PRODUCTREQUESTNAME ");
			sql2.append(" AND M.MATERIALTYPE IN('Organic','InOrganic') ");
			sql2.append(" AND M.MATERIALNAME =:MATERIALNAME ");
			sql2.append(" AND P.EVENTNAME='Receive' ");
			sql2.append(" AND (SUBSTR(P.PRODUCTREQUESTNAME,9,1)='F' OR SUBSTR(P.PRODUCTREQUESTNAME,9,1)='C') ");
			sql2.append(" GROUP BY W.SUPERPRODUCTREQUESTNAME,E.MATERIALSPECNAME,M.MATERIALNAME,E.QUANTITY ");

			Object[] bindArray2 = new Object[]{consumableName};
			
			List<Map<String, Object>> result2 = GenericServiceProxy.getSqlMesTemplate().queryForList(sql2.toString(), bindArray2);
			if(result2 != null && result2.size()>0)
			{
				for(Map<String, Object> data : result2)
				{
					BigDecimal unitConsumeQty = new BigDecimal(Double.parseDouble(data.get("QUANTITY").toString()));
					BigDecimal count = new BigDecimal(Double.parseDouble(data.get("COUNT").toString()));
					BigDecimal bConsumeQty = unitConsumeQty.multiply(count);
					
					totalConsumeQty = totalConsumeQty.add(bConsumeQty);
				}
				bQty = bOriQty.subtract(totalConsumeQty);
				qty = bQty.doubleValue();		
				log.info("BomQty="+String.format("%.3f", totalConsumeQty.doubleValue()));
			}
			
			//Update 
			if(qty != 0)
			{
				double qty2 = -qty;
				log.info("Qty="+qty2);
				
				StringBuffer sql = new StringBuffer();
				sql.append("INSERT INTO MES_SAPIF_PP010@OADBLINK.V3FAB.COM ");//@db-link
				sql.append(" ( SEQ, MATERIALSPECNAME, QUANTITY,");
				sql.append("   CONSUMEUNIT, FACTORYCODE, FACTORYPOSITION, BATCHNO,");
				sql.append("   KITTIME, UNKITTIME, ESBFLAG, RESULT, RESULTMESSAGE ,EVENTUSER ,EVENTCOMMENT)");
				sql.append("VALUES  ");
				sql.append(" ( :seq, :materialspecname, :quantity,  ");
				sql.append("   :consumunit, :factorycode, :factoryposition, :batchno,");
				sql.append("   :kittime, :unkittime, :esbflag,:result, :resultmessage,:eventName, :eventComment)");

				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("seq", TimeUtils.getCurrentEventTimeKey());
				bindMap.put("materialspecname",consumableSpecName);
				bindMap.put("quantity",String.format("%.3f", qty2));
				bindMap.put("consumunit",consumeUnit);
				bindMap.put("factorycode",factoryCode); 
				bindMap.put("factoryposition",factoryPosition);
				bindMap.put("batchno", batchNo);
				bindMap.put("kittime", kitTime);
				bindMap.put("unkittime", UnKitTime);
				bindMap.put("esbflag", "N");
				bindMap.put("result", "");
				bindMap.put("resultmessage", "");
				bindMap.put("eventName", getEventUser());
				bindMap.put("eventComment", getEventComment());	
				
				try
				{
					GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
				}
				catch(Exception e)
		        {
					eventLog.info("DBLink Update filed");
		        }			
			}
		}				
	}
}
