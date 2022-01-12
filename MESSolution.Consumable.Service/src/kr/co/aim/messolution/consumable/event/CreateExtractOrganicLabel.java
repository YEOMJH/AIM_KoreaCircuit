package kr.co.aim.messolution.consumable.event;

import org.jdom.Document;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.OrganicExtractCard;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class CreateExtractOrganicLabel extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		String materialID = SMessageUtil.getBodyItemValue(doc, "MATERIALID", true);
		String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", true);
		String location = SMessageUtil.getBodyItemValue(doc, "LOCATION", true);
		String grade = SMessageUtil.getBodyItemValue(doc, "GRADE", true);
		
		EventInfo eventInfo = EventInfoUtil.makeEventInfo("CreateExtracktOrganicLabel", getEventUser(), getEventComment(), "", "");
		eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		
		OrganicExtractCard dataInfo = new OrganicExtractCard();
		try{
			dataInfo = ExtendedObjectProxy.getOrganicExtractCardService().selectByKey(false, new Object[]{materialID});
		}
		catch(Exception e){
			dataInfo = new OrganicExtractCard();
		}
		
//		if(dataInfo!= null){
//			throw new CustomException("MATERIAL-0037", "MaterilID has created by other user.");
//			
//		}
		
		dataInfo.setMaterialID(materialID);
		dataInfo.setDescription(description);
		dataInfo.setLocation(location);
		dataInfo.setState("Created");
		dataInfo.setGrade(grade);
		dataInfo.setLastEventName(eventInfo.getEventName());
		dataInfo.setLastEventUser(eventInfo.getEventUser());
		dataInfo.setLastEventTime(eventInfo.getEventTime());
		dataInfo.setLastEventComment(eventInfo.getEventComment());
		dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());

		
		ExtendedObjectProxy.getOrganicExtractCardService().create(eventInfo, dataInfo);
		
		return doc;
	}

}
