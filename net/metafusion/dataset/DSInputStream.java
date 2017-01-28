package net.metafusion.dataset;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.List;

import net.metafusion.util.DSList;
import net.metafusion.util.FragmentList;
import net.metafusion.util.Tag;
import net.metafusion.util.UID;
import net.metafusion.util.VR;
import acme.storage.SSStore;
import acme.util.Log;
import acme.util.Util;

public class DSInputStream extends FilterInputStream
{
	static void log(String s)
	{
		Log.log(s);
	}
	public static boolean	supportMixedExplicitImplicitHack	= true;
	private List			dumpList;

	public List getDumpList()
	{
		return dumpList;
	}

	public void setDumpList(List dumpList)
	{
		this.dumpList = dumpList;
	}
	private HashMap	filter;

	public void setFilter(HashMap filter)
	{
		this.filter = filter;
	}

	public DSInputStream(InputStream is)
	{
		super(is);
		this.is = is;
		// acme.util.Stats.inc("new.DSInputStream");
	}

	public DSInputStream(InputStream is, UID syntax)
	{
		super(is);
		this.is = is;
		this.syntax = syntax;
		// acme.util.Stats.inc("new.DSInputStream");
	}
	UID	syntax			= UID.ImplicitVRLittleEndian;
	UID	nextGroupSyntax	= null;
	int	lastGroup		= 0;

	public boolean getKeepImages()
	{
		return keepImages;
	}

	public void setKeepImages(boolean keepImages)
	{
		this.keepImages = keepImages;
	}
	boolean	keepImages			= false;
	boolean	explicit			= true;
	boolean	le					= true;
	boolean	be					= false;
	byte	temp[]				= new byte[32];
	int		pos					= 0;
	boolean	disableSkipValues	= false;
	int		imageOffset			= 0;
	int		imageSize			= 0;

	public int GetImageOffset()
	{
		return imageOffset;
	}

	public int GetImageSize()
	{
		return imageSize;
	}

	public void skip(int len) throws IOException
	{
		pos += len;
		super.skip(len);
	}

	@Override
	public int read() throws IOException
	{
		pos++;
		return super.read();
	}

	public final void readFully(byte b[]) throws IOException
	{
		readFully(b, 0, b.length);
	}

	public final void readFully(byte b[], int off, int len) throws IOException
	{
		int n = 0;
		while (n < len)
		{
			int count = read(b, off + n, len - n);
			if (count < 0)
				throw new EOFException();
			n += count;
		}
		pos += len;
	}
	boolean	eof	= false;

	public boolean eof()
	{
		return eof;
	}

	short readShortAndSetEOF() throws Exception
	{
		if (eof)
			return 0;
		int c = read();
		if (c == -1)
		{
			eof = true;
			return 0;
		}
		temp[0] = (byte) c;
		c = read();
		if (c == -1)
		{
			eof = true;
			return 0;
		}
		temp[1] = (byte) c;
		return Util.decodeShort(be, temp, 0);
	}

	short readShort() throws Exception
	{
		readFully(temp, 0, 2);
		return Util.decodeShort(be, temp, 0);
	}

	int readInt() throws Exception
	{
		readFully(temp, 0, 4);
		return Util.decodeInt(be, temp, 0);
	}

	int readInt(byte b[]) throws Exception
	{
		return Util.decodeInt(be, b, 0);
	}

	long readLong() throws Exception
	{
		readFully(temp, 0, 8);
		return Util.decodeLong(be, temp, 0);
	}

	String readString(int len) throws Exception
	{
		byte b[] = new byte[len];
		readFully(b);
		while (--len >= 0)
			if (b[len] != 0 && b[len] != (byte) ' ')
				break;
		len++;
		String s = new String(b, 0, len);
		return s;
	}

	public float readFloat() throws Exception
	{
		int i = readInt();
		return Float.intBitsToFloat(i); // endianness???
	}

	public double readDouble() throws Exception
	{
		long l = readLong();
		double d = 0.006230;
		long dl = Double.doubleToLongBits(d);
		if (dl == 0)
			;
		return Double.longBitsToDouble(l);
	}

	void skipValue(int size) throws Exception
	{
		skip(size);
	}

