package kr.co.aim.messolution.consumable.event;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
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

public class ReleaseHoldMaterial extends SyncHandler {
	private static Log log = LogFactory.getLog(ReleaseHoldMaterial.class);
	
	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> materialElementList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ReleaseHoldMaterial", getEventUser(), getEventComment(), null, null);

		// TransportState isn't set. set the state in StockIn Screen.
		for (Element materialElement : materialElementList)
		{
			String materialKind = materialElement.getChildText("MATERIALKIND");
			String materialSpecName = materialElement.getChildText("MATERIALSPECNAME");
			String batchNo = materialElement.getChildText("BATCHNO");

			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE MES_WMSIF_HOLD ");
			sql.append("   SET HOLDSTATE = :HOLDSTATE, ");
			sql.append("       RELEASESYSTEM = :RELEASESYSTEM, ");
			sql.append("       RELEASEUSER = :RELEASEUSER, ");
			sql.append("       RELEASETIME = :RELEASETIME ");
			sql.append(" WHERE MATERIALSPECNAME = :MATERIALSPECNAME ");
			sql.append("   AND BATCHNO = :BATCHNO ");

			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("HOLDSTATE", "N");
			bindMap.put("RELEASESYSTEM", "OIC");
			bindMap.put("RELEASEUSER", eventInfo.getEventUser());
			bindMap.put("RELEASETIME", eventInfo.getEventTime());
			bindMap.put("INTERFACEDATE", new SimpleDateFormat("yyyyMMdd").format(eventInfo.getEventTime()));
			bindMap.put("INTERFACEFLAG", "");
			bindMap.put("INTERFACECOMMENT", eventInfo.getEventComment());
			bindMap.put("MATERIALSPECNAME", materialSpecName);
			bindMap.put("BATCHNO", batchNo);

			greenFrameServiceProxy.getSqlTemplate().update(sql.toString(), bindMap);

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
		String condition = " BATCHNO = ? AND CONSUMABLESPECNAME = ? AND CONSUMABLEHOLDSTATE = 'Y' ";
		Object[] bindSet = new Object[] { batchNo, consumableSpecName };

		try
		{
			List<Consumable> consumableList = ConsumableServiceProxy.getConsumableService().select(condition, bindSet);

			for (Consumable consumableData : consumableList)
			{
				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("CONSUMABLEHOLDSTATE", "N");

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
		String condition = " BATCHNO = ? AND DURABLESPECNAME = ? AND DURABLEHOLDSTATE = 'Y' ";
		Object[] bindSet = new Object[] { batchNo, durableSpecName };

		try
		{
			List<Durable> durableList = DurableServiceProxy.getDurableService().select(condition, bindSet);

			for (Durable durableData : durableList)
			{
				kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
				setEventInfo.getUdfs().put("DURABLEHOLDSTATE", "N");

				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durableData, setEventInfo, eventInfo);
			}
		}
		catch (Exception e)
		{
			log.info("DurableData is Not Exist");
		}
	}

}
