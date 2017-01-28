package integration;

import java.io.Serializable;

public class MFAttachment implements Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;

	public MFAttachment()
	{
	}

	public MFAttachment(long id, String name, String label)
	{
		this.id = id;
		this.name = name;
		this.label = label;
	}
	public long id;
	public String name = "";
	public String label = "";
}
