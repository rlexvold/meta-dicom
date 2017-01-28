package net.metafusion.ris4d;

import static net.metafusion.ris4d.Ris4DCommand.efautoclFile;
import static net.metafusion.ris4d.Ris4DCommand.expressApp;
import static net.metafusion.ris4d.Ris4DCommand.expressClientFile;
import static net.metafusion.ris4d.Ris4DCommand.localStoreHost;
import static net.metafusion.ris4d.Ris4DCommand.localStorePassword;
import static net.metafusion.ris4d.Ris4DCommand.localStoreRisPort;
import static net.metafusion.ris4d.Ris4DCommand.localStoreUserName;
import static net.metafusion.ris4d.Ris4DCommand.log;
import static net.metafusion.ris4d.Ris4DCommand.scribeApp;
import static net.metafusion.ris4d.commands.MedicareCommand.csvDelimiter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;

import net.metafusion.figaro.Figaro;
import net.metafusion.medicare.hic.HICCA;
import net.metafusion.medicare.ictk.ICtk;
import net.metafusion.ptburn.PTBurner;
import net.metafusion.util.AEMap;
import net.metafusion.util.GlobalProperties;
import net.metafusion.util.InternalSelfCheck;
import net.metafusion.util.OsUtils;
import acme.util.Util;

public class Ris4D
{
	static File	root;

	static File getRoot()
	{
		if (root == null)
			root = OsUtils.isMac() ? new File("/Applications/Metafusion") : new File("c:/metafusion");
		return root;
	}
	static Ris4D	instance	= null;

	public static Ris4D get()
	{
		return instance;
	}

	public static void init(int port) throws Exception
	{
		instance = new Ris4D(port);
		instance.run();
	}
	class Handler implements Runnable
	{
		Socket			s;
		InputStream		is;
		OutputStream	os;

		Handler(Socket s) throws Exception
		{
			this.s = s;
			is = new BufferedInputStream(s.getInputStream());
			os = new BufferedOutputStream(s.getOutputStream());
		}

		public void run()
		{
			byte b[] = new byte[4096];
			StringBuffer sb = new StringBuffer();
			try
			{
				for (;;)
				{
					int cnt = is.read(b);
					if (cnt == -1)
						break;
					sb.append(new String(b, 0, cnt));
					if (sb.toString().endsWith("\n"))
					{
						sb.setLength(sb.length() - 1);
						break;
					}
				}
				Util.log("Ris4d request: " + sb);
				Ris4DCommand rsc = new Ris4DCommand(sb.toString());
				String reply = rsc.newGetResult();
				Util.log("Ris4d reply: " + reply);
				os.write(reply.getBytes());
				os.flush();
				// s.shutdownOutput();
				// Thread.sleep(1000);
			}
			catch (Exception e)
			{
				Util.log("Ris4d caught", e);
				e.printStackTrace();
				try
				{
					os.write(("error;" + e.getMessage() + "").getBytes());
					os.flush();
				}
				catch (IOException e1)
				{
				}
			}
			finally
			{
				Util.safeClose(is);
				Util.safeClose(os);
				Util.safeClose(s);
			}
		}
	}
	ServerSocket	ss;

	Ris4D(int port) throws Exception
	{
		ss = new ServerSocket(port);
	}

	public void run()
	{
		InternalSelfCheck beacon = new InternalSelfCheck();
		beacon.setUrlStrings((String[]) GlobalProperties.get().get("beaconURLs"));
		beacon.setAdditionalUrl("GLUE_" + AEMap.getDefault().getName());
		beacon.setWaitTimeInMinutes((Integer) GlobalProperties.get().get("beaconWaitTime"));
		Util.startDaemonThread(beacon);
		for (;;)
		{
			Socket s = null;
			try
			{
				s = ss.accept();
				Handler h = new Handler(s);
				s = null;
				new Thread(h).start();
			}
			catch (Exception e)
			{
				Util.log("Ris4d server caught", e);
			}
			finally
			{
				Util.safeClose(s);
			}
		}
	}

