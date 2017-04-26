package com.centekeng.software.xenon.relay.time;

import java.awt.Color;
import java.net.InetAddress;

import org.apache.commons.net.ntp.ClientTimeSource;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.directory.server.ntp.time.ServerTimeSource;
import org.terracotta.statistics.Time.TimeSource;

import com.centekeng.software.xenon.relay.Main;
import com.centekeng.software.xenon.relay.MainFrame;
import com.centekeng.software.xenon.relay.comms.CommunicationHandler;
import com.centekeng.software.xenon.relay.tools.ArrayTools;

/**
 * Handles communicating with the time server
 * 
 * @author Daniel Centore
 *
 */
public class TimeHandler implements ClientTimeSource, ServerTimeSource
{
	/**
	 * How many samples are required
	 */
	private static final int TIMES = 5;
	
	/**
	 * How many ms between taking samples
	 */
	private static final int UPDATE_TIME = 5000;
	
	/**
	 * How many ms before NTP times out
	 */
	private static final int NTP_TIMEOUT = 10_000;
	
	/**
	 * The current offset between this computer and the main server
	 */
	private volatile long currentOffset = Integer.MAX_VALUE;
	
	/**
	 * The last offset received from the time server
	 */
	private volatile long lastOffset;
	
	/**
	 * An array of the last TIMES offsets received
	 */
	private volatile long offsets[] = new long[TIMES];
	
	/**
	 * Our current position in the offsets array
	 */
	private volatile int offsetId = 0;
	
	/**
	 * Indicates whether or not we think we are connected right now
	 */
	private volatile boolean connected = false;
	
	/**
	 * Server URL
	 */
	private String server;
	
	/**
	 * The port the NTP server will be running on on the server
	 */
	private int ntpPortServer;
	
	/**
	 * The main time thread
	 */
	private Thread syncThread = null;
	
	/**
	 * The thread for updating the {@link MainFrame}
	 */
	private Thread indicatorMaintain = null;
	
	/**
	 * Instructs running threads that we want them to quit
	 */
	private volatile boolean pleaseStop = false;
	
	/**
	 * The source of the time that we use to compare with the main server
	 */
	private ClientTimeSource timeSource;
	
	private MainFrame mf;
	private CommunicationHandler ch;
	
	/**
	 * Creates a new {@link TimeHandler}
	 * 
	 * @param server url of the server
	 * @param ntpPortServer port that the NTP server on the server will use
	 * @param mainFrame
	 * @param ch
	 */
	public TimeHandler(String server, int ntpPortServer, MainFrame mainFrame,
			CommunicationHandler ch)
	{
		this.server = server;
		this.ntpPortServer = ntpPortServer;
		this.mf = mainFrame;
		this.ch = ch;
		
		timeSource = new MonotonicTime();
	}
	
	/**
	 * Stops any threads currently running in here. Blocks until they actually quit.
	 */
	public void stop()
	{
		pleaseStop = true;
		
		if (syncThread != null)
		{
			syncThread.interrupt();
			
			while (syncThread.isAlive())
				Main.sleep(10);
		}
		
		if (indicatorMaintain != null)
		{
			indicatorMaintain.interrupt();
			
			while (indicatorMaintain.isAlive())
				Main.sleep(10);
		}
		
		pleaseStop = false;
	}
	
	/**
	 * Starts the time sync thread and indicator maintenance thread
	 */
	public void start()
	{
		// Run the main thread
		syncThread = new Thread()
		{
			public void run()
			{
				TimeHandler.this.syncThread();
			}
		};
		syncThread.start();
		
		// Run the indicator maintenance thread
		indicatorMaintain = new Thread()
		{
			public void run()
			{
				TimeHandler.this.maintainIndicator();
			}
		};
		indicatorMaintain.start();
	}
	
	/**
	 * Signals whether or not we are connected
	 * 
	 * @param connected
	 */
	private void setConnected(boolean connected)
	{
		this.connected = connected;
		if (this.connected)
			mf.setTimeserverConnection("Connected", Color.GREEN);
		else
			mf.setTimeserverConnection("Disconnected", Color.RED);
	}
	
