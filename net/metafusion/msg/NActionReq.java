/*
 * Created by IntelliJ IDEA.
 * User: mb
 * Date: Dec 6, 2003
 * Time: 4:57:37 PM
 */
package net.metafusion.msg;

import net.metafusion.dataset.DS;
import net.metafusion.dataset.DSViewDef;
import net.metafusion.util.Message;
import net.metafusion.util.Tag;

public class NActionReq extends Cmd
{
	public NActionReq()
	{
		super(d);
	}

	public NActionReq(DS ds) throws Exception
	{
		super(d, ds);
	}

	public NActionReq(Message m) throws Exception
	{
		super(d, m.getCmd());
	}
	public String RequestedSOPClassUID;
	public String RequestedSOPInstanceUID;
	public short ActionTypeID;
	public static DSViewDef d = new DSViewDef(NActionReq.class, new Tag[] { Tag.RequestedSOPClassUID, Tag.CommandField, Tag.MessageID, Tag.DataSetType,
			Tag.RequestedSOPInstanceUID, Tag.ActionTypeID });
}