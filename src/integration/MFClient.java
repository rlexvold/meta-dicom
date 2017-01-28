package integration;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

import net.metafusion.pdfutils.PdfUtils;
import net.metafusion.util.GlobalProperties;
import acme.util.CompressionStream;
import acme.util.Util;

public class MFClient implements Runnable
{
	static MFClient				instance;
	private static String		richMediaRsyncModule	= "richmedia";
	private Object				args[]					= null;
	protected boolean			async					= false;
	private volatile boolean	done					= false;
	public String				host					= "";
	boolean						isLogout				= false;
	public String				name					= "";
	public String				password				= "";
	public int					port					= 4007;
	private Object				result					= null;
	private Socket				s						= null;
	long						sessionID				= 0;
	private static Integer		bytesInAMegabyte		= 1048576;
	private int					chunkSize				= 10 * bytesInAMegabyte;

	static MFClient get()
	{
		return instance;
	}

	static public void log(String s)
	{
		System.out.println(s);
	}

	static public void log(String s, Exception e)
	{
		System.out.println(s + e);
	}

	public static void main(String[] args)
	{
		MFClient c = new MFClient();
		c.test();
	}

	public MFClient()
	{
		calcChunkSize();
	}

	public MFClient(String host, String port, String name, String password)
	{
		init(host, Integer.parseInt(port), name, password);
	}

	public MFClient(String host, int port, String name, String password)
	{
		init(host, port, name, password);
	}

	public void init(String host, int port, String name, String password)
	{
		this.host = host;
		this.port = port;
		this.name = name;
		this.password = password;
		calcChunkSize();
	}

	private void calcChunkSize()
	{
		chunkSize = 10 * bytesInAMegabyte;
		Integer tmp = (Integer) GlobalProperties.get().get("rsyncChunkSizeMB");
		if (tmp != null)
			chunkSize = tmp * bytesInAMegabyte;
	}

	public MFStudy attach(long id, String name, String label, byte[] data)
	{
		send(new Object[] { "attach", new MFLong(id), name, label, data });
		Object o = getObject(new MFStudy());
		if (o instanceof MFStudy)
			return (MFStudy) o;
		return new MFStudy();
		// return (MFStudy)getObject(new MFStudy());
	}

	public void authFailHook()
	{
		log("connect auth");
		throw new RuntimeException("authentication fail");
	}

	public String cfind(String sourceAE, String searchArgs, String returnArgs)
	{
		SearchBeanExtended sb = new SearchBeanExtended();
		String returnString = "ok;";
		String[] args = searchArgs.split(",");
		for (int i = 0; i < args.length; i++)
		{
			String[] cmd = args[i].split("=");
			if (cmd[0].equalsIgnoreCase("accessionnumber"))
			{
				sb.setAccessionNum(cmd[1]);
			}
			else if (cmd[0].equalsIgnoreCase("firstname"))
			{
				sb.setFirstName(cmd[1]);
			}
			else if (cmd[0].equalsIgnoreCase("lastname"))
			{
				sb.setLastName(cmd[1]);
			}
			else if (cmd[0].equalsIgnoreCase("modality"))
			{
				sb.setModality(cmd[1]);
			}
			else if (cmd[0].equalsIgnoreCase("fromdate"))
			{
				sb.setFromDate(cmd[1]);
			}
			else if (cmd[0].equalsIgnoreCase("todate"))
			{
				sb.setToDate(cmd[1]);
			}
			else if (cmd[0].equalsIgnoreCase("patientid"))
			{
				sb.setPatientID(cmd[1]);
			}
			else if (cmd[0].equalsIgnoreCase("referringphysician"))
			{
				sb.setReferringPhysician(cmd[1]);
			}
			else if (cmd[0].equalsIgnoreCase("studydescription"))
			{
				sb.setStudyDescription(cmd[1]);
			}
			else if (cmd[0].equalsIgnoreCase("institution"))
			{
				sb.setInstitutionName(cmd[1]);
			}
		}
		send(new Object[] { "cfind", sourceAE, sb });
		MFExtendedStudy[] results = (MFExtendedStudy[]) getObject("");
		String[] returns = returnArgs.split(",");
		for (int i = 0; i < results.length; i++)
		{
			System.out.println(i + 1 + ": " + results[i].studyUID);
			for (int j = 0; j < returns.length; j++)
			{
				boolean validEntry = true;
				if (returns[j].equalsIgnoreCase("patientID"))
				{
					returnString += results[i].patientID;
				}
				else if (returns[j].equalsIgnoreCase("studyUID"))
				{
					returnString += results[i].studyUID;
				}
				else if (returns[j].equalsIgnoreCase("date"))
				{
					returnString += results[i].dateTime;
				}
				else if (returns[j].equalsIgnoreCase("studyIDString"))
				{
					returnString += results[i].studyIDString;
				}
				else if (returns[j].equalsIgnoreCase("description"))
				{
					returnString += results[i].description;
				}
				else if (returns[j].equalsIgnoreCase("modalities"))
				{
					returnString += results[i].modality;
				}
				else if (returns[j].equalsIgnoreCase("referringPhysicianName"))
				{
					returnString += results[i].referrer;
				}
				else if (returns[j].equalsIgnoreCase("stationName"))
				{
					returnString += results[i].station;
				}
				else if (returns[j].equalsIgnoreCase("institutionName"))
				{
					returnString += results[i].institution;
				}
				else if (returns[j].equalsIgnoreCase("state"))
				{
					returnString += results[i].state;
				}
				else if (returns[j].equalsIgnoreCase("reader"))
				{
					returnString += results[i].reader;
				}
				else if (returns[j].equalsIgnoreCase("version"))
				{
					returnString += results[i].version;
				}
				else if (returns[j].equalsIgnoreCase("accessionNumber"))
				{
					returnString += results[i].accessionNumber;
				}
				else
				{
					validEntry = false;
				}
				if (validEntry && j < returns.length - 1)
				{
					returnString += ",";
				}
			}
			returnString += ";";
		}
		return returnString;
	}

