package acme.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class NullOutputStream extends FilterOutputStream
{
	public NullOutputStream(OutputStream out)
	{
		super(out);
	}

	public NullOutputStream()
	{
		super(null);
	}

	@Override
	public void write(int b) throws IOException
	{
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException
	{
	}
}
