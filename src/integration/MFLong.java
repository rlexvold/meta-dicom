package integration;

import java.io.Serializable;

public class MFLong implements Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;
	public long l;

	MFLong(long l)
	{
		this.l = l;
	}
}