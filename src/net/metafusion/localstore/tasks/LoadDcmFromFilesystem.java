package net.metafusion.localstore.tasks;

import java.io.File;

import net.metafusion.localstore.DicomStore;
import net.metafusion.localstore.tasks.CheckForMisplacedImageEntry.FR;
import acme.storage.SSStore;
import acme.util.FileUtil;
import acme.util.Log;

public class LoadDcmFromFilesystem extends Task
{
	private boolean	ignoreFileExtension	= false;

	@Override
	public void run()
	{
		if(cmdArgs.length < 3)
			return;
		if (cmdArgs.length == 4)
		{
			String tmp = cmdArgs[3];
			if(tmp.equalsIgnoreCase("true"))
				ignoreFileExtension = true;
			else
				ignoreFileExtension = false;
		}
			DicomStore.get().loadDCMFileDataFromFilesystem(new File(cmdArgs[2]), ignoreFileExtension);
	}

	public static void main(String[] args)
	{
		start(args, LoadDcmFromFilesystem.class);
	}
}
