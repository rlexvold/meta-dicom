package net.metafusion.msg;

import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSViewDef;
import net.metafusion.util.Tag;

public class CFindRsp extends Cmd
{
	public CFindRsp()
	{
		super(d);
	}

	public CFindRsp(DS ds) throws Exception
	{
		super(d, ds);
	}
	public static DSViewDef d = new DSViewDef(CFindRsp.class, new Tag[] { Tag.AffectedSOPClassUID, Tag.CommandField, Tag.MessageIDToBeingRespondedTo, Tag.DataSetType, Tag.Status });
}
