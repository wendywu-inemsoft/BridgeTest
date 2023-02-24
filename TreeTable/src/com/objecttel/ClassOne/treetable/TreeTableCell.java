package com.objecttel.ClassOne.treetable;

public class TreeTableCell 
{
	private String cellValue=null;
	private boolean highlight=false;
	
	public TreeTableCell(String cellValue, boolean highlight) 
	{
		super();
		this.cellValue = cellValue;
		this.highlight = highlight;
	}

	public String getCellValue() {
		return cellValue;
	}

	public void setCellValue(String cellValue) {
		this.cellValue = cellValue;
	}

	public boolean isHighlight() 
	{
		return highlight;
	}

	public void setHighlight(boolean highlight) {
		this.highlight = highlight;
	}
	
	public String toString()
	{
		return cellValue;
	}
}
