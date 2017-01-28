package net.metafusion.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import acme.db.View;

public class ProcedureStepView extends View
{
	static ProcedureStepView view = null;

	static public synchronized ProcedureStepView get()
	{
		if (view == null)
		{
			view = new ProcedureStepView();
		}
		return view;
	}

	ProcedureStepView()
	{
		super("ris_procedurestep", new String[] { "ScheduledProcedureStepID" }, new String[] { "StudyInstanceUID", "StoredProcedureStepUID", "PerformedStationAETitle",
				"ScheduledProcedureStepStartDate", "ScheduledProcedureStepStartTime", "PerformedProcedureStepStartDate", "PerformedProcedureStepStartTime",
				"PerformedProcedureStepEndDate", "PerformedProcedureStepEndTime", "ScheduledPerformingPhysiciansName", "ScheduledProcedureStepDescription",
				"PerformedProcedureStepStatus", "PerformedProcedureStepDescription", "PerformedProcedureTypeDescription", "RequestedContrastAgent", "Modality",
				"ScheduledStationAET" });
	}

	/*
	 * `ScheduledProcedureStepID` VARCHAR(255) DEFAULT '', `StudyInstanceUID`
	 * VARCHAR(255) DEFAULT '',
	 * 
	 * `StoredProcedureStepUID` VARCHAR(255) DEFAULT '',
	 * 
	 * `PerformedStationAETitle` VARCHAR(255) DEFAULT '',
	 * 
	 * `ScheduledProcedureStepStartDate` VARCHAR(255) DEFAULT '',
	 * `ScheduledProcedureStepStartTime` VARCHAR(255) DEFAULT '',
	 * 
	 * `PerformedProcedureStepStartDate` VARCHAR(255) DEFAULT '',
	 * `PerformedProcedureStepStartTime` VARCHAR(255) DEFAULT '',
	 * 
	 * `PerformedProcedureStepEndDate` VARCHAR(255) DEFAULT '',
	 * `PerformedProcedureStepEndTime` VARCHAR(255) DEFAULT '',
	 * 
	 * `ScheduledPerformingPhysiciansName` VARCHAR(255) DEFAULT '',
	 * `ScheduledProcedureStepDescription` VARCHAR(255) DEFAULT '',
	 * 
	 * `PerformedProcedureStepStatus` VARCHAR(255) DEFAULT '',
	 * `PerformedProcedureStepDescription` VARCHAR(255) DEFAULT '',
	 * `PerformedProcedureTypeDescription` VARCHAR(255) DEFAULT '',
	 * 
	 * `RequestedContrastAgent` VARCHAR(255) DEFAULT '', `Modality` VARCHAR(255)
	 * DEFAULT '', `ScheduledStationAET` VARCHAR(255) DEFAULT ''
	 */
	public ProcedureStep selectByStoreProcedureStepUID(String uid)
	{
		return (ProcedureStep) doSelect1(buildSelect("StoredProcedureStepUID=?"), new Object[] { uid });
	}

	public ProcedureStep selectByStudyInstanceUIDAndScheduledProcedureStepID(String uid, String psID)
	{
		return (ProcedureStep) doSelect1(buildSelect("StudyInstanceUID=? and ScheduledProcedureStepID=?"), new Object[] { uid, psID });
	}

	public List selectByStudyInstanceUID(String uid)
	{
		return (List) doSelectN(buildSelect("StudyInstanceUID=?"), new Object[] { uid });
	}

	protected Object load(ResultSet rs, int offset) throws Exception
	{
		ProcedureStep a = new ProcedureStep();
		a.ScheduledProcedureStepID = (rs.getString(offset++));
		a.StudyInstanceUID = (rs.getString(offset++));
		a.StoredProcedureStepUID = (rs.getString(offset++));
		a.PerformedStationAETitle = (rs.getString(offset++));
		a.ScheduledProcedureStepStartDate = (rs.getString(offset++));
		a.ScheduledProcedureStepStartTime = (rs.getString(offset++));
		a.PerformedProcedureStepStartDate = (rs.getString(offset++));
		a.PerformedProcedureStepStartTime = (rs.getString(offset++));
		a.PerformedProcedureStepEndDate = (rs.getString(offset++));
		a.PerformedProcedureStepEndTime = (rs.getString(offset++));
		a.ScheduledPerformingPhysiciansName = (rs.getString(offset++));
		a.ScheduledProcedureStepDescription = (rs.getString(offset++));
		a.PerformedProcedureStepStatus = (rs.getString(offset++));
		a.PerformedProcedureStepDescription = (rs.getString(offset++));
		a.PerformedProcedureTypeDescription = (rs.getString(offset++));
		a.RequestedContrastAgent = (rs.getString(offset++));
		a.Modality = (rs.getString(offset++));
		a.ScheduledStationAET = (rs.getString(offset++));
		return a;
	}

	protected void store(Object o, PreparedStatement ps, boolean pk, int i) throws Exception
	{
		ProcedureStep a = (ProcedureStep) o;
		if (pk)
			ps.setString(i++, a.ScheduledProcedureStepID);
		else
		{
			ps.setString(i++, a.StudyInstanceUID);
			ps.setString(i++, a.StoredProcedureStepUID);
			ps.setString(i++, a.PerformedStationAETitle);
			ps.setString(i++, a.ScheduledProcedureStepStartDate);
			ps.setString(i++, a.ScheduledProcedureStepStartTime);
			ps.setString(i++, a.PerformedProcedureStepStartDate);
			ps.setString(i++, a.PerformedProcedureStepStartTime);
			ps.setString(i++, a.PerformedProcedureStepEndDate);
			ps.setString(i++, a.PerformedProcedureStepEndTime);
			ps.setString(i++, a.ScheduledPerformingPhysiciansName);
			ps.setString(i++, a.ScheduledProcedureStepDescription);
			ps.setString(i++, a.PerformedProcedureStepStatus);
			ps.setString(i++, a.PerformedProcedureStepDescription);
			ps.setString(i++, a.PerformedProcedureTypeDescription);
			ps.setString(i++, a.RequestedContrastAgent);
			ps.setString(i++, a.Modality);
			ps.setString(i++, a.ScheduledStationAET);
		}
	}
}
