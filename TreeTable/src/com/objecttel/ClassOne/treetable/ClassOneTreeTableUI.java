package com.objecttel.ClassOne.treetable;

import java.awt.Color;

import javax.swing.JComponent;

import org.jdesktop.swingx.JXTreeTable;

import com.elevenworks.swing.treetable.BrushedMetalCellRendererPane;
import com.elevenworks.swing.treetable.BrushedMetalTreeTableUI;

public class ClassOneTreeTableUI extends BrushedMetalTreeTableUI
{
	private Color hightlight=null;
	
	public ClassOneTreeTableUI(Color color)
	{
		super();
		this.hightlight=color;
	}
		
	public void installUI(JComponent c)
	{
		super.installUI(c);

		table.remove(rendererPane);

		rendererPane = new BrushedMetalCellRendererPane();
		table.add(rendererPane);

		((JXTreeTable)table).setTreeCellRenderer(new ClassOneTreeCellRenderer(hightlight));
	}
}
