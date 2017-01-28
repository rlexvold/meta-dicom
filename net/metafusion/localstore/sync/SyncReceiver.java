package net.metafusion.localstore.sync;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import net.metafusion.localstore.DicomStore;
import net.metafusion.model.Study;
import net.metafusion.model.StudyView;
import acme.db.JDBCUtil;
import acme.storage.SSStore;
import acme.util.Util;

public class SyncReceiver implements Runnable
{
	static void log(String s)
	{
		Util.log(s);
	}

	static void log(String s, Exception e)
	{
		Util.log(s, e);
	}
	Socket s;
	ObjectOutputStream oos;
	ObjectInputStream ois;

	public SyncReceiver(Socket s) throws Exception
	{
		this.s = s;
		this.ois = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
		String str = (String) ois.readObject();
		if (!str.startsWith("CONNECT")) throw new RuntimeException("receive bad connect string");
		this.oos = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
		oos.writeObject("OK");
		oos.flush();
	}

	public void close()
	{
		Util.safeClose(ois);
		Util.safeClose(oos);
		Util.safeClose(s);
	}

	public void run()
	{
		for (;;)
		{
			File f = null;
			FileOutputStream fos = null;
			try
			{
				SyncMsg sm;
				sm = (SyncMsg) ois.readObject();
				if (sm.length != 0)
				{
					f = SSStore.get().createTempFile(".syncd");
					fos = new FileOutputStream(f);
					Util.copyStream(ois, fos, sm.length);
					fos.close();
					sm.file = f;
				}
				dispatch(sm);
				// Util.sleep(10000);
			}
			catch (Exception e)
			{
				log("SyncReceiver caught: will close", e);
				close();
			}
			finally
			{
				Util.safeClose(fos);
				Util.safeDelete(f);
			}
		}
	}

	protected void dispatch(SyncMsg sm) throws Exception
	{
		String msg = "FAIL";
		try
		{
			log("syncDispatch: " + sm);
			if (sm.cmd.equals("syncImage"))
				imageSync(sm.file);
			else if (sm.cmd.equals("syncStudyDelete"))
				syncStudyDelete((String) sm.args.get("studyuid"));
			else if (sm.cmd.startsWith("mf_"))
				dispatchMF(sm);
			else throw new RuntimeException("unknown command");
			msg = "OK";
		}
		catch (Exception e)
		{
			log("dispatch caught ", e);
			msg = "FAIL " + e.getMessage();
		}
		finally
		{
			oos.writeObject(msg);
			oos.flush();
		}
	}

	void imageSync(File f)
	{
		try
		{
			DicomStore.get().put(f);
		}
		catch (Exception e)
		{
			log("imageSync", e);
		}
	}

	void syncStudyDelete(String studyuid) throws Exception
	{
		Study study = StudyView.get().selectByUID(studyuid);
		if (study != null) DicomStore.get().deleteStudy(study); // watch sync
																// with cache
																// (if any)
	}
	HashMap updateMap = new HashMap();
	class Update
	{
		String sql;
		Object args[];

		Update(String name, String sql, Object args[])
		{
			updateMap.put(name, this);
			this.sql = sql;
			this.args = args;
		}

		Update(String name, String sql, String a1)
		{
			this(name, sql, new Object[] { a1 });
		}

		Update(String name, String sql, String a1, String a2)
		{
			this(name, sql, new Object[] { a1, a2 });
		}

		Update(String name, String sql, String a1, String a2, String a3)
		{
			this(name, sql, new Object[] { a1, a2, a3 });
		}

		int update(HashMap argMap)
		{
			Object o[] = new Object[args.length];
			for (int i = 0; i < args.length; i++)
			{
				String name = (String) args[i];
				String value = (String) argMap.get(name);
				if (name.startsWith("can")) value = value != null ? "Y" : "N";
				o[i] = value;
			}
			return JDBCUtil.get().update(sql, o);
		}
	}
	Update mf_clear_assignments = new Update("mf_clear_assignments", "delete from web_assign where dcm_studyuid = ? ", "studyuid");
	Update mf_set_assignedto = new Update("mf_set_assignedto", "update web_study set assignedto=? where dcm_studyuid = ?", "assignedto", "studyuid");
	// Update mf_set_studypath = new Update("mf_set_studypath",
	// "update web_study set studypath= ? where dcm_studyuid = ? ",
	// "studypath",
	// "studyuid"
	// );
	// Update mf_set_transcriptpath = new Update("mf_set_transcriptpath",
	// "update web_study set transcriptpath= ? where dcm_studyuid = ? ",
	// "transcriptpath",
	// "studyuid"
	// );
	Update mf_add_assignment = new Update("mf_add_assignment", "insert into web_assign(userid, dcm_studyid) values (?,?) ", "username", "studyuid");
	Update mf_insert_user = new Update("mf_insert_user", "INSERT INTO `web_user`(`userid`, `username`, `password`, `canAssign`, `canRead`, `firstname`, `lastname`, "
			+ "`title`, `license`,  `street`, `city`, `state`, `zip`, `phone`, `email`) values " + "(NULL, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?) ", new String[] { "username", "password",
			"canAssign", "canRead", "firstname", "lastname", "title", "license", "street", "city", "state", "zip", "phone", "email" });
	Update mf_update_user = new Update("mf_update_user", "update web_user" + " set password=?, canAssign=?, canRead=?, firstname=?, lastname=?, title=?, license=?,  "
			+ " street=?, city=?, state=?, zip=?, phone=?, email=? " + " where username=? ", new String[] { "password", "canAssign", "canRead", "firstname", "lastname", "title",
			"license", "street", "city", "state", "zip", "phone", "email", "username" });
	Update mf_delete_user = new Update("mf_delete_user", "delete from web_user where username = ? ", "username");

	void dispatchMF(SyncMsg sm)
	{
		Update up = (Update) updateMap.get(sm.cmd);
		if (up == null) throw new RuntimeException("unknown command " + sm.cmd);
		// map out user
		up.update(sm.args);
	}
}