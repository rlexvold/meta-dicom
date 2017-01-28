package net.metafusion.archive;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;

import net.metafusion.util.AE;

public class ArchiveJob implements Serializable
{
	protected ArrayList<ArchiveDataSet>	dataSets		= null;
	protected IArchive					device			= null;
	protected String					jobId			= null;
	protected Integer					numCopies		= 1;
	protected Set<String>				studyList		= null;
	protected Boolean					done			= false;
	protected ArrayList<Exception>		errors			= null;
	protected String					result			= null;
	protected Integer					numDone			= 0;
	protected Long						mediaSize		= 0L;
	protected String					archiveSystem	= null;
	protected File						archiveDir		= null;
	protected Long						fixedDataSize	= 0L;
	protected File						sourceJobDir	= null;
	protected File						staticDir		= null;
	protected File						labelDir		= null;

	public File getLabelDir()
	{
		return labelDir;
	}

	public void setLabelDir(File labelDir)
	{
		this.labelDir = labelDir;
	}

	public File getStaticDir()
	{
		return staticDir;
	}

	public void setStaticDir(File staticDir)
	{
		this.staticDir = staticDir;
	}

	public Boolean getDone()
	{
		return done;
	}

	public void setDone(Boolean done)
	{
		this.done = done;
	}

	public File getSourceJobDir()
	{
		return sourceJobDir;
	}

	public void setSourceJobDir(File sourceJobDir)
	{
		this.sourceJobDir = sourceJobDir;
	}

	public Long getFixedDataSize()
	{
		return fixedDataSize;
	}

	public void setFixedDataSize(Long fixedDataSize)
	{
		this.fixedDataSize = fixedDataSize;
	}

	public File getArchiveDir()
	{
		return archiveDir;
	}

	public void setArchiveDir(File archiveDir)
	{
		this.archiveDir = archiveDir;
	}

	public String getArchiveSystem()
	{
		return archiveSystem;
	}

	public void setArchiveSystem(String archiveSystem)
	{
		this.archiveSystem = archiveSystem;
	}

	public Long getMediaSize()
	{
		return mediaSize;
	}

	public void setMediaSize(Long mediaSize)
	{
		this.mediaSize = mediaSize;
	}

	public Boolean isDone()
	{
		if (device != null)
			device.updateJobStatus(this);
		return done;
	}

	public void setDone(boolean done)
	{
		this.done = done;
	}

	public ArrayList<Exception> getErrors()
	{
		return errors;
	}

	public void setErrors(ArrayList<Exception> errors)
	{
		this.errors = errors;
	}

	public void addError(Exception error)
	{
		this.errors.add(error);
	}

	public Integer getNumDone()
	{
		return numDone;
	}

	public void incNumDone()
	{
		numDone++;
	}

	public void setNumDone(Integer numDone)
	{
		this.numDone = numDone;
	}

	public String getResult()
	{
		return result;
	}

	public void setResult(String result)
	{
		this.result = result;
	}

	public ArchiveJob()
	{
		studyList = new TreeSet<String>();
		dataSets = new ArrayList<ArchiveDataSet>();
		errors = new ArrayList<Exception>();
	}

	public void updateDataSet(ArchiveDataSet ds)
	{
	}

	public void addDataSet(ArchiveDataSet ds)
	{
		ds.setSetId(jobId + (dataSets.size() + 1));
		dataSets.add(ds);
	}

	public void addStudy(String uid)
	{
		studyList.add(uid);
	}

	public ArrayList<ArchiveDataSet> getDataSets()
	{
		return dataSets;
	}

	public IArchive getDevice()
	{
		return device;
	}

	public String getJobId()
	{
		return jobId;
	}

	public Integer getNumCopies()
	{
		return numCopies;
	}

	public Set<String> getStudyList()
	{
		return studyList;
	}

	public void removeStudy(String uid)
	{
		studyList.remove(uid);
	}

	public void setDataSets(ArrayList<ArchiveDataSet> dataSets)
	{
		this.dataSets = dataSets;
	}

	public void setDevice(IArchive device)
	{
		this.device = device;
	}

	public void setJobId(String jobId)
	{
		this.jobId = jobId;
	}

	public void setNumCopies(Integer numCopies)
	{
		this.numCopies = numCopies;
	}

	public void setStudyList(Set<String> studyList)
	{
		this.studyList = studyList;
	}
}
