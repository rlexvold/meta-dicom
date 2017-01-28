package net.metafusion.dicom.communication;

import java.util.ArrayList;
import net.metafusion.Dicom;
import net.metafusion.communication.socket.AbstractSocketHandler;
import net.metafusion.dataset.DS;
import net.metafusion.dicom.message.DicomMessage;
import net.metafusion.message.AbstractMessage;
import net.metafusion.net.DicomClientSession;
import net.metafusion.util.Message;
import net.metafusion.util.Tag;
import acme.util.Util;

public class DicomSocketAdapter extends AbstractSocketHandler
{
	DicomClientSession dicomSession = null;

	public DicomSocketAdapter()
	{
		super();
	}

	public AbstractMessage read(Integer timeout)
	{
		return null;
	}

	public boolean write(AbstractMessage message)
	{
		try
		{
			ArrayList<DicomMessage> results = new ArrayList<DicomMessage>();
			DicomMessage msg = (DicomMessage) message;
			DS attrSet = new DS();
			String level = "";
			msg.getCommand().setMessageID(dicomSession.getNextMsgID());
			attrSet.put(Tag.QueryRetrieveLevel, level);
			Message req = new Message(dicomSession, msg.getCommand(), attrSet);
			dicomSession.connect(msg.getServerName(), serverConnectionInfo.getHostname(), serverConnectionInfo.getPort());
			dicomSession.writeMessage(req);
			Message resp;
			boolean done = false;
			while (true)
			{
				resp = dicomSession.readMessage();
				if (resp == null)
				{
					Util.log("FAIL: no response");
					break;
				}
				switch (resp.getStatus())
				{
					case Dicom.PENDING:
						Util.log("have pend");
						DS tmpDS = resp.getDataSet();
						if (tmpDS != null)
						{
							DicomMessage tmpMsg = new DicomMessage();
							tmpMsg.setDataSet(tmpDS);
							results.add(tmpMsg);
						}
						break;
					case Dicom.SUCCESS:
						Util.log("have done");
						return true;
					default:
						Util.log("have error " + resp.getStatus());
						return false;
				}
			}
		}
		catch (Exception e)
		{
			System.out.println("DicomSocketAdapter ERROR: " + e.getMessage());
		}
		finally
		{
			dicomSession.close(true);
		}
		return false;
	}
}
