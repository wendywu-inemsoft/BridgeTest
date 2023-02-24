package com.objecttel.ClassOne.BridgeTest;
import java .io.*;

/** 
 * <p>Copyright: Copyright ObjectTel (c) 2003-2013</p>
 * <p>Company: Objecttel</p> 
 */
public class RadioMasterData implements Comparable, Serializable {

    private String radioName;
    private String confNumber;
    private String radioGWExt;


    public RadioMasterData(String rdName, String confName, String ext) {

        radioName = rdName;
        confNumber = confName;
        radioGWExt = ext;
    }

    public int compareTo(Object o)
    {
        if(o == null)
            return -1;
        if(!(o instanceof RadioMasterData))
            return -1;
        RadioMasterData rdData = (RadioMasterData)o;
        return radioName.compareTo(rdData.radioName);
    }

    public boolean equals(Object o)
    {
        if(o == null)
            return false;
        if(!(o instanceof RadioMasterData))
            return false;
        RadioMasterData rdData = (RadioMasterData)o;
        return confNumber.equals(rdData.confNumber);
    }

    public String getRadioName()
    {
        return radioName;
    }

    public String getConfNumber()
    {
        return confNumber;
    }

    public String getRadioGWExt()
    {
        return radioGWExt;
    }

    public String getColumnValue(int column)
    {
        switch(column)
        {
          case 0:
              return radioName;
          case 1:
              return confNumber;
          case 2:
              return radioGWExt;
          default:
              return "Unknown";
        }

    }

}
