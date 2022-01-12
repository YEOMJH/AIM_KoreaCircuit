package kr.co.aim.messolution.consumable.event;

import java.util.ArrayList;
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
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableHistory;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class ModifyCrucible extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> eMaterialList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Modify", getEventUser(), getEventComment(), null, null);

		for (Element materialE : eMaterialList)
		{
			String materialID = materialE.getChildText("MATERIALID");
			String materialKind = materialE.getChildText("MATERIALKIND");
			String quantity = materialE.getChildText("QUANTITY");
			String capacity = materialE.getChildText("CAPACITY");
			String timeUsedLimit = materialE.getChildText("TIMEUSEDLIMIT");
			String timeUsed = materialE.getChildText("TIMEUSED");
			String durationUsedLimit = materialE.getChildText("DURATIONUSEDLIMIT");
			String durationUsed = materialE.getChildText("DURATIONUSED");
			String materialState = materialE.getChildText("MATERIALSTATE");
			String transportState = materialE.getChildText("TRANSPORTSTATE");
			String holdState = materialE.getChildText("HOLDSTATE");
			String materialLocationName = materialE.getChildText("MATERIALLOCATIONNAME");
			String cleanState = materialE.getChildText("CLEANSTATE");
			String machineName = materialE.getChildText("MACHINENAME");

			if (materialKind.equals(GenericServiceProxy.getConstantMap().MaterialKind_Consumable))
			{
				Consumable consumable = MESConsumableServiceProxy.getConsumableInfoUtil().getMaterialData(materialID);

				double oriQty = consumable.getQuantity();

				consumable.setQuantity(Double.parseDouble(quantity));
				consumable.setConsumableState(materialState);
				consumable.setMaterialLocationName(materialLocationName);
				ConsumableServiceProxy.getConsumableService().update(consumable);

				SetEventInfo setEventInfo = new SetEventInfo();
				setEventInfo.getUdfs().put("TIMEUSEDLIMIT", timeUsedLimit);
				setEventInfo.getUdfs().put("TIMEUSED", timeUsed);
				setEventInfo.getUdfs().put("DURATIONUSEDLIMIT", durationUsedLimit);
				setEventInfo.getUdfs().put("DURATIONUSED", durationUsed);
				setEventInfo.getUdfs().put("TRANSPORTSTATE", transportState);
				setEventInfo.getUdfs().put("CONSUMABLEHOLDSTATE", holdState);
				setEventInfo.getUdfs().put("MACHINENAME", machineName);
				
				MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(materialID, setEventInfo, eventInfo);

				String condition = "where consumablename=? and timekey=(select max(timekey) from consumablehistory where consumablename=? and eventname = ? ) ";
				Object[] bindSet = new Object[] { materialID, materialID, "Modify" };

				List<ConsumableHistory> conHistList = new ArrayList<ConsumableHistory>();
				try
				{
					conHistList = ConsumableServiceProxy.getConsumableHistoryService().select(condition, bindSet);

					ConsumableHistory conHist = conHistList.get(0);
					conHist.setOldQuantity(oriQty);

					ConsumableServiceProxy.getConsumableHistoryService().update(conHist);
				}
				catch (Exception e)
				{

				}
			}
			else
			{
				Durable durable = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(materialID);

				durable.setCapacity(Long.parseLong(capacity));
				durable.setDurableState(materialState);
				durable.setTimeUsedLimit(Double.parseDouble(timeUsedLimit));
				durable.setTimeUsed(Double.parseDouble(timeUsed));
				durable.setDurationUsedLimit(Double.parseDouble(durationUsedLimit));
				durable.setDurationUsed(Double.parseDouble(durationUsed));
				durable.setDurableCleanState(cleanState);

				Map<String, String> udfs = new HashMap<String, String>();

				udfs.put("TRANSPORTSTATE", transportState);
				udfs.put("DURABLEHOLDSTATE", holdState);
				udfs.put("MACHINENAME", machineName);
				
				durable.setUdfs(udfs);

				DurableServiceProxy.getDurableService().update(durable);

				kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
				setEventInfo.setUdfs(udfs);
				MESDurableServiceProxy.getDurableServiceImpl().setEvent(durable, setEventInfo, eventInfo);
			}
		}

		return doc;
	}
}