	// what do about VM? (ignored)
	public Object readValue(VR vr, int size) throws Exception
	{
		// log("readValue "+vr.toString()+" "+size);
		Object o = null;
		Class bt = vr.getBaseType();
		lastValueString = "";
		if (size == 0)
			;// null
		else if (bt == String.class)
		{
			o = readString(size);
			lastValueString = o.toString();
		}
		else if (bt == short.class)
		{
			o = new Short(readShort());
			lastValueString = o.toString();
			if (size > 2)
				skip(size - 2);
		}
		else if (bt == int.class)
		{
			o = new Integer(readInt());
			lastValueString = o.toString();
			if (size > 4)
				skip(size - 4);
		}
		else if (bt == float.class)
		{
			o = new Float(readFloat());
			lastValueString = o.toString();
			if (size > 4)
				skip(size - 4);
		}
		else if (bt == double.class)
		{
			o = new Double(readDouble());
			lastValueString = o.toString();
			if (size > 8)
				skip(size - 8);
		}
		else if (bt == byte[].class)
		{
			byte[] bytesToDump = new byte[size < 32 ? size : 32];
			if (keepImages)
			{
				byte ba[] = new byte[size];
				// log("allocByteArray "+vr+":"+size);
				readFully(ba);
				o = ba;
				System.arraycopy(ba, 0, bytesToDump, 0, bytesToDump.length);
			}
			else
			{
				// Log("skipping "+size);
				readFully(bytesToDump);
				if (size > bytesToDump.length)
					skip(size - bytesToDump.length);
			}
			if (dumpList != null)
			{
				StringBuffer sb = new StringBuffer(bytesToDump.length);
				for (byte element : bytesToDump)
				{
					char ch = (char) element;
					if (ch > 32 && ch < 128)
						sb.append(ch);
					else
						sb.append(" ");
				}
				lastValueString = sb.toString();
			}
		}
		else
			throw new Exception("bad VR base type " + bt);
		// log(""+vr+" "+size+" "+o);
		// log("readValue: "+o);
		return o;
	}
	int	tagPos	= 0;

	Tag readTag() throws Exception
	{
		tagPos = pos;
		if (eof)
			return null;
		short group = readShortAndSetEOF();
		if (eof)
			return null;
		boolean currentLE = le;
		if (nextGroupSyntax != null && group != lastGroup)
		{
			setSyntax(nextGroupSyntax);
			nextGroupSyntax = null;
			if (currentLE != le)
				group = (short) (((group & 0x0FF) << 8) | ((group >> 8) & 0x0FF));
		}
		lastGroup = group;
		short id = readShort();
		if (group == 0x2001 && id == 0x1025)
			log("" + group + ":" + id);
		Tag tag = Tag.get(group, id);
		if (tag == null)
			tag = new Tag(group, id);
		return tag;
	}

	/*
	 * void skipSeqItem(int len) throws Exception { if (len == -1) { for (;;) { Tag t = readTag(); Object v =
	 * readTagValue(t, true); if (v == null || t == Tag.ItemDelimitationItem) break; } } else { skip(len); } } DS
	 * readSeqItem(int len) throws Exception { DS dss; if (len == -1) { dss = new DS(); for (;;) { Tag t = readTag();
	 * Object v = readTagValue(t, true); if (v == null || t == Tag.ItemDelimitationItem) break; if (t.getID() == 0)
	 * continue; dss.put(t, v); } } else { byte[] b = new byte[len]; Log("alloc "+len); readFully(b); DSInputStream
	 * loader = new DSInputStream(new ByteArrayInputStream(b), syntax); dss = loader.readDS(); } return dss; }
	 */
	void skipSequence(int len) throws Exception
	{
		// log("====skipSequence");
		readSequence(len);
		// log("====EndSkipdSequence");
	}

