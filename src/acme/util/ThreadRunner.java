package acme.util;

public class ThreadRunner implements Runnable
{
	protected Runnable				runningObject;
	protected Thread				thread;
	volatile protected boolean		done		= false;
	volatile protected Exception	exception	= null;
	protected Long					id;

	public ThreadRunner(Runnable r)
	{
		this.runningObject = r;
	}

	public ThreadRunner()
	{
		this.runningObject = this;
	}

	public void start() throws Exception
	{
		done = false;
		exception = null;
		thread = new Thread(this);
		setId(thread.getId());
		thread.start();
	}

	public void run()
	{
		try
		{
			runningObject.run();
		}
		catch (Exception e)
		{
			if (e instanceof NestedException)
				exception = ((NestedException) e).getException();
			else
				exception = e;
		}
	}

	public void cancel()
	{
		if (!isDone() && (runningObject instanceof Cancellable))
		{
			((Cancellable) runningObject).cancel();
		}
	}

	public boolean isDone()
	{
		return done;
	}

	public Exception getException()
	{
		return exception;
	}

	public Thread getThread()
	{
		return thread;
	}

	public Runnable getRunningObject()
	{
		return runningObject;
	}

	public Long getId()
	{
		return id;
	}

	public void setId(Long id)
	{
		this.id = id;
	}

	public void suspendTask()
	{
		if (thread == null)
			return;
		thread.suspend();
	}

	public void resumeTask()
	{
		if (thread == null)
			return;
		thread.resume();
	}

	public void setTaskPriority(Integer priority)
	{
		if (thread == null)
			return;
		thread.setPriority(priority);
	}

	public void cancelTask()
	{
		if (thread == null)
			return;
		thread.stop();
	}
}