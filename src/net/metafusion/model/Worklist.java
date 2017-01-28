package net.metafusion.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import net.metafusion.Dicom;
import net.metafusion.dataset.DS;
import net.metafusion.util.DSList;
import net.metafusion.util.Tag;
import acme.util.Log;

public class Worklist
{
	protected static void log(String s)
	{
		Log.log(s);
	}

	protected static void vlog(String s)
	{
		Log.vlog(s);
	}
	public Long		WorklistID						= 0L;
	public String	PatientSex						= "";
	public String	Referrer						= "";
	public String	StudyInstanceUID				= "";
	public String	StudyDate						= "";
	public String	State							= "";				// 'N', 'Q'ueued, 'I'nProgress, 'C'ompleted
	public String	AETitle							= "";
	// String Modality;
	public String	AccessionNumber					= "";
	public String	PatientName						= "";
	public String	PatientID						= "";
	// String RequestingPhysician;
	public String	RequestedProcedureDescription	= "";
	public String	RequestedProcedureID			= "";
	public String	PatientBirthDate				= "";
	// String RequestedProcedureCodeSequence;
	static List		wlList							= new ArrayList();
	static List		psList							= new ArrayList();
	static
	{
		Worklist wl = new Worklist();
		wl.State = "N";
		wl.AETitle = "ModalitySCU";
		wl.AccessionNumber = "ACC001";
		wl.PatientName = "PATIENT^FOO";
		wl.PatientID = "PAT";
		wl.StudyInstanceUID = "1.2.3.4.5.6.7.8.9";
		wl.RequestedProcedureDescription = "SOME STUDY DESC";
		wlList.add(wl);
		ProcedureStep ps = new ProcedureStep();
		ps.StudyInstanceUID = wl.StudyInstanceUID;
		ps.ScheduledProcedureStepID = "1000";
		ps.PerformedStationAETitle = "";
		ps.ScheduledProcedureStepStartDate = "20051025";
		ps.ScheduledProcedureStepStartTime = "174723.000";
		ps.PerformedProcedureStepStartDate = "";
		ps.PerformedProcedureStepStartTime = "";
		ps.PerformedProcedureStepEndDate = "";
		ps.PerformedProcedureStepEndTime = "";
		ps.ScheduledPerformingPhysiciansName = "Dr foo";
		ps.ScheduledProcedureStepDescription = "SS description";
		// ps.ScheduledActionItemCodeSequence = "code seq";
		ps.PerformedProcedureStepStatus = "";
		ps.PerformedProcedureStepDescription = "";
		ps.PerformedProcedureTypeDescription = "";
		ps.RequestedContrastAgent = "";
		ps.Modality = "";
		ps.ScheduledStationAET = wl.AETitle;
		psList.add(ps);
	}

	public static ProcedureStep findPS(String studyUID, String spsID)
	{
		log("findPS by (by studyuid, and scheduled proc step id): " + studyUID + ":" + spsID);
		ProcedureStep ps = ProcedureStepView.get().selectByStudyInstanceUIDAndScheduledProcedureStepID(studyUID, spsID);
		if (ps == null)
			log("findPS: not found!!!");
		return ps;
	}

	public static ProcedureStep findPS(String spsUID)
	{
		log("findProcedureStep (by step uid): " + spsUID);
		ProcedureStep ps = ProcedureStepView.get().selectByStoreProcedureStepUID(spsUID);
		if (ps == null)
			log("findProcedureStep: not found!!!");
		return ps;
	}

	public static List findPSs(String studyUID)
	{
		log("findPSs: " + studyUID);
		List l = ProcedureStepView.get().selectByStudyInstanceUID(studyUID);
		if (l.size() == 0)
			log("findPSs: none found!!!");
		return l;
	}

	public static Worklist findWL(String studyUID)
	{
		log("findWL: " + studyUID);
		Worklist wl = WorklistView.get().selectByUID(studyUID);
		if (wl == null)
			log("findWL: not found!!!");
		return wl;
	}

	public static List findWLs(String aet)
	{
		log("findWL (today only): " + aet);
		List l = WorklistView.get().selectTodayForAET(aet);
		if (l.size() == 0)
			log("findWLs: none found!!!");
		return l;
	}
	static long	lastID	= 0;

