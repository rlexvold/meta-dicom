package net.metafusion.admin;

import java.io.Serializable;
import acme.util.XML;

public class ProxyBean extends ServerBean implements Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;

	public ProxyBean()
	{
		super();
		setProxy(true);
	}

	public ProxyBean(String name)
	{
		super(name);
		setProxy(true);
	}

	public ProxyBean(String name, String ae, boolean isActive)
	{
		super(name, ae, true, isActive);
	}

	public ProxyBean(XML x)
	{
		super(x);
		this.isProxy = true;
	}
}
