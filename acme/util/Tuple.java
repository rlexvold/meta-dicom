package acme.util;

public class Tuple
{
	Object t[];

	public Tuple(Object a)
	{
		t = new Object[] { a };
	}

	public Tuple(Object a, Object b)
	{
		t = new Object[] { a, b };
	}

	public Tuple(Object a, Object b, Object c)
	{
		t = new Object[] { a, b, c };
	}

	public Tuple(Object a, Object b, Object c, Object d)
	{
		t = new Object[] { a, b, c, d };
	}

	public Tuple(Object a, Object b, Object c, Object d, Object e)
	{
		t = new Object[] { a, b, c, d, e };
	}

	public Tuple(Object[] t)
	{
		this.t = t;
	}

	public Object get(int index)
	{
		return t[index];
	}

	public int length()
	{
		return t.length;
	}
}
