package net.metafusion.ris4d.commands;

import java.util.Date;
import java.util.HashMap;

import net.metafusion.ris4d.RisCommand;

public class AsyncCommand extends RisCommand
{
	private static HashMap<Long, RisCommand>	threadMap;
	private static AsyncCommand					instance	= null;
	private static String						name		= "AsyncCommand";

	public static AsyncCommand get()
	{
		if (instance == null)
			instance = new AsyncCommand();
		return instance;
	}

	public AsyncCommand()
	{
		super(name);
		threadMap = new HashMap<Long, RisCommand>();
	}

	public static HashMap<String, String> register(HashMap<String, String> map)
	{
		map = registerCommand(map, "cancelAsyncTask", name);
		map = registerCommand(map, "checkAsyncTaskStatus", name);
		map = registerCommand(map, "suspendAsyncTask", name);
		map = registerCommand(map, "resumeAsyncTask", name);
		map = registerCommand(map, "setAsyncTaskPriority", name);
		return map;
	}

	@Override
	public void run()
	{
		this.exception = null;
		Long taskId = Long.parseLong(args[1]);
		RisCommand tr = threadMap.get(taskId);
		if (tr == null)
			this.exception = new Exception("Invalid thread id: " + taskId);
		if (cmd.equalsIgnoreCase("cancelAsyncTask"))
		{
			tr.cancelTask();
			threadMap.remove(taskId);
			setResult("ok;\n");
		}
		else if (cmd.equalsIgnoreCase("checkAsyncTaskStatus"))
		{
			if (tr.isDone())
			{
				setResult(tr.getResult());
				this.exception = tr.getException();
				threadMap.remove(taskId);
			}
			else
			{
				setResult("running\n");
			}
		}
		else if (cmd.equalsIgnoreCase("suspendAsyncTask"))
		{
			tr.suspendTask();
			setResult("ok;\n");
		}
		else if (cmd.equalsIgnoreCase("resumeAsyncTask"))
		{
			tr.resumeTask();
			setResult("ok;\n");
		}
		else if (cmd.equalsIgnoreCase("setAsyncTaskPriority"))
		{
			tr.setTaskPriority(Integer.parseInt(args[1]));
			setResult("ok;\n");
		}
		else
		{
			this.exception = new Exception("AsyncCommand: Invalid command");
		}
		return;
	}

	public Long startAsyncTask(RisCommand theCommand) throws Exception
	{
		theCommand.start();
		Date now = new Date();
		threadMap.put(now.getTime(), theCommand);
		Exception e = theCommand.getException();
		if (e != null)
			throw e;
		return now.getTime();
	}
}
