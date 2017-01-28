package net.metafusion.pdu;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.metafusion.net.DicomSession;
import net.metafusion.util.Role;
import net.metafusion.util.RoleMap;
import net.metafusion.util.UID;
import acme.util.Log;

public class AssociateRequest extends PDU
{
	UID				abstractSyntax[]		= new UID[256];
	UID				appContext;								// UID.DICOMApplicationContextName
	int				asyncMaxInvoke;
	int				asyncMaxPerform;
	String			buffer32;
	String			calledEntity;
	String			callingEntity;
	UID				impClassUID;
	String			impName;
	int				maxPDULenRequest		= 32768;
	RoleMap			roles					= new RoleMap();
	UID				roleUID;
	DicomSession	session;
	List			transferSyntaxList[]	= new List[256];	// of UID
	int				version;

	// public static enum ContextTypes {APPLICATION_CONTEXT=0x10,
	// PRESENTATION_CONTEXT=0x20, ABSTRACT_SYNTAX=0x30};
	static void log(String s)
	{
		Log.log(s);
	}

	static void pdu(String s)
	{
		Log.vvlog(s);
	}

	public AssociateRequest(DataInputStream is) throws Exception
	{
		super(A_ASSOCIATE_RQ, is);
		Log.vvlog("<<<<<<<<<<<<<<<<<<<<<<<<< AssociateRequest PDU <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		version = is.readShort();
		is.skip(2);
		calledEntity = getString(is, 16);
		callingEntity = getString(is, 16);
		buffer32 = getString(is, 32).trim();
		Log.vvlog("read version=" + version + " calledEntity=" + calledEntity + " callingEntity=" + callingEntity + " buffer32=" + buffer32);
		for (;;)
		{
			int type = is.read();
			if (type == -1)
				break;
			is.skip(1);
			int len = is.readShort();
			byte dataBytes[] = new byte[len];
			is.readFully(dataBytes);
			if (type == 0)
				break;
			if (type == 0x10)
			{
				appContext = UID.get(new String(dataBytes).trim());
				Log.vvlog("read appContext=" + appContext);
			}
			else if (type == 0x20)
				parseContext(new DataInputStream(new ByteArrayInputStream(dataBytes)));
			else if (type == 0x50)
				parseUserInformation(new DataInputStream(new ByteArrayInputStream(dataBytes)));
		}
	}

	public AssociateRequest(DicomSession s, DataOutputStream os, int version, String aeCalled, String aeCalling) throws Exception
	{
		super(A_ASSOCIATE_RQ, os);
		pdu(">>>>>>>>>>>>>>>>>>>>>>>>> AssociateRequest PDU >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		pdu("write version=" + version + " aeCalled=" + aeCalled + " aeCalling=" + aeCalling);
		session = s;
		os.writeShort((short) version);
		os.writeShort(0);
		// nb: flip-flop
		addString(os, aeCalling, 16);
		addString(os, aeCalled, 16);
		for (int i = 0; i < 32; i++)
			os.write(0);
	}

	public void addAppContext(String uid) throws Exception
	{
		pdu("write addAppContext=" + uid);
		addTypedString(os, 0x10, uid);
	}

	public void addPresContext(int id, int resOrReas, UID abstractSyntax, List transferSyntaxList) throws Exception
	{
		// session.syntax[id] = UID.get(abstractSyntax);
		// session.xferSyntax[id] = null;
		pdu("write addPresContext id=" + id + " abstractSyntax=" + abstractSyntax + " transferSyntaxList=" + transferSyntaxList);
		this.abstractSyntax[id] = abstractSyntax;
		this.transferSyntaxList[id] = transferSyntaxList;
		os.write((byte) 0x20);
		os.write((byte) 0);
		int size = 1 + 1 + 1 + 1;
		if (abstractSyntax != null)
			size += abstractSyntax.getUID().length() + 4;
		if (transferSyntaxList != null)
			for (int i = 0; i < transferSyntaxList.size(); i++)
				size += ((UID) transferSyntaxList.get(i)).getUID().length() + 4;
		os.writeShort((short) size);
		os.write((byte) id);
		os.write((byte) 0);
		os.write((byte) resOrReas);
		os.write((byte) 0);
		if (abstractSyntax != null)
			addTypedString(os, 0x30, abstractSyntax.getUID());
		if (transferSyntaxList != null)
			for (int i = 0; i < transferSyntaxList.size(); i++)
				addTypedString(os, 0x40, ((UID) transferSyntaxList.get(i)).getUID());
	}

	public void addPresContext(RoleMap roleMap) throws Exception
	{
		// roleMap.assignContextIDs();
		int id = 1;
		Iterator iter = roleMap.iterator();
		while (iter.hasNext())
		{
			Role r = (Role) iter.next();
			addPresContext(id++, 0, r.getUID(), r.getSyntaxList());
		}
	}

	public void addUserData(int maxPDU, RoleMap roles) throws Exception
	{
		pdu("write maxPDU=" + maxPDU);
		os.write((byte) 0x50);
		os.write((byte) 0);
		int size = 1 + 1 + 2 + 4;
		Iterator iter = roles.iterator();
		while (iter.hasNext())
		{
			Role role = (Role) iter.next();
			pdu("write role1=" + role.getUID().getUID());
			size += 1 + 1 + 2 + 2 + role.getUID().getUID().length() + 1 + 1;
		}
		os.writeShort((short) size);
		os.write((byte) 0x51);
		os.write((byte) 0);
		os.writeShort((short) 4);
		os.writeInt(maxPDU);
		iter = roles.iterator();
		while (iter.hasNext())
		{
			Role role = (Role) iter.next();
			pdu("write role2=" + role.getUID().getUID() + " role.isUser()" + role.isUser() + " isProvider" + role.isProvider());
			os.write(0x54);
			os.write(0);
			os.writeShort(2 + role.getUID().getUID().length() + 1 + 1);
			os.writeShort(role.getUID().getUID().length());
			os.write(role.getUID().getUID().getBytes());
			os.write(role.isUser() ? 1 : 0);
			os.write(role.isProvider() ? 1 : 0);
		}
	}

	public UID[] getAbstractSyntax()
	{
		return abstractSyntax;
	}

	public UID getAppContext()
	{
		return appContext;
	}

	public int getAsyncMaxInvoke()
	{
		return asyncMaxInvoke;
	}

	public int getAsyncMaxPerform()
	{
		return asyncMaxPerform;
	}

	public String getBuffer32()
	{
		return buffer32;
	}

	public String getCalledEntity()
	{
		return calledEntity;
	}

	public String getCallingEntity()
	{
		return callingEntity;
	}

	public UID getImpClassUID()
	{
		return impClassUID;
	}

	public String getImpName()
	{
		return impName;
	}

	public int getMaxPDULenRequest()
	{
		return maxPDULenRequest;
	}

	// HashMap userRole;
	public RoleMap getRoles()
	{
		return roles;
	}

	public List[] getTransferSyntaxList()
	{
		return transferSyntaxList;
	}

	public int getVersion()
	{
		return version;
	}

	public void setBuffer32(String buffer32)
	{
		this.buffer32 = buffer32;
	}

	void addRole(UID uid, int user, int provider)
	{
		// handle role
		roles.add(new Role(uid, (UID) null, user == 1, provider == 1));
	}

	void parseContext(DataInputStream is) throws IOException
	{
		int id = is.read();
		is.skip(3);
		int type = is.read();
		if (type != 0x30)
			throw new RuntimeException("bad type in context");
		is.skip(1);
		abstractSyntax[id] = UID.get(getShortString(is).trim());
		pdu("read abstractSyntax[id=" + id + "]=" + abstractSyntax[id]);
		transferSyntaxList[id] = new ArrayList();
		for (;;)
		{
			type = is.read();
			if (type == -1)
				break;
			if (type != 0x40)
				throw new RuntimeException("bad type in context");
			is.skip(1);
			UID u = UID.get(getShortString(is).trim());
			transferSyntaxList[id].add(u);
			pdu("read transferSyntaxList[id=" + id + "]=" + u.getUID());
		}
	}

	void parseUserInformation(DataInputStream is) throws IOException
	{
		for (;;)
		{
			int userRole;
			int provRole;
			int type = is.read();
			if (type == -1)
				break;
			is.skip(1);
			int len = is.readShort();
			byte dataBytes[] = new byte[len];
			is.readFully(dataBytes);
			// Util.dumpBytes(dataBytes);
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(dataBytes));
			switch (type)
			{
				case 0x51:
				{
					maxPDULenRequest = dis.readInt();
					pdu("maxPDULenRequest=" + maxPDULenRequest);
					break;
				}
				case 0x52:
				{
					impClassUID = UID.get(getString(dis, len).trim());
					pdu("impClassUID=" + impClassUID);
					break;
				}
				case 0x53:
				{
					asyncMaxInvoke = dis.readShort();
					asyncMaxPerform = dis.readShort();
					pdu("asyncMaxInvoke=" + asyncMaxInvoke + " asyncMaxPerform=" + asyncMaxPerform);
					break;
				}
				case 0x54:
				{
					roleUID = UID.get(getShortString(dis).trim());
					userRole = dis.read();
					provRole = dis.read();
					addRole(roleUID, userRole, provRole);
					pdu(" addRole=" + roleUID + " userRole=" + userRole + " provRole=" + provRole);
					break;
				}
				case 0x55:
				{
					pdu(" impName=" + impName);
					impName = getString(dis, len);
					break;
				}
				default:
					Log.log("Unknown PDU user info: " + type + "[" + len + "]");
			}
		}
	}
}
