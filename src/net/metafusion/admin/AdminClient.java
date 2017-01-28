package net.metafusion.admin;

import integration.SearchBean;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import acme.util.Util;

public class AdminClient implements Runnable
{
	static AdminClient	instance;

	static AdminClient get()
	{
		return instance;
	}

	static public void log(String s)
	{
		Util.log(s);
	}

	static public void log(String s, Exception e)
	{
		Util.log(s, e);
	}

	public static void main(String[] args)
	{
		AdminClient c = new AdminClient("localhost", 5105);
		// List configs = c.getOlderConfigs();
		// log(""+configs);
		// String config = c.getConfig("");
		// List l = c.search(new SearchBean());
		// log(""+l);
		List list = new ArrayList();
		list.add("1.2.840.113619.2.134.1762872355.2791.1103295873.519");
		File f = new File("C:\\temp.zip");
		boolean b = c.archiveStudyList(list, f);
		log("" + b);
	}

	static public void vlog(String s)
	{
		Util.log(s);
	}
	private Object				args[]	= null;
	protected boolean			async	= false;
	private volatile boolean	done	= false;
	private String				host;
	private File				inFile	= null;
	private InputStream			is		= null;
	private OutputStream		os		= null;
	private ObjectInputStream	ois		= null;
	private ObjectOutputStream	oos		= null;
	private File				outFile	= null;
	private int					port;
	private Object				result	= null;
	private Socket				s		= null;

	public AdminClient(String host, int port)
	{
		log("new AdminClient " + host + " " + port);
		this.host = host;
		this.port = port;
	}

	public boolean archiveStudyList(List studyUIDList, File f)
	{
		send(new Object[] { "archiveStudyList", studyUIDList }, null, f);
		return getBoolean();
	}

	public StudiesInfoBean burnStudyList(List studyUIDList)
	{
		send(new Object[] { "burnStudyList", studyUIDList }, null, null);
		return (StudiesInfoBean) getObject();
	}

	public void close()
	{
		// Util.safeClose(ois);
		// Util.safeClose(oos);
		// ois = null;
		// oos = null;
		Util.safeClose(os);
		Util.safeClose(is);
		Util.safeClose(s);
		is = null;
		os = null;
		s = null;
	}

	public List deleteStudyList(List studyUIDList)
	{
		send(new Object[] { "deleteStudyList", studyUIDList }, null, null);
		return (List) getObject();
	}

	public String doDiagnostic(String cmd, String argument1, String argument2)
	{
		send(new Object[] { "doDiagnostic", cmd, argument1, argument2, argument2 }, null, null);
		return (String) getObject();
	}

	public boolean establish(String password)
	{
		send(new Object[] { "establish", password != null ? password : "" }, null, null);
		return getBoolean();
	}

	private boolean getBoolean()
	{
		if (result instanceof RuntimeException)
			throw (RuntimeException) result;
		if (result instanceof Exception)
			throw new RuntimeException((Exception) result);
		return result != null ? ((Boolean) result).booleanValue() : false;
	}

	public long[] getCapacityFreeSpace()
	{
		send(new Object[] { "getCapacityFreeSpace" }, null, null);
		return (long[]) getObject();
	}

	public String getConfig(String name)
	{
		send(new Object[] { "getConfig", name }, null, null);
		return (String) getObject();
	}

	public String[] getDiagnostics()
	{
		send(new Object[] { "getDiagnostics" }, null, null);
		return (String[]) getObject();
	}

	public String getExpiration()
	{
		send(new Object[] { "getExpiration" }, null, null);
		return (String) getObject();
	}

	public String[] getFreePerUsedSpace()
	{
		send(new Object[] { "getFreePerUsedSpace" }, null, null);
		return (String[]) getObject();
	}

	public boolean getImageOnDisk(String imageUID, File f)
	{
		send(new Object[] { "getImageOnDisk", imageUID }, null, f);
		return getBoolean();
	}

	public boolean getLogFile(File f)
	{
		send(new Object[] { "getLogFile" }, null, f);
		return getBoolean();
	}

	private Object getObject()
	{
		if (result instanceof RuntimeException)
			throw (RuntimeException) result;
		if (result instanceof Exception)
			throw new RuntimeException((Exception) result);
		return result;
	}

