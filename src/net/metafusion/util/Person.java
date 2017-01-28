package net.metafusion.util;

public class Person
{
	public Person()
	{
		s = "";
	}

	public Person(String s)
	{
		this.s = s;
	}
	String s;

	public String toString()
	{
		return "Person: " + s;
	}
}
