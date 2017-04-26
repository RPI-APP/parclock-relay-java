package com.centekeng.software.xenon.relay;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import com.centekeng.software.xenon.relay.comms.CommunicationHandler;
import com.centekeng.software.xenon.relay.storage.DataBuffer;
import com.centekeng.software.xenon.relay.storage.TrackSelector;

/**
 * 
 * @author Daniel Centore
 *
 */
public class Main
{
	// Only change this if the protocol changes. It makes it so other versions can't connect to it.
	public static final int PROTOCOL_VERSION = 12;
	
	public static void main(String args[]) throws FileNotFoundException
	{
		System.out.println("... Begin");
		
		String server = null;
		int ntpPortServer = -1; // For the Server NTP Server
		int ntpPortClient = -1; // For the Client NTP server
		int relayPort = -1; // The port for data comms
		int labviewPort = -1; // The port LabView connects to
		
		// == Load info from the config file == //
		Scanner scan = new Scanner(new File("./config.txt"));
		
		while (scan.hasNext())
		{
			String s = scan.nextLine();
			if (s.startsWith("#"))
				continue;
			
			String p[] = s.split(":");
			
			String param = p[0];
			String value = "";
			for (int i = 1; i < p.length; ++i)
				value += p[i] + (i == p.length - 1 ? "" : ":");
			value = value.trim();
			
			if (param.equalsIgnoreCase("server"))
				server = value;
			else if (param.equalsIgnoreCase("ntpPortServer"))
				ntpPortServer = Integer.parseInt(value);
			else if (param.equalsIgnoreCase("ntpPortClient"))
				ntpPortClient = Integer.parseInt(value);
			else if (param.equalsIgnoreCase("relayPort"))
				relayPort = Integer.parseInt(value);
			else if (param.equalsIgnoreCase("labviewPort"))
				labviewPort = Integer.parseInt(value);
		}
		
		scan.close();
		
		// Create the main frame
		MainFrame mf = new MainFrame();
		mf.setVisible(true);
		
		// == Begin the communication procedure ==
		
		// Create storage container
		DataBuffer dataBuffer = new DataBuffer();
		
		// Create communications object
		CommunicationHandler ch = new CommunicationHandler(mf, server, relayPort, ntpPortClient,
				ntpPortServer, labviewPort, dataBuffer);
		
		// Begin choosing where data goes
		TrackSelector trackSelector = new TrackSelector(dataBuffer, mf, ch);
		trackSelector.trackSelection();
		
		// Begin main communications loop
		ch.communicationsLoop();
	}
	
	/**
	 * Just Thread.sleep without the checked warning
	 * 
	 * @param millis the length of time to sleep in milliseconds
	 */
	public static void sleep(long millis)
	{
		try
		{
			Thread.sleep(millis);
		} catch (InterruptedException e)
		{
		}
	}
}
