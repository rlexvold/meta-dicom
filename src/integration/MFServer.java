package integration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

import net.metafusion.Dicom;
import net.metafusion.admin.StudyBean;
import net.metafusion.archive.ArchiveJob;
import net.metafusion.archive.ArchiveSystem;
import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSFileView;
import net.metafusion.dicom.DicomWrapper;
import net.metafusion.localstore.DicomQuery;
import net.metafusion.localstore.ServerUtil;
import net.metafusion.localstore.richmedia.RichMediaManager;
import net.metafusion.model.JDBCView;
import net.metafusion.model.Patient;
import net.metafusion.model.PatientView;
import net.metafusion.model.ReviewView;
import net.metafusion.model.RisGlueLog;
import net.metafusion.model.RisGlueLogView;
import net.metafusion.model.Series;
import net.metafusion.model.SeriesView;
import net.metafusion.model.Study;
import net.metafusion.model.StudyView;
import net.metafusion.model.WebView;
import net.metafusion.pacs.PacsFactory;
import net.metafusion.pdfutils.PdfUtils;
import net.metafusion.util.AEMap;
import net.metafusion.util.DicomDir;
import net.metafusion.util.DicomUtil;
import net.metafusion.util.PatientUtils;
import acme.storage.SSStore;
import acme.util.CompressionStream;
import acme.util.FileUtil;
import acme.util.Log;
import acme.util.LongKeyCache;
import acme.util.Util;
import acme.util.rsync.RsyncServer;

public class MFServer
{
	static class ServerSess
	{
		public long		id;
		LinkedList		messages	= new LinkedList();
		public boolean	refreshNewStudy;
		public boolean	refreshRequest;
		public long		ticks;
		MFUser			user;
		long			version		= 0;

		public ServerSess(long id, MFUser user)
		{
			this.id = id;
			this.user = user;
			ticks = System.currentTimeMillis();
		}

		synchronized public void addMessage(String title, String text)
		{
			messages.add(new MFMessage(title, text));
		}

		synchronized public MFIdle idle()
		{
			ticks = System.currentTimeMillis();
			MFIdle idle = new MFIdle();
			if (messages.size() != 0)
			{
				MFMessage[] m = (MFMessage[]) messages.toArray(new MFMessage[messages.size()]);
				messages.clear();
				idle.messages = m;
			}
			idle.refreshNewStudy = refreshNewStudy;
			idle.refreshRequest = refreshRequest;
			refreshNewStudy = false;
			refreshRequest = false;
			return idle;
		}
	}
	static MFServer		instance			= null;
	static final long	serialVersionUID	= 1L;

	static public MFServer get()
	{
		return instance;
	}

	// add backdoor
	static public void init()
	{
		instance = new MFServer();
		List users = JDBCView.get().getWebUsers();
		Iterator iter = users.iterator();
		while (iter.hasNext())
		{
			Object[] oo = (Object[]) iter.next();
			get().addUser(Long.parseLong("" + oo[0]), (String) oo[1], (String) oo[2]);
		}
	}

	static public void log(String s)
	{
		Util.log(s);
	}

	static public void log(String s, Exception e)
	{
		Util.log(s, e);
	}

	static public void vlog(String s)
	{
		Util.log(s);
	}
	long			lastAgeCheck	= 0;
	long			lastSessID		= 0;
	LongKeyCache	sessCache		= new LongKeyCache(500);
	SSStore			store			= SSStore.get();
	LongKeyCache	studyCache		= new LongKeyCache(10000);
	HashMap			userIDMap		= new HashMap();
	HashMap			userNameMap		= new HashMap();
	MFUser[]		users			= new MFUser[0];

	synchronized ServerSess addSession(MFUser user)
	{
		long ticks = System.currentTimeMillis();
		if (lastSessID >= ticks)
		{
			lastSessID = lastSessID + 1;
		}
		else
		{
			lastSessID = ticks;
		}
		vlog("addSession " + user.name + " " + lastSessID);
		ServerSess sess = new ServerSess(lastSessID, user);
		sessCache.put(lastSessID, sess);
		return sess;
	}

	synchronized void addUser(long id, String name, String password)
	{
		MFUser user = new MFUser(id, name);
		user.password = password;
		userIDMap.put(new Long(id), user);
		userNameMap.put(name, user);
		users = (MFUser[]) userNameMap.values().toArray(new MFUser[userNameMap.size()]);
	}

	void ageSessions()
	{
		if (lastAgeCheck > System.currentTimeMillis() - 1000 * 60 * 5)
		{
			return;
		}
		lastAgeCheck = System.currentTimeMillis();
		refreshUsers();
		List toDelete = new LinkedList();
		Iterator iter = sessCache.getKeys().iterator();
		long ticks = System.currentTimeMillis() - 1000 * 60 * 60;
		while (iter.hasNext())
		{
			ServerSess sess = (ServerSess) sessCache.getNoLRU(((Long) (iter.next())).longValue());
			if (sess != null && sess.ticks < ticks)
			{
				toDelete.add(sess);
			}
		}
		iter = toDelete.iterator();
		while (iter.hasNext())
		{
			ServerSess sess = ((ServerSess) iter.next());
			log("aging out: " + sess.id + ":" + sess.user.name);
			sessCache.remove(sess.id);
		}
		lastAgeCheck = System.currentTimeMillis();
	}

