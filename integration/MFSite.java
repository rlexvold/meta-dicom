package integration;

import java.io.Serializable;

public class MFSite implements Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;

	public MFSite()
	{
	}

	public MFSite(long id, String name)
	{
		this.id = id;
		this.name = name;
	}

	void AddModality(MFModality m)
	{
		MFModality[] newm = new MFModality[modality.length + 1];
		System.arraycopy(modality, 0, newm, 0, modality.length);
		newm[modality.length] = m;
		modality = newm;
	}

	MFModality[] getModality()
	{
		return modality;
	}
	long id;
	String name = "";
	MFModality[] modality = new MFModality[0];
	public class RSite
	{
		public long SiteID;
		public String Name;
		public RModality[] Modality = new RModality[0];
	}
	public class RModality
	{
		public long ModalityID;
		public String Name;
		public String Type;
		public String Institution;
		public String Station;
	}
}
