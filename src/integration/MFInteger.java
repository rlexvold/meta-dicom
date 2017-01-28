package integration;

import java.io.Serializable;

public class MFInteger implements Serializable
{
	static final long	serialVersionUID	= 1L;
	protected int		serialVersion		= 1;
	public int			i;

	public MFInteger(int i)
	{
		this.i = i;
	}
}