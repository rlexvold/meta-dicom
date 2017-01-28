package acme.storage;

import java.io.Serializable;

public class SSMetaData implements Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;
	protected int version = 1;
	protected String type = "";

	public int getSerialVersion()
	{
		return serialVersion;
	}

	public void setSerialVersion(int serialVersion)
	{
		this.serialVersion = serialVersion;
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}
}