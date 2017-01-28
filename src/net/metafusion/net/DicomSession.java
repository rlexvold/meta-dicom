package net.metafusion.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Iterator;

import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSInputStream;
import net.metafusion.msg.Cmd;
import net.metafusion.pdu.Abort;
import net.metafusion.pdu.AssociateAccept;
import net.metafusion.pdu.AssociateReject;
import net.metafusion.pdu.AssociateRequest;
import net.metafusion.pdu.DataTransfer;
import net.metafusion.pdu.PDU;
import net.metafusion.pdu.ReleaseRequest;
import net.metafusion.pdu.ReleaseResponse;
import net.metafusion.util.Message;
import net.metafusion.util.Role;
import net.metafusion.util.RoleMap;
import net.metafusion.util.Tag;
import net.metafusion.util.UID;
import acme.storage.SSStore;
import acme.util.CompressionStream;
import acme.util.Log;
import acme.util.TSLog;
import acme.util.Util;
import acme.util.XByteArrayInputStream;
import acme.util.XByteArrayOutputStream;

/*
 * Low-level communication protocol to support DICOM spec.  Communicates on PDU level
 */
public class DicomSession
{
	public static int				InternalMaxPDU			= 512 * 1024;
	private String					sourceAE				= null;
	private boolean					compressDataFlag		= false;
	public Role						contextMap[]			= new Role[256];
	protected DataInputStream		dis;
	protected DataOutputStream		dos;
	protected InputStream			is;
	protected OutputStream			os;
	protected boolean				isConnected				= false;
	protected RoleMap				maximalRoleMap;
	private int						maxPDU					= InternalMaxPDU;
	private int						nextMsgID				= 1;
	private byte[]					pduBytes				= new byte[InternalMaxPDU];
	private XByteArrayInputStream	pduBAIS					= new XByteArrayInputStream(pduBytes);
	private XByteArrayOutputStream	pduBAOS					= new XByteArrayOutputStream(pduBytes);
	private String					destAE					= null;
	protected InetSocketAddress		remoteInetSocketAddress	= new InetSocketAddress(0);
	protected RoleMap				roleMap					= new RoleMap();
	protected Socket				s;

	DicomSession(RoleMap maximalRoleMap)
	{
		setMaxPDU(maxPDU);
		this.maximalRoleMap = maximalRoleMap;
	}

	public String getSourceAE()
	{
		return sourceAE;
	}

	public void setSourceAE(String sourceAE)
	{
		this.sourceAE = sourceAE;
	}

	DicomSession(Socket s, RoleMap maximalRoleMap)
	{
		setMaxPDU(maxPDU);
		this.s = s;
		this.maximalRoleMap = maximalRoleMap;
	}

	public void close(boolean orderly)
	{
		if (orderly)
		{
			try
			{
				release();
			}
			catch (Exception e)
			{
			}
		}
		Util.safeClose(dis);
		Util.safeClose(dos);
		Util.safeClose(is);
		Util.safeClose(os);
		Util.safeClose(s);
		is = null;
		os = null;
		dis = null;
		dos = null;
		s = null;
		isConnected = false;
	}

	void fail(String msg)
	{
		close(false);
		Log.log("fail: " + msg);
		throw new RuntimeException(msg);
	}

	// public String getAET()
	// {
	// return AET;
	// }
	public Role[] getContextMap()
	{
		return contextMap;
	}

	public RoleMap getMaximalRoleMap()
	{
		return maximalRoleMap;
	}

	public int getMaxPDU()
	{
		return maxPDU;
	}

	public short getNextMsgID()
	{
		return (short) nextMsgID++;
	}

	public byte[] getPDUBytes()
	{
		return pduBytes;
	}

	public DataInputStream getPDUInputStream() throws Exception
	{
		return new DataInputStream(pduBAIS);
	}

	public DataOutputStream getPDUOutputStream() throws Exception
	{
		pduBAOS.reset();
		return new DataOutputStream(pduBAOS);
	}

	// wrap
	public int getPresContextID(UID uid)
	{
		if (uid == null)
		{
			throw new RuntimeException("No pres context for null UID");
		}
		Role r = roleMap.get(uid);
		if (r != null)
		{
			return r.getPresContextID();
		}
		throw new RuntimeException("No pres context for " + uid.getUID());
	}

