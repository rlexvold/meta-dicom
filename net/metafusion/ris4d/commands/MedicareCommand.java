package net.metafusion.ris4d.commands;

import java.util.HashMap;

import net.metafusion.medicare.hic.HICCA;
import net.metafusion.medicare.ictk.ICtk;
import net.metafusion.ris4d.RisCommand;
import acme.util.Log;

public class MedicareCommand extends RisCommand
{
	private static String	name			= "MedicareCommand";
	private LogCommand		log				= null;
	public static int		csvDelimiter	= ',';

	public MedicareCommand()
	{
		super(name);
		log = new LogCommand();
		log.setClient(this.client);
	}

	public static HashMap<String, String> register(HashMap<String, String> map)
	{
		map = registerCommand(map, "iCtk", name);
		map = registerCommand(map, "hicca", name);
		return map;
	}

	@Override
	public void run()
	{
		String returnString = null;
		try
		{
			if (cmd.equalsIgnoreCase("hicca"))
			{
				Log.log("hicca;" + args[1]);
				String file = HICCA.getInstance().process(args[1], csvDelimiter);
				returnString = file != null ? "ok;" + file + "\n" : "err;\n";
			}
			else if (cmd.equalsIgnoreCase("iCtk"))
			{
				Log.log("iCtk;" + args[1]);
				String file = ICtk.getInstance().process(args[1], csvDelimiter);
				returnString = file != null ? "ok;" + file + "\n" : "err;\n";
			}
			else
			{
				returnString = "Invalid command";
				this.exception = new Exception("MedicareCommand, invalid command: " + cmd);
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
