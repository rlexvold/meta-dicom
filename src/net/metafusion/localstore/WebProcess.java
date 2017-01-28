package net.metafusion.localstore;

import java.io.File;
import net.metafusion.model.Study;
import net.metafusion.model.StudyView;
import net.metafusion.util.DicomDir;
import acme.db.JDBCUtil;
import acme.storage.SSStore;
import acme.util.Log;
import acme.util.StringUtil;
import acme.util.Util;

public class WebProcess implements Runnable
{
	public void run()
	{
		for (;;)
		{
			// Util.sleep(5000);
			Util.sleep(50000); // On Stellaria and Eugenia with faster Shuttle
								// CPUs I changed this delay to make sure the
								// /etc/rc.local launch of the DICOM server does
								// not give mySQL connection error
			try
			{
				String select = " select s.dcm_studyid from web_study s, web_assign a where (s.studypath is null or s.studypath = '') " + " and a.dcm_studyid = s.dcm_studyid "
						+ " group by s.dcm_studyid ";
				Log.vlog("QUEUE ZIP query " + select); // added by Cyrus
				String s = JDBCUtil.get().selectString(select);
				if (s != null)
				{
					Log.vlog("QUEUE ZIP " + s);
					Study study = StudyView.get().selectByID(Long.parseLong(s));
					// /// HACKKKKKKKKKKKKKKKKKKKKKKK
					File f = DicomDir.CreateDicomDirZipForWeb(SSStore.get().getStudyDir(study.getStudyID()), study);
					Log.log("QUEUE SERVER CREATED ZIP " + f.getAbsolutePath());
					String update = "update web_study set studypath = '" + f.getAbsolutePath() + "' " + " where dcm_studyid = " + study.getStudyID();
					update = StringUtil.replaceAll(update, "\\", "\\\\");
					Log.vlog("QUEUE UPDATE IS " + update);
					JDBCUtil.get().update(update);
					// /// HACKKKKKKKKKKKKKKKKKKKKKKK
				}
			}
			catch (Exception e)
			{
				Log.log("QUEUE HACK STUDY CAUGHT " + e, e);
			}
		}
	}
}