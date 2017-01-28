package net.metafusion.pacs;

import integration.MFDemoStudy;
import integration.MFFileInfo;

import java.util.ArrayList;
import java.util.Date;

public interface IPacs
{
	public ArrayList<MFFileInfo> createThumbnailsbyStudy(String studyUID);

	public ArrayList<MFFileInfo> createThumbnailsbySeries(String seriesUID);

	public void deleteStudy(String studyuid);

	public void mergeStudies(String patientStudyUID, String studySourceUID, String accessionNumber, Date studyDate);
	
	public String loadDemoStudies(MFDemoStudy demo);
}
