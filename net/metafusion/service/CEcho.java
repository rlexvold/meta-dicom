package net.metafusion.service;

import net.metafusion.Dicom;
import net.metafusion.msg.CEchoReq;
import net.metafusion.net.DicomSession;
import net.metafusion.util.Message;
import net.metafusion.util.UID;

public class CEcho extends DicomClientService
{
	public CEcho(DicomSession s) throws Exception
	{
		super("CEcho", s);
	}

	public int runit() throws Exception
	{
		CEchoReq echo = new CEchoReq();
		echo.AffectedSOPClassUID = UID.Verification.getUID();
		echo.CommandField = Dicom.C_ECHO_RQ;
		echo.MessageID = sess.getNextMsgID();
		echo.DataSetType = Dicom.COMMAND_DATASET_ABSENT;
		sess.writeMessage(echo);
		Message resp = sess.readMessage();
		assert resp.getCommandID() == Dicom.C_ECHO_RSP;
		return resp.getStatus();
	}
}
