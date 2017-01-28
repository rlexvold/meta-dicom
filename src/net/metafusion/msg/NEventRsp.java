package net.metafusion.msg;

import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSViewDef;
import net.metafusion.util.Tag;

public class NEventRsp extends Cmd
{
	public NEventRsp()
	{
		super(d);
	}

	public NEventRsp(DS ds) throws Exception
	{
		super(d, ds);
	}
	public String AffectedSOPInstanceUID;
	public static DSViewDef d = new DSViewDef(NEventRsp.class, new Tag[] { Tag.AffectedSOPClassUID, Tag.CommandField, Tag.MessageIDToBeingRespondedTo, Tag.DataSetType, Tag.Status,
			Tag.AffectedSOPInstanceUID });
}
