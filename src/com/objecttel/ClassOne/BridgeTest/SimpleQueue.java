package com.objecttel.ClassOne.BridgeTest;

import java.util.LinkedList;

import org.apache.log4j.Logger;

/** 
 * <p>Copyright: Copyright ObjectTel (c) 2003-2013</p>
 * <p>Company: Objecttel</p> 
 */
public class SimpleQueue implements Runnable
{
	private static Logger log = Logger.getLogger(SimpleQueue.class);
	protected String name; // optional name for queue
	protected LinkedList myQueue; // terminal control and event queue
	protected Thread thread; // empties queue, invokes callbacks
	protected boolean shutDown; // class disposal flag

	public SimpleQueue()
	{
		this.name="";
		this.myQueue=new LinkedList(); // allow differing types on queue
		this.thread=null; // thread for dequeue logic
		this.shutDown=false; // shutdown flag for thread
		// create thread for dequeue
		try
		{
			thread=new Thread(this,"Queue for "+name);
			thread.start();
		}
		catch(Exception e)
		{
			log.info("SimpleQueue: Creating event thread for "+name,e);
		}
		return;
	}

	public SimpleQueue(String name)
	{
		this.name=name; // name the queue
		this.myQueue=new LinkedList(); // allow differing types on queue
		this.thread=null; // thread for dequeue logic
		this.shutDown=false; // shutdown flag for thread
		// create thread for dequeue
		try
		{
			thread=new Thread(this,"Queue for "+name);
			thread.start();
		}
		catch(Exception e)
		{
			log.info("SimpleQueue: Creating event thread for "+name,e);
		}
		return;
	}

	public void run()
	{
		// until shutdown
		while(!shutDown)
		{
			// while the queue is not empty
			while(myQueue!=null&&myQueue.size()>0)
			{
				// remove entry from queue
				Object queueEntry;
				synchronized(myQueue)
				{
					queueEntry=myQueue.removeFirst();
				}
				if(!shutDown)
					processEntry(queueEntry);
			} // endwhile queue has another entry
			// wait for arrival of new queue entry
			if(myQueue!=null)
			{
				synchronized(this)
				{
					try
					{
						this.wait();
					}
					catch(Exception e)
					{
						System.err.println("queueEntry::run: exception while waiting "+e);
					}
				}
			}
			else
				shutDown=true;
		} // end until shutdown
		// indicate queue and this thread may be garbage collected
		myQueue=null;
		thread=null;
		return;
	}

	public void finalize()
	{
		// initiate shutdown of dequeue thread	    
		shutDown=true;
		synchronized(this)
		{
			this.notify();
		}
		// indicate members may be garbage collected
		name=null;
		myQueue=null;
		return;
	}

	public void queueEntry(Object refObject)
	{
		// if queue is not  shutdown
		if(!shutDown)
		{
			// add the object to the queue
			synchronized(myQueue)
			{
				myQueue.add(refObject);
			}
			// notify terminal control thread of new event
			synchronized(this)
			{
				this.notify();
			}
		}
		return;
	}

	public void processEntry(Object entry)
	{
		return;
	}
}
