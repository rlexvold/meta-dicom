package net.metafusion.localstore;

import integration.MFServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.metafusion.Dicom;
import net.metafusion.admin.ForwardRuleBean;
import net.metafusion.admin.ServerBean;
import net.metafusion.admin.StorageRuleBean;
import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSFileView;
import net.metafusion.dataset.DSInputStream;
import net.metafusion.dataset.DSOutputStream;
import net.metafusion.localstore.sync.Sync;
import net.metafusion.model.DicomHeader;
import net.metafusion.model.DicomHeaderView;
import net.metafusion.model.Image;
import net.metafusion.model.ImageView;
import net.metafusion.model.Patient;
import net.metafusion.model.PatientView;
import net.metafusion.model.QueueView;
import net.metafusion.model.Series;
import net.metafusion.model.SeriesView;
import net.metafusion.model.Study;
import net.metafusion.model.StudyView;
import net.metafusion.util.DicomUtil;
import net.metafusion.util.GlobalProperties;
import net.metafusion.util.ImageMetaInfo;
import net.metafusion.util.InternalSelfCheck;
import net.metafusion.util.Tag;
import net.metafusion.util.UID;
import acme.db.JDBCUtil;
import acme.db.RowProcessor;
import acme.storage.SSInputStream;
import acme.storage.SSStore;
import acme.storage.SSStoreFactory;
import acme.util.FileUtil;
import acme.util.Log;
import acme.util.Util;
import acme.util.XML;

public class DicomStore
{
	private String	currentStudyUID	= null;

	static void log(String s)
	{
		Log.log(s);
	}

	static void log(String s, Exception e)
	{
		Log.log(s, e);
	}

	static void vlog(String s)
	{
		Log.vlog(s);
	}

	static void vlog(String s, Exception e)
	{
		Log.log(s, e);
	}
	static DicomStore	instance	= null;

	public static void init()
	{
		instance = new DicomStore(null);
	}

	public static void init(ServerBean serverBean)
	{
		instance = new DicomStore(serverBean);
	}

	public static DicomStore get()
	{
		return instance;
	}

	public long getNextID()
	{
		return store.getNextID();
	}
	SSStore		store;
	ServerBean	serverBean	= null;

	private DicomStore(ServerBean serverBean)
	{
		this.store = SSStoreFactory.getStore();
		this.serverBean = serverBean;
	}

	void setServerBean(ServerBean serverBean)
	{
		this.serverBean = serverBean;
	}

	public SSStore getSSStore()
	{
		return store;
	}

	public DicomQuery query(net.metafusion.util.UID root, String level, DS tags)
	{
		return new DicomQuery(root, level, tags);
	}

	public boolean exists(String sopInstanceUID)
	{
		return imageView.selectByUID(sopInstanceUID) != null;
	}

	public Image getImage(String sopInstanceUID)
	{
		return (Image) imageView.selectByUID(sopInstanceUID);
	}

	public boolean exists(long imageid)
	{
		return imageView.exists(imageid);
	}

	public Image getImage(long imageid)
	{
		return (Image) imageView.selectByID(imageid);
	}

	public long getSize(Image image)
	{
		return store.getSize(image.getStudyID(), image.getImageID());
	}

	// public SSMetaData getImageMetaData(Image image) throws Exception {
	// return store;
	// }
	public SSInputStream getImageStream(Image image) throws Exception
	{
		return store.getInputStream(image.getStudyID(), image.getImageID());
	}

	// // called by recover-thread or syncing or backup
	// public void put(long imageid, Object meta, InputStream is) throws
	// Exception {
	// if (store.exists(imageid)) {
	// Util.log("DicomStore imageid="+imageid+" already exists");
	// return;
	// }
	// String instanceUID = meta.get("MediaStorageSOPInstanceUID");
	// if (ImageView.get().exists(instanceUID)) {
	// Util.log("DicomStore instanceUID="+instanceUID+" already exists");
	// return;
	// }
	//
	// store.put(imageid, meta, is, type);
	// SSObject o = store.get(imageid);
	// load1(o, true, false);
	//
	// SyncCache.get().newImage(imageid);
	// UtilProcess.requestFreeSpaceCheck();
	// }
	// only called by recover and sync
	public void put(File f, boolean applyRules) throws Exception
	{
		ImageMetaInfo meta = (ImageMetaInfo) SSStore.get().getMetaFromFile(f);
		put(meta, f, applyRules, null, null);
	}

	public void put(File f) throws Exception
	{
		put(f, false);
	}

	public void put(File f, ImageMetaInfo meta, HashMap replaceUIDMap) throws Exception
	{
		put(meta, f, false, replaceUIDMap, null);
	}

	public File addFileToStudy(Study study, File f, String extension) throws Exception
	{
		assert f.exists();
		long id = this.getNextID();
		File studyFile = new File(store.getStudyDir(study.getStudyID()), "" + id + extension);
		assert !studyFile.exists();
		boolean ok = FileUtil.rename(f, studyFile, true);
		if (!ok)
			throw new RuntimeException("could not move file into study " + f.getAbsolutePath() + " " + studyFile.getAbsolutePath());
		return studyFile;
	}

	// called from dicom
	public void putWithRules(ImageMetaInfo imi, File f) throws Exception
	{
		put(imi, f, true, null, null);
	}

	public void putWithRules(ImageMetaInfo imi, File f, String originAET) throws Exception
	{
		put(imi, f, true, null, originAET);
	}

	public String putWithRulesReturnStudyUID(ImageMetaInfo imi, File f, String originAET) throws Exception
	{
		put(imi, f, true, null, originAET);
		return currentStudyUID;
	}

	public void putWithoutRules(ImageMetaInfo imi, File f) throws Exception
	{
		put(imi, f, false, null, null);
	}

