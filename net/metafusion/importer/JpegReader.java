package net.metafusion.importer;

import java.io.BufferedInputStream;
import java.io.FileInputStream;

import acme.util.Log;
import acme.util.Util;

public class JpegReader
{
	public ImageInfo readImageInfo(String filename) throws Exception
	{
		BufferedInputStream fs = null;
		ImageInfo ii = new ImageInfo();
		try
		{
			fs = new BufferedInputStream(new FileInputStream(filename));
			ii.setInput(fs);
			// check does the actual work, you won't get results before
			// you have called it
			if (!ii.check())
				Util.log("Not a supported image file format.");
		}
		catch (Exception e)
		{
			Log.log("ConverterListener.readSourceFile Error: ", e);
			throw e;
		}
		finally
		{
			if (fs != null)
				fs.close();
		}
		return ii;
	}
}
