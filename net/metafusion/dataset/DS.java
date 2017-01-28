package net.metafusion.dataset;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import net.metafusion.util.UID;
import acme.util.Util;

public class DS implements Serializable
{
	int imageOffset = 0;
	int imageSize = 0;
	TreeMap map = new TreeMap();

	public DS()
	{
		// acme.util.Stats.inc("new.DS");
	}

	public DS(File f)
	{
		DSInputStream.readFile(f, this, null, null, false);
	}

	public DS(InputStream is, net.metafusion.util.UID syntax) throws Exception
	{
		DSInputStream.readFrom(is, syntax, this);
	}

	// public DS(InputStream is) throws Exception {
	// DSInputStream.readFrom(is, this);
	// }
	public HashMap calculateOffsets(UID syntax) throws Exception
	{
		HashMap offsetMap = new HashMap();
		DSOutputStream dos = new DSOutputStream(offsetMap, syntax);
		dos.writeDS(this);
		return offsetMap;
	}

	public void clear()
	{
		map.clear();
	}

	public boolean contains(net.metafusion.util.Tag t)
	{
		return map.containsKey(t);
	}

	public boolean containsValue(net.metafusion.util.Tag t)
	{
		return map.get(t) != null;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof DS)) return false;
		Iterator i1 = getTags().iterator();
		Iterator i2 = ((DS) o).getTags().iterator();
		for (;;)
		{
			boolean b = i1.hasNext();
			if (b != i2.hasNext()) return false;
			if (!b) return true;
			net.metafusion.util.Tag t1 = (net.metafusion.util.Tag) i1.next();
			net.metafusion.util.Tag t2 = (net.metafusion.util.Tag) i2.next();
			if (!t1.equals(t2)) return false;
			Object v1 = get(t1);
			Object v2 = ((DS) o).get(t2);
			if (v1 instanceof String && ((String) v1).length() == 0) v1 = null;
			if (v2 instanceof String && ((String) v2).length() == 0) v2 = null;
			if (v1 == null && v2 == null) continue;
			if (v1 == null || v2 == null) return false;
			if (!v1.equals(v2)) return false;
		}
	}

	public Object get(net.metafusion.util.Tag t)
	{
		return map.get(t);
	}

	public Set getEntrySet()
	{
		return map.entrySet();
	}

	public int getImageOffset()
	{
		return imageOffset;
	}

	public int getImageSize()
	{
		return imageSize;
	}

	public int getInt(net.metafusion.util.Tag t)
	{
		Object o = map.get(t);
		return o != null ? ((Integer) o).intValue() : -1;
	}

	public short getShort(net.metafusion.util.Tag t)
	{
		Object o = map.get(t);
		return o != null ? ((Short) o).shortValue() : -1;
	}

	public String getString(net.metafusion.util.Tag t)
	{
		return (String) map.get(t);
	}

	public String getString(net.metafusion.util.Tag t, String def)
	{
		String s = (String) map.get(t);
		if (s == null || s.length() == 0) return def;
		return s;
	}

	public Set getTags()
	{
		return map.keySet();
	}

	public int getUnsignedShort(net.metafusion.util.Tag t)
	{
		Object o = map.get(t);
		return o != null ? (((Short) o).shortValue() & 0x0000FFFF) : -1;
	}

	public boolean hasNestedDS()
	{
		Iterator iter = map.values().iterator();
		while (iter.hasNext())
			if (iter.next() instanceof List) return true;
		return false;
	}

	public void put(net.metafusion.util.Tag t, Object o)
	{
		map.put(t, o);
	}

	public void put(net.metafusion.util.Tag t, UID uid)
	{
		map.put(t, uid != null ? uid.getUID() : null);
	}

	public void putInt(net.metafusion.util.Tag t, int i)
	{
		map.put(t, new Integer(i));
	}

	public void putShort(net.metafusion.util.Tag t, int s)
	{
		map.put(t, new Short((short) s));
	}

	public void putString(net.metafusion.util.Tag t, String s)
	{
		map.put(t, s);
	}

	public void remove(net.metafusion.util.Tag t)
	{
		map.remove(t);
	}

	public void setImageOffset(int imageOffset)
	{
		this.imageOffset = imageOffset;
	}

	public void setImageSize(int imageSize)
	{
		this.imageSize = imageSize;
	}

	@Override
	public String toString()
	{
		return toString("\n");
	}

	public String toString(String sep)
	{
		StringBuffer sb = new StringBuffer();
		sb.append("DS[" + sep);
		Iterator iter = getTags().iterator();
		while (iter.hasNext())
		{
			net.metafusion.util.Tag t = (net.metafusion.util.Tag) iter.next();
			Object v = get(t);
			sb.append(t.toString());
			sb.append(":");
			if (v != null && v instanceof Short)
				sb.append("" + (((Short) v).shortValue() & 0x0000FFFF));
			else sb.append(v != null ? v.toString() : "NULL");
			sb.append(sep);
		}
		sb.append("DS]");
		return sb.toString();
	}

	public void writeTo(File f) throws Exception
	{
		OutputStream os = new FileOutputStream(f);
		try
		{
			DSOutputStream dos = new DSOutputStream(os);
			dos.writeDS(this);
		}
		finally
		{
			Util.safeClose(os);
		}
	}

	public void writeTo(File f, net.metafusion.util.UID syntax) throws Exception
	{
		OutputStream os = new FileOutputStream(f);
		try
		{
			DSOutputStream dos = new DSOutputStream(os, syntax);
			dos.writeDS(this);
		}
		finally
		{
			Util.safeClose(os);
		}
	}

	public void writeTo(File f, net.metafusion.util.UID syntax, boolean append) throws Exception
	{
		OutputStream os = new FileOutputStream(f.getAbsolutePath(), append);
		try
		{
			DSOutputStream dos = new DSOutputStream(os, syntax);
			dos.writeDS(this);
		}
		finally
		{
			Util.safeClose(os);
		}
	}

	public void writeTo(OutputStream os) throws Exception
	{
		DSOutputStream dos = new DSOutputStream(os);
		dos.writeDS(this);
	}

	public void writeTo(OutputStream os, net.metafusion.util.UID syntax) throws Exception
	{
		DSOutputStream dos = new DSOutputStream(os, syntax);
		dos.writeDS(this);
	}
}
