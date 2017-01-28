package integration;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public class MFInputStream extends DataInputStream
{
	private InputStream is;

	public MFInputStream(InputStream is)
	{
		super(is);
		this.is = is;
	}

	public Object readObject() throws Exception
	{
		int size = 0;
		try
		{
			size = readInt();
		}
		catch (IOException e)
		{
			return null;
		}
		// if (size < 0 || size > 1000000) throw new
		// RuntimeException("readObject buffer too big:" + size);
		// RAL - removed 1MB check
		if (size < 0) throw new RuntimeException("readObject buffer too big:" + size);
		byte buffer[] = new byte[size];
		readFully(buffer);
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buffer));
		return ois.readObject();
	}
}