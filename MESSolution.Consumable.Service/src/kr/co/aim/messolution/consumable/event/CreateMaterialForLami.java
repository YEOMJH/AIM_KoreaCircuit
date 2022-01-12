package kr.co.aim.messolution.consumable.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.transaction.PropagationBehavior;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.info.CreateInfo;
import kr.co.aim.greentrack.durable.management.data.DurableSpec;
import kr.co.aim.greentrack.generic.exception.DuplicateNameSignal;
import kr.co.aim.greentrack.generic.exception.FrameworkErrorSignal;
import kr.co.aim.greentrack.generic.exception.InvalidStateTransitionSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class CreateMaterialForLami extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String boxName = SMessageUtil.getBodyItemValue(doc, "BOXNAME", false);

		List<Element> eMaterialList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", false);
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateMaterialForLami", getEventUser(), getEventComment(), null, null);

		for (Element materialE : eMaterialList)
		{
			String factoryName = materialE.getChildText("FACTORYNAME");
			String materialID = materialE.getChildText("MATERIALID");
			String materialType = materialE.getChildText("MATERIALTYPE");
			String materialKind = materialE.getChildText("MATERIALKIND");
			String materialSpecName = materialE.getChildText("MATERIALSPECNAME");
			String materialSpecVersion = materialE.getChildText("MATERIALSPECVERSION");
			String quantity = materialE.getChildText("QTANTITY");
			String materialState = materialE.getChildText("MATERIALSTATE");
			String batchNo = materialE.getChildText("BATCHNO");
			String boxid = materialE.getChildText("BOXID");

			String productDate = materialE.getChildText("PRODUCTDATE");
			String expirationDate = materialE.getChildText("EXPIRATIONDATE");
			String location = materialE.getChildText("WMSFACTORYNAME");

			if (materialKind.equals(GenericServiceProxy.getConstantMap().MaterialKind_Consumable))
			{
				ConsumableSpec consumableSpec = GenericServiceProxy.getSpecUtil().getMaterialSpec(factoryName, materialSpecName, materialSpecVersion);

				Map<String, String> udfs = new HashMap<String, String>();
				udfs.put("PRODUCTDATE", productDate);
				udfs.put("EXPIRATIONDATE", expirationDate);
				udfs.put("BATCHNO", batchNo);
				udfs.put("TIMEUSEDLIMIT", consumableSpec.getUdfs().get("TIMEUSEDLIMIT"));
				udfs.put("DURATIONUSEDLIMIT", consumableSpec.getUdfs().get("DURATIONUSEDLIMIT"));
				udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_InStock);
				udfs.put("WMSFACTORYNAME", location);
				udfs.put("BOXID", boxid);

				CreateInfo createInfo = MESConsumableServiceProxy.getConsumableInfoUtil().createInfo(factoryName, "", materialID, materialSpecName, materialSpecVersion,
						consumableSpec.getConsumableType(), Long.valueOf(quantity).longValue(), udfs);

				MESConsumableServiceProxy.getConsumableServiceImpl().createMaterial(eventInfo, materialID, createInfo);

			}
			else
			{
				try
				{
					DurableSpec durableSpec = GenericServiceProxy.getSpecUtil().getDurableSpec(factoryName, materialSpecName, materialSpecVersion);

					kr.co.aim.greentrack.durable.management.info.CreateInfo createInfo = MESDurableServiceProxy.getDurableInfoUtil().createInfo(materialID, materialSpecName, materialSpecVersion,
							quantity, factoryName);

					MESDurableServiceProxy.getDurableServiceImpl().create(materialID, createInfo, eventInfo);
				}
				catch (FrameworkErrorSignal fe)
				{
					throw new CustomException("MATERIAL-9999", fe.getMessage());
				}
				catch (DuplicateNameSignal de)
				{
					throw new CustomException("MATERIAL-9003", materialID);
				}

			}

		}

		return doc;
	}
}
