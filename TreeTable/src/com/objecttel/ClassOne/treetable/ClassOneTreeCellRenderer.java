package com.objecttel.ClassOne.treetable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTree;

import org.jdesktop.swingx.JXTreeTable;

import com.elevenworks.swing.treetable.BrushedMetalTreeCellRenderer;

public class ClassOneTreeCellRenderer extends BrushedMetalTreeCellRenderer
{
	private Color hightlight=Color.BLACK;
	
	public ClassOneTreeCellRenderer(Color color)
	{
		super();
		this.hightlight=color;
	}

	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
	{
		label.setForeground(hightlight);

		if (selected)
		{
			Component vRenderPane = tree.getParent();
			if (vRenderPane != null)
			{
				JXTreeTable vTable = (JXTreeTable)vRenderPane.getParent();
				if (vTable != null)
				{
					if (vTable.hasFocus())
					{
						label.setForeground(Color.WHITE);
					}
				}
			}
		}
		// value is of type DefaultMutableTreeNode, DefaultMutableTreeNode.toString() returns userObject.toString()
		String str=value.toString();
		String[] tokens=str.split(";");
		label.setText(tokens[0]);
		if(tokens[1].equalsIgnoreCase("true"))
		{
			label.setFont(new Font("SansSerif", Font.BOLD, 12));
		}
		else
		{
			label.setFont(new Font("SansSerif", Font.PLAIN, 11));
		}
		return label;
	}
}
