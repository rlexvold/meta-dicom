package net.metafusion.localstore.service;

import java.util.Iterator;
import java.util.List;
import net.metafusion.Dicom;
import net.metafusion.dataset.DS;
import net.metafusion.localstore.DicomQuery;
import net.metafusion.localstore.DicomStore;
import net.metafusion.model.Worklist;
import net.metafusion.msg.CFindReq;
import net.metafusion.msg.CFindRsp;
import net.metafusion.net.DicomServerSession;
import net.metafusion.util.Message;
import net.metafusion.util.Tag;
import net.metafusion.util.UID;
import acme.util.Util;

public class CLocalStoreFindService extends DicomServiceProvider
{
	DicomStore	store	= DicomStore.get();

	public CLocalStoreFindService(DicomServerSession s) throws Exception
	{
		super(s, "CFindService");
	}

	boolean handleWorklist(Message msg) throws Exception
	{
		CFindReq req = new CFindReq(msg);
		DS tags = msg.getDataSet();
		assert tags != null;
		// sess.logAccess("find-request","");
		String aet = sess.getDestAE();
		List list = Worklist.queryWorklist(aet);
		Iterator iter = list.iterator();
		while (iter.hasNext())
		{
			DS ds = (DS) iter.next();
			ds.put(Tag.RetrieveAET, sess.getDestAE());
			Util.log("wlds: " + ds);
			CFindRsp rsp = new CFindRsp();
			rsp.AffectedSOPClassUID = req.AffectedSOPClassUID;
			rsp.CommandField = (short) Dicom.C_FIND_RSP;
			rsp.MessageIDToBeingRespondedTo = req.MessageID;
			rsp.DataSetType = (short) Dicom.COMMAND_DATASET_PRESENT;
			rsp.Status = (short) Dicom.PENDING;
			sess.writeMessage(rsp, ds);
		}
		CFindRsp srsp = new CFindRsp();
		srsp.AffectedSOPClassUID = req.AffectedSOPClassUID;
		srsp.CommandField = (short) Dicom.C_FIND_RSP;
		srsp.MessageIDToBeingRespondedTo = req.MessageID;
		srsp.DataSetType = (short) Dicom.COMMAND_DATASET_ABSENT;
		srsp.Status = (short) Dicom.SUCCESS;
		sess.writeMessage(srsp);
		return true;
	}

	protected boolean handle(Message msg) throws Exception
	{
		CFindReq req = new CFindReq(msg);
		DS tags = msg.getDataSet();
		assert tags != null;
		sess.logAccess("find-request", "");
		if (req.getAffectedSOPClassUID().equals(UID.ModalityWorklistInformationModelFIND.getUID()))
		{
			return handleWorklist(msg);
		}
		DicomQuery iter = store.query(UID.get(req.getAffectedSOPClassUID()), tags.getString(Tag.QueryRetrieveLevel), tags);
		while (iter.hasNext())
		{
			DS ds = (DS) iter.next();
			ds.put(Tag.RetrieveAET, sess.getDestAE()); // not sure if this needed
			CFindRsp rsp = new CFindRsp();
			rsp.AffectedSOPClassUID = req.AffectedSOPClassUID;
			rsp.CommandField = (short) Dicom.C_FIND_RSP;
			rsp.MessageIDToBeingRespondedTo = req.MessageID;
			rsp.DataSetType = (short) Dicom.COMMAND_DATASET_PRESENT;
			rsp.Status = (short) Dicom.PENDING;
			sess.logAccess("find", "StudyInstanceUID=" + ds.getString(Tag.StudyInstanceUID));
			sess.writeMessage(rsp, ds);
		}
		CFindRsp srsp = new CFindRsp();
		srsp.AffectedSOPClassUID = req.AffectedSOPClassUID;
		srsp.CommandField = (short) Dicom.C_FIND_RSP;
		srsp.MessageIDToBeingRespondedTo = req.MessageID;
		srsp.DataSetType = (short) Dicom.COMMAND_DATASET_ABSENT;
		srsp.Status = (short) Dicom.SUCCESS;
		sess.writeMessage(srsp);
		return true;
	}
}