	private void put(ImageMetaInfo imi, File f, boolean applyRules, HashMap replaceUIDMap, String originAET) throws Exception
	{
		SSInputStream sis = null;
		if (UtilProcess.freeSpaceWarning())
		{
			log("!!!!!!!!!!!!!!!!!!!!!!  FREE SPACE WARNING !!!!!!!!!!!!!!!!!!");
			log("!!!!!!!!!!!!!!!!!!!!!!  REJECTING STORAGE PUT !!!!!!!!!!!!!!!!!!");
			UtilProcess.requestFreeSpaceRulesCheck();
			throw new RuntimeException("FREE SPACE WARNING");
		}
		try
		{
			DS ds = null;
			DSFileView v = null;
			if (f.getName().endsWith(".dcm"))
			{
				Object[] objs = DSFileView.viewMap.loadReturnDS(f);
				v = (DSFileView) objs[0];
				ds = (DS) objs[1];
			}
			else
			{
				sis = new SSInputStream(f);
				Object[] objs = DSFileView.viewMap.loadReturnDS(sis, UID.get(imi.getTransferSyntax()));
				v = (DSFileView) objs[0];
				ds = (DS) objs[1];
				sis.close();
				sis = null;
			}
			if (!imi.getMediaStorageSOPInstanceUID().equals(v.SOPInstanceUID))
				throw new RuntimeException("put: !imi.getMediaStorageSOPInstanceUID().equals(v.SOPInstanceUID");
			// this is a hack for build loading
			if (replaceUIDMap != null)
			{
				String uid = (String) replaceUIDMap.get(v.StudyInstanceUID);
				if (uid != null)
					v.StudyInstanceUID = uid;
				else
					log("no replacement for study " + uid);
				uid = (String) replaceUIDMap.get(v.SeriesInstanceUID);
				if (uid != null)
					v.SeriesInstanceUID = uid;
				else
					log("no replacement for series " + uid);
				uid = (String) replaceUIDMap.get(imi.getMediaStorageSOPInstanceUID());
				if (uid != null)
					imi.setMediaStorageSOPInstanceUID(uid);
				else
					log("no replacement for image " + uid);
			}
			// start synchronized
			// boolean storeExists = store.exists(studyid, imageid);
			// boolean imageViewExists =
			// ImageView.get().exists(v.SOPInstanceUID);
			// if (storeExists) {
			// Util.log("DicomStore study="+studyid+"imageid="+imageid+" already
			// exists");
			// }
			// if (imageViewExists) {
			// Util.log("DicomStore instanceUID="+v.SOPInstanceUID+" already
			// exists");
			// }
			// if (storeExists && imageViewExists) {
			// Util.log("study and store exist", skipping put)
			// return;
			// }
			Study study = null;
			Series series = null;
			Image image = null;
			synchronized (this)
			{
				String id = v.PatientID;
				// if no patient id, use StudyInstanceUID as patientid
				if (id == null || id.length() == 0)
				{
					id = v.StudyInstanceUID;
					v.PatientID = id;
				}
				// RAL - ExtID is not unique enough, since different
				// institutions could duplicate it. Now will key on ExtID,
				// PatientName, DOB, and Sex)
				// Patient p = patientView.selectByExtID(id);
				Patient p = patientView.selectUnique(id, v.PatientName, v.PatientBirthDate, v.PatientSex);
				if (id != null && p == null)
				{
					imi.setPatientID(getNextID());
					p = new Patient(imi, v);
					patientView.insert(p);
				}
				imi.setPatientID(p != null ? p.getPatientID() : 0);
				boolean risNotify = false;
				currentStudyUID = v.StudyInstanceUID;
				String checkUrl = "PatientName=" + v.PatientName + "/Modality=" + v.Modality + "/InstitutionName=" + v.InstitutionName + "/StudyDate=" + v.StudyDate
						+ "/StationName=" + v.StationName;
				InternalSelfCheck.setLastStudy(checkUrl);
				study = currentStudyUID != null ? studyView.selectByUID(currentStudyUID) : null;
				if (currentStudyUID != null && study == null)
				{
					imi.setStudyID(getNextID());
					char origin = Study.ORIGIN_LOCAL;
					study = new Study(imi, v, origin, "");
					if (origin == Study.ORIGIN_LOCAL && applyRules)
					{
						study.setState("U");
						if (LocalStore.get() != null && LocalStore.get().IsRIS())
						{
							MFServer.get().newStudyArrived(v);
						}
					}
					else
						study.setState("A");
					if (originAET != null)
						study.setOriginAET(originAET);
					studyView.insert(study);
				}
				else
				{
					study.setDateLastImage(new Timestamp(System.currentTimeMillis()));
					if (originAET != null)
						study.setOriginAET(originAET);
					studyView.update(study); // update last modified date
				}
				imi.setStudyID(study != null ? study.getStudyID() : 0);
				// / HACK !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				// move this when integrating ris
				if (applyRules)
				{
					try
					{
						String countSQL = "select count(*) from web_study where dcm_studyid= " + study.getStudyID() + " ";
						int count = JDBCUtil.get().selectInt(countSQL);
						if (count == 0)
						{
							String insertSQL = "insert into web_study(dcm_studyid) values (" + study.getStudyID() + ")";
							Log.vlog(insertSQL);
							JDBCUtil.get().update(insertSQL);
						}
					}
					catch (Exception e)
					{
						Log.log("CREATED ZIP HACK caught " + e, e);
					}
				}
				// / HACK !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				id = v.SeriesInstanceUID;
				series = id != null ? seriesView.selectByUID(id) : null;
				if (id != null && series == null)
				{
					imi.setSeriesID(getNextID());
					series = new Series(imi, v);
					seriesView.insert(series);
					String modalities = study.getModalities() != null ? study.getModalities() : "";
					String modality = series.getModality() != null ? series.getModality() : "";
					if (modality.length() != 0 && modalities.indexOf(modality) == -1)
					{
						if (modalities.length() != 0)
							modalities += "\\";
						modalities += modality; // todo: watch for ris bugs here
						study.setModalities(modalities);
						StudyView.get().update(study);
					}
				}
				imi.setSeriesID(series != null ? series.getSeriesID() : 0);
				id = imi.getMediaStorageSOPInstanceUID();
				image = id != null ? (Image) imageView.selectByUID(id) : null;
				if (id != null && image == null)
				{
					imi.setImageID(getNextID());
					image = new Image(imi, v);
					imageView.insert(image);
					seriesView.updateCount(series.getSeriesID(), series.getCount() + 1);
					Boolean fullHeader = (Boolean) GlobalProperties.get().get("storeDicomHeaders");
					if (fullHeader != null && fullHeader == true)
					{
						try
						{
							DicomHeaderView.get().insertFullHeader(image.imageID, ds);
						}
						catch (Exception e)
						{
							Log.log("Error trying to store full header in header table.  Image is still proceeding", e);
						}
						// DicomHeader dh = new DicomHeader();
						// dh.setImageID(image.imageID);
						// dh.setDicomHeader(ds);
						// DicomHeaderView.get().insert(dh);
					}
				}
				imi.setImageID(image != null ? image.getImageID() : 0);
			}
			// small race between data in database and file ready
			// rewrite meta
			File fullFile = store.putMetaFile(imi.getStudyID(), imi.getImageID(), imi, f);
			// RAL removed threaded aspect, see if it fixes heap problem
			// if (applyRules) DicomUtil.scheduleConvertImageToJpeg(image);
			if (applyRules)
				DicomUtil.convertImageToJpeg(image, true);
			if (SyncCache.get() != null)
				SyncCache.get().newImage(imi.getImageID());
			UtilProcess.requestFreeSpaceRulesCheck();
			if (LocalStore.get() != null && LocalStore.get().IsRIS())
				MFServer.get().flushStudy(imi.getStudyID());
			if (applyRules)
				applyRules(study, series, image);
			if (LocalStore.get() != null && LocalStore.get().IsSync() && applyRules)
				Sync.get().put(fullFile);
		}
		finally
		{
			Util.safeClose(sis);
		}
	}

