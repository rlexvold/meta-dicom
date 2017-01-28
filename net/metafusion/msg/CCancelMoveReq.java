package net.metafusion.msg;

import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSViewDef;
import net.metafusion.util.Tag;

public class CCancelMoveReq extends Cmd
{
	public CCancelMoveReq()
	{
		super(d);
	}

	public CCancelMoveReq(DS ds) throws Exception
	{
		super(d, ds);
	}
	public static DSViewDef d = new DSViewDef(CCancelMoveReq.class, new Tag[] { Tag.CommandField, Tag.MessageIDToBeingRespondedTo, Tag.DataSetType });
}
