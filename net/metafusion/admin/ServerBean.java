package net.metafusion.admin;

import java.io.Serializable;
import java.util.Iterator;
import acme.util.XML;

public class ServerBean implements Serializable
{
	static final long serialVersionUID = 1L;
	private String ae = "gui";
	private boolean isActive = true;
	private String name = "";
	protected boolean invalid = false;
	protected boolean isProxy = false;
	protected int serialVersion = 1;
	protected XML xml;
	ForwardRuleBean frb[] = ForwardRuleBean.getEmptyArray();
	PollRuleBean prb[] = PollRuleBean.getEmptyArray();
	StorageRuleBean srb[] = new StorageRuleBean[] { new StorageRuleBean(0, false, "", ""), new StorageRuleBean(1, false, "", "") };

	public ServerBean()
	{
		this.name = "";
	}

	public ServerBean(String name)
	{
		this.name = name;
	}

	public ServerBean(String name, String ae, boolean isActive)
	{
		this.name = name;
		this.ae = ae;
		this.isProxy = false;
		this.isActive = isActive;
	}

	public ServerBean(String name, String ae, boolean isProxy, boolean isActive)
	{
		this.name = name;
		this.ae = ae;
		this.isProxy = isProxy;
		this.isActive = isActive;
	}

	public ServerBean(XML x)
	{
		this.xml = x;
		this.name = x.get("name", name);
		this.ae = x.get("ae", "");
		this.isProxy = false;
		this.isActive = x.getBoolean("isactive");
		XML rules = x.getNode("rules");
		if (rules != null) loadRules(rules);
	}

	public String getAE()
	{
		return ae;
	}

	public ForwardRuleBean[] getForwardRule()
	{
		return frb;
	}

	public String getName()
	{
		return name;
	}

	public PollRuleBean[] getPollRule()
	{
		return prb;
	}

	public StorageRuleBean[] getStorageRule()
	{
		return srb;
	}

	public boolean isActive()
	{
		return isActive;
	}

	public boolean isInvalid()
	{
		return invalid;
	}

	public boolean isProxy()
	{
		return isProxy;
	}

	public void save()
	{
		xml.addAttr("name", name);
		xml.addAttr("ae", ae);
		xml.addAttr("isactive", isActive ? "true" : "false");
		XML oldRules = xml.getNode("rules");
		if (oldRules != null) xml.removeNode(oldRules);
		XML rules = new XML("rules");
		rules.addNode(srb[0].toXML());
		rules.addNode(srb[1].toXML());
		for (int i = 0; i < frb.length; i++)
			rules.addNode(frb[i].toXML());
		for (int i = 0; i < prb.length; i++)
			rules.addNode(prb[i].toXML());
		xml.add(rules);
	}

	public void setActive(boolean active)
	{
		isActive = active;
	}

	public void setAE(String ae)
	{
		this.ae = ae;
	}

	public void setForwardRule(ForwardRuleBean[] frb)
	{
		this.frb = frb;
	}

	public void setInvalid(boolean invalid)
	{
		this.invalid = invalid;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public void setPollRule(PollRuleBean[] prb)
	{
		this.prb = prb;
	}

	public void setProxy(boolean proxy)
	{
		isProxy = proxy;
	}

	public void setStorageRule(StorageRuleBean[] srb)
	{
		this.srb = srb;
	}

	public String toString()
	{
		return name;
	}

	public XML toXML()
	{
		XML x = new XML("store");
		x.addAttr("name", name);
		x.addAttr("ae", ae);
		x.addAttr("isactive", isActive ? "true" : "false");
		XML rules = new XML("rules");
		rules.addNode(srb[0].toXML());
		rules.addNode(srb[1].toXML());
		for (int i = 0; i < frb.length; i++)
			rules.addNode(frb[i].toXML());
		for (int i = 0; i < prb.length; i++)
			rules.addNode(prb[i].toXML());
		x.add(rules);
		return x;
	}

	// new ForwardRuleBean[] {
	// new ForwardRuleBean(0, false, "",""), new ForwardRuleBean(1, false,
	// "",""),
	// new ForwardRuleBean(0, false, "",""), new ForwardRuleBean(1, false,
	// "",""),
	// new ForwardRuleBean(0, false, "",""), new ForwardRuleBean(1, false,
	// "",""),
	// new ForwardRuleBean(0, false, "",""), new ForwardRuleBean(1, false,
	// "","")
	// };
	void loadRules(XML rules)
	{
		srb = new StorageRuleBean[] { new StorageRuleBean(0, false, "", ""), new StorageRuleBean(1, false, "", "") };
		frb = ForwardRuleBean.getEmptyArray();
		prb = PollRuleBean.getEmptyArray();
		// new ForwardRuleBean[] {
		// new ForwardRuleBean(0, false, "",""), new ForwardRuleBean(0, false,
		// "",""),
		// new ForwardRuleBean(0, false, "",""), new ForwardRuleBean(0, false,
		// "",""),
		// new ForwardRuleBean(0, false, "",""), new ForwardRuleBean(0, false,
		// "",""),
		// new ForwardRuleBean(0, false, "",""), new ForwardRuleBean(0, false,
		// "","")
		// };
		int frbCount = 0;
		int prbCount = 0;
		for (Iterator iter = rules.getList().iterator(); iter.hasNext();)
		{
			XML x = (XML) iter.next();
			if (x.getName().equalsIgnoreCase("storage"))
			{
				StorageRuleBean b = new StorageRuleBean(x);
				srb[b.getType()] = b;
			} else if (x.getName().equalsIgnoreCase("forward"))
			{
				ForwardRuleBean b = new ForwardRuleBean(x);
				frb[(frbCount++) % frb.length] = b;
			} else if (x.getName().equalsIgnoreCase("polling"))
			{
				PollRuleBean b = new PollRuleBean(x);
				prb[(prbCount++) % prb.length] = b;
			}
		}
	}
}
