/*
 * Created by IntelliJ IDEA.
 * User: mb
 * Date: Nov 19, 2003
 * Time: 6:32:37 PM
 */
package acme.util;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class XMLConfigFile
{
	static XMLConfigFile	defaultConfig	= null;

	static public XMLConfigFile getDefault()
	{
		return defaultConfig;
	}
	private HashMap	nameMap	= new HashMap();
	private XML		fullXML;
	private XML		config;
	private File	file;
	private String	name	= "";

	public String getDefaultName()
	{
		return name;
	}

	public boolean isDefined(String name)
	{
		return config.get() != null;
	}

	public String get(String name)
	{
		return config.get(name, "");
	}

	public String get(String name, String def)
	{
		return config.get(name, def);
	}

	public int getInt(String name)
	{
		return config.getInt(name, 0);
	}

	public int getInt(String name, int def)
	{
		return config.getInt(name, def);
	}

	public boolean getBoolean(String name)
	{
		return config.getBoolean(name, false);
	}

	public boolean getBoolean(String name, boolean def)
	{
		return config.getBoolean(name, def);
	}

	public XMLConfigFile(File file, String name) throws Exception
	{
		if (defaultConfig == null)
			defaultConfig = this;
		this.name = name;
		this.file = file;
		fullXML = new XML(file);
		List l = fullXML.getList();
		for (Iterator iter = l.iterator(); iter.hasNext();)
		{
			XML xml = (XML) iter.next();
			if (xml.get("name") != null)
				nameMap.put(xml.get("name"), xml);
		}
		for (Iterator iter = l.iterator(); iter.hasNext();)
		{
			XML xml = (XML) iter.next();
			substitute(xml);
		}
		config = (XML) nameMap.get(name);
	}

	public XMLConfigFile(File file) throws Exception
	{
		if (defaultConfig == null)
		{
			Util.log("set defaultconfig " + file.toString() + " " + name);
			defaultConfig = this;
		}
		this.file = file;
		fullXML = new XML(file);
		config = fullXML;
	}

	void substitute(XML xml)
	{
		List l = xml.getList();
		if (l == null)
			return;
		for (Iterator iter = l.iterator(); iter.hasNext();)
		{
			XML x = (XML) iter.next();
			if (x.getName().equals("INCLUDE"))
			{
				XML include = (XML) nameMap.get(x.get("name"));
				if (include == null)
					throw new RuntimeException("could not find INCLUDE " + x.get("name"));
				x.substitute(xml, include);
			}
			else
				substitute(x);
		}
	}

	public XML getXML()
	{
		return config;
	}
	// public XML get(String name) {
	// return (XML)nameMap.get(name);
	// }
	//
	public HashMap	subconfigMap	= new HashMap();

	public File getConfigRoot()
	{
		// return new File(file, "..");
		return file.getParentFile();
	}

	public XML getCurrentAdmin() throws Exception
	{
		return new XML(new File(getConfigRoot(), "metaadmin.xml"));
	}

	public XML getOlderAdmin(String name) throws Exception
	{
		File archive = new File(getConfigRoot(), "archive");
		return new XML(new File(archive, name));
	}

	public static Date getDateFromFileName(File f)
	{
		String name = f.getName();
		Date d = new Date();
		try
		{
			name = name.substring(name.indexOf('-') + 1, name.indexOf('.'));
			d = DateUtil.parseYYYYMMDDHHMMSS(name);
		}
		catch (Exception e)
		{
			System.out.println("getDateFromFileName:could not parse" + f.getName());
		}
		return d;
	}
	private HashMap	lastChange	= new HashMap();

	public XML getSubconfig(String name) throws Exception
	{
		XML xml;
		// File f = new File(new File(file, ".."), name);
		File f = new File(file.getParentFile(), name);
		lastChange.put(name, new Long(f.lastModified()));
		xml = new XML(f);
		subconfigMap.put(name, xml);
		return xml;
	}

	public boolean subconfigChanged(String name)
	{
		File f = new File(file.getParentFile(), name);
		// File f = new File(new File(file, ".."), name);
		Long l = (Long) lastChange.get(name);
		return l != null ? (f.lastModified() != l.longValue()) : false;
	}

	public File getSubconfigFile(String name)
	{
		return new File(file.getParentFile(), name);
		// return new File(new File(file, ".."), name);
	}
}