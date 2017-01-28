package acme.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class EchoedOutputStream extends FilterOutputStream
{
	OutputStream os;

	public EchoedOutputStream(OutputStream out, OutputStream os)
	{
		super(out);
		this.os = os;
	}

	public void write(int b) throws IOException
	{
		out.write(b);
		os.write(b);
	}

	public void write(byte[] b, int off, int len) throws IOException
	{
		out.write(b, off, len);
		os.write(b, off, len);
	}
}
