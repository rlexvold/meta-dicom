package net.metafusion.pdu;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class Abort extends PDU
{
	public Abort(DataInputStream is) throws Exception
	{
		super(A_ABORT, is);
		acme.util.Log.vvvlog("<<<<<<<<<<<<<<<<<<<<<<<<< A_ABORT PDU <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	public Abort(DataOutputStream os) throws Exception
	{
		super(A_ABORT, os);
		acme.util.Log.vvvlog(">>>>>>>>>>>>>>>>>>>>>>>>> A_ABORT PDU >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		os.write(new byte[] { 0, 0, 0, 0 }); // do we set source?
	}
}
