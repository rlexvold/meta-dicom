package acme.db;

import java.io.ByteArrayInputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import acme.util.Log;
import acme.util.XML;

public class View
{
	protected static void log(String s)
	{
		if (!disableOutput) Log.vlog(s);
	}
	static protected boolean disableOutput = false;
	String pks[];
	String values[];
	String fields[];
	int numField;
	int numKey;
	int numValue;
	String TABLE = "";
	String FL = "";
	String IL = "";
	String UL = "";
	String PK = "";
	String PK_TAG = "";
	String createSQL = "";
	String updateSQL = "";
	String deleteSQL = "";
	String deleteAllSQL = "";
	String pkSQL = "";
	String allSQL = "";
	String countSQL = "";

	protected View()
	{
	}

	protected View(String name, String pks[], String values[])
	{
		this.pks = pks;
		this.values = values;
		TABLE = name;
		numField = pks.length + values.length;
		fields = new String[numField];
		System.arraycopy(pks, 0, fields, 0, pks.length);
		System.arraycopy(values, 0, fields, pks.length, values.length);
		numKey = pks.length;
		numValue = values.length;
		for (int i = 0; i < pks.length; i++)
		{
			String comma = i < pks.length - 1 ? "," : "";
			FL += pks[i] + comma;
			IL += "?" + comma;
			PK += pks[i] + "=?" + comma;
		}
		if (numKey != 0 && numValue != 0)
		{
			FL += ",";
			IL += ",";
		}
		for (int i = 0; i < values.length; i++)
		{
			String comma = i < values.length - 1 ? "," : "";
			FL += values[i] + comma;
			IL += "?" + comma;
			UL += values[i] + "=?" + comma;
			;
		}
		createSQL = "insert into " + TABLE + "(" + FL + ") values (" + IL + ")";
		updateSQL = "update " + TABLE + " set " + UL + " where " + PK;
		deleteSQL = "delete from " + TABLE + " where " + PK;
		deleteAllSQL = "delete from " + TABLE;
		pkSQL = "select " + FL + " from " + TABLE + "  where " + PK;
		allSQL = "select " + FL + " from " + TABLE + " ";
		countSQL = "select count(*) from " + TABLE;
	}

	protected String buildSelect(String where)
	{
		return allSQL + " where " + where;
	}

	protected XML selectBlob(String name, String where)
	{
		String sql = "select " + name + " from " + TABLE + " where " + where;
		byte[] blob = JDBCUtil.get().selectBlob(sql);
		if (blob == null) return null;
		XML xml = null;
		try
		{
			xml = new XML(new ByteArrayInputStream(blob));
		}
		catch (Exception e)
		{
			Log.log("selectBlob " + sql + " caught ", e);
		}
		return xml;
	}

	protected int executeUpdate(PreparedStatement ps) throws Exception
	{
		return ps.executeUpdate();
	}

	protected int doUpdate(String sql, Object o, int k, int f)
	{
		PreparedStatement ps = null;
		try
		{
			log("update: " + o);
			ps = JDBCUtil.get().getPS(DBManager.get(), sql);
			if (f != -1) store(o, ps, false, f);
			if (k != -1) store(o, ps, true, k);
			return ps.executeUpdate();
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

	protected int doInsert(String sql, Object o, int k, int f)
	{
		PreparedStatement ps = null;
		try
		{
			log("insert: " + o);
			ps = JDBCUtil.get().getPS(DBManager.get(), sql);
			if (k != -1) store(o, ps, true, k);
			if (f != -1) store(o, ps, false, f);
			return ps.executeUpdate();
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

	protected Object doSelect1(String sql, Object[] args)
	{
		PreparedStatement ps = null;
		try
		{
			ps = JDBCUtil.get().getPS(DBManager.get(), sql);
			if (args != null) for (int i = 0; i < args.length; i++)
				ps.setObject(i + 1, args[i]);
			ResultSet rs = ps.executeQuery();
			Object o = rs.next() ? load(rs, 1) : null;
			log("load1: " + o);
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

	protected List doSelectN(String sql, Object[] args)
	{
		PreparedStatement ps = null;
		try
		{
			List l = new ArrayList();
			ps = JDBCUtil.get().getPS(DBManager.get(), sql);
			if (args != null) for (int i = 0; i < args.length; i++)
				ps.setObject(i + 1, args[i]);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				Object o = load(rs, 1);
				log("loadN: " + o);
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

	protected String doSelectString(String sql, Object[] args)
	{
		PreparedStatement ps = null;
		try
		{
			ps = JDBCUtil.get().getPS(DBManager.get(), sql);
			if (args != null) for (int i = 0; i < args.length; i++)
				ps.setObject(i + 1, args[i]);
			ResultSet rs = ps.executeQuery();
			String s = rs.next() ? rs.getString(1) : null;
			log("select: " + s);
			return s;
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

	protected int doSelectInt(String sql, Object[] args)
	{
		String s = doSelectString(sql, args);
		log("select: " + s);
		return s != null ? Integer.parseInt(s) : 0;
	}

	protected long doSelectLong(String sql, Object[] args)
	{
		String s = doSelectString(sql, args);
		log("select: " + s);
		return s != null ? Long.parseLong(s) : 0;
	}

	public boolean insert(Object o)
	{
		return doInsert(createSQL, o, 1, 1 + numKey) == 1;
	}

	public boolean update(Object o)
	{
		return doUpdate(updateSQL, o, numField, 1) == 1;
	}

	public boolean delete(Object a)
	{
		return doUpdate(deleteSQL, a, 1, -1) == 1;
	}

	public boolean delete(long key)
	{
		Object o = select1(key);
		if (o == null) log("delete: not found id=" + key + " sql=" + pkSQL);
		return delete(o);
	}

	public boolean deleteAll()
	{
		return doUpdate(deleteAllSQL, null, -1, -1) == 1;
	}

	public boolean deleteWhere(String where)
	{
		String sql = deleteAllSQL;
		if (where != null && where.length() != 0) sql = deleteAllSQL + " where " + where;
		return doUpdate(sql, null, -1, -1) == 1;
	}

	protected Object select1(long key)
	{
		return doSelect1(pkSQL, new Object[] { new Long(key) });
	}

	protected Object select1(String key)
	{
		return doSelect1(pkSQL, new Object[] { key });
	}

	public Object select1()
	{
		return doSelect1(allSQL, null);
	}

	public List selectAll()
	{
		return doSelectN(allSQL, null);
	}

	public List selectWhere(String where)
	{
		String sql = allSQL;
		if (where != null && where.length() > 0)
		{
			if (where.startsWith("limit"))
				sql += " " + where;
			else sql += " where " + where;
		}
		return doSelectN(sql, null);
	}

	public List selectWhereOrder(String where, String order)
	{
		String sql = allSQL;
		if (where != null && !where.startsWith("limit")) sql += " where " + where;
		if (order != null) sql += " order by " + order;
		if (where != null && where.startsWith("limit")) sql += " " + where;
		return doSelectN(sql, null);
	}

	protected Object select1Where(String where)
	{
		String sql = allSQL;
		if (where != null && where.length() > 0)
		{
			if (where.startsWith("limit"))
				sql += " " + where;
			else sql += " where " + where;
		}
		return doSelect1(sql, null);
	}

	public int count()
	{
		return JDBCUtil.get().selectInt(countSQL);
	}

	// override
	protected Object load(ResultSet rs, int offset) throws Exception
	{
		return null;
	}

	protected void store(Object o, PreparedStatement ps, boolean pk, int i) throws Exception
	{
	}
}
