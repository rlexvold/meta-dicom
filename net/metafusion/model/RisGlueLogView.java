package net.metafusion.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import acme.db.JDBCUtil;
import acme.db.View;

public class RisGlueLogView extends View
{
	static RisGlueLogView	view		= null;
	private static String	tableName	= "ris_glue_log";

	static public synchronized RisGlueLogView get()
	{
		if (view == null)
			view = new RisGlueLogView();
		return view;
	}

	public long getLastId()
	{
		List l = selectWhereOrder("limit 0,1", "logID DESC");
		if (l.size() == 0)
			return 0;
		RisGlueLog rl = (RisGlueLog) l.get(0);
		return rl.getLogID();
	}

	RisGlueLogView()
	{
		super(tableName, new String[] { "logID" }, new String[] { "ris", "input", "output", "timeEntered", "status" });
	}

	protected Object load(ResultSet rs, int offset) throws Exception
	{
		RisGlueLog a = new RisGlueLog();
		a.setLogID(rs.getLong(offset++));
		a.setRis(rs.getString(offset++));
		a.setInput(rs.getString(offset++));
		a.setOutput(rs.getString(offset++));
		a.setTimeEntered(rs.getTimestamp(offset++));
		a.setStatus(rs.getString(offset++));
		return a;
	}

	public void createTable()
	{
		String createString = "create table "
				+ tableName
				+ " ( logID INTEGER NOT NULL AUTO_INCREMENT, ris VARCHAR(255), input VARCHAR(1024), output VARCHAR(1024), timeEntered timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP, status varchar(20) NOT NULL default 'entered', PRIMARY KEY(logID)) ENGINE=InnoDB DEFAULT CHARSET=latin1;";
		JDBCUtil.get().update(createString);
	}

	@Override
	protected void store(Object o, PreparedStatement ps, boolean pk, int i) throws Exception
	{
		RisGlueLog a = (RisGlueLog) o;
		if (pk)
			ps.setLong(i++, a.getLogID());
		else
		{
			ps.setString(i++, a.getRis());
			ps.setString(i++, a.getInput());
			ps.setString(i++, a.getOutput());
			ps.setTimestamp(i++, a.getTimeEntered());
			ps.setString(i++, a.getStatus());
		}
	}
}