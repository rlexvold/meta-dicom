package net.metafusion.model;

import java.util.List;
import acme.db.JDBCUtil;

public class WebView
{
	static WebView view = null;

	static public synchronized WebView get()
	{
		if (view == null)
		{
			view = new WebView();
		}
		return view;
	}

	public String getWebUsername(String id)
	{
		try
		{
			return JDBCUtil.get().selectString("select username from web_user where userid=" + id);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public String getWebUserId(String userName)
	{
		try
		{
			return JDBCUtil.get().selectString("select userid from web_user where username='" + userName + "'");
		}
		catch (Exception e)
		{
			return null;
		}
	}

	public String referrerAdd(String name, String passwd)
	{
		JDBCUtil.get().update("insert into web_user(username,password,canAssign,canRead) values (?, ?,?,?)", new Object[] { name, passwd, "N", "Y" });
		String id = JDBCUtil.get().selectString("select userid from web_user where username='" + name + "'");
		return id;
	}

	public void referrerModify(String id, String name, String passwd)
	{
		JDBCUtil.get().update("update web_user set username=?, password=? where userid=?", new Object[] { name, passwd, id });
	}

	public void referrerDelete(String id)
	{
		JDBCUtil.get().update("delete from web_user where userid=?", new Object[] { id });
		JDBCUtil.get().update("delete from web_assign where userid=?", new Object[] { id });
	}

	public void publishStudy(String userId, String uid)
	{
		String studyId = JDBCUtil.get().selectString("select studyID from dcm_study where studyUID='" + uid + "'");
		JDBCUtil.get().update("insert into web_assign(userid, dcm_studyid) values (?,?)", new Object[] { userId, studyId });
		List l = JDBCUtil.get().selectList("select u.username from web_user u, web_assign a where a.userid=u.userid and a.dcm_studyid=" + studyId);
		String assignedTo = ":";
		for (int i = 0; i < l.size(); i++)
			assignedTo += l.get(i) + ":";
		JDBCUtil.get().update("update web_study set assignedto=? where dcm_studyid=?", new Object[] { assignedTo, studyId });
	}

	public void unpublishStudy(String userId, String uid)
	{
		String studyId = JDBCUtil.get().selectString("select studyID from dcm_study where studyUID='" + uid + "'");
		JDBCUtil.get().update("delete from web_assign where userid=? and dcm_studyid=?", new Object[] { userId, studyId });
		String assignedTo = ":";
		List l = JDBCUtil.get().selectList("select u.username from web_user u, web_assign a where a.userid=u.userid and a.dcm_studyid=" + studyId);
		for (int i = 0; i < l.size(); i++)
			assignedTo += l.get(i) + ":";
		JDBCUtil.get().update("update web_study set assignedto=? where dcm_studyid=?", new Object[] { assignedTo, studyId });
	}

	public void setTranscriptPath(String studyId, String path)
	{
		JDBCUtil.get().update("update web_study set transcriptPath=? where dcm_studyid=?", new Object[] { path, studyId });
	}
}
