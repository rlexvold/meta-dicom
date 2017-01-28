package net.metafusion.dataset;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import net.metafusion.util.Tag;
import net.metafusion.util.UID;
import acme.storage.SSInputStream;

public class DSViewDef
{
	Field	imageOffsetField	= null;
	Field	imageSizeField		= null;
	Class	k;
	Tag		tag[];
	HashMap	tagMap				= new HashMap();

	public DSViewDef(Class k, Tag tag[])
	{
		this(k, tag, null, null);
	}

	public DSViewDef(Class k, Tag tag[], String imageOffsetFieldName, String imageSizeFieldName)
	{
		this.k = k;
		this.tag = tag;
		if (imageOffsetFieldName != null)
			imageOffsetField = getField(k, imageOffsetFieldName);
		if (imageSizeFieldName != null)
			imageSizeField = getField(k, imageSizeFieldName);
		for (int i = 0; i < tag.length; i++)
		{
			try
			{
				Tag t = tag[i];
				// Util.log(""+t);
				Field f = getField(k, t.getKey());
				tagMap.put(t, f);
				if (f.getType() != t.getVR().getBaseType())
					throw new Exception("wrong type for " + tag);
			}
			catch (Exception e)
			{
				throw new RuntimeException("addLoadTags " + tag[i] + "caught " + e);
			}
		}
	}

	public Field getField(Tag t)
	{
		return (Field) tagMap.get(t);
	}

	public Object load(DS ds, Object o) throws Exception
	{
		if (o == null)
			o = newInstance();
		for (int i = 0; i < tag.length; i++)
			if (ds.contains(tag[i]))
			{
				Field f = (Field) tagMap.get(tag[i]);
				Object v = ds.get(tag[i]);
				set(o, f, v);
			}
		if (imageOffsetField != null)
			set(o, imageOffsetField, new Integer(ds.getImageOffset()));
		if (imageSizeField != null)
			set(o, imageSizeField, new Integer(ds.getImageSize()));
		return o;
	}

	public Object load(File f) throws Exception
	{
		DS ds = DSInputStream.readFile(f, tagMap);
		return load(ds, null);
	}

	public Object[] loadReturnDS(File f) throws Exception
	{
		HashMap empty = null;
		DS ds = DSInputStream.readFile(f, empty);
		Object tmp = load(ds, null);
		Object[] returnObj = new Object[] { tmp, ds };
		return returnObj;
	}

	public Object load(SSInputStream is, UID xferSyntax) throws Exception
	{
		DS ds = DSInputStream.readFrom(is, xferSyntax, tagMap);
		return load(ds, null);
	}

	public Object[] loadReturnDS(SSInputStream is, UID xferSyntax) throws Exception
	{
		HashMap empty = null;
		DS ds = DSInputStream.readFrom(is, xferSyntax, empty);
		Object tmp = load(ds, null);
		Object[] returnObj = new Object[] { tmp, ds };
		return returnObj;
	}

	public Object newInstance() throws Exception
	{
		Object o = k.newInstance();
		return o;
	}

	public void set(Object o, Field f, Object value) throws Exception
	{
		f.set(o, value);
	}

	public DS store(Object o, DS ds) throws Exception
	{
		if (ds == null)
			ds = null;
		for (int i = 0; i < tag.length; i++)
		{
			Tag t = tag[i];
			Field f = (Field) tagMap.get(t);
			Object v = f.get(o);
			ds.put(t, v);
		}
		return ds;
	}

	private Field getField(Class tk, String name)
	{
		Field f = null;
		while (f == null && tk != null)
			try
			{
				f = tk.getDeclaredField(name);
			}
			catch (Exception e)
			{
				tk = tk.getSuperclass();
			}
		return f;
	}

	boolean contains(Tag t)
	{
		return tagMap.containsKey(t);
	}
}
