package net.metafusion.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import net.metafusion.dataset.DS;
import net.metafusion.localstore.service.DicomServiceProvider;
import net.metafusion.pdu.AssociateAccept;
import net.metafusion.pdu.AssociateReject;
import net.metafusion.pdu.AssociateRequest;
import net.metafusion.pdu.PDU;
import net.metafusion.util.AE;
import net.metafusion.util.AEMap;
import net.metafusion.util.Message;
import net.metafusion.util.Role;
import net.metafusion.util.UID;
import acme.util.Log;
import acme.util.ThreadSafeCounter;

public class DicomServerSession extends DicomSession implements Runnable
{
	static public ThreadSafeCounter	sessionCount	= new ThreadSafeCounter();
	DicomServiceProvider			currentService	= null;
	public final int				MAX_SESSION		= 100;
	DicomServer						server;

	public DicomServerSession(DicomServer server, Socket s, boolean compress)
	{
		super(s, server.roleMap);
		setCompressDataFlag(compress);
		this.server = server;
		remoteInetSocketAddress = (InetSocketAddress) s.getRemoteSocketAddress();
	}

	public void establish()
	{
		try
		{
			isConnected = false;
			s.setSoTimeout(60 * 30 * 1000);
			setStreams();
			PDU pdu;
			pdu = readPDU();
			if (pdu == null)
			{
				// ignore open/close with no io (alteon)
				return;
			}
			Log.vlog("========================= DicomServerSession.establish at " + new Date() + " ========================");
			if (pdu.isRaw())
			{
				handleRawRequest(s, is, os);
				return; // will fall out of session
			}
			if (!(pdu instanceof AssociateRequest))
			{
				fail("expected AssociateRequest");
			}
			AssociateRequest ar = (AssociateRequest) pdu;
			setMaxPDU(ar.getMaxPDULenRequest());
			String entity = ar.getCallingEntity();
			if (entity == null)
			{
				fail("DicomServerSession.establish() - Empty calling entity");
			}
			setDestAE(entity);
			String sourceAEName = ar.getCalledEntity();
			if (sourceAEName != null)
				setSourceAE(sourceAEName);
			else
				setSourceAE(AEMap.getDefault().getName());
			try
			{
				Integer port = 0;
				AE ae = AEMap.get(getDestAE());
				if (ae != null)
				{
					if (ae.isMobile())
					{
						String hostname = null;
						try
						{
							hostname = remoteInetSocketAddress.getAddress().getHostAddress();
							port = remoteInetSocketAddress.getPort();
						}
						catch (Throwable e)
						{
						}
						if (hostname != null)
						{
							ae.setHostName(hostname);
							ae.setMobile(true);
							AEMap.putMobile(ae);
						}
					}
				}
			}
			catch (Exception e)
			{
				Log.log("problem setting dynamic hostname for DicomServerSession", e);
			}
			if (sessionCount.get() > MAX_SESSION)
			{
				Log.log("REJECT SESSION TOO BUSY");
				AssociateReject arj = new AssociateReject(getPDUOutputStream(), AssociateReject.RESULT_TRAMSIENT, 1, AssociateReject.REASON_NO_REASON_GIVEN);
				writePDU(arj);
				close(true);
			}
			else
			{
				AssociateAccept aa = createAssociateAccept(ar);
				try
				{
					writePDU(aa);
				}
				catch (Exception e)
				{
					Log.log("writePDU 1: " + e.getMessage());
					throw e;
				}
				isConnected = true;
			}
		}
		catch (Exception e)
		{
			Log.log("connect caught " + e, e);
		}
	}

