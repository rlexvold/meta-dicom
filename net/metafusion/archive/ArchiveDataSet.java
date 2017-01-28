package net.metafusion.archive;

import java.io.File;
import java.io.Serializable;

public class ArchiveDataSet implements Serializable
{
	protected String	setId		= null;
	protected File		setDir		= null;
	protected Boolean	done		= false;
	protected Exception	error		= null;
	protected File		sourceDir	= null;
	protected File		labelDir	= null;

	public Boolean getDone()
	{
		return done;
	}

	public void setDone(Boolean done)
	{
		this.done = done;
	}

	public File getLabelDir()
	{
		return labelDir;
	}

	public void setLabelDir(File labelDir)
	{
		this.labelDir = labelDir;
	}

	public File getSourceDir()
	{
		return sourceDir;
	}

	public void setSourceDir(File sourceDir)
	{
		this.sourceDir = sourceDir;
	}

	public boolean isDone()
	{
		return done;
	}

	public void setDone(boolean done)
	{
		this.done = done;
	}

	public Exception getError()
	{
		return error;
	}

	public void setError(Exception error)
	{
		this.error = error;
	}

	public String getSetId()
	{
		return setId;
	}

	public void setSetId(String setId)
	{
		this.setId = setId;
	}

	public File getSetDir()
	{
		return setDir;
	}

	public void setSetDir(File setDir)
	{
		this.setDir = setDir;
	}
}
