package net.metafusion.ris4d.commands;

import integration.MFDemoStudy;

import java.util.HashMap;

import net.metafusion.ris4d.RisCommand;

public class DemoCommand extends RisCommand
{
	private static String	name	= "DemoCommand";
	private LogCommand		log		= null;

	public DemoCommand()
	{
		super(name);
		log = new LogCommand();
		log.setClient(this.client);
	}

	public static HashMap<String, String> register(HashMap<String, String> map)
	{
		map = registerCommand(map, "loadDemoStudies", name);
		return map;
	}

	@Override
	public void run()
	{
		String returnString = null;
		try
		{
			if (cmd.equalsIgnoreCase("loadDemoStudies"))
			{
				int i = 1;
				MFDemoStudy demo = new MFDemoStudy();
				if (args.length > i)
					demo.setStudyType(args[i++]);
				if (args.length > i)
				{
					Integer tmpInt = new Integer(1);
					try
					{
						tmpInt = Integer.parseInt(args[i++]);
					}
					catch (Exception e)
					{
						tmpInt = 1;
					}
					demo.setStudyNumber(tmpInt);
				}
				for (; i < args.length; i++)
				{
					String[] vals = args[i].split("=");
					if (vals.length == 2)
					{
						demo.addValue(vals[0], vals[1]);
					}
				}
				client.send(new Object[] { "loadDemoStudies", demo });
				returnString = (String) client.getObject("");
				if (returnString == null || returnString.length() == 0)
				{
					returnString = "error\n";
				}
			}
			else
			{
				returnString = "Invalid command";
				this.exception = new Exception(name + ", invalid command: " + cmd);
			}
			this.setResult(returnString);
			this.done = true;
		}
		catch (Exception e)
		{
			this.done = true;
			this.setResult("Error");
			this.exception = e;
		}
	}
}
