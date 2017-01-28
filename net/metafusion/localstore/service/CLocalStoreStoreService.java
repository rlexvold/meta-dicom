package net.metafusion.localstore.service;

import java.io.File;
import java.util.Date;

import net.metafusion.Dicom;
import net.metafusion.localstore.DicomStore;
import net.metafusion.localstore.LocalStore;
import net.metafusion.localstore.LocalStoreDicomServer;
import net.metafusion.model.Image;
import net.metafusion.model.ImageView;
import net.metafusion.model.Study;
import net.metafusion.model.StudyView;
import net.metafusion.msg.CStoreReq;
import net.metafusion.msg.CStoreRsp;
import net.metafusion.net.DicomServerSession;
import net.metafusion.simulator.Simulator;
import net.metafusion.util.AEMap;
import net.metafusion.util.ImageMetaInfo;
import net.metafusion.util.Message;
import net.metafusion.util.Tag;
import net.metafusion.util.UID;
import acme.storage.SSStore;
import acme.util.Log;
import acme.util.Util;

public class CLocalStoreStoreService extends DicomServiceProvider
{
	LocalStoreDicomServer	server;

	public CLocalStoreStoreService(DicomServerSession s) throws Exception
	{
		super(s, "CStoreService");
		server = (LocalStoreDicomServer) sess.getServer();
		// store = server.getStore();
	}
	DicomStore	store	= DicomStore.get();

	// SSLocalStore store;
	ImageMetaInfo getMeta(UID xferSyntax, CStoreReq storeReq, long size)
	{
		ImageMetaInfo imi = new ImageMetaInfo();
		imi.setMediaStorageSOPClassUID(storeReq.AffectedSOPClassUID);
		imi.setMediaStorageSOPInstanceUID(storeReq.AffectedSOPInstanceUID);
		imi.setTransferSyntax(xferSyntax.getUID());
		imi.setSCP_AE(sess.getSourceAE());
		imi.setSCU_AE(sess.getDestAE());
		imi.setDataCreateTime(new Date().toString());
		return imi;
	}

	protected boolean handle(Message msg) throws Exception
	{
		CStoreReq req = new CStoreReq(msg);
		File f = msg.getDataFile();
		assert f != null;
		if (SSStore.get().isExpired())
		{
			throw new RuntimeException("Server has expired");
		}
		if (LocalStore.get().IsSink())
		{
			Util.log("sink: dropping " + req.AffectedSOPInstanceUID);
		}
		else
		{
			ImageMetaInfo imi = getMeta(sess.getContextMap()[msg.getPresContextID()].getSyntax(), req, f.length() - SSStore.METADATA_SIZE);
			sess.logAccess("store", "SOPInstanceUID=" + req.AffectedSOPInstanceUID);
			try
			{
				String studyUID = store.putWithRulesReturnStudyUID(imi, f, sess.getDestAE());
				try
				{
					if (studyUID != null)
						serviceLog.setStudyuid(studyUID);
				}
				catch (Exception e)
				{
					Log.log("Error trying to set StudyUID in CLocalStoreStoreService: " + e.getMessage());
				}
			}
			catch (Exception e)
			{
				Log.log("CLocalStoreStoreService putWithRules exception: " + e.getMessage());
				Log.aLog("Problem storing file, UID: " + req.AffectedSOPInstanceUID + " ERROR: " + e.getMessage());
				CStoreRsp rsp = new CStoreRsp();
				rsp.AffectedSOPClassUID = req.AffectedSOPClassUID;
				rsp.AffectedSOPInstanceUID = req.AffectedSOPInstanceUID;
				rsp.CommandField = (short) Dicom.C_STORE_RSP;
				rsp.MessageIDToBeingRespondedTo = (short) req.MessageID;
				rsp.DataSetType = Dicom.COMMAND_DATASET_ABSENT;
				rsp.ErrorComment = e.getMessage();
				rsp.Status = Dicom.FAIL;
				sess.writeMessage(rsp);
				throw e;
			}
			if (LocalStore.get().IsEcho())
			{
				Image i = ImageView.get().selectByID(imi.getImageID());
				Simulator.get().addImage(i);
			}
		}
		Util.safeDelete(f);
		CStoreRsp rsp = new CStoreRsp();
		rsp.AffectedSOPClassUID = req.AffectedSOPClassUID;
		rsp.AffectedSOPInstanceUID = req.AffectedSOPInstanceUID;
		rsp.CommandField = (short) Dicom.C_STORE_RSP;
		rsp.MessageIDToBeingRespondedTo = (short) req.MessageID;
		rsp.DataSetType = Dicom.COMMAND_DATASET_ABSENT;
		rsp.Status = Dicom.SUCCESS;
		sess.writeMessage(rsp);
		return true;
	}

	synchronized void test()
	{
	}
}
