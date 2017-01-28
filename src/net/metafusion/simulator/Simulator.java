/*
 * Created by IntelliJ IDEA.
 * User: mb
 * Date: Jul 26, 2003
 * Time: 3:06:06 PM
 */
package net.metafusion.simulator;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import net.metafusion.Dicom;
import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSInputStream;
import net.metafusion.localstore.DicomStore;
import net.metafusion.model.Image;
import net.metafusion.model.Patient;
import net.metafusion.model.PatientView;
import net.metafusion.model.Series;
import net.metafusion.model.SeriesView;
import net.metafusion.model.Study;
import net.metafusion.model.StudyView;
import net.metafusion.net.DicomClientSession;
import net.metafusion.service.CStore;
import net.metafusion.service.CStoreFile;
import net.metafusion.util.AE;
import net.metafusion.util.AEMap;
import net.metafusion.util.RoleMap;
import net.metafusion.util.UID;
import acme.storage.SSInputStream;
import acme.storage.SSStore;
import acme.util.GUI;
import acme.util.MathUtil;
import acme.util.StringUtil;
import acme.util.Util;
import acme.util.XMLConfigFile;

public class Simulator implements Runnable
{
	static void log(String s)
	{
		Util.log(s);
	}

	static void log(String s, Exception e)
	{
		Util.log(s, e);
	}
	AE ae;
	int count;

	Simulator(String aeName, int count)
	{
		instance = this;
		ae = AEMap.get(aeName);
		this.count = count;
	}

	public Simulator()
	{
		instance = this;
	}
	static Simulator instance = null;

	synchronized static public Simulator get()
	{
		if (instance == null) new Simulator();
		return instance;
	}
	ArrayList studyList = new ArrayList();
	int MAX_PATIENT = 100;
	int patientIndex = 0;
	class SimStudy
	{
		Study study;
		int patient;
		String newUID;
		ArrayList seriesList = new ArrayList();
	}
	class SimSeries
	{
		Series series;
		String newUID;
		ArrayList imageList = new ArrayList();
	}
	class SimImage
	{
		Image image;
		String newUID;
	}
	HashMap map = new HashMap();

	synchronized public void addImage(Image i)
	{
		Study study = StudyView.get().selectByID(i.getStudyID());
		Series series = SeriesView.get().selectByID(i.getSeriesID());
		Patient patient = PatientView.get().select(i.getPatientID());
		SimImage si = (SimImage) map.get("" + i.getImageID());
		if (si != null) return;
		SimStudy sx = (SimStudy) map.get("" + i.getStudyID());
		if (sx == null)
		{
			sx = new SimStudy();
			map.put("" + i.getStudyID(), sx);
			studyList.add(sx);
		}
		SimSeries ss = (SimSeries) map.get("" + i.getSeriesID());
		if (ss == null)
		{
			ss = new SimSeries();
			map.put("" + i.getSeriesID(), ss);
			sx.seriesList.add(ss);
		}
		si = new SimImage();
		si.image = i;
		map.put("" + i.getImageID(), si);
		ss.imageList.add(si);
	}
	int studyIndex = 0;
	SimStudy study = null;

	synchronized SimStudy nextStudy()
	{
		study = null;
		if (studyIndex >= studyList.size())
		{
			studyIndex = 0;
			return study;
		}
		if (studyIndex < studyList.size())
		{
			study = (SimStudy) studyList.get(studyIndex++);
			study.newUID = newInstanceUID();
			study.patient = patientIndex;
			patientIndex = (patientIndex + 1) % MAX_PATIENT;
		} else study = null;
		return study;
	}
	int seriesIndex = 0;
	SimSeries series = null;

	synchronized SimSeries nextSeries()
	{
		series = null;
		if (seriesIndex >= study.seriesList.size())
		{
			seriesIndex = 0;
			return series;
		}
		if (seriesIndex < study.seriesList.size())
		{
			series = (SimSeries) study.seriesList.get(seriesIndex++);
			series.newUID = newInstanceUID();
		} else series = null;
		return series;
	}
	int imageIndex = 0;
	SimImage image = null;

	synchronized SimImage nextImage()
	{
		image = null;
		if (imageIndex >= series.imageList.size())
		{
			imageIndex = 0;
			return image;
		}
		if (imageIndex < series.imageList.size())
		{
			image = (SimImage) series.imageList.get(imageIndex++);
			image.newUID = newInstanceUID();
			image.image.setImageUID(image.newUID);
		} else series = null;
		return image;
	}
	DicomClientSession clientSess = null;

