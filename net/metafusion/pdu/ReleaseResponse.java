package net.metafusion.pdu;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ReleaseResponse extends PDU
{
	public ReleaseResponse(DataInputStream is) throws Exception
	{
		super(A_RELEASE_RP, is);
		acme.util.Log.vvlog("<<<<<<<<<<<<<<<<<<<<<<<<< ReleaseResponse PDU <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	public ReleaseResponse(DataOutputStream os) throws Exception
	{
		super(A_RELEASE_RP, os);
		acme.util.Log.vvlog(">>>>>>>>>>>>>>>>>>>>>>>>> ReleaseResponse PDU >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		os.write(new byte[] { 0, 0, 0, 0 });
	}
}
