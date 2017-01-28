package net.metafusion.ptburn;

import acme.util.Util;

public class Threader
{
	static int tcount = 0;

	static synchronized void inc()
	{
		tcount++;
	}
	static class Foo implements Runnable
	{
		public void run()
		{
			for (;;)
			{
				Util.sleep(100);
				inc();
			}
		}
	}

	public static void main(String[] args)
	{
		for (int i = 0; i < 1000; i++)
			new Thread(new Foo()).start();
		long count = 0;
		long sum = 0;
		long stop = System.currentTimeMillis() + 10 * 1000;
		while (System.currentTimeMillis() <= stop)
		{
			double d = Math.random();
			sum += d;
			count++;
		}
		System.out.println("" + tcount);
		System.out.println("" + count);
	}
}