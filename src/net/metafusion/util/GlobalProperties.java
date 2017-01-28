package net.metafusion.util;

import java.util.Properties;

public class GlobalProperties extends Properties
{
	public static GlobalProperties instance = null;

	public static GlobalProperties get()
	{
		if (instance == null)
		{
			instance = new GlobalProperties();
		}
		return instance;
	}
	
}
