package net.metafusion.localstore;

import java.io.File;
import acme.storage.SSStore;
import acme.util.FileUtil;
import acme.util.Util;

public class UtilProcess implements Runnable
{
	static void log(String s)
	{
		Util.log(s);
	}

	static void log(String s, Exception e)
	{
		Util.log(s, e);
	}
	static long capacity = 0;
	static long free = 0;

	synchronized public static long getCapacityInK()
	{
		return capacity;
	}

	synchronized public static long getFreeSpaceInK()
	{
		return free;
	}

	// warning if less than 250MB
	synchronized public static boolean freeSpaceWarning()
	{
		return capacity != 0 && free < 1024 * 250;
	}
	static boolean checkFreeSpace = false;

	synchronized static void requestFreeSpaceRulesCheck()
	{
		checkFreeSpace = true;
	}
	static long lastCheck = 0;

	synchronized static boolean checkFreeSpaceRulesNow()
	{
		// always check at least every five minutes
		if (System.currentTimeMillis() > lastCheck + 1000 * 60 * 5)
		{
			checkFreeSpace = false;
			return true;
		}
		// at most every 1 minutes
		if (checkFreeSpace && System.currentTimeMillis() > lastCheck + 1000 * 60 * 1)
		{
			checkFreeSpace = false;
			return true;
		}
		return false;
	}

	public void run()
	{
		File f = SSStore.get().getRootDir();
		f.getParentFile();
		for (;;)
		{
			try
			{
				if (checkFreeSpaceRulesNow())
				{
					log("UtilProcess: checking free space rules...");
					DicomStore.get().applyFreeSpaceRules();
					lastCheck = System.currentTimeMillis();
				}
				long[] capFree = FileUtil.getCapFreeNowInK(SSStore.get().getFilesystemRoot());
				// make sure check doesn't fail
				if (capFree[0] != 0 || capFree[1] != 0)
				{
					capacity = capFree[0];
					free = capFree[1];
				}
			}
			catch (Exception e)
			{
				log("UtilProcess caught (continuing)", e);
			}
			Util.sleep(15000);
		}
	}
}