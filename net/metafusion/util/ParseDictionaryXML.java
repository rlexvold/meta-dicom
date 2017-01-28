package net.metafusion.util;

import java.io.File;
import java.util.Iterator;
import acme.util.StringUtil;
import acme.util.XML;

public class ParseDictionaryXML
{
	static void log(String s)
	{
		acme.util.Log.log(s);
	}

	// public static final Tag DimensionIndexValues = new Tag(0x00209157);
	// (0020,9158) VR=LT Frame Comments//
	static void doElem(XML x)
	{
		// String type = x.get("type");
		Iterator iter = x.getList().iterator();
		while (iter.hasNext())
		{
			XML xml = (XML) iter.next();
			String tag = xml.get("tag");
			String name = xml.get("name");
			String retired = xml.get("retired", "false");
			String key = xml.get("key");
			String vm = xml.get("vm", "1");
			String vr = xml.get("vr");
			tag = StringUtil.replaceAll(tag, "x", "0");
			tag = StringUtil.replaceAll(tag, "\\(", "0x");
			tag = StringUtil.replaceAll(tag, "\\,", ",0x");
			tag = StringUtil.replaceAll(tag, "[\\(\\)]", "");
			acme.util.Log.log("public static final Tag " + key + " = new Tag(" + tag + ",\"" + name + "\"" + ",\"" + vr + "\",\"" + vm + "\"," + retired + ");");
		}
	}

	public static void main(String[] args)
	{
		try
		{
			XML xml = new XML(new File("c:\\etc\\Dictionary.xml"));
			Iterator iter = xml.getList().iterator();
			while (iter.hasNext())
			{
				XML x = (XML) iter.next();
				if (x.getName().equals("elements")) doElem(x);
			}
		}
		catch (Exception e)
		{
			acme.util.Log.log("caught " + e);
		}
	}
}