	synchronized public MFStudy attach(long id, String name, String label, byte[] data) throws Exception
	{
		long attachID = store.getNextID();
		MFStudy study = getStudy(id);
		if (study == null)
		{
			throw new RuntimeException("attach: study not found " + id);
		}
		store.putRaw(id, "" + attachID + ".att", new ByteArrayInputStream(data));
		study.addAttachment(name, attachID, label);
		store.putRawObject(id, "study.dat", study.getStub());
		study.version++;
		StudyView.get().risUpdate(study);
		study.success = true;
		return study;
	}

	private MFExtendedStudy[] cfindRemote(String sourceAE, SearchBean sb)
	{
		ArrayList<DS> results = new DicomWrapper().DicomSearch(sourceAE, MFConverter.SearchBeanToDataSet(sb, Dicom.STUDY_LEVEL));
		if (results == null || results.size() == 0)
		{
			return null;
		}
		MFExtendedStudy[] resultsMF = new MFExtendedStudy[results.size()];
		for (int i = 0; i < results.size(); i++)
		{
			resultsMF[i] = MFConverter.DataSetToMFExtendedStudy((DS) results.get(i));
		}
		return resultsMF;
	}

	synchronized void closeSession(long id)
	{
		vlog("addSession " + id);
		if (id == 0)
		{
			return;
		}
		sessCache.remove(id);
	}

	private boolean cmoveRemote(String sourceAE, String destAE, String moveList)
	{
		// RAL - for eFilm CMOVE's to work, the level must be STUDY_LEVEL
		return new DicomWrapper().DicomMove(sourceAE, destAE, MFConverter.MoveListToDataSet(moveList), Dicom.STUDY_LEVEL);
	}

	private String createReportPdf(String sourceFiles, String destFile)
	{
		String[] fileList = sourceFiles.split(",");
		try
		{
			for (int i = 0; i < fileList.length; i++)
			{
				fileList[i] = PdfUtils.ConvertFileToPdf(fileList[i]);
			}
			PdfUtils.concatenate(fileList, destFile);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return "err;" + e.getMessage();
		}
		return "ok;";
	}

	synchronized void deleteUser(long id, String name)
	{
		userIDMap.remove(new Long(id));
		userNameMap.remove(name);
		users = (MFUser[]) userNameMap.values().toArray(new MFUser[userNameMap.size()]);
	}

