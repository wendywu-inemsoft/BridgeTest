package com.objecttel.ClassOne.BridgeTest;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.avaya.conferencing.api.acp.control.Conference;
import com.avaya.conferencing.api.acp.control.ConferencingObject;
import com.avaya.conferencing.api.acp.control.Connection;
import com.avaya.conferencing.api.acp.control.EndPoint;
import com.avaya.conferencing.api.acp.control.States;
import com.avaya.conferencing.api.acp.control.events.ChildEvent;
import com.avaya.conferencing.api.acp.control.events.ConferenceEndPointListener;
import com.avaya.conferencing.api.acp.control.events.DTMFEvent;
import com.avaya.conferencing.api.acp.control.events.DTMFListener;
import com.avaya.conferencing.api.acp.control.events.SourceSequentialEventListener;
import com.avaya.conferencing.api.acp.control.operations.Disconnect;
import com.avaya.conferencing.api.acp.control.operations.LectureMode;
import com.avaya.conferencing.api.acp.control.operations.MakeCall;
import com.avaya.conferencing.api.acp.control.operations.ModifyDetails;
import com.avaya.conferencing.api.acp.control.operations.Mute;
import com.avaya.conferencing.api.acp.control.operations.OperationFactory;
import com.avaya.conferencing.api.acp.control.State;



/**
 * <p>Copyright: Copyright ObjectTel (c) 2003-2013</p>
 * <p>Company: Objecttel</p>
 */
public class RadioAccessPoint extends SimpleQueue{
	private static Logger log = Logger.getLogger(RadioAccessPoint.class);
	private RadioMasterData radioData;
	private String confNumber;
	private Conference activeConference=null;
	private DTMFListenerAdapter dtmfListener=null;
	private ConferenceEndPointListenerAdapter conferenceEndPointListener=null;
	private PropertyChangeListenerAdapter conferencePropertyListener=null;
	private PropertyChangeListenerAdapter endPointPropertyListener=null;
	private String logPrefix="";
	private boolean dialingRadio=false;
	private Integer activeConferenceID=null;

	public RadioAccessPoint(RadioMasterData rdData)
	{
		super(rdData.getRadioName());
		radioData=rdData;
		logPrefix=radioData.getRadioName() + " ";		
		dtmfListener=new DTMFListenerAdapter();
		conferenceEndPointListener=new ConferenceEndPointListenerAdapter();
		conferencePropertyListener=new PropertyChangeListenerAdapter(BridgeEvent.CONFERENCE_PROPERTY_CHANGED);
		endPointPropertyListener=new PropertyChangeListenerAdapter(BridgeEvent.ENDPOINT_PROPERTY_CHANGED);
	}

	private class ConferenceEndPointListenerAdapter implements ConferenceEndPointListener
	{
		@Override
		public void childAdded(ChildEvent<Conference, EndPoint> evt) {
			try{
				log.info(logPrefix + "receive EndPoint added event, seqNum=" + evt.getSequenceNumber());
				BridgeEvent bridgeEvent=new BridgeEvent(BridgeEvent.ENDPOINT_ADDED, evt, System.currentTimeMillis());
				queueEntry(bridgeEvent);
			}
			catch(Exception e)
			{
				log.error(logPrefix + "childAdded exception", e);
			}

		}

		@Override
		public void childRemoved(ChildEvent<Conference, EndPoint> evt) {
			try{
				log.info(logPrefix + "receive EndPoint removed event, seqNum=" + evt.getSequenceNumber());
				BridgeEvent bridgeEvent=new BridgeEvent(BridgeEvent.ENDPOINT_REMOVED, evt, System.currentTimeMillis());
				queueEntry(bridgeEvent);
			}catch(Exception e)
			{
				log.error(logPrefix + "childRemoved exception", e);
			}			
		}
	}

	private class PropertyChangeListenerAdapter implements PropertyChangeListener {
		private int eventType;
		public PropertyChangeListenerAdapter(int type)
		{
			eventType=type;
		}
		public void propertyChange(PropertyChangeEvent evt)
		{
			try{
				log.info(logPrefix + "receive " + BridgeEvent.toString(eventType) + " event"); 			
				BridgeEvent bridgeEvent=new BridgeEvent(eventType, evt, System.currentTimeMillis());
				queueEntry(bridgeEvent); 

			}catch(Exception e)
			{
				log.error(logPrefix + "propertyChange ", e);
			}
		}
	}

