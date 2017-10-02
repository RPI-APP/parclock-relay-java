package com.centeksoftware.parclock.javarelay;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.SwingUtilities;

/**
 * @author Daniel Centore
 * 
 */
public class MainFrame extends javax.swing.JFrame
{
	
	private static final long serialVersionUID = 1L;
	
	protected static final int MAXIMUM_LENGTH = 10000;
	
	/**
	 * Creates new form MainFrame
	 */
	public MainFrame()
	{
		initComponents();
		
		new Thread()
		{
			@Override public void run()
			{
				while (true)
				{
					Runnable runnable = new Runnable()
					{
						@Override public void run()
						{
							mainserverOffset.setText(offsetText);
							mainserverTime.setText(timeText);
							mainserverConnection.setText(mainServerText);
							mainserverConnection.setForeground(mainServerColor);
							mainserverURL.setText(mainServerUrl);
							
							timeserverConnection.setText(timeserverText);
							timeserverConnection.setForeground(timeserverColor);
							
							labviewConnection.setText(labviewText);
							labviewConnection.setForeground(labviewColor);
							
							labviewPort.setText(lvPort);
							justRam.setText(jRam);
							
							justFF.setText(recovSize);
							
							justTransmitted.setText(transmitted);
							
							action1.setText(act1);
							action2.setText(act2);
							
							// Without this it gets all laggy sometimes
							MainFrame.this.repaint();
						}
					};
					SwingUtilities.invokeLater(runnable);
					
					Main.sleep(10);
				}
			}
		}.start();
	}
	
