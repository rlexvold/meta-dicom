package net.metafusion.msg;

import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSViewDef;
import net.metafusion.util.Tag;

public class CMoveRsp extends Cmd
{
	public CMoveRsp()
	{
		super(d);
	}

	public CMoveRsp(DS ds) throws Exception
	{
		super(d, ds);
	}
	public short NumberOfRemainingSubOperations;
	public short NumberOfCompletedSubOperations;
	public short NumberOfFailedSubOperations;
	public short NumberOfWarningSubOperations;
	public static DSViewDef d = new DSViewDef(CMoveRsp.class, new Tag[] { Tag.AffectedSOPClassUID, Tag.CommandField, Tag.MessageIDToBeingRespondedTo, Tag.DataSetType, Tag.Status,
			Tag.NumberOfRemainingSubOperations, Tag.NumberOfCompletedSubOperations, Tag.NumberOfFailedSubOperations, Tag.NumberOfWarningSubOperations });
}
