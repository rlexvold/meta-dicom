package acme.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class Stats
{
	static int getIndex(String name)
	{
		Integer i = (Integer) hm.get(name);
		if (i == null)
		{
			if (hw == MAX)
			{
				int newstats[] = new int[MAX * 2];
				System.arraycopy(stats, 0, newstats, 0, hw);
				stats = newstats;
				MAX *= 2;
			}
			hm.put(name, i = new Integer(hw++));
		}
		return i != null ? i.intValue() : -1;
	}

	synchronized static public int get(String name)
	{
		return stats[getIndex(name)];
	}

	synchronized static public void set(String name, int value)
	{
		stats[getIndex(name)] = value;
	}

	synchronized static public void inc(String name)
	{
		stats[getIndex(name)]++;
	}

	static String toMB(long num)
	{
		num /= (1024 * 1024);
		return "" + num + "MB";
	}

	synchronized static public SortedMap getStats()
	{
		SortedMap m = new TreeMap();
		Iterator iter = hm.entrySet().iterator();
		while (iter.hasNext())
		{
			Map.Entry e = (Map.Entry) iter.next();
			m.put(e.getKey(), new Integer(stats[((Integer) e.getValue()).intValue()]));
		}
		m.put("system.freeMemory", toMB(Runtime.getRuntime().freeMemory()));
		m.put("system.totalMemory", toMB(Runtime.getRuntime().totalMemory()));
		m.put("system.maxMemory", toMB(Runtime.getRuntime().maxMemory()));
		return m;
	}
	static int MAX = 1000;
	static int stats[] = new int[MAX];
	static HashMap hm = new HashMap();
	static int hw = 0;
}
