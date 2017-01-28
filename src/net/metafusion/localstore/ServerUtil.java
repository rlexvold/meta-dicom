package net.metafusion.localstore;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.metafusion.Dicom;
import net.metafusion.model.Image;
import net.metafusion.model.ImageView;
import net.metafusion.model.Study;
import net.metafusion.model.StudyView;
import net.metafusion.net.DicomClientSession;
import net.metafusion.service.CEcho;
import net.metafusion.service.CStore;
import net.metafusion.util.AE;
import net.metafusion.util.DicomDir;
import net.metafusion.util.DicomUtil;
import net.metafusion.util.RoleMap;
import acme.storage.SSStore;
import acme.util.FileUtil;
import acme.util.Log;
import acme.util.Util;
import acme.util.ZipUtil;

public class ServerUtil
{
	static public void log(String s)
	{
		Log.log(s);
	}

	static public void log(String s, Exception e)
	{
		Log.log(s, e);
	}

	static public void vlog(String s)
	{
		Log.log(s);
	}

	public static boolean verify(AE ae)
	{
		if (ae == null) return false;
		boolean fail = true;
		DicomClientSession sess = null;
		try
		{
			log("verify to " + ae);
			sess = new DicomClientSession(RoleMap.getPingRoleMap());
			sess.connect(ae);
			if (sess.isConnected())
			{
//				CEcho echo = new CEcho(sess);
//				echo.run();
//				if (echo.getResult() != Dicom.SUCCESS)
//					log("ping failed");
//				else
					fail = false;
			}
		}
		catch (Exception e)
		{
			log("ping caught ", e);
		}
		finally
		{
			if (sess != null) sess.close(true);
		}
		return !fail;
	}

	static public boolean sendImages(AE ae, List imageList)
	{
		DicomClientSession clientSess = null;
		boolean good = false;
		try
		{
			Iterator iter = imageList.iterator();
			clientSess = new DicomClientSession(RoleMap.getStoreUserRoleMap());
			boolean connected = clientSess.connect(ae);
			if (!connected) return false;
			while (connected && iter.hasNext())
			{
				Image image = (Image) iter.next();
				Log.log("send " + image.getImageID() + " to " + ae);
				CStore store = new CStore(clientSess, image);
				store.run();
				if (store.getResult() != Dicom.SUCCESS) return false;
			}
			good = true;
			clientSess.close(true);
		}
		catch (Exception e)
		{
			log("sendImages caught ", e);
		}
		finally
		{
			if (clientSess != null) clientSess.close(false);
		}
		return good;
	}

	static public boolean sendStudy(AE ae, Study s)
	{
		if (ae == null) return false;
		List imageList = ImageView.get().selectByStudy(s.getStudyID());
		return sendImages(ae, imageList);
	}
	static public class StudySender implements Runnable
	{
		private AE ae;
		private List studyList;

		public StudySender(AE ae, List studyList)
		{
			this.ae = ae;
			this.studyList = studyList;
		}

		public void run()
		{
			Iterator iter = studyList.iterator();
			while (iter.hasNext())
			{
				Study s = (Study) iter.next();
				boolean good = sendStudy(ae, s);
				if (!good)
				{
					Log.log("sendstudy " + ae + " " + s.getStudyUID() + " failed");
					return;
				}
			}
		}
	}

	static public boolean sendStudyListAsync(AE ae, List studyList)
	{
		if (!verify(ae))
		{
			log("sendStudyAsync: could not verify " + ae);
			return false;
		}
		Runnable r = new StudySender(ae, studyList);
		new Thread(r).start();
		return true;
	}

	static public String[] archiveToDVD(List studyList)
	{
		File root = null;
		try
		{
			root = SSStore.get().createTempDir("DICOMDIR");
			root.mkdir();
			java.sql.Date d = new java.sql.Date(System.currentTimeMillis());
			File dicomDirRoot = new File(root, "DICOMDIR" + DicomUtil.formatDate(d) + DicomUtil.formatTime(d));
			dicomDirRoot.mkdir();
			DicomDir dd = new DicomDir(dicomDirRoot, studyList);
			String[] exec = Util.execAndThrow("growisofs -dvd-compat -Z /dev/scd0 -R -J " + dicomDirRoot.getAbsolutePath());
			return exec;
		}
		catch (Exception e)
		{
			Log.log("archiveToDVD caught ", e);
			return new String[] { "ArchiveToDVD failed " + e };
		}
		finally
		{
			FileUtil.safeDeleteDirRecursive(root);
		}
	}

