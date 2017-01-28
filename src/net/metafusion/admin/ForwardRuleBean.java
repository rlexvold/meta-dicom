package net.metafusion.admin;

import java.io.Serializable;
import acme.util.StringUtil;
import acme.util.XML;

public class ForwardRuleBean implements Cloneable, Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;
	public static final int OF_MODALITY_TYPE = 0;
	public static final int TO_RADIOLOGIST_TYPE = 1;
	public static final int FROM_SOURCE_TYPE = 2;
	public static final int FROM_PHYSICIAN_TYPE = 3;
	public static final String[] TYPE_NAME = new String[] { "Of Modality", "To Radiologist", "From Source", "From Physician" };
	public static final int MAX_RULE = 20;

	public static ForwardRuleBean[] getEmptyArray()
	{
		ForwardRuleBean frb[] = new ForwardRuleBean[MAX_RULE];
		for (int i = 0; i < MAX_RULE; i++)
			frb[i] = new ForwardRuleBean(0, false, "", "");
		return frb;
	}

	public ForwardRuleBean(int type, boolean enabled, String arg, String destAE)
	{
		this.type = type;
		this.enabled = enabled;
		this.arg = arg;
		this.destAE = destAE;
	}

	public ForwardRuleBean(int type, boolean enabled, String arg, String destAE, String start, String end)
	{
		this.type = type;
		this.enabled = enabled;
		this.arg = arg;
		this.destAE = destAE;
		this.start = start;
		this.end = end;
	}

	public ForwardRuleBean(ForwardRuleBean frb)
	{
		this.type = frb.type;
		this.enabled = frb.enabled;
		this.arg = frb.arg;
		this.destAE = frb.destAE;
		this.start = frb.start;
		this.end = frb.end;
	}

	public ForwardRuleBean(XML xml)
	{
		this.type = StringUtil.findString(TYPE_NAME, xml.get("type"), 0);
		this.enabled = xml.getBoolean("isactive");
		this.arg = xml.get("arg1", "");
		this.destAE = xml.get("arg2", "");
		this.start = xml.get("start", "00:00");
		this.end = xml.get("end", "24:00");
	}

	public XML toXML()
	{
		XML x = new XML("forward");
		x.addAttr("type", TYPE_NAME[type]);
		x.addAttr("isactive", enabled ? "true" : "false");
		x.addAttr("arg1", arg);
		x.addAttr("arg2", destAE);
		x.addAttr("start", start);
		x.addAttr("end", end);
		return x;
	}
	private int type;
	private boolean enabled;
	private String arg;
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

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public String getArg()
	{
		return arg;
	}

	public void setArg(String arg)
	{
		this.arg = arg;
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
