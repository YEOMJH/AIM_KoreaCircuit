package kr.co.aim.messolution.durable.event.CNX;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskMaterials;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class CreateMaskMaterials extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String materialType = SMessageUtil.getBodyItemValue(doc, "MATERIALTYPE", true);
		String maskMaterialName = SMessageUtil.getBodyItemValue(doc, "MASKMATERIALNAME", true);
		String checkState = SMessageUtil.getBodyItemValue(doc, "CHECKSTATE", true);
		String maskSpec = SMessageUtil.getBodyItemValue(doc, "MASKSPEC", true);
		String maskLayer=SMessageUtil.getBodyItemValue(doc, "LAYER", true);
		String Property = SMessageUtil.getBodyItemValue(doc, "PROPERTY", false);
		String strThickness = SMessageUtil.getBodyItemValue(doc, "THICKNESS", false);
		String strAddQuantity = SMessageUtil.getBodyItemValue(doc, "ADDQUANTITY", false);
		String strAddScrapQuantity = SMessageUtil.getBodyItemValue(doc, "ADDSCRAPQUANTITY", false);
		String maskMaterialVersion=SMessageUtil.getBodyItemValue(doc, "MASKMATERIALVERSION", false);

		long thickness = (!StringUtil.equals(strThickness, "")) ? Long.parseLong(strThickness) : 0;
		long addQuantity = (!StringUtil.equals(strAddQuantity, "")) ? Long.parseLong(strAddQuantity) : 0;
		long addScrapQuantity = (!StringUtil.equals(strAddScrapQuantity, "")) ? Long.parseLong(strAddScrapQuantity) : 0;

		EventInfo eventInfo = EventInfoUtil.makeEventInfo("", this.getEventUser(), this.getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		MaskMaterials maskMaterials = new MaskMaterials();

		if (checkState.equals("Create"))
		{
			if(addQuantity<addScrapQuantity)
			{
				throw new CustomException("MASK-0076", maskMaterialName);
			}
			eventInfo.setEventName("CreateMaskMaterials");

			maskMaterials.setMaterialType(materialType);
			maskMaterials.setMaskMaterialName(maskMaterialName);;
			maskMaterials.setMaskSpec(maskSpec);;
			maskMaterials.setMaskMaterialVersion(maskMaterialVersion);
		    maskMaterials.setLayer(maskLayer);
			maskMaterials.setThickness(thickness);
			maskMaterials.setProperty(Property);
			maskMaterials.setTotalQuantity(addQuantity);
			maskMaterials.setScrapQuantity(addScrapQuantity);
			maskMaterials.setAddQuantity(addQuantity);
			maskMaterials.setAddScrapQuantity(addScrapQuantity);
			maskMaterials.setCreateUser(eventInfo.getEventUser());
			maskMaterials.setCreateTime(eventInfo.getEventTime());
			maskMaterials.setLastEventName(eventInfo.getEventName());
			maskMaterials.setLastEventUser(eventInfo.getEventUser());
			maskMaterials.setLastEventTime(eventInfo.getEventTime());
			maskMaterials.setLastEventComment(eventInfo.getEventComment());

			ExtendedObjectProxy.getMaskMaterialsService().create(eventInfo, maskMaterials);
		}

		if (checkState.equals("Modify"))
		{
			maskMaterials = ExtendedObjectProxy.getMaskMaterialsService().selectByKey(false, new Object[] { materialType, maskMaterialName });

			if((addQuantity+maskMaterials.getTotalQuantity())<(addScrapQuantity+maskMaterials.getScrapQuantity()))
			{
				throw new CustomException("MASK-0076", maskMaterials.getMaskMaterialName());
			}
			
			eventInfo.setEventName("ModifyMaterials");

			maskMaterials.setProperty(Property);
			maskMaterials.setAddQuantity(addQuantity);
			maskMaterials.setAddScrapQuantity(addScrapQuantity);
			maskMaterials.setTotalQuantity(maskMaterials.getTotalQuantity() + addQuantity);
			maskMaterials.setScrapQuantity(maskMaterials.getScrapQuantity() + addScrapQuantity);
			maskMaterials.setCreateUser(eventInfo.getEventUser());
			maskMaterials.setCreateTime(eventInfo.getEventTime());
			maskMaterials.setLastEventName(eventInfo.getEventName());
			maskMaterials.setLastEventUser(eventInfo.getEventUser());
			maskMaterials.setLastEventTime(eventInfo.getEventTime());
			maskMaterials.setLastEventComment(eventInfo.getEventComment());

			ExtendedObjectProxy.getMaskMaterialsService().modify(eventInfo, maskMaterials);
		}

		return doc;
	}
}