	public Object dispatch(MFInputStream ois, MFOutputStream oos, ServerSess sess, String cmd)
	{
		if (!cmd.equalsIgnoreCase("idle"))
		{
			log("dispatch: " + cmd);
		}
		cmd = cmd.intern();
		try
		{
			if (cmd.equalsIgnoreCase("ping"))
			{
				return new MFBoolean(ping());
			}
			else if (cmd.equalsIgnoreCase("getuserlist"))
			{
				return getUserList();
			}
			else if (cmd.equalsIgnoreCase("getusernamelist"))
			{
				return getUserNameList();
			}
			else if (cmd.equalsIgnoreCase("logout"))
			{
				return new MFBoolean(logout(sess.id));
			}
			else if (cmd.equalsIgnoreCase("query"))
			{
				return query(getString(ois), (SearchBean) getObject(ois));
			}
			else if (cmd.equalsIgnoreCase("attach"))
			{
				return attach(getLong(ois), getString(ois), getString(ois), (byte[]) getObject(ois));
			}
			else if (cmd.equalsIgnoreCase("readattachedfile"))
			{
				return readAttachedFile(getLong(ois), getLong(ois));
			}
			else if (cmd.equalsIgnoreCase("update"))
			{
				return update((MFStudy) getObject(ois));
			}
			else if (cmd.equalsIgnoreCase("loadstudy"))
			{
				return loadStudy(getLong(ois));
			}
			else if (cmd.equalsIgnoreCase("requestreview"))
			{
				return new MFBoolean(requestReview(getLong(ois), getString(ois)));
			}
			else if (cmd.equalsIgnoreCase("removereview"))
			{
				return new MFBoolean(removeReview(getLong(ois), getString(ois)));
			}
			else if (cmd.equalsIgnoreCase("idle"))
			{
				return sess.idle();
			}
			else if (cmd.equalsIgnoreCase("sendmessage"))
			{
				return new MFBoolean(sendMessage(getString(ois), getString(ois), getString(ois)));
			}
			else if (cmd.equalsIgnoreCase("sendstudy"))
			{
				return new MFBoolean(sendStudy(getLong(ois), getString(ois)));
			}
			else if (cmd.equalsIgnoreCase("sendstudyuid"))
			{
				return new MFBoolean(sendStudyUID(getString(ois), getString(ois)));
			}
			else if (cmd.equalsIgnoreCase("getFileList"))
			{
				return getFileList(getString(ois));
			}
			else if (cmd.equalsIgnoreCase("putFile"))
			{
				return new MFBoolean(putFile(getString(ois), getString(ois), (byte[]) getObject(ois)));
			}
			else if (cmd.equalsIgnoreCase("getFile"))
			{
				return getFile(getString(ois), getString(ois));
			}
			else if (cmd.equalsIgnoreCase("downloadStudies"))
			{
				return new MFBoolean(downloadStudies(getString(ois), (String[]) getObject(ois), getString(ois), getString(ois)));
			}
			else if (cmd.equalsIgnoreCase("referrerAdd"))
			{
				return (referrerAdd(getString(ois), getString(ois)));
			}
			else if (cmd.equalsIgnoreCase("referrerModify"))
			{
				return (referrerModify(getString(ois), getString(ois), getString(ois)));
			}
			else if (cmd.equalsIgnoreCase("referrerDelete"))
			{
				return (referrerDelete(getString(ois)));
			}
			else if (cmd.equalsIgnoreCase("publishStudy"))
			{
				return (publishStudy(getString(ois), getString(ois)));
			}
			else if (cmd.equalsIgnoreCase("unpublishStudy"))
			{
				return (unpublishStudy(getString(ois), getString(ois)));
			}
			else if (cmd.equalsIgnoreCase("publishAttachment"))
			{
				return (publishAttachment(getString(ois), getString(ois), (byte[]) getObject(ois)));
			}
			else if (cmd.equalsIgnoreCase("unpublishAttachment"))
			{
				return (unpublishAttachment(getString(ois), getString(ois)));
			}
			else if (cmd.equalsIgnoreCase("cfind"))
			{
				return (cfindRemote(getString(ois), (SearchBean) getObject(ois)));
			}
			else if (cmd.equalsIgnoreCase("cmove"))
			{
				return new MFBoolean(cmoveRemote(getString(ois), getString(ois), getString(ois)));
			}
			else if (cmd.equalsIgnoreCase("patientCleanupAll"))
			{
				return patientCleanupAll(getString(ois));
			}
			else if (cmd.equalsIgnoreCase("patientCleanupByPatient"))
			{
				return patientCleanupByPatient(getString(ois), getString(ois));
			}
			else if (cmd.equalsIgnoreCase("patientCleanupByStudy"))
			{
				return patientCleanupByStudy(getString(ois), getString(ois));
			}
			else if (cmd.equalsIgnoreCase("createReportPdf"))
			{
				return (createReportPdf(getString(ois), getString(ois)));
			}
			else if (cmd.equalsIgnoreCase("getLocalTime"))
				return getLocalTime();
			else if (cmd.equalsIgnoreCase("getRichMediaDir"))
			{
				return (getRichMediaDir(getString(ois)));
			}
			else if (cmd.equalsIgnoreCase("getRichMediaFileList"))
			{
				return getRichMediaFileList(getString(ois));
			}
			else if (cmd.equalsIgnoreCase("logTransferStart"))
			{
				return logTransferStart(getString(ois), getString(ois), getString(ois));
			}
			else if (cmd.equalsIgnoreCase("changeLogStatus"))
			{
				return changeLogStatus(getString(ois), getString(ois));
			}
			else if (cmd.equalsIgnoreCase("createThumbnailsByStudyUID"))
			{
				return (createThumbnailsByStudyUID(getString(ois)));
			}
			else if (cmd.equalsIgnoreCase("createThumbnailsBySeriesUID"))
			{
				return (createThumbnailsBySeriesUID(getString(ois)));
			}
			else if (cmd.equalsIgnoreCase("sendFileFromServer"))
			{
				return sendFileFromServer((MFFileInfo) getObject(ois), getInt(ois), getInt(ois));
			}
			else if (cmd.equalsIgnoreCase("sendFileToServer"))
			{
				return sendFileToServer((MFFileInfo) getObject(ois), (byte[]) getObject(ois), getInt(ois));
			}
			else if (cmd.equalsIgnoreCase("deleteStudy"))
			{
				return deleteStudy(getString(ois));
			}
			else if (cmd.equalsIgnoreCase("getRsyncSums"))
			{
				return getRsyncSums((File) getObject(ois), getInt(ois), getInt(ois));
			}
			else if (cmd.equalsIgnoreCase("processRsyncDeltas"))
			{
				return new MFBoolean(processRsyncDeltas((File) getObject(ois), (byte[]) getObject(ois), getInt(ois), getInt(ois)));
			}
			else if (cmd.equalsIgnoreCase("rsyncFinalize"))
			{
				return new MFBoolean(rsyncFinalize((File) getObject(ois)));
			}
			else if (cmd.equalsIgnoreCase("getRsyncDeltas"))
			{
				return getRsyncDeltas((File) getObject(ois), (byte[]) getObject(ois), getInt(ois), getInt(ois));
			}
			else if (cmd.equalsIgnoreCase("mergeStudies"))
			{
				return mergeStudies(getString(ois), getString(ois), getString(ois), getString(ois));
			}
			else if (cmd.equalsIgnoreCase("archiveJob"))
			{
				return archiveJob((ArchiveJob) getObject(ois));
			}
			else if (cmd.equalsIgnoreCase("archiveGetJobInfo"))
			{
				return archiveGetJobInfo((ArchiveJob) getObject(ois));
			}
			else if (cmd.equalsIgnoreCase("runArchiveJob"))
			{
				return runArchiveJob((ArchiveJob) getObject(ois));
			}
			else if(cmd.equalsIgnoreCase("loadDemoStudies"))
			{
				return loadDemoStudies((MFDemoStudy) getObject(ois));
			}
			else
			{
				throw new RuntimeException("bad command " + cmd);
			}
		}
		catch (Exception e)
		{
			log("" + cmd + " mfcaught", e);
			return e;
		}
		finally
		{
		}
		// return new RuntimeException("mf unknown command: "+cmd);
	}

