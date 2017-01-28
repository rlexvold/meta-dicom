package acme.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

public class Util
{
	public static void log(String s)
	{
		Log.log(s);
		// acme.util.Log.raw(s);
	}

	public static void log(String s, Exception e)
	{
		Log.log(s, e);
		// net.metafusion.util.Log.log(s, e);
	}

	public static String stackTraceToString(Exception e)
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(os);
		e.printStackTrace(pw);
		pw.flush();
		return os.toString();
	}

	public static Object newInstance(String name)
	{
		try
		{
			return Class.forName(name).newInstance();
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public static Object instanceOf(String name, Object arg1)
	{
		try
		{
			Constructor cons = Class.forName(name).getConstructor(new Class[] { arg1.getClass() });
			return cons.newInstance(new Object[] { arg1 });
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public static int decodeInt(boolean be, byte[] b, int offset)
	{
		return be ? decodeInt(b, offset) : decodeIntLE(b, offset);
	}

	public static short decodeShort(boolean be, byte[] b, int offset)
	{
		return be ? decodeShort(b, offset) : decodeShortLE(b, offset);
	}

	public static long decodeLong(boolean be, byte[] b, int offset)
	{
		return be ? decodeLong(b, offset) : decodeLongLE(b, offset);
	}

	public static void encodeInt(boolean be, int v, byte[] b, int offset)
	{
		if (be)
			encodeInt(v, b, offset);
		else
			encodeIntLE(v, b, offset);
	}

	public static void encodeLong(boolean be, long v, byte[] b, int offset)
	{
		if (be)
			encodeLong(v, b, offset);
		else
			encodeLongLE(v, b, offset);
	}

	public static void encodeShort(boolean be, short v, byte[] b, int offset)
	{
		if (be)
			encodeShort(v, b, offset);
		else
			encodeShortLE(v, b, offset);
	}

	public static int decodeInt(byte[] b, int offset)
	{
		return ((b[offset + 0] & 0xFF) << 24) | ((b[offset + 1] & 0xFF) << 16) | ((b[offset + 2] & 0xFF) << 8) | b[offset + 3] & 0xFF;
	}

	public static short decodeShort(byte[] b, int offset)
	{
		return (short) (((b[offset + 0] & 0xFF) << 8) | (b[offset + 1]) & 0xFF);
	}

	public static void encodeInt(int v, byte[] b, int offset)
	{
		b[offset] = (byte) ((v >>> 24) & 0xFF);
		b[offset + 1] = (byte) ((v >>> 16) & 0xFF);
		b[offset + 2] = (byte) ((v >>> 8) & 0xFF);
		b[offset + 3] = (byte) ((v >>> 0) & 0xFF);
	}

	public static void encodeShort(short v, byte[] b, int offset)
	{
		b[offset] = (byte) ((v >>> 8) & 0xFF);
		b[offset + 1] = (byte) ((v >>> 0) & 0xFF);
	}

	public static int decodeIntLE(byte[] b, int offset)
	{
		return (((b[offset + 3]) & 0xFF) << 24) | (((b[offset + 2]) & 0xFF) << 16) | (((b[offset + 1]) & 0xFF) << 8) | b[offset + 0] & 0xFF;
	}

	public static short decodeShortLE(byte[] b, int offset)
	{
		return (short) (((b[offset + 1] & 0xFF) << 8) | (b[offset + 0]) & 0xFF);
	}

	public static void encodeIntLE(int v, byte[] b, int offset)
	{
		b[offset] = (byte) ((v >>> 0) & 0xFF);
		b[offset + 1] = (byte) ((v >>> 8) & 0xFF);
		b[offset + 2] = (byte) ((v >>> 16) & 0xFF);
		b[offset + 3] = (byte) ((v >>> 24) & 0xFF);
	}

	public static void encodeShortLE(short v, byte[] b, int offset)
	{
		b[offset] = (byte) ((v >>> 0) & 0xFF);
		b[offset + 1] = (byte) ((v >>> 8) & 0xFF);
	}

	public static long decodeLongLE(byte b[], int offset)
	{
		long l = 0;
		for (int i = 0; i < 8; i++)
			l = l | (((long) b[offset + i] & 0x0FF) << i * 8);
		return l;
	}

	public static long decodeLong(byte b[], int offset)
	{
		long l = 0;
		for (int i = 0; i < 8; i++)
			l = l | (((long) b[offset + i]) << (7 - i) * 8);
		return l;
	}

	public static void encodeLongLE(long l, byte b[], int offset)
	{
		for (int i = 0; i < 8; i++)
			b[offset + i] = (byte) (l >>> i * 8);
	}

	public static void encodeLong(long l, byte b[], int offset)
	{
		for (int i = 0; i < 8; i++)
			b[offset + i] = (byte) (l >>> (7 - i) * 8);
	}

	public static boolean equalBytes(byte a[], byte b[])
	{
		if (a.length != b.length)
			return false;
		for (int i = 0; i < a.length; i++)
			if (a[i] != b[i])
				return false;
		return true;
	}

	public static byte[] read(InputStream is, int size)
	{
		byte b[] = new byte[size];
		int cnt = 0;
		try
		{
			cnt = is.read(b);
			if (cnt != size)
				b = null;
		}
		catch (Exception e)
		{
			Log.log("read: expected" + size + " got " + cnt);
			b = null;
		}
		return b;
	}

	public static byte[] readFully(InputStream is, int size)
	{
		byte b[] = new byte[size];
		int cnt = size;
		int pos = 0;
		try
		{
			while (cnt > 0)
			{
				int io = is.read(b, pos, cnt);
				if (io == -1)
					break;
				pos += io;
				cnt -= io;
			}
			Util.Assert(cnt == 0);
		}
		catch (Exception e)
		{
			acme.util.Log.log("read: expected" + size + " got " + cnt);
			b = null;
		}
		return b;
	}

	public static String dumpBytesToString(byte[] b)
	{
		return dumpBytesToString(b, 0, b.length);
	}

	public static void dumpBytes(byte[] b)
	{
		dumpBytes(b, 0, b.length);
	}
	static char	hexMap[]	= { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	public static String dumpBytesToString(byte[] b, int offset, int len)
	{
		StringBuffer sb = new StringBuffer();
		StringBuffer chars = new StringBuffer();
		int pos = offset;
		int end = pos + len;
		sb.setLength(0);
		while (pos < end)
		{
			// int lineStart = pos;
			chars.setLength(0);
			sb.append("[" + StringUtil.lpad("" + (pos - offset), '0', 4) + "]");
			for (int i = 0; i < 32; i++)
			{
				if (i != 0 && (i % 4) == 0)
				{
					sb.append(' ');
					;// chars.append(' ');
				}
				if (pos < end)
				{
					char c = (char) b[pos];
					if (Character.isISOControl(c))
						c = '.';
					chars.append(c);
					sb.append(hexMap[((b[pos] >> 4) & 0xF)]);
					sb.append(hexMap[((b[pos]) & 0xF)]);
					pos++;
				}
				else
					sb.append("  ");
			}
			sb.append("  ");
			sb.append(chars.toString());
			sb.append("\n");
		}
		sb.setLength(sb.length() - 1);
		return sb.toString();
	}

	public static void dumpBytes(byte[] b, int offset, int len)
	{
		String s = dumpBytesToString(b, offset, len);
		acme.util.Log.log(s);
	}

	static public Buffer readFile(File f) throws Exception
	{
		Buffer b;
		FileInputStream fs = null;
		try
		{
			long size = f.length();
			fs = new FileInputStream(f);
			byte[] buffer = new byte[(int) size];
			int len = fs.read(buffer);
			Assert(len == size);
			b = new Buffer(buffer);
		}
		finally
		{
			if (fs != null)
				fs.close();
		}
		return b;
	}

	static public byte[] readFile(String name, int offset, int length) throws Exception
	{
		byte[] data = null;
		try
		{
			FileInputStream fis = new FileInputStream(name);
			data = new byte[length];
			fis.skip(offset);
			int count = fis.read(data, 0, length);
			fis.close();
			if (count == -1)
				return null;
			if (count < length)
			{
				byte[] tmp = new byte[count];
				System.arraycopy(data, 0, tmp, 0, count);
				data = tmp;
			}
		}
		catch (IndexOutOfBoundsException ie)
		{
			return null;
		}
		catch (Exception e)
		{
			throw e;
		}
		return data;
	}

	static public void writeFile(Buffer b, File f) throws Exception
	{
		FileOutputStream fs = null;
		try
		{
			File parents = f.getParentFile();
			if (parents != null && !parents.exists())
				parents.mkdirs();
			fs = new FileOutputStream(f);
			fs.write(b.getBuffer(), b.getStart(), b.getLength());
		}
		finally
		{
			if (fs != null)
				fs.close();
		}
	}

	static public void writeFile(byte[] b, File f) throws Exception
	{
		writeFile(b, f, false);
	}

	static public void writeFile(byte[] b, File f, boolean append) throws Exception
	{
		FileOutputStream fs = null;
		try
		{
			File parents = f.getParentFile();
			if (parents != null && !parents.exists())
				parents.mkdirs();
			fs = new FileOutputStream(f, append);
			fs.write(b);
		}
		finally
		{
			if (fs != null)
				fs.close();
		}
	}

	static public boolean writeObjectToFile(Object o, File f)
	{
		File temp = null;
		try
		{
			File parents = f.getParentFile();
			if (parents != null && !parents.exists())
				parents.mkdirs();
			temp = new File(f.getParentFile(), f.getName() + ".tmp");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
			oos.flush();
			writeFile(baos.toByteArray(), temp);
			boolean b = FileUtil.rename(temp, f, false);
			if (!b)
			{
				log("writeObjectToFile rename fail " + temp.getAbsolutePath());
				return false;
			}
			else
				temp = null;
		}
		catch (Exception e)
		{
			log("writeObjectToFile", e);
			return false;
		}
		finally
		{
			Util.safeDelete(temp);
		}
		return true;
	}

	static public Object readObjectFromFile(File f)
	{
		try
		{
			byte[] b = readWholeFile(f);
			if (b == null)
				return null;
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b));
			Object o = ois.readObject();
			return o;
		}
		catch (Exception e)
		{
			log("readObjectFromFile " + f, e);
			e.printStackTrace();
		}
		return null;
	}

	static public byte[] readWholeFile(File f) throws Exception
	{
		byte[] buffer = null;
		FileInputStream fs = null;
		try
		{
			long size = f.length();
			fs = new FileInputStream(f);
			buffer = new byte[(int) size];
			int len = fs.read(buffer);
			Assert(len == size);
		}
		catch (Exception e)
		{
			buffer = null;
			throw e;
		}
		finally
		{
			try
			{
				if (fs != null)
					fs.close();
			}
			catch (Exception e)
			{
				;
			}
		}
		return buffer;
	}

	static public String readLine(InputStream is)
	{
		boolean first = true;
		StringBuffer sb = new StringBuffer();
		try
		{
			for (;;)
			{
				int r = is.read();
				if (r == -1)
					break;
				first = false;
				if (r == 13)
					continue;
				if (r == 10)
					break;
				sb.append((char) r);
			}
		}
		catch (IOException e)
		{
			first = true;
		}
		return first ? null : sb.toString();
	}

	// synchronize?
	public static void safeClose(Socket s)
	{
		try
		{
			if (s != null)
				s.close();
		}
		catch (IOException e)
		{
			;
		}
	}

	public static void safeClose(ServerSocket s)
	{
		try
		{
			if (s != null)
				s.close();
		}
		catch (IOException e)
		{
			;
		}
	}

	public static void safeClose(InputStream is)
	{
		try
		{
			if (is != null)
				is.close();
		}
		catch (IOException e)
		{
			;
		}
	}

	public static void safeClose(OutputStream os)
	{
		try
		{
			if (os != null)
				os.close();
		}
		catch (IOException e)
		{
			;
		}
	}

	public static void safeClose(RandomAccessFile f)
	{
		try
		{
			if (f != null)
				f.close();
		}
		catch (Exception e)
		{
			;
		}
	}

	public static void safeClose(Reader f)
	{
		try
		{
			if (f != null)
				f.close();
		}
		catch (Exception e)
		{
			;
		}
	}

	public static void safeClose(Writer f)
	{
		try
		{
			if (f != null)
				f.close();
		}
		catch (Exception e)
		{
			;
		}
	}

	public static void safeDelete(File f)
	{
		if (f != null)
			Log.log("safeDelete: " + f.getName());
		// Cyrus - take out these lines - debugging only for ImageNorth MR
		// if (f.getName().endsWith(".msgd")) {
		// Log.log("not deleting: "+f.getAbsolutePath());
		// return;
		// }
		// else Log.log("================== safeDelete:null file");
		try
		{
			if (f != null)
				f.delete();
		}
		catch (Exception e)
		{
			;
		}
	}

	private static int read(InputStream is, byte b[]) throws IOException
	{
		try
		{
			return is.read(b);
		}
		catch (IOException e)
		{
			if (is.getClass().equals(FileInputStream.class))
			{
				Log.log("*********************************");
				Log.log("read caught FileSystemError. Exiting..." + e);
				Log.log(Util.stackTraceToString(e));
				System.exit(-1);
			}
			throw e;
		}
	}

	private static int read(InputStream is, byte b[], int offset, int size) throws IOException
	{
		try
		{
			return is.read(b, offset, size);
		}
		catch (IOException e)
		{
			if (is.getClass().equals(FileInputStream.class))
			{
				Log.log("*********************************");
				Log.log("read caught FileSystemError. Exiting..." + e);
				Log.log(Util.stackTraceToString(e));
				System.exit(-1);
			}
			throw e;
		}
	}

	private static void write(OutputStream os, byte b[], int offset, int size) throws IOException
	{
		try
		{
			os.write(b, offset, size);
		}
		catch (IOException e)
		{
			if (os.getClass().equals(FileOutputStream.class))
			{
				Log.log("*********************************");
				Log.log("write caught FileSystemError. Exiting..." + e);
				Log.log(Util.stackTraceToString(e));
				System.exit(-1);
			}
			throw e;
		}
	}

	public static void copyStreamToFile(InputStream is, File f) throws Exception
	{
		OutputStream os = null;
		try
		{
			os = new FileOutputStream(f);
			copyStream(is, os);
		}
		finally
		{
			Util.safeClose(os);
		}
	}

	public static void copyFileToStream(File f, OutputStream os) throws Exception
	{
		InputStream is = null;
		try
		{
			is = new FileInputStream(f);
			copyStream(is, os);
		}
		finally
		{
			Util.safeClose(os);
		}
	}

	public static void copyStream(InputStream is, OutputStream os) throws IOException
	{
		try
		{
			byte buffer[] = new byte[4096];
			for (;;)
			{
				int cnt = read(is, buffer);
				if (cnt == -1)
					break;
				write(os, buffer, 0, cnt);
			}
			os.flush();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			throw e;
		}
	}

	public static void copyStream(InputStream is, OutputStream os, int max) throws IOException
	{
		byte buffer[] = new byte[4096];
		int togo = max;
		while (togo > 0)
		{
			int cnt = togo > 4096 ? 4096 : togo;
			cnt = read(is, buffer, 0, cnt);
			if (cnt == -1)
				break;
			write(os, buffer, 0, cnt);
			togo -= cnt;
		}
		os.flush();
	}

	public static void copyStream(InputStream is, OutputStream os, long max) throws IOException
	{
		byte buffer[] = new byte[4096];
		long togo = max;
		while (togo > 0)
		{
			int cnt = togo > 4096 ? 4096 : (int) togo;
			cnt = read(is, buffer, 0, cnt);
			if (cnt == -1)
				break;
			write(os, buffer, 0, cnt);
			togo -= cnt;
		}
		os.flush();
	}

	public static Properties parseArgs(String argv[])
	{
		Properties p = new Properties();
		int arg = 0;
		for (String s : argv)
		{
			if (s.charAt(0) == '-')
			{
				int eqi = s.indexOf('=');
				if (eqi == -1)
					p.put(s, "true");
				else
					p.put(s.substring(0, eqi), s.substring(eqi + 1));
			}
			else
				p.put("argv" + (arg++), s);
		}
		p.put("argc", new Integer(arg));
		return p;
	}

	public static int parseArgv(String argv[])
	{
		Properties p = System.getProperties();
		int arg = 0;
		for (String s : argv)
		{
			if (s.charAt(0) == '-')
			{
				int eqi = s.indexOf('=');
				if (eqi == -1)
					p.put(s, "true");
				else
					p.put(s.substring(0, eqi), s.substring(eqi + 1));
			}
			else
				p.put("argv" + (arg++), s);
		}
		p.put("argc", new Integer(arg));
		return arg;
	}
	static BufferedReader	promptReader	= null;

	static public String prompt(String prompt) throws Exception
	{
		if (promptReader == null)
			promptReader = new BufferedReader(new InputStreamReader(java.lang.System.in));
		if (prompt == null)
			prompt = "";
		java.lang.System.out.print(prompt + "> ");
		String s = promptReader.readLine();
		return s;
	}

	static public void joinThreads(Collection c)
	{
		Iterator iter = c.iterator();
		while (iter.hasNext())
		{
			Thread t = (Thread) iter.next();
			while (t.isAlive())
				try
				{
					t.join();
				}
				catch (InterruptedException ex)
				{
					;
				}
		}
	}

	public static boolean sleep(int ms)
	{
		boolean interrupted = false;
		try
		{
			Thread.sleep(ms);
		}
		catch (InterruptedException e)
		{
			interrupted = true;
		}
		return interrupted;
	}

	public static boolean wait(Object o, int ms)
	{
		boolean interrupted = false;
		try
		{
			synchronized (o)
			{
				o.wait(ms);
			}
		}
		catch (InterruptedException e)
		{
			interrupted = true;
		}
		return interrupted;
	}

	public static boolean randSleep(int maxMS)
	{
		int ms = (int) (Math.random() * maxMS);
		return ms != 0 ? sleep(ms) : false;
	}
	static Boolean	isWindows	= null;

	public static boolean isWindows()
	{
		if (isWindows == null)
		{
			String os = (String) System.getProperties().get("os.name");
			isWindows = new Boolean(os != null && os.startsWith("Windows"));
		}
		return isWindows.booleanValue();
	}
	static class DaemonWrapper implements Runnable
	{
		Runnable	r;
		boolean		log	= true;

		DaemonWrapper(Runnable r)
		{
			this.r = r;
		}

		DaemonWrapper(Runnable r, boolean log)
		{
			this.r = r;
			this.log = log;
		}

		public void run()
		{
			if (log)
				Util.log("starting daemon " + r.getClass().getName());
			try
			{
				r.run();
			}
			catch (Exception e)
			{
				Util.log("!!!!!! warning: exception caught in daemon " + r.getClass().getName());
				e.printStackTrace();
			}
			finally
			{
				if (log)
					Util.log("exit daemon " + r.getClass().getName());
			}
		}
	}

	public static Thread startDaemonThread(Runnable r)
	{
		Thread t = new Thread(new DaemonWrapper(r));
		t.setName(r.toString());
		t.setDaemon(true);
		t.start();
		return t;
	}

	public static Thread startDaemonThread(Runnable r, boolean log)
	{
		Thread t = new Thread(new DaemonWrapper(r, log));
		t.setName(r.toString());
		t.setDaemon(true);
		t.start();
		return t;
	}
	private static int	count	= 0;

	synchronized public static File generateUniqueName(File path, String prefix, String post)
	{
		File f;
		Calendar c = Calendar.getInstance();
		String baseName = prefix + "-" + StringUtil.int2(c.get(Calendar.MONTH) + 1) + StringUtil.int2(c.get(Calendar.DAY_OF_MONTH)) + StringUtil.int2(c.get(Calendar.YEAR) - 2000)
				+ "_" + StringUtil.int2(c.get(Calendar.HOUR_OF_DAY)) + StringUtil.int2(c.get(Calendar.MINUTE));
		String fileName = baseName + post;
		int tries = 0;
		for (;;)
		{
			f = new File(path, fileName);
			if (!f.exists() || tries++ > 250)
				break;
			fileName = baseName + "_" + count + post;
			count++;
			Util.sleep(100); // avoiding locking up on windows
		}
		return f;
	}

	// public static XML encodeXML(Object o) {
	// try {
	// ByteArrayOutputStream baos = new ByteArrayOutputStream();
	// XMLEncoder encoder = new XMLEncoder(baos);
	// encoder.writeObject(o);
	// encoder.close();
	// InputStream is = new ByteArrayInputStream(baos.toByteArray());
	// return new XML(is);
	// } catch (Exception e) {
	// Util.log("could not encodeXML "+o);
	// return null;
	// }
	// }
	// public static Object decodeXML(XML x) {
	// try {
	// ByteArrayOutputStream baos = new ByteArrayOutputStream();
	// x.writeTo(baos);
	// baos.close();
	// XMLDecoder decoder = new XMLDecoder(new
	// ByteArrayInputStream(baos.toByteArray()));
	// return decoder.readObject();
	// } catch (Exception e) {
	// Util.log("could not decodeXML "+x);
	// return null;
	// }
	// }
	public static void Assert(boolean b)
	{
		if (!b)
			throw new RuntimeException("assertion failed!");
	}

	static public String[] exec(String cmdLine)
	{
		return exec(cmdLine, null);
	}

	static public String[] exec(String cmdLine, File workingDir)
	{
		ArrayList al = new ArrayList();
		try
		{
			String line;
			Process p;
			if (workingDir != null)
				p = Runtime.getRuntime().exec(cmdLine, new String[] { "path", System.getenv("path") }, workingDir);
			else
				p = Runtime.getRuntime().exec(cmdLine);
			// BufferedReader input =
			// new BufferedReader
			// (new InputStreamReader(p.getErrorStream()));
			// while ((line = input.readLine()) != null) {
			// line.trim();
			// al.add(line);
			// }
			// input.close();
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null)
			{
				line.trim();
				al.add(line);
			}
			input.close();
		}
		catch (Exception err)
		{
			Util.log("exec: " + cmdLine + " caught " + err);
		}
		return (String[]) al.toArray(new String[al.size()]);
	}

	static public String[] execAndThrow(String cmdLine) throws Exception
	{
		ArrayList al = new ArrayList();
		try
		{
			String line;
			Process p = Runtime.getRuntime().exec(cmdLine);
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null)
			{
				line.trim();
				al.add(line);
			}
			input.close();
		}
		catch (Exception err)
		{
			Util.log("exec: " + cmdLine + " caught " + err);
			throw err;
		}
		return (String[]) al.toArray(new String[al.size()]);
	}

	static public String getManifestVersion()
	{
		String s = null;
		Package pp = Util.class.getPackage();
		s = pp != null ? pp.getImplementationVersion() : null;
		return s == null ? "No Manifest" : s;
	}
}
