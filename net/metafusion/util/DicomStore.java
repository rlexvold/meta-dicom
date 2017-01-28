package net.metafusion.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.Iterator;
import java.util.Properties;
import net.metafusion.model.Image;
import net.metafusion.model.ImageView;
import net.metafusion.model.PatientView;
import net.metafusion.model.SeriesView;
import net.metafusion.model.StudyView;
import net.metafusion.msg.CStoreReq;
import acme.util.FileUtil;
import acme.util.Log;

class DicomStore
{
	static void log(String s)
	{
		Log.log(s);
	}

	Iterator getLLPatientIterator()
	{
		return patientView.selectAll().iterator();
	}

	Iterator getLLStudyIterator()
	{
		return studyView.selectAll().iterator();
	}

	Iterator getLLSeriesIterator()
	{
		return seriesView.selectAll().iterator();
	}

	Iterator getLLImageIterator()
	{
		return imageView.selectAll().iterator();
	}

	void deleteAll()
	{
		imageView.deleteAll();
		patientView.deleteAll();
		seriesView.deleteAll();
		studyView.deleteAll();
	}

	// todo: handle issue with store zfer syntax!!!!!
	// todo: handle issue with store zfer syntax!!!!!
	// todo: handle issue with store zfer syntax!!!!!
	// nb: not thread safe
	long getID(long lastID)
	{
		long id = System.currentTimeMillis();
		return id == lastID ? id + 1 : id;
	}
	// long lastImageID = 0;
	// long getNextImageID() {
	// lastImageID = getID(lastImageID);
	// return lastImageID;
	// }
	long lastPatientID = 0;

	long getNextPatientID()
	{
		lastPatientID = getID(lastPatientID);
		return lastPatientID;
	}
	long lastSeriesID = 0;

	long getNextSeriesID()
	{
		lastSeriesID = getID(lastSeriesID);
		return lastSeriesID;
	}
	long lastStudyID = 0;

	long getNextStudyID()
	{
		lastStudyID = getID(lastStudyID);
		return lastStudyID;
	}
	static DicomStore dicomStore = null;

	public static DicomStore get()
	{
		return dicomStore;
	}
	File root;
	ImageView imageView = ImageView.get();
	PatientView patientView = PatientView.get();
	SeriesView seriesView = SeriesView.get();
	StudyView studyView = StudyView.get();

	void load1(File f) throws Exception
	{
		// ImageMetaInfo imi = new ImageMetaInfo(f);
		// FileInputStream df = new FileInputStream(new File(f.getParentFile(),
		// imi.getObjectName()+".dat"));
		// //xxxDSInputStream dis = new xxxDSInputStream(df,
		// UID.ExplicitVRLittleEndian);
		//
		// DSFileView v = (DSFileView)DSFileView.viewMap.load(df);
		//
		//
		// Log(v.toString());
		// df.close(); // leaks
		//
		// String id = v.PatientID;
		// Patient p = id != null ? patientView.selectByExtID(id) : null;
		// if (id != null && p == null) {
		// imi.setPatientID(getNextPatientID());
		// p = new Patient(imi, v);
		// patientView.insert(p);
		// }
		// imi.setPatientID(p!=null?p.getPatientID():0);
		//
		//
		// id = v.StudyInstanceUID;
		// Study study = id != null ? studyView.selectByUID(id) : null;
		// if (id != null && study == null) {
		// imi.setStudyID(getNextStudyID());
		// study = new Study(imi, v);
		// studyView.insert(study);
		// }
		// imi.setStudyID(study!=null?study.getStudyID():0);
		//
		//
		// id = v.SeriesInstanceUID;
		// Series series = id != null ? seriesView.selectByUID(id) : null;
		// if (id != null && series == null) {
		// imi.setSeriesID(getNextSeriesID());
		// series = new Series(imi, v);
		// seriesView.insert(series);
		// }
		// imi.setSeriesID(series!=null?series.getSeriesID():0);
		//
		// id = imi.getMediaStorageSOPInstanceUID();
		// Image i = id != null ? (Image)imageView.selectByUID(id) : null;
		// if (id != null && i == null) {
		// imi.setImageID(getNextImageID());
		// i = new Image(imi, v);
		// imageView.insert(i);
		// }
		// imi.setImageID(i!=null?i.getImageID():0);
		// // store changes
		// imi.store(f);
	}

