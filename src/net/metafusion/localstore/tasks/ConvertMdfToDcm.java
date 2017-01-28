package net.metafusion.localstore.tasks;

import java.io.File;

import net.metafusion.localstore.DicomStore;
import net.metafusion.localstore.tasks.CheckForMisplacedImageEntry.FR;
import net.metafusion.model.Image;
import net.metafusion.model.ImageView;
import net.metafusion.util.DicomUtil;
import acme.storage.SSStore;
import acme.util.FileUtil;
import acme.util.Log;

public class ConvertMdfToDcm extends Task
{
	public static void main(String[] args)
	{
		start(args, ConvertMdfToDcm.class);
	}

	public void run()
	{
		boolean recurseFlag = false;
		int i = 2;
		if (cmdArgs.length == i)
		{
			printUsage();
			return;
		}
		if (cmdArgs.length == i + 2)
		{
			if (!cmdArgs[i].equalsIgnoreCase("-R"))
			{
				printUsage();
				return;
			}
			recurseFlag = true;
			i++;
		}
		File directory = new File(cmdArgs[i]);
		FileUtil.forEachFile(directory, recurseFlag, false, true, new FR());
	}
	class FR extends FileUtil.FileRunnable
	{
		@Override
		public void run(File f)
		{
			String name = f.getName();
			if (!name.endsWith(".mdf"))
				return;
			File dcmFile = new File(f.getAbsolutePath().replace(".mdf", ".dcm"));
			if (!DicomUtil.convertMDFToDCM(f, dcmFile))
				Log.log("Error converting " + f);
			else
				Log.log("Converted " + name);
		}
	}

	public void printUsage()
	{
		System.out.println("Usage: ConvertMdfToDcm <path to metafusion.xml> <PACS name> -R <directory or filename>");
		System.out.println("-R = recursive");
	}
}
