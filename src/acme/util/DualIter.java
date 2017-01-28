package acme.util;

import java.util.Iterator;

class DualIter implements Iterator
{
	Iterator a;
	Iterator b;
	boolean inPlace = false;
	Object ao;
	Object bo;

	public DualIter(Iterator a, Iterator b)
	{
		this.a = a;
		this.b = b;
	};

	void advance()
	{
		if (inPlace) return;
		inPlace = true;
		if (ao == null && a != null)
		{
			ao = a.hasNext() ? a.next() : null;
			if (ao == null) a = null;
		}
		if (bo == null && b != null)
		{
			bo = b.hasNext() ? b.next() : null;
			if (bo == null) b = null;
		}
	}

	public boolean hasNext()
	{
		advance();
		return ao != null || bo != null;
	}

	public Object next()
	{
		advance();
		inPlace = false;
		int cmp = 0;
		if (ao == null && bo == null) return null;
		if (ao == null)
			cmp = 1;
		else if (bo == null)
			cmp = -1;
		else cmp = ((Comparable) ao).compareTo(bo);
		Object rv = null;
		if (cmp >= 0)
		{
			rv = bo;
			bo = null;
		} else
		{
			rv = ao;
			ao = null;
		}
		return rv;
	}

	public void remove()
	{
		throw new RuntimeException("not implemented");
	}
};
