package net.metafusion.model;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;

import net.metafusion.dataset.DSFileView;
import net.metafusion.util.DicomUtil;
import net.metafusion.util.ImageMetaInfo;

public class Study implements Serializable
{
	public static final char	ORIGIN_LOCAL		= 'O';
	public static final char	ORIGIN_REMOTE		= 'R';
	static final long			serialVersionUID	= 1L;
	public String				accessionNumber;
	public Date					date;
	public Timestamp			dateEntered;
	public Timestamp			dateLastImage;
	public Timestamp			dateModified;
	public String				description;
	public String				institutionName;
	public String				modalities;
	public String				nameOfPhysicianReadingStudy;
	public char					origin;
	public String				originAET;
	public long					patientID;
	public String				reader;
	public String				referringPhysicianName;
	protected int				serialVersion		= 1;
	public String				state;
	public String				stationName;
	public long					studyID;
	public String				studyIDString;
	public String				studyUID;
	public Time					time;
	public int					version;

	public Study()
	{
		// acme.util.Stats.inc("new Study");
	}

	public Study(ImageMetaInfo imi, DSFileView v, char origin, String originAET)
	{
		studyID = imi.getStudyID();
		patientID = imi.getPatientID();
		studyUID = v.StudyInstanceUID;
		date = DicomUtil.parseDate(v.StudyDate);
		time = DicomUtil.parseTime(v.StudyTime);
		studyIDString = v.StudyID;
		accessionNumber = v.AccessionNumber;
		description = v.StudyDescription;
		referringPhysicianName = v.ReferringPhysicianName;
		nameOfPhysicianReadingStudy = v.NameOfPhysicianReadingStudy;
		dateEntered = new Timestamp(System.currentTimeMillis());
		dateModified = new Timestamp(System.currentTimeMillis());
		stationName = v.StationName;
		institutionName = v.InstitutionName;
		state = "A";
		reader = "";
		version = 0;
		// acme.util.Stats.inc("new Study");
		this.origin = origin;
		this.originAET = originAET;
		Date tmpDate = DicomUtil.parseDate(v.AcquisitionDate);
		Time tmpTime = DicomUtil.parseTime(v.AcquisitionTime);
		dateLastImage = new Timestamp(tmpDate.getTime() + tmpTime.getTime());
	}

	public String getAccessionNumber()
	{
		return accessionNumber;
	}

	public Date getDate()
	{
		return date;
	}

	public Timestamp getDateEntered()
	{
		return dateEntered;
	}

	public Timestamp getDateLastImage()
	{
		return dateLastImage;
	}

	public Timestamp getDateModified()
	{
		return dateModified;
	}

	public String getDescription()
	{
		return description;
	}

	public String getInstitutionName()
	{
		return institutionName;
	}

	public String getModalities()
	{
		return modalities;
	}

	public String getNameOfPhysicianReadingStudy()
	{
		return nameOfPhysicianReadingStudy;
	}

	public char getOrigin()
	{
		return origin;
	}

	public String getOriginAET()
	{
		return originAET;
	}

	public long getPatientID()
	{
		return patientID;
	}

	public String getReader()
	{
		return reader;
	}

	public String getReferringPhysicianName()
	{
		return referringPhysicianName;
	}

	public String getState()
	{
		return state;
	}

	public String getStationName()
	{
		return stationName;
	}

	public long getStudyID()
	{
		return studyID;
	}

	public String getStudyIDString()
	{
		return studyIDString;
	}

	public String getStudyUID()
	{
		return studyUID;
	}

	public Time getTime()
	{
		return time;
	}

	public int getVersion()
	{
		return version;
	}

	public void setAccessionNumber(String accessionNumber)
	{
		this.accessionNumber = accessionNumber;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	public void setDateEntered(Timestamp dateEntered)
	{
		this.dateEntered = dateEntered;
	}

	public void setDateLastImage(Timestamp dateLastImage)
	{
		this.dateLastImage = dateLastImage;
	}

	public void setDateModified(Timestamp dateModified)
	{
		this.dateModified = dateModified;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public void setInstitutionName(String institutionName)
	{
		this.institutionName = institutionName;
	}

	public void setModalities(String modalities)
	{
		this.modalities = modalities;
	}

	public void setNameOfPhysicianReadingStudy(String nameOfPhysicianReadingStudy)
	{
		this.nameOfPhysicianReadingStudy = nameOfPhysicianReadingStudy;
	}

	public void setOrigin(char origin)
	{
		this.origin = origin;
	}

	public void setOriginAET(String originAET)
	{
		this.originAET = originAET;
	}

	public void setPatientID(long patientID)
	{
		this.patientID = patientID;
	}

	public void setReader(String reader)
	{
		this.reader = reader;
	}

	public void setReferringPhysicianName(String referringPhysicianName)
	{
		this.referringPhysicianName = referringPhysicianName;
	}

	public void setState(String state)
	{
		this.state = state;
	}

	public void setStationName(String stationName)
	{
		this.stationName = stationName;
	}

	public void setStudyID(long studyID)
	{
		this.studyID = studyID;
	}

	public void setStudyIDString(String studyIDString)
	{
		this.studyIDString = studyIDString;
	}

	public void setStudyUID(String studyUID)
	{
		this.studyUID = studyUID;
	}

	public void setTime(Time time)
	{
		this.time = time;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	@Override
	public String toString()
	{
		return "study:" + accessionNumber + ":" + studyUID;
	}
}
