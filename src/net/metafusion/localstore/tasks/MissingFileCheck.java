package net.metafusion.localstore.tasks;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import net.metafusion.localstore.DicomStore;
import net.metafusion.model.ImageFile;
import net.metafusion.model.ImageFileView;
import net.metafusion.model.Patient;
import net.metafusion.model.PatientStudy;
import net.metafusion.model.PatientStudyView;
import net.metafusion.model.Series;
import net.metafusion.model.SeriesView;
import net.metafusion.model.Study;
import net.metafusion.util.Counter;
import net.metafusion.util.FileChecker;
import acme.storage.SSStore;
import acme.util.Log;

public class MissingFileCheck extends Task
{
	@Override
	public void run()
	{
		TreeSet<Long> missingImages = new TreeSet<Long>();
		TreeSet<Long> missingMdf = new TreeSet<Long>();
		TreeSet<Long> studySet = new TreeSet<Long>();
		HashMap studyMap = new HashMap();
		HashMap seriesMap = new HashMap();
		initLogs("MissingFileCheck");
		DicomStore ds = DicomStore.get();
		SSStore ss = ds.getSSStore();
		List<ImageFile> li = ImageFileView.get().selectAll();
		log("MissingFileCheck start: " + li.size() + " images in DB");
		Iterator<ImageFile> i = li.iterator();
		while (i.hasNext())
		{
			ImageFile image = i.next();
			Counter studyCount = (Counter) studyMap.get(image.getStudyID());
			Counter seriesCount = (Counter) seriesMap.get(image.getSeriesID());
			if (studyCount == null)
			{
				studyCount = new Counter(image.getStudyID());
			}
			if (seriesCount == null)
			{
				seriesCount = new Counter(image.getSeriesID());
			}
			studyCount.count1++;
			seriesCount.count1++;
			FileChecker fc = new FileChecker(image);
			if (!fc.imageExists())
			{
				studySet.add(image.getStudyID());
				studyCount.count2++;
				seriesCount.count2++;
				missingImages.add(image.getImageID());
			}
			if (!fc.mdfExists())
			{
				studySet.add(image.getStudyID());
				studyCount.count3++;
				seriesCount.count3++;
				missingMdf.add(image.getImageID());
			}
			studyMap.put(image.getStudyID(), studyCount);
			seriesMap.put(image.getSeriesID(), seriesCount);
		}
		log("Total missing: Images - " + missingImages.size() + "  MDFs - " + missingMdf.size(), false);
		log("********* Study list **********", false);
		Iterator<Long> si = studySet.iterator();
		while (si.hasNext())
		{
			Counter c = (Counter) studyMap.get(si.next());
			if (c != null)
			{
				log(c.id.toString() + ": " + c.count2 + " missing jpg, " + c.count3 + " missing mdf, " + c.count1 + " total files", false);
				if (c.count3 > 0)
				{
					List tmp = PatientStudyView.get().selectWhere("dcm_study.studyid = " + c.id);
					PatientStudy pss = (PatientStudy) tmp.get(0);
					Study study = pss.getStudy();
					Patient patient = pss.getPatient();
					List seriesList = SeriesView.get().selectByStudy(c.id);
					String msg = "First name: " + patient.getFirstName() + ", Last Name: " + patient.getLastName() + ", Dicom Name: " + patient.getDicomName() + ", Study Date: "
							+ study.date + ", Modality: " + study.modalities + ", Study UID: " + study.studyUID + ", Accession: " + study.accessionNumber;
					vlog(msg, false);
					Iterator<Series> is = seriesList.iterator();
					while (is.hasNext())
					{
						Series series = is.next();
						Counter sc = (Counter) seriesMap.get(series.seriesID);
						if (sc.count3 > 0)
						{
							msg = "Series UID: " + series.seriesUID + ", Modality: " + series.modality + ", DB count: " + sc.count1 + ", Missing MDF: " + sc.count3;
							vlog(msg, false);
						}
					}
				}
			}
		}
		log("********* Missing Image list **********", false);
		Iterator<Long> ii = missingImages.iterator();
		while (ii.hasNext())
		{
			log(ii.next().toString(), false);
		}
		log("********* Missing MDF list **********", false);
		Iterator<Long> mi = missingMdf.iterator();
		while (mi.hasNext())
		{
			log(mi.next().toString(), false);
		}
	}

	private boolean checkForEntry(TreeSet<Long> tree, Long id)
	{
		Iterator<Long> i = tree.iterator();
		while (i.hasNext())
		{
			if (i.next() == id)
				return true;
		}
		return false;
	}

	public static void main(String[] args)
	{
		Log.setVerbose(0);
		version = "2.0";
		start(args, MissingFileCheck.class);
	}
}
