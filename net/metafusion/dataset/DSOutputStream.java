package net.metafusion.dataset;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.metafusion.util.DSList;
import net.metafusion.util.FragmentList;
import net.metafusion.util.Tag;
import net.metafusion.util.UID;
import net.metafusion.util.VR;
import acme.util.Log;
import acme.util.NullOutputStream;
import acme.util.Util;

public class DSOutputStream extends FilterOutputStream
{
	static void log(String s)
	{
		Log.vvlog(s);
	}
	HashMap					offsetMap		= null;
	UID						syntax			= UID.ImplicitVRLittleEndian;
	UID						nextGroupSyntax	= null;
	boolean					explicit		= false;
	boolean					le				= true;
	boolean					be				= false;
	boolean					outputGroupTags	= true;
	byte					temp[]			= new byte[64];
	ByteArrayOutputStream	bos				= null;
	OutputStream			savedOUT;
	int						currentGroup	= -1;

	void startGroup(int group) throws Exception
	{
		Util.Assert(currentGroup == -1);
		if (nextGroupSyntax != null)
		{
			setSyntax(nextGroupSyntax);
			nextGroupSyntax = null;
		}
		if (group != 0 && group != 2)
			return;
		currentGroup = group;
		savedOUT = super.out;
		if (bos == null)
		{
			// log("alloc "+8192);
			bos = new ByteArrayOutputStream(8192);
		}
		else
			bos.reset();
		super.out = bos;
	}

	void endGroup() throws Exception
	{
		if (currentGroup == -1)
			return;
		super.out = savedOUT;
		Tag t = Tag.get(currentGroup, 0);
		writeTagValue(t, new Integer(bos.size()));
		bos.writeTo(super.out);
		currentGroup = -1;
	}

	void setSyntax(UID uid)
	{
		syntax = uid;
		if (uid == UID.ImplicitVRLittleEndian)
		{
			explicit = false;
			le = true;
			be = false;
		}
		else if (uid == UID.ExplicitVRLittleEndian)
		{
			explicit = true;
			le = true;
			be = false;
		}
		else if (uid == UID.DeflatedExplicitVRLittleEndian)
		{
			// what is this????
			explicit = true;
			le = true;
			be = false;
		}
		else if (uid == UID.ExplicitVRBigEndian)
		{
			explicit = true;
			le = false;
			be = true;
		}
		else if (!uid.getUID().startsWith("1.2.840.10008.1.2"))
		{
			throw new RuntimeException("unxpected UID for dataset " + uid);
		}
	}

	public DSOutputStream(HashMap offsetMap, UID uid) throws Exception
	{
		super(new NullOutputStream());
		this.offsetMap = offsetMap;
		outputGroupTags = false;
		setSyntax(uid);
		// acme.util.Stats.inc("new.DSOutputStream");
	}

	public DSOutputStream(OutputStream os, UID uid) throws Exception
	{
		super(os);
		setSyntax(uid);
		// acme.util.Stats.inc("new.DSOutputStream");
	}

	public DSOutputStream(OutputStream os) throws Exception
	{
		super(os);
		// acme.util.Stats.inc("new.DSOutputStream");
	}
	int	offset	= 0;

	public void write(int b) throws IOException
	{
		offset++;
		out.write(b);
	}

	public void write(byte b[], int off, int len) throws IOException
	{
		offset += len;
		out.write(b, off, len);
	}

	void writeShort(short s) throws Exception
	{
		Util.encodeShort(be, s, temp, 0);
		write(temp, 0, 2);
	}

	void writeInt(int i) throws Exception
	{
		Util.encodeInt(be, i, temp, 0);
		write(temp, 0, 4);
	}

	void writeLong(long l) throws Exception
	{
		Util.encodeLong(be, l, temp, 0);
		write(temp, 0, 8);
	}

	int sizeString(String s)
	{
		return ((s.getBytes().length + 1) & 1);
	}

