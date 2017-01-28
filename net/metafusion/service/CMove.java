package net.metafusion.service;

import java.util.ArrayList;

import net.metafusion.Dicom;
import net.metafusion.dataset.DS;
import net.metafusion.msg.CMoveReq;
import net.metafusion.net.DicomClientSession;
import net.metafusion.util.AEMap;
import net.metafusion.util.Message;
import net.metafusion.util.RoleMap;
import net.metafusion.util.Tag;
import net.metafusion.util.UID;
import acme.util.Util;

public class CMove extends DicomClientService
{
	public static DS buildSearch(Tag key, String value)
	{
		DS ds = new DS();
		Tag tags[] = {
				Tag.PatientName,
				Tag.PatientID,
				Tag.StudyDate,
				Tag.StudyTime,
				Tag.AccessionNumber,
				Tag.StudyID,
				Tag.StudyInstanceUID,
				Tag.Modality,
				Tag.SeriesInstanceUID,
				Tag.SeriesNumber,
				Tag.ImageType,
				Tag.SOPInstanceUID,
				Tag.SOPClassUID,
				Tag.InstanceNumber };
		putTagList(ds, tags, key, value != null ? value.toUpperCase() : null);
		return ds;
	}

	public static void putTagList(DS ds, Tag list[], Tag key, String value)
	{
		for (int i = 0; i < list.length; i++)
			ds.put(list[i], key.equals(list[i]) ? value : null);
	}
	private DicomClientSession	sess	= null;

	public CMove()
	{
		super("CMove", null);
	}

	public DicomClientSession getSess()
	{
		return sess;
	}

	public int runit() throws Exception
	{
		if (sess == null)
			sess = new DicomClientSession(RoleMap.getClientRoleMap());
		sess.connect(AEMap.get(sourceAE));
		CMoveReq moveReq = new CMoveReq();
		moveReq.CommandField = Dicom.C_MOVE_RQ;
		moveReq.MessageID = sess.getNextMsgID();
		moveReq.Priority = Dicom.MEDIUM;
		moveReq.AffectedSOPClassUID = UID.StudyRootQueryRetrieveInformationModelMOVE.getUID();
		moveReq.MoveDestination = destAE;
		moveReq.DataSetType = (short) Dicom.COMMAND_DATASET_PRESENT;
		Message req = new Message(sess, moveReq, attrSet);
		sess.writeMessage(req);
		Message resp;
		for (;;)
		{
			resp = sess.readMessage();
			if (resp == null)
			{
				Util.log("FAIL: no response");
				break;
			}
			Util.log("cmd=" + resp.getCmd().toString());
			if (resp.getStatus() == Dicom.PENDING)
			{
				// Util.log("have resp " + resp.getDataSet().toString());
				// results.add(resp.getDataSet());
			}
			else if (resp.getStatus() == Dicom.SUCCESS)
			{
				Util.log("done");
				break;
			}
			else
			{
				Util.log("error " + resp.getStatus());
				break;
			}
		}
		sess.close(true);
		return resp != null ? resp.getStatus() : -1;
	}

	public void setSess(DicomClientSession sess)
	{
		this.sess = sess;
	}
}
