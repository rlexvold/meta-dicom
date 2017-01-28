package net.metafusion.archive;

import integration.MFClient;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import net.metafusion.archive.primera.PrimeraArchiveDevice;
import net.metafusion.archive.primera.PrimeraDataSet;
import net.metafusion.model.Study;
import net.metafusion.model.StudyView;
import net.metafusion.util.AE;
import net.metafusion.util.AEMap;
import net.metafusion.util.DicomDir;
import net.metafusion.util.DicomUtil;
import net.metafusion.util.GlobalProperties;
import acme.storage.SSStore;
import acme.util.FileUtil;
import acme.util.Util;
import acme.util.rsync.RsyncServer;

public class ArchiveSystem
{
	private MFClient	client	= null;

	public static ArchiveJob newJob()
	{
		ArchiveJob job = new ArchiveJob();
		Long time = System.currentTimeMillis();
		job.setJobId(time.toString());
		return job;
	}

	public static ArchiveJob getJobInfo(ArchiveJob job)
	{
		String type = (String) GlobalProperties.get().get("archiveSystem");
		if (type != null)
		{
			if (type.equalsIgnoreCase("primera"))
				job.setDevice(new PrimeraArchiveDevice());
		}
		String dir = (String) GlobalProperties.get().get("archiveDir");
		if (dir == null)
			dir = "c:\\temp\\archive";
		File tmp = new File(dir, job.getJobId());
		File dataDir = new File(tmp, "data");
		File labelDir = new File(tmp, "label");
		if (!dataDir.exists())
			dataDir.mkdirs();
		if (!labelDir.exists())
			labelDir.mkdirs();
		job.setArchiveDir(dataDir);
		job.setLabelDir(labelDir);
		job.setStaticDir(new File(dir, "static"));
		job.setFixedDataSize(FileUtil.getSize(job.getStaticDir()));
		if (job.getFixedDataSize() == null)
			job.setFixedDataSize(0L);
		return job;
	}

	public ArchiveJob runJob(ArchiveJob job) throws Exception
	{
		job = createDataSets(job);
		job.getDevice().runJob(job);
		while (!job.isDone())
		{
			Util.sleep(5000);
		}
		return job;
	}

	private Long processFiles(File srcDir, File destDir, Long currentSpace) throws Exception
	{
		Long size = 0L;
		File files[] = srcDir.listFiles();
		if (files.length == 0)
		{
			srcDir.delete();
			return currentSpace;
		}
		for (int i = 0; i < files.length; i++)
		{
			File f = files[i];
			if (f.isDirectory())
			{
				String tmp = f.getAbsolutePath().replace(srcDir.getAbsolutePath(), "");
				File tmpFile = new File(destDir, tmp);
				currentSpace = processFiles(f, tmpFile, currentSpace);
				if (currentSpace == null)
					return currentSpace;
			}
			else
			{
				size = f.length();
				if (size < currentSpace)
				{
					String tmp = f.getAbsolutePath().replace(srcDir.getAbsolutePath(), "");
					File destFile = new File(destDir, tmp);
					FileUtil.rename(f, destFile, false);
					currentSpace -= size;
				}
				else
				{
					currentSpace = 0L;
					break;
				}
			}
		}
		return currentSpace;
	}

	public ArchiveJob createDataSets(ArchiveJob job) throws Exception
	{
		Long freeSpace = job.getMediaSize() - job.getFixedDataSize();
		Long jobSize = FileUtil.getSize(job.getArchiveDir());
		if (jobSize < freeSpace)
		{
			PrimeraDataSet ds = new PrimeraDataSet();
			ds.setSetDir(job.getArchiveDir());
			ds.setLabelDir(job.getLabelDir());
			ds.setSetId(job.getJobId());
			job.addDataSet(ds);
			FileUtil.copyDir(job.getStaticDir(), job.getArchiveDir());
			return job;
		}
		int count = 0;
		while (true)
		{
			count++;
			PrimeraDataSet ds = new PrimeraDataSet();
			ds.setSetId(job.getJobId() + "_" + count);
			File tmp = SSStore.get().createTempDir("archive" + ds.getSetId());
			File dataDir = new File(tmp, "data");
			if (!dataDir.exists())
				dataDir.mkdirs();
			File labelDir = new File(tmp, "label");
			if (!labelDir.exists())
				labelDir.mkdirs();
			ds.setSetDir(dataDir);
			ds.setLabelDir(labelDir);
			job.addDataSet(ds);
			FileUtil.copyDir(job.getStaticDir(), ds.getSetDir());
			Long currentSpace = freeSpace;
			currentSpace = processFiles(job.getArchiveDir(), ds.getSetDir(), currentSpace);
			if (currentSpace == null || currentSpace > 0L)
				break;
		}
		return job;
	}
	private class CreateDcm extends FileUtil.FileRunnable
	{
		File	tmpDir	= null;

