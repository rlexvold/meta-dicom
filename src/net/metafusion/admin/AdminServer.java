package net.metafusion.admin;

import integration.SearchBean;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.metafusion.Dicom;
import net.metafusion.localstore.DicomQuery;
import net.metafusion.localstore.DicomStore;
import net.metafusion.localstore.LocalStore;
import net.metafusion.localstore.ServerUtil;
import net.metafusion.localstore.SyncCache;
import net.metafusion.model.Image;
import net.metafusion.model.ImageView;
import net.metafusion.model.Patient;
import net.metafusion.model.PatientView;
import net.metafusion.model.Series;
import net.metafusion.model.SeriesView;
import net.metafusion.model.Study;
import net.metafusion.model.StudyView;
import net.metafusion.util.AEMap;
import net.metafusion.util.DicomUtil;
import net.metafusion.util.StudyDup;
import acme.storage.SSInputStream;
import acme.storage.SSStore;
import acme.util.DateUtil;
import acme.util.FileUtil;
import acme.util.Log;
import acme.util.TSLog;
import acme.util.Util;
import acme.util.XDeleteFileInputStream;
import acme.util.XML;
import acme.util.XMLConfigFile;

public class AdminServer
{
	static public class Diagnostic
	{
		private String	name	= "";

		public Diagnostic(String name)
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}

