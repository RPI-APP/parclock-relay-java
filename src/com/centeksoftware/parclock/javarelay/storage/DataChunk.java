package com.centekeng.software.xenon.relay.storage;

/**
 * Represents a single measurement
 * 
 * @author Daniel Centore
 *
 */
public class DataChunk
{
	/**
	 * The character to be used akin to a comma in CSV. It is a Group Separator here.
	 */
	public static final char SEP_COMMA = 0x1D;
	
	/**
	 * The character to be used akin to a newline in CSV. It is a Record Separator here.
	 */
	public static final char SEP_ENDL = 0x1E;
	
	/**
	 * The ID of the field this data belongs to
	 */
	private String uuid;
	
	/**
	 * The actual chunk of data
	 */
	private String data;
	
	/**
	 * The adjusted timestamp (ie offset is already applied here)
	 */
	private long timestampMs;
	
	/**
	 * Creates a new {@link DataChunk}
	 * 
	 * @param uuid The universally unique identifier (UUID) of the data field this measurement
	 *            belongs to
	 * @param data The data in String format
	 * @param timestampMs The custom timestamp in ms
	 */
	public DataChunk(String uuid, String data, long timestampMs)
	{
		this.uuid = uuid;
		this.data = data;
		this.timestampMs = timestampMs;
	}
	
	/**
	 * Returns all the data in a String format for backup purposes
	 * 
	 * @return The backup String
	 */
	public String toBackup()
	{
		return uuid + SEP_COMMA + data + SEP_COMMA + timestampMs;
	}

	public String getUuid()
	{
		return uuid;
	}

	public String getData()
	{
		return data;
	}

	public long getTimestampMs()
	{
		return timestampMs;
	}
	
}
