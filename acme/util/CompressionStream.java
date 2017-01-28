package acme.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressionStream
{
	public static int write(byte[] output, DataOutputStream dos) throws Exception
	{
		Deflater def = new Deflater(Deflater.BEST_SPEED);
		def.setInput(output);
		byte[] tmpOutput = new byte[output.length * 2];
		def.finish();
		int count = def.deflate(tmpOutput);
		dos.writeInt(count);
		dos.write(tmpOutput, 0, count);
		dos.flush();
		return count;
	}

	public static DataInputStream read(DataInputStream dis) throws Exception
	{
		byte[] b;
		int length = dis.readInt();
		if (length == -1)
			return null;
		byte[] input = new byte[length];
		dis.readFully(input);
		Inflater inf = new Inflater();
		inf.setInput(input);
		byte[] output = new byte[length * 100];
		int outputLength = inf.inflate(output);
		byte[] trimmedOutput = new byte[outputLength];
		System.arraycopy(output, 0, trimmedOutput, 0, outputLength);
		return new DataInputStream(new ByteArrayInputStream(trimmedOutput));
	}

	public static byte[] write(Object data) throws Exception
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream bos = new ObjectOutputStream(baos);
		bos.writeObject(data);
		ByteArrayOutputStream dbaos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(dbaos);
		write(baos.toByteArray(), dos);
		return dbaos.toByteArray();
	}

	public static byte[] read(byte[] data) throws Exception
	{
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		DataInputStream dis = new DataInputStream(bais);
		byte[] b;
		int length = dis.readInt();
		if (length == -1)
			return null;
		byte[] input = new byte[length];
		dis.readFully(input);
		Inflater inf = new Inflater();
		inf.setInput(input);
		byte[] output = new byte[length * 100];
		int outputLength = inf.inflate(output);
		byte[] trimmedOutput = new byte[outputLength];
		System.arraycopy(output, 0, trimmedOutput, 0, outputLength);
		return trimmedOutput;
	}

	public static Object readObject(byte[] data) throws Exception
	{
		byte[] b = read(data);
		ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b));
		return ois.readObject();
	}
}
