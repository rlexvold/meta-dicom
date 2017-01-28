package net.metafusion.model;

public class Entity
{
	String name;
	String dicomName;
	String type;
	String host;
	short port;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getDicomName()
	{
		return dicomName;
	}

	public void setDicomName(String dicomName)
	{
		this.dicomName = dicomName;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public short getPort()
	{
		return port;
	}

	public void setPort(short port)
	{
		this.port = port;
	}
}