	public void run()
	{
		DicomClientSession clientSess = null;
		String target = XMLConfigFile.getDefault().get("echo/target");
		boolean keepImages = XMLConfigFile.getDefault().getBoolean("echo/keepimages");
		for (;;)
		{
			if (clientSess != null)
			{
				clientSess.close(true);
				clientSess = null;
			}
			SimStudy study = nextStudy();
			if (study == null)
			{
				Util.sleep(5000);
				continue;
			}
			for (;;)
			{
				SimSeries series = nextSeries();
				if (series == null) break;
				for (;;)
				{
					SimImage image = nextImage();
					if (image == null) break;
					for (;;)
					{
						File f = null;
						SSInputStream ssis = null;
						FileInputStream is = null;
						try
						{
							if (clientSess == null || !clientSess.isConnected())
							{
								boolean connected = false;
								try
								{
									clientSess = new DicomClientSession(RoleMap.getStoreUserRoleMap());
									connected = clientSess.connect(AEMap.get(target));
								}
								catch (Exception e)
								{
									e.printStackTrace();
									clientSess = null;
								}
								if (connected) break;
								log("connection failed");
								Util.sleep(5000);
								continue;
							}
							Image i = image.image;
							ssis = DicomStore.get().getImageStream(i);
							ds = DSInputStream.readFrom(ssis, UID.get(i.getImageUID()), keepImages);
							f = gen(ds, "patient^p" + study.patient, "pid-" + study.patient, study.newUID, series.newUID, image.newUID);
							is = new FileInputStream(f);
							CStore store = new CStore(clientSess, image.image, is);
							store.run();
							if (store.getResult() == Dicom.SUCCESS)
								break;
							else
							{
								log("send failed: retrying");
								Util.sleep(5000);
								continue;
							}
						}
						catch (Exception e)
						{
							log("send failed: retrying", e);
							break;
						}
						finally
						{
							Util.safeClose(is);
							Util.safeClose(ssis);
							Util.safeDelete(f);
						}
					}
				}
			}
		}
	}

	public void test() throws Exception
	{
		String target = XMLConfigFile.getDefault().get("echo/target");
		boolean keepImages = XMLConfigFile.getDefault().getBoolean("echo/keepimages");
		long id = 1115428736256l;
		Image i = DicomStore.get().getImage(id);
		SSInputStream ssis = DicomStore.get().getImageStream(i);
		ds = DSInputStream.readFrom(ssis, UID.get(i.getImageUID()), true);
		File f = gen(ds, "pat", "patid", "1.2.3.4", "2.3.4.5", "3.4.5.6");
		log("" + f.getAbsolutePath());
	}
	Calendar c = Calendar.getInstance();
	DS ds;
	long lastT = 0;

	void setTime(long t)
	{
		if (t == lastT) t = lastT + 1;
		lastT = t;
		c.setTime(new java.util.Date(t));
	}

	String date8()
	{
		return StringUtil.int4(c.get(Calendar.YEAR)) + StringUtil.int2(c.get(Calendar.MONTH) + 1) + StringUtil.int2(c.get(Calendar.DAY_OF_MONTH));
	}

	String time6()
	{
		return StringUtil.int2(c.get(Calendar.HOUR_OF_DAY)) + StringUtil.int2(c.get(Calendar.MINUTE)) + StringUtil.int2(c.get(Calendar.SECOND));
	}
	int uidCount = 0;

	String newInstanceUID()
	{
		return "1." + date8() + "." + time6() + "." + (uidCount++);
	}

	void replace(net.metafusion.util.Tag t, String s)
	{
		if (ds.contains(t)) ds.put(t, s);
	}
	int MAXID = 10000;
	ArrayList ids = new ArrayList(MAXID);
	{
		for (int i = 0; i < MAXID; i++)
			ids.add(i, new Integer(i));
		Collections.shuffle(ids);
	}
	int idIndex = 0;
	int id = 0;

	String getName(String h)
	{
		if (idIndex == MAXID)
			id = 0;
		else id = ((Integer) ids.get(idIndex++)).intValue();
		return h + StringUtil.int4(id);
	}

