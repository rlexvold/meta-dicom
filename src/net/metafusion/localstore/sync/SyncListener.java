package net.metafusion.localstore.sync;

import java.net.ServerSocket;
import java.net.Socket;
import acme.util.Util;

public class SyncListener implements Runnable
{
	static void log(String s)
	{
		Util.log(s);
	}

	static void log(String s, Exception e)
	{
		Util.log(s);
	}
	private int port;

	public SyncListener(int port) throws Exception
	{
		this.port = port;
		ss = new ServerSocket(port);
	}
	private ServerSocket ss = null;

	public void run()
	{
		while (!ss.isClosed())
			try
			{
				Socket s = ss.accept();
				Util.startDaemonThread(new SyncReceiver(s));
			}
			catch (Exception e)
			{
				log("ServerSocket caught (cont) ", e);
			}
	}

	public void close()
	{
		Util.safeClose(ss);
	}
}