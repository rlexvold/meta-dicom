package net.metafusion.admin;

import java.io.Serializable;

public class LocalStoreBean implements Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;
	private String name;

	public LocalStoreBean(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String toString()
	{
		return name;
	}
}
