package net.metafusion.model;

	import java.util.Comparator;

	public class PatientStudySeriesImageSorter implements Comparator<PatientStudySeriesImage>
	{
		public int compare(PatientStudySeriesImage arg0, PatientStudySeriesImage arg1)
		{
			long id0 = arg0.getSeries().seriesID;
			long id1 = arg1.getSeries().seriesID;
			if(id0 != id1)
			{
				if(id0 < id1)
					return -1;
				if(id0 > id1)
					return 1;
			}
			Integer ins0 = new Integer(arg0.getImage().instanceNumber.trim());
			Integer ins1 = new Integer(arg1.getImage().instanceNumber.trim());
			if(ins0 > ins1) return 1;
			if(ins0 < ins1) return -1;
			return 0;
		}
	}
