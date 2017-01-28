/*
 * Created by IntelliJ IDEA.
 * User: mb
 * Date: Oct 23, 2003
 * Time: 7:15:37 PM
 */
package net.metafusion.localstore.tasks;

import java.io.File;
import net.metafusion.Dicom;
import net.metafusion.localstore.DicomStore;
import net.metafusion.localstore.LocalStoreConfig;
import net.metafusion.localstore.LocalStoreDicomServer;
import net.metafusion.util.AEMap;
import acme.db.DBManager;
import acme.storage.SSStore;
import acme.storage.SSStoreFactory;
import acme.util.XML;
import acme.util.XMLConfigFile;

public class SyncFromLocalStorage implements Runnable
{
	static void log(String s)
	{
		acme.util.Log.log(s);
	}
	LocalStoreConfig config;
	LocalStoreDicomServer dicomServer;
	SSStore store;
	DicomStore dicomStore;

	SyncFromLocalStorage(LocalStoreConfig config) throws Exception
	{
		this.config = config;
		DBManager.init(config.getDBClassName(), config.getDBURL());
		DBManager.test();
		XML dict = XMLConfigFile.getDefault().getSubconfig("dictionary.xml");
		// XML ae = XMLConfigFile.getDefault().getSubconfig("ae.xml");
		Dicom.init();
		AEMap.setDefault(AEMap.get(System.getProperty("-srcae")));
		store = SSStoreFactory.getStore();
		DicomStore.init();
		dicomStore = DicomStore.get();
	}

	public void run()
	{
		dicomStore.syncFilesAndDatabase();
	}

	public static void main(String[] args)
	{
		try
		{
			if (args.length == 0)
			{
				log("usage: syncfromlocal conf_file_path.xml");
				System.exit(1);
			}
			log("syncfromlocal: localstore conf=" + args[0]);
			LocalStoreConfig config = new LocalStoreConfig(new XML(new File(args[0])));
			SyncFromLocalStorage sync = new SyncFromLocalStorage(config);
			log("sync.run");
			sync.run();
			log("sync done");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
