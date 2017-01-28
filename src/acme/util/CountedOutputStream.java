package acme.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CountedOutputStream extends FilterOutputStream
{
	private int offset = 0;

	public int getOffset()
	{
		return offset;
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	public CountedOutputStream(OutputStream out)
	{
		super(out);
		offset = 0;
	}

	public CountedOutputStream()
	{
		super(null);
		offset = 0;
	}

	public void write(int b) throws IOException
	{
		offset++;
		if (out != null) out.write(b);
	}

	public void write(byte[] b, int off, int len) throws IOException
	{
		offset += len;
		out.write(b, off, len);
	}
}
