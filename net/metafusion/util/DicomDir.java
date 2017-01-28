package net.metafusion.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.metafusion.Dicom;
import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSInputStream;
import net.metafusion.dataset.DSOutputStream;
import net.metafusion.localstore.DicomStore;
import net.metafusion.model.Image;
import net.metafusion.model.ImageView;
import net.metafusion.model.Patient;
import net.metafusion.model.PatientView;
import net.metafusion.model.Series;
import net.metafusion.model.SeriesView;
import net.metafusion.model.Study;
import acme.storage.SSInputStream;
import acme.storage.SSStore;
import acme.storage.SSStoreFactory;
import acme.util.FileUtil;
import acme.util.Log;
import acme.util.StringUtil;
import acme.util.Util;
import acme.util.ZipUtil;

public class DicomDir implements Runnable
{
	private static void log(String s)
	{
		Log.log(s);
	}

	private static void vlog(String s)
	{
		Log.vlog(s);
	}
	private File		rootDir;
	private ArrayList	patientList	= new ArrayList();
	private Map			patientMap	= new HashMap();

	private Iterator getPatientIter()
	{
		return patientList.iterator();
	}

	private Iterator getStudyIter(Patient p)
	{
		List l = (List) patientMap.get("" + p.getPatientID());
		return l.iterator();
	}

	// HACCKKKKK !!!!!!!!!!!!!!!!!!!!!! verify this
	// only one at a time RACE
	synchronized static public File CreateDicomDirZipForWeb(File studyDir, Study study)
	{
		// File outgoingRoot = new File(SSStoreFactory.getRoot(), "outgoing");
		// if (!outgoingRoot.exists())
		// throw new RuntimeException("outgoing dir missing
		// "+outgoingRoot.getAbsolutePath());
		File tempRoot = null;
		File dicomDirRoot = null;
		File zipFile = null;
		try
		{
			List studies = new ArrayList();
			studies.add(study);
			// WATCH RACE CONDITION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			String unique;
			for (;;)
			{
				java.sql.Date d = new java.sql.Date(System.currentTimeMillis());
				unique = "DICOMDIR" + DicomUtil.formatDate(d) + DicomUtil.formatTime(d);
				zipFile = new File(studyDir, unique + ".zip");
				if (zipFile.exists())
					Util.sleep(1000); // should never happen
				else
					break;
			}
			tempRoot = SSStore.get().getTempDir();// Util.generateUniqueName(
			// SSStoreFactory
			// .getStore().
			// getTempRoot(),
			// "DICOMDIR","");
			tempRoot.mkdir();
			dicomDirRoot = new File(tempRoot, unique);
			dicomDirRoot.mkdir();
			DicomDir dd = new DicomDir(dicomDirRoot, studies);
			File efilmZipFile = new File(SSStoreFactory.getRoot(), "efilm.zip");
			try
			{
				boolean b = ZipUtil.unzip(efilmZipFile, dicomDirRoot);
			}
			catch (Exception e)
			{
				Log.log("could not find/unzip efilm.zip in " + efilmZipFile.getAbsolutePath());
			}
			ZipUtil.zip(dicomDirRoot, zipFile);
			dd = null;
		}
		catch (Exception e)
		{
			Log.log("handleArchive caught ", e);
			Util.safeDelete(zipFile);
		}
		finally
		{
			FileUtil.safeDeleteDirRecursive(dicomDirRoot);
		}
		return zipFile;
	}

	static public File CreateDicomDirZipForArchive(List studies)
	{
		// File outgoingRoot = new File(SSStoreFactory.getRoot(), "outgoing");
		// if (!outgoingRoot.exists())
		// throw new RuntimeException("outgoing dir missing
		// "+outgoingRoot.getAbsolutePath());
		File tempRoot = null;
		File dicomDirRoot = null;
		File zipFile = null;
		try
		{
			// WATCH RACE CONDITION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			String unique;
			for (;;)
			{
				java.sql.Date d = new java.sql.Date(System.currentTimeMillis());
				unique = "DICOMDIR" + DicomUtil.formatDate(d) + DicomUtil.formatTime(d);
				zipFile = new File(SSStore.get().getTempRoot(), unique + ".zip");
				if (zipFile.exists())
					Util.sleep(1000); // should never happen
				else
					break;
			}
			tempRoot = SSStore.get().getTempDir();// Util.generateUniqueName(
			// SSStoreFactory
			// .getStore().
			// getTempRoot(),
			// "DICOMDIR","");
			tempRoot.mkdir();
			dicomDirRoot = new File(tempRoot, unique);
			dicomDirRoot.mkdir();
			DicomDir dd = new DicomDir(dicomDirRoot, studies);
			File efilmZipFile = new File(SSStoreFactory.getRoot(), "efilm.zip");
			boolean b = ZipUtil.unzip(efilmZipFile, dicomDirRoot);
			if (!b)
				Log.log("could not find/unzip efilm.zip in " + efilmZipFile.getAbsolutePath());
			ZipUtil.zip(dicomDirRoot, zipFile);
			dd = null;
		}
		catch (Exception e)
		{
			Log.log("handleArchive caught ", e);
			Util.safeDelete(zipFile);
		}
		finally
		{
			FileUtil.safeDeleteDirRecursive(dicomDirRoot);
		}
		return zipFile;
	}

