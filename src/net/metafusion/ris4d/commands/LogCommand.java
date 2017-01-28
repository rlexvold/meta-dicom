package net.metafusion.ris4d.commands;

import java.util.HashMap;

import net.metafusion.ris4d.RisCommand;

public class LogCommand extends RisCommand
{
	private static String	name	= "LogCommand";

	public LogCommand()
	{
		super(name);
	}

	@Override
	public void run()
	{
		if (cmd.equalsIgnoreCase("changeLogStatus"))
		{
			this.result = changeStatus(args[1], args[2]);
		}
		else if (cmd.equalsIgnoreCase("logTransferStart"))
		{
			this.result = logStart(args[1], args[2], args[3]);
		}
		else
		{
			this.exception = new Exception("LogCommand: unknown command" + cmd);
		}
		this.done = true;
	}

	public String changeStatus(String logId, String status)
	{
		client.send(new Object[] { "changeLogStatus", logId, status });
		return "";
	}

	public String logStart(String sourceFile, String destFile, String studyuid)
	{
		client.send(new Object[] { "logTransferStart", sourceFile, destFile, studyuid });
		return (String) client.getObject("");
	}

	public static HashMap<String, String> register(HashMap<String, String> map)
	{
		map = registerCommand(map, "changeLogStatus", name);
		map = registerCommand(map, "logTransferStart", name);
		return map;
	}
}
