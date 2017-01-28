package net.metafusion.util;

import java.util.Iterator;
import net.metafusion.dataset.DS;
import net.metafusion.model.Patient;
import net.metafusion.model.Study;

class StudyIterator
{
	DicomStore store;
	DS tags;
	Iterator iter;

	StudyIterator(Patient p, DS tags)
	{
		store = DicomStore.get();
		this.tags = tags;
		this.p = p;
		iter = store.getLLStudyIterator();
		inPlace = false;
	}
	boolean inPlace = false;
	Patient p = null;
	Study study = null;

	// todo: date handling
	boolean match(DS ds, net.metafusion.util.Tag t, String value)
	{
		if (value == null || value.length() == 0 || t.getVR().getBaseType() != String.class) return true; // todo:
																											// support
																											// non-string
																											// ?
		String v = ds.getString(t);
		return v == null || value.indexOf(v) != -1;
	}

	boolean match(Study s, DS ds)
	{
		if (!match(ds, net.metafusion.util.Tag.StudyInstanceUID, s.studyUID)) return false;
		// todo: better date checking, other formats???
		if (!match(ds, net.metafusion.util.Tag.StudyDate, DicomUtil.formatDate(s.date))) return false;
		if (!match(ds, net.metafusion.util.Tag.StudyID, s.studyIDString)) return false;
		if (!match(ds, net.metafusion.util.Tag.AccessionNumber, s.accessionNumber)) return false;
		return true;
	}

	void load1(DS tags, DS d, net.metafusion.util.Tag t, Object value)
	{
		if (tags.contains(t)) d.put(t, value);
	}

	void load(DS tags, DS d)
	{
		if (study == null) return;
		load1(tags, d, net.metafusion.util.Tag.StudyInstanceUID, study.studyUID);
		load1(tags, d, net.metafusion.util.Tag.StudyDate, DicomUtil.formatDate(study.date));
		load1(tags, d, net.metafusion.util.Tag.StudyID, study.studyIDString);
		load1(tags, d, net.metafusion.util.Tag.AccessionNumber, study.accessionNumber);
	}

	boolean advance()
	{
		if (inPlace) return study != null;
		inPlace = true;
		study = null;
		for (;;)
		{
			if (!iter.hasNext()) return false;
			study = (Study) iter.next();
			if ((p != null ? p.patientID == study.patientID : true) && match(study, tags)) break;
		}
		return study != null;
	}

	public boolean hasNext()
	{
		return advance();
	}

	public Study next()
	{
		advance();
		inPlace = false;
		// Util.Log("study="+study);
		return study;
	}

	public void remove()
	{
		assert false;
	}
}
