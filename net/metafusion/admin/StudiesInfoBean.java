package net.metafusion.admin;

import java.io.Serializable;
import acme.util.XML;

public class StudiesInfoBean implements Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;
	int numStudies;
	int numSeries;
	int numImages;
	long size = 0;
	String[] msgs;

	public StudiesInfoBean()
	{
	}

	public StudiesInfoBean(XML x)
	{
		numStudies = x.getInt("numStudies");
		numSeries = x.getInt("numSeries");
		numImages = x.getInt("numImages");
		size = x.getLong("size");
	}

	public XML toXML()
	{
		XML x = new XML("StudiesInfoBean");
		x.addAttr("numStudies", "" + numStudies);
		x.addAttr("numSeries", "" + numSeries);
		x.addAttr("numImages", "" + numImages);
		x.addAttr("size", "" + (size < 1 ? 1 : size));
		return x;
	}

	public int getNumStudies()
	{
		return numStudies;
	}

	public void setNumStudies(int numStudies)
	{
		this.numStudies = numStudies;
	}

	public int getNumSeries()
	{
		return numSeries;
	}

	public void setNumSeries(int numSeries)
	{
		this.numSeries = numSeries;
	}

	public int getNumImages()
	{
		return numImages;
	}

	public void setNumImages(int numImages)
	{
		this.numImages = numImages;
	}

	public long getSize()
	{
		return size;
	}

	public void setSize(long size)
	{
		this.size = size;
	}

	public String[] getMsgs()
	{
		return msgs;
	}

	public void setMsgs(String[] msgs)
	{
		this.msgs = msgs;
	}

	public String toString()
	{
		return "Info: " + numStudies + " studies. " + numSeries + " series. " + numImages + " images. " + (size / (1024 * 1024)) + " MB";
	}
}
