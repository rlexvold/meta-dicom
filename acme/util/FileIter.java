package acme.util;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class FileIter implements Iterator
{
	List dirList = new LinkedList();
	File files[] = null;
	int fi = 0;
	boolean inPlace = false;
	File file = null;
	FileNameFilter filter;

	public FileIter(File root, FileNameFilter filter)
	{
		this.filter = filter;
		dirList.add(root);
	}

	void advance()
	{
		if (inPlace) return;
		inPlace = true;
		file = null;
		for (;;)
		{
			if (fi < files.length)
			{
				file = files[fi++];
				return;
			} else if (dirList.size() > 0)
			{
				File f = (File) dirList.remove(0);
				fi = 0;
				files = f.listFiles(filter);
				File dirs[] = f.listFiles(new FileNameFilter(FileNameFilter.BYDIR));
				for (int i = 0; i < dirs.length; i++)
					dirList.add(dirs[i]);
			} else
			{
				return;
			}
		}
	}

	public boolean hasNext()
	{
		advance();
		return file != null;
	}

	public Object next()
	{
		advance();
		inPlace = false;
		return file;
	}

	public void remove()
	{
		throw new RuntimeException("not implemented");
	}
};
