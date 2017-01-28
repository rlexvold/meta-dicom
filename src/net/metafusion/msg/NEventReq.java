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

public class NEventReq extends Cmd
{
	public NEventReq()
	{
		super(d);
	}

	public NEventReq(DS ds) throws Exception
	{
		super(d, ds);
	}

	public NEventReq(Message m) throws Exception
	{
		super(d, m.getCmd());
	}
	public String AffectedSOPInstanceUID;
	public short EventTypeID;
	public static DSViewDef d = new DSViewDef(NEventReq.class, new Tag[] { Tag.AffectedSOPClassUID, Tag.CommandField, Tag.MessageID, Tag.DataSetType, Tag.AffectedSOPInstanceUID,
			Tag.EventTypeID });
}