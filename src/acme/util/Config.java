package acme.util;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class Config
{
	static XML config = null;

	public static XML getConfig()
	{
		return config;
	}

	public static void setConfig(XML config)
	{
		Config.config = config;
		if (config == null) throw new RuntimeException("null config set");
		Iterator iter = config.getList().iterator();
		while (iter.hasNext())
		{
			XML x = (XML) iter.next();
			if (x.getName().equals("property"))
			{
				System.setProperty(x.get("name"), x.get("value"));
			}
		}
	}

	public static void load(File f) throws Exception
	{
		XML xml = new XML(f);
		setConfig(xml);
	}

	public static XML search(String attr, String value)
	{
		return config.search(attr, value);
	}

	public static String getName()
	{
		return config.getName();
	}

	public static String get()
	{
		return config.get();
	}

	public static XML getNode(String name)
	{
		return config.getNode(name);
	}

	public static String get(String name)
	{
		return config.get(name);
	}

	public static String get(String name, String def)
	{
		return config.get(name, def);
	}

	public static int getInt(String name)
	{
		return Integer.parseInt(config.get(name));
	}

	public static int getInt(String name, int def)
	{
		return Integer.parseInt(config.get(name, "" + def));
	}

	public static List getList()
	{
		return config.getList();
	}

	public static List getList(String name)
	{
		return config.getList(name);
	}
}
