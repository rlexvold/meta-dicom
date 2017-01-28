package net.metafusion.util;

import java.util.List;
import acme.util.Util;
import acme.util.XML;

public class Role
{
	int presContextID = 0;
	boolean provider;
	net.metafusion.util.UID syntax;
	List syntaxList = null;
	net.metafusion.util.UID uid;
	boolean user;

	public Role(net.metafusion.util.UID uid, List syntaxList, boolean user, boolean provider)
	{
		this.uid = uid;
		this.syntax = null;
		this.syntaxList = syntaxList;
		this.user = user;
		this.provider = provider;
	}

	public Role(net.metafusion.util.UID uid, net.metafusion.util.UID syntax, boolean user, boolean provider)
	{
		this.uid = uid;
		this.syntax = syntax;
		this.syntaxList = null;
		this.user = user;
		this.provider = provider;
	}

	public Role(XML xml)
	{
		Util.Assert(xml.getName().equals("role"));
		uid = net.metafusion.util.UID.get(xml.get("uid"));
		user = xml.get("user").equalsIgnoreCase("true");
		provider = xml.get("provider").equalsIgnoreCase("true");
		List sl = xml.getList("roles");
		for (int i = 0; i < sl.size(); i++)
		{
			XML x = (XML) sl.get(i);
			Util.Assert(x.getName().equals("UID"));
			net.metafusion.util.UID uid = net.metafusion.util.UID.get(x.get("name"));
			syntaxList.add(uid);
			if (syntax == null) syntax = uid;
		}
	}

	public int compareTo(Object o)
	{
		return uid.compareTo(o);
	}

	public boolean equals(Object o)
	{
		return uid.equals(o);
	}

	public int getPresContextID()
	{
		return presContextID;
	}

	public net.metafusion.util.UID getSyntax()
	{
		return syntax;
	}

	public List getSyntaxList()
	{
		return syntaxList;
	}

	public net.metafusion.util.UID getUID()
	{
		return uid;
	}

	public boolean isProvider()
	{
		return provider;
	}

	public boolean isUser()
	{
		return user;
	}

	public void setPresContextID(int presContextID)
	{
		this.presContextID = presContextID;
	}

	public void setProvider(boolean provider)
	{
		this.provider = provider;
	}

	public void setSyntax(net.metafusion.util.UID syntax)
	{
		this.syntax = syntax;
	}

	public void setSyntaxList(List syntaxList)
	{
		this.syntaxList = syntaxList;
	}

	public void setUID(net.metafusion.util.UID uid)
	{
		this.uid = uid;
	}

	public void setUser(boolean user)
	{
		this.user = user;
	}
}
