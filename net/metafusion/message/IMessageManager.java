package net.metafusion.message;

import net.metafusion.communication.ICommunicationHandler;

public interface IMessageManager
{
	public boolean sendMessage(AbstractMessage message);

	public boolean canHandle(MessageType msgType);

	public AbstractMessage receiveMessage();
}