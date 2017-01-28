package net.metafusion.localstore;

import java.io.File;
import java.io.IOException;
import net.metafusion.net.DicomServerConfig;
import acme.util.XML;

public class LocalStoreConfig
{
	public LocalStoreConfig(XML xml) throws IOException
	{
		ae = xml.get("dicomserver/ae");
		dsConfig = new DicomServerConfig(xml.getNode("dicomserver"));
		// storeConfig = new SSConfig(xml.getNode("storage"));
		// remoteStoreName = xml.get("remotestorage/name");
		// remoteClientName = xml.get("remotestorage/clientname");
		// remoteStore =
		// Util.decodeInetSocketAddress(xml.get("remotestorage/ipaddr"));
		dbConfig = xml.getNode("database");
	}
	XML dbConfig;
	// SSConfig storeConfig;
	// String remoteStoreName;
	// String remoteClientName;
	DicomServerConfig dsConfig;
	String ae;
	File incoming;
	File temp;

	public String getAEName()
	{
		return ae;
	}

	public DicomServerConfig getDicomServerConfig()
	{
		return dsConfig;
	}

	// public SSConfig getSSConfig() {
	// return storeConfig;
	// }
	// String getRemoteStoreName() {
	// return remoteStoreName;
	// }
	//
	// String getRemoteClientName() {
	// return remoteClientName;
	// }
	//
	// InetSocketAddress getRemoteStore() {
	// return remoteStore;
	// }
	public String getDBClassName()
	{
		return dbConfig.get("class");
	}

	public String getDBURL()
	{
		return dbConfig.get("url");
	}
}