	private String loadDemoStudies(MFDemoStudy demo)
	{
		return PacsFactory.getPacsInterface().loadDemoStudies(demo);
	}

	private ArchiveJob runArchiveJob(ArchiveJob job) throws Exception
	{
		ArchiveSystem as = new ArchiveSystem();
		return as.runJob(job);
	}

	private ArchiveJob archiveGetJobInfo(ArchiveJob job) throws Exception
	{
		return ArchiveSystem.getJobInfo(job);
	}

	private ArchiveJob archiveJob(ArchiveJob job) throws Exception
	{
		ArchiveSystem as = new ArchiveSystem();
		job = as.createStudyArchiveData(job);
		job = as.transferData(job);
		job = as.runRemoteJob(job);
		return job;
	}

	private Object mergeStudies(String patientStudyUID, String studySourceUID, String accession, String studyDate) throws Exception
	{
		Date tmp = DicomUtil.parseDate(studyDate);
		PacsFactory.getPacsInterface().mergeStudies(patientStudyUID, studySourceUID, accession, tmp);
		return null;
	}

	private boolean rsyncFinalize(File theFile)
	{
		RsyncServer rm = new RsyncServer();
		rm.finalize(theFile);
		return true;
	}

	private String[] getRichMediaFileList(String studyuid) throws Exception
	{
		String dir = getRichMediaDir(studyuid);
		File tmp = new File(dir);
		return tmp.list(new FileUtil.UtilFilenameFilter(null, null));
	}

	private byte[] getRsyncDeltas(File theFile, byte[] compressedSums, int offset, int length) throws Exception
	{
		List sums = (List) CompressionStream.readObject(compressedSums);
		RsyncServer rm = new RsyncServer();
		List deltas = rm.getDeltas(sums, theFile.getAbsolutePath(), offset, length);
		if (deltas == null)
			throw new EOFException();
		byte[] compressedDeltas = CompressionStream.write(deltas);
		return compressedDeltas;
	}

	private boolean processRsyncDeltas(File theFile, byte[] compressedDeltas, int offset, int length) throws Exception
	{
		List deltas = (List) CompressionStream.readObject(compressedDeltas);
		RsyncServer rm = new RsyncServer();
		rm.processDeltasAndWriteFile(theFile.getAbsolutePath(), deltas, offset, length);
		return true;
	}

	private byte[] getRsyncSums(File theFile, int offset, int length) throws Exception
	{
		RsyncServer rm = new RsyncServer();
		File parent = theFile.getParentFile();
		if (!parent.exists())
			parent.mkdirs();
		List sums = rm.getSums(theFile.getAbsolutePath(), offset, length);
		if (sums == null)
			throw new EOFException();
		return CompressionStream.write(sums);
	}

	private String deleteStudy(String studyuid) throws Exception
	{
		try
		{
			PacsFactory.getPacsInterface().deleteStudy(studyuid);
		}
		catch (Exception e)
		{
			throw e;
		}
		return "success";
	}

	private String changeLogStatus(String id, String status)
	{
		String result;
		try
		{
			RichMediaManager rm = new RichMediaManager();
			rm.changeLogStatus(Long.parseLong(id), status);
			return "ok";
		}
		catch (Exception e)
		{
			Log.log("logTransferStart error: ", e);
		}
		return null;
	}

	private String logTransferStart(String source, String destination, String studyuid)
	{
		String result;
		try
		{
			RichMediaManager rm = new RichMediaManager();
			result = rm.logTransferStart(source, destination, studyuid);
		}
		catch (Exception e)
		{
			Log.log("logTransferStart error: ", e);
			return null;
		}
		return result;
	}

	private ArrayList<MFFileInfo> createThumbnailsBySeriesUID(String seriesUID) throws Exception
	{
		ArrayList<MFFileInfo> returnList = PacsFactory.getPacsInterface().createThumbnailsbySeries(seriesUID);
		if (returnList == null)
			throw new Exception("createThumbnailsBySeriesUID: null ID");
		return returnList;
	}

	private ArrayList<MFFileInfo> createThumbnailsByStudyUID(String studyUID) throws Exception
	{
		ArrayList<MFFileInfo> returnList = PacsFactory.getPacsInterface().createThumbnailsbyStudy(studyUID);
		if (returnList == null)
			throw new Exception("createThumbnailsByStudyUID: null ID");
		return returnList;
	}

	synchronized public byte[] sendFileFromServer(MFFileInfo info, int offset, int length) throws Exception
	{
		byte[] data = Util.readFile(info.getSourceFile().getAbsolutePath(), offset, length);
		if (data == null)
			throw new EOFException();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		CompressionStream.write(data, dos);
		return baos.toByteArray();
	}

	synchronized public boolean sendFileToServer(MFFileInfo info, byte[] data, int offset) throws Exception
	{
		data = CompressionStream.read(data);
		boolean append = false;
		if (offset > 0)
			append = true;
		Util.writeFile(data, new File(info.getSourceFile().getAbsolutePath()), append);
		return true;
	}

	public String getRichMediaDir(String studyuid)
	{
		String result = null;
		try
		{
			RichMediaManager rm = new RichMediaManager();
			result = rm.getMediaDir(studyuid);
		}
		catch (Exception e)
		{
			Log.log("getRichMediaDir ERROR: ", e);
		}
		return result;
	}

