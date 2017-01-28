package acme.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.metafusion.util.GlobalProperties;
import acme.util.FileIter;
import acme.util.FileNameFilter;
import acme.util.FileUtil;
import acme.util.Log;
import acme.util.LongKeyCache;
import acme.util.MSJavaHack;
import acme.util.StringUtil;
import acme.util.Util;
import acme.util.XByteArrayOutputStream;

public class SSStore
{
	public static final int	METADATA_SIZE	= 4096;

	// public static final int SITE_ID = 0; // will vary will mutltisite
	// public static final long SITE_ID_MAX = 0x100l; // will vary will
	// multisite
	// public static final long SITE_ID_MASK = 0xFFFFFFFFFFFFFF00l; // will vary
	// will mutltisite
	public static SSStore get()
	{
		return instance;
	}
	static public SSStore	instance	= null;

	static void log(String s)
	{
		Log.log(s);
	}

	static void log(String s, Exception e)
	{
		Log.log(s, e);
	}

	static void vlog(String s)
	{
		Log.vlog(s);
	}
	String	name;
	File	root;
	File	trashRoot;
	File	tempRoot;
	File	filesystemRoot;
	File	syncRoot;
	File	richMediaRoot;

	public File getRichMediaRoot()
	{
		return richMediaRoot;
	}

	public void setRichMediaRoot(File richMediaRoot)
	{
		this.richMediaRoot = richMediaRoot;
	}

	public String getName()
	{
		return name;
	}

	public File getRootDir()
	{
		return root;
	}

	public File getTempDir()
	{
		return tempRoot;
	}

	public File getTrashDir()
	{
		return trashRoot;
	}

	public File getSyncDir()
	{
		return syncRoot;
	}

	public File getFilesystemRoot()
	{
		return filesystemRoot;
	}

	SSStore(String name, File root)
	{
		if (instance == null)
			instance = this;
		this.name = name;
		this.root = root;
		if (!this.root.exists())
			this.root.mkdir();
		assert this.root.isDirectory();
		this.trashRoot = new File(root.getParentFile(), "__TRASH__");
		if (trashRoot.exists())
			FileUtil.deleteDirectoryContents(trashRoot);
		this.syncRoot = new File(root.getParentFile(), "__SYNC__");
		syncRoot.mkdir();
		this.tempRoot = new File(root.getParentFile(), "__TEMP__");
		if (tempRoot.exists())
			FileUtil.deleteDirectoryContents(trashRoot);
		filesystemRoot = FileUtil.getFilesystemRoot(root);
		long l[] = FileUtil.getCapFreeNowInK(filesystemRoot);
		log("VolRoot=" + filesystemRoot + " capk=" + l[0] + " freek=" + l[1]);
		// Date d = new Date(2005-1900,7,1);
		// Log.log("===== "+d.getTime());
		loadexp();
		if (getExpiration() == null)
			log("Expiration Date: none");
		else
			log("Expiration Date: " + getExpiration());
	}

	void loadexp()
	{
		try
		{
			byte b[] = Util.readWholeFile(new File(root.getParentFile(), "access.def"));
			if (b != null && b.length > 0)
			{
				exp = new Date(Long.parseLong(new String(b)));
			}
		}
		catch (Exception e)
		{
			log("could not read access.def", e);
		}
	}
	private boolean	isExpired	= false;
	private Date	exp			= null;

	public Date getExpiration()
	{
		return exp;
	}

	public boolean isExpired()
	{
		if (exp == null)
			return false;
		isExpired = System.currentTimeMillis() > exp.getTime();
		if (isExpired)
		{
			loadexp();
			isExpired = System.currentTimeMillis() > exp.getTime();
		}
		if (isExpired)
			log("Server has expired!!");
		return isExpired;
	}
	volatile Calendar	c				= Calendar.getInstance();
	volatile long		lastDirID		= 0;
	volatile File		lastDir			= null;
	volatile int		lastYear		= 0;
	volatile int		lastDayOfYear	= 0;
	LongKeyCache		parentCache		= new LongKeyCache(100);
	LongKeyCache		dayCache		= new LongKeyCache(100);

	public void initCache()
	{
		parentCache.clear();
		dayCache.clear();
	}

