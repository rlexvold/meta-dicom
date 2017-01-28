package net.metafusion.pacs;

import integration.MFDemoStudy;
import integration.MFFileInfo;

import java.util.ArrayList;
import java.util.Date;

import net.metafusion.localstore.DicomStore;
import net.metafusion.model.Study;
import net.metafusion.model.StudyView;
import acme.util.Log;

public class PacsFacade implements IPacs
{
	public ArrayList<MFFileInfo> createThumbnailsbySeries(String seriesUID)
	{
		return PacsUtil.get().createThumbnailsbySeries(seriesUID);
	}

	public ArrayList<MFFileInfo> createThumbnailsbyStudy(String studyUID)
	{
		return PacsUtil.get().createThumbnailsbyStudy(studyUID);
	}

	public void deleteStudy(String studyuid)
	{
		try
		{
			Study study = StudyView.get().selectByUID(studyuid);
			if (study != null)
			{
				DicomStore.get().deleteStudy(study);
			}
		}
		catch (Exception e)
		{
			Log.log("could not delete study" + studyuid + " ", e);
		}
	}

	public void mergeStudies(String patientStudyUID, String studySourceUID, String accessionNumber, Date studyDate)
	{
		PacsUtil.get().mergeStudies(patientStudyUID, studySourceUID, accessionNumber, studyDate);
	}

	public String loadDemoStudies(MFDemoStudy study)
	{
		return PacsUtil.get().loadDemoStudies(study);
	}
}