		public CreateDcm(File tmpDir)
		{
			this.tmpDir = tmpDir;
		}

		@Override
		public void run(File f)
		{
			String name = f.getName();
			if (!name.endsWith(".mdf"))
				return;
			name = name.replace(".mdf", ".dcm");
			File dcmFile = new File(tmpDir, name);
			DicomUtil.convertMDFToDCM(f, dcmFile);
		}
	}

	public ArchiveJob createStudyArchiveData(ArchiveJob job) throws Exception
	{
		AE ae = AEMap.get(job.getArchiveSystem());
		if (ae == null)
		{
			throw new Exception("ArchiveSystem not in AEMap: " + ae);
		}
		createClient(ae);
		client.send(new Object[] { "archiveGetJobInfo", job });
		Object tmp = client.getObject("");
		job = (ArchiveJob) tmp;
		Iterator<String> i = job.getStudyList().iterator();
		ArrayList<Study> studies = new ArrayList<Study>();
		while (i.hasNext())
		{
			Study study = StudyView.get().selectByUID(i.next());
			studies.add(study);
			Long studyID = study.studyID;
		}
		job.setSourceJobDir(SSStore.get().createTempDir("archive"));
		job.getSourceJobDir().mkdirs();
		DicomDir dd = new DicomDir(job.getSourceJobDir(), studies);
		return job;
	}

	public ArchiveJob createStudyArchiveDataOld(ArchiveJob job) throws Exception
	{
		AE ae = AEMap.get(job.getArchiveSystem());
		if (ae == null)
		{
			throw new Exception("ArchiveSystem not in AEMap: " + ae);
		}
		createClient(ae);
		client.send(new Object[] { "archiveGetJobInfo", job });
		Object tmp = client.getObject("");
		job = (ArchiveJob) tmp;
		Iterator<String> i = job.getStudyList().iterator();
		job.setSourceJobDir(SSStore.get().createTempDir("archive"));
		while (i.hasNext())
		{
			Study study = StudyView.get().selectByUID(i.next());
			Long studyID = study.studyID;
			File destDir = new File(job.getSourceJobDir(), studyID.toString());
			File studyDir = SSStore.get().getStudyDir(studyID, false);
			FileUtil.forEachFile(studyDir, false, false, true, new CreateDcm(destDir));
		}
		return job;
	}

	private void createClient(AE ae)
	{
		if (client == null && ae != null)
			client = new MFClient(ae.getHostName(), ae.getRisPort(), "matt", "matt");
	}
	private class FR extends FileUtil.FileRunnable
	{
		private ArchiveJob	job	= null;

		public FR(ArchiveJob job)
		{
			this.job = job;
		}

		@Override
		public void run(File f)
		{
			RsyncServer rm = new RsyncServer();
			String tmp = f.getAbsolutePath().replace(job.getSourceJobDir().getAbsolutePath(), "");
			File destFile = new File(job.getArchiveDir(), tmp);
			try
			{
				boolean result = rm.put(client, f, destFile);
			}
			catch (Exception e)
			{
			}
		}
	}

	public ArchiveJob transferData(ArchiveJob job) throws Exception
	{
		FileUtil.forEachFile(job.getSourceJobDir(), true, false, true, new FR(job));
		return job;
	}

	public ArchiveJob runRemoteJob(ArchiveJob job) throws Exception
	{
		client.send(new Object[] { "runArchiveJob", job });
		job = (ArchiveJob) client.getObject("");
		return job;
	}
}
