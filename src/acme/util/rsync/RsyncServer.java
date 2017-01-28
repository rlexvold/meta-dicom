package acme.util.rsync;

import integration.MFClient;
import integration.MFFileInfo;
import integration.MFInteger;

import java.io.BufferedInputStream;
import java.io.File;
import java.security.MessageDigest;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.metafusion.util.GlobalProperties;

import org.metastatic.rsync.Checksum32;
import org.metastatic.rsync.Configuration;
import org.metastatic.rsync.Generator;
import org.metastatic.rsync.JarsyncProvider;
import org.metastatic.rsync.Matcher;
import org.metastatic.rsync.Rebuilder;

import acme.storage.SSStore;
import acme.util.CompressionStream;
import acme.util.FileUtil;
import acme.util.Log;
import acme.util.MSJavaHack;
import acme.util.Util;

public class RsyncServer
{
	private String			hostname			= null;
	private String			module				= null;
	private Configuration	conf				= null;
	private String[]		mds					= null;
	private static Integer	bytesInAMegabyte	= 1048576;
	private Integer			chunkSize			= 5 * bytesInAMegabyte;

	public String getHostname()
	{
		return hostname;
	}

	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}

	public String getModule()
	{
		return module;
	}

	public void setModule(String module)
	{
		this.module = module;
	}

	private String[] getMessageDigests()
	{
		HashSet algs = new HashSet();
		String[] tries = { "md2", "md4", "md5", "sha-1", "ripemd128", "ripemd160", "tiger", "whirlpool", "brokenmd4" };
		for (int i = 0; i < tries.length; i++)
		{
			try
			{
				MessageDigest.getInstance(tries[i]);
				algs.add(tries[i]);
			}
			catch (Exception x)
			{
			}
		}
		return (String[]) algs.toArray(new String[algs.size()]);
	}

	public RsyncServer()
	{
		init(null, null);
	}

	public RsyncServer(String host, String mod)
	{
		init(host, mod);
	}

	public void init(String host, String mod)
	{
		this.hostname = host;
		this.module = mod;
		mds = getMessageDigests();
		Security.addProvider(new JarsyncProvider());
		conf = new Configuration();
		try
		{
			conf.strongSum = MessageDigest.getInstance("MD4", "JARSYNC");
		}
		catch (Exception e)
		{
			Log.log("RsyncServer, invalid algorithm");
		}
		conf.strongSumLength = conf.strongSum.getDigestLength();
		conf.weakSum = new Checksum32();
		Integer tmp = (Integer) GlobalProperties.get().get("rsyncChunkSizeMB");
		if (tmp != null)
			chunkSize = tmp * bytesInAMegabyte;
	}

	public void get(MFClient client, File sourceFile, File destFile) throws Exception
	{
		if (destFile.exists())
		{
			byte[] oldData = null;
			int offset = 0;
			for (;;)
			{
				List sums = getSums(destFile.getAbsolutePath(), offset, chunkSize);
				if (sums == null)
					break;
				byte[] compressedSums = CompressionStream.write(sums);
				client.send(new Object[] { "getRsyncDeltas", sourceFile, compressedSums, new MFInteger(offset), new MFInteger(chunkSize) });
				byte[] compressedDeltas = (byte[]) client.getObject(null);
				if (compressedDeltas == null)
					break;
				List deltas = (List) CompressionStream.readObject(compressedDeltas);
				processDeltasAndWriteFile(destFile.getAbsolutePath(), deltas, offset, chunkSize);
				offset += chunkSize;
			}
			finalize(destFile);
		}
		else
		{
			MFFileInfo mf = new MFFileInfo(sourceFile);
			client.sendFileFromServer(mf, destFile);
		}
		return;
	}

	public boolean put(MFClient client, File sourceFile, File destFile) throws Exception
	{
		int offset = 0;
		for (;;)
		{
			client.send(new Object[] { "getRsyncSums", destFile, new MFInteger(offset), new MFInteger(chunkSize) });
			Object tmp = client.getObject(null);
			if (tmp == null && offset == 0) // Destination file doesn't exist,
			// no
			// need to use rsync
			{
				MFFileInfo mf = new MFFileInfo(destFile);
				client.sendFileToServer(mf, sourceFile);
				return true;
			}
			else if (tmp == null)
				break;
			else
			{
				byte[] compressedSums = (byte[]) tmp;
				List sums = (List) CompressionStream.readObject(compressedSums);
				byte[] newData = null;
				List deltas = getDeltas(sums, sourceFile.getAbsolutePath(), offset, chunkSize);
				byte[] compressedDeltas = CompressionStream.write(deltas);
				client.send(new Object[] { "processRsyncDeltas", destFile, compressedDeltas, new MFInteger(offset), new MFInteger(chunkSize) });
				offset += chunkSize;
			}
		}
		client.send(new Object[] { "rsyncFinalize", destFile });
		return true;
	}

	public List getSums(byte[] old) throws Exception
	{
		Generator gen = new Generator(conf);
		return gen.generateSums(old);
	}

	public List getSums(String theFile, int offset, int length) throws Exception
	{
		byte[] data = Util.readFile(theFile, offset, length);
		if (data == null)
			return null;
		return getSums(data);
	}

	public List getDeltas(List sums, String dataFile, int offset, int length) throws Exception
	{
		byte[] newData = Util.readFile(dataFile, offset, length);
		if (newData == null)
			return null;
		return getDeltas(sums, newData);
	}

	public List getDeltas(List sums, byte[] newData)
	{
		if (newData == null)
			return new ArrayList();
		Matcher mat = new Matcher(conf);
		return mat.hashSearch(sums, newData);
	}

	public File getTempFile(File oldFile)
	{
		return new File(oldFile.getAbsolutePath() + ".rsynctmp");
	}

	public void processDeltasAndWriteFile(String oldFile, List deltas, int offset, int length) throws Exception
	{
		File tempFile = getTempFile(new File(oldFile));
		boolean append = true;
		byte[] oldData = Util.readFile(oldFile, offset, length);
		byte[] newData = processDeltas(oldData, deltas);
		if (offset == 0)
			append = false;
		Util.writeFile(newData, tempFile, append);
	}

	public void finalize(File oldFile)
	{
		File tempFile = getTempFile(oldFile);
		FileUtil.safeDelete(oldFile);
		FileUtil.safeRename(tempFile, oldFile);
	}

	public byte[] processDeltas(byte[] oldData, List deltas) throws Exception
	{
		return Rebuilder.rebuild(oldData, deltas);
	}
}
