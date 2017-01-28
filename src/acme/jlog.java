package acme;

import acme.util.Util;

public class jlog
{
	public static void info(String s)
	{
		Util.log(s);
	}

	public static void debug(String s)
	{
		;// Util.log(s);
	}

	public static void error(String s)
	{
		Util.log(s);
	}

	public static void error(String s, Exception e)
	{
		Util.log(s, e);
	}

	public static void warn(String s)
	{
		Util.log(s);
	}

	public static void warn(String s, Exception e)
	{
		Util.log(s, e);
	}

	public static void fatal(String s)
	{
		Util.log(s);
	}

	public static void fatal(String s, Exception e)
	{
		Util.log(s, e);
	}
}