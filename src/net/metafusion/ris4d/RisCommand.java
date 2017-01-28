package net.metafusion.ris4d;

import integration.MFClient;

import java.util.HashMap;

import acme.util.ThreadRunner;

public abstract class RisCommand extends ThreadRunner
{
	protected static String	name;
	protected static String	packageName	= "net.metafusion.ris4d.commands.";
	protected String		result;
	protected MFClient		client;
	protected String		cmd;
	protected String[]		args;

	public RisCommand(String name)
	{
		super();
		this.name = name;
	}

	public static HashMap<String, String> register(HashMap<String, String> map)
	{
		return null;
	}

	public abstract void run();

	protected static HashMap<String, String> registerCommand(HashMap<String, String> map, String command, String name)
	{
		map.put(command.toLowerCase(), packageName + name);
		return map;
	}

	public String getResult()
	{
		return result;
	}

	public void setResult(String result)
	{
		this.result = result;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public MFClient getClient()
	{
		return client;
	}

	public void setClient(MFClient client)
	{
		this.client = client;
	}

	public String getCmd()
	{
		return cmd;
	}

	public void setCmd(String cmd)
	{
		this.cmd = cmd;
	}

	public String[] getArgs()
	{
		return args;
	}

	public void setArgs(String[] args)
	{
		this.args = args;
	}
}
