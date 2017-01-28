package net.metafusion.pdu;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import net.metafusion.Dicom;

public class PDU
{
	// Constants -----------------------------------------------------
	public static final int C_STORE_RQ = 0x0001;
	public static final int C_STORE_RSP = 0x8001;
	public static final int C_GET_RQ = 0x0010;
	public static final int C_GET_RSP = 0x8010;
	public static final int C_FIND_RQ = 0x0020;
	public static final int C_FIND_RSP = 0x8020;
	public static final int C_MOVE_RQ = 0x0021;
	public static final int C_MOVE_RSP = 0x8021;
	public static final int C_ECHO_RQ = 0x0030;
	public static final int C_ECHO_RSP = 0x8030;
	public static final int N_EVENT_REPORT_RQ = 0x0100;
	public static final int N_EVENT_REPORT_RSP = 0x8100;
	public static final int N_GET_RQ = 0x0110;
	public static final int N_GET_RSP = 0x8110;
	public static final int N_SET_RQ = 0x0120;
	public static final int N_SET_RSP = 0x8120;
	public static final int N_ACTION_RQ = 0x0130;
	public static final int N_ACTION_RSP = 0x8130;
	public static final int N_CREATE_RQ = 0x0140;
	public static final int N_CREATE_RSP = 0x8140;
	public static final int N_DELETE_RQ = 0x0150;
	public static final int N_DELETE_RSP = 0x8150;
	public static final int C_CANCEL_RQ = 0x0FFF;
	public static final int MEDIUM = 0x0000;
	public static final int HIGH = 0x0001;
	public static final int LOW = 0x0002;
	public static final int NO_DATASET = 0x0101;
	public static final int A_ASSOCIATE_RQ = 1;
	public static final int A_ASSOCIATE_AC = 2;
	public static final int A_ASSOCIATE_RJ = 3;
	public static final int P_DATA_TF = 4;
	public static final int A_RELEASE_RQ = 5;
	public static final int A_RELEASE_RP = 6;
	public static final int A_ABORT = 7;
	boolean raw = false;

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}
	int type;
	byte bytes[];
	/*
	 * PDU(int type) { this.type = type; buffer = new Buffer(8192);
	 * buffer.setBigEndian(true); } PDU(int type, int pduLen) { this.type =
	 * type; this.pduLen = pduLen; }
	 */
	DataInputStream is;
	DataOutputStream os;

	public PDU()
	{
		raw = true;
	}

	public boolean isRaw()
	{
		return raw;
	}

	PDU(int type, DataInputStream is)
	{
		this.type = type;
		this.is = is;
		acme.util.Stats.inc("new.PDU");
	}

	PDU(int type, DataOutputStream os)
	{
		this.type = type;
		this.os = os;
		acme.util.Stats.inc("new.PDU");
	}

	String getString(DataInputStream is, int len) throws IOException
	{
		byte b[] = new byte[len];
		is.readFully(b);
		return new String(b).trim();
	}

	String getShortString(DataInputStream is) throws IOException
	{
		int len = is.readShort();
		byte b[] = new byte[len];
		is.readFully(b);
		return new String(b).trim();
	}

	void addString(DataOutputStream os, String s, int len) throws Exception
	{
		byte b[] = s.getBytes();
		if (b.length >= len)
			os.write(b, 0, len);
		else
		{
			os.write(b);
			for (int i = b.length; i < len; i++)
				os.write(' ');
		}
	}

	private int getTypedStringSize(String s)
	{
		return (s.length() & 0x1) == 1 ? s.length() + 1 : s.length();
	}

	void addTypedString(DataOutputStream os, int type, String s) throws Exception
	{
		os.write((byte) type);
		os.write((byte) 0);
		// if ((s.length() & 0x1)==1) {
		// Util.log("!!!!!!!!!!!!!!!!!!!!!!!!!! PAD PAD");
		// os.writeShort((short)(s.length()+1));
		// os.write(s.getBytes());
		// os.write(' ');
		// } else
		{
			os.writeShort((short) s.length());
			os.write(s.getBytes());
		}
	} // ??? pad this
	static HashMap msgName = new HashMap();
	static
	{
		msgName.put(new Integer(0x0001), "C_STORE_RQ");
		msgName.put(new Integer(0x8001), "C_STORE_RSP");
		msgName.put(new Integer(0x0010), "C_GET_RQ");
		msgName.put(new Integer(0x8010), "C_GET_RSP");
		msgName.put(new Integer(0x0020), "C_FIND_RQ");
		msgName.put(new Integer(0x8020), "C_FIND_RSP");
		msgName.put(new Integer(0x0021), "C_MOVE_RQ");
		msgName.put(new Integer(0x8021), "C_MOVE_RSP");
		msgName.put(new Integer(0x0030), "C_ECHO_RQ");
		msgName.put(new Integer(0x8030), "C_ECHO_RSP");
		msgName.put(new Integer(0x0100), "N_EVENT_REPORT_RQ");
		msgName.put(new Integer(0x8100), "N_EVENT_REPORT_RSP");
		msgName.put(new Integer(0x0110), "N_GET_RQ");
		msgName.put(new Integer(0x8110), "N_GET_RSP");
		msgName.put(new Integer(0x0120), "N_SET_RQ");
		msgName.put(new Integer(0x8120), "N_SET_RSP");
		msgName.put(new Integer(0x0130), "N_ACTION_RQ");
		msgName.put(new Integer(0x8130), "N_ACTION_RSP");
		msgName.put(new Integer(0x0140), "N_CREATE_RQ");
		msgName.put(new Integer(0x8140), "N_CREATE_RSP");
		msgName.put(new Integer(0x0150), "N_DELETE_RQ");
		msgName.put(new Integer(0x8150), "N_DELETE_RSP");
		msgName.put(new Integer(0x0FFF), "C_CANCEL_RQ");
	};

	public static String getMsgName(int msg)
	{
		String s = (String) msgName.get(new Integer(msg));
		if (s != null) return s;
		return "Invalid Msg " + msg;
	}
	static String PDU_NAME[] = { "BAD_PDU_0", "A_ASSOCIATE_RQ", "A_ASSOCIATE_AC", "A_ASSOCIATE_RJ", "P_DATA_TF", "A_RELEASE_RQ", "A_RELEASE_RP", "A_ABORT" };

	public static String getPDUName(int pdu)
	{
		if (pdu <= A_ABORT && pdu >= 1) return PDU_NAME[pdu];
		return "Invalid PDU " + pdu;
	}

	public static PDU getPDU(int type, byte[] b, int len) throws Exception
	{
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(b, 0, len));
		PDU pdu = null;
		switch (type)
		{
			case Dicom.A_ASSOCIATE_RQ:
				pdu = new AssociateRequest(dis);
				break;
			case Dicom.A_ASSOCIATE_AC:
				pdu = new AssociateAccept(dis);
				break;
			case Dicom.A_ASSOCIATE_RJ:
				pdu = new AssociateReject(dis);
				break;
			case Dicom.P_DATA_TF:
				pdu = new DataTransfer(dis);
				break;
			case Dicom.A_RELEASE_RQ:
				pdu = new ReleaseRequest(dis);
				break;
			case Dicom.A_RELEASE_RP:
				pdu = new ReleaseResponse(dis);
				break;
			case Dicom.A_ABORT:
				pdu = new Abort(dis);
				break;
			default:
				throw new Exception("bad PDU Type " + type);
		}
		return pdu;
	}
}
