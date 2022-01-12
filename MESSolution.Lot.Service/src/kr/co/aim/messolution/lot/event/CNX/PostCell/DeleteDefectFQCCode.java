package kr.co.aim.messolution.lot.event.CNX.PostCell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.FQCRule;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.messolution.lot.MESLotServiceProxy;
import kr.co.aim.greenframe.greenFrameServiceProxy;
import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;

import kr.co.aim.messolution.extended.object.management.data.EnumDef;
import kr.co.aim.messolution.extended.object.management.data.EnumDefValue;
import kr.co.aim.messolution.extended.object.management.impl.EnumDefService;
import kr.co.aim.messolution.extended.object.management.impl.EnumDefValueService;

public class DeleteDefectFQCCode extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String enumName = SMessageUtil.getBodyItemValue(doc, "ENUMNAME", true);
		String enumValue = SMessageUtil.getBodyItemValue(doc, "ENUMVALUE", true);

		boolean EnumDefValueInfo = ExtendedObjectProxy.getEnumDefValueService().isExistEnumNameInfoByEnumValue(enumValue);
		boolean EnumDefValue = ExtendedObjectProxy.getEnumDefValueService().isExistEnumNameInfo(enumName, enumValue);

		if (!EnumDefValue)
		{
			throw new CustomException("ENUM-0001", enumName);
		}

		boolean enumDef = ExtendedObjectProxy.getEnumDefService().isExistEnumName(enumValue);

		if (EnumDefValueInfo)
		{
			// delete enumValue list by enumValue--lev3 code
			ExtendedObjectProxy.getEnumDefValueService().removeEnumValuelist(enumValue);

			// lev2 code
			ExtendedObjectProxy.getEnumDefValueService().remove(enumName, enumValue);

			// delete enumDef by enumName
			ExtendedObjectProxy.getEnumDefService().remove(enumValue);
		}
		else
		{
			ExtendedObjectProxy.getEnumDefValueService().remove(enumName, enumValue);
		}

		return doc;
	}

}
