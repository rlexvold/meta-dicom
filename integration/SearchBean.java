package integration;

//import acme.util.XML;
import java.io.Serializable;

public class SearchBean implements Serializable
{
	static final long serialVersionUID = 1L;
	static final int MAX_TO_RETURN = 1000;
	protected int serialVersion = 1;
	private String patientID = "";
	private String lastName = "";
	private String firstName = "";
	private String modality = "";
	private String fromDate = "";
	private String toDate = "";
	private String studyDescription = "";
	private String stationName = "";
	private String referringPhysician = "";
	private String radiologist = "";
	private String accessionNum = "";
	private String state = "";
	private String modalityList = "";
	private String reader = "";

	public SearchBean()
	{
	}

	/*
	 * public SearchBean(XML x) { patientID= x.get("patientID",""); lastName=
	 * x.get("lastName",""); firstName= x.get("firstName",""); modality=
	 * x.get("modality",""); fromDate= x.get("fromDate",""); toDate=
	 * x.get("toDate",""); studyDescription= x.get("studyDescription","");
	 * stationName= x.get("stationName",""); referringPhysician=
	 * x.get("referringPhysician",""); radiologist= x.get("radiologist","");
	 * accessionNum= x.get("accessionNum",""); }
	 * 
	 * public XML toXML() { XML x = new XML("SearchBean");
	 * x.addAttr("patientID",patientID); x.addAttr("lastName",lastName);
	 * x.addAttr("firstName",firstName); x.addAttr("modality",modality);
	 * x.addAttr("fromDate",fromDate); x.addAttr("toDate",toDate);
	 * x.addAttr("studyDescription",studyDescription);
	 * x.addAttr("stationName",stationName);
	 * x.addAttr("referringPhysician",referringPhysician);
	 * x.addAttr("radiologist",radiologist);
	 * x.addAttr("accessionNum",accessionNum); return x; }
	 */
	public String getPatientID()
	{
		return patientID;
	}

	public void setPatientID(String patientID)
	{
		this.patientID = patientID;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getModality()
	{
		return modality;
	}

	public void setModality(String modality)
	{
		this.modality = modality;
	}

	public String getFromDate()
	{
		return fromDate;
	}

	public void setFromDate(String fromDate)
	{
		this.fromDate = fromDate;
	}

	public String getToDate()
	{
		return toDate;
	}

	public void setToDate(String toDate)
	{
		this.toDate = toDate;
	}

	public String getStudyDescription()
	{
		return studyDescription;
	}

	public void setStudyDescription(String studyDescription)
	{
		this.studyDescription = studyDescription;
	}

	public String getStationName()
	{
		return stationName;
	}

	public void setStationName(String stationName)
	{
		this.stationName = stationName;
	}

	public String getReferringPhysician()
	{
		return referringPhysician;
	}

	public void setReferringPhysician(String referringPhysician)
	{
		this.referringPhysician = referringPhysician;
	}

	public String getRadiologist()
	{
		return radiologist;
	}

	public void setRadiologist(String radiologist)
	{
		this.radiologist = radiologist;
	}

	public String getAccessionNum()
	{
		return accessionNum;
	}

	public void setAccessionNum(String accessionNum)
	{
		this.accessionNum = accessionNum;
	}

	public String getState()
	{
		return state;
	}

	public void setState(String state)
	{
		this.state = state;
	}

	public String getModalityList()
	{
		return modalityList;
	}

	public void setModalityList(String modalityList)
	{
		this.modalityList = modalityList;
	}

	public String getReader()
	{
		return reader;
	}

	public void setReader(String reader)
	{
		this.reader = reader;
	}
}
