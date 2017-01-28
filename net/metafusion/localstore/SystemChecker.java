package net.metafusion.localstore;

import net.metafusion.model.DicomHeaderView;
import net.metafusion.model.ProcedureStepView;
import net.metafusion.model.RichMediaView;
import net.metafusion.model.RisGlueLogView;
import net.metafusion.model.ServiceLogView;
import net.metafusion.model.SystemInfo;
import net.metafusion.model.SystemInfoView;
import net.metafusion.model.WorklistView;
import acme.util.Log;
import acme.util.Util;

public class SystemChecker
{
	private static int	schemaVersion	= 7;

	public static void checkDb()
	{
		int currentVersion = 0;
		try
		{
			currentVersion = new Integer(SystemInfoView.get().getCurrentVersion());
		}
		catch (Exception e)
		{
			SystemInfoView.get().createTable();
		}
		if (currentVersion != schemaVersion)
		{
			Util.log("SystemChecker: Schema update from version: " + currentVersion + " to: " + schemaVersion);
			updateSchema(currentVersion);
		}
	}

	public static void updateSchema(Integer currentVersion)
	{
		try
		{
			RisGlueLogView.get().selectWhere("logID=1");
		}
		catch (Exception e)
		{
			RisGlueLogView.get().createTable();
		}
		try
		{
			RichMediaView.get().selectWhere("mediaID=1");
		}
		catch (Exception e)
		{
			RichMediaView.get().createTable();
		}
		try
		{
			ServiceLogView.get().selectWhere("serviceLogId=1");
		}
		catch (Exception e)
		{
			if (currentVersion == 4)
			{
				ServiceLogView.get().updateSchema();
			}
			else
				ServiceLogView.get().createTable();
		}
		try
		{
			if (currentVersion < 6)
				DicomHeaderView.get().createTable();
		}
		catch (Exception e)
		{
			Log.log("Error trying to create DicomHeader table: ", e);
		}
		try
		{
			if(currentVersion < 7)
			{
				WorklistView.get().updateTable();
			}
		}
		catch(Exception e)
		{
			Log.log("Error modifying RIS tables:",e);
		}
		SystemInfo si = new SystemInfo();
		si.setSystemKey("schemaVersion");
		si.setSystemValue(new Integer(schemaVersion).toString());
		SystemInfoView.get().update(si);
	}
}
