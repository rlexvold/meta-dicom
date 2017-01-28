package acme.util;

public class Logger
{
	public interface LoggerImpl
	{
		public void println(String s);
	}
	private static LoggerImpl impl = new LoggerImpl()
	{
		public void println(String s)
		{
			System.out.println(s);
		}
	};

	public static void setLoggerImpl(LoggerImpl impl)
	{
		Logger.impl = impl;
	}

	public static void log(String s)
	{
		impl.println(s);
	}

	public static void log(String s, Exception e)
	{
		impl.println(s + " caught " + e);
		impl.println(Util.stackTraceToString(e));
	}
}