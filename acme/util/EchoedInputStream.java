package acme.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class EchoedInputStream extends FilterInputStream
{
	OutputStream out;

	public EchoedInputStream(InputStream in, OutputStream out)
	{
		super(in);
		this.out = out;
	}

	public int read() throws IOException
	{
		int b = in.read();
		if (b != -1)
		{
			out.write(b);
		}
		return b;
	}

	public int read(byte[] buf, int off, int len) throws IOException
	{
		len = in.read(buf, off, len);
		if (len != -1)
		{
			out.write(buf, off, len);
		}
		return len;
	}

	public long skip(long n) throws IOException
	{
		byte[] buf = new byte[512];
		long total = 0;
		while (total < n)
		{
			long len = n - total;
			len = read(buf, 0, len < buf.length ? (int) len : buf.length);
			if (len == -1) { return total; }
			out.write(buf, 0, (int) len);
			total += len;
		}
		return total;
	}
}
