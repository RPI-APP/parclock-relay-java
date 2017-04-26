package com.centeksoftware.parclock.javarelay.comms;

import java.awt.Color;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Scanner;

import com.centeksoftware.parclock.javarelay.MainFrame;
import com.centeksoftware.parclock.javarelay.storage.DataBuffer;
import com.centeksoftware.parclock.javarelay.storage.DataChunk;
import com.centeksoftware.parclock.javarelay.time.TimeHandler;

/**
 * The main server for communicating with LabView
 * 
 * @author Daniel Centore
 *
 */
public class LabviewServer
{
	/**
	 * Character for separating the UUID from the data
	 */
	private static final String UUID_DELIM = Character.toString((char) 0x1C);
	
	/**
	 * Character for separating {@link DataChunk}s
	 */
	private static final String FIELD_DELIM = Character.toString((char) 0x1E);
	
	private int labviewPort;
	private DataBuffer buffer;
	private TimeHandler th;
	
	private MainFrame mf;
	
	private boolean connected = false;
	
	private HashMap<String, Long> lastTime = new HashMap<String, Long>();
	
	public LabviewServer(int labviewPort, DataBuffer dataBuffer, TimeHandler th, MainFrame mf)
	{
		this.labviewPort = labviewPort;
		this.buffer = dataBuffer;
		this.th = th;
		this.mf = mf;
		
		mf.setLabviewPort(labviewPort);
	}
	
	private void setConnected(boolean c)
	{
		this.connected = c;
		
		if (connected)
			mf.setLabviewConnection("Connected", Color.green);
		else
			mf.setLabviewConnection("Disconnected", Color.red);
	}
	
	/**
	 * Begins the communication with LabView
	 */
	public void start()
	{
		new Thread()
		{
			public void run()
			{
				LabviewServer.this.server();
			}
		}.start();
	}
	
	private void server()
	{
		while (true)
		{
			setConnected(false);
			mf.println("LV", "Trying to connect to LabView...");
			
			ServerSocket serverSocket;
			Socket sock;
			try
			{
				serverSocket = new ServerSocket(labviewPort);
				sock = serverSocket.accept();
			} catch (Exception e)
			{
				e.printStackTrace();
				
				mf.println("LV", "Failed to connect. Trying again...");
				
				try
				{
					Thread.sleep(5000);
				} catch (InterruptedException e1)
				{
					e1.printStackTrace();
				}
				
				continue;
			}
			
			setConnected(true);
			mf.println("LV", "Successfully connected to LabView.");
			
			try
			{
				Scanner scan = new Scanner(sock.getInputStream());
				
				scan.useDelimiter(FIELD_DELIM);
				
				while (scan.hasNext())
				{
					String item = scan.next();
					long time = th.getCorrectedTime();
					
					// Turn weird newlines into Unix ones
					item = item.replaceAll("\r\n", "\n");
					item = item.replaceAll("\n\r", "\n");
					item = item.replaceAll("\r", "\n");
					
					String[] split = item.split(UUID_DELIM);
					String data = split[0];
					String guid = split[1];
					
					String guidSplit[] = guid.split("\n");
					guid = guidSplit[guidSplit.length - 1].trim();
					
					if (lastTime.containsKey(guid) && lastTime.get(guid) >= time)
					{
						mf.println("LV", "Time was not monotonically increasing; skipping value. "
								+ guid);
						continue;
					}
					lastTime.put(guid, time);
					
					buffer.addChunk(new DataChunk(guid, data, time));
					
				}
				scan.close();
				
				sock.close();
				serverSocket.close();
			} catch (Exception e)
			{
				e.printStackTrace();
				mf.println("LV", "Lost connection to LabView.");
			}
		}
	}
	
}
