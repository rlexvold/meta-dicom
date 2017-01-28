package net.metafusion.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class ServiceLog implements Serializable
{
	static final long	serialVersionUID	= 1L;
	Long				serviceLogId		= 0L;
	String				serviceType;
	String				studyuid;
	Timestamp			start;
	Timestamp			end;
	String				status;
	String				failureMessage;
	String				sourceAE;
	String				sourceIP;
	String				destAE;
	String				destIP;

	public Long getServiceLogId()
	{
		return serviceLogId;
	}

	public void setServiceLogId(Long serviceLogId)
	{
		this.serviceLogId = serviceLogId;
	}

	public Timestamp getStart()
	{
		return start;
	}

	public void setStart(Timestamp start)
	{
		this.start = start;
	}

	public Timestamp getEnd()
	{
		return end;
	}

	public void setEnd(Timestamp end)
	{
		this.end = end;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getFailureMessage()
	{
		return failureMessage;
	}

	public void setFailureMessage(String failureMessage)
	{
		this.failureMessage = failureMessage;
	}

	public String getSourceAE()
	{
		return sourceAE;
	}

	public void setSourceAE(String sourceAE)
	{
		this.sourceAE = sourceAE;
	}

	public String getSourceIP()
	{
		return sourceIP;
	}

	public void setSourceIP(String sourceIP)
	{
		this.sourceIP = sourceIP;
	}

	public String getDestAE()
	{
		return destAE;
	}

	public void setDestAE(String destAE)
	{
		this.destAE = destAE;
	}

	public String getDestIP()
	{
		return destIP;
	}

	public void setDestIP(String destIP)
	{
		this.destIP = destIP;
	}

	public static long getSerialVersionUID()
	{
		return serialVersionUID;
	}

	public String getServiceType()
	{
		return serviceType;
	}

	public void setServiceType(String serviceType)
	{
		this.serviceType = serviceType;
	}

	public String getStudyuid()
	{
		return studyuid;
	}

	public void setStudyuid(String studyuid)
	{
		this.studyuid = studyuid;
	}
}
