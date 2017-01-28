package net.metafusion.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Iterator;
import java.util.Set;

import net.metafusion.dataset.DS;
import net.metafusion.util.Tag;
import acme.db.JDBCUtil;
import acme.db.View;

public class DicomHeaderView extends View
{
	static DicomHeaderView	view		= null;
	private static String	tableName	= "dcm_header";

	static public synchronized DicomHeaderView get()
	{
		if (view == null)
			view = new DicomHeaderView();
		return view;
	}

	DicomHeaderView()
	{
		super(tableName, new String[] { "imageID", "dicomTag" }, new String[] { "dicomValue" });
	}

	protected Object load(ResultSet rs, int offset) throws Exception
	{
		DicomHeader a = new DicomHeader();
		a.setImageID(rs.getLong(offset++));
		a.setDicomTag(rs.getString(offset++));
		a.setDicomValue(rs.getString(offset++));
		// a.setDicomHeader((DS) rs.getBlob(offset++));
		return a;
	}

	public void createTable()
	{
		String create = "create table " + tableName
				+ " ( imageID bigint(20) NOT NULL, dicomTag char(15) NOT NULL, dicomValue text, PRIMARY KEY(imageID, dicomTag)) ENGINE=InnoDB DEFAULT CHARSET=latin1;";
		JDBCUtil.get().update(create);
	}

	@Override
	protected void store(Object o, PreparedStatement ps, boolean pk, int i) throws Exception
	{
		DicomHeader a = (DicomHeader) o;
		if (pk)
		{
			ps.setLong(i++, a.getImageID());
			ps.setString(i++, a.getDicomTag());
		}
		else
		{
			ps.setString(i++, a.getDicomValue());
		}
	}

	public void insertFullHeader(long imageID, DS ds)
	{
		String sql = "insert into " + tableName + "(imageID, dicomTag, dicomValue) values ";
		boolean first = true;
		Set tags = ds.getTags();
		Iterator<Tag> i = tags.iterator();
		DicomHeader dh = new DicomHeader();
		dh.setImageID(imageID);
		while (i.hasNext())
		{
			Tag tag = i.next();
			if (tag != Tag.PixelData)
			{
				Object value = ds.get(tag);
				if (value != null)
				{
					if (!first)
						sql += ",";
					sql += "(" + imageID + ",'" + tag.getTagString() + "','" + value.toString() + "')";
					first = false;
				}
			}
		}
		sql += ";";
		JDBCUtil.get().update(sql);
	}
}
