package net.metafusion.model;

import java.io.Serializable;
import java.sql.Timestamp;
import net.metafusion.dataset.DSFileView;
import net.metafusion.util.ImageMetaInfo;

public class Image implements Serializable
{
	static final long serialVersionUID = 1L;
	static public final char STATUS_LOCAL = 'L';
	static public final char STATUS_REMOTE = 'R';
	public String classUID;
	public Timestamp dateEntered;
	public Timestamp dateModified;
	public long imageID;
	public String imageType;
	public String imageUID;
	public String instanceNumber;
	public long patientID;
	protected int serialVersion = 1;
	public long seriesID;
	public char status;
	public long studyID;
	public String transferSyntaxUID;

	// static public final char STATUS_PENDING? = 'P';
	public Image()
	{
		// acme.util.Stats.inc("new Image");
	}

	public Image(ImageMetaInfo imi, DSFileView v)
	{
		imageID = imi.getImageID();
		seriesID = imi.getSeriesID();
		studyID = imi.getStudyID();
		patientID = imi.getPatientID();
		imageType = v.ImageType;
		classUID = imi.getMediaStorageSOPClassUID();
		imageUID = imi.getMediaStorageSOPInstanceUID();
		transferSyntaxUID = imi.getTransferSyntax();
		instanceNumber = v.InstanceNumber;
		dateEntered = new Timestamp(System.currentTimeMillis());
		dateModified = new Timestamp(System.currentTimeMillis());
		// acme.util.Stats.inc("new Image");
	}

	public String getClassUID()
	{
		return classUID;
	}

	public Timestamp getDateEntered()
	{
		return dateEntered;
	}

	public Timestamp getDateModified()
	{
		return dateModified;
	}

	public long getImageID()
	{
		return imageID;
	}

	public String getImageType()
	{
		return imageType;
	}

	public String getImageUID()
	{
		return imageUID;
	}

	public String getInstanceNumber()
	{
		return instanceNumber;
	}

	public long getPatientID()
	{
		return patientID;
	}

	public long getSeriesID()
	{
		return seriesID;
	}

	public char getStatus()
	{
		return status;
	}

	public long getStudyID()
	{
		return studyID;
	}

	public String getTransferSyntaxUID()
	{
		return transferSyntaxUID;
	}

	public void setClassUID(String classUID)
	{
		this.classUID = classUID;
	}

	public void setDateEntered(Timestamp dateEntered)
	{
		this.dateEntered = dateEntered;
	}

	public void setDateModified(Timestamp dateModified)
	{
		this.dateModified = dateModified;
	}

	public void setImageID(long imageID)
	{
		this.imageID = imageID;
	}

	public void setImageType(String imageType)
	{
		this.imageType = imageType;
	}

	public void setImageUID(String imageUID)
	{
		this.imageUID = imageUID;
	}

	public void setInstanceNumber(String instanceNumber)
	{
		this.instanceNumber = instanceNumber;
	}

	public void setPatientID(long patientID)
	{
		this.patientID = patientID;
	}

	public void setSeriesID(long seriesID)
	{
		this.seriesID = seriesID;
	}

	public void setStatus(char status)
	{
		this.status = status;
	}

	public void setStudyID(long studyID)
	{
		this.studyID = studyID;
	}

	public void setTransferSyntaxUID(String transferSyntaxUID)
	{
		this.transferSyntaxUID = transferSyntaxUID;
	}

	public String toString()
	{
		return "image:" + imageType + ":" + imageUID;
	}
}
