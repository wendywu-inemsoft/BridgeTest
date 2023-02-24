package com.objecttel.ClassOne.treetable;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;

import com.elevenworks.swing.panel.BrushedMetalPanel;
import com.elevenworks.swing.panel.BrushedMetalScrollPaneUI;
import com.elevenworks.swing.table.BrushedMetalTableHeaderRenderer;

/**
 * @author sli
 *
 */
public class ClassOneTreeTable extends BrushedMetalPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6665157233674717729L;
	private DefaultMutableTreeNode rootNode=null;
	private JXTreeTableWrapper vTreeTable=null;
	private Color hightlight=Color.BLACK;
	private String[] columnNames=null;
	
	/**
	 * @param columnNames	Column name string array, if null, default is {"Radio Name","Conference Number","Count","SNR","Time"}
	 * @param hightlight	Defines the tree table row font color, if null, default is Color.BLACK
	 */
	public ClassOneTreeTable(String[] columnNames,Color hightlight)
	{
		this.columnNames=columnNames;
		this.hightlight=hightlight;
		init();
	}
	
	private void init()
	{
		this.setLayout(new BorderLayout());

		// Create a panel
		JPanel vPanel = new JPanel();
		vPanel.setBorder(new EmptyBorder(10,10,10,10));
		vPanel.setLayout(new BorderLayout());
		this.add(vPanel, BorderLayout.CENTER);

		// Create the treetable
		vTreeTable = new JXTreeTableWrapper(hightlight);
		JScrollPane vScrollPane = new JScrollPane(vTreeTable,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		vScrollPane.setUI(new BrushedMetalScrollPaneUI());
		vPanel.add(vScrollPane,BorderLayout.CENTER);
	}
	
	public void addTreeNode(DefaultMutableTreeNode rowData)
	{
		if(rootNode==null)
		{
			rootNode = new DefaultMutableTreeNode(new TreeTableRowData("","","","","",true,true));
		}
		rootNode.add(rowData);
		configureTreeTable();
	}
	
	public void clearContent()
	{
		if(rootNode!=null)
		{
			rootNode.removeAllChildren();
		}
	}
	
	private void configureTreeTable()
	{
		if(rootNode==null)
		{
			rootNode = new DefaultMutableTreeNode(new TreeTableRowData("None","None","None","None","None",true,true));
		}
		vTreeTable.setTreeTableModel(new ClassOneTreeTableModel(rootNode,columnNames));

//		vTreeTable.setTreeCellRenderer(new BrushedMetalTreeCellRenderer());
//		TableCellRenderer vCellRenderer = new BrushedMetalTableCellRenderer();

		// configure column header
		for (int i = 0; i < vTreeTable.getColumnCount(); i++)
		{
			TableColumn vColumn = vTreeTable.getColumnModel().getColumn(i);
			vColumn.setHeaderRenderer(new BrushedMetalTableHeaderRenderer());
			if (i > 0)
			{
				vColumn.setCellRenderer(new ClassOneTableCellRenderer(hightlight));
			}
		}
	}
}
