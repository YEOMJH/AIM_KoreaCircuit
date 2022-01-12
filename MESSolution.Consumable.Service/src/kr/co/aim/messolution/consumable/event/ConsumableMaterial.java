package kr.co.aim.messolution.consumable.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpecKey;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class ConsumableMaterial extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		List<Element> consumList = SMessageUtil.getBodySequenceItemList(doc, "CONSUMABLEMATERIALLIST", true);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ConsumableMaterial", getEventUser(), getEventComment(), "",
				"");
		// eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

		for (Element consum : consumList) {
			String consumableName = SMessageUtil.getChildText(consum, "CONSUMABLENAME", false);
			String consumableSpecName = SMessageUtil.getChildText(consum, "MATERIALSPECNAME", true);
			String quantity = SMessageUtil.getChildText(consum, "QUANTITY", true);
			double dQuantity = Double.parseDouble(quantity);
			String kitTime = SMessageUtil.getChildText(consum, "KITTIME", true);
			String unkitTime = SMessageUtil.getChildText(consum, "UNKITTIME", true);
			String factoryCode = SMessageUtil.getChildText(consum, "FACTORYCODE", true);
			String factoryPosition = SMessageUtil.getChildText(consum, "FACTORYPOSITION", true);
			String batchNo= SMessageUtil.getChildText(consum, "BATCHNO", false);

			if (Integer.parseInt(kitTime) > Integer.parseInt(unkitTime)) {
				throw new CustomException("CONSUMABLE-0003", kitTime, unkitTime);
			}

			if (false) {
				Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil()
						.getConsumableData(consumableName);
				//CommonValidation.checkConsumableState(consumableData);

				// Double oldQuantity = consumableData.getQuantity();
				// Double newQuantity = CommonUtil.doubleAdd(oldQuantity,
				// dQuantity);

				Map<String, String> udfs = new HashMap<String, String>();
				udfs.put("KITTIME", kitTime);
				udfs.put("UNKITTIME", unkitTime);

				DecrementQuantityInfo decrementQuantityInfo = MESConsumableServiceProxy.getConsumableInfoUtil()
						.decrementQuantityInfo(null, null, null, null, eventInfo.getEventTimeKey(), -dQuantity, udfs);

				MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(consumableData,
						decrementQuantityInfo, eventInfo);

				setToSAP(consumableName,consumableSpecName,dQuantity,kitTime,unkitTime,factoryCode,factoryPosition,batchNo);

			}
			else {
				setToSAP(consumableName,consumableSpecName,dQuantity,kitTime,unkitTime,factoryCode,factoryPosition,batchNo);
			}
		}
		return doc;
	}

	private void setToSAP(String consumableName,String consumableSpecName,Double quantity,String kitTime,
			String UnKitTime,String factoryCode,String factoryPosition,String batchNo)throws CustomException{
		String consumeUnit="";
		StringBuffer sql1 = new StringBuffer();
		sql1.append("SELECT DISTINCT CONSUMEUNIT ");
		sql1.append("  FROM CT_ERPBOM ");
		sql1.append(" WHERE MATERIALSPECNAME = ? ");

		Object[] bindArray = new Object[]{consumableSpecName};
		
		List<Map<String, Object>> result = GenericServiceProxy.getSqlMesTemplate().queryForList(sql1.toString(), bindArray);
		if(result!=null&&result.size()>0)
		{
			consumeUnit=result.get(0).get("CONSUMEUNIT").toString();
		}
		
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
		bindMap.put("quantity",quantity);
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
		
		GenericServiceProxy.getSqlMesTemplate().update(sql.toString(), bindMap);
		
	}
}
