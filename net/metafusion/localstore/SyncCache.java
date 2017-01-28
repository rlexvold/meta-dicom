package net.metafusion.localstore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import acme.db.JDBCUtil;
import acme.util.Log;
import acme.util.XML;
import acme.util.XMLConfigFile;

public class SyncCache
{
	private static SyncCache instance = null;

	static public SyncCache get()
	{
		return instance;
	}

	static public void init()
	{
		String date = "";
		try
		{
			XML admin = XMLConfigFile.getDefault().getCurrentAdmin();
			date = admin.get("DATE");
		}
		catch (Exception e)
		{
			Log.log("could not open admin", e);
			throw new RuntimeException(e);
		}
		instance = new SyncCache(date);
	}

	private SyncCache(String adminVersion)
	{
		this.adminVersion = adminVersion;
		String sel = "select imageid from dcm_image order by imageid desc limit " + CACHE_SIZE;
		Log.log("SYNC SELECT IS " + sel);
		List l = JDBCUtil.get().selectList(sel);
		Collections.reverse(l);
		Iterator iter = l.iterator();
		while (iter.hasNext())
		{
			newImage(((Long) iter.next()).longValue());
		}
		String sel2 = "select deleteid,studyid from dcm_delete order by deleteid desc limit " + CACHE_SIZE;
		Log.log("SYNC SELECT2 IS " + sel);
		List l2 = JDBCUtil.get().selectTupleList(sel2);
		Collections.reverse(l2);
		Iterator iter2 = l2.iterator();
		while (iter2.hasNext())
		{
			Object[] t = (Object[]) iter2.next();
			delStudy(((Long) t[0]).longValue(), ((Long) t[1]).longValue());
		}
	}
	private long maxSync = 0;
	private long maxDel = 0;
	private int CACHE_SIZE = 1000;
	private long syncID[] = new long[CACHE_SIZE];
	private long delID[] = new long[CACHE_SIZE];
	private long delStudyID[] = new long[CACHE_SIZE];
	private String adminVersion = "";

	public synchronized String getAdminVersion()
	{
		return adminVersion;
	}

	public synchronized void setAdminVersion(String version)
	{
		this.adminVersion = version;
	}

	public long getMaxSync()
	{
		return maxSync;
	}

	public long getMaxDelSync()
	{
		return maxDel;
	}

	public synchronized void newImage(long id)
	{
		if (maxSync > id) return;
		maxSync = id;
		syncID[(int) (id % (long) CACHE_SIZE)] = id;
	}

	public synchronized void delStudy(long deleteid, long studyid)
	{
		if (maxDel > deleteid) return;
		maxDel = deleteid;
		delID[(int) (deleteid % (long) CACHE_SIZE)] = deleteid;
		delStudyID[(int) (deleteid % (long) CACHE_SIZE)] = studyid;
	}

	public synchronized List getSyncFromCache(long id)
	{
		List l = new ArrayList();
		for (;;)
		{
			id++;
			if (syncID[(int) (id % (long) CACHE_SIZE)] == id)
				l.add(new Long(id));
			else break;
		}
		return l;
	}

	public synchronized List getDelFromCache(long id)
	{
		List l = new ArrayList();
		for (;;)
		{
			id++;
			if (delID[(int) (id % (long) CACHE_SIZE)] == id)
				l.add(new Object[] { new Long(id), new Long(delStudyID[(int) (id % (long) CACHE_SIZE)]) });
			else break;
		}
		return l;
	}

	public synchronized List getSync(long id)
	{
		if (id != 0 && syncID[(int) (id % (long) CACHE_SIZE)] == id) return getSyncFromCache(id);
		String sel = "select imageid from dcm_image where imageid > " + id + "order by imageid limit " + CACHE_SIZE;
		Log.log("SYNC SELECT IS " + sel);
		List l = JDBCUtil.get().selectList(sel);
		return l;
	}

	public synchronized List getDelSync(long id)
	{
		if (id != 0 && syncID[(int) (id % (long) CACHE_SIZE)] == id) return getSyncFromCache(id);
		String sel = "select deleteid,studyid from dcm_delete where deleteid > " + id + "order by deleteid limit " + CACHE_SIZE;
		Log.log("SYNC DEL SELECT IS " + sel);
		List l = JDBCUtil.get().selectTupleList(sel);
		return l;
	}
}