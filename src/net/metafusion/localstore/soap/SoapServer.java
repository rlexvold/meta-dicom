package net.metafusion.localstore.soap;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import net.metafusion.localstore.LocalStore;
import acme.util.Util;

public class SoapServer implements Runnable
{
	static void log(String s)
	{
		Util.log(s);
	}

	static void log(String s, Exception e)
	{
		Util.log(s);
	}

	public static void init(int port)
	{
		log("soap init:" + port);
		SoapServer s = new SoapServer(port);
		Util.startDaemonThread(s);
	}
	private int port;

	public SoapServer(int port)
	{
		this.port = port;
	}
	private ServerSocket ss = null;

	public void run()
	{
		try
		{
			ss = new ServerSocket(port);
			for (;;)
				try
				{
					Socket s = ss.accept();
					if (!LocalStore.get().IsSync())
					{
						log("Soap request with no sync");
						s.close();
						continue;
					}
					Util.startDaemonThread(new SoapRequest(this, s));
				}
				catch (IOException e)
				{
					log("SoapServer caught ", e);
				}
		}
		catch (Exception e)
		{
			log("SoapServer caught ", e);
		}
	}

	public void close()
	{
		Util.safeClose(ss);
	}

	public static void main(String[] args)
	{
		log("soap start");
		SoapServer s = new SoapServer(9090);
		s.run();
	}
}