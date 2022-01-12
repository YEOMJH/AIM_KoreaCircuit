package kr.co.aim.messolution.consumable.event;

import java.util.Map;

import org.jdom.Document;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.durable.MESDurableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaterialProduct;
import kr.co.aim.messolution.generic.GenericServiceProxy;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.AsyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.product.MESProductServiceProxy;
import kr.co.aim.greentrack.consumable.ConsumableServiceProxy;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.durable.DurableServiceProxy;
import kr.co.aim.greentrack.durable.management.data.Durable;
import kr.co.aim.greentrack.durable.management.info.SetEventInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;
import kr.co.aim.greentrack.product.management.data.Product;

public class AssignLamiFilmReport  extends AsyncHandler {

	@Override
	public void doWorks(Document doc) throws CustomException
	{
		String messageName = SMessageUtil.getMessageName(doc);	
		String machineName = SMessageUtil.getBodyItemValue(doc, "MACHINENAME", true);
		String unitName = SMessageUtil.getBodyItemValue(doc, "UNITNAME", false);
		String productName = SMessageUtil.getBodyItemValue(doc, "PRODUCTNAME", false);
		String materialName = SMessageUtil.getBodyItemValue(doc, "MATERIALNAME", false);

		EventInfo eventInfo = EventInfoUtil.makeEventInfo_InUseConsumable(messageName, this.getEventUser(), this.getEventComment(), null, null);
		String timeKey = TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime());

		eventInfo.setEventTimeKey(timeKey);
		
		Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
		Product productData = MESProductServiceProxy.getProductServiceUtil().getProductData(productName);

		// Create MaterialProduct
		MaterialProduct dataInfo = new MaterialProduct();
		dataInfo.setTimeKey(eventInfo.getEventTimeKey());
		dataInfo.setProductName(productName);
		dataInfo.setLotName(productData.getLotName());
		dataInfo.setMaterialKind(GenericServiceProxy.getConstantMap().MaterialKind_Consumable);
		dataInfo.setMaterialType(consumableData.getConsumableType());
		dataInfo.setMaterialName(materialName);
		dataInfo.setQuantity(1);
		dataInfo.setEventName(eventInfo.getEventName());
		dataInfo.setEventTime(eventInfo.getEventTime());
		dataInfo.setFactoryName(productData.getFactoryName());
		dataInfo.setProductSpecName(productData.getProductSpecName());
		dataInfo.setProductSpecVersion(productData.getProductSpecVersion());
		dataInfo.setProcessFlowName(productData.getProcessFlowName());
		dataInfo.setProcessFlowVersion(productData.getProcessFlowVersion());
		dataInfo.setProcessOperationName(productData.getProcessOperationName());			
		dataInfo.setProcessOperationVersion(productData.getProcessOperationVersion());
		dataInfo.setMachineName(machineName);
		dataInfo.setMaterialLocationName(unitName);

		
		ExtendedObjectProxy.getMaterialProductService().create(eventInfo, dataInfo);			

		double quantity = 1;

		eventInfo.setEventName("decrementQuantity");

		Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Consumable.class.getSimpleName());
		udfs.put("ASSIGNEDQTY", String.valueOf(quantity));
		DecrementQuantityInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo(null, null, null, null, eventInfo.getEventTimeKey(), quantity, udfs);
		MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(consumableData, transitionInfo, eventInfo);

		Consumable newConsumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(materialName);
		
		// 2020-12-15	dhko	Modify
		// If the quantity of Consumable is zero, force the ConsumableState to 'NotAvailable' and sign the contract.
		if( newConsumableData.getQuantity() == 0 )
		{
			String filmBoxName = newConsumableData.getUdfs().get("CARRIERNAME").toString();
			Durable durableData = MESDurableServiceProxy.getDurableServiceUtil().getDurableData(filmBoxName);
			
			//Change Film State
			newConsumableData.setConsumableState("NotAvailable");
			ConsumableServiceProxy.getConsumableService().update(newConsumableData);
			
			//Deassign Material To Box
			eventInfo.setEventName("DeassignMaterialToBox");
			kr.co.aim.greentrack.consumable.management.info.SetEventInfo setEventInfo = new kr.co.aim.greentrack.consumable.management.info.SetEventInfo();
			setEventInfo.getUdfs().put("CARRIERNAME", "");
			setEventInfo.getUdfs().put("SEQ", "");
			ConsumableServiceProxy.getConsumableService().setEvent(newConsumableData.getKey(), eventInfo, setEventInfo);
			
			if(durableData.getLotQuantity() == 1)
				durableData.setDurableState(GenericServiceProxy.getConstantMap().Dur_Available);

			durableData.setLotQuantity(durableData.getLotQuantity() - 1);

			DurableServiceProxy.getDurableService().update(durableData);
			SetEventInfo setEventInfoDur = new SetEventInfo();
			durableData = DurableServiceProxy.getDurableService().setEvent(durableData.getKey(), eventInfo, setEventInfoDur);
			
		}			  
	}
}