	DSList readSequence(int len) throws Exception
	{
		DSList l = new DSList();
		// log("====readSequence");
		DSInputStream loader = this;
		if (len != -1)
		{
			byte b[] = new byte[len];
			// log("alloc "+len);
			readFully(b);
			loader = new DSInputStream(new ByteArrayInputStream(b), syntax);
		}
		for (;;)
		{
			DS ds = null;
			Tag tag = loader.readTag();
			if (tag == null)
				break;
			int itemLen = loader.readInt();
			if (tag.equals(Tag.SeqDelimitationItem))
				break;
			if (tag != Tag.Item)
				throw new Exception("expected item got " + tag);
			if (itemLen != -1)
			{
				byte b[] = new byte[itemLen];
				// log("alloc "+itemLen);
				loader.readFully(b);
				DSInputStream ll = new DSInputStream(new ByteArrayInputStream(b), syntax);
				ds = ll.readDS();
			}
			else
			{
				ds = new DS();
				for (;;)
				{
					tag = loader.readTag();
					if (tag == null)
						break;
					value = loader.readTagValue(tag, false);
					if (tag.equals(Tag.ItemDelimitationItem))
						break;
					ds.put(tag, value);
				}
			}
			if (ds != null)
				l.add(ds);
		}
		return l;
	}

	/*
	 * int itemLen = loader.readInt(); if (tag == Tag.SeqDelimitationItem) break; if (tag != Tag.Item) throw new
	 * Exception("expected item got "+tag);
	 * 
	 * dss.put(tag, loader.readSeqItem(itemLen)); }
	 * 
	 * 
	 * { dss = new DS(); for (;;) { Tag tag = readTag(); if (tag == null) break; int itemLen = readInt(); if (tag ==
	 * Tag.SeqDelimitationItem) break; if (tag != Tag.Item) throw new Exception("expected item got
	 * "+tag); dss.put(tag, readSeqItem(itemLen)); } Log("seq="+dss); } Log("====EndReadSequence"); return dss;
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * Log("====readSequence"); if (len != -1) { byte b[] = new byte[len]; Log("alloc "+len); readFully(b);
	 * DSInputStream loader = new DSInputStream(new ByteArrayInputStream(b), syntax); dss = loader.readDS();
	 * Log("seq="+dss); } else { dss = new DS(); for (;;) { Tag tag = readTag(); if (tag == null) break; int itemLen =
	 * readInt(); if (tag == Tag.SeqDelimitationItem) break; if (tag != Tag.Item) throw new
	 * Exception("expected item got "+tag); dss.put(tag, readSeqItem(itemLen)); } Log("seq="+dss); }
	 * Log("====EndReadSequence"); return dss;
	 */
	void skipFragments() throws Exception
	{
		// log("====skipFragments");
		for (;;)
		{
			Tag tag = readTag();
			int size = readInt();
			if (tag == Tag.SeqDelimitationItem)
				break;
			if (tag != Tag.Item)
				throw new Exception("expected item tag " + tag);
			skip(size);
		}
		// log("====EndSkipFragments");
	}

	List readFragments() throws Exception
	{
		List ll = new FragmentList();
		// log("====readFragments");
		for (;;)
		{
			Tag tag = readTag();
			int size = readInt();
			if (tag == Tag.SeqDelimitationItem)
				break;
			if (tag != Tag.Item)
				throw new Exception("expected item tag " + tag);
			byte ba[] = new byte[size];
			// log("alloc "+size);
			readFully(ba);
			ll.add(ba);
		}
		// log("====EndReadFragments");
		return ll;
	}

