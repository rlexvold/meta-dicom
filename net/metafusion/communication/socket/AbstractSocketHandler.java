package net.metafusion.communication.socket;

import java.net.Socket;
import net.metafusion.communication.AbstractCommunicationHandler;
import net.metafusion.communication.CommunicationProtocol;

public abstract class AbstractSocketHandler extends AbstractCommunicationHandler
{
	protected SocketConnectionInfo serverConnectionInfo = null;
	protected Socket socket;

	public AbstractSocketHandler()
	{
		protocolHandled = CommunicationProtocol.SOCKET;
	}

	public void closeConnection()
	{
	}

	public SocketConnectionInfo getServerConnectionInfo()
	{
		return serverConnectionInfo;
	}

	public void setServerConnectionInfo(SocketConnectionInfo serverConnectionInfo)
	{
		this.serverConnectionInfo = serverConnectionInfo;
	}

	public Socket getSocket()
	{
		return socket;
	}

	public void setSocket(Socket socket)
	{
		this.socket = socket;
	}
}