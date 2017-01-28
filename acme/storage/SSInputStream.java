package acme.storage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

public class SSInputStream extends FileInputStream
{
	public SSInputStream(File f) throws IOException
	{
		super(f);
		positionToDataStart();
	}

	public void positionToMetaDataStart() throws IOException
	{
		this.getChannel().position(0);
	}

	public void positionToDataStart() throws IOException
	{
		this.getChannel().position(SSStore.METADATA_SIZE);
	}

	public SSMetaData getMeta() throws Exception
	{
		SSMetaData meta = null;
		try
		{
			positionToMetaDataStart();
			byte b[] = new byte[SSStore.METADATA_SIZE];
			long cnt = read(b);
			if (cnt != SSStore.METADATA_SIZE) throw new RuntimeException("file too small for metadata");
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b));
			meta = (SSMetaData) ois.readObject();
			b = null;
			ois.close();
		}
		finally
		{
			positionToDataStart();
		}
		return meta;
	}
}