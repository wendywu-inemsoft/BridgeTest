package com.objecttel.ClassOne.BridgeTest;
import javax.swing.JList;
import java.awt.Insets;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.util.Arrays;
import javax.swing.AbstractListModel;
import java.util.SortedSet;
import java.util.Iterator;
import java.util.Collection;
import java.util.TreeSet;
import java.awt.GridBagConstraints;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import java.awt.GridBagLayout;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.util.*;
import javax.swing.table.AbstractTableModel;
import java.awt.Dimension;
import java.awt.*;
import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;

/** 
 * <p>Copyright: Copyright ObjectTel (c) 2003-2013</p>
 * <p>Company: Objecttel</p> 
 */
public class RadioArea {
    private JFrame frame;
    private RadioListModel radioListModel;
    private JButton newButton;
    private JButton editButton;
    private JButton deleteButton;
    private JTable radioListTable;


    public RadioArea(JFrame iFrame) {
        frame = iFrame;
    }

    public JPanel getRadioAreaPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        radioListModel = new RadioListModel();
        radioListTable = new JTable(radioListModel);
        radioListTable.setPreferredScrollableViewportSize(new Dimension(800,
                150));
        radioListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        mainPanel.add(new JScrollPane(radioListTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        newButton = new JButton("New");
        newButton.addActionListener(new AddListener());
        editButton = new JButton("Edit");
        editButton.addActionListener(new EditListener());
        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new RemoveListener());
        buttonPanel.add(newButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        Collection rdList = readMonitoredRadiosFromFlatFile();
        if (rdList != null)
            radioListModel.setAllRowData(rdList);
        return mainPanel;
    }


    private class AddListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            EditDlg editDlg = new EditDlg(frame, "New Radio", null);
            editDlg.setVisible(true);
            if (editDlg.isAcceptableValues()) {
                RadioMasterData rdData = editDlg.getInputRadioData();
                radioListModel.addARowData(rdData);
                UpdateTableDisplay();
                writeMonitoredRadiosToFlatFile();

            }

        }
    }


    private void UpdateTableDisplay() {
        radioListTable.clearSelection();
        radioListTable.revalidate();
        radioListTable.repaint();
    }


    private class RemoveListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int row = radioListTable.getSelectedRow();
            if (row >= 0) {
                RadioMasterData rdData = (RadioMasterData) radioListModel.
                                         getRowData(row);
                Object[] options = new Object[] {"Yes", "No"};
                if (JOptionPane.showOptionDialog(frame,
                                                 "Are You Sure You Want Delete " +
                                                 rdData.getRadioName() + "?",
                                                 "Delete Radio",
                                                 JOptionPane.DEFAULT_OPTION,
                                                 JOptionPane.QUESTION_MESSAGE, null,
                                                 options, options[1]) == 0) {

                    radioListModel.removeARow(row);
                    UpdateTableDisplay();
                    writeMonitoredRadiosToFlatFile();
                }
            }
        }
    }


    private class EditListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            int row = radioListTable.getSelectedRow();
            if (row >= 0) {
                RadioMasterData editData = (RadioMasterData) radioListModel.
                                           getRowData(row);
                EditDlg editDlg = new EditDlg(frame, "Edit Radio", editData);
                editDlg.setVisible(true);
                if (editDlg.isAcceptableValues()) {
                    RadioMasterData rdData = editDlg.getInputRadioData();
                    radioListModel.updateARowData(editData, rdData);
                    UpdateTableDisplay();
                    writeMonitoredRadiosToFlatFile();

                }
            }
        }
    }


    private class RadioListModel extends AbstractTableModel {
        private String[] columnNames = {"EndPoint Name",
                                       "Conference Number",
                                       "EndPoint Extension"};
        private ArrayList data;

        public RadioListModel() {
            data = new ArrayList();
        }

        public void setAllRowData(Collection dataI) {
            data.clear();
            data.addAll(dataI);
            Collections.sort(data);
        }

        public void addARowData(Object aData) {
            data.add(aData);
            Collections.sort(data);
        }

        public void updateARowData(Object oldData, Object newData) {
            data.remove(oldData);
            data.add(newData);
            Collections.sort(data);
        }

        public void removeARow(int row) {
            data.remove(row);
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            RadioMasterData record = (RadioMasterData) data.get(row);
            return record.getColumnValue(col);
        }

        public Object getRowData(int row) {
            return data.get(row);
        }

        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            return false;
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return data.size();
        }

        public ArrayList getRadioList() {
            return data;
        }
    }


    private class EditDlg extends JDialog {
        private JTextField radioNameField;
        private JTextField confNumberField;
        private JTextField GWExtField;
        private JButton okBtn;
        private JButton cancelBtn;
        private JFrame parent;
        private boolean isGoodValues;
        private RadioMasterData editData;

        public EditDlg(JFrame iParent, String title, RadioMasterData iEditData) {
            super(iParent, title, true);
            parent = iParent;
            editData = iEditData;
            init();
            setSize(378, 150);

            setLocationRelativeTo(null);

            setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

            addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                    Object[] options = new Object[] {"Yes", "No"};
                    if (JOptionPane.showOptionDialog(parent,
                            "Are You Sure You Want to Cancel?", "Radio Data",
                            JOptionPane.DEFAULT_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null,
                            options, options[1]) == 0) {
                        windowCloseAttempt();
                    } else {
                        verifyValues();
                    }
                }
            });

        }

        private void windowCloseAttempt() {
            isGoodValues = false;
            dispose();
        }

        private void verifyValues() {

            // validation of name
            String name = radioNameField.getText().trim();

            if (name.length() == 0) {
                JOptionPane.showMessageDialog(this, "Name Can Not Be Blank",
                                              "Name Error",
                                              JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (editData == null ||
                !editData.getRadioName().equalsIgnoreCase(name)) {

                if (isThisNameDefined(name)) {
                    JOptionPane.showMessageDialog(this,
                                                  "Name Is Already In Use",
                                                  "Name Error",
                                                  JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            String confNumber = confNumberField.getText().trim();

            if (confNumber.length() == 0) {
                JOptionPane.showMessageDialog(this, "Number Can Not Be Blank",
                                              "Number Error",
                                              JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!isNumeric(confNumber)) {
                JOptionPane.showMessageDialog(this,
                                              "Non-Numerical Conference Number Are Not Allowed.",
                                              "Number Error",
                                              JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (editData == null || !editData.getConfNumber().equals(confNumber)) {
                if (isThisNumberDefined(confNumber)) {
                    JOptionPane.showMessageDialog(this,
                                                  confNumber +
                                                  " Is Already In Use",
                                                  "Number Error",
                                                  JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            String GWExt = GWExtField.getText().trim();

            if (GWExt.length() == 0) {
                JOptionPane.showMessageDialog(this, "Number Can Not Be Blank",
                                              "Number Error",
                                              JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!isNumeric(GWExt)) {
                JOptionPane.showMessageDialog(this,
                                              "Non-Numerical Radio Gateway Ext Is Not Allowed.",
                                              "Number Error",
                                              JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (editData == null || !editData.getRadioGWExt().equals(GWExt)) {
                if (isThisNumberDefined(GWExt)) {
                    JOptionPane.showMessageDialog(this,
                                                  GWExt + " Is Already In Use",
                                                  "Number Error",
                                                  JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            isGoodValues = true;
            dispose();
            return;

        }

        private void init() {
            JPanel dataPanel = new JPanel(new GridLayout(3, 2));
            dataPanel.add(new JLabel("Radio Name:"));
            radioNameField = new JTextField();
            dataPanel.add(radioNameField);
            dataPanel.add(new JLabel("Conference Number:"));
            confNumberField = new JTextField();
            dataPanel.add(confNumberField);
            dataPanel.add(new JLabel("Radio Gateway Ext.:"));
            GWExtField = new JTextField();
            dataPanel.add(GWExtField);
            JPanel buttonPanel = new JPanel();
            okBtn = new JButton("OK");
            okBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    verifyValues();
                }
            });

            cancelBtn = new JButton("Cancel");
            cancelBtn.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    windowCloseAttempt();
                }
            });

            if (editData != null) {
                radioNameField.setText(editData.getRadioName());
                confNumberField.setText(editData.getConfNumber());
                GWExtField.setText(editData.getRadioGWExt());
            }

            buttonPanel.add(okBtn);
            buttonPanel.add(cancelBtn);
            JPanel mainPane = new JPanel(new BorderLayout());
            mainPane.add(dataPanel, BorderLayout.CENTER);
            mainPane.add(buttonPanel, BorderLayout.SOUTH);
            setContentPane(mainPane);

        }

        private boolean isNumeric(String number) {
            char[] c = number.toCharArray();
            for (int i = 0; i < c.length; i++) {
                if (c[i] < '0' || c[i] > '9')
                    return false;
            }
            return true;
        }

        public boolean isAcceptableValues() {
            return isGoodValues;
        }

        public RadioMasterData getInputRadioData() {
            return new RadioMasterData(radioNameField.getText().trim(),
                                       confNumberField.getText().trim(),
                                       GWExtField.getText().trim());
        }

    }


    private boolean isThisNameDefined(String name) {
        if (name != null) {
            for (int i = 0; i < radioListModel.data.size(); i++) {
                RadioMasterData rdData = (RadioMasterData) radioListModel.data.
                                         get(i);
                if (rdData.getRadioName().equalsIgnoreCase(name))
                    return true;
            }
        }
        return false;
    }

    private boolean isThisNumberDefined(String number) {
        if (number != null) {
            for (int i = 0; i < radioListModel.data.size(); i++) {
                RadioMasterData rdData = (RadioMasterData) radioListModel.data.
                                         get(i);
                if (rdData.getConfNumber().equals(number))
                    return true;
                if (rdData.getRadioGWExt().equals(number))
                    return true;
            }
        }
        return false;
    }

    private ArrayList readMonitoredRadiosFromFlatFile() {
        ObjectInputStream in = null;
        ArrayList monitoredRadios = null;
        try {
            FileInputStream istream = new FileInputStream(
                    "etc\\Cache\\MonitoredRadios");
            in = new ObjectInputStream(istream);
            monitoredRadios = (ArrayList) in.readObject();
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
        return monitoredRadios;
    }

    private void writeMonitoredRadiosToFlatFile() {
        ArrayList monitoredRadios = radioListModel.data;
        FileOutputStream ostream = null;
        ObjectOutputStream out = null;
        File file = new File("etc\\Cache");
        if (!file.exists()) {
            file.mkdirs();
        }

        try {

            ostream = new FileOutputStream("etc\\Cache\\MonitoredRadios");
            out = new ObjectOutputStream(ostream);
            out.writeObject(monitoredRadios);
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

    public HashMap getMonitoredRadios()
    {
        HashMap radioMap = new HashMap<String, RadioMasterData>();
        for(int i=0; i<radioListModel.data.size(); i++)
        {
            RadioMasterData rdData = (RadioMasterData) radioListModel.data.get(i);
            radioMap.put(rdData.getConfNumber(), rdData);
        }

        return radioMap;
    }


}
