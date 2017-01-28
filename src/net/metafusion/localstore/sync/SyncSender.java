package net.metafusion.localstore.sync;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import acme.storage.SSStore;
import acme.util.FileUtil;
import acme.util.Util;

public class SyncSender implements Runnable
{
	static void log(String s)
	{
		Util.log(s);
	}

	static void log(String s, Exception e)
	{
		Util.log(s);
	}
	String host;
	int port;

	public SyncSender(String host, int port)
	{
		this.host = host;
		this.port = port;
		init();
	}
	Socket s = null;
	ObjectOutputStream oos = null;
	ObjectInputStream ois = null;
	int lo = 100000000;
	int hi = 100000000;
	File syncRoot;
	static int start = 100000000;

	void init()
	{
		syncRoot = new File(SSStore.get().getSyncDir(), host + port);
		if (!syncRoot.exists()) syncRoot.mkdir();
		List l = FileUtil.listFiles(syncRoot, new String[] { ".log" });
		File fs[] = (File[]) l.toArray(new File[l.size()]);
		Arrays.sort(fs);
		if (fs.length == 0)
			hi = lo = start;
		else
		{
			lo = Integer.parseInt(fs[0].getName().substring(0, fs[0].getName().length() - 4));
			hi = Integer.parseInt(fs[fs.length - 1].getName().substring(0, fs[0].getName().length() - 4)) + 1;
		}
	}

	synchronized SyncMsg peek()
	{
		if (lo >= hi) return null;
		File f = new File(syncRoot, "" + lo + ".log");
		SyncMsg m = (SyncMsg) Util.readObjectFromFile(f);
		if (m.id != lo)
		{
			log("SyncMsg peek badid expected id " + lo + " " + m);
			remove();
			return null;
		}
		return m;
	}

	synchronized boolean remove()
	{
		File f = new File(syncRoot, "" + lo + ".log");
		if (!f.exists())
		{
			log("sync:  removeLo exists failed " + f.getAbsolutePath());
			return false;
		}
		boolean b = f.delete();
		if (!b)
		{
			log("sync: removeLo delete failed " + f.getAbsolutePath());
			return false;
		}
		lo++;
		return true;
	}

	synchronized void add(SyncMsg sm)
	{
		sm.id = hi;
		File f = new File(syncRoot, "" + hi + ".log");
		boolean b = Util.writeObjectToFile(sm, f);
		if (!b)
			log("sync: add writeObjectToFile failed");
		else hi++;
		notify();
	}

	boolean connect()
	{
		if (s != null) return true;
		try
		{
			s = new Socket(host, port);
			oos = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
			oos.writeObject("CONNECT 1.0");
			oos.flush();
			ois = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
			String s = (String) ois.readObject();
			if (s == null || !s.startsWith("OK")) throw new RuntimeException("bad connect reply " + s);
		}
		catch (Exception e)
		{
			close();
			return false;
		}
		return true;
	}

	void close()
	{
		Util.safeClose(oos);
		Util.safeClose(ois);
		Util.safeClose(s);
		oos = null;
		ois = null;
		s = null;
	}

	boolean send(SyncMsg sm)
	{
		FileInputStream fis = null;
		long fileSize = 0;
		try
		{
			if (sm.id != lo)
			{
				log("!!!!!bad id (skipping) lo=" + lo + " " + sm);
				return true;
			}
			if (sm.file != null)
			{
				File f = sm.file;
				if (!f.exists() || !f.isFile())
				{
					log("!!!!!cannot locate sync file (skipping)" + sm);
					return true;
				}
				try
				{
					fis = new FileInputStream(f);
				}
				catch (FileNotFoundException e)
				{
					if (fis == null)
					{
						log("!!!!!cannot open sync file (skipping)" + sm);
						return true;
					}
				}
				fileSize = sm.file.length();
				sm.length = fileSize;
			}
			oos.writeObject(sm);
			oos.flush();
			if (fileSize != 0)
			{
				Util.copyStream(fis, oos);
				fis.close();
				fis = null;
				oos.flush();
			}
			String resp = (String) ois.readObject();
			if (resp == null || !resp.startsWith("OK")) log("!!! sync fail (cont) " + resp + " " + sm);
			return true;
		}
		catch (Exception e)
		{
			log("SyncSender caught (disconnecting)" + e);
			close();
			return false;
		}
		finally
		{
			Util.safeClose(fis);
		}
	}

	public void run()
	{
		for (;;)
		{
			// log("peek");
			SyncMsg sm = peek();
			if (sm == null)
			{
				Util.wait(this, 30000);
				continue;
			}
			// log("connect");
			if (!connect())
			{
				Util.sleep(15000);
				continue;
			}
			if (send(sm)) remove();
		}
	}
}