	private class DTMFListenerAdapter implements DTMFListener, SourceSequentialEventListener
	{
		public void dtmfReceived(DTMFEvent evt)
		{
			try{
				log.info(logPrefix + "receive DTMF event, seqNum=" + evt.getSequenceNumber());
				BridgeEvent bridgeEvent=new BridgeEvent(BridgeEvent.DTMF_EVENT, evt, System.currentTimeMillis());
				queueEntry(bridgeEvent);
			}
			catch(Exception e)
			{
				log.error(logPrefix + "dtmfReceived exception ", e);
			}
		}

		public void eventBacklogPurged(Object source, int purgeCount)
		{
			log.info( logPrefix + "eventBacklogPurged() " + source + " purgeCount=" + purgeCount);
		}

		public boolean eventBacklogWarning(Object source, int queueSize)
		{
			log.info( logPrefix + "eventBacklogWarning() " + source + " queueSize="  + queueSize);
			if(queueSize > 1024)
				return true;
			else
				return false;
		}

		public int getEventBacklogLimit()
		{
			return 1024;
		}
	}	

	public void processEntry(java.lang.Object entry)
	{
		if(entry!=null){
			if(entry instanceof BridgeEvent){
				processEntry((BridgeEvent)entry);
			}
			else{
				log.info(logPrefix + "Unknown queue event type: "+entry.getClass().getName());
			}
		}
		return;
	}  

	private void processEntry(BridgeEvent event)
	{

		try{
			int eventType = event.getEventType();      

			// you can't use a switch with Integers... you need ints
			if(eventType==BridgeEvent.CONFERENCE_START){
				processConferenceStart(event);
			}
			else if(eventType==BridgeEvent.CONFERENCE_RECOVER)
			{
				processConferenceRecover(event);
			}
			else if(eventType==BridgeEvent.CONFERENCE_ENDED)
			{
				processConferenceEnd(event);
			}      
			else if(eventType==BridgeEvent.CONFERENCE_PROPERTY_CHANGED)
			{
				processConferencePropertyChange(event);
			}
			else if(eventType==BridgeEvent.ENDPOINT_ADDED)
			{
				processEndPointAdded(event);
			}
			else if(eventType==BridgeEvent.ENDPOINT_REMOVED)
			{
				processEndPointRemoved(event);
			}     
			else if(eventType==BridgeEvent.ENDPOINT_PROPERTY_CHANGED)
			{
				processParticipantPropertyChange(event);
			} 

			else if(eventType==BridgeEvent.DTMF_EVENT){
				processDTMFEvent(event);
			}      
			else{
				log.error(logPrefix + "Invalid event type: "+eventType);
			}
		}
		catch(Exception e){
			log.error(logPrefix + "Exception in processEntry(): ",e);
		}
		return;
	}

	private void processConferenceStart(BridgeEvent event)
	{
		try{
			EventObject evt=event.getEvent();
			ChildEvent<Connection,Conference> confEvt = (ChildEvent<Connection,Conference>)evt;
			Conference conference = confEvt.getChild();
			long seqNum= confEvt.getSequenceNumber();
			Integer conferenceID=(Integer)conference.getPropertyValue(BridgeConstant.CONF_ID);
			activeConference=conference;
			activeConferenceID=conferenceID;
			registerConferenceListeners();
			List<String> customFields = new ArrayList<String>();
          	customFields.add(radioData.getRadioGWExt());
          	customFields.add("1");
          	Map<String, Object> properties = new HashMap<String, Object>();
          	// update conference info for unit testing
          	properties.put(Conference.NAME, radioData.getRadioName());
            properties.put(Conference.CUSTOM_FIELDS, customFields);
            setConferenceProperties(conference, properties);
			if(!isPhoneNumberInConference(radioData.getRadioGWExt(), conference))
			{
				Thread localThread=new Thread()
				{
					public void run()
					{
						dialingRadio=true;
						if(!makeCall(activeConference, radioData.getRadioGWExt()))
						{
							log.error(logPrefix + " fail to dial radio gateway, close conferece");
							closeConference(activeConference);
						}
						dialingRadio=false;
					}					
				};
				localThread.start();
			}
			log.info(logPrefix + "conference open with ID: " + conferenceID + ", sequence number: " + seqNum);
		}
		catch(Exception e)
		{
			log.error(logPrefix + "processConferenceStart ", e);
		}
	}

