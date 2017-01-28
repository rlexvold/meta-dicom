package acme.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class NullInputStream extends FilterInputStream
{
	public NullInputStream(InputStream in)
	{
		super(in);
	}

	public NullInputStream()
	{
		super(null);
	}

	public int read() throws IOException
	{
		return 0;
	}

	public int read(byte[] buf, int off, int len) throws IOException
	{
		for (int i = off; i < off + len; i++)
			buf[i] = 0;
		return len;
	}

	public long skip(long n) throws IOException
	{
		return n;
	}
}
