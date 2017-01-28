package net.metafusion.msg;

import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSViewDef;
import net.metafusion.util.Message;
import net.metafusion.util.Tag;

public class CStoreReqForMove extends Cmd
{
	public CStoreReqForMove()
	{
		super(d);
	}

	public CStoreReqForMove(DS ds) throws Exception
	{
		super(d, ds);
	}

	public CStoreReqForMove(Message m) throws Exception
	{
		super(d, m.getCmd());
	}
	public short Priority;
	public String AffectedSOPInstanceUID;
	public String MoveOriginatorAET;
	public short MoveOriginatorMessageID;
	public static DSViewDef d = new DSViewDef(CStoreReqForMove.class, new Tag[] { Tag.AffectedSOPClassUID, Tag.CommandField, Tag.MessageID, Tag.AffectedSOPInstanceUID,
			Tag.Priority, Tag.MoveOriginatorAET, Tag.MoveOriginatorMessageID, Tag.DataSetType });
}