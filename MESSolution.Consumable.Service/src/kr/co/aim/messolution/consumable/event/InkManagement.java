package kr.co.aim.messolution.consumable.event;

import java.util.HashMap;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.info.CreateInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class InkManagement extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String consumableName = SMessageUtil.getBodyItemValue(doc, "CONSUMABLENAME", true);
		String consumableSpecName = SMessageUtil.getBodyItemValue(doc, "CONSUMABLESPECNAME", true);
		String consumableSpecVersion = SMessageUtil.getBodyItemValue(doc, "CONSUMABLESPECVERSION", false);
		String weight = SMessageUtil.getBodyItemValue(doc, "QUANTITY", true);
		String productDate = SMessageUtil.getBodyItemValue(doc, "PRODUCTDATE", true);
		String batchNo = SMessageUtil.getBodyItemValue(doc, "BATCHNO", false);

		boolean isExistConsumable = MESConsumableServiceProxy.getConsumableServiceUtil().isExistConsumable(consumableName);
		ConsumableSpec consumableSpec = GenericServiceProxy.getSpecUtil().getMaterialSpec(factoryName, consumableSpecName, consumableSpecVersion);

		if (isExistConsumable)
			throw new CustomException("MATERIAL-9003", consumableName);

		if (!StringUtils.equals(consumableSpec.getConsumableType(), GenericServiceProxy.getConstantMap().ConsumableType_Ink))
			throw new CustomException("MATERIAL-9002", consumableSpec.getConsumableType(), consumableName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateMaterial", getEventUser(), getEventComment(), null, null);

		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("PRODUCTDATE", productDate);
		udfs.put("TIMEUSEDLIMIT", consumableSpec.getUdfs().get("TIMEUSEDLIMIT"));
		udfs.put("DURATIONUSEDLIMIT", consumableSpec.getUdfs().get("DURATIONUSEDLIMIT"));
		udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_Bank);
		udfs.put("BATCHNO", batchNo);

		CreateInfo createInfo = MESConsumableServiceProxy.getConsumableInfoUtil().createInfo(factoryName, "", consumableName, consumableSpecName, consumableSpec.getKey().getConsumableSpecVersion(), consumableSpec.getConsumableType(), Long.parseLong(weight), udfs);

		MESConsumableServiceProxy.getConsumableServiceImpl().createMaterial(eventInfo, consumableName, createInfo);

		return doc;
	}
}
