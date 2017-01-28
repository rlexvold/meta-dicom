package net.metafusion.localstore.tasks;

import java.io.File;

import net.metafusion.Dicom;
import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSInputStream;
import net.metafusion.localstore.tasks.ConvertMdfToDcm.FR;
import net.metafusion.model.DicomHeaderView;
import net.metafusion.util.DicomUtil;
import net.metafusion.util.Tag;
import net.metafusion.util.UID;
import acme.util.FileUtil;
import acme.util.Log;

public class CreateDcmHeaderInfo extends Task
{
	public static void main(String[] args)
	{
		start(args, CreateDcmHeaderInfo.class);
	}

	public void run()
	{
		boolean recurseFlag = false;
		if (cmdArgs.length != 3)
		{
			printUsage();
			return;
		}
		File directory = new File(cmdArgs[2]);
		FileUtil.forEachFile(directory, true, false, true, new FR());
	}
	class FR extends FileUtil.FileRunnable
	{
		@Override
		public void run(File f)
		{
			String name = f.getName();
			if (!name.endsWith(".mdf"))
				return;
			DS imageDS = null;
			String tmp = name.replace(".mdf", "");
			try
			{
				Long imageID = Long.parseLong(tmp);
				imageDS = DSInputStream.readFileAndImages(f);
				imageDS.put(Tag.FileMetaInformationVersion, new byte[] { 1, 0 });
				imageDS.put(Tag.MediaStorageSOPClassUID, imageDS.get(Tag.SOPClassUID));
				imageDS.put(Tag.MediaStorageSOPInstanceUID, imageDS.get(Tag.SOPInstanceUID));
				imageDS.put(Tag.TransferSyntaxUID, UID.ExplicitVRLittleEndian);
				imageDS.put(Tag.ImplementationClassUID, Dicom.METAFUSION_UID_PREFIX);
				imageDS.put(Tag.ImplementationVersionName, Dicom.METAFUSION_IMPLEMENTATION_NAME);
				DicomHeaderView.get().insertFullHeader(imageID, imageDS);
				log("Completed file: " + f);
			}
			catch (Exception e)
			{
				log("ERROR with file " + f + ": " + e.getMessage());
			}
		}
	}

	public void printUsage()
	{
		System.out.println("Usage: CreateDcmHeaderInfo <path to metafusion.xml> <PACS name> <directory or filename>");
		System.out.println("-R = recursive");
	}
}
