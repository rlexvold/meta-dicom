package net.metafusion.admin;

import java.io.Serializable;
import acme.util.XML;

public class PollRuleBean implements Cloneable, Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;
	public static final int MAX_POLL = 10;

	public static PollRuleBean[] getEmptyArray()
	{
		PollRuleBean frb[] = new PollRuleBean[MAX_POLL];
		for (int i = 0; i < MAX_POLL; i++)
			frb[i] = new PollRuleBean(false, "", "");
		return frb;
	}

	public PollRuleBean(boolean enabled, String secs, String destAE)
	{
		this.enabled = enabled;
		this.min = secs;
		this.destAE = destAE;
	}

	public PollRuleBean(boolean enabled, String secs, String destAE, String start, String end)
	{
		this.enabled = enabled;
		this.min = secs;
		this.destAE = destAE;
		this.start = start;
		this.end = end;
	}

	public PollRuleBean(PollRuleBean frb)
	{
		this.enabled = frb.enabled;
		this.min = frb.min;
		this.destAE = frb.destAE;
		this.start = frb.start;
		this.end = frb.end;
	}

	public PollRuleBean(XML xml)
	{
		this.enabled = xml.getBoolean("isactive");
		this.min = xml.get("arg1", "");
		this.destAE = xml.get("arg2", "");
		this.start = xml.get("start", "00:00");
		this.end = xml.get("end", "24:00");
	}

	public XML toXML()
	{
		XML x = new XML("polling");
		x.addAttr("isactive", enabled ? "true" : "false");
		x.addAttr("arg1", min);
		x.addAttr("arg2", destAE);
		x.addAttr("start", start);
		x.addAttr("end", end);
		return x;
	}
	private boolean enabled;
	private String min;
	private String destAE;
	private String start = "00:00";
	private String end = "24:00";

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public String getDestAE()
	{
		return destAE;
	}

	public void setDestAE(String destAE)
	{
		this.destAE = destAE;
	}

	public String getMin()
	{
		return min;
	}

	public void setMin(String arg)
	{
		this.min = arg;
	}

	public String getStart()
	{
		return start;
	}

	public void setStart(String start)
	{
		this.start = start;
	}

	public String getEnd()
	{
		return end;
	}

	public void setEnd(String end)
	{
		this.end = end;
	}

	public boolean isTimeValid(int minutesIntoDay)
	{
		try
		{
			if (start.equals("00:00") && end.equals("24:00")) return true;
			int startMin = Integer.parseInt(start.substring(0, 2)) * 60 + Integer.parseInt(start.substring(3));
			int endMin = Integer.parseInt(end.substring(0, 2)) * 60 + Integer.parseInt(end.substring(3));
			return (startMin >= minutesIntoDay) && (endMin < minutesIntoDay);
		}
		catch (NumberFormatException e)
		{
			return true;
		}
	}
}