	public static List queryWorklist(String aet)
	{
		List dsList = new LinkedList();
		List wlList = findWLs(aet);
		for (Iterator iter = wlList.iterator(); iter.hasNext();)
		{
			Worklist wl = (Worklist) iter.next();
			DS ds = new DS();
			ds.put(Tag.AccessionNumber, wl.AccessionNumber);
			ds.put(Tag.PatientName, wl.PatientName);
			ds.put(Tag.PatientID, wl.PatientID);
			ds.put(Tag.PatientBirthDate, wl.PatientBirthDate);
			ds.put(Tag.StudyInstanceUID, wl.StudyInstanceUID);
			ds.put(Tag.RequestedProcedureDescription, wl.RequestedProcedureDescription);
			ds.put(Tag.RequestedProcedureID, wl.RequestedProcedureID);
			ds.put(Tag.PatientSex, wl.PatientSex);
			ds.put(Tag.ReferringPhysicianName, wl.Referrer);
			List psList = findPSs(wl.StudyInstanceUID);
			DSList l = new DSList();
			for (Iterator psiter = psList.iterator(); psiter.hasNext();)
			{
				ProcedureStep p = (ProcedureStep) psiter.next();
				log("found PS: ScheduledProcedureStepID=" + p.ScheduledProcedureStepID);
				DS pds = new DS();
				pds.put(Tag.Modality, p.Modality);
				pds.put(Tag.ScheduledStationAET, p.ScheduledStationAET);
				pds.put(Tag.RequestedContrastAgent, p.RequestedContrastAgent);
				pds.put(Tag.SPSStartDate, p.ScheduledProcedureStepStartDate);
				pds.put(Tag.SPSStartTime, p.ScheduledProcedureStepStartTime);
				pds.put(Tag.ScheduledPerformingPhysicianName, p.ScheduledPerformingPhysiciansName);
				pds.put(Tag.SPSDescription, p.ScheduledProcedureStepDescription);
				// pds.put(Tag.ScheduledProtocolCodeSeq,
				// p.ScheduledActionItemCodeSequence);
				pds.put(Tag.SPSID, p.ScheduledProcedureStepID);
				l.add(pds);
			}
			ds.put(Tag.SPSSeq, l);
			dsList.add(ds);
		}
		return dsList;
	}

	static public void processCreatePerformedProcedureStep(String psUID, DS ds)
	{
		log("processCreatePerformedProcedureStep:  uid=" + psUID + " ds=" + ds);
		DSList dsl = (DSList) ds.get(Tag.ScheduledStepAttributesSeq);
		if (dsl == null)
		{
			log("error: request missing ScheduledStepAttributesSeq");
			return;
		}
		// we should only have 1 iteration here
		for (Iterator iter = dsl.iterator(); iter.hasNext();)
		{
			DS ssDS = (DS) iter.next();
			String studyUID = (String) ssDS.getString(Tag.StudyInstanceUID, "");
			String spsID = (String) ssDS.getString(Tag.SPSID, "");
			if (studyUID == null || spsID == null)
			{
				log("missing studyUID or spsID");
				continue;
			}
			Worklist wl = findWL(studyUID);
			if (wl == null)
			{
				log("could not find worklist " + studyUID);
				continue;
			}
			ProcedureStep ps = findPS(studyUID, spsID);
			if (ps == null)
			{
				log("could not find ProcedureStep for scheduled procedure step id=" + spsID);
				continue;
			}
			// ignored for save
			wl.PatientName = ds.getString(Tag.PatientName, wl.PatientName);
			wl.PatientID = ds.getString(Tag.PatientID, wl.PatientID);
			ps.StoredProcedureStepUID = psUID;
			ps.PerformedStationAETitle = ds.getString(Tag.PerformedStationAET, ps.PerformedStationAETitle);
			ps.PerformedProcedureStepStartDate = ds.getString(Tag.PPSStartDate, ps.PerformedProcedureStepStartDate);
			ps.PerformedProcedureStepStartTime = ds.getString(Tag.PPSStartTime, ps.PerformedProcedureStepStartTime);
			ps.PerformedProcedureStepEndDate = ds.getString(Tag.PPSEndDate, ps.PerformedProcedureStepEndDate);
			ps.PerformedProcedureStepEndTime = ds.getString(Tag.PPSEndTime, ps.PerformedProcedureStepEndTime);
			ps.PerformedProcedureStepStatus = ds.getString(Tag.PPSStatus, ps.PerformedProcedureStepStatus);
			ps.PerformedProcedureStepDescription = ds.getString(Tag.PPSStatus, ps.PerformedProcedureStepDescription);
			ps.PerformedProcedureTypeDescription = ds.getString(Tag.PerformedProcedureTypeDescription, ps.PerformedProcedureTypeDescription);
			ProcedureStepView.get().update(ps);
		}
	}

