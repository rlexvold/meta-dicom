package net.metafusion.localstore.service;

import java.sql.Timestamp;
import java.util.Date;

import net.metafusion.Dicom;
import net.metafusion.dataset.DS;
import net.metafusion.model.ServiceLog;
import net.metafusion.model.ServiceLogView;
import net.metafusion.net.DicomServerSession;
import net.metafusion.util.AE;
import net.metafusion.util.AEMap;
import net.metafusion.util.Message;
import net.metafusion.util.Tag;
import net.metafusion.util.UID;
import acme.storage.SSStore;
import acme.util.Log;
import acme.util.Util;

public class DicomServiceProvider implements Runnable
{
	protected boolean				isDone;
	protected String				name;
	protected DicomServerSession	sess;
	Message							current		= null;
	boolean							firstMsg	= true;
	protected ServiceLog			serviceLog	= null;

	static void log(String s)
	{
		Log.log(s);
	}

	static void vlog(String s)
	{
		Log.vlog(s);
	}

	public DicomServiceProvider(DicomServerSession sess, String name)
	{
		this.name = name;
		this.sess = sess;
	}

	public Message getCurrent()
	{
		return current;
	}

	public boolean isDone()
	{
		return isDone;
	}

	public void run()
	{
		assert isDone == false;
		log("enter " + name + (firstMsg ? "[start]" : "[cont]") + "...");
		try
		{
			serviceLog = new ServiceLog();
			serviceLog.setStart(new Timestamp(new Date().getTime()));
			AE ae = AEMap.get(sess.getSourceAE());
			if (ae != null)
			{
				String name = ae.getName();
				if (name != null && name.length() > 0)
					serviceLog.setDestAE(name);
				String ip = ae.getHostName() + ":" + ae.getPort();
				if (ip != null && ip.length() > 0)
					serviceLog.setDestIP(ip);
			}
			ae = AEMap.get(sess.getDestAE());
			if (ae != null)
			{
				String name = ae.getName();
				if (name != null && name.length() > 0)
					serviceLog.setSourceAE(name);
				String ip = ae.getHostName() + ":" + ae.getPort();
				if (ip != null && ip.length() > 0)
					serviceLog.setSourceIP(ip);
			}
			serviceLog.setServiceType(this.name);
			DS tags = current.getDataSet();
			if (tags != null)
			{
				String studyuid = tags.getString(Tag.StudyInstanceUID);
				if (studyuid != null && studyuid.length() > 0)
					serviceLog.setStudyuid(studyuid);
			}
			serviceLog.setStatus("started");
			if (serviceLog != null)
			{
				ServiceLogView.get().insert(serviceLog);
				serviceLog = ServiceLogView.get().getLast();
			}
		}
		catch (Exception e)
		{
			Log.log(this.name + " error logging start - " + e.getMessage());
		}
		try
		{
			acme.util.Stats.inc("run." + name);
			isDone = handle(current);
			try
			{
				if (serviceLog != null)
				{
					serviceLog.setEnd(new Timestamp(new Date().getTime()));
					serviceLog.setStatus("finished");
					ServiceLogView.get().update(serviceLog);
				}
			}
			catch (Exception e)
			{
				Log.log(this.name + " error logging finish - " + e.getMessage());
			}
			firstMsg = false;
			current = null;
		}
		catch (Exception e)
		{
			Log.log("DicomServiceProvider: Exception - " + e.getMessage());
			try
			{
				if (serviceLog != null)
				{
					serviceLog.setStatus("error");
					serviceLog.setFailureMessage(e.getMessage());
					serviceLog.setEnd(new Timestamp(new Date().getTime()));
					ServiceLogView.get().update(serviceLog);
				}
			}
			catch (Exception es)
			{
				Log.log("Error logging error in service log - original error: " + e.getMessage() + "   service log error: " + es.getMessage());
			}
			current = null;
			isDone = true;
			log(name + "(done) caught " + e);
			log(Util.stackTraceToString(e));
			sess.close(false);
		}
		vlog("exit " + name + (isDone ? "[done]." : "[cont]..."));
	}

	public void setCurrent(Message current)
	{
		this.current = current;
	}

	protected boolean handle(Message m) throws Exception
	{
		return true;
	}
}
