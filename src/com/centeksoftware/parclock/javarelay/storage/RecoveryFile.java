package com.centeksoftware.parclock.javarelay.storage;

import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import com.centeksoftware.parclock.javarelay.MainFrame;

/**
 * Class representing the data recovery file
 * 
 * @author Daniel Centore
 *
 */
public class RecoveryFile
{
	/**
	 * Used at the start if a record to signify that it has not yet been sent
	 */
	private static final char BEGIN_UNSENT = 'U';
	/**
	 * Used at the start if a record to signify that it has been sent
	 */
	private static final char BEGIN_SENT = 'S';
	/**
	 * Used at the end of all records to verify that the record is complete
	 */
	private static final char END_ALL = 'T';
	/**
	 * The location of the recovery file
	 */
	private static final String RECOVERY_FILE = "./recovery.txt";
	/**
	 * The start position of the previously read {@link DataChunk} in bytes
	 */
	private volatile long previousPosition = -1;
	/**
	 * The start position of the next {@link DataChunk} to be read in bytes
	 */
	private volatile long currentPosition = 0;
	/**
	 * The {@link RandomAccessFile} for reading and writing the recovery file
	 */
	private RandomAccessFile recoveryFile;
	/**
	 * The amount of time (in ms) between file flushes
	 */
	private static final long BETWEEN_FLUSHES = 2000;
	/**
	 * The last (unix) time the file was flushed
	 */
	private volatile long lastFlush = 0;
	/**
	 * True if definitely empty; false if we're UNSURE
	 */
	private volatile boolean isEmpty = false;
	/**
	 * This is populated by the peek function, and remains the "next" {@link DataChunk} until it is
	 * explicitly cleared using markPreviousChunkSent()
	 */
	private volatile DataChunk peekNext = null;
	/**
	 * The number of elements added to the file in this session. The actual number might be higher
	 * (but never lower)
	 */
	private volatile int count = 0;
	
	private MainFrame mf;
	
	/**
	 * Accesses the recovery file
	 */
	public RecoveryFile(MainFrame mf)
	{
		this.mf = mf;
		try
		{
			recoveryFile = new RandomAccessFile(RECOVERY_FILE, "rw");
		} catch (FileNotFoundException e)
		{
			System.out.println("ERROR: Recovery file not found");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	/**
	 * Checks if the recovery file is empty
	 * 
	 * @return True if it contains no elements; false otherwise
	 */
	public synchronized boolean isEmpty()
	{
		return (isEmpty || (peekNextChunk() == null));
	}
	
	private void flushIfNeeded()
	{
		if (lastFlush == 0)
		{
			lastFlush = System.currentTimeMillis();
			return;
		}
		
		if (System.currentTimeMillis() - lastFlush > BETWEEN_FLUSHES)
		{
			try
			{
				recoveryFile.getFD().sync();
			} catch (IOException e)
			{
				mf.println("RF", "Something is wrong with the recovery file: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		lastFlush = System.currentTimeMillis();
	}
	
	/**
	 * Appends a new {@link DataChunk} to the end of the recovery file
	 * 
	 * @param dc The chunk to add
	 */
	public synchronized void appendDataChunk(DataChunk dc)
	{
		try
		{
			recoveryFile.seek(recoveryFile.length());
			
			StringBuilder sb = new StringBuilder();
			sb.append(BEGIN_UNSENT);
			sb.append(DataChunk.SEP_COMMA);
			sb.append(dc.toBackup());
			sb.append(DataChunk.SEP_COMMA);
			sb.append(END_ALL);
			sb.append(DataChunk.SEP_ENDL);
			
			recoveryFile.writeBytes(sb.toString());
			
			++count;
			
			isEmpty = false;
		} catch (IOException e)
		{
			e.printStackTrace();
			mf.println("RF", "Something is wrong with the recovery file: " + e.getMessage());
		}
		
		flushIfNeeded();
		
	}
	
	/**
	 * Mark the previously peeked {@link DataChunk} as sent in recovery file
	 */
	public synchronized void markPreviousChunkSent()
	{
		if (count > 0)
			--count;
		
		try
		{
			// System.out.println("Marking read from position " + previousPosition);
			recoveryFile.seek(previousPosition);
			
			recoveryFile.writeByte(BEGIN_SENT);
			
			flushIfNeeded();
			
		} catch (IOException e)
		{
			e.printStackTrace();
			mf.println("RF", "Something is wrong with the recovery file: " + e.getMessage());
		}
		
		peekNext = null;
		
		// If the file is now empty, clear it!
		if (peekNextChunk() == null)
			emptyFlatFile();
	}
	
	private void emptyFlatFile()
	{
		try
		{
			recoveryFile.setLength(0);
			
			previousPosition = -1;
			currentPosition = 0;
		} catch (IOException e)
		{
			e.printStackTrace();
			mf.println("RF", "Something is wrong with the recovery file: " + e.getMessage());
		}
	}
	
	/**
	 * Returns the number of elements added to the file in this session. The actual total number
	 * might be higher (but never lower)
	 * 
	 * @return The number of elements added to the file in this session
	 */
	public int getCount()
	{
		return count;
	}
	
	/**
	 * Takes a look at the next {@link DataChunk} in the recovery file, without actually altering
	 * our current position in the file
	 * 
	 * @return The next {@link DataChunk} in the recovery file
	 */
	public synchronized DataChunk peekNextChunk()
	{
		if (peekNext != null)
			return peekNext;
		
		DataChunk dc = readNextChunk();
		
		peekNext = dc;
		
		return dc;
	}
	
	private DataChunk readNextChunk()
	{
		while (true)
		{
			try
			{
				recoveryFile.seek(currentPosition);
				
				// Keep reading the file until we reach an unsent item
				char first = (char) recoveryFile.readByte();
				
				while (first == BEGIN_SENT)
				{
					char c;
					do
					{
						c = (char) recoveryFile.readByte();
					} while (c != DataChunk.SEP_ENDL);
					
					currentPosition = recoveryFile.getFilePointer();
					
					first = (char) recoveryFile.readByte();
				}
				
				String recoveryData = first + "";
				
				char c = (char) recoveryFile.readByte();
				while (c != DataChunk.SEP_ENDL)
				{
					recoveryData += c;
					
					c = (char) recoveryFile.readByte();
				}
				
				previousPosition = currentPosition;
				currentPosition = recoveryFile.getFilePointer();
				
				// System.out.println("Setting prev to: " + previousPosition);
				// System.out.println("Setting current to: " + currentPosition);
				
				String[] parts = recoveryData.split(DataChunk.SEP_COMMA + "");
				if (parts.length != 5)
				{
					// Something was wrong with the recovery data
					// This is expected if a line became corrupted.
					
					mf.println("RF",
							"A line in the recovery file was corrupted and is being ignored.");
					
					continue;
				}
				
				DataChunk dc = new DataChunk(parts[1], parts[2], Long.parseLong(parts[3]));
				return dc;
				
			} catch (EOFException e)
			{
				// Reached the end of the file; no more chunks.
				// This is normal.
				
				isEmpty = true;
				return null;
			} catch (IOException e)
			{
				mf.println("RF", "Something is wrong with the recovery file: " + e.getMessage());
				e.printStackTrace();
				
				return null;
			}
		}
	}
}
