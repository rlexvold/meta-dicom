package net.metafusion.localstore.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import acme.db.JDBCUtil;
import acme.util.Util;

public class Sync
{
	static Sync sync = new Sync();

	public static Sync get()
	{
		return sync;
	}
	static private ArrayList syncList = new ArrayList();

	static public void init(int port) throws Exception
	{
		Util.log("Sync.init");
		SyncListener s = new SyncListener(port);
		Util.startDaemonThread(s);
	}

	static public void addDest(String host, int port)
	{
		Util.log("Sync.addDest " + host + " " + port);
		SyncSender sender = new SyncSender(host, port);
		Util.startDaemonThread(sender);
		syncList.add(sender);
	}

	public void put(File combinedFile)
	{
		for (int i = 0; i < syncList.size(); i++)
		{
			SyncSender sender = (SyncSender) syncList.get(i);
			SyncMsg sm = new SyncMsg();
			sm.cmd = "syncImage";
			sm.file = combinedFile;
			sender.add(sm);
		}
	}

	public void putDelete(String studyuid)
	{
		for (int i = 0; i < syncList.size(); i++)
		{
			SyncSender sender = (SyncSender) syncList.get(i);
			SyncMsg sm = new SyncMsg();
			sm.cmd = "syncStudyDelete";
			sm.args = new HashMap();
			sm.args.put("studyuid", studyuid);
			sender.add(sm);
		}
	}

	public void put(String mfString, HashMap map)
	{
		if (map.containsKey("userid"))
		{
			String username = JDBCUtil.get().selectString("select username from web_user where userid =" + map.get("userid"));
			map.put("username", username);
		}
		if (map.containsKey("studyid"))
		{
			String studyuid = JDBCUtil.get().selectString("select studyuid from dcm_study where studyid =" + map.get("studyid"));
			map.put("studyuid", studyuid);
		}
		for (int i = 0; i < syncList.size(); i++)
		{
			SyncSender sender = (SyncSender) syncList.get(i);
			SyncMsg sm = new SyncMsg();
			sm.cmd = mfString;
			sm.args = map;
			sender.add(sm);
		}
	}
}