package net.metafusion.msg;

import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSViewDef;
import net.metafusion.util.Message;
import net.metafusion.util.Tag;

public class CFindReq extends Cmd
{
	public CFindReq()
	{
		super(d);
	}

	public CFindReq(DS ds) throws Exception
	{
		super(d, ds);
	}

	public CFindReq(Message m) throws Exception
	{
		super(d, m.getCmd());
	}
	public short Priority;
	public static DSViewDef d = new DSViewDef(CFindReq.class, new Tag[] { Tag.AffectedSOPClassUID, Tag.CommandField, Tag.MessageID, Tag.Priority, Tag.DataSetType });
}