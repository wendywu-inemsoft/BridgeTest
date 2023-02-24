package com.objecttel.ClassOne.BridgeTest;

import java.util.EventObject;
import com.avaya.conferencing.api.acp.control.Conference;

/** 
 * <p>Copyright: Copyright ObjectTel (c) 2003-2013</p>
 * <p>Company: Objecttel</p> 
 */
public class BridgeEvent {
	public static final int CONFERENCE_START=1;
	public static final int CONFERENCE_ENDED=2;
	public static final int ENDPOINT_ADDED=3;
	public static final int ENDPOINT_REMOVED=4;
	public static final int CONFERENCE_PROPERTY_CHANGED=5;
	public static final int ENDPOINT_PROPERTY_CHANGED=6;
	public static final int DTMF_EVENT=7;
	public static final int TALKER_EVENT=8;
	public static final int CONFERENCE_RECOVER=100;
		  
  private int eventType;
	private EventObject event;
	private Conference conference;
	private java.lang.Object userRequest;
	
	public Conference getConference() {
		return conference;
	}

	public void setConference(Conference conference) {
		this.conference = conference;
	}

	private long enterTimeStamp;
	public BridgeEvent(int eventType, EventObject event, long ts) {
		super();
		this.eventType = eventType;
		this.event = event;
		this.conference=null;
		this.enterTimeStamp=ts;
	}
	
	public BridgeEvent(int eventType, Conference conf, long ts)
	{
		super();
		this.eventType = eventType;
		this.conference = conf;
		this.event = null;
		this.enterTimeStamp=ts;
	}
	
	public BridgeEvent(int eventType, long ts)
	{
		super();
		this.eventType = eventType;
		this.conference = null;;
		this.event = null;
		this.enterTimeStamp=ts;
	}
	
	public BridgeEvent(int eventType, Object request, long ts)
	{
		this.eventType = eventType;
		this.conference = null;;
		this.event = null;
		this.enterTimeStamp=ts;
		this.userRequest=request;
	}
	
	public long getEnterTimeStamp() {
		return enterTimeStamp;
	}	

	public int getEventType() {
		return eventType;
	}
	public void setEventType(int eventType) {
		this.eventType = eventType;
	}
	public EventObject getEvent() {
		return event;
	}
	public void setEvent(EventObject event) {
		this.event = event;
	}
	
	public Object getUserRequest()
	{
		return userRequest;
	}
	
	public static String toString(int type)
	{
		if(type == CONFERENCE_START)
			return "conference start";
		else if(type == CONFERENCE_ENDED)
			return "conference ended";		
		else if(type == ENDPOINT_ADDED)
			return "endpoint added";
		else if(type == ENDPOINT_REMOVED)
			return "endpoint removed";
		else if(type ==CONFERENCE_PROPERTY_CHANGED)
			return "conference property changed";
		else if(type == ENDPOINT_PROPERTY_CHANGED)
			return "endpoint property changed";
		else if(type == DTMF_EVENT)
			return "DTMF";
		else if (type == TALKER_EVENT)
			return "talker";	
    else
      return "Invalid event " + type;
	}

}