	public boolean downloadStudies(String type, String args[], String host, String port)
	{
		log(type + " " + args + ":" + args.length + " " + host + ":" + port);
		File zipFile = null;
		FileInputStream fis = null;
		Socket s = null;
		List studies = null;
		boolean result = false;
		if (type.equals("uid"))
		{
			studies = getStudies(args);
		}
		else if (type.equals("date"))
		{
			studies = getStudiesByDate(args);
		}
		else
		{
			throw new RuntimeException("unknown download type");
		}
		try
		{
			log("zipping studies: " + studies.size());
			zipFile = DicomDir.CreateDicomDirZipForArchive(studies);
			if (zipFile == null)
			{
				throw new RuntimeException("could not create dicomdir");
			}
			log("zip file size: " + zipFile.length());
			fis = new FileInputStream(zipFile);
			log("send to " + host + ":" + Integer.parseInt(port));
			s = new Socket(host, Integer.parseInt(port));
			s.shutdownInput();
			OutputStream os = s.getOutputStream();
			Util.copyStream(fis, os);
			s.shutdownOutput();
			log("copy done ");
			os.close();
			result = true;
		}
		catch (IOException e)
		{
			log("downloadStudies caught", e);
		}
		finally
		{
			Util.safeClose(fis);
			Util.safeClose(s);
			Util.safeDelete(zipFile);
		}
		return result;
	}

	public synchronized void flushStudy(long id)
	{
		studyCache.remove(id);
	}

	synchronized public byte[] getFile(String uid, String name) throws Exception
	{
		Study study = StudyView.get().selectByUID(uid);
		log("getFile " + uid + " " + name);
		if (study == null)
		{
			throw new RuntimeException("readFile: study not found " + uid);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = null;
		try
		{
			is = store.getRawInputStream(study.getStudyID(), name);
			Util.copyStream(is, baos);
		}
		finally
		{
			Util.safeClose(is);
		}
		return baos.toByteArray();
	}

	synchronized public String getFileList(String uid)
	{
		Study study = StudyView.get().selectByUID(uid);
		if (study == null)
		{
			throw new RuntimeException("getFileList: study not found " + uid);
		}
		File f[] = store.getFiles(study.getStudyID(), ";atx");
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < f.length; i++)
		{
			sb.append(f[i].getName().substring(0, f[i].getName().length() - 4) + "\n");
		}
		log("getFileList " + uid + " " + sb.toString());
		return sb.toString();
	}

	int getInt(MFInputStream ois) throws Exception
	{
		MFInteger l = (MFInteger) ois.readObject();
		return l.i;
	}

	List getList(MFInputStream ois) throws Exception
	{
		return (List) ois.readObject();
	}

	long getLong(MFInputStream ois) throws Exception
	{
		MFLong l = (MFLong) ois.readObject();
		return l.l;
	}

	Object getObject(MFInputStream ois) throws Exception
	{
		return ois.readObject();
	}

	String getString(MFInputStream ois) throws Exception
	{
		return (String) ois.readObject();
	}

	List getStudies(String args[])
	{
		List l = new ArrayList();
		for (int i = 0; i < args.length; i++)
		{
			Study study = StudyView.get().selectByUID(args[i]);
			if (study == null)
			{
				throw new RuntimeException("study not found");
			}
			l.add(study);
		}
		return l;
	}

	List getStudiesByDate(String args[])
	{
		java.sql.Date start = DicomUtil.parseDate(args[0]);
		java.sql.Date end = DicomUtil.parseDate(args[1]);
		List l = StudyView.get().selectBetweenInclusive(start, end);
		return l;
	}

	public synchronized MFStudy getStudy(long id)
	{
		MFStudy mfstudy = (MFStudy) studyCache.get(id);
		if (mfstudy != null)
		{
			return mfstudy;
		}
		Study study = StudyView.get().selectByID(id);
		if (study == null)
		{
			return null;
		}
		mfstudy = new MFStudy();
		mfstudy.studyID = study.getStudyID();
		mfstudy.studyIDString = study.getStudyIDString();
		mfstudy.state = study.getState();
		mfstudy.studyUID = study.getStudyUID();
		mfstudy.version = study.getVersion();
		Patient p = PatientView.get().select(study.getPatientID());
		if (p != null)
		{
			mfstudy.patientID = p.getExtID();
			mfstudy.patientName = p.getViewableDicomName();
			mfstudy.patientSex = p.getSex();
			mfstudy.patientBDay = DicomUtil.formatDate(p.getDob());
		}
		// mfstudy.dateTime=DicomUtil.formatDate(study.getDate())+"
		// "+DicomUtil.formatTime(new java.sql.Date(study.getTime().getTime()));
		mfstudy.dateTime = DicomUtil.formatDateTime(study.getDate(), study.getTime());
		mfstudy.modality = study.getModalities();
		mfstudy.description = study.getDescription();
		mfstudy.referrer = study.getReferringPhysicianName();
		mfstudy.lastViewer = "";
		mfstudy.institution = study.getInstitutionName(); // ********************
		// **************
		mfstudy.station = study.getStationName(); // /*************************
		mfstudy.reader = study.getReader(); // **********************************
		// ***
		List l = SeriesView.get().selectByStudy(id);
		mfstudy.series = new MFSeries[l.size()];
		int index = 0;
		Iterator iter = l.iterator();
		while (iter.hasNext())
		{
			Series s = (Series) iter.next();
			MFSeries mfseries = new MFSeries();
			mfseries.seriesNumber = s.getSeriesNumber();
			mfseries.modality = s.getModality();
			mfseries.description = s.getDescription();
			mfseries.imageCount = s.getCount();
			mfstudy.series[index++] = mfseries;
		}
		mfstudy.dictations = new MFDictation[0];
		mfstudy.reports = new MFReport[0];
		mfstudy.attachments = new MFAttachment[0];
		mfstudy.notes = new MFNote();
		try
		{
			if (store.rawExists(id, "study.dat"))
			{
				MFStudyStub stub = (MFStudyStub) store.getRawObject(id, "study.dat");
				if (stub.studyID != mfstudy.studyID)
				{
					throw new RuntimeException("stub study did not match");
				}
				mfstudy.attachments = stub.attachments;
				mfstudy.notes = stub.notes;
			}
		}
		catch (Exception e)
		{
			log("load study stub", e);
			throw new RuntimeException("stub study caught " + e);
		}
		studyCache.put(id, mfstudy);
		return mfstudy;
	}

