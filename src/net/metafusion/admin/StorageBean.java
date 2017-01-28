package net.metafusion.admin;

import java.io.Serializable;
import acme.util.XML;

public class StorageBean implements Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;
	private long totalSize = 0;
	private long freeSize = 0;

	public StorageBean()
	{
	}

	public StorageBean(XML x)
	{
		totalSize = Long.parseLong(x.get("capacity", "0"));
		freeSize = Long.parseLong(x.get("free", "0"));
	}

	public StorageBean(long l[])
	{
		totalSize = l[0];
		freeSize = l[1];
	}
	private String freeString = "";
	private String perFreeString = "";

	public StorageBean(String s[])
	{
		freeString = s[0];
		perFreeString = s[1];
	}

	public String getFreeString()
	{
		return freeString;
	}

	public String getPerFreeString()
	{
		return perFreeString;
	}

	public long getTotalSize()
	{
		return totalSize;
	}

	public void setTotalSize(long totalSize)
	{
		this.totalSize = totalSize;
	}

	public long getFreeSize()
	{
		return freeSize;
	}

	public void setFreeSize(long freeSize)
	{
		this.freeSize = freeSize;
	}

	public int getPercentFree()
	{
		if (totalSize == 0) return 0;
		return (int) ((freeSize * 100) / totalSize);
	}
}
