package net.metafusion.pdu;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class AssociateReject extends PDU
{
	static void log(String s)
	{
		acme.util.Log.vvvlog(s);
	}
	int result;
	int source;
	int reason;
	public static final int RESULT_PERMANENT = 1;
	public static final int RESULT_TRAMSIENT = 2;
	public static final int REASON_NO_REASON_GIVEN = 1;
	public static final int REASON_APPLICATION_CONTEXT_NOT_SUPPORTED = 2;
	public static final int REASON_CALLING_AET_NOT_RECOGNIZED = 3;
	public static final int REASON_CALLED_AET_NOT_RECOGNIZED = 7;

	public AssociateReject(DataInputStream is) throws Exception
	{
		super(A_ASSOCIATE_RJ, is);
		acme.util.Log.vvvlog("<<<<<<<<<<<<<<<<<<<<<<<<< AssociateReject PDU <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		is.skip(1);
		result = is.read();
		source = is.read();
		reason = is.read();
		acme.util.Stats.inc("new.AssociateReject");
	}

	public AssociateReject(DataOutputStream os, int result, int source, int reason) throws Exception
	{
		super(A_ASSOCIATE_RJ, os);
		acme.util.Log.vvvlog(">>>>>>>>>>>>>>>>>>>>>>>>> AssociateReject PDU >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		acme.util.Log.vvvlog("write result=" + result + " source" + source + " reason=" + reason);
		os.write(new byte[] { 0, (byte) result, (byte) source, (byte) reason });
	}
}
