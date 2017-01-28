package net.metafusion.model;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import net.metafusion.dataset.DSFileView;
import net.metafusion.util.DicomUtil;
import net.metafusion.util.ImageMetaInfo;

public class Series implements Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;

	public Series()
	{
		// acme.util.Stats.inc("new Series");
	}

	public Series(ImageMetaInfo imi, DSFileView v)
	{
		seriesID = imi.getSeriesID();
		studyID = imi.getStudyID();
		patientID = imi.getPatientID();
		modality = v.Modality;
		seriesUID = v.SeriesInstanceUID;
		seriesNumber = v.SeriesNumber;
		date = DicomUtil.parseDate(v.StudyDate);
		physicianName = v.PerformingPhysicianName;
		operatorName = v.OperatorName;
		description = v.SeriesDescription;
		bodyPart = v.BodyPartExamined;
		stationName = v.StationName;
		nameOfPhysicianReadingStudy = v.NameOfPhysicianReadingStudy;
		dateEntered = new Timestamp(System.currentTimeMillis());
		dateModified = new Timestamp(System.currentTimeMillis());
		count = 0;
		institutionName = v.InstitutionName;
		// acme.util.Stats.inc("new Image");
	}

	@Override
	public String toString()
	{
		return "series:" + modality + ":" + seriesUID;
	}
	public long seriesID;
	public long studyID;
	public long patientID;
	public String modality;
	public String seriesUID;
	public String seriesNumber;
	public Date date;
	public String physicianName;
	public String operatorName;
	public String description;
	public String bodyPart;
	public String stationName;
	public String nameOfPhysicianReadingStudy;
	public Timestamp dateEntered;
	public Timestamp dateModified;
	public String institutionName;
	public int count;

	public long getSeriesID()
	{
		return seriesID;
	}

	public void setSeriesID(long seriesID)
	{
		this.seriesID = seriesID;
	}

	public long getStudyID()
	{
		return studyID;
	}

	public void setStudyID(long studyID)
	{
		this.studyID = studyID;
	}

	public long getPatientID()
	{
		return patientID;
	}

	public void setPatientID(long patientID)
	{
		this.patientID = patientID;
	}

	public String getModality()
	{
		return modality;
	}

	public void setModality(String modality)
	{
		this.modality = modality;
	}

	public String getSeriesUID()
	{
		return seriesUID;
	}

	public void setSeriesUID(String seriesUID)
	{
		this.seriesUID = seriesUID;
	}

	public String getSeriesNumber()
	{
		return seriesNumber;
	}

	public void setSeriesNumber(String seriesNumber)
	{
		this.seriesNumber = seriesNumber;
	}

	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	public String getPhysicianName()
	{
		return physicianName;
	}

	public void setPhysicianName(String physicianName)
	{
		this.physicianName = physicianName;
	}

	public String getOperatorName()
	{
		return operatorName;
	}

	public void setOperatorName(String operatorName)
	{
		this.operatorName = operatorName;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getBodyPart()
	{
		return bodyPart;
	}

	public void setBodyPart(String bodyPart)
	{
		this.bodyPart = bodyPart;
	}

	public Timestamp getDateEntered()
	{
		return dateEntered;
	}

	public void setDateEntered(Timestamp dateEntered)
	{
		this.dateEntered = dateEntered;
	}

	public Timestamp getDateModified()
	{
		return dateModified;
	}

	public void setDateModified(Timestamp dateModified)
	{
		this.dateModified = dateModified;
	}

	public String getStationName()
	{
		return stationName;
	}

	public void setStationName(String stationName)
	{
		this.stationName = stationName;
	}

	public String getNameOfPhysicianReadingStudy()
	{
		return nameOfPhysicianReadingStudy;
	}

	public void setNameOfPhysicianReadingStudy(String nameOfPhysicianReadingStudy)
	{
		this.nameOfPhysicianReadingStudy = nameOfPhysicianReadingStudy;
	}

	public String getInstitutionName()
	{
		return institutionName;
	}

	public void setInstitutionName(String institution)
	{
		this.institutionName = institution;
	}

	public int getCount()
	{
		return count;
	}

	public void setCount(int count)
	{
		this.count = count;
	}
}