	public String getDestAE()
	{
		return destAE;
	}

	public void setDestAE(String ae)
	{
		this.destAE = ae;
	}

	public InetSocketAddress getRemoteSocketAddress()
	{
		return remoteInetSocketAddress;
	}

	public RoleMap getRoleMap()
	{
		return roleMap;
	}

	private DataInputStream getUncompressedPduStream()
	{
		byte[] b;
		if (compressDataFlag)
		{
			try
			{
				dis = CompressionStream.read(dis);
			}
			catch (Exception e)
			{
				return null;
			}
		}
		return dis;
	}

	UID getXferSyntax(int id)
	{
		return contextMap[id].getSyntax();
	}

	UID getXferSyntax(UID uid)
	{
		return roleMap.get(uid).getSyntax();
	}

	public boolean handleSessionPDU(PDU pdu) throws Exception
	{
		int type = pdu.getType();
		Log.vvlog("handleSessionPDU " + PDU.getPDUName(type));
		switch (type)
		{
			case PDU.A_ASSOCIATE_RQ:
			case PDU.A_ASSOCIATE_AC:
			case PDU.A_ASSOCIATE_RJ:
				throw new Exception("unexpected PDU type " + type);
			case PDU.A_RELEASE_RQ:
				Log.vvlog("got release rq !!!!!");
				ReleaseResponse rr = new ReleaseResponse(getPDUOutputStream());
				writePDU(rr);
				break;
			case PDU.A_RELEASE_RP:
				Log.vvlog("got release rp !!!!!");
				break;
			case PDU.A_ABORT:
				Log.log("got abort !!!!!");
				break;
			default:
				throw new Exception("bad PDU Type " + type);
		}
		return true;
	}

	public boolean isConnected()
	{
		return isConnected;
	}

	public void logAccess(String op, String arg)
	{
		TSLog.get().log(op, destAE.trim(), remoteInetSocketAddress.toString(), arg);
	}

