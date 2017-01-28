package net.metafusion.communication;

import net.metafusion.message.AbstractMessage;
import net.metafusion.message.IMessageManager;

public interface ICommunicationHandler
{
	public void setServerConnectionInfo(AbstractConnectionInfo theServerConnectionInfo);

	public boolean canHandle(AbstractConnectionInfo serverInfo);
	
	public boolean write(AbstractMessage msg);

	public AbstractMessage read(Integer timeout);
}
