/*
 * Created by IntelliJ IDEA.
 * User: mb
 * Date: Oct 23, 2003
 * Time: 7:15:37 PM
 */
package net.metafusion.localstore.tasks;

import java.io.File;
import net.metafusion.Dicom;
import net.metafusion.net.DicomClientSession;
import net.metafusion.service.CEcho;
import net.metafusion.util.AE;
import net.metafusion.util.AEMap;
import net.metafusion.util.RoleMap;
import acme.util.Log;
import acme.util.Util;
import acme.util.XMLConfigFile;

public class Ping implements Runnable
{
	static void log(String s)
	{
		Log.log(s);
	}
	int count = 1;
	AE destAE = null;

	public Ping() throws Exception
	{
		count = Integer.parseInt(System.getProperty("-count", "1"));
		Dicom.init();
		AEMap.setDefault(AEMap.get(System.getProperty("-srcae")));
		destAE = AEMap.get(System.getProperty("-destae"));
	}

	public Ping(AE destAE, int count) throws Exception
	{
		this.count = count;
		this.destAE = destAE;
	}

	public Ping(AE destAE) throws Exception
	{
		this.count = 1;
		this.destAE = destAE;
	}

	public void run()
	{
		DicomClientSession sess = null;
		try
		{
			log("ping to " + destAE);
			sess = new DicomClientSession(RoleMap.getPingRoleMap());
			sess.connect(destAE);
			if (!sess.isConnected()) throw new Exception("could not connect");
			for (int i = 0; i < count; i++)
			{
				log("Verify " + i);
				CEcho echo = new CEcho(sess);
				echo.run();
				if (echo.getResult() != Dicom.SUCCESS)
				{
					log("ping failed");
					throw new RuntimeException("ping failed");
				}
			}
			sess.close(true);
		}
		catch (Exception e)
		{
			log("ping caught " + e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		finally
		{
			if (sess != null) sess.close(false);
		}
	}

	public static void main(String[] args)
	{
		try
		{
			if (args.length == 0)
			{
				log("usage: ping conf_file_path.xml -srcae=srcae -destae=destae [-count=n]");
				System.exit(1);
			}
			XMLConfigFile configFile = new XMLConfigFile(new File(args[0]), "default");
			Util.parseArgv(args);
			// Log.init("ping");
			Ping sync = new Ping();
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
