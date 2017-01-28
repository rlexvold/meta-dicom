package acme.util;

import java.io.File;
import java.io.FilenameFilter;

public class FileNameFilter implements FilenameFilter
{
	public static final int BYDIR = 1;
	public static final int BYFILE = 2;
	boolean byDir = false;
	boolean byFile = false;
	String pre = null;
	String post = null;

	public FileNameFilter()
	{
		this.byDir = true;
		this.byFile = true;
	}

	public FileNameFilter(int type)
	{
		this.byDir = (type & BYDIR) != 0;
		this.byFile = (type & BYDIR) != 0;
	}

	public FileNameFilter(String post)
	{
		this.byDir = false;
		this.byFile = true;
		this.post = post;
	}

	public FileNameFilter(String pre, String post)
	{
		this.byDir = false;
		this.byFile = true;
		this.pre = pre;
		this.post = post;
	}

	public boolean accept(File f, String name)
	{
		boolean isDir = new File(f, name).isDirectory();
		if (byDir)
			return isDir;
		else if (isDir) return false;
		if (!byFile) return false;
		name = name.toLowerCase();
		if (pre != null && !name.startsWith(pre)) return false;
		if (post != null && !name.endsWith(post)) return false;
		return true;
	}
}