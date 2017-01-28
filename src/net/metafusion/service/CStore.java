package net.metafusion.service;

import java.io.InputStream;

import net.metafusion.Dicom;
import net.metafusion.localstore.DicomStore;
import net.metafusion.model.Image;
import net.metafusion.model.Study;
import net.metafusion.model.StudyView;
import net.metafusion.msg.CStoreReq;
import net.metafusion.msg.CStoreReqForMove;
import net.metafusion.net.DicomSession;
import net.metafusion.util.Message;
import acme.util.Log;
import acme.util.Util;

public class CStore extends DicomClientService
{
	Image		image;
	InputStream	inputStream;
	String		reqAET;
	short		reqMsgId;
	Message		resp;
	String		sopInstanceUID;
	DicomStore	store	= DicomStore.get();

	public CStore(DicomSession s, Image image) throws Exception
	{
		super("CStore", s);
		this.sopInstanceUID = null;
		this.reqAET = null;
		this.reqMsgId = 0;
		this.image = image;
	}

	public CStore(DicomSession s, Image image, InputStream is) throws Exception
	{
		super("CStore", s);
		this.sopInstanceUID = null;
		this.reqAET = null;
		this.reqMsgId = 0;
		this.image = image;
		this.inputStream = is;
	}

	public CStore(DicomSession s, String sopInstanceUID, String reqAET, short reqMsgId) throws Exception
	{
		super("CStore", s);
		this.sopInstanceUID = sopInstanceUID;
		this.reqAET = reqAET;
		this.reqMsgId = reqMsgId;
	}

	public Message getResp()
	{
		return resp;
	}

	private void serviceLogSetUID()
	{
		if (serviceLog != null)
		{
			try
			{
				Study study = StudyView.get().selectByID(image.getStudyID());
				serviceLog.setStudyuid(study.studyUID);
				if (reqAET != null)
					serviceLog.setDestAE(reqAET);
			}
			catch (Exception e)
			{
				Log.log("Error trying to set StudyUID in dcm_service_log: " + e.getMessage());
			}
		}
	}

	protected int runit() throws Exception
	{
		InputStream is = null;
		if (image == null)
			image = store.getImage(sopInstanceUID);
		if (image == null)
			throw new Exception("could not find sopInstance " + sopInstanceUID);
		serviceLogSetUID();
		try
		{
			sess.logAccess("store-to", "SOPInstanceUID=" + sopInstanceUID);
			if (inputStream == null)
				is = store.getImageStream(image);
			else
				is = inputStream;
			if (reqAET != null)
			{
				CStoreReqForMove req = new CStoreReqForMove();
				req.AffectedSOPClassUID = image.classUID;
				req.AffectedSOPInstanceUID = image.imageUID;
				req.Priority = Dicom.MEDIUM;
				req.CommandField = Dicom.C_STORE_RQ;
				req.MessageID = sess.getNextMsgID();
				req.DataSetType = (short) Dicom.COMMAND_DATASET_PRESENT;
				req.MoveOriginatorAET = reqAET;
				req.MoveOriginatorMessageID = reqMsgId;
				sess.writeMessage(req, is);
			}
			else
			{
				CStoreReq req = new CStoreReq();
				req.AffectedSOPClassUID = image.classUID;
				req.AffectedSOPInstanceUID = image.imageUID;
				req.Priority = Dicom.MEDIUM;
				req.CommandField = Dicom.C_STORE_RQ;
				req.MessageID = sess.getNextMsgID();
				req.DataSetType = (short) Dicom.COMMAND_DATASET_PRESENT;
				sess.writeMessage(req, is);
			}
			resp = sess.readMessage();
			if (resp == null)
				return -1;
			assert resp.getCommandID() == Dicom.C_STORE_RSP;
			return resp.getStatus();
		}
		finally
		{
			Util.safeClose(is);
		}
	}
}
