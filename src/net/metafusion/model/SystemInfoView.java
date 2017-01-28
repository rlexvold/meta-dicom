package net.metafusion.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import acme.db.JDBCUtil;
import acme.db.View;

public class SystemInfoView extends View
{
	static SystemInfoView	view		= null;
	private static String	tableName	= "dcm_system";

	static public synchronized SystemInfoView get()
	{
		if (view == null)
			view = new SystemInfoView();
		return view;
	}

	SystemInfoView()
	{
		super(tableName, new String[] { "systemKey" }, new String[] { "systemValue" });
	}

	protected Object load(ResultSet rs, int offset) throws Exception
	{
		SystemInfo a = new SystemInfo();
		a.setSystemKey(rs.getString(offset++));
		a.setSystemValue(rs.getString(offset++));
		return a;
	}

	public void createTable()
	{
		String create = "create table " + tableName + " ( systemKey varchar(255) NOT NULL, systemValue varchar(255) NOT NULL, PRIMARY KEY(systemKey)) ENGINE=InnoDB DEFAULT CHARSET=latin1;";
		JDBCUtil.get().update(create);
		SystemInfo si = new SystemInfo();
		si.setSystemKey("schemaVersion");
		si.setSystemValue("0");
		SystemInfoView.get().insert(si);
	}

	public String getCurrentVersion()
	{
		SystemInfo si = (SystemInfo) select1("schemaVersion");
		return si.getSystemValue();
	}

	@Override
	protected void store(Object o, PreparedStatement ps, boolean pk, int i) throws Exception
	{
		SystemInfo a = (SystemInfo) o;
		if (pk)
			ps.setString(i++, a.getSystemKey());
		else
		{
			ps.setString(i++, a.getSystemValue());
		}
	}
}