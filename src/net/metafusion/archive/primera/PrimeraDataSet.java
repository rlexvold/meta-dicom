package net.metafusion.archive.primera;

import java.io.File;

import net.metafusion.archive.ArchiveDataSet;

public class PrimeraDataSet extends ArchiveDataSet
{
	private File	jrqFile		= null;
	private File	inpFile		= null;
	private File	doneFile	= null;
	private File	errFile		= null;

	public File getJrqFile()
	{
		return jrqFile;
	}

	@Override
	public void setSetId(String setId)
	{
		this.setId = setId;
		setJrqFile(new File(setId + ".JRQ"));
	}

	public void setJrqFile(File jrqFile)
	{
		this.jrqFile = jrqFile;
		setInpFile(new File(jrqFile.getAbsolutePath().replace("JRQ", "INP")));
		setErrFile(new File(jrqFile.getAbsolutePath().replace("JRQ", "ERR")));
		setDoneFile(new File(jrqFile.getAbsolutePath().replace("JRQ", "DON")));
	}

	public File getInpFile()
	{
		return inpFile;
	}

	public void setInpFile(File inpFile)
	{
		this.inpFile = inpFile;
	}

	public File getDoneFile()
	{
		return doneFile;
	}

	public void setDoneFile(File doneFile)
	{
		this.doneFile = doneFile;
	}

	public File getErrFile()
	{
		return errFile;
	}

	public void setErrFile(File errFile)
	{
		this.errFile = errFile;
	}
}
