package net.metafusion.localstore;

import java.io.File;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.metafusion.Dicom;
import net.metafusion.admin.ServerBean;
import net.metafusion.figaro.Figaro;
import net.metafusion.importer.ImporterListener;
import net.metafusion.localstore.mirror.MirrorClient;
import net.metafusion.localstore.soap.SoapServer;
import net.metafusion.localstore.sync.Sync;
import net.metafusion.localstore.tasks.Ping;
import net.metafusion.net.DicomServerSession;
import net.metafusion.simulator.Simulator;
import net.metafusion.util.AE;
import net.metafusion.util.AEMap;
import net.metafusion.util.GlobalProperties;
import net.metafusion.util.InternalSelfCheck;
import acme.db.DBManager;
import acme.storage.SSStore;
import acme.storage.SSStoreFactory;
import acme.util.Log;
import acme.util.TSLog;
import acme.util.Util;
import acme.util.XML;
import acme.util.XMLConfigFile;

public class LocalStore implements Runnable
{
	// MaxTracker max = new MaxTracker();
	// MaxTracker total = new MaxTracker();
	class MaxTracker
	{
		long	last	= 0;
		long	max		= 0;

		MaxTracker()
		{
		}

		MaxTracker(long l)
		{
			max = l;
			last = l;
		}

		boolean add(long l)
		{
			boolean change = false;
			if (Math.abs(last - l) > 1)
				change = true;
			last = l;
			if (l > max)
			{
				max = l;
				change = true;
			}
			return change;
		}

		@Override
		public String toString()
		{
			return "" + last + "(" + max + ")";
		}
	}
	static public File		CONFIG_FILE;
	static public File		CONFIG_ROOT;
	static public File		DATA_ROOT;
	static LocalStore		instance						= null;
	TSLog					accessLog;
	File					accessLogFile;
	AE						ae;
	int						batCartAltPort					= 0;
	int						batCartPort						= 80;
	String					cmoveQueryType					= null;
	LocalStoreDicomServer	dicomServer;
	LocalStoreDicomServer	dicomServerCompressed;
	DicomStore				dicomStore;
	int						forwardThreadCount;
	MaxTracker				freeMax							= new MaxTracker();
	boolean					importerListenerDeleteRecord	= true;
	int						importerListenerPollInterval	= 30000;
	boolean					isBackup						= false;
	boolean					isBatCart						= false;
	boolean					isEcho							= false;
	// RAL - Added for importer
	boolean					isImporterListener				= false;
	boolean					isPrimary						= false;
	boolean					isRIS							= false;
	boolean					isSecondary						= false;
	boolean					isSink							= false;
	boolean					isSOAP							= false;
	boolean					isSync							= false;
	AE						primaryAE;
	int						risPort							= 4007;
	String					role							= "";
	AE						secondaryAE;
	ServerBean				serverBean						= new ServerBean();
	int						soapPort						= 9090;
	String					syncHost						= "";
	List					syncList						= new ArrayList();
	int						syncPort						= 0;

