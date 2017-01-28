package net.metafusion.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import acme.db.JDBCUtil;
import acme.db.View;

public class RichMediaView extends View
{
	static RichMediaView	view		= null;
	private static String	tableName	= "dcm_richmedia";

	static public synchronized RichMediaView get()
	{
		if (view == null)
			view = new RichMediaView();
		return view;
	}

	RichMediaView()
	{
		super(tableName, new String[] { "mediaID" }, new String[] { "source", "studyid", "destination", "size", "timeEntered", "status" });
	}

	public RichMedia selectById(long id)
	{
		return (RichMedia) doSelect1(buildSelect("mediaID=?"), new Object[] { new Long(id) });
	}

	public long getNextId()
	{
		List l = selectWhereOrder("limit 0,1", "mediaID DESC");
		if (l.size() == 0)
			return 1;
		RichMedia rm = (RichMedia) l.get(0);
		return rm.getRichMediaID() + 1;
	}

	protected Object load(ResultSet rs, int offset) throws Exception
	{
		RichMedia a = new RichMedia();
		a.setRichMediaID(rs.getLong(offset++));
		a.setSource(rs.getString(offset++));
		a.setStudyid(rs.getLong(offset++));
		a.setDestination(rs.getString(offset++));
		a.setSize(rs.getLong(offset++));
		a.setTimeEntered(rs.getTimestamp(offset++));
		a.setStatus(rs.getString(offset++));
		return a;
	}

	public void createTable()
	{
		String createString = "create table "
				+ tableName
				+ " ( mediaID INTEGER NOT NULL, source VARCHAR(255), studyid INTEGER, destination varchar(512), size integer, timeEntered timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP, status varchar(20) NOT NULL, PRIMARY KEY(mediaID)) ENGINE=InnoDB DEFAULT CHARSET=latin1;";
		JDBCUtil.get().update(createString);
	}

	@Override
	protected void store(Object o, PreparedStatement ps, boolean pk, int i) throws Exception
	{
		RichMedia a = (RichMedia) o;
		if (pk)
			ps.setLong(i++, a.getRichMediaID());
		else
		{
			ps.setString(i++, a.getSource());
			ps.setLong(i++, a.getStudyid());
			ps.setString(i++, a.getDestination());
			ps.setLong(i++, a.getSize());
			ps.setTimestamp(i++, a.getTimeEntered());
			ps.setString(i++, a.getStatus());
		}
	}
}
