package net.metafusion.localstore.tasks;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.metafusion.localstore.DicomStore;
import net.metafusion.model.ImageFile;
import net.metafusion.model.ImageFileView;
import acme.storage.SSStore;
import acme.util.FileUtil;
import acme.util.Log;

public class CheckForExtraMdf extends Task
{
	private Long				currentStudyId	= null;
	private SSStore				ss				= null;
	private Long				numFiles		= 0L;
	private int					count			= 0;
	private HashMap<Long, Long>	imageStudyList	= null;
	private class FR extends FileUtil.FileRunnable
	{
		@Override
		public void run(File f)
		{
			if (!f.getName().endsWith(".mdf"))
				return;
			numFiles++;
			count++;
			if (count == 1000)
			{
				System.out.println(numFiles.toString());
				count = 0;
			}
			Long imageid = null;
			try
			{
				imageid = Long.decode(f.getName().substring(0, f.getName().length() - 4));
			}
			catch (Exception e)
			{
				vlog("Filename is not a number: " + f.getAbsolutePath());
				return;
			}
			Long studyid = imageStudyList.get(imageid);
			if (studyid == null || studyid == 0)
			{
				vlog("Not in DB: " + f.getAbsolutePath());
				return;
			}
			try
			{
				File studyDir = ss.getStudyDir(studyid, false);
				if (!studyDir.getCanonicalPath().contentEquals(f.getParentFile().getCanonicalPath()))
				{
					log("Wrong dir - should be: " + studyDir.getCanonicalPath() + " is in: " + f.getCanonicalPath());
				}
			}
			catch (Exception e)
			{
				log("Exception checking: " + f.getAbsolutePath() + " , message: " + e.getMessage());
			}
		}
	}

	private void loadImageList()
	{
		imageStudyList = new HashMap<Long, Long>();
		List<ImageFile> tmp = ImageFileView.get().selectAll();
		for (Iterator<ImageFile> i = tmp.iterator(); i.hasNext();)
		{
			ImageFile image = i.next();
			imageStudyList.put(image.getImageID(), image.getStudyID());
		}
		tmp = null;
	}

	@Override
	public void run()
	{
		File srcDir = null;
		for (int i = 2; i < cmdArgs.length; i++)
		{
			int mark = cmdArgs[i].indexOf('=');
			if (mark == -1)
				printUsage();
			String option = cmdArgs[i].substring(0, mark);
			String value = cmdArgs[i].substring(mark + 1);
			if (option.equalsIgnoreCase("src"))
			{
				srcDir = new File(value);
				if (!srcDir.exists())
				{
					log("Source directory does not exist: " + value);
					System.exit(1);
				}
			}
			else
				printUsage();
		}
		if (srcDir == null)
			printUsage();
		initLogs("CheckForExtraMdf", true);
		Log.setVerbose(0);
		loadImageList();
		log("Database loaded: " + imageStudyList.size() + " images");
		vlog("************Extra MDF's***********");
		log("************Misplaced MDF's***********");
		DicomStore ds = DicomStore.get();
		ss = ds.getSSStore();
		FileUtil.forEachFile(srcDir, true, false, true, new FR());
		log("Total number of files checked: " + numFiles);
	}

	protected void printUsage()
	{
		System.out.println("Usage: CheckForExtraMdf <path to metafusion.xml> <AE title> src=<root directory>");
		System.exit(1);
	}

	public static void main(String[] args)
	{
		start(args, CheckForExtraMdf.class);
	}
}