	public static void main(String[] args)
	{
		try
		{
			// MFClient client = new MFClient();
			//
			// for (;;) {
			//
			// boolean b = client.ping();
			//
			// MFIdle idle = client.idle();
			//
			// Util.log(""+b);
			// Util.sleep(5000);
			// }
			File root;
			if (args.length == 1)
				root = new File(args[0]);
			else
				root = getRoot();
			Figaro.setRoot(root);
			Figaro.checkIt();
			log("Version: " + Util.getManifestVersion());
			log("Ris4d loading props from root=" + root + " ris4d.props");
			File f = new File(root, "ris4d.props");
			FileInputStream fis = new FileInputStream(f);
			Properties props = new Properties();
			props.load(fis);
			localStoreHost = props.getProperty("localStoreHost");
			log("localStoreHost=" + localStoreHost);
			localStoreRisPort = props.getProperty("localStoreRisPort");
			log("localStoreRisPort=" + localStoreRisPort);
			localStoreUserName = props.getProperty("localStoreUserName");
			log("localStoreUserName=" + localStoreUserName);
			localStorePassword = props.getProperty("localStorePassword");
			log("localStorePassword=" + localStorePassword);
			String tmp = props.getProperty("csvDelimiter");
			if (tmp != null)
				csvDelimiter = tmp.charAt(0);
			log("csvDelimiter=" + csvDelimiter);
			tmp = props.getProperty("rsyncChunkSizeInMB");
			Integer size = 10;
			if (tmp != null)
			{
				try
				{
					size = Integer.parseInt(tmp);
				}
				catch (Exception e)
				{
				}
			}
			GlobalProperties.get().put("rsyncChunkSizeMB", size);
			tmp = props.getProperty("openOfficeCmd");
			if (tmp != null)
				GlobalProperties.get().put("openOffice/cmd", tmp);
			tmp = props.getProperty("openOfficePort");
			if (tmp != null)
			{
				try
				{
					size = Integer.parseInt(tmp);
					GlobalProperties.get().put("openOffice/port", size);
				}
				catch (Exception e)
				{
				}
			}
			tmp = props.getProperty("beaconURL");
			if (tmp != null && tmp.length() > 1)
			{
				GlobalProperties.get().put("beaconURLs", tmp.split(","));
			}
			Integer tmpInt = new Integer(60);
			tmp = props.getProperty("beaconWaitTime");
			if (tmp != null)
			{
				try
				{
					tmpInt = Integer.parseInt(tmp);
				}
				catch (Exception e)
				{
					tmpInt = 60;
				}
			}
			else
				tmpInt = 60;
			GlobalProperties.get().put("beaconWaitTime", tmpInt);
			if (!OsUtils.isMac())
			{
				efautoclFile = new File(props.getProperty("efautoclFile"));
				log("efautoclFile=" + efautoclFile);
				expressClientFile = new File(props.getProperty("expressClientFile"));
				log("expressClientFile=" + expressClientFile);
				expressApp = new File(props.getProperty("expressApp"));
				log("expressApp=" + expressApp);
				scribeApp = new File(props.getProperty("scribeApp"));
				log("scribeApp=" + scribeApp);
				PTBurner.ptRoot = new File(props.getProperty("ptRoot"));
				log("ptRoot=" + PTBurner.ptRoot);
				PTBurner.ptStatusFile = new File(props.getProperty("ptStatusFile"));
				log("ptStatusFile=" + PTBurner.ptStatusFile);
			}
			// RAL - moved outside the if !Mac section to test it on Mac OS X
			String enableHICCA = (props.getProperty("enableHICCA"));
			log("enableHICCA=" + enableHICCA);
			if (enableHICCA != null && enableHICCA.trim().equalsIgnoreCase("true"))
			{
				String hld = props.getProperty("hiconline.logicpack.dir");
				log("hiconline.logicpack.dir=" + hld);
				String jlp = props.getProperty("java.library.path");
				log("java.library.path=" + jlp);
				HICCA.init(hld, jlp);
			}
			String enableICtk = (props.getProperty("enableICtk"));
			log("enableICtk=" + enableICtk);
			if (enableICtk != null && enableICtk.trim().equalsIgnoreCase("true"))
			{
				String wrapperDll = props.getProperty("ictk.wrapper.dll");
				log("ictk.wrapper.dll=" + wrapperDll);
				String easyclaimDll = props.getProperty("ictk.easyclaim.dll");
				ICtk.init(new File(wrapperDll), new File(easyclaimDll));
			}
			String osirixUrl = props.getProperty("osirixRpcUrl");
			Ris4DCommand.init(localStoreHost, localStoreRisPort, localStoreUserName, localStorePassword, osirixUrl);
			new Ris4D(4010).run();
		}
		catch (Exception e)
		{
			log("Ris4D failed with: " + e);
			e.printStackTrace();
		}
		finally
		{
		}
	}
}