package net.metafusion.ris4d.commands;

import integration.MFClient;
import integration.MFFileInfo;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import net.metafusion.ris4d.RisCommand;
import acme.util.CompressionStream;
import acme.util.Util;
import acme.util.rsync.RsyncServer;

public class RichMediaCommand extends RisCommand
{
	private static String	name	= "RichMediaCommand";
	private LogCommand		log		= null;

	public RichMediaCommand()
	{
		super(name);
		log = new LogCommand();
		log.setClient(this.client);
	}

	@Override
	public void setClient(MFClient client)
	{
		log.setClient(client);
		this.client = client;
	}

	@Override
	public void run()
	{
		String returnString = null;
		try
		{
			if (cmd.equalsIgnoreCase("putRichMedia"))
			{
				returnString = put(args[1], args[2]);
			}
			else if (cmd.equalsIgnoreCase("putRichMediaFile"))
			{
				returnString = putFile(args[1], args[2]);
			}
			else if (cmd.equalsIgnoreCase("getRichMedia"))
			{
				returnString = getDir(args[1], args[2]);
			}
			else if (cmd.equalsIgnoreCase("getRichMediaFile"))
			{
				returnString = getFile(args[1], args[2], args[3]);
			}
			else if (cmd.equalsIgnoreCase("getRichMediaFileList"))
			{
				returnString = "ok;";
				String[] files = getFileList(args[1]);
				for (int i = 0; i < files.length; i++)
				{
					returnString = returnString + files + ".";
				}
				returnString += "\n";
			}
			else
			{
				returnString = "Invalid command";
				this.exception = new Exception("RichMediaCommand, invalid command: " + cmd);
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

	public static HashMap<String, String> register(HashMap<String, String> map)
	{
		map = registerCommand(map, "putRichMedia", name);
		map = registerCommand(map, "getRichMedia", name);
		map = registerCommand(map, "putRichMediaFile", name);
		map = registerCommand(map, "getRichMediaFile", name);
		map = registerCommand(map, "getRichMediaFileList", name);
		return map;
	}

	public String[] getFileList(String studyuid) throws Exception
	{
		client.send(new Object[] { "getRichMediaFileList", studyuid });
		String[] files = (String[]) client.getObject("");
		return files;
	}

	public String getDir(String studyuid, String dest) throws Exception
	{
		String logId = log.logStart("", dest, studyuid);
		String[] files = getFileList(studyuid);
		for (int i = 0; i < files.length; i++)
		{
			getFile(studyuid, files[i], dest, false);
		}
		log.changeStatus(logId, "finished");
		return "ok;" + logId + "\n";
	}

	public String put(String studyuid, String source) throws Exception
	{
		File sourceFile = new File(source);
		if (sourceFile.exists() == false)
		{
			return "err;File/Directory not found: " + source + "\n";
		}
		if (sourceFile.isFile())
		{
			return putFile(studyuid, source);
		}
		String logId = log.logStart(source, "", studyuid);
		String[] files = sourceFile.list();
		if (files != null)
		{
			for (int i = 0; i < files.length; i++)
			{
				File tmpFile = new File(sourceFile, files[i]);
				if (tmpFile.isFile())
					putFile(studyuid, tmpFile.getAbsolutePath(), false);
			}
		}
		log.changeStatus(logId, "finished");
		return "ok;" + logId + "\n";
	}

	public String getFile(String studyuid, String filename, String dest) throws Exception
	{
		return getFile(studyuid, filename, dest, true);
	}

	public String getFile(String studyuid, String filename, String dest, boolean logFlag) throws Exception
	{
		String logId = "";
		client.send(new Object[] { "getRichMediaDir", studyuid });
		String mediaDir = (String) client.getObject("");
		File sourceFile = new File(mediaDir, filename);
		if (logFlag)
		{
			logId = log.logStart(sourceFile.getAbsolutePath(), dest, studyuid);
		}
		File destFile = new File(dest, sourceFile.getName());
		RsyncServer rm = new RsyncServer();
		rm.get(client, sourceFile, destFile);
		if (logFlag)
		{
			log.changeStatus(logId, "finished");
		}
		return "ok;" + logId + "\n";
	}

	public String putFile(String studyuid, String source) throws Exception
	{
		return putFile(studyuid, source, true);
	}

	public String putFile(String studyuid, String source, boolean logFlag) throws Exception
	{
		String logId = "";
		client.send(new Object[] { "getRichMediaDir", studyuid });
		String mediaDir = (String) client.getObject("");
		File sourceFile = new File(source);
		File destFile = new File(mediaDir, sourceFile.getName());
		if (logFlag)
		{
			logId = log.logStart(sourceFile.getAbsolutePath(), destFile.getAbsolutePath(), studyuid);
		}
		RsyncServer rm = new RsyncServer();
		boolean result = rm.put(client, sourceFile, destFile);
		if (result)
		{
			if (logFlag)
			{
				log.changeStatus(logId, "finished");
			}
			return "ok;" + logId + "\n";
		}
		else
		{
			if (logFlag)
			{
				log.changeStatus(logId, "error");
			}
			return "err;" + logId + "\n";
		}
	}
}
