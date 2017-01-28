package integration;

import java.util.Date;

import net.metafusion.dataset.DS;
import net.metafusion.service.CFind;
import net.metafusion.util.DicomUtil;
import net.metafusion.util.Tag;

public class MFConverter
{
	public static synchronized String[] MoveListToDataSet(String moveList)
	{
		return moveList.split(",");
	}

	public static synchronized DS SearchBeanToDataSet(SearchBean sb, String level)
	{
		DS ds = CFind.buildSearch(Tag.PatientName, "");
		if (sb.getFirstName() != null && sb.getFirstName().trim() != "") ds.putString(Tag.PatientName, sb.getFirstName().trim());
		if (sb.getLastName() != null && sb.getLastName().trim() != "") ds.putString(Tag.PatientName, sb.getLastName().trim());
		ds.putString(Tag.QueryRetrieveLevel, level);
		if (sb.getPatientID() != null && sb.getPatientID().trim() != "") ds.putString(Tag.PatientID, sb.getPatientID().trim());
		String fromDate = sb.getFromDate().trim();
		String toDate = sb.getToDate().trim();
		if (fromDate == null || fromDate.length() == 0)
		{
			if (toDate != null && toDate.length() != 0)
			{
				ds.putString(Tag.StudyDate, "19010101-" + toDate);
			}
		} else
		{
			if (toDate == null || toDate.length() == 0)
			{
				Date tmp = new Date();
				java.sql.Date today = new java.sql.Date(tmp.getTime());
				ds.putString(Tag.StudyDate, fromDate + "-" + DicomUtil.formatDate(today));
			} else
			{
				ds.putString(Tag.StudyDate, fromDate + "-" + toDate);
			}
		}
		if (sb.getAccessionNum() != null && sb.getAccessionNum().trim() != "") ds.putString(Tag.AccessionNumber, sb.getAccessionNum().trim());
		if (sb.getReferringPhysician() != null && sb.getReferringPhysician().trim() != "") ds.putString(Tag.ReferringPhysicianName, sb.getReferringPhysician().trim());
		if (sb.getModalityList() != null && sb.getModalityList().trim() != "") ds.putString(Tag.ModalitiesInStudy, sb.getModalityList().trim());
		return ds;
	}

	public static synchronized DS MFStudyToDataSet(MFStudy mfstudy)
	{
		DS ds = new DS();
		ds.putString(Tag.StudyID, new Long(mfstudy.studyID).toString());
		ds.putString(Tag.StudyID, mfstudy.studyIDString);
		ds.putString(Tag.Status, mfstudy.state);
		ds.putString(Tag.StudyInstanceUID, mfstudy.studyUID);
		ds.putString(Tag.PatientID, mfstudy.patientID);
		ds.putString(Tag.PatientName, mfstudy.patientName);
		ds.putString(Tag.PatientSex, mfstudy.patientSex);
		ds.putString(Tag.PatientBirthDate, mfstudy.patientBDay);
		ds.putString(Tag.StudyDate, mfstudy.dateTime);
		ds.putString(Tag.Modality, mfstudy.modality);
		ds.putString(Tag.StudyDescription, mfstudy.description);
		ds.putString(Tag.ReferringPhysicianName, mfstudy.referrer);
		ds.putString(Tag.InstitutionName, mfstudy.institution);
		ds.putString(Tag.StationName, mfstudy.station);
		return ds;
	}

	public static synchronized MFExtendedStudy DataSetToMFExtendedStudy(DS ds)
	{
		MFExtendedStudy mfstudy = new MFExtendedStudy();
		String studyID = ds.getString(Tag.StudyID);
		// if(studyID != null && studyID.length() > 0)
		// mfstudy.studyID = new Long(studyID);
		mfstudy.studyIDString = ds.getString(Tag.StudyID);
		mfstudy.state = ds.getString(Tag.Status);
		mfstudy.studyUID = ds.getString(Tag.StudyInstanceUID);
		mfstudy.patientID = ds.getString(Tag.PatientID);
		mfstudy.patientName = ds.getString(Tag.PatientName);
		mfstudy.patientSex = ds.getString(Tag.PatientSex);
		mfstudy.patientBDay = ds.getString(Tag.PatientBirthDate);
		mfstudy.dateTime = ds.getString(Tag.StudyDate);
		mfstudy.modality = ds.getString(Tag.Modality);
		mfstudy.description = ds.getString(Tag.StudyDescription);
		mfstudy.referrer = ds.getString(Tag.ReferringPhysicianName);
		mfstudy.institution = ds.getString(Tag.InstitutionName);
		mfstudy.station = ds.getString(Tag.StationName);
		mfstudy.accessionNumber = ds.getString(Tag.AccessionNumber);
		mfstudy.reader = "";
		mfstudy.series = new MFSeries[0];
		mfstudy.dictations = new MFDictation[0];
		mfstudy.reports = new MFReport[0];
		mfstudy.attachments = new MFAttachment[0];
		mfstudy.notes = new MFNote();
		return mfstudy;
	}
}