	private void processConferenceRecover(BridgeEvent event)
	{
		try{
			Conference conference = event.getConference();
			if(conference != null)
			{
				Integer conferenceID=(Integer)conference.getPropertyValue(BridgeConstant.CONF_ID);
				activeConference=conference;
				activeConferenceID=conferenceID;
				registerConferenceListeners();
				if(!isPhoneNumberInConference(radioData.getRadioGWExt(), conference))
				{
					Thread localThread=new Thread()
					{
						public void run()
						{
							dialingRadio=true;
							if(!makeCall(activeConference, radioData.getRadioGWExt()))
							{
								log.error(logPrefix + " fail to dial radio gateway, close conferece");
								closeConference(activeConference);
							}
							dialingRadio=false;
						}					
					};
					localThread.start();
				}
				log.info(logPrefix + "conference recover with ID: " + conferenceID);
			}
		}
		catch(Exception e)
		{
			log.error(logPrefix + "processConferenceRecover ", e);
		}
	}

	private void processConferenceEnd(BridgeEvent event)
	{
		try{
			EventObject evt=event.getEvent();
			ChildEvent<Connection,Conference> confEvt = (ChildEvent<Connection,Conference>)evt;
			Conference conference = confEvt.getChild();
			long seqNum= confEvt.getSequenceNumber();
			Integer conferenceID=(Integer)conference.getPropertyValue(BridgeConstant.CONF_ID);			
			unregisterConferenceListeners();
			activeConference=null;
			activeConferenceID=null;
			log.info(logPrefix + "conference close with ID: " + conferenceID + ", sequence number: " + seqNum);
		}
		catch(Exception e)
		{
			log.error(logPrefix + "processConferenceStart ", e);
		}
	}

	private void processConferencePropertyChange(BridgeEvent event)
	{
		EventObject evt=event.getEvent();			
		if(evt != null)
		{    		
			PropertyChangeEvent propertyEvent = (PropertyChangeEvent)evt;
			log.info(logPrefix+"process ConferenceProperty Change event,"
					+ " property names=" + propertyEvent.getPropertyName()
					+", old values=" + propertyEvent.getOldValue() + ", new values=" + propertyEvent.getNewValue() );
		}
	}

	private void processEndPointAdded(BridgeEvent bridgeEvent)
	{
		EventObject evt=bridgeEvent.getEvent();
		if(evt != null)
		{  			
			ChildEvent<Conference, EndPoint> event = (ChildEvent<Conference, EndPoint>) evt;
			log.info(logPrefix+"Process EndPoint Added Event, seqNum=" + event.getSequenceNumber() + " ...");
			EndPoint endPoint = event.getChild();
			Conference conference = event.getSource();  			
			// get the conference channel ID
			if(conference != null && endPoint != null)
			{
				Integer conferenceID=(Integer)conference.getPropertyValue(BridgeConstant.CONF_ID);
				if(conferenceID!=null){
					// if conference is the current conference
					if(conferenceID.equals(activeConferenceID)){
						// get the participant ID
						Integer endPointID=(Integer)endPoint.getPropertyValue(BridgeConstant.ENDPOINT_ID);
						if(endPointID!=null){
							log.info(logPrefix+"EndPoint Added Event: conference: "+conferenceID+
									" participant: "+endPointID);
							dumpProperties("EndPoint " + endPointID+ ":", endPoint);
							Set<State> state=endPoint.getState();
							// test mute and un-mute
							if(state == null || state.contains(States.MUTED))
							{
								//muteEndPoint(endPoint, false);								
							}
							
							

							if(isEndpointRadio(endPoint))
							{
								setEndPointProperty(endPoint, EndPoint.NAME, radioData.getRadioName());
								List<String> customFields = new ArrayList<String>();
								customFields.add("Talk and Listen");
								customFields.add("Radio");
								customFields.add(getPhoneNumberFromEndpoint(endPoint));
								setEndPointProperty(endPoint, EndPoint.CUSTOM_FIELDS, customFields);
							}
							else
							{
								String phoneNumber=getPhoneNumberFromEndpoint(endPoint);
								setEndPointProperty(endPoint, EndPoint.NAME, "Tel " + phoneNumber);
								List<String> customFields = new ArrayList<String>();
								customFields.add("Talk and Listen");
								customFields.add("Phone");
								customFields.add(phoneNumber);
								setEndPointProperty(endPoint, EndPoint.CUSTOM_FIELDS, customFields);
							}
						}
					}
				}
			}
		}
	}

