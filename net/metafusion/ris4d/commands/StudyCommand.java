package net.metafusion.ris4d.commands;

import java.util.HashMap;

import net.metafusion.ris4d.RisCommand;

public class StudyCommand extends RisCommand
{
	private static String	name	= "StudyCommand";

	public StudyCommand()
	{
		super(name);
	}

	@Override
	public void run()
	{
		String returnString = null;
		if (cmd.equalsIgnoreCase("deleteStudy"))
		{
			client.send(new Object[] { "deleteStudy", args[1] });
			returnString = (String) client.getObject("");
			if (returnString == null || returnString.length() == 0)
			{
				returnString = "error\n";
			}
		}
		else if (cmd.equalsIgnoreCase("mergeStudies"))
		{
			client.send(new Object[] { "mergeStudies", args[1], args[2], args[3], args[4] });
			returnString = (String) client.getObject("");
			if (returnString == null || returnString.length() == 0)
			{
				returnString = "error\n";
			}
		}
		else
		{
			returnString = "Invalid command";
			this.exception = new Exception("Invalid command");
		}
		this.setResult(returnString);
		this.done = true;
	}

	public static HashMap<String, String> register(HashMap<String, String> map)
	{
		map = registerCommand(map, "deleteStudy", name);
		map = registerCommand(map, "mergeStudies", name);
		return map;
	}
}
