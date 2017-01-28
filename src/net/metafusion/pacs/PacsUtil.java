package net.metafusion.pacs;

import integration.MFDemoStudy;
import integration.MFFileInfo;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSInputStream;
import net.metafusion.dataset.DSOutputStream;
import net.metafusion.localstore.DicomStore;
import net.metafusion.model.Image;
import net.metafusion.model.ImageView;
import net.metafusion.model.Patient;
import net.metafusion.model.PatientStudy;
import net.metafusion.model.PatientStudyView;
import net.metafusion.model.Series;
import net.metafusion.model.SeriesView;
import net.metafusion.model.Study;
import net.metafusion.model.StudyView;
import net.metafusion.util.DicomUtil;
import net.metafusion.util.Tag;
import acme.storage.SSStore;
import acme.util.FileUtil;
import acme.util.Log;
import acme.util.Util;
import acme.util.FileUtil.FileRunnable;

public class PacsUtil
{
	private static PacsUtil	instance	= null;

	public static PacsUtil get()
	{
		if (instance == null)
		{
			instance = new PacsUtil();
		}
		return instance;
	}

	public ArrayList<MFFileInfo> createThumbnails(List<Image> imageList)
	{
		ArrayList<MFFileInfo> returnList = new ArrayList<MFFileInfo>();
		if (imageList != null && imageList.size() != 0)
		{
			try
			{
				File destDir = new File("/data/thumb", new Long(imageList.get(0).getStudyID()).toString());
				if (!destDir.exists())
					destDir.mkdirs();
				Log.log("Creating thumbnails in: " + destDir);
				Iterator<Image> i = imageList.iterator();
				SSStore ss = DicomStore.get().getSSStore();
				while (i.hasNext())
				{
					Image image = i.next();
					String sourceDir = ss.getStudyDir(image.getStudyID()).getAbsolutePath();
					Long id = image.getImageID();
					String filename = id + ".jpg";
					File sourceFile = new File(sourceDir, filename);
					File destFile = new File(destDir, filename);
					if (!sourceFile.exists())
					{
						DicomUtil.convertImageToJpeg(image, true);
					}
					FileUtil.copyFile(sourceFile, destFile);
					MFFileInfo info = new MFFileInfo(id, destFile);
					returnList.add(info);
				}
			}
			catch (Exception e)
			{
				Log.log("PacsUtil, creating thumbnails", e);
				return null;
			}
		}
		return returnList;
	}

	public ArrayList<MFFileInfo> createThumbnailsbySeries(String seriesUID)
	{
		Series series = SeriesView.get().selectByUID(seriesUID);
		if (series == null)
			return null;
		List<Image> imageList = ImageView.get().selectBySeries(series.seriesID);
		if (imageList == null)
			return null;
		return createThumbnails(imageList);
	}

	public ArrayList<MFFileInfo> createThumbnailsbyStudy(String studyUID)
	{
		Study study = StudyView.get().selectByUID(studyUID);
		if (study == null)
			return null;
		List<Image> imageList = ImageView.get().selectByStudy(study.studyID);
		if (imageList == null)
			return null;
		return createThumbnails(imageList);
	}

