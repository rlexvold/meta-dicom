package acme.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import net.metafusion.model.ServiceLogView;
import net.metafusion.util.GlobalProperties;

public class Log
{
	static int				level				= 1;
	static File				logFile				= null;
	static PrintStream		vps					= null;
	static PrintStream		ps					= null;
	static PrintStream		aps					= null;
	static boolean			init				= false;
	static String			prefix				= "";
	static int				currentDay			= -1;
	static int				currentMonth		= -1;
	static File				path				= new File(".");
	static SimpleDateFormat	formatter			= new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]");
	static int				keepLogDays			= 1200;
	static int				keepVlogDays		= 120;
	static int				keepAdminLogDays	= 120;

	public synchronized static void rotate()
	{
		if (aps == null)
		{
			File adminFile = new File(path, "admin.log");
			try
			{
				aps = new PrintStream(new FileOutputStream(adminFile, true));
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("could not create aps printstream");
			}
		}
		Calendar c = Calendar.getInstance();
		int day = c.get(Calendar.DAY_OF_MONTH);
		if (currentDay == day)
			return;
		currentDay = day;
		// Clean out service log table
		ServiceLogView.get().cleanup();
		Calendar vlogThreshold = Calendar.getInstance();
		vlogThreshold.add(Calendar.DATE, -keepVlogDays);
		String[] vlogFiles = path.list(new FileNameFilter("vlog"));
		for (int i = 0; i < vlogFiles.length; i++)
		{
			File delLog = new File(path, vlogFiles[i]);
			Long lastMod = delLog.lastModified();
			if (lastMod < vlogThreshold.getTimeInMillis())
				Util.safeDelete(delLog);
		}
		// rename verbose
		Util.safeClose(vps);
		String baseName = prefix + "-" + StringUtil.int2(c.get(Calendar.MONTH) + 1) + StringUtil.int2(c.get(Calendar.DAY_OF_MONTH)) + StringUtil.int2(c.get(Calendar.YEAR) - 2000)
				+ ".vlog";
		File newDayFile = new File(path, baseName);
		try
		{
			vps = new PrintStream(new FileOutputStream(newDayFile, true));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("could not create vps printstream");
		}
		Calendar logThreshold = Calendar.getInstance();
		logThreshold.add(Calendar.DATE, -keepLogDays);
		String[] logFiles = path.list(new FileNameFilter("log"));
		for (int i = 0; i < logFiles.length; i++)
		{
			File delLog = new File(path, logFiles[i]);
			Long lastMod = delLog.lastModified();
			if (lastMod < logThreshold.getTimeInMillis())
				Util.safeDelete(delLog);
		}
		int month = c.get(Calendar.MONTH);
		if (month == currentMonth)
			return;
		currentMonth = month;
		// rename progress
		File currentLogFile = new File(path, prefix + ".log");
		baseName = prefix + "-" + StringUtil.int2(c.get(Calendar.MONTH) + 1) + StringUtil.int2(c.get(Calendar.DAY_OF_MONTH)) + StringUtil.int2(c.get(Calendar.YEAR) - 2000) + "_"
				+ StringUtil.int2(c.get(Calendar.HOUR_OF_DAY)) + StringUtil.int2(c.get(Calendar.MINUTE));
		logFile = new File(path, baseName + ".log");
		if (logFile.exists())
		{
			int count = 1;
			for (;;)
			{
				String fileName = baseName + "_" + (count++) + ".log";
				logFile = new File(path, fileName);
				if (!logFile.exists() || count > 150)
					break;
			}
		}
		Util.safeClose(ps);
		FileUtil.safeRename(currentLogFile, logFile);
		try
		{
			ps = new PrintStream(new FileOutputStream(currentLogFile, true));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("could not create ps printstream");
		}
	}

	static public void init(File initPath, String initPrefix)
	{
		if (init)
			return;
		try
		{
			initPath.mkdir();
			init = true;
			path = initPath;
			prefix = initPrefix;
			Integer tmp = (Integer) GlobalProperties.get().get("daysToKeepLog");
			if (tmp != null)
				keepLogDays = tmp;
			tmp = (Integer) GlobalProperties.get().get("daysToKeepVlog");
			if (tmp != null)
				keepVlogDays = tmp;
			tmp = (Integer) GlobalProperties.get().get("defaultLogVerbosity");
			if (tmp != null)
				level = tmp;
			rotate();
		}
		catch (Exception e)
		{
			System.out.println("could not create Log file");
			e.printStackTrace();
		}
	}
	static final boolean	suppress	= false;

	synchronized static private void dovlog(String s)
	{
		if (suppress)
			return;
		s = formatter.format(new Date()) + "[" + Thread.currentThread().getName() + "] " + s;
		System.out.println(s);
		if (!init)
			return;
		if (vps != null)
			vps.println(s);
	}

	private synchronized static void dolog(String s)
	{
		if (suppress)
			return;
		s = formatter.format(new Date()) + "[" + Thread.currentThread().getName() + "] " + s;
		System.out.println(s);
		if (!init)
			return;
		if (ps != null)
			ps.println(s);
		if (vps != null)
			vps.println(s);
	}

	public synchronized static void aLog(String s)
	{
		s = "ADMIN TASK: " + formatter.format(new Date()) + "[" + Thread.currentThread().getName() + "] " + s;
		System.out.println(s);
		if (init)
		{
			if (aps != null)
				aps.println(s);
			if (ps != null)
				ps.println(s);
		}
	}

	public static void log(String s)
	{
		dolog(s);
	}

	public static void log(String s, Exception e)
	{
		if (!suppress)
		{
			dolog("exception: " + s);
			dolog(Util.stackTraceToString(e));
		}
		else
		{
			System.out.println("exception: " + s);
			System.out.println(Util.stackTraceToString(e));
		}
	}

	public static void vlog(String s)
	{
		if (level >= 1)
			dovlog(s);
	}

	public static void vvlog(String s)
	{
		if (level >= 2)
			dovlog(s);
	}

	public static void vvvlog(String s)
	{
		if (level >= 3)
			dovlog(s);
	}

	static public void setVerbose(int newLevel)
	{
		level = newLevel;
	}

	static public int getLevel()
	{
		return level;
	}

	static public boolean v()
	{
		return level >= 1;
	}

	static public boolean vv()
	{
		return level >= 2;
	}

	static public boolean vvv()
	{
		return level >= 3;
	}
}
