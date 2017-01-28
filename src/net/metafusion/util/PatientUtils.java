package net.metafusion.util;

import java.io.File;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSInputStream;
import net.metafusion.model.Image;
import net.metafusion.model.ImageView;
import net.metafusion.model.Patient;
import net.metafusion.model.PatientStudy;
import net.metafusion.model.PatientStudyView;
import net.metafusion.model.PatientView;
import net.metafusion.model.Study;
import net.metafusion.model.StudyView;
import acme.db.JDBCUtil;
import acme.storage.SSStore;
import acme.storage.SSStoreFactory;
import acme.util.Util;

public class PatientUtils
{
	private boolean dbEqualsMetaFile(Patient dbPatient, DS metaFile)
	{
		if (!dbPatient.getExtID().equalsIgnoreCase(metaFile.getString(Tag.PatientID)))
			return false;
		if (!dbPatient.getDicomName().equalsIgnoreCase(metaFile.getString(Tag.PatientName)))
			return false;
		if (!dbPatient.getSex().equalsIgnoreCase(metaFile.getString(Tag.PatientSex)))
			return false;
		return true;
	}

	private DS getMdfInfo(long studyID, long imageID) throws Exception
	{
		SSStore store = SSStoreFactory.getStore();
		File imageFile = store.getFile(studyID, imageID);
		InputStream mdfis = SSStore.getInputStream(imageFile);
		UID syntax = UID.ImplicitVRLittleEndian;
		DSInputStream dis = new DSInputStream(mdfis, syntax);
		DS ds = new DS();
		ds = dis.readDS(ds);
		mdfis.close();
		return ds;
	}

	private Image getSingleImage(long studyID)
	{
		ImageView iv = ImageView.get();
		List images = iv.selectWhere("studyID='" + studyID + "' LIMIT 0,1");
		Image image = (Image) images.get(0);
		return image;
	}

	public void cleanupStudy(long studyID, DS ds)
	{
		Patient p = PatientView.get().selectUnique(ds.getString(Tag.PatientID), ds.getString(Tag.PatientName), ds.getString(Tag.PatientBirthDate), ds.getString(Tag.PatientSex));
		if (p == null)
		{
			// Create new patient entry
			p = new Patient();
			long patientID = net.metafusion.localstore.DicomStore.get().getNextID();
			p.setPatientID(patientID);
			p.setDicomName(ds.getString(Tag.PatientName));
			PN pn = new PN(p.getDicomName());
			p.setFirstName(pn.getFirst());
			p.setMiddleName(pn.getMiddle());
			p.setLastName(pn.getLast());
			p.setSex(ds.getString(Tag.PatientSex));
			p.setExtID(ds.getString(Tag.PatientID));
			p.setDob(DicomUtil.parseDate(ds.getString(Tag.PatientBirthDate)));
			p.setDateEntered(new Timestamp(System.currentTimeMillis()));
			p.setDateModified(new Timestamp(System.currentTimeMillis()));
			PatientView.get().insert(p);
		}
		// Change entry in Study and Series tables
		StudyView piv = StudyView.get();
		Study study = piv.selectByID(studyID);
		study.setPatientID(p.getPatientID());
		piv.update(study);
		JDBCUtil.get().update("update dcm_series set patientID=" + p.getPatientID() + " where studyID=" + studyID);
		JDBCUtil.get().update("update dcm_image set patientID=" + p.getPatientID() + " where studyID=" + studyID);
	}

	public ArrayList<String> patientCleanup(List<PatientStudyView> patients)
	{
		// Iterate through all series in the list
		Iterator i = patients.iterator();
		ArrayList<String> returnList = new ArrayList<String>();
		while (i.hasNext())
		{
			try
			{
				PatientStudy pv = (PatientStudy) i.next();
				// Pick one representative image file from the series
				Image image = getSingleImage(pv.getStudy().getStudyID());
				// Read in the metadata of the image file
				DS ds = getMdfInfo(pv.getStudy().studyID, image.imageID);
				// Compare file to DB
				if (dbEqualsMetaFile(pv.getPatient(), ds) == false)
				{
					cleanupStudy(pv.getStudy().getStudyID(), ds);
					returnList.add(pv.getStudy().getStudyUID());
				}
			}
			catch (Exception e)
			{
				Util.log(e.getMessage());
			}
		}
		return returnList;
	}

	public ArrayList<String> patientCleanup(String[] patients)
	{
		PatientStudyView v = PatientStudyView.get();
		String patientList = "";
		for (int i = 0; i < patients.length; i++)
		{
			if (i == patients.length - 1)
				patientList += "'" + patients[i] + "'";
			else
				patientList = patientList + "'" + patients[i] + "',";
		}
		return patientCleanup(v.selectWhere("patientid in (" + patientList + ");"));
	}

	public ArrayList<String> patientCleanupStudy(String[] studies)
	{
		PatientStudyView v = PatientStudyView.get();
		String studiesList = "";
		for (int i = 0; i < studies.length; i++)
		{
			if (i == studies.length - 1)
				studiesList += "'" + studies[i] + "'";
			else
				studiesList = studiesList + "'" + studies[i] + "',";
		}
		return patientCleanup(v.selectWhere("studyUID in (" + studiesList + ");"));
	}

	public ArrayList<String> patientCleanupAll()
	{
		PatientStudyView v = PatientStudyView.get();
		return patientCleanup(v.selectAll());
	}
}
