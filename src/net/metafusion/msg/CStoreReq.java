package net.metafusion.msg;

import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSViewDef;
import net.metafusion.util.Message;
import net.metafusion.util.Tag;

public class CStoreReq extends Cmd
{
	public CStoreReq()
	{
		super(d);
	}

	public CStoreReq(DS ds) throws Exception
	{
		super(d, ds);
	}

	public CStoreReq(Message m) throws Exception
	{
		super(d, m.getCmd());
	}
	public short Priority;
	public String AffectedSOPInstanceUID;
	public static DSViewDef d = new DSViewDef(CStoreReq.class, new Tag[] { Tag.AffectedSOPClassUID, Tag.CommandField, Tag.MessageID, Tag.AffectedSOPInstanceUID, Tag.Priority,
	// Tag.MoveOriginatorAET,
			// Tag.MoveOriginatorMessageID,
			Tag.DataSetType });
}