	private void processEndPointRemoved(BridgeEvent bridgeEvent)
	{
		EventObject evt=bridgeEvent.getEvent();
		if(evt != null)
		{
			ChildEvent<Conference, EndPoint> event = (ChildEvent<Conference, EndPoint>) evt;
			log.info(logPrefix+"Process EndPoint Removed Event, seqNum=" + event.getSequenceNumber() + " ...");
			EndPoint endPoint = event.getChild();
			Conference conference = event.getSource();
			if(conference != null && endPoint != null)
			{
				// get the conference channel ID
				Integer conferenceID=(Integer)conference.getPropertyValue(BridgeConstant.CONF_ID);
				if(conferenceID!=null){
					// if conference is the current conference
					if(conferenceID.equals(activeConferenceID)){
						// get the participant ID
						Integer endPointID=(Integer)endPoint.getPropertyValue(BridgeConstant.ENDPOINT_ID);
						if(endPointID!=null){
							log.info(logPrefix+"EndPoint Exit Event: conference: "+conferenceID+
									" endPoint: "+endPointID + ", endPoint is active: " + endPoint.isActive());
							Collection<EndPoint> endPointList=conference.getEndPoints();
							if(endPointList == null || endPointList.size()<=1)
							{
								log.info(logPrefix + "only one or no endpoint in conference, close the conference");
								closeConference(activeConference);
							}
						}
					}
				}
			}
		}
	}

	private void processParticipantPropertyChange(BridgeEvent bridgeEvent)
	{
		EventObject evt=bridgeEvent.getEvent();
		if(evt != null)
		{
			PropertyChangeEvent propertyEvent = (PropertyChangeEvent) evt;
			EndPoint endPoint = (EndPoint)propertyEvent.getSource();
			String propertyName=propertyEvent.getPropertyName();
			if(propertyName != null)
			{
				Integer endPointID = (Integer) endPoint.getPropertyValue(BridgeConstant.ENDPOINT_ID);
				Object oldValue=propertyEvent.getOldValue();
				Object newValue=propertyEvent.getNewValue();
				log.info(logPrefix+"EndPoint property change event for endpoint ID=" 
						+ endPointID + " name=" + endPoint.getName()+ ", property name=" + propertyName +
						", old value=" + oldValue + ", new value=" + newValue);
			}
		}
	}

	private void processDTMFEvent(BridgeEvent bridgeEvent)
	{
		EventObject evt = bridgeEvent.getEvent();  	
		if(evt != null)
		{
			DTMFEvent dtmfEvent = (DTMFEvent)evt;
			log.info(logPrefix+"Process DTMF event " + dtmfEvent.getSequenceNumber());
			EndPoint endPoint = dtmfEvent.getSource();
			if(endPoint != null)
			{     
				// get the endPoint ID
				Integer endPointID=(Integer)endPoint.getPropertyValue(BridgeConstant.ENDPOINT_ID);

				String pattern=dtmfEvent.getDtmf();
				if(pattern!=null){
					log.info(logPrefix+"Received DTMF for participantID: "+endPointID+ " (" + endPoint.getName() + ")" +
							", Pattern: "+pattern);
				}
			}
		}
	}

	private void registerConferenceListeners()
	{
		try{
			if(activeConference != null)
			{
				log.info(logPrefix + "registerAllConferenceListener......");

				activeConference.addDTMFListener(dtmfListener);  				
				activeConference.addEndPointListener(conferenceEndPointListener);
				activeConference.addPropertyChangeListener(conferencePropertyListener);
				activeConference.addEndPointPropertyChangeListener(endPointPropertyListener);  			
			}
		}
		catch(Exception e)
		{
			log.error(logPrefix + "registerAllConferenceListener", e);
		}
	}

	private void unregisterConferenceListeners()
	{
		try{
			if(activeConference != null)
			{
				log.info(logPrefix + "unregisterAllConferenceListener......");
				activeConference.removeDTMFListener(dtmfListener);
				activeConference.removeEndPointListener(conferenceEndPointListener);
				activeConference.removePropertyChangeListener(conferencePropertyListener);
				activeConference.removeEndPointPropertyChangeListener(endPointPropertyListener);
			}
		}
		catch(Exception e)
		{
			log.error(logPrefix + "unregisterAllConferenceListener", e);
		}
	}