	void writeString(String s) throws Exception
	{
		byte b[] = s.getBytes();
		write(b);
		if ((b.length & 1) != 0)
			write((byte) ' ');
	}

	public void writeFloat(float f) throws Exception
	{
		writeInt(Float.floatToIntBits(f));
	}

	public void writeDouble(double d) throws Exception
	{
		writeLong(Double.doubleToLongBits(d));
	}

	void writeLen(int size, int len) throws Exception
	{
		if ((len != -1 && (len & 1) == 1) || len < -1)
			throw new Exception("bad len " + size);
		if (size == 2)
			writeShort((short) len);
		else if (size == 4)
			writeInt(len);
		else
			throw new Exception("bad len " + size);
	}

	/*
	 * void writeSEQ(DS dss) throws Exception { DSOutputStream dos = new DSOutputStream(this, syntax);
	 * dos.outputGroupTags = false; Iterator iter = dss.getEntrySet().iterator(); while (iter.hasNext()) { Map.Entry
	 * entry = (Map.Entry)iter.next(); DS ds2 = (DS)entry.getValue(); dos.writeShort((short)0xFFFE);
	 * dos.writeShort((short)0xE000); dos.writeInt(-1); dos.writeDS(ds2); dos.writeShort((short)0xFFFE);
	 * dos.writeShort((short)0xE00D); dos.writeInt(0); } dos.outputGroupTags = true; }
	 */
	void writeTagValue(Tag t, Object v) throws Exception
	{
		boolean savedExplicit = explicit;
		boolean savedle = le;
		boolean savedbe = be;
		// group two always explicit little endian
		if (t.getGroup() == 2)
		{
			explicit = true;
			le = true;
			be = false;
		}
		// log(""+offset+": writeTagValue "+t);
		VR vr = t.getVR();
		short group = (short) t.getGroup();
		short id = (short) t.getID();
		writeShort(group);
		writeShort(id);
		if (group == 0x1111)
			;// Log("xxxx");
		if (id == 0x10)
			;// Log("XXX");
		int lenlen = 0;
		if (explicit)
		{
			writeString(vr.getName());
			if (vr.shortLen())
			{
				lenlen = 2;
			}
			else
			{
				writeShort((short) 0);
				lenlen = 4;
			}
		}
		else
		{
			lenlen = 4;
		}
		Class bt = vr.getBaseType();
		// log(""+t+":"+(v!=null?v.toString():"null"));
		if (v == null)
		{
			writeLen(lenlen, 0);
		}
		else if (bt == String.class)
		{
			String s = (String) v;
			// log("writeString ["+s.length()+"]"+s);
			byte[] b = s.getBytes();
			boolean odd = false;
			if ((b.length & 1) == 1)
			{
				odd = true;
			}
			int newLength = b.length + (odd ? 1 : 0);
			writeLen(lenlen, newLength);
			write(b);
			if (vr.equals(VR.UI) && newLength > 64)
			{
				Log.log("UID longer than 64 bytes!!!!", new Exception("Tag: " + t.toString() + "  Value: " + s + "  exceeds DICOM limit of 64 bytes"));
			}
			if (odd)
				if (vr.equals(VR.UI))
					write(0);
				else
					write(' ');
		}
		else if (bt == short.class)
		{
			writeLen(lenlen, 2);
			if (v instanceof Short)
				writeShort(((Short) v).shortValue());
			else
				writeShort(((Integer) v).shortValue());
		}
		else if (bt == int.class)
		{
			writeLen(lenlen, 4);
			writeInt(((Integer) v).intValue());
		}
		else if (bt == float.class)
		{
			writeLen(lenlen, 4);
			writeFloat(((Float) v).floatValue());
		}
		else if (bt == double.class)
		{
			writeLen(lenlen, 8);
			writeDouble(((Double) v).doubleValue());
		}
		else if (v instanceof byte[])
		{
			byte[] b = (byte[]) v;
			boolean odd = false;
			if ((b.length & 1) == 1)
			{
				odd = true;
			}
			int newLength = b.length + (odd ? 1 : 0);
			writeLen(lenlen, newLength);
			write(b);
			if (odd)
				write(0);
		}
		else if (v instanceof FragmentList)
		{
			writeLen(lenlen, -1);
			Iterator iter = ((List) v).iterator();
			while (iter.hasNext())
			{
				byte b[] = (byte[]) iter.next();
				writeShort((short) 0x0FFFE);
				writeShort((short) 0x0E000);
				writeInt(b.length + ((b.length & 1) == 1 ? 1 : 0));
				write(b);
				if ((b.length & 1) == 1)
					write(0);
			}
			writeShort((short) 0x0FFFE);
			writeShort((short) 0x0E0DD);
			writeInt(0);
		}
		else if (v instanceof DSList)
		{
			boolean savedOutputGroupTags = outputGroupTags;
			outputGroupTags = false;
			writeLen(lenlen, -1);
			Iterator iter = ((List) v).iterator();
			while (iter.hasNext())
			{
				DS ds2 = (DS) iter.next();
				if (offsetMap != null)
					offsetMap.put(ds2, new Integer(offset));
				writeShort((short) 0x0FFFE);
				writeShort((short) 0x0E000);
				writeInt(-1);
				writeDS(ds2);
				writeShort((short) 0x0FFFE);
				writeShort((short) 0x0E00D);
				writeInt(0);
			}
			writeShort((short) 0x0FFFE);
			writeShort((short) 0x0E0DD);
			writeInt(0);
			outputGroupTags = savedOutputGroupTags;
		}
		else
		{
			throw new Exception("bad VR base type " + bt);
		}
		// group two always explicit little endian
		if (t.getGroup() == 2)
		{
			explicit = savedExplicit;
			le = savedle;
			be = savedbe;
		}
	}

