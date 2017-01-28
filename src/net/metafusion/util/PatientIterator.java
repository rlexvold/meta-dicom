package net.metafusion.util;

import java.util.Iterator;
import net.metafusion.dataset.DS;
import net.metafusion.model.Patient;

class PatientIterator
{
	DicomStore store;
	DS tags;
	Iterator iter;

	PatientIterator(DS tags)
	{
		store = DicomStore.get();
		this.tags = tags;
		iter = store.getLLPatientIterator();
	}
	boolean inPlace = false;;
	DS current;
	Patient p = null;

	// todo: date handling
	boolean match(DS ds, Tag t, String value)
	{
		if (value == null || value.length() == 0 || t.getVR().getBaseType() != String.class) return true; // todo:
																											// support
																											// non-string
																											// ?
		String v = ds.getString(t);
		return v == null || value.indexOf(v) != -1;
	}

	boolean match(Patient p, DS ds)
	{
		if (!match(ds, Tag.PatientName, p.dicomName)) return false;
		if (!match(ds, Tag.PatientSex, p.sex)) return false;
		if (!match(ds, Tag.PatientID, p.extID)) return false;
		// todo: better date checking, other formats???
		if (!match(ds, Tag.PatientBirthDate, DicomUtil.formatDate(p.dob))) return false;
		return true;
	}

	void load1(DS tags, DS d, Tag t, Object value)
	{
		if (tags.contains(t)) d.put(t, value);
	}

	void load(DS tags, DS d)
	{
		if (p == null) return;
		load1(tags, d, Tag.PatientName, p.dicomName);
		load1(tags, d, Tag.PatientSex, p.sex);
		load1(tags, d, Tag.PatientID, p.extID);
		load1(tags, d, Tag.PatientBirthDate, DicomUtil.formatDate(p.dob));
	}

	boolean advance()
	{
		if (inPlace) return p != null;
		inPlace = true;
		p = null;
		for (;;)
		{
			if (!iter.hasNext()) return false;
			p = (Patient) iter.next();
			if (match(p, tags)) break;
		}
		return p != null;
	}

	public boolean hasNext()
	{
		return advance();
	}

	public Patient next()
	{
		advance();
		inPlace = false;
		DS ds = new DS();
		// Util.Log("patient="+p);
		return p;
	}

	public void remove()
	{
		assert false;
	}
}
