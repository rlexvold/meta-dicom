package net.metafusion.admin;

import java.io.Serializable;
import acme.util.XML;

public class AEBean implements Comparable, Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;
	private String name = "";
	private String host = "";
	private String port = "";

	public XML toXML()
	{
		XML x = new XML("ae");
		x.addAttr("name", name);
		x.addAttr("host", host);
		x.addAttr("port", port);
		return x;
	}

	public AEBean(XML x)
	{
		this.name = x.get("name", "");
		this.host = x.get("host", "");
		this.port = x.get("port", "");
	}

	public AEBean(AEBean ae)
	{
		this.name = ae.name;
		this.host = ae.host;
		this.port = ae.port;
	}

	public AEBean(String name, String host, String port)
	{
		this.name = name;
		this.host = host;
		this.port = port;
	}

	public AEBean(String name)
	{
		this.name = name;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getHost()
	{
		return host;
	}

	public void setHost(String host)
	{
		this.host = host;
	}

	public String getPort()
	{
		return port;
	}

	public void setPort(String port)
	{
		this.port = port;
	}

	public int hashCode()
	{
		return name.hashCode();
	}

	public boolean equals(Object o)
	{
		return name.equals(((AEBean) o).getName());
	}

	public int compareTo(Object o)
	{
		return name.compareTo(((AEBean) o).getName());
	}

	public String toString()
	{
		return name;
	}

	public String toString2()
	{
		return "AE: " + name + ":" + host + ":" + port;
	}
}
