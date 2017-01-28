package net.metafusion.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import acme.db.View;

public class QueueView extends View
{
	static QueueView view = null;

	static public synchronized QueueView get()
	{
		if (view == null)
		{
			view = new QueueView();
		}
		return view;
	}

	QueueView()
	{
		super("dcm_queue", new String[] { "id" }, new String[] { "name", "msg", "argl", "args" });
	}
	//
	private long id;
	private String name;
	private String msg;
	private long argl;
	private String args;

	protected Object load(ResultSet rs, int offset) throws Exception
	{
		QueueElem a = new QueueElem();
		a.setId(rs.getLong(offset++));
		a.setName(rs.getString(offset++));
		a.setMsg(rs.getString(offset++));
		a.setArgl(rs.getLong(offset++));
		a.setArgs(rs.getString(offset++));
		return a;
	}

	protected void store(Object o, PreparedStatement ps, boolean pk, int i) throws Exception
	{
		QueueElem a = (QueueElem) o;
		if (pk)
			ps.setLong(i++, a.getId());
		else
		{
			ps.setString(i++, a.getName());
			ps.setString(i++, a.getMsg());
			ps.setLong(i++, a.getArgl());
			ps.setString(i++, a.getArgs());
		}
	}

	public void delete(QueueElem q)
	{
		delete((Object) q);
	}

	public QueueElem peek(String name)
	{
		QueueElem q = (QueueElem) select1Where("name='" + name + "' order by id");
		return q;
	}
	static long lastId = 0;

	public synchronized void enqueue(String name, String msg, long l, String s)
	{
		QueueElem e = new QueueElem();
		e.setName(name);
		e.setMsg(msg);
		e.setArgl(l);
		e.setArgs(s);
		long t = System.currentTimeMillis();
		if (t <= lastId) t = lastId + 1;
		e.setId(t);
		lastId = t;
		insert(e);
	}

	public QueueElem dequeue(String name)
	{
		QueueElem e = peek(name);
		if (e != null) delete(e);
		return e;
	}
}
