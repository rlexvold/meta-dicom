package net.metafusion.localstore;

import net.metafusion.util.AE;
import acme.util.Log;
import acme.util.Util;

public class SyncProcess implements Runnable
{
	private static void log(String s)
	{
		Log.log(s);
	}

	private static void log(String s, Exception e)
	{
		Log.log(s, e);
	}

	public void runPrimary()
	{
	}

	long[] getIDs(AE ae, long maxIDHave)
	{
		return null;
	}

	public boolean load(AE ae, long id)
	{
		updateMax(id);
		return false;
	}
	long maxID = 0;

	void updateMax(long id)
	{
		if (id > maxID) maxID = id;
	}

	public void sync(AE ae)
	{
		for (;;)
		{
			long[] ids = null;
			try
			{
				ids = getIDs(ae, maxID);
			}
			catch (Exception e)
			{
				Util.sleep(5 * 60 * 1000);
			}
			if (ids == null)
			{
				Util.sleep(1000); // todo: sleep here or on other side
				continue;
			}
			for (long element : ids)
			{
				if (DicomStore.get().exists(element)) continue;
				if (!load(ae, element))
				{
					Util.sleep(5 * 60 * 1000);
					continue;
				}
			}
		}
	}

	public void runSecondary()
	{
		for (;;)
			sync(LocalStore.get().GetPrimaryAE());
	}

	public void runBackup()
	{
		for (;;)
		{
			sync(LocalStore.get().GetPrimaryAE());
			sync(LocalStore.get().GetSecondaryAE());
		}
	}

	public void run()
	{
		if (LocalStore.get().IsPrimary())
			Log.log("SyncProcess: No sync process needed.");
		else if (LocalStore.get().isSecondary)
			runSecondary();
		else if (LocalStore.get().isBackup)
			runBackup();
		else log("SyncProcess: unknown state");
	}

	public static void start()
	{
		Thread t = new Thread(new SyncProcess());
		t.setDaemon(true);
		t.start();
	}
	// private boolean sync(String idString) {
	// long id = Long.parseLong(idString);
	// if (LocalStore.get().hasBackup()) {
	// Log.log("sync " + idString);
	// InetSocketAddress sa = LocalStore.get().getBackup();
	// XML xml = AdminClient.doImageSync(sa.getHostName(), sa.getPort(),
	// Long.parseLong(idString));
	// return xml.getBoolean("result");
	// }
	// return false;
	// }
	//
	// private boolean syncDelete(String idString) {
	// long id = Long.parseLong(idString);
	// if (LocalStore.get().hasBackup()) {
	// Log.log("syncDelete " + idString);
	// InetSocketAddress sa = LocalStore.get().getBackup();
	// XML xml = AdminClient.doImageSyncDelete(sa.getHostName(), sa.getPort(),
	// Long.parseLong(idString));
	// return xml.getBoolean("result");
	// }
	// return false;
	// }
	//
	// private boolean syncconfig() {
	// try {
	// if (!LocalStore.get().isProxy() && LocalStore.get().hasBackup()) {
	// XML config = XMLConfigFile.getDefault().getCurrentAdmin();
	// InetSocketAddress sa = LocalStore.get().getBackup();
	// XML xml = AdminClient.doPropagateConfig(sa.getHostName(), sa.getPort(),
	// config);
	// return xml.getBoolean("result");
	// }
	// } catch (Exception e) {
	// log("syncconfig caught", e);
	// return false;
	// }
	// return true;
	// }
	//
	//
	// public void run() {
	//
	// for (; ;) {
	// boolean failed = false;
	// if (delQueue.size() > 0) {
	// Long l = (Long) delQueue.get(0);
	// if (syncDelete("" + l))
	// delQueue.remove(0);
	// else
	// failed = true;
	// } else if (addQueue.size() > 0) {
	// Long l = (Long) addQueue.get(0);
	// if (sync("" + l))
	// addQueue.remove(0);
	// else
	// failed = true;
	// } else if (setSyncConfig(false)) {
	// if (!syncconfig()) {
	// setSyncConfig(true);
	// failed = true;
	// }
	// } else {
	// Util.sleep(5000);
	// }
	// if (failed) {
	// Util.sleep(60 * 1000);
	// }
	// }
	//
	// }
	//
	// private static int MAX_SIZE = 10000;
	// private static List addQueue = Collections.synchronizedList(new
	// LinkedList());
	// private static List delQueue = Collections.synchronizedList(new
	// LinkedList());
	// private static boolean syncConfig = false;
	//
	// private synchronized static boolean setSyncConfig(boolean on) {
	// boolean rv = syncConfig;
	// syncConfig = on;
	// return rv;
	// }
	// private static synchronized void add(long id) {
	// if (addQueue.size() >= MAX_SIZE)
	// addQueue.remove(0);
	// addQueue.add(new Long(id));
	// }
	// private static synchronized void del(long id) {
	// if (delQueue.size() >= MAX_SIZE)
	// delQueue.remove(0);
	// delQueue.add(new Long(id));
	// }
	//
	// public static void sync(long id) {
	// add(id);
	// }
	//
	// public static void syncDelete(long id) {
	// del(id);
	// }
	//
	// public static void syncConfig() {
	// setSyncConfig(true);
	// }
	//
}