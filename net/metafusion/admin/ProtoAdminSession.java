package net.metafusion.admin;

import integration.SearchBean;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class ProtoAdminSession
{
	protected ProtoAdminSession()
	{
		addAE(new AEBean("ae1", "localhost", "80"));
		addAE(new AEBean("ae2", "localhost", "80"));
		addAE(new AEBean("ae3", "localhost", "80"));
		addAE(new AEBean("ae4", "localhost", "80"));
		addAE(new AEBean("ae5", "localhost", "80"));
	}
	//
	// Metafusion.Setup
	//
	private boolean isConnected = true;
	private String host = "";
	private int port = 0;

	public void connect(String host, int port)
	{
		isConnected = true;
		this.host = host;
		this.port = port;
	}

	public boolean isConnected()
	{
		return isConnected;
	}

	public String getHost()
	{
		return host;
	}

	public int getPort()
	{
		return port;
	}
	private String commitDate = new Date().toString();

	public String getCommitDate()
	{
		return commitDate;
	}

	public boolean commit()
	{
		commitDate = new Date().toString();
		return true;
	}
	//
	// Metafusion.AE
	//
	private HashMap aeMap = new HashMap();
	private List aeList = new ArrayList();

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
		aeMap.put(ae.getName(), ae);
		aeList.add(ae);
		Collections.sort(aeList);
	}

	public void removeAE(AEBean ae)
	{
		if (!aeMap.containsKey(ae.getName())) throw new RuntimeException("AE " + ae.getName() + " does not exist.");
		aeMap.remove(ae.getName());
		aeList.remove(ae);
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
	static boolean failVerify = true;

	public boolean verifyAE(AEBean ae)
	{
		failVerify = !failVerify;
		return failVerify;
	}

	//
	//
	//
	public ServerBean[] getLocalStores()
	{
		return new ServerBean[] { new ServerBean("sandiego") };
	}

	public ProxyBean[] getProxies()
	{
		return new ProxyBean[] { new ProxyBean("proxy1"), new ProxyBean("proxy2"), new ProxyBean("proxy3") };
	}

	public StudyBean[] search(AEBean ae, SearchBean search)
	{
		StudyBean[] sb = new StudyBean[50];
		for (int i = 0; i < 50; i++)
		{
			StudyBean s = new StudyBean();
			s.setAccession("acc" + i);
			s.setBirthdate("1/1/2000");
			s.setDate("1/1/2000");
			s.setDescription("a study ");
			s.setModality("CT");
			s.setName("Patient Name" + i);
			s.setPatientID("ididid" + i);
			s.setRadiologist("Dr Good");
			s.setReferringMD("Dr Night");
			s.setSex("F");
			s.setStationName("ct66");
			s.setStudyID("123.324.434.344." + i);
			s.setTime("10:00:00");
			sb[i] = s;
		}
		return sb;
	}
	StorageRuleBean srb[] = new StorageRuleBean[] { new StorageRuleBean(0, false, "", ""), new StorageRuleBean(1, false, "", "") };
	ForwardRuleBean frb[] = ForwardRuleBean.getEmptyArray();
	PollRuleBean prb[] = PollRuleBean.getEmptyArray();

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
	public StorageRuleBean[] getStorageRuleBean(ServerBean serverBean)
	{
		StorageRuleBean a[] = new StorageRuleBean[srb.length];
		for (int i = 0; i < srb.length; i++)
			a[i] = new StorageRuleBean(srb[i]);
		return a;
	}

	public ForwardRuleBean[] getForwardRuleBean(ServerBean serverBean)
	{
		ForwardRuleBean a[] = new ForwardRuleBean[frb.length];
		for (int i = 0; i < frb.length; i++)
			a[i] = new ForwardRuleBean(frb[i]);
		return a;
	}

	public PollRuleBean[] getPollRuleBean(ServerBean serverBean)
	{
		PollRuleBean a[] = new PollRuleBean[prb.length];
		for (int i = 0; i < prb.length; i++)
			a[i] = new PollRuleBean(prb[i]);
		return a;
	}

	public void setStorageRuleBean(ServerBean sb, StorageRuleBean[] srb)
	{
		this.srb = srb;
	}

	public void setForwardRuleBean(ServerBean sb, ForwardRuleBean[] frb)
	{
		this.frb = frb;
	}

	public void setPollRuleBean(ServerBean sb, PollRuleBean[] prb)
	{
		this.prb = prb;
	}

	public StorageBean getStorage(ServerBean sb)
	{
		return new StorageBean();
	}

	public void setActive(ServerBean serverBean, boolean active)
	{
		serverBean.setActive(active);
	}
	List log = new ArrayList();

	public List getLog(ServerBean serverBean, Date start, int maxToGet)
	{
		log = new ArrayList();
		for (int i = 0; i < (maxToGet == 0 ? 100 : maxToGet); i++)
			log.add(new LogBean());
		return log;
	}

	public void getLogNext(List l, int maxToGet)
	{
		for (int i = 0; i < (maxToGet == 0 ? 100 : maxToGet); i++)
			l.add(new LogBean());
	}
}
