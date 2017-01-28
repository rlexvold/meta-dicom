package net.metafusion.localstore.mirror;

import java.io.File;
import java.net.ConnectException;
import java.sql.Timestamp;
import java.util.List;
import net.metafusion.admin.AdminClient;
import net.metafusion.admin.StudiesDumpBean;
import net.metafusion.localstore.DicomStore;
import net.metafusion.model.Image;
import net.metafusion.model.ImageView;
import net.metafusion.model.Patient;
import net.metafusion.model.PatientView;
import net.metafusion.model.Series;
import net.metafusion.model.SeriesView;
import net.metafusion.model.Study;
import net.metafusion.model.StudyView;
import net.metafusion.util.AE;
import net.metafusion.util.AEMap;
import acme.storage.SSStore;
import acme.util.Util;

public class MirrorClient implements Runnable
{
	public static void log(String s)
	{
		Util.log("" + s);
	}

	public static void startInstance(String aet) throws Exception
	{
		// Timestamp ts = StudyView.get().getDateLastImage(aet);
		// long id = StudyView.get().getLastStudyIDForAET(aet, ts);
		//
		// List l = StudyView.get().selectLocalIDsPast(new Timestamp(0), 0,
		// 100);
		// Util.log(""+l);
		AE ae = AEMap.get(aet);
		if (ae == null) throw new RuntimeException("MirrorClient aet not found: " + aet);
		MirrorClient rsc = new MirrorClient(aet, ae.getHostName(), ae.getPort());
		new Thread(rsc).start();
	}
	AdminClient adminClient;
	int sleepSeconds = 1; // 15;
	int minSleepSeconds = 1; // 15;
	int maxSleepSeconds = 10; // 30*60;
	String aet;
	String host;
	int port;
	Timestamp lastTS;
	String lastUIDForTS;

	MirrorClient(String aet, String host, int port)
	{
		this.aet = aet;
		this.host = host;
		this.port = port;
	}

	public void run()
	{
		Util.sleep(1000);
		lastTS = StudyView.get().getDateLastImage(aet);
		if (lastTS == null) lastTS = new Timestamp(0);
		lastUIDForTS = StudyView.get().getLastStudyUIDForAET(aet, lastTS);
		AdminClient ac = new AdminClient(host, port);
		for (;;)
		{
			StudiesDumpBean sdb = null;
			try
			{
				sdb = ac.selectStudiesUpdatedSince(lastTS, lastUIDForTS);
			}
			catch (Exception e)
			{
				if (e instanceof RuntimeException && ((RuntimeException) e).getCause() instanceof ConnectException)
					; // suppress logging
				else Util.log("selectStudiesUpdatedSince", e);
			}
			if (sdb == null || sdb.studies.size() == 0)
			{
				sleepSeconds = Math.min(sleepSeconds * 2, maxSleepSeconds);
			} else
			{
				try
				{
					merge(aet, sdb);
				}
				catch (Exception e)
				{
					Util.log("merge sdb", e);
				}
				sleepSeconds = minSleepSeconds;
			}
			Util.sleep(sleepSeconds * 1000);
		}
	}

