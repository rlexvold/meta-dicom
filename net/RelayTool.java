/*
 * Created by IntelliJ IDEA.
 * User: mb
 * Date: Dec 17, 2003
 * Time: 6:50:24 PM
 */
package net;
//testing
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import acme.util.Util;

/**
 * Created by IntelliJ IDEA. User: BreadenMx Date: Dec 17, 2003 Time: 6:07:04 PM
 * To change this template use Options | File Templates.
 */
public class RelayTool implements Runnable
{
	// static String HOST="10.42.3.20";
	static String HOST = "localhost";
	// static final String HOST="10.80.5.66";
	static int PORT_IN = 5107; // watch for PORT-1
	static int PORT_OUT = 5105; // watch for PORT-1

	static void log(String s)
	{
		System.out.println(s);
	}
	InetSocketAddress addr = new InetSocketAddress(HOST, PORT_OUT);
	Socket a;
	Socket b;
	InputStream isa;
	OutputStream osa;
	InputStream isb;
	OutputStream osb;
	static final int SIZE = 8192;
	boolean donea = false;
	boolean doneb = false;
	Runnable reada = new Runnable()
	{
		public void run()
		{
			log("reada");
			FileOutputStream fos = null;
			try
			{
				File f = Util.generateUniqueName(new File("."), "in", ".dat");
				fos = new FileOutputStream(f);
				byte buffer[] = new byte[SIZE];
				for (;;)
				{
					int cnt = isa.read(buffer, 0, SIZE);
					// Util.sleep(50);
					// log("reada "+cnt);
					if (cnt == -1)
					{
						log("closea");
						Util.safeClose(osb);
						Util.safeClose(isa);
						break;
					}
					fos.write(buffer, 0, cnt);
					osb.write(buffer, 0, cnt);
				}
			}
			catch (Exception e)
			{
				log("reada caught " + e);
				e.printStackTrace();
			}
			finally
			{
				Util.safeClose(fos);
			}
			donea = true;
		}
	};
	Runnable readb = new Runnable()
	{
		public void run()
		{
			log("readb");
			FileOutputStream fos = null;
			try
			{
				File f = Util.generateUniqueName(new File("."), "out", ".dat");
				fos = new FileOutputStream(f);
				byte buffer[] = new byte[SIZE];
				for (;;)
				{
					int cnt = isb.read(buffer, 0, SIZE);
					// Util.sleep(50);
					// log("readb "+cnt);
					if (cnt == -1)
					{
						log("closeb");
						Util.safeClose(osa);
						Util.safeClose(isb);
						break;
					}
					fos.write(buffer, 0, cnt);
					osa.write(buffer, 0, cnt);
				}
			}
			catch (Exception e)
			{
				log("readb caught " + e);
				e.printStackTrace();
			}
			finally
			{
				Util.safeClose(fos);
			}
			doneb = true;
		}
	};

	public void run()
	{
		try
		{
			while (!donea || !doneb)
			{
				Util.sleep(500);
			}
			log("donea and doneb");
			Util.safeClose(a);
			Util.safeClose(b);
		}
		catch (Exception e)
		{
			log("ab caught " + e);
			e.printStackTrace();
		}
	}

	RelayTool(Socket a) throws Exception
	{
		this.a = a;
		b = new Socket(addr.getAddress(), addr.getPort());
		isa = a.getInputStream();
		osa = a.getOutputStream();
		isb = b.getInputStream();
		osb = b.getOutputStream();
		new Thread(reada).start();
		new Thread(readb).start();
	}

	static void start() throws Exception
	{
		ServerSocket server = new ServerSocket(PORT_IN, 5);
		server.setReuseAddress(true);
		for (;;)
		{
			Socket socket = server.accept();
			log("accept");
			new Thread(new RelayTool(socket)).start();
		}
	}

	public static void main(String args[])
	{
		try
		{
			log("relay in_port out_host_ip out_port");
			log("e.g. relay 5107 10.42.3.20 5105");
			HOST = args[1];
			PORT_IN = Integer.parseInt(args[0]);
			PORT_OUT = Integer.parseInt(args[2]);
			log("start IN=" + PORT_IN + " => HOST=" + HOST + " PORT=" + PORT_OUT + " (" + new File(".").getAbsolutePath() + ")");
			RelayTool.start();
		}
		catch (Exception e)
		{
			log("relay caught " + e);
			e.printStackTrace();
		}
	}
}