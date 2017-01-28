package acme.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import acme.util.Log;

public class JoinedView
{
	protected static void log(String s)
	{
		Log.log(s);
	}

	protected static void vlog(String s)
	{
		Log.vlog(s);
	}
	View views[];
	String selectList = "";
	String tableList = "";
	String countSQL;
	String selectSQL;
	String joinList;
	Class klass;

	public JoinedView(Class klass, View views[], String joinList)
	{
		this.klass = klass;
		this.joinList = joinList;
		this.views = views;
		ArrayList al = new ArrayList();
		for (int i = 0; i < views.length; i++)
		{
			View v = views[i];
			tableList += v.TABLE; // +" "+v.TAG;
			if (i < views.length - 1) tableList += ",";
			for (int j = 0; j < v.numField; j++)
			{
				selectList += v.TABLE + "." + v.fields[j];
				if (j < v.numField - 1 || i < views.length - 1) selectList += ",";
			}
		}
		countSQL = "select count(*) from " + tableList + " where " + joinList;
		selectSQL = "select " + selectList + " from " + tableList + " where " + joinList;
	}

	Object doSelect1(String sql)
	{
		PreparedStatement ps = null;
		try
		{
			ps = JDBCUtil.get().getPS(DBManager.get(), sql);
			ResultSet rs = ps.executeQuery();
			Object o = rs.next() ? load(rs) : null;
			vlog("jload1: " + o);
			return o;
		}
		catch (Exception e)
		{
			throw new RuntimeException("" + e);
		}
		finally
		{
			JDBCUtil.get().release(ps);
		}
	}

	List doSelectN(String sql)
	{
		PreparedStatement ps = null;
		try
		{
			List l = new ArrayList();
			ps = JDBCUtil.get().getPS(DBManager.get(), sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				Object o = load(rs);
				vlog("jloadN: " + o);
				l.add(o);
			}
			return l;
		}
		catch (Exception e)
		{
			throw new RuntimeException("" + e);
		}
		finally
		{
			JDBCUtil.get().release(ps);
		}
	}

	protected Object select1(long key)
	{
		return null;
	}

	protected Object select1(String where)
	{
		String sql = selectSQL;
		if (where != null && where.length() > 0) if (where.startsWith("limit"))
			sql += " " + where;
		else sql += " and " + where;
		return doSelect1(sql);
	}

	public List selectWhere(String where)
	{
		vlog("v.selectWhere:" + where + " ======================================================================");
		String sql = selectSQL;
		if (where != null && where.length() > 0) if (where.startsWith("limit"))
			sql += " " + where;
		else sql += " and " + where;
		return doSelectN(sql);
	}

	public List selectAll()
	{
		String sql = selectSQL;
		return doSelectN(sql);
	}

	public int count()
	{
		return JDBCUtil.get().selectInt(countSQL);
	}

	protected Object load(ResultSet rs) throws Exception
	{
		Object o = klass.newInstance();
		int pos = 1;
		for (int i = 0; i < views.length; i++)
		{
			View v = views[i];
			setObjectPart(o, i, v.load(rs, pos));
			pos += v.numField;
		}
		return o;
	}

	// override
	protected void setObjectPart(Object object, int index, Object part)
	{
		assert false;
	}
}