	synchronized MFUser getUser(long id)
	{
		MFUser user = (MFUser) userIDMap.get(new Long(id));
		return user;
	}

	synchronized MFUser getUser(String name)
	{
		if (name == null)
		{
			return null;
		}
		MFUser user = (MFUser) userNameMap.get(name);
		return user;
	}

	public MFUser[] getUserList()
	{
		return users;
	}

	synchronized public String[] getUserNameList()
	{
		return (String[]) userNameMap.keySet().toArray(new String[userNameMap.size()]);
	}

	public void handleRequest(Socket s)
	{
		MFInputStream ois = null;
		MFOutputStream oos = null;
		long sessID = 0;
		InputStream resultis = null;
		try
		{
			ois = new MFInputStream((s.getInputStream()));
			oos = new MFOutputStream((s.getOutputStream()));
			try
			{
				sessID = ois.readLong();
			}
			catch (IOException e)
			{
				// eat exception on client disconnect
				return;
			}
			ServerSess sess = sessID != 0 ? (ServerSess) sessCache.get(sessID) : null;
			if (sess == null)
			{
				MFUser user = null;
				oos.writeLong(0);
				oos.flush();
				String name = (String) ois.readObject();
				String pwd = (String) ois.readObject();
				user = getUser(name);
				if (user == null || !user.password.equals(pwd))
				{
					oos.writeLong(0);
					oos.flush();
					if (user != null)
					{
						refreshUsers();
					}
					// s.shutdownInput();
					// s.shutdownOutput();
					return;
				}
				sess = addSession(user);
			}
			oos.writeLong(sess.id);
			oos.flush();
			String msg = (String) ois.readObject();
			Object result = dispatch(ois, oos, sess, msg);
			// s.shutdownInput();
			if (result instanceof Exception)
			{
				result = new MFError(((Exception) result).getMessage());
			}
			// log("result="+result);
			oos.writeObject(result);
			oos.flush();
			// wait for client close
			try
			{
				ois.read();
			}
			catch (IOException e)
			{
				;
				e.printStackTrace();
			}
			// s.shutdownOutput();
		}
		catch (Exception e)
		{
			log("mfhandleRequest", e);
		}
		finally
		{
		}
	}

	public MFStudy loadStudy(long id)
	{
		MFStudy s = getStudy(id);
		return s;
	}

	public boolean logout(long sessID)
	{
		closeSession(sessID);
		return true;
	}

	public void newStudyArrived(DSFileView v)
	{
		vlog("new study " + v.StudyInstanceUID);
		Iterator iter = sessCache.getKeys().iterator();
		while (iter.hasNext())
		{
			ServerSess sess = (ServerSess) sessCache.getNoLRU(((Long) (iter.next())).longValue());
			if (sess != null)
			{
				sess.refreshNewStudy = true;
			}
		}
		sendMessage("All Users", "" + DicomUtil.formatDateTime(new java.sql.Date(System.currentTimeMillis()), new Time(System.currentTimeMillis())) + " NEW STUDY", "" + v.Modality
				+ ": " + v.PatientName + " (" + v.PatientID + ")");
	}

	private void logCleanup(String ris, String input, String output)
	{
		RisGlueLog rlog = new RisGlueLog();
		rlog.setInput(input);
		rlog.setOutput(output);
		rlog.setRis(ris);
		rlog.setStatus("entered");
		RisGlueLogView.get().insert(rlog);
	}

	private String arrayToString(ArrayList<String> array)
	{
		String returnString = "";
		Iterator i = array.iterator();
		while (i.hasNext())
		{
			returnString += i.next();
			if (i.hasNext())
				returnString += ",";
		}
		return returnString;
	}

	private String patientCleanupAll(String ris)
	{
		PatientUtils pu = new PatientUtils();
		ArrayList<String> returnList = pu.patientCleanupAll();
		String returnString = arrayToString(returnList);
		logCleanup(ris, "patientCleanupByAll", returnString);
		return returnString;
	}

