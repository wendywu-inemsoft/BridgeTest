package com.objecttel.ClassOne.treetable;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;

public class TreeTableTest extends JFrame
{	
	/**
	 * 
	 */
	private static final long serialVersionUID = 896020076298039856L;

	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception exc)
		{
			// Do nothing
		}

		String[] columnNames={"test1","test1","test1","test1"};
		ClassOneTreeTable treeTable = new ClassOneTreeTable(null,Color.BLUE);
		DefaultMutableTreeNode radioData1 = new DefaultMutableTreeNode(new TreeTableRowData("Radio1","41941","5","100","",true,true));
    	radioData1.add(new DefaultMutableTreeNode(new TreeTableRowData("Radio1","41941","1","100","",false,true)));
    	radioData1.add(new DefaultMutableTreeNode(new TreeTableRowData("Radio2","41942","1","99","",false,false)));
    	radioData1.add(new DefaultMutableTreeNode(new TreeTableRowData("Radio3","41943","1","98","",false,false)));
    	radioData1.add(new DefaultMutableTreeNode(new TreeTableRowData("Radio4","41944","1","97","",false,false)));
    	radioData1.add(new DefaultMutableTreeNode(new TreeTableRowData("Radio5","41945","1","96","",false,false)));
    	treeTable.addTreeNode(radioData1);
    	
    	JFrame testFrame=new TreeTableTest(new Dimension(600,300),"ClassOne Tree Table");
    	testFrame.getContentPane().add(treeTable, BorderLayout.CENTER);

		testFrame.setVisible(true);
	}
	
	public TreeTableTest(Dimension size,String title)
	{
		init(size,title);
	}
	
	private void init(Dimension size,String title)
	{
		super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		if(size!=null)
			super.setSize(size.width,size.height);
		else
			super.setSize(800,600);
		
		if(title!=null)
		{
			super.setTitle(title);
		}
		else
		{
			super.setTitle("");
		}
	}
}
