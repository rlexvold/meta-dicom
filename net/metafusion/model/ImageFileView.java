package net.metafusion.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import acme.db.View;

public class ImageFileView extends View
{
	static ImageFileView	view	= null;

	static public synchronized ImageFileView get()
	{
		if (view == null)
		{
			view = new ImageFileView();
		}
		return view;
	}

	ImageFileView()
	{
		super("dcm_image", new String[] { "imageID" }, new String[] { "studyID", "seriesID" });
	}

	public ImageFile selectByID(long id)
	{
		return (ImageFile) doSelect1(buildSelect("imageID=?"), new Object[] { new Long(id) });
	}

	public List<ImageFile> selectBySeries(long seriesID)
	{
		return (List<ImageFile>) selectWhere("seriesid = " + seriesID);
	}

	//
	protected Object load(ResultSet rs, int offset) throws Exception
	{
		ImageFile i = new ImageFile();
		i.setImageID(rs.getLong(offset++));
		i.setStudyID(rs.getLong(offset++));
		i.setSeriesID(rs.getLong(offset++));
		return i;
	}

	protected void store(Object o, PreparedStatement ps, boolean pk, int i) throws Exception
	{
		ImageFile a = (ImageFile) o;
		if (pk)
			ps.setLong(i++, a.getImageID());
		else
		{
			ps.setLong(i++, a.getStudyID());
			ps.setLong(i++, a.getSeriesID());
		}
	}
}
