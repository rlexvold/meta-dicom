package net.metafusion.tool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import net.metafusion.Dicom;
import net.metafusion.dataset.DS;
import net.metafusion.localstore.DicomStore;
import net.metafusion.model.Image;
import net.metafusion.model.ImageView;
import net.metafusion.model.Patient;
import net.metafusion.model.PatientView;
import net.metafusion.model.Series;
import net.metafusion.model.SeriesView;
import net.metafusion.model.Study;
import net.metafusion.model.StudyView;
import net.metafusion.util.DSList;
import net.metafusion.util.DicomUtil;
import net.metafusion.util.Tag;
import net.metafusion.util.UID;
import acme.db.DBManager;
import acme.util.FileUtil;
import acme.util.Log;
import acme.util.StringUtil;
import acme.util.Util;
import acme.util.XMLConfigFile;

public class DICOMDIRTool implements Runnable
{
	static void log(String s)
	{
		Log.log(s);
	}

	public DICOMDIRTool()
	{
	}

	InputStream getImageStream(Image image) throws Exception
	{
		return DicomStore.get().getImageStream(image);
	}
	class DSListIter
	{
		DSList list;
		Iterator iter;
		DS next;

		DSListIter(DSList l)
		{
			iter = l.iterator();
			if (iter.hasNext())
				next = (DS) iter.next();
			else next = null;
		}

		DS next()
		{
			DS curr = next;
			if (iter.hasNext())
				next = (DS) iter.next();
			else next = null;
			return curr;
		}

		DS peek()
		{
			return next;
		}
	}

	boolean isPatient(DS ds)
	{
		return ds.get(Tag.DirectoryRecordType).equals("PATIENT");
	}

	boolean isStudy(DS ds)
	{
		return ds.get(Tag.DirectoryRecordType).equals("STUDY");
	}

	boolean isSeries(DS ds)
	{
		return ds.get(Tag.DirectoryRecordType).equals("SERIES");
	}

	boolean isImage(DS ds)
	{
		return ds.get(Tag.DirectoryRecordType).equals("IMAGE");
	}

	String createDir(File root, String name)
	{
		int cnt = 1;
		String dirName = name;
		assert root.isDirectory() && root.canWrite();
		for (;;)
		{
			File f = new File(root, dirName);
			if (!f.exists())
			{
				boolean success = f.mkdir();
				if (!success) throw new RuntimeException("could not create unique dir: " + f);
				return dirName;
			}
			dirName = name + (cnt++);
			if (cnt > 100) throw new RuntimeException("could not create unique dir: " + f);
		}
	}

	String verifyFileName(File root, String name)
	{
		int cnt = 1;
		String fileName = name;
		assert root.isDirectory() && root.canWrite();
		for (;;)
		{
			File f = new File(root, fileName);
			if (!f.exists()) { return fileName; }
			fileName = name + (cnt++);
			if (cnt > 100) throw new RuntimeException("could not create unique dir: " + f);
		}
	}

