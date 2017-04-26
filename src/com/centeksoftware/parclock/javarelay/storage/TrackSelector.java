package com.centeksoftware.parclock.javarelay.storage;

import com.centeksoftware.parclock.javarelay.MainFrame;
import com.centeksoftware.parclock.javarelay.comms.CommunicationHandler;

/**
 * The main class for deciding where data should come from and where it should go
 * 
 * @author Daniel Centore
 *
 */
public class TrackSelector
{
	private DataBuffer buff;
	private MainFrame mf;
	private RecoveryFile ff;
	private CommunicationHandler ch;
	
	/**
	 * The number of items successfully transmitted to the server
	 */
	private int transmitted = 0;
	
	public TrackSelector(DataBuffer buff, MainFrame mf, CommunicationHandler ch)
	{
		this.buff = buff;
		this.mf = mf;
		this.ff = new RecoveryFile(mf);
		this.ch = ch;
	}
	
	/**
	 * Starts sending data where it should be sent
	 * 
	 * @param mc
	 */
	public void trackSelection()
	{
		// Run the main server
		new Thread()
		{
			public void run()
			{
				TrackSelector.this.run();
			}
		}.start();
		
	}
	
	private void run()
	{
		buff.setInterruptMe(Thread.currentThread());
		
		// Wait until there's something in the buffer
		// if there isn't already something in the flat file
		if (ff.isEmpty())
		{
			mf.setRecoveryFile(0, true);
			
			while (buff.isEmpty())
			{
				try
				{
					// Gets interrupted when we add to buffer
					Thread.sleep(10000);
				} catch (InterruptedException e)
				{
				}
			}
		}
		
		while (true)
		{
			mf.setRamBuffer(buff.size());
			
			boolean ffEmpty = ff.isEmpty();
			boolean connected = ch.isConnected();
			
			boolean popBuffToMs = false;
			boolean popFlatToMs = false;
			boolean dumpBuffToFF = false;
			
			// If connected and flat file is empty, pop buffer to main server
			// If connected and flat file has content, pop flat file to main server
			// If not connected, dump buffer to flat files
			
			if (connected)
			{
				if (ffEmpty)
					popBuffToMs = true;
				else
				{
					popFlatToMs = true;
					dumpBuffToFF = true;
				}
			}
			else
				dumpBuffToFF = true;
			
			mf.setActions(popBuffToMs, popFlatToMs, dumpBuffToFF);
			
			if (ffEmpty)
				mf.setRecoveryFile(0, true);
			else
				mf.setRecoveryFile(ff.getCount(), false);
			
			if (popBuffToMs && !buff.isEmpty())
			{
				DataChunk dc = buff.peek();
				boolean success = ch.sendChunk(dc);
				
				// Data chunk successfully sent
				if (success)
				{
					buff.poll(); // Remove the chunk from our queue
					mf.setTransmitted(++transmitted);
				}
			}
			if (popFlatToMs)
			{
				DataChunk dc = ff.peekNextChunk();
				
				boolean success = ch.sendChunk(dc);
				
				// Data chunk successfully sent
				if (success)
				{
					ff.markPreviousChunkSent(); // Mark it as having been sent
					mf.setTransmitted(++transmitted);
				}
			}
			if (dumpBuffToFF)
			{
				while (!buff.isEmpty())
					ff.appendDataChunk(buff.poll());
			}
			
			mf.setRamBuffer(buff.size());
			
			// Wait until there's something in the buffer
			if (ffEmpty)
			{
				while (buff.isEmpty())
				{
					try
					{
						// Gets interrupted when we add to buffer
						Thread.sleep(10000);
					} catch (InterruptedException e)
					{
					}
				}
			}
			
		}
	}
	
}