	void applyRules(Study study, Series series, Image i)
	{
		Calendar c = Calendar.getInstance();
		int minutesIntoDay = c.get(Calendar.MINUTE) + c.get(Calendar.HOUR_OF_DAY) * 60;
		// String doc = ds.getString(Tag.NameOfPhysicianReadingStudy);
		// Log.info("enqueue mf_andromeda "+i.getStoredObjectID());
		// QueueView.get().enqueue("mf_andromeda", "push",i.getStoredObjectID(),
		// null);
		if (serverBean != null)
		{
			ForwardRuleBean fbArray[] = serverBean.getForwardRule();
			int ruleCount = 0;
			for (int j = 0; j < fbArray.length; j++)
			{
				ForwardRuleBean frb = fbArray[j];
				if (!frb.isEnabled() || !frb.isTimeValid(minutesIntoDay))
					continue;
				ruleCount++;
			}
			vlog("apple forward rules: count=" + ruleCount);
			for (int j = 0; j < fbArray.length; j++)
			{
				ForwardRuleBean frb = fbArray[j];
				if (!frb.isEnabled() || !frb.isTimeValid(minutesIntoDay))
					continue;
				applyRule(frb, study, series, i);
			}
		}
	}

	public void cleanupDB()
	{
		// PatientView pv = new PatientView().
		String q = "select dcm_patient.patientid, count(dcm_image.patientid) from dcm_patient left join dcm_image on dcm_patient.patientid = dcm_image.patientid group by dcm_image.patientid";
		JDBCUtil.get().select(q, new RowProcessor()
		{
			public void processRow(ResultSet rs) throws Exception
			{
				log("cleanupDB: " + rs.getString(1) + " " + rs.getString(2));
			}
		});
	}

	public long deleteStudy(Study study) throws Exception
	{
		long size = 0;
		log("delete study " + study + " from database");
		long patientID = study.getPatientID();
		List seriesList = SeriesView.get().selectByStudy(study.getStudyID());
		for (Iterator iter = seriesList.iterator(); iter.hasNext();)
		{
			Series s = (Series) iter.next();
			size += deleteSeries(s);
		}
		StudyView.get().delete(study);
		try
		{
			long deleteid = getNextID();
			String insertSQL = "insert into dcm_delete(deleteid,studyid) values ( " + deleteid + "," + study.getStudyID() + ")";
			JDBCUtil.get().update(insertSQL);
		}
		catch (Exception e)
		{
			Log.log("insert dcm_delete entry  " + e, e);
		}
		int patientImageCount = PatientView.get().countImages(patientID);
		if (patientImageCount == 0)
		{
			log("Delete patient " + patientID);
			PatientView.get().delete(patientID);
		}
		// / delete web_view hack
		try
		{
			String delSQL = "delete from  web_study where dcm_studyid= '" + study.getStudyID() + "' ";
			JDBCUtil.get().update(delSQL);
		}
		catch (Exception e)
		{
			Log.log("delete from  web_study  " + e, e);
		}
		try
		{
			String delSQL = "delete from  web_assign where dcm_studyid= '" + study.getStudyID() + "' ";
			JDBCUtil.get().update(delSQL);
		}
		catch (Exception e)
		{
			Log.log("delete from  web_assign " + e, e);
		}
		// / delete web_view hack
		// delete dir, .zip file and anything else left behinf
		store.deleteStudy(study.getStudyID());
		if (LocalStore.get().IsSync())
		{
			Sync.get().putDelete(study.getStudyUID());
		}
		return size;
	}

	private long deleteSeries(Series series) throws Exception
	{
		long size = 0;
		log("delete series " + series + " from database");
		List imageList = ImageView.get().selectBySeries(series.getSeriesID());
		for (Iterator iter = imageList.iterator(); iter.hasNext();)
		{
			Image i = (Image) iter.next();
			size += deleteImage(i);
		}
		SeriesView.get().delete(series);
		return size;
	}

