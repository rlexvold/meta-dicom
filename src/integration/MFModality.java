package integration;

import java.io.Serializable;

public class MFModality implements Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;
	public long id;
	public String name = "";
	public String type = "";
	public String institution = "";
	public String station = "";

	public MFModality()
	{
	}

	public MFModality(long id, String name, String type, String institution, String station)
	{
		this.id = id;
		this.name = name;
		this.type = type;
		this.institution = institution;
		this.station = station;
	}
}
