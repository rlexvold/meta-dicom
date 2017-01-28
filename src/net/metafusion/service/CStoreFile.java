package net.metafusion.service;

import java.io.File;
import net.metafusion.Dicom;
import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSInputStream;
import net.metafusion.msg.CStoreReq;
import net.metafusion.net.DicomSession;
import net.metafusion.util.Message;
import net.metafusion.util.Tag;
import net.metafusion.util.UID;

public class CStoreFile extends DicomClientService
{
	public CStoreFile(DicomSession s, File f) throws Exception
	{
		super("CStoreFile", s);
		this.f = f;
	}
	File f;
	Message resp;

	public Message getResp()
	{
		return resp;
	}

	UID getSOPClass(DS ds)
	{
		String s = ds.getString(Tag.MediaStorageSOPClassUID);
		if (s == null) s = ds.getString(Tag.SOPClassUID);
		return s != null ? UID.get(s) : null;
	}

	UID getUID(DS ds)
	{
		String s = ds.getString(Tag.SOPInstanceUID);
		return s != null ? UID.get(s) : null;
	}

	protected int runit() throws Exception
	{
		DS ds = DSInputStream.readFileAndImages(f);
		CStoreReq req = new CStoreReq();
		req.AffectedSOPClassUID = getSOPClass(ds).getUID();
		req.AffectedSOPInstanceUID = getUID(ds).getUID();
		req.Priority = Dicom.MEDIUM;
		req.CommandField = Dicom.C_STORE_RQ;
		req.MessageID = sess.getNextMsgID();
		req.DataSetType = (short) Dicom.COMMAND_DATASET_PRESENT;
		sess.writeMessage(req, ds);
		resp = sess.readMessage();
		if (resp == null) return -1;
		assert resp.getCommandID() == Dicom.C_STORE_RSP;
		return resp.getStatus();
	}
}
