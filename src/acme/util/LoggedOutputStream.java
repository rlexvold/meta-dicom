package acme.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LoggedOutputStream extends FilterOutputStream
{
	public static void log(String s)
	{
		Log.log(s);
	}

	public LoggedOutputStream(OutputStream out)
	{
		super(out);
	}

	public void write(int b) throws IOException
	{
		out.write(b);
		log("write[1]: " + b);
	}

	/**
	 * Writes an array of bytes. Will block until the bytes are actually
	 * written.
	 * 
	 * @param b
	 *            the data to be written
	 * @param off
	 *            the start offset of the data
	 * @param len
	 *            the number of bytes to be written
	 * @exception IOException
	 *                if an I/O error has occurred
	 */
	public void write(byte[] b, int off, int len) throws IOException
	{
		out.write(b, off, len);
		log("write[" + len + "]:");
		Util.dumpBytes(b, off, len);
	}
}
