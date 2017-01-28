package net.metafusion.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import net.metafusion.Dicom;
import net.metafusion.dataset.DS;
import net.metafusion.msg.Cmd;
import net.metafusion.net.DicomSession;
import net.metafusion.pdu.DataTransfer;
import net.metafusion.pdu.PDU;
import net.metafusion.pdu.PDV;
import acme.storage.SSStore;
import acme.util.FileUtil;
import acme.util.Log;
import acme.util.Util;
import acme.util.XByteArrayInputStream;
import acme.util.XByteArrayOutputStream;

public class Message
{
	static final int	DONE			= 0, START_CMD = 1, MORE_CMD = 2, HAVE_CMD = 3, START_DATA = 4, MORE_DATA = 5, HAVE_DATA = 6;
	// 0,0x0800 -> word == 0x0101 if no data set
	static byte[]		headerBytes		= new byte[SSStore.METADATA_SIZE];
	// XByteArrayOutputStream dataStream = new XByteArrayOutputStream();
	DS					cmd;
	int					commandID;
	boolean				dataInFile;
	DS					dataSet;
	int					dataSetType;
	OutputStream		dataStream;
	File				file;
	InputStream			fileInputStream;
	int					messageID;
	int					messageIDBeingRespondedTo;
	int					presContextID	= -1;
	DicomSession		ss;
	int					state			= START_CMD;
	int					status;

	static void log(String s)
	{
		Log.log(s);
	}

	public Message(DicomSession ss)
	{
		this.ss = ss;
	}

	public Message(DicomSession ss, Cmd cmd) throws Exception
	{
		this.ss = ss;
		this.cmd = cmd.store();
		dataSetType = Dicom.COMMAND_DATASET_ABSENT;
	}

	public Message(DicomSession ss, Cmd cmd, DS data) throws Exception
	{
		this.ss = ss;
		this.cmd = cmd.store();
		this.dataSet = data;
		dataInFile = false;
		dataSetType = Dicom.COMMAND_DATASET_PRESENT;
	}

	public Message(DicomSession ss, Cmd cmd, File f) throws Exception
	{
		this.ss = ss;
		this.cmd = cmd.store();
		this.file = f;
		this.fileInputStream = new FileInputStream(f);
		dataInFile = true;
		dataSetType = Dicom.COMMAND_DATASET_PRESENT;
	}

	public Message(DicomSession ss, Cmd cmd, InputStream is) throws Exception
	{
		this.ss = ss;
		this.cmd = cmd.store();
		this.file = null;
		this.fileInputStream = is;
		dataInFile = true;
		dataSetType = Dicom.COMMAND_DATASET_PRESENT;
	}

	public Message(DicomSession ss, DS cmd)
	{
		this.ss = ss;
		this.cmd = cmd;
		dataSetType = Dicom.COMMAND_DATASET_ABSENT;
	}

	public Message(DicomSession ss, DS cmd, DS data)
	{
		this.ss = ss;
		this.cmd = cmd;
		this.dataSet = data;
		dataInFile = false;
		dataSetType = Dicom.COMMAND_DATASET_PRESENT;
	}

	public Message(DicomSession ss, DS cmd, File f)
	{
		this.ss = ss;
		this.cmd = cmd;
		this.file = f;
		dataInFile = true;
		dataSetType = Dicom.COMMAND_DATASET_PRESENT;
	}

	public boolean addPDU(DataTransfer dt) throws Exception
	{
		for (;;)
		{
			PDV pdv = dt.nextPDV();
			if (pdv == null)
				break;
			// Util.Log(""+pdv);
			state_machine(pdv);
		}
		return state == DONE;
	}

	public void close()
	{
		Util.safeClose(dataStream);
		if (dataInFile && file != null) // should not exist by now
			Util.safeDelete(file);
	}

	public DS getCmd()
	{
		return cmd;
	}

	public byte[] getCommandBytes() throws Exception
	{
		XByteArrayOutputStream baos = new XByteArrayOutputStream();
		cmd.writeTo(baos, UID.ImplicitVRLittleEndian);
		return baos.getBuf();
	}

	public DS getCommandDS() throws Exception
	{
		return cmd;
	}

	public int getCommandID()
	{
		return commandID;
	}

	public UID getSyntax()
	{
		return ss.contextMap[presContextID].getSyntax();
	}

	public byte[] getDataBytes() throws Exception
	{
		if (dataSet == null)
			return null;
		XByteArrayOutputStream baos = new XByteArrayOutputStream();
		dataSet.writeTo(baos, getSyntax());
		return baos.toByteArray(); // ,baos.getBuf();
	}

	public File getDataFile()
	{
		return file;
	}

	public DS getDataSet()
	{
		return dataSet;
	}

	public InputStream getFileInputStream()
	{
		return fileInputStream;
	}

	public int getMessageID()
	{
		return messageID;
	}

	public int getMessageIDBeingRespondedTo()
	{
		return messageIDBeingRespondedTo;
	}

