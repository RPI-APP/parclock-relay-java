package com.centeksoftware.parclock.javarelay.time;

import org.apache.commons.net.ntp.ClientTimeSource;
import org.apache.directory.server.ntp.time.ServerTimeSource;

/**
 * Handles keeping track of monotonically and linearly increasing time
 * 
 * @author Daniel Centore
 *
 */
public class MonotonicTime implements ClientTimeSource, ServerTimeSource
{
	private static final int MILLION = 1_000_000;
	
	private long milliNanoDifference;
	
	/**
	 * Starts the monotonic time counter at the current UTC time. NOTE: This does not mean that the
	 * time remains UTC. This will count leap seconds while UTC will ignore them.
	 */
	public MonotonicTime()
	{
		milliNanoDifference = System.currentTimeMillis() - (System.nanoTime() / MILLION);
		
		// milliNanoDifference += new Random().nextInt(50000);
	}
	
	/**
	 * Starts the monotonic time counter at a start value
	 * 
	 * @param start Starting time (in ms)
	 */
	public MonotonicTime(long start)
	{
		milliNanoDifference = start - (System.nanoTime() / MILLION);
	}
	
	@Override
	public long currentTimeMillis()
	{
		long linearMs = System.nanoTime() / MILLION + milliNanoDifference;
		return linearMs;
	}
	
}