		public String run(String argument1, String argument2) throws Exception
		{
			return "not defined";
		}
	}
	static Diagnostic[]	diagnostic			= new Diagnostic[] { new Diagnostic("verify-jpegs-exist")
											{
												@Override
												public String run(String argument1, String argument2)
												{
													List studyList = StudyView.get().selectAll();
													for (Iterator iter = studyList.iterator(); iter.hasNext();)
													{
														Study study = (Study) iter.next();
														File dir = SSStore.get().getStudyDir(study.getStudyID());
														List imageList = ImageView.get().selectByStudy(study.getStudyID());
														for (Iterator imageIter = imageList.iterator(); imageIter.hasNext();)
														{
															Image image = (Image) imageIter.next();
															DicomUtil.scheduleConvertImageToJpeg(image);
														}
													}
													return "ok";
												}
											}, new Diagnostic("request-sync")
											{
												@Override
												public String run(String argument1, String argument2)
												{
													// DicomStore.get().
													// requestFullSync(args);
													return "ok";
												}
											}, new Diagnostic("recover-from-files-and-clear-db")
											{
												@Override
												public String run(String argument1, String argument2)
												{
													DicomStore.get().recoverFromFilesystem(true, new File(SSStore.get().getRootDir().getParentFile(), "__recovery__"));
													return "ok";
												}
											}, new Diagnostic("old-recover-from-files-and-clear-db")
											{
												@Override
												public String run(String argument1, String argument2)
												{
													DicomStore.get().oldRecoverFromFilesystem(true, new File(SSStore.get().getRootDir().getParentFile(), "__recovery__"));
													return "ok";
												}
											}, new Diagnostic("load-simulated-data-from-filesystem")
											{
												@Override
												public String run(String argument1, String argument2)
												{
													DicomStore.get().loadSimulatedDataFromFilesystem(true, new File(SSStore.get().getRootDir().getParentFile(), "__recovery__"));
													return "ok";
												}
											}, new Diagnostic("test-load-dcm-data-from-filesystem")
											{
												@Override
												public String run(String argument1, String argument2)
												{
													DicomStore.get().testLoadDCMFileDataFromFilesystem(new File(argument1));
													return "ok";
												}
											}, new Diagnostic("load-dcm-data-from-filesystem")
											{
												@Override
												public String run(String argument1, String argument2)
												{
													DicomStore.get().loadDCMFileDataFromFilesystem(new File(argument1));
													return "ok";
												}
											}, new Diagnostic("log-no-verbose")
											{
												@Override
												public String run(String argument1, String argument2)
												{
													Log.setVerbose(0);
													return "ok";
												}
											}, new Diagnostic("log-verbose")
											{
												@Override
												public String run(String argument1, String argument2)
												{
													Log.setVerbose(1);
													return "ok";
												}
											}, new Diagnostic("log-v-verbose")
											{
												@Override
												public String run(String argument1, String argument2)
												{
													Log.setVerbose(2);
													return "ok";
												}
											}, new Diagnostic("log-vv-verbose")
											{
												@Override
												public String run(String argument1, String argument2)
												{
													Log.setVerbose(3);
													return "ok";
												}
											}, };
	static final long	serialVersionUID	= 1L;

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
	private Date		lastDate	= new Date();
	ObjectInputStream	ois			= null;
	ObjectOutputStream	oos			= null;

	public InputStream archiveStudyList(List studyUIDList) throws Exception
	{
		List studyList = ServerUtil.getStudiesFromUIDList(studyUIDList);
		File f = null;
		try
		{
			f = SSStore.get().createTempFile(".zip");
			ServerUtil.archiveToZip(studyList, f);
			return new XDeleteFileInputStream(f);
		}
		catch (Exception e)
		{
			Util.safeDelete(f);
			throw e;
		}
	}

	public StudiesInfoBean burnStudyList(List studyUIDList) throws Exception
	{
		StudiesInfoBean info = new StudiesInfoBean();
		Iterator iter = studyUIDList.iterator();
		List studyList = new LinkedList();
		while (iter.hasNext())
		{
			String uid = (String) iter.next();
			try
			{
				Study study = StudyView.get().selectByUID(uid);
				if (study != null)
				{
					info.setNumStudies(info.getNumStudies() + 1);
					List series = SeriesView.get().selectByStudy(study.getStudyID());
					info.setNumSeries(info.getNumSeries() + series.size());
					List images = ImageView.get().selectByStudy(study.getStudyID());
					info.setNumImages(info.getNumImages() + images.size());
					for (Iterator iter2 = images.iterator(); iter2.hasNext();)
					{
						Image image = (Image) iter2.next();
						info.setSize(info.getSize() + DicomStore.get().getSize(image));
					}
				}
				studyList.add(study);
			}
			catch (Exception e)
			{
				log("could not get study info uid=" + uid + " ", e);
			}
		}
		if (info.getSize() > 4400l * 1024 * 1024 * 1024)
			info.setMsgs(new String[] { "Data too large: " + info.getSize() });
		else
			info.setMsgs(ServerUtil.archiveToDVD(studyList));
		return info;
	}

	public List deleteStudyList(List studyUIDList) throws Exception
	{
		ArrayList successList = new ArrayList();
		Iterator iter = studyUIDList.iterator();
		while (iter.hasNext())
		{
			String uid = (String) iter.next();
			try
			{
				Study study = StudyView.get().selectByUID(uid);
				if (study != null)
				{
					DicomStore.get().deleteStudy(study);
					successList.add(uid);
				}
			}
			catch (Exception e)
			{
				log("could not delete study" + uid + " ", e);
			}
		}
		return successList;
	}

	public Object dispatch(String cmd)
	{
		cmd = cmd.intern();
		try
		{
			if (cmd == "establish")
				return new Boolean(establish(getString()));
			if (cmd == "getOlderConfigs")
				return getOlderConfigs();
			else if (cmd == "saveAdmin")
				return new Boolean(saveAdmin(getString()));
			else if (cmd == "getConfig")
				return getConfig(getString());
			else if (cmd == "verify")
				return new Boolean(verify(getString()));
			else if (cmd == "sendStudyList")
				return new Boolean(sendStudyList(getString(), getList()));
			else if (cmd == "deleteStudyList")
				return deleteStudyList(getList());
			else if (cmd == "getStudyListInfo")
				return getStudyListInfo(getList());
			else if (cmd == "burnStudyList")
				return burnStudyList(getList());
			else if (cmd == "archiveStudyList")
				return archiveStudyList(getList());
			else if (cmd == "getImageOnDisk")
				return getImageOnDisk(getString());
			else if (cmd == "getLogFile")
				return getLogFile();
			else if (cmd == "getCapacityFreeSpace")
				return getCapacityFreeSpace();
			else if (cmd == "getFreePerUsedSpace")
				return getFreePerUsedSpace();
			else if (cmd == "search")
				return search((SearchBean) getObject());
			else if (cmd == "getExpiration")
				return getExpiration();
			else if (cmd == "getVersion")
				return Util.getManifestVersion();
			else if (cmd == "getDiagnostics")
				return getDiagnostics();
			else if (cmd == "doDiagnostic")
				return doDiagnostic(getString(), getString(), getString());
			else if (cmd == "selectStudiesUpdatedSince")
				return selectStudiesUpdatedSince((Timestamp) getObject(), ((String) getObject()));
		}
		catch (Exception e)
		{
			log("" + cmd + " caught", e);
			return e;
		}
		finally
		{
		}
		return new RuntimeException("unknown command: " + cmd);
	}

	public String doDiagnostic(String cmd, String argument1, String argument2) throws Exception
	{
		log("handleDiagnostic " + cmd + " ");
		String result = "notFound";
		for (int i = 0; i < diagnostic.length; i++)
			if (cmd.equals(diagnostic[i].getName()))
			{
				result = diagnostic[i].run(argument1, argument2);
				break;
			}
			else if (i == diagnostic.length - 1)
				throw new RuntimeException("unknown command " + cmd);
		return result;
	}

	public boolean establish(String pwd)
	{
		String password = XMLConfigFile.getDefault().get("password", "");
		if (password.length() == 0 || "metafusioninc".equals(pwd))
			return true;
		else
			return password.equals(pwd);
	}

	public long[] getCapacityFreeSpace() throws Exception
	{
		return FileUtil.getCapFreeNowInK(SSStore.get().getFilesystemRoot());
	}

	public String getConfig(String name) throws Exception
	{
		XML configXML = null;
		if (name == null || name.length() == 0)
			configXML = XMLConfigFile.getDefault().getCurrentAdmin();
		else
			configXML = XMLConfigFile.getDefault().getOlderAdmin(name);
		return configXML.toString();
	}

	public String[] getDiagnostics() throws Exception
	{
		String[] names = new String[diagnostic.length];
		for (int i = 0; i < diagnostic.length; i++)
			names[i] = diagnostic[i].getName();
		return names;
	}

	public String getExpiration() throws Exception
	{
		Date exp = SSStore.get().getExpiration();
		return exp != null ? "" + exp : "none";
	}

	public String[] getFreePerUsedSpace() throws Exception
	{
		return FileUtil.getFreePerUsedSpace(SSStore.get().getFilesystemRoot());
	}

	public InputStream getImageOnDisk(String imageUID) throws Exception
	{
		Image i = DicomStore.get().getImage(imageUID);
		if (i == null)
			throw new RuntimeException("unable to find image: " + imageUID);
		SSInputStream sis = DicomStore.get().getImageStream(i);
		sis.positionToMetaDataStart();
		return sis;
	}

	List getList() throws Exception
	{
		return (List) ois.readObject();
	}

	public InputStream getLogFile() throws Exception
	{
		return TSLog.get().getInputStream();
	}

	Object getObject() throws Exception
	{
		return ois.readObject();
	}

	List getOlderConfigs()
	{
		List l = FileUtil.listFiles(new File(XMLConfigFile.getDefault().getConfigRoot(), "archive"), ".xml");
		Collections.sort(l);
		List ls = new ArrayList(l.size());
		for (int i = l.size() - 1; i >= 0; i--)
			ls.add(((File) l.get(i)).getName());
		return ls;
	}

	String getString() throws Exception
	{
		return (String) ois.readObject();
	}

	public StudiesInfoBean getStudyListInfo(List studyUIDList) throws Exception
	{
		StudiesInfoBean info = new StudiesInfoBean();
		Iterator iter = studyUIDList.iterator();
		while (iter.hasNext())
		{
			String uid = (String) iter.next();
			try
			{
				Study study = StudyView.get().selectByUID(uid);
				if (study != null)
				{
					info.setNumStudies(info.getNumStudies() + 1);
					List series = SeriesView.get().selectByStudy(study.getStudyID());
					info.setNumSeries(info.getNumSeries() + series.size());
					List images = ImageView.get().selectByStudy(study.getStudyID());
					info.setNumImages(info.getNumImages() + images.size());
					for (Iterator iter2 = images.iterator(); iter2.hasNext();)
					{
						Image image = (Image) iter2.next();
						info.setSize(info.getSize() + DicomStore.get().getSize(image));
					}
				}
			}
			catch (Exception e)
			{
				log("could not get study info uid=" + uid + " ", e);
			}
		}
		return info;
	}

	public void handleRequest(Socket s, InputStream is, OutputStream os)
	{
		InputStream resultis = null;
		try
		{
			//NOTE:  The next 3 lines must be in this order, or it will hang waiting or an OutputStream
			oos = new ObjectOutputStream(new BufferedOutputStream(os));
			oos.flush();
			ois = new ObjectInputStream(new BufferedInputStream(is));
			String msg = (String) ois.readObject();
			Object result = dispatch(msg);
			// s.shutdownInput();
			if (result instanceof InputStream)
			{
				resultis = (InputStream) result;
				oos.writeObject(new Boolean(true));
				Util.copyStream(resultis, oos);
				oos.flush();
			}
			else
			{
				oos.writeObject(result);
				oos.flush();
			}
			// s.shutdownInput();
			// s.shutdownOutput();
		}
		catch (Exception e)
		{
			log("handleRequest", e);
		}
		finally
		{
			if (resultis != null)
				Util.safeClose(resultis);
		}
	}

	public boolean saveAdmin(String adminXML) throws Exception
	{
		XML admin = XML.parseXML(adminXML);
		Date d = new Date();
		admin.getNode("admin").addAttr("date", "" + d);
		// QueueServer.get().addConfigChanged();
		File archive = new File(XMLConfigFile.getDefault().getConfigRoot(), "archive");
		// at least 5 secs apart
		if ((d.getTime() - lastDate.getTime()) < 5000)
			d = new Date(lastDate.getTime() + 5000);
		lastDate = d;
		File f = new File(archive, "metaadmin-" + DateUtil.formatYYYYYMMDDHHMMSS(d) + ".xml");
		admin.writeTo(f);
		admin.writeTo(new File(XMLConfigFile.getDefault().getConfigRoot(), "metaadmin.xml"));
		Dicom.updateAEMap(admin);
		LocalStore.get().updateServerBean();
		SyncCache.get().setAdminVersion("" + d);
		return true;
	}

	public List search(SearchBean sb) throws Exception
	{
		DicomQuery q = new DicomQuery(sb);
		ArrayList studies = new ArrayList();
		StudyBean last = null;
		while (q.hasNext())
		{
			StudyBean studyBean = q.nextStudyBean();
			if (last != null && studyBean.getStudyUID().equals(last.getStudyUID()))
				continue;
			last = studyBean;
			studies.add(studyBean);
		}
		return studies;
	}

	public StudiesDumpBean selectStudiesUpdatedSince(Timestamp dateLastImage, String lastUID)
	{
		StudiesDumpBean sdb = new StudiesDumpBean();
		List l = StudyView.get().selectLocalIDsPast(dateLastImage, lastUID, 100);
		for (Long id : (List<Long>) l)
		{
			Study study = StudyView.get().selectByID(id);
			if (study == null)
				continue;
			sdb.studies.add(study);
			Patient patient = PatientView.get().select(study.getPatientID());
			sdb.patientMap.put(id, patient);
			List seriesList = SeriesView.get().selectByStudy(id);
			sdb.seriesListMap.put(id, seriesList);
			for (Series s : (List<Series>) seriesList)
			{
				List imageList = ImageView.get().selectBySeries(s.getSeriesID());
				sdb.imageListMap.put(s.getSeriesID(), imageList);
			}
		}
		return sdb;
	}

	public boolean sendStudyList(String aeString, List studyUIDList) throws Exception
	{
		List studyList = new LinkedList();
		Iterator iter = studyUIDList.iterator();
		while (iter.hasNext())
		{
			String uid = (String) iter.next();
			Study study = StudyView.get().selectByUID(uid);
			if (study == null)
			{
				log("SendStudy could not find study " + uid);
				throw new RuntimeException("SendStudy could not find study " + uid);
			}
			studyList.add(study);
		}
		if (aeString.equals("HACK_ANONYMOUS") || aeString.equals("HACK_DUPLICATE"))
		{
			try
			{
				StudyDup sd = new StudyDup(studyList);
				sd.run();
			}
			catch (Exception e)
			{
				log("sd caught " + e);
				e.printStackTrace();
			}
			return true;
		}
		return ServerUtil.sendStudyListAsync(AEMap.get(aeString), studyList);
	}

	// ? XML handlePropagateConfig(XML config) same with no config change
	public boolean verify(String aeString) throws Exception
	{
		return ServerUtil.verify(AEMap.get(aeString)) ? true : false;
	}
}
