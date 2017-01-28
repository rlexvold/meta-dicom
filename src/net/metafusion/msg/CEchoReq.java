package net.metafusion.msg;

import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSViewDef;
import net.metafusion.util.Message;
import net.metafusion.util.Tag;

public class CEchoReq extends Cmd
{
	public CEchoReq()
	{
		super(d);
	}

	public CEchoReq(DS ds) throws Exception
	{
		super(d, ds);
	}

	public CEchoReq(Message m) throws Exception
	{
		super(d, m.getCmd());
	}
	public static DSViewDef d = new DSViewDef(CEchoReq.class, new Tag[] { Tag.AffectedSOPClassUID, Tag.CommandField, Tag.MessageID, Tag.DataSetType });
}
