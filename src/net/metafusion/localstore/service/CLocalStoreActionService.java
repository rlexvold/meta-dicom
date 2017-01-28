package net.metafusion.localstore.service;

import java.util.Iterator;
import java.util.List;
import net.metafusion.Dicom;
import net.metafusion.dataset.DS;
import net.metafusion.localstore.DicomStore;
import net.metafusion.model.ImageView;
import net.metafusion.msg.NActionReq;
import net.metafusion.msg.NActionRsp;
import net.metafusion.msg.NEventReq;
import net.metafusion.net.DicomClientSession;
import net.metafusion.net.DicomServerSession;
import net.metafusion.util.AE;
import net.metafusion.util.AEMap;
import net.metafusion.util.DSList;
import net.metafusion.util.Message;
import net.metafusion.util.RoleMap;
import net.metafusion.util.Tag;
import net.metafusion.util.UID;
import acme.util.Log;

public class CLocalStoreActionService extends DicomServiceProvider
{
	static void log(String s)
	{
		Log.log(s);
	}
	DicomStore	store	= DicomStore.get();

	public CLocalStoreActionService(DicomServerSession s) throws Exception
	{
		super(s, "CActionService");
	}
	static class SendStorageCommitEvent implements Runnable
	{
		static void log(String s)
		{
			Log.log(s);
		}

		static void log(String s, Exception e)
		{
			Log.log(s, e);
		}

		SendStorageCommitEvent(AE ae, String sourceAE, List sopSeq, String transactionUID)
		{
			this.ae = ae;
			this.sourceAE = sourceAE;
			this.sopSeq = sopSeq;
			this.transactionUID = transactionUID;
		}
		AE		ae;
		String	sourceAE;
		List	sopSeq;
		String	transactionUID;

		public boolean send()
		{
			DicomClientSession clientSess = null;
			boolean good = false;
			try
			{
				clientSess = new DicomClientSession(RoleMap.getStorageCommitEventRoleMap());
				boolean connected = clientSess.connect(ae);
				if (!connected)
					return false;
				Log.log("send commit report ====== " + sopSeq);
				NEventReq eventReq = new NEventReq();
				eventReq.AffectedSOPClassUID = UID.StorageCommitmentPushModel.getUID();
				// eventReq.AffectedSOPInstanceUID =
				// UID.StorageCommitmentPushModel.getUID();
				eventReq.CommandField = (short) Dicom.N_EVENT_REPORT_RQ;
				eventReq.MessageID = clientSess.getNextMsgID();
				eventReq.DataSetType = (short) Dicom.COMMAND_DATASET_PRESENT;
				eventReq.AffectedSOPInstanceUID = UID.StorageCommitmentPushModelSOPInstance.getUID();
				eventReq.EventTypeID = 1;
				DS ds = new DS();
				ds.put(Tag.RetrieveAET, sourceAE);
				ds.put(Tag.TransactionUID, transactionUID);
				DSList list = new DSList();
				for (Iterator iter = sopSeq.iterator(); iter.hasNext();)
				{
					DS ds1 = (DS) iter.next();
					String refSOPInstanceUID = (String) ds1.get(Tag.RefSOPInstanceUID);
					if (ImageView.get().exists(refSOPInstanceUID))
					{
						Log.vlog("rsp add " + refSOPInstanceUID);
						DS dsadd = new DS();
						dsadd.put(Tag.RefSOPClassUID, ds1.get(Tag.RefSOPClassUID));
						dsadd.put(Tag.RefSOPInstanceUID, refSOPInstanceUID);
						list.add(dsadd);
					}
					else
						Log.log("!!!!!!! storage commit could not find " + refSOPInstanceUID);
				}
				Log.vlog("addList" + list);
				ds.put(Tag.RefSOPSeq, list); // sopSeq
				clientSess.writeMessage(eventReq, ds);
				Message eventRsp = clientSess.readMessage();
				assert eventRsp.getCommandID() == Dicom.N_EVENT_REPORT_RSP;
				assert eventRsp.getStatus() == Dicom.SUCCESS;
				good = true;
				clientSess.close(true);
			}
			catch (Exception e)
			{
				log("NEventReq.send caught ", e);
			}
			finally
			{
				if (clientSess != null)
					clientSess.close(false);
			}
			return good;
		}

		public void run()
		{
			send();
		}
	}

	protected boolean handle(Message msg) throws Exception
	{
		NActionReq req = new NActionReq(msg);
		DS tags = msg.getDataSet();
		assert tags != null;
		assert req.ActionTypeID == 1;
		sess.logAccess("commit", "");
		String transactionUID = (String) tags.get(Tag.TransactionUID);
		List sopSeq = (List) tags.get(Tag.RefSOPSeq);
		Log.log("storage-commit: xid=" + transactionUID);
		Log.log("storage-commit: sopSeq=" + sopSeq);
		NActionRsp rsp = new NActionRsp();
		rsp.AffectedSOPClassUID = req.RequestedSOPClassUID;
		rsp.CommandField = (short) Dicom.N_ACTION_RSP;
		rsp.MessageIDToBeingRespondedTo = req.MessageID;
		rsp.DataSetType = (short) Dicom.COMMAND_DATASET_ABSENT;
		rsp.Status = (short) Dicom.SUCCESS;
		rsp.AffectedSOPInstanceUID = req.RequestedSOPInstanceUID;
		sess.writeMessage(rsp);
		new Thread(new SendStorageCommitEvent(AEMap.get(sess.getDestAE()), sess.getSourceAE(), sopSeq, transactionUID)).start();
		return true;
	}
}
