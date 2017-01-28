package net.metafusion.model;

import java.util.List;
import acme.db.JoinedView;
import acme.db.View;

public class PatientStudySeriesImageView extends JoinedView
{
	static PatientStudySeriesImageView view = null;

	static public synchronized PatientStudySeriesImageView get()
	{
		if (view == null)
		{
			view = new PatientStudySeriesImageView();
		}
		return view;
	}

	public PatientStudySeriesImageView()
	{
		super(PatientStudySeriesImage.class, new View[] { PatientView.get(), StudyView.get(), SeriesView.get(), ImageView.get() },
				"dcm_study.patientID = dcm_patient.patientID and " + "dcm_study.studyID = dcm_series.studyID and dcm_series.seriesID = dcm_image.seriesID");
	}

	protected void setObjectPart(Object object, int index, Object part)
	{
		PatientStudySeriesImage ps = (PatientStudySeriesImage) object;
		if (index == 0)
			ps.setPatient((Patient) part);
		else if (index == 1)
			ps.setStudy((Study) part);
		else if (index == 2)
			ps.setSeries((Series) part);
		else ps.setImage((Image) part);
	}

	public static void main(String[] args)
	{
		log("start");
		try
		{
			PatientStudySeriesImageView v = PatientStudySeriesImageView.get();
			// v.insert(new Image(1,"2",new Date(System.currentTimeMillis())));
			List l = v.selectAll();
			for (int i = 0; i < l.size(); i++)
			{
				PatientStudy ps = (PatientStudy) l.get(i);
				log("" + l.get(i));
			}
			PatientStudy ps = (PatientStudy) v.select1(null);
			log("" + ps);
			l = v.selectAll();
			for (int i = 0; i < l.size(); i++)
			{
				ps = (PatientStudy) l.get(i);
				log("" + l.get(i));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			log("Error: failed with " + e);
			System.exit(-1);
		}
	}
}
