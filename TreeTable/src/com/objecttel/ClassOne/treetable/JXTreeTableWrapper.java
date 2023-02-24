package com.objecttel.ClassOne.treetable;

import java.awt.Color;

import javax.swing.plaf.ComponentUI;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.TreeTableModel;

import com.elevenworks.swing.table.BrushedMetalTableUI;

public class JXTreeTableWrapper extends JXTreeTable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5312261804916765703L;

	public JXTreeTableWrapper(TreeTableModel treeModel,Color color)
	{
		super(treeModel);
		setUI(new ClassOneTreeTableUI(color));
	}

	public JXTreeTableWrapper(Color color)
	{
		super();
		setUI(new ClassOneTreeTableUI(color));
	}
	
	protected void setUI(ComponentUI newUI)
	{
		if (newUI instanceof BrushedMetalTableUI)
		{
			super.setUI(newUI);
		}
	}
	
	public boolean getScrollableTracksViewportHeight()
	{
		return getPreferredSize().height < getParent().getHeight();
	}

	public boolean getScrollableTracksViewportWidth()
	{
		return getPreferredSize().width < getParent().getWidth();
	}
}
