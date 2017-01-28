package net.metafusion.localstore;

import integration.SearchBean;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import net.metafusion.Dicom;
import net.metafusion.admin.StudyBean;
import net.metafusion.dataset.DS;
import net.metafusion.model.Image;
import net.metafusion.model.Patient;
import net.metafusion.model.PatientStudy;
import net.metafusion.model.PatientStudySeries;
import net.metafusion.model.PatientStudySeriesImage;
import net.metafusion.model.PatientStudySeriesImageView;
import net.metafusion.model.PatientStudySeriesView;
import net.metafusion.model.PatientStudyView;
import net.metafusion.model.PatientView;
import net.metafusion.model.Series;
import net.metafusion.model.Study;
import net.metafusion.util.DicomUtil;
import net.metafusion.util.GlobalProperties;
import net.metafusion.util.Tag;
import net.metafusion.util.UID;
import acme.db.CustomSQL;
import acme.util.Log;
import acme.util.StringUtil;

public class DicomQuery implements Iterator
{
	final static int	QUERY_LIMIT	= 2500;

	static void log(String s)
	{
		Log.log(s);
	}

	static void vlog(String s)
	{
		Log.vlog(s);
	}
	// DicomStore store;
	UID						root;
	String					level;
	int						levelIndex;
	DS						tags;
	List					results;
	Iterator				iter;
	static SimpleDateFormat	searchBeanFormat	= new SimpleDateFormat("MM/dd/yyyy");

	public DicomQuery(SearchBean sb)
	{
		vlog("DicomQuery: " + sb.toString());
		CustomSQL where = new CustomSQL();
		String s = "";
		boolean searchSeries = false;
		// where.addOrderBy("dcm_study.studyID");
		where.addGroupBy("dcm_study.studyID");
		s = sb.getPatientID();
		if (s != null && s.length() != 0)
		{
			where.addEquals("dcm_patient.extID", StringUtil.split(s, '\\'));
		}
		String ss[] = new String[2];
		if (sb.getLastName().length() != 0 || sb.getFirstName().length() != 0)
		{
			int count = 0;
			if (sb.getLastName().length() != 0)
				ss[count++] = sb.getLastName();
			if (sb.getFirstName().length() != 0)
				ss[count++] = sb.getFirstName();
			if (count == 1)
				where.addLike("dcm_patient.dicomname", ss[0]);
			else
				where.addLike("dcm_patient.dicomname", ss, CustomSQL.AND);
		}
		Date fromDate = null;
		Date toDate = null;
		if (sb.getFromDate().length() > 0)
			fromDate = DicomUtil.parseDate(sb.getFromDate());// new
		// Date(searchBeanFormat.parse(sb.getFromDate()).getTime());
		if (sb.getToDate().length() > 0)
			toDate = DicomUtil.parseDate(sb.getToDate());// new
		// Date(searchBeanFormat.parse(sb.getToDate()).getTime());
		if (fromDate != null && toDate != null)
			where.addRange("dcm_study.date", fromDate, toDate);
		else if (fromDate != null)
			where.addGreaterEquals("dcm_study.date", fromDate);
		else if (toDate != null)
			where.addLessEquals("dcm_study.date", toDate);
		s = sb.getAccessionNum();
		if (s.length() != 0)
		{
			where.addLike("dcm_study.accessionNumber", s);
		}
		s = sb.getReferringPhysician();
		if (s.length() != 0)
		{
			where.addLike("dcm_study.referringPhysicianName", s);
		}
		s = sb.getModality();
		if (s.length() != 0)
		{
			vlog("synthetic Tag.ModalitiesInStudy");
			where.addLike("dcm_study.modalities", s);
		}
		s = sb.getStudyDescription();
		if (s.length() != 0)
		{
			where.addLike("dcm_study.description", s);
		}
		s = sb.getStationName();
		if (s.length() != 0)
		{
			where.addLike("dcm_series.stationName", s);
			searchSeries = true;
		}
		s = sb.getRadiologist();
		if (s.length() != 0)
		{
			where.addLike("dcm_series.nameOfPhysicianReadingStudy", s);
			searchSeries = true;
		}
		s = sb.getReader();
		if (s.length() != 0)
		{
			where.addEquals("dcm_study.reader", s);
		}
		s = sb.getState();
		if (s.length() != 0)
		{
			where.addEquals("dcm_study.state", s);
		}
		s = sb.getModalityList();
		if (s.length() != 0)
		{
			where.addFieldInString("dcm_study.modalities", s);
		}
		where.addLimit(QUERY_LIMIT);
		if (searchSeries)
			results = PatientStudySeriesView.get().selectWhere(where.get());
		else
			results = PatientStudyView.get().selectWhere(where.get());
		iter = results.iterator();
	}

