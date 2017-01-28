package net.metafusion.communication;

import java.io.File;
import net.metafusion.Dicom;
import net.metafusion.admin.ServerBean;
import net.metafusion.communication.socket.SocketConnectionInfo;
import net.metafusion.dicom.message.DicomMessage;
import net.metafusion.message.IMessageManager;
import net.metafusion.message.MessageManagerFactory;
import net.metafusion.message.MessageType;
import net.metafusion.msg.CMoveReq;
import net.metafusion.util.AE;
import net.metafusion.util.AEMap;
import acme.util.Log;
import acme.util.Util;
import acme.util.XML;
import acme.util.XMLConfigFile;

public class TestIt
{
	public static void main(String[] args)
	{
		TestIt tmp = new TestIt();
		tmp.go();
	}
	static public File CONFIG_FILE;
	static public File CONFIG_ROOT;
	static public File DATA_ROOT;

	public XML getCurrentAdminStore() throws Exception
	{
		CONFIG_FILE = new File("C:/mydocs/Personal/Lexicon/Cyrus/LocalStore/conf/metafusion.xml");
		XMLConfigFile configFile = new XMLConfigFile(CONFIG_FILE, "greg");
		CONFIG_ROOT = CONFIG_FILE.getParentFile();
		Log.vlog("" + configFile.getXML());
		DATA_ROOT = new File(configFile.getXML().get("storage/root"));
		XMLConfigFile f = XMLConfigFile.getDefault();
		String storeName = f.getDefaultName();
		System.out.println("getCurrentAdminStore " + storeName);
		Util.log("getConfigRoot() " + f.getConfigRoot().getAbsolutePath());
		XML root = new XML(new File(f.getConfigRoot(), "metaadmin.xml"));
		XML x = root.getNode("stores");
		if (x != null) x = x.search("name", storeName);
		if (x == null)
		{
			x = root.getNode("proxies");
			if (x != null) x = x.search("name", storeName);
		}
		if (x == null) throw new RuntimeException("could not load admin for " + storeName);
		return x;
	}

	public void go()
	{
		try
		{
			XML storeXML = getCurrentAdminStore();
//			serverBean = new ServerBean(storeXML);
			AE serverAE = AEMap.get(System.getProperty("-ae", "SERVER"));
			Dicom.init();
			AEMap.setDefault(serverAE);
			SocketConnectionInfo serverInfo = new SocketConnectionInfo();
			serverInfo.setCommunicationProtocol(CommunicationProtocol.SOCKET);
			serverInfo.setMessageType(MessageType.DICOM);
			serverInfo.setHostname("127.0.0.1");
			serverInfo.setPort(5219);
			DicomMessage msg = new DicomMessage();
			msg.setServerName("greg");
			msg.setDataSet(msg.searchByPatientName("HAZARI"));
			CMoveReq moveReq = new CMoveReq(msg.getDataSet());
			moveReq.MoveDestination = "greg";
			moveReq.CommandField = Dicom.C_FIND_RQ;
			moveReq.Priority = Dicom.MEDIUM;
			moveReq.DataSetType = (short) Dicom.COMMAND_DATASET_PRESENT;
			IMessageManager msgManager = MessageManagerFactory.getFactory().getMessageHandler(serverInfo);
			msgManager.sendMessage(msg);
		}
		catch (Exception e)
		{
			System.out.println("TestIt ERROR: " + e.getMessage());
		}
	}
}
