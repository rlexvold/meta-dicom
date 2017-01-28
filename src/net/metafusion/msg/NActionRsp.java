package net.metafusion.msg;

import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSViewDef;
import net.metafusion.util.Tag;

public class NActionRsp extends Cmd
{
	public NActionRsp()
	{
		super(d);
	}

	public NActionRsp(DS ds) throws Exception
	{
		super(d, ds);
	}
	public String AffectedSOPInstanceUID;
	public static DSViewDef d = new DSViewDef(NActionRsp.class, new Tag[] { Tag.AffectedSOPClassUID, Tag.CommandField, Tag.MessageIDToBeingRespondedTo, Tag.DataSetType,
			Tag.Status, Tag.AffectedSOPInstanceUID });
}
