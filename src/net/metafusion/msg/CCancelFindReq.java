package net.metafusion.msg;

import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSViewDef;
import net.metafusion.util.Tag;

public class CCancelFindReq extends Cmd
{
	public CCancelFindReq()
	{
		super(d);
	}

	public CCancelFindReq(DS ds) throws Exception
	{
		super(d, ds);
	}
	public static DSViewDef d = new DSViewDef(CCancelFindReq.class, new Tag[] { Tag.CommandField, Tag.MessageIDToBeingRespondedTo, Tag.DataSetType });
}
