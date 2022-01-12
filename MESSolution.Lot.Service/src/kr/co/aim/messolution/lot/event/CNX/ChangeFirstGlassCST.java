package kr.co.aim.messolution.lot.event.CNX;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.master.ConstantMap;
import kr.co.aim.messolution.generic.util.CommonValidation;
import kr.co.aim.messolution.generic.util.ConvertUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.lot.LotServiceProxy;
import kr.co.aim.greentrack.lot.management.data.Lot;
import kr.co.aim.greentrack.lot.management.info.SetEventInfo;
import kr.co.aim.greentrack.product.ProductServiceProxy;
import kr.co.aim.greentrack.product.management.data.Product;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;

public class ChangeFirstGlassCST extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String factoryName = SMessageUtil.getBodyItemValue(doc, "FACTORYNAME", true);
		String parentLotName = SMessageUtil.getBodyItemValue(doc, "PARENTLOTNAME", true);
		String carrierName = SMessageUtil.getBodyItemValue(doc, "CARRIERNAME", true);
		String newCarrierName = SMessageUtil.getBodyItemValue(doc, "NEWCARRIERNAME", true);

		String jobName = getJobName(factoryName, parentLotName);

		if (StringUtils.isEmpty(jobName))
			throw new CustomException("FIRSTGLASS-0012", parentLotName);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("ChangeFirstGlassCST", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setEventComment("ChangeFirstGlassCST : " + eventInfo.getEventComment());

		List<Map<String, Object>> firstGlassLotList = getFirstGlassLotList(jobName);

		for (Map<String, Object> firstGlassLot : firstGlassLotList)
		{
			String lotName = ConvertUtil.getMapValueByName(firstGlassLot, "LOTNAME");
			Lot lotData = MESLotServiceProxy.getLotServiceUtil().getLotData(lotName);

			CommonValidation.checkLotProcessState(lotData);

			lotData.setCarrierName(newCarrierName);

			LotServiceProxy.getLotService().update(lotData);

			SetEventInfo setEventInfo = new SetEventInfo();
			LotServiceProxy.getLotService().setEvent(lotData.getKey(), eventInfo, setEventInfo);

			List<Product> firstGlassProductList = MESProductServiceProxy.getProductServiceUtil().getProductListByLotName(lotName);

			for (Product productData : firstGlassProductList)
			{
				productData.setCarrierName(newCarrierName);

				ProductServiceProxy.getProductService().update(productData);

				kr.co.aim.greentrack.product.management.info.SetEventInfo setProdEventInfo = new kr.co.aim.greentrack.product.management.info.SetEventInfo();
				ProductServiceProxy.getProductService().setEvent(productData.getKey(), eventInfo, setProdEventInfo);
			}
		}

		// Old DurableData
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(carrierName);
		long quantity = durableData.getLotQuantity();

		eventInfo.setEventName("DeassignCarrier");
		durableStateChange(carrierName, GenericServiceProxy.getConstantMap().Dur_Available, 0, eventInfo);

		// New DurableData
		eventInfo.setEventName("AssignCarrier");
		durableStateChange(newCarrierName, GenericServiceProxy.getConstantMap().Dur_InUse, quantity, eventInfo);

		return doc;
	}

	private String getJobName(String factoryName, String parentLotName)
	{
		String jobName = "";

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT L.JOBNAME ");
		sql.append("  FROM LOT L, CT_FIRSTGLASSJOB FG ");
		sql.append(" WHERE L.JOBNAME = FG.JOBNAME ");
		sql.append("   AND L.FACTORYNAME = :FACTORYNAME ");
		sql.append("   AND L.LOTNAME = :LOTNAME ");

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("FACTORYNAME", factoryName);
		bindMap.put("LOTNAME", parentLotName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		if (sqlResult.size() > 0)
		{
			jobName = ConvertUtil.getMapValueByName(sqlResult.get(0), "JOBNAME");
		}

		return jobName;
	}

	private List<Map<String, Object>> getFirstGlassLotList(String jobName)
	{
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT L.LOTNAME ");
		sql.append("  FROM LOT L, CT_FIRSTGLASSJOB FG ");
		sql.append(" WHERE L.JOBNAME = FG.JOBNAME ");
		sql.append("   AND FG.JOBNAME = :JOBNAME ");
		sql.append("   AND L.CARRIERNAME IS NOT NULL ");

		Map<String, String> bindMap = new HashMap<String, String>();
		bindMap.put("JOBNAME", jobName);

		@SuppressWarnings("unchecked")
		List<Map<String, Object>> sqlResult = GenericServiceProxy.getSqlMesTemplate().queryForList(sql.toString(), bindMap);

		return sqlResult;
	}

	public void durableStateChange(String durableName, String durableState, long lotQuantity, EventInfo eventInfo) throws CustomException
	{
		DurableKey durableKey = new DurableKey();
		durableKey.setDurableName(durableName);
		Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(durableName);
		ConstantMap constantMap = GenericServiceProxy.getConstantMap();

		if (durableState.equals("InUse"))
		{
			durableData.setDurableState(constantMap.Dur_InUse);
			durableData.setLotQuantity(lotQuantity);
		}

		if (durableState.equals("Available"))
		{
			durableData.setDurableState(constantMap.Dur_Available);
			durableData.setLotQuantity(0);
		}

		DurableServiceProxy.getDurableService().update(durableData);
		kr.co.aim.greentrack.durable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.durable.management.info.SetEventInfo();

		DurableServiceProxy.getDurableService().setEvent(durableKey, eventInfo, setEventInfo);
	}
}
