package acme.util;

import java.util.Stack;

public class Pool
{
	Stack free = new Stack();

	public synchronized Object grab()
	{
		Object o;
		if (free.empty())
			o = allocate();
		else o = free.pop();
		return o;
	}

	public synchronized void clear()
	{
		while (!free.empty())
		{
			clear(free.pop());
		}
	}

	public synchronized void release(Object o)
	{
		free.push(o);
	}

	public Object allocate()
	{
		return null;
	}

	public void clear(Object o)
	{
	}
}
