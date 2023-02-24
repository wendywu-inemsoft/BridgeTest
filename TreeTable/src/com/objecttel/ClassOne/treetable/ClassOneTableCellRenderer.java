package com.objecttel.ClassOne.treetable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;

import com.elevenworks.swing.table.BrushedMetalTableCellRenderer;

public class ClassOneTableCellRenderer extends BrushedMetalTableCellRenderer
{
	private Color hightlight=Color.BLACK;
	
	public ClassOneTableCellRenderer(Color color)
	{
		hightlight=color;
	}
	
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		if (isSelected)
		{
			if (table.hasFocus())
			{
				label.setForeground(Color.WHITE);
			}
			else
			{
				label.setForeground(hightlight);
			}
		}
		else
		{
			label.setForeground(hightlight);
		}

		label.setText(getText(value));
		label.setIcon(getIcon(value));
		if(((TreeTableCell)value).isHighlight())
			label.setFont(new Font("SansSerif", Font.BOLD, 12));
		else
			label.setFont(new Font("SansSerif", Font.PLAIN, 11));
		
		return label;
	}
}