	private String patientCleanupByPatient(String ris, String patientList)
	{
		PatientUtils pu = new PatientUtils();
		ArrayList<String> returnList = pu.patientCleanup(patientList.split(","));
		String returnString = arrayToString(returnList);
		logCleanup(ris, "patientCleanupByPatient: " + patientList, returnString);
		return returnString;
	}

	private String patientCleanupByStudy(String ris, String studyList)
	{
		PatientUtils pu = new PatientUtils();
		ArrayList<String> returnList = pu.patientCleanupStudy(studyList.split(","));
		String returnString = arrayToString(returnList);
		logCleanup(ris, "patientCleanupByStudy: " + studyList, returnString);
		return returnString;
	}

	private String getLocalTime()
	{
		TimeZone tz = Calendar.getInstance().getTimeZone();
		java.sql.Date sqlTime = new java.sql.Date(Calendar.getInstance().getTimeInMillis());
		String returnString = DicomUtil.formatTime2(sqlTime) + ";" + tz.getOffset(sqlTime.getTime()) / 3600000;
		return returnString;
	}

	public boolean ping()
	{
		return true;
	}

	private String publishAttachment(String uid, String name, byte[] data)
	{
		try
		{
			Study study = StudyView.get().selectByUID(uid);
			if (study == null)
			{
				throw new RuntimeException("StudyDoesNotExist");
			}
			File f = store.putRaw(study.getStudyID(), name, new ByteArrayInputStream(data));
			WebView.get().setTranscriptPath("" + study.getStudyID(), f.getAbsolutePath());
			return "ok;";
		}
		catch (Exception e)
		{
			log("publishAttachment", e);
			return "err;" + e.getMessage();
		}
	}

	private String publishAttachment(String uid, String name, String data)
	{
		return publishAttachment(uid, name, data.getBytes());
	}

	private String publishStudy(String userId, String uid)
	{
		try
		{
			Study study = StudyView.get().selectByUID(uid);
			if (study == null)
			{
				throw new RuntimeException("StudyDoesNotExist");
			}
			String userName = WebView.get().getWebUsername(userId);
			if (userName == null || userName.length() == 0)
			{
				throw new RuntimeException("UserDoesNotExist");
			}
			WebView.get().publishStudy(userId, uid);
			return "ok;";
		}
		catch (Exception e)
		{
			log("publishStudy", e);
			return "err;" + e.getMessage();
		}
	}

	synchronized public boolean putFile(String uid, String name, byte[] data) throws Exception
	{
		if (uid == null || name == null || data == null)
		{
			return false;
		}
		Study study = StudyView.get().selectByUID(uid);
		log("putFile " + uid + " " + name + " len=" + data.length);
		if (study == null)
		{
			throw new RuntimeException("putFile: study not found " + uid);
		}
		store.putRaw(study.getStudyID(), name, new ByteArrayInputStream(data));
		return true;
	}

	public MFStudy[] query(String s, SearchBean sb)
	{
		if (sb != null && sb.getState().equals("X"))
		{
			long[] rlist = ReviewView.get().getAssigned(sb.getReader());
			List l = new LinkedList();
			for (int i = 0; i < rlist.length; i++)
			{
				MFStudy study = getStudy(rlist[i]);
				if (study != null)
				{
					l.add(study);
				}
			}
			return (MFStudy[]) l.toArray(new MFStudy[l.size()]);
		}
		else
		{
			DicomQuery query = new DicomQuery(sb);
			List l = new LinkedList();
			while (query.hasNext())
			{
				l.add(query.nextStudyBean());
			}
			MFStudy[] studies = new MFStudy[l.size()];
			Iterator iter = l.iterator();
			int index = 0;
			while (iter.hasNext())
			{
				studies[index++] = getStudy(Long.parseLong(((StudyBean) iter.next()).getStudyID())); // todo
			}
			// :
			// optimize
			return studies;
		}
	}

	public byte[] readAttachedFile(long studyid, long id) throws Exception
	{
		log("readAtt " + studyid + " " + id);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream is = null;
		try
		{
			is = store.getRawInputStream(studyid, "" + id + ".att");
			Util.copyStream(is, baos);
		}
		finally
		{
			Util.safeClose(is);
		}
		return baos.toByteArray();
	}

	private String referrerAdd(String name, String passwd)
	{
		try
		{
			String userId = WebView.get().getWebUserId(name);
			if (userId != null && userId.length() != 0)
			{
				throw new RuntimeException("AlreadyUsedUsername");
			}
			String id = WebView.get().referrerAdd(name, passwd);
			return "ok;" + id;
		}
		catch (Exception e)
		{
			log("referrerAdd", e);
			return "err;" + e.getMessage();
		}
	}

	private String referrerDelete(String id)
	{
		try
		{
			String userName = WebView.get().getWebUsername(id);
			if (userName == null || userName.length() == 0)
			{
				throw new RuntimeException("UserDoesNotExist");
			}
			WebView.get().referrerDelete(id);
			return "ok;";
		}
		catch (Exception e)
		{
			log("referrerDelete", e);
			return "err;" + e.getMessage();
		}
	}

