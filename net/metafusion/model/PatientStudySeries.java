package net.metafusion.model;

public class PatientStudySeries
{
	Patient patient;
	Study study;
	Series series;

	@Override
	public String toString()
	{
		return "[" + patient.toString() + ":" + study.toString() + ":" + series.toString() + "]";
	}

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

	public Series getSeries()
	{
		return series;
	}

	public void setSeries(Series series)
	{
		this.series = series;
	}
}