	DicomQuery(UID root, String level, DS tags)
	{
		vlog("DicomQuery: " + level + ":" + root.toString());
		if (Log.vvv())
			Log.vvvlog("tags: " + tags);
		// store = DicomStore.get();
		this.root = root;
		this.level = level;
		this.tags = tags;
		levelIndex = DicomUtil.getPatientLevelIndex(level);
		CustomSQL where = new CustomSQL();
		String s = "";
		if (levelIndex >= Dicom.PATIENT_LEVEL_INDEX)
		{
			s = tags.getString(Tag.PatientName);
			if (s != null)
			{
				String ss[] = StringUtil.split(s, ',');
				if (ss.length == 1)
					where.addLike("dcm_patient.dicomname", ss[0]);
				else
					where.addLike("dcm_patient.dicomname", ss, CustomSQL.AND);
			}
			s = tags.getString(Tag.PatientID);
			if (s != null)
				where.addEquals("dcm_patient.extID", StringUtil.split(s, '\\'));
			if (levelIndex == Dicom.PATIENT_LEVEL_INDEX)
			{
				where.addLimit(QUERY_LIMIT);
				results = PatientView.get().selectWhere(where.get());
				iter = results.iterator();
			}
		}
		if (levelIndex >= Dicom.STUDY_LEVEL_INDEX)
		{
			s = tags.getString(Tag.StudyDate);
			if (tags.containsValue(Tag.StudyDate))
			{
				Date d[] = DicomUtil.parseDateRange(s);
				if (d.length == 1)
					where.addEquals("dcm_study.date", d[0]);
				else
					where.addRange("dcm_study.date", d[0], d[1]);
			}
			s = tags.getString(Tag.AccessionNumber);
			if (s != null)
			{
				where.addLike("dcm_study.accessionNumber", StringUtil.split(s, '\\'));
			}
			s = tags.getString(Tag.StudyInstanceUID);
			if (s != null)
			{
				where.addEquals("dcm_study.studyUID", StringUtil.split(s, '\\'));
			}
			s = tags.getString(Tag.StudyID);
			if (s != null)
			{
				where.addEquals("dcm_study.studyIDString", StringUtil.split(s, '\\'));
			}
			s = tags.getString(Tag.ReferringPhysicianName);
			if (s != null)
			{
				where.addLike("dcm_study.referringPhysicianName", s);
			}
			s = tags.getString(Tag.ModalitiesInStudy);
			if (s != null)
			{
				vlog("synthetic Tag.ModalitiesInStudy");
				where.addLike("dcm_study.modalities", s);
			}
			s = tags.getString(Tag.StudyDescription);
			if (s != null)
			{
				where.addLike("dcm_study.description", s);
			}
			if (levelIndex == Dicom.STUDY_LEVEL_INDEX)
			{
				where.addLimit(QUERY_LIMIT);
				results = PatientStudyView.get().selectWhere(where.get());
				iter = results.iterator();
			}
		}
		if (levelIndex >= Dicom.SERIES_LEVEL_INDEX)
		{
			s = tags.getString(Tag.Modality);
			if (s != null)
			{
				where.addEquals("dcm_series.modality", StringUtil.split(s, '\\'));
			}
			s = tags.getString(Tag.SeriesInstanceUID);
			if (s != null)
			{
				where.addEquals("dcm_series.seriesUID", StringUtil.split(s, '\\'));
			}
			s = tags.getString(Tag.SeriesDescription);
			if (s != null)
			{
				where.addLike("dcm_series.description", s);
			}
			s = tags.getString(Tag.SeriesNumber);
			if (s != null)
			{
				where.addEquals("dcm_series.seriesNumber", StringUtil.split(s, '\\'));
			}
			s = tags.getString(Tag.BodyPartExamined);
			if (s != null)
			{
				where.addLike("dcm_series.bodyPart", s);
			}
			s = tags.getString(Tag.OperatorName);
			if (s != null)
			{
				where.addLike("dcm_series.operatorName", s);
			}
			s = tags.getString(Tag.SeriesDate);
			if (s != null)
			{
				Date d[] = DicomUtil.parseDateRange(s);
				if (d.length == 1)
					where.addEquals("dcm_series.date", d[0]);
				else
					where.addRange("dcm_series.date", d[0], d[1]);
			}
			if (levelIndex == Dicom.SERIES_LEVEL_INDEX)
			{
				where.addLimit(QUERY_LIMIT);
				results = PatientStudySeriesView.get().selectWhere(where.get());
				iter = results.iterator();
			}
		}
		if (levelIndex >= Dicom.IMAGE_LEVEL_INDEX)
		{
			s = tags.getString(Tag.ImageType);
			if (s != null)
			{
				where.addEquals("dcm_image.imageType", StringUtil.split(s, '\\'));
			}
			s = tags.getString(Tag.SOPInstanceUID);
			if (s != null)
			{
				where.addEquals("dcm_image.imageUID", StringUtil.split(s, '\\'));
			}
			s = tags.getString(Tag.SOPClassUID);
			if (s != null)
			{
				where.addEquals("dcm_image.classUID", StringUtil.split(s, '\\'));
			}
			s = tags.getString(Tag.InstanceNumber);
			if (s != null)
			{
				where.addEquals("dcm_image.instanceNumber", StringUtil.split(s, '\\'));
			}
			if (levelIndex == Dicom.IMAGE_LEVEL_INDEX)
			{
				// RAL - added order by instanceNumber to ensure the order of
				// the images is consistent with instance order.
				String queryType = GlobalProperties.get().getProperty("cmoveQueryType");
				if (queryType != null && queryType.length() > 0)
				{
					if (queryType.compareToIgnoreCase("imageuid") == 0)
					{
						where.addOrderBy("dcm_image.imageuid asc");
					}
					if (queryType.compareToIgnoreCase("instancenumber") == 0)
					{
						where.addOrderBy("dcm_image.seriesid asc,dcm_image.instanceNumber+0 asc");
					}
					if (queryType.compareToIgnoreCase("imageid") == 0)
					{
						where.addOrderBy("dcm_image.seriesid asc,dcm_image.imageID asc");
					}
				}
				where.addLimit(QUERY_LIMIT);
				log("C-MOVE Query: " + where.get());
				results = PatientStudySeriesImageView.get().selectWhere(where.get());
				/*
				 * RAL - added order by instanceNumber to ensure the order of
				 * the images is consistent with instance order. Had to
				 * implement it as a Java sort because instanceNumber is a
				 * string value in the database, so a query doesn't sort
				 * properly found out how to implicitly cast a string as an
				 * integer, so the order by above works
				 * Collections.sort(results, new
				 * PatientStudySeriesImageSorter());
				 */
				iter = results.iterator();
			}
		}
	}