	int getLastID()
	{
		return id;
	}
	// int STUDY_PER_MAX=10;
	// int SERIES_PER=10;
	// int IMAGES_PER=30;
	static int STUDY_PER_MAX = 3;
	static int SERIES_PER = 5;
	static int IMAGES_PER = 20;

	void xxrun() throws Exception
	{
		// int studyCount = MathUtil.rand(1,STUDY_PER_MAX);
		// int seriesCount = MathUtil.rand(1,SERIES_PER);
		// int imageCount = MathUtil.rand(1,IMAGES_PER);
		File f = null;
		try
		{
			DicomClientSession sess = new DicomClientSession(RoleMap.getClientRoleMap());
			sess.connect(ae);
			while (count-- > 0)
			{
				int studyCount = MathUtil.rand(1, STUDY_PER_MAX);
				int seriesCount = MathUtil.rand(1, SERIES_PER);
				int imageCount = MathUtil.rand(1, IMAGES_PER);
				log("1:" + studyCount + ":" + seriesCount + ":" + imageCount);
				String patName = getName("PAT");
				String patId = "ID" + StringUtil.int4(getLastID());
				for (int i = 0; i < studyCount; i++)
				{
					String studyInstanceUID = newInstanceUID();
					for (int j = 0; j < seriesCount; j++)
					{
						String seriesInstanceUID = newInstanceUID();
						for (int k = 0; k < imageCount; k++)
							try
							{
								f = gen(null, patName, patId, studyInstanceUID, seriesInstanceUID, newInstanceUID());
								CStoreFile store = new CStoreFile(sess, f);
								store.run();
								assert store.getResult() == Dicom.SUCCESS;
							}
							catch (Exception e)
							{
								e.printStackTrace();
							}
							finally
							{
								Util.safeDelete(f);
							}
					}
				}
			}
			sess.close(true);
		}
		finally
		{
			;// Util.safeDelete(f);
		}
	}

	File gen(DS ds, String patName, String patId, String studyInstanceUID, String seriesInstanceUID, String instanceUID) throws Exception
	{
		log("gen: " + patName + ":" + patId + ":" + studyInstanceUID + ":" + seriesInstanceUID + ":" + instanceUID);
		setTime(System.currentTimeMillis());
		if (ds == null) ds = DSInputStream.readFileAndImages(new File("c:/mr.dat"));
		// ds = DSInputStream.readFileAndImages(new File("c:/mr_nopix.dat"));
		// Log(ds.toString());
		// remove all file tags
		Set tags = ds.getTags();
		Iterator iter = tags.iterator();
		while (iter.hasNext())
		{
			net.metafusion.util.Tag t = (net.metafusion.util.Tag) iter.next();
			if (t.getGroup() == 0x0002) iter.remove();
		}
		// String instanceUID = newInstanceUID();
		// log("gen:
		// "+patName+":"+patId+":"+studyInstanceUID+":"+seriesInstanceUID+":"+instanceUID);
		replace(net.metafusion.util.Tag.SOPInstanceUID, instanceUID);
		replace(net.metafusion.util.Tag.StudyDate, date8());
		replace(net.metafusion.util.Tag.SeriesDate, date8());
		replace(net.metafusion.util.Tag.ContentDate, date8());
		replace(net.metafusion.util.Tag.StudyTime, time6() + ".0");
		replace(net.metafusion.util.Tag.SeriesTime, time6() + ".0");
		replace(net.metafusion.util.Tag.ContentTime, time6() + ".0");
		replace(net.metafusion.util.Tag.AccessionNumber, "ACC" + StringUtil.int4(uidCount++));
		replace(net.metafusion.util.Tag.ReferringPhysicianName, getName("DOC"));
		replace(net.metafusion.util.Tag.PatientName, patName);
		replace(net.metafusion.util.Tag.PatientID, patId);
		replace(net.metafusion.util.Tag.StudyInstanceUID, studyInstanceUID);
		replace(net.metafusion.util.Tag.SeriesInstanceUID, seriesInstanceUID);
		replace(net.metafusion.util.Tag.StudyID, getName(""));
		replace(net.metafusion.util.Tag.SeriesNumber, getName(""));
		replace(net.metafusion.util.Tag.AcquisitionNumber, "1");
		replace(net.metafusion.util.Tag.InstanceNumber, "1");
		File f = SSStore.get().createTempFile(".tmp");
		ds.writeTo(f);
		return f;
	}

