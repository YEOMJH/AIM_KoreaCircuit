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

public class EditDefectFQCCode extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException
	{
		String enumName = SMessageUtil.getBodyItemValue(doc, "ENUMNAME", true);
		String enumValue = SMessageUtil.getBodyItemValue(doc, "ENUMVALUE", true);
		String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", true);

		boolean EnumDefValue = ExtendedObjectProxy.getEnumDefValueService().isExistEnumNameInfo(enumName, enumValue);
		boolean enumDef = ExtendedObjectProxy.getEnumDefService().isExistEnumName(enumName);

		if (EnumDefValue)
		{
			// insert enumDefValue
			EnumDefValue enumDefValue = new EnumDefValue();
			enumDefValue.setEnumName(enumName);
			enumDefValue.setEnumValue(enumValue);
			enumDefValue.setDescription(description);

			ExtendedObjectProxy.getEnumDefValueService().modifyDescription(enumDefValue);

			if (enumDef)
			{
				ExtendedObjectProxy.getEnumDefService().modify(enumValue, description);
			}

		}

		return doc;
	}

}
