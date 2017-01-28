package net.metafusion.localstore.service;

import net.metafusion.Dicom;
import net.metafusion.dataset.DS;
import net.metafusion.localstore.DicomStore;
import net.metafusion.model.Worklist;
import net.metafusion.msg.NActionReq;
import net.metafusion.msg.NActionRsp;
import net.metafusion.net.DicomServerSession;
import net.metafusion.util.Message;
import net.metafusion.util.Tag;
import acme.util.Log;

public class CLocalStoreSetService extends DicomServiceProvider
{
	static void log(String s)
	{
		Log.log(s);
	}
	DicomStore store = DicomStore.get();

	public CLocalStoreSetService(DicomServerSession s) throws Exception
	{
		super(s, "SetService");
	}

	protected boolean handle(Message msg) throws Exception
	{
		NActionReq req = new NActionReq(msg);
		DS cmd = msg.getCmd();
		DS tags = msg.getDataSet();
		// assert tags != null;
		// assert req.ActionTypeID == 1;
		// sess.logAccess("commit","");
		log("CLocalStoreSetService got " + msg.getDataSet());
		Worklist.processSetPerformedProcedureStep(cmd.getString(Tag.RequestedSOPClassUID), tags);
		NActionRsp rsp = new NActionRsp();
		rsp.AffectedSOPClassUID = cmd.getString(Tag.RequestedSOPClassUID);
		rsp.CommandField = (short) Dicom.N_SET_RSP;
		rsp.MessageIDToBeingRespondedTo = req.MessageID;
		rsp.DataSetType = (short) Dicom.COMMAND_DATASET_ABSENT;
		rsp.Status = (short) Dicom.SUCCESS;
		rsp.AffectedSOPInstanceUID = cmd.getString(Tag.RequestedSOPInstanceUID);
		sess.writeMessage(rsp);
		return true;
	}
}
