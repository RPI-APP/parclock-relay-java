package com.centeksoftware.parclock.javarelay.storage;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * The buffer of data as it comes in from LabView
 * 
 * @author Daniel Centore
 *
 */
public class DataBuffer
{
	/**
	 * The queue backend of the buffer
	 */
	private LinkedBlockingQueue<DataChunk> queue = new LinkedBlockingQueue<DataChunk>();
	
	/**
	 * What we should interrupt when new data comes in
	 */
	private Thread interruptMe = null;
	
	/**
	 * Adds a {@link DataChunk} to the queue
	 * 
	 * @param chunk The chunk to add
	 */
	public void addChunk(DataChunk chunk)
	{
		try
		{
			queue.put(chunk);
		} catch (InterruptedException e)
		{
			// Should never happen
			e.printStackTrace();
		}
		
		if (interruptMe != null)
			interruptMe.interrupt();
	}
	
	/**
	 * Sets the {@link Thread} that should be interrupted when new data arrives to the buffer
	 * 
	 * @param interruptMe The {@link Thread} to set it to
	 */
	public void setInterruptMe(Thread interruptMe)
	{
		this.interruptMe = interruptMe;
	}
	
	/**
	 * Returns true if the buffer contains no elements
	 * 
	 * @return Whether or not the buffer contains zero elements
	 */
	public boolean isEmpty()
	{
		return queue.isEmpty();
	}
	
	/**
	 * Gets the next {@link DataChunk} and removes it from the buffer
	 * 
	 * @return The next {@link DataChunk}
	 */
	public DataChunk poll()
	{
		return queue.poll();
	}
	
	/**
	 * Gets the next {@link DataChunk} without removing it from the buffer
	 * 
	 * @return The next {@link DataChunk}
	 */
	public DataChunk peek()
	{
		return queue.peek();
	}
	
	/**
	 * Returns the number of {@link DataChunk}s in the buffer
	 * 
	 * @return The number of {@link DataChunk}s in the buffer
	 */
	public int size()
	{
		return queue.size();
	}
}
