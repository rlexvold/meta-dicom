package net.metafusion.localstore.tasks;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import net.metafusion.model.Image;
import net.metafusion.model.ImageFile;
import net.metafusion.model.ImageFileView;
import net.metafusion.model.ImageView;
import net.metafusion.model.Study;
import net.metafusion.model.StudyView;
import net.metafusion.util.DicomDir;
import net.metafusion.util.DicomUtil;
import acme.db.JDBCUtil;
import acme.storage.SSStore;
import acme.util.FileNameFilter;
import acme.util.Log;
import acme.util.StringUtil;

public class CreateMissingJpegsAndZips extends Task
{
	private static boolean	forceJpeg	= false;

	private void createMissingJpegs()
	{
		try
		{
			List<ImageFile> allImages = ImageFileView.get().selectAll();
			for (Iterator<ImageFile> i = allImages.iterator(); i.hasNext();)
			{
				Image image = ImageView.get().selectByID(i.next().getImageID());
				if (DicomUtil.convertImageToJpeg(image, forceJpeg))
					log("Converted: " + image.imageID);
			}
		}
		catch (Exception e)
		{
			log("DB Problem: " + e.getMessage());
		}
	}

	private void createMissingZips()
	{
		log("Creating missing zips...");
		FileNameFilter ff = new FileNameFilter("zip");
		List sv = StudyView.get().selectAll();
		for (int i = 0; i < sv.size(); i++)
		{
			try
			{
				vlog("Zips: " + (i + 1) + " of " + sv.size());
				Study study = (Study) sv.get(i);
				if (study != null)
				{
					File studyDir = SSStore.get().getStudyDir(study.getStudyID());
					if (studyDir == null)
					{
						vlog("Null studyDir for studyID: " + study.getStudyID());
					}
					else
					{
						String[] files = studyDir.list(ff);
						if (files == null || files.length == 0)
						{
							vlog("Creating zip for study dir: " + studyDir.getAbsolutePath());
							File zipFile = DicomDir.CreateDicomDirZipForWeb(studyDir, study);
							String update = "update web_study set studypath = '" + zipFile.getAbsolutePath() + "' " + " where dcm_studyid = " + study.getStudyID();
							update = StringUtil.replaceAll(update, "\\", "\\\\");
							JDBCUtil.get().update(update);
							vlog("success creating zip: " + zipFile.getAbsolutePath());
						}
						else
						{
							vlog("Zip already exists in " + studyDir.getAbsolutePath());
						}
					}
				}
				else
				{
					vlog("Study is null");
				}
			}
			catch (Throwable e)
			{
				vlog("Problem with zip: " + e.getMessage());
			}
		}
	}

	@Override
	public void run()
	{
		Log.setVerbose(0);
		initLogs("CreateMissingJpegs", true);
		boolean doJpegs = true;
		boolean doZips = true;
		for (int i = 2; i < cmdArgs.length; i++)
		{
			if (cmdArgs[i].equalsIgnoreCase("noZip"))
				doZips = false;
			else if (cmdArgs[i].equalsIgnoreCase("noJpeg"))
				doJpegs = false;
			else if (cmdArgs[i].equalsIgnoreCase("forceJpeg"))
				forceJpeg = true;
		}
		if (doJpegs)
			createMissingJpegs();
		if (doZips)
			createMissingZips();
	}

	public static void main(String[] args)
	{
		start(args, CreateMissingJpegsAndZips.class);
	}
}
