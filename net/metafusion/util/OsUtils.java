package net.metafusion.util;

public class OsUtils
{
	public static Boolean isMac()
	{
		return System.getProperty("os.name").toLowerCase().contains("mac os");
	}
}
