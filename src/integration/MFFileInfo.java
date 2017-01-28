package integration;

import java.io.File;
import java.io.Serializable;

public class MFFileInfo implements Serializable
{
	private Long	fileId;
	private Long	size;
	private File	sourceFile;

	public MFFileInfo(Long id, File name)
	{
		this.fileId = id;
		this.sourceFile = name;
		this.size = name.length();
	}

	public MFFileInfo(File name)
	{
		this.sourceFile = name;
		this.fileId = null;
		this.size = null;
	}

	public Long getFileId()
	{
		return fileId;
	}

	public void setFileId(Long fileId)
	{
		this.fileId = fileId;
	}

	public Long getSize()
	{
		return size;
	}

	public void setSize(Long size)
	{
		this.size = size;
	}

	public File getSourceFile()
	{
		return sourceFile;
	}

	public void setSourceFilename(File sourceFile)
	{
		this.sourceFile = sourceFile;
	}
}