	void load(File root) throws Exception
	{
		if (!root.isDirectory()) throw new RuntimeException("must be dir: " + root);
		deleteAll();
		File fl[] = root.listFiles(new FilenameFilter()
		{
			public boolean accept(File f, String name)
			{
				return name.endsWith(".def");
			}
		});
		for (File f : fl)
		{
			load1(f);
		}
	}

	public DicomStore(File root) throws Exception
	{
		if (dicomStore != null) throw new Exception("multiple stores");
		dicomStore = this;
		this.root = root;
		//
		// uncomment to reload database from files
		//
		// load(root);
	}

	public boolean exists(String sopInstanceUID)
	{
		return imageView.selectByUID(sopInstanceUID) != null;
	}

	public Image getImage(String sopInstanceUID)
	{
		return imageView.selectByUID(sopInstanceUID);
	}

	// public File getImageFile(Image image) {
	// return new File(root, image.objectName+".dat");
	// }
	// public InputStream getImageStream(Image image) throws Exception {
	// FileInputStream fis = new FileInputStream(getImageFile(image));
	// return fis;
	// }
	// ImageMetaInfo getImageMetaInfoForCmd(UID xferSyntax, xxxDataSet storeCmd)
	// throws Exception {
	// ImageMetaInfo imi = new ImageMetaInfo();
	// imi.setTransferSyntax(xferSyntax.getUID());
	// imi.setMediaStorageSOPInstanceUID(storeCmd.getUID(Tag.MediaStorageSOPInstanceUID).getUID());
	// imi.setMediaStorageSOPClassUID(storeCmd.getUID(Tag.MediaStorageSOPClassUID).getUID());
	// String name = imi.getMediaStorageSOPInstanceUID().replace('.', '_');
	// // imi.setObjectName(name);
	// // imi.setImageID(getNextImageID());
	// return imi;
	// }
	Properties getPropsForCmd(UID xferSyntax, CStoreReq storeReq) throws Exception
	{
		Properties p = new Properties();
		p.put("objectName", storeReq.AffectedSOPInstanceUID.replace('.', '_'));
		p.put("MediaStorageSOPClassUID", storeReq.AffectedSOPClassUID);
		p.put("MediaStorageSOPInstanceUID", storeReq.AffectedSOPInstanceUID);
		p.put("TransferSyntaxUID", xferSyntax.getUID());
		p.put("imageID", "" + 0);
		p.put("patientID", "" + 0);
		p.put("seriesID", "" + 0);
		p.put("studyID", "" + 0);
		return p;
	}

	public boolean put(UID xferSyntax, CStoreReq storeReq, File dataFile) throws Exception
	{
		String fileName = null;
		boolean replace = false;
		try
		{
			Properties p = getPropsForCmd(xferSyntax, storeReq);
			fileName = (String) p.get("objectName");
			File f = new File(root, fileName + ".dat");
			replace = f.exists();
			if (replace && !f.delete()) throw new Exception("could not delete " + f);
			if (!FileUtil.rename(dataFile, f, true)) throw new Exception("could not rename " + dataFile);
			FileOutputStream fos = new FileOutputStream(new File(root, fileName + ".def"));
			try
			{
				p.store(fos, null);
			}
			finally
			{
				fos.close();
			}
		}
		catch (Exception e)
		{
			Log.log("dicomstore.put ", e);
			throw e;
		}
		load1(new File(root, fileName + ".def"));
		return replace;
	}

	/*
	 * public boolean put(UID xferSyntax, xxxDataSet storeCmd, InputStream is)
	 * throws Exception { Properties p = getPropsForCmd(xferSyntax, storeCmd);
	 * String fileName = (String)p.get("FileName"); File f = new File(root,
	 * fileName+".dat"); boolean replace = f.exists(); FileOutputStream fos =
	 * new FileOutputStream(f); Util.copyStream(is, fos); fos.close(); fos = new
	 * FileOutputStream(new File(root, fileName+".def")); p.store(fos, null);
	 * fos.close(); // exception handling return replace; }
	 */
	public Properties getProperties(String sopInstanceUID) throws Exception
	{
		return null; // (Properties)hm.get(sopInstanceUID);
	}

	public Iterator iterator()
	{
		return new Iterator()
		{
			// ***** remvoe this
			Iterator iter = imageView.selectAll().iterator();

			public boolean hasNext()
			{
				return iter.hasNext();
			}

			public Object next()
			{
				return iter.next();
			}

			public void remove()
			{
				assert false;
			}
		};
	}
	// public xxDicomStoreIterator getIterator(UID root, String level, DS tags)
	// {
	// return new xxDicomStoreIterator(root, level, tags);
	//
	// }
}
