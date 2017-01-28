package net.metafusion.communication;

import java.util.ArrayList;
import net.metafusion.dicom.communication.DicomSocketAdapter;

public class CommunicationHandlerFactory
{
	private static ArrayList<ICommunicationHandler> providerList = new ArrayList<ICommunicationHandler>();
	private static CommunicationHandlerFactory instance = new CommunicationHandlerFactory();

	public ICommunicationHandler getCommunicationHandler(AbstractConnectionInfo serverInfo)
	{
		for (int i = 0; i < providerList.size(); i++)
		{
			try
			{
				if (providerList.get(i).canHandle(serverInfo) == true)
				{
					ICommunicationHandler tmpHandler = providerList.get(i).getClass().newInstance();
					tmpHandler.setServerConnectionInfo(serverInfo);
					return tmpHandler;
				}
			}
			catch (Throwable e)
			{
			}
		}
		return null;
	}

	public static CommunicationHandlerFactory getFactory()
	{
		if (instance == null) instance = new CommunicationHandlerFactory();
		return instance;
	}

	public void addCommunicationHandler(ICommunicationHandler theHandler)
	{
		providerList.add(theHandler);
	}

	public CommunicationHandlerFactory()
	{
		addCommunicationHandler(new DicomSocketAdapter());
	}
}