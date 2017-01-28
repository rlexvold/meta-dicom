package integration;

import java.io.Serializable;

public class MFError implements Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;

	public MFError()
	{
	}

	public MFError(String msg)
	{
		this.msg = msg;
	}
	public String msg = "";
}