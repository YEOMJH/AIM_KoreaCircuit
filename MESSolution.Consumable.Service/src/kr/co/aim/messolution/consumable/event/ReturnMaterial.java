package kr.co.aim.messolution.consumable.event;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.data.ConsumableKey;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.data.DurableKey;
import kr.co.aim.greentrack.durable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;

import org.jdom.Document;
import org.jdom.Element;

public class ReturnMaterial extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> materialElementList = SMessageUtil.getBodySequenceItemList(doc, "MATERIALLIST", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("Return", getEventUser(), getEventComment(), null, null);

		// TransportState isn't set. set the state in StockIn Screen.
		for (Element materialElement : materialElementList)
		{
			String materialKind = materialElement.getChildText("MATERIALKIND");
			String materialName = materialElement.getChildText("MATERIALNAME");
			String boxId = materialElement.getChildText("BOXID");
			String materialSpecName = materialElement.getChildText("MATERIALSPECNAME");
			String quantity = materialElement.getChildText("QUANTITY");
			String unit = materialElement.getChildText("UNIT");
			String batchNo = materialElement.getChildText("BATCHNO");
			String vendor = materialElement.getChildText("VENDOR");
			String vendorBatchNo = materialElement.getChildText("VENDORBATCHNO");
			String productDate = materialElement.getChildText("PRODUCTDATE");
			String expireDate = materialElement.getChildText("EXPIREDATE");
			String wmsFactoryCode = materialElement.getChildText("WMSFACTORYCODE");
			String wmsFactoryPosition = materialElement.getChildText("WMSFACTORYPOSITION");
			String departExpenseFlag = materialElement.getChildText("DEPARTEXPENSEFLAG");
			
			// Insert MES_WMSIF_RETURN
			String sql = "INSERT INTO MES_WMSIF_RETURN "
					   + "(NEWBOXID, BOXID, MATERIALSPECNAME, QUANTITY, UNIT, BATCHNO, VENDOR, VENDORBATCHNO, PRODUCTDATE, EXPIREDATE, WMSFACTORYCODE, WMSFACTORYPOSITION, DEPARTEXPENSEFLAG, INTERFACEDATE, INTERFACEFLAG, INTERFACECOMMENT) "
					   + "VALUES "
					   + "(:NEWBOXID, :BOXID, :MATERIALSPECNAME, :QUANTITY, :UNIT, :BATCHNO, :VENDOR, :VENDORBATCHNO, :PRODUCTDATE, :EXPIREDATE, :WMSFACTORYCODE, :WMSFACTORYPOSITION, :DEPARTEXPENSEFLAG, :INTERFACEDATE, :INTERFACEFLAG, :INTERFACECOMMENT) ";
			
			Map<String, Object> bindMap = new HashMap<String, Object>();
			bindMap.put("NEWBOXID", "R" + boxId);
			bindMap.put("BOXID", boxId);
			bindMap.put("MATERIALSPECNAME", materialSpecName);
			bindMap.put("QUANTITY", quantity);
			bindMap.put("UNIT", unit);
			bindMap.put("BATCHNO", batchNo);
			bindMap.put("VENDOR", vendor);
			bindMap.put("VENDORBATCHNO", vendorBatchNo);
			bindMap.put("PRODUCTDATE", productDate);
			bindMap.put("EXPIREDATE", expireDate);
			bindMap.put("WMSFACTORYCODE", wmsFactoryCode);
			bindMap.put("WMSFACTORYPOSITION", wmsFactoryPosition);
			bindMap.put("DEPARTEXPENSEFLAG", departExpenseFlag);
			bindMap.put("INTERFACEDATE", new SimpleDateFormat("yyyyMMdd").format(eventInfo.getEventTime()));
			bindMap.put("INTERFACEFLAG", "");
			bindMap.put("INTERFACECOMMENT", eventInfo.getEventComment());
			
			greenFrameServiceProxy.getSqlTemplate().update(sql, bindMap);
			
			// Update NewBoxId of Consumable/Durable
			if ("Consumable".equals(materialKind))
			{
				this.returnConsumable(eventInfo, materialName, boxId);
			}
			else if ("Durable".equals(materialKind))
			{
				this.returnDurable(eventInfo, materialName, boxId);
			}
		}

		return doc;
	}
	
	private void returnConsumable(EventInfo eventInfo, String materialName, String boxId) throws CustomException
	{
		Consumable consumableData = ConsumableServiceProxy.getConsumableService().selectByKey(new ConsumableKey(materialName));
		
		DecrementQuantityInfo decrementQuantityInfo = new DecrementQuantityInfo();
		decrementQuantityInfo.setConsumerLotName("");
		decrementQuantityInfo.setConsumerPOName("");
		decrementQuantityInfo.setConsumerPOVersion("");
		decrementQuantityInfo.setConsumerProductName("");
		decrementQuantityInfo.setConsumerTimeKey("");
		decrementQuantityInfo.setQuantity(consumableData.getQuantity());
		
		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("NEWBOXID", "R" + boxId);
		
		decrementQuantityInfo.setUdfs(udfs);
		
		MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(consumableData, decrementQuantityInfo, eventInfo);
	}
	
	private void returnDurable(EventInfo eventInfo, String materialName, String boxId)
	{
		Durable durableData = DurableServiceProxy.getDurableService().selectByKey(new DurableKey(materialName));
		
		Map<String, String> udfs = new HashMap<String, String>();
		udfs.put("NEWBOXID", "R" + boxId);
		
		MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
		makeNotAvailableInfo.setUdfs(udfs);
		
		MESDurableServiceProxy.getDurableServiceImpl().makeNotAvailable(durableData, makeNotAvailableInfo, eventInfo);
	}
}
