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
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.jdom.Document;
import org.jdom.Element;

public class StockOutMaterial extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> eMaterialList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("StockOut", getEventUser(), getEventComment(), null, null);

		for (Element materialE : eMaterialList)
		{
			String materialID = materialE.getChildText("MATERIALID");
			String materialKind = materialE.getChildText("MATERIALKIND");

			if (materialKind.equals(GenericServiceProxy.getConstantMap().MaterialKind_Consumable))
			{
				Consumable consumable = MESConsumableServiceProxy.getConsumableInfoUtil().getMaterialData(materialID);

				if (consumable.getUdfs().get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().MaterialLocation_OnEQP))
					throw new CustomException("MATERIAL-9023", materialID);
				
				if (consumable.getUdfs().get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().MaterialLocation_OutStock))
					throw new CustomException("MATERIAL-9025", materialID);
				
				if (consumable.getUdfs().get("CONSUMABLEHOLDSTATE").equals(GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_Y))
					throw new CustomException("MATERIAL-9016", materialID);

				Map<String, String> udfs = new HashMap<String, String>();

				// PR Type
				if (StringUtil.equals(consumable.getConsumableType(), GenericServiceProxy.getConstantMap().MaterialType_PR))
				{
					// DetailType : PhotoGlue
					if (StringUtil.equals(consumable.getUdfs().get("DETAILCONSUMABLETYPE"), GenericServiceProxy.getConstantMap().DetailMaterialType_PhotoGlue))
					{
						Timestamp currentTime = eventInfo.getEventTime();
						Timestamp materialStockTime = TimeStampUtil.getTimestamp(consumable.getUdfs().get("MATERIALSTOCKTIME"));

						long intervalTime = (currentTime.getTime() - materialStockTime.getTime()) / (1000 * 60 * 60 * 24);
						long lifeTimeStore = Long.valueOf(consumable.getUdfs().get("LIFETIMESTORE"));

						if (intervalTime > lifeTimeStore)
							throw new CustomException("MATERIAL-9029", lifeTimeStore);

						// update COVEROPENTIME
						udfs.put("COVEROPENTIME", TimeStampUtil.toTimeString(eventInfo.getEventTime()));
					}
					// DetailType : OrganicGlue
					else if (StringUtil.equals(consumable.getUdfs().get("DETAILCONSUMABLETYPE"), GenericServiceProxy.getConstantMap().DetailMaterialType_OrganicGlue))
					{
						Timestamp currentTime = eventInfo.getEventTime();
						Timestamp freezeTime = TimeStampUtil.getTimestamp(consumable.getUdfs().get("FREEZETIME"));

						long intervalTime = (currentTime.getTime() - freezeTime.getTime()) / (1000 * 60 * 60 * 24);
						long lifeTimeStore = Long.valueOf(consumable.getUdfs().get("LIFETIMESTORE"));
						long lifeTimeOpen = Long.valueOf(consumable.getUdfs().get("LIFETIMEOPEN"));

						// Compare LifeTimeStore (Once)
						if (StringUtil.isEmpty(consumable.getUdfs().get("THAWTIME")))
						{
							if (intervalTime > lifeTimeStore)
								throw new CustomException("MATERIAL-9029", lifeTimeStore);
						}
						// Compare LifeTimeOpen
						else
						{
							if (intervalTime > lifeTimeOpen)
								throw new CustomException("MATERIAL-9028", lifeTimeOpen);
						}
					}
				}

				SetMaterialLocationInfo setMaterialLocationInfo = new SetMaterialLocationInfo();
				setMaterialLocationInfo.setMaterialLocationName("");

				udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_OutStock);
				setMaterialLocationInfo.setUdfs(udfs);

				MESConsumableServiceProxy.getConsumableServiceImpl().setMaterialLocation(consumable, setMaterialLocationInfo, eventInfo);
			}
			else
			{
				Durable durable = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(materialID);

				if (durable.getUdfs().get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().Dur_ONEQP))
					throw new CustomException("MATERIAL-9023", materialID);
				
				if (durable.getUdfs().get("TRANSPORTSTATE").equals(GenericServiceProxy.getConstantMap().Dur_OUTSTK))
					throw new CustomException("MATERIAL-9025", materialID);
				
				if (durable.getUdfs().get("DURABLEHOLDSTATE").equals(GenericServiceProxy.getConstantMap().DURABLE_HOLDSTATE_Y))
					throw new CustomException("MATERIAL-9016", materialID);

				SetMaterialLocationInfo setMaterialLocationInfo = new SetMaterialLocationInfo();
				setMaterialLocationInfo.setMaterialLocationName("");
				Map<String, String> udfs = new HashMap<String, String>();
				udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().Dur_OUTSTK);
				setMaterialLocationInfo.setUdfs(udfs);

				MESDurableServiceProxy.getDurableServiceImpl().setMaterialLocation(durable, setMaterialLocationInfo, eventInfo);
			}

		}

		return doc;
	}
}
