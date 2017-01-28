package acme.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;

public class TSLogReader
{
	static void log(String s)
	{
		System.out.println(s);
	}
	private File file;
	private FileInputStream fis;
	private BufferedReader in;

	public TSLogReader(File f) throws Exception
	{
		this.file = f;
		reset();
	}

	public void close()
	{
		try
		{
			if (fis != null) fis.close();
		}
		catch (Exception e)
		{
		}
		try
		{
			if (in != null) fis.close();
		}
		catch (Exception e)
		{
		}
		fis = null;
		in = null;
	}

	public void reset()
	{
		close();
		lastPos = -1;
		last = null;
		lastLine = null;
		try
		{
			fis = new FileInputStream(file);
		}
		catch (FileNotFoundException e)
		{
			throw new NestedException(e);
		}
		in = new BufferedReader(new InputStreamReader(fis));
	}
	long lastPos = -1;
	String last[];
	String lastLine;

	private String[] parseLine(String s)
	{
		if (s == null) return null;
		int count = 0;
		int start = 0;
		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) == ',') count++;
		String lp[] = new String[count + 1];
		count = 0;
		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) == ',')
			{
				lp[count++] = s.substring(start, i);
				start = i + 1;
			}
		lp[count] = s.substring(start);
		return lp;
	}

	public String[] advanceTo(long pos)
	{
		try
		{
			if (pos < lastPos || lastPos == -1) reset();
			if (pos == lastPos) return last;
			last = null;
			lastLine = null;
			lastPos = -1;
			String s;
			for (;;)
			{
				s = in.readLine();
				if (s == null) return last;
				int i = s.indexOf(',');
				if (i == -1) return last;
				lastPos = Long.parseLong(s.substring(0, i));
				if (lastPos >= pos) break;
			}
			last = parseLine(s);
			lastLine = s;
			return last;
		}
		catch (Exception e)
		{
			close();
			return null;
		}
	}

	public String[] advance()
	{
		try
		{
			if (last == null || fis == null) return null;
			last = null;
			lastPos = -1;
			lastLine = null;
			String s = in.readLine();
			if (s == null) return null;
			last = parseLine(s);
			lastPos = Long.parseLong(last[0]);
			lastLine = s;
			return last;
		}
		catch (Exception e)
		{
			close();
			return null;
		}
	}

	public long getLastPos()
	{
		return lastPos;
	}

	public String getLastLine()
	{
		return lastLine;
	}

	public void dump(long pos)
	{
		log("dump" + pos);
		String s[] = advanceTo(pos);
		while (s != null)
		{
			log("" + getLastPos() + "-" + s[0] + "-" + s[1] + "-" + s[2]);
			s = advance();
		}
	}
}