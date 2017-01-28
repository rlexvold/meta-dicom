package net.metafusion.model;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.List;

import net.metafusion.util.GlobalProperties;
import acme.db.JDBCUtil;
import acme.db.View;

public class ServiceLogView extends View
{
	static ServiceLogView	view		= null;
	private static String	tableName	= "dcm_service_log";
	private static String	primaryKey	= "serviceLogId";

	static public synchronized ServiceLogView get()
	{
		if (view == null)
		{
			view = new ServiceLogView();
		}
		return view;
	}

	ServiceLogView()
	{
		super(tableName, new String[] { primaryKey }, new String[] {
				"serviceType",
				"studyuid",
				"start",
				"end",
				"status",
				"failureMessage",
				"sourceAE",
				"sourceIP",
				"destAE",
				"destIP" });
	}

	public ServiceLog getLast()
	{
		List l = selectWhereOrder("limit 0,1", primaryKey + " DESC");
		if (l.size() == 0)
			return null;
		ServiceLog rl = (ServiceLog) l.get(0);
		return rl;
	}

	//
	protected Object load(ResultSet rs, int offset) throws Exception
	{
		ServiceLog i = new ServiceLog();
		i.setServiceLogId(rs.getLong(offset++));
		i.setServiceType(rs.getString(offset++));
		i.setStudyuid(rs.getString(offset++));
		i.setStart(rs.getTimestamp(offset++));
		i.setEnd(rs.getTimestamp(offset++));
		i.setStatus(rs.getString(offset++));
		i.setFailureMessage(rs.getString(offset++));
		i.setSourceAE(rs.getString(offset++));
		i.setSourceIP(rs.getString(offset++));
		i.setDestAE(rs.getString(offset++));
		i.setDestIP(rs.getString(offset++));
		return i;
	}

	protected void store(Object o, PreparedStatement ps, boolean pk, int i) throws Exception
	{
		ServiceLog a = (ServiceLog) o;
		if (pk)
			ps.setLong(i++, a.getServiceLogId());
		else
		{
			ps.setString(i++, a.getServiceType());
			ps.setString(i++, a.getStudyuid());
			ps.setTimestamp(i++, a.getStart());
			ps.setTimestamp(i++, a.getEnd());
			ps.setString(i++, a.getStatus());
			ps.setString(i++, a.getFailureMessage());
			ps.setString(i++, a.getSourceAE());
			ps.setString(i++, a.getSourceIP());
			ps.setString(i++, a.getDestAE());
			ps.setString(i++, a.getDestIP());
		}
	}

	public void createTable()
	{
		String createString = "create table "
				+ tableName
				+ " ( "
				+ primaryKey
				+ " INTEGER NOT NULL AUTO_INCREMENT, serviceType VARCHAR(20), studyuid VARCHAR(128), start timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP, end timestamp,"
				+ "status VARCHAR(20), failureMessage VARCHAR(1024), sourceAE VARCHAR(50), sourceIP VARCHAR(50), destAE VARCHAR(50), destIP VARCHAR(50), PRIMARY KEY(" + primaryKey
				+ ")) ENGINE=InnoDB DEFAULT CHARSET=latin1;";
		JDBCUtil.get().update(createString);
	}

	public void updateSchema()
	{
		String updateSql = "alter table " + tableName + " add studyuid VARCHAR(128)";
		JDBCUtil.get().update(updateSql);
	}

	public void cleanup()
	{
		try
		{
			Integer days = (Integer) GlobalProperties.get().get("daysToKeepServiceLog");
			if (days == null)
				days = 120;
			Calendar threshold = Calendar.getInstance();
			threshold.add(Calendar.DATE, -days);
			Date thresh = new Date(threshold.getTimeInMillis());
			deleteWhere("end < '" + thresh + "' and status = 'finished'");
		}
		catch (Exception e)
		{
		}
	}
}
