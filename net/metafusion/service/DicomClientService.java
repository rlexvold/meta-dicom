package net.metafusion.service;

import java.sql.Timestamp;
import java.util.Date;

import net.metafusion.dataset.DS;
import net.metafusion.model.ServiceLog;
import net.metafusion.model.ServiceLogView;
import net.metafusion.net.DicomSession;
import net.metafusion.util.AE;
import net.metafusion.util.AEMap;
import net.metafusion.util.Tag;
import acme.util.Log;
import acme.util.Util;

public abstract class DicomClientService implements Runnable
{
	protected DicomSession	sess;
	protected boolean		isDone		= false;
	protected String		name;
	protected int			result		= -1;
	protected String		sourceAE	= null;
	protected String		destAE		= null;

	public String getDestAE()
	{
		return destAE;
	}

	public void setDestAE(String destAE)
	{
		this.destAE = destAE;
	}

	public ServiceLog getServiceLog()
	{
		return serviceLog;
	}

	public void setServiceLog(ServiceLog serviceLog)
	{
		this.serviceLog = serviceLog;
	}
	protected ServiceLog	serviceLog	= null;
	protected DS			attrSet		= null;

	public String getSourceAE()
	{
		return sourceAE;
	}

	public void setSourceAE(String sourceAE)
	{
		this.sourceAE = sourceAE;
	}

	static void log(String s)
	{
		acme.util.Log.log(s);
	}

	public DicomClientService(String name, DicomSession s)
	{
		this.sess = s;
		if (sess != null)
			sourceAE = sess.getSourceAE();
		this.name = name;
	}

	public int getResult()
	{
		return result;
	}

	public boolean isDone()
	{
		return isDone;
	}

	private AE serviceLogStart()
	{
		AE sAE = null;
		try
		{
			if (sourceAE == null && sess != null)
				sourceAE = sess.getSourceAE();
			acme.util.Stats.inc("run." + name);
			serviceLog = new ServiceLog();
			serviceLog.setStart(new Timestamp(new Date().getTime()));
			if (sourceAE != null)
			{
				sAE = AEMap.get(sourceAE);
				if (sAE != null)
				{
					serviceLog.setSourceAE(sourceAE);
					serviceLog.setSourceIP(sAE.getHostName() + ":" + sAE.getPort());
				}
			}
			if (destAE != null)
			{
				AE dAE = AEMap.get(destAE);
				if (dAE != null)
				{
					serviceLog.setDestAE(destAE);
					serviceLog.setDestIP(dAE.getHostName() + ":" + dAE.getPort());
				}
			}
			String studyuid = "unknown";
			if (attrSet != null)
				studyuid = attrSet.getString(Tag.StudyInstanceUID);
			serviceLog.setStudyuid(studyuid);
			serviceLog.setServiceType(this.name);
			serviceLog.setStatus("started");
			ServiceLogView.get().insert(serviceLog);
			serviceLog = ServiceLogView.get().getLast();
		}
		catch (Exception e)
		{
			Log.log(this.name + " error logging - " + e.getMessage());
		}
		return sAE;
	}

	private void serviceLogFinished(AE sAE)
	{
		try
		{
			serviceLog.setEnd(new Timestamp(new Date().getTime()));
			if (serviceLog.getSourceAE() == null || serviceLog.getSourceAE().equalsIgnoreCase("unknown"))
			{
				if (sAE != null)
				{
					serviceLog.setSourceAE(sourceAE);
					serviceLog.setSourceIP(sAE.getHostName() + ":" + sAE.getPort());
				}
			}
			serviceLog.setStatus("finished");
			ServiceLogView.get().update(serviceLog);
		}
		catch (Exception e)
		{
			Log.log(this.name + " error logging - " + e.getMessage());
		}
	}

	private void serviceLogError(Exception e)
	{
		serviceLog.setStatus("error");
		serviceLog.setFailureMessage(e.getMessage());
		serviceLog.setEnd(new Timestamp(new Date().getTime()));
		ServiceLogView.get().update(serviceLog);
	}

	public void run()
	{
		isDone = false;
		AE sAE = serviceLogStart();
		try
		{
			result = runit();
			serviceLogFinished(sAE);
			isDone = true;
		}
		catch (Exception e)
		{
			result = -1;
			isDone = true;
			log(name + " caught " + e);
			log(Util.stackTraceToString(e));
			serviceLogError(e);
		}
	}

	protected int runit() throws Exception
	{
		return -1;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public DicomSession getSess()
	{
		return sess;
	}

	public void setSess(DicomSession sess)
	{
		this.sess = sess;
	}

	public DS getAttrSet()
	{
		return attrSet;
	}

	public void setAttrSet(DS attrSet)
	{
		this.attrSet = attrSet;
	}

	public void setDone(boolean isDone)
	{
		this.isDone = isDone;
	}

	public void setResult(int result)
	{
		this.result = result;
	}
}
