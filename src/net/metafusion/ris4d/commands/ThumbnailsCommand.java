package net.metafusion.ris4d.commands;

import integration.MFFileInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import net.metafusion.ris4d.RisCommand;
import acme.util.FileUtil;
import acme.util.Util;
import acme.util.rsync.RsyncServer;

public class ThumbnailsCommand extends RisCommand
{
	private static String	name	= "ThumbnailsCommand";

	public ThumbnailsCommand()
	{
		super(name);
	}

	@Override
	public void run()
	{
		String returnString = null;
		client.send(new Object[] { cmd, args[1] });
		File destDir = new File(args[2]);
		if (!destDir.exists())
			destDir.mkdirs();
		ArrayList<MFFileInfo> returnList = (ArrayList<MFFileInfo>) client.getObject("ERROR");
		try
		{
			RsyncServer rm = new RsyncServer();
			for (int i = 0; i < returnList.size(); i++)
			{
				MFFileInfo fileInfo = returnList.get(i);
				File destFile = new File(destDir, fileInfo.getFileId() + ".jpg");
				rm.get(client, fileInfo.getSourceFile(), destFile);
			}
			this.setResult("ok;\n");
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
		map = registerCommand(map, "createThumbnailsByStudyUID", name);
		map = registerCommand(map, "createThumbnailsBySeriesUID", name);
		return map;
	}
}
