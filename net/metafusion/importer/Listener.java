package net.metafusion.importer;

import java.util.ArrayList;

public interface Listener
{
	public abstract ArrayList<ImageImportHeader> getHeaders();

	public abstract boolean listenForEvent(int pollIntervalInMsec);

	public abstract void cleanUp(ImageImportHeader h, boolean deleteRecordFlag);
}