	private long deleteImage(Image image) throws Exception
	{
		long size;
		log("delete image " + image + " from database");
		long id = image.getImageID();
		size = store.delete(image.getStudyID(), id);
		DicomHeaderView.get().deleteWhere("imageID=" + id);
		ImageView.get().delete(image);
		return size;
	}

	public void deleteFromDB(long imageid) throws Exception
	{
		log("SYNC: removing " + imageid + " from database");
		ImageView imageView = ImageView.get();
		Image image = imageView.selectByID(imageid);
		if (image == null)
		{
			log("delete image " + imageid + " not found in database");
			return;
		}
		long seriesID = image.getSeriesID();
		long studyID = image.getStudyID();
		long patientID = image.getPatientID();
		if (!imageView.delete(image))
			log("deleteFromDB: could not delete image " + imageid);
		if (imageView.countImagesForSeries(seriesID) == 0)
		{
			log("delete series " + seriesID);
			if (!SeriesView.get().delete(seriesID))
				log("could not delete seriesID=" + seriesID);
		}
		if (imageView.countImagesForStudy(studyID) == 0)
		{
			log("delete study " + studyID);
			if (!StudyView.get().delete(studyID))
				log("could not delete studyID=" + studyID);
		}
		if (imageView.countImagesForPatient(patientID) == 0)
		{
			log("delete patient " + patientID);
			if (!PatientView.get().delete(patientID))
				log("could not delete patientID=" + patientID);
		}
	}
	// nb: not thread safe
	// long lastImageID = 0;
	// synchronized long getNextImageID() {
	// lastImageID = getID(lastImageID);
	// return lastImageID;
	// }
	// synchronized long getNextPatientID() {
	// return SSStore.get().getNextID();
	// }
	// synchronized long getNextSeriesID() {
	// return SSStore.get().getNextID();
	// }
	// synchronized long getNextStudyID() {
	// return SSStore.get().getNextID();
	// }
	ImageView	imageView	= ImageView.get();
	PatientView	patientView	= PatientView.get();
	SeriesView	seriesView	= SeriesView.get();
	StudyView	studyView	= StudyView.get();

	void forward(Image i, String ae)
	{
		log("applyRule forward:" + i.getImageID() + " ae=" + ae);
		// QueueServer.get().addSendImage(i.getImageUID(),ae);
		ForwardProcess.forward(i.getImageID(), ae);
	}

	boolean contains(String s, String sub)
	{
		if (s == null || sub == null)
			return false;
		if (s.toUpperCase().indexOf(sub.toUpperCase()) != -1)
			return true;
		else
			return false;
	}

	boolean applyRule(ForwardRuleBean frb, Study study, Series series, Image i)
	{
		String arg = frb.getArg();
		String ae = frb.getDestAE();
		switch (frb.getType())
		{
			case ForwardRuleBean.OF_MODALITY_TYPE:
				if ("%".equals(arg) || contains(series.getModality(), arg))
					forward(i, ae);
				return true;
			case ForwardRuleBean.TO_RADIOLOGIST_TYPE:
				if ("%".equals(arg) || contains(study.getNameOfPhysicianReadingStudy(), arg))
					forward(i, ae);
				return true;
			case ForwardRuleBean.FROM_SOURCE_TYPE:
				if ("%".equals(arg) || contains(series.getStationName(), arg))
					forward(i, ae);
				return true;
			case ForwardRuleBean.FROM_PHYSICIAN_TYPE:
				if ("%".equals(arg) || contains(study.getReferringPhysicianName(), arg))
					forward(i, ae);
				return true;
		}
		return false;
	}

