package net.metafusion.pdu;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Iterator;
import net.metafusion.util.Role;
import net.metafusion.util.RoleMap;
import acme.util.Log;

public class AssociateAccept extends PDU
{
	static void log(String s)
	{
		acme.util.Log.log(s);
	}

	static void pdu(String s)
	{
		acme.util.Log.vvlog(s);
	}

	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}
	int version;

	public AssociateAccept(DataInputStream is) throws Exception
	{
		super(A_ASSOCIATE_AC, is);
		pdu("<<<<<<<<<<<<<<<<<<<<<<<<< AssociateAccept PDU <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
		version = is.readShort();
		pdu("version " + version);
		is.skip(2 + 16 + 16 + 32);
		// itemList = new ItemList(b);
		// Buffer b = buffer;
		// /buffer.addShort((short)version);
		// buffer.addZero(2+16+16+32);
		// itemList = new ItemList(buffer);
		for (;;)
		{
			int type = is.read();
			if (type == -1) break;
			is.skip(1);
			int len = is.readShort();
			byte dataBytes[] = new byte[len];
			is.readFully(dataBytes);
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(dataBytes));
			if (type == 0) break;
			if (type == 0x10)
			{
				appContextRequest = net.metafusion.util.UID.get(getString(dis, len).trim());
				pdu("read appContextRequest =" + appContextRequest);
			} else if (type == 0x21)
				parseContext(dis);
			else if (type == 0x50) parseUserInformation(dis);
		}
	}
	net.metafusion.util.UID appContextRequest; // UID.DICOMApplicationContextName

	void parseContext(DataInputStream dis) throws Exception
	{
		int id = dis.read();
		dis.skip(1);
		int result = dis.read();
		dis.skip(1);
		if (result == 0)
		{
			int type = dis.read();
			dis.skip(1);
			String s = getShortString(dis);
			transferSyntax[id] = net.metafusion.util.UID.get(s);
			pdu("read accept id=" + id + " type=" + type + " ts=" + transferSyntax[id]);
		}
	}
	int maxLen = 8192;
	int asyncMaxInvoke;
	int asyncMaxPerform;
	net.metafusion.util.UID impClassUID;
	net.metafusion.util.UID roleUID;
	int userRole;
	int provRole;
	String impName;
	RoleMap roleMap = new RoleMap();

	public RoleMap getRoleMap()
	{
		return roleMap;
	}

	void parseUserInformation(DataInputStream dis) throws Exception
	{
		for (;;)
		{
			int type = dis.read();
			if (type == -1) break;
			dis.skip(1);
			int len = dis.readShort();
			byte dataBytes[] = new byte[len];
			dis.readFully(dataBytes);
			DataInputStream ddis = new DataInputStream(new ByteArrayInputStream(dataBytes));
			switch (type)
			{
				case 0x51:
				{
					maxLen = ddis.readInt();
					pdu("read userinfo maxLen=" + maxLen);
					break;
				}
				case 0x52:
				{
					impClassUID = net.metafusion.util.UID.get(getString(ddis, len));
					pdu("read impClassUID=" + impClassUID);
					break;
				}
				case 0x53:
				{
					asyncMaxInvoke = ddis.readShort();
					asyncMaxPerform = ddis.readShort();
					pdu("read asyncMaxInvoke=" + asyncMaxInvoke + " asyncMaxPerform=" + asyncMaxPerform);
					break;
				}
				case 0x54:
				{
					roleUID = net.metafusion.util.UID.get(getShortString(ddis));
					userRole = ddis.read();
					provRole = ddis.read();
					roleMap.add(new Role(roleUID, (net.metafusion.util.UID) null, userRole != 0, provRole != 0));
					pdu("read roleUID=" + roleUID + " userRole=" + userRole + " provRole" + provRole);
					break;
				}
				case 0x55:
				{
					impName = getString(ddis, len);
					pdu("read impName=" + impName);
					break;
				}
				default:
				{
					Log.log("Unknown PDU user info: " + type + "[" + len + "]");
				}
			}
		}
	}

	public net.metafusion.util.UID getAppContextRequest()
	{
		return appContextRequest;
	}

	public int getMaxPDULen()
	{
		return maxLen;
	}

	public int getAsyncMaxInvoke()
	{
		return asyncMaxInvoke;
	}

	public int getAsyncMaxPerform()
	{
		return asyncMaxPerform;
	}

	public net.metafusion.util.UID getImpClassUID()
	{
		return impClassUID;
	}

	public net.metafusion.util.UID getRoleUID()
	{
		return roleUID;
	}

	public int getUserRole()
	{
		return userRole;
	}

	public int getProvRole()
	{
		return provRole;
	}

	public String getImpName()
	{
		return impName;
	}

	public net.metafusion.util.UID[] getAbstractSyntax()
	{
		return abstractSyntax;
	}

	public net.metafusion.util.UID[] getTransferSyntax()
	{
		return transferSyntax;
	}
	net.metafusion.util.UID abstractSyntax[] = new net.metafusion.util.UID[256];
	net.metafusion.util.UID transferSyntax[] = new net.metafusion.util.UID[256];

	public AssociateAccept(DataOutputStream os, int version, String aeCalled, String aeCalling, String buffer32) throws Exception
	{
		super(A_ASSOCIATE_AC, os);
		pdu(">>>>>>>>>>>>>>>>>>>>>>>>> A_ASSOCIATE_AC PDU >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		pdu("write version=" + version + " aeCalled=" + aeCalled + " aeCalling=" + aeCalling + " buffer=" + buffer32);
		os.writeShort((short) version);
		os.writeShort((short) 0);
		addString(os, aeCalled, 16);
		addString(os, aeCalling, 16);
		addString(os, buffer32, 32);
	}

	public void addAppContext(String uid) throws Exception
	{
		pdu("write addAppContext=" + uid);
		addTypedString(os, 0x10, uid);
	}

	public void addPresContext(int id, int resOrReas, String xferSyntax) throws Exception
	{
		pdu("write addPresContext id=" + id + " resOrRead=" + resOrReas + " xferSyntax=" + xferSyntax);
		os.write((byte) 0x21);
		os.write((byte) 0);
		int size = 1 + 1 + 1 + 1;
		if (xferSyntax != null) size += 4 + xferSyntax.length();
		os.writeShort((short) size);
		os.write((byte) id);
		os.write((byte) 0);
		os.write((byte) resOrReas);
		os.write((byte) 0);
		if (xferSyntax != null) addTypedString(os, 0x40, xferSyntax);
	}

	// add SCP/SCU role selection?
	public void addUserData(int maxPDULen, String impClassUID, int maxAsyncInvoke, int maxAsyncPerform, String impVersionName, RoleMap roles) throws Exception
	{
		pdu("write addUserData maxPDULen=" + maxPDULen + " impClassUID=" + impClassUID + " maxAsyncInvoke=" + maxAsyncInvoke + " impVersionName=" + impVersionName);
		os.write((byte) 0x50);
		os.write((byte) 0);
		int size = 1 + 1 + 2 + 4 + 1 + 1 + 2 + 2 + 2;
		size += 4 + impClassUID.length();
		size += 4 + impVersionName.length();
		Iterator iter = roles.iterator();
		while (iter.hasNext())
		{
			Role role = (Role) iter.next();
			pdu("write Role1 role=" + role.getUID().getUID());
			size += 1 + 1 + 2 + 2 + role.getUID().getUID().length() + 1 + 1;
		}
		os.writeShort((short) size);
		os.write((byte) 0x51);
		os.write((byte) 0);
		os.writeShort((short) 4);
		os.writeInt(maxPDULen);
		addTypedString(os, 0x52, impClassUID);
		os.write((byte) 0x53);
		os.write((byte) 0);
		os.writeShort((short) 4);
		os.writeShort((short) maxAsyncInvoke);
		os.writeShort((short) maxAsyncPerform);
		iter = roles.iterator();
		while (iter.hasNext())
		{
			Role role = (Role) iter.next();
			pdu("write Role2 role=" + role.getUID().getUID() + " isUser" + role.isUser() + " isProvider=" + role.isProvider());
			os.write(0x54);
			os.write(0);
			os.writeShort(2 + role.getUID().getUID().length() + 1 + 1);
			os.writeShort(role.getUID().getUID().length());
			os.write(role.getUID().getUID().getBytes());
			os.write(role.isUser() ? 1 : 0);
			os.write(role.isProvider() ? 1 : 0);
		}
		addTypedString(os, 0x55, impVersionName);
	}
	/*
	 * public void addUserData(int maxPDU, List roles) throws Exception {
	 * os.write((byte)0x50); os.write((byte)0); int size = 1+1+2+4; Iterator
	 * iter = roles.iterator(); while (iter.hasNext()) { Role role =
	 * (Role)iter.next(); size += 1+1+2+2+role.getUid().getUID().length()+1+1; }
	 * os.write((short)size); os.write((byte)0x51); os.write((byte)0);
	 * os.write((short)4); os.writeInt(maxPDU); iter = roles.iterator(); while
	 * (iter.hasNext()) { Role role = (Role)iter.next(); os.write(0x54);
	 * os.write(0); os.writeShort(2+role.getUid().getUID().length()+1+1);
	 * os.writeShort(role.getUid().getUID().length());
	 * os.write(role.getUid().getUID().getBytes()); os.write(role.isUser()?1:0);
	 * os.write(role.isProvider()?1:0); } }
	 */
}
