package net.metafusion.util;

import java.io.Serializable;
import acme.storage.SSMetaData;

public class ImageMetaInfo extends SSMetaData implements Serializable
{
	static final long serialVersionUID = 1L;

	public ImageMetaInfo()
	{
		super.setType("image");
	}
	String mediaStorageSOPInstanceUID = "";
	String mediaStorageSOPClassUID = "";
	String xferSyntax = "";
	String SCP_AE = "";
	String SCU_AE = "";
	String DataCreateTime = "";
	long imageID = 0;
	long patientID = 0;
	long seriesID = 0;
	long studyID = 0;

	public String getSCP_AE()
	{
		return SCP_AE;
	}

	public void setSCP_AE(String SCP_AE)
	{
		this.SCP_AE = SCP_AE;
	}

	public String getSCU_AE()
	{
		return SCU_AE;
	}

	public void setSCU_AE(String SCU_AE)
	{
		this.SCU_AE = SCU_AE;
	}

	public String getDataCreateTime()
	{
		return DataCreateTime;
	}

	public void setDataCreateTime(String dataCreateTime)
	{
		DataCreateTime = dataCreateTime;
	}

	public long getImageID()
	{
		return imageID;
	}

	public void setImageID(long imageID)
	{
		this.imageID = imageID;
	}

	public long getPatientID()
	{
		return patientID;
	}

	public void setPatientID(long patientID)
	{
		this.patientID = patientID;
	}

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

	public String getMediaStorageSOPInstanceUID()
	{
		return mediaStorageSOPInstanceUID;
	}

	public String getMediaStorageSOPClassUID()
	{
		return mediaStorageSOPClassUID;
	}

	public String getTransferSyntax()
	{
		return xferSyntax;
	}

	public void setMediaStorageSOPInstanceUID(String MediaStorageSOPInstanceUID)
	{
		this.mediaStorageSOPInstanceUID = MediaStorageSOPInstanceUID;
	}

	public void setMediaStorageSOPClassUID(String MediaStorageSOPClassUID)
	{
		this.mediaStorageSOPClassUID = MediaStorageSOPClassUID;
	}

	public void setTransferSyntax(String xferSyntax)
	{
		this.xferSyntax = xferSyntax;
	}
}