	private String referrerModify(String id, String name, String passwd)
	{
		try
		{
			String userName = WebView.get().getWebUsername(id);
			if (userName == null || userName.length() == 0)
			{
				throw new RuntimeException("UserDoesNotExist");
			}
			if (!userName.equals(name))
			{
				String checkId = WebView.get().getWebUserId(name);
				if (checkId != null && checkId.length() != 0)
				{
					throw new RuntimeException("AlreadyUsedUsername");
				}
			}
			WebView.get().referrerModify(id, name, passwd);
			return "ok;";
		}
		catch (Exception e)
		{
			log("referrerModify", e);
			return "err;" + e.getMessage();
		}
	}

	void refreshUsers()
	{
		List users = JDBCView.get().getWebUsers();
		Iterator iter = users.iterator();
		while (iter.hasNext())
		{
			Object[] oo = (Object[]) iter.next();
			long id = Long.parseLong("" + oo[0]);
			if (getUser(id) == null)
			{
				log("remove user " + id + " " + oo[1]);
				deleteUser(id, (String) oo[1]);
			}
		}
		iter = users.iterator();
		while (iter.hasNext())
		{
			Object[] oo = (Object[]) iter.next();
			addUser(Long.parseLong("" + oo[0]), (String) oo[1], (String) oo[2]);
		}
	}

	public boolean removeReview(long studyid, String user)
	{
		ReviewView.get().unassign(studyid, user);
		return true;
	}

	public boolean requestReview(long id, String userName)
	{
		ReviewView.get().assign(id, userName);
		updateRefreshAssign(userName);
		return true;
	}

	boolean sendMessage(String user, String title, String text)
	{
		Iterator iter = sessCache.getKeys().iterator();
		while (iter.hasNext())
		{
			ServerSess sess = (ServerSess) sessCache.getNoLRU(((Long) (iter.next())).longValue());
			if (user.equalsIgnoreCase("All Users") || sess.user.name.equalsIgnoreCase(user))
			{
				sess.addMessage(title, text);
			}
		}
		return true;
	}

	public boolean sendStudy(long studyid, String aeName)
	{
		Study study = StudyView.get().selectByID(studyid);
		if (study == null)
		{
			return false;
		}
		List studyList = new LinkedList();
		studyList.add(study);
		return ServerUtil.sendStudyListAsync(AEMap.get(aeName), studyList);
	}

	public boolean sendStudyUID(String studyUID, String aeName)
	{
		Study study = StudyView.get().selectByUID(studyUID);
		if (study == null)
		{
			return false;
		}
		List studyList = new LinkedList();
		studyList.add(study);
		return ServerUtil.sendStudyListAsync(AEMap.get(aeName), studyList);
	}

	private String unpublishAttachment(String uid, String name)
	{
		try
		{
			Study study = StudyView.get().selectByUID(uid);
			log("unpublishAttachment " + uid);
			if (study == null)
			{
				throw new RuntimeException("StudyDoesNotExist;" + uid);
			}
			File f = store.getRawFile(study.getStudyID(), name);
			if (f == null || !f.exists())
			{
				throw new RuntimeException("AttachmentDoesNotExist;" + uid);
			}
			store.deleteRaw(study.getStudyID(), name);
			WebView.get().setTranscriptPath("" + study.getStudyID(), "");
			return "ok;";
		}
		catch (Exception e)
		{
			log("unpublishAttachment", e);
			return "err;" + e.getMessage();
		}
	}

	private String unpublishStudy(String userId, String uid)
	{
		try
		{
			Study study = StudyView.get().selectByUID(uid);
			if (study == null)
			{
				throw new RuntimeException("StudyDoesNotExist");
			}
			String userName = WebView.get().getWebUsername(userId);
			if (userName == null || userName.length() == 0)
			{
				throw new RuntimeException("UserDoesNotExist");
			}
			WebView.get().unpublishStudy(userId, uid);
			WebView.get().setTranscriptPath("" + study.getStudyID(), "");
			return "ok;";
		}
		catch (Exception e)
		{
			log("unpublishStudy", e);
			return "err;" + e.getMessage();
		}
	}

	synchronized public MFStudy update(MFStudy ns) throws Exception
	{
		MFStudy s = getStudy(ns.studyID);
		if (s.version != ns.version)
		{
			s.success = false;
			return s;
		}
		boolean tableDirty = false;
		boolean stubDirty = false;
		if (ns.state != s.state)
		{
			tableDirty = true;
		}
		if (!ns.reader.equals(s.reader))
		{
			tableDirty = true;
		}
		if (ns.notes.note.length != s.notes.note.length)
		{
			stubDirty = true;
		}
		if (ns.attachments.length != s.attachments.length)
		{
			stubDirty = true;
		}
		if (stubDirty)
		{
			MFStudyStub stub = ns.getStub();
			store.putRawObject(stub.studyID, "study.dat", stub);
			s.attachments = ns.attachments;
			s.notes = ns.notes;
		}
		s.state = ns.state;
		s.reader = ns.reader;
		s.version++;
		StudyView.get().risUpdate(s);
		s.success = true;
		return s;
	}

	boolean updateRefreshAssign(String user)
	{
		Iterator iter = sessCache.getKeys().iterator();
		while (iter.hasNext())
		{
			ServerSess sess = (ServerSess) sessCache.getNoLRU(((Long) (iter.next())).longValue());
			if (sess.user.name.equalsIgnoreCase(user))
			{
				sess.refreshRequest = true;
			}
		}
		return true;
	}
}
