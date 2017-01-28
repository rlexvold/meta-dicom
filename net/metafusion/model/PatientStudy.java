package net.metafusion.model;

public class PatientStudy
{
	Patient patient;
	Study study;

	public Patient getPatient()
	{
		return patient;
	}

	public void setPatient(Patient patient)
	{
		this.patient = patient;
	}

	public Study getStudy()
	{
		return study;
	}

	public void setStudy(Study study)
	{
		this.study = study;
	}

	public String toString()
	{
		return "[" + patient.toString() + ":" + study.toString() + "]";
	}
}
