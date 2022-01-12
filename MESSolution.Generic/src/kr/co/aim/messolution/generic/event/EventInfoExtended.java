package kr.co.aim.messolution.generic.event;

import kr.co.aim.greentrack.generic.info.EventInfo;

public class EventInfoExtended extends EventInfo {

	public EventInfoExtended(EventInfo eventInfo) 
	{
		super.setEventName(eventInfo.getEventName());
		super.setEventTimeKey(eventInfo.getEventTimeKey());
		super.setEventTime(eventInfo.getEventTime());
		super.setEventUser(eventInfo.getEventUser());
		super.setEventComment(eventInfo.getEventComment());
		super.setLastEventTimekey(eventInfo.getLastEventTimekey());
		super.setReasonCodeType(eventInfo.getReasonCodeType());
		super.setReasonCode(eventInfo.getReasonCode());
		super.setBehaviorName(eventInfo.getBehaviorName());
		super.setCheckTimekeyValidation(eventInfo.isCheckTimekeyValidation());
	}
	
	public void Clear()
	{
		super.setEventName("");
		super.setEventTimeKey("");
		super.setEventTime(null);
		super.setEventUser("");
		super.setEventComment("");
		super.setLastEventTimekey("");
		super.setReasonCodeType("");
		super.setReasonCode("");
		super.setBehaviorName("");
		super.setCheckTimekeyValidation(true);
	}
	
	public void setValue(EventInfo eventInfo)
	{
		super.setEventName(eventInfo.getEventName());
		super.setEventTimeKey(eventInfo.getEventTimeKey());
		super.setEventTime(eventInfo.getEventTime());
		super.setEventUser(eventInfo.getEventUser());
		super.setEventComment(eventInfo.getEventComment());
		super.setLastEventTimekey(eventInfo.getLastEventTimekey());
		super.setReasonCodeType(eventInfo.getReasonCodeType());
		super.setReasonCode(eventInfo.getReasonCode());
		super.setBehaviorName(eventInfo.getBehaviorName());
		super.setCheckTimekeyValidation(eventInfo.isCheckTimekeyValidation());		
	}
}
