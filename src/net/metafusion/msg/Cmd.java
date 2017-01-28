package net.metafusion.msg;

import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSViewDef;

public class Cmd
{
	public String	AffectedSOPClassUID;
	public short	CommandField;
	public short	DataSetType;
	DSViewDef		def;
	public String	ErrorComment;
	public short	MessageID;
	public short	MessageIDToBeingRespondedTo;
	public short	Status;

	public Cmd(DSViewDef def)
	{
		this.def = def;
	}

	public Cmd(DSViewDef def, DS ds) throws Exception
	{
		this.def = def;
		def.load(ds, this);
	}

	public String getAffectedSOPClassUID()
	{
		return AffectedSOPClassUID;
	}

	public short getCommandField()
	{
		return CommandField;
	}

	public short getDataSetType()
	{
		return DataSetType;
	}

	public DSViewDef getDef()
	{
		return def;
	}

	public String getErrorComment()
	{
		return ErrorComment;
	}

	public short getMessageID()
	{
		return MessageID;
	}

	public short getMessageIDToBeingRespondedTo()
	{
		return MessageIDToBeingRespondedTo;
	}

	public short getStatus()
	{
		return Status;
	}

	public void setAffectedSOPClassUID(String affectedSOPClassUID)
	{
		AffectedSOPClassUID = affectedSOPClassUID;
	}

	public void setCommandField(short commandField)
	{
		CommandField = commandField;
	}

	public void setDataSetType(short dataSetType)
	{
		DataSetType = dataSetType;
	}

	public void setDef(DSViewDef def)
	{
		this.def = def;
	}

	public void setErrorComment(String errorMessage)
	{
		ErrorComment = errorMessage;
	}

	public void setMessageID(short messageID)
	{
		MessageID = messageID;
	}

	public void setMessageIDToBeingRespondedTo(short messageIDToBeingRespondedTo)
	{
		MessageIDToBeingRespondedTo = messageIDToBeingRespondedTo;
	}

	public void setStatus(short status)
	{
		Status = status;
	}

	public DS store() throws Exception
	{
		return def.store(this, new DS());
	}

	public DS store(DS ds) throws Exception
	{
		return def.store(this, ds);
	}
}