	// only one insert at a time.
	// synchronized void load1(ImageMetaInfo imi, DSFileView v) throws Exception
	// {
	// try {
	//
	// String id = v.PatientID;
	// // if no patient id, use StudyInstanceUID as patientid
	// if (id == null || id.length() == 0) {
	// id = v.StudyInstanceUID;
	// v.PatientID = id;
	// }
	//
	// Patient p = patientView.selectByExtID(id);
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
	// study = new Study(imi, v);
	// studyView.insert(study);
	// }
	// imi.setStudyID(study!=null?study.getStudyID():0);
	//
	// /// HACK !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	// try {
	// String countSQL = "select count(*) from web_study where dcm_studyid=
	// "+study.getStudyID()+" ";
	// int count = JDBCUtil.get().selectInt(countSQL);
	// if (count == 0) {
	// String insertSQL = "insert into web_study(dcm_studyid) values
	// ("+study.getStudyID()+")";
	// Log.log(insertSQL);
	// JDBCUtil.get().update(insertSQL);
	// }
	// } catch (Exception e) {
	// Log.log("CREATED ZIP HACK caught "+e);
	// e.printStackTrace();
	// }
	// /// HACK !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	//
	// id = v.SeriesInstanceUID;
	// Series series = id != null ? seriesView.selectByUID(id) : null;
	// if (id != null && series == null) {
	// imi.setSeriesID(getNextSeriesID());
	// series = new Series(imi, v);
	// seriesView.insert(series);
	// String modalities = study.getModalities() != null ? study.getModalities()
	// : "";
	// String modality = series.getModality() != null ? series.getModality() :
	// "";
	// if (modality.length() != 0 && modalities.indexOf(modality)== -1) {
	// if (modalities.length() != 0)
	// modalities += "\\";
	// modalities += modality;
	// study.setModalities(modalities);
	// StudyView.get().update(study);
	// }
	// }
	// imi.setSeriesID(series!=null?series.getSeriesID():0);
	//
	// id = imi.getMediaStorageSOPInstanceUID();
	// Image i = id != null ? (Image)imageView.selectByUID(id) : null;
	// if (id != null && i == null) {
	// i = new Image(imi, v);
	// imageView.insert(i);
	// }
	// imi.setImageID(i!=null?i.getImageID():0);
	//
	//
	// } catch (Exception e) {
	// log("load1 error",e);
	// } finally {
	// ;
	// }
	// }
	//
	public void syncFilesAndDatabase()
	{
		// try {
		// Log.log("SyncFilesAndDatabase "+new java.util.Date());
		// // first add missing files with database
		// // List ids = store.getIDs();
		// List ids = null;
		// //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!11store.getIDs();
		// for (Iterator iter=ids.iterator();iter.hasNext();) {
		// try {
		// long id = ((Long)iter.next()).longValue();
		// if (!store.isValid(id)) {
		// Log.log("SYNC: id "+id+" is NOT consistent on disk. cannot fix
		// !!!!!!!!!!!!!");
		// store.delete(id);
		// continue;
		// }
		// if (store.exists(id)) {
		// SSObject o = store.get(id);
		// Image image = getImage(id);
		// if (image == null) {
		// Log.log("SYNC: id="+id+" is on disk, NOT in database. fixing
		// !!!!!!!!!");
		// Log.log("syncing id ="+id);
		// recover(id);
		// }
		// }
		// } catch (Exception e) {
		// Log.log("SYNC: file recover failede", e);
		// }
		// }
		// // now match db with files
		// List dbIDs = getAllObjectIDs(0);
		// for (Iterator iter=dbIDs.iterator();iter.hasNext();) {
		// long id = ((Long)iter.next()).longValue();
		// boolean delete = false;
		// if (!store.isValid(id)) {
		// Log.log("SYNC: id "+id+" is NOT consistent on disk. removing
		// !!!!!!!!!!!!!");
		// store.delete(id);
		// delete = true;
		// }
		// if (!store.exists(id)) {
		// Log.log("SYNC: id "+id+" is not on disk. !!!!!!!!!!!!!");
		// delete = true;
		// }
		// if (delete) {
		// Log.log("SYNC: removing "+id+" from database");
		// deleteFromDB(id);
		// }
		// }
		//
		// } catch (Exception e) {
		// log("syncfilesdb", e);
		// } finally {
		// log("Exit finally");
		// }
	}

	void deleteStudiesOlderThan(Date d) throws Exception
	{
		List old = StudyView.get().selectOlderThan(d);
		for (Iterator iter = old.iterator(); iter.hasNext();)
		{
			Study s = (Study) iter.next();
			Log.log("Delete old study !!!!!!!!!!!!!!!! " + s);
			deleteStudy(s);
		}
	}

	private void xxxxxdeleteStudiesIfFreeSpaceLessThan(long bytes) throws Exception
	{
		long caps[] = store.getCapacityAndFreeSpace();
		int minToDelete = 50;
		if (caps[1] * 1024l < bytes)
		{
			List old = StudyView.get().selectOldest(minToDelete);
			for (Iterator iter = old.iterator(); iter.hasNext();)
			{
				Study s = (Study) iter.next();
				log("Delete old study !!!!!!!!!!!!!!!! " + s);
				deleteStudy(s);
			}
		}
	}

	void deleteStudiesIfFull(int hiPer, int loPer) throws Exception
	{
		int perUsed = store.getUsedPer();
		if (perUsed < hiPer)
			return;
		boolean done = false;
		while (!done)
		{
			List old = StudyView.get().selectOldest(500);
			if (old.size() == 0)
				break;
			for (Iterator iter = old.iterator(); !done && iter.hasNext();)
			{
				Study s = (Study) iter.next();
				log("Delete old study (full)!!!!!!!!!!!!!!!! " + s);
				deleteStudy(s);
				perUsed = store.getUsedPer();
				done = perUsed <= loPer;
				log("delete perUsed=" + perUsed + " done=" + done);
			}
		}
	}

	void apply(StorageRuleBean srb)
	{
		if (!srb.isEnabled())
			return;
		if (srb.getArg1() == null || srb.getArg1().length() == 0)
			return;
		if (srb.getType() == StorageRuleBean.AGE_TYPE)
		{
			try
			{
				long days = Long.parseLong(srb.getArg1());
				long d = System.currentTimeMillis() - (days * 24l * 60l * 60l * 1000l);
				deleteStudiesOlderThan(new Date(d));
			}
			catch (Exception e)
			{
				log("could not apply age rule", e);
			}
		}
		else if (srb.getType() == StorageRuleBean.FREESPACE_TYPE)
		{
			try
			{
				// long hwBytes = (long)(Double.parseDouble(srb.getArg1()) *
				// 1024*1024*1024);
				// long lwBytes = (long)(Double.parseDouble(srb.getArg2()) *
				// 1024*1024*1024);
				int hiPer = Integer.parseInt(srb.getArg1());
				int loPer = Integer.parseInt(srb.getArg2());
				deleteStudiesIfFull(hiPer, loPer);
			}
			catch (Exception e)
			{
				log("could not apply freespace rule", e);
			}
		}
	}

	public void applyFreeSpaceRules()
	{
		int ruleCount = 0;
		StorageRuleBean srb[] = this.serverBean.getStorageRule();
		for (int i = 0; i < srb.length; i++)
			if (srb[i].isEnabled())
				ruleCount++;
		log("applyFreeSpaceRules count=" + ruleCount);
		for (int i = 0; i < srb.length; i++)
			if (srb[i].isEnabled())
				apply(srb[i]);
	}

