package acme.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtil
{
	private static void log(String s)
	{
		System.out.println(s);
	}
	static private final int SIZE = 8192;

	static private void add(byte buffer[], ZipOutputStream zos, File f, String path) throws Exception
	{
		if (f.isDirectory())
		{
			path = path + f.getName() + "/";
			ZipEntry entry = new ZipEntry(path);
			zos.putNextEntry(entry);
			File files[] = MSJavaHack.get().listFiles(f);
			for (int i = 0; i < files.length; i++)
				add(buffer, zos, files[i], path);
		} else
		{
			ZipEntry entry = new ZipEntry(path + f.getName());
			zos.putNextEntry(entry);
			InputStream is = new BufferedInputStream(new FileInputStream(f));
			for (;;)
			{
				int cnt = is.read(buffer);
				if (cnt == -1) break;
				zos.write(buffer, 0, cnt);
			}
			// zos.closeEntry();
			is.close();
		}
	}

	static public boolean zip(File root, File zipFile)
	{
		byte buffer[] = new byte[SIZE];
		FileOutputStream dest = null;
		ZipOutputStream zos = null;
		boolean good = false;
		try
		{
			// if (zipFile.exists())
			// zipFile.delete();
			dest = new FileOutputStream(zipFile);
			zos = new ZipOutputStream(new BufferedOutputStream(dest));
			add(buffer, zos, root, "");
			zos.close();
			dest.close();
			good = true;
		}
		catch (Exception e)
		{
			try
			{
				zos.close();
			}
			catch (Exception ex)
			{
			}
			try
			{
				dest.close();
			}
			catch (Exception ex)
			{
			}
			e.printStackTrace();
		}
		return good;
	}

	static private void extractDir(File f)
	{
		if (f.exists())
		{
			// log("dir exists "+f);
			if (!f.isDirectory()) throw new RuntimeException("extractDir fail " + f);
		} else
		{
			boolean b = f.mkdir();
			if (!b) throw new RuntimeException("extractDir fail " + f);
		}
	}

	static private void extractFile(byte[] buffer, File f, InputStream in, long size)
	{
		OutputStream out = null;
		try
		{
			if (f.exists()) f.delete();
			out = new FileOutputStream(f);
			for (;;)
			{
				int cnt = in.read(buffer);
				if (cnt == -1) break;
				out.write(buffer, 0, cnt);
			}
			out.close();
		}
		catch (IOException e)
		{
			try
			{
				out.close();
			}
			catch (Exception ex)
			{
			}
			e.printStackTrace();
			throw new RuntimeException("extractFile failed " + f + ":" + e);
		}
	}

	static public boolean unzip(File zipFile, File root)
	{
		byte buffer[] = new byte[SIZE];
		boolean good = false;
		if (!zipFile.exists()) throw new RuntimeException("unzip zipFile fail " + zipFile);
		if (!root.isDirectory()) throw new RuntimeException("extractDir root fail " + root);
		ZipFile zf = null;
		try
		{
			zf = new ZipFile(zipFile);
			Enumeration e = zf.entries();
			while (e.hasMoreElements())
			{
				ZipEntry zipEntry = (ZipEntry) e.nextElement();
				if (zipEntry.isDirectory())
				{
					extractDir(new File(root, zipEntry.getName()));
				} else
				{
					InputStream is = zf.getInputStream(zipEntry);
					try
					{
						extractFile(buffer, new File(root, zipEntry.getName()), is, zipEntry.getSize());
					}
					finally
					{
						is.close();
					}
				}
			}
			zf.close();
			good = true;
		}
		catch (Exception e)
		{
			try
			{
				zf.close();
			}
			catch (Exception ex)
			{
			}
			e.printStackTrace();
		}
		return good;
	}
	// boolean b = zip(new File("c:\\projects"), new File("c:\\test.zip"));
	// boolean bb = unzip(new File("c:\\test.zip"), new File("c:\\tmp"));
}
