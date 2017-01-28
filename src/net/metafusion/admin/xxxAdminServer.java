package net.metafusion.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import acme.util.Util;
import acme.util.XML;
import acme.util.XMLConfigFile;

class xxxAdminServer
{
	private xxxAdminServer()
	{
	}
	XML nullXML = XML.parseXML(" <metaadmin> " + " <admin host='none' port='0' date='Sun Feb 15 09:36:46 PST 2004' /> " + " <aelist> " + " </aelist> " + " <stores> "
			+ " </stores> " + " <proxies> " + " </proxies> " + " </metaadmin> ");
	XML configXML = nullXML;

	boolean haveConfig()
	{
		return configXML != nullXML;
	}

	public XML loadConfig(String host, int port)
	{
		configXML = nullXML;
		try
		{
			configXML = XMLConfigFile.getDefault().getSubconfig("metaadmin.xml");
		}
		catch (Exception e)
		{
			Util.log("could not load config: " + e);
		}
		refresh();
		return configXML;
	}

	public void refresh()
	{
		try
		{
			loadae();
		}
		catch (Exception e)
		{
			Util.log("could not load ae: " + e);
		}
		try
		{
			loadstores();
		}
		catch (Exception e)
		{
			Util.log("could not load stores: " + e);
		}
	}

	public boolean storeConfig(String host, int port, XML config)
	{
		this.configXML = config;
		return true;
	}
	// public void commit() {
	// // todo: do commit not used
	// }
	//
	// public StorageBean getStorage(AE ae) {
	// StorageBean sb = new StorageBean();
	// // not used
	// return sb;
	// }
	//
	// ae
	//
	private HashMap aeMap = new HashMap();
	private List aeList = new ArrayList();

	private void loadae()
	{
		aeMap.clear();
		aeList.clear();
		List l = configXML.getList("aelist");
		for (Iterator iter = l.iterator(); iter.hasNext();)
		{
			XML x = (XML) iter.next();
			AEBean ae = new AEBean(x);
			aeMap.put(ae.getName(), ae);
			aeList.add(ae);
			Collections.sort(aeList);
		}
	}

	public AEBean[] getAE()
	{
		return (AEBean[]) aeList.toArray(new AEBean[aeList.size()]);
	}

	public void addAE(AEBean ae)
	{
		if (ae.getName().length() == 0) throw new RuntimeException("Name must be defined.");
		if (ae.getHost().length() == 0) throw new RuntimeException("Host must be defined.");
		if (ae.getPort().length() == 0) throw new RuntimeException("Port must be defined.");
		if (aeMap.containsKey(ae.getName())) throw new RuntimeException("AE with this name already exists.");
		try
		{
			;// InetAddress inet = InetAddress.getByName(host);
		}
		catch (Exception e)
		{
			throw new RuntimeException("The host does not exist.");
		}
		try
		{
			int p = Integer.parseInt(ae.getPort());
		}
		catch (Exception e)
		{
			throw new RuntimeException("The port does not exist.");
		}
		XML x = ae.toXML();
		configXML.getNode("aelist").add(x);
		refresh();
	}

	public void removeAE(AEBean ae)
	{
		if (!aeMap.containsKey(ae.getName())) throw new RuntimeException("AE " + ae.getName() + " does not exist.");
		aeMap.remove(ae.getName());
		aeList.remove(ae);
		XML ael = configXML.getNode("aelist");
		ael.removeChildNode("name", ae.getName());
		refresh();
	}

	public void updateAE(AEBean ae, String host, String port)
	{
		boolean fail = true;
		AEBean orig = (AEBean) aeMap.get(ae.getName());
		removeAE(ae);
		try
		{
			ae.setHost(host);
			ae.setPort(port);
			addAE(ae);
			fail = false;
		}
		finally
		{
			if (fail) addAE(orig);
		}
	}

	public boolean verifyAE(String name)
	{
		return true;
	}
	ServerBean[] localStores = new ServerBean[0];
	ProxyBean[] proxies = new ProxyBean[0];

	void loadstores()
	{
		localStores = new ServerBean[0];
		proxies = new ProxyBean[0];
		List l = configXML.getNode("stores").getList();
		ServerBean[] sb = new ServerBean[l.size()];
		int i = 0;
		for (Iterator iter = l.iterator(); iter.hasNext();)
		{
			XML x = (XML) iter.next();
			sb[i++] = new ServerBean(x);
		}
		i = 0;
		l = configXML.getNode("proxies").getList();
		ProxyBean[] pb = new ProxyBean[l.size()];
		for (Iterator iter = l.iterator(); iter.hasNext();)
		{
			XML x = (XML) iter.next();
			pb[i++] = new ProxyBean(x);
		}
		localStores = sb;
		proxies = pb;
	}

	public ServerBean[] getLocalStores()
	{
		return localStores;
	}

	public ProxyBean[] getProxies()
	{
		return proxies;
	}
}