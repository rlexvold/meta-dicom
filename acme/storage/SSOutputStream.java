package acme.storage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import acme.util.XByteArrayOutputStream;

class SSOutputStream extends FileOutputStream
{
	private SSOutputStream(File f) throws IOException
	{
		super(f);
		if (this.getChannel().position() < SSStore.METADATA_SIZE) this.getChannel().position(SSStore.METADATA_SIZE);
	}

	public void writeMeta(SSMetaData meta) throws Exception
	{
		long pos = this.getChannel().position();
		try
		{
			this.getChannel().position(0);
			byte b[] = new byte[SSStore.METADATA_SIZE];
			ByteArrayOutputStream bos = new XByteArrayOutputStream(b);
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(meta);
			if (bos.size() > SSStore.METADATA_SIZE) { throw new RuntimeException("metadata too large " + bos.size()); }
			write(b);
			oos.close();
			bos = null;
			b = null;
		}
		finally
		{
			if (pos < SSStore.METADATA_SIZE) pos = SSStore.METADATA_SIZE;
			this.getChannel().position(pos);
		}
	}

	public long size() throws Exception
	{
		return this.getChannel().size() - SSStore.METADATA_SIZE;
	}
}