	private void processConfig(XML config) throws Exception
	{
		ae = AEMap.get(config.get("ae", ""));
		if (ae == null)
			throw new Exception("ae not found");
		role = config.get("role");
		Log.log("ROLE = " + role);
		if (role.equals("primary"))
			isPrimary = true;
		else if (role.equals("secondary"))
		{
			isSecondary = true;
			primaryAE = AEMap.get(config.get("primaryae", ""));
			if (primaryAE == null)
				throw new Exception("primaryAE not found");
		}
		else if (role.equals("backup"))
		{
			isBackup = true;
			primaryAE = AEMap.get(config.get("primaryae", ""));
			if (primaryAE == null)
				throw new Exception("primaryAE not found");
			secondaryAE = AEMap.get(config.get("secondaryAE", ""));
			if (secondaryAE == null)
				throw new Exception("secondaryAE not found");
		}
		else if (role.equals("sink"))
			isSink = true;
		else if (role.equals("echo"))
			isEcho = true;
		else
		{
			Log.log("UNKNOWN ROLE " + role);
			Log.log("exiting...");
			System.exit(-1);
		}
		String s = config.get("batcart/port");
		if (s != null && s.length() > 0)
		{
			isBatCart = true;
			batCartPort = Integer.parseInt(s);
			s = config.get("batcart/altport");
			if (s != null && s.length() > 0)
				batCartAltPort = Integer.parseInt(s);
		}
		// RAL - Added for importer
		s = config.get("importerListener/pollIntervalInMsec");
		if (s != null && s.length() > 0)
		{
			isImporterListener = true;
			importerListenerPollInterval = Integer.parseInt(s);
			GlobalProperties.get().put("importListenerPollInterval", importerListenerPollInterval);
		}
		s = config.get("importerListener/removeRecordAfterProcessing");
		if (s != null && s.length() > 0)
		{
			if (s.equals("true") || s.equals("t") || s.equals("1"))
				importerListenerDeleteRecord = true;
			else
				importerListenerDeleteRecord = false;
			isImporterListener = true;
			GlobalProperties.get().put("importerListenerDeleteRecord", importerListenerDeleteRecord);
		}
		s = config.get("cmoveQuery/type");
		if (s != null && s.length() > 0)
		{
			cmoveQueryType = s.toLowerCase();
			GlobalProperties.get().put("cmoveQueryType", cmoveQueryType);
		}
		s = config.get("richmedia/dir");
		if (s != null)
		{
			SSStore tmp = SSStore.get();
			tmp.setRichMediaRoot(new File(tmp.getRootDir().getAbsolutePath(), s));
			GlobalProperties.get().put("richmedia", s);
		}
		s = config.get("openOffice/port");
		if (s != null)
		{
			GlobalProperties.get().put("openOffice/port", s);
		}
		s = config.get("openOffice/cmd");
		if (s != null)
		{
			GlobalProperties.get().put("openOffice/cmd", s);
		}
		s = config.get("options/auditLogDir");
		if (s != null)
		{
			GlobalProperties.get().put("auditLogDir", s);
		}
		else
		{
			GlobalProperties.get().put("auditLogDir", "/data/log/audit");
		}
		s = config.get("options/beaconURL");
		if (s != null && s.length() > 1)
		{
			GlobalProperties.get().put("beaconURLs", s.split(","));
		}
		Integer tmpInt = new Integer(60);
		s = config.get("options/beaconWaitTime");
		if (s != null)
		{
			try
			{
				tmpInt = Integer.parseInt(s);
			}
			catch (Exception e)
			{
				tmpInt = 60;
			}
		}
		else
			tmpInt = 60;
		GlobalProperties.get().put("beaconWaitTime", tmpInt);
		s = config.get("options/rsyncChunkSizeMB");
		if (s != null)
		{
			try
			{
				tmpInt = Integer.parseInt(s);
			}
			catch (Exception e)
			{
				tmpInt = 5;
			}
		}
		else
			tmpInt = 5;
		GlobalProperties.get().put("rsyncChunkSizeMB", tmpInt);
		tmpInt = 120;
		s = config.get("options/daysToKeepServiceLog");
		if (s != null)
		{
			try
			{
				tmpInt = Integer.parseInt(s);
			}
			catch (Exception e)
			{
				tmpInt = 120;
			}
		}
		GlobalProperties.get().put("daysToKeepServiceLog", tmpInt);
		s = config.get("options/storeDicomHeaders");
		if (s != null && s.length() > 0)
		{
			Boolean flag = false;
			if (s.equalsIgnoreCase("true"))
				flag = true;
			GlobalProperties.get().put("storeDicomHeaders", flag);
		}
		s = config.get("ris/port");
		if (s != null && s.length() > 0)
		{
			isRIS = true;
			risPort = Integer.parseInt(s);
		}
		s = config.get("archive/type");
		if (s != null && s.length() > 0)
		{
			GlobalProperties.get().put("archiveSystem", s);
		}
		s = config.get("archive/dir");
		if (s != null && s.length() > 0)
		{
			GlobalProperties.get().put("archiveDir", s);
		}
		s = config.get("primera/device");
		if (s != null && s.length() > 0)
		{
			GlobalProperties.get().put("archiveDevice", s);
			s = config.get("primera/jobdir");
			if (s != null && s.length() > 0)
			{
				GlobalProperties.get().put("primeraJobDir", s);
			}
			s = config.get("primera/labeldir");
			if (s != null && s.length() > 0)
			{
				GlobalProperties.get().put("primeraLabelDir", s);
			}
		}
		s = config.get("soap/port");
		if (s != null && s.length() > 0)
		{
			isSOAP = true;
			soapPort = Integer.parseInt(s);
		}
		if (config.getNode("sync") != null)
		{
			isSync = true;
			syncHost = config.get("sync/host");
			syncPort = config.getInt("sync/port");
			List l = config.getNode("sync").getList("dest");
			for (int i = 0; i < l.size(); i++)
			{
				XML x = (XML) l.get(i);
				InetSocketAddress isa = new InetSocketAddress(x.get("host"), x.getInt("port"));
				syncList.add(isa);
			}
		}
		XML m = config.getNode("remote_view");
		if (m != null)
		{
			List l = m.getList("source");
			for (int i = 0; i < l.size(); i++)
			{
				XML xml = (XML) l.get(0);
				MirrorClient.startInstance(xml.get("ae"));
			}
		}
		forwardThreadCount = config.getInt("forwardthreads", ForwardProcess.NUM_FORWARD_THREAD);
	}

