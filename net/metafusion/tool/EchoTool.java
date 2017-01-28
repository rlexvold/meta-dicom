package net.metafusion.tool;

import java.io.File;
import net.metafusion.Dicom;
import net.metafusion.net.DicomClientSession;
import net.metafusion.service.CEcho;
import net.metafusion.util.AE;
import net.metafusion.util.AEMap;
import net.metafusion.util.RoleMap;
import acme.util.Util;
import acme.util.XMLConfigFile;

public class EchoTool implements Runnable
{
	static void log(String s)
	{
		Util.log(s);
	}
	DicomClientSession sess;
	int count = 1;
	AE destAE = null;
	AE ae = null;

	public EchoTool()
	{
		count = Integer.parseInt(System.getProperty("-count", "1"));
		destAE = AEMap.get(System.getProperty("-destae", "SERVER"));
		ae = AEMap.get(System.getProperty("-ae", "CLIENT"));
		AEMap.setDefault(ae);
		sess = new DicomClientSession(RoleMap.getClientRoleMap());
	}

	public void runit() throws Exception
	{
		sess.connect(destAE);
		for (int i = 0; i < count; i++)
		{
			CEcho echo = new CEcho(sess);
			echo.run();
			if (echo.getResult() == Dicom.SUCCESS)
				log("success");
			else log("fail");
		}
		sess.close(true);
	}

	public void run()
	{
		try
		{
			runit();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			log("run caught " + e);
			throw new RuntimeException(e.getMessage());
		}
	}

	public static void main(String[] args)
	{
		try
		{
			Util.parseArgv(args);
			XMLConfigFile configFile = new XMLConfigFile(new File(args[0]), "default");
			// Log.init("ping");
			Dicom.init();
			AEMap.setDefault(new AE("CLIENT"));
			new EchoTool().run();
		}
		catch (Exception e)
		{
			log("echo caught " + e);
			e.printStackTrace();
		}
	}
}
