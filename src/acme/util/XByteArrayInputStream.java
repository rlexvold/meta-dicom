package acme.util;

import java.io.ByteArrayInputStream;

public class XByteArrayInputStream extends ByteArrayInputStream
{
	public XByteArrayInputStream(byte[] b)
	{
		super(b);
	}

	public XByteArrayInputStream(XByteArrayOutputStream b)
	{
		super(b.getBuf());
		reset(b.size());
	}

	public void reset(int size)
	{
		this.pos = 0;
		this.count = size;
		this.mark = 0;
	}
}
