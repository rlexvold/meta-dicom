package net.metafusion.model;

public class PatientStudySeriesImage
{
	Patient patient;
	Study study;
	Series series;
	Image image;

	public String toString()
	{
		return "[" + patient.toString() + ":" + study.toString() + ":\n" + series.toString() + image.toString() + "]";
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

	public Image getImage()
	{
		return image;
	}

	public void setImage(Image image)
	{
		this.image = image;
	}
}
