/*
 * Created by IntelliJ IDEA.
 * User: mb
 * Date: Oct 24, 2003
 * Time: 6:31:43 PM
 */
package acme.util;

import java.io.File;

class FileMonitor implements Runnable
{
	private FileMonitor(File f)
	{
		this.f = f;
		sleep = 30000;
		if (f.exists())
			lastMod = f.lastModified();
		else lastMod = -1;
	}

	private FileMonitor(File f, int sleep)
	{
		this.f = f;
		this.sleep = sleep;
		if (f.exists())
			lastMod = f.lastModified();
		else lastMod = -1;
	}
	private int sleep;
	private File f;
	private long lastMod;

	public File getFile()
	{
		return f;
	}

	public void run()
	{
		for (;;)
		{
			if (f.exists())
			{
				if (lastMod == -1)
				{
					lastMod = f.lastModified();
					onCreate();
				} else if (lastMod != f.lastModified())
				{
					lastMod = f.lastModified();
					onUpdate();
				}
			} else
			{
				if (lastMod != -1)
				{
					lastMod = -1;
					onDelete();
				}
			}
			Util.sleep(sleep);
		}
	}

	protected void onUpdate()
	{
		System.out.println("onUpdate: " + f.getName());
	}

	protected void onDelete()
	{
		System.out.println("onDelete: " + f.getName());
	}

	protected void onCreate()
	{
		System.out.println("onCreate: " + f.getName());
	}
}