	static public void processSetPerformedProcedureStep(String uid, DS ds)
	{
		log("processSetPerformedProcedureStep:  uid=" + uid + " ds=" + ds);
		ProcedureStep ps = findPS(uid);
		if (ps == null)
			return;
		ps.PerformedProcedureStepEndDate = ds.getString(Tag.PPSEndDate, ps.PerformedProcedureStepEndDate);
		ps.PerformedProcedureStepEndTime = ds.getString(Tag.PPSEndTime, ps.PerformedProcedureStepEndTime);
		ps.PerformedProcedureStepStatus = ds.getString(Tag.PPSStatus, ps.PerformedProcedureStepStatus);
		ProcedureStepView.get().update(ps);
		// }
	}

	public Long getWorklistID()
	{
		return WorklistID;
	}

	public void setWorklistID(Long worklistID)
	{
		WorklistID = worklistID;
	}

	public String getStudyInstanceUID()
	{
		return StudyInstanceUID;
	}

	public void setStudyInstanceUID(String studyInstanceUID)
	{
		StudyInstanceUID = studyInstanceUID;
	}

	public String getStudyDate()
	{
		return StudyDate;
	}

	public void setStudyDate(String studyDate)
	{
		StudyDate = studyDate;
	}

	public String getState()
	{
		return State;
	}

	public void setState(String state)
	{
		State = state;
	}

	public String getAETitle()
	{
		return AETitle;
	}

	public void setAETitle(String title)
	{
		AETitle = title;
	}

	public String getAccessionNumber()
	{
		return AccessionNumber;
	}

	public void setAccessionNumber(String accessionNumber)
	{
		AccessionNumber = accessionNumber;
	}

	public String getPatientName()
	{
		return PatientName;
	}

	public void setPatientName(String patientName)
	{
		PatientName = patientName;
	}

	public String getPatientID()
	{
		return PatientID;
	}

	public void setPatientID(String patientID)
	{
		PatientID = patientID;
	}

	public String getRequestedProcedureDescription()
	{
		return RequestedProcedureDescription;
	}

	public void setRequestedProcedureDescription(String requestedProcedureDescription)
	{
		RequestedProcedureDescription = requestedProcedureDescription;
	}

	public String getRequestedProcedureID()
	{
		return RequestedProcedureID;
	}

	public void setRequestedProcedureID(String requestedProcedureID)
	{
		RequestedProcedureID = requestedProcedureID;
	}

	public String getPatientBirthDate()
	{
		return PatientBirthDate;
	}

	public void setPatientBirthDate(String patientBirthDate)
	{
		PatientBirthDate = patientBirthDate;
	}

	public static List getWlList()
	{
		return wlList;
	}

	public static void setWlList(List wlList)
	{
		Worklist.wlList = wlList;
	}

	public static List getPsList()
	{
		return psList;
	}

	public static void setPsList(List psList)
	{
		Worklist.psList = psList;
	}

	public static long getLastID()
	{
		return lastID;
	}

	public static void setLastID(long lastID)
	{
		Worklist.lastID = lastID;
	}

	public String getPatientSex()
	{
		return PatientSex;
	}

	public void setPatientSex(String patientSex)
	{
		PatientSex = patientSex;
	}

	public String getReferrer()
	{
		return Referrer;
	}

	public void setReferrer(String referrer)
	{
		Referrer = referrer;
	}
}