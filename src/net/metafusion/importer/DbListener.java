package net.metafusion.importer;

import java.util.ArrayList;
import acme.util.Util;

public class DbListener implements Listener
{
	private ArrayList<ImageImportHeader> resultSet;

	public ArrayList<ImageImportHeader> getHeaders()
	{
		return resultSet;
	}

	public boolean listenForEvent(int pollIntervalInMsec)
	{
		resultSet = null;
		while (true)
		{
			resultSet = ImageImportView.get().getAllHeaders();
			if (resultSet != null && resultSet.size() > 0) return true;
			Util.sleep(pollIntervalInMsec);
		}
	}

	public void cleanUp(ImageImportHeader h, boolean deleteRecordFlag)
	{
		for (int i = 0; i < resultSet.size(); i++)
		{
			if (resultSet.get(i).getImportID() == h.getImportID())
			{
				if (deleteRecordFlag)
					ImageImportView.get().delete(resultSet.get(i).getImportID());
				else ImageImportView.get().setProcessed(resultSet.get(i).getImportID());
			}
		}
	}
}