	public List<File> getAllStudyDir()
	{
		List<File> l = new LinkedList<File>();
		File files[] = root.listFiles();
		for (int i = 0; i < files.length; i++)
		{
			File f = files[i];
			if (f.isFile() || f.getName().length() != 8)
				continue;
			File studies[] = f.listFiles();
			for (int j = 0; j < studies.length; j++)
			{
				if (studies[j].isDirectory())
					l.add(studies[j]);
			}
		}
		return l;
	}

	public synchronized boolean isStudyDir(File dir) throws Exception
	{
		Long studyid = null;
		if (dir == null || dir.isFile())
			throw new Exception();
		if (!dir.getCanonicalPath().contains(root.getCanonicalPath()))
			return false;
		String dateString = dir.getParent().replace(root.getCanonicalPath() + "/", "");
		Long date = Long.decode(dateString);
		try
		{
			studyid = Long.decode(dir.getName());
		}
		catch (Exception e)
		{
			return false;
		}
		c.setTimeInMillis(studyid);
		File day = new File(root.getCanonicalPath(), "" + c.get(Calendar.YEAR) + StringUtil.int2(c.get(Calendar.MONTH) + 1) + StringUtil.int2(c.get(Calendar.DAY_OF_MONTH)));
		if (day == dir)
		{
			throw new Exception();
		}
		File studydir = new File(day, dir.getName());
		if (dir.getCanonicalPath().equals(studydir.getCanonicalPath()))
			return true;
		return false;
	}

	public synchronized File getStudyDir(long parentID)
	{
		return getStudyDir(parentID, true);
	}

	public synchronized File getStudyDir(long parentID, boolean createFlag)
	{
		if (parentID == 0)
			throw new RuntimeException("Invalid id: 0");
		File f = (File) parentCache.get(parentID);
		if (f != null)
			return f;
		c.setTimeInMillis(parentID);
		int year = c.get(Calendar.YEAR);
		int dayOfYear = c.get(Calendar.DAY_OF_YEAR);
		long dayCacheKey = year * 400l + dayOfYear;
		File day = (File) dayCache.get(dayCacheKey);
		if (day == null)
		{
			day = new File(root, "" + c.get(Calendar.YEAR) + StringUtil.int2(c.get(Calendar.MONTH) + 1) + StringUtil.int2(c.get(Calendar.DAY_OF_MONTH)));
			if (!day.exists() && createFlag)
				day.mkdir();
			dayCache.put(dayCacheKey, day);
		}
		f = new File(day, "" + parentID);
		if (!f.exists())
			f.mkdir();
		parentCache.put(parentID, f);
		return f;
	}

	public File getFile(long parentid, long id)
	{
		File parent = getStudyDir(parentid);
		return new File(parent, "" + id + ".mdf");
	}

	public File getRawFile(long parentid, String name)
	{
		File parent = getStudyDir(parentid);
		return new File(parent, name);
	}

	public File[] getFiles(long parentid, String ext)
	{
		List l = new LinkedList();
		File parent = getStudyDir(parentid);
		File files[] = parent.listFiles();
		for (int i = 0; i < files.length; i++)
			if (files[i].getName().endsWith(ext))
				l.add(files[i]);
		return (File[]) l.toArray(new File[l.size()]);
	}

	public long getSize(long parentid, long id)
	{
		File f = getFile(parentid, id);
		return f.exists() ? f.length() : 0;
	}

	public boolean deleteStudy(long studyID)
	{
		long size = 0;
		File studyDir = getStudyDir(studyID);
		boolean b = FileUtil.deleteDirectoryRecursive(studyDir);
		if (!b)
		{
			Log.log("could not delete study dir " + studyDir.getAbsolutePath());
		}
		return b;
	}

	public long delete(long parentid, long id)
	{
		long size = 0;
		File m = getFile(parentid, id);
		if (m.exists())
		{
			size += m.length();
			m.delete();
		}
		return size;
	}

	public boolean exists(long parentid, long id)
	{
		File m = getFile(parentid, id);
		return m.exists();
	}

	// add more checking...
	public boolean isValid(long parentid, long id)
	{
		File m = getFile(parentid, id);
		return m.exists();
	}

