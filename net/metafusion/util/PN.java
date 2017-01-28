package net.metafusion.util;

import acme.util.StringUtil;

public class PN
{
	public PN(String name)
	{
		if (name == null) name = "";
		String elem[];
		if (name.indexOf('^') != -1)
			elem = StringUtil.split(name, '^');
		else elem = StringUtil.split(name, ',');
		last = elem.length > 0 ? elem[0] : "";
		first = elem.length > 1 ? elem[1] : "";
		middle = elem.length > 2 ? elem[2] : "";
	}
	String middle;
	String last;
	String first;

	public String getFirst()
	{
		return first;
	}

	public void setFirst(String first)
	{
		this.first = first;
	}

	public String getMiddle()
	{
		return middle;
	}

	public void setMiddle(String middle)
	{
		this.middle = middle;
	}

	public String getLast()
	{
		return last;
	}

	public void setLast(String last)
	{
		this.last = last;
	}
}
