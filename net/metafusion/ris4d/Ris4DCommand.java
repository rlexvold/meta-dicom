package net.metafusion.ris4d;

import integration.MFClient;
import integration.xmlrpc.XmlRpcClient;
import integration.xmlrpc.XmlRpcStruct;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import net.metafusion.medicare.hic.HICCA;
import net.metafusion.ptburn.PTBurner;
import net.metafusion.ris4d.commands.ArchiveCommand;
import net.metafusion.ris4d.commands.AsyncCommand;
import net.metafusion.ris4d.commands.AsyncWrapperCommand;
import net.metafusion.ris4d.commands.DemoCommand;
import net.metafusion.ris4d.commands.MedicareCommand;
import net.metafusion.ris4d.commands.RichMediaCommand;
import net.metafusion.ris4d.commands.StudyCommand;
import net.metafusion.ris4d.commands.ThumbnailsCommand;
import net.metafusion.util.InternalSelfCheck;
import acme.util.FileUtil;
import acme.util.StringUtil;
import acme.util.Util;
import acme.util.ZipUtil;

public class Ris4DCommand
{
	class Downloader implements Runnable
	{
		File			file	= null;
		Socket			s		= null;
		ServerSocket	ss		= null;

		Downloader() throws Exception
		{
			ss = new ServerSocket(0);
		}

		public void close()
		{
			Util.safeClose(ss);
			Util.safeClose(s);
		}

		File getFile()
		{
			return file;
		}

		public void run()
		{
			InputStream is = null;
			File temp = null;
			FileOutputStream fos = null;
			try
			{
				ss.setSoTimeout(60 * 1000 * 1000);
				s = ss.accept();
				is = s.getInputStream();
				temp = File.createTempFile("dicomdir", ".zip");
				fos = new FileOutputStream(temp);
				Util.copyStream(is, fos);
				log("copyStream done");
				file = temp;
				temp = null;
			}
			catch (IOException e)
			{
				log("Downloader caught " + e);
				Util.safeDelete(temp);
				temp = null;
			}
			finally
			{
				Util.safeClose(fos);
				Util.safeClose(s);
				Util.safeClose(ss);
			}
		}
	}
	static HashMap							clientMap				= new HashMap();
	private static HashMap<String, String>	commandMap				= new HashMap<String, String>();
	static int								csvDelimiter			= ',';
	static MFClient							defClient				= null;
	static File								efautoclFile			= new File("c:\\efilmris\\efAutoCl.exe");
	static File								expressApp				= new File("C:\\Program Files\\NCH Swift Sound\\Express\\express.exe");
	static File								expressClientFile		= new File("c:\\efilmris\\expressclient\\Release\\expressclient.exe");
	static String							localStoreHost			= "localhost";
	static String							localStorePassword		= "matt";
	static String							localStoreRisPort		= "4109";
	static String							localStoreUserName		= "matt";
	private static String					osirixCheckStudyString	= "checkIfStudyExistsLocally";
	private static String					osirixXmlRpcUrl			= "http://localhost:9080";
	static File								scribeApp				= new File("C:\\Program Files\\NCH Swift Sound\\Scribe\\scribe.exe");

	static String doit(String str) throws Exception
	{
		log("<" + str);
		Ris4DCommand cmd = new Ris4DCommand(str);
		String res = cmd.newGetResult();
		log("<" + res);
		return res;
	}

	public static int exec(boolean wait, File app, String args) throws Exception
	{
		return exec(wait, app.getAbsolutePath() + " " + args);
	}

	public String[] getArgs()
	{
		return args;
	}

	public void setArgs(String[] args)
	{
		this.args = args;
	}

	public MFClient getClient()
	{
		return client;
	}

	public void setClient(MFClient client)
	{
		this.client = client;
	}

	public String getCmdLine()
	{
		return cmdLine;
	}

	public void setCmdLine(String cmdLine)
	{
		this.cmdLine = cmdLine;
	}

	public static int exec(boolean wait, File app, String args[]) throws Exception
	{
		String command = app.getAbsolutePath();
		if (args != null)
		{
			for (String s : args)
			{
				if (s == null || s.length() == 0)
				{
					continue;
				}
				if (s.indexOf(" ") != -1 && s.indexOf("\"") == -1)
				{
					command += " \"" + s + "\"";
				}
				else
				{
					command += " " + s;
				}
			}
		}
		return exec(wait, command);
	}

