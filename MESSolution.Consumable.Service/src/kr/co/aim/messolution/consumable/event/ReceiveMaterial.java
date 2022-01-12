package kr.co.aim.messolution.consumable.event;

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
import kr.co.aim.greentrack.consumable.management.data.ConsumableSpec;
import kr.co.aim.greentrack.consumable.management.info.CreateInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class ReceiveMaterial extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> materialElementList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Create", getEventUser(), getEventComment(), null, null);

		// TransportState isn't set. set the state in StockIn Screen.
		for (Element materialElement : materialElementList)
		{
			String materialKind = materialElement.getChildText("MATERIALKIND");
			String materialName = materialElement.getChildText("MATERIALNAME");
			String interfaceTableName = StringUtils.EMPTY;
			
			if (GenericServiceProxy.getConstantMap().MaterialKind_Consumable.equals(materialKind))
			{
				interfaceTableName = "MES_WMSIF_CON";
				createMaterialToConsumable(eventInfo, materialElement);
			}
			else if (GenericServiceProxy.getConstantMap().MaterialKind_Durable.equals(materialKind))
			{
				interfaceTableName = "MES_WMSIF_DUR";
				createMaterialToDurable(eventInfo, materialElement);
			}

			// Update InterfaceFlag
			String sql = "UPDATE "  + interfaceTableName
					   + "   SET INTERFACEFLAG = 'Y' "
					   + " WHERE MATERIALNAME = ? ";
			
			GenericServiceProxy.getSqlMesTemplate().update(sql, new Object[] { materialName });
		}

		return doc;
	}
	
	private void createMaterialToConsumable(EventInfo eventInfo, Element materialElement) throws CustomException
	{
		String factoryName = materialElement.getChildText("FACTORYNAME");
		String materialName = materialElement.getChildText("MATERIALNAME");
		String materialSpecName = materialElement.getChildText("MATERIALSPECNAME");
		String quantity = materialElement.getChildText("QUANTITY");
		String batchNo = materialElement.getChildText("BATCHNO");
		String productDate = materialElement.getChildText("PRODUCTDATE");
		String expireDate = materialElement.getChildText("EXPIREDATE");
		String wmsFactoryPosition = materialElement.getChildText("WMSFACTORYPOSITION");
		String DepartExpenseFlag = materialElement.getChildText("DEPARTEXPENSEFLAG");
		
		if(!expireDate.isEmpty())
		{
			java.sql.Timestamp expirationDatetimeStamp = TimeUtils.getTimestamp(expireDate);
			expireDate = expirationDatetimeStamp.toString();
		}
		
		ConsumableSpec consumableSpecData = GenericServiceProxy.getSpecUtil().getMaterialSpec(factoryName, materialSpecName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION);
		
		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("PRODUCTDATE", productDate);
		udfs.put("EXPIRATIONDATE", expireDate);
		udfs.put("BATCHNO", batchNo);
		udfs.put("TIMEUSEDLIMIT", consumableSpecData.getUdfs().get("TIMEUSEDLIMIT"));
		udfs.put("DURATIONUSEDLIMIT", consumableSpecData.getUdfs().get("DURATIONUSEDLIMIT"));
		udfs.put("DEPARTEXPENSEFLAG", DepartExpenseFlag);
		udfs.put("WMSFACTORYNAME", wmsFactoryPosition);
		
		String consumableType = consumableSpecData.getConsumableType();
		
		if(consumableType.endsWith("Lamination"))
		{
			udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_OutStock);
		}
		else
		{
			udfs.put("TRANSPORTSTATE", GenericServiceProxy.getConstantMap().MaterialLocation_OutEQP);
		}
		
		if("PR".equals(consumableType) || "Adhesive Agent".equals(consumableType) || "Organic adhesive".equals(consumableType))
        {
        	udfs.put("DETAILCONSUMABLESTATE", "Freeze");
        	udfs.put("FREEZETIME", eventInfo.getEventTime().toString());
        }
		
		if ("PR".equals(consumableType))
		{
			if (StringUtils.isEmpty(consumableSpecData.getUdfs().get("LIFETIMESTORE")))
			{
				// MATERIAL-0040:{0} Value is null. Please Check ConsumableSpec.
				throw new CustomException("MATERIAL-0040", "LIFETIMESTORE");
			}

			if (StringUtils.isEmpty(consumableSpecData.getUdfs().get("LIFETIMEOPEN")))
				throw new CustomException("MATERIAL-0040", "LIFETIMEOPEN");

			if (StringUtils.isEmpty(consumableSpecData.getUdfs().get("DETAILCONSUMABLETYPE")))
				throw new CustomException("MATERIAL-0040", "DETAILCONSUMABLETYPE");

			udfs.put("LIFETIMESTORE", consumableSpecData.getUdfs().get("LIFETIMESTORE"));
			udfs.put("LIFETIMEOPEN", consumableSpecData.getUdfs().get("LIFETIMEOPEN"));
			udfs.put("DETAILCONSUMABLETYPE", consumableSpecData.getUdfs().get("DETAILCONSUMABLETYPE"));

			if ("OrganicGlue".equals(consumableSpecData.getUdfs().get("DETAILCONSUMABLETYPE")))
			{
				udfs.put("FREEZETIME", TimeStampUtil.toTimeString(eventInfo.getEventTime()));
				udfs.put("DETAILCONSUMABLESTATE", GenericServiceProxy.getConstantMap().DetailMaterialState_Freeze);

			}
			else if ("PhotoGlue".equals(consumableSpecData.getUdfs().get("DETAILCONSUMABLETYPE")))
			{
				udfs.put("MATERIALSTOCKTIME", TimeStampUtil.toTimeString(eventInfo.getEventTime()));
			}
		}
		
		CreateInfo createInfo = MESConsumableServiceProxy.getConsumableInfoUtil().createInfo(factoryName, "", materialName, consumableSpecData.getKey().getConsumableSpecName(), consumableSpecData.getKey().getConsumableSpecVersion(),
				consumableSpecData.getConsumableType(), Long.valueOf(quantity).longValue(), udfs);

		MESConsumableServiceProxy.getConsumableServiceImpl().createMaterial(eventInfo, materialName, createInfo);
	}
	
	private void createMaterialToDurable(EventInfo eventInfo, Element materialElement) throws CustomException
	{
		String factoryName = materialElement.getChildText("FACTORYNAME");
		String materialName = materialElement.getChildText("MATERIALNAME");
		String materialSpecName = materialElement.getChildText("MATERIALSPECNAME");
		String quantity = materialElement.getChildText("QUANTITY");
		String expireDate = materialElement.getChildText("EXPIREDATE");
		
		if(!expireDate.isEmpty())
		{
			java.sql.Timestamp expirationDatetimeStamp = TimeUtils.getTimestamp(expireDate);
			expireDate = expirationDatetimeStamp.toString();
		}
		
		kr.co.aim.greentrack.durable.management.info.CreateInfo createInfo = MESDurableServiceProxy.getDurableInfoUtil().createInfo(materialName, 
				materialSpecName, GenericServiceProxy.getConstantMap().DEFAULT_ACTIVE_VERSION, quantity, factoryName);
		
		Map<String, String> udfs = createInfo.getUdfs();
		udfs.put("EXPIRATIONDATE", expireDate);

		createInfo.setUdfs(udfs);
		
		MESDurableServiceProxy.getDurableServiceImpl().create(materialName, createInfo, eventInfo);
	}
}
