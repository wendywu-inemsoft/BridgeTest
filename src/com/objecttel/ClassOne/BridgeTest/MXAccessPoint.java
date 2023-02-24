package com.objecttel.ClassOne.BridgeTest;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.avaya.conferencing.api.acp.control.*;
import com.avaya.conferencing.api.acp.control.events.*;
import com.avaya.conferencing.api.acp.control.operations.*;

import org.apache.log4j.Logger;


/** 
 * <p>Copyright: Copyright ObjectTel (c) 2003-2013</p>
 * <p>Company: Objecttel</p>  
 * @author Wendy Wu
 * @version 1.0
 */
public class MXAccessPoint implements ConferenceListener{
    private static Logger log = Logger.getLogger(MXAccessPoint.class);

    private BridgeTest bridgeTest;
    private String connectionUrl;
    private String userName ;
    private char[] password;
    private Connection connection=null;
    private boolean stop;
    private Object synObj;
   
    private HashMap<String, RadioAccessPoint> radioAccessMap=null;
   
    private boolean getBridgeTimeEnabled = false;
    private long getBridgeTimeInterval = 180000;
    private Timer getBridgeTimeTimer=null;
    private GetBridgeTimeTask getTimeTask=null;
   
    private int counter=0;
    private ArrayList<String> monitorList=new ArrayList<String>();


    public MXAccessPoint(BridgeTest parent, String ip, String user, char[] iPassword) throws Exception
    {
    	bridgeTest=parent;
    	connectionUrl = "smodapi://" + ip;
    	userName = user;
    	password = iPassword;
    	
    	stop = false;
    	
    	synObj = new Object();    	
    	
    	getBridgeTimeEnabled = Boolean.parseBoolean(BridgeTest.appProperties.getProperty("getBridgeTimeEnabled", "false"));
    	getBridgeTimeInterval = Long.parseLong(BridgeTest.appProperties.getProperty("getBridgeTimeInterval", "180000"));
    	radioAccessMap=new HashMap<String, RadioAccessPoint>();
    	
    	if(getBridgeTimeEnabled)
    	{
    		getBridgeTimeTimer = new Timer();
    		getTimeTask = new GetBridgeTimeTask();
    		getBridgeTimeTimer.scheduleAtFixedRate(getTimeTask, 0, getBridgeTimeInterval);
    	}
    	
    	buildRadioAccessPoint();
    	getMXConnection();
    	connection.addConferenceListener(this);
    }

    private void getMXConnection() throws Exception {

        connection = ConferencingProviders.getConnection(
                connectionUrl, userName, password);

        log.info("Connected at " + connection.getName() +
                 ", there are currently "
                 + connection.getConferences().size()
                 + " conference(s) open");
    }

    private void getBridgeTime()
    {
        try{
            if(connection != null)
                log.info("*** The bridge time is " +
                     connection.execute(connection.getOperationFactory().
                                        newGetCurrentTime()).get() +
                     ", our time is " + new Date() + " ***");
        }
        catch(Exception e)
        {
            log.warn("Error when get bridge time " , e);
        }
    }
    
    private void buildRadioAccessPoint()
    {
    	radioAccessMap.clear();
    	HashMap<String, RadioMasterData>  monitoredRadioMap = bridgeTest.getMonitorRadios();
    	Set<String> radioConfNumbers=monitoredRadioMap.keySet();
    	Iterator<String> itr=radioConfNumbers.iterator();
    	while(itr.hasNext())
    	{
    		String confNum=itr.next();
    		RadioMasterData data=monitoredRadioMap.get(confNum);
    		RadioAccessPoint radioAccessPoint=new RadioAccessPoint(data);
    		radioAccessMap.put(confNum, radioAccessPoint);
    	}
    }

    public void enable() {
        CatchCurrentConference();
    }    

    private void CatchCurrentConference() {
    	for (Conference conference : connection.getConferences()) {
    		Integer conferenceId=(Integer)conference.getPropertyValue(BridgeConstant.CONF_ID);

    		log.info("Found conference in progress: "+conferenceId);
    		RadioAccessPoint rdAccessPoint=getRadioAccessPointFromConference(conference);
    		if(rdAccessPoint != null)
    		{
    			BridgeEvent bridgeEvent = new BridgeEvent(BridgeEvent.CONFERENCE_RECOVER, conference, System.currentTimeMillis());
    			rdAccessPoint.queueEntry(bridgeEvent);
    		}
    	}
    } 
   
    public ArrayList getRadioGWExtList(HashMap<String, RadioMasterData> monitoredRadioMap)
    {
        ArrayList radioGWExts = new ArrayList<String>();
        for(RadioMasterData rdData : monitoredRadioMap.values())
        {
            radioGWExts.add(rdData.getRadioGWExt());
        }
        return radioGWExts;
    }

