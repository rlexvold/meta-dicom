package net.metafusion.dicom.message;

import java.util.Collection;
import net.metafusion.communication.AbstractConnectionInfo;
import net.metafusion.dataset.DS;
import net.metafusion.message.AbstractMessage;
import net.metafusion.message.AbstractMessageManager;
import net.metafusion.message.MessageType;
import net.metafusion.net.DicomSession;
import net.metafusion.service.CMove;
import net.metafusion.util.AE;
import net.metafusion.util.UID;

public class DicomMessageManager extends AbstractMessageManager
{
	public DicomMessageManager()
	{
		typeHandled = MessageType.DICOM;
	}

	public AbstractMessage receiveMessage()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public boolean sendMessage(AbstractMessage message)
	{
		return comHandler.write(message);
	}

}