	public void xxxput(long parentid, long id, SSMetaData metadata, InputStream is) throws Exception
	{
		File m = getFile(parentid, id);
		if (m.exists() || m.exists())
		{
			log("SSObject put error: id=" + id + " m.exists=" + m.exists() + " d.exists=" + m.exists());
			if (m.exists() || m.exists())
				throw new RuntimeException("SSObject get files inconsistent " + id);
			throw new RuntimeException("object " + id + " already exists");
		}
		FileOutputStream fos = null;
		try
		{
			// todo sdsdsd xml.writeTo(m);
			fos = new FileOutputStream(m);
			Util.copyStream(is, fos);
			FileUtil.flushAndClose(fos);
		}
		catch (Exception e)
		{
			Log.log("store.put caught ", e);
			Util.safeClose(fos);
			Util.safeDelete(m);
		}
		finally
		{
		}
		return;
	}

	public void xxxput(long parentid, long id, Object metadata, File f) throws Exception
	{
		File m = getFile(parentid, id);
		if (m.exists() || m.exists())
		{
			Log.log("SSObject put error: id=" + id + " m.exists=" + m.exists() + " d.exists=" + m.exists());
			if (m.exists() || m.exists())
				throw new RuntimeException("SSObject get files inconsistent " + id);
			throw new RuntimeException("object " + id + " already exists");
		}
		FileInputStream fis = new FileInputStream(f);
		try
		{
			;// put(parentid, id, metadata, fis);
		}
		finally
		{
			fis.close();
			f.delete(); // todo optimize
		}
	}

	public void updateMeta(File f, SSMetaData meta) throws Exception
	{
		byte b[] = new byte[SSStore.METADATA_SIZE];
		ByteArrayOutputStream bos = new XByteArrayOutputStream(b);
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(meta);
		if (bos.size() > SSStore.METADATA_SIZE)
		{
			throw new RuntimeException("metadata too large " + bos.size());
		}
		oos.close();
		RandomAccessFile raf = null;
		try
		{
			raf = new RandomAccessFile(f, "rw");
			raf.write(b);
		}
		finally
		{
			Util.safeClose(raf);
		}
		b = null;
	}

	public void updateMeta(long parentid, long id, SSMetaData meta) throws Exception
	{
		File f = getFile(parentid, id);
		byte b[] = new byte[SSStore.METADATA_SIZE];
		ByteArrayOutputStream bos = new XByteArrayOutputStream(b);
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(meta);
		if (bos.size() > SSStore.METADATA_SIZE)
		{
			throw new RuntimeException("metadata too large " + bos.size());
		}
		oos.close();
		b = null;
		RandomAccessFile raf = null;
		try
		{
			raf = new RandomAccessFile(f, "rw");
			raf.write(b);
		}
		finally
		{
			Util.safeClose(raf);
		}
	}

	// puts a exernal file w/meta space already allocated
	public File putMetaFile(long parentid, long id, SSMetaData meta, File f) throws Exception
	{
		updateMeta(f, meta);
		File newFile = getFile(parentid, id);
		FileUtil.rename(f, newFile, true);
		return newFile;
	}

	private long xxxxxxfindMaxID()
	{
		List l = FileUtil.listFiles(root, new String[] { ".dat" });
		Iterator iter = l.iterator();
		long max = 0;
		while (iter.hasNext())
		{
			File f = (File) iter.next();
			if (f.getName().startsWith("data-"))
			{
				String idString = f.getName().substring(5, f.getName().length() - 4);
				try
				{
					long id = Long.parseLong(idString);
					if (id > max)
					{
						max = id;
					}
				}
				catch (NumberFormatException e)
				{
					log("could not parse " + idString);
				}
			}
		}
		return max;
	}
	static long	lastID	= 0;

	synchronized public long getNextID()
	{
		long id = System.currentTimeMillis();
		if (id <= lastID)
		{
			lastID += 1;
		}
		else
		{
			lastID = id;
		}
		return lastID;
	}

	public boolean sync() throws IOException
	{
		return true;
	}

	//
	// remoting
	//
	// boolean exists(String idString) {
	// return metaFile(Long.parseLong(idString)).exists();
	// }
	//
	// String getMetaString(String idString) {
	// File m = metaFile(Long.parseLong(idString));
	// if (m.isFile())
	// return new String(Util.readWholeFile(m));
	// else return null;
	// }
	public long[] getCapacityAndFreeSpace()
	{
		return FileUtil.getCapFreeNowInK(filesystemRoot);
	}

	public int getUsedPer()
	{
		return FileUtil.getPerUsed(filesystemRoot);
	}

