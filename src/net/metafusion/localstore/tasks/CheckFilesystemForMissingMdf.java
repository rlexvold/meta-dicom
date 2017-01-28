package net.metafusion.localstore.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import acme.util.FileUtil;
import acme.util.StringUtil;

public class CheckFilesystemForMissingMdf extends Task
{
	private static int				numMissingFiles	= 0;
	private static int				totalFiles		= 0;
	private static ArrayList<Long>	studies;
	private static boolean			studyEntered	= false;
	private Date					startTime;

	private void init()
	{
		initLogs("CheckForMissingMdf");
		studies = new ArrayList<Long>();
	}
	class FR extends FileUtil.FileRunnable
	{
		@Override
		public void run(File f)
		{
			if (f.isDirectory())
			{
				studyEntered = false;
				return;
			}
			if (!f.getName().endsWith(".jpg"))
				return;
			totalFiles++;
			File mdfFile = new File(f.getAbsolutePath().replace(".jpg", ".mdf"));
			if (!mdfFile.exists())
			{
				if (!studyEntered)
				{
					String dirs[] = mdfFile.getAbsolutePath().split("/");
					studies.add(Long.decode(dirs[dirs.length - 2]));
					studyEntered = true;
				}
				numMissingFiles++;
			}
		}
	}

	private void doIt(String dir)
	{
		startTime = new Date();
		FileUtil.forEachFile(new File(dir), true, true, true, new FR());
		Arrays.sort(studies.toArray());
		Date endTime = new Date();
		Double elapsed = (double) (endTime.getTime() - startTime.getTime());
		elapsed = elapsed / 1000 / 60;
		log(numMissingFiles + " MDF files missing in " + studies.size() + " studies, " + elapsed + " minutes");
		for (int i = 0; i < studies.size(); i++)
			log(studies.get(i).toString());
	}

	public static void main(String[] args)
	{
		CheckFilesystemForMissingMdf mdf = new CheckFilesystemForMissingMdf();
		mdf.init();
		mdf.doIt(args[0]);
	}
}
