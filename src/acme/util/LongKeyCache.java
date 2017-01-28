package acme.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class LongKeyCache
{
	private HashMap hash = new HashMap();
	private LinkedList lru = new LinkedList();
	int maxSize = 100;
	Elem lastRef = null;
	int version = 0;

	public Set getKeys()
	{
		return hash.keySet();
	}
	class Elem
	{
		public Elem(long key, Object value)
		{
			this.key = key;
			this.value = value;
		}
		private long key;
		private Object value;
		int version = 0;

		public int getVersion()
		{
			return version; // todo: watch out for rollover
		}

		public void setVersion(int version)
		{
			this.version = version;
		}

		public int hashCode()
		{
			return (int) (key ^ (key >>> 32));
		}

		public boolean equals(Object obj)
		{
			if (obj instanceof Elem) { return key == ((Elem) obj).getKey(); }
			return false;
		}

		public long getKey()
		{
			return key;
		}

		public Object getValue()
		{
			return value;
		}
	}

	public LongKeyCache(int maxSize)
	{
		this.maxSize = maxSize;
	}

	public synchronized void clear()
	{
		hash = new HashMap();
		lru = new LinkedList();
		lastRef = null;
	}

	public synchronized Object getNoLRU(long key)
	{
		if (lastRef != null && lastRef.getKey() == key) return lastRef.getValue();
		Elem elem = (Elem) hash.get(new Long(key));
		if (elem != null) return elem.getValue();
		return null;
	}

	public synchronized Object get(long key)
	{
		if (lastRef != null && lastRef.getKey() == key) return lastRef.getValue();
		Elem elem = (Elem) hash.get(new Long(key));
		if (elem != null && elem != lru.getLast())
		{
			lru.remove(elem);
			lru.addLast(elem);
			lastRef = elem;
			lastRef.setVersion(version++);
			return elem.getValue();
		}
		return null;
	}

	public synchronized void put(long key, Object value)
	{
		Elem elem = new Elem(key, value);
		if (hash.containsKey(new Long(key))) Util.log("note: LongKeyValueCache duplicate key: " + key); // throw
		// new
		// RuntimeException("LongKeyValueCache
		// duplicate
		// key:
		// "+key);
		hash.put(new Long(key), elem);
		if (lru.size() >= maxSize)
		{
			Elem relem = (Elem) lru.removeFirst();
			if (relem != null) hash.remove(new Long(relem.key));
		}
		lru.addLast(elem);
		lastRef = elem;
		lastRef.setVersion(version++);
	}

	public synchronized void remove(long key)
	{
		Elem elem = (Elem) hash.remove(new Long(key));
		if (elem != null) lru.remove(elem);
		lastRef = null;
	}

	public synchronized List changedSince(int version, int max)
	{
		List changed = new LinkedList();
		ListIterator iter = lru.listIterator(lru.size());
		while (iter.hasPrevious())
		{
			Elem elem = (Elem) iter.previous();
			if (elem.getVersion() > version)
			{
				if (changed.size() == max) break;
				changed.add(elem);
			} else break;
		}
		return changed;
	}
}