package integration;

import java.io.Serializable;

public class MFSession implements Serializable
{
	static final long serialVersionUID = 1L;
	protected int serialVersion = 1;

	static public void init(MFClient client)
	{
		session = new MFSession(client);
	}
	static public MFSession session;

	static public MFSession get()
	{
		return session;
	}

	public MFSession(MFClient client)
	{
		this.client = client;
	}
	volatile MFClient client;

	public String getLoginUser()
	{
		return client.name;
	}

	public MFUser[] getUserList()
	{
		MFUser[] user = client.getUserList();
		return user;
	}

	public String[] getUserNameList()
	{
		String[] userName = client.getUserNameList();
		return userName;
	}

	public void logout()
	{
		client.logout();
	}

	public MFStudy[] queryArchive(SearchBean sb)
	{
		if (sb == null) sb = new SearchBean();
		MFStudy[] study = client.query("archive", sb);
		return study;
	}

	public MFStudy[] queryDictate(SearchBean sb)
	{
		if (sb == null) sb = new SearchBean();
		sb.setState("R");
		sb.setReader(client.name);
		MFStudy[] study = client.query("dictate", sb);
		return study;
	}

	public MFStudy[] querySignOff(SearchBean sb)
	{
		if (sb == null) sb = new SearchBean();
		sb.setState("D");
		sb.setReader(client.name);
		MFStudy[] study = client.query("signoff", sb);
		return study;
	}

	public MFStudy[] queryUnread(SearchBean sb)
	{
		if (sb == null) sb = new SearchBean();
		sb.setState("U");
		MFStudy[] study = client.query("unread", sb);
		return study;
	}

	public MFStudy[] queryReview(SearchBean sb)
	{
		if (sb == null) sb = new SearchBean();
		sb.setState("X");
		sb.setReader(client.name);
		MFStudy[] study = client.query("review", sb);
		return study;
	}

	public MFStudy attachFile(MFStudy study, String fileName, String label, byte[] data)
	{
		return client.attach(study.studyID, fileName, label, data);
	}

	public byte[] readAttachedFile(MFStudy study, long id)
	{
		byte[] b = client.readAttachedFile(study.studyID, id);
		return b;
	}

	public void requestReview(MFStudy study, String userName)
	{
		client.requestReview(study.studyID, userName);
	}

	public void removeReview(MFStudy study, String userName)
	{
		client.removeReview(study.studyID, userName);
	}

	public MFStudy loadStudy(long id)
	{
		return client.loadStudy(id);
	}

	public MFStudy update(MFStudy study)
	{
		return client.update(study);
	}

	public MFSite[] getSiteList()
	{
		return new MFSite[0];
	}

	public MFIdle idle()
	{
		return client.idle();
	}

	public void sendMessage(String userName, String title, String text)
	{
		client.sendMessage(userName, title, text);
	}

	public boolean sendStudy(long id, String ae)
	{
		return client.sendStudy(id, ae);
	}
	// data
	//
	// HashMap userMap = new HashMap();
	// void initData()
	// {
	// for (int i=0; i<names.length; i++)
	// {
	// userMap.put(names[i],new MFUser(100+i, names[i]));
	// userMap.put(user.name, user);
	// }
	// }
	//
	//
	// MFUser user = new MFUser(1, "matt");
	// String names[] =
	// {
	// "YOUNG",
	// "WEISS",
	// "WALLACE",
	// "VOLPICELLI",
	// "TABIBIAN",
	// "SIMMONS",
	// "SIMA",
	// };
	//
	//
	//
	// MFSite sites[] = new MFSite[10];
	//
	//
	// public static String[] SiteDef = new String[]
	// {
	// // "id,name",
	// "1,SISERS OF MERCY",
	// "2,OCONNOR",
	// "3,GOOD SAMARITAN"
	// };
	//
	// public static String[] ModalityDef = new String[]
	// {
	// // "id,type,name",
	// "1,CT,PICKER",
	// "1,CT,ALLCT66",
	// "1,NM,AE_ECAM",
	// "1,US,ULTRASND1",
	// "1,US,ULTRASND2",
	// "1,US,ULTRASND3",
	// "1,US,ULTRASND4",
	// "1,XR,XRAY1",
	// "1,XR,XRAY2",
	// "1,XR,XRAY3",
	// "1,XR,XRAY4",
	// "2,CT,CTTRAILER",
	// "2,MR,LX_MR",
	// "2,NM,AE_ECAM",
	// "2,US,ULTRASND5",
	// "2,US,ULTRASND6",
	// "2,US,ULTRASND7",
	// "2,US,ULTRASND8",
	// "2,XR,XRAY5",
	// "2,XR,XRAY6",
	// "3,CT,ANONCT1",
	// "3,CT,ANONCT2",
	// "3,NM,ANONNM",
	// "3,US,ANONSND1",
	// "3,US,ANONSND2",
	// "3,US,ANONSND3",
	// "3,US,ANONSND4",
	// "3,XR,XRANON1",
	// "3,XR,XRANON2",
	// "3,XR,XRANON3",
	// "3,XR,XRANON4"
	// };
	//
	//
	//
	//
	// public static String test()
	// {
	// Socket s = null;
	// try
	// {
	// s = new Socket("127.0.0.1",4007);
	// ObjectInputStream ois = null;
	// ObjectOutputStream oos = null;
	// oos = new ObjectOutputStream(new
	// BufferedOutputStream(s.getOutputStream()));
	// oos.writeObject(new MFStudy());
	// oos.flush();
	// ois = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
	// Object o = ois.readObject();
	// return o.toString();
	// }
	// catch (Exception e)
	// {
	// throw new RuntimeException("caught "+e);
	// }
	// finally
	// {
	// try { s.close(); }
	// catch (Exception e) { ; }
	//
	//
	//
	// }
	//
	// }
}