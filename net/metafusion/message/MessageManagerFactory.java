package net.metafusion.message;

import java.util.ArrayList;
import net.metafusion.communication.AbstractConnectionInfo;
import net.metafusion.communication.CommunicationHandlerFactory;
import net.metafusion.communication.ICommunicationHandler;
import net.metafusion.dicom.message.DicomMessageManager;

public class MessageManagerFactory
{
	private static ArrayList<AbstractMessageManager> providerList = new ArrayList<AbstractMessageManager>();
	private static MessageManagerFactory instance = new MessageManagerFactory();

	public IMessageManager getMessageHandler(AbstractConnectionInfo serverInfo)
	{
		for (int i = 0; i < providerList.size(); i++)
		{
			try
			{
				if (providerList.get(i).canHandle(serverInfo.getMessageType()) == true)
				{
					AbstractMessageManager tmpHandler = providerList.get(i).getClass().newInstance();
					ICommunicationHandler comHandler = CommunicationHandlerFactory.getFactory().getCommunicationHandler(serverInfo);
					comHandler.setServerConnectionInfo(serverInfo);
					tmpHandler.setComHandler(comHandler);
					return tmpHandler;
				}
			}
			catch (Throwable e)
			{
			}
		}
		return null;
	}

	public static MessageManagerFactory getFactory()
	{
		if (instance == null) instance = new MessageManagerFactory();
		return instance;
	}

	public void addMessageHandler(AbstractMessageManager theHandler)
	{
		providerList.add(theHandler);
	}

	public MessageManagerFactory()
	{
		addMessageHandler(new DicomMessageManager());
	}
}