	public void runit() throws Exception
	{
		File root = new File("/home/dicomdir");
		FileUtil.deleteDirectoryContents(root);
		DSList dsList = new DSList();
		DS rootDS = new DS();
		rootDS.put(Tag.FileMetaInformationVersion, new byte[] { 0, 1 });
		rootDS.put(Tag.MediaStorageSOPClassUID, UID.MediaStorageDirectoryStorage);
		rootDS.put(Tag.MediaStorageSOPInstanceUID, Dicom.GenerateUniqueUID());
		// rootDS.put(Tag.TransferSyntaxUID, UID.ImplicitVRLittleEndian); //
		// todo: watch this
		rootDS.put(Tag.TransferSyntaxUID, UID.ExplicitVRLittleEndian); // todo:
																		// watch
																		// this
		rootDS.put(Tag.ImplementationClassUID, Dicom.METAFUSION_UID_PREFIX);
		rootDS.put(Tag.ImplementationVersionName, Dicom.METAFUSION_IMPLEMENTATION_NAME);
		rootDS.put(Tag.FileSetID, "FILESET_ID");
		rootDS.put(Tag.RootDirectoryFirstRecord, new Integer(0));
		rootDS.put(Tag.RootDirectoryLastRecord, new Integer(0));
		rootDS.put(Tag.FileSetConsistencyFlag, new Integer(0));
		rootDS.put(Tag.DirectoryRecordSequence, dsList);
		List l = PatientView.get().selectAll();
		for (Iterator iter = l.iterator(); iter.hasNext();)
		{
			Patient p = (Patient) iter.next();
			String patientDirName = StringUtil.stripNonAlphaNum(StringUtil.capitalize(p.getDicomName()));
			if (patientDirName.length() == 0) patientDirName = "PATIENT";
			patientDirName = createDir(root, patientDirName);
			File patientDir = new File(root, patientDirName);
			log("adding " + p);
			DS ds = new DS();
			dsList.add(ds);
			ds.put(Tag.NextDirectoryRecord, new Integer(0));
			ds.put(Tag.RecordInUseFlag, new Short((short) -1));
			ds.put(Tag.LowerLevelDirectoryOffset, new Integer(0));
			ds.put(Tag.DirectoryRecordType, "PATIENT");
			ds.put(Tag.PatientName, p.getDicomName());
			ds.put(Tag.PatientID, p.getExtID());
			List sl = StudyView.get().selectWhere("patientID = " + p.getPatientID());
			for (Iterator iter2 = sl.iterator(); iter2.hasNext();)
			{
				Study study = (Study) iter2.next();
				String studyDirName = DicomUtil.formatDate(study.getDate());
				if (studyDirName.length() == 0) studyDirName = "STUDY";
				studyDirName = createDir(patientDir, studyDirName);
				File studyDir = new File(patientDir, studyDirName);
				log("adding " + study);
				ds = new DS();
				dsList.add(ds);
				ds.put(Tag.NextDirectoryRecord, new Integer(0));
				ds.put(Tag.RecordInUseFlag, new Short((short) -1));
				ds.put(Tag.LowerLevelDirectoryOffset, new Integer(0));
				ds.put(Tag.DirectoryRecordType, "STUDY");
				ds.put(Tag.StudyDate, DicomUtil.formatDate(study.getDate()));
				ds.put(Tag.StudyTime, DicomUtil.formatTime(study.getDate()));
				ds.put(Tag.AccessionNumber, study.getAccessionNumber());
				ds.put(Tag.StudyInstanceUID, study.getStudyUID());
				ds.put(Tag.StudyID, study.getStudyIDString());
				List seriesList = SeriesView.get().selectWhere("studyID = " + study.getStudyID());
				for (Iterator iter3 = seriesList.iterator(); iter3.hasNext();)
				{
					Series series = (Series) iter3.next();
					String seriesDirName = series.getModality();
					if (seriesDirName.length() == 0) seriesDirName = "SERIES";
					seriesDirName = createDir(studyDir, seriesDirName);
					File seriesDir = new File(studyDir, seriesDirName);
					log("adding " + series);
					ds = new DS();
					dsList.add(ds);
					ds.put(Tag.NextDirectoryRecord, new Integer(0));
					ds.put(Tag.RecordInUseFlag, new Short((short) -1));
					ds.put(Tag.LowerLevelDirectoryOffset, new Integer(0));
					ds.put(Tag.DirectoryRecordType, "SERIES");
					ds.put(Tag.Modality, series.getModality());
					ds.put(Tag.SeriesInstanceUID, series.getSeriesUID());
					ds.put(Tag.SeriesNumber, series.getSeriesNumber());
					List imageList = ImageView.get().selectWhere("seriesID = " + series.getSeriesID());
					for (Iterator iter4 = imageList.iterator(); iter4.hasNext();)
					{
						Image image = (Image) iter4.next();
						String imageName = image.getInstanceNumber();
						if (imageName.length() == 0) imageName = "INSTANCE";
						imageName = verifyFileName(seriesDir, imageName);
						log("adding " + image);
						InputStream imageStream = null;
						OutputStream outputStream = null;
						try
						{
							imageStream = getImageStream(image);
							outputStream = new FileOutputStream(new File(seriesDir, imageName));
							Util.copyStream(imageStream, outputStream);
						}
						finally
						{
							Util.safeClose(imageStream);
							Util.safeClose(outputStream);
						}
						ds = new DS();
						dsList.add(ds);
						ds.put(Tag.NextDirectoryRecord, new Integer(0));
						ds.put(Tag.RecordInUseFlag, new Short((short) -1));
						ds.put(Tag.LowerLevelDirectoryOffset, new Integer(0));
						ds.put(Tag.DirectoryRecordType, "IMAGE");
						ds.put(Tag.RefFileID, patientDirName + "\\" + studyDirName + "\\" + seriesDirName + "\\" + imageName);
						ds.put(Tag.RefSOPClassUIDInFile, image.getClassUID());
						ds.put(Tag.RefSOPInstanceUIDInFile, image.getImageUID());
						ds.put(Tag.RefSOPTransferSyntaxUIDInFile, image.getTransferSyntaxUID());
						ds.put(Tag.InstanceNumber, image.getInstanceNumber());
					}
				}
			}
		}
		log("" + rootDS);
		HashMap offsetMap = rootDS.calculateOffsets(UID.ImplicitVRLittleEndian);
		log("" + offsetMap);
		DSListIter iter = new DSListIter(dsList);
		DS lastPatient = null;
		DS lastStudy = null;
		DS lastSeries = null;
		DS lastImage = null;
		for (;;)
		{
			DS ds = iter.next();
			if (ds == null) break;
			Integer offset = new Integer(((Integer) offsetMap.get(ds)).intValue() + 128 + 4 + 20); // 20
																									// is
																									// hack
			// ds.put(Tag.FileMetaInformationVersion, ""+offset);
			if (isPatient(ds))
			{
				lastStudy = null;
				lastSeries = null;
				lastImage = null;
				// rootDS.put(Tag.RootDirectoryFirstRecord, new
				// Integer(((Integer)offsetMap.get(dsList.getFirst())).intValue()+128+4));
				// rootDS.put(Tag.RootDirectoryLastRecord, new
				// Integer(((Integer)offsetMap.get(dsList.getLast())).intValue()+128+4));
				if (lastPatient == null)
					rootDS.put(Tag.RootDirectoryFirstRecord, offset);
				else lastPatient.put(Tag.NextDirectoryRecord, offset);
				rootDS.put(Tag.RootDirectoryLastRecord, offset);
				lastPatient = ds;
			} else if (isStudy(ds))
			{
				lastSeries = null;
				lastImage = null;
				if (lastStudy == null)
					lastPatient.put(Tag.LowerLevelDirectoryOffset, offset);
				else lastStudy.put(Tag.NextDirectoryRecord, offset);
				lastStudy = ds;
			} else if (isSeries(ds))
			{
				lastImage = null;
				if (lastSeries == null)
					lastStudy.put(Tag.LowerLevelDirectoryOffset, offset);
				else lastSeries.put(Tag.NextDirectoryRecord, offset);
				lastSeries = ds;
			} else if (isImage(ds))
			{
				if (lastImage == null)
					lastSeries.put(Tag.LowerLevelDirectoryOffset, offset);
				else lastImage.put(Tag.NextDirectoryRecord, offset);
				lastImage = ds;
			}
		}
		log("" + rootDS);
		File f = new File(root, "DICOMDIR");
		FileOutputStream fos = new FileOutputStream(f);
		fos.write(new byte[128]);
		fos.write("DICM".getBytes());
		fos.close();
		rootDS.writeTo(f, UID.ExplicitVRLittleEndian, true);
	}

	public void run()
	{
		try
		{
			runit();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			log("run caught " + e);
			throw new RuntimeException(e.getMessage());
		}
	}

	public static void main(String[] args)
	{
		try
		{
			if (args.length < 1)
			{
				log("usage: DICOMDIR conf_file_path.xml ");
				System.exit(1);
			}
			System.out.println("DICOMDIR conf=" + args[0] + " targ=default");
			Util.parseArgv(args);
			XMLConfigFile configFile = new XMLConfigFile(new File(args[0]), "default");
			// Log.init("dd");
			DBManager.init();
			Dicom.init();
			DicomStore.init();
			new DICOMDIRTool().run();
		}
		catch (Exception e)
		{
			log("DICOMDIR caught " + e);
			e.printStackTrace();
		}
	}
}
