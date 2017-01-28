package net.metafusion.util;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

public class FragmentList extends LinkedList
{
	public boolean equals(Object o)
	{
		if (o == this) return true;
		if (!(o instanceof List)) return false;
		ListIterator e1 = listIterator();
		ListIterator e2 = ((List) o).listIterator();
		while (e1.hasNext() && e2.hasNext())
		{
			byte o1[] = (byte[]) e1.next();
			byte o2[] = (byte[]) e2.next();
			if (o1 == null && o2 == null) continue;
			if (o1.length != o2.length) return false;
			for (int i = 0; i < o1.length; i++)
				if (o1[i] != o2[i]) return false;
		}
		return !(e1.hasNext() || e2.hasNext());
	}
}
