package net.metafusion.localstore.tasks;

import java.io.File;

import net.metafusion.dataset.DSFileView;
import net.metafusion.localstore.DicomStore;
import net.metafusion.model.Image;
import net.metafusion.model.ImageView;
import net.metafusion.model.Patient;
import net.metafusion.model.PatientView;
import net.metafusion.util.ImageMetaInfo;
import net.metafusion.util.UID;
import acme.storage.SSInputStream;
import acme.storage.SSStore;
import acme.util.FileUtil;
import acme.util.Log;

public class CheckForMisplacedImageEntry extends Task
{
	public CheckForMisplacedImageEntry()
	{
	}
	class FR extends FileUtil.FileRunnable
	{
		@Override
		public void run(File f)
		{
			if (!f.getName().endsWith(".mdf"))
				return;
			try
			{
				ImageMetaInfo imi = (ImageMetaInfo) SSStore.get().getMetaFromFile(f);
				SSInputStream sis = new SSInputStream(f);
				DSFileView v = (DSFileView) DSFileView.viewMap.load(sis, UID.get(imi.getTransferSyntax()));
				sis.close();
				Image i = ImageView.get().selectByID(Long.parseLong(f.getName().substring(0, f.getName().length() - 4)));
				if (i == null)
					System.out.println("missing db for " + f.getAbsolutePath());
				else
				{
					Patient p = PatientView.get().select(i.patientID);
					if (!p.dicomName.equals(v.PatientName))
					{
						System.out.println("Patient image mismatch: FILE: name[" + f.getAbsolutePath() + "] patient name[" + v.PatientName + "] imageUID[" + v.SOPInstanceUID
								+ "] " + "   DB: name[" + p.dicomName + "] studyid.imageid[" + i.studyID + "." + i.imageID + "] imageUID[" + i.imageUID + "]");
					}
				}
			}
			catch (Exception e)
			{
				System.out.println("Exception: " + e.getMessage());
			}
		}
	}

	@Override
	public void run()
	{
		DicomStore ds = DicomStore.get();
		SSStore ss = ds.getSSStore();
		Log.setVerbose(0);
		FileUtil.forEachFile(ds.getSSStore().getRootDir(), true, false, true, new FR());
	}

	public static void main(String[] args)
	{
		start(args, CheckForMisplacedImageEntry.class);
	}
}
