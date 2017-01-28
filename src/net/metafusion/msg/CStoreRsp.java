package net.metafusion.msg;

import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSViewDef;
import net.metafusion.util.Tag;

public class CStoreRsp extends Cmd
{
	public CStoreRsp()
	{
		super(d);
	}

	public CStoreRsp(DS ds) throws Exception
	{
		super(d, ds);
	}
	public String AffectedSOPInstanceUID;
	public static DSViewDef d = new DSViewDef(CStoreRsp.class, new Tag[] { Tag.AffectedSOPClassUID, Tag.CommandField, Tag.MessageIDToBeingRespondedTo, Tag.DataSetType, Tag.Status,
			Tag.AffectedSOPInstanceUID, Tag.ErrorComment });
}
