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
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;
import org.jdom.Element;

public class ModifyMaterial extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		//List<Element> eMaterialList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Modify", getEventUser(), getEventComment(), null, null);

		
		String materialID = SMessageUtil.getBodyItemValue(doc, "MATERIALID", true);
		String materialType =  SMessageUtil.getBodyItemValue(doc, "MATERIALTYPE", true);
		String materialKind =  SMessageUtil.getBodyItemValue(doc, "MATERIALKIND", false);
		String quantity =  SMessageUtil.getBodyItemValue(doc, "QUANTITY", false);
		String timeUsedLimit =  SMessageUtil.getBodyItemValue(doc, "TIMEUSEDLIMIT", false);
		String timeUsed =  SMessageUtil.getBodyItemValue(doc, "TIMEUSED", false);
		String durationUsedLimit =  SMessageUtil.getBodyItemValue(doc, "DURATIONUSEDLIMIT", false);
		String durationUsed = SMessageUtil.getBodyItemValue(doc, "DURATIONUSED", false);

		String materialState =  SMessageUtil.getBodyItemValue(doc, "MATERIALSTATE", false);
		String transportState =  SMessageUtil.getBodyItemValue(doc, "TRANSPORTSTATE", false);
		String holdState =  SMessageUtil.getBodyItemValue(doc, "HOLDSTATE", false);
		String materialLocationName = SMessageUtil.getBodyItemValue(doc, "MATERIALLOCATIONNAME", false);
		String cleanState =  SMessageUtil.getBodyItemValue(doc, "CLEANSTATE", false);
		String expirationDate =  SMessageUtil.getBodyItemValue(doc, "EXPIRATIONDATE", false);
		
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
			setEventInfo.getUdfs().put("EXPIRATIONDATE", expirationDate);
			
			if( quantity.equals("0") )
			{
				String filmBoxName = consumable.getUdfs().get("CARRIERNAME").toString();
				
				if (!filmBoxName.isEmpty())
				{
					Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(filmBoxName);
					
					//Deassign Material To Box
					EventInfo eventInfoD = EventInfoUtil.makeEventInfo("DeassignFilm", getEventUser(), getEventComment(), null, null);
					setEventInfo.getUdfs().put("CARRIERNAME", "");
					setEventInfo.getUdfs().put("SEQ", "");
					
					if(durableData.getLotQuantity() == 1)
						durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);

					durableData.setLotQuantity(durableData.getLotQuantity() - 1);

					DurableServiceProxy.getDurableService().update(durableData);
					kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfoDur = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
					durableData = DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfoD, setEventInfoDur);
				}
			}
			
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

			durable.setDurableState(materialState);
			durable.setTimeUsedLimit(Double.parseDouble(timeUsedLimit));
			durable.setTimeUsed(Double.parseDouble(timeUsed));
			durable.setDurationUsedLimit(Double.parseDouble(durationUsedLimit));
			durable.setDurationUsed(Double.parseDouble(durationUsed));
			durable.setDurableCleanState(cleanState);
			//houxk 20201202
//			if (StringUtil.equals(materialType, GenericServiceProxy.getConstantMap().DURABLETYPE_FilmBox)
//					|| StringUtil.equals(materialType, GenericServiceProxy.getConstantMap().DURABLETYPE_PeelingFilmBox))
//				durable.setDurableCleanState(cleanState);

			Map<String, String> udfs = new HashMap<String, String>();

			udfs.put("TRANSPORTSTATE", transportState);
			udfs.put("DURABLEHOLDSTATE", holdState);

			durable.setUdfs(udfs);

			DurableServiceProxy.getDurableService().update(durable);

			kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();
			setEventInfo.setUdfs(udfs);
			MESDurableServiceProxy.getDurableServiceImpl().setEvent(durable, setEventInfo, eventInfo);
		}
	

		return doc;
	}
}
