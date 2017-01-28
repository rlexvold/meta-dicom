package net.metafusion.ptburn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import acme.util.FileUtil;
import acme.util.Util;

public class IniFile
{
	static void log(String s)
	{
		System.out.println(s);
	}
	private HashMap<String, String>	map			= new HashMap<String, String>();
	private HashMap<String, List>	sectionMap	= new HashMap<String, List>();

	public IniFile()
	{
	}

	public IniFile(File f)
	{
		BufferedReader br = null;
		List sectionList = null;
		try
		{
			br = new BufferedReader(new FileReader(f));
			String section = "";
			for (;;)
			{
				String line = br.readLine();
				if (line == null)
					break;
				line = line.trim();
				if (line.length() == 0 || line.startsWith("#"))
					continue;
				if (line.startsWith("["))
				{
					section = line.substring(1, line.indexOf(']'));
					sectionList = new LinkedList();
					sectionMap.put(section, sectionList);
				}
				else
				{
					if (sectionList != null)
						sectionList.add(line);
					String tok[] = line.split("=");
					if (tok.length == 1)
						put(section, tok[0].trim(), "");
					else if (tok.length == 2)
						put(section, tok[0].trim(), tok[1].trim());
					else
						log("ini: bad line " + line);
				}
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			Util.safeClose(br);
		}
	}

	String[] getSection(String name)
	{
		List l = sectionMap.get(name);
		if (l == null)
			return null;
		return (String[]) l.toArray(new String[l.size()]);
	}

	public boolean write(File f)
	{
		FileOutputStream fos = null;
		try
		{
			File tempFile = new File(f.getParent(), f.getName() + ".tmp");
			fos = new FileOutputStream(tempFile);
			String keys[] = map.keySet().toArray(new String[0]);
			Arrays.sort(keys);
			String lastSection = "";
			for (String key : keys)
			{
				String value = map.get(key);
				String kv[] = key.split("=");
				if (!kv[0].equals(lastSection))
					fos.write(("[" + kv[0] + "]\r\n").getBytes());
				fos.write((kv[1] + "=" + value + "\r\n").getBytes());
				lastSection = kv[0];
			}
			fos.close();
			boolean rename = FileUtil.rename(tempFile, f, false);
			if (!rename)
			{
				log("rename FAIL " + tempFile + "=>" + f);
				return false;
			}
		}
		catch (Exception e)
		{
			log("write ini caught " + e);
			return false;
		}
		finally
		{
			Util.safeClose(fos);
		}
		return true;
	}

	public void clear()
	{
		map.clear();
		sectionMap.clear();
	}

	public void put(String key, String value)
	{
		map.put("=" + key, value);
	}

	public String get(String key)
	{
		return map.get("=" + key);
	}

	public void put(String section, String key, String value)
	{
		map.put(section + "=" + key, value);
	}

	public String get(String section, String key)
	{
		return map.get(section + "=" + key);
	}

	public int getInt(String section, String key)
	{
		return Integer.parseInt(map.get(section + "=" + key));
	}

	public boolean contains(String section, String key)
	{
		return map.containsKey(section + "=" + key);
	}

	static void test()
	{
		IniFile a = new IniFile(new File("C:/desktop/goldwave.ini"));
		a.write(new File("c:/desktop/test.ini"));
		IniFile b = new IniFile(new File("c:/desktop/test.ini"));
		log("done");
	}
}