	public LocalStore() throws Exception
	{
		instance = this;
		XML storeXML = getCurrentAdminStore();
		serverBean = new ServerBean(storeXML);
		DBManager.init();
		Dicom.init();
		DicomStore.init(serverBean);
		SyncCache.init();
		accessLogFile = new File(SSStoreFactory.getRoot(), "access.log");
		accessLog = new TSLog(accessLogFile);
		dicomStore = DicomStore.get();
		log("syncFilesAndDatabase");
		processConfig(XMLConfigFile.getDefault().getXML());
	}

	static int calc(long value)
	{
		return (int) ((value / 1024) / 1024);
	}

	static public LocalStore get()
	{
		return instance;
	}

	static void log(String s)
	{
		Log.log(s);
	}

	static void log(String s, Exception e)
	{
		Log.log(s, e);
	}

	public int getBatCartAltPort()
	{
		return batCartAltPort;
	}

	public int getBatCartPort()
	{
		return batCartPort;
	}

	public XML getCurrentAdminStore() throws Exception
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

	public AE GetPrimaryAE()
	{
		return primaryAE;
	}

	public int getRISPort()
	{
		return risPort;
	}

	public AE GetSecondaryAE()
	{
		return secondaryAE;
	}

	public int getSOAPPort()
	{
		return soapPort;
	}

	public String getSyncHost()
	{
		return syncHost;
	}

	public List getSyncList()
	{
		return syncList;
	}

	public int getSyncPort()
	{
		return syncPort;
	}

	public boolean IsBackup()
	{
		return isBackup;
	}

	public boolean IsBatCart()
	{
		return isBatCart;
	}

	public boolean IsEcho()
	{
		return isEcho;
	}

	// RAL - Added for importer
	public boolean IsImporterListener()
	{
		return isImporterListener;
	}

	public boolean IsPrimary()
	{
		return isPrimary;
	}

	public boolean IsRIS()
	{
		return isRIS;
	}

	public boolean IsSecondary()
	{
		return isSecondary;
	}

	public boolean IsSink()
	{
		return isSink;
	}

	public boolean IsSOAP()
	{
		return isSOAP;
	}

	public boolean IsSync()
	{
		return isSync;
	}

