package net.metafusion.net;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;

import net.metafusion.localstore.service.DicomServiceProvider;
import net.metafusion.pdu.AssociateAccept;
import net.metafusion.pdu.AssociateReject;
import net.metafusion.pdu.AssociateRequest;
import net.metafusion.pdu.PDU;
import net.metafusion.util.AE;
import net.metafusion.util.AEMap;
import net.metafusion.util.Role;
import net.metafusion.util.RoleMap;
import net.metafusion.util.UID;
import acme.storage.SSStore;
import acme.util.Log;

public class DicomClientSession extends DicomSession
{
	public DicomClientSession(RoleMap maximalRoleMap)
	{
		super(maximalRoleMap);
	}
	DicomServiceProvider	currentService	= null;

	public boolean connect(AE ae) throws Exception
	{
		try
		{
			if (ae.getZipPort() != null)
			{
				setCompressDataFlag(true);
				if (connect(ae.getName(), ae.getHostName(), ae.getZipPort(), true))
				{
					return true;
				}
			}
		}
		catch (Exception e)
		{
		}
		setCompressDataFlag(false);
		return connect(ae.getName(), ae.getHostName(), ae.getPort(), false);
	}

	public boolean connect(String aename, String host, int port) throws Exception
	{
		return connect(aename, host, port, false);
	}

	// Used to initiate communications with another DICOM server.
	public boolean connect(String aename, String host, int port, boolean compress) throws Exception
	{
		Log.vlog("========================= DicomClientSession.connect at " + new Date() + " ========================");
		Log.vlog("connectTo ae=" + aename + " host=" + host + " port=" + port);
		setDestAE(aename);
		setSourceAE(AEMap.getDefault().getName());
		acme.util.Stats.inc("tcp.connect");
		s = new Socket(host, port);
		// s.setSoLinger(true,60);
		isConnected = false;
		setStreams();
		remoteInetSocketAddress = (InetSocketAddress) s.getRemoteSocketAddress();
		AssociateRequest ar = new AssociateRequest(this, getPDUOutputStream(), 1, AEMap.getDefault().getName(), aename);
		ar.addAppContext(UID.DICOMApplicationContextName.getUID());
		Log.vvlog("maximalRoleMap=" + maximalRoleMap);
		ar.addPresContext(maximalRoleMap);
		ar.addUserData(getMaxPDU(), maximalRoleMap);
		writePDU(ar);
		PDU pdu = readPDU();
		if (pdu instanceof AssociateAccept)
		{
			AssociateAccept aa = (AssociateAccept) pdu;
			for (int i = 0; i < 256; i++)
			{
				UID syntax = ar.getAbstractSyntax()[i];
				if (syntax == null)
					continue;
				UID xferSyntax = aa.getTransferSyntax()[i];
				if (xferSyntax == null)
					continue;
				Role r = maximalRoleMap.copyRole(syntax, xferSyntax);
				if (r == null)
					continue;
				roleMap.add(r);
				r.setPresContextID(i);
				contextMap[i] = r;
				Role aaRole = aa.getRoleMap().get(r.getUID());
				if (aaRole != null)
				{
					r.setUser(aaRole.isUser());
					r.setProvider(aaRole.isProvider());
				}
			}
			Log.vvlog("accepted=" + roleMap + " maxPDU=" + aa.getMaxPDULen());
			this.setMaxPDU(aa.getMaxPDULen());
			isConnected = true;
			Log.vlog("connected");
		}
		else if (pdu instanceof AssociateReject)
		{
			Log.log("rejeced (AssociateReject)");
			close(false);
		}
		return isConnected;
	}
}
