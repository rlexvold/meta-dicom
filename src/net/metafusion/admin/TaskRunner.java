package net.metafusion.admin;

public class TaskRunner
{
	public void run(Runnable r) throws Exception
	{
		r.run();
	}

	public void runWithProgress(Runnable r, String message, int timeout) throws Exception
	{
		r.run();
	}
}