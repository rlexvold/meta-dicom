package net.metafusion.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import acme.util.Log;
import acme.util.XML;

public class AEMap
{
	static HashMap<String, AE>	map			= new HashMap<String, AE>();
	static AE					def			= new AE("NULL_AE");
	static AE					alt			= new AE("NULL_AE");
	static HashMap<String, AE>	mobileMap	= new HashMap<String, AE>();

	// from adminConf
	static public void load(XML xml) throws Exception
	{
		xml = xml.getNode("aelist");
		Iterator iter = xml.getList().iterator();
		while (iter.hasNext())
		{
			XML x = (XML) iter.next();
			AE ae = new AE(x.get("name"), x.get("host"), Integer.parseInt(x.get("port")));
			String zip = x.get("zipPort");
			Integer zipPort = null;
			if (zip != null)
			{
				zipPort = Integer.parseInt(zip);
				ae.setZipPort(zipPort);
			}
			String ris = x.get("RISPORT");
			Integer risPort = null;
			if (ris != null)
			{
				risPort = Integer.parseInt(ris);
				ae.setRisPort(risPort);
			}
			put(ae);
			if (ae.isMobile())
			{
				putMobile(ae);
			}
		}
	}

	public static AE getAlt()
	{
		return alt;
	}

	public static void setAlt(AE alt)
	{
		AEMap.alt = alt;
	}

	static public void setDefault(AE ae)
	{
		def = ae;
	}

	static public AE getDefault()
	{
		return def;
	}

	static public Collection values()
	{
		return map.values();
	}

	static public boolean put(AE ae)
	{
		if (ae.isMobile())
		{
			putMobile(ae);
		}
		return map.put(ae.getName().trim().toUpperCase(), ae) != null;
	}

	static public AE get(String ae)
	{
		String key = ae.trim().toUpperCase();
		boolean result = map.containsKey(key);
		AE tmp = map.get(key);
		if (tmp == null)
		{
			Log.aLog("Unknown AE: " + ae + ", does it need to be added to metaadmin.xml?");
			return null;
		}
		if (tmp.isMobile())
		{
			AE mobile = getMobile(ae);
			if (mobile != null)
				return mobile;
		}
		return tmp;
	}

	static public boolean putMobile(AE ae)
	{
		if (ae == null)
			return false;
		AE tmp = ae.copyAE();
		tmp.setMobile(true);
		return mobileMap.put(tmp.getName().trim().toUpperCase(), tmp) != null;
	}

	static public AE getMobile(String ae)
	{
		if (ae == null)
			return null;
		return mobileMap.get(ae.trim().toUpperCase());
	}
}
