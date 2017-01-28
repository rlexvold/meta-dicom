package net.metafusion.model;

import java.util.List;
import acme.db.JDBCUtil;
import acme.util.Log;

public class ReviewView
{
	static ReviewView view = null;

	static public synchronized ReviewView get()
	{
		if (view == null)
		{
			view = new ReviewView();
		}
		return view;
	}

	public void assign(long studyid, String name)
	{
		String sql = "insert into ris_review(studyid,name) values (" + studyid + ",'" + name + "')";
		try
		{
			JDBCUtil.get().update(sql);
		}
		catch (Exception e)
		{
			Log.log("assign caught ", e);
			// eat exception
		}
	}

	public void unassign(long studyid, String name)
	{
		String sql = "delete from ris_review where studyid=" + studyid + " and name='" + name + "'";
		JDBCUtil.get().update(sql);
	}

	public long[] getAssigned(String name)
	{
		String sql = "select studyid from ris_review where name='" + name + "'";
		List l = JDBCUtil.get().selectList(sql);
		long[] ll = new long[l.size()];
		for (int i = 0; i < l.size(); i++)
		{
			ll[i] = ((Long) l.get(i)).longValue();
		}
		return ll;
	}
}