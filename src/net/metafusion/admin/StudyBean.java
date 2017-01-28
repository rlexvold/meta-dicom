package net.metafusion.admin;

import java.io.Serializable;
import acme.util.XML;

public class StudyBean implements Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;

	public StudyBean()
	{
	}

	public StudyBean(XML x)
	{
		patientID = x.get("patientID", "");
		name = x.get("name", "");
		accession = x.get("accession", "");
		modality = x.get("modality", "");
		description = x.get("description", "");
		date = x.get("date", "");
		time = x.get("time", "");
		studyID = x.get("studyID", "");
		sex = x.get("sex", "");
		birthdate = x.get("birthdate", "");
		referringMD = x.get("referringMD", "");
		radiologist = x.get("radiologist", "");
		stationName = x.get("stationName", "");
		studyUID = x.get("studyUID", "");
	}

	public XML toXML()
	{
		XML x = new XML("StudyBean");
		x.addAttr("patientID", patientID);
		x.addAttr("name", name);
		x.addAttr("accession", accession);
		x.addAttr("modality", modality);
		x.addAttr("description", description);
		x.addAttr("date", date);
		x.addAttr("time", time);
		x.addAttr("studyID", studyID);
		x.addAttr("sex", sex);
		x.addAttr("birthdate", birthdate);
		x.addAttr("referringMD", referringMD);
		x.addAttr("radiologist", radiologist);
		x.addAttr("stationName", stationName);
		x.addAttr("studyUID", studyUID);
		return x;
	}
	private boolean selected = false;
	private String patientID = "";
	private String name = "";
	private String accession = "";
	private String modality = "";
	private String description = "";
	private String date = "";
	private String time = "";
	private String studyID = "";
	private String sex = "";
	private String birthdate = "";
	private String referringMD = "";
	private String radiologist = "";
	private String stationName = "";
	private String studyUID = "";

	public String getPatientID()
	{
		return patientID;
	}

	public boolean isSelected()
	{
		return selected;
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}

	public void setPatientID(String patientID)
	{
		this.patientID = patientID;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getAccession()
	{
		return accession;
	}

	public void setAccession(String accession)
	{
		this.accession = accession;
	}

	public String getModality()
	{
		return modality;
	}

	public void setModality(String modality)
	{
		this.modality = modality;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getDate()
	{
		return date;
	}

	public void setDate(String date)
	{
		this.date = date;
	}

	public String getTime()
	{
		return time;
	}

	public void setTime(String time)
	{
		this.time = time;
	}

	public String getStudyID()
	{
		return studyID;
	}

	public void setStudyID(String studyID)
	{
		this.studyID = studyID;
	}

	public String getSex()
	{
		return sex;
	}

	public void setSex(String sex)
	{
		this.sex = sex;
	}

	public String getBirthdate()
	{
		return birthdate;
	}

	public void setBirthdate(String birthdate)
	{
		this.birthdate = birthdate;
	}

	public String getReferringMD()
	{
		return referringMD;
	}

	public void setReferringMD(String referringMD)
	{
		this.referringMD = referringMD;
	}

	public String getRadiologist()
	{
		return radiologist;
	}

	public void setRadiologist(String radiologist)
	{
		this.radiologist = radiologist;
	}

	public String getStationName()
	{
		return stationName;
	}

	public void setStationName(String stationName)
	{
		this.stationName = stationName;
	}

	public String getStudyUID()
	{
		return studyUID;
	}

	public void setStudyUID(String studyUID)
	{
		this.studyUID = studyUID;
	}

	public String toString()
	{
		return toXML().toString();
	}
}
