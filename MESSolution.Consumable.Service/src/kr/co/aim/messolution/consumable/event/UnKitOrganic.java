package kr.co.aim.messolution.consumable.event;

import java.util.List;
import java.util.Map;

import kr.co.aim.messolution.consumable.MESConsumableServiceProxy;
import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.CrucibleLot;
import kr.co.aim.messolution.extended.object.management.data.Organic;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.CommonUtil;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.util.object.ObjectUtil;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.consumable.management.data.Consumable;
import kr.co.aim.greentrack.consumable.management.info.DecrementQuantityInfo;
import kr.co.aim.greentrack.consumable.management.info.MakeNotAvailableInfo;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.apache.commons.lang.StringUtils;
import org.jdom.Document;
import org.jdom.Element;

public class UnKitOrganic extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		List<Element> consumerbleList = SMessageUtil.getBodySequenceItemList(doc, "CONSUMERBLELIST", true);

		for (Element consumableE : consumerbleList)
		{
			// Consume Organic Qty
			String crucibleLotName = consumableE.getChild("CRUCIBLELOTNAME").getText();
			String quantity = consumableE.getChild("QUANTITY").getText();
			Double unKitQuantity = Double.parseDouble(quantity);

			List<Organic> organicList = ExtendedObjectProxy.getOrganicService().select(" CRUCIBLELOTNAME = ?", new Object[] { crucibleLotName });

			for (Organic organicE : organicList)
			{
				if (organicE.getInputQty() > 0)
				{
					if (organicE.getInputQty() >= unKitQuantity)
					{
						Double unKitQuantityE = unKitQuantity;
						EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnKitOrganic", getEventUser(), getEventComment(), null, null);
						Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(organicE.getConsumableName());
						Consumable oldConsumable = (Consumable) ObjectUtil.copyTo(consumableData);

						Organic oldCtOrganicData = (Organic) ObjectUtil.copyTo(organicE);
						organicE.setInputQty(CommonUtil.doubleSubtract(oldCtOrganicData.getInputQty(), unKitQuantityE));
						ExtendedObjectProxy.getOrganicService().update(organicE);

						String oriKitQty = oldConsumable.getUdfs().get("KITQUANTITY");

						if (StringUtils.isEmpty(oriKitQty))
							oriKitQty = "0";

						String kitQty = Double.toString(CommonUtil.doubleSubtract(Double.parseDouble(oriKitQty), unKitQuantityE));

						Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Consumable.class.getSimpleName());
						udfs.put("KITQUANTITY", kitQty);
						udfs.put("UNKITTIME", TimeStampUtil.toTimeString(eventInfo.getEventTime()));

						if (Double.parseDouble(oriKitQty) - unKitQuantityE == 0)
							udfs.put("KITUSER", "");

						eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

						DecrementQuantityInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo(null, null, null, null, eventInfo.getEventTimeKey(),
								unKitQuantityE, udfs);
						MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(consumableData, transitionInfo, eventInfo);

						// Set CrucibleLot Qty
						eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
						CrucibleLot dataInfo = ExtendedObjectProxy.getCrucibleLotService().selectByKey(true, new Object[] { crucibleLotName });
						dataInfo.setWeight(CommonUtil.doubleSubtract(dataInfo.getWeight(), unKitQuantityE));
						ExtendedObjectProxy.getCrucibleLotService().modify(eventInfo, dataInfo);

						// Consumable State Set Mask NotAvailable
						consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(organicE.getConsumableName());
						if (consumableData.getQuantity() == 0)
						{
							consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(organicE.getConsumableName());
							MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
							MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(consumableData, makeNotAvailableInfo, eventInfo);
						}

						break;

					}
					else
					{
						Double unKitQuantityE = organicE.getInputQty();
						unKitQuantity = CommonUtil.doubleSubtract(unKitQuantity, unKitQuantityE);
						EventInfo eventInfo = EventInfoUtil.makeEventInfo("UnKitOrganic", getEventUser(), getEventComment(), null, null);
						Consumable consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(organicE.getConsumableName());
						Consumable oldConsumable = (Consumable) ObjectUtil.copyTo(consumableData);

						Organic oldCtOrganicData = (Organic) ObjectUtil.copyTo(organicE);
						organicE.setInputQty(CommonUtil.doubleSubtract(oldCtOrganicData.getInputQty(), unKitQuantityE));
						ExtendedObjectProxy.getOrganicService().update(organicE);

						String oriKitQty = oldConsumable.getUdfs().get("KITQUANTITY");

						if (StringUtils.isEmpty(oriKitQty))
							oriKitQty = "0";

						String kitQty = Double.toString(CommonUtil.doubleSubtract(Double.parseDouble(oriKitQty), unKitQuantityE));

						Map<String, String> udfs = CommonUtil.setNamedValueSequence(SMessageUtil.getBodyElement(doc), Consumable.class.getSimpleName());
						udfs.put("KITQUANTITY", kitQty);
						udfs.put("UNKITTIME", TimeStampUtil.toTimeString(eventInfo.getEventTime()));

						if (Double.parseDouble(oriKitQty) - unKitQuantityE == 0)
							udfs.put("KITUSER", "");

						eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());

						DecrementQuantityInfo transitionInfo = MESConsumableServiceProxy.getConsumableInfoUtil().decrementQuantityInfo(null, null, null, null, eventInfo.getEventTimeKey(),
								unKitQuantityE, udfs);
						MESConsumableServiceProxy.getConsumableServiceImpl().decrementQuantity(consumableData, transitionInfo, eventInfo);

						// Set CrucibleLot Qty
						eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
						CrucibleLot dataInfo = ExtendedObjectProxy.getCrucibleLotService().selectByKey(true, new Object[] { crucibleLotName });
						dataInfo.setWeight(CommonUtil.doubleSubtract(dataInfo.getWeight(), unKitQuantityE));
						ExtendedObjectProxy.getCrucibleLotService().modify(eventInfo, dataInfo);

						// Consumable State Set Mask NotAvailable
						consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(organicE.getConsumableName());
						if (consumableData.getQuantity() == 0)
						{
							consumableData = MESConsumableServiceProxy.getConsumableInfoUtil().getConsumableData(organicE.getConsumableName());
							MakeNotAvailableInfo makeNotAvailableInfo = new MakeNotAvailableInfo();
							MESConsumableServiceProxy.getConsumableServiceImpl().makeNotAvailable(consumableData, makeNotAvailableInfo, eventInfo);
						}
					}
				}
			}
		}

		return doc;
	}
}