	private boolean makeCall(Conference conference, String destination) {
		if (destination != null && !destination.equals("")) {
			EndPoint eP=null;
			try {
				changeLectureMode(conference, true);
				OperationFactory factory = conference.getConnection().getOperationFactory();
				MakeCall makecallop = factory.newMakeCall();
				Set<String> codes = conference.getConfereeCodes();
				Iterator<String> itr = codes.iterator();
				String confereeCode = null;
				if(itr.hasNext())
					confereeCode = itr.next();
				makecallop.setDestination(destination);
				if(confereeCode != null)
					makecallop.setEndPointProperty(EndPoint.NAME, confereeCode);
				/*Set<State> epState=new HashSet<State>();
  				epState.add(States.MUTED);
  				makecallop.setEndPointStates(epState);*/
				conference.execute(makecallop);

				long timeElapsed=0;
				while(!makecallop.isDone() && timeElapsed<10000)
				{
					try{
						Thread.sleep(100);
						timeElapsed=timeElapsed+100;
					}
					catch(Exception e)
					{

					}
				}

				if(makecallop.isDone())
				{
					eP = (EndPoint) makecallop.get();
					if(eP != null)
					{
						Set<State> states=eP.getState();
						if(!states.contains(States.ESTABLISHING))
						{							
							//muteEndPoint(eP, false);								
							log.info(logPrefix + " success to make call to "  + destination);
							return true;
						}
						else
						{						
							log.info(logPrefix + " fail to make call to " + destination + ", disconnect the endpoint" );
							disconnectEndPoint(eP);
						}
					}
				}
				else
				{

					if(activeConference != null)
					{  					
						for(EndPoint endPoint : activeConference.getEndPoints())
						{
							String displayNumber=endPoint.getFromDisplay();
							if(displayNumber != null)
							{
								displayNumber=displayNumber.trim();
								String tmp[]=displayNumber.split("[ ,]");
								if(tmp != null && tmp.length >0)
								{
									String calledNumber = tmp[0].trim();
									if(destination.endsWith(calledNumber) || calledNumber.endsWith(destination))
									{
										Set states = endPoint.getState();
										if(states != null && states.contains(States.ESTABLISHING))
										{
											disconnectEndPoint(endPoint);
										}
									}
								}
							}
						}  					
					}	  			
				}
			} catch (Exception e) {
				log.error("Fail to make call to " + destination, e);
			}
			finally{
				changeLectureMode(conference, false);	
				if(eP != null)
				{
					muteEndPoint(eP, false);
				}
			}
		}
		return false;
	}

	private boolean isPhoneNumberInConference(String phoneNumber, Conference conf)
	{
		if(conf != null && phoneNumber != null && phoneNumber.length()>0)
		{
			for(EndPoint endPoint : conf.getEndPoints())
			{
				if(endPoint.isActive())
				{
					String endPointNumber = getPhoneNumberFromEndpoint(endPoint);

					if(phoneNumber.endsWith(endPointNumber))
						return true;

					if(endPointNumber.endsWith(phoneNumber))
						return true;
				}
			}
		}
		return false;
	}  


	private String getPhoneNumberFromEndpoint(EndPoint endPoint)
	{
		String phoneNumber=endPoint.getFromNumber();
		if(phoneNumber != null)
		{
			phoneNumber = phoneNumber.trim();
			if(phoneNumber.length()>0)
				return phoneNumber;
		}
		String displayNum= endPoint.getFromDisplay();
		if(displayNum != null && displayNum.length()>0)
		{
			String tmp[]=displayNum.split("[ ,]");
			phoneNumber=tmp[0].trim();
			if(phoneNumber.length()>=0)
				return phoneNumber;
		}

		return null;
	}

	private void disconnectEndPoint(EndPoint endPoint)
	{
		if(endPoint != null)
		{ 
			Integer endPointId=(Integer)endPoint.getPropertyValue(BridgeConstant.ENDPOINT_ID);
			try {
				OperationFactory factory = endPoint.getConnection().getOperationFactory();
				Disconnect disconnectEndpoint = factory.newDisconnect();
				endPoint.execute(disconnectEndpoint);

				if(disconnectEndpoint.get(20000, TimeUnit.MILLISECONDS))
					log.info(logPrefix + "success to disconnect EndPoint " + endPoint.getName() + ", endpoint Id=" + endPointId);
				else
					log.warn(logPrefix + "Fail to disconnect EndPoint " + endPoint.getName() + ", endpoint Id=" + endPointId);


			} catch (Exception e) {
				if(endPoint.isActive())
					log.error(logPrefix + "Fail to disconnect EndPoint " + endPoint.getName() + ", endpoint Id=" + endPointId, e);
				else
					log.warn(logPrefix + "Fail to disconnect EndPoint " + endPoint.getName() + " since it is not connected, endpoint Id=" + endPointId, e);
			}
		}
	} 