	/**
	 * Indicates whether or not we think we are connected right now
	 * 
	 * @return
	 */
	public boolean isConnected()
	{
		return connected;
	}
	
	/**
	 * Runs the main synchronization thread
	 */
	private void syncThread()
	{
		while (!pleaseStop)
		{
			if (!ch.isConnected())
			{
				setConnected(false);
				return;
			}
			
			boolean success = attemptNewOffset();
			updateCurrentOffset();
			
			if (success)
				mf.println("TH", "Got an offset: " + lastOffset);
			else
				mf.println("TH", "Failed to get an offset for some reason");
			
			if (Thread.interrupted())
				continue;
			
			try
			{
				Thread.sleep(UPDATE_TIME);
			} catch (InterruptedException e)
			{
			}
		}
	}
	
	/**
	 * Runs the indicator update thread
	 */
	private void maintainIndicator()
	{
		while (!pleaseStop)
		{
			mf.setTime(getCorrectedTime());
			mf.setOffset(currentOffset);
			
			try
			{
				Thread.sleep(10);
			} catch (InterruptedException e)
			{
			}
		}
	}
	
	/**
	 * Attempts to obtain a new offset and add it to our offsets list
	 * 
	 * @return True if succeeded; False otherwise
	 */
	private boolean attemptNewOffset()
	{
		long off = getOffset(server, ntpPortServer, timeSource);
		if (off != Integer.MAX_VALUE)
		{
			offsets[offsetId] = lastOffset = off;
			++offsetId;
			if (offsetId >= offsets.length)
				offsetId = 0;
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Updates the current offset to the median of our offsets array
	 */
	private void updateCurrentOffset()
	{
		currentOffset = ArrayTools.findMedian(offsets);
	}
	
	/**
	 * Requests a new offset from an NTP server
	 * 
	 * @param server The server URL
	 * @param port The port of the NTP server
	 * @param timeSource The local {@link TimeSource} to compare against
	 * @return Either the offset in milliseconds (which should be added to
	 *         {@link TimeSource#currentTimeMillis()} to get the remote time) or
	 *         {@link Integer#MAX_VALUE} if the sync failed.
	 */
	private long getOffset(String server, int port, ClientTimeSource timeSource)
	{
		NTPUDPClient client = new NTPUDPClient(timeSource);
		
		// We want to timeout if a response takes longer than 10 seconds
		client.setDefaultTimeout(NTP_TIMEOUT);
		try
		{
			client.open();
			
			InetAddress hostAddr = InetAddress.getByName(server);
			TimeInfo info = client.getTime(hostAddr, port);
			
			mf.println("TH", "Server time:" + info.getReturnTime());
			
			info.computeDetails();
			long offset = info.getOffset().longValue();
			
			client.close();
			
			setConnected(true);
			return offset;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		client.close();
		
		setConnected(false);
		
		return Integer.MAX_VALUE;
	}
	
	/**
	 * Gets (system time at the time we started running the program) + (elapsed seconds since start
	 * of program) + (difference between server and client time) This should mitigate the effects of
	 * leap seconds
	 * 
	 * @return
	 */
	public long getCorrectedTime()
	{
		return timeSource.currentTimeMillis() + currentOffset;
	}
	
	@Override
	public long currentTimeMillis()
	{
		return getCorrectedTime();
	}
	
	/**
	 * Performs a full time sync to the remote server
	 * 
	 * @return True if success; False otherwise
	 */
	public boolean fullSync()
	{
		int successes = 0;
		while (successes < offsets.length)
		{
			boolean succ = attemptNewOffset();
			
			successes += (succ ? 1 : 0);
			if (succ)
				mf.println("TH", "Got an offset: " + lastOffset);
			else
				return false;
		}
		
		updateCurrentOffset();
		
		return true;
	}
	
}
