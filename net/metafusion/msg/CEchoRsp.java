package net.metafusion.msg;

import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSViewDef;
import net.metafusion.util.Tag;

public class CEchoRsp extends Cmd
{
	public CEchoRsp()
	{
		super(d);
	}

	public CEchoRsp(DS ds) throws Exception
	{
		super(d, ds);
	}
	public static DSViewDef d = new DSViewDef(CEchoRsp.class, new Tag[] { Tag.AffectedSOPClassUID, Tag.CommandField, Tag.MessageIDToBeingRespondedTo, Tag.DataSetType, Tag.Status });
}
