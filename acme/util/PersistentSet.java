package acme.util;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class PersistentSet
{
	File root;

	public PersistentSet(File root)
	{
		this.root = root;
		sync();
	}
	SortedSet set = new TreeSet();

	File getFile(String key)
	{
		return new File(root, key + ".elm");
	}

	String getKey(File f)
	{
		String name = f.getName();
		if (name.endsWith(".elm"))
			return name.substring(0, name.length() - 4);
		else return null;
	}

	synchronized public String first()
	{
		return (String) set.first();
	}

	synchronized public String last()
	{
		return (String) set.last();
	}

	synchronized public boolean isEmpty()
	{
		return set.isEmpty();
	}

	synchronized public int size()
	{
		return set.size();
	}

	synchronized public void sync()
	{
		set.clear();
		File files[] = root.listFiles();
		for (int i = 0; i < files.length; i++)
		{
			String key = getKey(files[i]);
			if (key != null) set.add(key);
		}
	}

	synchronized public void clear()
	{
		set.clear();
		File files[] = root.listFiles();
		for (int i = 0; i < files.length; i++)
		{
			if (getKey(files[i]) != null)
			{
				files[i].delete();
			}
		}
	}

	synchronized public boolean contains(String s)
	{
		return set.contains(s);
	}

	synchronized public boolean add(String s) throws IOException
	{
		for (int i = 0; i < s.length(); i++)
		{
			char ch = s.charAt(i);
			if (((int) ch < 128 && Character.isJavaIdentifierPart(ch)) || ch == '-') throw new RuntimeException("invalid key " + s);
		}
		if (set.contains(s)) return false;
		File f = getFile(s);
		if (!f.exists()) if (!f.createNewFile()) throw new RuntimeException("could not create " + f.getName());
		set.add(s);
		return true;
	}

	synchronized public void remove(String s)
	{
		set.remove(s);
		File f = getFile(s);
		if (f.exists()) if (!f.delete()) throw new RuntimeException("could not delete " + f.getName());
	}

	synchronized public String[] toArray()
	{
		String s[] = new String[set.size()];
		set.toArray(s);
		return s;
	}

	synchronized public Iterator iterator()
	{
		return set.iterator();
	}
}
