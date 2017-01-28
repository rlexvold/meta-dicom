package net.metafusion.util;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import acme.util.Buffer;
import acme.util.Log;
import acme.util.Util;

public class VR
{
	static void log(String s)
	{
		Log.vlog(s);
	}
	static HashMap hm = new HashMap();

	public final static VR get(String s)
	{
		if (s.length() > 2) s = s.substring(0, 2);
		// if (s.length() != 2)
		// Util.Log(s);
		if (Character.isUpperCase(s.charAt(0)) && Character.isUpperCase(s.charAt(1)))
		{
			VR vr = (VR) hm.get(s);
			if (vr == null) vr = new VR(s);
			return vr;
		}
		return null;
	}

	public final static boolean exists(String s)
	{
		if (s.length() > 2) s = s.substring(0, 2);
		// if (s.length() != 2)
		// Util.Log(s);
		if (Character.isUpperCase(s.charAt(0)) && Character.isUpperCase(s.charAt(1)))
		{
			VR vr = (VR) hm.get(s);
			if (vr != null) return true;
			return false;
		}
		return false;
	}

	public final static VR get(Buffer b)
	{
		return get(b.getString(2));
	}

	public final static VR get(byte[] b, int offset)
	{
		return get(new String(b, offset, 2));
	}

	VR(String name, Class k, Class baseK, boolean shortLen)
	{
		this.name = name;
		this.k = k;
		this.baseK = baseK;
		this.typeName = k.getName();
		if (typeName.lastIndexOf('.') != -1) typeName = typeName.substring(typeName.lastIndexOf('.') + 1);
		unknown = true;
		stringRep = name + "[" + typeName + "]";
		this.shortLen = shortLen;
		hm.put(name, this);
	}

	VR(String unknownName)
	{
		this.name = unknownName;
		this.k = byte[].class;
		this.baseK = byte[].class;
		this.typeName = k.getName();
		if (typeName.lastIndexOf('.') != -1) typeName = typeName.substring(typeName.lastIndexOf('.') + 1);
		unknown = true;
		stringRep = "UNKNOWN" + "[" + typeName + "]";
		shortLen = false;
	}
	String stringRep;
	String name;
	Class k;
	Class baseK;
	String typeName;
	boolean shortLen;
	boolean unknown;

	public boolean isUnknown()
	{
		return unknown;
	}

	public String getName()
	{
		return name;
	}

	public Class getType()
	{
		return k;
	}

	public Class getBaseType()
	{
		return baseK;
	}

	public boolean shortLen()
	{
		return shortLen;
	}

	@Override
	public String toString()
	{
		return stringRep;
	}
	public final static VR AE = new VR("AE", String.class, String.class, true);
	public final static VR AS = new VR("AS", String.class, String.class, true);
	public final static VR AT = new VR("AT", byte[].class, byte[].class, true);
	public final static VR CS = new VR("CS", String.class, String.class, true);
	public final static VR DA = new VR("DA", DDate.class, String.class, true);
	public final static VR DS = new VR("DS", String.class, String.class, true);
	public final static VR DT = new VR("DT", Date.class, String.class, true);
	public final static VR FL = new VR("FL", float.class, float.class, true);
	public final static VR FD = new VR("FD", double.class, double.class, true);
	public final static VR IS = new VR("IS", String.class, String.class, true);
	public final static VR LO = new VR("LO", String.class, String.class, true);
	public final static VR LT = new VR("LT", String.class, String.class, true);
	public final static VR OB = new VR("OB", byte[].class, byte[].class, false);
	public final static VR OW = new VR("OW", byte[].class, byte[].class, false);
	public final static VR PN = new VR("PN", Person.class, String.class, true);
	public final static VR SH = new VR("SH", String.class, String.class, true);
	public final static VR SL = new VR("SL", int.class, int.class, true);
	public final static VR SS = new VR("SS", short.class, short.class, true);
	public final static VR SQ = new VR("SQ", LinkedList.class, LinkedList.class, false); // todo
																							// ??????????????????????????
	public final static VR ST = new VR("ST", String.class, String.class, true);
	public final static VR TM = new VR("TM", Date.class, String.class, true);
	public final static VR UI = new VR("UI", String.class, String.class, true);
	public final static VR UL = new VR("UL", int.class, int.class, true);
	public final static VR UN = new VR("UN", byte[].class, byte[].class, false);
	public final static VR US = new VR("US", short.class, short.class, true);
	public final static VR UT = new VR("UT", String.class, String.class, false); // todo
																					// see
																					// spec
																					// is
																					// this
																					// correct
	public final static VR NONE = new VR("NO", byte[].class, byte[].class, false);

	public Buffer getElement(Buffer b)
	{
		Buffer elem = null;
		if (shortLen)
		{
			int len = b.getShort();
			if ((len & 1) == 1) Log.log("warning: element length odd " + len);
			Util.Assert(len != -1);
			elem = new Buffer(b, len);
			b.skip(len);
		} else
		{
			int skip = b.getShort();
			if (skip != 0) Log.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! warning: reserved 0 set to " + skip);
			int len = b.getInt();
			if ((len & 1) == 1) Log.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! warning: element length odd " + len);
			Util.Assert(len != -1);
			elem = new Buffer(b, len);
			b.skip(len);
		}
		return elem;
	}

	public static Buffer getElementImplicit(Buffer b)
	{
		int len = b.getInt();
		if ((len & 1) == 1) Log.log("warning: element length odd " + len);
		Util.Assert(len != -1);
		Buffer elem = new Buffer(b, len);
		b.skip(len);
		return elem;
	}

	@Override
	public boolean equals(Object o)
	{
		return o != null && o instanceof VR && name.equals(((VR) o).getName());
	}
}
