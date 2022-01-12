
package kr.co.aim.messolution.generic.util;

import kr.co.aim.greenframe.util.time.TimeStampUtil;
import kr.co.aim.greentrack.generic.info.EventInfo;
import kr.co.aim.greentrack.generic.util.TimeUtils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public class EventInfoUtil implements ApplicationContextAware
{
	
	private ApplicationContext	applicationContext;

	public void setApplicationContext( ApplicationContext arg0 ) throws BeansException
	{
		// TODO Auto-generated method stub
		applicationContext = arg0;
	}
	
	public static EventInfo makeEventInfoForOrganic( String eventName, String eventUser, String eventComment)
	{
		EventInfo eventInfo = new EventInfo();
		
		eventInfo.setBehaviorName( "SplitForOrganic" );
		eventInfo.setEventName( eventName );
		eventInfo.setEventUser( eventUser );
		
		if ( eventComment == null )
			eventComment = "";
		
		eventInfo.setEventComment( eventComment );
		eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
		// Do Not Check TimeKey Validation
		eventInfo.setCheckTimekeyValidation(false);
	    eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setReasonCodeType("");
		eventInfo.setReasonCode("");
		
		return eventInfo;
	}
	
	public static EventInfo makeEventInfo( String eventName, String eventUser, String eventComment)
	{
		EventInfo eventInfo = new EventInfo();
		
		eventInfo.setBehaviorName( "greenTrack" );
		eventInfo.setEventName( eventName );
		eventInfo.setEventUser( eventUser );
		
		if ( eventComment == null )
			eventComment = "";
		
		eventInfo.setEventComment( eventComment );
		eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
		// Do Not Check TimeKey Validation
		eventInfo.setCheckTimekeyValidation(false);
	    eventInfo.setEventTimeKey(TimeUtils.getCurrentEventTimeKey());
		eventInfo.setReasonCodeType("");
		eventInfo.setReasonCode("");
		
		return eventInfo;
	}
	
	public static EventInfo makeEventInfo( String eventName, String eventUser, String  eventComment,java.sql.Timestamp eventTimekey)
	{
		EventInfo eventInfo = new EventInfo();

		eventInfo.setBehaviorName("");
		eventInfo.setEventName(eventName);
		eventInfo.setEventUser(eventUser);
		eventInfo.setCheckTimekeyValidation(false);

		if (eventComment == null)
			eventComment = "";

		eventInfo.setEventComment(eventComment);
		eventInfo.setEventTime(eventTimekey);

		eventInfo.setReasonCodeType("");
		eventInfo.setReasonCode("");

		return eventInfo;
	}
	
	public static EventInfo makeEventInfo( String eventName, String eventUser, String  eventComment,
			String reasonCodeType, String reasonCode, String checkTimekeyValidation)
	{
		EventInfo eventInfo = new EventInfo();
		
		eventInfo.setBehaviorName( "" );
		eventInfo.setEventName( eventName );
		eventInfo.setEventUser( eventUser );

		if (checkTimekeyValidation.equals("N"))
			eventInfo.setCheckTimekeyValidation(false);

		if ( eventComment == null )
			eventComment = "";
		
		eventInfo.setEventComment( eventComment );
		eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
		
		if ( reasonCodeType == null )
			reasonCodeType = "";
		
		eventInfo.setReasonCodeType( reasonCodeType );
		
		if ( reasonCode == null )
			reasonCode = "";
		
		eventInfo.setReasonCode( reasonCode );
		
		return eventInfo;
	}

	public static EventInfo makeEventInfo( String eventName, String eventUser, String eventComment,
			String reasonCodeType, String reasonCode )
	{
		EventInfo eventInfo = new EventInfo();
		
		eventInfo.setBehaviorName( "greenTrack" );
		eventInfo.setEventName( eventName );
		eventInfo.setEventUser( eventUser );
		
		if ( eventComment == null )
			eventComment = "";
		
		eventInfo.setEventComment( eventComment );
		eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
		// Do Not Check TimeKey Validation
		eventInfo.setCheckTimekeyValidation(false);
		
		if ( reasonCodeType == null )
			reasonCodeType = "";
		
		eventInfo.setReasonCodeType( reasonCodeType );
		
		if ( reasonCode == null )
			reasonCode = "";
		
		eventInfo.setReasonCode( reasonCode );
		
		return eventInfo;
	}
	
	public static EventInfo makeEventInfo_completeRework( String eventName, String eventUser, String eventComment,
			String reasonCodeType, String reasonCode )
	{
		EventInfo eventInfo = new EventInfo();
		
		eventInfo.setBehaviorName( "L" );
		eventInfo.setEventName( eventName );
		eventInfo.setEventUser( eventUser );
		
		if ( eventComment == null )
			eventComment = "";
		
		eventInfo.setEventComment( eventComment );
		eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
		// Do Not Check TimeKey Validation
		eventInfo.setCheckTimekeyValidation(false);
		
		if ( reasonCodeType == null )
			reasonCodeType = "";
		
		eventInfo.setReasonCodeType( reasonCodeType );
		
		if ( reasonCode == null )
			reasonCode = "";
		
		eventInfo.setReasonCode( reasonCode );
		
		return eventInfo;
	}
	
	public static EventInfo makeEventInfo_BankManage( String eventName, String eventUser, String eventComment,	String reasonCodeType, String reasonCode )
	{
		EventInfo eventInfo = new EventInfo();
		
		eventInfo.setBehaviorName( "BankManagement" );
		eventInfo.setEventName( eventName );
		eventInfo.setEventUser( eventUser );
		
		if ( eventComment == null )
			eventComment = "";
		
		eventInfo.setEventComment( eventComment );
		eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
		// Do Not Check TimeKey Validation
		eventInfo.setCheckTimekeyValidation(false);
		
		if ( reasonCodeType == null )
			reasonCodeType = "";
		
		eventInfo.setReasonCodeType( reasonCodeType );
		
		if ( reasonCode == null )
			reasonCode = "";
		
		eventInfo.setReasonCode( reasonCode );
		
		return eventInfo;
	}
	
	public static EventInfo makeEventInfo_IgnoreHold( String eventName, String eventUser, String eventComment,
			String reasonCodeType, String reasonCode )
	{
		EventInfo eventInfo = new EventInfo();
		
		eventInfo.setBehaviorName( "LP" );
		eventInfo.setEventName( eventName );
		eventInfo.setEventUser( eventUser );
		
		if ( eventComment == null )
			eventComment = "";
		
		eventInfo.setEventComment( eventComment );
		eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
		// Do Not Check TimeKey Validation
		eventInfo.setCheckTimekeyValidation(false);
		
		if ( reasonCodeType == null )
			reasonCodeType = "";
		
		eventInfo.setReasonCodeType( reasonCodeType );
		
		if ( reasonCode == null )
			reasonCode = "";
		
		eventInfo.setReasonCode( reasonCode );
		
		return eventInfo;
	}
	
	public static EventInfo makeEventInfo_InUseConsumable( String eventName, String eventUser, String eventComment,
			String reasonCodeType, String reasonCode )
	{
		EventInfo eventInfo = new EventInfo();
		
		eventInfo.setBehaviorName( "LP" );
		eventInfo.setEventName( eventName );
		eventInfo.setEventUser( eventUser );
		
		if ( eventComment == null )
			eventComment = "";
		
		eventInfo.setEventComment( eventComment );
		eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
		// Do Not Check TimeKey Validation
		eventInfo.setCheckTimekeyValidation(false);
		
		if ( reasonCodeType == null )
			reasonCodeType = "";
		
		eventInfo.setReasonCodeType( reasonCodeType );
		
		if ( reasonCode == null )
			reasonCode = "";
		
		eventInfo.setReasonCode( reasonCode );
		
		return eventInfo;
	}
	
	public static EventInfo makeEventInfo_TrackInOutWithoutHistory( String eventName, String eventUser, String eventComment,
			String reasonCodeType, String reasonCode )
	{
		EventInfo eventInfo = new EventInfo();
		
		eventInfo.setBehaviorName( "LPC" );
		eventInfo.setEventName( eventName );
		eventInfo.setEventUser( eventUser );
		
		if ( eventComment == null )
			eventComment = "";
		
		eventInfo.setEventComment( eventComment );
		eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
		// Do Not Check TimeKey Validation
		eventInfo.setCheckTimekeyValidation(false);
		
		if ( reasonCodeType == null )
			reasonCodeType = "";
		
		eventInfo.setReasonCodeType( reasonCodeType );
		
		if ( reasonCode == null )
			reasonCode = "";
		
		eventInfo.setReasonCode( reasonCode );
		
		return eventInfo;
	}
	
	public static EventInfo makeEventInfo_BufferMix( String eventName, String eventUser, String eventComment,
			String reasonCodeType, String reasonCode )
	{
		EventInfo eventInfo = new EventInfo();
		
		eventInfo.setBehaviorName( "LPS" );
		eventInfo.setEventName( eventName );
		eventInfo.setEventUser( eventUser );
		
		if ( eventComment == null )
			eventComment = "";
		
		eventInfo.setEventComment( eventComment );
		eventInfo.setEventTime( TimeStampUtil.getCurrentTimestamp());
		// Do Not Check TimeKey Validation
		eventInfo.setCheckTimekeyValidation(false);
		
		if ( reasonCodeType == null )
			reasonCodeType = "";
		
		eventInfo.setReasonCodeType( reasonCodeType );
		
		if ( reasonCode == null )
			reasonCode = "";
		
		eventInfo.setReasonCode( reasonCode );
		
		return eventInfo;
	}
}
