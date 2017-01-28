package acme.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import acme.util.Util;
import acme.util.XML;

class SSObject
{
	SSObject(long parentId, long id, File f)
	{
		this.id = id;
		this.m = m;
		this.d = d;
	}
	long parentID;
	long id;
	File m;
	File d;
	XML meta;
	FileInputStream fis;

	public Object getMeta() throws Exception
	{
		if (meta == null) meta = new XML(m);
		return meta;
	}

	public long getSize() throws Exception
	{
		return d.length();
	}

	public InputStream getInputStream() throws IOException
	{
		if (fis == null) fis = new FileInputStream(d);
		return fis;
	}

	public void close()
	{
		Util.safeClose(fis);
		fis = null;
	}

	public long getID()
	{
		return id;
	}
}