	public void close()
	{
		try
		{
			s.close();
		}
		catch (Exception e)
		{
			;
		}
		s = null;
	}

	public boolean cmove(String sourceAE, String destAE, String moveList)
	{
		send(new Object[] { "cmove", sourceAE, destAE, moveList });
		return getBoolean();
	}

	public void connectFailHook()
	{
		log("connect fail");
		throw new RuntimeException("connection fail");
	}

	public String createReportPdf(String sourceFiles, String destFile)
	{
		String[] fileList = sourceFiles.split(",");
		try
		{
			for (int i = 0; i < fileList.length; i++)
			{
				fileList[i] = PdfUtils.ConvertFileToPdf(fileList[i]);
			}
			PdfUtils.concatenate(fileList, destFile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "err;" + e.getMessage();
		}
		return "ok;";
	}

	public boolean downloadStudies(String type, String args[], String host, String port)
	{
		send(new Object[] { "downloadStudies", type, args, host, port });
		return getBoolean();
	}

	public boolean exceptionHook(Object result)
	{
		// log("exceptionHook " + result);
		if (result instanceof RuntimeException)
			throw (RuntimeException) result;
		if (result instanceof Exception)
			throw new RuntimeException("" + result);
		if (result instanceof MFError)
			throw new RuntimeException(((MFError) result).msg);
		return false;
	}

	public boolean getBoolean()
	{
		try
		{
			if (exceptionHook(result))
				return false;
		}
		catch (Exception e)
		{
			return false;
		}
		return result != null ? ((MFBoolean) result).b : false;
	}

	synchronized public void sendFileFromServer(MFFileInfo fileInfo, File destFile) throws Exception
	{
		boolean append = false;
		int offset = 0;
		for (;;)
		{
			send(new Object[] { "sendFileFromServer", fileInfo, new MFInteger(offset), new MFInteger(chunkSize) });
			byte[] input = (byte[]) getObject(new byte[0]);
			if (input == null || input.length == 0)
				break;
			input = CompressionStream.read(input);
			Util.writeFile(input, destFile, append);
			offset += chunkSize;
			append = true;
		}
	}

	synchronized public void sendFileToServer(MFFileInfo fileInfo, File src) throws Exception
	{
		int offset = 0;
		for (;;)
		{
			byte[] input = Util.readFile(src.getAbsolutePath(), offset, chunkSize);
			if (input == null)
				break;
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			CompressionStream.write(input, dos);
			send(new Object[] { "sendFileToServer", fileInfo, baos.toByteArray(), new MFInteger(offset) });
			offset += chunkSize;
		}
	}

	public byte[] getFile(String uid, String name, String type)
	{
		send(new Object[] { "getFile", uid, name + ";" + type + ";atx" });
		return (byte[]) getObject(new byte[0]);
	}

	public String getFileList(String uid)
	{
		send(new Object[] { "getFileList", uid });
		return (String) getObject("");
	}

	public String getLocalTime()
	{
		send(new Object[] { "getLocalTime" });
		return "ok;" + (String) getObject("") + ";\n";
	}

	public Object getObject(Object failValue)
	{
		try
		{
			if (exceptionHook(result))
				return failValue;
		}
		catch (Exception e)
		{
			return failValue;
		}
		if (result == null || result instanceof Exception)
			return failValue;
		return result;
	}

	public MFUser[] getUserList()
	{
		send(new Object[] { "getuserlist" });
		return (MFUser[]) getObject(new MFUser[0]);
	}

	public String[] getUserNameList()
	{
		send(new Object[] { "getusernamelist" });
		return (String[]) getObject(new String[0]);
	}

	public MFIdle idle()
	{
		send2(new Object[] { "idle" });
		if (result == null)
			return new MFIdle();
		Object o = getObject(new MFIdle());
		if (o == null || !(o instanceof MFIdle))
			return new MFIdle();
		return (MFIdle) o;
	}

	synchronized public boolean isDone()
	{
		return done;
	}

	public MFStudy loadStudy(long studyid)
	{
		send(new Object[] { "loadstudy", new MFLong(studyid) });
		Object o = getObject(new MFStudy());
		if (o instanceof MFStudy)
			return (MFStudy) o;
		return new MFStudy();
		// return (MFStudy)getObject(new MFStudy());
	}

	public void logout()
	{
		try
		{
			isLogout = true;
			send(new Object[] { "logout" });
		}
		finally
		{
			isLogout = false;
			sessionID = 0;
		}
	}

	public void needAuthHook()
	{
	}

	public String patientCleanup(String ris, String patientList)
	{
		String[] arg = patientList.split(":");
		if (arg[0].equalsIgnoreCase("all"))
		{
			send(new Object[] { "patientCleanupAll", ris });
		}
		else if (arg[0].equalsIgnoreCase("patientuid"))
		{
			send(new Object[] { "patientCleanupByPatient", ris, arg[1].trim() });
		}
		else if (arg[0].equalsIgnoreCase("studyuid"))
		{
			send(new Object[] { "patientCleanupByStudy", ris, arg[1].trim() });
		}
		else
			return "err;invalid patientCleanup command: " + patientList;
		return "ok;" + (String) getObject("") + ";\n";
	}

	//
	// commands...
	//
	public boolean ping()
	{
		send(new Object[] { "ping" });
		return getBoolean();
	}

	public String publishAttachment(String uid, String name, byte[] data)
	{
		send(new Object[] { "publishAttachment", uid, name, data });
		return (String) getObject("");
	}

	public String publishStudy(String userId, String uid)
	{
		send(new Object[] { "publishStudy", userId, uid });
		return (String) getObject("");
	}

	public boolean putFile(String uid, String name, String type, byte[] data)
	{
		send(new Object[] { "putFile", uid, name + ";" + type + ";atx", data });
		return getBoolean();
	}

	public MFStudy[] query(String s, SearchBean sb)
	{
		send(new Object[] { "query", s, sb });
		Object o = getObject(new MFStudy[0]);
		if (o instanceof MFStudy[])
			return (MFStudy[]) o;
		return new MFStudy[0];
	}

	public byte[] readAttachedFile(long studyid, long id)
	{
		send(new Object[] { "readattachedfile", new MFLong(studyid), new MFLong(id) });
		return (byte[]) getObject(new byte[0]);
	}

	public String referrerAdd(String name, String passwd)
	{
		send(new Object[] { "referrerAdd", name, passwd });
		return (String) getObject("");
	}

	public String referrerDelete(String userId)
	{
		send(new Object[] { "referrerDelete", userId });
		return (String) getObject("");
	}

	public String referrerModify(String userId, String name, String passwd)
	{
		send(new Object[] { "referrerModify", userId, name, passwd });
		return (String) getObject("");
	}

	public void removeReview(long id, String userName)
	{
		send(new Object[] { "removereview", new MFLong(id), userName });
		getBoolean();
	}

	public void requestReview(long id, String userName)
	{
		send(new Object[] { "requestreview", new MFLong(id), userName });
		getBoolean();
	}

	public void run()
	{
		boolean failed = false;
		s = null;
		MFOutputStream oos = null;
		MFInputStream ois = null;
		log("MFClient.run");
		setDone(false);
		try
		{
			for (;;)
			{
				s = null;
				if (failed || host.length() == 0 || password.length() == 0)
				{
					if (isLogout)
						return;
					needAuthHook();
				}
				try
				{
					log("connect " + host + " " + port);
					s = new Socket(host, port);
					// s.setSoLinger(true,5);
				}
				catch (IOException e)
				{
					log("connection fail " + e);
				}
				if (s == null)
				{
					if (isLogout)
						return;
					connectFailHook();
					failed = true;
					continue;
				}
				log("write sess" + sessionID);
				s.setSoTimeout(30 * 60 * 1000);
				oos = new MFOutputStream((s.getOutputStream()));
				ois = new MFInputStream((s.getInputStream()));
				oos.writeLong(sessionID);
				oos.flush();
				log("read resp" + sessionID);
				long respID = ois.readLong();
				if (respID == 0)
				{
					log("write name passwd");
					oos.writeObject(name);
					oos.writeObject(password);
					oos.flush();
					respID = ois.readLong();
					if (respID == 0)
					{
						log("auth fail");
						authFailHook(); // auth failure
						failed = true;
						try
						{
							if (s != null)
								s.close();
						}
						catch (Exception e)
						{
							;
						}
						s = null;
						continue;
					}
				}
				sessionID = respID;
				break;
			}
			log("write args");
			for (Object element : args)
				oos.writeObject(element);
			oos.flush();
			s.setSoTimeout(60 * 1000);
			log("readObject");
			result = ois.readObject();
			log("result=" + result);
		}
		catch (Exception e)
		{
			log("run " + args[0], e);
			result = e;
		}
		finally
		{
			try
			{
				if (s != null)
					s.close();
			}
			catch (Exception e)
			{
				;
			}
			s = null;
			setDone(true);
		}
		log("MFClient.run exit");
	}

	public void run2()
	{
		s = null;
		MFOutputStream oos = null;
		MFInputStream ois = null;
		log("MFClient.run");
		setDone(false);
		try
		{
			for (;;)
			{
				s = null;
				if (host.length() == 0 || password.length() == 0)
					return;
				try
				{
					s = new Socket(host, port);
					// s.setSoLinger(true,5);
				}
				catch (IOException e)
				{
				}
				if (s == null)
					return;
				s.setSoTimeout(10 * 1000);
				oos = new MFOutputStream((s.getOutputStream()));
				ois = new MFInputStream((s.getInputStream()));
				oos.writeLong(sessionID);
				oos.flush();
				long respID = ois.readLong();
				if (respID == 0)
				{
					oos.writeObject(name);
					oos.writeObject(password);
					oos.flush();
					respID = ois.readLong();
					if (respID == 0)
						return;
				}
				sessionID = respID;
				// sessionID = respID;
				break;
			}
			for (Object element : args)
				oos.writeObject(element);
			oos.flush();
			s.setSoTimeout(10 * 1000);
			result = ois.readObject();
		}
		catch (Exception e)
		{
			log("run2 " + args[0], e);
			result = e;
		}
		finally
		{
			try
			{
				if (s != null)
					s.close();
			}
			catch (Exception e)
			{
				;
			}
			s = null;
			setDone(true);
		}
		log("MFClient.run2 exit");
	}

	public void send(Object[] args)
	{ // , File inFile, File outFile) {
		log("admin(" + host + ":" + port + "):" + args[0] + " async=" + async);
		this.args = args;
		setDone(false);
		s = null;
		result = null;
		args = null;
		run();
	}

	public void send2(Object[] args)
	{ // , File inFile, File outFile) {
		log("admin(" + host + ":" + port + "):" + args[0] + " async=" + async);
		this.args = args;
		setDone(false);
		s = null;
		result = null;
		args = null;
		run2();
	}

	public void sendMessage(String userName, String title, String text)
	{
		send(new Object[] { "sendmessage", userName, title, text });
		getBoolean();
	}

	public boolean sendStudy(long studyid, String ae)
	{
		send(new Object[] { "sendstudy", new MFLong(studyid), ae });
		return getBoolean();
	}

	public boolean sendStudyUID(String studyuid, String ae)
	{
		send(new Object[] { "sendstudyuid", studyuid, ae });
		return getBoolean();
	}

	synchronized public void setDone(boolean done)
	{
		this.done = done;
	}

	public void test()
	{
		MFClient client = new MFClient();
		boolean p = client.ping();
		log("" + p);
		client.downloadStudies("daily", new String[] { "a", "b", "b" }, "localhost", "127");
		MFUser[] user = getUserList();
		String[] unl = getUserNameList();
		MFStudy mfs;
		MFStudy[] sl = query("query", new SearchBean());
		attach(sl[0].studyID, "foo", "foo file", new byte[] { 1, 2, 3, 4, 5, 6, 7 });
		mfs = loadStudy(sl[0].studyID);
		byte[] b = readAttachedFile(mfs.studyID, mfs.attachments[0].id);
		update(mfs);
		requestReview(mfs.studyID, "bar");
		logout();
		p = client.ping();
		log("" + p);
	}

	public String unpublishAttachment(String uid, String name)
	{
		send(new Object[] { "unpublishAttachment", uid, name });
		return (String) getObject("");
	}

	public String unpublishStudy(String userId, String uid)
	{
		send(new Object[] { "unpublishStudy", userId, uid });
		return (String) getObject("");
	}

	public MFStudy update(MFStudy study)
	{
		send(new Object[] { "update", study });
		Object o = getObject(new MFStudy());
		if (o instanceof MFStudy)
			return (MFStudy) o;
		return new MFStudy();
		// return (MFStudy)getObject(new MFStudy());
	}

	public void waitHook()
	{
	}
}