	public void mergeStudies(String patientSourceUID, String studySourceUID, String accessionNumber, Date studyDate)
	{
		class mergeStudyFR extends FileUtil.FileRunnable
		{
			private String			accession		= null;
			private Patient			patient			= null;
			private Study			study2			= null;
			private java.sql.Date	studySqlDate	= null;
			private File			tmpStudyDir		= null;

			public mergeStudyFR(Patient p, Study s, java.sql.Date date, String accession, File dir)
			{
				patient = p;
				study2 = s;
				studySqlDate = date;
				this.accession = accession;
				tmpStudyDir = dir;
			}

			@Override
			public void run(File f)
			{
				if (!f.getName().endsWith(".mdf"))
					return;
				DS imageDS = null;
				File dcmFile = new File(tmpStudyDir, f.getName().replace("mdf", "dcm"));
				try
				{
					imageDS = DSInputStream.readFileAndImages(f);
					String studyUID = imageDS.getString(Tag.StudyInstanceUID) + ".246";
					String seriesUID = imageDS.getString(Tag.SeriesInstanceUID) + ".3";
					String instanceUID = seriesUID + System.currentTimeMillis();
					imageDS.put(Tag.MediaStorageSOPInstanceUID, instanceUID);
					imageDS.put(Tag.SOPInstanceUID, instanceUID);
					imageDS.put(Tag.AcquisitionDate, DicomUtil.formatDate(studySqlDate));
					imageDS.put(Tag.AcquisitionTime, DicomUtil.formatTime(studySqlDate));
					imageDS.put(Tag.StudyDate, DicomUtil.formatDate(studySqlDate));
					imageDS.put(Tag.StudyTime, DicomUtil.formatTime(studySqlDate));
					imageDS.put(Tag.AccessionNumber, accession);
					imageDS.put(Tag.PatientID, patient.extID);
					imageDS.put(Tag.PatientName, patient.dicomName);
					imageDS.put(Tag.PatientBirthDate, DicomUtil.formatDate(patient.getDob()));
					imageDS.put(Tag.PatientSex, patient.sex);
					imageDS.put(Tag.StudyInstanceUID, studyUID);
					imageDS.put(Tag.SeriesInstanceUID, seriesUID);
					DSOutputStream.writeDicomFile(imageDS, dcmFile);
				}
				catch (Exception e)
				{
					Log.log("FR ERROR: " + e.getMessage());
				}
			}
		}
		try
		{
			java.sql.Date studySqlDate = new java.sql.Date(studyDate.getTime());
			String accession = accessionNumber;
			List studyList = PatientStudyView.get().selectWhere("studyUID = '" + patientSourceUID + "'");
			PatientStudy study1 = (PatientStudy) studyList.get(0);
			Patient patient = study1.getPatient();
			Study study2 = StudyView.get().selectByUID(studySourceUID);
			File tmpStudyDir = SSStore.get().createTempDir("mergeStudy");
			File sourceStudyDir = SSStore.get().getStudyDir(study2.studyID);
			FileUtil.forEachFile(sourceStudyDir, true, false, true, new mergeStudyFR(patient, study2, studySqlDate, accession, tmpStudyDir));
			DicomStore.get().loadDCMFileDataFromFilesystem(tmpStudyDir, false);
			FileUtil.deleteDirectoryContents(tmpStudyDir);
			tmpStudyDir.delete();
		}
		catch (Exception e)
		{
			Log.log("mergeStudies ERROR:" + e.getMessage());
		}
	}

	public String loadDemoStudies(MFDemoStudy study)
	{
		DS newHeaderInfo = new DS();
		DateFormat df = new SimpleDateFormat("yyyyMMdd");
		DateFormat dt = new SimpleDateFormat("HHmmss");
		Date now = new Date();
		String studyUID = "1.2.826.0.1.3680043.2.712." + now.getTime();
		newHeaderInfo.put(Tag.StudyInstanceUID, studyUID);
		newHeaderInfo.put(Tag.PatientName, "Siko^Amie");
		newHeaderInfo.put(Tag.PatientID, "99999999");
		newHeaderInfo.put(Tag.PatientBirthDate, "19610101");
		newHeaderInfo.put(Tag.PatientSex, "F");
		newHeaderInfo.put(Tag.StudyDescription, "Brain");
		newHeaderInfo.put(Tag.ReferringPhysicianName, "Mel Praktiss");
		newHeaderInfo.put(Tag.AccessionNumber, "");
		newHeaderInfo.put(Tag.StudyDate, df.format(now));
		newHeaderInfo.put(Tag.StudyTime, dt.format(now));
		newHeaderInfo.put(Tag.SourceApplicationEntityTitle, "AE_CT");
		newHeaderInfo.put(Tag.InstitutionName, "Glow Radiology LLC");
		Iterator<String> keys = study.getPairs().keySet().iterator();
		while (keys.hasNext())
		{
			String key = keys.next();
			Tag t = Tag.get(key);
			if (t != null)
			{
				newHeaderInfo.remove(t);
				newHeaderInfo.put(t, study.getValue(key));
			}
		}
		File srcDir = new File(SSStore.get().getRootDir(), "demo/" + study.getStudyType() + "/" + study.getStudyNumber());
		if (!srcDir.exists())
		{
			Util.log("Demo directory does not exist! " + srcDir.getAbsolutePath());
			return null;
		}
		if (srcDir.isFile())
		{
			Util.log("Demo directory is actually a file, " + srcDir.getAbsolutePath());
			return null;
		}
		imageCount = 1;
		Util.log("Loading demo studies from: " + srcDir.getAbsolutePath());
		seriesMap = new HashMap<String, String>();
		FileUtil.forEachFile(srcDir, true, false, true, new loadFile(newHeaderInfo));
		return studyUID;
	}
	private Integer					imageCount	= 0;
	private HashMap<String, String>	seriesMap;
	class loadFile extends FileRunnable
	{
		private DS	headerInfo	= null;

		public loadFile(DS header)
		{
			this.headerInfo = header;
		}

		@Override
		public void run(File f)
		{
			try
			{
				seriesMap = DicomStore.get().loadDemoDicomFile(f, headerInfo, imageCount++, seriesMap);
			}
			catch (Exception e)
			{
			}
		}
	}
}
