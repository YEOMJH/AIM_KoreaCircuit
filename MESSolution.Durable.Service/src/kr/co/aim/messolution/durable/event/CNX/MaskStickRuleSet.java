package kr.co.aim.messolution.durable.event.CNX;

import java.util.List;
import org.jdom.Document;
import org.jdom.Element;

import com.sun.xml.internal.ws.util.StringUtils;

import kr.co.aim.messolution.extended.object.ExtendedObjectProxy;
import kr.co.aim.messolution.extended.object.management.data.MaskStickRule;
import kr.co.aim.messolution.generic.errorHandler.CustomException;
import kr.co.aim.messolution.generic.eventHandler.SyncHandler;
import kr.co.aim.messolution.generic.util.EventInfoUtil;
import kr.co.aim.messolution.generic.util.SMessageUtil;
import kr.co.aim.greenframe.exception.greenFrameDBErrorSignal;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.StringUtil;
import kr.co.aim.greentrack.generic.util.TimeUtils;

public class MaskStickRuleSet extends SyncHandler {

	@Override
	public Object doWorks(Document doc) throws CustomException {
		String actionFlag = SMessageUtil.getBodyItemValue(doc, "ACTIONFLAG", false);
		if(actionFlag.equals("Add"))
		{
			List<Element>MaskStickRuleSetList = SMessageUtil.getBodySequenceItemList(doc, "MaskStickSetLIST", true);
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("AddMaskStickRuleSet", this.getEventUser(), this.getEventComment(), "", "");
			eventInfo.setEventTimeKey(TimeUtils.getEventTimeKeyFromTimestamp(eventInfo.getEventTime()));
			for(Element eledur : MaskStickRuleSetList)
			{
				String maskFilmLayer = eledur.getChild("MASKFILMLAYER").getText();
				String stickType = eledur.getChild("STICKTYPE").getText();
				String stickFilmLayer = eledur.getChild("STICKFILMLAYER").getText();
				String description = eledur.getChild("DESCRIPTION").getText();		
				if(!checkExist(maskFilmLayer, stickType,stickFilmLayer)){
					throw new CustomException("MASKSTICK-0001", maskFilmLayer,stickType);
				}				
				MaskStickRule dataInfo = new MaskStickRule();
				dataInfo.setMaskFilmLayer(maskFilmLayer);
				dataInfo.setStickType(stickType);
				dataInfo.setStickFilmLayer(stickFilmLayer);
				dataInfo.setDescription(description);
				
				dataInfo.setLastEventName(eventInfo.getEventName());
				dataInfo.setLastEventUser(eventInfo.getEventUser());
				dataInfo.setLastEventTime(eventInfo.getEventTime());
				dataInfo.setLastEventComment(eventInfo.getEventComment());
				dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
			
				ExtendedObjectProxy.getMaskStickRuleServices().create(eventInfo, dataInfo);
			}
		}
		else if (actionFlag.equals("Remove"))
		{
			List<Element>MaskStickRuleSetList = SMessageUtil.getBodySequenceItemList(doc, "RemoveMaskStickRuleLIST", true);
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("RemoveMaskStickRule", this.getEventUser(), this.getEventComment(), "", "");
			for(Element eledur : MaskStickRuleSetList)
			{
				String maskFilmLayer = eledur.getChild("MASKFILMLAYER").getText();
				String stickType = eledur.getChild("STICKTYPE").getText();
				String stickFilmLayer = eledur.getChild("STICKFILMLAYER").getText();
				String description = eledur.getChild("DESCRIPTION").getText();	
				if(!checkExist(maskFilmLayer, stickType,stickFilmLayer)){
					throw new CustomException("MASKSTICK-0002", maskFilmLayer,stickType);
				}
				MaskStickRule dataInfo = new MaskStickRule();
				dataInfo.setMaskFilmLayer(maskFilmLayer);
				dataInfo.setStickType(stickType);
				dataInfo.setStickFilmLayer(stickFilmLayer);
				dataInfo.setDescription(description);
				
				dataInfo.setLastEventName(eventInfo.getEventName());
				dataInfo.setLastEventUser(eventInfo.getEventUser());
				dataInfo.setLastEventTime(eventInfo.getEventTime());
				dataInfo.setLastEventComment(eventInfo.getEventComment());
				dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
				ExtendedObjectProxy.getMaskStickRuleServices().remove(eventInfo, dataInfo);
			}
		}
		else if(actionFlag.equals("Modify"))
		{
			EventInfo eventInfo = EventInfoUtil.makeEventInfo("ModifyMaskStickRule", this.getEventUser(), this.getEventComment(), "", "");
			String maskFilmLayer = SMessageUtil.getBodyItemValue(doc, "MASKFILMLAYER", true);		
			String stickType = SMessageUtil.getBodyItemValue(doc, "STICKTYPE", true);	
			String stickFilmLayer = SMessageUtil.getBodyItemValue(doc, "STICKFILMLAYER", false);	
			String description = SMessageUtil.getBodyItemValue(doc, "DESCRIPTION", false);
			if(checkExist(maskFilmLayer, stickType,stickFilmLayer)){
				throw new CustomException("MASKSTICK-0001", maskFilmLayer,stickType);
			}
			MaskStickRule dataInfo = new MaskStickRule();
			dataInfo.setMaskFilmLayer(maskFilmLayer);
			dataInfo.setStickType(stickType);
			dataInfo.setStickFilmLayer(stickFilmLayer);
			dataInfo.setDescription(description);
			
			dataInfo.setLastEventName(eventInfo.getEventName());
			dataInfo.setLastEventUser(eventInfo.getEventUser());
			dataInfo.setLastEventTime(eventInfo.getEventTime());
			dataInfo.setLastEventComment(eventInfo.getEventComment());
			dataInfo.setLastEventTimekey(eventInfo.getEventTimeKey());
			ExtendedObjectProxy.getMaskStickRuleServices().modify(eventInfo, dataInfo);
		}
		else
		{
			//CUSTOM-0001: There is something unexpected occured,please call CIM!
			throw new CustomException("CUSTOM-0001");
		}
	    return doc;
	}
	
	private boolean checkExist(String maskFilmLayer,String stickType,String stickFilmLayer) throws greenFrameDBErrorSignal, CustomException
	{
		try
		{
			ExtendedObjectProxy.getMaskStickRuleServices().selectByKey(false, new Object[] {maskFilmLayer,stickType,stickFilmLayer});
			return false;
		}
		catch (Exception e)
		{
			return true;
		}
	}
}