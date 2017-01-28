package net.metafusion.localstore.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import net.metafusion.Dicom;
import net.metafusion.admin.ServerBean;
import net.metafusion.localstore.DicomStore;
import net.metafusion.util.GlobalProperties;
import acme.db.DBManager;
import acme.util.Log;
import acme.util.StringUtil;
import acme.util.Util;
import acme.util.XML;
import acme.util.XMLConfigFile;

public class Task
{
	protected static String[]			cmdArgs		= null;
	protected static PrintStream		ps			= null;
	protected static PrintStream		vps			= null;
	protected static SimpleDateFormat	formatter	= new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ");
	protected static String				logPath		= "/data/log/audit";
	protected static String				version		= "1.0";

	protected static void log(String s)
	{
		log(s, true);
	}

	protected static void vlog(String s)
	{
		vlog(s, true);
	}

	protected static void log(String s, boolean ts)
	{
		if (ts)
		{
			s = formatter.format(new Date()) + s;
		}
		if (ps != null)
			ps.println(s);
		System.out.println(s);
	}

	protected static void vlog(String s, boolean ts)
	{
		if (ts)
		{
			s = formatter.format(new Date()) + s;
		}
		if (vps != null)
		{
			vps.println(s);
		}
	}

	protected static void initLogs(String logPrefix)
	{
		initLogs(logPrefix, true);
	}

	protected static void initLogs(String logPrefix, boolean verboseFlag)
	{
		if (logPath == null || logPrefix == null)
			return;
		File logFileDir = new File(logPath, logPrefix);
		if (!logFileDir.exists())
			logFileDir.mkdirs();
		Calendar c = Calendar.getInstance();
		int day = c.get(Calendar.DAY_OF_MONTH);
		String baseName = logPrefix + "-" + StringUtil.int2(c.get(Calendar.MONTH) + 1) + StringUtil.int2(c.get(Calendar.DAY_OF_MONTH))
				+ StringUtil.int2(c.get(Calendar.YEAR) - 2000) + "_" + StringUtil.int2(c.get(Calendar.HOUR_OF_DAY)) + StringUtil.int2(c.get(Calendar.MINUTE));
		File logFile = new File(logFileDir, baseName + ".log");
		File verboseLog = new File(logFileDir, "verbose" + baseName + ".log");
		try
		{
			ps = new PrintStream(new FileOutputStream(logFile, true));
			if (verboseFlag)
				vps = new PrintStream(new FileOutputStream(verboseLog, true));
			else
				vps = null;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("could not create printstream: " + logFile.getAbsolutePath());
		}
	}

	static void log(String s, Exception e)
	{
		log(s + " - Exception: " + e.getMessage());
	}

	static public XML getCurrentAdminStore() throws Exception
	{
		XMLConfigFile f = XMLConfigFile.getDefault();
		String storeName = f.getDefaultName();
		System.out.println("getCurrentAdminStore " + storeName);
		Util.log("getConfigRoot() " + f.getConfigRoot().getAbsolutePath());
		XML root = new XML(new File(f.getConfigRoot(), "metaadmin.xml"));
		XML x = root.getNode("stores");
		if (x != null)
			x = x.search("name", storeName);
		if (x == null)
		{
			x = root.getNode("proxies");
			if (x != null)
				x = x.search("name", storeName);
		}
		if (x == null)
			throw new RuntimeException("could not load admin for " + storeName);
		return x;
	}

	protected void printUsage()
	{
		log("usage: task conf_file_path.xml target");
		System.exit(1);
	}

	public static void start(String[] args, Class c)
	{
		cmdArgs = args;
		try
		{
			Task t = (Task) c.newInstance();
			log("Version: " + Util.getManifestVersion());
			if (args.length == 0)
			{
				t.printUsage();
			}
			if (args[0].equalsIgnoreCase("version"))
			{
				System.out.println(c.getName() + " version " + version);
				System.exit(1);
			}
			System.out.println("task conf=" + args[0] + " targ=" + args[1]);
			File CONFIG_FILE = new File(args[0]);
			XMLConfigFile configFile = new XMLConfigFile(CONFIG_FILE, args[1]);
			File CONFIG_ROOT = CONFIG_FILE.getParentFile();
			Log.vlog("" + configFile.getXML());
			File DATA_ROOT = new File(configFile.getXML().get("storage/root"));
			XML storeXML = getCurrentAdminStore();
			ServerBean serverBean = new ServerBean(storeXML);
			XML config = XMLConfigFile.getDefault().getXML();
			DBManager.init();
			Dicom.init();
			DicomStore.init(serverBean);
			DicomStore dicomStore = DicomStore.get();
			log("start task");
			t.run();
			log("end task");
		}
		catch (Exception e)
		{
			log("task  caught " + args[0] + " " + args[1]);
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void run() throws Exception
	{
	}
}