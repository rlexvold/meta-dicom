package integration;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class MFOutputStream extends DataOutputStream
{
	private OutputStream os;

	public MFOutputStream(OutputStream os) throws Exception
	{
		super(os);
		this.os = os;
	}

	public void writeObject(Object o) throws Exception
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.flush();
		writeInt(baos.size());
		baos.writeTo(os);
	}
}