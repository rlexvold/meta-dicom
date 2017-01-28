package net.metafusion.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;

import net.metafusion.localstore.service.DicomServiceProvider;
import net.metafusion.util.AE;
import net.metafusion.util.AEMap;
import net.metafusion.util.RoleMap;
import acme.util.Log;
import acme.util.Util;
import acme.util.XMLConfigFile;

public class DicomServer implements Runnable
{
	HashMap			providerMap	= new HashMap();
	RoleMap			roleMap;
	AE				serverAE	= null;
	AE				serverAE2	= null;
	ServerSocket	ss;
	ServerSocket	ss2;
	ServerSocket	ssZip;

	public DicomServer() throws Exception
	{
		DicomServerConfig config = new DicomServerConfig(XMLConfigFile.getDefault().getXML().getNode("dicomserver"));
		Log.log("enter DicomServer");
		serverAE = AEMap.get(XMLConfigFile.getDefault().getXML().get("ae"));
		AEMap.setDefault(serverAE);
		Thread.yield();
		if (XMLConfigFile.getDefault().getXML().get("ae2") != null)
			serverAE2 = AEMap.get(XMLConfigFile.getDefault().getXML().get("ae2"));
		Thread.sleep(1000);
		Log.log("about to call new serversocket port =" + serverAE.getPort());
		ss = new ServerSocket(serverAE.getPort(), 200);
		Log.log("about to call new setReuseAddress");
		ss.setReuseAddress(true);
		Log.log("DicomServer start: " + serverAE);
		roleMap = config.getRoleMap(); // new RoleMap(xml.getNode("roles"));
		if (serverAE2 != null)
		{
			AEMap.setAlt(serverAE2);
			int port1 = serverAE.getPort();
			int port2 = serverAE2.getPort();
			if (port1 != port2)
			{
				Log.log("about to start serverAE2");
				Log.log("about to call new serversocket2 port =" + serverAE.getPort());
				ss2 = new ServerSocket(serverAE2.getPort(), 200);
				// ss2.setSoTimeout(30000);
				Log.log("about to call new setReuseAddress2");
				ss2.setReuseAddress(true);
				Log.log("DicomServer start2: " + serverAE);
			}
		}
		if (serverAE.getZipPort() != null)
		{
			Log.log("about to start compress server");
			Log.log("about to call new serversocketZip port =" + serverAE.getZipPort());
			ssZip = new ServerSocket(serverAE.getZipPort(), 200);
			ssZip.setReuseAddress(true);
			Log.log("DicomServer startZip: " + serverAE);
		}
	}

	public void addServiceProvider(int cmd, Class dicomServiceProvider)
	{
		Constructor cons = dicomServiceProvider.getConstructors()[0];
		providerMap.put(new Integer(cmd), cons);
	}

	public void close()
	{
		Util.safeClose(ss);
		Util.safeClose(ss2);
		Util.safeClose(ssZip);
	}

	public AE getServerAE()
	{
		return serverAE;
	}

	public DicomServiceProvider getServiceProvider(int cmd, DicomSession sess)
	{
		DicomServiceProvider dsp = null;
		try
		{
			Constructor cons = (Constructor) providerMap.get(new Integer(cmd));
			if (cons != null)
				dsp = (DicomServiceProvider) cons.newInstance(new Object[] { sess });
		}
		catch (Exception e)
		{
			Log.log("setServiceProvider " + cmd + " caught...", e);
		}
		return dsp;
	}

	protected void handleRawRequest(Socket s, InputStream is, OutputStream os) throws Exception
	{
	}

	public void run()
	{
		try
		{
			if (ss2 != null)
			{
				Thread alt = new Thread(new Runnable()
				{
					public void run()
					{
						run2();
					}
				});
				alt.setName("Alt DicomServerThread");
				alt.start();
			}
		}
		catch (Exception e)
		{
			Log.log("could not start alt server thread", e);
		}
		try
		{
			if (ssZip != null)
			{
				Log.log("Starting zip thread...");
				Thread zip = new Thread(new Runnable()
				{
					public void run()
					{
						runZip();
					}
				});
				zip.setName("Zip DicomServerThread");
				zip.start();
			}
		}
		catch (Exception e)
		{
			Log.log("could not start zip server thread", e);
		}
		try
		{
			while (ss != null && !ss.isClosed())
			{
				Socket sock = null;
				try
				{
					sock = ss.accept();
					// sock.setSoLinger(true, 300);
				}
				catch (java.net.SocketTimeoutException e)
				{
					// timeout
					continue;
				}
				// Log.force("accept " + sock.getInetAddress());
				// acme.util.Stats.inc("tcp.accept");
				try
				{
					DicomServerSession s = new DicomServerSession(this, sock, false);
					Thread serve = new Thread(s);
					serve.setName("DicomServerionSessionThread");
					serve.start();
				}
				catch (Exception e)
				{
					Log.log("new DicomServerSession caught ", e);
					Util.safeClose(sock);
				}
			}
		}
		catch (IOException e)
		{
			if (e instanceof SocketException && e.getMessage().equals("socket closed"))
				Log.log("server socket closed");
			else
				Log.log("DicomServer caught " + e);
		}
		finally
		{
			Util.safeClose(ss);
		}
	}

	public void run2()
	{
		try
		{
			while (ss2 != null && !ss2.isClosed())
			{
				Socket sock = null;
				try
				{
					sock = ss2.accept();
					// sock.setSoLinger(true, 300);
				}
				catch (java.net.SocketTimeoutException e)
				{
					// timeout
					continue;
				}
				// Log.force("accept " + sock.getInetAddress());
				// acme.util.Stats.inc("tcp.accept");
				try
				{
					DicomServerSession s = new DicomServerSession(this, sock, false);
					Thread serve = new Thread(s);
					serve.setName("Alt DicomServerSessionThread");
					serve.start();
				}
				catch (Exception e)
				{
					Log.log("new DicomServerSession2 caught ", e);
					Util.safeClose(sock);
				}
			}
		}
		catch (IOException e)
		{
			if (e instanceof SocketException && e.getMessage().equals("socket closed"))
				Log.log("server socket closed2");
			else
				Log.log("DicomServer caught2 " + e);
		}
		finally
		{
			Util.safeClose(ss2);
		}
	}

	public void runZip()
	{
		try
		{
			Log.log("runZip");
			while (ssZip != null && !ssZip.isClosed())
			{
				Log.log("RunZip waiting for connection");
				Socket sock = null;
				try
				{
					sock = ssZip.accept();
				}
				catch (java.net.SocketTimeoutException e)
				{
					// timeout
					continue;
				}
				try
				{
					DicomServerSession s = new DicomServerSession(this, sock, true);
					new Thread(s).start();
				}
				catch (Exception e)
				{
					Log.log("new DicomServerSessionZip caught ", e);
					Util.safeClose(sock);
				}
			}
		}
		catch (IOException e)
		{
			if (e instanceof SocketException && e.getMessage().equals("socket closed"))
				Log.log("server socket closed Zip");
			else
				Log.log("DicomServer caughtZip " + e);
		}
		finally
		{
			Util.safeClose(ssZip);
		}
	}
}
