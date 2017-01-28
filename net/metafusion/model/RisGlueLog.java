package net.metafusion.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class RisGlueLog implements Serializable
{
	long		logID;
	String		input;
	String		output;
	String		ris;
	Timestamp	timeEntered;
	String		status;

	public long getLogID()
	{
		return logID;
	}

	public void setLogID(long logID)
	{
		this.logID = logID;
	}

	public String getInput()
	{
		return input;
	}

	public void setInput(String input)
	{
		this.input = input;
	}

	public String getOutput()
	{
		return output;
	}

	public void setOutput(String output)
	{
		this.output = output;
	}

	public String getRis()
	{
		return ris;
	}

	public void setRis(String ris)
	{
		this.ris = ris;
	}

	public Timestamp getTimeEntered()
	{
		return timeEntered;
	}

	public void setTimeEntered(Timestamp timestamp)
	{
		this.timeEntered = timestamp;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}
}