	// public StudiesDumpBean selectStudiesPast(Timestamp dateLastImage, long
	// lastId, int limit) {
	// StudiesDumpBean sdb = new StudiesDumpBean();
	// List l = StudyView.get().selectLocalIDsPast(new Timestamp(0), 0, 100);
	//
	// for (Long id : (List<Long>)l) {
	// Study study = StudyView.get().selectByID(id);
	// if (study == null)
	// continue;
	// sdb.studies.add(study);
	// Patient patient = PatientView.get().select(study.getPatientID());
	// sdb.patientMap.put(id, patient);
	// List seriesList = SeriesView.get().selectByStudy(id);
	// sdb.seriesListMap.put(id, seriesList);
	// for (Series s : (List<Series>)seriesList) {
	// List imageList = ImageView.get().selectBySeries(s.getSeriesID());
	// sdb.imageListMap.put(s.getSeriesID(), imageList);
	// }
	// }
	// return sdb;
	// }
	// public StudiesDumpBean selectStudiesUpdatedSince(Timestamp dateLastImage,
	// long lastId) {
	void merge(String remoteAET, StudiesDumpBean sdb)
	{
		for (Study remoteStudy : (List<Study>) sdb.studies)
		{
			long remoteStudyId = remoteStudy.getStudyID();
			long patientId;
			long studyId;
			long seriesId;
			lastTS = remoteStudy.getDateLastImage();
			lastUIDForTS = remoteStudy.getStudyUID();
			log("merge " + aet + " study=" + remoteStudy);
			try
			{
				Patient remotePatient = (Patient) sdb.patientMap.get(remoteStudyId);
				Patient patient = PatientView.get().selectByExtID(remotePatient.getExtID());
				log("merge  " + aet + " patient=" + remotePatient);
				if (patient == null)
				{
					remotePatient.setPatientID(DicomStore.get().getNextID());
					boolean b = PatientView.get().insert(remotePatient);
					patientId = remotePatient.getPatientID();
				} else
				{
					patientId = patient.getPatientID();
				}
				Study study = StudyView.get().selectByUID(remoteStudy.getStudyUID());
				remoteStudy.setPatientID(patientId);
				remoteStudy.setOrigin(Study.ORIGIN_REMOTE);
				remoteStudy.setOriginAET(remoteAET);
				if (study == null)
				{
					remoteStudy.setStudyID(DicomStore.get().getNextID());
					boolean b = StudyView.get().insert(remoteStudy);
				} else
				{
					remoteStudy.setStudyID(study.getStudyID());
					boolean b = StudyView.get().update(remoteStudy);
				}
				studyId = remoteStudy.getStudyID();
				List seriesList = (List) sdb.seriesListMap.get(remoteStudyId);
				for (Series remoteSeries : (List<Series>) seriesList)
				{
					long remoteSeriesID = remoteSeries.getSeriesID();
					Util.log("merge  " + aet + " remoteSeries=" + remoteSeries);
					Series series = SeriesView.get().selectByUID(remoteSeries.getSeriesUID());
					remoteSeries.setStudyID(studyId);
					remoteSeries.setPatientID(patientId);
					if (series == null)
					{
						remoteSeries.setSeriesID(DicomStore.get().getNextID());
						boolean b = SeriesView.get().insert(remoteSeries);
					} else
					{
						remoteSeries.setSeriesID(series.getStudyID());
						boolean b = SeriesView.get().update(remoteSeries);
					}
					seriesId = remoteSeries.getSeriesID();
					List imageList = (List) sdb.imageListMap.get(remoteSeriesID);
					for (Image remoteImage : (List<Image>) imageList)
					{
						Util.log("merge  " + aet + " remoteImage=" + remoteImage);
						Image image = ImageView.get().selectByUID(remoteImage.getImageUID());
						if (image == null)
						{
							remoteImage.setImageID(DicomStore.get().getNextID());
							remoteImage.setPatientID(patientId);
							remoteImage.setStudyID(studyId);
							remoteImage.setSeriesID(seriesId);
							remoteImage.setStatus(Image.STATUS_REMOTE);
							boolean b = ImageView.get().insert(remoteImage);
						}
					}
				}
			}
			catch (Exception e)
			{
				Util.log("merge error  " + aet + " merging", e);
			}
		}
	}

	// need timeout to reload
	void loadRemoteStudy(Study study)
	{
		List seriesList = SeriesView.get().selectByStudy(study.getStudyID());
		for (Series s : (List<Series>) seriesList)
		{
			List imageList = ImageView.get().selectBySeries(s.getSeriesID());
			for (Image i : (List<Image>) imageList)
			{
				if (i.getStatus() == Image.STATUS_REMOTE)
				{
					File f = null;
					try
					{
						f = SSStore.get().createTempFile(".remote");
						boolean b = adminClient.getImageOnDisk(i.getImageUID(), f);
						if (b)
						{
							DicomStore.get().put(f);
						}
					}
					catch (Exception e)
					{
						Util.log("loadRemoteStudy", e);
					}
					finally
					{
						Util.safeDelete(f); // should be moved before delete if
											// successfull
					}
				}
			}
		}
	}

	synchronized public static boolean loadRemoteImage(String aeString, Image i)
	{
		if (i.getStatus() == Image.STATUS_REMOTE)
		{
			AE ae = AEMap.get(aeString);
			if (ae == null) throw new RuntimeException("loadRemoteImage aet not found: " + aeString);
			AdminClient ac = new AdminClient(ae.getHostName(), ae.getPort());
			File f = null;
			try
			{
				f = SSStore.get().createTempFile(".remote");
				boolean b = ac.getImageOnDisk(i.getImageUID(), f);
				if (b)
				{
					DicomStore.get().put(f);
				} else return false;
			}
			catch (Exception e)
			{
				Util.log("loadRemoteImage", e);
				return false;
			}
			finally
			{
				Util.safeDelete(f); // should be moved before delete if
									// successfull
			}
			return ImageView.get().setStatus(i.getImageID(), Image.STATUS_LOCAL);
		} else return true;
	}
}
