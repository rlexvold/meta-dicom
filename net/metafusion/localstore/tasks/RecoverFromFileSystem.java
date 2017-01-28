package net.metafusion.localstore.tasks;

import java.io.File;

import net.metafusion.localstore.DicomStore;
import net.metafusion.model.Image;
import net.metafusion.model.ImageView;
import acme.storage.SSStore;
import acme.util.FileUtil;

public class RecoverFromFileSystem extends Task
{
	public RecoverFromFileSystem()
	{
	}

	@Override
	public void run()
	{
		if (cmdArgs == null || cmdArgs.length < 3)
		{
			log("RecoverFromFileSystem: you need to supply a source directory.");
			return;
		}
		DicomStore ds = DicomStore.get();
		boolean init = false;
		boolean applyRules = false;
		if (cmdArgs.length > 3 && cmdArgs[3].equalsIgnoreCase("true"))
			init = true;
		if (cmdArgs.length > 4 && cmdArgs[4].equalsIgnoreCase("true"))
			applyRules = true;
		ds.recoverFromFilesystemRules(init, new File(cmdArgs[2]), applyRules);
	}

	public static void main(String[] args)
	{
		start(args, RecoverFromFileSystem.class);
	}
}
