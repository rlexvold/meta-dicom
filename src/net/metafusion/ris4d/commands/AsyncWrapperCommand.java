package net.metafusion.ris4d.commands;

import java.util.HashMap;

import net.metafusion.ris4d.Ris4DCommand;
import net.metafusion.ris4d.RisCommand;

public class AsyncWrapperCommand extends RisCommand
{
	private static String	name	= "AsyncWrapperCommand";
	private Ris4DCommand	risCmd	= null;

	public AsyncWrapperCommand()
	{
		super(name);
	}

	public static HashMap<String, String> register(HashMap<String, String> map)
	{
		map = registerCommand(map, "AsyncWrapper", name);
		return map;
	}

	@Override
	public void run()
	{
		if (risCmd == null)
		{
			this.done = true;
			this.exception = new RuntimeException("AsyncWrapperCommand: risCmd is NULL");
			return;
		}
		risCmd.setArgs(getArgs());
		risCmd.setClient(getClient());
		try
		{
			this.result = risCmd.getResult();
			this.done = true;
			return;
		}
		catch (Exception e)
		{
			this.done = true;
			this.exception = e;
			return;
		}
	}

	public Ris4DCommand getRisCmd()
	{
		return risCmd;
	}

	public void setRisCmd(Ris4DCommand risCmd)
	{
		this.risCmd = risCmd;
	}
}
