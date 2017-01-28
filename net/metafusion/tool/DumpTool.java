package net.metafusion.tool;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import net.metafusion.Dicom;
import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSInputStream;
import net.metafusion.net.DicomClientSession;
import net.metafusion.util.AE;
import net.metafusion.util.AEMap;
import net.metafusion.util.UID;
import acme.util.FileUtil;
import acme.util.Util;

public class DumpTool implements Runnable
{
	static void log(String s)
	{
		Util.log(s);
	}
	DicomClientSession sess;
	int count = 1;
	AE destAE = null;
	AE ae = null;

	public DumpTool()
	{
		count = Integer.parseInt(System.getProperty("-count", "1"));
		destAE = AEMap.get(System.getProperty("-destae", "SERVER"));
		ae = AEMap.get(System.getProperty("-ae", "CLIENT"));
		AEMap.setDefault(ae);
	}

	boolean dump(net.metafusion.util.Tag t, DS ds)
	{
		String s = ds.getString(t);
		if (s != null)
		{
			UID uid = UID.get(s);
			;
			log("" + t.getKey() + ":" + uid);
		} else log("" + t.getKey() + ":" + s);
		return s != null;
	}

	boolean dump(net.metafusion.util.Tag ta[], DS ds)
	{
		boolean exists = true;
		for (int i = 0; i < ta.length; i++)
		{
			net.metafusion.util.Tag t = ta[i];
			if (dump(t, ds)) exists = false;
		}
		return exists;
	}

	public void runit() throws Exception
	{
		File f = new File("c:/tmp/DICOMDIR");
		DS ds = new DS(f);
		log("" + ds);
	}

	public void runit2() throws Exception
	{
		List l = FileUtil.listFiles(new File("c:\\dicom\\dcm_images\\"), // Medical_Images"),
				// new String[] { "CT-MONO2-16-chest.dcm" }); //, ".dcm",
				// ".dic", ".img" });
				new String[] { ".dcm", ".dic", ".img" });
		// "CT-MONO2-16-chest.dcm" "MR-MONO2-12-shoulder.dcm"
		// "US-PAL-8-10x-echo.dcm"
		int count = 0;
		Iterator iter = l.iterator();
		while (iter.hasNext())
		{
			File f = (File) iter.next();
			// DS ds = new DS(f);
			DS ds = DSInputStream.readFile(f, null, null, null, true);
			// if (dss.get(Tag.TransferSyntaxUID) == null)
			// continue;
			// if (dss.get(Tag.MediaStorageSOPClassUID) != null ||
			// dss.get(Tag.SOPClassUID) != null)
			// continue;
			// if (!dss.contains(Tag.TransferSyntaxUID))
			// continue;
			// if (!dss.contains(Tag.AffectedSOPClassUID))
			// continue;
			// if (ds.contains(net.metafusion.util.Tag.SOPInstanceUID))
			// continue;
			log("" + (count++) + "============================================== " + f.getName());
			dump(net.metafusion.util.Tag.SOPInstanceUID, ds);
			dump(net.metafusion.util.Tag.AffectedSOPClassUID, ds);
			dump(net.metafusion.util.Tag.MediaStorageSOPClassUID, ds);
			dump(net.metafusion.util.Tag.ImplementationClassUID, ds);
			dump(net.metafusion.util.Tag.SOPClassUID, ds);
			dump(net.metafusion.util.Tag.TransferSyntaxUID, ds);
		}
	}

	String getSOPClass(DS ds)
	{
		String s = ds.getString(net.metafusion.util.Tag.MediaStorageSOPClassUID);
		if (s != null) return s;
		return ds.getString(net.metafusion.util.Tag.SOPClassUID);
	}

	public void run()
	{
		try
		{
			runit2();
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
			AE ae = new AE("CLIENT");
			AEMap.setDefault(ae);
			Dicom.init("CLIENT", "c:\\desktop\\metafusion\\pcserver1\\");
			new DumpTool().run();
		}
		catch (Exception e)
		{
			log("dump caught " + e);
			e.printStackTrace();
		}
	}
}