	Object readTagValue(Tag tag, boolean skip) throws Exception
	{
		lastValueString = "";
		if (tag == null)
			return null;
		if (disableSkipValues && skip)
			skip = false;
		VR vr = tag.getVR();
		// log(""+tag);
		// if (tag.equals(Tag.PixelData)) {
		// log("");
		// }
		// if (tag.getGroup() == 0x018)
		// if (tag.getID() == 0x602C)
		// log("XXX");
		// return sequence helpers
		if (tag.getGroup() == 0xFFFE)
		{
			readInt();
			// log("FFFE:"+pos+":"+tag);
			return null;
		}
		int len;
		if (explicit)
		{
			// hack sometimes implicit is mixed in by implementations
			// !!!! evaluate how safe this is!!!!!!
			temp[0] = (byte) read();
			temp[1] = (byte) read();
			if (supportMixedExplicitImplicitHack)
			{
				if (VR.exists("" + (char) temp[0] + (char) temp[1]))
					vr = VR.get("" + (char) temp[0] + (char) temp[1]);
				else
					vr = null;
			}
			else
				vr = VR.get("" + (char) temp[0] + (char) temp[1]);
			if (vr == null)
			{
				temp[2] = (byte) read();
				temp[3] = (byte) read();
				vr = tag.getVR();
				if (vr == null)
					vr = VR.UN;
				len = readInt(temp);
			}
			else
			{
				if (vr == null)
					vr = VR.UN;
				if (tag.vr == VR.UN)
					tag.vr = vr;
				if (vr.shortLen())
					len = readShort();
				else
				{
					skip(2);
					len = readInt();
				}
			}
		}
		else
		{
			vr = tag.getVR();
			if (vr == null)
				vr = VR.UN;
			len = readInt();
		}
		valueLen = len;
		if (tag.equals(Tag.PixelData))
		{
			imageOffset = pos;
			imageSize = len;
		}
		if (skip && (tag != Tag.TransferSyntaxUID))
		{
			if (vr == VR.SQ || (vr == VR.UN && len == -1))
				skipSequence(len);
			else if (len == -1)
				skipFragments();
			else
				skipValue(len);
		}
		else if (vr == VR.SQ || (vr == VR.UN && len == -1))
			value = readSequence(len);
		else if (len == -1)
			value = readFragments();
		else
			value = readValue(tag.getVR(), len);
		if (tag == Tag.TransferSyntaxUID)
			nextGroupSyntax = UID.get((String) value);
		// log(""+tagPos+":"+tag+":"+value);
		return value;
	}
	InputStream	is;
	Tag			tag;
	Object		value;
	String		lastValueString;
	int			valueLen;

	boolean advance() throws Exception
	{
		for (;;)
		{
			tag = readTag();
			if (tag == null)
				return false;
			// only return infilter (if exists) always ignore group lengths
			boolean returnTagValue = (filter == null || filter.containsKey(tag)) && (tag.getID() != 0);
			value = readTagValue(tag, false);
			if (dumpList != null)
			{
				DSDumpItem item = new DSDumpItem();
				item.DataType = tag.getVR().getName();
				item.Description = tag.getName();
				item.Group = tag.getGroup();
				item.Element = tag.getID();
				item.ValueLength = valueLen;
				item.ValueString = lastValueString;
				dumpList.add(item);
			}
			if (returnTagValue)
				break;
		}
		return true;
	}

	public DS readDS() throws Exception
	{
		return readDS(new DS());
	}

	public DS readDS(DS ds) throws Exception
	{
		while (advance())
			ds.put(tag, value);
		ds.setImageOffset(GetImageOffset());
		ds.setImageSize(GetImageSize());
		return ds;
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
			// is this every used?
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
			throw new RuntimeException("unxpected UID for dataset " + uid);
	}

	// static public DS readFrom(InputStream is) throws Exception {
	// return readFrom(is, UID.ImplicitVRLittleEndian, new DS());
	// }
	// UID.ImplicitVRLittleEndian
	static public DS readFrom(InputStream is, UID syntax, HashMap filter) throws Exception
	{
		DSInputStream dis = new DSInputStream(is, syntax);
		if (filter != null)
			dis.setFilter(filter);
		return dis.readDS(new DS());
	}

	// static public DS readFrom(InputStream is, UID syntax, DS ds) throws
	// Exception {
	// DSInputStream dis = new DSInputStream(is, UID.ImplicitVRLittleEndian);
	// ds = dis.readDS(ds);
	// return ds;
	// }
	static public DS readFrom(InputStream is, UID syntax) throws Exception
	{
		return readFrom(is, syntax, new DS());
	}

	// UID.ImplicitVRLittleEndian
	static public DS readFrom(InputStream is, UID syntax, boolean keepImages) throws Exception
	{
		DS ds = new DS();
		DSInputStream dis = new DSInputStream(is, syntax);
		dis.keepImages = keepImages;
		ds = dis.readDS(ds);
		return ds;
	}

	static public DS readFrom(InputStream is, UID syntax, DS ds) throws Exception
	{
		DSInputStream dis = new DSInputStream(is, syntax);
		ds = dis.readDS(ds);
		return ds;
	}

	static public DS readFile(File f)
	{
		return readFile(f, new DS(), null, null, false);
	}

	static public DS readFileAndImages(File f)
	{
		return readFile(f, new DS(), null, null, true);
	}