	/*
	 * 
	 * pr, patient select from dcm_patient where pr, study select from
	 * dcm_patient p, dcm_study s where patientID="KEY" and
	 * dcm_patient.patientId = dcm_study.patientID and .... pr, series select
	 * from dcm_patient p, dcm_study s, dcm_series z where patientID="KEY" and
	 * p.patientId = s.patientID and s.studyInstanceUID = "KEY2" and s.studyID =
	 * z.studyID and ... pr, image select from dcm_patient p, dcm_study s,
	 * dcm_series z, dcn_image i where patientID="KEY" and p.patientId =
	 * s.patientID and s.studyInstanceUID = "KEY2" and s.studyID = z.studyID and
	 * z.seriesUID = "KEY3" and z.seriesID = i.seriesID and ...
	 * 
	 * 
	 * 
	 * sr, study
	 * 
	 * sr,series
	 * 
	 * sr, image
	 * 
	 * ....
	 */
	void load1(DS tags, DS d, Tag t, Object value)
	{
		if (tags.contains(t))
			d.put(t, value);
	}

	void load(Patient p, DS tags, DS d)
	{
		if (p == null)
			return;
		load1(tags, d, Tag.PatientName, p.dicomName);
		load1(tags, d, Tag.PatientSex, p.sex);
		load1(tags, d, Tag.PatientID, p.extID);
		load1(tags, d, Tag.PatientBirthDate, DicomUtil.formatDate(p.dob));
	}

