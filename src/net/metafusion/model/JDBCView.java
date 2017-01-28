package net.metafusion.model;

import java.util.List;
import acme.db.JDBCUtil;

public class JDBCView
{
	static JDBCView view = null;

	static public synchronized JDBCView get()
	{
		if (view == null)
		{
			view = new JDBCView();
		}
		return view;
	}

	public List getWebUsers()
	{
		List l = JDBCUtil.get().selectTupleList("select userid, username, password from web_user order by username");
		return l;
	}

	public List getExtKeys()
	{
		List l = JDBCUtil.get().selectList("select key from dcm_ext");
		return l;
	}

	public boolean extValueExists(String key)
	{
		int count = JDBCUtil.get().selectInt("select count(*) from dcm_ext where key='" + key + "'");
		return count != 0;
	}

	public boolean putExtValue(String key, String value)
	{
		int count = JDBCUtil.get().update("insert into from dcm_ext(key,value) values(?,?)", new Object[] { key, value });
		return count != 0;
	}

	public String getExtValue(String key)
	{
		return JDBCUtil.get().selectString("select value from dcm_ext where key='" + key + "'");
	}

	public String getKeyValue(String key)
	{
		return JDBCUtil.get().selectString("select value from dcm_keyvalue where key='" + key + "'");
	}

	public boolean putKeyValue(String key, String value)
	{
		int count = JDBCUtil.get().update("insert into from dcm_keyvalue(key,value) values(?,?)", new Object[] { key, value });
		return count != 0;
	}

	public boolean deleteKeyValue(String key, String value)
	{
		int count = JDBCUtil.get().update("delete from dcm_keyvalue where key=?", new Object[] { key });
		return count != 0;
	}
}