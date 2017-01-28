package net.metafusion.model;

import acme.db.JoinedView;
import acme.db.View;

public class PatientStudyView extends JoinedView
{
	static PatientStudyView view = null;

	static public synchronized PatientStudyView get()
	{
		if (view == null)
		{
			view = new PatientStudyView();
		}
		return view;
	}

	public PatientStudyView()
	{
		super(PatientStudy.class, new View[] { PatientView.get(), StudyView.get() }, "dcm_study.patientID = dcm_patient.patientID");
	}

	protected void setObjectPart(Object object, int index, Object part)
	{
		PatientStudy ps = (PatientStudy) object;
		if (index == 0)
			ps.setPatient((Patient) part);
		else ps.setStudy((Study) part);
	}
}
