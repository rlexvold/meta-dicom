package net.metafusion.pdu;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ReleaseRequest extends PDU
{
	public ReleaseRequest(DataInputStream is) throws Exception
	{
		super(A_RELEASE_RQ, is);
		acme.util.Log.vvlog("<<<<<<<<<<<<<<<<<<<<<<<<< ReleaseRequest PDU <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	public ReleaseRequest(DataOutputStream os) throws Exception
	{
		super(A_RELEASE_RQ, os);
		acme.util.Log.vvlog(">>>>>>>>>>>>>>>>>>>>>>>>> ReleaseRequest PDU >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		os.write(new byte[] { 0, 0, 0, 0 });
	}
}
