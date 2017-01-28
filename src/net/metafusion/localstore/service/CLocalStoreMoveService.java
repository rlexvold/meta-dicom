package net.metafusion.localstore.service;

import net.metafusion.Dicom;
import net.metafusion.dataset.DS;
import net.metafusion.localstore.DicomQuery;
import net.metafusion.localstore.DicomStore;
import net.metafusion.localstore.mirror.MirrorClient;
import net.metafusion.model.Image;
import net.metafusion.model.ServiceLogView;
import net.metafusion.model.StudyView;
import net.metafusion.msg.CMoveReq;
import net.metafusion.msg.CMoveRsp;
import net.metafusion.net.DicomClientSession;
import net.metafusion.net.DicomServerSession;
import net.metafusion.service.CStore;
import net.metafusion.util.AE;
import net.metafusion.util.AEMap;
import net.metafusion.util.Message;
import net.metafusion.util.RoleMap;
import net.metafusion.util.Tag;
import net.metafusion.util.UID;
import acme.util.Util;

public class CLocalStoreMoveService extends DicomServiceProvider
{
	DicomStore	store	= DicomStore.get();

	public CLocalStoreMoveService(DicomServerSession s) throws Exception
	{
		super(s, "CMoveService");
	}

	// This method is for handling CMove requests from other servers
	protected boolean handle(Message msg) throws Exception
	{
		CMoveReq req = new CMoveReq(msg);
		DS tags = msg.getDataSet();
		assert tags != null;
		// make sure we a image UID
		if (!tags.contains(Tag.SOPInstanceUID))
			tags.put(Tag.SOPInstanceUID, null);
		sess.logAccess("move-request", "");
		AE ae = AEMap.get(req.MoveDestination);
		if (serviceLog != null && ae != null)
			serviceLog.setDestAE(ae.getName());
		DicomClientSession clientSess = new DicomClientSession(RoleMap.getStoreUserRoleMap());
		boolean connected = clientSess.connect(AEMap.get(req.MoveDestination));
		// todo: betterhandling of connect errors
		DicomQuery iter = store.query(UID.get(req.getAffectedSOPClassUID()), Dicom.IMAGE_LEVEL, tags);
		try
		{
			int completed = 0;
			int failed = 0;
			while (connected && iter.hasNext())
			{
				DS ds = (DS) iter.next();
				ds.put(Tag.RetrieveAET, sess.getDestAE()); // not sure if this
				// needed
				// Util.log("WAIT 30000");
				// Util.sleep(30000);
				Image image = store.getImage(ds.getString(Tag.SOPInstanceUID));
				if (image.getStatus() == Image.STATUS_REMOTE)
				{
					String aet = StudyView.get().getOriginAET(image.getStudyID());
					Util.log("load image " + image.getImageUID() + " from " + aet);
					if (!MirrorClient.loadRemoteImage(aet, image))
					{
						Util.log("load image failed ) " + image.getImageUID() + " from " + aet);
						failed++;
						continue;
					}
				}
				CStore store = new CStore(clientSess, ds.getString(Tag.SOPInstanceUID), req.MoveDestination, req.getMessageID());
				store.run();
				if (store.getResult() == Dicom.SUCCESS)
					completed++;
				else
					failed++;
				if (iter.hasNext())
				{
					CMoveRsp rsp = new CMoveRsp();
					rsp.AffectedSOPClassUID = req.AffectedSOPClassUID;
					rsp.CommandField = (short) Dicom.C_MOVE_RSP;
					rsp.MessageIDToBeingRespondedTo = req.MessageID;
					rsp.DataSetType = (short) Dicom.COMMAND_DATASET_ABSENT;
					rsp.Status = (short) Dicom.PENDING;
					rsp.NumberOfRemainingSubOperations = 1;
					rsp.NumberOfCompletedSubOperations = (short) completed;
					rsp.NumberOfFailedSubOperations = (short) failed;
					rsp.NumberOfWarningSubOperations = 0;
					sess.writeMessage(rsp);
					// sess.writeMessage(rsp, ds);
				}
			}
			CMoveRsp srsp = new CMoveRsp();
			srsp.AffectedSOPClassUID = req.AffectedSOPClassUID;
			srsp.CommandField = (short) Dicom.C_MOVE_RSP;
			srsp.MessageIDToBeingRespondedTo = req.MessageID;
			srsp.DataSetType = (short) Dicom.COMMAND_DATASET_ABSENT;
			srsp.Status = (short) Dicom.SUCCESS;
			srsp.NumberOfRemainingSubOperations = 0;
			srsp.NumberOfCompletedSubOperations = (short) completed;
			srsp.NumberOfFailedSubOperations = (short) failed;
			srsp.NumberOfWarningSubOperations = 0;
			sess.writeMessage(srsp);
			clientSess.close(true);
			clientSess = null;
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (clientSess != null)
				clientSess.close(false);
			if (iter.hasNext())
			{
				// send the rest is efilm closed connection early
				DicomClientSession clientSess2 = new DicomClientSession(RoleMap.getStoreUserRoleMap());
				try
				{
					boolean connected2 = clientSess.connect(ae);
					while (connected2 && iter.hasNext())
					{
						DS ds = (DS) iter.next();
						ds.put(Tag.RetrieveAET, sess.getDestAE()); // not sure if
						// this needed
						Image image = store.getImage(ds.getString(Tag.SOPInstanceUID));
						if (image.getStatus() == Image.STATUS_REMOTE)
						{
							String aet = StudyView.get().getOriginAET(image.getStudyID());
							Util.log("load image (2) " + image.getImageUID() + " from " + aet);
							if (MirrorClient.loadRemoteImage(aet, image))
							{
								CStore store = new CStore(clientSess, ds.getString(Tag.SOPInstanceUID), req.MoveDestination, req.getMessageID());
								store.run();
							}
							else
								Util.log("load image failed (2) " + image.getImageUID() + " from " + aet);
						}
					}
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				finally
				{
					if (clientSess != null)
						clientSess.close(false);
				}
			}
		}
		return true;
	}
}
