package net.metafusion.archive;

public interface IArchive
{
	public void runJob(ArchiveJob ds) throws Exception;

	public void updateJobStatus(ArchiveJob job);
}
