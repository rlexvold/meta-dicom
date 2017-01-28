package net.metafusion.util;

public class Counter implements Comparable<Counter>
{
	public Long	id;
	public Long	count1	= 0L;
	public Long	count2	= 0L;
	public Long	count3	= 0L;

	public Counter(Long id)
	{
		this.id = id;
	}

	public int compareTo(Counter o)
	{
		if (id < o.id)
			return -1;
		if (id > o.id)
			return 1;
		return 0;
	}

	public Long getTotalCount()
	{
		return count1 + count2 + count3;
	}
}