	public List getOlderConfigs()
	{
		send(new Object[] { "getOlderConfigs" }, null, null);
		return (List) getObject();
	}

	public StudiesInfoBean getStudyListInfo(List studyUIDList)
	{
		send(new Object[] { "getStudyListInfo", studyUIDList }, null, null);
		return (StudiesInfoBean) getObject();
	}

	public String getVersion()
	{
		send(new Object[] { "getVersion" }, null, null);
		return (String) getObject();
	}

	synchronized public boolean isDone()
	{
		return done;
	}

	protected void onRun()
	{
		log("AdminClient.onRun()");
		while (!isDone())
		{
			log("" + done);
			Util.sleep(250);
		}
		log("AdminClient.onRun() exit");
	}

	public void run()
	{
		try
		{
			log("AdminClient.run");
			setDone(false);
			log("AdminClient::Connect " + host + " " + port);
			s = new Socket(host, port);
			s.setSoTimeout(200000);
			// s.setSoLinger(true, 5);
			os = s.getOutputStream();
			is = s.getInputStream();
			os.write('Y'); // before oos
			os.flush();
			
			//NOTE:  The next 3 lines must be in this order, or it will hang waiting or an OutputStream
			ois = new ObjectInputStream(new BufferedInputStream(is));
			oos = new ObjectOutputStream(new BufferedOutputStream(os));
			oos.flush();
			for (int i = 0; i < args.length; i++)
			{
				oos.writeObject(args[i]);
			}
			if (inFile != null)
			{
				FileInputStream fis = null;
				try
				{
					fis = new FileInputStream(inFile);
					Util.copyStream(fis, oos);
				}
				finally
				{
					Util.safeClose(fis);
				}
			}
			oos.flush();
			// s.shutdownOutput(); // todo: is this needed no j# equivalent
			log("AdminClient.run ObjectInputStream");
			result = ois.readObject();
			if (outFile != null)
			{
				FileOutputStream fos = null;
				try
				{
					fos = new FileOutputStream(outFile);
					Util.copyStream(ois, fos);
					Util.safeClose(fos);
				}
				finally
				{
					Util.safeClose(fos);
				}
			}
			log("AdminClient.run shutdownInput");
			// s.shutdownOutput();
			// s.shutdownInput(); // todo: is this needed no j# equivalent
		}
		catch (Exception e)
		{
			if (e instanceof ConnectException || e.getCause() instanceof ConnectException)
				log("AdminClient ConnectException");
			else
				log("run " + args[0], e);
			result = e;
		}
		finally
		{
			close();
			setDone(true);
		}
		log("AdminClient.run exit");
	}

	public boolean saveAdmin(String adminXML)
	{
		send(new Object[] { "saveAdmin", adminXML }, null, null);
		return getBoolean();
	}

	public List search(SearchBean sb)
	{
		send(new Object[] { "search", sb }, null, null);
		return (List) getObject();
	}

	public StudiesDumpBean selectStudiesUpdatedSince(Timestamp dateLastImage, String lastUID)
	{
		send(new Object[] { "selectStudiesUpdatedSince", dateLastImage, lastUID }, null, null);
		return (StudiesDumpBean) getObject();
	}

	public void send(Object[] args, File inFile, File outFile)
	{
		log("admin(" + host + ":" + port + "):" + args[0] + " async=" + async);
		this.args = args;
		this.inFile = inFile;
		this.outFile = outFile;
		setDone(false);
		s = null;
		os = null;
		is = null;
		inFile = null;
		outFile = null;
		result = null;
		args = null;
		if (async)
		{
			Thread r = new Thread(this);
			r.start();
			onRun();
		}
		else
			run();
	}

	public boolean sendStudyList(String aeString, List studyUIDList)
	{
		send(new Object[] { "sendStudyList", aeString, studyUIDList }, null, null);
		return getBoolean();
	}

	synchronized public void setDone(boolean done)
	{
		this.done = done;
	}

	public boolean verify(String aeString)
	{
		send(new Object[] { "verify", aeString }, null, null);
		return getBoolean();
	}
}