	public void closeConference(Conference conf)
	{
		if(conf != null)
		{
			Integer conferenceId=(Integer)conf.getPropertyValue(BridgeConstant.CONF_ID);
			boolean isActive = conf.isActive();  		
			try {
				OperationFactory factory = conf.getConnection().getOperationFactory();
				Disconnect disconnectConf = factory.newDisconnect();
				conf.execute(disconnectConf);

				if(disconnectConf.get(20000, TimeUnit.MILLISECONDS))
				{
					log.info(logPrefix + "Close conference " + conf.getName() + ", conferenceId=" + conferenceId);
				}
				else
				{
					if(!conf.getName().equals("HOLD"))
					{
						if(isActive)
							log.error(logPrefix + "Fail to close conference " + conf.getName() + ", conferenceId=" + conferenceId);
						else
							log.warn(logPrefix + "Fail to close conference " + conf.getName() + ", conferenceId=" + conferenceId);
					}
				}		

			} catch (Exception e) {
				isActive = conf.isActive();
				if(isActive)
					log.error(logPrefix+"Fail to close conference " + conf.getName() + ", conferenceId=" + conferenceId, e);
				else
					log.warn(logPrefix + "Fail to close conference " + conf.getName() + ", conferenceId=" + conferenceId, e);
			}
		}
	}

	private void dumpProperties(String header, ConferencingObject<? > conferencingObject) {  	
		StringBuffer tmp = new StringBuffer();
		int i=0;
		for (String properyName : conferencingObject.getProperties()) 
		{
			if(i%5==0)
			{
				if(tmp.length()>0)
					log.info(logPrefix + header + tmp.toString());
				tmp=new StringBuffer();
			}
			tmp.append(" " + properyName + "=" +
					conferencingObject.getPropertyValue(properyName)); 
			i++;
		}
	}

	private boolean muteEndPoint(EndPoint endPoint, boolean mute)
	{
		String str="";
		if(mute)
			str="Mute";
		else
			str="Unmute";
		if(endPoint != null)
		{
			Integer endPointId=(Integer)endPoint.getPropertyValue(BridgeConstant.ENDPOINT_ID);

			log.info(logPrefix + "Try to " + str + " endPoint " + endPointId + ", its states=" + endPoint.getState());

			try {
				/**
				 * check endpoint state if it is in MESSAGE_PLAYBACK, 
				 * if so, wait message play finished
				 * In ACPI mute operation come with MESSAGE_PLAYBACK, if endpoint is in
				 * MESSAGE_PLAYBACK, the operation will fail, therefore, CFBrSrv should wait
				 * The MESSAGE_PLAYBACK could not be disabled.
				 * 3/22/2013 Wendy
				 */
				Set states = endPoint.getState();
				long timeElapsed=0;
				long sTime=System.currentTimeMillis();
				while(states != null && states.contains(States.MESSAGE_PLAYBACK) 
						&& timeElapsed<2000)
				{
					try{
						Thread.sleep(100);
					}
					catch(Exception e)
					{

					}
					timeElapsed=timeElapsed + 100;
					states = endPoint.getState();
				}
				long endTime=System.currentTimeMillis();
				OperationFactory factory = endPoint.getConnection().getOperationFactory();
				Mute muteEndpoint = factory.newMute();
				states = endPoint.getState();

				if(states != null && states.contains(States.MESSAGE_PLAYBACK))
				{
					log.warn(logPrefix + "the endpoint, endpoint Id=" + endPointId 
							+ ", is not ready to be " + str + "d, its state=" + states + ", waitTime=" + (endTime-sTime) + " ms.");
				}
				else
				{
					log.info(logPrefix + "the endpoint, endpoint Id=" + endPointId 
							+ ", is ready to be " + str + "d, its state=" + states + ", waitTime=" + (endTime-sTime) + " ms.");
				}
				if(mute)
				{
					if(states==null || !states.contains(States.MUTED))
					{
						muteEndpoint.on();
						endPoint.execute(muteEndpoint);
						log.info(logPrefix + "Succeed to mute endpoint, endpoint Id=" + endPointId);
					}
					else
					{
						log.info(logPrefix + " the endPoint " + endPointId + " is already muted");
					}
				}
				else
				{
					if(states ==null || states.contains(States.MUTED) 
							|| states.contains(States.OBSERVE_ONLY))
					{
						muteEndpoint.off();
						endPoint.executeAs(endPoint.getAuthentication(), muteEndpoint);
						log.info(logPrefix + "Succeed to unmute endpoint, endpoint Id=" + endPointId);
					}
					else
					{
						log.info(logPrefix + " the endPoint " + endPointId + " is already un-muted");
					}
				}
				return true;  			  			
			} catch (Exception e) {
				Set states = endPoint.getState();
				if(endPoint.isActive())
				{
					if(mute)
					{
						if(states != null && states.contains(States.MUTED)|| states.contains(States.OBSERVE_ONLY))
						{
							log.warn(logPrefix + "The endpoint is already muted, endpoint Id=" + endPointId, e);
						}
						else
						{
							log.error(logPrefix + "Fail to " + str + " EndPoint, endpoint Id=" + endPointId, e);
						}
					}
					else
					{
						if(states != null && !states.contains(States.MUTED) && !states.contains(States.OBSERVE_ONLY))
						{
							log.warn(logPrefix + "the endpoint is already unmuted, endpoint Id=" + endPointId, e);
						}
						else
						{
							log.warn( logPrefix + "Fail to " + str + " EndPoint, endpoint Id=" + endPointId, e);
						}
					}  
				}
				else
				{
					log.warn(logPrefix + "Fail to " + str + " EndPoint since it is not active, endpoint Id=" + endPointId, e);
				}
			}
		}
		return false;
	}