	static public void archiveToZip(List studyList, File zipFile)
	{
		File root = null;
		try
		{
			root = SSStore.get().createTempDir("DICOMDIR");
			root.mkdir();
			java.sql.Date d = new java.sql.Date(System.currentTimeMillis());
			File dicomDirRoot = new File(root, "DICOMDIR" + DicomUtil.formatDate(d) + DicomUtil.formatTime(d));
			dicomDirRoot.mkdir();
			DicomDir dd = new DicomDir(dicomDirRoot, studyList);
			ZipUtil.zip(dicomDirRoot, zipFile);
		}
		catch (Exception e)
		{
			Log.log("handleArchive caught ", e);
		}
		finally
		{
			FileUtil.safeDeleteDirRecursive(root);
		}
	}

	static public List getStudiesFromUIDList(List studyUIDList)
	{
		ArrayList list = new ArrayList();
		Iterator iter = studyUIDList.iterator();
		while (iter.hasNext())
		{
			String uid = (String) iter.next();
			Study study = StudyView.get().selectByUID(uid);
			if (study == null) throw new RuntimeException("could not find study uid=" + uid);
			list.add(study);
		}
		return list;
	}
}
// ///// HACKKKKKKKKKKKKKKKKKKKKKKK
// try {
// File f = DicomDir.CreateDicomDirZipForWeb(study);
// Log.log("CREATED ZIP "+ f.getAbsolutePath());
// String update = "update web_study set studypath = '"+f.getAbsolutePath()+"'
// "+
// " where dcm_studyid = "+study.getStudyID();
// update = StringUtil.replaceAll(update, "\\","\\\\");
// Log.log("UPDATE IS "+ update);
// JDBCUtil.get().update(update);
// } catch (Exception e) {
// e.printStackTrace();
// Log.log("CREATED ZIP HACK caught "+e);
// }
// ///// HACKKKKKKKKKKKKKKKKKKKKKKK
// void handleArchive(XML studyUIDList, OutputStream os) {
// File root = null;
// File dicomDirRoot = null;
// File zipFile = null;
// FileInputStream fis = null;
//
// try {
// List studies = new ArrayList();
// Iterator iter = studyUIDList.getList().iterator();
// while (iter.hasNext()) {
// XML uid = (XML)iter.next();
// log(uid.get());
// //os.write(uid.get().getBytes());
//
// Study study = (Study)StudyView.get().selectByUID(uid.get());
// if (study == null)
// throw new RuntimeException("archive: could not find study uid="+uid.get());
//
// ///// HACKKKKKKKKKKKKKKKKKKKKKKK
// try {
// File f = DicomDir.CreateDicomDirZipForWeb(study);
// Log.log("CREATED ZIP "+ f.getAbsolutePath());
// String update = "update web_study set studypath = '"+f.getAbsolutePath()+"'
// "+
// " where dcm_studyid = "+study.getStudyID();
// update = StringUtil.replaceAll(update, "\\","\\\\");
// Log.log("UPDATE IS "+ update);
// JDBCUtil.get().update(update);
// } catch (Exception e) {
// e.printStackTrace();
// Log.log("CREATED ZIP HACK caught "+e);
// }
// ///// HACKKKKKKKKKKKKKKKKKKKKKKK
//
//
// studies.add(study);
//
// }
// root =
// Util.generateUniqueName(SSStoreFactory.getStore().getTempRoot(),"DICOMDIR","");
// root.mkdir();
//
//
// //// ?????? !!! does leak temp dires
// java.sql.Date d = new java.sql.Date(System.currentTimeMillis());
// dicomDirRoot = new File(root, "DICOMDIR"+DicomUtil.formatDate(d) +
// DicomUtil.formatTime(d));
// dicomDirRoot.mkdir();
//
// zipFile = new File(root, "dicomdir.zip");
//
// DicomDir dd = new DicomDir(dicomDirRoot, studies);
// ZipUtil.zip(dicomDirRoot, zipFile);
//
// } catch (Exception e) {
// Log.log("archive caught ",e);
// } finally {
// FileUtil.safeDeleteDir(root);
// }
// }