	// synchronized static public File CreateDicomDirZipForArchive(File zipFile,
	// List studies) {
	// // File outgoingRoot = new File(SSStoreFactory.getRoot(), "outgoing");
	// // if (!outgoingRoot.exists())
	// // throw new RuntimeException("outgoing dir missing
	// "+outgoingRoot.getAbsolutePath());
	//
	// if (zipFile.exists())
	// throw new RuntimeException("zipFile exists")
	// File tempRoot = null;
	// File dicomDirRoot = null;
	// File zipFile = null;
	//
	// try {
	//
	// // WATCH RACE CONDITION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	// String unique;
	// for (;;) {
	// java.sql.Date d = new java.sql.Date(System.currentTimeMillis());
	// unique = "DICOMDIR"+DicomUtil.formatDate(d) + DicomUtil.formatTime(d);
	// zipFile = new File(studyDir, unique+".zip");
	// if (zipFile.exists())
	// Util.sleep(1000); // should never happen
	// else break;
	// }
	//
	// tempRoot = SSStore.get().getTempDir();//
	// Util.generateUniqueName(SSStoreFactory.getStore().getTempRoot(),"DICOMDIR"
	// ,"");
	// tempRoot.mkdir();
	//
	// dicomDirRoot = new File(tempRoot, unique);
	// dicomDirRoot.mkdir();
	//
	// DicomDir dd = new DicomDir(dicomDirRoot, studies);
	//
	// File efilmZipFile = new File(SSStoreFactory.getRoot(), "efilm.zip");
	// boolean b = ZipUtil.unzip(efilmZipFile, dicomDirRoot);
	// if (!b)
	// Log.log("could not find/unzip efilm.zip in
	// "+efilmZipFile.getAbsolutePath());
	//
	// ZipUtil.zip(dicomDirRoot, zipFile);
	// dd = null;
	// } catch (Exception e) {
	// Log.log("handleArchive caught ",e);
	// Util.safeDelete(zipFile);
	// } finally {
	// FileUtil.safeDeleteDirRecursive(tempRoot);
	// }
	// return zipFile;
	// }
	public DicomDir(File rootDir, List studies) throws Exception
	{
		this.rootDir = rootDir;
		if (!rootDir.exists())
			if (!rootDir.mkdir())
				throw new RuntimeException("DicomDir: mkdir " + rootDir + " failed");
		FileUtil.deleteDirectoryContents(rootDir);
		// add DICOMDIR level for efilm
		this.rootDir = new File(rootDir, "DICOM");
		if (!this.rootDir.mkdir())
			throw new RuntimeException("DicomDir: mkdir " + rootDir + " failed");
		for (Iterator iter = studies.iterator(); iter.hasNext();)
		{
			Study study = (Study) iter.next();
			Patient p = PatientView.get().select(study.getPatientID());
			if (p == null)
				throw new RuntimeException("DicomDir: could not find patient " + study.getPatientID());
			ArrayList al = (ArrayList) patientMap.get("" + p.getPatientID());
			if (al == null)
			{
				al = new ArrayList();
				patientMap.put("" + p.getPatientID(), al);
				patientList.add(p);
			}
			al.add(study);
		}
		runit();
	}

	private SSInputStream getImageStream(Image image) throws Exception
	{
		return DicomStore.get().getImageStream(image);
	}
	private class DSListIter
	{
		DSList		list;
		Iterator	iter;
		DS			next;