	public int getPresContextID()
	{
		return presContextID;
	}

	public Message getResponse(Cmd cmd) throws Exception
	{
		Message m = new Message(ss, cmd);
		m.setPresContextID(this.getPresContextID());
		return m;
	}

	public Message getResponse(Cmd cmd, DS ds) throws Exception
	{
		Message m = new Message(ss, cmd, ds);
		m.setPresContextID(this.getPresContextID());
		return m;
	}

	public Message getResponse(Cmd cmd, File f) throws Exception
	{
		Message m = new Message(ss, cmd, f);
		m.setPresContextID(this.getPresContextID());
		return m;
	}

	public int getStatus()
	{
		return status;
	}

	public boolean hasDataSet()
	{
		return dataSetType != Dicom.COMMAND_DATASET_ABSENT;
	}

	public boolean isDataInFile()
	{
		return dataInFile;
	}

	public boolean nextPDU(PDU pdu)
	{
		return false;
	}

	public void setCmd(DS ds)
	{
		cmd = ds;
	}

	public void setDataSet(DS dataSet)
	{
		this.dataSet = dataSet;
	}

	public void setFileInputStream(InputStream is)
	{
		fileInputStream = is;
	}

	public void setPresContextID(int presContextID)
	{
		this.presContextID = presContextID;
	}

	InputStream getCommandStream() throws Exception
	{
		XByteArrayOutputStream baos = new XByteArrayOutputStream();
		cmd.writeTo(baos, UID.ImplicitVRLittleEndian);
		return new XByteArrayInputStream(baos);
	}

	InputStream getDataStream() throws Exception
	{
		if (dataSet != null)
		{
			XByteArrayOutputStream baos = new XByteArrayOutputStream();
			dataSet.writeTo(baos, getSyntax());
			return new XByteArrayInputStream(baos);
		}
		else if (dataInFile)
			return file != null ? new FileInputStream(file) : fileInputStream;
		else
			return null;
	}

	void state_machine(PDV pdv) throws Exception
	{
		XByteArrayOutputStream xbas;
		switch (state)
		{
			case START_CMD:
				presContextID = pdv.getId();
				if (ss.contextMap[presContextID].getSyntax() == null)
					throw new RuntimeException("bad presContextID");
				if (!pdv.isCmd())
					throw new RuntimeException("expected cmd pdv");
				dataStream = new XByteArrayOutputStream();
				state = MORE_CMD;
				state_machine(pdv);
				break;
			case MORE_CMD:
				if (!pdv.isCmd())
					throw new RuntimeException("expected cmd pdv");
				dataStream.write(pdv.getBytes());
				if (pdv.isDone())
				{
					state = HAVE_CMD;
					state_machine(null);
				}
				break;
			case HAVE_CMD:
				xbas = (XByteArrayOutputStream) dataStream;
				cmd = new DS(new ByteArrayInputStream(xbas.getBuf(), 0, xbas.size()), UID.ImplicitVRLittleEndian);
				messageID = cmd.getUnsignedShort(Tag.MessageID);
				messageIDBeingRespondedTo = cmd.getUnsignedShort(Tag.MessageIDToBeingRespondedTo);
				commandID = cmd.getUnsignedShort(Tag.CommandField);
				status = cmd.getUnsignedShort(Tag.Status);
				dataSetType = cmd.getUnsignedShort(Tag.DataSetType);
				if (commandID == Dicom.C_STORE_RQ)
					dataInFile = true;
				if (dataSetType == Dicom.COMMAND_DATASET_ABSENT)
					state = DONE;
				else
					state = START_DATA;
				break;
			case START_DATA:
				if (!pdv.isData())
					throw new RuntimeException("expected data pdv");
				if (dataInFile)
				{
					file = SSStore.get().createTempFile(".msgd");
					dataStream = SSStore.get().getOutputStream(file);
				}
				else
					((XByteArrayOutputStream) dataStream).reset();
				state = MORE_DATA;
				state_machine(pdv);
				break;
			case MORE_DATA:
				// Log("MORE_DATA");
				if (!pdv.isData())
					throw new RuntimeException("expected data pdv");
				dataStream.write(pdv.getBytes());
				if (pdv.isDone())
				{
					state = HAVE_DATA;
					state_machine(null);
				}
				break;
			case HAVE_DATA:
				// Log("HAVE_DATA");
				if (dataStream instanceof XByteArrayOutputStream)
				{
					xbas = (XByteArrayOutputStream) dataStream;
					dataSet = new DS(new ByteArrayInputStream(xbas.getBuf(), 0, xbas.size()), getSyntax());
				}
				if (dataStream instanceof FileOutputStream)
					FileUtil.flushAndClose((FileOutputStream) dataStream);
				else
					dataStream.close();
				state = DONE;
				break;
			case DONE:
				throw new RuntimeException("got pdv after done");
				// break;
		}
	}
}
