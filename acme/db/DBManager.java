package acme.db;

import java.util.HashMap;
import acme.util.Log;
import acme.util.XML;
import acme.util.XMLConfigFile;

public class DBManager
{
	private static DBManager instance;
	private static HashMap hashMap = new HashMap();

	public synchronized static void init() throws Exception
	{
		if (instance != null) return;
		Log.vlog("DBMGRXML=" + XMLConfigFile.getDefault().getXML());
		String driverClass = XMLConfigFile.getDefault().getXML().get("database/class");
		String url = XMLConfigFile.getDefault().getXML().get("database/url");
		DBManager.init(driverClass, url);
		instance = new DBManager(driverClass, url);
		DBManager.test();
	}

	public synchronized static void init(String driverClass, String url) throws Exception
	{
		if (instance != null) return;
		instance = new DBManager(driverClass, url);
	}

	public synchronized static void init(XML config) throws Exception
	{
		if (instance != null) return;
		instance = new DBManager(config.get("class"), config.get("url"));
	}

	public synchronized static void init(String name, String driverClass, String url) throws Exception
	{
		hashMap.put(name, new DBManager(driverClass, url));
	}

	public synchronized static void set(String name) throws Exception
	{
		DBManager db = (DBManager) hashMap.get(name);
		if (db == null) throw new RuntimeException("cannot find db " + name);
		instance = db;
	}

	public synchronized static void init(String name, XML config) throws Exception
	{
		hashMap.put(name, new DBManager(config.get("class"), config.get("url")));
	}

	public static DBManager get()
	{
		if (instance == null) throw new RuntimeException("DBManager had not been initialized");
		return instance;
	}

	public static DBManager get(String name)
	{
		DBManager dbMgr = (DBManager) hashMap.get(name);
		if (dbMgr == null) throw new RuntimeException("DBManager" + name + " had not been initialized");
		return dbMgr;
	}

	private DBManager(String driverClass, String url) throws Exception
	{
		this.driverClass = driverClass;
		this.url = url;
		Class.forName(driverClass).newInstance();
	}
	String driverClass;
	String url;

	public String getDriverClass()
	{
		return driverClass;
	}

	public String getURL()
	{
		return url;
	}

	public static boolean test()
	{
		return JDBCUtil.get().test(DBManager.get());
	}
}
