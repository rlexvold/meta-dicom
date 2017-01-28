package net.metafusion.ris4d.commands;

import java.util.ArrayList;
import java.util.HashMap;

import net.metafusion.archive.ArchiveJob;
import net.metafusion.archive.ArchiveSystem;
import net.metafusion.ris4d.RisCommand;
import net.metafusion.util.AE;
import net.metafusion.util.AEMap;
import acme.util.Util;

public class ArchiveCommand extends RisCommand
{
	private static String	name	= "ArchiveCommand";
	private LogCommand		log		= null;

	public ArchiveCommand()
	{
		super(name);
		log = new LogCommand();
		log.setClient(this.client);
	}

	public static HashMap<String, String> register(HashMap<String, String> map)
	{
		map = registerCommand(map, "archiveStudies", name);
		return map;
	}

	@Override
	public void run()
	{
		String returnString = null;
		try
		{
			if (cmd.equalsIgnoreCase("archiveStudies"))
			{
				ArchiveJob job = ArchiveSystem.newJob();
				job.setArchiveSystem(args[1]);
				job.setMediaSize(Long.parseLong(args[2]));
				String[] studies = args[3].split(",");
				for (int i = 0; i < studies.length; i++)
				{
					job.addStudy(studies[i]);
				}
				client.send(new Object[] { "archiveJob", job });
				job = (ArchiveJob) client.getObject("");
				returnString = "ok;" + job.getResult();
				ArrayList<Exception> errors = job.getErrors();
				if (errors.size() > 0)
					this.exception = errors.get(0);
				else
					this.exception = null;
			}
			else
			{
				returnString = "Invalid command";
				this.exception = new Exception("ArchiveCommand, invalid command: " + cmd);
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