	public static int exec(boolean wait, String command) throws Exception
	{
		log("exec: " + command);
		Process p = Runtime.getRuntime().exec(command);
		if (wait)
		{
			int rv = p.waitFor();
			return rv;
		}
		return 0;
	}

	static void init(String host, String port, String name, String password)
	{
		init(host, port, name, password, osirixXmlRpcUrl);
	}

	static void init(String host, String port, String name, String password, String osirixUrl)
	{
		log("Ris4DCommand.init " + host + " " + port);
		localStoreHost = host;
		localStoreRisPort = port;
		localStoreUserName = name;
		localStorePassword = name;
		osirixXmlRpcUrl = osirixUrl;
		defClient = new MFClient(host, port, name, password);
		clientMap.put(host + ":" + port, defClient);
		commandMap = ThumbnailsCommand.register(commandMap);
		commandMap = AsyncCommand.register(commandMap);
		commandMap = RichMediaCommand.register(commandMap);
		commandMap = StudyCommand.register(commandMap);
		commandMap = ArchiveCommand.register(commandMap);
		commandMap = DemoCommand.register(commandMap);
		commandMap = MedicareCommand.register(commandMap);
	}

	static void log(String s)
	{
		Util.log(s);
	}

	public static void open(String path) throws Exception
	{
		String[] cmd = new String[3];
		cmd[0] = "cmd.exe";
		cmd[1] = "/C";
		cmd[2] = path;
		Runtime rt = Runtime.getRuntime();
		System.out.println("opening " + cmd[0] + " " + cmd[1] + " " + cmd[2]);
		Process proc = rt.exec(cmd);
	}