	public void loadDicomFile(File dicomFile, boolean applyRules) throws Exception
	{
		UID ts = UID.ImplicitVRLittleEndian;
		DS ds = DSInputStream.readFileAndImages(dicomFile);
		ImageMetaInfo imi = new ImageMetaInfo();
		imi.setMediaStorageSOPClassUID(ds.getString(Tag.MediaStorageSOPClassUID));
		imi.setMediaStorageSOPInstanceUID(ds.getString(Tag.MediaStorageSOPInstanceUID));
		imi.setTransferSyntax(ts.getUID()); // imvrle
		ds.remove(Tag.FileMetaInformationVersion);
		ds.remove(Tag.MediaStorageSOPClassUID);
		ds.remove(Tag.MediaStorageSOPInstanceUID);
		ds.remove(Tag.TransferSyntaxUID);
		ds.remove(Tag.ImplementationClassUID);
		ds.remove(Tag.ImplementationVersionName);
		ds.remove(Tag.SourceApplicationEntityTitle);
		ds.remove(Tag.PrivateInformationCreatorUID);
		ds.remove(Tag.PrivateInformation);
		File tempFile = null;
		OutputStream os = null;
		try
		{
			tempFile = store.createTempFile(".dcmimp");
			os = SSStore.get().getOutputStream(tempFile);
			ds.writeTo(os, ts);
			os.close();
			os = null;
			if (applyRules)
				putWithRules(imi, tempFile);
			else
				putWithoutRules(imi, tempFile);
			Util.safeDelete(tempFile);
		}
		finally
		{
			Util.safeClose(os);
		}
	}

	public HashMap<String, String> loadDemoDicomFile(File dicomFile, DS newHeaderInfo, Integer imageCount, HashMap<String, String> seriesMap) throws Exception
	{
		UID ts = UID.ImplicitVRLittleEndian;
		DS ds = DSInputStream.readFileAndImages(dicomFile);
		String studyUID = newHeaderInfo.getString(Tag.StudyInstanceUID);
		String seriesUID = ds.getString(Tag.SeriesInstanceUID);
		if (seriesUID == null)
			return seriesMap;
		String tmpUID = seriesMap.get(seriesUID);
		if (tmpUID == null)
		{
			tmpUID = studyUID + "." + seriesMap.size();
			seriesMap.put(seriesUID, tmpUID);
		}
		seriesUID = tmpUID;
		String instanceNumber = ds.getString(Tag.InstanceNumber);
		if (instanceNumber == null)
			instanceNumber = imageCount.toString();
		String imageUID = seriesUID + "." + instanceNumber;
		newHeaderInfo.put(Tag.SeriesInstanceUID, seriesUID);
		newHeaderInfo.put(Tag.MediaStorageSOPInstanceUID, imageUID);
		newHeaderInfo.put(Tag.SOPInstanceUID, imageUID);
		Iterator<Tag> i = newHeaderInfo.getTags().iterator();
		while (i.hasNext())
		{
			Tag tmp = i.next();
			ds.remove(tmp);
			ds.put(tmp, newHeaderInfo.get(tmp));
		}
		ds.put(Tag.TransferSyntaxUID, ts.getUID());
		File tempFile = null;
		OutputStream os = null;
		try
		{
			tempFile = store.createTempFile(".dcm");
			os = SSStore.get().getOutputStream(tempFile);
			ds.writeTo(os, ts);
			os.close();
			loadDicomFile(tempFile, true);
		}
		finally
		{
			Util.safeClose(os);
			Util.safeDelete(tempFile);
		}
		return seriesMap;
	}

	public void testLoadDicomFile(File dicomFile) throws Exception
	{
		DS ds = DSInputStream.readFileAndImages(dicomFile);
		ImageMetaInfo imi = new ImageMetaInfo();
		imi.setMediaStorageSOPClassUID(ds.getString(Tag.MediaStorageSOPClassUID));
		imi.setMediaStorageSOPInstanceUID(ds.getString(Tag.MediaStorageSOPInstanceUID));
		imi.setTransferSyntax(UID.ImplicitVRLittleEndian.getUID()); // imvrle
		ds.remove(Tag.FileMetaInformationVersion);
		ds.remove(Tag.MediaStorageSOPClassUID);
		ds.remove(Tag.MediaStorageSOPInstanceUID);
		ds.remove(Tag.TransferSyntaxUID);
		ds.remove(Tag.ImplementationClassUID);
		ds.remove(Tag.ImplementationVersionName);
		ds.remove(Tag.SourceApplicationEntityTitle);
		ds.remove(Tag.PrivateInformationCreatorUID);
		ds.remove(Tag.PrivateInformation);
		ds.remove(Tag.FileMetaInformationVersion);
		File tempFile = null;
		OutputStream os = null;
		try
		{
			tempFile = store.createTempFile(".dcmimp");
			os = SSStore.get().getOutputStream(tempFile);
			ds.writeTo(os, UID.ImplicitVRLittleEndian);
			os.close();
			os = null;
		}
		finally
		{
			Util.safeDelete(tempFile);
			Util.safeClose(os);
		}
	}

	void oldRecover(File root, String basename, File recovered) throws Exception
	{
		File f = null;
		OutputStream sos = null;
		FileInputStream fis = null;
		try
		{
			File m = new File(root, "meta-" + basename + ".xml");
			File d = new File(root, "data-" + basename + ".dat");
			if (!m.exists())
			{
				throw new RuntimeException("meta does not exist:");
			}
			if (!d.exists())
			{
				throw new RuntimeException("data does not exist:");
			}
			XML xml = new XML(m);
			ImageMetaInfo imi = new ImageMetaInfo();
			imi.setMediaStorageSOPInstanceUID(xml.get(Tag.MediaStorageSOPInstanceUID.getKey()));
			imi.setMediaStorageSOPClassUID(xml.get(Tag.MediaStorageSOPClassUID.getKey()));
			imi.setTransferSyntax(xml.get(Tag.TransferSyntaxUID.getKey()));
			imi.setSCP_AE(xml.get("SCP_AE", ""));
			imi.setSCU_AE(xml.get("SCU_AE", ""));
			imi.setDataCreateTime(xml.get("DataCreateTime", ""));
			f = store.createTempFile(".recover");
			sos = store.getOutputStream(f);
			fis = new FileInputStream(d);
			Util.copyStream(fis, sos);
			fis.close();
			sos.close();
			store.updateMeta(f, imi);
			put(f);
			FileUtil.rename(f, new File(recovered, "meta-" + basename + ".xml"), false);
			FileUtil.rename(d, new File(recovered, "data-" + basename + ".dat"), false);
		}
		catch (Exception e)
		{
			log("oldRecover caught", e);
			log("continuing...");
		}
		finally
		{
			Util.safeClose(sos);
			Util.safeClose(fis);
			Util.safeDelete(f);
		}
	}

