package net.metafusion.model;

import java.io.Serializable;
import java.util.Properties;

public class SystemInfo implements Serializable
{
	String	systemKey;
	String	systemValue;
	
	public String getSystemKey()
	{
		return systemKey;
	}

	public void setSystemKey(String key)
	{
		this.systemKey = key;
	}

	public String getSystemValue()
	{
		return systemValue;
	}

	public void setSystemValue(String value)
	{
		this.systemValue = value;
	}

}
