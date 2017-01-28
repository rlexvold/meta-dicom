package net.metafusion.localstore;

import integration.MFServer;
import java.net.ServerSocket;
import java.net.Socket;
import acme.util.Log;
import acme.util.Util;

public class RISProcess
{
	static void log(String s)
	{
		Log.log(s);
	}

	static void vlog(String s)
	{
		Log.vlog(s);
	}

	static void log(String s, Exception e)
	{
		Log.log(s, e);
	}
	static RISProcess	instance	= null;

	public static void init(int port) throws Exception
	{
		log("RIS.init " + port);
		MFServer.init();
		Util.startDaemonThread(new RISServer(port));
	}

	public static RISProcess get()
	{
		return instance;
	}

	public RISProcess()
	{
		instance = this;
	}
	static class RISServer implements Runnable
	{
		MFServer	mfserver	= MFServer.get();
		int			port;

		public RISServer(int port) throws Exception
		{
			this.port = port;
		}

		public void run()
		{
			try
			{
				ServerSocket ss = new ServerSocket(port);
				for (;;)
					try
					{
						Socket s = ss.accept();
						// s.setSoLinger(true,5);
						RISSession bs = new RISSession(mfserver, s);
						Util.startDaemonThread(bs, false);
					}
					catch (Exception e)
					{
						log("risess", e);
					}
			}
			catch (Exception e)
			{
				log("RISServer caught (exit) port=" + port, e);
			}
		}
	}
	static class RISSession implements Runnable
	{
		Socket		s;
		MFServer	server;

		public RISSession(MFServer server, Socket s) throws Exception
		{
			this.server = server;
			this.s = s;
		}

		public void run()
		{
			try
			{
				server.handleRequest(s);
			}
			catch (Exception e)
			{
				log("RISSession caught", e);
			}
			finally
			{
				// vlog("close");
				Util.safeClose(s);
			}
		}
	}
}