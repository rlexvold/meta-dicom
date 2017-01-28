package net.metafusion.model;

public class ProcedureStep
{
	public String ScheduledProcedureStepID = "";
	public String StudyInstanceUID = "";
	public String StoredProcedureStepUID = "";
	public String PerformedStationAETitle = "";
	// String RequestedProcedureID;
	// String PerformedProcedureStepID;
	public String ScheduledProcedureStepStartDate = "";
	public String ScheduledProcedureStepStartTime = "";
	public String PerformedProcedureStepStartDate = "";
	public String PerformedProcedureStepStartTime = "";
	public String PerformedProcedureStepEndDate = "";
	public String PerformedProcedureStepEndTime = "";
	public String ScheduledPerformingPhysiciansName = "";
	public String ScheduledProcedureStepDescription = "";
	// public String ScheduledActionItemCodeSequence="";
	public String PerformedProcedureStepStatus = "";
	public String PerformedProcedureStepDescription = "";
	public String PerformedProcedureTypeDescription = "";
	// String ProtocolName;
	// String SeriesInstanceUID;
	// String PerformingPhysiciansName;
	// String OperatorsName;
	// String SeriesDescription;
	public String RequestedContrastAgent = "";
	public String Modality = "";
	public String ScheduledStationAET = "";

	public String getStudyInstanceUID()
	{
		return StudyInstanceUID;
	}

	public void setStudyInstanceUID(String studyInstanceUID)
	{
		StudyInstanceUID = studyInstanceUID;
	}

	public String getScheduledProcedureStepID()
	{
		return ScheduledProcedureStepID;
	}

	public void setScheduledProcedureStepID(String scheduledProcedureStepID)
	{
		ScheduledProcedureStepID = scheduledProcedureStepID;
	}

	public String getStoredProcedureStepUID()
	{
		return StoredProcedureStepUID;
	}

	public void setStoredProcedureStepUID(String storedProcedureStepUID)
	{
		StoredProcedureStepUID = storedProcedureStepUID;
	}

	public String getPerformedStationAETitle()
	{
		return PerformedStationAETitle;
	}

	public void setPerformedStationAETitle(String performedStationAETitle)
	{
		PerformedStationAETitle = performedStationAETitle;
	}

	public String getScheduledProcedureStepStartDate()
	{
		return ScheduledProcedureStepStartDate;
	}

	public void setScheduledProcedureStepStartDate(String scheduledProcedureStepStartDate)
	{
		ScheduledProcedureStepStartDate = scheduledProcedureStepStartDate;
	}

	public String getScheduledProcedureStepStartTime()
	{
		return ScheduledProcedureStepStartTime;
	}

	public void setScheduledProcedureStepStartTime(String scheduledProcedureStepStartTime)
	{
		ScheduledProcedureStepStartTime = scheduledProcedureStepStartTime;
	}

	public String getPerformedProcedureStepStartDate()
	{
		return PerformedProcedureStepStartDate;
	}

	public void setPerformedProcedureStepStartDate(String performedProcedureStepStartDate)
	{
		PerformedProcedureStepStartDate = performedProcedureStepStartDate;
	}

	public String getPerformedProcedureStepStartTime()
	{
		return PerformedProcedureStepStartTime;
	}

	public void setPerformedProcedureStepStartTime(String performedProcedureStepStartTime)
	{
		PerformedProcedureStepStartTime = performedProcedureStepStartTime;
	}

	public String getPerformedProcedureStepEndDate()
	{
		return PerformedProcedureStepEndDate;
	}

	public void setPerformedProcedureStepEndDate(String performedProcedureStepEndDate)
	{
		PerformedProcedureStepEndDate = performedProcedureStepEndDate;
	}

	public String getPerformedProcedureStepEndTime()
	{
		return PerformedProcedureStepEndTime;
	}

	public void setPerformedProcedureStepEndTime(String performedProcedureStepEndTime)
	{
		PerformedProcedureStepEndTime = performedProcedureStepEndTime;
	}

	public String getScheduledPerformingPhysiciansName()
	{
		return ScheduledPerformingPhysiciansName;
	}

	public void setScheduledPerformingPhysiciansName(String scheduledPerformingPhysiciansName)
	{
		ScheduledPerformingPhysiciansName = scheduledPerformingPhysiciansName;
	}

	public String getScheduledProcedureStepDescription()
	{
		return ScheduledProcedureStepDescription;
	}

	public void setScheduledProcedureStepDescription(String scheduledProcedureStepDescription)
	{
		ScheduledProcedureStepDescription = scheduledProcedureStepDescription;
	}

	public String getPerformedProcedureStepStatus()
	{
		return PerformedProcedureStepStatus;
	}

	public void setPerformedProcedureStepStatus(String performedProcedureStepStatus)
	{
		PerformedProcedureStepStatus = performedProcedureStepStatus;
	}

	public String getPerformedProcedureStepDescription()
	{
		return PerformedProcedureStepDescription;
	}

	public void setPerformedProcedureStepDescription(String performedProcedureStepDescription)
	{
		PerformedProcedureStepDescription = performedProcedureStepDescription;
	}

	public String getPerformedProcedureTypeDescription()
	{
		return PerformedProcedureTypeDescription;
	}

	public void setPerformedProcedureTypeDescription(String performedProcedureTypeDescription)
	{
		PerformedProcedureTypeDescription = performedProcedureTypeDescription;
	}

	public String getRequestedContrastAgent()
	{
		return RequestedContrastAgent;
	}

	public void setRequestedContrastAgent(String requestedContrastAgent)
	{
		RequestedContrastAgent = requestedContrastAgent;
	}

	public String getModality()
	{
		return Modality;
	}

	public void setModality(String modality)
	{
		Modality = modality;
	}

	public String getScheduledStationAET()
	{
		return ScheduledStationAET;
	}

	public void setScheduledStationAET(String scheduledStationAET)
	{
		ScheduledStationAET = scheduledStationAET;
	}
}
