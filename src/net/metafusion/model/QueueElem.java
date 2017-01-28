package net.metafusion.model;

class QueueElem
{
	private long id;
	private String name;
	private String msg;
	private long argl;
	private String args;

	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getMsg()
	{
		return msg;
	}

	public void setMsg(String msg)
	{
		this.msg = msg;
	}

	public long getArgl()
	{
		return argl;
	}

	public void setArgl(long argl)
	{
		this.argl = argl;
	}

	public String getArgs()
	{
		return args;
	}

	public void setArgs(String args)
	{
		this.args = args;
	}

	@Override
	public String toString()
	{
		return "QueueElem[" + id + "]: msg=" + msg + " argl=" + argl + " args=" + args;
	}
}
