package net.metafusion.util;

import java.util.Iterator;
import net.metafusion.dataset.DS;
import net.metafusion.model.Series;
import net.metafusion.model.Study;

class SeriesIterator
{
	DicomStore store;
	DS tags;
	Iterator iter;

	SeriesIterator(Study s, DS tags)
	{
		store = DicomStore.get();
		this.tags = tags;
		this.s = s;
		iter = store.getLLSeriesIterator();
		inPlace = false;
	}
	boolean inPlace = false;
	Series series = null;
	Study s = null;

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

	boolean match(Series s, DS ds)
	{
		if (!match(ds, net.metafusion.util.Tag.Modality, s.modality)) return false;
		if (!match(ds, net.metafusion.util.Tag.SeriesInstanceUID, s.seriesUID)) return false;
		// todo: better date checking, other formats???
		if (!match(ds, net.metafusion.util.Tag.SeriesDate, DicomUtil.formatDate(s.date))) return false;
		if (!match(ds, net.metafusion.util.Tag.PerformingPhysicianName, s.physicianName)) return false;
		if (!match(ds, net.metafusion.util.Tag.OperatorName, s.operatorName)) return false;
		return true;
	}

	void load1(DS tags, DS d, net.metafusion.util.Tag t, Object value)
	{
		if (tags.contains(t)) d.put(t, value);
	}

	void load(DS tags, DS d)
	{
		if (series == null) return;
		load1(tags, d, net.metafusion.util.Tag.Modality, series.modality);
		load1(tags, d, net.metafusion.util.Tag.SeriesInstanceUID, series.seriesUID);
		load1(tags, d, net.metafusion.util.Tag.SeriesDate, DicomUtil.formatDate(series.date));
		load1(tags, d, net.metafusion.util.Tag.PerformingPhysicianName, series.physicianName);
		load1(tags, d, net.metafusion.util.Tag.OperatorName, series.operatorName);
	}

	boolean advance()
	{
		if (inPlace) return series != null;
		inPlace = true;
		series = null;
		for (;;)
		{
			if (!iter.hasNext()) return false;
			series = (Series) iter.next();
			if ((s != null ? s.studyID == series.studyID : true) && match(series, tags)) break;
		}
		return series != null;
	}

	public boolean hasNext()
	{
		return advance();
	}

	public Series next()
	{
		advance();
		inPlace = false;
		// Util.Log("series="+series);
		return series;
	}

	public void remove()
	{
		assert false;
	}
}
