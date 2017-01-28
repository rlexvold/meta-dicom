package net.metafusion.archive.primera;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import net.metafusion.archive.ArchiveDataSet;
import net.metafusion.archive.ArchiveJob;
import net.metafusion.archive.IArchive;
import net.metafusion.util.GlobalProperties;
import acme.storage.SSStore;
import acme.util.FileUtil;

public class PrimeraArchiveDevice implements IArchive, Serializable
{
	private File	queueDir		= new File("c:\\PTBurnJobs");
	private File	labelSourceDir	= new File("c:\\data\\primera\\label");

	public void runJob(ArchiveJob job) throws Exception
	{
		ArrayList<ArchiveDataSet> dataList = job.getDataSets();
		FileWriter fw = null;
		FileWriter labelWriter = null;
		for (int i = 0; i < dataList.size(); i++)
		{
			try
			{
				PrimeraDataSet ds = (PrimeraDataSet) dataList.get(i);
				File sourceLabel = new File(labelSourceDir, "singleStudyLabel.std");
				File destLabel = new File(ds.getLabelDir(), "singleStudyLabel.std");
				FileUtil.copyFile(sourceLabel, destLabel);
				File inputFile = new File(ds.getLabelDir(), "input.csv");
				labelWriter = new FileWriter(inputFile);
				labelWriter.write("Patient Name," + job.getJobId() + "," + (i + 1) + "," + dataList.size());
				labelWriter.close();
				File jrqFile = SSStore.get().createTempFile(ds.getJrqFile().getName());
				fw = new FileWriter(jrqFile);
				fw.write("JobID=" + ds.getSetId() + "\n");
				fw.write("Data=" + ds.getSetDir().getAbsolutePath() + "\n");
				fw.write("PrintLabel=" + destLabel.getAbsolutePath() + "\n");
				fw.close();
				File jobFile = new File(queueDir, ds.getJrqFile().getName());
				jrqFile.renameTo(jobFile);
			}
			finally
			{
				if (fw != null)
					fw.close();
				if (labelWriter != null)
					labelWriter.close();
			}
		}
	}

	public PrimeraArchiveDevice()
	{
		String dir = GlobalProperties.get().getProperty("primeraJobDir");
		if (dir != null)
			queueDir = new File(dir);
		dir = GlobalProperties.get().getProperty("primeraLabelDir");
		if (dir != null)
			labelSourceDir = new File(dir);
	}

	public void updateJobStatus(ArchiveJob job)
	{
		Iterator i = job.getDataSets().iterator();
		while (i.hasNext())
		{
			PrimeraDataSet data = (PrimeraDataSet) i.next();
			if (!data.isDone())
			{
				File tmp = new File(queueDir, data.getDoneFile().getName());
				if (tmp.exists())
				{
					data.setDone(true);
					job.incNumDone();
					tmp.delete();
					continue;
				}
				tmp = new File(queueDir, data.getInpFile().getName());
				if (tmp.exists())
				{
					data.setDone(false);
					continue;
				}
				tmp = new File(queueDir, data.getErrFile().getName());
				if (tmp.exists())
				{
					BufferedReader br = null;
					try
					{
						br = new BufferedReader(new FileReader(tmp));
						String error = br.readLine();
						Exception e = new Exception("Primera error: " + error);
						throw e;
					}
					catch (Exception e)
					{
						data.setError(e);
						job.addError(e);
					}
					finally
					{
						try
						{
							if (br != null)
								br.close();
						}
						catch (Exception e)
						{
						}
					}
					data.setDone(true);
					job.incNumDone();
					continue;
				}
			}
			if (job.getNumDone() == job.getDataSets().size())
			{
				job.setDone(true);
			}
		}
	}
}