	private void logMessage(Message msg)
	{
		if (Log.vvv())
		{
			File tempFile = null;
			Log.vvvlog(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> MSG " + PDU.getMsgName(msg.getCmd().getUnsignedShort(Tag.CommandField)) + " ("
					+ msg.getCmd().getUnsignedShort(Tag.CommandField) + ") >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			Log.vvvlog("CMD=" + msg.getCmd().toString());
			try
			{
				if (Log.vvv())
				{
					if (msg.hasDataSet())
					{
						if (msg.getDataSet() != null)
						{
							Log.vvvlog("dataSet=" + msg.getDataSet().toString());
						}
						else if (msg.getDataFile() != null)
						{
							DS ds = new DS(msg.getDataFile());
							Log.vvvlog("dataSet=" + msg.getDataSet().toString());
						}
						else if (msg.getFileInputStream() != null)
						{
							FileOutputStream fos = null;
							InputStream fis = null;
							try
							{
								tempFile = SSStore.get().createTempFile(".writeMsgTmp");
								fos = new FileOutputStream(tempFile);
								Util.copyStream(msg.getFileInputStream(), fos);
								fos.close();
								msg.setFileInputStream(new FileInputStream(tempFile));
								fis = new FileInputStream(tempFile);
								DS ds = new DS(fis, UID.ImplicitVRLittleEndian); // always
								// ImplicitVRLittleEndian
								Log.vvvlog("dataSet=" + ds.toString());
							}
							finally
							{
								Util.safeClose(fos);
								Util.safeClose(fis);
							}
						}
						else
						{
							Log.vvvlog("dataSet: =NULL");
						}
					}
					else
					{
						Log.vvvlog("dataSet: noDataSet");
					}
				}
			}
			catch (Exception e)
			{
				Log.log("Log dataset caught", e);
			}
			finally
			{
				Util.safeDelete(tempFile);
			}
		}
	}

	public Message readMessage()
	{
		if (!isConnected)
		{
			return null;
		}
		Message data = new Message(this);
		try
		{
			for (;;)
			{
				PDU pdu = readPDU();
				if (pdu == null)
				{
					close(false);
					return null;
				}
				Log.vvvlog("readPDU " + PDU.getPDUName(pdu.getType()));
				if (pdu.getType() == PDU.P_DATA_TF)
				{
					boolean done = data.addPDU((DataTransfer) pdu);
					if (done)
					{
						break;
					}
				}
				else
				{
					if (handleSessionPDU(pdu) == true)
					{
						close(false);
						break;
					}
				}
			}
		}
		catch (Exception e)
		{
			Log.log("", e);
			fail("readData caught " + e);
			if (data != null)
			{
				data.close();
			}
			data = null;
		}
		if (!isConnected)
		{
			data = null;
		}
		return data;
	}

	public PDU readPDU() throws Exception
	{
		DataInputStream d = getUncompressedPduStream();
		if (d == null)
			return null;
		PDU pdu = null;
		int type = d.read();
		if (type == -1)
		{
			return null;
		}
		// admin hack
		if (type == 'X')
		{
			return null;
		}
		// create raw pdu
		if (type == 'Y')
		{
			Log.vlog("raw PDU");
			return new PDU();
		}
		d.skipBytes(1);
		int pduLen = d.readInt();
		if (pduLen > InternalMaxPDU)
		{
			throw new RuntimeException("pdu too long " + pduLen);
		}
		Log.vvvlog("READ pduLen =" + pduLen + " max=" + maxPDU);
		d.readFully(pduBytes, 0, pduLen);
		if (Log.vvv() && (pduLen < 512 || type != PDU.P_DATA_TF))
		{
			Log.vvvlog("==== READ PDU=======================================================");
			Log.vvvlog(Util.dumpBytesToString(pduBytes, 0, Math.min(2048, pduLen)));
		}
		pduBAIS.reset(pduLen);
		DataInputStream is = getPDUInputStream();
		switch (type)
		{
			case PDU.A_ASSOCIATE_RQ:
				pdu = new AssociateRequest(is);
				break;
			case PDU.A_ASSOCIATE_AC:
				pdu = new AssociateAccept(is);
				break;
			case PDU.A_ASSOCIATE_RJ:
				pdu = new AssociateReject(is);
				break;
			case PDU.P_DATA_TF:
				pdu = new DataTransfer(is);
				break;
			case PDU.A_RELEASE_RQ:
				pdu = new ReleaseRequest(is);
				break;
			case PDU.A_RELEASE_RP:
				pdu = new ReleaseResponse(is);
				break;
			case PDU.A_ABORT:
				pdu = new Abort(is);
				break;
			default:
				throw new Exception("bad PDU Type " + type);
		}
		return pdu;
	}

	public void release() throws Exception
	{
		if (!isConnected)
		{
			return;
		}
		ReleaseRequest rr = new ReleaseRequest(getPDUOutputStream());
		writePDU(rr);
		// set timeout for release resp
		s.setSoTimeout(10000);
		PDU pdu = readPDU();
		isConnected = false;
	}

	// public void setAET(String AET)
	// {
	// this.AET = AET;
	// }
	public void setCompressDataFlag(boolean b)
	{
		compressDataFlag = b;
	}

	public void setConnected(boolean connected)
	{
		isConnected = connected;
	}

	public void setMaxPDU(int maxPDU)
	{
		Util.log("setMaxPDU " + maxPDU + " old =" + this.maxPDU);
		if (maxPDU == 0)
		{
			maxPDU = InternalMaxPDU;
			Util.log("setMaxPDU 0 -- USING INTERNAL MAXPDU ");
		}
		if (maxPDU <= 512 || (maxPDU & 1) == 1)
		{
			throw new RuntimeException("bad pdu len" + maxPDU);
		}
		if (maxPDU < InternalMaxPDU)
		{
			this.maxPDU = maxPDU;
		}
	}

	protected void setStreams() throws IOException
	{
		is = s.getInputStream();
		os = s.getOutputStream();
		dis = new DataInputStream(is);
		dos = new DataOutputStream(os);
	}

	private void writeCompressedPduStream(byte[] output) throws Exception
	{
		if (compressDataFlag)
		{
			int count = CompressionStream.write(output, dos);
			int saved = output.length - count;
			Double percent = new Double(saved * 100 / output.length);
			Log.log("Compressed send of " + output.length + " bytes, saved: " + saved + " bytes, " + percent + "%");
		}
		else
		{
			dos.write(output);
		}
		dos.flush();
	}

	public void writeMessage(Cmd m) throws Exception
	{
		Message msg = new Message(this, m);
		writeMessage(msg);
	}

	public void writeMessage(Cmd m, DS ds) throws Exception
	{
		Message msg = new Message(this, m, ds);
		writeMessage(msg);
	}

	public void writeMessage(Cmd m, File f) throws Exception
	{
		Message msg = new Message(this, m, f);
		writeMessage(msg);
	};

	public void writeMessage(Cmd m, InputStream is) throws Exception
	{
		Message msg = new Message(this, m, is);
		writeMessage(msg);
	}

	private void writeMessageStream(DataOutputStream dbos, ByteArrayOutputStream bos) throws Exception
	{
		dbos.flush();
		writeCompressedPduStream(bos.toByteArray());
		bos.reset();
	}

	private void writeMessageStream(ByteArrayOutputStream bos) throws Exception
	{
		writeCompressedPduStream(bos.toByteArray());
		bos.reset();
	}

	public void writeMessageOld(Message msg)
	{
		if (!isConnected)
		{
			return;
		}
		try
		{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			DataOutputStream dbos = new DataOutputStream(bos);
			logMessage(msg);
			byte b[] = msg.getCommandBytes();
			dbos.write(0x04);
			dbos.write(0);
			dbos.writeInt(4 + 1 + 1 + b.length);
			dbos.writeInt(1 + 1 + b.length);
			int presContext = msg.getPresContextID();
			if (presContext == -1)
			{
				presContext = getPresContextID(UID.get(msg.getCmd().getString(Tag.AffectedSOPClassUID)));
			}
			msg.setPresContextID(presContext); // !!! sloppy clean this up
			dbos.write(presContext);
			dbos.write(DataTransfer.CMD_MASK + DataTransfer.DONE_MASK);
			dbos.write(b);
			if (Log.vvv())
			{
				Log.vvvlog("==== WRITE MSG CMD PDU=======================================================");
				Log.vvvlog("0x04,0x00,int=" + (4 + 1 + 1 + b.length) + ",int=" + (1 + 1 + b.length) + ",pcb=" + presContext + ",flagsb="
						+ (DataTransfer.CMD_MASK + DataTransfer.DONE_MASK));
				Log.vvvlog(Util.dumpBytesToString(b, 0, Math.min(2048, b.length)));
			}
			writeMessageStream(dbos, bos);
			if (msg.hasDataSet())
			{
				if (!msg.isDataInFile())
				{
					int maxPayload = getMaxPDU() - (1 + 1 + 4 + 4 + 1 + 1);
					b = msg.getDataBytes();
					int pos = 0;
					int togo = b != null ? b.length : 0;
					while (togo > 0)
					{ // will there be need to send zero length dataset?
						int cnt = togo > maxPayload ? maxPayload : togo;
						togo -= cnt;
						// could try to read here... for abort
						dbos.write(0x04);
						dbos.write(0);
						dbos.writeInt(4 + 1 + 1 + cnt);
						dbos.writeInt(1 + 1 + cnt);
						dbos.write(presContext);
						dbos.write(togo == 0 ? DataTransfer.DONE_MASK : 0);
						dbos.write(b, pos, cnt);
						pos += cnt;
						writeMessageStream(dbos, bos);
					}
				}
				else
				{
					File f = msg.getDataFile();
					InputStream fis = f != null ? new FileInputStream(f) : msg.getFileInputStream();
					assert fis != null;
					int maxPayload = getMaxPDU() - (1 + 1 + 4 + 4 + 1 + 1);
					byte buffer[] = new byte[maxPayload];
					try
					{
						for (;;)
						{
							int cnt = fis.read(buffer);
							if (cnt == -1)
							{
								cnt = 0;
							}
							dbos.write(0x04);
							dbos.write(0);
							dbos.writeInt(4 + 1 + 1 + cnt);
							dbos.writeInt(1 + 1 + cnt);
							dbos.write(presContext);
							dbos.write(cnt != maxPayload ? DataTransfer.DONE_MASK : 0);
							dbos.write(buffer, 0, cnt);
							writeMessageStream(dbos, bos);
							if (cnt != maxPayload)
							{
								break;
							}
						}
					}
					finally
					{
						fis.close();
					}
				}
			}
		}
		catch (Exception e)
		{
			Log.log("", e);
			fail("writeData caught " + e);
		}
	}

	private void writeMessageStream(byte[] b, int start, int length, int presContext, int cmd) throws Exception
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dbos = new DataOutputStream(bos);
		dbos.write(0x04);
		dbos.write(0);
		dbos.writeInt(4 + 1 + 1 + length);
		dbos.writeInt(1 + 1 + length);
		dbos.write(presContext);
		dbos.write(cmd);
		dbos.write(b, start, length);
		if (Log.vvv())
		{
			Log.vvvlog("==== WRITE MSG CMD PDU=======================================================");
			Log.vvvlog("0x04,0x00,int=" + (4 + 1 + 1 + length) + ",int=" + (1 + 1 + b.length) + ",pcb=" + presContext + ",flagsb=" + cmd);
			Log.vvvlog(Util.dumpBytesToString(b, 0, Math.min(5096, length)));
		}
		dbos.flush();
		writeMessageStream(bos);
		dbos.close();
		bos.close();
	}

	public void writeMessage(Message msg)
	{
		if (!isConnected)
		{
			return;
		}
		try
		{
			if (msg.getPresContextID() == -1)
			{
				msg.setPresContextID(getPresContextID(UID.get(msg.getCmd().getString(Tag.AffectedSOPClassUID))));
			}
			byte b[] = msg.getCommandBytes();
			logMessage(msg);
			writeMessageStream(b, 0, b.length, msg.getPresContextID(), DataTransfer.CMD_MASK + DataTransfer.DONE_MASK);
			if (msg.hasDataSet())
			{
				try
				{
					DS ds = null;
					if (!msg.isDataInFile())
					{
						ds = msg.getDataSet();
					}
					else
					{
						UID syntax = UID.ImplicitVRLittleEndian;
						ds = DSInputStream.readFrom(msg.getFileInputStream(), syntax, true);
					}
					XByteArrayOutputStream baos = new XByteArrayOutputStream();
					Log.log("Writing message, syntax = " + msg.getSyntax().toString());
					ds.writeTo(baos, msg.getSyntax());
					b = baos.toByteArray();
				}
				catch (Exception e)
				{
					Log.log("Error writing message: ", e);
				}
				int maxPayload = getMaxPDU() - (1 + 1 + 4 + 4 + 1 + 1);
				int pos = 0;
				int togo = b != null ? b.length : 0;
				while (togo > 0)
				{
					int cnt = togo > maxPayload ? maxPayload : togo;
					togo -= cnt;
					if(cnt % 2 != 0)
						System.out.println("ODD COUNT!!!!!!!!!!!!!!!!!!!!!!!!!");
					int done = togo == 0 ? DataTransfer.DONE_MASK : 0;
					writeMessageStream(b, pos, cnt, msg.getPresContextID(), done);
					pos += cnt;
				}
			}
		}
		catch (Exception e)
		{
			Log.log("", e);
			fail("writeData caught " + e);
		}
	}

	public void writePDU(PDU pdu) throws Exception
	{
		Log.vvvlog("WRITE pduLen =" + pduBAOS.size());
		if (Log.vvv() && (pduBAOS.size() < 512 || pdu.getType() != PDU.P_DATA_TF))
		{
			Log.vvvlog("==== WRITE PDU=======================================================");
			Log.vvvlog(Util.dumpBytesToString(pduBytes, 0, Math.min(2048, pduBAOS.size())));
		}
		ByteArrayOutputStream bos = new ByteArrayOutputStream(6 + pduBAOS.size());
		DataOutputStream dbos = new DataOutputStream(bos);
		dbos.write(pdu.getType());
		dbos.write(0); // skip byte according to DICOM spec. 3.8
		dbos.writeInt(pduBAOS.size());
		dbos.write(pduBAOS.getBuf(), 0, pduBAOS.size());
		dbos.flush();
		writeCompressedPduStream(bos.toByteArray());
	}

	public void xxxxassignContextIDs(RoleMap roleMap)
	{
		int id = 1;
		contextMap[0] = null;
		Iterator iter = roleMap.iterator();
		while (iter.hasNext())
		{
			Role r = (Role) iter.next();
			contextMap[id] = r;
			r.setPresContextID(id++);
		}
		for (; id < 256; id++)
		{
			contextMap[id] = null;
		}
	}
}
