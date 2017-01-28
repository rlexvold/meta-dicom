package net.metafusion.communication;

import net.metafusion.message.AbstractMessage;
import net.metafusion.message.IMessageManager;
import net.metafusion.message.MessageType;

public abstract class AbstractCommunicationHandler implements ICommunicationHandler
{
	private AbstractConnectionInfo serverConnectionInfo = null;
	protected CommunicationProtocol protocolHandled = null;

	public AbstractConnectionInfo getServerConnectionInfo()
	{
		return serverConnectionInfo;
	}

	public void setServerConnectionInfo(AbstractConnectionInfo theServerConnectionInfo)
	{
		serverConnectionInfo = theServerConnectionInfo;
	}

	public boolean canHandle(AbstractConnectionInfo serverInfo)
	{
		if (serverInfo.getCommunicationProtocol() == protocolHandled) return true;
		return false;
	}

	public CommunicationProtocol getProtocolHandled()
	{
		return protocolHandled;
	}

	public void setProtocolHandled(CommunicationProtocol protocol)
	{
		protocolHandled = protocol;
	}
}