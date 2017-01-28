package net.metafusion.localstore;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import net.metafusion.Dicom;
import net.metafusion.admin.AdminServer;
import net.metafusion.localstore.service.CLocalStoreCreateService;
import net.metafusion.localstore.service.CLocalStoreEchoService;
import net.metafusion.localstore.service.CLocalStoreFindService;
import net.metafusion.localstore.service.CLocalStoreMoveService;
import net.metafusion.localstore.service.CLocalStoreSetService;
import net.metafusion.localstore.service.CLocalStoreStoreService;
import net.metafusion.net.DicomServer;
import acme.util.Log;

public class LocalStoreDicomServer extends DicomServer
{
	static void log(String s)
	{
		Log.log(s);
	}

	static void log(String s, Exception e)
	{
		Log.log(s, e);
	}
	LocalStore	localStore;
	AdminServer	adminServer	= new AdminServer();

	public LocalStoreDicomServer(LocalStore localStore) throws Exception
	{
		super();
		this.localStore = localStore;
		addServiceProvider(Dicom.C_ECHO_RQ, CLocalStoreEchoService.class);
		addServiceProvider(Dicom.C_STORE_RQ, CLocalStoreStoreService.class);
		addServiceProvider(Dicom.C_FIND_RQ, CLocalStoreFindService.class);
		addServiceProvider(Dicom.C_MOVE_RQ, CLocalStoreMoveService.class);
		addServiceProvider(Dicom.N_CREATE_RQ, CLocalStoreCreateService.class); // worklist
		addServiceProvider(Dicom.N_SET_RQ, CLocalStoreSetService.class);
		if (localStore.IsPrimary())
		{
		}
		else if (localStore.IsSecondary())
		{
		}
		else if (localStore.IsBackup())
		{
		}
	}

	@Override
	protected void handleRawRequest(Socket s, InputStream is, OutputStream os) throws Exception
	{
		adminServer.handleRequest(s, is, os);
	}
}