	File gen1() throws Exception
	{
		setTime(System.currentTimeMillis());
		ds = DSInputStream.readFileAndImages(new File("c:/mr.dat"));
		// ds = DSInputStream.readFileAndImages(new File("c:/mr_nopix.dat"));
		// Log(ds.toString());
		// remove all file tags
		Set tags = ds.getTags();
		Iterator iter = tags.iterator();
		while (iter.hasNext())
		{
			net.metafusion.util.Tag t = (net.metafusion.util.Tag) iter.next();
			if (t.getGroup() == 0x0002) iter.remove();
		}
		String instanceUID = newInstanceUID();
		replace(net.metafusion.util.Tag.SOPInstanceUID, instanceUID);
		replace(net.metafusion.util.Tag.StudyDate, date8());
		replace(net.metafusion.util.Tag.SeriesDate, date8());
		replace(net.metafusion.util.Tag.ContentDate, date8());
		replace(net.metafusion.util.Tag.StudyTime, time6() + ".0");
		replace(net.metafusion.util.Tag.SeriesTime, time6() + ".0");
		replace(net.metafusion.util.Tag.ContentTime, time6() + ".0");
		replace(net.metafusion.util.Tag.AccessionNumber, "ACC" + StringUtil.int4(uidCount++));
		replace(net.metafusion.util.Tag.ReferringPhysicianName, getName("DOC"));
		replace(net.metafusion.util.Tag.PatientName, getName("PAT"));
		replace(net.metafusion.util.Tag.PatientID, "ID" + StringUtil.int4(getLastID()));
		replace(net.metafusion.util.Tag.StudyInstanceUID, newInstanceUID());
		replace(net.metafusion.util.Tag.SeriesInstanceUID, newInstanceUID());
		replace(net.metafusion.util.Tag.StudyID, getName(""));
		replace(net.metafusion.util.Tag.SeriesNumber, getName(""));
		replace(net.metafusion.util.Tag.AcquisitionNumber, "1");
		replace(net.metafusion.util.Tag.InstanceNumber, "1");
		File f = File.createTempFile("temp", "tmp");
		ds.writeTo(f);
		return f;
	}

	void runXX() throws Exception
	{
		File f = null;
		try
		{
			DicomClientSession sess = new DicomClientSession(RoleMap.getClientRoleMap());
			sess.connect(ae);
			while (count-- > 0)
			{
				f = gen1();
				CStoreFile store = new CStoreFile(sess, f);
				store.run();
				assert store.getResult() == Dicom.SUCCESS;
			}
			sess.close(true);
		}
		finally
		{
			Util.safeDelete(f);
		}
	}

	public static void main(String[] args)
	{
		try
		{
			int count = 0;
			try
			{
				String target = args[1];
				count = Integer.parseInt(args[2]);
				STUDY_PER_MAX = Integer.parseInt(args[3]); // 3
				SERIES_PER = Integer.parseInt(args[4]); // 5
				IMAGES_PER = Integer.parseInt(args[5]); // 20
				log("target =" + target);
				log("iterations=" + count);
				log("STUDY_PER_MAX=" + STUDY_PER_MAX);
				log("SERIES_PER=" + SERIES_PER);
				log("IMAGES_PER=" + IMAGES_PER);
			}
			catch (Exception e)
			{
				log("usage: xmlconfig xmltarget #iterations #STUDY_PER_MAX #SERIES_PER #IMAGES_PER");
				System.exit(-1);
			}
			XMLConfigFile configFile = new XMLConfigFile(new File(args[0]), args[1]);
			Dicom.init();
			Util.log("Simulator");
			// Log.enableAll(false);
			Simulator sim = new Simulator("pcserver1", count);
			// sim.run();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void xmain(String[] args)
	{
		log("start");
		try
		{
			Dicom.init();
			String s = "<frame name='Simulator'> " + "<items>" + "<textfield label='serverae: ' name='sae' />" + "<textfield label='count: ' name='count' />" + "</items>"
					+ "<defaults>" + "<default name='sae' value='SERVER'/>" + "<default name='count' value='1' />" + "</defaults>" + "</frame>";
			for (;;)
			{
				HashMap hm = GUI.showGUI(s);
				Simulator sim = new Simulator((String) hm.get("sae"), Integer.parseInt((String) hm.get("count")));
				// sim.run();
			}
		}
		catch (Exception e)
		{
			log("simulator caught " + e);
			e.printStackTrace();
		}
	}
}