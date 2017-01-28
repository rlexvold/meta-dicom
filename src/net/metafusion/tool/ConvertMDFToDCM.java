package net.metafusion.tool;

import java.io.File;
import net.metafusion.Dicom;
import net.metafusion.util.AE;
import net.metafusion.util.AEMap;
import net.metafusion.util.DicomUtil;

public class ConvertMDFToDCM
{
	static void log(String s)
	{
		System.out.println("" + s);
	}

	public static void main(String[] args)
	{
		try
		{
			if (args.length != 3)
			{
				log("usage: convert dicomroot mdffile dcmfile");
				log("configdir: directory where conf dir resides");
				log("mdffile: mdffile to convert");
				log("dcmfile: dcm file to create, deleted if it exists");
				return;
			}
			File configDir = new File(args[0]);
			File mdfFile = new File(args[1]);
			File dcmFile = new File(args[2]);
			if (!configDir.exists() || !configDir.isDirectory())
			{
				log("bad config dir");
				return;
			}
			if (!mdfFile.exists())
			{
				log("bad mdf file dir");
				return;
			}
			if (dcmFile.exists())
			{
				dcmFile.delete();
			}
			AE ae = new AE("CLIENT");
			AEMap.setDefault(ae);
			Dicom.init("CLIENT", configDir.getAbsolutePath());
			boolean ok = DicomUtil.convertMDFToDCM(mdfFile, dcmFile);
			if (!ok) log("convert failed");
		}
		catch (Exception e)
		{
			log("convert caught " + e);
			e.printStackTrace();
		}
	}
}
