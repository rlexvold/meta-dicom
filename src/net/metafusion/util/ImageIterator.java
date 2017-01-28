package net.metafusion.util;

import java.util.Iterator;
import net.metafusion.dataset.DS;
import net.metafusion.model.Image;
import net.metafusion.model.Series;

class ImageIterator
{
	DicomStore store;
	DS tags;
	Iterator iter;

	ImageIterator(Series s, DS tags)
	{
		store = DicomStore.get();
		this.tags = tags;
		this.s = s;
		iter = store.getLLImageIterator();
	}
	boolean inPlace = false;
	Image image = null;
	Series s = null;

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

	boolean match(Image i, DS ds)
	{
		if (!match(ds, net.metafusion.util.Tag.ImageType, i.imageType)) return false;
		if (!match(ds, net.metafusion.util.Tag.MediaStorageSOPInstanceUID, i.imageUID)) return false;
		if (!match(ds, net.metafusion.util.Tag.MediaStorageSOPClassUID, i.classUID)) return false;
		return true;
	}

	void load1(DS tags, DS d, net.metafusion.util.Tag t, Object value)
	{
		if (tags.contains(t)) d.put(t, value);
	}

	void load(DS tags, DS d)
	{
		if (image == null) return;
		load1(tags, d, net.metafusion.util.Tag.SOPInstanceUID, image.imageUID);
		load1(tags, d, net.metafusion.util.Tag.SOPClassUID, image.classUID);
	}

	boolean advance()
	{
		if (inPlace) return image != null;
		inPlace = true;
		image = null;
		for (;;)
		{
			if (!iter.hasNext()) return false;
			image = (Image) iter.next();
			if ((s != null ? s.seriesID == image.seriesID : true) && match(image, tags)) break;
		}
		return image != null;
	}

	public boolean hasNext()
	{
		return advance();
	}

	public Image next()
	{
		advance();
		inPlace = false;
		// Util.Log("image="+image);
		return image;
	}

	public void remove()
	{
		assert false;
	}
}
