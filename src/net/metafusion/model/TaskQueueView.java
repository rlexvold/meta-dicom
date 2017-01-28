package net.metafusion.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import acme.db.View;
import acme.util.Log;

public class TaskQueueView extends View
{
	static TaskQueueView view = null;

	static public synchronized TaskQueueView get()
	{
		if (view == null) view = new TaskQueueView();
		return view;
	}

	TaskQueueView()
	{
		super("dcm_queue", new String[] { "id" }, new String[] { "time", "action", "arg1", "arg2", "arg3", "status", "attempts", "maxAttempts", "intervalMS" });
		disableOutput = true;
	}

	//
	@Override
	protected Object load(ResultSet rs, int offset) throws Exception
	{
		TaskQueueElem a = new TaskQueueElem();
		a.setId(rs.getLong(offset++));
		a.setTime(rs.getLong(offset++));
		a.setAction(rs.getString(offset++));
		a.setArg1(rs.getString(offset++));
		a.setArg2(rs.getString(offset++));
		a.setArg3(rs.getString(offset++));
		a.setStatus(rs.getString(offset++));
		a.setAttempts(rs.getInt(offset++));
		a.setMaxAttempts(rs.getInt(offset++));
		a.setIntervalMS(rs.getInt(offset++));
		return a;
	}

	@Override
	protected void store(Object o, PreparedStatement ps, boolean pk, int i) throws Exception
	{
		TaskQueueElem a = (TaskQueueElem) o;
		if (pk)
			ps.setLong(i++, a.getId());
		else
		{
			ps.setLong(i++, a.getTime());
			ps.setString(i++, a.getAction());
			ps.setString(i++, a.getArg1());
			ps.setString(i++, a.getArg2());
			ps.setString(i++, a.getArg3());
			ps.setString(i++, a.getStatus());
			ps.setInt(i++, a.getAttempts());
			ps.setInt(i++, a.getMaxAttempts());
			ps.setInt(i++, a.getIntervalMS());
		}
	}

	public void reschedule(TaskQueueElem q)
	{
		if (q.getMaxAttempts() != 0) q.setAttempts(q.getAttempts() + 1);
		q.setTime(System.currentTimeMillis() + q.getIntervalMS());
		update(q);
	}

	public void reschedule(TaskQueueElem q, long msDelay)
	{
		if (q.getMaxAttempts() != 0) q.setAttempts(q.getAttempts() + 1);
		q.setTime(System.currentTimeMillis() + msDelay);
		update(q);
	}

	public void delete(TaskQueueElem q)
	{
		if (q.getMaxAttempts() == 0) log("WARNING: deleted TaskQueueElem " + q);
		delete((Object) q);
	}

	public void complete(TaskQueueElem q, boolean success)
	{
		q.setTime(System.currentTimeMillis());
		q.setStatus(success ? "T" : "F");
		update(q);
	}

	public TaskQueueElem findAction(String action)
	{
		TaskQueueElem q = (TaskQueueElem) select1Where(" action='" + action + "'");
		return q;
	}

	public TaskQueueElem peek()
	{
		TaskQueueElem q = (TaskQueueElem) select1Where("time <=" + System.currentTimeMillis() + " and status = 'P'  order by action, time");
		return q;
		// if (q.getAction().equalsIgnoreCase("sync"))
		// return q;
		// // verify no syncs waiting (gives priority to syncs)
		// TaskQueueElem qsync = (TaskQueueElem)select1Where("time
		// <="+System.currentTimeMillis()+" and status = 'P' and action='sync'
		// order by action, time");
		// if (qsync != null)
		// return qsync;
		// else return q;
		// if (q.getMaxAttempts() != 0) {
		// q.setAttempts(q.getAttempts()+1);
		// update(q);
		// }
		// return q;
	}

	public TaskQueueElem find(long id)
	{
		TaskQueueElem q = (TaskQueueElem) select1(id);
		return q;
	}

	public void trim()
	{
		log("QueueView.trim");
		deleteWhere(" status != 'P' and time < " + (System.currentTimeMillis() - 1000 * 60 * 60));
	}

	private long verifyId(long w)
	{
		for (;;)
		{
			TaskQueueElem q = find(w);
			if (q == null) break;
			w = w + (int) (Math.random() * 1000) + 1;
		}
		return w;
	}

	public synchronized void add(String action, String arg1, String arg2, String arg3, int maxAttempts, int intervalMS)
	{
		Log.log("add " + action + " " + arg1 + " " + arg2);
		add(System.currentTimeMillis(), action, arg1, arg2, arg3, maxAttempts, intervalMS);
	}

	public synchronized void addIfMissing(String action, String arg1, String arg2, String arg3, int maxAttempts, int intervalMS)
	{
		Log.log("addIfMissing " + action + " " + arg1);
		if (findAction(action) == null) add(System.currentTimeMillis(), action, arg1, arg2, arg3, maxAttempts, intervalMS);
	}
	public static long lastId = 0;

	public synchronized void add(long when, String action, String arg1, String arg2, String arg3, int maxAttempts, int intervalMS)
	{
		Log.log("add " + action + " " + arg1 + " " + arg2);
		TaskQueueElem e = new TaskQueueElem(when, action, arg1, arg2, arg3, maxAttempts, intervalMS);
		long id = System.currentTimeMillis();
		if (id == lastId) id = lastId + 1;
		id = verifyId(id);
		lastId = id;
		e.setId(id);
		insert(e);
	}
}