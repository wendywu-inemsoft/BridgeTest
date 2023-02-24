package com.objecttel.ClassOne.treetable;

public class TreeTableRowData 
{
	private TreeTableCell radioName=null;
	private TreeTableCell conferenceNum=null;
	private TreeTableCell count=null;
	private TreeTableCell signalNoiseRatio=null;
	private TreeTableCell timestamp=null;
	private boolean isRoot=false;
	private boolean hightlight=false;

	public TreeTableRowData(String radioName, String conferenceNum,
			String count, String signalNoiseRatio, String timestamp, boolean isRoot, boolean hightlight) 
	{
		super();
		this.isRoot = isRoot;
		this.hightlight = hightlight;
		if(hightlight)
		{
			this.radioName=new TreeTableCell(radioName,true);
			this.conferenceNum=new TreeTableCell(conferenceNum,true);
			this.count=new TreeTableCell(count,true);
			this.signalNoiseRatio=new TreeTableCell(signalNoiseRatio,true);
			this.timestamp=new TreeTableCell(timestamp,true);
		}
		else
		{
			this.radioName=new TreeTableCell(radioName,false);
			this.conferenceNum=new TreeTableCell(conferenceNum,false);
			this.count=new TreeTableCell(count,false);
			this.signalNoiseRatio=new TreeTableCell(signalNoiseRatio,false);
			this.timestamp=new TreeTableCell(timestamp,false);
		}
	}
	
	public boolean isHightlight() {
		return hightlight;
	}

	public void setHightlight(boolean hightlight) {
		this.hightlight = hightlight;
	}

	public boolean isRoot() {
		return isRoot;
	}


	public void setRoot(boolean isRoot) {
		this.isRoot = isRoot;
	}
	
	public TreeTableCell getColumnValue(int index)
	{
		switch(index)
		{
			case 0:
				return radioName;
			case 1:
				return conferenceNum;
			case 2: 
				return count;
			case 3:
				return signalNoiseRatio;
			case 4:
				return timestamp;
			default:
				return null;
		}
	}
	
	public String toString()
	{
		return radioName.getCellValue()+";"+hightlight;
	}
}
