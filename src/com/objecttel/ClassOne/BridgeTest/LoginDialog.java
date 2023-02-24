package com.objecttel.ClassOne.BridgeTest;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

/** 
 * <p>Copyright: Copyright ObjectTel (c) 2003-2013</p>
 * <p>Company: Objecttel</p> 
 */
public class LoginDialog extends JDialog implements ActionListener, KeyListener {

    private BridgeTest bridgeTest;
    private boolean isProcessing=false;
    public LoginDialog() {
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    JLabel jLabel1 = new JLabel();
    JLabel jLabel2 = new JLabel();
    JLabel jLabel3 = new JLabel();
    JTextField MXIpField = new JTextField();
    JTextField userNameField = new JTextField();
    JButton loginButton = new JButton();
    JButton cancelButton = new JButton();
    JPasswordField passwordField = new JPasswordField();
    JLabel mesgLabel = new JLabel();
    public LoginDialog(BridgeTest test) {
        super(test.getMainWindow(), true);
        this. bridgeTest = test;
        try {
            jbInit();
            configActionListener();

            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    windowCloseAttempt();
                }
            });

            Insets insets = getInsets();
            setSize(290 + insets.left + insets.right,
                    210 + insets.top + insets.bottom);

            setLocationRelativeTo(test.getMainWindow());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void configActionListener()
    {
        loginButton.addActionListener(this);
        cancelButton.addActionListener(this);
        MXIpField.addKeyListener (this);
        userNameField.addKeyListener(this);
        passwordField.addKeyListener(this);
    }

    private void setInitData() {
        String[] loginData = bridgeTest.getLoginData();
        if (loginData != null) {
            if (loginData[0] != null && !loginData[0].equals("")) {
                MXIpField.setText(loginData[0]);
            }
            if (loginData[1] != null && !loginData[1].equals("")) {
                userNameField.setText(loginData[1]);
                passwordField.grabFocus();
            }
        }
    }


    public void addNotify() {
        super.addNotify();
        setInitData();
    }


    private void jbInit() throws Exception {
        this.getContentPane().setLayout(null);
        this.setDefaultCloseOperation(javax.swing.WindowConstants.
                                      DO_NOTHING_ON_CLOSE);
        this.setResizable(true);
        this.setTitle("Meeting Exchange Login");
        jLabel1.setFont(new java.awt.Font("Dialog", Font.BOLD, 11));
        jLabel1.setText("MX IP:");
        jLabel1.setBounds(new Rectangle(25, 8, 70, 27));
        userNameField.setText("");
        userNameField.setBounds(new Rectangle(107, 40, 138, 23));
        loginButton.setBounds(new Rectangle(66, 126, 66, 27));
        loginButton.setToolTipText("");
        loginButton.setText("Login");

        cancelButton.setBounds(new Rectangle(155, 126, 81, 27));
        cancelButton.setMaximumSize(new Dimension(59, 23));
        cancelButton.setMinimumSize(new Dimension(59, 23));
        cancelButton.setPreferredSize(new Dimension(59, 23));
        cancelButton.setMnemonic('0');
        cancelButton.setText("Cancel");
        jLabel2.setFont(new java.awt.Font("Dialog", Font.BOLD, 11));
        jLabel3.setFont(new java.awt.Font("Dialog", Font.BOLD, 11));
        passwordField.setToolTipText("");
        passwordField.setText("");
        passwordField.setBounds(new Rectangle(107, 69, 138, 23));
        jLabel3.setToolTipText("");
        jLabel3.setText("Password:");
        jLabel3.setBounds(new Rectangle(25, 65, 70, 27));
        jLabel2.setText("User Name:");
        jLabel2.setBounds(new Rectangle(25, 36, 70, 27));
        MXIpField.setBounds(new Rectangle(107, 12, 138, 23));
        mesgLabel.setFont(new java.awt.Font("Dialog", Font.BOLD, 11));
        mesgLabel.setText("");
        mesgLabel.setBounds(new Rectangle(25, 99, 222, 24));
        this.getContentPane().add(jLabel3, null);
        this.getContentPane().add(jLabel2, null);
        this.getContentPane().add(jLabel1, null);
        this.getContentPane().add(cancelButton, null);
        this.getContentPane().add(userNameField, null);
        this.getContentPane().add(passwordField, null);
        this.getContentPane().add(MXIpField, null);
        this.getContentPane().add(mesgLabel);
        this.getContentPane().add(loginButton, null);

    }

    protected JRootPane createRootPane() {
        JRootPane rootPane = new JRootPane();
        KeyStroke stroke = KeyStroke.getKeyStroke("ESCAPE");
        Action actionListener = new AbstractAction() {
            public void actionPerformed(ActionEvent actionEvent) {
                windowCloseAttempt();
            }
        };
        InputMap inputMap = rootPane.getInputMap(JComponent.
                                                 WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(stroke, "ESCAPE");
        rootPane.getActionMap().put("ESCAPE", actionListener);

        return rootPane;
    }


    private void verifyValues() {

        // validation of name
        final String ip = MXIpField.getText().trim();

        if (ip.length() == 0) {
            updataMessage("IP Address Can Not Be Blank!", true);
            return;
        }
       

        final String userName = userNameField.getText().trim();
        if (userName.length() == 0) {
            updataMessage("User Name Can Not Be Blank!", true);
            return;

        }

        final char[] password = passwordField.getPassword();

        updataMessage("Login MX " + ip + "...", false);
        resetButtons(false);

        Thread localThread = new Thread()
        {
            public void run()
            {
                if (bridgeTest.loginMX(ip, userName, password))
                {
                    isProcessing = false;
                    dispose();
                }
                else {
                    updataMessage("Login MX " + ip + " Attempt Failed!", true);
                    resetButtons(true);
                }
            }
        };
        localThread.start();
        return;
    }

    private void windowCloseAttempt() {
           if(!isProcessing)
               dispose();
       }

       public void actionPerformed(ActionEvent ae) {
       Object src = ae.getSource();
       if (src.equals(loginButton)) {
           verifyValues();
       }
       else if(src.equals(cancelButton))
       {
          windowCloseAttempt();
       }
   }

   private void resetButtons(boolean enabled)
   {
       if(enabled)
           isProcessing = false;
       else
           isProcessing = true;
       MXIpField.setEnabled(enabled);
       userNameField.setEnabled(enabled);
       passwordField.setEnabled(enabled);
       loginButton.setEnabled(enabled);
       cancelButton.setEnabled(enabled);
   }

   private void updataMessage(String mesg, boolean isError) {

       if (mesg != null) {

           mesgLabel.setText(mesg);
           if (isError)
               mesgLabel.setForeground(Color.RED);
           else
               mesgLabel.setForeground(Color.BLUE);
       }

   }

   public void keyReleased(KeyEvent e) {}

   public void keyPressed(KeyEvent e) {
       int key = e.getKeyCode();
       if (key == KeyEvent.VK_ENTER) {
           verifyValues();
       }
   }
    public void keyTyped(KeyEvent e)
    {

    }


}



