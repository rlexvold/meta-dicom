package net.metafusion.localstore.service;

import net.metafusion.Dicom;
import net.metafusion.msg.CEchoReq;
import net.metafusion.msg.CEchoRsp;
import net.metafusion.net.DicomServerSession;
import net.metafusion.util.Message;
import net.metafusion.util.UID;

public class CLocalStoreEchoService extends DicomServiceProvider
{
	public CLocalStoreEchoService(DicomServerSession s) throws Exception
	{
		super(s, "CEcho");
	}

	protected boolean handle(Message msg) throws Exception
	{
		CEchoReq req = new CEchoReq(msg);
		CEchoRsp rsp = new CEchoRsp();
		sess.logAccess("echo", "");
		rsp.AffectedSOPClassUID = UID.Verification.getUID();
		rsp.CommandField = (short) 0x8030;
		rsp.MessageIDToBeingRespondedTo = req.MessageID;
		rsp.DataSetType = Dicom.COMMAND_DATASET_ABSENT;
		rsp.Status = Dicom.SUCCESS;
		sess.writeMessage(rsp);
		return true;
	}
}
