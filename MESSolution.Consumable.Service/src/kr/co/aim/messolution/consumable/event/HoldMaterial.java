package kr.co.aim.messolution.consumable.event;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.Element;

public class HoldMaterial extends SyncHandler {

	private static Log log = LogFactory.getLog(HoldMaterial.class);

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> materialElementList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("HoldMaterial", getEventUser(), getEventComment(), null, null);
		eventInfo.setReasonCode("WMSHOLD");
		eventInfo.setReasonCodeType("WMS");

		// TransportState isn't set. set the state in StockIn Screen.
		for (Element materialElement : materialElementList)
		{
			String materialKind = materialElement.getChildText("MATERIALKIND");
			String materialSpecName = materialElement.getChildText("MATERIALSPECNAME");
			String batchNo = materialElement.getChildText("BATCHNO");
			String wmsFactoryCode = materialElement.getChildText("WMSFACTORYCODE");
			String wmsFactoryPosition = materialElement.getChildText("WMSFACTORYPOSITION");

			if (checkExistData(materialSpecName, batchNo))
			{
				StringBuffer sql = new StringBuffer();
				sql.append("UPDATE MES_WMSIF_HOLD ");
				sql.append("   SET WMSFACTORYCODE = :WMSFACTORYCODE, ");
				sql.append("       WMSFACTORYPOSITION = :WMSFACTORYPOSITION, ");
				sql.append("       HOLDSTATE = :HOLDSTATE, ");
				sql.append("       HOLDUSER = :HOLDUSER, ");
				sql.append("       HOLDTIME = :HOLDTIME, ");
				sql.append("       HOLDREASON = :HOLDREASON, ");
				sql.append("       RELEASESYSTEM = '', ");
				sql.append("       RELEASEUSER = '', ");
				sql.append("       RELEASETIME = '', ");
				sql.append("       INTERFACEDATE = :INTERFACEDATE, ");
				sql.append("       INTERFACEFLAG = :INTERFACEFLAG, ");
				sql.append("       INTERFACECOMMENT = :INTERFACECOMMENT ");
				sql.append(" WHERE MATERIALSPECNAME = :MATERIALSPECNAME ");
				sql.append("   AND BATCHNO = :BATCHNO ");

				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("WMSFACTORYCODE", wmsFactoryCode);
				bindMap.put("WMSFACTORYPOSITION", wmsFactoryPosition);
				bindMap.put("HOLDSTATE", "Y");
				bindMap.put("HOLDUSER", eventInfo.getEventUser());
				bindMap.put("HOLDTIME", eventInfo.getEventTime());
				bindMap.put("HOLDREASON", eventInfo.getReasonCode());
				bindMap.put("RELEASESYSTEM", "");
				bindMap.put("RELEASEUSER", "");
				bindMap.put("RELEASETIME", "");
				bindMap.put("INTERFACEDATE", new SimpleDateFormat("yyyyMMdd").format(eventInfo.getEventTime()));
				bindMap.put("INTERFACEFLAG", "");
				bindMap.put("INTERFACECOMMENT", eventInfo.getEventComment());
				bindMap.put("MATERIALSPECNAME", materialSpecName);
				bindMap.put("BATCHNO", batchNo);

				greenFrameServiceProxy.getSqlTemplate().update(sql.toString(), bindMap);
			}
			else
			{
				StringBuffer sql = new StringBuffer();
				sql.append("INSERT INTO MES_WMSIF_HOLD ");
				sql.append("   (MATERIALSPECNAME, BATCHNO, WMSFACTORYCODE, WMSFACTORYPOSITION, HOLDSTATE,  ");
				sql.append("    HOLDUSER, HOLDTIME, HOLDREASON, RELEASESYSTEM, RELEASEUSER,  ");
				sql.append("    RELEASETIME, INTERFACEDATE, INTERFACEFLAG, INTERFACECOMMENT) ");
				sql.append(" VALUES ");
				sql.append("   (:MATERIALSPECNAME, :BATCHNO, :WMSFACTORYCODE, :WMSFACTORYPOSITION, :HOLDSTATE,  ");
				sql.append("    :HOLDUSER, :HOLDTIME, :HOLDREASON, :RELEASESYSTEM, :RELEASEUSER,  ");
				sql.append("    :RELEASETIME, :INTERFACEDATE, :INTERFACEFLAG, :INTERFACECOMMENT) ");

				Map<String, Object> bindMap = new HashMap<String, Object>();
				bindMap.put("MATERIALSPECNAME", materialSpecName);
				bindMap.put("BATCHNO", batchNo);
				bindMap.put("WMSFACTORYCODE", wmsFactoryCode);
				bindMap.put("WMSFACTORYPOSITION", wmsFactoryPosition);
				bindMap.put("HOLDSTATE", "Y");
				bindMap.put("HOLDUSER", eventInfo.getEventUser());
				bindMap.put("HOLDTIME", eventInfo.getEventTime());
				bindMap.put("HOLDREASON", eventInfo.getReasonCode());
				bindMap.put("RELEASESYSTEM", "");
				bindMap.put("RELEASEUSER", "");
				bindMap.put("RELEASETIME", "");
				bindMap.put("INTERFACEDATE", new SimpleDateFormat("yyyyMMdd").format(eventInfo.getEventTime()));
				bindMap.put("INTERFACEFLAG", "");
				bindMap.put("INTERFACECOMMENT", eventInfo.getEventComment());

				greenFrameServiceProxy.getSqlTemplate().update(sql.toString(), bindMap);
			}
			
			if (StringUtils.equals("Consumable", materialKind))
			{
				this.holdConsumable(eventInfo, materialSpecName, batchNo);
			}
			else if (StringUtils.equals("Durable", materialKind))
			{
				this.holdDurable(eventInfo, materialSpecName, batchNo);
			}
		}