		DSListIter(DSList l)
		{
			iter = l.iterator();
			if (iter.hasNext())
				next = (DS) iter.next();
			else
				next = null;
		}

		DS next()
		{
			DS curr = next;
			if (iter.hasNext())
				next = (DS) iter.next();
			else
				next = null;
			return curr;
		}

		DS peek()
		{
			return next;
		}
	}

	private boolean isPatient(DS ds)
	{
		return ds.get(Tag.DirectoryRecordType).equals("PATIENT");
	}

	private boolean isStudy(DS ds)
	{
		return ds.get(Tag.DirectoryRecordType).equals("STUDY");
	}

	private boolean isSeries(DS ds)
	{
		return ds.get(Tag.DirectoryRecordType).equals("SERIES");
	}

	private boolean isImage(DS ds)
	{
		return ds.get(Tag.DirectoryRecordType).equals("IMAGE");
	}

	private String createDir(File root, String name)
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
				if (!success)
					throw new RuntimeException("could not create unique dir: " + f);
				return dirName;
			}
			dirName = name + (cnt++);
			if (cnt > 100)
				throw new RuntimeException("could not create unique dir: " + f);
		}
	}

	private String verifyFileName(File root, String name)
	{
		int cnt = 1;
		String fileName = name;
		assert root.isDirectory() && root.canWrite();
		for (;;)
		{
			File f = new File(root, fileName);
			if (!f.exists())
			{
				return fileName;
			}
			fileName = name + (cnt++);
			if (cnt > 100)
				throw new RuntimeException("could not create unique dir: " + f);
		}
	}

	private void runit() throws Exception
	{
		DSList dsList = new DSList();
		DS rootDS = new DS();
		rootDS.put(Tag.FileMetaInformationVersion, new byte[] { 0, 1 });
		rootDS.put(Tag.MediaStorageSOPClassUID, UID.MediaStorageDirectoryStorage);
		rootDS.put(Tag.MediaStorageSOPInstanceUID, Dicom.GenerateUniqueUID());
		// rootDS.put(Tag.TransferSyntaxUID, UID.ImplicitVRLittleEndian);
		rootDS.put(Tag.TransferSyntaxUID, UID.ExplicitVRLittleEndian);
		rootDS.put(Tag.ImplementationClassUID, Dicom.METAFUSION_UID_PREFIX);
		rootDS.put(Tag.ImplementationVersionName, Dicom.METAFUSION_IMPLEMENTATION_NAME);
		rootDS.put(Tag.FileSetID, "FILESET_ID");
		rootDS.put(Tag.RootDirectoryFirstRecord, new Integer(0));
		rootDS.put(Tag.RootDirectoryLastRecord, new Integer(0));
		rootDS.put(Tag.FileSetConsistencyFlag, new Integer(0));
		rootDS.put(Tag.DirectoryRecordSequence, dsList);
		// List l = PatientView.get().selectAll();
		for (Iterator iter = getPatientIter(); iter.hasNext();)
		{
			Patient p = (Patient) iter.next();
			String patientDirName = p.getDicomName();
			patientDirName = patientDirName != null ? StringUtil.stripNonAlphaNum(StringUtil.capitalize(patientDirName)) : "";
			if (patientDirName.length() == 0)
				patientDirName = "PATIENT";
			patientDirName = createDir(rootDir, patientDirName);
			File patientDir = new File(rootDir, patientDirName);
			vlog("adding " + p);
			DS ds = new DS();
			dsList.add(ds);
			ds.put(Tag.NextDirectoryRecord, new Integer(0));
			ds.put(Tag.RecordInUseFlag, new Short((short) -1));
			ds.put(Tag.LowerLevelDirectoryOffset, new Integer(0));
			ds.put(Tag.DirectoryRecordType, "PATIENT");
			ds.put(Tag.PatientName, p.getDicomName());
			ds.put(Tag.PatientID, p.getExtID());
			// List sl = StudyView.get().selectWhere("patientID =
			// "+p.getPatientID());
			for (Iterator iter2 = getStudyIter(p); iter2.hasNext();)
			{
				Study study = (Study) iter2.next();
				String studyDirName = DicomUtil.formatDate(study.getDate());
				if (studyDirName.length() == 0)
					studyDirName = "STUDY";
				studyDirName = createDir(patientDir, studyDirName);
				File studyDir = new File(patientDir, studyDirName);
				vlog("adding " + study);
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
					if (seriesDirName.length() == 0)
						seriesDirName = "SERIES";
					seriesDirName = createDir(studyDir, seriesDirName);
					File seriesDir = new File(studyDir, seriesDirName);
					vlog("adding " + series);
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
						if (imageName.length() == 0)
							imageName = "INSTANCE";
						imageName = verifyFileName(seriesDir, imageName);
						vlog("adding " + image);
						SSInputStream imageStream = null;
						OutputStream outputStream = null;
						try
						{
							imageStream = getImageStream(image);
							ImageMetaInfo imi = (ImageMetaInfo) imageStream.getMeta();
							UID syntax = UID.get(imi.getTransferSyntax());
							DS imageDS = DSInputStream.readFrom(imageStream, syntax, true); // todo
							// :
							// watch
							// endian
							// !!!!!!!!
							imageDS.put(Tag.FileMetaInformationVersion, new byte[] { 1, 0 });
							imageDS.put(Tag.MediaStorageSOPClassUID, image.getClassUID());
							imageDS.put(Tag.MediaStorageSOPInstanceUID, image.getImageUID());
							imageDS.put(Tag.TransferSyntaxUID, UID.ExplicitVRLittleEndian);
							imageDS.put(Tag.ImplementationClassUID, Dicom.METAFUSION_UID_PREFIX);
							imageDS.put(Tag.ImplementationVersionName, Dicom.METAFUSION_IMPLEMENTATION_NAME);
							DSOutputStream.writeDicomFile(imageDS, new File(seriesDir, imageName));
							imageDS = null;
						}
						catch (Exception e)
						{
							Log.log("ERROR creating DICOM file: ", e);
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
						ds.put(Tag.RefFileID, "DICOM" + "\\" + patientDirName + "\\" + studyDirName + "\\" + seriesDirName + "\\" + imageName);
						ds.put(Tag.RefSOPClassUIDInFile, image.getClassUID());
						ds.put(Tag.RefSOPInstanceUIDInFile, image.getImageUID());
						// ds.put(Tag.RefSOPTransferSyntaxUIDInFile,
						// image.getTransferSyntaxUID());
						ds.put(Tag.RefSOPTransferSyntaxUIDInFile, UID.ExplicitVRLittleEndian);
						ds.put(Tag.InstanceNumber, image.getInstanceNumber());
					}
				}
			}
		}
		// log(""+rootDS);
		HashMap offsetMap = rootDS.calculateOffsets(UID.ImplicitVRLittleEndian);
		// log(""+offsetMap);
		DSListIter iter = new DSListIter(dsList);
		DS lastPatient = null;
		DS lastStudy = null;
		DS lastSeries = null;
		DS lastImage = null;
		for (;;)
		{
			DS ds = iter.next();
			if (ds == null)
				break;
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
				// Integer(((Integer)offsetMap.get(dsList.getFirst())).intValue()
				// +128+4));
				// rootDS.put(Tag.RootDirectoryLastRecord, new
				// Integer(((Integer)offsetMap.get(dsList.getLast())).intValue()+
				// 128+4));
				if (lastPatient == null)
					rootDS.put(Tag.RootDirectoryFirstRecord, offset);
				else
					lastPatient.put(Tag.NextDirectoryRecord, offset);
				rootDS.put(Tag.RootDirectoryLastRecord, offset);
				lastPatient = ds;
			}
			else if (isStudy(ds))
			{
				lastSeries = null;
				lastImage = null;
				if (lastStudy == null)
					lastPatient.put(Tag.LowerLevelDirectoryOffset, offset);
				else
					lastStudy.put(Tag.NextDirectoryRecord, offset);
				lastStudy = ds;
			}
			else if (isSeries(ds))
			{
				lastImage = null;
				if (lastSeries == null)
					lastStudy.put(Tag.LowerLevelDirectoryOffset, offset);
				else
					lastSeries.put(Tag.NextDirectoryRecord, offset);
				lastSeries = ds;
			}
			else if (isImage(ds))
			{
				if (lastImage == null)
					lastSeries.put(Tag.LowerLevelDirectoryOffset, offset);
				else
					lastImage.put(Tag.NextDirectoryRecord, offset);
				lastImage = ds;
			}
		}
		rootDS.put(Tag.DataSetTrailingPadding, new byte[] { 0, 0 });
		// Log.get().force(""+rootDS);
		File f = new File(rootDir.getParentFile(), "DICOMDIR");
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
}
