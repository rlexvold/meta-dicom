package acme.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LoggedInputStream extends FilterInputStream
{
	// static Log log = new Log("LoggedInputStream");
	public static void log(String s)
	{
		Log.log(s);
	}

	public LoggedInputStream(InputStream in)
	{
		super(in);
	}

	public int read() throws IOException
	{
		int b = in.read();
		if (b != -1)
		{
			log("read[1]:" + b);
		} else log("read1: EOF");
		return b;
	}

	/**
	 * Reads into an array of bytes. Will block until some input is available.
	 * 
	 * @param buf
	 *            the buffer into which the data is read
	 * @param off
	 *            the start offset of the data
	 * @param len
	 *            the maximum number of bytes read
	 * @return the actual number of bytes read, or -1 if the end of the stream
	 *         is reached.
	 * @exception IOException
	 *                if an I/O error has occurred
	 */
	public int read(byte[] buf, int off, int len) throws IOException
	{
		len = in.read(buf, off, len);
		if (len != -1)
		{
			log("read[" + len + "]:");
			Util.dumpBytes(buf, off, len);
		} else log("read: EOF");
		return len;
	}

	/**
	 * Skips specified number of bytes of input.
	 * 
	 * @param n
	 *            the number of bytes to skip
	 * @return the actual number of bytes skipped
	 * @exception IOException
	 *                if an I/O error has occurred
	 */
	public long skip(long n) throws IOException
	{
		byte[] buf = new byte[512];
		long total = 0;
		while (total < n)
		{
			long len = n - total;
			len = read(buf, 0, len < buf.length ? (int) len : buf.length);
			log("skip:");
			Util.dumpBytes(buf, 0, (int) len);
			if (len == -1)
			{
				log("skip: EOF");
				return total;
			}
			total += len;
		}
		return total;
	}
}
