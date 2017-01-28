package net.metafusion.admin;

import java.io.Serializable;
import acme.util.StringUtil;
import acme.util.XML;

public class StorageRuleBean implements Cloneable, Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;
	public static final int AGE_TYPE = 0;
	public static final int FREESPACE_TYPE = 1;
	static String typeNames[] = { "age", "freespace" };

	public StorageRuleBean(int type, boolean enabled, String arg1, String arg2)
	{
		this.type = type;
		this.enabled = enabled;
		this.arg1 = arg1;
		this.arg2 = arg2;
	}

	public StorageRuleBean(StorageRuleBean srb)
	{
		this.type = srb.type;
		this.enabled = srb.enabled;
		this.arg1 = srb.arg1;
		this.arg2 = srb.arg2;
	}

	public StorageRuleBean(XML xml)
	{
		this.type = StringUtil.findString(typeNames, xml.get("type"), 0);
		this.enabled = xml.getBoolean("isactive");
		this.arg1 = xml.get("arg1", "");
		this.arg2 = xml.get("arg2", "");
	}

	public XML toXML()
	{
		XML x = new XML("storage");
		x.addAttr("type", typeNames[type]);
		x.addAttr("isactive", enabled ? "true" : "false");
		x.addAttr("arg1", arg1);
		x.addAttr("arg2", arg2);
		return x;
	}
	private int type;
	private boolean enabled;
	private String arg1 = "";
	private String arg2 = "";

	public boolean isEnabled()
	{
		return enabled;
	}

	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public String getArg1()
	{
		return arg1;
	}

	public void setArg1(String arg1)
	{
		this.arg1 = arg1;
	}

	public String getArg2()
	{
		return arg2;
	}

	public void setArg2(String arg2)
	{
		this.arg2 = arg2;
	}
}