	void load(Study study, DS tags, DS d)
	{
		if (study == null)
			return;
		load1(tags, d, Tag.StudyInstanceUID, study.studyUID);
		load1(tags, d, Tag.StudyDate, DicomUtil.formatDate(study.date));
		load1(tags, d, Tag.StudyTime, DicomUtil.formatTime(study.date));
		load1(tags, d, Tag.StudyID, study.studyIDString);
		load1(tags, d, Tag.AccessionNumber, study.accessionNumber);
		load1(tags, d, Tag.ModalitiesInStudy, study.modalities);
		load1(tags, d, Tag.StudyDescription, study.description);
		load1(tags, d, Tag.ReferringPhysicianName, study.referringPhysicianName);
	}

	void load(Series series, DS tags, DS d)
	{
		if (series == null)
			return;
		load1(tags, d, Tag.Modality, series.modality);
		load1(tags, d, Tag.SeriesInstanceUID, series.seriesUID);
		load1(tags, d, Tag.SeriesDate, DicomUtil.formatDate(series.date));
		load1(tags, d, Tag.SeriesNumber, series.seriesNumber);
		load1(tags, d, Tag.PerformingPhysicianName, series.physicianName);
		load1(tags, d, Tag.OperatorName, series.operatorName);
		load1(tags, d, Tag.BodyPartExamined, series.bodyPart);
		load1(tags, d, Tag.StudyDescription, series.description);
	}

	void load(Image image, DS tags, DS d)
	{
		if (image == null)
			return;
		load1(tags, d, Tag.SOPInstanceUID, image.imageUID);
		load1(tags, d, Tag.SOPClassUID, image.classUID);
		load1(tags, d, Tag.InstanceNumber, image.instanceNumber);
		load1(tags, d, Tag.ImageType, image.imageType);
	}

