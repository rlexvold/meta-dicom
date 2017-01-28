package acme.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MSJavaHack
{
	static private MSJavaHack hack = new MSJavaHack();

	static public MSJavaHack get()
	{
		return hack;
	}

	static public void set(MSJavaHack hack)
	{
		MSJavaHack.hack = hack;
	}

	public File[] listFiles(File f)
	{
		// was f.listFiles();
		String s[] = f.list();
		if (s == null) return new File[0];
		int count = s.length;
		for (String element : s)
			if (element.equals(".") || element.equals("..")) count--;
		File fs[] = new File[count];
		int index = 0;
		for (String element : s)
			if (!(element.equals(".") || element.equals(".."))) fs[index++] = new File(f, element);
		return fs;
	}
	static long tempCount = 0;

	synchronized public File createTempFile(String prefix, String suffix, File parent)
	{
		// was f = File.createTempFile("metadd",".zip");
		File f;
		for (;;)
		{
			long count = System.currentTimeMillis();
			if (count > tempCount)
				tempCount = count;
			else tempCount = tempCount + 1;
			f = new File(parent, prefix + tempCount + suffix);
			if (!f.exists()) break;
		}
		try
		{
			FileOutputStream fos = new FileOutputStream(f);
			fos.close();
		}
		catch (IOException e)
		{
			Log.log("could not createTempFile", e);
			throw new RuntimeException(e);
		}
		Log.log("create temp file:" + f.getName());
		return f;
	}

	synchronized public File createTempDir(String prefix, File parent)
	{
		// was f = File.createTempFile("metadd",".zip");
		File f;
		for (;;)
		{
			long count = System.currentTimeMillis();
			if (count > tempCount)
				tempCount = count;
			else tempCount = tempCount + 1;
			f = new File(parent, prefix + tempCount);
			if (!f.exists() && f.mkdir()) break;
			count++;
		}
		Log.log("create temp dir:" + f.getName());
		return f;
	}

	public File createTempFile(String prefix, String suffix)
	{
		return createTempFile(prefix, suffix, new File("."));
	}
}