package acme.db;

import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import acme.util.Log;
import acme.util.NestedException;
import acme.util.Util;

public class JDBCUtil
{
	// static Pool pool = new Pool(){
	// public Object allocate() {
	// try {
	// Object o = DriverManager.getConnection(
	// DBManager.get().getURL());
	// return o;
	// } catch (SQLException e) {
	// throw new RuntimeException(""+e);
	// }
	// }
	// public synchronized void release(Object o) {
	// try {
	// ((Connection)o).close();
	// } catch (SQLException e) {
	// throw new RuntimeException(""+e);
	// }
	// }
	// };
	static JDBCUtil instance = new JDBCUtil();

	public static JDBCUtil get()
	{
		return instance;
	}

	public synchronized void release(Connection c)
	{
		try
		{
			if (c != null) c.close();
		}
		catch (SQLException e)
		{
			throw new RuntimeException("" + e);
		}
	}

	public synchronized PreparedStatement getPS(DBManager db, String sql) throws Exception
	{
		Connection con = DriverManager.getConnection(db.getURL());
		// acme.util.Log.db("prepare: "+sql);
		return con.prepareStatement(sql);
	}

	public synchronized boolean test(DBManager db)
	{
		boolean test = false;
		Connection con = null;
		try
		{
			con = DriverManager.getConnection(db.getURL());
			test = con != null;
		}
		catch (Exception e)
		{
			Log.log("JDBC test failed " + DBManager.get().getURL());
			throw new NestedException(e);
		}
		finally
		{
			release(con);
		}
		return test;
	}

	public synchronized void release(PreparedStatement ps)
	{
		if (ps == null) return;
		try
		{
			release(ps.getConnection());
			try
			{
				ps.close();
			}
			catch (Exception e)
			{
				;
			}
		}
		catch (SQLException e)
		{
			throw new RuntimeException("" + e);
		}
	}

	public synchronized int update(DBManager db, String sql)
	{
		PreparedStatement ps = null;
		try
		{
			ps = JDBCUtil.get().getPS(db, sql);
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

	public synchronized int update(String sql)
	{
		return update(DBManager.get(), sql);
	}

	public synchronized int update(String sql, Object[] args)
	{
		PreparedStatement ps = null;
		try
		{
			ps = JDBCUtil.get().getPS(DBManager.get(), sql);
			for (int i = 0; i < args.length; i++)
				ps.setObject(i + 1, args[i]);
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

	public synchronized Object selectObject(DBManager db, String sql)
	{
		PreparedStatement ps = null;
		try
		{
			ps = JDBCUtil.get().getPS(db, sql);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				return rs.getObject(1);
			} else return null;
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

	public synchronized Object selectObject(String sql)
	{
		return selectObject(DBManager.get(), sql);
	}

	public synchronized Object selectTuple(DBManager db, String sql)
	{
		PreparedStatement ps = null;
		try
		{
			ps = JDBCUtil.get().getPS(db, sql);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				int count = rs.getMetaData().getColumnCount();
				Object t[] = new Object[count];
				for (int i = 0; i < count; i++)
					t[i] = rs.getObject(i + 1);
				return t;
			} else return null;
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

	public synchronized Object selectTuple(String sql)
	{
		return selectTuple(DBManager.get(), sql);
	}

	public synchronized List selectTupleList(DBManager db, String sql)
	{
		List l = new LinkedList();
		PreparedStatement ps = null;
		try
		{
			ps = JDBCUtil.get().getPS(db, sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				int count = rs.getMetaData().getColumnCount();
				Object t[] = new Object[count];
				for (int i = 0; i < count; i++)
					t[i] = rs.getObject(i + 1);
				l.add(t);
			}
		}
		catch (Exception e)
		{
			throw new RuntimeException("" + e);
		}
		finally
		{
			JDBCUtil.get().release(ps);
		}
		return l;
	}

	public synchronized List selectTupleList(String sql)
	{
		return selectTupleList(DBManager.get(), sql);
	}

	public synchronized List selectList(DBManager db, String sql)
	{
		List l = new LinkedList();
		PreparedStatement ps = null;
		try
		{
			ps = JDBCUtil.get().getPS(db, sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next())
			{
				l.add(rs.getObject(1));
			}
			;
		}
		catch (Exception e)
		{
			throw new RuntimeException("" + e);
		}
		finally
		{
			JDBCUtil.get().release(ps);
		}
		return l;
	}

	public synchronized List selectList(String sql)
	{
		return selectList(DBManager.get(), sql);
	}

	public synchronized String selectString(DBManager db, String sql)
	{
		PreparedStatement ps = null;
		try
		{
			ps = JDBCUtil.get().getPS(db, sql);
			ResultSet rs = ps.executeQuery();
			return rs.next() ? rs.getString(1) : null;
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

	public synchronized String selectString(String sql)
	{
		return selectString(DBManager.get(), sql);
	}

	public synchronized int selectInt(String sql)
	{
		String s = selectString(sql);
		return s != null ? Integer.parseInt(s) : 0;
	}

	public synchronized int selectInt(DBManager db, String sql)
	{
		return selectInt(db, sql);
	}

	public synchronized boolean selectBlob(DBManager db, String sql, OutputStream os)
	{
		PreparedStatement ps = null;
		try
		{
			ps = JDBCUtil.get().getPS(db, sql);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				Blob b = rs.getBlob(1);
				Util.copyStream(b.getBinaryStream(), os);
				return true;
			} else return false;
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

	public synchronized boolean selectBlob(String sql, OutputStream os)
	{
		return selectBlob(DBManager.get(), sql, os);
	}

	public synchronized byte[] selectBlob(DBManager db, String sql)
	{
		PreparedStatement ps = null;
		try
		{
			ps = JDBCUtil.get().getPS(db, sql);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
			{
				Blob b = rs.getBlob(1);
				return b.getBytes(1l, (int) b.length());
			} else return null;
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

	public synchronized byte[] selectBlob(String sql)
	{
		return selectBlob(DBManager.get(), sql);
	}

	public synchronized int select(String sql, RowProcessor rp)
	{
		return select(DBManager.get(), sql, rp);
	}

	public synchronized int select(DBManager db, String sql, RowProcessor rp)
	{
		PreparedStatement ps = null;
		try
		{
			ps = JDBCUtil.get().getPS(db, sql);
			ResultSet rs = ps.executeQuery();
			int count = 0;
			try
			{
				while (rs.next())
				{
					count++;
					rp.processRow(rs);
				}
			}
			catch (SQLException e)
			{
				Log.log("select caught ", e);
			}
			return count;
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
}
