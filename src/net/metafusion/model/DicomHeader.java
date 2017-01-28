package net.metafusion.model;

import java.io.Serializable;

import net.metafusion.dataset.DS;

public class DicomHeader implements Serializable
{
	static final long	serialVersionUID	= 1L;
	public long			imageID;
	public DS			dicomHeader;
	public String		dicomTag;
	public String		dicomValue;

	public String getDicomTag()
	{
		return dicomTag;
	}

	public void setDicomTag(String dicomTag)
	{
		this.dicomTag = dicomTag;
	}

	public String getDicomValue()
	{
		return dicomValue;
	}

	public void setDicomValue(String dicomValue)
	{
		this.dicomValue = dicomValue;
	}

	public long getImageID()
	{
		return imageID;
	}

	public void setImageID(long imageID)
	{
		this.imageID = imageID;
	}

	public DS getDicomHeader()
	{
		return dicomHeader;
	}

	public void setDicomHeader(DS dicomHeader)
	{
		this.dicomHeader = dicomHeader;
	}
}
