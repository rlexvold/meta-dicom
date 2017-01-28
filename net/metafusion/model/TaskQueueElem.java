package net.metafusion.model;

public class TaskQueueElem
{
	private long id;
	private long time;
	private String action;
	private String arg1;
	private String arg2;
	private String arg3;
	private int attempts;
	private String status;
	private int maxAttempts;
	private int intervalMS;

	@Override
	public String toString()
	{
		return "TQE: id=" + id + ",action=" + action + ",arg1=" + arg1;
	}

	public TaskQueueElem()
	{
	}

	public TaskQueueElem(long time, String action, String arg1, String arg2, String arg3, int maxAttempts, int intervalMS)
	{
		this.id = 0;
		this.time = time;
		this.action = action;
		this.arg1 = arg1;
		this.arg2 = arg2;
		this.arg3 = arg3;
		this.attempts = 0;
		this.status = "P";
		this.maxAttempts = maxAttempts;
		this.intervalMS = intervalMS;
	}

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public long getTime()
	{
		return time;
	}

	public void setTime(long time)
	{
		this.time = time;
	}

	public String getAction()
	{
		return action;
	}

	public void setAction(String action)
	{
		this.action = action;
	}

	public String getArg1()
	{
		return arg1;
	}

	public void setArg1(String arg1)
	{
		this.arg1 = arg1;
	}

	public String getArg2()
	{
		return arg2;
	}

	public void setArg2(String arg2)
	{
		this.arg2 = arg2;
	}

	public String getArg3()
	{
		return arg3;
	}

	public void setArg3(String arg3)
	{
		this.arg3 = arg3;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public boolean hasFailed()
	{
		return status.equals("F");
	}

	public boolean hasSucceeded()
	{
		return status.equals("T");
	}

	public boolean isPending()
	{
		return status.equals("P");
	}

	public int getAttempts()
	{
		return attempts;
	}

	public void setAttempts(int attempts)
	{
		this.attempts = attempts;
	}

	public int getMaxAttempts()
	{
		return maxAttempts;
	}

	public void setMaxAttempts(int maxAttempts)
	{
		this.maxAttempts = maxAttempts;
	}

	public int getIntervalMS()
	{
		return intervalMS;
	}

	public void setIntervalMS(int intervalMS)
	{
		this.intervalMS = intervalMS;
	}
}