	public void oldRecoverFromFilesystem(boolean init, File recoveryRoot)
	{
		try
		{
			store.initCache();
			log("recoverFromFilesystem " + new java.util.Date());
			if (init)
			{
				clearDatabase();
			}
			File storageRoot = store.getRootDir();
			final File recovered = new File(storageRoot.getParentFile(), "__recovered__");
			recovered.mkdir();
			FileUtil.forEachFile(recoveryRoot, true, false, true, new FileUtil.FileRunnable()
			{
				public void run(File f)
				{
					try
					{
						if (f.getName().endsWith(".xml"))
						{
							String basename = f.getName().substring(5, f.getName().lastIndexOf('.'));
							try
							{
								oldRecover(f.getParentFile(), basename, recovered);
							}
							catch (Exception e)
							{
								log("could not recover " + basename, e);
							}
						}
						else if (!f.getName().endsWith(".dat"))
						{
							log("did not process: " + f.getName());
						}
					}
					catch (Exception e)
					{
						log("error loading " + f.getAbsolutePath(), e);
						log("continuing...");
					}
				}
			});
			String names[] = storageRoot.list();
			for (int i = 0; i < names.length; i++)
			{
				if (names[i].endsWith(".xml"))
				{
					String basename = names[i].substring(5, names[i].lastIndexOf('.'));
					try
					{
						oldRecover(storageRoot, basename, recovered);
					}
					catch (Exception e)
					{
						log("could not recover " + basename, e);
					}
				}
			}
			names = storageRoot.list();
			for (int i = 0; i < names.length; i++)
			{
				if (names[i].endsWith(".xml") || names[i].endsWith(".dat"))
					log("done: still exists: " + names[i]);
			}
		}
		catch (Exception e)
		{
			log("recoverFromFilesystem failed ", e);
		}
	}

	void clearDatabase()
	{
		log("resetting database...");
		ImageView.get().deleteAll();
		PatientView.get().deleteAll();
		QueueView.get().deleteAll();
		SeriesView.get().deleteAll();
		StudyView.get().deleteAll();
		// todo: hack: delete web access/usage need init mathod for db
		try
		{
			String delSQL = "delete from  dcm_delete ";
			JDBCUtil.get().update(delSQL);
		}
		catch (Exception e)
		{
			Log.log("delete from  dcm_delete  " + e, e);
		}
		// / delete web_view hack
		try
		{
			String delSQL = "delete from  web_access ";
			JDBCUtil.get().update(delSQL);
		}
		catch (Exception e)
		{
			Log.log("delete from  web_access  " + e, e);
		}
		try
		{
			String delSQL = "delete from  web_assign ";
			JDBCUtil.get().update(delSQL);
		}
		catch (Exception e)
		{
			Log.log("delete from  web_assign  " + e, e);
		}
		try
		{
			String delSQL = "delete from  web_study ";
			JDBCUtil.get().update(delSQL);
		}
		catch (Exception e)
		{
			Log.log("delete from  web_study  " + e, e);
		}
		try
		{
			String delSQL = "delete from  web_trackdownloads ";
			JDBCUtil.get().update(delSQL);
		}
		catch (Exception e)
		{
			Log.log("delete from  web_trackdownloads  " + e, e);
		}
	}

	public void recoverFromFilesystem(boolean init, File recoveryRoot)
	{
		recoverFromFilesystemRules(init, recoveryRoot, false);
	}

	public void recoverFromFilesystemRules(boolean init, File recoveryRoot, boolean applyRules)
	{
		try
		{
			store.initCache();
			log("recoverFromFilesystem " + new java.util.Date());
			if (init)
			{
				clearDatabase();
			}
			final File recovered = new File(store.getRootDir().getParentFile(), "__recovered__");
			recovered.mkdir();
			final boolean apply = applyRules;
			FileUtil.forEachFile(recoveryRoot, true, false, true, new FileUtil.FileRunnable()
			{
				public void run(File f)
				{
					try
					{
						if (f.getName().endsWith(".mdf"))
						{
							put(f, apply);
							File rf = new File(recovered, f.getName());
							boolean renamed = FileUtil.rename(f, rf, true);
							if (renamed)
								log("recovered: " + f.getName());
							else
								log("recover: could not rename " + f.getAbsolutePath());
						}
						else
							log("did not process: " + f.getName());
					}
					catch (Exception e)
					{
						log("error loading " + f.getAbsolutePath(), e);
						log("continuing...");
					}
				}
			});
			// delete unused dirs
			FileUtil.forEachFile(recoveryRoot, true, true, false, new FileUtil.FileRunnable()
			{
				public void run(File f)
				{
					try
					{
						if (f.isDirectory() && f.list().length == 0)
						{
							log("delete unused dir " + f.getName());
							f.delete();
						}
					}
					catch (Exception e)
					{
						log("error delete unused dir " + f.getAbsolutePath(), e);
						log("continuing...");
					}
				}
			});
			// seconds pass to clear days
			FileUtil.forEachFile(recoveryRoot, true, true, false, new FileUtil.FileRunnable()
			{
				public void run(File f)
				{
					try
					{
						if (f.isDirectory() && f.list().length == 0)
						{
							log("delete unused dir " + f.getName());
							f.delete();
						}
					}
					catch (Exception e)
					{
						log("error delete unused dir " + f.getAbsolutePath(), e);
						log("continuing...");
					}
				}
			});
		}
		catch (Exception e)
		{
			log("recoverFromFilesystem failed ", e);
		}
	}

