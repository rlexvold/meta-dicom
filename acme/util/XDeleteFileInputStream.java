package acme.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class XDeleteFileInputStream extends FileInputStream
{
	private File file = null;

	public XDeleteFileInputStream(File file) throws FileNotFoundException
	{
		super(file);
		this.file = file;
	}

	public void close() throws IOException
	{
		try
		{
			super.close();
		}
		finally
		{
			FileUtil.safeDelete(file);
		}
	}
}