package acme.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class XByteArrayOutputStream extends ByteArrayOutputStream
{
	public XByteArrayOutputStream()
	{
	}

	public XByteArrayOutputStream(byte[] b)
	{
		super();
		buf = b;
	}

	public void writeTo(OutputStream out, int start, int len) throws IOException
	{
		out.write(buf, start, len);
	}

	public byte[] getBuf()
	{
		return buf;
	}
}