		return doc;
	}

	private void holdConsumable(EventInfo eventInfo, String consumableSpecName, String batchNo)
	{
		String condition = " BATCHNO = ? AND CONSUMABLESPECNAME = ? AND NVL(CONSUMABLEHOLDSTATE, 'N') = 'N' ";
		Object[] bindSet = new Object[] { batchNo, consumableSpecName };

		try
		{
			List<Consumable> consumableList = ConsumableServiceProxy.getConsumableService().select(condition, bindSet);

			for (Consumable consumableData : consumableList)
			{
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("CONSUMABLEHOLDSTATE", "Y");

				MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(consumableData.getKey().getConsumableName(), setEventInfo, eventInfo);
			}
		}
		catch (Exception e)
		{
			log.info("ConsumableData is Not Exist");
		}
	}

	private void holdDurable(EventInfo eventInfo, String durableSpecName, String batchNo)
	{
		String condition = " BATCHNO = ? AND DURABLESPECNAME = ? AND NVL(DURABLEHOLDSTATE, 'N') = 'N' ";
		Object[] bindSet = new Object[] { batchNo, durableSpecName };

		try
		{
			List<Durable> durableList = DurableServiceProxy.getDurableService().select(condition, bindSet);

			for (Durable durableData : durableList)
			{
				kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
				setEventInfo.getUdfs().put("DURABLEHOLDSTATE", "Y");

				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
			}
		}
		catch (Exception e)
		{
			log.info("DurableData is Not Exist");
		}
	}

	private boolean checkExistData(String materialSpecName, String batchNo)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT * ");
		sql.append("  FROM MES_WMSIF_HOLD ");
		sql.append(" WHERE MATERIALSPECNAME = :MATERIALSPECNAME ");
		sql.append("   AND BATCHNO = :BATCHNO ");

		Map<String, Object> args = new HashMap<String, Object>();
		args.put("MATERIALSPECNAME", materialSpecName);
		args.put("BATCHNO", batchNo);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), args);

		boolean flag = false;

		if (sqlResult.size() > 0)
			flag = true;

		return flag;
	}

}
