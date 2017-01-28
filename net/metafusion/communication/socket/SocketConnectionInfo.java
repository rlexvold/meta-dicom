package net.metafusion.communication.socket;

import net.metafusion.communication.AbstractConnectionInfo;

public class SocketConnectionInfo extends AbstractConnectionInfo
{
	private String hostname;
	private Integer port;

	public String getHostname()
	{
		return hostname;
	}

	public Integer getPort()
	{
		return port;
	}

	public void setHostname(String theHostname)
	{
		hostname = theHostname;
	}

	public void setPort(Integer thePort)
	{
		port = thePort;
	}
}