	public synchronized void println(final String source, final String text)
	{
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date now = new Date();
		String time = sdfDate.format(now);
		
		try
		{
			bw.write(time + "[" + source + "]: " + text + "\n");
			bw.flush();
			
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Runnable runnable = new Runnable()
		{
			@Override public void run()
			{
				textArea.append("[" + source + "]: " + text + "\n");
				
				String text = textArea.getText();
				if (text.length() > MAXIMUM_LENGTH)
					textArea.setText(text.substring(text.length() - MAXIMUM_LENGTH, text.length()));
				
				if (!lockScroll.isSelected())
					textArea.setCaretPosition(textArea.getDocument().getLength());
			}
		};
		SwingUtilities.invokeLater(runnable);
		
	}
	
	String offsetText = "-";
	
	public void setOffset(final long offset)
	{
		offsetText = offset + " ms";
	}
	
	String timeText = "-";
	
	public void setTime(final long time)
	{
		long tenths = time / 100;
		long secs = time / 1000;
		tenths -= (secs * 10);
		
		timeText = secs + "." + tenths;
	}
	
	String mainServerText = "Disconnected";
	Color mainServerColor = Color.RED;
	
	public void setMainserverConnection(final String text, final Color color)
	{
		mainServerText = text;
		mainServerColor = color;
	}
	
	String mainServerUrl = "-";
	
	public void setMainserverUrl(final String text)
	{
		mainServerUrl = text;
		
	}
	
	String timeserverText = "Disconnected";
	Color timeserverColor = Color.RED;
	
	public void setTimeserverConnection(final String text, final Color color)
	{
		timeserverText = text;
		timeserverColor = color;
	}
	
	String labviewText = "0";
	Color labviewColor = Color.RED;
	
	public void setLabviewConnection(final String text, final Color color)
	{
		if ("Connected".equals(text))
		{
			labviewText = Integer.toString(Integer.parseInt(labviewText) + 1);
			if (1 == Integer.parseInt(labviewText))
			{
				labviewColor = color;
			}
		} else
		{
			labviewText = Integer.toString(Integer.parseInt(labviewText) - 1);
			if (0 == Integer.parseInt(labviewText))
			{
				labviewColor = color;
			}
		}
	}
	
	String lvPort = "-";
	
	public void setLabviewPort(final int port)
	{
		lvPort = port + "";
	}
	
	String jRam = "0 Items";
	
	public void setRamBuffer(final int size)
	{
		jRam = size + "";
		
	}
	
	String recovSize = "-";
	
	public void setRecoveryFile(final int size, boolean empty)
	{
		if (empty)
			recovSize = "Empty";
		else
			recovSize = size + "+";
	}
	
	String transmitted = "0 Items";
	
	public void setTransmitted(final int size)
	{
		transmitted = size + " Items";
	}
	
	String act1 = "-";
	String act2 = "-";
	
	public void setActions(final boolean popBuffToMs, final boolean popFlatToMs, final boolean dumpBuffToFF)
	{
		if (popBuffToMs && !popFlatToMs && !dumpBuffToFF)
		{
			act1 = "RAM Buffer    ==> Main Server";
			act2 = "-";
		} else if (!popBuffToMs && popFlatToMs && dumpBuffToFF)
		{
			act1 = "RAM Buffer    ==> Recovery File";
			act2 = "Recovery File ==> Main Server";
		} else if (!popBuffToMs && !popFlatToMs && dumpBuffToFF)
		{
			act1 = "RAM Buffer    ==> Recovery File";
			act2 = "-";
		} else
		{
			act1 = act2 = "This should never happen";
		}
	}
	
	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated
	 * by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
	// <editor-fold defaultstate="collapsed" desc="Generated
	// Code">//GEN-BEGIN:initComponents
	private void initComponents()
	{
		
		jLabel7 = new javax.swing.JLabel();
		jLabel1 = new javax.swing.JLabel();
		mainserverConnection = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();
		jLabel4 = new javax.swing.JLabel();
		labviewConnection = new javax.swing.JLabel();
		jLabel6 = new javax.swing.JLabel();
		jScrollPane1 = new javax.swing.JScrollPane();
		textArea = new javax.swing.JTextArea();
		jLabel8 = new javax.swing.JLabel();
		lockScroll = new javax.swing.JCheckBox();
		mainserverURL = new javax.swing.JLabel();
		mainserverTime = new javax.swing.JLabel();
		mainserverOffset = new javax.swing.JLabel();
		jLabel12 = new javax.swing.JLabel();
		labviewPort = new javax.swing.JLabel();
		jLabel14 = new javax.swing.JLabel();
		jLabel15 = new javax.swing.JLabel();
		jLabel16 = new javax.swing.JLabel();
		justRam = new javax.swing.JLabel();
		jLabel18 = new javax.swing.JLabel();
		justFF = new javax.swing.JLabel();
		jLabel2 = new javax.swing.JLabel();
		timeserverConnection = new javax.swing.JLabel();
		jLabel5 = new javax.swing.JLabel();
		justTransmitted = new javax.swing.JLabel();
		jLabel10 = new javax.swing.JLabel();
		action1 = new javax.swing.JLabel();
		action2 = new javax.swing.JLabel();
		
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd_HH;mm;ss");// dd/MM/yyyy
		Date now = new Date();
		String timestamp = sdfDate.format(now);
		timestamp = timestamp.replace(" ", "_");
		
		filename = "C:\\Users\\Ian\\Documents\\log_" + timestamp + ".txt"; // replace "C:\\Users\\Ian\\Documents\\" with actual file location
		log = new File(filename);
		try
		{
			log.createNewFile();
			fw = new FileWriter(log);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
		bw = new BufferedWriter(fw);
		
		jLabel7.setText("jLabel7");
		
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("Xenon Labview Relay");
		setBackground(new java.awt.Color(75, 75, 75));
		setForeground(new java.awt.Color(66, 66, 66));
		
		jLabel1.setText("Main Server:");
		
		mainserverConnection.setForeground(java.awt.Color.red);
		mainserverConnection.setText("Disconnected");
		
		jLabel3.setText("Time:");
		
		jLabel4.setText("Connected Sources");
		
		labviewConnection.setForeground(java.awt.Color.red);
		labviewConnection.setText("0");
		
		jLabel6.setText("URL:");
		
		textArea.setEditable(false);
		textArea.setColumns(20);
		textArea.setRows(5);
		jScrollPane1.setViewportView(textArea);
		
		jLabel8.setText("Offset:");
		
		lockScroll.setText("Lock Scrolling");
		
		mainserverURL.setText("-");
		
		mainserverTime.setText("-");
		
		mainserverOffset.setText("-");
		
		jLabel12.setText("Port:");
		
		labviewPort.setText("-");
		
		jLabel14.setText("Data:");
		
		jLabel16.setText("RAM Buffer:");
		
		justRam.setText("0 Items");
		
		jLabel18.setText("Recovery File:");
		
		justFF.setText("-");
		
		jLabel2.setText("Time Server:");
		
		timeserverConnection.setForeground(java.awt.Color.red);
		timeserverConnection.setText("Disconnected");
		
		jLabel5.setText("Transmitted:");
		
		justTransmitted.setText("0 Items");
		
		jLabel10.setText("Current Actions");
		
		action1.setText("-");
		
		action2.setText("-");
		
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(
				layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addGroup(layout.createSequentialGroup().addContainerGap()
								.addGroup(layout
										.createParallelGroup(
												javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(
												layout.createSequentialGroup()
														.addGroup(
																layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 537, Short.MAX_VALUE)
																		.addGroup(layout.createSequentialGroup().addGap(12, 12, 12).addGroup(layout
																				.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(action1)
																				.addGroup(layout
																						.createSequentialGroup().addComponent(action2)
																						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(lockScroll))))
																		.addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel14).addComponent(jLabel10)
																				.addGroup(layout.createSequentialGroup().addGap(12, 12, 12)
																						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel16).addComponent(jLabel3)
																								.addGroup(layout.createSequentialGroup().addComponent(jLabel18).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																										.addComponent(justFF))
																								.addGroup(layout.createSequentialGroup().addComponent(jLabel5).addGap(18, 18, 18).addComponent(justTransmitted)).addComponent(jLabel8))))
																				.addGap(0, 0, Short.MAX_VALUE))
																		.addGroup(layout.createSequentialGroup().addComponent(jLabel4).addGap(40, 40, 40)
																				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
																						.addGroup(layout.createSequentialGroup().addGap(12, 12, 12)
																								.addComponent(labviewPort, javax.swing.GroupLayout.PREFERRED_SIZE, 260, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(0, 0, Short.MAX_VALUE))
																						.addComponent(labviewConnection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
														.addContainerGap())
										.addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
												.addGroup(layout.createSequentialGroup().addGap(24, 24, 24).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel12).addComponent(jLabel6)))
												.addComponent(jLabel2).addComponent(jLabel1))
												.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(18, 18, 18).addGroup(layout
														.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
														.addComponent(timeserverConnection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
														.addGroup(layout.createSequentialGroup().addGap(12, 12, 12).addComponent(mainserverTime, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
														.addGroup(layout.createSequentialGroup().addComponent(jLabel15).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
																.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(justRam).addGap(0, 0, Short.MAX_VALUE))
																		.addGroup(layout.createSequentialGroup().addComponent(mainserverOffset, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
																				.addGap(56, 56, 56))))))
														.addGroup(layout.createSequentialGroup().addGap(18, 18, 18)
																.addGroup(layout
																		.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(12, 12, 12).addComponent(mainserverURL,
																				javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
																		.addComponent(mainserverConnection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))))));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(labviewConnection, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel12).addComponent(labviewPort))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel1).addComponent(mainserverConnection))
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(81, 81, 81).addComponent(jLabel15)).addGroup(layout.createSequentialGroup().addGap(3, 3, 3)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel6).addComponent(mainserverURL, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel2).addComponent(timeserverConnection))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel3).addComponent(mainserverTime, javax.swing.GroupLayout.Alignment.TRAILING))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel8).addComponent(mainserverOffset))))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(jLabel14).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel16).addComponent(justRam)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel18).addComponent(justFF)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel5).addComponent(justTransmitted)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel10)
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(action1).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(action2).addComponent(lockScroll)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
				.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE).addContainerGap()));
		
		pack();
	}// </editor-fold>//GEN-END:initComponents
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String args[])
	{
		/* Set the Nimbus look and feel */
		// <editor-fold defaultstate="collapsed" desc=" Look and feel setting code
		// (optional) ">
		/*
		 * If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel. For details see
		 * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
		 */
		try
		{
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
			{
				if ("Nimbus".equals(info.getName()))
				{
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex)
		{
			java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex)
		{
			java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex)
		{
			java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex)
		{
			java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
		}
		// </editor-fold>
		
		/* Create and display the form */
		java.awt.EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				new MainFrame().setVisible(true);
			}
		});
	}
	
	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.JLabel action1;
	private javax.swing.JLabel action2;
	private javax.swing.JLabel jLabel1;
	private javax.swing.JLabel jLabel10;
	private javax.swing.JLabel jLabel12;
	private javax.swing.JLabel jLabel14;
	private javax.swing.JLabel jLabel15;
	private javax.swing.JLabel jLabel16;
	private javax.swing.JLabel jLabel18;
	private javax.swing.JLabel jLabel2;
	private javax.swing.JLabel jLabel3;
	private javax.swing.JLabel jLabel4;
	private javax.swing.JLabel jLabel5;
	private javax.swing.JLabel jLabel6;
	private javax.swing.JLabel jLabel7;
	private javax.swing.JLabel jLabel8;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JLabel justFF;
	private javax.swing.JLabel justRam;
	private javax.swing.JLabel justTransmitted;
	private javax.swing.JLabel labviewConnection;
	private javax.swing.JLabel labviewPort;
	private javax.swing.JCheckBox lockScroll;
	private javax.swing.JLabel mainserverConnection;
	private javax.swing.JLabel mainserverOffset;
	private javax.swing.JLabel mainserverTime;
	private javax.swing.JLabel mainserverURL;
	private javax.swing.JTextArea textArea;
	private javax.swing.JLabel timeserverConnection;
	private String filename;
	private File log;
	private FileWriter fw;
	private BufferedWriter bw;
	
	// End of variables declaration//GEN-END:variables
}
