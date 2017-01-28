package net.metafusion.communication;

import net.metafusion.message.MessageType;
import net.metafusion.net.DicomServer;
import net.metafusion.util.RoleMap;

public abstract class AbstractConnectionInfo
{
	private CommunicationProtocol communicationProtocol = null;
	private MessageType messageType = null;

	public CommunicationProtocol getCommunicationProtocol()
	{
		return communicationProtocol;
	}

	public MessageType getMessageType()
	{
		return messageType;
	}

	public void setCommunicationProtocol(CommunicationProtocol theCommunicationProtocol)
	{
		communicationProtocol = theCommunicationProtocol;
	}

	public void setMessageType(MessageType messageType)
	{
		this.messageType = messageType;
	}
}