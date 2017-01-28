package net.metafusion.tool;

import java.io.File;
import java.net.InetSocketAddress;
import net.metafusion.Dicom;
import net.metafusion.admin.ServerBean;
import net.metafusion.localstore.DicomStore;
import net.metafusion.util.AE;
import acme.db.DBManager;
import acme.util.Log;
import acme.util.Util;
import acme.util.XML;
import acme.util.XMLConfigFile;

public class AdminTool
{
	static void log(String s)
	{
		Log.log(s);
	}
	DicomStore dicomStore;
	ServerBean serverBean = new ServerBean();
	AE ae;
	InetSocketAddress backup;

	public XML getCurrentAdminStore() throws Exception
	{
		XMLConfigFile f = XMLConfigFile.getDefault();
		String storeName = f.getDefaultName();
		System.out.println("getCurrentAdminStore " + storeName);
		Util.log("getConfigRoot() " + f.getConfigRoot().getAbsolutePath());
		XML root = new XML(new File(f.getConfigRoot(), "metaadmin.xml"));
		XML x = root.getNode("stores");
		if (x != null) x = x.search("name", storeName);
		if (x == null)
		{
			x = root.getNode("proxies");
			if (x != null) x = x.search("name", storeName);
		}
		if (x == null) throw new RuntimeException("could not load admin for " + storeName);
		return x;
	}

	public AdminTool() throws Exception
	{
		XML storeXML = getCurrentAdminStore();
		serverBean = new ServerBean(storeXML);
		XML config = XMLConfigFile.getDefault().getXML();
		DBManager.init();
		Dicom.init();
		DicomStore.init(serverBean);
		dicomStore = DicomStore.get();
	}
	String command = "";

	public void run()
	{
		try
		{
			if (command.equalsIgnoreCase("recover"))
				;// todo: dicomStore.recoverFromFilesystem();
			else if (command.equalsIgnoreCase("requestsync"))
				;// !!!!!!!!!!!!!!!!!!!!!!!!!!!!
					// dicomStore.requestFullSync(dicomStore.getIDs());
			else
			{
				log("unknown command");
				log("commands are:");
				log("recover: recover database from files");
				log("requestsync: send a full sync to the partner server");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			log("Exit finally");
		}
	}

	public static void main(String[] args)
	{
		try
		{
			if (args.length != 3)
			{
				log("usage: admintool conf_file_path.xml target [recover|requestsync]");
				System.exit(1);
			}
			System.out.println("admintool conf=" + args[0] + " targ=" + args[1] + " cmd=" + args[2]);
			XMLConfigFile configFile = new XMLConfigFile(new File(args[0]), args[1]);
			// acme.util.Log.init("admintool");
			AdminTool tool = new AdminTool();
			tool.command = args[2];
			log("admintool.run ");
			tool.run();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
}