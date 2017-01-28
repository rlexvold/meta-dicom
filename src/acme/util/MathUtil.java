package acme.util;

import java.util.Random;

public class MathUtil
{
	public static final int pin(int v, int lo, int hi)
	{
		if (v < lo) return lo;
		if (v > hi) return hi;
		return v;
	}
	static Random r = new Random();

	// inclusive
	synchronized public static final int rand(int low, int high)
	{
		return ((r.nextInt() & Integer.MAX_VALUE) % (high - low + 1)) + low;
	}

	public static final int safeInt(String s, int def)
	{
		try
		{
			return Integer.parseInt(s);
		}
		catch (Exception e)
		{
			return def;
		}
	}

	public static final int safeInt(String s)
	{
		return safeInt(s, 0);
	}
}
