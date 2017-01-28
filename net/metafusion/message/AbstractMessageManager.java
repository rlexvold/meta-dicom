package net.metafusion.message;

import java.util.Set;
import net.metafusion.communication.AbstractConnectionInfo;
import net.metafusion.communication.CommunicationHandlerFactory;
import net.metafusion.communication.ICommunicationHandler;

public abstract class AbstractMessageManager implements IMessageManager
{
	protected MessageType typeHandled = null;
	protected ICommunicationHandler comHandler = null;

	public boolean canHandle(MessageType msgType)
	{
		if (typeHandled == msgType) return true;
		return false;
	}

	public MessageType getTypeHandled()
	{
		return typeHandled;
	}

	public void setTypeHandled(MessageType type)
	{
		typeHandled = type;
	}

	public ICommunicationHandler getComHandler()
	{
		return comHandler;
	}

	public void setComHandler(ICommunicationHandler comHandler)
	{
		this.comHandler = comHandler;
	}
}