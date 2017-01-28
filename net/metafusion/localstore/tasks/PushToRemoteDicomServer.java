/*
 * Created by IntelliJ IDEA.
 * User: mb
 * Date: Oct 23, 2003
 * Time: 7:15:37 PM
 */
package net.metafusion.localstore.tasks;

import java.io.File;
import java.io.InputStream;
import net.metafusion.Dicom;
import net.metafusion.localstore.DicomStore;
import net.metafusion.model.Image;
import net.metafusion.msg.CStoreReq;
import net.metafusion.net.DicomSession;
import net.metafusion.service.DicomClientService;
import net.metafusion.util.Message;
import acme.db.DBManager;
import acme.util.Log;
import acme.util.Util;
import acme.util.XMLConfigFile;

public class PushToRemoteDicomServer implements Runnable
{
	static void log(String s)
	{
		Log.log(s);
	}
	DicomStore dicomStore;

	PushToRemoteDicomServer() throws Exception
	{
		DBManager.init();
		Dicom.init();
		DicomStore.init();
		dicomStore = DicomStore.get();
	}

	public void run()
	{
		for (;;)
		{
			try
			{
				// QueueElem elem = QueueView.get().peek("mf_andromeda");
				// if (elem == null)
				// break;
				// log(""+elem);
				// DicomClientSession clientSess = new
				// DicomClientSession(RoleMap.getStoreUserRoleMap());
				// boolean connected = clientSess.connect(AEMap.get("SERVER"));
				// log(""+connected);
				// if (connected) {
				// StoreClient store = new StoreClient(clientSess,
				// elem.getId());
				// store.run();
				// if (store.getResult() != Dicom.SUCCESS)
				// log("store failed");
				// clientSess.close(true);
				// if (store.getResult() == Dicom.SUCCESS)
				// ;//QueueView.get().delete(elem);
				// }
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			Util.sleep(5000);
		}
	}
	static class StoreClient extends DicomClientService
	{
		DicomStore store = DicomStore.get();

		public StoreClient(DicomSession s, long storageId) throws Exception
		{
			super("CStore", s);
			this.storageId = storageId;
		}
		long storageId;

		protected int runit() throws Exception
		{
			InputStream is = null;
			Image image = store.getImage(storageId);
			if (image == null) throw new Exception("could not find storageId " + storageId);
			try
			{
				sess.logAccess("store-to", "SOPInstanceUID=" + image.getImageUID());
				is = store.getImageStream(image);
				CStoreReq req = new CStoreReq();
				req.AffectedSOPClassUID = image.classUID;
				req.AffectedSOPInstanceUID = image.imageUID;
				req.Priority = Dicom.MEDIUM;
				req.CommandField = Dicom.C_STORE_RQ;
				req.MessageID = sess.getNextMsgID();
				req.DataSetType = (short) Dicom.COMMAND_DATASET_PRESENT;
				sess.writeMessage(req, is);
				Message resp = sess.readMessage();
				if (resp == null) return -1;
				assert resp.getCommandID() == Dicom.C_STORE_RSP;
				return resp.getStatus();
			}
			finally
			{
				Util.safeClose(is);
			}
		}
	}

	public static void main(String[] args)
	{
		try
		{
			if (args.length == 0)
			{
				log("usage: pushtoremotedicomserver conf_file_path.xml");
				System.exit(1);
			}
			System.out.println("pushtoremotedicomserver conf=" + args[0]);
			XMLConfigFile configFile = new XMLConfigFile(new File(args[0]), "default");
			// acme.util.Log.init("ptrds");
			PushToRemoteDicomServer sync = new PushToRemoteDicomServer();
			log("pushtoremotedicomserver.run");
			sync.run();
			log("pushtoremotedicomserver done");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