	public File getTempRoot()
	{
		if (!tempRoot.exists())
			if (!tempRoot.mkdir())
				throw new RuntimeException("could not create temp root " + tempRoot);
		return tempRoot;
	}

	public File createTempFile(String suffix)
	{
		return MSJavaHack.get().createTempFile("temp", suffix, getTempRoot());
	}

	public File createTempFile(File dir, String suffix)
	{
		return MSJavaHack.get().createTempFile("temp", suffix, dir);
	}

	public File createTempDir(String prefix)
	{
		return MSJavaHack.get().createTempDir(prefix, getTempRoot());
	}
	class IDIter implements Iterator
	{
		FileIter	iter;

		public IDIter(File f)
		{
			iter = new FileIter(f, new FileNameFilter("data-", ".dat"));
		};

		public boolean hasNext()
		{
			return iter.hasNext();
		}

		public Object next()
		{
			File f = (File) iter.next();
			if (f == null)
				return new Long(-1);
			String name = f.getName();
			name = name.substring(5, name.lastIndexOf('.'));
			return new Long(name);
		}

		public void remove()
		{
			throw new RuntimeException("not implemented");
		}
	};

	//
	// /
	// /
	// / Raw files used by RIS
	// /
	// /
	public boolean rawExists(long dirid, String name)
	{
		File f = getRawFile(dirid, name);
		return f.exists();
	}

	public Object getRawObject(long dirid, String name) throws Exception
	{
		InputStream is = null;
		try
		{
			is = getRawInputStream(dirid, name);
			ObjectInputStream ois = new ObjectInputStream(is);
			Object o = ois.readObject();
			return o;
		}
		finally
		{
			Util.safeClose(is);
		}
	}

	public InputStream getRawInputStream(long dirid, String name) throws Exception
	{
		File f = getRawFile(dirid, name);
		if (!f.exists())
			throw new RuntimeException("get: file does not exist");
		return new FileInputStream(f);
	}

	public void putRawObject(long dirid, String name, Object o) throws Exception
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(o);
		oos.close();
		putRaw(dirid, name, new ByteArrayInputStream(baos.toByteArray()));
	}

	public File putRaw(long dirid, String name, InputStream is) throws Exception
	{
		File f = getRawFile(dirid, name);
		// put always need to replace existing
		// if (f.exists())
		// throw new RuntimeException("put: file already exists");
		File temp = this.createTempFile(f.getParentFile(), ".raw");
		try
		{
			Util.copyStreamToFile(is, temp);
			boolean b = FileUtil.rename(temp, f, true);
			if (!b)
				throw new RuntimeException("putRaw 2nd rename fail ");
			return f;
		}
		finally
		{
			Util.safeDelete(temp);
		}
	}

	public boolean deleteRaw(long dirid, String name)
	{
		File f = getRawFile(dirid, name);
		return f.delete();
	}

	//
	// /
	// NEWWWW HACKKKKK
	//
	//
	public SSInputStream getInputStream(long parentid, long objectid) throws Exception
	{
		File f = getFile(parentid, objectid);
		return new SSInputStream(f);
	}

	static public SSInputStream getInputStream(File mdfFile) throws Exception
	{
		return new SSInputStream(mdfFile);
	}

	public SSMetaData getMetaFromFile(File f) throws Exception
	{
		SSInputStream sis = null;
		try
		{
			sis = new SSInputStream(f);
			return sis.getMeta();
		}
		finally
		{
			Util.safeClose(sis);
		}
	}

	static public void BuildObjectFile(File output, Object meta, File data) throws Exception
	{
		FileOutputStream fos = null;
		FileInputStream fis = null;
		try
		{
			fos = new FileOutputStream(output);
			ByteArrayOutputStream bas = new ByteArrayOutputStream(SSStore.METADATA_SIZE);
			ObjectOutputStream oos = new ObjectOutputStream(bas);
			oos.writeObject(meta);
			if (bas.size() > SSStore.METADATA_SIZE)
				throw new RuntimeException("metadata too large " + bas.size());
			bas.writeTo(fos);
			fos.getChannel().position(SSStore.METADATA_SIZE);
			fis = new FileInputStream(data);
			Util.copyStream(fis, fos);
		}
		finally
		{
			Util.safeClose(fis);
			Util.safeClose(fos);
		}
	}

	public OutputStream getOutputStream(File file) throws Exception
	{
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(new byte[METADATA_SIZE]);
		return fos;
	}
}