	private AssociateAccept createAssociateAccept(AssociateRequest ar) throws Exception
	{
		AssociateAccept aa = null;
		try
		{
			aa = new AssociateAccept(getPDUOutputStream(), 1, ar.getCalledEntity(), ar.getCallingEntity(), ar.getBuffer32());
		}
		catch (Exception e)
		{
			Log.log("EstablishException 1: " + e.getMessage());
			throw e;
		}
		try
		{
			aa.addAppContext(ar.getAppContext().getUID());
		}
		catch (Exception e)
		{
			Log.log("EstablishException 2: " + e.getMessage());
			throw e;
		}
		HashSet added = new HashSet();
		for (int i = 0; i < 256; i++)
		{
			UID syntax = null;
			try
			{
				syntax = ar.getAbstractSyntax()[i];
			}
			catch (Exception e)
			{
				Log.log("EstablishException 3: " + e.getMessage());
				throw e;
			}
			if (syntax == null)
			{
				continue;
			}
			List xfer = null;
			try
			{
				xfer = ar.getTransferSyntaxList()[i];
			}
			catch (Exception e)
			{
				Log.log("EstablishException 4: " + e.getMessage());
				throw e;
			}
			UID xferSyntax = null;
			try
			{
				xferSyntax = (UID) xfer.get(0);
			}
			catch (Exception e)
			{
				Log.log("EstablishException 5: " + e.getMessage());
				throw e;
			}
			if (xferSyntax == null)
			{
				continue;
			}
			Role r = null;
			for (int j = 0; j < xfer.size(); j++)
			{
				UID xf = null;
				try
				{
					xf = (UID) xfer.get(j);
				}
				catch (Exception e)
				{
					Log.log("EstablishException 6: " + e.getMessage());
					throw e;
				}
				Log.vvlog(syntax.getName() + ": " + xf.getName());
			}
			for (int j = 0; r == null && j < xfer.size(); j++)
			{
				try
				{
					r = maximalRoleMap.copyRole(syntax, xfer);
				}
				catch (Exception e)
				{
					Log.log("EstablishException 7: " + e.getMessage());
					throw e;
				}
			}
			boolean exists = false;
			String uidSyntaxString = "";
			if (r != null)
			{
				uidSyntaxString = null;
				try
				{
					uidSyntaxString = r.getUID().getUID() + ":" + r.getSyntax().getUID();
					exists = added.contains(uidSyntaxString);
				}
				catch (Exception e)
				{
					Log.log("EstablishException 8: " + e.getMessage());
					throw e;
				}
				if (exists)
				{
					Log.vvlog("fail role (for efilm) presContext exists " + uidSyntaxString);
				}
			}
			if (r == null || exists)
			{
				// always add a failure
				Log.vvlog("add fail (reason=2): " + i + " " + syntax.getName());
				aa.addPresContext(i, 2, UID.ImplicitVRLittleEndian.getUID());
			}
			else
			{
				try
				{
					added.add(uidSyntaxString);
				}
				catch (Exception e)
				{
					Log.log("EstablishException 9: " + e.getMessage());
					throw e;
				}
				try
				{
					contextMap[i] = r;
				}
				catch (Exception e)
				{
					Log.log("EstablishException 10: " + e.getMessage());
					throw e;
				}
				try
				{
					roleMap.add(r);
				}
				catch (Exception e)
				{
					Log.log("EstablishException 11: " + e.getMessage());
					throw e;
				}
				try
				{
					r.setPresContextID(i);
				}
				catch (Exception e)
				{
					Log.log("EstablishException 12: " + e.getMessage());
					throw e;
				}
				try
				{
					aa.addPresContext(i, 0, r.getSyntax().getUID());
				}
				catch (Exception e)
				{
					Log.log("EstablishException 13: " + e.getMessage());
					throw e;
				}
			}
		}
		try
		{
			aa.addUserData(getMaxPDU(), "1.1.1", 1, 1, "metafusion", roleMap);
		}
		catch (Exception e)
		{
			Log.log("EstablishException 14: " + e.getMessage());
			throw e;
		}
		return aa;
	}

	public DicomServer getServer()
	{
		return server;
	}

	protected void handleRawRequest(Socket s, InputStream is, OutputStream os) throws Exception
	{
		try
		{
			server.handleRawRequest(s, is, os);
		}
		catch (Exception e)
		{
			Log.log("handleRawRequest caught ", e);
		}
		// wait up to 30 seconds for eof
		try
		{
			s.setSoTimeout(30 * 60 * 1000);
			is.read();
		}
		catch (IOException e)
		{
			;// e.printStackTrace();
		}
		close(false);
	}

	public void run()
	{
		int count = 0;
		try
		{
			sessionCount.increment();
			establish();
			while (isConnected())
			{
				Message msg = readMessage();
				if (msg == null)
				{
					break;
				}
				count++;
				if (Log.vvv())
				{
					try
					{
						Log.vvvlog("<<<<<<<<<<<<<<<<<<<<<<<<< MSG " + PDU.getMsgName(msg.getCommandID()) + " (" + msg.getCommandID() + ") <<<<<<<<<<<<<<<<<<<<");
						if (Log.vvv())
						{
							Log.vvvlog("CMD=" + msg.getCmd().toString());
							if (msg.hasDataSet())
							{
								if (msg.getDataSet() != null)
								{
									Log.vvvlog("dataSet=" + msg.getDataSet().toString());
								}
								else if (msg.getDataFile() != null)
								{
									DS ds = new DS(msg.getDataFile());
									Log.vvvlog("dataSet=" + ds.toString());
								}
								else
								{
									Log.vvvlog("dataSet: =NULL");
								}
							}
							else
							{
								Log.vvvlog("dataSet: noDataSet");
							}
						}
					}
					catch (Exception e)
					{
						Log.log("logging dataset caught ", e);
					}
				}
				if (currentService == null)
				{
					currentService = server.getServiceProvider(msg.getCommandID(), this);
				}
				assert currentService != null;
				currentService.setCurrent(msg);
				currentService.run();
				if (currentService.isDone())
				{
					currentService = null;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.log("DicomServerSession.run " + count + ": ", e);
		}
		finally
		{
			sessionCount.decrement();
			close(false);
			Log.vlog("DicomServerSession exit: ");
		}
	}
}
