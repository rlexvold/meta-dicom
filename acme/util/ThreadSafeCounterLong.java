package acme.util;

public class ThreadSafeCounterLong
{
	long value = 0;

	public ThreadSafeCounterLong()
	{
	}

	public ThreadSafeCounterLong(long value)
	{
		this.value = value;
	}

	public synchronized long increment()
	{
		return value++;
	}

	public synchronized long decrement()
	{
		return --value;
	}

	public synchronized long add(long d)
	{
		long v = value;
		value += d;
		return v;
	}

	public synchronized long sub(long d)
	{
		value -= d;
		return value;
	}

	public synchronized long get()
	{
		return value;
	}

	public void waitFor(long value)
	{
		for (;;)
		{
			if (get() == value) return;
			Util.sleep(250);
		}
	}
}
