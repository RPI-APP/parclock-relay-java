package com.centekeng.software.xenon.relay.comms;

import java.awt.Color;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

import com.centekeng.software.xenon.relay.Main;
import com.centekeng.software.xenon.relay.MainFrame;
import com.centekeng.software.xenon.relay.storage.DataBuffer;
import com.centekeng.software.xenon.relay.storage.DataChunk;
import com.centekeng.software.xenon.relay.storage.TrackSelector;
import com.centekeng.software.xenon.relay.time.TimeHandler;
import com.centekeng.software.xenon.relay.time.TimeServer;

/**
 * Handles big picture communications including handshaking, organizing time syncs, and starting
 * data transmissions
 * 
 * @author Daniel Centore
 *
 */
public class CommunicationHandler
{
	/**
	 * How long to wait before trying to reconnect to the server, by default.
	 */
	private static final long DEFAULT_RESTART_WAIT = 1_000;
	
	/**
	 * What gets sent between fields in a record
	 */
	private static final String FIELD_DELIM = Character.toString((char) 0x1E);
	
	private MainFrame mf;
	private String url;
	private int relayPort;
	private int ntpPortClient;
	private int ntpPortServer;
	private int labviewPort;
	private TimeHandler timeHandler = null;
	
	private Socket clientSocket;
	private DataOutputStream out;
	
	private DataBuffer dataBuffer;
	
	private boolean connected;
	
	private LabviewServer labView = null;
	private Scanner scan = null;
	
	public CommunicationHandler(MainFrame mf, String url, int relayPort, int ntpPortClient,
			int ntpPortServer, int labviewPort, DataBuffer dataBuffer)
	{
		this.mf = mf;
		this.url = url;
		this.relayPort = relayPort;
		this.labviewPort = labviewPort;
		this.dataBuffer = dataBuffer;
		this.ntpPortClient = ntpPortClient;
		this.ntpPortServer = ntpPortServer;
	}
	
	/**
	 * The main loop which handles big picture communications including handshaking and what we are
	 * instructed to do after that
	 */
	public void communicationsLoop()
	{
		long wait = 0;
		
		// Re-establish communications incessantly
		while (true)
		{
			// Close anything that might still be open
			try
			{
				if (scan != null)
					scan.close();
				
				if (out != null)
					out.close();
			} catch (Exception e)
			{
			}
			
			try
			{
				// Delay before next communication attempt (if requested)
				if (wait > 0)
				{
					mf.println("CH", "Waiting " + wait + " ms");
					Main.sleep(wait);
				}
				
				wait = DEFAULT_RESTART_WAIT;
				
				mf.println("CH", "Performing handshake");
				// Attempt initial communications with server
				long result = handshake();
				
				if (result < 0)
				{
					mf.println("CH", "Failure #" + result);
					
					// Unknown failure or bad protocol version
					continue;
				}
				else if (result == 0)
				{
					mf.println("CH", "Successful handshake!");
					
					// Successful handshake! Let's do stuff
					
					if (timeHandler == null)
					{
						timeHandler = new TimeHandler(url, ntpPortServer, mf, this);
						mf.println("CH", "Started time handler");
					}
					else
						mf.println("CH", "Reusing existing time handler");
					
					// Perform a full time synchronization
					if (!timeHandler.fullSync())
					{
						mf.println("CH", "Time synchronization failed; dying.");
						continue;
					}
					mf.println("CH", "Time synchronization succeeded.");
					
					// Begin normal operation
					normalOperation();
					
					// Wait for something to fail
					while (this.connected && timeHandler.isConnected())
						Main.sleep(10);
					
					labelServerConnected(false);
					
					continue;
				}
				else
				{
					// Instructed to wait before retrying connection
					wait = result;
					
					mf.println("CH", "Instructed to wait " + wait + " ms");
					
					continue;
				}
				
			} catch (Exception e)
			{
				e.printStackTrace();
				continue;
			}
		}
	}
	
