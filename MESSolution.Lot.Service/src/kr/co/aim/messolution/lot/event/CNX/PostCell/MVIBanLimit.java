package kr.co.aim.messolution.lot.event.CNX.PostCell;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.EnumDefValue;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;

public class MVIBanLimit extends SyncHandler {

	private static Log log = LogFactory.getLog(MVIBanLimit.class);

	@Override
	public Object doWorks(Document doc) throws CustomException {
		
		String actionName = SMessageUtil.getBodyItemValue(doc, "ACTIONNAME", true);
		String enumName = SMessageUtil.getBodyItemValue(doc, "ENUMNAME", true);
		String enumValue = SMessageUtil.getBodyItemValue(doc, "ENUMVALUE", true);
		String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo(actionName+"MVIBanLimit", getEventUser(), getEventComment(), "", "");
		
		boolean EnumDefValue = ExtendedObjectProxy.getEnumDefValueService().isExistEnumNameInfo(enumName, enumValue);
		boolean enumDef = ExtendedObjectProxy.getEnumDefService().isExistEnumName(enumName);
		
		// insert enumDef_enumValue/description
		if(StringUtil.equals(actionName, "Add"))
		{
			if(EnumDefValue)
			{
				throw new CustomException("MVI-0001",enumValue);
			}
			EnumDefValue enumDefValue = new EnumDefValue();
			enumDefValue.setEnumName(enumName);
			enumDefValue.setEnumValue(enumValue);
			enumDefValue.setDescription(description);
			enumDefValue.setDefaultFlag("N");
			enumDefValue.setSeq( ExtendedObjectProxy.getEnumDefValueService().getEnumDefValueSeq(enumName));
			
			ExtendedObjectProxy.getEnumDefValueService().create(enumDefValue);
			log.info("Excute Insert Notice :Inserted into EnumDefValue! ");
			
		}
		else if(StringUtil.equals(actionName, "Modify"))
		{
			// update enumDefValue_description
			EnumDefValue enumDefValue = new EnumDefValue();
			enumDefValue.setEnumName(enumName);
			enumDefValue.setEnumValue(enumValue);
			enumDefValue.setDescription(description);

			ExtendedObjectProxy.getEnumDefValueService().modifyDescription(enumDefValue);
			log.info("Excute Insert Notice :Modify into EnumDefValue! ");
		}
		else if(StringUtil.equals(actionName, "Delete"))
		{
			if(!EnumDefValue)
			{
				throw new CustomException("SYS-0010",enumName);
			}
			ExtendedObjectProxy.getEnumDefValueService().remove(enumName,enumValue);
			log.info("Excute Insert Notice :Delete into EnumDefValue! ");
		}
		
		return doc;
	}
}
