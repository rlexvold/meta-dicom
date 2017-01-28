package net.metafusion.model;

import java.io.Serializable;

public class ImageFile implements Serializable
{
	private long	imageID		= 0L;
	private long	studyID		= 0L;
	private long	seriesID	= 0L;

	public long getImageID()
	{
		return imageID;
	}

	public void setImageID(long imageID)
	{
		this.imageID = imageID;
	}

	public long getStudyID()
	{
		return studyID;
	}

	public void setStudyID(long studyID)
	{
		this.studyID = studyID;
	}

	public long getSeriesID()
	{
		return seriesID;
	}

	public void setSeriesID(long seriesID)
	{
		this.seriesID = seriesID;
	}
}
