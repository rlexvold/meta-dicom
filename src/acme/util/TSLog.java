package acme.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

// time stamped log: ttttt,aaaa,bbb,ccc,ddd...,xxxx
public class TSLog
{
	private static TSLog tsLog = null;

	static public TSLog get()
	{
		return tsLog;
	}

	public TSLogReader getReader() throws Exception
	{
		return new TSLogReader(file);
	}

	public FileInputStream getInputStream() throws Exception
	{
		return new FileInputStream(file);
	}
	private long lastTime = 0;
	private File file;
	private FileOutputStream fos;
	private PrintWriter pw;

	public TSLog(File f) throws Exception
	{
		Util.Assert(tsLog == null);
		tsLog = this;
		this.file = f;
		fos = new FileOutputStream(f, true);
		pw = new PrintWriter(fos);
	}

	public void close()
	{
		try
		{
			if (pw != null) pw.close();
		}
		catch (Exception e)
		{
		}
		try
		{
			if (fos != null) fos.close();
		}
		catch (IOException e)
		{
		}
	}

	synchronized public void log(String s)
	{
		long t = System.currentTimeMillis();
		if (t == lastTime)
		{
			t++;
			lastTime = t;
		}
		pw.println("\"" + new Date() + "\"," + s);
		pw.flush();
	}

	public void log(String a, String b)
	{
		log(a + "," + b);
	}

	public void log(String a, String b, String c)
	{
		log(a + "," + b + "," + c);
	}

	public void log(String a, String b, String c, String d)
	{
		log(a + "," + b + "," + c + "," + d);
	}

	public void log(String a, String b, String c, String d, String e)
	{
		log(a + "," + b + "," + c + "," + d + "," + e);
	}
}