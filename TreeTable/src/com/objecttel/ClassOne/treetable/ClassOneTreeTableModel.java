package com.objecttel.ClassOne.treetable;

import javax.swing.tree.DefaultMutableTreeNode;

import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

public class ClassOneTreeTableModel extends AbstractTreeTableModel
{
	private String [] titles = {"Radio Name","Conference Number","Count","SNR","Time"};


	public ClassOneTreeTableModel(DefaultMutableTreeNode root,String[] clmnNames)
	{
		super(root);
		setColumnTitles(clmnNames);
	}

	private void setColumnTitles(String[] names)
	{
		if(names!=null&&names.length>0)
		{
			if(titles!=null&&titles.length>0)
			{
				titles=null;
			}
			titles=names;
		}
	}
	
	/**
	 * Table Columns
	 */
	public String getColumnName(int column) 
	{
		if (column < titles.length)
			return (String) titles[column];
		else
			return "";
	}

	public int getColumnCount()
	{
		return titles.length;
	}

	//	public Class getColumnClass(int column)
	//	{
	//		return String.class;
	//	}

	public Object getValueAt(Object arg0, int arg1)
	{
		if(arg0 instanceof TreeTableRowData)
		{
			TreeTableRowData data = (TreeTableRowData)arg0;
			if(data != null)
			{
				return data.getColumnValue(arg1);
			}

		}

		if(arg0 instanceof DefaultMutableTreeNode)
		{
			DefaultMutableTreeNode dataNode = (DefaultMutableTreeNode)arg0;
			TreeTableRowData data = (TreeTableRowData)dataNode.getUserObject();
			if(data != null)
			{
				return data.getColumnValue(arg1);
			}
		}
		return null;
	}

	public Object getChild(Object arg0, int arg1)
	{

		if(arg0 instanceof DefaultMutableTreeNode)
		{
			DefaultMutableTreeNode nodes = (DefaultMutableTreeNode)arg0;
			return nodes.getChildAt(arg1);
		}
		return null;
	}

	public int getChildCount(Object arg0)
	{

		if(arg0 instanceof DefaultMutableTreeNode)
		{
			DefaultMutableTreeNode nodes = (DefaultMutableTreeNode)arg0;
			return nodes.getChildCount();
		}
		return 0;
	}

	public int getIndexOfChild(Object arg0, Object arg1)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isLeaf(Object node) 
	{
		return getChildCount(node) == 0;
	}
}
