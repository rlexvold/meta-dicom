package integration;

import java.io.Serializable;

public class MFBoolean implements Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;

	public MFBoolean()
	{
	}

	public MFBoolean(boolean b)
	{
		this.b = b;
	}

	public MFBoolean(int i)
	{
		this.b = i != 0;
	}
	public boolean b;
}