	public void loadSimulatedDataFromFilesystem(boolean init, File recoveryRoot)
	{
		try
		{
			store.initCache();
			log("loadSimulatedDataFromFilesystem " + new java.util.Date());
			if (init)
			{
				clearDatabase();
			}
			// final File recovered = new
			// File(store.getRootDir().getParentFile(), "__recovered__");
			// recovered.mkdir();
			final ArrayList al = new ArrayList();
			FileUtil.forEachFile(recoveryRoot, true, false, true, new FileUtil.FileRunnable()
			{
				public void run(File f)
				{
					if (f.getName().endsWith(".mdf"))
						al.add(f);
				}
			});
			HashMap replaceMap = new HashMap();
			for (;;)
			{
				replaceMap.clear();
				Iterator iter = al.iterator();
				int index = 1;
				String unique = "1." + System.currentTimeMillis();
				while (iter.hasNext())
				{
					File f = (File) iter.next();
					ImageMetaInfo meta = (ImageMetaInfo) SSStore.get().getMetaFromFile(f);
					SSInputStream sis = new SSInputStream(f);
					DSFileView v = (DSFileView) DSFileView.viewMap.load(sis, UID.ImplicitVRLittleEndian);
					sis.close();
					sis = null;
					String study = v.StudyInstanceUID;
					String series = v.SeriesInstanceUID;
					if (!replaceMap.containsKey(study))
						replaceMap.put(study, unique + ".1." + (index++));
					if (!replaceMap.containsKey(series))
						replaceMap.put(series, unique + ".2." + (index++));
					replaceMap.put(meta.getMediaStorageSOPInstanceUID(), unique + ".3." + (index++));
					File newFile = SSStore.get().createTempFile(".load");
					FileUtil.copyFile(f, newFile);
					put(newFile, meta, replaceMap);
				}
			}
		}
		catch (Exception e)
		{
			log("loadSimulatedDataFromFilesystem failed ", e);
		}
	}
	long	importCount	= 0;

	public void loadDCMFileDataFromFilesystem(File recoveryRoot)
	{
		loadDCMFileDataFromFilesystem(recoveryRoot, false);
	}

	public void loadDCMFileDataFromFilesystem(File recoveryRoot, final boolean ignoreExtension)
	{
		try
		{
			importCount = 0;
			store.initCache();
			FileUtil.forEachFile(recoveryRoot, true, false, true, new FileUtil.FileRunnable()
			{
				public void run(File f)
				{
					if (ignoreExtension || f.getName().endsWith(".dcm"))
					{
						log("######################################## process: " + importCount + " " + f.getAbsolutePath());
						importCount++;
						try
						{
							DicomStore.get().loadDicomFile(f, true);
							// System.gc();
						}
						catch (NullPointerException ne)
						{
						}
						catch (Exception e)
						{
							log("============================== " + f.getAbsolutePath(), e);
						}
					}
				}
			});
		}
		catch (Exception e)
		{
			log("loadDCMFileDataFromFilesystem failed ", e);
		}
	}

	public void mattsloadDCMFileDataFromFilesystem(File recoveryRoot)
	{
		try
		{
			store.initCache();
			log("loadDCMFileDataFromFilesystem " + new java.util.Date() + " " + recoveryRoot.getAbsolutePath());
			// final File recovered = new
			// File(store.getRootDir().getParentFile(), "__recovered__");
			// recovered.mkdir();
			final List al = new LinkedList();
			FileUtil.forEachFile(recoveryRoot, true, false, true, new FileUtil.FileRunnable()
			{
				public void run(File f)
				{
					if (f.getName().endsWith(".dcm"))
						al.add(f);
				}
			});
			Iterator iter = al.iterator();
			int count = 1;
			int max = al.size();
			log("loadDCMFileDataFromFilesystem: # of files: " + al.size());
			while (iter.hasNext())
			{
				File f = (File) iter.next();
				log("######################################## process: " + count + ":" + max + " " + f.getAbsolutePath());
				count++;
				try
				{
					DicomStore.get().loadDicomFile(f, true); // Matt change
					// to Cyrus:
					// true will
					// generate JPEG
					// thumbnails
					// and also
					// create web
					// tables
					// RAL - added forced garbage collection to help with heap
					// problem
					// System.gc();
				}
				catch (Exception e)
				{
					log("============================== " + f.getAbsolutePath(), e);
				}
			}
		}
		catch (Exception e)
		{
			log("loadDCMFileDataFromFilesystem failed ", e);
		}
	}

	public void testLoadDCMFileDataFromFilesystem(File recoveryRoot)
	{
		try
		{
			store.initCache();
			log("testLoadDCMFileDataFromFilesystem " + new java.util.Date() + " " + recoveryRoot.getAbsolutePath());
			// final File recovered = new
			// File(store.getRootDir().getParentFile(), "__recovered__");
			// recovered.mkdir();
			final List al = new LinkedList();
			FileUtil.forEachFile(recoveryRoot, true, false, true, new FileUtil.FileRunnable()
			{
				public void run(File f)
				{
					if (f.getName().endsWith(".dcm"))
						al.add(f);
				}
			});
			Iterator iter = al.iterator();
			int count = 1;
			int max = al.size();
			log("testLoadDCMFileDataFromFilesystem: # of files: " + al.size());
			while (iter.hasNext())
			{
				File f = (File) iter.next();
				log("######################################## test process: " + count + ":" + max + " " + f.getAbsolutePath());
				count++;
				try
				{
					DicomStore.get().testLoadDicomFile(f);
				}
				catch (Exception e)
				{
					log("============================== " + f.getAbsolutePath(), e);
				}
			}
		}
		catch (Exception e)
		{
			log("testLoadDCMFileDataFromFilesystem failed ", e);
		}
	}
}
