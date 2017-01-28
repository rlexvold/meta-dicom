package net.metafusion.localstore.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;
import java.util.TreeSet;

public class ParseLogFile
{
	public static void main(String[] args)
	{
		TreeSet<String> ts = new TreeSet<String>();
		if (args.length == 0)
		{
			System.out.println("Usage: ParseLogFile <log file>");
			return;
		}
		for (int i = 0; i < args.length; i++)
		{
			File f = new File(args[i]);
			if (!f.exists())
			{
				System.out.println("File does not exist: " + f);
				continue;
			}
			try
			{
				BufferedReader fs = new BufferedReader(new FileReader(f));
				String saveLine = "";
				String tmpLine;
				while ((tmpLine = fs.readLine()) != null)
				{
					if (tmpLine.contains("######################################## process:"))
					{
						saveLine = tmpLine;
					}
					else if (tmpLine.contains("putMetaFile: could not rename"))
					{
						File dirFile = new File(saveLine.substring(saveLine.indexOf('/')));
						ts.add(dirFile.getParent());
					}
				}
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage());
			}
		}
		Iterator<String> i = ts.iterator();
		while (i.hasNext())
		{
			System.out.println("java -cp /metafusion/lib/classes.jar net.metafusion.localstore.tasks.LoadDcmFromFilesystem /metafusion/conf/metafusion.xml IZD_MetaStore01 "
					+ i.next());
		}
	}
}
