package acme.util;

public class ThreadSafeCounter
{
	private volatile int value = 0;
	private volatile int min = 0;
	private volatile int max = 0;
	private volatile int total = 0;

	public ThreadSafeCounter()
	{
	}

	public ThreadSafeCounter(int value)
	{
		this.value = max = min = value;
	}

	public synchronized int increment()
	{
		set(value + 1);
		total++;
		return value;
	}

	public synchronized int decrement()
	{
		set(value - 1);
		return value;
	}

	public synchronized int add(int d)
	{
		int v = value;
		set(value + d);
		total += d;
		return v;
	}

	public synchronized int sub(int d)
	{
		int v = value;
		set(value - d);
		return v;
	}

	public synchronized int get()
	{
		return value;
	}

	public synchronized int getTotalAdditions()
	{
		return total;
	}

	public synchronized void set(int v)
	{
		if (v > max) max = v;
		if (v < min) min = v;
		value = v;
	}

	public int getMin()
	{
		return min;
	}

	public int getMax()
	{
		return max;
	}

	public void waitFor(int value)
	{
		for (;;)
		{
			if (get() == value) return;
			Util.sleep(250);
		}
	}

	@Override
	public String toString()
	{
		if (min == 0)
			return value + "(" + max + ")";
		else return value + "(" + min + "," + max + ")";
	}
}