	public void run()
	{
		try
		{
			log("Version: " + Util.getManifestVersion());
			log("new LocalStoreDicomServer" + new Date());
			if (IsSecondary())
			{
				Util.sleep(10 * 1000); // give time to startup
				try
				{
					Ping p = new Ping(GetPrimaryAE(), 1);
					p.run();
				}
				catch (Exception e)
				{
					Log.log("could not ping primary");
					Util.sleep(2 * 60 * 1000); // give some more time to
					// startup
				}
			}
			dicomServer = new LocalStoreDicomServer(this);
			Thread dicomServerThread = new Thread(dicomServer);
			dicomServerThread.setName("DicomServerThread");
			if (IsSync())
			{
				Sync.init(getSyncPort());
				for (int i = 0; i < syncList.size(); i++)
				{
					java.net.InetSocketAddress isa = (InetSocketAddress) syncList.get(i);
					Sync.addDest(isa.getHostName(), isa.getPort());
				}
			}
			log("start dicomserverthread");
			dicomServerThread.start();
			// watch for any races here
			// Util.startDaemonThread(new SyncProcess());
			Util.startDaemonThread(new UtilProcess());
			Util.startDaemonThread(new WebProcess());
			InternalSelfCheck beacon = new InternalSelfCheck();
			beacon.setUrlStrings((String[]) GlobalProperties.get().get("beaconURLs"));
			beacon.setAdditionalUrl("PACS_" + AEMap.getDefault().getName());
			beacon.setWaitTimeInMinutes((Integer) GlobalProperties.get().get("beaconWaitTime"));
			Util.startDaemonThread(beacon);
			for (int i = 0; i < forwardThreadCount; i++)
				Util.startDaemonThread(new ForwardProcess());
			if (IsEcho())
				Util.startDaemonThread(new Simulator());
			// create batcart instance
			if (IsBatCart())
				BatCart.init(getBatCartPort(), getBatCartAltPort());
			if (IsRIS())
				RISProcess.init(getRISPort());
			if (IsSOAP())
				SoapServer.init(getSOAPPort());
			// RAL - Added for importer
			if (IsImporterListener())
				ImporterListener.init(ImporterListener.ListenTypes.DATABASE_LISTENER, importerListenerPollInterval, importerListenerDeleteRecord);
			// WebService.init(8888);
			// StudyDup.test();
			// MirrorClient.startInstance("PCSERVER1");
			long nextTime = 0;
			int lastMax = -1;
			long delay = 5000;
			for (;;)
			{
				boolean dirty = false;
				int free = (calc(Runtime.getRuntime().freeMemory()));
				int max = (calc(Runtime.getRuntime().maxMemory()));
				int total = (calc(Runtime.getRuntime().totalMemory()));
				if (max != lastMax)
					dirty = true;
				else if (free <= 1 && max == total)
					dirty = true;
				freeMax.add(free);
				lastMax = max;
				if (dirty || System.currentTimeMillis() > nextTime)
				{
					Log.rotate();
					String msg = "### Free:" + freeMax + " Total:" + total + " Max:" + max + " Sess:" + DicomServerSession.sessionCount + " c="
							+ DicomServerSession.sessionCount.getTotalAdditions() + " t=" + Thread.activeCount() + " bq=" + ForwardProcess.bqueue.size();
					Log.log(msg + " " + new Date());
					nextTime = System.currentTimeMillis() + delay;
					if (delay < 1000 * 60 * 60)
						delay += 5 * 1000;
				}
				Util.sleep(5000);
			}
			// t.join();
		}
		catch (Exception e)
		{
			Log.log("LocalStoreDicomServer.run", e);
		}
		finally
		{
			log("Exit finally");
		}
	}

	public void updateServerBean()
	{
		log("updateServerBean");
		try
		{
			XML storeXML = getCurrentAdminStore();
			serverBean = new ServerBean(storeXML);
			if (serverBean != null)
				DicomStore.get().setServerBean(serverBean);
		}
		catch (Exception e)
		{
			log("could not updateServerBean " + e, e);
		}
	}

	public static void main(String[] args)
	{
		try
		{
			log("Version: " + Util.getManifestVersion());
			if (args.length != 2)
			{
				log("usage: localstore conf_file_path.xml target");
				System.exit(1);
			}
			System.out.println("localstore conf=" + args[0] + " targ=" + args[1]);
			CONFIG_FILE = new File(args[0]);
			XMLConfigFile configFile = new XMLConfigFile(CONFIG_FILE, args[1]);
			CONFIG_ROOT = CONFIG_FILE.getParentFile();
			Figaro.setRoot(CONFIG_ROOT);
			Figaro.checkIt();
			Log.vlog("" + configFile.getXML());
			DATA_ROOT = new File(configFile.getXML().get("storage/root"));
			// FileUtil.initLibDirForJConfig(new File(new
			// File(args[0]).getParentFile().getParentFile(), "lib"));
			XML config = XMLConfigFile.getDefault().getXML();
			Integer tmpInt = 1200;
			String s = config.get("options/daysToKeepLog");
			if (s != null)
			{
				try
				{
					tmpInt = Integer.parseInt(s);
				}
				catch (Exception e)
				{
					tmpInt = 1200;
				}
			}
			GlobalProperties.get().put("daysToKeepLog", tmpInt);
			tmpInt = 120;
			s = config.get("options/daysToKeepVlog");
			if (s != null)
			{
				try
				{
					tmpInt = Integer.parseInt(s);
				}
				catch (Exception e)
				{
					tmpInt = 120;
				}
			}
			GlobalProperties.get().put("daysToKeepVlog", tmpInt);
			tmpInt = 1;
			s = config.get("options/defaultLogVerbosity");
			if (s != null)
			{
				try
				{
					tmpInt = Integer.parseInt(s);
				}
				catch (Exception e)
				{
					tmpInt = 1;
				}
			}
			GlobalProperties.get().put("defaultLogVerbosity", tmpInt);
			Log.init(new File(DATA_ROOT, "log"), "ls");
			Log.log("Start at " + new Date());
			LocalStore localstore = new LocalStore();
			// Connection con = null;
			//
			// con = DriverManager.getConnection(
			// "jdbc:mysql://10.80.1.62/metafusion?user=matt&password=matt");
			Log.log("localstore.run");
			localstore.run();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