	DS fill(Object o, DS ds)
	{
		DS fds = new DS();
		Patient p = null;
		Study study = null;
		Series series = null;
		Image image = null;
		if (o instanceof Patient)
		{
			p = (Patient) o;
		}
		else if (o instanceof PatientStudy)
		{
			p = ((PatientStudy) o).getPatient();
			study = ((PatientStudy) o).getStudy();
		}
		else if (o instanceof PatientStudySeries)
		{
			p = ((PatientStudySeries) o).getPatient();
			study = ((PatientStudySeries) o).getStudy();
			series = ((PatientStudySeries) o).getSeries();
		}
		else if (o instanceof PatientStudySeriesImage)
		{
			p = ((PatientStudySeriesImage) o).getPatient();
			study = ((PatientStudySeriesImage) o).getStudy();
			series = ((PatientStudySeriesImage) o).getSeries();
			image = ((PatientStudySeriesImage) o).getImage();
		}
		else
			throw new RuntimeException("bad join type");
		load(p, tags, fds);
		load(study, tags, fds);
		load(series, tags, fds);
		load(image, tags, fds);
		// cleanup: verify with multiple
		if (tags.contains(Tag.NumberOfPatientRelatedStudies))
			fds.put(Tag.NumberOfPatientRelatedStudies, "1");
		if (tags.contains(Tag.NumberOfPatientRelatedSeries))
			fds.put(Tag.NumberOfPatientRelatedSeries, "1");
		if (tags.contains(Tag.NumberOfPatientRelatedInstances))
			fds.put(Tag.NumberOfPatientRelatedInstances, "1");
		if (tags.contains(Tag.NumberOfStudyRelatedSeries))
			fds.put(Tag.NumberOfStudyRelatedSeries, "1");
		if (tags.contains(Tag.NumberOfStudyRelatedInstances))
			fds.put(Tag.NumberOfStudyRelatedInstances, "1");
		if (tags.contains(Tag.NumberOfSeriesRelatedInstances))
			fds.put(Tag.NumberOfSeriesRelatedInstances, "1");
		Iterator iter = tags.getTags().iterator();
		while (iter.hasNext())
		{
			Tag t = (Tag) iter.next();
			if (!fds.contains(t))
			{
				fds.put(t, tags.get(t));
			}
		}
		return fds;
	}

	StudyBean fillStudyBean(Object o)
	{
		StudyBean sb = new StudyBean();
		Patient p = null;
		Study study = null;
		Series series = null;
		Image image = null;
		if (o instanceof Patient)
		{
			p = (Patient) o;
		}
		else if (o instanceof PatientStudy)
		{
			p = ((PatientStudy) o).getPatient();
			study = ((PatientStudy) o).getStudy();
		}
		else if (o instanceof PatientStudySeries)
		{
			p = ((PatientStudySeries) o).getPatient();
			study = ((PatientStudySeries) o).getStudy();
			series = ((PatientStudySeries) o).getSeries();
		}
		else if (o instanceof PatientStudySeriesImage)
		{
			p = ((PatientStudySeriesImage) o).getPatient();
			study = ((PatientStudySeriesImage) o).getStudy();
			series = ((PatientStudySeriesImage) o).getSeries();
			image = ((PatientStudySeriesImage) o).getImage();
		}
		else
			throw new RuntimeException("bad join type");
		sb.setPatientID(p.getExtID());
		sb.setName(p.getDicomName());
		sb.setAccession(study.getAccessionNumber());
		sb.setModality(study.getModalities());
		sb.setDescription(study.getDescription());
		sb.setDate(DicomUtil.formatDate(study.getDate()));
		sb.setTime(DicomUtil.formatTime(study.getDate()));
		sb.setSex(p.getSex());
		sb.setBirthdate(DicomUtil.formatDate(p.getDob()));
		sb.setRadiologist(study.getNameOfPhysicianReadingStudy());
		sb.setStationName(series != null ? series.getStationName() : "");
		sb.setStudyUID(study.getStudyUID());
		sb.setStudyID("" + study.getStudyID());
		return sb;
	}

	public boolean hasNext()
	{
		return iter.hasNext();
	}

	public DS next()
	{
		Object o = iter.next();
		DS ds = o != null ? fill(o, tags) : null;
		if (Log.vvv())
			Log.vvvlog("iter->: " + ds);
		return ds;
	}

	public StudyBean nextStudyBean()
	{
		Object o = iter.next();
		StudyBean studyBean = o != null ? fillStudyBean(o) : null;
		return studyBean;
	}

	public void remove()
	{
		assert false;
	}
}