    public RadioMasterData getRadioMasterDataFromGWExt(HashMap<String, RadioMasterData> datalist, String phoneNumber)
    {
        if(phoneNumber != null && !phoneNumber.trim().equals(""))
        {
            phoneNumber=phoneNumber.trim();
            Collection c = datalist.values();
            Iterator itr = c.iterator();
            while(itr.hasNext())
            {
                RadioMasterData rdData = (RadioMasterData) itr.next();
                if(phoneNumber.endsWith(rdData.getRadioGWExt()))
                    return rdData;
            }
        }
        return null;
    }

    private void dumpProperties(ConferencingObject<? >
                                        conferencingObject) {

        // determine the properties supported by a conferencing
        // object and display their current values
        StringBuffer tmp = new StringBuffer();
        for (String properyName : conferencingObject.getProperties()) {
            tmp.append(" " + properyName + "=" +
                       conferencingObject.getPropertyValue(properyName));

        }
        log.info("conferencingObject properties: " + tmp.toString());
    }

    private String isConferenceMonitored(Set confNumsMonitored, Conference conference)
    {
        Set conferenceCodes = conference.getConfereeCodes();
        Iterator itr = conferenceCodes.iterator();
        while(itr.hasNext())
        {
            String conferenceCode = (String) itr.next();
            if(confNumsMonitored.contains(conferenceCode))
                return conferenceCode;
        }

        log.info("Conference " + conferenceCodes + " is not monitored");
        return null;
    }
    
    public RadioAccessPoint getRadioAccessPointFromConference(Conference conference)
    {
    	RadioAccessPoint radioAccess=null;
    	boolean dbContainLDN=false;
    	Set<String> confCodes=conference.getConfereeCodes();
    	Integer conferenceID=(Integer)conference.getPropertyValue(BridgeConstant.CONF_ID);
    	if(confCodes != null)
    	{
    		boolean emptyCodes=true;
    		Iterator<String> itr=confCodes.iterator();
    		while(itr.hasNext())
    		{
    			String confCode = itr.next();
    			if(confCode != null)
    			{
    				confCode = confCode.trim();
    				if(confCode.length()>0)
    					emptyCodes=false;

    				radioAccess=radioAccessMap.get(confCode);
    				if(radioAccess != null)
    					return radioAccess;
    			}
    		}
    	}
    	return radioAccess;
    }

    private boolean isRadioMonitored(ArrayList monitoredGWExts, String phoneNumber)
    {
        for(int i=0; i<monitoredGWExts.size(); i++)
        {
            String rdGWExt = (String)monitoredGWExts.get(i);
            if(phoneNumber.endsWith(rdGWExt))
                return true;
        }
        log.info("Phone Number " + phoneNumber + " is not monitored");
        return false;
    }

    private boolean isRadioMonitored(RadioMasterData rdData, String phoneNumber)
    {
        if(rdData != null && phoneNumber != null)
        {
            return phoneNumber.endsWith(rdData.getRadioGWExt());
        }

        log.info("Phone Number " + phoneNumber + " is not monitored");
        return false;
    }

    public void logout()
    {
        log.info("Disconnect MX connection ...");
        stop = true;
        synchronized (synObj) {
            synObj.notify();
        }

        if(connection != null)
        {
            connection.close();
            log.info(" MX disconnected");
        }

        if(getBridgeTimeTimer != null)
            getBridgeTimeTimer.cancel();

        if(getTimeTask != null)
            getTimeTask.cancel();

    }

    public void childAdded(ChildEvent<Connection, Conference> evt) {
    	log.info("Receive Conference added event, seqNum=" + evt.getSequenceNumber());
    	Conference conf = evt.getChild();
    	dumpProperties(conf);
    	RadioAccessPoint accessPoint=getRadioAccessPointFromConference(conf);
    	if(accessPoint != null)
    	{
    		BridgeEvent bridgeEvt = new BridgeEvent(BridgeEvent.CONFERENCE_START, evt, System.currentTimeMillis());
    		accessPoint.queueEntry(bridgeEvt);
    	}
    }

   public void childRemoved(ChildEvent<Connection,Conference> evt)
   {
	   log.info("Receive Conference removed event, seqNum=" + evt.getSequenceNumber());
	   Conference conf = evt.getChild();
	   dumpProperties(conf);
	   RadioAccessPoint accessPoint=getRadioAccessPointFromConference(conf);
	   if(accessPoint != null)
	   {
		   BridgeEvent bridgeEvt = new BridgeEvent(BridgeEvent.CONFERENCE_ENDED, evt, System.currentTimeMillis());
		   accessPoint.queueEntry(bridgeEvt);
	   }
   }
  
   
   private boolean addToLockList(String phoneNumber) {
       boolean go=false;
       synchronized (monitorList) {
           if (!monitorList.contains(phoneNumber)) {
               monitorList.add(phoneNumber);
               go = true;
           }
       }
       return go;
   }

   private void removeFromLockList(String phoneNumber) {
       synchronized (monitorList) {
           monitorList.remove(phoneNumber);
       }
   }

   private boolean isMeasuring(String phoneNumber) {
       synchronized (monitorList) {
           return monitorList.contains(phoneNumber);
       }
   }   

   private class GetBridgeTimeTask extends TimerTask {
       public void run()
       {
           getBridgeTime();
       }
   }
}
