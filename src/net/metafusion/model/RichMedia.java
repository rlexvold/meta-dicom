package net.metafusion.model;

import java.sql.Timestamp;

public class RichMedia
{
	Long		richMediaID;
	String		source;
	Long		studyid;
	String		destination;
	Long		size;
	Timestamp	timeEntered;
	String		status;

	public Long getRichMediaID()
	{
		return richMediaID;
	}

	public void setRichMediaID(Long richMediaID)
	{
		this.richMediaID = richMediaID;
	}

	public Long getStudyid()
	{
		return studyid;
	}

	public void setStudyid(Long studyid)
	{
		this.studyid = studyid;
	}

	public Long getSize()
	{
		return size;
	}

	public void setSize(Long size)
	{
		this.size = size;
	}

	public Timestamp getTimeEntered()
	{
		return timeEntered;
	}

	public void setTimeEntered(Timestamp timeEntered)
	{
		this.timeEntered = timeEntered;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getSource()
	{
		return source;
	}

	public void setSource(String source)
	{
		this.source = source;
	}

	public String getDestination()
	{
		return destination;
	}

	public void setDestination(String destination)
	{
		this.destination = destination;
	}
}