	private boolean isEndpointRadio(EndPoint endPoint)
	{
		String number=getPhoneNumberFromEndpoint(endPoint);
		if(number != null)
		{
			if(number.endsWith(radioData.getRadioGWExt()) || radioData.getRadioGWExt().endsWith(number))
				return true;
		}
		return false;
	}
	private boolean setEndPointProperty(EndPoint endPoint, String key, Object value)
	{
		if(key != null && value != null)
		{
			HashMap<String, Object> map=new HashMap<String, Object>();
			map.put(key, value);
			return setEndPointProperties(endPoint, map);
		}
		return false;
	}

	private boolean setEndPointProperties( EndPoint endPoint, Map<String, Object>properties)
	{
		if(endPoint != null)
		{
			if(endPoint.isActive())
			{
				Integer endpointId=(Integer)endPoint.getPropertyValue(BridgeConstant.ENDPOINT_ID);
				long sTime = System.currentTimeMillis();
				try {  			
					OperationFactory factory = endPoint.getConnection().getOperationFactory();
					ModifyDetails modifyDetail = factory.newModifyDetails();
					modifyDetail.setPropertyValues(properties);
					endPoint.execute(modifyDetail); 
					long timeElapsed=0;
					boolean isDone=modifyDetail.isDone();
					boolean isActive=endPoint.isActive();

					while(timeElapsed < 10000 && !isDone && isActive)
					{
						try{
							Thread.sleep(100);
						}catch(Exception e)
						{

						}
						timeElapsed=timeElapsed + 100;
						isDone=modifyDetail.isDone();
						isActive=endPoint.isActive();
					}

					isActive=endPoint.isActive();
					if(!isActive)
					{
						log.warn(logPrefix + "Fail to setEndpointProperties " + properties
								+ "since the endPoint is not active, endPointId=" + endpointId +", time used=" + (System.currentTimeMillis() - sTime) + "ms");
						return false;
					}

					if(!isDone)
					{
						log.warn(logPrefix + "Timeout ! Fail to setEndpointProperties " + properties
								+ ", endpointId=" + endpointId +", time used=" + (System.currentTimeMillis() - sTime) + "ms");
						return false;
					}

					Set result=modifyDetail.get();
					log.debug(logPrefix + "Succeed to setEndpointProperties " + properties + ", result=" + result
							+ ", time used=" + (System.currentTimeMillis() - sTime) + "ms");
					return true;

				} catch (Exception e) { 
					boolean isActive=endPoint.isActive();
					Set states=endPoint.getState();  			
					if(isActive)
					{
						String stateStr="";
						if(states != null)
							stateStr=states.toString();
						log.error(logPrefix + "Fail to setEndPointProperties " + properties
								+ ",endpointId=" + endpointId +", state=" + stateStr +", time used=" + (System.currentTimeMillis() - sTime) + "ms", e);
					}
					else
						log.warn(logPrefix + "Fail to setEndPointProperties " + properties
								+ ", endpointId=" + endpointId +", time used=" + (System.currentTimeMillis() - sTime) + "ms", e);
				}
			}
			else
				log.warn(logPrefix + "Fail to setEndPointProperties " + properties + " since the endpoint is not active");
		}
		return false;
	}
	
