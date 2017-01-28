package acme.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class FileUtil
{
	private static boolean	isInit	= false;

	// public static void initLibDirForJConfig(File libDir) {
	// init(libDir);
	// }
	private static void init(File libDir)
	{
		if (isInit)
			return;
		// File testFile = new File(libDir, "JConfig.zip");
		// if (!testFile.exists())
		// throw new RuntimeException("FileUtil: could not find
		// "+testFile.getAbsolutePath());
		// FileRegistry.initialize(libDir, 0);
		isInit = true;
	}

	// [matt@Amsterdam bin]$ df -k
	// Filesystem 1K-blocks Used Available Use% Mounted on
	// /dev/hda2 95105192 8972916 81301132 10% /
	// /dev/hda1 46636 28374 15854 65% /boot
	// none 248848 0 248848 0% /dev/shm
	// can be: either of these two
	// /dev/hdc3 219166856 381076 207652736 1% /data1
	// 219166856 381076 207652736 1% /data1
	static void parseDFKLine(String lastLine, long capFree[])
	{
		capFree[0] = 0;
		capFree[1] = 0;
		try
		{
			if (lastLine.charAt(0) == '/')
			{
				lastLine = lastLine.substring(lastLine.indexOf(' ')).trim();
			}
			else
			{
				lastLine = lastLine.trim();
			}
			String s[] = lastLine.split("(\\s)+");
			long l1 = Long.parseLong(s[0]);
			long l2 = Long.parseLong(s[2]);
			capFree[0] = l1;
			capFree[1] = l2;
		}
		catch (Exception e)
		{
			Util.log("parseDFKLine:" + e);
		}
	}

	static void parseDFKLine2(String lastLine, String freePerUsed[])
	{
		freePerUsed[0] = "";
		freePerUsed[1] = "";
		try
		{
			if (lastLine.charAt(0) == '/')
			{
				lastLine = lastLine.substring(lastLine.indexOf(' ')).trim();
			}
			else
			{
				lastLine = lastLine.trim();
			}
			String s[] = lastLine.split("(\\s)+");
			long l1 = Long.parseLong(s[0]);
			long l2 = Long.parseLong(s[2]);
			freePerUsed[0] = "" + (l2 / (1024 * 1024));
			if (s[3].endsWith("%"))
				s[3] = s[3].substring(0, s[3].length() - 1);
			freePerUsed[1] = s[3];
		}
		catch (Exception e)
		{
			Util.log("parseDFKLine2:" + e);
		}
	}

	static public File getFilesystemRoot(File f)
	{
		File parent = f.getParentFile();
		if (parent == null)
			return f;
		for (;;)
		{
			parent = f.getParentFile();
			if (parent.getParent() == null)
				return f;
			f = parent;
		}
	}

	static public int getPerUsed(File root)
	{
		String[] cf = new String[2];
		getFromDFK2(null, cf, root);
		return Integer.parseInt(cf[1]);
	}

	static public long[] getCapFreeNowInK(File root)
	{
		long[] cf = new long[2];
		getFromDFK(null, cf, root);
		return cf;
	}

	static public String[] getFreePerUsedSpace(File root)
	{
		String[] cf = new String[2];
		getFromDFK2(null, cf, root);
		return cf;
	}

	static private void getFromDFK(File f, long capFree[], File root)
	{
		String linuxDataRoot = "/" + root.getName(); // only works on unix
		capFree[0] = 0;
		capFree[1] = 0;
		if (Util.isWindows())
			return;
		String lines[] = Util.exec("df -k " + linuxDataRoot);
		if (lines.length == 0)
			return;
		String last = lines[lines.length - 1];
		// check for valid line
		if (last.charAt(0) == '/' || Character.isSpaceChar(last.charAt(0)))
		{
			parseDFKLine(last, capFree);
		}
		else
		{
			Util.log("invalid dfk line: " + last);
		}
	}

	static private void getFromDFK2(File f, String freePerFree[], File root)
	{
		String linuxDataRoot = "/" + root.getName(); // only works on unix
		freePerFree[0] = "";
		freePerFree[1] = "";
		if (Util.isWindows())
			return;
		String lines[] = Util.exec("df -k " + linuxDataRoot);
		if (lines.length == 0)
			return;
		String last = lines[lines.length - 1];
		// check for valid line
		if (last.charAt(0) == '/' || Character.isSpaceChar(last.charAt(0)))
		{
			parseDFKLine2(last, freePerFree);
		}
		else
		{
			Util.log("invalid dfk line: " + last);
		}
	}
	// static private void getFromDFK(File f, long capFree[]) {
	// capFree[0] = capFree[1] = 0;
	// FileReader fr = null;
	// BufferedReader br = null;
	// try {
	// File dfk = new File(f.getParent(), "__CAPACITY__");
	// fr = new FileReader(dfk);
	// br = new BufferedReader(fr);
	// String last = null;
	// for (;;) {
	// String s = br.readLine();
	// if (s == null)
	// break;
	// last = s.trim();
	// }
	// if (last.charAt(0) == '/') {
	// last = last.substring(last.indexOf(' ')).trim();
	// }
	// String s[] = last.split("(\\s)+");
	// long l1 = Long.parseLong(s[0])*1024;
	// long l2 = Long.parseLong(s[2])*1024;
	// capFree[0] = l1;
	// capFree[1] = l2;
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// try {
	// if (fr != null)
	// fr.close();
	// } catch (IOException e) {
	// }
	// }
	// }
	static class FreeSpaceInfo
	{
		long	staleMS;
		long	capFree[]	= new long[2];
	}
	static HashMap	infoMap	= new HashMap();

	// public static boolean deleteDir(File dir) {
	// if (dir.isDirectory()) {
	// String[] children = dir.list();
	// for (int i=0; i<children.length; i++) {
	// boolean success = deleteDir(new File(dir, children[i]));
	// if (!success) {
	// return false;
	// }
	// }
	// }
	//
	// // The directory is now empty so delete it
	// return dir.delete();
	// }
	static void doListFiles(List l, File root, String[] ext)
	{
		if (root == null)
			return;
		File files[] = root.listFiles();
		if (files == null)
			return;
		for (int i = 0; i < files.length; i++)
		{
			File f = files[i];
			if (!f.isFile())
				continue;
			boolean match = ext == null;
			for (int j = 0; !match && j < ext.length; j++)
				match = f.getName().endsWith(ext[j]) || f.getName().toLowerCase().endsWith(ext[j]);
			if (match)
				l.add(f);
		}
		for (int i = 0; i < files.length; i++)
		{
			File f = files[i];
			if (f.isDirectory())
				doListFiles(l, f, ext);
		}
	}

	static public List listFiles(File root, String[] extensions)
	{
		List l = new ArrayList();
		doListFiles(l, root, extensions);
		return l;
	}
	public static class UtilFilenameFilter implements FilenameFilter
	{
		String	in[];
		String	out[];

		String remove(String s, char ch)
		{
			if (s == null)
				return null;
			for (;;)
			{
				int i = s.indexOf('*');
				if (i == -1)
					break;
				s = s.substring(0, i) + s.substring(i + 1);
			}
			return s;
		}

		String[] parse(String s)
		{
			String sa[] = StringUtil.split(s, ',');
			for (int i = 0; i < sa.length; i++)
				sa[i] = remove(sa[i], '*').toLowerCase();
			return sa;
		}

		public UtilFilenameFilter(String ins, String outs)
		{
			in = ins != null ? parse(ins) : null;
			out = outs != null ? parse(outs) : null;
		}

		public boolean accept(File f, String name)
		{
			if (new File(f, name).isDirectory())
				return false;
			boolean ok = in == null ? true : false;
			name = name.toLowerCase();
			if (in != null)
				for (int i = 0; !ok && i < in.length; i++)
					ok = name.indexOf(in[i]) != -1;
			if (ok && out != null)
				for (int i = 0; ok && i < in.length; i++)
					ok = name.indexOf(in[i]) == -1;
			return ok;
		}
	};

	public static List listFiles(FilenameFilter filter, File dir, List l)
	{
		File f[] = dir.listFiles(filter);
		if (l == null)
			l = new ArrayList();
		if (f != null)
			for (int i = 0; i < f.length; i++)
				l.add(f[i]);
		return l;
	}

	public static List listFiles(File dir, String in, String out)
	{
		return listFiles(new UtilFilenameFilter(in, out), dir, null);
	}

	public static List listFiles(File dir, String in)
	{
		return listFiles(new UtilFilenameFilter(in, null), dir, null);
	}

	public static List listFiles(File dir)
	{
		return listFiles(new UtilFilenameFilter(null, null), dir, null);
	}

	public static List findFiles(File dir, String in)
	{
		return findFiles(dir, in, null);
	}

	public static List findFiles(File dir)
	{
		return findFiles(dir, null, null);
	}

	public static List findFiles(File dir, String in, String out)
	{
		FilenameFilter filter = new UtilFilenameFilter(in, out);
		List files = new ArrayList();
		LinkedList dirs = new LinkedList();
		dirs.add(dir);
		while (dirs.size() != 0)
		{
			File f = (File) dirs.removeFirst();
			File fl[] = f.listFiles();
			for (int i = 0; i < fl.length; i++)
				if (fl[i].isDirectory())
					dirs.add(fl[i]);
			listFiles(filter, f, files);
		}
		return files;
	}

	public static boolean deleteDirectoryRecursive(File f)
	{
		boolean b = deleteDirectoryContents(f);
		if (!b)
			return b;
		// Log.log("deleteDirectoryRecursive: "+f.getName());
		return f.delete();
	}

	public static boolean deleteDirectoryContents(File f)
	{
		if (!f.isDirectory())
			return false;
		File contents[] = f.listFiles();
		for (int i = 0; i < contents.length; i++)
		{
			boolean succ = false;
			if (contents[i].isDirectory())
				succ = deleteRecursive(contents[i]);
			else
				succ = contents[i].delete();
			if (!succ)
				return false;
		}
		return true;
	}

	public static boolean deleteRecursive(File f)
	{
		boolean succ = false;
		if (f.isDirectory())
		{
			File contents[] = f.listFiles();
			for (int i = 0; i < contents.length; i++)
			{
				if (contents[i].isDirectory())
					succ = deleteRecursive(contents[i]);
				else
				{
					// Log.log("deleteDirectoryRecursive:
					// "+contents[i].getName());
					succ = contents[i].delete();
				}
				if (!succ)
					return false;
			}
		}
		succ = f.delete();
		return succ;
	}

	public static boolean rename(File oldFile, File newFile, boolean journal) throws Exception
	{
		boolean good = false;
		if (newFile.exists() && journal)
		{
			Calendar c = Calendar.getInstance();
			String extension = ".deleted_" + StringUtil.int2(c.get(Calendar.MONTH) + 1) + StringUtil.int2(c.get(Calendar.DAY_OF_MONTH))
					+ StringUtil.int2(c.get(Calendar.YEAR) - 2000) + "_" + StringUtil.int2(c.get(Calendar.HOUR_OF_DAY)) + StringUtil.int2(c.get(Calendar.MINUTE));
			File backupFile = new File(newFile.getAbsolutePath() + extension);
			Log.aLog("Journaling file: " + newFile.getAbsolutePath());
			if (!newFile.renameTo(backupFile))
			{
				Log.aLog("Could not journal " + newFile.getAbsolutePath() + " to " + backupFile.getAbsolutePath());
				throw new Exception("Could not journal old file: " + newFile);
			}
		}
		if (newFile.exists())
			newFile.delete();
		try
		{
			good = oldFile.renameTo(newFile);
		}
		catch (Exception e)
		{
			good = false;
		}
		if (!good)
		{
			copyFile(oldFile, newFile);
			oldFile.delete();
		}
		return good;
	}

	public static boolean safeRename(File oldFile, File newFile)
	{
		boolean good = false;
		// Log.log("renamFile: "+oldFile.getAbsolutePath()+"
		// "+newFile.getAbsolutePath());
		try
		{
			if (oldFile != null && newFile != null)
				good = rename(oldFile, newFile, false);
		}
		catch (Exception e)
		{
			;
		}
		return good;
	}

	public static boolean safeDelete(File f)
	{
		boolean good = false;
		// if (f != null)
		// Log.log("safeDelete: "+f.getName());
		// else Log.log("================== safeDelete:null file");
		try
		{
			if (f != null)
				good = f.delete();
		}
		catch (Exception e)
		{
			;
		}
		return good;
	}

	public static void safeDeleteDirRecursive(File f)
	{
		try
		{
			if (f != null)
				deleteDirectoryRecursive(f);
		}
		catch (Exception e)
		{
			;
		}
	}

	public static void flushAndClose(FileOutputStream fos) throws IOException
	{
		fos.flush();
		fos.getFD().sync();
		fos.close();
	}

	public static void copyFile(File oldf, File newf) throws Exception
	{
		if (needsCopying(newf, oldf.length()))
		{
			FileInputStream fis = null;
			FileOutputStream fos = null;
			try
			{
				File parents = newf.getParentFile();
				if (parents != null && !parents.exists())
					parents.mkdirs();
				fis = new FileInputStream(oldf);
				fos = new FileOutputStream(newf);
				Util.copyStream(fis, fos);
			}
			catch (Exception e)
			{
				Log.aLog("Unable to copy " + oldf.getAbsolutePath() + " to " + newf.getAbsolutePath());
				throw e;
			}
			finally
			{
				Util.safeClose(fis);
				Util.safeClose(fos);
			}
		}
	}
	public static class FileRunnable
	{
		public void run(File f)
		{
		}
	}

	public static boolean needsCopying(File dest, Long srcSize)
	{
		try
		{
			if (dest.exists() && dest.isFile())
			{
				if (dest.length() == srcSize)
					return false;
			}
		}
		catch (Exception e)
		{
			Log.log("SmartCopy", e);
		}
		return true;
	}

	public static void forEachFile(File root, boolean recurse, boolean doDirs, boolean doFiles, FileRunnable fr)
	{
		if (root == null)
			return;
		File files[] = root.listFiles();
		if (files == null)
			return;
		for (int i = 0; i < files.length; i++)
		{
			File f = files[i];
			if (f.isDirectory())
			{
				if (doDirs)
					fr.run(f);
				if (recurse)
				{
					forEachFile(f, recurse, doDirs, doFiles, fr);
				}
			}
			else
			{
				if (doFiles)
				{
					fr.run(f);
				}
			}
		}
	}

	public static void main(String[] args)
	{
		// getFromDFK(new File("c:/__CAPACITY__"), new long[2]);
		// init(new File("c:/home/matt/metafusion/lib"));
		String s[] = Util.exec("cmd /c dir");
		Util.log("" + s);
		long l[] = new long[2];
		// /dev/hdc3 219166856 381076 207652736 1% /data1
		parseDFKLine("/dev/hdc3            219166856    381076 207652736   1% /data1", l);
		Util.log("" + l);
	}

	public static Long getSize(File dir)
	{
		Long size = 0L;
		File files[] = dir.listFiles();
		if (files == null)
			return size;
		for (int i = 0; i < files.length; i++)
		{
			File f = files[i];
			if (f.isDirectory())
			{
				size += getSize(f);
			}
			else
			{
				size += f.length();
			}
		}
		return size;
	}

	public static void copyDir(File src, File dest) throws Exception
	{
		class copyFR extends FileRunnable
		{
			String	src;
			String	dest;

			public copyFR(String src, String dest)
			{
				this.src = src;
				this.dest = dest;
			}

			@Override
			public void run(File f)
			{
				String tmp = f.getAbsolutePath().replace(src, dest);
				File tmpFile = new File(tmp);
				try
				{
					copyFile(f, tmpFile);
				}
				catch (Exception e)
				{
				}
			}
		}
		forEachFile(src, true, false, true, new copyFR(src.getAbsolutePath(), dest.getAbsolutePath()));
	}
}