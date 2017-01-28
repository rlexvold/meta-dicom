package net.metafusion.model;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;
import net.metafusion.dataset.DSFileView;
import net.metafusion.util.DicomUtil;
import net.metafusion.util.ImageMetaInfo;
import net.metafusion.util.PN;

public class Patient implements Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;

	public Patient()
	{
		// acme.util.Stats.inc("new Patient");
	}

	public Patient(ImageMetaInfo imi, DSFileView v)
	{
		patientID = imi.getPatientID();
		dicomName = v.PatientName != null ? v.PatientName : "";
		PN pn = new PN(v.PatientName);
		firstName = pn.getFirst();
		middleName = pn.getMiddle();
		lastName = pn.getLast();
		sex = v.PatientSex;
		extID = v.PatientID;
		dob = DicomUtil.parseDate(v.PatientBirthDate);
		dateEntered = new Timestamp(System.currentTimeMillis());
		dateModified = new Timestamp(System.currentTimeMillis());
		// acme.util.Stats.inc("new Image");
	}

	public String toString()
	{
		return "patient:" + extID + ":" + dicomName;
	}
	public long patientID;
	public String dicomName;
	public String firstName;
	public String middleName;
	public String lastName;
	public String sex;
	public String extID;
	public Date dob;
	public Timestamp dateEntered;
	public Timestamp dateModified;

	public long getPatientID()
	{
		return patientID;
	}

	public void setPatientID(long patientID)
	{
		this.patientID = patientID;
	}

	public String getViewableDicomName()
	{
		return dicomName.replace('^', ' ');
	}

	public String getDicomName()
	{
		return dicomName;
	}

	public void setDicomName(String dicomName)
	{
		this.dicomName = dicomName;
	}

	public String getFirstName()
	{
		return firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getMiddleName()
	{
		return middleName;
	}

	public void setMiddleName(String middleName)
	{
		this.middleName = middleName;
	}

	public String getLastName()
	{
		return lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public String getSex()
	{
		return sex;
	}

	public void setSex(String sex)
	{
		this.sex = sex;
	}

	public String getExtID()
	{
		return extID;
	}

	public void setExtID(String extID)
	{
		this.extID = extID;
	}

	public Date getDob()
	{
		return dob;
	}

	public void setDob(Date dob)
	{
		this.dob = dob;
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
}
