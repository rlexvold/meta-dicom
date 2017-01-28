package net.metafusion.msg;

import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSViewDef;
import net.metafusion.util.Message;
import net.metafusion.util.Tag;

public class CMoveReq extends Cmd
{
	public CMoveReq()
	{
		super(d);
	}

	public CMoveReq(DS ds) throws Exception
	{
		super(d, ds);
	}

	public CMoveReq(Message m) throws Exception
	{
		super(d, m.getCmd());
	}
	public short Priority;
	public String MoveDestination;
	public static DSViewDef d = new DSViewDef(CMoveReq.class, new Tag[] { Tag.AffectedSOPClassUID, Tag.CommandField, Tag.MessageID, Tag.Priority, Tag.MoveDestination,
			Tag.DataSetType });
}