	static public DS readFile(File f, List dumpList)
	{
		return readFile(f, new DS(), null, dumpList, false);
	}

	static public DS readFile(File f, HashMap filter)
	{
		return readFile(f, new DS(), filter, null, false);
	}

	static public DS readFile(File f, DS ds, HashMap filter, List dumpList, boolean keepImages)
	{
		if (ds == null)
			ds = new DS();
		RandomAccessFile raf = null;
		FileInputStream fis = null;
		InputStream mdfis = null;
		try
		{
			boolean dicomTagged = false;
			raf = new RandomAccessFile(f, "r");
			// b.dump(512);
			// b.setBigEndian(false);
			if (raf.length() < 128 + 4)
				throw new Exception("File too small " + raf.length());
			raf.seek(128);
			byte dicm[] = new byte[4];
			raf.readFully(dicm);
			String s = new String(dicm);
			if (!s.equals("DICM"))
				dicomTagged = false;
			// Log.error("not a DICM tagged file");
			else
				dicomTagged = true;
			raf.close();
			raf = null;
			// mdf file hack
			boolean isMDFFile = f.getName().toLowerCase().endsWith(".mdf");
			if (!isMDFFile)
			{
				fis = new FileInputStream(f);
				UID syntax = dicomTagged ? UID.ExplicitVRLittleEndian : UID.ImplicitVRLittleEndian;
				if (dicomTagged)
					fis.skip(128 + 4);
				DSInputStream dis = new DSInputStream(fis, syntax);
				dis.setKeepImages(keepImages);
				if (filter != null)
					dis.setFilter(filter);
				if (dumpList != null)
					dis.setDumpList(dumpList);
				ds = dis.readDS(ds);
				// update offset to include header
				if (dicomTagged)
					ds.setImageOffset(ds.getImageOffset() + (128 + 4));
				fis.close();
				fis = null;
				return ds;
			}
			else
			{
				mdfis = SSStore.getInputStream(f);
				UID syntax = UID.ImplicitVRLittleEndian;
				DSInputStream dis = new DSInputStream(mdfis, syntax);
				dis.setKeepImages(keepImages);
				if (filter != null)
					dis.setFilter(filter);
				if (dumpList != null)
					dis.setDumpList(dumpList);
				ds = dis.readDS(ds);
				// update offset to include header
				if (dicomTagged)
					ds.setImageOffset(ds.getImageOffset() + SSStore.METADATA_SIZE);
				mdfis.close();
				mdfis = null;
				return ds;
			}
		}
		catch (OutOfMemoryError oe)
		{
			Log.log("Problem reading file: " + f);
			return null;
		}
		catch (Exception e)
		{
			Log.log("loadFile caught ", e);
			return null;
		}
		finally
		{
			Util.safeClose(fis);
			Util.safeClose(raf);
			Util.safeClose(mdfis);
		}
	}

	public static void main(String[] args) throws Exception
	{
		// try {
		// Dicom.init(new AE("CLIENT"), new File("conf/dictionary.xml"));
		// //DumpDicomFile(new File("c:\\dcm_images\\image11.dcm"));
		//
		// List l = Util.listFiles(new File("c:\\dicom\\dcm_images"),
		// //\\Medical_Images"),
		// new String[] { ".dcm", ".dic", ".img" });
		// // new String[] { "xa_integris.dcm" , ".dcm", ".dic", ".img" });
		// // new String[] { "epicard.dcm" });
		//
		// Iterator iter = l.iterator();
		// while (iter.hasNext()) {
		// File f = (File)iter.next();
		// if (f.getName().indexOf(".dcm.dcm") != -1)
		// continue;
		// Log.force("==============================================
		// "+f.getName()+"=================================================");
		// Log.force("==============================================
		// "+f.getName()+"=================================================");
		// Log.force("==============================================
		// "+f.getName()+"=================================================");
		// Log.force("==============================================
		// "+f.getName()+"=================================================");
		// DS ds = DSInputStream.readFile(f.getAbsoluteFile(), null);
		// // Log(dss.toString());
		// }
		//
		// }
		// catch (Exception e) {
		// e.printStackTrace();
		// Log.debug("main caught "+e);
		// }
	}
	//
	//
	//
}
