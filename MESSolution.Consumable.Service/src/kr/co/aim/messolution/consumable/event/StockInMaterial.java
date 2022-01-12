package kr.co.aim.messolution.consumable.event;

import java.sql.Timestamp;
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
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.generic.exception.NotFoundSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;
import org.jdom.Element;

public class StockInMaterial extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		List<Element> eMaterialList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("StockIn", getEventUser(), getEventComment(), null, null);

		for (Element materialE : eMaterialList)
		{
			String factoryName = materialE.getChildText("FACTORYNAME");
			String materialID = materialE.getChildText("MATERIALID");
			String materialKind = materialE.getChildText("MATERIALKIND");
			String materialSpecName = materialE.getChildText("MATERIALSPECNAME");
			String materialSpecVersion = materialE.getChildText("MATERIALSPECVERSION");

			if (materialKind.equals(GenericServiceProxy.getConstantMap().MaterialKind_Consumable))
			{
				Consumable consumable = MESConsumableServiceProxy.getConsumableInfoUtil().getMaterialData(materialID);
				Double quantity = consumable.getQuantity();

				if (consumable.getUdfs().get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().MaterialLocation_OnEQP))
					throw new CustomException("MATERIAL-9023", materialID);

				if (consumable.getUdfs().get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().MaterialLocation_InStock))
					throw new CustomException("MATERIAL-9024", materialID);

				if (consumable.getUdfs().get("CONSUMABLEHOLDSTATE").equals(GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_Y))
					throw new CustomException("MATERIAL-9016", materialID);
				
				if (quantity <= 0)
					throw new CustomException("MATERIAL-9026", materialID);

				if (StringUtil.equals(consumable.getConsumableType(), GenericServiceProxy.getConstantMap().MaterialType_PR))
				{
					if (StringUtil.equals(consumable.getUdfs().get("DETAILCONSUMABLETYPE"), GenericServiceProxy.getConstantMap().DetailMaterialType_PhotoGlue))
					{
						Timestamp currentTime = eventInfo.getEventTime();
						Timestamp coverOpenTime = TimeStampUtil.getTimestamp(consumable.getUdfs().get("COVEROPENTIME"));

						long intervalTime = (currentTime.getTime() - coverOpenTime.getTime()) / (1000 * 60 * 60 * 24);
						long lifeTimeOpen = Long.valueOf(consumable.getUdfs().get("LIFETIMEOPEN"));

						if (intervalTime > lifeTimeOpen)
							throw new CustomException("MATERIAL-9028", lifeTimeOpen);

					}
					else if (StringUtil.equals(consumable.getUdfs().get("DETAILCONSUMABLETYPE"), GenericServiceProxy.getConstantMap().DetailMaterialType_OrganicGlue))
					{
						String detailConsumableState = consumable.getUdfs().get("DETAILCONSUMABLESTATE");
						if (!StringUtil.equals(detailConsumableState, GenericServiceProxy.getConstantMap().DetailMaterialState_Freeze))
							throw new CustomException("MATERIAL-9030", materialID);
					}
				}

				ConsumableSpec consumableSpec = GenericServiceProxy.getSpecUtil().getMaterialSpec(factoryName, materialSpecName, materialSpecVersion);
				SetMaterialLocationInfo setMaterialLocationInfo = new SetMaterialLocationInfo();
				setMaterialLocationInfo.setMaterialLocationName(consumableSpec.getUdfs().get("STOCKLOCATION"));
				Map<String, String> udfs = new HashMap<String, String>();
				udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_InStock);
				setMaterialLocationInfo.setUdfs(udfs);

				MESConsumableServiceProxy.getConsumableServiceImpl().setMaterialLocation(consumable, setMaterialLocationInfo, eventInfo);
			}
			else
			{
				Durable durable = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(materialID);

				double timeUsedLimit = durable.getTimeUsedLimit();
				double timeUsed = durable.getTimeUsed();
				double durationUsedLimit = durable.getDurationUsedLimit();
				double durationUsed = durable.getDurationUsed();

				if (durable.getUdfs().get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().Dur_ONEQP))
					throw new CustomException("MATERIAL-9023", materialID);

				if (durable.getUdfs().get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().Dur_INSTK))
					throw new CustomException("MATERIAL-9024", materialID);

				if (durable.getUdfs().get("DURABLEHOLDSTATE").equals(GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_Y))
					throw new CustomException("MATERIAL-9016", materialID);

				// Validation by each material type
				if (StringUtil.equals(durable.getDurableType(), GenericServiceProxy.getConstantMap().MaterialType_Pallet))
				{
					// Check validation FPC in the pallet
					List<Durable> fpcList = null;
					try
					{
						fpcList = DurableServiceProxy.getDurableService().select(" PALLETNAME = ?", new Object[] { materialID });
					}
					catch (NotFoundSignal nfs)
					{
					}

					if (fpcList != null && fpcList.size() > 0)
					{
						throw new CustomException("MATERIAL-0018");
					}
				}
				else if (StringUtil.equals(durable.getDurableType(), GenericServiceProxy.getConstantMap().MaterialType_WorkTable))
				{
					if (timeUsedLimit <= timeUsed)
						throw new CustomException("MATERIAL-9015", materialID);
				}
				else if (StringUtil.equals(durable.getDurableType(), GenericServiceProxy.getConstantMap().MaterialType_FPC))
				{
					if (timeUsedLimit <= timeUsed)
						throw new CustomException("MATERIAL-9015", materialID);
					
					if (durationUsedLimit <= durationUsed)
						throw new CustomException("MATERIAL-9014", materialID);

					// Check validation FPC in the pallet
					String palletName = durable.getUdfs().get("PALLETNAME");
					if (StringUtil.isNotEmpty(palletName))
						throw new CustomException("MATERIAL-0019", palletName);

				}

				DurableSpec durableSpec = GenericServiceProxy.getSpecUtil().getDurableSpec(factoryName, materialSpecName, materialSpecVersion);

				SetMaterialLocationInfo setMaterialLocationInfo = new SetMaterialLocationInfo();
				setMaterialLocationInfo.setMaterialLocationName(durableSpec.getUdfs().get("STOCKLOCATION"));
				Map<String, String> udfs = new HashMap<String, String>();
				udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_INSTK);
				setMaterialLocationInfo.setUdfs(udfs);

				MESDurableServiceProxy.getDurableServiceImpl().setMaterialLocation(durable, setMaterialLocationInfo, eventInfo);
			}
		}

		return doc;
	}
}