	void writeDS(DS ds) throws Exception
	{
		// log("writeDS exp="+explicit+" le="+le);
		Iterator<Map.Entry<Tag, Object>> iter = ds.getEntrySet().iterator();
		Tag last = null;
		while (iter.hasNext())
		{
			Map.Entry<Tag, Object> entry = iter.next();
			Tag t = entry.getKey();
			Object v = entry.getValue();
			// if (v == null)
			// Log.log("Found tag: " + t.toString() + " value: null");
			// else
			// Log.log("Found tag: " + t.toString() + " value: " + v.toString());
			if (t == Tag.TransferSyntaxUID)
			{
				nextGroupSyntax = UID.get((String) v);
				// log("writeDS nextGroupSyntax = "+nextGroupSyntax);
			}
			if (outputGroupTags && (last == null || t.getGroup() != last.getGroup()))
			{
				if (last != null)
					endGroup();
				startGroup(t.getGroup());
			}
			if (t.getID() != 0)
			{
				writeTagValue(t, v);
			}
			last = t;
		}
		if (outputGroupTags)
			endGroup();
	}

	static public void writeDicomFile(DS ds, File f)
	{
		FileOutputStream fos = null;
		try
		{
			File parent = f.getParentFile();
			if (!parent.exists())
				parent.mkdirs();
			fos = new FileOutputStream(f);
			fos.write(new byte[128]);
			fos.write("DICM".getBytes());
			ds.writeTo(fos, UID.ExplicitVRLittleEndian);
			fos.close();
			fos = null;
		}
		catch (Exception e)
		{
			Log.log("writeFile caught ", e);
		}
		finally
		{
			Util.safeClose(fos);
		}
	}

	static public void writeRawDicomFile(DS ds, File f)
	{
		FileOutputStream fos = null;
		try
		{
			fos = new FileOutputStream(f);
			ds.writeTo(fos, UID.ImplicitVRLittleEndian);
			fos.close();
			fos = null;
		}
		catch (Exception e)
		{
			Log.log("writeFile caught ", e);
		}
		finally
		{
			Util.safeClose(fos);
		}
	}
}
