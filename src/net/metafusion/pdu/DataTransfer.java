package net.metafusion.pdu;

import java.io.DataInputStream;
import net.metafusion.net.DicomSession;
import acme.util.Util;

public class DataTransfer extends PDU
{
	static void log(String s)
	{
		acme.util.Log.vlog(s);
	}
	public static final int CMD_MASK = 0x1, DONE_MASK = 0x2;

	public DataTransfer(DataInputStream is) throws Exception
	{
		super(P_DATA_TF, is);
		acme.util.Log.vvlog("<<<<<<<<<<<<<<<<<<<<<<<<< DataTransfer PDU <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		/*
		 * while (!b.atEnd()) { int len = b.getInt(); int id = b.getByte(); int
		 * flags = b.getByte(); if (b.getBytesLeft() < len-2) throw new
		 * Exception("DataBuffer too short for PDV"); Buffer data = new
		 * Buffer(b.getBuffer(), b.getPos(), len-2); b.skip(len-2); PDV pdv =
		 * new PDV(id, (flags&1)==1, (flags&2)==2, data); Log("add "+pdv);
		 * pdvList.add(pdv); }
		 */
	}

	public PDV nextPDV() throws Exception
	{
		byte lenBytes[] = new byte[4];
		for (int i = 0; i < 4; i++)
		{
			int b = is.read();
			if (b == -1) return null;
			lenBytes[i] = (byte) b;
		}
		int len = Util.decodeInt(lenBytes, 0);
		if (len < 0 || len > DicomSession.InternalMaxPDU) throw new Exception("bad pdv len (9/28/2007) " + len);
		int id = is.read();
		int flags = is.read();
		byte data[] = new byte[len - 2];
		is.readFully(data);
		PDV pdv = new PDV(id, (flags & CMD_MASK) == CMD_MASK, (flags & DONE_MASK) == DONE_MASK, data);
		acme.util.Log.vvlog("read PDV " + pdv);
		return pdv;
	}
	// simplify, one cmd and one data ---------
	/*
	 * public DataTransfer(Buffer b) { super(P_DATA_TF, new Buffer(4));
	 * buffer.addBytes(new byte[] { 0,0,0,0 }); }
	 * 
	 * class PDV { PDV(int index, int id, boolean cmd, boolean done, Buffer b) {
	 * this.index = index; this.id = id; this.cmd = cmd; this.done = done;
	 * this.b = b; } int index; int id; boolean done; boolean cmd; Buffer b; }
	 * 
	 * int index = 0; int lastID = -1;
	 * 
	 * class Cmd { Cmd(int id, Buffer b) { this.id = id; this.b = b; } int id;
	 * Buffer b; void parseData() { b.setBigEndian(false); } long
	 * getTagValue(int type, int v) { return 0; } } class Message { Message(int
	 * id, Buffer b) { this.id = id; this.b = b; } int id; Buffer b; }
	 * 
	 * Cmd first; Cmd last; List cmdList = new ArrayList(); List dataList = new
	 * ArrayList();
	 * 
	 * boolean cmdPhase = true; boolean dataPhase = false; boolean hasData =
	 * false;
	 * 
	 * boolean addBuffer(byte[] buffer) { Buffer b = new Buffer(buffer); int pos =
	 * 0; for (;;) { int pdvLen = b.getInt(); if (b.getPos()+pdvLen >=
	 * b.getLength()) { int id = b.getByte(); int flags = b.getByte(); boolean
	 * isData = (flags & 1) == 1; boolean isDone = (flags & 2) == 2; pos =
	 * b.getPos(); if (cmdPhase) { if (isData) throw new
	 * RuntimeException("Message in command phase"); boolean newCmd = last!=null ||
	 * last.id == id; if (newCmd) { Cmd cmd = new Cmd(id, new Buffer(buffer,
	 * b.getPos(), pdvLen)); if (first == null) first = cmd; last = cmd;
	 * cmdList.add(cmd); } else last.b = new Buffer(last.b, buffer, pos,
	 * pdvLen); if (isDone) { cmdPhase = false; Iterator iter =
	 * cmdList.iterator(); while (iter.hasNext()) { Cmd c = (Cmd)iter.next();
	 * c.parseData(); } // ?????????????????????????????? // !!!!!! dataPhase =
	 * first.getTagValue(Tag.DataSetType.,0x0101) != 0x0101; //
	 * ??????????????????????????????? } } else if (dataPhase) { Message data =
	 * new Message(type, new Buffer(buffer, b.getPos(), pdvLen));
	 * dataList.add(data); dataPhase = !isDone; } pos += pdvLen; } break; }
	 * return !cmdPhase && !dataPhase; }
	 * 
	 * public void write(XDataOutputStream os) { // if not data byte[] b = new
	 * byte[] { (byte)type, 0, 0, 0, 0, 0 }; Util.encodeInt(buffer.getLength(),
	 * b, 2); os.writeBytes(b); os.writeBytes(buffer.getBuffer(),
	 * buffer.getStart(), buffer.getLength()); }
	 * 
	 */
}
