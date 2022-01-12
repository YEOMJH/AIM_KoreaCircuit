package kr.co.aim.messolution.consumable.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.info.CreateInfo;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.consumable.management.info.SetEventInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.info.SetMaterialLocationInfo;
import kr.co.aim.greentrack.generic.info.TransitionInfo;

public class CreateOrganic extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{

		Element bodyElement = SMessageUtil.getBodyElement(doc);
		Element materialListElement = bodyElement.getChild("MATERIALLIST");

		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", false);

		List<Element> materialElementList = materialListElement.getChildren();

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);

		for (Element element : materialElementList)
		{

			String consumableName = element.getChildText("ORGANICNAME");
			String consumableSpecName = element.getChildText("ORGANICSPECNAME");
			String consumableSpecVersion = GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION;
			String weight = element.getChildText("WEIGHT");
			String productDate = element.getChildText("PRODUCTIONDATE");
			String effectiveDate = element.getChildText("EFFECTIVEDATE");
			String batchNo = element.getChildText("BATCHNO");

			boolean isExistConsumable = MESConsumableServiceProxy.getConsumableServiceUtil().isExistConsumable(consumableName);
			ConsumableSpec consumableSpec = GenericServiceProxy.getSpecUtil().getMaterialSpec(factoryName, consumableSpecName, consumableSpecVersion);

			if (isExistConsumable)
			{
				if (StringUtils.equals(consumableSpec.getConsumableType(), GenericServiceProxy.getConstantMap().ConsumableType_InOrganic))
				{
					Consumable oldConsumable = MESConsumableServiceProxy.getConsumableInfoUtil().getMaterialData(consumableName);
					Consumable consumable = MESConsumableServiceProxy.getConsumableInfoUtil().getMaterialData(consumableName);
					consumable.setCreateQuantity(CommonUtil.doubleAdd(oldConsumable.getCreateQuantity(), Double.parseDouble(weight)));
					consumable.setQuantity(CommonUtil.doubleAdd(oldConsumable.getQuantity(), Double.parseDouble(weight)));
					ConsumableServiceProxy.getConsumableService().update(consumable);

					Map<String, String> udfs = new HashMap<String, String>();
					SetEventInfo setEventInfo = new SetEventInfo();

					setEventInfo.setUdfs(udfs);
					MESConsumableServiceProxy.getConsumableServiceImpl().setEvent(consumableName, setEventInfo, eventInfo);

					return doc;
				}
				else
				{
					throw new CustomException("MATERIAL-9003", consumableName);
				}

			}

			if (!StringUtils.equals(consumableSpec.getConsumableType(), GenericServiceProxy.getConstantMap().ConsumableType_Organic)
					&& !StringUtils.equals(consumableSpec.getConsumableType(), GenericServiceProxy.getConstantMap().ConsumableType_InOrganic))
			{
				throw new CustomException("MATERIAL-9002", consumableSpec.getConsumableType(), consumableName);
			}

			Map<String, String> udfs = new HashMap<String, String>();
			udfs.put("PRODUCTDATE", productDate);
			udfs.put("EXPIRATIONDATE", effectiveDate);
			udfs.put("BATCHNO", batchNo);
			udfs.put("TIMEUSEDLIMIT", consumableSpec.getUdfs().get("TIMEUSEDLIMIT"));
			udfs.put("DURATIONUSEDLIMIT", consumableSpec.getUdfs().get("DURATIONUSEDLIMIT"));
			udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_Bank);

			CreateInfo createInfo = MESConsumableServiceProxy.getConsumableInfoUtil().createInfo(factoryName, "", consumableName, consumableSpecName,
					consumableSpec.getKey().getConsumableSpecVersion(), consumableSpec.getConsumableType(), Long.parseLong(weight), udfs);

			MESConsumableServiceProxy.getConsumableServiceImpl().createMaterial(eventInfo, consumableName, createInfo);
		}

		return doc;
	}

}
