package com.objecttel.ClassOne.BridgeTest;
import java.awt.*;
import java.awt.event.*;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.swing.*;

import org.apache.log4j.Logger;
import java.io.FileOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileNotFoundException;


/** 
 * <p>Copyright: Copyright ObjectTel (c) 2003-2013</p>
 * <p>Company: Objecttel</p> 
 */
public class BridgeTest implements ActionListener{
	private static Logger log = Logger.getLogger(BridgeTest.class);
	private JFrame frame;      
	private JLabel messageLabel;    

	private JMenuItem loginMenuItem;   
	private JMenuItem exitMenuItem;  

	private RadioArea radioArea;
	private MXAccessPoint mxAccessPoint=null;

	public static Properties appProperties;

	private String mxIPEntered="";
	private String usernameEntered="";

	public BridgeTest(JFrame iFrame) {
		frame = iFrame;
		init();
	}

	private void init()
	{
		try{
			log.info("Start the Bridge Test Application ...");
			appProperties = new Properties();
			FileInputStream isStream = new FileInputStream("etc\\ini.txt");
			appProperties.load(isStream);
			isStream.close();

			readMXLoginDataFromFlatFile();

		}catch(Exception e)
		{
			log.error("Load ini error", e);
		}
	}

	public JFrame getMainWindow()
	{
		return frame;
	}

	public JPanel createMainPanel()
	{
		JPanel mainPane = new JPanel(new BorderLayout());
		JTabbedPane tabbedPane = new JTabbedPane();       

		JPanel radioListPanel = createRadioListPanel();

		tabbedPane.addTab("EndPoint List", radioListPanel);
		//Add the tabbed pane to this panel.

		messageLabel = new JLabel();
		messageLabel.setFont(new java.awt.Font("Dialog", Font.BOLD, 11));
		setMessage("NO MX Connected", false);
		mainPane.add(tabbedPane, BorderLayout.CENTER);
		mainPane.add(messageLabel, BorderLayout.PAGE_END);

		//The following line enables to use scrolling tabs.
		tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		return mainPane;

	}

	public JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		loginMenuItem = new JMenuItem("Login");
		loginMenuItem.addActionListener(this);
		menu.add(loginMenuItem);

		menu.addSeparator();
		exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(this);
		menu.add(exitMenuItem);
		menuBar.add(menu);
		return menuBar;
	}

	/**
	 * Create the GUI and show it.  For thread safety,
	 * this method should be invoked from
	 * the event dispatch thread.
	 */
	private static void createAndShowGUI() {
		//Create and set up the window.
		JFrame frame = new JFrame("ClassOne Bridge Test Application");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

		final BridgeTest bridgeTest = new BridgeTest(frame);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent we) {
				bridgeTest.exit();
			}
		});

		JPanel mainPane = bridgeTest.createMainPanel();
		frame.setJMenuBar(bridgeTest.createMenuBar());
		mainPane.setOpaque(true);
		frame.setContentPane(mainPane);


		//Display the window.
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}


	private JPanel createRadioListPanel()
	{
		radioArea = new RadioArea(frame);
		return radioArea.getRadioAreaPanel();
	}

	public void actionPerformed(ActionEvent ae) {
		Object src = ae.getSource();
		if (src.equals(exitMenuItem)) {
			exit();
		}
		else if(src.equals(loginMenuItem))
		{
			log.info("loginMenuItem is clicked");
			if(loginMenuItem.getText().equals("Login"))
				popUpLoginDialog();
			else
				logoutCurrentMX();
		}

	}

	public void exit() {
		logoutCurrentMX();
		log.info("Bridge Test Application shut down ...");
		System.exit(0);
	}

	public static void main(String[] args) {
		//Schedule a job for the event dispatch thread:
		//creating and showing this application's GUI.
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				//Turn off metal's use of bold fonts
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				createAndShowGUI();
			}
		});
	}

	private void popUpLoginDialog()
	{
		if(mxAccessPoint == null)
		{
			LoginDialog loginDialog = new LoginDialog(this);

			loginDialog.setVisible(true);
		}
	}

	public boolean loginMX(String ip, String userName, char[] password) {

		try {
			mxAccessPoint = new MXAccessPoint(this,ip, userName, password);
			setMessage("Connected to MX " + ip, false);
			setLoginMenuText(false);
			mxAccessPoint.enable();
			if ((mxIPEntered == null || !mxIPEntered.equals(ip)) ||
					(usernameEntered == null || !usernameEntered.equals(userName))) {
				mxIPEntered = ip;
				usernameEntered=userName;
				writeMXLoginDataToFlatFile();
			}
			return true;
		} catch (Exception e) {
			mxAccessPoint = null;
			log.error("Fail to connect to MX " + ip, e);
		}
		catch(Throwable e)
		{
			mxAccessPoint = null;
			log.error("Fail to connect to MX " + ip, e);
		}


		return false;
	}

	private void logoutCurrentMX()
	{
		if(mxAccessPoint != null)
		{
			mxAccessPoint.logout();
			mxAccessPoint=null;
			setLoginMenuText(true);
			setMessage("NO MX Connected", false);
		}
	}

	private void setLoginMenuText(boolean login)
	{
		if(login)
			loginMenuItem.setText("Login");
		else
			loginMenuItem.setText("Logout");
	}

	private void setMessage(String mesg, boolean error)
	{
		messageLabel.setText("   " + mesg);
		if(error)
			messageLabel.setForeground(Color.red);
		else
			messageLabel.setForeground(Color.blue);
	}    

	public HashMap getMonitorRadios()
	{
		return radioArea.getMonitoredRadios();
	}

	public static String getCurrentTimeStr() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"yyyy/MM/dd HH:mm:ss.SSS");
		Date date = new Date(System.currentTimeMillis());
		return dateFormat.format(date);
	}

	public String[] getLoginData()
	{
		String[] data = new String[2];
		data[0]=mxIPEntered;
		data[1]=usernameEntered;
		return data;
	}
	private void readMXLoginDataFromFlatFile()
	{
		ObjectInputStream in = null;
		ArrayList monitoredRadios = null;
		try {
			FileInputStream istream = new FileInputStream(
					"etc\\Cache\\MXData");
			in = new ObjectInputStream(istream);
			mxIPEntered = (String) in.readObject();
			usernameEntered = (String) in.readObject();
		} catch (Exception e) {
			if (!(e instanceof FileNotFoundException))
				e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (Exception e) {
				}
			}
		}

	}

	private void writeMXLoginDataToFlatFile() {

		FileOutputStream ostream = null;
		ObjectOutputStream out = null;
		File file = new File("etc\\Cache");
		if (!file.exists()) {
			file.mkdirs();
		}

		try {
			ostream = new FileOutputStream("etc\\Cache\\MXData");
			out = new ObjectOutputStream(ostream);
			out.writeObject(mxIPEntered);
			out.writeObject(usernameEntered);
		} catch (Exception e) {
			e.printStackTrace();

		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception e) {
				}
			}
		}

	}

	private class MouseListenerAdapter implements MouseListener {
		public void mousePressed(MouseEvent me) {

		}

		public void mouseReleased(MouseEvent me) {

		}

		public void mouseExited(MouseEvent me) {
		}

		public void mouseEntered(MouseEvent me) {
		}

		public void mouseClicked(MouseEvent me) {}

	}
}
