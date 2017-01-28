package net.metafusion.util;

public class AE
{
	private String	hostName	= null;
	private boolean	mobile		= false;
	private String	name		= null;
	private Integer	port		= null;
	private Integer	zipPort		= null;
	private Integer	risPort		= null;

	public Integer getRisPort()
	{
		return risPort;
	}

	public void setRisPort(Integer risPort)
	{
		this.risPort = risPort;
	}

	public AE(String name)
	{
		init(name, "unknown", 5105, null);
	}

	public AE(String name, String hostName, int port)
	{
		init(name, hostName, port, null);
	}

	public AE(String name, String hostName, int port, Integer zipPort)
	{
		init(name, hostName, port, zipPort);
	}

	public AE copyAE()
	{
		AE tmp = new AE(this.name, this.hostName, this.port, this.zipPort);
		return tmp;
	}

	public String getHostName()
	{
		return hostName;
	}

	public String getName()
	{
		return name;
	}

	public Integer getPort()
	{
		return port;
	}

	public Integer getZipPort()
	{
		return zipPort;
	}

	private void init(String name, String hostName, int port, Integer zipPort)
	{
		this.name = name;// .toUpperCase();
		this.hostName = hostName;
		this.port = port;
		this.zipPort = zipPort;
		if (hostName == null || hostName.length() == 0)
		{
			setMobile(true);
		}
		else
			setMobile(false);
	}

	public boolean isMobile()
	{
		return mobile;
	}

	public void setHostName(String hostName)
	{
		this.hostName = hostName;
		if (hostName == null || hostName.length() == 0)
		{
			setMobile(true);
		}
		else
			setMobile(false);
	}

	public void setMobile(boolean mobile)
	{
		this.mobile = mobile;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setPort(Integer port)
	{
		this.port = port;
	}

	public void setZipPort(Integer zipPort)
	{
		this.zipPort = zipPort;
	}

	public String toString()
	{
		String zipString = " zipPort=";
		if (zipPort != null)
			zipString += zipPort;
		else
			zipString += "null";
		return "AE: name=" + name + " host=" + hostName + " port=" + port + zipString;
	}
}