	/**
	 * Performs initial communication handshake with the server
	 * 
	 * @return 0 for successful sequence, -1 for unknown failure, otherwise wait x ms
	 */
	private long handshake()
	{
		mf.println("CH", "Trying to connect to main server...");
		
		if (clientSocket != null)
		{
			try
			{
				out.close();
				clientSocket.close();
			} catch (IOException e)
			{
				mf.println("CH", "Failure while closing last clientSocket. Ignoring.");
				mf.println("CH", e.getMessage());
			}
		}
		
		boolean weHaveTime = (timeHandler != null);
		
		try
		{
			clientSocket = new Socket(url, relayPort);
			scan = new Scanner(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());
			
			mf.println("CH", "Successfully connected");
			
			// Tell the server what protocol version we are using
			out.writeBytes(Main.PROTOCOL_VERSION + "\n");
			
			// See if the server is cool with that
			String result = scan.nextLine();
			if (!result.equals("1"))
			{
				mf.println("CH", "Server refuses to accept client using protocol version ["
						+ Main.PROTOCOL_VERSION + "]");
				return -2;
			}
			
			mf.println("CH", "Server accepts protocol version " + Main.PROTOCOL_VERSION);
			
			// Tell the main server whether or not we have the time
			out.writeBytes((weHaveTime ? "1" : "0") + "\n");
			
			// See if the server wants the time
			// result = scan.nextLine();
			boolean serverNeedsTime = (scan.nextLine().equals("1"));
			
			mf.println("CH", "Server has time? [" + !serverNeedsTime + "]");
			mf.println("CH", "We have time? [" + weHaveTime + "]");
			
			if (serverNeedsTime)
			{
				long wait;
				
				if (weHaveTime)
				{
					// If the server needs the time and we have it, send it
					
					// Start time server
					mf.println("CH", "Starting time server on port " + ntpPortClient);
					
					TimeServer ts = new TimeServer(ntpPortClient, timeHandler);
					ts.start();
					
					// Tell server it may now connect to time server
					out.writeBytes("1\n");
					
					// Wait for instructions for when to try again
					wait = Long.parseLong(scan.nextLine());
					
					// Kill time server
					ts.stop();
				}
				else
				{
					// If the server needs the time and we don't have it, end the session and try
					// again later
					
					// Wait for instructions for when to try again
					wait = Long.parseLong(scan.nextLine());
				}
				
				// Quit and retry in wait ms
				
				wait = Math.max(wait, 1); // leq 0 is not indicative of a wait time
				
				return wait;
			}
			else
			{
				// If the server doesn't need the time, (re)sync our time and begin normal operation
				return 0;
			}
			
		} catch (Exception e)
		{
			e.printStackTrace();
			return -1;
		} finally
		{
			// scan.close();
		}
	}
	
	private void labelServerConnected(boolean c)
	{
		this.connected = c;
		
		if (connected)
			mf.setMainserverConnection("Connected", Color.green);
		else
			mf.setMainserverConnection("Disconnected", Color.red);
	}
	
	/**
	 * Starts (or restarts) the time server and starts the LabVIEW server (if necessary). Labels
	 * ourselves as connected so {@link TrackSelector} starts sending data to the interwebs.
	 */
	private void normalOperation()
	{
		labelServerConnected(true);
		
		mf.println("CH", "Restarting time handler");
		
		timeHandler.stop();
		timeHandler.start();
		
		// Begin communication with the LabView instance
		if (labView == null)
		{
			mf.println("CH", "Establishing LabVIEW communications");
			
			labView = new LabviewServer(labviewPort, dataBuffer, timeHandler, mf);
			labView.start();
		}
		else
			mf.println("CH", "Reusing existing LabVIEW comms");
	}
	
	/**
	 * Attempts to send a {@link DataChunk} to the server
	 * 
	 * @param dataChunk The {@link DataChunk} to send
	 * @return Whether or not the send succeeded
	 */
	public synchronized boolean sendChunk(DataChunk dataChunk)
	{
		try
		{
			writeLatest(dataChunk);
			
			return true;
		} catch (IOException e)
		{
			labelServerConnected(false);
			mf.println("MC",
					"Lost connection while trying to transmit chunk: " + dataChunk.toString());
			e.printStackTrace();
			
			return false;
		}
	}
	
	/**
	 * Attempt to send a {@link DataChunk} to the server
	 * 
	 * @param poll The {@link DataChunk} to send
	 * @throws IOException If sending failed
	 */
	private void writeLatest(DataChunk poll) throws IOException
	{
		// Not the best way to do this but whatever
		if (out == null)
			throw new IOException("Out is null");
		
		out.writeBytes("\n" + poll.getUuid() + FIELD_DELIM + poll.getData() + FIELD_DELIM
				+ poll.getTimestampMs() + '\n');
	}

	public boolean isConnected()
	{
		return connected;
	}
}
