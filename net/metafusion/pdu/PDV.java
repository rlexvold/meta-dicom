package net.metafusion.pdu;

import acme.util.Util;

public class PDV
{
	byte b[];
	boolean cmd;
	boolean done;
	int id;

	static void log(String s)
	{
		Util.log(s);
	}

	public PDV(int id, boolean cmd, boolean done, byte[] b)
	{
		this.id = id;
		this.cmd = cmd;
		this.done = done;
		this.b = b;
	}

	public byte[] getBytes()
	{
		return b;
	}

	public int getId()
	{
		return id;
	}

	public boolean isCmd()
	{
		return cmd;
	}

	public boolean isData()
	{
		return !cmd;
	}

	public boolean isDone()
	{
		return done;
	}

	public void setBytes(byte[] b)
	{
		this.b = b;
	}

	public void setCmd(boolean cmd)
	{
		this.cmd = cmd;
	}

	public void setData(boolean data)
	{
		this.cmd = !data;
	}

	public void setDone(boolean done)
	{
		this.done = done;
	}

	public void setId(int id)
	{
		this.id = id;
	}

	public String toString()
	{
		return "PDV " + id + (cmd ? " CMD" : " DATA") + (done ? " DONE" : " CONT [" + b.length + "]");
	}
}