	// <efilm arg="" study="" />
	// <scribe arg"" file="" />
	// <express arg="" text="" />
	static void searchOsirix(String arg)
	{
		String s = "tell application \"OsiriX\" \n" + "activate  \n" + "end tell \n" + "tell application \"System Events\" \n" + "tell process \"OsiriX\" \n"
				+ "tell menu item \"Show Database Window\" of menu 1 of menu bar item \"File\" of menu bar 1 \n" + "click \n" + "end tell \n"
				+ "set value of text field 1 of group 1 of tool bar 1 of window 1 to \"" + arg + "\" \n"
				+ "perform action \"AXConfirm\" of text field 1 of group 1 of tool bar 1 of window 1 \n" + "click button \"2D-3D Viewer\" of tool bar 1 of window 1 \n"
				+ "end tell \n" + "end tell \n";
		log("osirix: " + arg);
		byte[] b = new byte[1000];
		File f = null;
		try
		{
			f = File.createTempFile("osirixaetmp", "txt");
			FileOutputStream fos = new FileOutputStream(f);
			fos.write((s).getBytes());
			fos.close();
			Process p = Runtime.getRuntime().exec("osascript " + f.getAbsolutePath());
			p.waitFor();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		finally
		{
			if (f != null)
			{
				f.delete();
			}
		}
	}
	private String		args[];
	private boolean		asyncFlag	= false;
	private MFClient	client		= defClient;
	private String		cmdLine;

	Ris4DCommand(String cmd)
	{
		cmdLine = cmd;
		args = cmd.split(";");
		checkAsync();
		checkAltHost();
	}

	private void checkAltHost()
	{
		String c = args[0].trim();
		String[] split = c.split("@");
		if (split.length > 1)
		{
			args[0] = split[0];
			String spec = split[1];
			client = (MFClient) clientMap.get(spec);
			if (client == null)
			{
				split = split[1].split(":");
				if (split.length == 1)
				{
					throw new RuntimeException("no colon in target spec: " + c);
				}
				String host = split[0];
				String port = split[1];
				log("ceating new client for " + spec);
				client = new MFClient(host, port, localStoreUserName, localStorePassword);
				clientMap.put(spec, client);
			}
		}
		else
		{
			client = defClient;
		}
	}

	private void checkAsync()
	{
		String c = args[0].trim();
		if (c.equalsIgnoreCase("async"))
		{
			asyncFlag = true;
			String[] newcmd = new String[args.length - 1];
			System.arraycopy(args, 1, newcmd, 0, args.length - 1);
			args = newcmd;
		}
		else
		{
			asyncFlag = false;
		}
	}

	boolean deleteLocalFile(String name)
	{
		File f = new File(name);
		if (f.isDirectory())
		{
			// never delete the root
			if (f.getParentFile() == null)
			{
				return false;
			}
			return FileUtil.deleteRecursive(f);
		}
		else
		{
			return f.delete();
		}
	}

	String downloadStudies(String type, String args[]) throws Exception
	{
		Downloader d = new Downloader();
		File tempDir = null;
		try
		{
			Thread t = new Thread(d);
			t.start();
			boolean b = client.downloadStudies(type, args, InetAddress.getLocalHost().getHostAddress(), "" + d.ss.getLocalPort());
			if (!b)
			{
				d.close();
				return "err;could not create archive";
			}
			try
			{
				t.join(5 * 60 * 1000);
			}
			catch (InterruptedException e)
			{
				d.close();
				return "err;timeout waiting for archive";
			}
			File f = d.getFile();
			tempDir = null;
			if (b && f != null && f.exists())
			{
				for (;;)
				{
					File tempFile = File.createTempFile("DICOM", ".tmp", null);
					tempDir = new File(tempFile.getParentFile(), tempFile.getName().substring(0, tempFile.getName().length() - 4));
					if (tempDir.mkdir())
					{
						break;
					}
				}
				boolean unzipped = ZipUtil.unzip(f, tempDir);
				if (!unzipped)
				{
					throw new RuntimeException("could not unzip");
				}
			}
		}
		catch (Exception e)
		{
			Util.safeDelete(tempDir);
			throw e;
		}
		finally
		{
			if (d != null)
			{
				Util.safeDelete(d.getFile());
			}
		}
		return "ok;" + tempDir.getAbsolutePath() + "\n";
	}

	int efilm(String arg) throws Exception
	{
		int i = exec(false, efautoclFile, arg);
		return i;
	}

	int express(String arg) throws Exception
	{
		int i = exec(true, expressClientFile, arg);
		// log("i"+i);
		if (i < 0)
		{
			exec(false, expressApp, "");
			Thread.sleep(5000);
			i = exec(true, expressClientFile, arg);
			// log("i2"+i);
		}
		return i;
	}

	String getFile(String uid, String name, String type) throws Exception
	{
		byte data[] = client.getFile(uid, name, type);
		if (data.length == 0 || data == null)
		{
			return "err;not found";
		}
		int index = name.lastIndexOf(".");
		String a = index != -1 ? name.substring(0, index) : name;
		String b = index != -1 ? name.substring(index) : null;
		File dir = new File(Ris4D.getRoot(), "RISTEMP");
		dir.mkdir();
		File f = new File(dir, name);
		f.delete();
		Util.writeFile(data, f);
		return "ok;" + f.getAbsolutePath() + "\n";
	}

	String getFileList(String uid)
	{
		return client.getFileList(uid);
	}

	public synchronized String getResult() throws Exception
	{
		String cmd = args[0].trim();
		if (cmdLine.startsWith("pt"))
		{
			return PTBurner.handlePtCommand(cmdLine);
		}
		if (cmd.equalsIgnoreCase("local-ping"))
		{
			return result(true);
		}
		if (cmd.equalsIgnoreCase("ping"))
		{
			return result(ping());
		}
		if (cmd.equalsIgnoreCase("putFile"))
		{
			return putFile(args[1], args[2], args[3], args[4]);
		}
		if (cmd.equalsIgnoreCase("getFile"))
		{
			return getFile(args[1], args[2], args[3]);
		}
		if (cmd.equalsIgnoreCase("downloadStudies"))
		{
			return downloadStudies(args[1], subArray(args, 2)) + "\n";
		}
		if (cmd.equalsIgnoreCase("sendStudy"))
		{
			return result(sendStudy(args[1], args[2]));
		}
		if (cmd.equalsIgnoreCase("deleteLocalFile"))
		{
			return result(deleteLocalFile(args[1]));
		}
		if (cmd.equalsIgnoreCase("getFileList"))
		{
			String result = getFileList(args[1]);
			int count = StringUtil.count(result, '\n');
			return "ok;" + count + "\n" + result;
		}
		if (cmd.equalsIgnoreCase("open"))
		{
			open(args[1]);
			return "ok;\n";
		}
		if (cmd.equalsIgnoreCase("osirix"))
		{
			return "ok;" + osirix(args[1]) + "\n";
		}
		if (cmd.equalsIgnoreCase("osirixStudyExistsByAccession"))
		{
			return "ok;" + osirixStudyExistsByAccession(args[1].trim()) + "\n";
		}
		if (cmd.equalsIgnoreCase("osirixStudyExistsByUID"))
		{
			return "ok;" + osirixStudyExistsByUID(args[1].trim()) + "\n";
		}
		if (cmd.equalsIgnoreCase("osirixOpenStudyByAccession"))
		{
			return "ok;" + osirixOpenStudyByAccession(args[1].trim()) + "\n";
		}
		if (cmd.equalsIgnoreCase("osirixOpenStudyByUID"))
		{
			return "ok;" + osirixOpenStudyByUID(args[1].trim()) + "\n";
		}
		if (cmd.equalsIgnoreCase("efilm"))
		{
			return "ok;" + efilm(args[1]) + "\n";
		}
		if (cmd.equalsIgnoreCase("express"))
		{
			return "ok;" + express(args[1]) + "\n";
		}
		if (cmd.equalsIgnoreCase("scribe"))
		{
			return "ok;" + scribe(args[1]) + "\n";
		}
		if (cmd.equalsIgnoreCase("hicca"))
		{
			log("hicca;" + args[1]);
			String file = HICCA.getInstance().process(args[1], csvDelimiter);
			return file != null ? "ok;" + file + "\n" : "err;\n";
		}
		if (cmd.equalsIgnoreCase("referrerAdd"))
		{
			return client.referrerAdd(args[1], args[2]) + "\n";
		}
		if (cmd.equalsIgnoreCase("referrerModify"))
		{
			return client.referrerModify(args[1], args[2], args[3]) + "\n";
		}
		if (cmd.equalsIgnoreCase("referrerDelete"))
		{
			return client.referrerDelete(args[1]) + "\n";
		}
		if (cmd.equalsIgnoreCase("publishStudy"))
		{
			return client.publishStudy(args[1], args[2]) + "\n";
		}
		if (cmd.equalsIgnoreCase("unpublishStudy"))
		{
			return client.unpublishStudy(args[1], args[2]) + "\n";
		}
		if (cmd.equalsIgnoreCase("publishAttachment"))
		{
			return new File(args[3]).exists() ? client.publishAttachment(args[1], args[2], Util.readWholeFile(new File(args[3]))) + "\n" : "err;AttachmentDoesNotExist\n";
		}
		if (cmd.equalsIgnoreCase("unpublishAttachment"))
		{
			return client.unpublishAttachment(args[1], args[2]) + "\n";
		}
		if (cmd.equalsIgnoreCase("cfind"))
		{
			return client.cfind(args[1], args[2], args[3]);
		}
		if (cmd.equalsIgnoreCase("cmove"))
		{
			return result(client.cmove(args[1], args[2], args[3]));
		}
		if (cmd.equalsIgnoreCase("CreateReportPDF"))
		{
			return client.createReportPdf(args[1], args[2]);
		}
		if (cmd.equalsIgnoreCase("patientCleanup"))
		{
			return client.patientCleanup(args[1].trim(), args[2].trim());
		}
		if (cmd.equalsIgnoreCase("getLocalTime"))
		{
			return client.getLocalTime();
		}
		// if (cmd.equalsIgnoreCase("putRichMedia"))
		// {
		// return client.putRichMedia(args[1], args[2]);
		// }
		// if (cmd.equalsIgnoreCase("getRichMedia"))
		// {
		// return client.getRichMedia(args[1], args[2]);
		// }
		// if (cmd.equalsIgnoreCase("createThumbnailsByStudyUID"))
		// return client.createThumbnailsByStudyUID(args[1], args[2]);
		// if (cmd.equalsIgnoreCase("createThumbnailsBySeriesUID"))
		// return client.createThumbnailsBySeriesUID(args[1], args[2]);
		throw new RuntimeException("bad command");
	}

	synchronized String newGetResult() throws Exception
	{
		String cmd = args[0].trim();
		String commandName = commandMap.get(cmd.toLowerCase());
		InternalSelfCheck.setLastStudy(commandName);
		RisCommand command = null;
		if (commandName == null)
		{
			if (asyncFlag)
			{
				AsyncWrapperCommand tmpCmd = new AsyncWrapperCommand();
				tmpCmd.setRisCmd(this);
				command = tmpCmd;
			}
			else
				return getResult();
		}
		else if (commandName.contains("AsyncCommand"))
		{
			command = AsyncCommand.get();
		}
		else
		{
			command = (RisCommand) Class.forName(commandName).newInstance();
		}
		if (command == null)
			throw new RuntimeException("Unkwnown command: " + cmd);
		command.setClient(client);
		command.setCmd(cmd);
		command.setArgs(args);
		if (asyncFlag)
		{
			Long id = AsyncCommand.get().startAsyncTask(command);
			return "ok;" + id + "\n";
		}
		command.run();
		if (command.getException() != null)
		{
			throw command.getException();
		}
		return command.getResult();
	}

	int osirix(String arg) throws Exception
	{
		searchOsirix(arg.trim());
		return 1;
	}

	private int osirixOpenStudyByAccession(String arg) throws Exception
	{
		XmlRpcClient client = new XmlRpcClient(osirixXmlRpcUrl, false);
		HashMap map = new HashMap();
		map.put("request", "(accessionNumber LIKE '" + arg + "')");
		map.put("table", "Study");
		map.put("execute", "Open");
		XmlRpcStruct returnStruct = (XmlRpcStruct) client.invoke(osirixCheckStudyString, new Object[] { map });
		String tmp = returnStruct.getString("exists").trim();
		if (tmp == null || tmp.equals("0"))
		{
			return 0;
		}
		return 1;
	}

	private int osirixOpenStudyByUID(String arg) throws Exception
	{
		XmlRpcClient client = new XmlRpcClient(osirixXmlRpcUrl, false);
		HashMap map = new HashMap();
		map.put("request", "(studyInstanceUID LIKE '" + arg + "')");
		map.put("table", "Study");
		map.put("execute", "Open");
		XmlRpcStruct returnStruct = (XmlRpcStruct) client.invoke(osirixCheckStudyString, new Object[] { map });
		String tmp = returnStruct.getString("exists").trim();
		if (tmp == null || tmp.equals("0"))
		{
			return 0;
		}
		return 1;
	}

	private int osirixStudyExistsByAccession(String arg) throws Exception
	{
		XmlRpcClient client = new XmlRpcClient(osirixXmlRpcUrl, false);
		HashMap map = new HashMap();
		map.put("request", "(accessionNumber LIKE '" + arg + "')");
		map.put("table", "Study");
		map.put("execute", "Select");
		XmlRpcStruct returnStruct = (XmlRpcStruct) client.invoke(osirixCheckStudyString, new Object[] { map });
		String tmp = returnStruct.getString("exists").trim();
		if (tmp == null || tmp.equals("0"))
		{
			return 0;
		}
		return 1;
	}

	private int osirixStudyExistsByUID(String arg) throws Exception
	{
		XmlRpcClient client = new XmlRpcClient(osirixXmlRpcUrl, false);
		HashMap map = new HashMap();
		map.put("request", "(studyInstanceUID LIKE '" + arg + "')");
		map.put("table", "Study");
		map.put("execute", "Select");
		XmlRpcStruct returnStruct = (XmlRpcStruct) client.invoke(osirixCheckStudyString, new Object[] { map });
		String tmp = returnStruct.getString("exists").trim();
		if (tmp == null || tmp.equals("0"))
		{
			return 0;
		}
		return 1;
	}

	boolean ping()
	{
		boolean b = client.ping();
		return b;
	}

	String putFile(String uid, String name, String type, String localPath) throws Exception
	{
		byte b[] = Util.readWholeFile(new File(localPath));
		if (b == null || b.length == 0)
		{
			return "err;No data found in file: " + localPath + "\n";
		}
		if (client.putFile(uid, name, type, b))
		{
			return "ok;\n";
		}
		else
		{
			return "err;\n";
		}
	}

	String result(boolean b)
	{
		if (b)
		{
			return "ok;\n";
		}
		else
		{
			return "err;fail\n";
		}
	}

	int scribe(String arg) throws Exception
	{
		int i = exec(false, scribeApp, arg);
		return i;
	}

	boolean sendStudy(String uid, String ae)
	{
		return client.sendStudyUID(uid, ae);
	}

	String[] subArray(String[] args, int start)
	{
		String sub[] = new String[args.length - start];
		System.arraycopy(args, start, sub, 0, sub.length);
		return sub;
	}
}