	private boolean setConferenceProperty(Conference conference, String key, Object value)
	  {
	  	if(key != null && value != null)
	  	{  		
	  		Map<String, Object> property = new HashMap<String, Object>();
				property.put(key, value);
				return setConferenceProperties(conference, property);  		
	  	}
	  	return false;
	  }
	private boolean setConferenceProperties( Conference confefrence, Map<String, Object>properties)
	{
		if(confefrence != null)
		{
			if(confefrence.isActive())
			{
				Integer confId=(Integer)confefrence.getPropertyValue(BridgeConstant.CONF_ID);
				long sTime = System.currentTimeMillis();
				try {  			
					OperationFactory factory = confefrence.getConnection().getOperationFactory();
					ModifyDetails modifyDetail = factory.newModifyDetails();
					modifyDetail.setPropertyValues(properties);
					confefrence.execute(modifyDetail); 
					long timeElapsed=0;
					boolean isDone=modifyDetail.isDone();
					boolean isActive=confefrence.isActive();

					while(timeElapsed < 10000 && !isDone && isActive)
					{
						try{
							Thread.sleep(100);
						}catch(Exception e)
						{

						}
						timeElapsed=timeElapsed + 100;
						isDone=modifyDetail.isDone();
						isActive=confefrence.isActive();
					}

					isActive=confefrence.isActive();
					if(!isActive)
					{
						log.warn(logPrefix + "Fail to setEndpointProperties " + properties
								+ "since the endPoint is not active, confId=" + confId +", time used=" + (System.currentTimeMillis() - sTime) + "ms");
						return false;
					}

					if(!isDone)
					{
						log.warn(logPrefix + "Timeout ! Fail to setEndpointProperties " + properties
								+ ", endpointId=" + confId +", time used=" + (System.currentTimeMillis() - sTime) + "ms");
						return false;
					}

					Set result=modifyDetail.get();
					log.debug(logPrefix + "Succeed to setEndpointProperties " + properties + ", result=" + result
							+ ", time used=" + (System.currentTimeMillis() - sTime) + "ms");
					return true;

				} catch (Exception e) { 
					boolean isActive=confefrence.isActive();
					Set states=confefrence.getState();  			
					if(isActive)
					{
						String stateStr="";
						if(states != null)
							stateStr=states.toString();
						log.error(logPrefix + "Fail to setEndPointProperties " + properties
								+ ", confId=" + confId +", state=" + stateStr +", time used=" + (System.currentTimeMillis() - sTime) + "ms", e);
					}
					else
						log.warn(logPrefix + "Fail to setEndPointProperties " + properties
								+ ", endpointId=" + confId +", time used=" + (System.currentTimeMillis() - sTime) + "ms", e);
				}
			}
			else
				log.warn(logPrefix + "Fail to setConferenceProperties " + properties + " since the conference is not active");
		}
		return false;
	}
	
	private boolean changeLectureMode(Conference conference, boolean toActivate)
	  {
	  	if(conference != null)
	  	{
	  		String str="";
	  		if(toActivate)
	  		{
	  			str="activate lecture mode";
	  		}
	  		else
	  		{
	  			str="deactivate lecture mode";
	  		}
	  		try{
	  			log.info(logPrefix +"try to " + str + "......"); 			

	  			OperationFactory factory = conference.getConnection().getOperationFactory();
	  			LectureMode lectureMode = factory.newLectureMode();  			
	  			if(toActivate)
	  			{
	  				lectureMode.on();  				
	  			}
	  			else
	  			{
	  				lectureMode.off();  				
	  			}
	  			conference.execute(lectureMode);
	  			
	  			lectureMode.get(20000, TimeUnit.MILLISECONDS);
	  			log.info(logPrefix + str);  				  				
	  			
	  			
	  			return true;
	  		}catch(Exception e)
	  		{
	  			if(conference != null && conference.isActive())
	  				log.error(logPrefix +"changeLectureMode", e);
	  			else
	  				log.error(logPrefix +"changeLectureMode", e);
	  		}
	  	}
	  	return false;  	
	  }
}
